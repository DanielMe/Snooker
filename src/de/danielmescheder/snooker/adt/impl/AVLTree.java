package de.danielmescheder.snooker.adt.impl;

import java.io.Serializable;

import de.danielmescheder.snooker.adt.def.Comparator;
import de.danielmescheder.snooker.adt.def.Dictionary;
import de.danielmescheder.snooker.adt.def.Position;
import de.danielmescheder.snooker.adt.def.PriorityQueue;
import de.danielmescheder.snooker.exception.InvalidPositionException;


/**
 * The only thing the AVLTree does is extending the LinkedBinarySearchTree so
 * that it keeps the height balanced property.
 * 
 * @author Daniel Mescheder
 * 
 * @param <K>
 *            Keys to store
 * @param <E>
 *            Elements to store
 */
public class AVLTree<K, E> extends LinkedBinarySearchTree<K, E> implements
		Serializable, Dictionary<K, E>, PriorityQueue<K, E>
{
	private static final long serialVersionUID = 5478496791422905574L;

	public AVLTree(Comparator<K> comparator)
	{
		super(comparator);
	}

	@Override
	public Position<KeyElementPair<K, E>> insertItemAt(KeyElementPair<K, E> kep,
			Position<KeyElementPair<K, E>> p) throws InvalidPositionException
	{
		if (p.container() != this)
		{
			throw new InvalidPositionException();
		}
		Position<KeyElementPair<K, E>> ret = super.insertItemAt(kep, p);

		while (!isRoot(p))
		{
			p = parent(p);
			if (!isBalanced(p))
			{
				restructure(higherChild(higherChild(p)), higherChild(p), p);
			}
		}
		
		return ret;
	}

	@Override
	public Position<KeyElementPair<K, E>> removeAt(Position<KeyElementPair<K, E>> p) throws InvalidPositionException
	{
		if (p.container() != this)
		{
			throw new InvalidPositionException();
		}
		Position<KeyElementPair<K, E>> q = super.removeAt(p);
		Position<KeyElementPair<K, E>> parent = q;
		

		while (q!=null)
		{
			if (!isBalanced(q))
			{
				restructure(higherChild(higherChild(q)), higherChild(q), q);
			}
			if(isRoot(q))
			{
				q=null;
			}
			else
			{
				q = parent(q);
			}

		}
		return parent;
	}

	private void restructure(Position<KeyElementPair<K, E>> x,
			Position<KeyElementPair<K, E>> y, Position<KeyElementPair<K, E>> z)
	{
		boolean leftY = (leftChild(z) == y);
		boolean leftX = (leftChild(y) == x);

		Position<KeyElementPair<K, E>> a = null, b = null, c = null, t1 = null, t2 = null, t3 = null, t4 = null;

		if (leftY && leftX)
		{
			a = x;
			b = y;
			c = z;
			t2 = rightChild(a);
			t3 = rightChild(b);
		}
		else if (leftY && !leftX)
		{
			a = y;
			b = x;
			c = z;
			t2 = leftChild(b);
			t3 = rightChild(b);
		}
		else if (!leftY && leftX)
		{
			a = z;
			b = x;
			c = y;
			t2 = leftChild(b);
			t3 = rightChild(b);
		}
		else if (!leftY && !leftX)
		{
			a = z;
			b = y;
			c = x;
			t2 = leftChild(b);
			t3 = leftChild(c);
		}
		t1 = leftChild(a);
		t4 = rightChild(c);

		moveSubTree(z, b);
		setLeftChild(b, a);
		setLeftChild(a, t1);
		setRightChild(a, t2);
		setRightChild(b, c);
		setLeftChild(c, t3);
		setRightChild(c, t4);
	}

	private boolean isBalanced(Position<KeyElementPair<K, E>> p)
	{
		return Math.abs(height(leftChild(p)) - height(rightChild(p))) < 2;
	}

}
