package de.danielmescheder.snooker.control;

import de.danielmescheder.snooker.gameflow.GamePhase;

/**
 * The ControllingUnit interface allows for the implementation of different
 * units to control the game. Every time input is required from these units, it
 * is given the current {@link GamePhase} on which it acts.
 * 
 */
public interface ControllingUnit {
	/**
	 * Handle the current {@link GamePhase} and take the necessary actions to
	 * advance to the next GamePhase.
	 * 
	 * @param phase
	 *            the GamePhase to handle
	 */
	public void handlePhase(GamePhase phase);
}
