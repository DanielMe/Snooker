package de.danielmescheder.snooker.simulation.event;

import java.util.Random;

import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.domain.Cue;

/**
 * The NoisyCueInteraction is a special case of {@link CueInteraction} that
 * incorporates gaussian noise into the values. Each value has it's own standard
 * deviation assigned to it. The idea behind this class is being able to sample
 * CueInteractions for improved accuracy.
 * 
 */
public class NoisyCueInteraction extends CueInteraction {
	private static Random r;

	public static float velDev = 1, transXDev = 1, transYDev = 1, elevDev = 1,
			destDev = 1;

	static {
		r = new Random();
	}

	public NoisyCueInteraction(float time, Cue cue, BilliardBall ball,
			float angDest, float angElev, float transX, float transY,
			float velocity) {
		super(time, cue, ball, setAngDestNoise(angDest),
				setAngElevNoise(angElev), setTransXNoise(transX),
				setTransYNoise(transY), setVelNoise(velocity));
	}

	private static float setVelNoise(float velocity) {
		return (float) (velocity + velDev * r.nextGaussian());
	}

	private static float setTransYNoise(float transY) {
		return (float) (transY + transYDev * r.nextGaussian());
	}

	private static float setTransXNoise(float transX) {
		return (float) (transX + transXDev * r.nextGaussian());
	}

	private static float setAngElevNoise(float angElev) {
		return (float) (angElev + elevDev * r.nextGaussian());
	}

	private static float setAngDestNoise(float angDest) {
		return (float) (angDest + destDev * r.nextGaussian());
	}

}
