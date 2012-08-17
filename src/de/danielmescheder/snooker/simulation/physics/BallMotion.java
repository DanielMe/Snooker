package de.danielmescheder.snooker.simulation.physics;

import com.jme.math.Vector3f;

import de.danielmescheder.snooker.domain.Ball;


/**
 * The BallMotion interface defines the basic query methods to get a
 * {@link Ball}'s angular velocity, velocity, position, the duration - and the
 * friction of the current {@link BallState}.
 * 
 */
public interface BallMotion {
	public Vector3f getAngularVelocity(Ball initial, float time);

	public Vector3f getVelocity(Ball initial, float time);

	public Vector3f getPosition(Ball initial, float time);

	public float getDuration(Ball initial);

	public float getFriction(Ball initial);
}
