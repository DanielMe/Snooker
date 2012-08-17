package de.danielmescheder.snooker.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


import com.jme.math.Vector3f;

import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.gameflow.GameState;
import de.danielmescheder.snooker.presentation.TablePresentation;
import de.danielmescheder.snooker.simulation.EventHandler;
import de.danielmescheder.snooker.simulation.InTimeSimulation;
import de.danielmescheder.snooker.simulation.Simulation;
import de.danielmescheder.snooker.simulation.event.BallCollision;
import de.danielmescheder.snooker.simulation.event.Event;
import de.danielmescheder.snooker.simulation.event.PocketingEvent;
import de.danielmescheder.snooker.simulation.event.SingleBallEvent;

/**
 * The AimLine class offers methods to show the movement of a ball and the
 * outcome of events as a line on the {@link TablePresentation} to facilitate aiming or show an AI's
 * plans
 * 
 * 
 */
public class AimLine {
	private class AimLineHandler implements EventHandler {
		public int collisionCount = 0;
		public BilliardBall monitoredBall;
		public ArrayList<ArrayList<Vector3f>> linePointsList;
		public ArrayList<Vector3f> points;

		@Override
		public void handle(Event e) {
			if (e instanceof BallCollision) {
				Set<BilliardBall> balls = e.getBallKeys();
				if (balls.contains(monitoredBall)) {
					sim.getBall(monitoredBall);
					addPointFromMonitoredBall();
					BilliardBall newMon = monitoredBall;
					for (BilliardBall b : balls) {
						if (!b.equals(monitoredBall)) {
							newMon = (BilliardBall) b.clone();
						}
					}
					monitoredBall = newMon;

					linePointsList.add(points);
					presentation.updateShotVisualization(linePointsList);
					points = new ArrayList<Vector3f>();
					addPointFromMonitoredBall();

					collisionCount++;
					if (collisionCount > visualizationDepth) {
						sim.pause();
					}
				}
			}
			if (e instanceof PocketingEvent) {
				Vector3f newPoint = ((PocketingEvent) e).getPocket()
						.getPosition().clone();
				addPoint(newPoint);
				linePointsList.add(points);
				sim.pause();
			}

		}

		/**
		 * Adds the current position of the monitored ball to the line
		 */
		public void addPointFromMonitoredBall() {
			addPoint(monitoredBall.getPosition().clone());

		}

		/**
		 * Adds a point to the aim line. All added points will later be
		 * connected
		 * 
		 * @param point
		 *            the point to add
		 */
		public void addPoint(Vector3f point) {
			point.z = monitoredBall.getRadius();
			point.x = point.x - (0.5f * state.getTable().getWidth());
			point.y = point.y - (0.5f * state.getTable().getLength());
			points.add(point);
		}
	}

	private TablePresentation presentation;
	private GameState state;
	private int visualizationDepth;
	private float accuracy;
	private AimLineHandler aimingHandler;
	Simulation sim;

	/**
	 * Constructs a new AimLine object with a maximum simulation depth and a
	 * certain accuracy
	 * 
	 * @param presentation
	 *            the {@link TablePresentation} that will show the line
	 * @param state
	 *            the current {@link GameState}
	 * @param depth
	 *            the number of {@link BallCollision} events that are allowed
	 *            before the visualization stops
	 * @param accuracy
	 *            the accuracy of the AimLine
	 */
	public AimLine(TablePresentation presentation, GameState state, int depth,
			float accuracy) {
		this.presentation = presentation;
		this.state = state;
		this.visualizationDepth = depth;
		this.accuracy = accuracy;
		sim = new InTimeSimulation(new HashSet<BilliardBall>(state.getBalls()),
				state.getTable());
		aimingHandler = new AimLineHandler();
		sim.addEventHandler(aimingHandler);
	}

	/**
	 * Shows the AimLine
	 * 
	 * @param event
	 *            the initial event from which to start the visualization
	 */
	public void showAimLine(SingleBallEvent event) {
		sim.init(Collections.singleton(event));

		aimingHandler.monitoredBall = (BilliardBall) event.getBall().clone();
		aimingHandler.linePointsList = new ArrayList<ArrayList<Vector3f>>();
		aimingHandler.points = new ArrayList<Vector3f>();
		aimingHandler.addPointFromMonitoredBall();
		aimingHandler.collisionCount = 0;

		int step = 1;
		while (!sim.isPaused() && sim.hasNextEvent()) {
			sim.getBall(aimingHandler.monitoredBall);
			aimingHandler.addPointFromMonitoredBall();
			sim.advanceTime(accuracy * step);
			step++;
		}
		aimingHandler.linePointsList.add(aimingHandler.points);
		presentation.updateShotVisualization(aimingHandler.linePointsList);

	}
}
