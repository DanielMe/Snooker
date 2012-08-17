package de.danielmescheder.snooker.gameflow.phases;

import de.danielmescheder.snooker.gameflow.GamePhase;
import de.danielmescheder.snooker.gameflow.GameState;
import de.danielmescheder.snooker.simulation.event.CueInteraction;
import de.danielmescheder.snooker.simulation.event.NoisyCueInteraction;

/**
 * The aiming phase creates a new CueInteraction event out of several parameters
 * 
 */
public class AimingPhase extends GamePhase {
	private float angDest, angElev, transX, transY, velocity;

	/**
	 * Construcs a standard AimingPhase
	 * 
	 * @param state
	 *            the current GameState
	 */
	public AimingPhase(GameState state) {
		super(state);
		angDest = 0;
		angElev = 0;
		transX = 0;
		transY = 0;
		velocity = 0;
	}

	/**
	 * Sets the cue translation in X-direction
	 * 
	 * @param transX
	 *            the translation in meters
	 */
	public void setTransX(float transX) {
		this.transX = transX;
	}

	/**
	 * Sets the cue translation in Y-direction
	 * 
	 * @param transY
	 *            the translation in meters
	 */
	public void setTransY(float transY) {
		this.transY = transY;
	}

	/**
	 * Sets the angle at which to hit the ball
	 * 
	 * @param angDest
	 *            the angle
	 */
	public void setAngDest(float angDest) {
		this.angDest = angDest;
	}

	/**
	 * Sets the elevation angle of the cue
	 * 
	 * @param angElev
	 *            the elevation angle
	 */
	public void setAngElev(float angElev) {
		this.angElev = angElev;
	}

	/**
	 * Sets the velocity of the cue in m/s
	 * 
	 * @param velocity
	 */
	public void setVelocity(float velocity) {
		this.velocity = velocity;
	}

	@Override
	public void finish() {
		if (finished) {
			return;
		}
		CueInteraction event = new NoisyCueInteraction(0, state
				.getCurrentPlayer().getCue(), state.getCueBall(), angDest,
				angElev, transX, transY, velocity);

		setNext(new SimulationPhase(state, event));
		super.finish();
	}
}