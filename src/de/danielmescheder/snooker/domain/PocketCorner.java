package de.danielmescheder.snooker.domain;


import com.jme.math.Vector3f;

import de.danielmescheder.snooker.simulation.event.Event;
import de.danielmescheder.snooker.simulation.event.PocketCornerCollision;
import de.danielmescheder.snooker.simulation.physics.Physics;

/**
 * The PocketCorner represents the rounded edges around a pocket

 */
public class PocketCorner extends SphericalGameObject
{

	/**
	 * Constructs a new pocket corner of a certain radius at a given position.
	 * @param position The position of the pocket corner
	 * @param radius The radius
	 */
	public PocketCorner(Vector3f position, float radius)
	{
		setPosition(position);
		setRadius(radius);
	}

	@Override
	public Event findCollision(BilliardBall b)
	{
		double time = Physics.collisionTime(b, this);
		
		if(time>=0)
		{
			return new PocketCornerCollision((float) (b.getTime() + time), b,this);
		}
		else
		{
			return null;
		}
	}
}
