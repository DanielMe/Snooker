package de.danielmescheder.snooker.testing;

import java.util.Set;

import de.danielmescheder.snooker.domain.BilliardBall;


/**
 * The TestDataCollector allows for the automatic creation of {@link Player}
 * statistics and outputs then in a CSV format.
 * 
 */
public class TestDataCollector {

	private String name;
	private int successfulPocket = 0;
	private int failedPocket = 0;
	private int triedShots = 0;
	private int totalScore = 0;
	private int bestScore = 0;
	private int totalPottedBalls = 0;
	private int accidentallyPottedBalls = 0;
	private int round = 0;
	private String toString;

	private Set<BilliardBall> lastPlannedBalls;
	private Set<BilliardBall> pottedBalls;

	public TestDataCollector(String name) {
		this.name = name;
		this.initToString();
	}

	public void registerPlannedBalls(Set<BilliardBall> balls) {
		this.lastPlannedBalls = balls;
		triedShots += 1;

	}

	public void registerPottedBalls(Set<BilliardBall> balls) {
		this.pottedBalls = balls;
		if (!balls.isEmpty()) {
			totalPottedBalls += balls.size();
			evaluateData();
		} else if (balls.isEmpty()) {
			if (!(lastPlannedBalls == null) && !lastPlannedBalls.isEmpty()) {
				failedPocket += lastPlannedBalls.size();
				updateString();
				round++;
			}
		}

	}

	private void evaluateData(){
		for(BilliardBall b : pottedBalls){
			if(!(lastPlannedBalls == null) && lastPlannedBalls.contains(b)){
				successfulPocket += 1;
			} else {
				accidentallyPottedBalls += 1;
			}
		}
		updateString();
		round++;
	}

	private void initToString() {
		toString = "Name , Round , TriedShots , Total Potted Balls , Accidentally Potted Balls , Successfully Pocketed Balls , Failed Pocket Attempts \n";
	}

	private void updateString() {
		toString += name + " , " + round + " , " + triedShots + " , "
				+ totalPottedBalls + " , " + accidentallyPottedBalls + " , "
				+ successfulPocket + " , " + failedPocket + "\n";
	}

	@Override
	public String toString() {
		return toString;
	}
}
