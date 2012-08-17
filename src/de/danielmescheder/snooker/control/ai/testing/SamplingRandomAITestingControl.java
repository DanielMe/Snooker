package de.danielmescheder.snooker.control.ai.testing;

import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.danielmescheder.snooker.control.ai.SamplingRandomAIControl;
import de.danielmescheder.snooker.control.ai.evaluator.SamplingRandomEvaluator;
import de.danielmescheder.snooker.control.ai.evaluator.SimpleSamplingEvaluator;
import de.danielmescheder.snooker.control.ai.evaluator.SimpleScoreEvaluator;
import de.danielmescheder.snooker.control.ai.generator.RandomCueInteractionGenerator;
import de.danielmescheder.snooker.control.ui.controller.SimulationControl;
import de.danielmescheder.snooker.domain.BilliardBall;
import de.danielmescheder.snooker.gameflow.GameState;
import de.danielmescheder.snooker.gameflow.phases.AimingPhase;
import de.danielmescheder.snooker.gameflow.phases.SimulationPhase;
import de.danielmescheder.snooker.presentation.SnookerTable3D;
import de.danielmescheder.snooker.presentation.TablePresentation;
import de.danielmescheder.snooker.simulation.event.CueInteraction;
import de.danielmescheder.snooker.testing.TestDataCollector;


/**
 * A version of the {@link SamplingRandomAIControl} that makes use of the
 * {@link TestDataCollector}
 * 
 * @see SamplingRandomAIControl
 */
public class SamplingRandomAITestingControl extends SamplingRandomAIControl
{
	
	private boolean showMarkings = false;
	
	private static final Logger logger = Logger.getLogger(SamplingRandomAITestingControl.class.getName());


	private TestDataCollector collector;
	


	public SamplingRandomAITestingControl(TablePresentation presentation, GameState state, TestDataCollector collector)
	{
		super(presentation,state);
		this.collector = collector;
	}
	

	@Override
	protected void handleSimulation(SimulationPhase phase)
	{
		presentation.setGameControl(new SimulationControl(presentation, phase, state, collector));
	}



	@Override
	protected void handleAiming(AimingPhase phase)
	{	
		toggleMarkings();

		RandomCueInteractionGenerator randomCIGenerator = new RandomCueInteractionGenerator();
		SamplingRandomEvaluator samplingRandomEvaluator = new SamplingRandomEvaluator(5);
		SimpleScoreEvaluator simpleScoreEvaluator = new SimpleScoreEvaluator();
		samplingRandomEvaluator.setCriticalScore(0.4f);
		simpleScoreEvaluator.setCriticalScore(.1f);
		
		long before = System.currentTimeMillis();
		int count = 0;
		int maxCount = 50000;
		double percent = 0;
		double minPercentage = 1f; 
		double safetyShotPercentage = 0.4f;
		double modPart = 5000;
		do
		{
			count++;
			cueStrike = randomCIGenerator.generate(state);
			if(samplingRandomEvaluator.evaluate(cueStrike,randomCIGenerator.getTarget(), state)){
				candidates.add(cueStrike);
				SimpleSamplingEvaluator sampler = new SimpleSamplingEvaluator(10);
				toggleAimLine(cueStrike);
				sampler.evaluate(cueStrike, randomCIGenerator.getTarget(), state);
				toggleAimLine(cueStrike);
				percent = sampler.getScore();
			}
			if(count % modPart == 0){
				minPercentage -= 0.1;
				logger.log(Level.FINER, "Decreasing minPercentage by 0.1");
				logger.log(Level.FINER, "Rechecking Candidates");
				for(CueInteraction candidate : candidates){
					if(samplingRandomEvaluator.evaluate(candidate,randomCIGenerator.getTarget(), state)){
						SimpleSamplingEvaluator sampler = new SimpleSamplingEvaluator(10);
						toggleAimLine(candidate);
						sampler.evaluate(candidate, randomCIGenerator.getTarget(), state);
						toggleAimLine(candidate);
						percent = sampler.getScore();
						if(percent >= minPercentage){
							cueStrike = candidate;
							logger.log(Level.FINER, "Took candidate", cueStrike);
							break;
						}
					}	
				}

			}
		} while (percent < minPercentage && count < maxCount && minPercentage > safetyShotPercentage);
		
		if(count >= maxCount || minPercentage <= safetyShotPercentage){
			logger.log(Level.FINER, "Generate safety shot");
			do
			{
				cueStrike = randomCIGenerator.generate(state);
			} while (!simpleScoreEvaluator.evaluate(cueStrike,randomCIGenerator.getTarget(), state));

		}
		else{
			logger.log(Level.FINE,"took shot with " + percent + "% success probability");
			
		}
		
		float time = (System.currentTimeMillis() - before)/1000f;
		logger.log(Level.FINE,"Generated " + count + " shots in "+ time + "s : "+ count/time + " shots/s");

		
		phase.setAngDest(cueStrike.getAngDest());
		phase.setAngElev(cueStrike.getAngElev());
		phase.setTransX(cueStrike.getTransX());
		phase.setTransY(cueStrike.getTransY());
		phase.setVelocity(cueStrike.getVelocity());
		
		enableAimLine(1000,cueStrike);
		
		simpleScoreEvaluator.evaluate(cueStrike,randomCIGenerator.getTarget(), state);
		Set<BilliardBall> plannedPottedBalls = simpleScoreEvaluator.getPlannedOutcomeSet();
		if(plannedPottedBalls != null){
			collector.registerPlannedBalls(plannedPottedBalls);			
		}
		toggleMarkings();

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
