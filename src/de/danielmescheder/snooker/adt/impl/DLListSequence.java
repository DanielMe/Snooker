package de.danielmescheder.snooker.adt.impl;

import de.danielmescheder.snooker.adt.def.Position;
import de.danielmescheder.snooker.adt.def.Sequence;
import de.danielmescheder.snooker.exception.InvalidPositionException;
import de.danielmescheder.snooker.exception.PositionOutOfRangeException;
import de.danielmescheder.snooker.exception.RankOutOfRangeException;

public class DLListSequence<E> extends DoubleLinkedList<E> implements
		Sequence<E>
{

	@Override
	public Position<E> atRank(int r) throws RankOutOfRangeException
	{
		Position<E> p;
		
		try
		{
			
			if (r >= size() / 2)
			{
				// The rank is in the upper half of the list.
				// it is cheaper to start at the last element and search
				// top-down
				p = last();
				for (int i = size() - 1; i > r; i--)
				{
					p = before(p);
				}
			}
			else
			{
				// The rank is in the lower half of the list.
				// it is cheaper to start at the first element and search
				// bottom up
				p = first();
				for (int i = 0; i < r; i++)
				{
					p = after(p);
				}
			}
		}
		catch (PositionOutOfRangeException e)
		{
			// It could happen that we get an PositionOutOfRange exception.
			// That means, that this rank is not in the list
			// We handle this by throwing a new Exception that
			// clearly indicates that the rank was illegal.
			throw new RankOutOfRangeException();
		}
		return p;
	}

	@Override
	public int rankOf(Position<E> p) throws InvalidPositionException
	{
		if (p.container() != this)
		{
			throw new InvalidPositionException();
		}
		if(isLast(p))
		{
			// This is an easy case:
			// p is the last element, so we don't have to search
			// the whole list.
			return size()-1;
		}
		Position<E> v = first();
		int rank = 0;
		try
		{
			while (v != p)
			{
				// as long as the desired position is not found
				// we continue searching.
				p = after(p);
				rank++;
			}
		}
		catch (PositionOutOfRangeException e)
		{
			// It could happen that the position was not found
			// We then throw the exception again.
			throw e;
		}
		return rank;
	}

	@Override
	public E elemAtRank(int r) throws RankOutOfRangeException
	{
		return atRank(r).element();
	}

	@Override
	public void insertAtRank(int r, E e) throws RankOutOfRangeException
	{
		insertBefore(atRank(r), e);
	}

	@Override
	public void removeAtRank(int r) throws RankOutOfRangeException
	{
		remove(atRank(r));
	}

	@Override
	public E replaceAtRank(int r, E e) throws RankOutOfRangeException
	{
		return replaceElement(atRank(r), e);
	}

}
