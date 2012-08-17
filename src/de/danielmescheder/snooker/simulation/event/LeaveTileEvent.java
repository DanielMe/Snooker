package de.danielmescheder.snooker.simulation.event;

import java.util.Map;
import com.jme.math.Vector3f;

import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.domain.Tile;
import de.danielmescheder.snooker.simulation.physics.BallMotion;


/**
 * The LeaveTileEvent is a special case of {@link TilingEvent} that takes place
 * when a ball leaves a tile associated to it.
 * It automatically updates the references of said tile to deregister the ball.
 * 
 */
public class LeaveTileEvent extends TilingEvent {
	public LeaveTileEvent(float time, BilliardBall ball, Tile tile) {
		super(time, ball, tile);
	}

	@Override
	public void handle(Map<BilliardBall, BilliardBall> target) {
		BilliardBall b = target.get(getBall());
		tile.getContent().remove(b);
		b.getTiles().remove(tile);

		BallMotion motion = b.getState().getMotion();
		Vector3f av = motion.getAngularVelocity(b, getTime());
		Vector3f p = motion.getPosition(b, getTime());
		b.setVelocity(b.getState().getMotion().getVelocity(b, getTime()));
		b.setTime(getTime());
		b.setAngularVelocity(av);
		b.setPosition(p);
	}

}
