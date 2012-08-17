package de.danielmescheder.snooker.control.ai.generator;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme.math.FastMath;
import com.jme.math.Vector3f;

import de.danielmescheder.snooker.control.ai.EventEvaluator;
import de.danielmescheder.snooker.control.ai.EventGenerator;
import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.domain.Ball.BallState;
import de.danielmescheder.snooker.gameflow.GameState;
import de.danielmescheder.snooker.math.BairstowSolver;
import de.danielmescheder.snooker.simulation.event.CueInteraction;
import de.danielmescheder.snooker.simulation.event.PocketingEvent;
import de.danielmescheder.snooker.simulation.event.SetValuesEvent;
import de.danielmescheder.snooker.simulation.physics.Physics;


/**
 * This {@link EventGenerator} generates {@link CueInteraction}s to cause a
 * {@link PocketingEvent} for another {@link BilliardBall}.
 * 
 */
public class DirectCueGenerator implements
		EventGenerator<CueInteraction, PocketingEvent> {
	private static final Logger logger = Logger
			.getLogger(DirectCueGenerator.class.getName());

	private static BairstowSolver solver = new BairstowSolver(null);
	private EventGenerator<SetValuesEvent, PocketingEvent> ballStateGenerator;
	private EventEvaluator<SetValuesEvent, PocketingEvent> ballStateEvaluator;
	private PocketingEvent currTarget;
	SetValuesEvent currEvent;

	public DirectCueGenerator(
			EventGenerator<SetValuesEvent, PocketingEvent> ballStateGenerator,
			EventEvaluator<SetValuesEvent, PocketingEvent> ballStateEvaluator) {
		this.ballStateEvaluator = ballStateEvaluator;
		this.ballStateGenerator = ballStateGenerator;
	}

	@Override
	public CueInteraction generate(GameState state) {
		logger.log(Level.INFO, "Generating next event");
		boolean legalCutAngle = false;
		Vector3f p1;
		float r1, r2;
		int iterations = 0;
		do {
			do {
				iterations++;
				if (iterations > 1000)
				{
					return null;
				}
				currEvent = ballStateGenerator.generate(state);
				currTarget = ballStateGenerator.getTarget();
				logger.log(Level.FINE, "Trying in order to pocket "
						+ currTarget, currEvent);
			} while (!ballStateEvaluator.evaluate(currEvent, currTarget, state));

			p1 = currEvent.getBall().getPosition();
			r1 = currEvent.getBall().getRadius();
			r2 = state.getCueBall().getRadius();

			Vector3f d = p1.subtract(state.getCueBall().getPosition());
			float maxAngle = FastMath.acos((r1 + r2) / d.length());

			float cutAngle = d.angleBetween(currEvent.getVelocity());
			legalCutAngle = cutAngle < maxAngle;
			logger.log(Level.FINE, "Cut angle " + cutAngle + " < "
					+ "max angle " + maxAngle, legalCutAngle);
		} while (!legalCutAngle);

		Vector3f targetPos = p1.add(currEvent.getVelocity().normalize().mult(
				-r1 - r2));
		Vector3f dist = targetPos.subtract(state.getCueBall().getPosition());
		float distLength = dist.length();
		logger.log(Level.FINER, "Dist (length: " + distLength + ")", dist);

		float vt = ((distLength * FastMath
				.sqr(currEvent.getVelocity().length())) / (dist.dot(currEvent
				.getVelocity())));
		logger.log(Level.FINER, "Velocity of ball", currEvent.getVelocity());
		logger.log(Level.FINER, "Target velocity at ball", vt);
		/*
		 * double[] coeff = new double[3]; float fric =
		 * BallState.SLIDING.getMotion().getFriction(state.getCueBall()); float
		 * b = vt/fric; float a = (1/(fric*Physics.g)-1/(2*distLength));
		 * coeff[0] = b-fric/(2*distLength); coeff[1] = -2*a*b; coeff[2] =
		 * FastMath.sqr(a)-(1/4*FastMath.sqr(distLength));
		 * 
		 * solver.setCoefficients(coeff); solver.solve(); float v0 = (float)
		 * solver.getMinNonNegReal();
		 */

		float mus = BallState.SLIDING.getMotion().getFriction(
				state.getCueBall());
		float mur = BallState.ROLLING.getMotion().getFriction(
				state.getCueBall());
		float g = Physics.g;
		float v0;

		if (vt < FastMath.sqrt((25f / 12f) * distLength * mus * g)) {
			/*double[] coeff = new double[3];
			coeff[0] = FastMath.sqr(vt) + 2f * mur * g * distLength;
			coeff[1] = (4f / 7f) * vt;
			coeff[2] = (24f * mur - 45f * mus) / (49f * mus);
			solver.setCoefficients(coeff);
			solver.solve();
			v0 = (float) solver.getMinNonNegReal();	*/
			v0 = FastMath.sqrt(((FastMath.sqr(vt)+2f*distLength*mur*g)*49f*mus)/(25f*mus+24f*mur));
		}
		else
		{
			v0 = FastMath.sqrt(FastMath.sqr(vt)+2*distLength*mus*g);
		}
		logger.log(Level.FINER, "Resulting initial velocity", v0);

		float ang = FastMath.atan2(dist.y, dist.x) + FastMath.PI / 2f;
		logger.log(Level.FINER, "Shot angle ", ang);
		float vc = -(v0 * (1 + (state.getCueBall().getMass() / state
				.getCurrentPlayer().getCue().getMass()))) / 2;
		CueInteraction ci = new CueInteraction(0, state.getCurrentPlayer()
				.getCue(), state.getCueBall(), ang, 0, 0, 0, vc);

		return ci;
	}

	@Override
	public PocketingEvent getTarget() {
		return currTarget;
	}

	@Override
	public void reset() {
		// Not Implemented

	}
}
