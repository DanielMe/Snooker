package de.danielmescheder.snooker.adt.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;

import de.danielmescheder.snooker.adt.def.List;
import de.danielmescheder.snooker.adt.def.Position;
import de.danielmescheder.snooker.adt.def.PositionContainer;
import de.danielmescheder.snooker.exception.EmptyListException;
import de.danielmescheder.snooker.exception.InvalidPositionException;
import de.danielmescheder.snooker.exception.NoNextIteratorElementException;
import de.danielmescheder.snooker.exception.PositionOutOfRangeException;


public class DoubleLinkedList<E> implements List<E>
{
	private static class ListElementIterator<E> implements Iterator<E>
	{
		private IterationDirection dir;
		private Position<E> next;
		private List<E> list;

		public enum IterationDirection
		{
			FORWARD, BACKWARD;
		}

		public ListElementIterator(List<E> list)
		{
			this(list, IterationDirection.FORWARD);
		}

		public ListElementIterator(List<E> list, IterationDirection direction)
		{
			this.dir = direction;
			this.list = list;
			if (list.isEmpty())
			{
				this.next = null;
			}
			else
			{
				this.next = (dir == IterationDirection.FORWARD) ? list.first()
						: list.last();
			}
		}

		public boolean hasNext()
		{
			return next != null;
		}

		public E next() throws NoSuchElementException
		{
			if (!hasNext())
			{
				throw new NoSuchElementException();
			}
			E ret = next.element();
			if (list.isLast(next))
			{
				next = null;
			}
			else
			{
				next = (dir == IterationDirection.FORWARD) ? list.after(next)
						: list.before(next);
			}
			return ret;
		}

		@Override
		public void remove()
		{
			// Not Implemented

		}

	}

	private static class ListPositionIterator<E> implements
			Iterator<Position<E>>
	{
		private IterationDirection dir;
		private Node<E> curr;
		private Node<E> first, last;

		public enum IterationDirection
		{
			FORWARD, BACKWARD;
		}

		public ListPositionIterator(Node<E> first, Node<E> last)
		{
			this(first, last, IterationDirection.FORWARD);
		}

		public ListPositionIterator(Node<E> first, Node<E> last,
				IterationDirection direction)
		{
			this.dir = direction;
			this.curr = null;
			this.first = first;
			this.last = last;

		}

		public boolean hasNext()
		{
			return (dir == IterationDirection.FORWARD) ? (curr != last)
					: (curr != first);
		}

		public Position<E> next() throws NoNextIteratorElementException
		{
			if (curr == null)
			{
				this.curr = (dir == IterationDirection.FORWARD) ? first : last;
			}
			else
			{
				if (hasNext())
				{
					curr = ((dir == IterationDirection.FORWARD) ? curr
							.getNext() : curr.getPrev());
				}
				else
				{
					throw new NoNextIteratorElementException();
				}
			}

			return curr;
		}

		@Override
		public void remove()
		{
			// Not Implemented

		}

	}

	private static class Node<I> implements Position<I>
	{
		private Node<I> prev;
		private Node<I> next;
		private I element;
		private PositionContainer<I> container;

		public Node(I element, PositionContainer<I> container)
		{
			this.element = element;
			this.container = container;
		}

		public Node<I> getPrev()
		{
			return prev;
		}

		public Node<I> getNext()
		{
			return next;
		}

		public void setPrev(Node<I> prev)
		{
			this.prev = prev;
		}

		public void setNext(Node<I> next)
		{
			this.next = next;
		}

		public I element()
		{
			return element;
		}

		public void setElement(I e)
		{
			this.element = e;
		}

		@Override
		public PositionContainer<I> container()
		{
			return container;
		}

		@Override
		public void invalidate()
		{
			this.container = null;
		}

	}

	/**
	 * Initializes an empty doubly linked list.
	 */
	public DoubleLinkedList()
	{
		// Header and trailer are dummy nodes that mark
		// the start and the end of the list
		header = new Node<E>(null, this);
		trailer = new Node<E>(null, this);

		// As long as the list is empty, header and trailer
		// are connected;
		header.setNext(trailer);
		trailer.setPrev(header);

		size = 0;
	}

