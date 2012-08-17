package de.danielmescheder.snooker.simulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme.math.FastMath;
import com.jme.math.Vector3f;

import de.danielmescheder.snooker.adt.def.Comparator;
import de.danielmescheder.snooker.adt.def.List;
import de.danielmescheder.snooker.adt.impl.AVLTree;
import de.danielmescheder.snooker.adt.impl.DoubleLinkedList;
import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.domain.CollidableGameObject;
import de.danielmescheder.snooker.domain.Cushion;
import de.danielmescheder.snooker.domain.Pocket;
import de.danielmescheder.snooker.domain.PocketCorner;
import de.danielmescheder.snooker.domain.SphericalGameObject;
import de.danielmescheder.snooker.domain.Table;
import de.danielmescheder.snooker.domain.Tile;
import de.danielmescheder.snooker.domain.Ball.BallState;
import de.danielmescheder.snooker.domain.Cushion.Orientation;
import de.danielmescheder.snooker.simulation.event.EnterTileEvent;
import de.danielmescheder.snooker.simulation.event.Event;
import de.danielmescheder.snooker.simulation.event.LeaveTileEvent;
import de.danielmescheder.snooker.simulation.event.StateChange;
import de.danielmescheder.snooker.simulation.event.TilingEvent;


/**
 * The abstract class Simulation implements all commonly used methods and
 * contains all commonly used ADTs for simulations. Tiling, event-queueing,
 * ball updating and time-advancing are all done in this class. This allows for
 * a simple and efficient way to build specialized simulations on top of it.
 * 
 */
public abstract class Simulation {
	public static double SPACE_THRESHOLD;
	public static double TIME_THRESHOLD;
	protected static final Logger logger = Logger.getLogger(Simulation.class
			.getName());

	protected final int hTiles = 3, vTiles = 4;

	protected float currTime;
	protected boolean paused;
	protected boolean ready;

	protected AVLTree<Float, Event> events;
	protected Map<BilliardBall, Map<Tile, List<Event>>> ballEvents;
	protected Map<BilliardBall, BilliardBall> initials;
	protected Set<BilliardBall> storedBalls;
	protected Table table;
	protected Collection<EventHandler> handlers;

	protected Tile[][] storedTiles;
	protected Tile[][] tiles;

	protected TimeComparator timeComp;

	private class TimeComparator implements Comparator<Float> {
		public boolean isComparable(Float a) {
			return true;
		}

		public boolean isEqual(Float a, Float b) {
			return Math.abs(a - b) < TIME_THRESHOLD;
		}

		public boolean isGreater(Float a, Float b) {
			return (a - b) > TIME_THRESHOLD;
		}

		public boolean isGreaterOrEqual(Float a, Float b) {
			return (a - b) > -TIME_THRESHOLD;
		}

		public boolean isLess(Float a, Float b) {
			return (a - b) < -TIME_THRESHOLD;
		}

		public boolean isLessOrEqual(Float a, Float b) {
			return (a - b) < TIME_THRESHOLD;
		}

	}

	public Simulation(Set<BilliardBall> balls, Table table) {
		storedBalls = new HashSet<BilliardBall>();
		for (BilliardBall b : balls) {
			storedBalls.add((BilliardBall) b.clone());
		}

		this.table = table;
		this.timeComp = new TimeComparator();
		this.handlers = new ArrayList<EventHandler>();
		initTiles(storedBalls);
		events = new AVLTree<Float, Event>(timeComp);

		initials = new HashMap<BilliardBall, BilliardBall>();
	}

	public void init(Set<? extends Event> initEvents) {
		currTime = 0;
		paused = false;
		ready = true;
	}

