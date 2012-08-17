package de.danielmescheder.snooker.gameflow;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.danielmescheder.snooker.gameflow.phases.GameInitPhase;


/**
 * The GameFlow contains the main game loop which executes until the nextPhase
 * is set to null
 * 
 */
public class GameFlow {
	private static final Logger logger = Logger.getLogger(GameFlow.class
			.getName());

	private GameState currentState;
	private GamePhase currentPhase;
	private GamePhase nextPhase;

	/**
	 * Construcs a standard gameflow which creates a new GameState and starts
	 * the GameInitPhase
	 */
	public GameFlow() {
		currentState = new GameState();
		currentPhase = new GameInitPhase(currentState);
	}

	/**
	 * Start the game
	 */
	public void start() {
		logger.log(Level.INFO, "START game flow");

		nextPhase = new GameInitPhase(currentState);

		while (nextPhase != null) {
			currentPhase = nextPhase;
			nextPhase = null;
			logger.log(Level.INFO, "START phase", currentPhase);
			currentPhase.start();
			currentState.getCurrentPlayer().getControllingUnit().handlePhase(
					currentPhase);
			while (!currentPhase.isFinished()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			logger.log(Level.INFO, "STOP phase", currentPhase);
			nextPhase = currentPhase.getNext();
		}

		logger.log(Level.INFO, "STOP game flow");
	}

	/**
	 * Get the current GameState
	 * 
	 * @return the GameState
	 */
	public GameState getCurrentState() {
		return currentState;
	}
}
