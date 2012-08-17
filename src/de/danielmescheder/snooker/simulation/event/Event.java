package de.danielmescheder.snooker.simulation.event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.danielmescheder.snooker.adt.def.Position;
import de.danielmescheder.snooker.adt.impl.KeyElementPair;
import de.danielmescheder.snooker.domain.BilliardBall;

/**
 * The abstract class Event provides the basic methods needed to manage the
 * interior structure of all events. The most important method is the handle()
 * method, which is executed every time the event happens. This allows for a
 * flexible way to deal with the different events that occur during a
 * {@link Simulation}.
 * 
 */
public abstract class Event {
	public Event(float time, Set<BilliardBall> balls) {
		this.time = time;
		this.balls = balls;
		this.listOccurrences = new ArrayList<Position<Event>>(2);
	}

	public void addListOccurrence(Position<Event> position) {
		listOccurrences.add(position);
	}

	public void setPQOccurrence(Position<KeyElementPair<Float, Event>> position) {
		pqOccurrence = position;
	}

	public void removeOccurrences() {
		for (Position<Event> p : listOccurrences) {
			p.container().remove(p);
		}
		pqOccurrence.container().remove(pqOccurrence);
		setPQOccurrence(null);
		this.listOccurrences.clear();
	}

	public float getTime() {
		return time;
	}

	public Set<BilliardBall> getBalls(Map<BilliardBall, BilliardBall> target) {
		Set<BilliardBall> balls = new HashSet<BilliardBall>();
		for (BilliardBall b : this.balls) {
			balls.add(target.get(b));
		}
		return balls;
	}

	public Set<BilliardBall> getBallKeys() {
		return this.balls;
	}

	public void setBalls(Set<BilliardBall> balls) {
		this.balls = balls;
	}

	@Override
	public String toString() {
		StringBuffer s = new StringBuffer("Event: "
				+ this.getClass().getSimpleName() + "(t:" + getTime() + ")");
		for (BilliardBall b : balls) {
			s.append(" :: " + b.getID());
		}
		return s.toString();
	}

	public abstract void handle(Map<BilliardBall, BilliardBall> target);

	private float time;
	private Set<BilliardBall> balls;
	private ArrayList<Position<Event>> listOccurrences;
	private Position<KeyElementPair<Float, Event>> pqOccurrence;

}
