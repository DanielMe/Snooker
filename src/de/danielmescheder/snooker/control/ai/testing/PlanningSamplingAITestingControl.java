package de.danielmescheder.snooker.control.ai.testing;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.danielmescheder.snooker.control.ai.PlanningSamplingAI;
import de.danielmescheder.snooker.control.ai.evaluator.CueInteractionToPocketEvaluator;
import de.danielmescheder.snooker.control.ai.evaluator.DepthSamplingEvaluator;
import de.danielmescheder.snooker.control.ai.evaluator.DirectPocketingEvaluator;
import de.danielmescheder.snooker.control.ai.evaluator.SafetyShotEvaluator;
import de.danielmescheder.snooker.control.ai.evaluator.SamplingRandomEvaluator;
import de.danielmescheder.snooker.control.ai.evaluator.SimpleSamplingEvaluator;
import de.danielmescheder.snooker.control.ai.evaluator.SimpleScoreEvaluator;
import de.danielmescheder.snooker.control.ai.generator.DirectCueGenerator;
import de.danielmescheder.snooker.control.ai.generator.DirectPocketGenerator;
import de.danielmescheder.snooker.control.ai.generator.RandomCueInteractionGenerator;
import de.danielmescheder.snooker.control.ui.controller.SimulationControl;
import de.danielmescheder.snooker.gameflow.GameState;
import de.danielmescheder.snooker.gameflow.phases.AimingPhase;
import de.danielmescheder.snooker.gameflow.phases.SimulationPhase;
import de.danielmescheder.snooker.presentation.SnookerTable3D;
import de.danielmescheder.snooker.presentation.TablePresentation;
import de.danielmescheder.snooker.simulation.event.CueInteraction;
import de.danielmescheder.snooker.simulation.event.PocketingEvent;
import de.danielmescheder.snooker.testing.TestDataCollector;


/**
 * A version of the {@link PlanningSamplingAI} that makes use of the
 * {@link TestDataCollector}
 * 
 * @see PlanningSamplingAI
 */
public class PlanningSamplingAITestingControl extends PlanningSamplingAI {

	private boolean showMarkings = false;

	private static final Logger logger = Logger
			.getLogger(PlanningSamplingAITestingControl.class.getName());

	private TestDataCollector collector;

	public PlanningSamplingAITestingControl(TablePresentation presentation,
			GameState state, TestDataCollector collector) {
		super(presentation, state);
		this.collector = collector;
	}

	@Override
	protected void handleSimulation(SimulationPhase phase) {
		presentation.setGameControl(new SimulationControl(presentation, phase,
				state, collector));
	}

	@Override
	protected void handleAiming(AimingPhase phase) {
		toggleMarkings();

		if (state.getBalls().size() == 1) {
			handleAimingFallbackOneBall(phase);
		}
		DirectPocketGenerator pocketingGenerator = new DirectPocketGenerator(4);
		DirectPocketingEvaluator pocketingEvaluator = new DirectPocketingEvaluator();
		DirectCueGenerator ciGenerator = new DirectCueGenerator(
				pocketingGenerator, pocketingEvaluator);

		DirectPocketGenerator secondLevelPocketGenerator = new DirectPocketGenerator(
				2);
		DirectCueGenerator secondLevelCueGenerator = new DirectCueGenerator(
				secondLevelPocketGenerator, pocketingEvaluator);
		CueInteractionToPocketEvaluator secondLevelEvaluator = new CueInteractionToPocketEvaluator(
				3);
		DepthSamplingEvaluator finalEvaluator = new DepthSamplingEvaluator(10,
				secondLevelCueGenerator, secondLevelEvaluator, 25);

		finalEvaluator.setCriticalScore(.2f);
		secondLevelEvaluator.setCriticalScore(0f);
		pocketingEvaluator.setCriticalScore(.5f);

		long before = System.currentTimeMillis();
		int count = 0;
		cueStrike = null;
		maxEvent = null;
		PocketingEvent maxTarget = null;
		double maxScore = -1;

		do {
			count++;
			cueStrike = ciGenerator.generate(state);
			if (cueStrike == null) {
				handleAimingFallbackPhaseTwo(phase);
				return;
			}
			if (finalEvaluator.evaluate(cueStrike, ciGenerator.getTarget(),
					state)) {
				if (finalEvaluator.getScore() > maxScore) {
					maxScore = finalEvaluator.getScore();
					maxEvent = cueStrike;
					maxTarget = ciGenerator.getTarget();
				}
			}
			enableAimLine(100, cueStrike);
			logger.log(Level.INFO, "Trying event in order to pocket "
					+ ciGenerator.getTarget(), cueStrike);
		} while (maxScore < 1 && (count < 60));
		if (maxEvent == null) {
			handleAimingFallbackPhaseOne(phase);
			return;
		}

		float time = (System.currentTimeMillis() - before) / 1000f;
		logger.log(Level.FINE, "Generated " + count + " shots in " + time
				+ "s : " + count / time + " shots/s");

		phase.setAngDest(maxEvent.getAngDest());
		phase.setAngElev(maxEvent.getAngElev());
		phase.setTransX(maxEvent.getTransX());
		phase.setTransY(maxEvent.getTransY());
		phase.setVelocity(maxEvent.getVelocity());

		collector.registerPlannedBalls(maxTarget.getBallKeys());

		enableAimLine(1000, maxEvent);
		toggleMarkings();

		phase.finish();
	}

