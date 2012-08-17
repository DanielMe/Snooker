package de.danielmescheder.snooker.control.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


import com.jme.input.ChaseCamera;
import com.jme.input.thirdperson.ThirdPersonMouseLook;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;

import de.danielmescheder.snooker.control.AimLine;
import de.danielmescheder.snooker.control.ControllingUnit;
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
import de.danielmescheder.snooker.gameflow.GamePhase;
import de.danielmescheder.snooker.gameflow.GameState;
import de.danielmescheder.snooker.gameflow.phases.AimingPhase;
import de.danielmescheder.snooker.gameflow.phases.FrameInitPhase;
import de.danielmescheder.snooker.gameflow.phases.PositioningPhase;
import de.danielmescheder.snooker.gameflow.phases.SimulationPhase;
import de.danielmescheder.snooker.presentation.SnookerTable3D;
import de.danielmescheder.snooker.presentation.TablePresentation;
import de.danielmescheder.snooker.simulation.event.CueInteraction;
import de.danielmescheder.snooker.simulation.event.SingleBallEvent;

/**
 * The PlanningSamplingAI is an advanced AI that makes use of several
 * {@link EventGenerator} and {@link EventEvalutator}s working together. For an
 * explanation of the AI, please refer to the paper.
 * 
 */
public class PlanningSamplingAI implements ControllingUnit
{
	protected static final Logger logger = Logger.getLogger(PlanningSamplingAI.class.getName());

	protected TablePresentation presentation;
	protected GameState state;
	protected float accuracy = 0.0005f;
	protected int visualizationDepth = 1;
	protected CueInteraction maxEvent, cueStrike;
	protected boolean showLine = true, showMarkings = false;

	public PlanningSamplingAI(TablePresentation presentation, GameState state)
	{
		this.presentation = presentation;
		this.state = state;
	}

	public void handlePhase(GamePhase phase)
	{
		HashMap<String, Object> props = new HashMap<String, Object>();

		props.put(ThirdPersonMouseLook.PROP_MAXROLLOUT, "3");
		props.put(ThirdPersonMouseLook.PROP_MINROLLOUT, "1");
		props.put(ThirdPersonMouseLook.PROP_MINASCENT, "" + 45 * FastMath.DEG_TO_RAD);
		props.put(ThirdPersonMouseLook.PROP_MAXASCENT, "" + 45 * FastMath.DEG_TO_RAD);
		props.put(ChaseCamera.PROP_INITIALSPHERECOORDS, new Vector3f(3, 0, 30 * FastMath.DEG_TO_RAD));
		props.put(ChaseCamera.PROP_WORLDUPVECTOR, new Vector3f(0, 0, 1));

		props.put(ChaseCamera.PROP_TARGETOFFSET, new Vector3f(0, 0, 0));

		ChaseCamera chaser = new ChaseCamera(presentation.getCamera(), presentation.getTableNode(), props);
		chaser.setMinDistance(state.getTable().getLength());
		chaser.setMaxDistance(state.getTable().getLength());

		presentation.setCameraControl(chaser);

		if (phase instanceof FrameInitPhase)
		{
			handleInit((FrameInitPhase) phase);
		}
		else if (phase instanceof PositioningPhase)
		{
			handlePositioning((PositioningPhase) phase);
		}
		else if (phase instanceof AimingPhase)
		{
			handleAiming((AimingPhase) phase);
		}
		else if (phase instanceof SimulationPhase)
		{
			handleSimulation((SimulationPhase) phase);
		}
	}

	protected void handleInit(FrameInitPhase phase)
	{
		presentation.initBalls();
	}

	protected void handleSimulation(SimulationPhase phase)
	{
		presentation.setGameControl(new SimulationControl(presentation, phase, state));
	}

	protected void handlePositioning(PositioningPhase phase)
	{
		phase.setPosition(state.getTable().getBrownSpot().x + state.getTable().getDRadius() / 2f, state.getTable().getBrownSpot().y
				- state.getTable().getDRadius() / 2f);
		phase.finish();
	}

