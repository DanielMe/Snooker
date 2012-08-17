package de.danielmescheder.snooker.simulation.physics;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme.math.FastMath;
import com.jme.math.Vector3f;

import de.danielmescheder.snooker.domain.Ball;
import de.danielmescheder.snooker.domain.Cushion;
import de.danielmescheder.snooker.domain.SphericalGameObject;
import de.danielmescheder.snooker.domain.Ball.BallState;
import de.danielmescheder.snooker.domain.Cushion.Orientation;
import de.danielmescheder.snooker.math.BairstowSolver;
import de.danielmescheder.snooker.math.Transformation3D;
import de.danielmescheder.snooker.math.XYRotationTrans3D;

/**
 * The Physics class is responsible for the calculation of the parameters of a
 * ball at a given time and the collision-detection between different
 * {@link CollidableGameObject}s.
 * 
 */
public class Physics {
	public static double THRESHOLD;
	private static final Logger logger = Logger.getLogger(Physics.class
			.getName());

	public static float angleTo(Vector3f a, Vector3f b) {

		return (float) (Math.atan2(b.y, b.x) - Math.atan2(a.y, a.x));

	}

	private static class SlidingMotion implements BallMotion {

		public Vector3f getAngularVelocity(Ball initial, float time) {
			Vector3f u = getRelativeVelocity(initial).normalize();
			Vector3f av = initial.getAngularVelocity().add(
					new Vector3f(0, 0, 1f).cross(u).mult(
							5f * SFRIC * g * (time - initial.getTime())
									/ (2f * initial.getRadius())));
			av.z = initial.getAngularVelocity().z - 5f * SPFRIC * g
					* (time - initial.getTime()) / (2f * initial.getRadius());
			return av;
		}

		public Vector3f getPosition(Ball initial, float time) {
			float ang = -angleTo(initial.getVelocity(), new Vector3f(1f, 0, 0));
			Transformation3D tableToBall = new XYRotationTrans3D(ang);
			Transformation3D ballToTable = new XYRotationTrans3D(-ang);

			Vector3f u = getRelativeVelocity(initial, tableToBall).normalize();

			Vector3f pos = new Vector3f(initial.getVelocity().length()
					* (time - initial.getTime()) - 0.5f * SFRIC * g
					* (time - initial.getTime()) * (time - initial.getTime())
					* u.x, -0.5f * SFRIC * g * (time - initial.getTime())
					* (time - initial.getTime()) * u.y, 0f);

			return initial.getPosition().add(ballToTable.transform(pos));
		}

		public Vector3f getVelocity(Ball initial, float time) {
			float ang = -angleTo(initial.getVelocity(), new Vector3f(1f, 0, 0));
			Transformation3D tableToBall = new XYRotationTrans3D(ang);
			Transformation3D ballToTable = new XYRotationTrans3D(-ang);

			Vector3f u = getRelativeVelocity(initial, tableToBall).normalize();

			return initial.getVelocity().add(
					ballToTable.transform(u.mult(-SFRIC * g
							* (time - initial.getTime()))));
		}

		public float getDuration(Ball initial) {
			Vector3f u = getRelativeVelocity(initial);
			return (2f * u.length() / (7f * SFRIC * g));
		}

		public float getFriction(Ball initial) {
			return SFRIC;
		}

	}

	private static class RollingMotion implements BallMotion {

		public Vector3f getAngularVelocity(Ball initial, float time) {
			Vector3f av = new Vector3f();
			Vector3f dir = initial.getAngularVelocity().normalize();
			av.x = initial.getAngularVelocity().x - RFRIC * g
					* (time - initial.getTime()) / initial.getRadius() * dir.x;
			av.y = initial.getAngularVelocity().y - RFRIC * g
					* (time - initial.getTime()) / initial.getRadius() * dir.y;
			av.z = initial.getAngularVelocity().z - 5f * SPFRIC * g
					* (time - initial.getTime()) / (2f * initial.getRadius());
			return av;
		}

		public Vector3f getPosition(Ball initial, float time) {
			return initial.getPosition().add(
					initial.getVelocity().mult(time - initial.getTime()))
					.subtract(
							initial.getVelocity().normalize().mult(
									0.5f * RFRIC * g
											* (time - initial.getTime())
											* (time - initial.getTime())));
		}

