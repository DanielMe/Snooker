package de.danielmescheder.snooker.simulation.event;

import java.util.Collections;

import de.danielmescheder.snooker.domain.BilliardBall;

/**
 * The abstract class SingleBallEvent is supposed to be the superclass of all
 * events that only involve one ball, such as {@link StateChange}s or
 * {@link CushionCollision}s. This facilitates construction of such events and
 * offers additional query methods.
 * 
 */
public abstract class SingleBallEvent extends Event {
	private BilliardBall ball;

	public SingleBallEvent(float time, BilliardBall ball) {
		super(time, Collections.singleton(ball));
		this.ball = ball;
	}

	public BilliardBall getBall() {
		return ball;
	}
}
