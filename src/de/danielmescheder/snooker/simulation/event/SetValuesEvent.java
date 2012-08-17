package de.danielmescheder.snooker.simulation.event;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


import com.jme.math.Vector3f;

import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.domain.Ball.BallState;

/**
 * The SetValuesEvent allows for direct setting of the ball's current state
 * variables: angular velocity, velocity and {@link BallState}. This makes it
 * easy to start a simulation without a {@link CueInteraction} or resuming a
 * {@link Simulation} that has been saved.
 * 
 */
public class SetValuesEvent extends SingleBallEvent {
	static final Logger logger = Logger.getLogger(SetValuesEvent.class
			.getName());

	private Vector3f v, av;
	private BallState state;

	public SetValuesEvent(float time, BilliardBall ball, Vector3f v,
			Vector3f av, BallState state) {
		super(time, ball);
		this.v = v;
		this.av = av;
		this.state = state;
	}

	@Override
	public void handle(Map<BilliardBall, BilliardBall> target) {
		for (BilliardBall b : this.getBalls(target)) {
			logger.log(Level.FINER, "Old ball state", b);
			b.setAngularVelocity(av);
			b.setVelocity(v);
			b.setState(state);
			b.setTime(getTime());
			logger.log(Level.FINER, "New ball state", b);
		}
	}

	public Vector3f getVelocity() {
		return v;
	}

	public Vector3f getAngularVelocity() {
		return av;
	}

	public BallState getState() {
		return state;
	}

}
