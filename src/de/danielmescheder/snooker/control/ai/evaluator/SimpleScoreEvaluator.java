package de.danielmescheder.snooker.control.ai.evaluator;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.danielmescheder.snooker.control.ai.EventEvaluator;
import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.gameflow.GameState;
import de.danielmescheder.snooker.logic.GameLogicHandler;
import de.danielmescheder.snooker.simulation.EventHandler;
import de.danielmescheder.snooker.simulation.InTimeSimulation;
import de.danielmescheder.snooker.simulation.Simulation;
import de.danielmescheder.snooker.simulation.event.CueInteraction;
import de.danielmescheder.snooker.simulation.event.Event;
import de.danielmescheder.snooker.simulation.event.PocketingEvent;
import de.danielmescheder.snooker.simulation.event.SingleBallEvent;


/**
 * The SimpleScoreEvaluator will examine a {@link Simulation} initialized by a
 * {@link CueInteraction} and calculate a score for this Simulation.
 * 
 */
public class SimpleScoreEvaluator implements
		EventEvaluator<CueInteraction, Event> {
	private static final Logger logger = Logger
			.getLogger(SimpleScoreEvaluator.class.getName());

	private double criticalScore = 0.5;
	private double score;
	private double value = 1;
	private PlannedShotOutcomeHandler shotOutcomeHandler;

	class AIHandler implements EventHandler {
		private Simulation sim;
		private GameLogicHandler logicHandler;
		private int eventCount;

		public AIHandler(Simulation sim, GameLogicHandler logicHandler) {
			this.sim = sim;
			this.logicHandler = logicHandler;
			eventCount = 0;
		}

		@Override
		public void handle(Event e) {
			eventCount++;

			if (eventCount > 10000 && logicHandler.getPottedScore() == 0
					|| logicHandler.foulCommitted()) {
				sim.pause();
			}
		}
	}

	@Override
	public void setCriticalScore(double score) {
		this.criticalScore = score;
	}

	class PlannedShotOutcomeHandler implements EventHandler {

		private Set<BilliardBall> pottedBalls = new HashSet<BilliardBall>();

		@Override
		public void handle(Event e) {
			if (e instanceof PocketingEvent) {
				SingleBallEvent event = (SingleBallEvent) e;

				BilliardBall b = event.getBall();
				pottedBalls.add(b);
			}
		}

		public Set<BilliardBall> getPottedBalls() {
			return pottedBalls;
		}

	}

	/**
	 * Any goal event will do - we only consider the score!
	 * 
	 * @see de.danielmescheder.snooker.control.ai.EventEvaluator#evaluate(de.danielmescheder.snooker.simulation.event.Event,
	 *      de.danielmescheder.snooker.simulation.event.Event,
	 *      de.danielmescheder.snooker.gameflow.GameState)
	 */
	@Override
	public boolean evaluate(CueInteraction event, Event target, GameState state) {
		logger.log(Level.INFO, "Evaluating event", event);
		score = 0;
		HashSet<Event> eventSet = new HashSet<Event>();
		eventSet.add(event);

		double maxScore = 0;
		maxScore = state.getMaxScore();
		InTimeSimulation sim = new InTimeSimulation(state.getBalls(), state
				.getTable());
		sim.init(eventSet);
		GameLogicHandler logicHandler;
		shotOutcomeHandler = new PlannedShotOutcomeHandler();
		logicHandler = new GameLogicHandler(state);
		sim.addEventHandler(logicHandler);
		sim.addEventHandler(shotOutcomeHandler);
		sim.addEventHandler(new AIHandler(sim, logicHandler));

		sim.finish();

		if (sim.isPaused()) {
			// simulation has been aborted
			score = -1;
		} else {
			logicHandler.evaluateEvents();

			if (logicHandler.foulCommitted()) {
				score = -((double) logicHandler.getFoulScore()) / 7f; 
			} else {
				score = (logicHandler.getPottedScore()) / maxScore;
			}
		}
		value = maxScore;
		logger.log(Level.FINE, "Score was", score);
		logger.log(Level.FINE, "Passed", (score >= criticalScore));
		return (score >= criticalScore);
	}

	@Override
	public double getScore() {
		return score;
	}

	public Set<BilliardBall> getPlannedOutcomeSet() {
		return shotOutcomeHandler.getPottedBalls();
	}

	@Override
	public double getValue() {
		return value;
	}

}
