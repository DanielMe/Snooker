package de.danielmescheder.snooker.control.ai.generator;

import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme.math.FastMath;
import com.jme.math.Vector3f;

import de.danielmescheder.snooker.control.ai.EventGenerator;
import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.domain.Pocket;
import de.danielmescheder.snooker.domain.Ball.BallState;
import de.danielmescheder.snooker.gameflow.GameState;
import de.danielmescheder.snooker.simulation.event.PocketingEvent;
import de.danielmescheder.snooker.simulation.event.SetValuesEvent;
import de.danielmescheder.snooker.simulation.physics.Physics;


/**
 * This {@link EventGenerator} generates events that will cause a given ball to
 * have a {@link PocketingEvent}.
 * 
 */
public class DirectPocketGenerator implements
		EventGenerator<SetValuesEvent, PocketingEvent> {
	private static final Logger logger = Logger
			.getLogger(DirectPocketGenerator.class.getName());
	private static Random random = new Random();
	private Iterator<Pocket> pocketIterator;
	private Iterator<BilliardBall> ballIterator;
	private GameState state;
	private BilliardBall currentBall;
	private Pocket currentPocket;
	//private float currentVelMult;
	private int variation = 0;
	private int variationsPerPocket;

	public DirectPocketGenerator(int variations) {
		variationsPerPocket = variations;
	}

	@Override
	public SetValuesEvent generate(GameState state) {
		logger.log(Level.INFO, "Generating next event");
		if (this.state != state) {
			this.state = state;
			reset();
		}
		if (!pocketIterator.hasNext() || currentBall == null) {
			do {
				if (!ballIterator.hasNext()) {
					ballIterator = state.getBalls().iterator();
					// currentVelMult += .3f;
					//System.out.println("Multiplyer now "+currentVelMult);
				}
				currentBall = ballIterator.next();
			} while (!state.getPossibleOnBallTypes().contains(
					currentBall.getType()));
			pocketIterator = state.getTable().getPockets().iterator();
			variation = 0;
		}

		if (variation >= variationsPerPocket || currentPocket == null) {
			currentPocket = pocketIterator.next();
			variation = 0;
		}

		Vector3f aimingPoint = currentPocket.getPosition().add(
					currentPocket.getOrientation().mult(
							currentPocket.getOuterRadius()));
			Vector3f varVec = new Vector3f(currentPocket.getOrientation().y,
					-currentPocket.getOrientation().x, 0);

			aimingPoint = aimingPoint.add(varVec.mult(.5f*currentPocket.getRadius()* random.nextFloat()));
		

		Vector3f dist = aimingPoint.subtract(currentBall.getPosition());
		// float vel = currentVelMult *
		// FastMath.sqrt(2*dist.length()*(BallState.ROLLING.getMotion().getFriction(currentBall))*Physics.g);
		float mur = BallState.ROLLING.getMotion().getFriction(currentBall);
		float mus = BallState.SLIDING.getMotion().getFriction(currentBall);

		float g = Physics.g;
		float vel = (random.nextFloat())+1.2f*FastMath.sqrt(dist.length() / (25f / (98f * mur * g) + 12f / (49f * mus * g)));

		variation++;
		SetValuesEvent e = new SetValuesEvent(0, currentBall, dist.normalize()
				.mult(vel), Vector3f.ZERO.clone(), BallState.SLIDING);
		logger.log(Level.FINE, "Generated Event", e);

		return e;
	}

	@Override
	public PocketingEvent getTarget() {
		return new PocketingEvent(0, currentBall, currentPocket);
	}

	@Override
	public void reset() {
		pocketIterator = state.getTable().getPockets().iterator();
		ballIterator = state.getBalls().iterator();
		currentBall = null;
		currentPocket = null;
	}

}
