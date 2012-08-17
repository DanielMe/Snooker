package de.danielmescheder.snooker.control.ui.controller;

import com.jme.input.ChaseCamera;
import com.jme.input.InputHandler;
import com.jme.input.MouseInput;
import com.jme.input.action.InputActionEvent;
import com.jme.input.action.MouseInputAction;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;

import de.danielmescheder.snooker.control.AimLine;
import de.danielmescheder.snooker.gameflow.GameState;
import de.danielmescheder.snooker.gameflow.phases.AimingPhase;
import de.danielmescheder.snooker.presentation.SnookerTable3D;
import de.danielmescheder.snooker.presentation.TablePresentation;
import de.danielmescheder.snooker.simulation.event.CueInteraction;

public class AimingControl extends InputHandler
{
	/**
	 * The AimingControl offers controls for the {@link SnookerTable3D} graphic
	 * representation to aim during the {@link AimingPhase}.
	 * 
	 */
	public AimingControl(final GameState state, final ChaseCamera camControl, final TablePresentation presentation, final AimingPhase phase)
	{
		presentation.showShotVisualization(true);
		((SnookerTable3D) presentation).showMarkings(true);
		((SnookerTable3D) presentation).markBalls(state.getOnBalls());
		// final Node object = presentation.getCueBallNode();

		class AimingMouseInput extends MouseInputAction
		{
			private final float maxV = 4.5f;

			private float angX = 0;
			private float angY = 0;

			private float transX = 0, transY = 0;
			private float velocity = 0;

			private long lastUpdate = 0;
			private float updateFrequency = 50;

			private float accuracy = 0.0005f;
			private int visualizationDepth = 2;

			private boolean goNext = false;
			private boolean finished = false;
			private AimLine aimLine;

			public AimingMouseInput()
			{
				aimLine = new AimLine(presentation, state, visualizationDepth, accuracy);
			}

			@Override
			public void performAction(InputActionEvent evt)
			{
				if (Math.abs(MouseInput.get().getWheelDelta()) > 0)
				{
					float increase = MouseInput.get().getWheelDelta() / (120);
					if (velocity - (0.025f * increase) >= 0 && velocity - (0.025f * increase) <= maxV)
					{
						velocity -= (0.025f * increase);
					}

				}
				if (MouseInput.get().isButtonDown(0))
				{

					goNext = true;
					// float transY = MouseInput.get().getYDelta() * 0.005f;
					// cue.getLocalTranslation().addLocal(0, 0, transY);
				}
				else if (goNext)
				{
					presentation.removeCue();
					phase.setAngDest(angX);
					phase.setAngElev(angY);
					phase.setTransX(transX);
					phase.setTransY(transY);

					phase.setVelocity(velocity);
					finished = true;

					phase.finish();
					goNext = false;
				}
				else
				{

					float x = MouseInput.get().getXDelta() * 0.0005f;
					float y = MouseInput.get().getYDelta() * 0.0005f;

					if (MouseInput.get().isButtonDown(1))
					{
						camControl.setTargetOffset(new Vector3f(0, 0, .3f));
						camControl.setMaxDistance(1.5f);
						camControl.setMinDistance(1f);

						float viableRadius = state.getCueBall().getRadius() * .9f;

						if (FastMath.sqr(transX + x) + FastMath.sqr(transY + y) > FastMath.sqr(viableRadius))
						{
							float angle = FastMath.atan2(transY + y, transX + x);
							transX = viableRadius * FastMath.cos(angle);
							transY = viableRadius * FastMath.sin(angle);
						}
						else
						{
							transX += x;
							transY += y;
						}
						presentation.transposeCue(transX, transY);

					}
					else if (MouseInput.get().isButtonDown(2))
					{
						if (velocity - (y * 0.5) >= 0 && velocity - (y * 0.5) <= maxV)
						{
							velocity -= (y * 0.5);

						}

					}
					else
					{
						camControl.setMaxDistance(3);
						camControl.setMinDistance(1);
						angX += x;
						if (y + angY > 0 && y + angY < FastMath.PI / 2)
						{
							angY += y;
						}
						presentation.rotateCue(angX, angY);

					}
					presentation.setCueDistance(velocity / 10);
				}
				if (!finished)
				{
					long time = System.currentTimeMillis();
					if ((time - lastUpdate) > updateFrequency)
					{
						CueInteraction ci = new CueInteraction(0, state.getCurrentPlayer().getCue(), state.getCueBall(), angX, angY, transX, transY, velocity);
						aimLine.showAimLine(ci);
						lastUpdate = time;
					}
				}

			}
		}
		this.addAction(new AimingMouseInput());
	}

}