	protected void handleAiming(AimingPhase phase)
	{

		toggleMarkings();

		if (state.getBalls().size() <= 1)
		{
			handleAimingFallbackOneBall(phase);
		}

		DirectPocketGenerator pocketingGenerator = new DirectPocketGenerator(2);
		DirectPocketingEvaluator pocketingEvaluator = new DirectPocketingEvaluator();
		DirectCueGenerator ciGenerator = new DirectCueGenerator(pocketingGenerator, pocketingEvaluator);

		DirectPocketGenerator secondLevelPocketGenerator = new DirectPocketGenerator(1);
		DirectCueGenerator secondLevelCueGenerator = new DirectCueGenerator(secondLevelPocketGenerator, pocketingEvaluator);
		CueInteractionToPocketEvaluator secondLevelEvaluator = new CueInteractionToPocketEvaluator(3);
		DepthSamplingEvaluator finalEvaluator = new DepthSamplingEvaluator(10, secondLevelCueGenerator, secondLevelEvaluator, 25);

		finalEvaluator.setCriticalScore(.2f);
		secondLevelEvaluator.setCriticalScore(0f);
		pocketingEvaluator.setCriticalScore(.5f);

		long before = System.currentTimeMillis();
		int count = 0;
		cueStrike = null;
		maxEvent = null;
		double maxScore = -1;

		do
		{
			count++;
			cueStrike = ciGenerator.generate(state);
			if (cueStrike == null)
			{
				handleAimingFallbackPhaseTwo(phase);
				return;
			}
			if (finalEvaluator.evaluate(cueStrike, ciGenerator.getTarget(), state))
			{
				if (finalEvaluator.getScore() > maxScore)
				{
					maxScore = finalEvaluator.getScore();
					maxEvent = cueStrike;
				}
			}
			enableAimLine(100, cueStrike);
			logger.log(Level.FINE, "Trying event in order to pocket " + ciGenerator.getTarget(), cueStrike);
		} while (maxScore < 1 && (count < 60));
		if (maxEvent == null)
		{
			handleAimingFallbackPhaseTwo(phase);
			return;
		}

		float time = (System.currentTimeMillis() - before) / 1000f;
		logger.log(Level.FINE, "Generated " + count + " shots in " + time + "s : " + count / time + " shots/s");

		phase.setAngDest(maxEvent.getAngDest());
		phase.setAngElev(maxEvent.getAngElev());
		phase.setTransX(maxEvent.getTransX());
		phase.setTransY(maxEvent.getTransY());
		phase.setVelocity(maxEvent.getVelocity());

		enableAimLine(1000, maxEvent);
		toggleMarkings();
		phase.finish();
	}

	private void handleAimingFallbackOneBall(AimingPhase phase)
	{
		DirectPocketGenerator pocketingGenerator = new DirectPocketGenerator(1);
		DirectPocketingEvaluator pocketingEvaluator = new DirectPocketingEvaluator();
		DirectCueGenerator ciGenerator = new DirectCueGenerator(pocketingGenerator, pocketingEvaluator);
		CueInteractionToPocketEvaluator finalEvaluator = new CueInteractionToPocketEvaluator(10);

		finalEvaluator.setCriticalScore(.1f);
		pocketingEvaluator.setCriticalScore(.5f);

		long before = System.currentTimeMillis();
		int count = 0;
		cueStrike = null;
		maxEvent = null;
		double maxScore = -1;

		do
		{
			count++;
			cueStrike = ciGenerator.generate(state);
			if (cueStrike == null)
			{
				handleAimingFallbackOneBallFallback(phase);
				return;
			}
			enableAimLine(10, cueStrike);

			if (finalEvaluator.evaluate(cueStrike, ciGenerator.getTarget(), state))
			{
				if (finalEvaluator.getScore() > maxScore)
				{
					maxScore = finalEvaluator.getScore();
					maxEvent = cueStrike;
				}
			}
			enableAimLine(100, cueStrike);

			logger.log(Level.FINE, "Trying event in order to pocket " + ciGenerator.getTarget(), cueStrike);
		} while (maxScore < 1 && (count < 40));
		if(maxEvent == null)
		{
			handleAimingFallbackOneBallFallback(phase);
		}

		float time = (System.currentTimeMillis() - before) / 1000f;
		logger.log(Level.FINE, "Generated " + count + " shots in " + time + "s : " + count / time + " shots/s");

		phase.setAngDest(maxEvent.getAngDest());
		phase.setAngElev(maxEvent.getAngElev());
		phase.setTransX(maxEvent.getTransX());
		phase.setTransY(maxEvent.getTransY());
		phase.setVelocity(maxEvent.getVelocity());

		enableAimLine(1000, maxEvent);

		phase.finish();
	}

