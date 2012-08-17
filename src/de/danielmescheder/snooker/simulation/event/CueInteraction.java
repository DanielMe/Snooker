package de.danielmescheder.snooker.simulation.event;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme.math.FastMath;
import com.jme.math.Vector3f;

import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.domain.Cue;
import de.danielmescheder.snooker.domain.Ball.BallState;
import de.danielmescheder.snooker.math.XYRotationTrans3D;


/**
 * The CueInteraction generates from the parameters of the cue shot the new
 * velocity and angular velocity of the involved ball.
 * 
 */
public class CueInteraction extends SingleBallEvent {
	static final Logger logger = Logger.getLogger(CueInteraction.class
			.getName());

	private Cue cue;
	private BilliardBall ball;
	private float transX, transY, angDest, angElev, velocity, time;

	public CueInteraction(float time, Cue cue, BilliardBall ball,
			float angDest, float angElev, float transX, float transY,
			float velocity) {
		super(time, ball);
		this.time = time;
		this.cue = cue;
		this.ball = ball;
		this.transX = transX;
		this.transY = transY;
		this.angDest = angDest;
		this.angElev = angElev;
		this.velocity = velocity;
	}

	public float getTransX() {
		return transX;
	}

	public float getTransY() {
		return transY;
	}

	public float getAngDest() {
		return angDest;
	}

	public float getAngElev() {
		return angElev;
	}

	public float getVelocity() {
		return velocity;
	}

	@Override
	public String toString() {
		return super.toString() + ", a: " + transX + ", b: " + transY
				+ ", alpha: " + angDest + ", theta: " + angElev + ", v: "
				+ velocity;
	}

	@Override
	public void handle(Map<BilliardBall, BilliardBall> target) {
		for (BilliardBall b : this.getBalls(target)) {
			logger.log(Level.FINER, "Old ball state", b);

			float viableRadius = b.getRadius() * .9f;

			if (FastMath.sqr(transX) + FastMath.sqr(transY) > FastMath
					.sqr(viableRadius)) {
				float angle = FastMath.atan2(transY, transX);
				transX = viableRadius * FastMath.cos(angle);
				transY = viableRadius * FastMath.sin(angle);
			}

			float c = FastMath.sqrt(FastMath.sqr(b.getRadius())
					- FastMath.sqr(transX) - FastMath.sqr(transY));
			float fUpper = 2 * b.getMass() * velocity;
			float fLowerPart1 = 1 + (b.getMass() / cue.getMass());
			float fLowerPart2 = 5 / (2 * FastMath.sqr(b.getRadius()));
			float fLowerPart3Part1 = FastMath.sqr(transX);
			float fLowerPart3Part2 = FastMath.sqr(transY)
					* FastMath.sqr(FastMath.cos(angElev));
			float fLowerPart3Part3 = FastMath.sqr(c)
					* FastMath.sqr(FastMath.sin(angElev));
			float fLowerPart3Part4 = 2 * transY * c * FastMath.cos(angElev)
					* FastMath.sin(angElev);
			float fLowerPart3Complete = fLowerPart3Part1 + fLowerPart3Part2
					+ fLowerPart3Part3 - fLowerPart3Part4;
			float fLower = fLowerPart1 + (fLowerPart2 * fLowerPart3Complete);
			float f = fUpper / fLower;

			float i = 2f / 5f * b.getMass() * b.getRadius() * b.getRadius();

			Vector3f angVel = new Vector3f((-c * f * FastMath.sin(angElev))
					+ (transY * f * FastMath.cos(angElev)), transX * f
					* FastMath.sin(angElev), -transX * f
					* FastMath.cos(angElev));
			angVel = angVel.mult(-1f / i);
			angVel.y = -angVel.y;

			// the z component is ignored
			Vector3f v = new Vector3f(0, (f / b.getMass())
					* FastMath.cos(angElev), 0);
			XYRotationTrans3D rot = new XYRotationTrans3D(-angDest);

			b.setVelocity(rot.transform(v));

			b.setAngularVelocity(rot.transform(angVel));
			b.setState(BallState.SLIDING);
			b.setTime(this.getTime());
			logger.log(Level.FINER, "New ball state", b);
		}
	}

	public NoisyCueInteraction toNoisyCueInteraction() {
		return new NoisyCueInteraction(time, cue, (BilliardBall) ball.clone(),
				angDest, angElev, transX, transY, velocity);
	}

}
