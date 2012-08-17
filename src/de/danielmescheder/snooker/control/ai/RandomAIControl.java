package de.danielmescheder.snooker.control.ai;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme.input.ChaseCamera;
import com.jme.input.thirdperson.ThirdPersonMouseLook;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;

import de.danielmescheder.snooker.control.AimLine;
import de.danielmescheder.snooker.control.ControllingUnit;
import de.danielmescheder.snooker.control.ai.evaluator.SimpleScoreEvaluator;
import de.danielmescheder.snooker.control.ai.generator.RandomCueInteractionGenerator;
import de.danielmescheder.snooker.control.ui.controller.SimulationControl;
import de.danielmescheder.snooker.gameflow.GamePhase;
import de.danielmescheder.snooker.gameflow.GameState;
import de.danielmescheder.snooker.gameflow.phases.AimingPhase;
import de.danielmescheder.snooker.gameflow.phases.FrameInitPhase;
import de.danielmescheder.snooker.gameflow.phases.PositioningPhase;
import de.danielmescheder.snooker.gameflow.phases.SimulationPhase;
import de.danielmescheder.snooker.presentation.SnookerTable3D;
import de.danielmescheder.snooker.presentation.TablePresentation;
import de.danielmescheder.snooker.simulation.event.CueInteraction;
import de.danielmescheder.snooker.simulation.event.SingleBallEvent;


/**
 * The RandomAIControl will randomly attempt to pot a ball and will execute the
 * first shot above a certain minimum score.
 * 
 */
public class RandomAIControl implements ControllingUnit {
	private static final Logger logger = Logger.getLogger(RandomAIControl.class
			.getName());

	private TablePresentation presentation;
	private GameState state;
	private float accuracy = 0.0005f;
	private int visualizationDepth = 2;
	private CueInteraction cueStrike;
	private boolean showMarkings = false;

	public RandomAIControl(TablePresentation presentation, GameState state) {
		this.presentation = presentation;
		this.state = state;
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
				state));
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
			enableAimLine(1, cueStrike);

		} while (!simpleScoreEvaluator.evaluate(cueStrike, randomCIGenerator
				.getTarget(), state));

		float time = (System.currentTimeMillis() - before) / 1000f;
		logger.log(Level.FINE, "Generated " + count + " shots in " + time
				+ "s : " + count / time + " shots/s");

		phase.setAngDest(cueStrike.getAngDest());
		phase.setAngElev(cueStrike.getAngElev());
		phase.setTransX(cueStrike.getTransX());
		phase.setTransY(cueStrike.getTransY());
		phase.setVelocity(cueStrike.getVelocity());

		enableAimLine(1000, cueStrike);
		toggleMarkings();
		phase.finish();
	}

	private void enableAimLine(long time, SingleBallEvent e) {
		presentation.showShotVisualization(true);
		AimLine aimLine = new AimLine(presentation, state, visualizationDepth,
				accuracy);
		aimLine.showAimLine(e);
		try {
			Thread.sleep(time);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		presentation.showShotVisualization(false);
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