		public Vector3f getVelocity(Ball initial, float time) {
			return initial.getVelocity().subtract(
					initial.getVelocity().normalize().mult(
							RFRIC * g * (time - initial.getTime())));

		}

		public float getDuration(Ball initial) {
			return initial.getVelocity().length() / (RFRIC * g);
		}

		public float getFriction(Ball initial) {
			return RFRIC;
		}
	}

	private static class RestingMotion implements BallMotion {

		public Vector3f getAngularVelocity(Ball initial, float time) {
			return Vector3f.ZERO.clone();
		}

		public Vector3f getPosition(Ball initial, float time) {
			return initial.getPosition();
		}

		public Vector3f getVelocity(Ball initial, float time) {
			return Vector3f.ZERO.clone();
		}

		public float getDuration(Ball initial) {
			return Float.POSITIVE_INFINITY;
		}

		public float getFriction(Ball initial) {
			return 0;
		}
	}

	static {
		rollingMotion = new RollingMotion();
		slidingMotion = new SlidingMotion();
		restingMotion = new RestingMotion();
	}

	public static BallMotion getRollingMotion() {
		return rollingMotion;
	}

	public static BallMotion getSlidingMotion() {
		return slidingMotion;
	}

	public static BallMotion getRestingMotion() {
		return restingMotion;
	}

	public static float collisionTime(Ball ball, Cushion cushion) {
		logger.log(Level.FINE, "Trying to find cushion collision", cushion);
		float r = ball.getRadius();

		float line = 0;
		boolean horizontal = false;
		int direction = 1;

		Orientation orientation = cushion.getOrientation();

		if (orientation == Orientation.NORTH) {
			line = cushion.getPosition().y - r;
			horizontal = true;
			direction = 1;
		}
		if (orientation == Orientation.EAST) {
			line = cushion.getPosition().x - r;
			horizontal = false;
			direction = 1;
		}
		if (orientation == Orientation.SOUTH) {
			line = cushion.getPosition().y + r;
			horizontal = true;
			direction = -1;
		}
		if (orientation == Orientation.WEST) {
			line = cushion.getPosition().x + r;
			horizontal = false;
			direction = -1;
		}

		// find and check for feasibility
		double t = lineCrossingTime(ball, line, horizontal, direction);
		logger.log(Level.FINE, "Cushion collision time found ", t);
		if (t == -1) {
			return -1;
		}

		Vector3f ballPos = ball.getState().getMotion().getPosition(ball,
				(float) (ball.getTime() + t));
		float min = 0, max = 0, act = 0;
		if (horizontal) {
			min = cushion.getPosition().x - 0.5f * cushion.getLength();
			max = cushion.getPosition().x + 0.5f * cushion.getLength();
			act = ballPos.x;
		} else {
			min = cushion.getPosition().y - 0.5f * cushion.getLength();
			max = cushion.getPosition().y + 0.5f * cushion.getLength();
			act = ballPos.y;
		}

		if (min > act || max < act) {
			logger.log(Level.FINE, "not feasible: " + ball + cushion);
			return -1;
		}

		// indeed feasible
		return (float) t;
	}

	public static float lineCrossingTime(Ball ball, float f,
			boolean horizontal, int direction) {
		double[] coeff = new double[3];

		if (horizontal) {
			coeff[0] = -direction * (-f + ball.getPosition().y);
		} else {
			coeff[0] = -direction * (-f + ball.getPosition().x);
		}

		logger.log(Level.FINE, "Target coeff", coeff[0]);

		if (ball.getState() == BallState.SLIDING) {
			logger.log(Level.FINE, "Ball is SLIDING", ball);
			float ang = FastMath.atan2(ball.getVelocity().y,
					ball.getVelocity().x);
			Transformation3D tableToBall = new XYRotationTrans3D(ang);
			Vector3f u = Physics.getRelativeVelocity(ball, tableToBall)
					.normalize();
			double mu = ball.getState().getMotion().getFriction(ball);
			double v0 = ball.getVelocity().length();
			double constFact = -0.5 * mu * g;

			if (horizontal) {
				coeff[1] = -direction * Math.sin(ang) * v0;
				coeff[2] = -direction * constFact
						* (Math.sin(ang) * u.x + Math.cos(ang) * u.y);
			} else {
				coeff[1] = -direction * Math.cos(ang) * v0;
				coeff[2] = -direction * constFact
						* (Math.cos(ang) * u.x - Math.sin(ang) * u.y);
			}
		} else if (ball.getState() == BallState.ROLLING) {
			logger.log(Level.FINE, "Ball is ROLLING", ball);
			double mu = ball.getState().getMotion().getFriction(ball);
			Vector3f v = ball.getVelocity();
			Vector3f vUnit = v.normalize();
			double constFact = -0.5 * mu * g;
			if (horizontal) {
				coeff[1] = -direction * v.y;
				coeff[2] = -direction * constFact * vUnit.y;
			} else {
				coeff[1] = -direction * v.x;
				coeff[2] = -direction * constFact * vUnit.x;
			}
		} else {
			return -1;
		}

		return solve(coeff);
	}

