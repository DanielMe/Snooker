package de.danielmescheder.snooker.simulation.event;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme.math.Vector3f;

import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.domain.Pocket;
import de.danielmescheder.snooker.domain.Ball.BallState;

/**
 * The PocketingEvent handles what happens after a ball has been registered as
 * pocketed.
 * 
 */
public class PocketingEvent extends SingleBallEvent {
	static final Logger logger = Logger.getLogger(PocketingEvent.class
			.getName());

	private Pocket pocket;

	public PocketingEvent(float time, BilliardBall ball, Pocket pocket) {
		super(time, ball);
		this.pocket = pocket;
	}

	@Override
	public void handle(Map<BilliardBall, BilliardBall> target) {
		BilliardBall ball = target.get(getBall());
		logger.log(Level.FINER, "Old ball state", ball);

		ball.setPosition(pocket.getPosition());
		ball.setVelocity(Vector3f.ZERO.clone());
		ball.setAngularVelocity(Vector3f.ZERO.clone());
		ball.setState(BallState.RESTING);
		ball.setTime(getTime());

		ball.setPosition(new Vector3f(-.5f, 4f * ball.getRadius()
				* ball.getID(), .6f));
		logger.log(Level.FINER, "New ball state", ball);

	}

	public Pocket getPocket() {
		return pocket;
	}
}
