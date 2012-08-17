package de.danielmescheder.snooker.domain;

import java.util.HashSet;
import java.util.Set;


import com.jme.math.Vector3f;

import de.danielmescheder.snooker.simulation.physics.BallMotion;
import de.danielmescheder.snooker.simulation.physics.Physics;

/**
 * This abstract class defines the properties of a ball-like object and keeps
 * track of its parameters.
 */

public abstract class Ball extends SphericalGameObject {
	public enum BallState {
		RESTING(Physics.getRestingMotion()), SLIDING(Physics.getSlidingMotion()), ROLLING(
				Physics.getRollingMotion());

		BallState(BallMotion motion) {
			this.motion = motion;
		}

		public BallMotion getMotion() {
			return motion;
		}

		private BallMotion motion;
	}

	/**
	 * Constructs a ball with standard properties.
	 */
	public Ball() {
		tiles = new HashSet<Tile>();
	}

	/**
	 * Constructs a new ball at a certain position.
	 * 
	 * @param position
	 *            the center coordinate of the ball
	 */
	public Ball(Vector3f position) {
		setPosition(position);
	}

	/**
	 * Gets current angular velocity.
	 * 
	 * @return the angular velocity
	 */
	public final Vector3f getAngularVelocity() {
		return angularVelocity;
	}

	/**
	 * Sets the current angular velocity.
	 * 
	 * @param av
	 *            new angular velocity
	 */
	public  void setAngularVelocity(Vector3f av) {
		this.angularVelocity = av;
	}

	/**
	 * Gets the current velocity.
	 * 
	 * @return the current velocity
	 */
	public  Vector3f getVelocity() {
		return velocity;
	}

	/**
	 * Sets the current velocity.
	 * 
	 * @param velocity
	 *            the new velocity
	 */
	public  void setVelocity(Vector3f velocity) {
		this.velocity = velocity;
	}

	/**
	 * Gets the ball's current time.
	 * 
	 * @return current time
	 */
	public  float getTime() {
		return time;
	}

	/**
	 * Sets the ball's current time.
	 * 
	 * @param time
	 *            the new time
	 */
	public  void setTime(float time) {
		this.time = time;
	}

	@Override
	/*
	 * * Returns a string containing the ball's information.
	 * 
	 * @return string representing ball
	 */
	public String toString() {
		return "t:" + time + ", p:" + getPosition().x + "|" + getPosition().y
				+ "|" + getPosition().z + ", v:" + velocity.x + "|"
				+ velocity.y + "|" + velocity.z + ", |v|:" + velocity.length()
				+ ", av:" + angularVelocity.x + "|" + angularVelocity.y + "|"
				+ angularVelocity.z + "|u|:"
				+ Physics.getRelativeVelocity(this).length() + "Mass: "
				+ this.getMass() + ", state:" + this.getState();
	}

	/**
	 * Gets the current BallState.
	 * 
	 * @return current BallState
	 */
	public  BallState getState() {
		return state;
	}

	/**
	 * Sets the current BallState.
	 * 
	 * @param state
	 *            new BallState
	 */
	public  void setState(BallState state) {
		this.state = state;
	}

	/**
	 * Sets the mass of the ball.
	 * 
	 * @param mass
	 *            mass in kg
	 */
	public  void setMass(float mass) {
		this.mass = mass;
	}

	/**
	 * Gets the ball's mass.
	 * 
	 * @return the mass
	 */
	public  float getMass() {
		return mass;
	}

	/**
	 * Gets the current friction.
	 * 
	 * @return the friction
	 */
	public  float getFriction() {
		return friction;
	}

	/**
	 * Sets the current friction.
	 * 
	 * @param friction
	 *            the new friction
	 */
	public  void setFriction(float friction) {
		this.friction = friction;
	}

	/**
	 * Gets the set of tiles that a ball is currently registered to.
	 * 
	 * @return a set of tiles
	 */
	public  Set<Tile> getTiles() {
		return tiles;
	}

	/**
	 * Sets the tiles that a ball is currently registered to.
	 * 
	 * @param tiles
	 *            the new set of tiles
	 */
	public  void setTiles(Set<Tile> tiles) {
		this.tiles = tiles;
	}

	private Vector3f velocity = Vector3f.ZERO.clone();
	private Vector3f angularVelocity = Vector3f.ZERO.clone();
	private float time;
	private float mass;
	private BallState state = BallState.RESTING;
	private float friction;
	private Set<Tile> tiles;

}
