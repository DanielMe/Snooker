package de.danielmescheder.snooker.simulation;

import java.util.HashMap;
import java.util.Set;

import de.danielmescheder.snooker.adt.def.List;
import de.danielmescheder.snooker.adt.impl.AVLTree;
import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.domain.Table;
import de.danielmescheder.snooker.simulation.event.EnterTileEvent;
import de.danielmescheder.snooker.simulation.event.Event;
import de.danielmescheder.snooker.simulation.event.LeaveTileEvent;


/**
 * The InTimeSimulation implementation of {@link Simulation} computes all
 * occurring events during playback, making it ideal for just-in-time
 * computations such as those needed in the {@link AimLine}.
 * 
 */
public class InTimeSimulation extends Simulation {

	public InTimeSimulation(Set<BilliardBall> balls, Table table) {
		super(balls, table);
	}

	@Override
	public void init(Set<? extends Event> initEvents) {
		initials = new HashMap<BilliardBall, BilliardBall>();
		events = new AVLTree<Float, Event>(timeComp);
		createFromStored(initials);

		for (Event e : initEvents) {
			for (BilliardBall b : e.getBalls(initials)) {
				List<Event> l = ballEvents.get(b).get(null);
				l.insertLast(e);
				e.addListOccurrence(l.last());
				e.setPQOccurrence(events.insertItem(e.getTime(), e));
			}
		}

		super.init(initEvents);
	}

	@Override
	public Event fetchNextEvent() {
		return events.minElement();
	}

	@Override
	public void handleEvent(Event event) {
		if (event instanceof EnterTileEvent) {
			if (initials.get(((EnterTileEvent) event).getBall()).getTiles()
					.contains(((EnterTileEvent) event).getTile())) {
				event.removeOccurrences();
				return;
			}
		}
		super.handleEvent(event);
		if (event instanceof EnterTileEvent) {
			queueEvents(((EnterTileEvent) event).getBall(), initials,
					((EnterTileEvent) event).getTile());
			event.removeOccurrences();
		} else if (event instanceof LeaveTileEvent) {
			clearEvents(((LeaveTileEvent) event).getBall(),
					((LeaveTileEvent) event).getTile());
		} else {
			for (BilliardBall ball : event.getBalls(initials)) {

				clearEvents(ball);
				// go through all balls that are involved
				// and do the event scheduling
				queueEvents(ball, initials);
			}
		}

	}

	@Override
	public boolean hasNextEvent() {
		return events.size() > 1;
	}

}
