package de.danielmescheder.snooker.control.ai.evaluator;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.danielmescheder.snooker.control.ai.EventEvaluator;
import de.danielmescheder.snooker.control.ai.EventGenerator;
import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.gameflow.GameState;
import de.danielmescheder.snooker.logic.GameLogicHandler;
import de.danielmescheder.snooker.simulation.EventHandler;
import de.danielmescheder.snooker.simulation.InTimeSimulation;
import de.danielmescheder.snooker.simulation.Simulation;
import de.danielmescheder.snooker.simulation.event.BallCollision;
import de.danielmescheder.snooker.simulation.event.CueInteraction;
import de.danielmescheder.snooker.simulation.event.Event;
import de.danielmescheder.snooker.simulation.event.NoisyCueInteraction;
import de.danielmescheder.snooker.simulation.event.PocketingEvent;


/**
 * For information about the workings of this {@link EventEvaluator}, please
 * consult the report.
 * 
 */
public class DepthSamplingEvaluator implements
		EventEvaluator<CueInteraction, PocketingEvent> {
	private static final Logger logger = Logger
			.getLogger(DepthSamplingEvaluator.class.getName());

	private double criticalScore = 0.5;
	private double score;
	private int samples;
	private int nextLevelShots;
	private float maxScore;
	private float nextLevelValue = 1;
	private EventGenerator<CueInteraction, PocketingEvent> nextLevelGen;
	private EventEvaluator<CueInteraction, PocketingEvent> nextLevelEval;

	private class AIHandler implements EventHandler {
		private Simulation sim;
		private GameLogicHandler logicHandler;
		private BilliardBall ball;
		private BilliardBall cueBall;
		private int collisionCount;
		private int maxCollisions;
		private boolean success = false;

		public AIHandler(Simulation sim, GameLogicHandler logicHandler,
				BilliardBall ball, BilliardBall cueBall, int maxCollisions) {
			this.sim = sim;
			this.ball = ball;
			this.cueBall = cueBall;
			this.maxCollisions = maxCollisions;
			this.logicHandler = logicHandler;
			collisionCount = 0;
		}

		@Override
		public void handle(Event e) {
			if (logicHandler.foulCommitted()) {
				sim.pause();
			}
			if (e instanceof BallCollision) {
				collisionCount++;

				if (collisionCount > maxCollisions) {
					sim.pause();
				}
			}
			if (e instanceof PocketingEvent) {
				if (e.getBallKeys().contains(ball)) {
					success = true;
				}
			}
		}

		public boolean success() {
			return success;
		}
	}

	public DepthSamplingEvaluator(int samples,
			EventGenerator<CueInteraction, PocketingEvent> nextLevelGenerator,
			EventEvaluator<CueInteraction, PocketingEvent> nextLevelEvaluator,
			int nextLevelShots) {
		this.samples = samples;
		this.nextLevelGen = nextLevelGenerator;
		this.nextLevelEval = nextLevelEvaluator;
		this.nextLevelShots = nextLevelShots;
	}

	@Override
	public void setCriticalScore(double score) {
		this.criticalScore = score;
	}

	@Override
	public boolean evaluate(CueInteraction event, PocketingEvent target,
			GameState state) {
		logger.log(Level.INFO, "Evaluating event", event);
		float points = 0;
		score = 0;

		int targetValue = target.getBall().getType().getValue();
		maxScore = 0;
		for (BilliardBall.Type type : state.getPossibleOnBallTypes()) {
			maxScore = Math.max(maxScore, type.getValue());
		}

		InTimeSimulation sim = new InTimeSimulation(state.getBalls(), state
				.getTable());

		for (int i = 0; i < samples; i++)
		{
			CueInteraction ci = event.toNoisyCueInteraction();
			sim.init(Collections.singleton(ci));
			GameLogicHandler logicHandler = new GameLogicHandler(state);
			sim.removeHandlers();
			sim.addEventHandler(logicHandler);
			AIHandler aiHandler = new AIHandler(sim, logicHandler, target
					.getBall(), event.getBall(), 20);
			sim.addEventHandler(aiHandler);

			sim.finish();

			logicHandler.evaluateEvents();

			if (sim.isPaused()) {
				if (logicHandler.foulCommitted()) {
					points -= logicHandler.getFoulScore() / (float) samples;
				}
			} else {
				if (aiHandler.success()) {
					points += targetValue / (float) samples;
				}
			}
		}

		score = Math.min(1, Math.max(-1, points / maxScore));
		logger.log(Level.FINER, "Score for this level", score);

		if (score >= criticalScore) {
			logger.log(Level.FINER, "Running sim for depth check", score);
			sim.init(Collections.singleton(event));
			GameLogicHandler logicHandler = new GameLogicHandler(state);
			sim.removeHandlers();
			sim.addEventHandler(logicHandler);

			sim.finish();
			float nextLevelScore = nextLevelScore(state, sim, logicHandler);
			score = (2 * points / (maxScore + nextLevelValue) + nextLevelScore) / 3f;

		}

		logger.log(Level.INFO, "Score was", score);
		logger.log(Level.FINE, "Passed", (score >= criticalScore));

		return (score >= criticalScore);
	}

	private float nextLevelScore(GameState state, Simulation sim,
			GameLogicHandler logicHandler) {
		GameState newState = (GameState) state.clone();
		for (BilliardBall b : newState.getBalls()) {
			sim.getBall(b);
			b.setTime(0);
		}
		logicHandler.updateState(newState);
		int count = 0;
		float maxNextLevelScore = -1;
		nextLevelValue = 1;
		do {
			count++;
			CueInteraction cueStrike = nextLevelGen.generate(newState);
			if (cueStrike == null) {
				return -1;
			}
			if (nextLevelEval.evaluate(cueStrike, nextLevelGen.getTarget(),
					newState)) {
				if (nextLevelEval.getScore() > maxNextLevelScore) {
					maxNextLevelScore = (float) (nextLevelEval.getScore() * nextLevelEval
							.getValue());
				}
			}
		} while (maxNextLevelScore < nextLevelEval.getValue()
				&& count < nextLevelShots);
		logger.log(Level.INFO, "Found next level score", maxNextLevelScore);
		nextLevelValue = (float) nextLevelEval.getValue();
		return (maxNextLevelScore / (maxScore + nextLevelValue));
	}

	@Override
	public double getScore() {
		return score;
	}

	@Override
	public double getValue() {
		return maxScore + nextLevelEval.getValue();
	}

}
