package de.danielmescheder.snooker.control.ui.controller;

import com.jme.input.InputHandler;
import com.jme.input.MouseInput;
import com.jme.input.action.InputActionEvent;
import com.jme.input.action.MouseInputAction;
import com.jme.math.FastMath;
import com.jme.scene.Node;

import de.danielmescheder.snooker.gameflow.GameState;
import de.danielmescheder.snooker.gameflow.phases.PositioningPhase;
import de.danielmescheder.snooker.presentation.SnookerTable3D;
import de.danielmescheder.snooker.presentation.TablePresentation;

/**
 * The PositioningControl offers controls for the {@link SnookerTable3D} graphic
 * representation to place the cue-ball on the {@link Table} during the
 * {@link PositioningPhase}.
 * 
 */

public class PositioningControl extends InputHandler {
	public PositioningControl(final TablePresentation presentation,
			final PositioningPhase phase, final GameState state) {
		final Node object = presentation.getCueBallNode();
		presentation.updateBall(state.getCueBall());
		class PositioningMouseInput extends MouseInputAction {
			private boolean goNext = false;
			private float x, y;

			@Override
			public void performAction(InputActionEvent evt) {
				if (MouseInput.get().isButtonDown(0)) {
					goNext = true;
				} else if (goNext) {
					phase.setPosition(state.getTable().getWidth() / 2f
							+ object.getLocalTranslation().getX(), state
							.getTable().getLength()
							/ 2f + object.getLocalTranslation().getY());

					phase.finish();
					goNext = false; 
				}

				float transX = MouseInput.get().getXDelta() * 0.001f;
				float transY = MouseInput.get().getYDelta() * 0.001f;


				object.getLocalRotation().fromAngles(-1f, 0, 0);

				float newX = object.getLocalTranslation().x + transX;
				float newY = object.getLocalTranslation().y + transY;

				newY = Math.min(newY, -state.getTable().getLength() / 2f
						+ state.getTable().getBaulkLineDistance());

				float distanceX = newX
						- (-state.getTable().getWidth() / 2f + state.getTable()
								.getBrownSpot().x);
				float distanceY = newY
						- (-state.getTable().getLength() / 2f + state
								.getTable().getBrownSpot().y);

				if (FastMath.sqr(distanceX) + FastMath.sqr(distanceY) > FastMath
						.sqr(state.getTable().getDRadius())) {
					float angle = FastMath.atan2(distanceY, distanceX);
					newX = -state.getTable().getWidth() / 2f
							+ state.getTable().getBrownSpot().x
							+ state.getTable().getDRadius()
							* FastMath.cos(angle);
					newY = -state.getTable().getLength() / 2f
							+ state.getTable().getBrownSpot().y
							+ state.getTable().getDRadius()
							* FastMath.sin(angle);
				}

				object.getLocalTranslation().setX(newX);
				object.getLocalTranslation().setY(newY);

				// object.updateGeometricState(evt.getTime(), true);
			}

		}

		this.addAction(new PositioningMouseInput());

	}
}
