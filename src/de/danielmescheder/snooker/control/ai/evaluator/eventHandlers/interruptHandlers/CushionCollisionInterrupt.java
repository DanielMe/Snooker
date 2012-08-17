package de.danielmescheder.snooker.control.ai.evaluator.eventHandlers.interruptHandlers;

import de.danielmescheder.snooker.simulation.EventHandler;
import de.danielmescheder.snooker.simulation.Simulation;
import de.danielmescheder.snooker.simulation.event.BallCollision;
import de.danielmescheder.snooker.simulation.event.CushionCollision;
import de.danielmescheder.snooker.simulation.event.Event;

/**
 * The {@link CushionCollisionInterrupt} pauses a simulation in case a certain
 * number of {@link CushionCollision}s has occurred. This serves as a heuristic
 * to quickly discard non-viable {@link Simulation}s.
 *  
 */
public class CushionCollisionInterrupt implements EventHandler {

	private int maxCushion = 0;
	private int currentCushion = 0;
	private boolean ballBallOccured;
	private Simulation sim;

	public CushionCollisionInterrupt(Simulation sim, int maxCushion) {
		this.maxCushion = maxCushion;
		this.sim = sim;
	}

	@Override
	public void handle(Event e) {
		if (e instanceof BallCollision) {
			ballBallOccured = true;
		}
		if (!ballBallOccured) {
			if (e instanceof CushionCollision) {
				currentCushion += 1;
			}
			if (currentCushion >= maxCushion) {
				sim.pause();
			}

		}
	}

}
