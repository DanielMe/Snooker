package de.danielmescheder.snooker.gameflow.phases;

import de.danielmescheder.snooker.gameflow.GamePhase;
import de.danielmescheder.snooker.gameflow.GameState;

/**
 * The BreakInitPhase updates all things necessary for a new break.
 * 
 */
public class BreakInitPhase extends GamePhase {
	/**
	 * Constructs a new BreakInitPhase
	 * 
	 * @param state
	 *            the GameState
	 */
	public BreakInitPhase(GameState state) {
		super(state);
	}

	@Override
	public void start() {
		super.start();
		state.setPossibleOnBalls(GameState.redBalls);
		finish();
	}

	@Override
	public void finish() {
		if (finished) {
			return;
		}
		setNext(new PositioningPhase(state));
		super.finish();
	}
}
