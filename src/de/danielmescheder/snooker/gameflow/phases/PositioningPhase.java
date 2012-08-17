package de.danielmescheder.snooker.gameflow.phases;

import de.danielmescheder.snooker.gameflow.GamePhase;
import de.danielmescheder.snooker.gameflow.GameState;

/**
 * The positioning phase allows for repositioning of the cue ball
 * 
 */
public class PositioningPhase extends GamePhase
{
	/**
	 * Construct new PositioningPhase
	 * @param state the current State
	 */
	public PositioningPhase(GameState state)
	{
		super(state);


	}

	@Override
	public void start()
	{
		super.start();
		state.getCueBall().setPosition(state.getTable().getBrownSpot().add(state.getTable().getDRadius()/2f, -state.getTable().getDRadius()/2f, 0));

	}

	/**
	 * Set the position of the cue ball
	 * @param x the X-Coordinate in meters
	 * @param y the Y-Coordinate in meters
	 */
	public void setPosition(float x, float y)
	{
		state.getCueBall().getPosition().set(x, y, 0);
	}

	@Override
	public void finish()
	{
		if(finished)
		{
			return;
		}
		setNext(new AimingPhase(state));
		super.finish();
	}
}