	private void handleAimingFallbackOneBallFallback(AimingPhase phase)
	{
		logger.log(Level.INFO, "Falling back to one ball search");

		RandomCueInteractionGenerator randomCIGenerator = new RandomCueInteractionGenerator();
		SimpleScoreEvaluator simpleScoreEvaluator = new SimpleScoreEvaluator();
		simpleScoreEvaluator.setCriticalScore(0.0);

		long before = System.currentTimeMillis();
		int count = 0;

		do
		{
			count++;
			cueStrike = randomCIGenerator.generate(state);
			enableAimLine(10, cueStrike);

		} while (!simpleScoreEvaluator.evaluate(cueStrike, randomCIGenerator.getTarget(), state));

		float time = (System.currentTimeMillis() - before) / 1000f;
		logger.log(Level.FINE, "Generated " + count + " shots in " + time + "s : " + count / time + " shots/s");

		phase.setAngDest(cueStrike.getAngDest());
		phase.setAngElev(cueStrike.getAngElev());
		phase.setTransX(cueStrike.getTransX());
		phase.setTransY(cueStrike.getTransY());
		phase.setVelocity(cueStrike.getVelocity());

		enableAimLine(1000, cueStrike);

		phase.finish();
	}

	private void handleAimingFallbackPhaseOne(AimingPhase phase)
	{
		logger.log(Level.INFO, "Falling back to random search");
		RandomCueInteractionGenerator randomCIGenerator = new RandomCueInteractionGenerator();
		SamplingRandomEvaluator samplingRandomEvaluator = new SamplingRandomEvaluator(5);
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
		do
		{
			count++;
			cueStrike = randomCIGenerator.generate(state);
			if (samplingRandomEvaluator.evaluate(cueStrike, randomCIGenerator.getTarget(), state))
			{
				candidates.add(cueStrike);
				SimpleSamplingEvaluator sampler = new SimpleSamplingEvaluator(100);
				toggleAimLine(cueStrike);
				sampler.evaluate(cueStrike, randomCIGenerator.getTarget(), state);
				toggleAimLine(cueStrike);
				percent = sampler.getScore();
			}
			if (count % modPart == 0)
			{
				minPercentage -= 0.1;
				logger.log(Level.FINER, "Decreasing minPercentage by 0.1");
				for (CueInteraction candidate : candidates)
				{
					if (samplingRandomEvaluator.evaluate(candidate, randomCIGenerator.getTarget(), state))
					{
						SimpleSamplingEvaluator sampler = new SimpleSamplingEvaluator(5);
						toggleAimLine(candidate);
						sampler.evaluate(candidate, randomCIGenerator.getTarget(), state);
						toggleAimLine(candidate);
						percent = sampler.getScore();
						if (percent >= minPercentage)
						{
							cueStrike = candidate;
							logger.log(Level.FINER, "Took candidate", cueStrike);
							break;
						}
					}
				}
			}
		} while (percent < minPercentage && count < maxCount && minPercentage > safetyShotPercentage);

		if (count >= maxCount || minPercentage <= safetyShotPercentage)
		{
			handleAimingFallbackPhaseTwo(phase);
			return;

		}
		else
		{
			logger.log(Level.FINE, "took shot with " + percent + "% success probability");

		}

		float time = (System.currentTimeMillis() - before) / 1000f;
		logger.log(Level.FINE, "Generated " + count + " shots in " + time + "s : " + count / time + " shots/s");

		phase.setAngDest(cueStrike.getAngDest());
		phase.setAngElev(cueStrike.getAngElev());
		phase.setTransX(cueStrike.getTransX());
		phase.setTransY(cueStrike.getTransY());
		phase.setVelocity(cueStrike.getVelocity());

		enableAimLine(1000, cueStrike);
		toggleMarkings();
		phase.finish();
	}

