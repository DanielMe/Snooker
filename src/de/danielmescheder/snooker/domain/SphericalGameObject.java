package de.danielmescheder.snooker.domain;

/**
 * The SphericalGameObject abstract class defines the properties
 * of a sphere.
 *
 */
public abstract class SphericalGameObject extends CollidableGameObject
{
	/**
	 * Get the radius of the sphere
	 * @return the radius in meters
	 */
	public float getRadius()
	{
		return radius;
	}

	/**
	 * Sets the radius of the sphere
	 * @param d the radius in meters
	 */
	public void setRadius(float d)
	{
		this.radius = d;
	}
	
	private float radius;

	@Override
	public String toString()
	{
		return "[S r:"+this.radius +" " + super.toString() +"]";
	}

}
