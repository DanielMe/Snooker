package de.danielmescheder.snooker.gameflow;

/**
 * The abstract class GamePhase contains information about the
 * next GamePhase and the associated GameState
 * 
 */
public abstract class GamePhase
{
	protected boolean finished;
	protected GamePhase next;
	protected GameState state;
	
	/**
	 * Constructs a new GamePhase object with a given state
	 * @param state the GameState
	 */
	public GamePhase(GameState state)
	{
		this.state = state;
	}
	
	/**
	 * Starts the phase
	 */
	public void start()
	{
		finished = false;
	}
	
	/**
	 * Finishes the phase
	 */
	public void finish()
	{
		finished = true;
	}
	
	/**
	 * Is the phase finished yet?
	 * @return true if the phase is finished; false otherwise
	 */
	public boolean isFinished()
	{
		return finished;
	}
	
	/**
	 * Gets the next GamePhase
	 * @return the next GamePhase
	 */
	public GamePhase getNext()
	{
		return next;
	}
	
	/**
	 * Sets the next GamePhase
	 * @param phase the next GamePhase
	 */
	public void setNext(GamePhase phase)
	{
		next = phase;
	}
	
	@Override
	public String toString()
	{
		return this.getClass().getSimpleName();
	}
}
