package de.danielmescheder.snooker.control.ai.evaluator;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.danielmescheder.snooker.control.ai.EventEvaluator;
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
public class CueInteractionToPocketEvaluator implements
		EventEvaluator<CueInteraction, PocketingEvent> {
	private static final Logger logger = Logger
			.getLogger(CueInteractionToPocketEvaluator.class.getName());

	private double criticalScore = 0.5;
	private double score;
	private int samples;
	private float maxScore;

	class AIHandler implements EventHandler {
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
					// sim.pause();
				}
			}
			if (e instanceof PocketingEvent) {
				if (e.getBallKeys().contains(ball)) {
					success = true;
				}
				if (e.getBallKeys().contains(cueBall)) {
				}
			}
		}

		public boolean success() {
			return success;
		}
	}

	public CueInteractionToPocketEvaluator(int samples) {
		this.samples = samples;
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

		int targetValue = target.getBall().getType().getValue();
		maxScore = 0;
		for (BilliardBall.Type type : state.getPossibleOnBallTypes()) {
			maxScore = Math.max(maxScore, type.getValue());
		}

		InTimeSimulation sim = new InTimeSimulation(state.getBalls(), state
				.getTable());

		for (int i = 0; i < samples; i++) {
			CueInteraction ci = new NoisyCueInteraction(event.getTime(), state
					.getCurrentPlayer().getCue(), event.getBall(), event
					.getAngDest(), event.getAngElev(), event.getTransX(), event
					.getTransY(), event.getVelocity());
			sim.init(Collections.singleton(ci));
			GameLogicHandler logicHandler = new GameLogicHandler(state);
			sim.removeHandlers();
			sim.addEventHandler(logicHandler);
			AIHandler aiHandler = new AIHandler(sim, logicHandler, target
					.getBall(), event.getBall(), 20);
			sim.addEventHandler(aiHandler);

			sim.finish();

			if (sim.isPaused()) {
				if (logicHandler.foulCommitted()) {
					points -= Math.min(logicHandler.getFoulScore(), maxScore);
				}

			} else {
				logicHandler.evaluateEvents();

				if (logicHandler.foulCommitted()) {
					points -= Math.min(logicHandler.getFoulScore(), maxScore);
				} else if (aiHandler.success()) {
					points += targetValue;
				}
			}
		}

		score = Math.min(Math.max(-1, points / (maxScore * samples)), 1);
		logger.log(Level.FINE, "Score was", score);
		logger.log(Level.FINE, "Passed", (score >= criticalScore));
		return (score >= criticalScore);
	}

	@Override
	public double getScore() {
		return score;
	}

	@Override
	public double getValue() {
		return maxScore;
	}

}
