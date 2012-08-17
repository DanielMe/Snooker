package de.danielmescheder.snooker.control.ai;

import de.danielmescheder.snooker.gameflow.GameState;
import de.danielmescheder.snooker.simulation.event.Event;

/**
 * The EventGenerator interface unifies the detection of {@link Event}s that
 * lead to a desired target event.
 * 
 * @param <E> The Event to be generated
 * @param <T> The target Event to be generated
 */
public interface EventGenerator<E extends Event, T extends Event> {
	public E generate(GameState state);

	public T getTarget();

	public void reset();
}
