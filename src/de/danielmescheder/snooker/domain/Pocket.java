package de.danielmescheder.snooker.domain;


import com.jme.math.Vector3f;

import de.danielmescheder.snooker.simulation.event.Event;
import de.danielmescheder.snooker.simulation.event.PocketingEvent;
import de.danielmescheder.snooker.simulation.physics.Physics;

/**
 * The Pocket class is used to represent a table-pocket in the simulation. A
 * ball will be counted as pocketed at the point of the collision event.
 * 
 * @author tim
 * 
 */
public class Pocket extends SphericalGameObject {
	private float outerRadius;
	private Vector3f orientation;

	/**
	 * Constructs a new pocket
	 * 
	 * @param position
	 *            the position of the pocket in 3D-space
	 * @param outerRadius
	 *            the pocket radius in meters
	 * @param orientation
	 *            the unit vector that indicates the pockets orientation on the
	 *            table (thus, the direction of the point that is furthest
	 *            inside the table.
	 */
	public Pocket(Vector3f position, float outerRadius, Vector3f orientation) {
		setPosition(position);
		setRadius(outerRadius);
		setOuterRadius(outerRadius);
		this.orientation = orientation;
	}

	/**
	 * Get the unit vector that indicates the pockets orientation on the table
	 * (thus, the direction of the point that is furthest inside the table.
	 * 
	 * @return unit vector
	 */
	public Vector3f getOrientation() {
		return orientation;
	}

	/**
	 * Sets the radius of the pocket
	 * @param outerRadius the radius in meters
	 */
	public void setOuterRadius(float outerRadius) {
		this.outerRadius = outerRadius;
	}

	/**
	 * Gets the radius of the pocket
	 * @return the radius
	 */
	public float getOuterRadius() {
		return outerRadius;
	}

	@Override
	public Event findCollision(BilliardBall b) {
		setRadius(outerRadius - b.getRadius());
		double time = Physics.collisionTime(b, this);

		if (time >= 0) {
			return new PocketingEvent((float) (b.getTime() + time), b, this);
		} else {
			return null;
		}
	}
}