	protected void createFromStored(Map<BilliardBall, BilliardBall> target) {
		ballEvents = new HashMap<BilliardBall, Map<Tile, List<Event>>>();

		for (BilliardBall ball : storedBalls) {
			// Create a null entry in the ball events map for events with an
			// unknown tile. Those events will not be deleted on tile transition
			// and only be scheduled when queueEvents is called without a
			// specific tile as argument.
			Map<Tile, List<Event>> tileEvents = new HashMap<Tile, List<Event>>();
			tileEvents.put(null, new DoubleLinkedList<Event>());
			ballEvents.put(ball, tileEvents);
			target.put(ball, (BilliardBall) ball.clone());

		}

		tiles = new Tile[hTiles][vTiles];
		for (int i = 0; i < hTiles; i++) {
			for (int j = 0; j < vTiles; j++) {
				tiles[i][j] = (Tile) storedTiles[i][j].clone();

				for (BilliardBall ball : storedBalls) {
					Map<Tile, List<Event>> tileEvents = ballEvents.get(ball);
					tileEvents.put(tiles[i][j], new DoubleLinkedList<Event>());
				}

				if (i != 0) {
					tiles[i][j].setWestTile(tiles[i - 1][j]);
					tiles[i - 1][j].setEastTile(tiles[i][j]);

				}
				if (j != 0) {
					tiles[i][j].setSouthTile(tiles[i][j - 1]);
					tiles[i][j - 1].setNorthTile(tiles[i][j]);

				}

				for (CollidableGameObject b : storedTiles[i][j].getContent()) {
					if (b instanceof BilliardBall) {
						tiles[i][j].addGameObject(target.get(b));
						(target.get(b)).getTiles().add(tiles[i][j]);
					} else {
						tiles[i][j].addGameObject(b);
					}
				}
			}
		}
	}

	protected void initTiles(Set<BilliardBall> targets) {
		storedTiles = new Tile[hTiles][vTiles];
		float tileWidth = (4 * table.getPocketRadius() + table.getWidth())
				/ hTiles;
		float tileLength = (4 * table.getPocketRadius() + table.getLength())
				/ vTiles;

		for (int i = 0; i < hTiles; i++) {
			for (int j = 0; j < vTiles; j++) {

				storedTiles[i][j] = new Tile("(" + i + "," + j + ")", i
						* tileWidth - 2 * table.getPocketRadius(), (i + 1)
						* tileWidth - 2 * table.getPocketRadius(), j
						* tileLength - 2 * table.getPocketRadius(), (j + 1)
						* tileLength - 2 * table.getPocketRadius());
				logger.log(Level.FINE, "Creating tile ", storedTiles[i][j]);
				if (i != 0) {
					storedTiles[i][j].setWestTile(storedTiles[i - 1][j]);
					storedTiles[i - 1][j].setEastTile(storedTiles[i][j]);

				}
				if (j != 0) {
					storedTiles[i][j].setSouthTile(storedTiles[i][j - 1]);
					storedTiles[i][j - 1].setNorthTile(storedTiles[i][j]);

				}
				for (Cushion c : table.getCushions()) {
					if (cushionInTile(c, storedTiles[i][j])) {
						storedTiles[i][j].addGameObject(c);
						logger.log(Level.FINE, "Adding cushion to tile " + c
								+ " " + storedTiles[i][j]);
					}
				}
				for (Pocket p : table.getPockets()) {
					if (sphereInTile(p, storedTiles[i][j])) {
						storedTiles[i][j].addGameObject(p);
						logger.log(Level.FINE, "Adding pocket to tile " + p
								+ " " + storedTiles[i][j]);
					}
				}
				for (PocketCorner pc : table.getPocketCorners()) {
					if (sphereInTile(pc, storedTiles[i][j])) {
						storedTiles[i][j].addGameObject(pc);
						logger.log(Level.FINE, "Adding pocket corner to tile "
								+ pc + " " + storedTiles[i][j]);
					}
				}
				for (BilliardBall b : targets) {
					if (sphereInTile(b, storedTiles[i][j])) {
						storedTiles[i][j].addGameObject(b);
						b.getTiles().add(storedTiles[i][j]);
						logger.log(Level.FINE, "Adding ball to tile " + b + " "
								+ storedTiles[i][j]);
					}
				}
			}
		}
	}

