package de.danielmescheder.snooker.control.ai.evaluator;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.danielmescheder.snooker.control.ai.EventEvaluator;
import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.domain.Pocket;
import de.danielmescheder.snooker.gameflow.GameState;
import de.danielmescheder.snooker.simulation.EventHandler;
import de.danielmescheder.snooker.simulation.InTimeSimulation;
import de.danielmescheder.snooker.simulation.Simulation;
import de.danielmescheder.snooker.simulation.event.BallCollision;
import de.danielmescheder.snooker.simulation.event.CushionCollision;
import de.danielmescheder.snooker.simulation.event.Event;
import de.danielmescheder.snooker.simulation.event.PocketingEvent;
import de.danielmescheder.snooker.simulation.event.SetValuesEvent;


/**
 * For information about the workings of this {@link EventEvaluator}, please
 * consult the report.
 * 
 */
public class DirectPocketingEvaluator implements
		EventEvaluator<SetValuesEvent, PocketingEvent> {
	private static final Logger logger = Logger
			.getLogger(DirectPocketingEvaluator.class.getName());

	private double criticalScore = 0.5;
	private double score;
	private double value = 1;

	class AIHandler implements EventHandler {
		private Simulation sim;
		private Pocket target;
		private boolean success = false;
		private BilliardBall ball;

		public AIHandler(Simulation sim, BilliardBall ball, Pocket target) {
			this.ball = ball;
			this.sim = sim;
			this.target = target;
		}

		@Override
		public void handle(Event e) {
			if (e.getBallKeys().contains(ball)) {
				if (e instanceof BallCollision) {
					logger
							.log(Level.FINER,
									"Ball collision occured, canceling");
					success = false;
					sim.pause();

				} else if (e instanceof CushionCollision) {
					logger.log(Level.FINER,
							"Cushion collision occured, canceling");
					success = false;
					sim.pause();
				} else if (e instanceof PocketingEvent) {
					logger.log(Level.FINER, "Pocketing Event occured...");
					if (((PocketingEvent) e).getPocket().equals(target)) {
						logger.log(Level.FINER, "...right pocket! YEHAW!");
						success = true;
					} else {
						logger.log(Level.FINER, "...wrong pocket! :-(");
						success = false;
					}
					sim.pause();
				}
			}
		}

		public boolean success() {
			return success;
		}
	}

	@Override
	public boolean evaluate(SetValuesEvent e, PocketingEvent target,
			GameState state) {
		logger.log(Level.INFO, "Evaluating event", e);
		InTimeSimulation sim = new InTimeSimulation(state.getBalls(), state
				.getTable());
		AIHandler aiHandler = new AIHandler(sim, target.getBall(), target
				.getPocket());
		sim.addEventHandler(aiHandler);

		sim.init(Collections.singleton(e));
		sim.finish();

		score = (aiHandler.success) ? 1 : 0;

		logger.log(Level.FINE, "Score was", score);
		logger.log(Level.FINE, "Passed", (score >= criticalScore));
		value = target.getBall().getType().getValue();
		return (score >= criticalScore);
	}

	@Override
	public void setCriticalScore(double score) {
		criticalScore = score;
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
