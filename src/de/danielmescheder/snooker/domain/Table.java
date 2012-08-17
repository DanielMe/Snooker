package de.danielmescheder.snooker.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;

/**
 * The Table class contains all information about the size, the geometry, the
 * ball-spots and the markings on a snooker table.
 * 
 * @author tim
 * 
 */
public class Table extends GameObject {
	private float width;
	private float length;

	private float baulkLineDistance;
	private float dRadius;
	private float blackDistance;

	private Vector3f brownSpot;
	private Vector3f yellowSpot;
	private Vector3f greenSpot;
	private Vector3f blueSpot;
	private Vector3f pinkSpot;
	private Vector3f blackSpot;
	private List<Vector3f> redSpots;

	private Map<BilliardBall.Type, Vector3f> colorSpots;

	private List<Cushion> cushions;

	private float pocketRadius;
	private List<Pocket> pockets;

	private float pocketCornerRadius;
	private List<PocketCorner> pocketCorners;

	private float ballRadius;

	private float slidingFriction;
	private float rollingFriction;
	private float spinningFriction;

	/**
	 * Constructs a new Table object
	 * 
	 * @param width
	 *            The width of the table in meters
	 * @param length
	 *            The length of the table in meters
	 * @param baulkLineDistance
	 *            The distance betweem the baulk line, the line on which the
	 *            yellow, brown and green balls are placed, and the bottom of
	 *            the table in meters
	 * @param dRadius
	 *            The radius of the d-shaped semicircle in which the cue-ball
	 *            can be placed
	 * @param blackDistance
	 *            The distance of the black ball from the table bottom in meters
	 * @param ballRadius
	 *            The radius of the used balls in meters
	 * @param pocketRadius
	 *            The radius of the pocket in meters
	 * @param pocketCornerRadius
	 *            The radius of the pocket corners in meters
	 */
	public Table(float width, float length, float baulkLineDistance,
			float dRadius, float blackDistance, float ballRadius,
			float pocketRadius, float pocketCornerRadius) {
		this.width = width;
		this.length = length;
		this.baulkLineDistance = baulkLineDistance;
		this.dRadius = dRadius;
		this.blackDistance = blackDistance;
		this.ballRadius = ballRadius;
		this.pocketRadius = pocketRadius;
		this.pocketCornerRadius = pocketCornerRadius;

		// Standard initial ball positions
		brownSpot = new Vector3f(width / 2f, baulkLineDistance, 0);
		yellowSpot = new Vector3f(brownSpot.x + dRadius, brownSpot.y, 0);
		greenSpot = new Vector3f(brownSpot.x - dRadius, brownSpot.y, 0);
		blueSpot = new Vector3f(width / 2f, length / 2f, 0);
		pinkSpot = new Vector3f(width / 2f, length * (3f / 4f), 0);
		blackSpot = new Vector3f(width / 2f, length - blackDistance, 0);

		colorSpots = new HashMap<BilliardBall.Type, Vector3f>();
		colorSpots.put(BilliardBall.Type.YELLOW, yellowSpot);
		colorSpots.put(BilliardBall.Type.GREEN, greenSpot);
		colorSpots.put(BilliardBall.Type.BROWN, brownSpot);
		colorSpots.put(BilliardBall.Type.BLUE, blueSpot);
		colorSpots.put(BilliardBall.Type.PINK, pinkSpot);
		colorSpots.put(BilliardBall.Type.BLACK, blackSpot);

		float spacing = 0.00001f;

		// build the pyramid of red balls
		redSpots = new ArrayList<Vector3f>(15);
		for (int y = 0; y < 5; y++) {
			for (int x = -y; x <= y; x += 2) {
				float entropy = 0;
				// float entropy = generator.nextFloat() / 10000;
				redSpots.add(new Vector3f(pinkSpot.x + x
						* (ballRadius + spacing + entropy), pinkSpot.y + 2f
						* (ballRadius + spacing + entropy) + y
						* FastMath.sqrt(3) * (ballRadius + spacing + entropy),
						0));

			}
		}

		// for (int y = 0; y < 20; y++)
		// {
		// for (int x = -10; x <= 10; x += 2)
		// {
		// // float entropy = 0;
		// float entropy = generator.nextFloat() / 10000;
		// redSpots.add(new Vector3f(blueSpot.x + x *
		// (ballRadius*2+spacing+entropy),
		// blueSpot.y + 2f * (ballRadius+spacing+entropy) + y
		// * (ballRadius*2+spacing+entropy), 0));
		//
		// }
		// }

		pockets = new ArrayList<Pocket>(6);

		// Bottom left
		pockets.add(new Pocket(new Vector3f(-pocketRadius / FastMath.sqrt(2),
				-pocketRadius / FastMath.sqrt(2), 0), pocketRadius,
				new Vector3f(1, 1, 0).normalize()));
		// Bottom right
		pockets.add(new Pocket(new Vector3f(width + pocketRadius
				/ FastMath.sqrt(2), -pocketRadius / FastMath.sqrt(2), 0),
				pocketRadius, new Vector3f(-1, 1, 0).normalize()));
		// Top left
		pockets.add(new Pocket(new Vector3f(-pocketRadius / FastMath.sqrt(2),
				length + pocketRadius / FastMath.sqrt(2), 0), pocketRadius,
				new Vector3f(1, -1, 0).normalize()));
		// Top right
		pockets.add(new Pocket(
				new Vector3f(width + pocketRadius / FastMath.sqrt(2), length
						+ pocketRadius / FastMath.sqrt(2), 0), pocketRadius,
				new Vector3f(-1, -1, 0).normalize()));

		// Left middle
		pockets.add(new Pocket(new Vector3f(-pocketRadius, length / 2f, 0),
				pocketRadius, new Vector3f(1, 0, 0)));
		// Right middle
		pockets.add(new Pocket(new Vector3f(width + pocketRadius, length / 2f,
				0), pocketRadius, new Vector3f(-1, 0, 0)));

		pocketCorners = new ArrayList<PocketCorner>(12);

		float cornerPocketOffset = FastMath.sqrt(2) * pocketRadius
				+ (FastMath.sqrt(2) - 1) * pocketCornerRadius;

		for (int x : new int[] { -1, 1 }) {
			for (int y : new int[] { -1, 1 }) {
				pocketCorners.add(new PocketCorner(new Vector3f(width / 2f + x
						* (width / 2f - cornerPocketOffset), length / 2f + y
						* (length / 2f + pocketCornerRadius), 0),
						pocketCornerRadius));
				pocketCorners.add(new PocketCorner(new Vector3f(width / 2f + x
						* (width / 2f + pocketCornerRadius), length / 2f + y
						* (length / 2f - cornerPocketOffset), 0),
						pocketCornerRadius));
			}
		}

		pocketCorners.add(new PocketCorner(new Vector3f(-pocketCornerRadius,
				length / 2f - pocketRadius - pocketCornerRadius, 0),
				pocketCornerRadius));
		pocketCorners.add(new PocketCorner(new Vector3f(-pocketCornerRadius,
				length / 2f + pocketRadius + pocketCornerRadius, 0),
				pocketCornerRadius));

		pocketCorners.add(new PocketCorner(new Vector3f(width
				+ pocketCornerRadius, length / 2f - pocketRadius
				- pocketCornerRadius, 0), pocketCornerRadius));
		pocketCorners.add(new PocketCorner(new Vector3f(width
				+ pocketCornerRadius, length / 2f + pocketRadius
				+ pocketCornerRadius, 0), pocketCornerRadius));

		cushions = new ArrayList<Cushion>(6);

		float vCushionLength = 0.5f * length - cornerPocketOffset
				- pocketRadius - pocketCornerRadius;
		float hCushionLength = width - 2f * cornerPocketOffset;

		// West
		cushions
				.add(new Cushion(new Vector3f(0, cornerPocketOffset + 0.5f
						* vCushionLength, 0), vCushionLength,
						Cushion.Orientation.WEST));
		cushions.add(new Cushion(new Vector3f(0, length - cornerPocketOffset
				- 0.5f * vCushionLength, 0), vCushionLength,
				Cushion.Orientation.WEST));

		// East
		cushions
				.add(new Cushion(new Vector3f(width, cornerPocketOffset + 0.5f
						* vCushionLength, 0), vCushionLength,
						Cushion.Orientation.EAST));
		cushions.add(new Cushion(new Vector3f(width, length
				- cornerPocketOffset - 0.5f * vCushionLength, 0),
				vCushionLength, Cushion.Orientation.EAST));

		// South
		cushions.add(new Cushion(new Vector3f(cornerPocketOffset + 0.5f
				* hCushionLength, 0, 0), vCushionLength,
				Cushion.Orientation.SOUTH));

		// North
		cushions.add(new Cushion(new Vector3f(cornerPocketOffset + 0.5f
				* hCushionLength, length, 0), vCushionLength,
				Cushion.Orientation.NORTH));

	}

