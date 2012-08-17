package de.danielmescheder.snooker.math;

import com.jme.math.Vector3f;

/**
 * The Transformation3D class facilitates 3-dimensional matrix transformations
 * of a {@link Vecor3f}.
 * 
 * @author tim
 * 
 */
public class Transformation3D {
	private float[][] matrix;

	public Transformation3D(float[][] matrix) {
		this.matrix = matrix;
	}

	public Transformation3D(float x1, float x2, float x3, float y1, float y2,
			float y3, float z1, float z2, float z3) {
		float[][] mat = { { x1, y1, z1 }, { x2, y2, z2 }, { x3, y3, z3 } };
		this.matrix = mat;
	}

	public Vector3f transform(Vector3f v) {
		return new Vector3f(matrix[0][0] * v.x + matrix[0][1] * v.y
				+ matrix[0][2] * v.z, matrix[1][0] * v.x + matrix[1][1] * v.y
				+ matrix[1][2] * v.z, matrix[2][0] * v.x + matrix[2][1] * v.y
				+ matrix[2][2] * v.z);
	}
}
