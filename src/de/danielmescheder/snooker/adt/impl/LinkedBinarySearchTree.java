package de.danielmescheder.snooker.adt.impl;

import java.io.Serializable;

import de.danielmescheder.snooker.adt.def.Comparator;
import de.danielmescheder.snooker.adt.def.Dictionary;
import de.danielmescheder.snooker.adt.def.Position;
import de.danielmescheder.snooker.adt.def.PriorityQueue;
import de.danielmescheder.snooker.exception.InvalidPositionException;


public class LinkedBinarySearchTree<K, E> extends
		LinkedBinaryTree<KeyElementPair<K, E>> implements Serializable,
		Dictionary<K, E>, PriorityQueue<K, E>
{
	private static final long serialVersionUID = 2194544390694332007L;

	public LinkedBinarySearchTree(Comparator<K> comparator)
	{
		this.c = comparator;
	}

	public E findElement(K key)
	{
		KeyElementPair<K, E> kep = search(key, root()).element();
		if (kep == null)
		{
			return null;
		}
		return kep.element();
	}

	public Position<KeyElementPair<K, E>> insertItem(K key, E element)
	{
		return insertItemAt(new KeyElementPair<K, E>(key, element), search(key,
				root()));
	}

	public E removeElement(K key)
	{
		// first find the element that should be removed
		Position<KeyElementPair<K, E>> p = search(key, root());
		removeAt(p);
		return p.element().element();
	}

	public Position<KeyElementPair<K, E>> removeAt(
			final Position<KeyElementPair<K, E>> p)
			throws InvalidPositionException
	{
		if (p.container() != this)
		{
			throw new InvalidPositionException();
		}
		if (p == min)
		{
			min = inOrderNext(p);
		}
		if (isExternal(p))
		{
			throw new RuntimeException("removing external node");
		}
		if (isExternal(rightChild(p)))
		{
			moveSubTree(p, leftChild(p));
		}
		else if (isExternal(leftChild(p)))
		{
			moveSubTree(p, rightChild(p));
		}
		else
		{
			Position<KeyElementPair<K, E>> q = inOrderNext(p);
			swapPositions(p, q);
			moveSubTree(p, rightChild(p));
		}
		Position<KeyElementPair<K, E>> parent = null;
		if (!(p == null || isRoot(p)))
		{
			parent = parent(p);
		}
		size -= 2;
		p.invalidate();
		return parent;
	}

	public Position<KeyElementPair<K, E>> search(K key,
			Position<KeyElementPair<K, E>> node)
	{
		// Recursive

		if (isExternal(node))
		{
			return node;
		}
		if (key == node.element().key())
		{
			return node;
		}
		else if (c.isLess(key, node.element().key()))
		{
			return search(key, leftChild(node));
		}
		else
		{
			return search(key, rightChild(node));
		}
	}

	public Position<KeyElementPair<K, E>> insertItemAt(
			KeyElementPair<K, E> kep, Position<KeyElementPair<K, E>> p)
	{
		if (isExternal(p))
		{
			expand(p);
			replaceElement(p, kep);
			if (min == null || c.isLess(kep.key(), min.element().key()))
			{
				min = p;
			}
			return p;
		}
		else
		{
			return insertItemAt(kep, search(kep.key(), rightChild(p)));
		}
	}

	public Position<KeyElementPair<K, E>> min()
	{
		return min;
	}

	public E minElement()
	{
		return min().element().element();
	}

	public K minKey()
	{
		return min().element().key();
	}

	@Override
	public void remove(Position<KeyElementPair<K, E>> p)
	{
		removeAt(p);
	}

	public E removeMin()
	{
		Position<KeyElementPair<K, E>> min = min();
		removeAt(min);
		return min.element().element();
	}

	private Comparator<K> c;
	private Position<KeyElementPair<K, E>> min;
}