	/**
	 * Gets the position of the initialization spot for a given type
	 * 
	 * @param type
	 *            The ball type
	 * @return The initialization spot
	 */
	public Vector3f getInitialSpot(BilliardBall.Type type) {
		if (!colorSpots.containsKey(type)) {
			throw new RuntimeException("No initial spot for type " + type);
		}
		return colorSpots.get(type);
	}

	/**
	 * Gets the table width
	 * 
	 * @return the table width in meters
	 */
	public float getWidth() {
		return width;
	}

	/**
	 * Gets the table length
	 * 
	 * @return the table length in meters
	 */
	public float getLength() {
		return length;
	}

	/**
	 * Gets the distance between the baulk line and the bottom of the table
	 * 
	 * @return the distance in meters
	 */
	public float getBaulkLineDistance() {
		return baulkLineDistance;
	}

	/**
	 * Gets the radius of the d-shaped semicircle
	 * 
	 * @return the radius in meters
	 */
	public float getDRadius() {
		return dRadius;
	}

	/**
	 * Gets the distance of the black spot from the bottom of the table
	 * 
	 * @return the distance in meters
	 */
	public float getBlackDistance() {
		return blackDistance;
	}

	/**
	 * Gets the brown initialization spot
	 * 
	 * @return the spot
	 */
	public Vector3f getBrownSpot() {
		return brownSpot;
	}

