package de.danielmescheder.snooker.simulation.event;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme.math.Vector3f;

import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.domain.CollidableGameObject;
import de.danielmescheder.snooker.domain.Cushion;
import de.danielmescheder.snooker.domain.Ball.BallState;
import de.danielmescheder.snooker.domain.Cushion.Orientation;
import de.danielmescheder.snooker.simulation.physics.BallMotion;

/**
 * The CushionCollision handles the {@link Collision} of a {@link BilliardBall}
 * and a {@link Cushion}. The new velocity vectors are set and the
 * {@link BallState} is set back to BallState.SLIDING .
 * 
 */
public class CushionCollision extends SingleBallEvent implements Collision {

	static final Logger logger = Logger.getLogger(CushionCollision.class
			.getName());

	private Cushion cushion;

	public CushionCollision(float time, BilliardBall ball, Cushion cushion) {
		super(time, ball);
		this.cushion = cushion;
	}

	public Cushion getCushion() {
		return cushion;
	}

	@Override
	public void handle(Map<BilliardBall, BilliardBall> target) {
		BilliardBall ball = target.get(getBall());
		logger.log(Level.FINER, "Old ball state", ball);

		BallMotion motion = ball.getState().getMotion();
		Vector3f av = motion.getAngularVelocity(ball, getTime());
		Vector3f p = motion.getPosition(ball, getTime());
		ball.setVelocity(ball.getState().getMotion().getVelocity(ball,
				getTime()));
		ball.setTime(getTime());
		ball.setAngularVelocity(av);
		ball.setPosition(p);
		ball.setState(BallState.SLIDING);
		Vector3f velocity = ball.getVelocity();

		if (cushion.getOrientation() == Orientation.NORTH
				|| cushion.getOrientation() == Orientation.SOUTH) {
			if (cushion.getOrientation() == Orientation.NORTH) {
				p.y -= 0.0001f;
			} else {
				p.y += 0.0001f;
			}
			velocity.y = -velocity.y;
		} else {
			if (cushion.getOrientation() == Orientation.EAST) {
				p.x -= 0.0001f;
			} else {
				p.x += 0.0001f;
			}
			velocity.x = -velocity.x;

		}
		logger.log(Level.FINER, "New ball state", ball);
	}

	@Override
	public Set<CollidableGameObject> getColidableObjects() {
		Set<CollidableGameObject> s = new HashSet<CollidableGameObject>(this
				.getBallKeys());
		s.add(this.getCushion());
		return s;
	}

}
