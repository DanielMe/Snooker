package de.danielmescheder.snooker.control.ai.generator;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme.math.FastMath;

import de.danielmescheder.snooker.control.ai.EventGenerator;
import de.danielmescheder.snooker.gameflow.GameState;
import de.danielmescheder.snooker.simulation.event.CueInteraction;
import de.danielmescheder.snooker.simulation.event.Event;


/**
 * This {@link EventGenerator} generates a random {@link CueInteraction}
 * 
 */
public class RandomCueInteractionGenerator implements
		EventGenerator<CueInteraction, Event> {
	private static final Logger logger = Logger
			.getLogger(RandomCueInteractionGenerator.class.getName());

	@Override
	public CueInteraction generate(GameState state) {
		logger.log(Level.INFO, "Generating next event");

		float angDest = FastMath.rand.nextFloat() * 2 * FastMath.PI;
		float angElev = 0;
		float transX = 0;
		float transY = 0;
		float velocity = FastMath.rand.nextFloat() * 5f + .05f;

		return new CueInteraction(0, state.getCurrentPlayer().getCue(), state
				.getCueBall(), angDest, angElev, transX, transY, velocity);
	}

	@Override
	public Event getTarget() {
		// This stupid generator does not have a goal... it's just random!
		return null;
	}

	@Override
	public void reset() {
		// Not Implemented

	}

}
