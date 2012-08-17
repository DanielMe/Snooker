package de.danielmescheder.snooker.simulation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;

import de.danielmescheder.snooker.adt.def.List;
import de.danielmescheder.snooker.adt.impl.AVLTree;
import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.domain.Table;
import de.danielmescheder.snooker.simulation.event.BallCollision;
import de.danielmescheder.snooker.simulation.event.EnterTileEvent;
import de.danielmescheder.snooker.simulation.event.Event;
import de.danielmescheder.snooker.simulation.event.LeaveTileEvent;
import de.danielmescheder.snooker.simulation.event.PocketingEvent;


/**
 * The PrescheduledSimulation implements a {@link Simulation} which, at
 * initialization, precomputes the events that will occur and stores them in a
 * queue to make playback run smoothly even on slower PCs or in computationally
 * intense situations.
 * 
 */
public class PrescheduledSimulation extends Simulation {
	private Queue<Event> eventQueue;
	private Queue<BallCollision> ballInteractionQueue;
	private Queue<PocketingEvent> pocketCollisionQueue;

	private Map<BilliardBall, BilliardBall> tempBalls;

	public PrescheduledSimulation(Set<BilliardBall> balls, Table table) {
		super(balls, table);
	}

	@Override
	public void init(Set<? extends Event> initialEvents) {
		tempBalls = new HashMap<BilliardBall, BilliardBall>();
		createFromStored(tempBalls);
		events = new AVLTree<Float, Event>(timeComp);

		for (Event e : initialEvents) {
			for (BilliardBall b : e.getBalls(tempBalls)) {

				List<Event> l = ballEvents.get(b).get(null);
				l.insertLast(e);
				e.addListOccurrence(l.last());
				e.setPQOccurrence(events.insertItem(e.getTime(), e));

			}
		}

		preSchedule();
		initials = new HashMap<BilliardBall, BilliardBall>();
		for (BilliardBall b : storedBalls) {
			initials.put(b, (BilliardBall) b.clone());
		}
		super.init(initialEvents);
	}

	@Override
	public Event fetchNextEvent() {
		return eventQueue.element();
	}

	@Override
	public void handleEvent(Event event) {
		logger.log(Level.INFO, "Handling prescheduled event", event);
		super.handleEvent(event);
		eventQueue.remove();
		if (event instanceof PocketingEvent) {
			pocketCollisionQueue.remove();
		}
		if (event instanceof BallCollision) {
			ballInteractionQueue.remove();
		}
	}

	@Override
	public boolean hasNextEvent() {
		return !eventQueue.isEmpty();
	}

	private void preSchedule() {
		eventQueue = new LinkedList<Event>();
		pocketCollisionQueue = new LinkedList<PocketingEvent>();
		ballInteractionQueue = new LinkedList<BallCollision>();

		while (events.size() > 1) {
			Event event = events.minElement();
			if (event instanceof EnterTileEvent) {
				if (tempBalls.get(((EnterTileEvent) event).getBall())
						.getTiles()
						.contains(((EnterTileEvent) event).getTile())) {
					event.removeOccurrences();
					continue;
				}
			}
			updateBallStates(event, tempBalls);
			if (event instanceof EnterTileEvent) {
				queueEvents(((EnterTileEvent) event).getBall(), tempBalls,
						((EnterTileEvent) event).getTile());
				event.removeOccurrences();
			} else if (event instanceof LeaveTileEvent) {
				clearEvents(((LeaveTileEvent) event).getBall(),
						((LeaveTileEvent) event).getTile());
			} else {
				for (BilliardBall ball : event.getBalls(tempBalls)) {

					clearEvents(ball);
					// go through all balls that are involved
					// and do the event scheduling
					queueEvents(ball, tempBalls);
				}
			}

			eventQueue.add(event);
			if (event instanceof PocketingEvent) {
				pocketCollisionQueue.add((PocketingEvent) event);
			}
			if (event instanceof BallCollision) {
				ballInteractionQueue.add((BallCollision) event);
			}
			logger.log(Level.INFO, "Prescheduling", event);

		}
	}

	public BallCollision getNextBallInteraction() {
		return ballInteractionQueue.peek();
	}

	public PocketingEvent getNextPocketCollision() {
		return pocketCollisionQueue.peek();
	}

}