	public static float collisionTime(Ball ball1, Ball ball2) {
		if (ball2.getState() == BallState.RESTING) {
			return collisionTime(ball1, (SphericalGameObject) ball2);
		}

		if (ball1.getState() == BallState.RESTING) {
			return collisionTime(ball2, (SphericalGameObject) ball1);
		}

		float ang1 = (float) Math.atan2(ball1.getVelocity().y, ball1
				.getVelocity().x);
		float ang2 = (float) Math.atan2(ball2.getVelocity().y, ball2
				.getVelocity().x);
		Transformation3D tableToBall1 = new XYRotationTrans3D(ang1);
		Transformation3D tableToBall2 = new XYRotationTrans3D(ang2);

		Vector3f a = new Vector3f();
		Vector3f b = new Vector3f();

		Vector3f u1;
		Vector3f u2;

		if (ball1.getState() == BallState.SLIDING) {
			u1 = getRelativeVelocity(ball1, tableToBall1).normalize();
		} else {
			u1 = tableToBall1.transform(ball1.getVelocity().normalize());
		}

		if (ball2.getState() == BallState.SLIDING) {
			u2 = getRelativeVelocity(ball2, tableToBall2).normalize();
		} else {
			u2 = tableToBall2.transform(ball2.getVelocity().normalize());
		}

		Vector3f c = ball2.getPosition().subtract(ball1.getPosition());

		b.x = ball2.getVelocity().length() * FastMath.cos(ang2)
				- ball1.getVelocity().length() * FastMath.cos(ang1);
		b.y = ball2.getVelocity().length() * FastMath.sin(ang2)
				- ball1.getVelocity().length() * FastMath.sin(ang1);

		a.x = 0.5f
				* g
				* (ball2.getState().getMotion().getFriction(ball2)
						* (u2.y * FastMath.sin(ang2) - u2.x
								* FastMath.cos(ang2)) - ball1.getState()
						.getMotion().getFriction(ball1)
						* (u1.y * FastMath.sin(ang1) - u1.x
								* FastMath.cos(ang1)));

		a.y = -0.5f
				* g
				* (ball2.getState().getMotion().getFriction(ball2)
						* (u2.x * FastMath.sin(ang2) + u2.y
								* FastMath.cos(ang2)) - ball1.getState()
						.getMotion().getFriction(ball1)
						* (u1.x * FastMath.sin(ang1) + u1.y
								* FastMath.cos(ang1)));

		double[] co = new double[5];

		co[0] = Math.pow(c.x, 2) + Math.pow(c.y, 2)
				- Math.pow(ball1.getRadius() + ball2.getRadius(), 2);
		co[1] = 2 * b.x * c.x + 2 * b.y * c.y;
		co[2] = Math.pow(b.x, 2) + 2 * a.x * c.x + 2 * a.y * c.y
				+ Math.pow(b.y, 2);
		co[3] = 2 * a.x * b.x + 2 * a.y * b.y;
		co[4] = Math.pow(a.x, 2) + Math.pow(a.y, 2);

		return solve(co);

	}

