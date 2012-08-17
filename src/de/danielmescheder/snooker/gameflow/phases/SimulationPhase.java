package de.danielmescheder.snooker.gameflow.phases;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.domain.Player;
import de.danielmescheder.snooker.gameflow.GamePhase;
import de.danielmescheder.snooker.gameflow.GameState;
import de.danielmescheder.snooker.logic.GameLogicHandler;
import de.danielmescheder.snooker.simulation.PrescheduledSimulation;
import de.danielmescheder.snooker.simulation.Simulation;
import de.danielmescheder.snooker.simulation.event.CueInteraction;
import de.danielmescheder.snooker.simulation.event.Event;


/**
 * The simulation phase starts the simulation with the given event
 * 
 */
public class SimulationPhase extends GamePhase {
	private static final Logger logger = Logger.getLogger(SimulationPhase.class
			.getName());

	private Simulation sim;
	private GameLogicHandler logicHandler;
	private CueInteraction cueShot;

	/**
	 * Constructs a new SimulationPhase object. The CueInteraction is taken as
	 * first event to start the simulation.
	 * 
	 * @param s
	 *            the current GameState
	 * @param cueShot
	 *            the initial cue shot
	 */
	public SimulationPhase(GameState s, CueInteraction cueShot) {
		super(s);
		setNext(new AimingPhase(state));
		this.cueShot = cueShot;

		this.sim = new PrescheduledSimulation(new HashSet<BilliardBall>(state
				.getBalls()), state.getTable());

		logicHandler = new GameLogicHandler(state);

		sim.addEventHandler(logicHandler);

	}

	@Override
	public void start() {
		super.start();
		final Set<Event> initEvents = new HashSet<Event>();
		initEvents.add(cueShot);
		Runnable simRunner = new Runnable() {
			@Override
			public void run() {
				sim.init(initEvents);
			}
		};

		Thread preScheduleSim = new Thread(simRunner);
		preScheduleSim.start();
	}

	@Override
	public void finish() {
		if (isFinished()) {
			return;
		}

		for (BilliardBall b : state.getBalls()) {
			b.setTime(0);
		}

		logicHandler.updateState(state);

		if (state.getBalls().size() <= 1) {
			System.out.println("Scores");
			for (Player p : state.getPlayers()) {
				System.out.println(p.getName() + ", " + p.getScore());
			}
			setNext(new FrameInitPhase(state));
		} else if (logicHandler.cueBallPocketed()) {
			setNext(new BreakInitPhase(state));
		} else {
			setNext(new AimingPhase(state));
		}

		super.finish();

	}

	/**
	 * Gets the current simulation
	 * 
	 * @return the simulation
	 */
	public Simulation getSimulation() {
		return sim;
	}
}