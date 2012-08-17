package de.danielmescheder.snooker.simulation.event;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


import com.jme.math.Vector3f;

import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.domain.CollidableGameObject;
import de.danielmescheder.snooker.domain.Ball.BallState;
import de.danielmescheder.snooker.simulation.physics.BallMotion;
import de.danielmescheder.snooker.simulation.physics.Physics;

/**
 * The BallCollision handles the setting of the new velocity, angular velocity
 * and position values after a collision between two {@link BilliardBall}s.
 *
 */
public class BallCollision extends Event implements Collision {
	public static float VELOCITY_THRESHOLD;
	private static final Logger logger = Logger.getLogger(BallCollision.class
			.getName());

	public BallCollision(float time, Set<BilliardBall> balls) {
		super(time, balls);
	}

	@Override
	public void handle(Map<BilliardBall, BilliardBall> target) {
		float time = getTime();
		BilliardBall[] b = new BilliardBall[2];
		b = getBalls(target).toArray(b);

		Vector3f[] v = new Vector3f[2];
		Vector3f[] av = new Vector3f[2];
		Vector3f[] p = new Vector3f[2];

		float[] vn = new float[2];
		float[] vt = new float[2];

		// (1) set new position and angular velocity values
		for (int i = 0; i < 2; i++) {
			BallMotion motion = b[i].getState().getMotion();
			av[i] = motion.getAngularVelocity(b[i], time);
			p[i] = motion.getPosition(b[i], time);
		}

		// (2) calculate unitNorm and tangentNorm
		Vector3f un = p[1].subtract(p[0]).normalize();
		Vector3f ut = new Vector3f(-un.y, un.x, un.z);

		// (3) get velocity components
		for (int i = 0; i < 2; i++) {
			BallMotion motion = b[i].getState().getMotion();

			v[i] = motion.getVelocity(b[i], time);
			vn[i] = un.dot(v[i]);
			vt[i] = ut.dot(v[i]);
		}

		// (4) update all the stuff
		for (int i = 0; i < 2; i++) {
			logger.log(Level.FINE, "Old ball state", b[i]);
			float newvn = (vn[i] * (b[i].getMass() - b[(i + 1) % 2].getMass()) + 2
					* b[(i + 1) % 2].getMass() * vn[(i + 1) % 2])
					/ (b[i].getMass() + b[(i + 1) % 2].getMass());
			Vector3f newVecn = un.mult(newvn);
			Vector3f newVect = ut.mult(vt[i]);

			b[i].setVelocity(newVecn.add(newVect));
			b[i].setAngularVelocity(av[i]);
			b[i].setPosition(p[i]);
			b[i].setTime(time);
			if (Math.abs(b[i].getVelocity().length()) < VELOCITY_THRESHOLD) {
				b[i].setState(BallState.RESTING);
				b[i].setAngularVelocity(Vector3f.ZERO.clone());
				b[i].setVelocity(Vector3f.ZERO.clone());
			} else if (Math.abs(Physics.getRelativeVelocity(b[i]).length()) < VELOCITY_THRESHOLD) {
				b[i].setState(BallState.ROLLING);
			} else {
				b[i].setState(BallState.SLIDING);

			}
			logger.log(Level.FINE, "New ball state", b[i]);
		}

		// check actual distance

		double distance = p[0].distance(p[1]);
		double error = (b[0].getRadius() + b[1].getRadius()) - distance;
		if (error > VELOCITY_THRESHOLD) {
			logger.log(Level.FINE, "Balls intersecting", b);
		}
	}

	@Override
	public Set<CollidableGameObject> getColidableObjects() {
		return new HashSet<CollidableGameObject>(this.getBallKeys());
	}
}
