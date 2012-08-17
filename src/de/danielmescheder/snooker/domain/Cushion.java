package de.danielmescheder.snooker.domain;


import com.jme.math.Vector3f;

import de.danielmescheder.snooker.simulation.event.CushionCollision;
import de.danielmescheder.snooker.simulation.event.Event;
import de.danielmescheder.snooker.simulation.physics.Physics;

/**
 * The Cushion class provides information about a billiard table cushion.
 * 
 */
public class Cushion extends CollidableGameObject {

	/**
	 * The Orientation enum defines the orientation of a certain object.
	 */
	public enum Orientation {
		NORTH, EAST, SOUTH, WEST;
	}

	private Orientation orientation;
	private float length;

	/**
	 * Construcs a new Cushion at a given position and orientation with a
	 * certain length.
	 * 
	 * @param position
	 *            The starting point of the cushion
	 * @param length
	 *            The length of the cushion im meters
	 * @param orientation
	 *            The orientation of the new Cushion
	 */
	public Cushion(Vector3f position, float length, Orientation orientation) {
		setPosition(position);
		setLength(length);
		this.orientation = orientation;
	}

	/**
	 * Sets the length of the cushion
	 * 
	 * @param length
	 *            the new length in meters
	 */
	public void setLength(float length) {
		this.length = length;
	}

	/**
	 * Gets the length of the cushion
	 * 
	 * @return the length
	 */
	public float getLength() {
		return this.length;
	}

	/**
	 * Gets the current orientation of the cushion
	 * 
	 * @return the current orientation
	 */
	public Orientation getOrientation() {
		return orientation;
	}

	/**
	 * Sets the current orientation of the cushion
	 * 
	 * @param orientation
	 *            the new orientation
	 */
	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
	}

	@Override
	public String toString() {
		return "[Cushion l:" + this.length + " o:" + this.orientation + " "
				+ super.toString() + "]";
	}

	@Override
	public Event findCollision(BilliardBall b) {
		double time = Physics.collisionTime(b, this);

		if (time >= 0) {
			return new CushionCollision((float) (b.getTime() + time), b, this);
		} else {
			return null;
		}
	}

}
