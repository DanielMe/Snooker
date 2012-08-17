package de.danielmescheder.snooker.exec;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.LogManager;

import de.danielmescheder.snooker.gameflow.GameFlow;


public class UMSnooker
{
    public static void main(String[] args) {
    	
    	try
		{
			LogManager.getLogManager().readConfiguration(new FileInputStream("logging.properties"));
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
        GameFlow game = new GameFlow();
        game.start();
    }
}
