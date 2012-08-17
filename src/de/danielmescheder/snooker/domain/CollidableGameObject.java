package de.danielmescheder.snooker.domain;


import com.jme.math.Vector3f;

import de.danielmescheder.snooker.simulation.event.Event;

/**
 * The abstract definition of a CollidableGameObject
 *
 */
public abstract class CollidableGameObject extends GameObject
{
	
	private Vector3f position = Vector3f.ZERO.clone();
	
	/**
	 * Gets the position of this object
	 * @return the position
	 */
	public Vector3f getPosition()
	{
		return position;
	}

	/**
	 * Set the current position of this object
	 * @param position the new position
	 */
	public void setPosition(Vector3f position)
	{
		this.position = position;
	}
	
	
	@Override
	public String toString()
	{
		return "[C p:"+position+" "+super.toString()+"]";
	}
	
	/**
	 * Returns the a collision event between this object and another.
	 * @param b the object to be checked against
	 * @return the collision event
	 */
	public abstract Event findCollision(BilliardBall b);
	
}
