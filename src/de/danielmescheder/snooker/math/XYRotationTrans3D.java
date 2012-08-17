package de.danielmescheder.snooker.math;

import com.jme.math.FastMath;

/**
 * This class allows for rotations on the X-Y plane of a {@link Vector3f} with a certain angle.
 * 
 */
public class XYRotationTrans3D extends Transformation3D {
	public XYRotationTrans3D(float angle) {
		super(FastMath.cos(angle), -FastMath.sin(angle), 0,
				FastMath.sin(angle), FastMath.cos(angle), 0, 0, 0, 1);
	}
}
