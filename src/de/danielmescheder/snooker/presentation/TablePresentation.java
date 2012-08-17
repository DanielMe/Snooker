package de.danielmescheder.snooker.presentation;

import java.util.ArrayList;
import java.util.Set;


import com.jme.app.SimplePassGame;
import com.jme.input.InputHandler;
import com.jme.input.KeyBindingManager;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.scene.Node;

import de.danielmescheder.snooker.domain.BilliardBall;

/**
 * The abstract class TablePresentation offers an interface for 
 * communication with the graphical representation of the simulation
 * using the JMonkeyEngine. {@link http://www.jMonkeyEngine.com}
 *
 */
public abstract class TablePresentation extends SimplePassGame implements Runnable
{
	protected InputHandler cameraControl;
	
	public abstract Node getTableNode();
	public abstract Node getCueBallNode();
	public abstract void updateBall(BilliardBall ball);
	
	/**
	 * Gets the current camera
	 * @return a camera
	 */
	public Camera getCamera()
	{
		return cam;
	}
	
	/** 
	 * Gets the cameraControl InputHandler
	 * @return the cameraControl
	 */ 
	public InputHandler getCameraControl()
	{
		return cameraControl;
	}
	
	/**
	 * Gets the gameControl InputHandler
	 * @return the gameControl
	 */
	public InputHandler getGameControl()
	{
		return input;
	}
	
	/**
	 * Sets the cameraControl for the presentation
	 * @param cam the cameraControl
	 */
	public void setCameraControl(InputHandler cam)
	{
		this.cameraControl = cam;
	}
	
	public void setGameControl(InputHandler control)
	{
		this.input = control;
	}
	
	@Override
	protected void simpleUpdate()
	{
		super.simpleUpdate();
		if (KeyBindingManager.getKeyBindingManager().isValidCommand("exit"))
		{
			finished = true;
		}
		input.update(tpf);
        if(cameraControl!=null)
        {
        	cameraControl.update(tpf);
        }
	}
	
	public abstract void initBalls();
	
	public abstract Node setCueToBall(Node ball);
	public abstract void rotateCue(float x, float y);
	public abstract void transposeCue(float x, float y);
	public abstract void setCueDistance(float dist);
	public abstract void showShotVisualization(boolean enabled);
	public abstract void updateShotVisualization(ArrayList<ArrayList<Vector3f>> points);
	public abstract Node getBallNode(BilliardBall b);
	public abstract void removeCue();
	
	public abstract void updateTileVisualization(Set<BilliardBall> balls);

	public boolean isReady()
	{
		return true;
	}

	@Override
	public void run()
	{
		super.start();
	}
	

}
