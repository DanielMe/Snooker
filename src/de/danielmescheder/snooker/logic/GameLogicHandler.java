package de.danielmescheder.snooker.logic;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import com.jme.math.Vector3f;

import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.domain.Player;
import de.danielmescheder.snooker.gameflow.GameState;
import de.danielmescheder.snooker.simulation.EventHandler;
import de.danielmescheder.snooker.simulation.event.BallCollision;
import de.danielmescheder.snooker.simulation.event.Event;
import de.danielmescheder.snooker.simulation.event.PocketingEvent;


/**
 * The GameLogicHandler is a special EventHandler that observes the game and
 * applies the correct rules
 * 
 */
public class GameLogicHandler implements EventHandler {
	private Set<BilliardBall.Type> possibleOnBalls;

	private BilliardBall.Type onBall;

	private boolean cueBallPocketed = false;
	private int pottedScore = 0;
	private int foulScore = 0;
	private boolean foulCommitted = false;
	private boolean firstBallCollision = true;
	private Set<BilliardBall> pottedBalls = new HashSet<BilliardBall>();

	/**
	 * Constructs a new GameLogicHandler with a given GameState
	 * 
	 * @param state
	 *            the GameState
	 */
	public GameLogicHandler(GameState state) {
		possibleOnBalls = state.getPossibleOnBallTypes();

		if (state.getPossibleOnBallTypes().size() == 1) {
			onBall = (BilliardBall.Type) state.getPossibleOnBallTypes().toArray()[0];
		}
	}

	/* EVENT HANDLERS */

	@Override
	public void handle(Event e) {
		if (e instanceof PocketingEvent) {
			handlePocketCollision((PocketingEvent) e);
		} else if (e instanceof BallCollision) {
			handleBallInteraction((BallCollision) e);
		}
	}

	private void handleBallInteraction(BallCollision e) {
		if (firstBallCollision) {
			firstBallCollision = false;
			for (BilliardBall b : e.getBallKeys()) {
				if (b.getType() != BilliardBall.Type.CUE) {
					if (possibleOnBalls.contains(b.getType())) {
						if (possibleOnBalls == GameState.colourBalls) {
							// coloured balls were on, so the first touched ball
							// will be the only one on
							onBall = b.getType();
						}
					} else {
						// touched ball is not on
						foulCommitted = true;
						foulScore = Math.max(foulScore, b.getType().getValue());
					}
					return; // there is only one non-cue ball in the first
					// collision
				}
			}
		}
	}

	private void handlePocketCollision(PocketingEvent e) {
		for (BilliardBall b : e.getBallKeys()) {
			if (b.getType() == BilliardBall.Type.CUE) {
				cueBallPocketed = true;
				foulCommitted = true;
			} else {
				if (b.getType() == onBall) {
					pottedScore += b.getType().getValue();
				} else if (onBall == null
						&& possibleOnBalls.contains(b.getType())) {
					onBall = b.getType();
					pottedScore += b.getType().getValue();
				} else {
					foulCommitted = true;
					foulScore = Math.max(foulScore, b.getType().getValue());
				}
				pottedBalls.add(b);
			}
		}
	}

	/* EVALUATION */

	/**
	 * This method starts the evaluation of the events that occurred
	 */
	public void evaluateEvents() {
		if (firstBallCollision) {
			foulCommitted = true;
		}

		// scores
		if (foulCommitted) {
			pottedScore = 0;
			foulScore = Math.max(4, foulScore);
		} else {
			foulScore = 0;
		}
	}

	/**
	 * Gets the score for the potted balls
	 * 
	 * @return the score
	 */
	public int getPottedScore() {
		return pottedScore;
	}

	/**
	 * Gets the foul score, the score the opponent will get if a foul occurred
	 * 
	 * @return
	 */
	public int getFoulScore() {
		return foulScore;
	}

	/**
	 * Was the cue ball pocketed?
	 * 
	 * @return true if it was pocketed, false otherwise
	 */
	public boolean cueBallPocketed() {
		return cueBallPocketed;
	}

	/**
	 * Has a foul been committed?
	 * 
	 * @return true if a foul has been committed, false otherwise
	 */
	public boolean foulCommitted() {
		return foulCommitted;
	}

	private boolean redBallsOnTable(GameState state) {
		for (BilliardBall ball : state.getBalls()) {
			if (ball.getType() == BilliardBall.Type.RED) {
				return true;
			}
		}
		return false;
	}

	/* STATE UPDATING */

