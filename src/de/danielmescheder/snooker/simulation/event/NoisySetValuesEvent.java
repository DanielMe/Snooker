package de.danielmescheder.snooker.simulation.event;

import java.util.Random;


import com.jme.math.Vector3f;

import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.domain.Ball.BallState;

/**
 * The NoisySetValuesEvent has the same purpose as the {@link SetValuesEvent}
 * but it incorporates gaussian noise into the values. The idea is that in this
 * way, also SetValuesEvents can be sampled.
 * 
 */
public class NoisySetValuesEvent extends SetValuesEvent {

	private static Random r;

	static {
		r = new Random();
	}

	private static Vector3f setVelNoise(Vector3f velocity, float velDev) {
		velocity.x = (float) (velocity.x + velDev * r.nextGaussian());
		velocity.y = (float) (velocity.y + velDev * r.nextGaussian());
		return velocity;
	}

	private static Vector3f setAngVelNoise(Vector3f angVel, float angVelDev) {
		angVel.x = (float) (angVel.x + angVelDev * r.nextGaussian());
		angVel.y = (float) (angVel.y + angVelDev * r.nextGaussian());
		return angVel;
	}

	public NoisySetValuesEvent(float time, BilliardBall ball, Vector3f v,
			Vector3f av, BallState state, float velDev, float angVelDev) {
		super(time, ball, setVelNoise(v, velDev),
				setAngVelNoise(av, angVelDev), state);
	}

}
