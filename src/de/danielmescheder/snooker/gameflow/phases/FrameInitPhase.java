package de.danielmescheder.snooker.gameflow.phases;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme.math.Vector3f;

import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.domain.Player;
import de.danielmescheder.snooker.domain.Table;
import de.danielmescheder.snooker.gameflow.GamePhase;
import de.danielmescheder.snooker.gameflow.GameState;


/**
 * The FrameInitPhase initializes the table for a new game
 * 
 */
public class FrameInitPhase extends GamePhase {
	private static final Logger logger = Logger.getLogger(FrameInitPhase.class
			.getName());

	/**
	 * Constructs a new FrameInitPhase
	 * 
	 * @param state
	 *            current GameState
	 */
	public FrameInitPhase(GameState state) {
		super(state);
	}

	@Override
	public void start() {
		super.start();

		setDefaults();

		finish();
	}

	/**
	 * This methods resets the frame back to the defaults
	 */
	private void setDefaults() {
		Table table = state.getTable();

		// --> Initialize the cue ball
		Vector3f cueBallPosition = new Vector3f(table.getBrownSpot().x
				+ table.getDRadius() / 2f, table.getBrownSpot().y
				- table.getDRadius() / 2f, 0);
		BilliardBall cueBall = new BilliardBall(0, BilliardBall.Type.CUE,
				cueBallPosition);
		setCueBall(cueBall);
		// We store all balls in a List:
		Set<BilliardBall> balls = new HashSet<BilliardBall>(25);
		balls.add(cueBall);

		// --> Initialize the colored balls
		balls.add(new BilliardBall(2, BilliardBall.Type.YELLOW, table
				.getYellowSpot()));
		balls.add(new BilliardBall(3, BilliardBall.Type.GREEN, table
				.getGreenSpot()));
		balls.add(new BilliardBall(4, BilliardBall.Type.BROWN, table
				.getBrownSpot()));
		balls.add(new BilliardBall(5, BilliardBall.Type.BLUE, table
				.getBlueSpot()));
		balls.add(new BilliardBall(6, BilliardBall.Type.PINK, table
				.getPinkSpot()));
		balls.add(new BilliardBall(7, BilliardBall.Type.BLACK, table
				.getBlackSpot()));

		// --> Initialize the red balls
		int id = 8;
		for (Vector3f redSpot : table.getRedSpots()) {
			balls.add(new BilliardBall(id++, BilliardBall.Type.RED, redSpot));
		}

		if (table.getRedSpots().isEmpty()) {
			state.setIsEndgame(true);
		}

		// --> Configure all balls
		for (BilliardBall ball : balls) {
			ball.setRadius(table.getBallRadius());
			ball.setMass(.120f);
			ball.setFriction(0.06f);
			ball.setTime(0);
		}

		// --> Update GameState with balls
		setBalls(balls);

		for (Player player : state.getPlayers()) {
			player.setScore(0);
		}
	}

	/**
	 * Sets the current cue ball
	 * 
	 * @param ball
	 *            the new cue ball
	 */
	public void setCueBall(BilliardBall ball) {
		state.setCueBall(ball);
	}

	/**
	 * Sets the new balls in the GameState
	 * 
	 * @param balls
	 *            a set of balls
	 */
	public void setBalls(Set<BilliardBall> balls) {
		state.setBalls(balls);
	}

	@Override
	public void finish() {
		if (finished) {
			return;
		}
		for (BilliardBall initial : state.getBalls()) {
			logger.log(Level.FINE, "initialized ball", initial);
		}
		setNext(new BreakInitPhase(state));
		super.finish();
	}
}
