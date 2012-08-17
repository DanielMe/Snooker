package de.danielmescheder.snooker.control.ai;

import de.danielmescheder.snooker.gameflow.GameState;
import de.danielmescheder.snooker.simulation.event.Event;

/**
 * The EventEvaluator interface unifies the evaluation of certain events under
 * given criteria. It takes an initial event that starts the test and an event which is
 * desired. Then, a score is computed.
 * 
 * @param <E> The initial event
 * @param <T> The event to test for
 */
public interface EventEvaluator<E extends Event, T extends Event> {
	/**
	 * Sets the critical score above which events are regarded as feasible
	 * 
	 * @param score
	 *            a value between -1 and 1. -1 is a
	 *            "perfect situation for the opponent" (He will score everything
	 *            with 100% propability) 1 is
	 *            "perfect situation for the current player"
	 */
	public void setCriticalScore(double score);

	/**
	 * Evaluate the event and return whether it is feasible or not. An event is
	 * considered feasible if its score is above the critical value.
	 * 
	 * @param e
	 *            The event to test
	 * @param state
	 *            the gamestate on which the event is investigated
	 * @return true/false: feasible or not
	 */
	public boolean evaluate(E e, T target, GameState state);

	public double getScore();

	public double getValue();
}
