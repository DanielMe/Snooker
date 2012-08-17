package de.danielmescheder.snooker.gameflow.phases;

import java.util.ArrayList;
import java.util.List;

import com.jme.app.AbstractGame.ConfigShowMode;

import de.danielmescheder.snooker.control.ai.NonPlanningSamplingAI;
import de.danielmescheder.snooker.control.ai.PlanningSamplingAI;
import de.danielmescheder.snooker.control.ai.RandomAIControl;
import de.danielmescheder.snooker.control.ai.SamplingRandomAIControl;
import de.danielmescheder.snooker.control.ui.ManualGameControl;
import de.danielmescheder.snooker.domain.Cue;
import de.danielmescheder.snooker.domain.Player;
import de.danielmescheder.snooker.domain.Table;
import de.danielmescheder.snooker.exec.UMSnooker;
import de.danielmescheder.snooker.gameflow.GamePhase;
import de.danielmescheder.snooker.gameflow.GameState;
import de.danielmescheder.snooker.math.BairstowSolver;
import de.danielmescheder.snooker.presentation.SnookerTable3D;
import de.danielmescheder.snooker.presentation.TablePresentation;
import de.danielmescheder.snooker.simulation.Simulation;
import de.danielmescheder.snooker.simulation.event.BallCollision;
import de.danielmescheder.snooker.simulation.event.NoisyCueInteraction;
import de.danielmescheder.snooker.simulation.physics.Physics;

/**
 * The GameInitPhase initializes the actual snooker game
 * 
 */
public class GameInitPhase extends GamePhase {
	/**
	 * Constructs new GameInitPhase
	 * 
	 * @param state
	 *            the current GameState
	 */
	public GameInitPhase(GameState state) {
		super(state);
	}

	@Override
	public void start() {
		super.start();

		setDefaults();
		initNoise();
		initThresholds();
		finish();
	}

	/**
	 * Initializes the thresholds for the different part of the simulation
	 */
	private void initThresholds() {
		BairstowSolver.THRESHOLD = 5E-6;
		Simulation.TIME_THRESHOLD = 1E-7;
		Simulation.SPACE_THRESHOLD = 1E-6;
		BallCollision.VELOCITY_THRESHOLD = 1E-8f;
		Physics.THRESHOLD = 5E-6;

	}

	/**
	 * Initializes the noise values for the NoisyCueInteraction
	 */
	private void initNoise() {
//		 NoisyCueInteraction.destDev = 0f;
//		 NoisyCueInteraction.elevDev = 0f;
//		 NoisyCueInteraction.transXDev = 0;
//		 NoisyCueInteraction.transYDev = 0;
//		 NoisyCueInteraction.velDev = 0;

		NoisyCueInteraction.destDev = .001f;
		NoisyCueInteraction.elevDev = .001f;
		NoisyCueInteraction.transXDev = .0001f;
		NoisyCueInteraction.transYDev = .0001f;
		NoisyCueInteraction.velDev = .01f;

	}

	/**
	 * Sets the default values
	 */
	private void setDefaults() {
		// --> Initialize the table
		Table table = new Table(1.778f, 3.569f, .737f, .292f, .324f,
				.0525f / 2f, .045f, .045f);
		setTable(table);

		// --> Initialize presentation
		TablePresentation presentation = initPresentation();

		// --> Initialize players
		Player p1 = new Player();
		p1.setName("O'Sullivan");
		//TestDataCollector c1 = new TestDataCollector(p1.getName());
		//p1.setControllingUnit(new RandomAITestingControl(presentation,state, c1));
		p1.setControllingUnit(new RandomAIControl(presentation,state));
		//		p1.setControllingUnit(new SamplingRandomAIControl(presentation,state));

		p1.setScore(0);
		p1.setCue(new Cue(.550f));

		Player p2 = new Player();
		p2.setName("Hendry");
		//TestDataCollector c2 = new TestDataCollector(p2.getName());
		//p2.setControllingUnit(new ManualGameControl(presentation,state));
		p2.setControllingUnit(new SamplingRandomAIControl(presentation,state));
		p2.setScore(0);
		p2.setCue(new Cue(.550f));

		List<Player> players = new ArrayList<Player>();
		players.add(p1);
		players.add(p2);
		setPlayers(players);
		setCurrentPlayer(state.getPlayers().get(0));
	}

	/**
	 * Initializes a new 3D presentation
	 * 
	 * @return the presentation
	 */
	private TablePresentation initPresentation() {
		TablePresentation presentation = new SnookerTable3D(state);
		presentation.setConfigShowMode(ConfigShowMode.AlwaysShow,
				UMSnooker.class.getClassLoader().getResource(
						"nl/unimaas/micc/umsnooker/image/snooker.png"));

		(new Thread(presentation)).start();

		while (!presentation.isReady()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return presentation;
	}

	/**
	 * Set the currently playing player
	 * 
	 * @param current
	 *            the current player
	 */
	public void setCurrentPlayer(Player current) {
		state.setCurrentPlayer(current);
	}

	/**
	 * Set the table used for the game
	 * 
	 * @param table
	 *            a table
	 */
	public void setTable(Table table) {
		state.setTable(table);
	}

	/**
	 * Set the list of players that participate
	 * 
	 * @param players
	 *            the list of players
	 */
	public void setPlayers(List<Player> players) {
		state.setPlayers(players);
	}

	@Override
	public void finish() {
		if (finished) {
			return;
		}
		setNext(new FrameInitPhase(state));
		super.finish();
	}
}
