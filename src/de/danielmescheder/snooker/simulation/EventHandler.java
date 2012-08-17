package de.danielmescheder.snooker.simulation;

import de.danielmescheder.snooker.simulation.event.Event;

/**
 * The EventHandler interface unifies the concept of building a
 * {@link Simulation} which generates and acts upon a certain {@link Event} This
 * makes EventHandlers one of the basic building blocks of the program.
 */
public interface EventHandler {
	public void handle(Event e);
}
