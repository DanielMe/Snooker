package de.danielmescheder.snooker.domain;

import java.util.HashSet;
import java.util.Set;

import de.danielmescheder.snooker.simulation.event.EnterTileEvent;
import de.danielmescheder.snooker.simulation.event.LeaveTileEvent;
import de.danielmescheder.snooker.simulation.physics.Physics;


public class Tile
{
	private String name;
	private float fromX, toX, fromY, toY;

	private float height,width;
	private Tile northTile, eastTile, southTile, westTile;

	private Set<CollidableGameObject> content;

	public Tile(String name, float fromX, float toX, float fromY, float toY)
	{
		this.name = name;
		this.fromX = Math.min(fromX, toX);
		this.toX = Math.max(fromX, toX);
		this.width = toX-fromX;
		this.fromY = Math.min(fromY, toY);
		this.toY = Math.max(fromY, toY);
		this.height = toY-fromY;

		content = new HashSet<CollidableGameObject>();
	}

	public float getFromX() {
		return fromX;
	}
	
	public void setFromX(float fromX) {
		this.fromX = fromX;
	}
	
	public float getToX() {
		return toX;
	}
	
	public void setToX(float toX) {
		this.toX = toX;
	}
	
	public float getFromY() {
		return fromY;
	}
	
	public void setFromY(float fromY) {
		this.fromY = fromY;
	}
	
	public float getToY() {
		return toY;
	}
	
	public void setToY(float toY) {
		this.toY = toY;
	}
	
	@Override
	public Object clone(){
		return new Tile(name,fromX,toX,fromY,toY);
	}
	public Tile getNorthTile()
	{
		return northTile;
	}

	public void setNorthTile(Tile northTile)
	{
		this.northTile = northTile;
	}

	public Tile getEastTile()
	{
		return eastTile;
	}

	public void setEastTile(Tile eastTile)
	{
		this.eastTile = eastTile;
	}

	public Tile getSouthTile()
	{
		return southTile;
	}

	public void setSouthTile(Tile southTile)
	{
		this.southTile = southTile;
	}

	public Tile getWestTile()
	{
		return westTile;
	}

	public void setWestTile(Tile westTile)
	{
		this.westTile = westTile;
	}

	public void addGameObject(CollidableGameObject o)
	{
		content.add(o);
	}

	public Set<CollidableGameObject> getContent()
	{
		return content;
	}

	public float getLowerBoundX()
	{
		return fromX;
	}

	public float getUpperBoundX()
	{
		return toX;
	}

	public float getLowerBoundY()
	{
		return fromY;
	}
	
	public float getUpperBoundY()
	{
		return toY;
	}
	
	public float getWidth()
	{
		return width;
	}
	
	public float getLength()
	{
		return height;
	}

	public LeaveTileEvent getLeaveEvent(BilliardBall b)
	{
		float minTime = Float.MAX_VALUE;
		float t = Physics.lineCrossingTime(b, this.toX + b.getRadius(), false,
				1);
		if (t >= 0)
		{
			minTime = Math.min(minTime, t);
		}
		t = Physics.lineCrossingTime(b, this.fromX - b.getRadius(), false, -1);
		if (t >= 0)
		{
			minTime = Math.min(minTime, t);
		}
		t = Physics.lineCrossingTime(b, this.toY + b.getRadius(), true, 1);
		if (t >= 0)
		{
			minTime = Math.min(minTime, t);
		}
		t = Physics.lineCrossingTime(b, this.fromY - b.getRadius(), true, -1);
		if (t >= 0)
		{
			minTime = Math.min(minTime, t);
		}

		if (minTime == Float.MAX_VALUE)
		{
			return null;
		}
		else
		{
			return new LeaveTileEvent(minTime + b.getTime(), b, this);
		}
	}

	public Set<EnterTileEvent> getEnterEvents(BilliardBall b)
	{
		Set<EnterTileEvent> events = new HashSet<EnterTileEvent>();
		float t;
		if (getEastTile() != null)
		{
			t = Physics.lineCrossingTime(b, this.toX - b.getRadius(), false, 1);
			if (t >= 0)
			{
				events.add(new EnterTileEvent(b.getTime() + t, b,
								getEastTile()));
			}
		}
		if (getWestTile() != null)
		{
			t = Physics.lineCrossingTime(b, this.fromX + b.getRadius(), false,
					-1);
			if (t >= 0)
			{
				events.add(new EnterTileEvent(b.getTime() + t, b,
								getWestTile()));
			}
		}
		if (getNorthTile() != null)
		{
			t = Physics.lineCrossingTime(b, this.toY - b.getRadius(), true, 1);
			if (t >= 0)
			{
				events.add(new EnterTileEvent(b.getTime() + t, b,
						getNorthTile()));
			}
		}
		if (getSouthTile() != null)
		{
			t = Physics.lineCrossingTime(b, this.fromY + b.getRadius(), true,
					-1);
			if (t >= 0)
			{
				events.add(new EnterTileEvent(b.getTime() + t, b,
						getSouthTile()));
			}
		}
		return events;

	}

	@Override
	public String toString()
	{
		return "[Tile "+name+" " + fromX + "-" + toX + " " + fromY + "-" + toY + "]";
	}
}
