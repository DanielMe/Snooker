package de.danielmescheder.snooker.control.ui.controller;

import java.util.HashSet;
import java.util.Set;

import com.jme.input.ChaseCamera;
import com.jme.input.InputHandler;
import com.jme.input.MouseInput;
import com.jme.input.action.InputActionEvent;
import com.jme.input.action.MouseInputAction;
import com.jme.scene.Node;

import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.gameflow.GamePhase;
import de.danielmescheder.snooker.gameflow.GameState;
import de.danielmescheder.snooker.gameflow.phases.SimulationPhase;
import de.danielmescheder.snooker.presentation.SnookerTable3D;
import de.danielmescheder.snooker.presentation.TablePresentation;
import de.danielmescheder.snooker.simulation.EventHandler;
import de.danielmescheder.snooker.simulation.PrescheduledSimulation;
import de.danielmescheder.snooker.simulation.Simulation;
import de.danielmescheder.snooker.simulation.event.Event;
import de.danielmescheder.snooker.simulation.event.PocketingEvent;
import de.danielmescheder.snooker.testing.TestDataCollector;

/**
 * The SimulationControl offers controls for the {@link SnookerTable3D} graphic
 * representation and {@link Simulation} during the {@link SimulationPhase}.
 * 
 */
public class SimulationControl extends InputHandler {
	private Simulation simulation;
	private GamePhase phase;
	private GameState state;
	private TablePresentation presentation;
	private TestDataCollector collector = null;
	private boolean collectData = false;
	private boolean isFinished = false;
	private ShotOutcomeHandler outcomeHandler;

	public SimulationControl(final TablePresentation presentation,
			SimulationPhase phase, final GameState state) {
		this.simulation = phase.getSimulation();
		this.phase = phase;
		this.state = state;
		this.presentation = presentation;
		presentation.showShotVisualization(false);
		((SnookerTable3D) presentation).showMarkings(false);

		// this.setTilesInPresentation();

		class SimulationMouseInput extends MouseInputAction {
			private boolean skipping = false;
			private boolean resumed = false;

			@Override
			public void performAction(InputActionEvent evt) {
				if (MouseInput.get().isButtonDown(2) && simulation.isPaused()
						&& (!resumed)) {
					simulation.resume();
					resumed = true;
				} else if (!MouseInput.get().isButtonDown(2) && resumed) {
					resumed = false;
				}
				if (MouseInput.get().isButtonDown(1) && !skipping) {
					skipping = true;
					simulation.finish();
					for (BilliardBall b : state.getBalls()) {
						simulation.getBall(b);
						presentation.updateBall(b);
					}
				} else if (!MouseInput.get().isButtonDown(1)) {
					skipping = false;
				}
			}
		}

		/*
		 * simulation.addEventHandler(new EventHandler() {
		 * 
		 * @Override public void handle(Event e) { if (e instanceof
		 * BallCollision) { ((SnookerTable3D)
		 * presentation).makeShine(e.getBallKeys()); }
		 * 
		 * } });
		 */

		this.addAction(new SimulationMouseInput());
	}

	public SimulationControl(final TablePresentation presentation,
			SimulationPhase phase, final GameState state,
			TestDataCollector collector) {
		this(presentation, phase, state);

		this.collector = collector;
		this.collectData = true;

		outcomeHandler = new ShotOutcomeHandler();
		simulation.addEventHandler(outcomeHandler);

	}

	@Override
	public void update(float time) {
		super.update(time);
		if (simulation.isReady()) {
			if (simulation.hasNextEvent()) {
				// simulation.finish();
				if (simulation instanceof PrescheduledSimulation) {
					Event nextPocketCollision = ((PrescheduledSimulation) simulation)
							.getNextPocketCollision();
					if (nextPocketCollision != null
							&& (nextPocketCollision.getTime() - simulation
									.getCurrentTime()) < 1.0f) {
						Set<BilliardBall> balls = nextPocketCollision
								.getBallKeys();
						BilliardBall ball = (BilliardBall) balls.toArray()[0];
						Node ballNode = presentation.getBallNode(ball);
						ChaseCamera cam = (ChaseCamera) presentation
								.getCameraControl();
						cam.setTarget(ballNode);
						time = time / 5;
					}

					else {
						ChaseCamera cam = (ChaseCamera) presentation
								.getCameraControl();
						cam.setTarget(presentation.getTableNode());
					}
				}
				simulation.advanceTime(time);

				for (BilliardBall b : state.getBalls()) {
					simulation.getBall(b);
					presentation.updateBall(b);
				}
				// presentation.updateTileVisualization(state.getBalls());
			} else {

				if (collectData && !isFinished) {
					isFinished = true;
					collector.registerPottedBalls(outcomeHandler
							.getPottedBalls());
					System.out.println(collector);
				}
				phase.finish();
				for (BilliardBall b : state.getBalls()) {
					presentation.updateBall(b);
				}
			}
		}
	}

	private void setTilesInPresentation() {
		((SnookerTable3D) presentation).setTiles(simulation.getTiles());
	}

	class ShotOutcomeHandler implements EventHandler {

		private Set<BilliardBall> pottedBalls = new HashSet<BilliardBall>();

		@Override
		public void handle(Event e) {
			if (e instanceof PocketingEvent) {
				pottedBalls.add(((PocketingEvent) e).getBall());
			}
		}

		public Set<BilliardBall> getPottedBalls() {
			return pottedBalls;
		}

	}
}