	private void handleAimingFallbackPhaseTwo(AimingPhase phase)
	{
		logger.log(Level.INFO, "Falling back to safety shots");

		RandomCueInteractionGenerator ciGenerator = new RandomCueInteractionGenerator();

		DirectPocketGenerator secondLevelPocketGenerator = new DirectPocketGenerator(1);
		DirectPocketingEvaluator secondLevelPocketEvaluator = new DirectPocketingEvaluator();
		DirectCueGenerator secondLevelCueGenerator = new DirectCueGenerator(secondLevelPocketGenerator, secondLevelPocketEvaluator);
		CueInteractionToPocketEvaluator secondLevelEvaluator = new CueInteractionToPocketEvaluator(4);
		SafetyShotEvaluator finalEvaluator = new SafetyShotEvaluator(4, secondLevelCueGenerator, secondLevelEvaluator, 30);

		finalEvaluator.setCriticalScore(0f);
		secondLevelEvaluator.setCriticalScore(-1f);
		secondLevelPocketEvaluator.setCriticalScore(.5f);

		long before = System.currentTimeMillis();
		int count = 0;

		do
		{
			count++;
			cueStrike = ciGenerator.generate(state);
			enableAimLine(10, cueStrike);

		} while (!finalEvaluator.evaluate(cueStrike, ciGenerator.getTarget(), state));

		float time = (System.currentTimeMillis() - before) / 1000f;
		logger.log(Level.FINE, "Generated " + count + " shots in " + time + "s : " + count / time + " shots/s");

		phase.setAngDest(cueStrike.getAngDest());
		phase.setAngElev(cueStrike.getAngElev());
		phase.setTransX(cueStrike.getTransX());
		phase.setTransY(cueStrike.getTransY());
		phase.setVelocity(cueStrike.getVelocity());

		enableAimLine(1000, cueStrike);
		toggleMarkings();
		phase.finish();
	}

	protected void enableAimLine(long time, SingleBallEvent e)
	{
		presentation.showShotVisualization(true);
		AimLine aimLine = new AimLine(presentation, state, visualizationDepth, accuracy);
		aimLine.showAimLine(e);
		try
		{
			Thread.sleep(time);
		}
		catch (InterruptedException ex)
		{
			ex.printStackTrace();
		}
		presentation.showShotVisualization(false);
	}

	protected void toggleAimLine(SingleBallEvent e)
	{
		if (showLine)
		{
			presentation.showShotVisualization(true);
			AimLine aimLine = new AimLine(presentation, state, visualizationDepth, accuracy);
			aimLine.showAimLine(e);
		}
		else
		{
			presentation.showShotVisualization(false);
		}
		showLine = !showLine;
	}

	private void toggleMarkings()
	{
		if (!showMarkings)
		{
			((SnookerTable3D) presentation).showMarkings(true);
			((SnookerTable3D) presentation).markBalls(state.getOnBalls());
		}
		else
		{
			((SnookerTable3D) presentation).showMarkings(false);
		}
		showMarkings = !showMarkings;
	}

}
