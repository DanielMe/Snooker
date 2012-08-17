package de.danielmescheder.snooker.control.ui;

import java.util.HashMap;


import com.jme.input.ChaseCamera;
import com.jme.input.thirdperson.ThirdPersonMouseLook;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;

import de.danielmescheder.snooker.control.ControllingUnit;
import de.danielmescheder.snooker.control.ui.controller.AimingControl;
import de.danielmescheder.snooker.control.ui.controller.PositioningControl;
import de.danielmescheder.snooker.control.ui.controller.SimulationControl;
import de.danielmescheder.snooker.gameflow.GamePhase;
import de.danielmescheder.snooker.gameflow.GameState;
import de.danielmescheder.snooker.gameflow.phases.AimingPhase;
import de.danielmescheder.snooker.gameflow.phases.FrameInitPhase;
import de.danielmescheder.snooker.gameflow.phases.PositioningPhase;
import de.danielmescheder.snooker.gameflow.phases.SimulationPhase;
import de.danielmescheder.snooker.presentation.TablePresentation;

/**
 * This implementation of {@link ControllingUnit} handles the control of the
 * game by a human player
 * 
 * @author tim
 * 
 */
public class ManualGameControl implements ControllingUnit {

	private TablePresentation presentation;
	private GameState state;

	/**
	 * Constructs a ManualGameControl with a given {@link TablePresentation} and {@link GameState}
	 * @param presentation the TablePresentation to act upon
	 * @param state the current GameState
	 */
	public ManualGameControl(TablePresentation presentation, GameState state) {
		this.presentation = presentation;
		this.state = state;
	}

	@Override
	public void handlePhase(GamePhase phase) {

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
		presentation.setGameControl(new SimulationControl(presentation, phase,
				state));

	}

	private void handlePositioning(PositioningPhase phase) {
		HashMap<String, Object> props = new HashMap<String, Object>();

		props.put(ThirdPersonMouseLook.PROP_MAXROLLOUT, "3");
		props.put(ThirdPersonMouseLook.PROP_MINROLLOUT, "1");
		props.put(ThirdPersonMouseLook.PROP_MINASCENT, "" + 30
				* FastMath.DEG_TO_RAD);
		props.put(ThirdPersonMouseLook.PROP_MAXASCENT, "" + 30
				* FastMath.DEG_TO_RAD);
		props.put(ChaseCamera.PROP_STAYBEHINDTARGET, true);

		props.put(ChaseCamera.PROP_WORLDUPVECTOR, new Vector3f(0, 0, 1));
		props.put(ChaseCamera.PROP_INITIALSPHERECOORDS, new Vector3f(3, 0,
				30 * FastMath.DEG_TO_RAD));
		props.put(ChaseCamera.PROP_TARGETOFFSET, new Vector3f(0, 0, 0));

		ChaseCamera chaser = new ChaseCamera(presentation.getCamera(),
				presentation.getCueBallNode(), props) {
			@Override
			protected void setupMouse() {
			}
		};
		// chaser.addAction(new Mover());
		chaser.setMaxDistance(3);
		chaser.setMinDistance(1);

		presentation.setCameraControl(chaser);
		presentation.setGameControl(new PositioningControl(presentation, phase,
				state));
	}

	private void handleAiming(AimingPhase phase) {
		HashMap<String, Object> props = new HashMap<String, Object>();

		props.put(ThirdPersonMouseLook.PROP_MAXROLLOUT, "3");
		props.put(ThirdPersonMouseLook.PROP_MINROLLOUT, "1");
		props.put(ThirdPersonMouseLook.PROP_MINASCENT, "" + 15
				* FastMath.DEG_TO_RAD);
		props.put(ThirdPersonMouseLook.PROP_MAXASCENT, "" + 30
				* FastMath.DEG_TO_RAD);
		props.put(ChaseCamera.PROP_STAYBEHINDTARGET, true);

		props.put(ChaseCamera.PROP_WORLDUPVECTOR, new Vector3f(0, 0, 1));
		props.put(ChaseCamera.PROP_INITIALSPHERECOORDS, new Vector3f(2, 0,
				15 * FastMath.DEG_TO_RAD));
		props.put(ChaseCamera.PROP_TARGETOFFSET, new Vector3f(0, 0, .3f));

		ChaseCamera chaser = new ChaseCamera(presentation.getCamera(),
				presentation.setCueToBall(presentation.getCueBallNode()), props) {
			@Override
			protected void setupMouse() {
			}
		};

		chaser.setMaxDistance(3);
		chaser.setMinDistance(1);

		presentation.setCameraControl(chaser);
		presentation.setGameControl(new AimingControl(state, chaser,
				presentation, phase));

	}

}
