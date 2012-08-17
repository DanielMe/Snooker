package de.danielmescheder.snooker.domain;

import java.util.HashSet;


import com.jme.math.Vector3f;

import de.danielmescheder.snooker.simulation.event.BallCollision;
import de.danielmescheder.snooker.simulation.event.Event;
import de.danielmescheder.snooker.simulation.physics.BallMotion;
import de.danielmescheder.snooker.simulation.physics.Physics;

/**
 * The BilliardBall implementation of the Ball abstract class.
 *
 */
public class BilliardBall extends Ball
{
	public enum Type
	{
		CUE(0), 
		RED(1),
		YELLOW(2), 
		GREEN(3), 
		BROWN(4),
		BLUE(5),
		PINK(6), 
		BLACK(7);

		private int value;
		Type(int value)
		{
			this.value = value;
		}
		
		public int getValue()
		{
			return value;
		}
	}

	private int id;
	private Type type;
	
	/**
	 * Constructs a BilliardBall object with a certain ID and a ball-type.
	 * 
	 * @param id The ID that is used to refer to this Object
	 * @param type The ball-type
	 */
	public BilliardBall(int id, Type type)
	{
		this.id = id;
		this.type = type;
	}
	
	/**
	 * Constructs a BilliardBall object with a certain ID and a ball-type
	 * at a given point in a 3D space.
	 * 
	 * @param id The ID that is used to refer to this Object
	 * @param type The ball-type
	 * @param position the position of the center coordinate
	 */
	public BilliardBall(int id, Type type, Vector3f position)
	{
		this(id, type);
		setPosition(position);
	}
	
	/**
	 * Constructs a BilliardBall object of type RED wit a certain ID
	 * @param id The ID that is used to refer to this Object
	 */
	public BilliardBall(int id)
	{
		this(id, Type.RED);
	}
	
	/**
	 * Gets the ID of the ball
	 * @return the ID
	 */
	public int getID()
	{
		return id;
	}
	
	/**
	 * Sets the ID of a ball
	 * @param id the new ID
	 */
	public void setID(int id)
	{
		this.id = id;
	}
	
	/**
	 * Gets the type of the ball
	 * @return the ball-type
	 */
	public Type getType()
	{
		return type;
	}
	
	/**
	 * Sets the type of the ball
	 * @param type new ball-type
	 */
	public void setType(Type type)
	{
		this.type = type;
	}

	@Override
	/**
	 * Checks if a given object equals the current billiard ball.
	 * @return true if the objects are equal, false otherwise 
	 */
	public boolean equals(Object b2)
	{
		if (b2 == null || !(b2 instanceof BilliardBall))
		{
			return false;
		}
		return ((BilliardBall) b2).getID() == id;
	}

	
	@Override
	public int hashCode()
	{
		return id;
	}
	

	@Override
	public Object clone()
	{
		BilliardBall copy = new BilliardBall(id, type);
		copy.setPosition(getPosition().clone());
		copy.setVelocity(getVelocity().clone());
		copy.setAngularVelocity(getAngularVelocity().clone());
		copy.setRadius(getRadius());
		copy.setMass(getMass());
		copy.setTime(getTime());
		copy.setState(getState());
		copy.setFriction(getFriction());
		return copy;
	}
	
	@Override
	public String toString()
	{
		return "b["+id+","+type.toString() + super.toString()+"]";
	}
	
	@Override
	public Event findCollision(BilliardBall b)
	{
		if (!b.equals(this) && !(b.getState() == BallState.RESTING && this.getState() == BallState.RESTING))
		{
			float currentTime, collisionTime;
			if (this.getTime() == b.getTime())
			{
				currentTime = this.getTime();
				collisionTime = Physics.collisionTime(this, b);
			}
			else
			{
				BilliardBall advanced, behind;
				if (b.getTime() < this.getTime())
				{
					advanced = this;
					behind = b;
				}
				else
				{
					advanced = b;
					behind = this;
				}
				currentTime = advanced.getTime();
				
				BilliardBall adjusted = (BilliardBall) behind.clone();
				BallMotion motion = behind.getState().getMotion();
				
				adjusted.setPosition(motion.getPosition(behind, currentTime));
				adjusted.setVelocity(motion.getVelocity(behind, currentTime));
				adjusted.setAngularVelocity(motion.getAngularVelocity(behind, currentTime));
				
				collisionTime = Physics.collisionTime(advanced, adjusted);
			}

			if (collisionTime >= 0)
			{
				HashSet<BilliardBall> h = new HashSet<BilliardBall>();
				h.add(b);
				h.add(this);
				return new BallCollision(currentTime + collisionTime, h);
			}
			else
			{
				return null;
			}

		}
		return null;
	}
}
