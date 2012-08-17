package de.danielmescheder.snooker.simulation.event;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme.math.Vector3f;

import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.domain.CollidableGameObject;
import de.danielmescheder.snooker.domain.PocketCorner;
import de.danielmescheder.snooker.domain.Ball.BallState;

/**
 * The PocketCornerCollision is a special case of a {@linl Collision} that is
 * executed when the ball hits the rounded corners next to the {@link Pocket}.
 * 
 */
public class PocketCornerCollision extends SingleBallEvent implements Collision {
	static final Logger logger = Logger.getLogger(PocketCornerCollision.class
			.getName());

	private PocketCorner pocketCorner;

	public PocketCornerCollision(float time, BilliardBall ball,
			PocketCorner pocketCorner) {
		super(time, ball);
		this.pocketCorner = pocketCorner;
	}

	@Override
	public void handle(Map<BilliardBall, BilliardBall> target) {
		BilliardBall ball = target.get(getBall());
		logger.log(Level.FINER, "Old ball state", ball);

		Vector3f p = ball.getState().getMotion().getPosition(ball, getTime());
		Vector3f v = ball.getState().getMotion().getVelocity(ball, getTime());
		Vector3f av = ball.getState().getMotion().getAngularVelocity(ball,
				getTime());

		Vector3f un = p.subtract(pocketCorner.getPosition()).normalize();
		Vector3f ut = new Vector3f(-un.y, un.x, un.z);

		Vector3f vn = un.mult(v.dot(un) / un.dot(un));
		Vector3f vt = ut.mult(v.dot(ut) / ut.dot(ut));

		v = vt.add(vn.mult(-1));

		ball.setPosition(p);
		ball.setVelocity(v);
		ball.setAngularVelocity(av);
		ball.setState(BallState.SLIDING);
		ball.setTime(getTime());
		logger.log(Level.FINER, "New ball state", ball);

	}

	@Override
	public Set<CollidableGameObject> getColidableObjects() {
		Set<CollidableGameObject> s = new HashSet<CollidableGameObject>(
				getBallKeys());
		s.add(getPocketCorner());
		return s;
	}

	private CollidableGameObject getPocketCorner() {
		return this.pocketCorner;
	}
}