	public static float collisionTime(Ball ball, SphericalGameObject sphere) {
		float ang = (float) Math.atan2(ball.getVelocity().y,
				ball.getVelocity().x);
		Transformation3D tableToBall = new XYRotationTrans3D(ang);

		Vector3f a = new Vector3f();
		Vector3f b = new Vector3f();
		Vector3f c = sphere.getPosition().subtract(ball.getPosition());

		Vector3f u;

		if (ball.getState() == BallState.SLIDING) {
			u = getRelativeVelocity(ball, tableToBall).normalize();
		} else {
			u = tableToBall.transform(ball.getVelocity().normalize());
		}

		b.x = -ball.getVelocity().length() * FastMath.cos(ang);
		b.y = -ball.getVelocity().length() * FastMath.sin(ang);

		a.x = 0.5f
				* g
				* (-ball.getState().getMotion().getFriction(ball) * (u.y
						* FastMath.sin(ang) - u.x * FastMath.cos(ang)));

		a.y = -0.5f
				* g
				* (-ball.getState().getMotion().getFriction(ball) * (u.x
						* FastMath.sin(ang) + u.y * FastMath.cos(ang)));

		double[] co = new double[5];

		co[0] = Math.pow(c.x, 2) + Math.pow(c.y, 2)
				- Math.pow(ball.getRadius() + sphere.getRadius(), 2);
		co[1] = 2 * b.x * c.x + 2 * b.y * c.y;
		co[2] = Math.pow(b.x, 2) + 2 * a.x * c.x + 2 * a.y * c.y
				+ Math.pow(b.y, 2);
		co[3] = 2 * a.x * b.x + 2 * a.y * b.y;
		co[4] = Math.pow(a.x, 2) + Math.pow(a.y, 2);

		return solve(co);

	}

	private static float solve(double[] co) {
		logger.log(Level.FINE, "Solving polynomial", Arrays.toString(co));

		if (co[0] < THRESHOLD) {
			logger.log(Level.FINER, "Constant equal to zero", co);

			if (derivativeSmallerZero(co, 1, 4)) {
				logger.log(Level.FINER, "Derivative smaller zero found", co);
				return 0;
			} else {
				logger.log(Level.FINER, "No derivative smaller zero found", co);

				return -1;
			}
		} else if (co[0] < -THRESHOLD) {
			boolean smallerZero = true;
			for (int i = 1; (i < co.length) && smallerZero; i++) {
				smallerZero = smallerZero && co[i] < -THRESHOLD;
			}
			if (smallerZero) {
				return -1;
			}
		} else if (co[0] > THRESHOLD) {
			boolean greaterZero = true;
			for (int i = 1; (i < co.length) && greaterZero; i++) {
				greaterZero = greaterZero && co[i] > THRESHOLD;
			}
			if (greaterZero) {
				return -1;
			}
		}

		solver.setCoefficients(co);
		solver.solve();
		if (solver.foundNonNegReal()) {
			logger.log(Level.FINER, "Non negative real root found", solver
					.getMinNonNegReal());
			return (float) solver.getMinNonNegReal();
		}
		logger.log(Level.FINER, "No root found");
		return -1;
	}

	private static boolean derivativeSmallerZero(double[] co, int d, int max) {
		if (d > max) {
			return false;
		}
		if (co[d] < -THRESHOLD) {
			return true;
		} else if (Math.abs(co[d]) < THRESHOLD) {
			// return derivativeSmallerZero(co, d+1, max);
			return false;
		} else {
			return false;
		}

	}

	public static Vector3f getRelativeVelocity(Ball ball) {
		return ball.getVelocity().add(
				new Vector3f(0, 0, ball.getRadius()).cross(ball
						.getAngularVelocity()));

	}

	public static Vector3f getRelativeVelocity(Ball ball, Transformation3D t) {
		return t.transform(ball.getVelocity()).add(
				new Vector3f(0, 0, ball.getRadius()).cross(t.transform(ball
						.getAngularVelocity())));
	}

	private static RollingMotion rollingMotion;
	private static SlidingMotion slidingMotion;
	private static RestingMotion restingMotion;

	private static BairstowSolver solver = new BairstowSolver(null);

	// ============ Constants ============== \\
	private static final float SFRIC = 0.2f;
	private static final float RFRIC = 0.016f;
	private static final float SPFRIC = 0.044f;

	public static final float g = 9.81f;
	// public static final float g = 1.624f;

}
