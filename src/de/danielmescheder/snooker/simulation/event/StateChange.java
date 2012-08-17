package de.danielmescheder.snooker.simulation.event;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme.math.Vector3f;

import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.domain.Ball.BallState;
import de.danielmescheder.snooker.simulation.physics.BallMotion;


/**
 * A StateChange is an {@link Event} that, depending on the current
 * {@link BallState}, handles the transition to the next BallState and applies
 * the corresponding physics.
 * 
 * @author tim
 * 
 */
public class StateChange extends SingleBallEvent {
	static final Logger logger = Logger.getLogger(StateChange.class.getName());

	public StateChange(BilliardBall ball) {
		super(ball.getTime() + ball.getState().getMotion().getDuration(ball),
				ball);
		if (ball.getState() == BallState.SLIDING) {
			this.newState = BallState.ROLLING;
		} else if (ball.getState() == BallState.ROLLING) {
			this.newState = BallState.RESTING;
		}
	}

	@Override
	public void handle(Map<BilliardBall, BilliardBall> target) {
		BilliardBall ball = target.get(getBall());

		logger.log(Level.FINER, "Old ball state", ball);

		BallMotion motion = ball.getState().getMotion();

		// first calculate the vectors, then store them into the object... the
		// original values are still needed to calculate the new values!
		Vector3f v = motion.getVelocity(ball, getTime());
		Vector3f p = motion.getPosition(ball, getTime());
		Vector3f av = motion.getAngularVelocity(ball, getTime());

		ball.setTime(getTime());
		ball.setState(newState);

		// prevent non null resting state vectors that could occur by rounding
		// errors
		// at the transition to the RESTING state
		if (ball.getState() == BallState.RESTING) {
			ball.setAngularVelocity(Vector3f.ZERO.clone());
			ball.setVelocity(Vector3f.ZERO.clone());
		} else {
			ball.setAngularVelocity(av);
			ball.setVelocity(v);
		}
		ball.setPosition(p);
		logger.log(Level.FINER, "New ball state", ball);

	}

	private BallState newState;

}
