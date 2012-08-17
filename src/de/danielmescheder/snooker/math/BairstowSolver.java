package de.danielmescheder.snooker.math;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The BairstowSolver can solve polynomial equations of any order.
 * 
 */
public class BairstowSolver {
	public static double THRESHOLD;
	static final Logger logger = Logger.getLogger(BairstowSolver.class
			.getName());

	private double[] coefficients;
	private double smallestRoot;
	private int order;

	/**
	 * Polynomial solver using Bairstow's method
	 * 
	 * @param coefficients
	 *            an array with the coefficients stored in the way: f(x) =
	 *            coeff[0] + coeff[1]*(X^1) + ... + coeff[order]*(X^order)
	 * @param order
	 *            the order of the polynomial
	 */
	public BairstowSolver(double[] coefficients) {
		this.setCoefficients(coefficients);
	}

	/**
	 * Set the coefficients
	 * 
	 * @param coefficients
	 *            an array with the coefficients stored in the way: f(x) =
	 *            coeff[0] + coeff[1]*(X^1) + ... + coeff[order]*(X^order)
	 */
	public void setCoefficients(double[] coefficients) {
		// round too small coefficients so that (hopefully) no infinite loop
		// occurs
		if (coefficients != null) {
			int i = coefficients.length - 1;
			while (i >= 0 && Math.abs(coefficients[i]) < THRESHOLD) {
				i--;
			}
			if (i < coefficients.length - 1) {
				this.coefficients = Arrays.copyOfRange(coefficients, 0, i + 1);
			} else {
				this.coefficients = coefficients;
			}
			setOrder(i);
		} else {
			this.coefficients = null;
			setOrder(0);
		}

	}

	/**
	 * Get the coefficient array
	 * 
	 * @return an array with the coefficients stored in the way: f(x) = coeff[0]
	 *         + coeff[1]*(X^1) + ... + coeff[order]*(X^order)
	 */
	public double[] getCoefficients() {
		return coefficients;
	}

	private void setOrder(int order) {
		this.order = order;
	}

	/**
	 * Gets the order of the polynomial.
	 * 
	 * @return the order
	 */
	public int getOrder() {
		return order;
	}

	/**
	 * Has a real, non-negative root been found?
	 * 
	 * @return true if yes, false otherwise
	 */
	public boolean foundNonNegReal() {
		return smallestRoot != Double.MAX_VALUE;
	}

	public double getMinNonNegReal() {
		return smallestRoot;
	}

	/**
	 * Solves the given polynomial and puts the results in the arrayList.
	 * 
	 * @return true if there are any roots, false otherwise
	 * @author Stephen R. Schmitt
	 * 
	 *         translated into Java from
	 *         http://home.att.net/~srschmitt/linbairstow.html
	 * 
	 */

	public boolean solve() {

		smallestRoot = Double.MAX_VALUE;
		int iteration = 0;

		double[] a, b, d;
		double alpha1 = 0, alpha2 = 0;
		double beta1 = 0, beta2 = 0;
		double delta1 = 0, delta2 = 0;
		int i, j, k = 0;
		int count = 0;
		int n;

		// long time1 = System.nanoTime();
		n = this.getOrder();

		if (n == 0) {
			return false;
		}

		if (n == 1) {
			foundRoot(-coefficients[0] / coefficients[1]);
			return true;
		}

		if (n == 2) {
			double cPart = coefficients[0];
			double bPart = coefficients[1];
			double aPart = coefficients[2];
			double rootPart = (bPart * bPart) - (4 * aPart * cPart);
			if (rootPart < 0) {
				return false;
			}
			double squareRootPart = Math.sqrt(rootPart);
			double solution1 = (-bPart + squareRootPart) / (2 * aPart);
			double solution2 = (-bPart - squareRootPart) / (2 * aPart);
			foundRoot(solution1);
			foundRoot(solution2);
			return true;
		}

		b = new double[n + 2];
		d = new double[n + 2];

		a = new double[n + 2];

		while (Math.abs(coefficients[0]) < THRESHOLD && n > 2) {
			foundRoot(0);
			coefficients = Arrays.copyOfRange(coefficients, 1,
					coefficients.length);
			n--;
		}

		for (i = 0; i <= n; i++) {
			a[n + 1 - i] = coefficients[i];
		}
		if (Math.abs(a[1] - 1) > THRESHOLD) {
			for (i = 3; i <= n + 2; i++) {
				a[i - 1] = a[i - 1] / a[1];
			}
			a[1] = 1;

		}

		count = 1;

		do {

			// guesses
			alpha1 = 100;
			beta1 = -100;
			do {
				iteration++;
				b[0] = 0;
				d[0] = 0;
				b[1] = 1;
				d[1] = 1;

				j = 2;
				k = 1;

				for (i = 3; i <= n + 2; i++) {
					b[i - 1] = a[i - 1] - alpha1 * b[j - 1] - beta1 * b[k - 1];
					d[i - 1] = b[i - 1] - alpha1 * d[j - 1] - beta1 * d[k - 1];
					j = j + 1;
					k = k + 1;
				}
				j = n;
				k = n - 1;

				delta1 = (Math.pow(d[j - 1], 2) - (d[n] - b[n]) * d[k - 1]);
				if (delta1 == 0) {
					return false;
				}
				alpha2 = (b[n] * d[j - 1] - b[n + 1] * d[k - 1]) / delta1;
				beta2 = (b[n + 1] * d[j - 1] - (d[n] - b[n]) * b[n]) / delta1;

				alpha1 = alpha1 + alpha2;
				beta1 = beta1 + beta2;

				if (iteration == 10000) {
					logger.log(Level.WARNING, "BairstowSolver crashed", Arrays
							.toString(coefficients));
					return false;

				}

			} while (Math.abs(alpha2) > THRESHOLD
					&& Math.abs(beta2) > THRESHOLD);
			iteration = 0;

			delta1 = Math.pow(alpha1, 2) - 4 * beta1;

			// roots are real
			if (delta1 >= 0) {
				delta2 = Math.sqrt(delta1);
				double solution1 = ((delta2 - alpha1) / 2);
				double solution2 = ((delta2 + alpha1) / -2);
				foundRoot(solution1);
				foundRoot(solution2);

			}
			count = count + 2;
			n = n - 2;
			if (n >= 2) {
				for (i = 2; i <= n + 2; i++) {
					a[i - 1] = b[i - 1];
				}
			}

		} while (n >= 2);

		if (n == 1) {
			double solution1 = -b[2];
			foundRoot(solution1);
		}

		return true;
	}

	private void foundRoot(double sol) {
		if (sol < -THRESHOLD) {
			return;
		}
		if (sol < smallestRoot) {
			smallestRoot = sol;
		}
	}

	@Override
	public String toString() {
		String s = "Coefficients:  \n" + Arrays.toString(coefficients)
				+ "\nSmallest root: " + smallestRoot;

		return s;
	}
}
