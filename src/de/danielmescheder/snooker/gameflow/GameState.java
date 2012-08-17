package de.danielmescheder.snooker.gameflow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jme.math.Vector3f;

import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.domain.Player;
import de.danielmescheder.snooker.domain.Table;
import de.danielmescheder.snooker.domain.BilliardBall.Type;


/**
 * The GameState stores and provides information about the state of the game.
 * 
 */
public class GameState
{
	public static final Set<BilliardBall.Type> redBalls;
	public static final Set<BilliardBall.Type> colourBalls;

	static
	{
		redBalls = new HashSet<BilliardBall.Type>();
		colourBalls = new HashSet<BilliardBall.Type>();

		redBalls.add(Type.RED);
		colourBalls.add(Type.BLUE);
		colourBalls.add(Type.BLACK);
		colourBalls.add(Type.BROWN);
		colourBalls.add(Type.GREEN);
		colourBalls.add(Type.PINK);
		colourBalls.add(Type.YELLOW);
	}
	
	private Set<BilliardBall.Type> onBalls;
	private List<Player> players;
	private Set<BilliardBall> balls;
	private BilliardBall cueBall;
	private Table table;
	private Player currentPlayer;
	private GamePhase currentPhase;
	private boolean isEndgame;
	
	/**
	 * Constructs new empty GameState
	 */
	public GameState()
	{
		balls = new HashSet<BilliardBall>();
	}
	
	
	/**
	 * Finds out of a given spot is free for a certain ball.
	 * @param spot The position of the spot
	 * @param ball	The ball to check against
	 * @return true if the spot is free; false otherwise
	 */
	public boolean isSpotFree(Vector3f spot, BilliardBall ball)
	{
		for (BilliardBall otherBall : balls)
		{
			if (otherBall.getPosition().distance(spot) <= ball.getRadius() + otherBall.getRadius())
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Gets a set of the on-balls types, the balls that can be legally played.
	 * @return the onBalls
	 */
	public Set<BilliardBall.Type> getPossibleOnBallTypes()
	{
		return onBalls;
	}

	/**
	 * Gets a set of all on-balls
	 * @return onBalls
	 */

	 public Set<BilliardBall> getOnBalls(){
		 HashSet<BilliardBall> onBalls = new HashSet<BilliardBall>();
		 for(BilliardBall b : this.getBalls()){
			 if(this.getPossibleOnBallTypes().contains(b.getType())){
				 onBalls.add(b);
			 }
		 }
		 return onBalls;
		 
	 }
	 
	 
	 
	/**
	 * Sets the set of possible on-balls, the balls that can be legally played.
	 * @param onBalls the new onBalls
	 */
	public void setPossibleOnBalls(Set<BilliardBall.Type> onBalls)
	{
		this.onBalls = onBalls;
	}

	/**
	 * Adds a ball to the GameState
	 * @param ball the BilliardBall to add
	 */
	public void addBall(BilliardBall ball)
	{
		balls.add(ball);
	}
	
	/**
	 * Gets the cue ball
	 * @return the cue ball
	 */
	public BilliardBall getCueBall()
	{
		return cueBall;
	}
	
	/**
	 * Sets the cue ball
	 * @param ball the cue ball
	 */
	public void setCueBall(BilliardBall ball)
	{
		this.cueBall = ball;
	}
	
	/**
	 * Gets a list of all players
	 * @return the players
	 */
	public List<Player> getPlayers()
	{
		return players;
	}
	
	/**
	 * Sets the list of players
	 * @param players the players
	 */
	public void setPlayers(List<Player> players)
	{
		this.players = players;
	}
	
	/**
	 * Gets the set of all balls in the GameState
	 * @return the balls
	 */
	public Set<BilliardBall> getBalls()
	{
		return balls;
	}
	
	/**
	 * Sets the set of all balls in the GameState
	 * @param balls the balls
	 */
	public void setBalls(Set<BilliardBall> balls)
	{
		this.balls = balls;
	}
	
	/**
	 * Gets the table object for this GameState
	 * @return the table
	 */
	public Table getTable()
	{
		return table;
	}
	
	/**
	 * Sets the table object for this GameState
	 * @param table
	 */
	public void setTable(Table table)
	{
		this.table = table;
	}
	
	/**
	 * Gets the currently active player
	 * @return the current player
	 */
	public Player getCurrentPlayer()
	{
		return currentPlayer;
	}
	
	/**
	 * Sets the currently active player
	 * @param currentPlayer the current player
	 */
	public void setCurrentPlayer(Player currentPlayer)
	{
		this.currentPlayer = currentPlayer;
	}
	
	/**
	 * Gets the current GamePhase
	 * @return the current phase
	 */
	public GamePhase getCurrentPhase()
	{
		return currentPhase;
	}
	
	/**
	 * Sets the current GamePhase
	 * @param currentPhase the current phase
	 */
	public void setCurrentPhase(GamePhase currentPhase)
	{
		this.currentPhase = currentPhase;
	}
	
	/**
	 * Checks if the end game has started
	 * @return true if the endgame has started, false otherwise
	 */
	public boolean isEndgame()
	{
		return isEndgame;
	}
	
	/**
	 * Sets the end game parameter
	 * @param isEndgame endgame
	 */
	public void setIsEndgame(boolean isEndgame)
	{
		this.isEndgame = isEndgame;
	}

	/**
	 * Gets the maximum possible score
	 * @return the max score
	 */
	public int getMaxScore(){
		int maxScore = Integer.MIN_VALUE;
		for (BilliardBall.Type type : this.getPossibleOnBallTypes())
		{
			maxScore = Math.max(maxScore, type.getValue());
		}	
		return maxScore;
	}
	
	@Override
	public Object clone()
	{
		GameState newState = new GameState();
		Set<BilliardBall> newBalls = new HashSet<BilliardBall>();
		BilliardBall newCueBall = null;
		for(BilliardBall b : getBalls())
		{
			BilliardBall newBall = (BilliardBall) b.clone();
			newBalls.add(newBall);
			if(b == getCueBall())
			{
				newCueBall = newBall;
			}
		}
		newState.setCueBall(newCueBall);
		newState.setBalls(newBalls);
		newState.setIsEndgame(isEndgame());
		newState.setCurrentPhase(getCurrentPhase());
		List<Player> newPlayers = new ArrayList<Player>();
		Player newCurrentPlayer = null;
		for(Player p : getPlayers())
		{
			Player newPlayer = (Player)p.clone();
			newPlayers.add(newPlayer);
			if(p == getCurrentPlayer())
			{
				newCurrentPlayer = newPlayer;
			}
		}
		newState.setPlayers(newPlayers);
		newState.setCurrentPlayer(newCurrentPlayer);
		newState.setPossibleOnBalls(getPossibleOnBallTypes());
		newState.setTable(getTable());
		return newState;
		
	}
}