	private boolean sphereInTile(SphericalGameObject s, Tile t) {
		Vector3f circleDistance = new Vector3f();
		circleDistance.x = Math.abs(s.getPosition().x - t.getLowerBoundX()
				- t.getWidth() / 2);
		circleDistance.y = Math.abs(s.getPosition().y - t.getLowerBoundY()
				- t.getLength() / 2);

		if (circleDistance.x > (t.getWidth() / 2 + s.getRadius())) {
			return false;
		}
		if (circleDistance.y > (t.getLength() / 2 + s.getRadius())) {
			return false;
		}

		if (circleDistance.x <= (t.getWidth() / 2)) {
			return true;
		}
		if (circleDistance.y <= (t.getLength() / 2)) {
			return true;
		}

		float cornerDistance = FastMath.sqrt(FastMath.sqr(circleDistance.x
				- t.getWidth() / 2)
				+ FastMath.sqr(circleDistance.y - t.getLength() / 2));

		return (cornerDistance <= s.getRadius());
	}

	private boolean cushionInTile(Cushion c, Tile t) {
		if (c.getOrientation() == Orientation.NORTH
				|| c.getOrientation() == Orientation.SOUTH) {
			if (c.getPosition().y < t.getLowerBoundY()
					|| c.getPosition().y > t.getUpperBoundY()) {
				logger.log(Level.FINER, "Horizontal Cushion not in tile", c);
				return false;
			}
			if (c.getLength() + 2 * c.getPosition().x - 2 * t.getLowerBoundX() > -SPACE_THRESHOLD) {
				logger.log(Level.FINER, "Horizontal Cushion in tile", c);
				return true;
			}
		} else {
			if (c.getPosition().x < t.getLowerBoundX()
					|| c.getPosition().x > t.getUpperBoundX()) {
				logger.log(Level.FINER, "Vertical Cushion not in tile", c);
				return false;
			}
			if ((c.getLength() + 2 * c.getPosition().y - 2 * t.getLowerBoundY()) > -SPACE_THRESHOLD) {
				logger.log(Level.FINER, "Vertical Cushion in tile", c);
				return true;
			}
		}
		logger.log(Level.FINER, "Cushion not in tile", c);
		return false;
	}

	public void addEventHandler(EventHandler eh) {
		handlers.add(eh);
	}

	protected void clearEvents(BilliardBall ball) {
		for (Tile t : ball.getTiles()) {
			// go through all tiles in which the ball is present
			// and delete all events that might occur there
			clearEvents(ball, t);
		}

		for (Event e : ballEvents.get(ball).get(null)) {
			// Remove all events that were not associated with a
			// specific tile.
			e.removeOccurrences();
			logger.log(Level.FINER, "removed event", e);
		}

	}

	protected void clearEvents(BilliardBall ball, Tile tile) {
		for (Event e : ballEvents.get(ball).get(tile)) {
			// go through all events that happen in this tile
			// for this ball and remove them in all the datastructures
			// where they are stored.
			e.removeOccurrences();
			logger.log(Level.FINER, "removed event", e);
		}
	}

	protected void queueEvents(BilliardBall ball,
			Map<BilliardBall, BilliardBall> targets, Tile tile) {
		for (CollidableGameObject go : tile.getContent()) {
			Event e = go.findCollision(targets.get(ball));
			if (e != null) {
				logger.log(Level.FINE, "Queueing...", e);
				for (BilliardBall b : e.getBalls(targets)) {
					List<Event> l = ballEvents.get(b).get(tile);
					l.insertLast(e);
					e.addListOccurrence(l.last());
				}
				e.setPQOccurrence(events.insertItem(e.getTime(), e));
			}
		}

		// find tiling events
		if (ball.getState() != BallState.RESTING) {
			List<Event> l = ballEvents.get(ball).get(tile);
			LeaveTileEvent lte = tile.getLeaveEvent(ball);
			if (lte != null) {
				l.insertLast(lte);
				lte.addListOccurrence(l.last());
				lte.setPQOccurrence(events.insertItem(lte.getTime(), lte));
				logger.log(Level.FINE, "Queueing...", lte);
			}
			for (EnterTileEvent ete : tile.getEnterEvents(ball)) {
				logger.log(Level.FINE, "Queueing...", ete);
				l.insertLast(ete);
				ete.addListOccurrence(l.last());
				ete.setPQOccurrence(events.insertItem(ete.getTime(), ete));
			}
		}
	}

