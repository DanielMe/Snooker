package de.danielmescheder.snooker.simulation.event;

import java.util.Set;

import de.danielmescheder.snooker.domain.CollidableGameObject;


/**
 * The Collision interface unites the query for all {@link CollidableGameObject}s
 * involved in such Collision.
 * 
 */
public interface Collision {
	public Set<CollidableGameObject> getColidableObjects();
}
