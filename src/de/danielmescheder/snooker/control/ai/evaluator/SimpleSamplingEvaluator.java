package de.danielmescheder.snooker.control.ai.evaluator;

import java.util.HashSet;
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


/**
 * The SimpleSamplingEvaluator takes a {@link CueInteraction} and samples this
 * event. Then the probability of success can be accessed.
 * 
 */
public class SimpleSamplingEvaluator implements
		EventEvaluator<CueInteraction, Event> {
	private static final Logger logger = Logger
			.getLogger(SimpleSamplingEvaluator.class.getName());

	private double criticalScore = 0.5;
	private double score;
	private double samples = 10;
	private double value;

	public SimpleSamplingEvaluator(int samples) {
		this.samples = samples;
	}

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
			if ((eventCount > 10000 && logicHandler.getPottedScore() == 0)
					|| logicHandler.foulCommitted()) {
				sim.pause();
			}
		}
	}

	@Override
	public void setCriticalScore(double score) {
		this.criticalScore = score;
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

		double maxScore = 0;
		for (BilliardBall.Type type : state.getPossibleOnBallTypes()) {
			maxScore = Math.max(maxScore, type.getValue());
		}
		double totalScore = 0;
		for (int i = 0; i < samples; i++) {

			HashSet<Event> eventSet = new HashSet<Event>();
			eventSet.add(event.toNoisyCueInteraction());
			InTimeSimulation sim = new InTimeSimulation(state.getBalls(), state
					.getTable());
			sim.init(eventSet);
			GameLogicHandler logicHandler;

			logicHandler = new GameLogicHandler(state);
			sim.addEventHandler(logicHandler);
			sim.addEventHandler(new AIHandler(sim, logicHandler));
			sim.finish();

			if (sim.isPaused()) {
				// simulation has been aborted
			} else {
				logicHandler.evaluateEvents();

				if (logicHandler.foulCommitted()) {
				} else {
					if ((double) logicHandler.getPottedScore() > 0)
						totalScore += 1;
				}
			}
		}
		value = maxScore;
		score = (totalScore / samples);
		logger.log(Level.FINE, "Sampling a shot. Score: " + score);

		return (true);
	}

	@Override
	public double getScore() {
		return score;
	}

	@Override
	public double getValue() {
		return value;
	}

}