	/**
	 * Gets the yellow initialization spot
	 * 
	 * @return the spot
	 */
	public Vector3f getYellowSpot() {
		return yellowSpot;
	}

	/**
	 * Gets the green initialization spot
	 * 
	 * @return the spot
	 */
	public Vector3f getGreenSpot() {
		return greenSpot;
	}

	/**
	 * Gets the blue initialization spot
	 * 
	 * @return the spot
	 */
	public Vector3f getBlueSpot() {
		return blueSpot;
	}

	/**
	 * Gets the pink initialization spot
	 * 
	 * @return the spot
	 */
	public Vector3f getPinkSpot() {
		return pinkSpot;
	}

	/**
	 * Gets the black initialization spot
	 * 
	 * @return the spot
	 */
	public Vector3f getBlackSpot() {
		return blackSpot;
	}

	/**
	 * Gets a list containing the red initialization spots
	 * 
	 * @return the list containing the spots
	 */
	public List<Vector3f> getRedSpots() {
		return redSpots;
	}

	/**
	 * Gets a list containing the cushions
	 * 
	 * @return the list containing the cushions
	 */
	public List<Cushion> getCushions() {
		return cushions;
	}

	/**
	 * Gets the radius of the pockets
	 * 
	 * @return the radius in meters
	 */
	public float getPocketRadius() {
		return pocketRadius;
	}

	/**
	 * Gets a list containing the pockets
	 * 
	 * @return the list containing the pockets
	 */
	public List<Pocket> getPockets() {
		return pockets;
	}

	/**
	 * Gets the radius of the pocket corners
	 * 
	 * @return the radius in meters
	 */
	public float getPocketCornerRadius() {
		return pocketCornerRadius;
	}

	/**
	 * Gets a list containing the pocket corners
	 * 
	 * @return the list containing the corners
	 */
	public List<PocketCorner> getPocketCorners() {
		return pocketCorners;
	}

	/**
	 * Gets the radius of the balls
	 * 
	 * @return the radius in meters
	 */
	public float getBallRadius() {
		return ballRadius;
	}

	/**
	 * Gets the coefficient of friction during the sliding state
	 * 
	 * @return the friction
	 */
	public float getSlidingFriction() {
		return slidingFriction;
	}

	/**
	 * Gets the coefficient of friction during the rolling state
	 * 
	 * @return the friction
	 */
	public float getRollingFriction() {
		return rollingFriction;
	}

	/**
	 * Gets the coefficient of friction during the spinning state
	 * 
	 * @return the friction
	 */
	public float getSpinningFriction() {
		return spinningFriction;
	}

}