	@Override
	public Position<E> after(Position<E> p) throws PositionOutOfRangeException,
			InvalidPositionException
	{
		if (p.container() != this)
		{
			throw new InvalidPositionException();
		}
		if (isLast(p))
		{
			throw new PositionOutOfRangeException();
		}
		return ((Node<E>) p).getNext();
	}

	@Override
	public Position<E> before(Position<E> p)
			throws PositionOutOfRangeException, InvalidPositionException
	{
		if (p.container() != this)
		{
			throw new InvalidPositionException();
		}
		if (isFirst(p))
		{
			throw new PositionOutOfRangeException();
		}
		return ((Node<E>) p).getPrev();
	}

	@Override
	public Position<E> first() throws EmptyListException
	{
		if (isEmpty())
		{
			throw new EmptyListException();
		}
		return header.getNext();
	}

	@Override
	public void insertAfter(Position<E> p, E e) throws InvalidPositionException
	{
		if (p.container() != this)
		{
			throw new InvalidPositionException();
		}
		Node<E> node = (Node<E>) p;
		Node<E> v = new Node<E>(e, this);

		v.setPrev(node);
		v.setNext(node.getNext());
		node.getNext().setPrev(v);
		node.setNext(v);

		size++;

	}

	@Override
	public void insertBefore(Position<E> p, E e)
			throws InvalidPositionException
	{
		if (p.container() != this)
		{
			throw new InvalidPositionException();
		}

		Node<E> node = (Node<E>) p;
		Node<E> v = new Node<E>(e, this);

		v.setNext(node);
		v.setPrev(node.getPrev());
		node.getPrev().setNext(v);
		node.setPrev(v);

		size++;
	}

	public void insertFirst(E e)
	{
		insertAfter(header, e);
	}

	public void insertLast(E e)
	{
		insertBefore(trailer, e);
	}

	@Override
	public boolean isEmpty()
	{
		return size() == 0;
	}

	@Override
	public boolean isFirst(Position<E> p) throws InvalidPositionException
	{
		if (p.container() != this)
		{
			throw new InvalidPositionException();
		}
		return p == first();
	}

	@Override
	public boolean isLast(Position<E> p) throws InvalidPositionException
	{
		if (p.container() != this)
		{
			throw new InvalidPositionException();
		}
		return p == last();
	}

	@Override
	public Position<E> last()
	{
		if (isEmpty())
		{
			throw new EmptyListException();
		}
		return trailer.getPrev();
	}

	@Override
	public void remove(Position<E> p) throws InvalidPositionException
	{
		if (p.container() != this)
		{
			throw new InvalidPositionException();
		}
		Node<E> node = (Node<E>) p;

		node.getPrev().setNext(node.getNext());
		node.getNext().setPrev(node.getPrev());

		node.invalidate();

		size--;
	}

	@Override
	public E replaceElement(Position<E> p, E e) throws InvalidPositionException
	{
		if (p.container() != this)
		{
			throw new InvalidPositionException();
		}
		Node<E> node = (Node<E>) p;
		E old = node.element();
		node.setElement(e);

		return old;
	}

	@Override
	public int size()
	{
		return size;
	}

	@Override
	public void swapElements(Position<E> p, Position<E> q)
			throws InvalidPositionException
	{
		if (p.container() != this || q.container() != this)
		{
			throw new InvalidPositionException();
		}

		Node<E> node1 = (Node<E>) p;
		Node<E> node2 = (Node<E>) q;

		E tmp = node1.element();
		node1.setElement(node2.element());
		node2.setElement(tmp);

	}

	@Override
	public Iterator<E> elements()
	{
		return new ListElementIterator<E>(this);
	}

	@Override
	public Iterator<Position<E>> positions()
	{
		return new ListPositionIterator<E>((Node<E>) first(), (Node<E>) last());
	}

	@Override
	public Iterator<E> iterator()
	{
		return elements();
	}

	@Override
	public String toString()
	{
		StringBuffer ret = new StringBuffer();
		for (E e : this)
		{
			ret.append("[" + e + "]");
		}
		return ret.toString();
	}

	private final Node<E> header;
	private final Node<E> trailer;
	private int size;

}
