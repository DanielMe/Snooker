package de.danielmescheder.snooker.adt.impl;

import de.danielmescheder.snooker.adt.def.BinaryTree;
import de.danielmescheder.snooker.adt.def.Position;

public abstract class EulerTour<E>
{
	public abstract void visitLeft(Position<E> p);

	public abstract void visitRight(Position<E> p);

	public abstract void visitBelow(Position<E> p);

	public void tourLeft(BinaryTree<E> t, Position<E> p)
	{
		visitLeft(p);
		if (stopped)
		{
			pos = p;
			return;
		}
		if (t.isInternal(p))
		{
			tourLeft(t, t.leftChild(p));
		}
		else
		{
			tourBelow(t, p);
		}
	}

	public void tourRight(BinaryTree<E> t, Position<E> p)
	{
		visitRight(p);
		if (stopped)
		{
			pos = p;
			return;
		}
		if (!t.isRoot(p))
		{
			if (t.rightChild(t.parent(p)) == p)
			{
				tourRight(t, t.parent(p));
			}
			else
			{
				tourBelow(t, t.parent(p));
			}
		}
		else
		{
			stop();
		}
	}

	public void tourBelow(BinaryTree<E> t, Position<E> p)
	{
		visitBelow(p);
		if (stopped)
		{
			pos = p;
			return;
		}
		if (t.isInternal(p))
		{
			tourLeft(t, t.rightChild(p));
		}
		else
		{
			tourRight(t, p);
		}
	}

	public void stop()
	{
		stopped = true;
	}

	public void start()
	{
		stopped = false;
	}

	public boolean stopped()
	{
		return stopped;
	}

	public Position<E> position()
	{
		return pos;
	}

	private boolean stopped = false;
	private Position<E> pos;
}