	private void handleAimingFallbackOneBall(AimingPhase phase) {

		DirectPocketGenerator pocketingGenerator = new DirectPocketGenerator(4);
		DirectPocketingEvaluator pocketingEvaluator = new DirectPocketingEvaluator();
		DirectCueGenerator ciGenerator = new DirectCueGenerator(
				pocketingGenerator, pocketingEvaluator);
		CueInteractionToPocketEvaluator finalEvaluator = new CueInteractionToPocketEvaluator(
				20);

		finalEvaluator.setCriticalScore(.1f);
		pocketingEvaluator.setCriticalScore(.5f);

		long before = System.currentTimeMillis();
		int count = 0;
		cueStrike = null;
		maxEvent = null;
		PocketingEvent maxTarget = null;
		double maxScore = -1;

		do {
			count++;
			cueStrike = ciGenerator.generate(state);
			if (cueStrike == null) {
				handleAimingFallbackOneBallFallback(phase);
				return;
			}
			if (finalEvaluator.evaluate(cueStrike, ciGenerator.getTarget(),
					state)) {
				if (finalEvaluator.getScore() > maxScore) {
					maxScore = finalEvaluator.getScore();
					maxEvent = cueStrike;
					maxTarget = ciGenerator.getTarget();
				}
			}
			// enableAimLine(100, cueStrike);

			logger.log(Level.INFO, "Trying event in order to pocket "
					+ ciGenerator.getTarget(), cueStrike);
		} while (maxScore < 1 && (count < 40 || maxEvent == null));

		float time = (System.currentTimeMillis() - before) / 1000f;
		logger.log(Level.FINE, "Generated " + count + " shots in " + time
				+ "s : " + count / time + " shots/s");

		phase.setAngDest(maxEvent.getAngDest());
		phase.setAngElev(maxEvent.getAngElev());
		phase.setTransX(maxEvent.getTransX());
		phase.setTransY(maxEvent.getTransY());
		phase.setVelocity(maxEvent.getVelocity());

		collector.registerPlannedBalls(maxTarget.getBallKeys());

		enableAimLine(1000, maxEvent);
		toggleMarkings();

		phase.finish();
	}

	private void handleAimingFallbackOneBallFallback(AimingPhase phase) {
		RandomCueInteractionGenerator randomCIGenerator = new RandomCueInteractionGenerator();
		SimpleScoreEvaluator simpleScoreEvaluator = new SimpleScoreEvaluator();
		simpleScoreEvaluator.setCriticalScore(0.2);

		long before = System.currentTimeMillis();
		int count = 0;

		do {
			count++;
			cueStrike = randomCIGenerator.generate(state);
			// enableAimLine(1, cueStrike);

		} while (!simpleScoreEvaluator.evaluate(cueStrike, randomCIGenerator
				.getTarget(), state));

		float time = (System.currentTimeMillis() - before) / 1000f;
		logger.log(Level.FINE, "Generated " + count + " shots in " + time
				+ "s : " + count / time + " shots/s");

		phase.setAngDest(cueStrike.getAngDest());
		phase.setAngElev(cueStrike.getAngElev());
		phase.setTransX(cueStrike.getTransX());
		phase.setTransY(cueStrike.getTransY());
		phase.setVelocity(cueStrike.getVelocity());

		enableAimLine(1000, cueStrike);
		toggleMarkings();

		phase.finish();
	}