	protected void queueEvents(BilliardBall ball,
			Map<BilliardBall, BilliardBall> targets) {
		// find all events for objects in the current tile
		for (Tile tile : targets.get(ball).getTiles()) {
			queueEvents(ball, targets, tile);
		}
		// find statechanges
		if (ball.getState() != BallState.RESTING) {
			StateChange sc = new StateChange(ball);
			logger.log(Level.FINE, "Queueing...", sc);
			List<Event> l = ballEvents.get(ball).get(null);

			l.insertLast(sc);
			sc.addListOccurrence(l.last());
			sc.setPQOccurrence(events.insertItem(sc.getTime(), sc));
		}

	}

	public void getBall(BilliardBall b) {
		BilliardBall initial = initials.get(b);
		if (initial.getState() != BallState.RESTING) {
			b.setPosition(initial.getState().getMotion().getPosition(initial,
					currTime));
			b.setVelocity(initial.getState().getMotion().getVelocity(initial,
					currTime));
			b.setAngularVelocity(initial.getState().getMotion()
					.getAngularVelocity(initial, currTime));
		} else {
			b.setPosition(initial.getPosition());
			b.setVelocity(initial.getVelocity());
		}
		b.setState(initial.getState());
		b.setTime(currTime);
		b.setTiles(initial.getTiles());
	}

	public void advanceTime(float t) {
		if (t < 0) {
			throw new RuntimeException();
		}
		logger.log(Level.FINE, "Advancing time", t);

		float newTime = currTime + t;
		while (hasNextEvent() && (newTime) > fetchNextEvent().getTime()
				&& !paused) {
			handleEvent(fetchNextEvent());
		}
		if (!paused) {
			currTime = newTime;
		}
		logger.log(Level.FINE, "Advanced in time by " + t + " to new time",
				currTime);
	}

	public void finish() {
		logger.log(Level.FINE, "Finishing Simulation");

		while (hasNextEvent()) {
			if (paused) {
				return;
			}
			handleEvent(fetchNextEvent());
		}
	}

	public void updateBallStates(Event event,
			Map<BilliardBall, BilliardBall> targets) {
		logger.log(Level.FINE, "Updating ball states for event", event);
		if (event instanceof EnterTileEvent) {
			logger.log(Level.FINE, "Ball now in new tile",
					((TilingEvent) event).getTile());
		}
		if (event instanceof LeaveTileEvent) {
			logger.log(Level.FINE, "Ball left tile", ((LeaveTileEvent) event)
					.getTile());
		}
		event.handle(targets);
	}

	public void handleEvent(Event event) {

		currTime = event.getTime();
		updateBallStates(event, initials);
		for (EventHandler h : handlers) {
			h.handle(event);
		}
	}

	public abstract Event fetchNextEvent();

	public abstract boolean hasNextEvent();

	public void pause() {
		logger.info("PAUSE at " + currTime);
		this.paused = true;
	}

	public void resume() {
		logger.info("RESUME at " + currTime);
		this.paused = false;
	}

	public boolean isReady() {
		return ready;
	}

	public boolean isPaused() {
		return paused;
	}

	public float getCurrentTime() {
		return currTime;
	}

	public Tile[][] getTiles() {
		return storedTiles;
	}

	public void removeHandlers() {
		handlers.clear();
	}

	public Map<BilliardBall, BilliardBall> getInitials() {
		return initials;
	}
}
