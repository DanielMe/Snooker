package de.danielmescheder.snooker.domain;

/**
 * The Cue class stores the properties of a cue.
 *
 */
public class Cue extends GameObject
{
	private float mass;
	
	/**
	 * Construcs a new cue with a given mass.
	 * @param mass The cue-mass in kg
	 */
	
	public Cue(float mass)
	{
		this.mass = mass;
	}
	
	/**
	 * Gets the cue-mass
	 * @return mass
	 */
	public float getMass()
	{
		return mass;
	}
}
