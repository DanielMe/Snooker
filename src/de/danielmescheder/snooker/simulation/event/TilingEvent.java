package de.danielmescheder.snooker.simulation.event;

import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.domain.Tile;

/**
 * The abstract class TilingEvent unites the basic constructor and query methods
 * common to all TilingEvents
 * 
 */
public abstract class TilingEvent extends SingleBallEvent {
	protected Tile tile;

	public TilingEvent(float time, BilliardBall ball, Tile tile) {
		super(time, ball);
		this.tile = tile;
	}

	public Tile getTile() {
		return tile;
	}

}
