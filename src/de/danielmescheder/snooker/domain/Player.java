package de.danielmescheder.snooker.domain;

import de.danielmescheder.snooker.control.ControllingUnit;

/**
 * The Player object manages all the player-related information
 * 
 */
public class Player {
	private ControllingUnit controllingUnit;
	private String name;
	private int score;
	private Cue cue;

	/**
	 * Gets the current ControllingUnit
	 * 
	 * @return the ControllingUnit
	 */
	public ControllingUnit getControllingUnit() {
		return controllingUnit;
	}

	/**
	 * Sets the current ControllingUnit
	 * 
	 * @param controllingUnit
	 *            the new ControllingUnit
	 */
	public void setControllingUnit(ControllingUnit controllingUnit) {
		this.controllingUnit = controllingUnit;
	}

	/**
	 * Gets the player's name
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the player's name
	 * 
	 * @param name
	 *            the name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the player's current score
	 * 
	 * @return the score
	 */
	public int getScore() {
		return score;
	}

	/**
	 * Sets the player's current score
	 * 
	 * @param score
	 *            the new score
	 */
	public void setScore(int score) {
		this.score = score;
	}

	/**
	 * Gets the player's current cue
	 * 
	 * @return the cue
	 */
	public Cue getCue() {
		return cue;
	}

	/**
	 * Sets the player's current cue
	 * 
	 * @param cue
	 *            the new cue
	 */
	public void setCue(Cue cue) {
		this.cue = cue;
	}

	@Override
	public Object clone() {
		Player newPlayer = new Player();
		newPlayer.setControllingUnit(getControllingUnit());
		newPlayer.setCue(getCue());
		newPlayer.setName(getName());
		newPlayer.setScore(getScore());
		return newPlayer;
	}
}