package de.danielmescheder.snooker.control.ai.testing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme.input.ChaseCamera;
import com.jme.input.thirdperson.ThirdPersonMouseLook;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;

import de.danielmescheder.snooker.control.ControllingUnit;
import de.danielmescheder.snooker.control.ai.evaluator.SimpleScoreEvaluator;
import de.danielmescheder.snooker.control.ai.generator.RandomCueInteractionGenerator;
import de.danielmescheder.snooker.control.ui.controller.SimulationControl;
import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.gameflow.GamePhase;
import de.danielmescheder.snooker.gameflow.GameState;
import de.danielmescheder.snooker.gameflow.phases.AimingPhase;
import de.danielmescheder.snooker.gameflow.phases.FrameInitPhase;
import de.danielmescheder.snooker.gameflow.phases.PositioningPhase;
import de.danielmescheder.snooker.gameflow.phases.SimulationPhase;
import de.danielmescheder.snooker.presentation.SnookerTable3D;
import de.danielmescheder.snooker.presentation.TablePresentation;
import de.danielmescheder.snooker.simulation.EventHandler;
import de.danielmescheder.snooker.simulation.InTimeSimulation;
import de.danielmescheder.snooker.simulation.Simulation;
import de.danielmescheder.snooker.simulation.event.BallCollision;
import de.danielmescheder.snooker.simulation.event.CueInteraction;
import de.danielmescheder.snooker.simulation.event.Event;
import de.danielmescheder.snooker.simulation.event.PocketingEvent;
import de.danielmescheder.snooker.testing.TestDataCollector;


/**
 * A version of the {@link RandomAIControl} that makes use of the
 * {@link TestDataCollector}
 * 
 * @see RandomAIControl
 */

public class RandomAITestingControl implements ControllingUnit {
	
	private boolean showMarkings = false;
	
	private static final Logger logger = Logger
			.getLogger(RandomAITestingControl.class.getName());

	private TablePresentation presentation;
	private GameState state;
	private float accuracy = 0.0005f;
	private int visualizationDepth = 2;
	private CueInteraction cueStrike;
	private TestDataCollector collector = null;
	private boolean collectData = false;

	public RandomAITestingControl(TablePresentation presentation,
			GameState state) {
		this.presentation = presentation;
		this.state = state;
	}

	public RandomAITestingControl(TablePresentation presentation,
			GameState state, TestDataCollector collector) {
		this.presentation = presentation;
		this.state = state;
		this.collector = collector;
		collectData = true;
	}

	public void handlePhase(GamePhase phase) {
		HashMap<String, Object> props = new HashMap<String, Object>();

		props.put(ThirdPersonMouseLook.PROP_MAXROLLOUT, "3");
		props.put(ThirdPersonMouseLook.PROP_MINROLLOUT, "1");
		props.put(ThirdPersonMouseLook.PROP_MINASCENT, "" + 45
				* FastMath.DEG_TO_RAD);
		props.put(ThirdPersonMouseLook.PROP_MAXASCENT, "" + 45
				* FastMath.DEG_TO_RAD);
		props.put(ChaseCamera.PROP_INITIALSPHERECOORDS, new Vector3f(3, 0,
				30 * FastMath.DEG_TO_RAD));
		props.put(ChaseCamera.PROP_WORLDUPVECTOR, new Vector3f(0, 0, 1));

		props.put(ChaseCamera.PROP_TARGETOFFSET, new Vector3f(0, 0, 0));

		ChaseCamera chaser = new ChaseCamera(presentation.getCamera(),
				presentation.getTableNode(), props);
		chaser.setMinDistance(state.getTable().getLength());
		chaser.setMaxDistance(state.getTable().getLength());

		presentation.setCameraControl(chaser);

		if (phase instanceof FrameInitPhase) {
			handleInit((FrameInitPhase) phase);
		} else if (phase instanceof PositioningPhase) {
			handlePositioning((PositioningPhase) phase);
		} else if (phase instanceof AimingPhase) {
			handleAiming((AimingPhase) phase);
		} else if (phase instanceof SimulationPhase) {
			handleSimulation((SimulationPhase) phase);
		}
	}