	/**
	 * Updates the GameState according to the rules
	 * 
	 * @param state
	 *            the GameState to be updated
	 */
	public void updateState(GameState state) {
		evaluateEvents();

		handlePottedBalls(state);

		Player currentPlayer = state.getCurrentPlayer();

		int playerIndex = state.getPlayers().indexOf(currentPlayer);
		Player nextPlayer = state.getPlayers().get(
				(playerIndex + 1) % state.getPlayers().size());

		// SCORES
		if (foulCommitted) {
			nextPlayer.setScore(nextPlayer.getScore() + foulScore);
		} else {
			currentPlayer.setScore(currentPlayer.getScore() + pottedScore);
		}

		if (foulCommitted || pottedScore == 0) {
			state.setCurrentPlayer(nextPlayer);
		}

		if (!state.isEndgame()) {
			if (!foulCommitted && pottedScore > 0) {
				if (state.getPossibleOnBallTypes() == GameState.redBalls) {
					state.setPossibleOnBalls(GameState.colourBalls);
				} else {
					state.setPossibleOnBalls(GameState.redBalls);
				}
			} else {
				state.setPossibleOnBalls(GameState.redBalls);
			}

			if (!redBallsOnTable(state)
					&& state.getPossibleOnBallTypes() == GameState.redBalls) {
				state.setIsEndgame(true);
				state.setPossibleOnBalls(Collections
						.singleton(BilliardBall.Type.YELLOW));
			}
		} else {
			if (pottedScore > 0) {
				if (state.getPossibleOnBallTypes() == GameState.redBalls) {
					state.setPossibleOnBalls(GameState.colourBalls);
				} else {
					int minValue = 7;
					BilliardBall.Type minType = BilliardBall.Type.BLACK;
					for (BilliardBall ball : state.getBalls()) {
						if (ball.getType() != BilliardBall.Type.CUE
								&& ball.getType().getValue() < minValue) {
							minValue = ball.getType().getValue();
							minType = ball.getType();
						}
					}
					state.setPossibleOnBalls(Collections.singleton(minType));
				}
			}
		}
	}

	private void handlePottedBalls(GameState state) {
		BilliardBall[] pottedBallArray = pottedBalls
				.toArray(new BilliardBall[0]);

		// start spotting with the highest-valued balls
		Arrays.sort(pottedBallArray, new BallValueComparator());

		for (BilliardBall ball : pottedBallArray) {
			if (GameState.colourBalls.contains(ball.getType())) {
				if (!state.isEndgame() || foulCommitted) {
					spotColouredBall(ball, state);
				} else {
					state.getBalls().remove(ball);
				}
			} else if (GameState.redBalls.contains(ball.getType())) {
				state.getBalls().remove(ball);
			}
		}
	}

	private void spotColouredBall(BilliardBall ball, GameState state) {
		Vector3f[] initialSpots = new Vector3f[] {
				state.getTable().getInitialSpot(ball.getType()),
				state.getTable().getBlackSpot(),
				state.getTable().getPinkSpot(), state.getTable().getBlueSpot(),
				state.getTable().getBrownSpot(),
				state.getTable().getGreenSpot(),
				state.getTable().getYellowSpot() };

		// check initial spots of coloured balls
		Vector3f spot = initialSpots[0];
		boolean foundSpot = false;
		for (int i = 0; !foundSpot && i < initialSpots.length; i++) {
			if (state.isSpotFree(initialSpots[i], ball)) {
				spot = initialSpots[i];
				foundSpot = true;
			}
		}

		if (!foundSpot) {
			// all spots were occupied
			spot = state.getTable().getInitialSpot(ball.getType());
			float step = .001f;
			// move from default spot to top cushion until there is a free spot
			while (!foundSpot) {
				spot.y += step;
				if (spot.y > state.getTable().getLength() - ball.getRadius()) {
					// top cushion is reached, search towards bottom cushion
					spot = state.getTable().getInitialSpot(ball.getType());
					step = -step;
					spot.y += step;
				}
				if (state.isSpotFree(spot, ball)) {
					foundSpot = true;
				}
			}
		}


		for (BilliardBall stateBall : state.getBalls()) {
			if (stateBall.equals(ball)) {
				stateBall.setPosition(spot.clone());
				break;
			}
		}
	}

	private class BallValueComparator implements Comparator<BilliardBall> {
		@Override
		public int compare(BilliardBall b1, BilliardBall b2) {
			return ((Integer) b2.getType().getValue()).compareTo(b1
					.getType().getValue());
		}

	}
}
