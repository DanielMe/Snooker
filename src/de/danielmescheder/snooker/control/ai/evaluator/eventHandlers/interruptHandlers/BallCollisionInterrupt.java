package de.danielmescheder.snooker.control.ai.evaluator.eventHandlers.interruptHandlers;

import de.danielmescheder.snooker.simulation.EventHandler;
import de.danielmescheder.snooker.simulation.Simulation;
import de.danielmescheder.snooker.simulation.event.BallCollision;
import de.danielmescheder.snooker.simulation.event.Event;
/**
 * The {@link BallCollisionInterrupt} pauses a simulation in case a certain
 * number of {@link BallCollision}s has occurred. This serves as a heuristic
 * to quickly discard non-viable {@link Simulation}s.
 *  
 */
public class BallCollisionInterrupt implements EventHandler{
	
	private Simulation sim;
	private int maxCollisions = 0;
	private int collisionCount = 0;
	public BallCollisionInterrupt(Simulation sim,int maxCollisions ){
		this.sim = sim;
		this.maxCollisions = maxCollisions;
	}
	@Override
	public void handle(Event e){
		if (e instanceof BallCollision)
		{
			collisionCount++;
			
			if (collisionCount > maxCollisions)
			{
				sim.pause();
			}
		}
		
	}

}