	private void handleInit(FrameInitPhase phase) {
		presentation.initBalls();
	}

	private void handleSimulation(SimulationPhase phase) {
		presentation.setGameControl(new SimulationControl(presentation, phase,
				state, collector));
	}

	private void handlePositioning(PositioningPhase phase) {
		phase.setPosition(state.getTable().getBrownSpot().x
				+ state.getTable().getDRadius() / 2f, state.getTable()
				.getBrownSpot().y
				- state.getTable().getDRadius() / 2f);
		phase.finish();
	}

	private void handleAiming(AimingPhase phase) {
		toggleMarkings();

		RandomCueInteractionGenerator randomCIGenerator = new RandomCueInteractionGenerator();
		SimpleScoreEvaluator simpleScoreEvaluator = new SimpleScoreEvaluator();
		simpleScoreEvaluator.setCriticalScore(0.2);

		long before = System.currentTimeMillis();
		int count = 0;

		do {
			count++;
			cueStrike = randomCIGenerator.generate(state);
		} while (!simpleScoreEvaluator.evaluate(cueStrike, randomCIGenerator
				.getTarget(), state));

		float time = (System.currentTimeMillis() - before) / 1000f;
		logger.log(Level.FINE, "Generated " + count + " shots in " + time
				+ "s : " + count / time + " shots/s");

		Set<BilliardBall> plannedPottedBalls = simpleScoreEvaluator
				.getPlannedOutcomeSet();
		if (collectData) {
			collector.registerPlannedBalls(plannedPottedBalls);
		}

		phase.setAngDest(cueStrike.getAngDest());
		phase.setAngElev(cueStrike.getAngElev());
		phase.setTransX(cueStrike.getTransX());
		phase.setTransY(cueStrike.getTransY());
		phase.setVelocity(cueStrike.getVelocity());

		presentation.showShotVisualization(true);
		toggleMarkings();

		showAimLine();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		presentation.showShotVisualization(false);

		phase.finish();
	}

	private void showAimLine() {

		BilliardBall simCueBall = state.getCueBall();

		Event cueShot = new CueInteraction(0,
				state.getCurrentPlayer().getCue(), simCueBall, cueStrike
						.getAngDest(), cueStrike.getAngElev(), cueStrike
						.getTransX(), cueStrike.getTransY(), cueStrike
						.getVelocity());

		Set<Event> initEvents = new HashSet<Event>();
		initEvents.add(cueShot);

		final Simulation sim = new InTimeSimulation(new HashSet<BilliardBall>(
				state.getBalls()), state.getTable());
		sim.init(initEvents);

		class AimingHandler implements EventHandler {
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

			public void addPointFromMonitoredBall() {
				addPoint(monitoredBall.getPosition().clone());

			}

			public void addPoint(Vector3f point) {
				point.z = monitoredBall.getRadius();
				point.x = point.x - (0.5f * state.getTable().getWidth());
				point.y = point.y - (0.5f * state.getTable().getLength());
				points.add(point);
			}
		}

		AimingHandler aimingHandler = new AimingHandler();
		aimingHandler.monitoredBall = (BilliardBall) simCueBall.clone();
		aimingHandler.linePointsList = new ArrayList<ArrayList<Vector3f>>();
		aimingHandler.points = new ArrayList<Vector3f>();
		aimingHandler.addPointFromMonitoredBall();
		sim.addEventHandler(aimingHandler);

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
	private void toggleMarkings() {
		if (!showMarkings) {
			((SnookerTable3D) presentation).showMarkings(true);
			((SnookerTable3D) presentation).markBalls(state.getOnBalls());
			showMarkings = !showMarkings;
		} else {
			((SnookerTable3D) presentation).showMarkings(false);
			showMarkings = !showMarkings;
		}
	}
}