	private void handleAimingFallbackPhaseOne(AimingPhase phase) {
		logger.log(Level.INFO, "Falling back to random search");
		RandomCueInteractionGenerator randomCIGenerator = new RandomCueInteractionGenerator();
		SamplingRandomEvaluator samplingRandomEvaluator = new SamplingRandomEvaluator(
				5);
		SimpleScoreEvaluator simpleScoreEvaluator = new SimpleScoreEvaluator();
		samplingRandomEvaluator.setCriticalScore(0.4f);
		simpleScoreEvaluator.setCriticalScore(.1f);
		ArrayList<CueInteraction> candidates = new ArrayList<CueInteraction>();

		long before = System.currentTimeMillis();
		int count = 0;
		int maxCount = 10000;
		double percent = 0;
		double minPercentage = 1f;
		double safetyShotPercentage = 0.4f;
		double modPart = 1000;
		do {
			count++;
			cueStrike = randomCIGenerator.generate(state);
			if (samplingRandomEvaluator.evaluate(cueStrike, randomCIGenerator
					.getTarget(), state)) {
				candidates.add(cueStrike);
				SimpleSamplingEvaluator sampler = new SimpleSamplingEvaluator(
						100);
				toggleAimLine(cueStrike);
				sampler.evaluate(cueStrike, randomCIGenerator.getTarget(),
						state);
				toggleAimLine(cueStrike);
				percent = sampler.getScore();
			}
			if (count % modPart == 0) {
				minPercentage -= 0.1;
				logger.log(Level.FINER, "Decreasing minPercentage by 0.1");
				for (CueInteraction candidate : candidates) {
					if (samplingRandomEvaluator.evaluate(candidate,
							randomCIGenerator.getTarget(), state)) {
						SimpleSamplingEvaluator sampler = new SimpleSamplingEvaluator(
								5);
						toggleAimLine(candidate);
						sampler.evaluate(candidate, randomCIGenerator
								.getTarget(), state);
						toggleAimLine(candidate);
						percent = sampler.getScore();
						if (percent >= minPercentage) {
							cueStrike = candidate;
							logger
									.log(Level.FINER, "Took candidate",
											cueStrike);
							break;
						}
					}
				}
			}
		} while (percent < minPercentage && count < maxCount
				&& minPercentage > safetyShotPercentage);

		if (count >= maxCount || minPercentage <= safetyShotPercentage) {
			handleAimingFallbackPhaseTwo(phase);
			return;

		} else {
			logger.log(Level.FINE, "took shot with " + percent
					+ "% success probability");

		}

		float time = (System.currentTimeMillis() - before) / 1000f;
		logger.log(Level.FINE, "Generated " + count + " shots in " + time
				+ "s : " + count / time + " shots/s");

		phase.setAngDest(cueStrike.getAngDest());
		phase.setAngElev(cueStrike.getAngElev());
		phase.setTransX(cueStrike.getTransX());
		phase.setTransY(cueStrike.getTransY());
		phase.setVelocity(cueStrike.getVelocity());

		enableAimLine(1000, cueStrike);
		toggleMarkings();

		phase.finish();
	}

	private void handleAimingFallbackPhaseTwo(AimingPhase phase) {
		logger.log(Level.INFO, "Falling back to safety shots");

		RandomCueInteractionGenerator ciGenerator = new RandomCueInteractionGenerator();

		DirectPocketGenerator secondLevelPocketGenerator = new DirectPocketGenerator(
				3);
		DirectPocketingEvaluator secondLevelPocketEvaluator = new DirectPocketingEvaluator();
		DirectCueGenerator secondLevelCueGenerator = new DirectCueGenerator(
				secondLevelPocketGenerator, secondLevelPocketEvaluator);
		CueInteractionToPocketEvaluator secondLevelEvaluator = new CueInteractionToPocketEvaluator(
				4);
		SafetyShotEvaluator finalEvaluator = new SafetyShotEvaluator(4,
				secondLevelCueGenerator, secondLevelEvaluator, 30);

		finalEvaluator.setCriticalScore(0f);
		secondLevelEvaluator.setCriticalScore(-1f);
		secondLevelPocketEvaluator.setCriticalScore(.5f);

		long before = System.currentTimeMillis();
		int count = 0;

		do {
			count++;
			cueStrike = ciGenerator.generate(state);
			// enableAimLine(10, cueStrike);

		} while (!finalEvaluator.evaluate(cueStrike, ciGenerator.getTarget(),
				state));

		float time = (System.currentTimeMillis() - before) / 1000f;
		logger.log(Level.FINE, "Generated " + count + " shots in " + time
				+ "s : " + count / time + " shots/s");

		phase.setAngDest(cueStrike.getAngDest());
		phase.setAngElev(cueStrike.getAngElev());
		phase.setTransX(cueStrike.getTransX());
		phase.setTransY(cueStrike.getTransY());
		phase.setVelocity(cueStrike.getVelocity());
		toggleMarkings();

		enableAimLine(1000, cueStrike);
		phase.finish();
	}

	private void toggleMarkings() {
		if (!showMarkings) {
			((SnookerTable3D) presentation).showMarkings(true);
			((SnookerTable3D) presentation).markBalls(state.getOnBalls());
			showMarkings = !showMarkings;
		} else {
			((SnookerTable3D) presentation).showMarkings(false);
			showMarkings = !showMarkings;
		}
	}
}
