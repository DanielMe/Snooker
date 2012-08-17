package de.danielmescheder.snooker.adt.impl;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.danielmescheder.snooker.adt.def.BinaryTree;
import de.danielmescheder.snooker.adt.def.Position;
import de.danielmescheder.snooker.adt.def.PositionContainer;
import de.danielmescheder.snooker.exception.InvalidPositionException;
import de.danielmescheder.snooker.exception.NoExternalNodeException;
import de.danielmescheder.snooker.exception.NoSuchPositionException;


public class LinkedBinaryTree<E> implements BinaryTree<E>, Serializable,
		Iterable<E>
{
	private static final long serialVersionUID = 4148021909137313272L;

	private static class LinkedTreeNode<I> implements Position<I>, Serializable
	{
		private static final long serialVersionUID = -4945057687505741406L;

		public LinkedTreeNode(PositionContainer<I> container)
		{
			this(null, null, container);
		}

		public LinkedTreeNode(LinkedTreeNode<I> parent,
				PositionContainer<I> container)
		{
			this(null, parent, container);
		}

		public LinkedTreeNode(I element, LinkedTreeNode<I> parent,
				PositionContainer<I> container)
		{
			this.element = element;
			this.parent = parent;
			this.container = container;
			this.height = 0;
		}

		public void setLeftChild(LinkedTreeNode<I> node)
		{
			leftChild = node;
		}

		public LinkedTreeNode<I> leftChild()
		{
			return leftChild;
		}

		public void setRightChild(LinkedTreeNode<I> node)
		{
			rightChild = node;
		}

		public void setParent(LinkedTreeNode<I> node)
		{
			parent = node;
		}

		public void setElement(I element)
		{
			this.element = element;
		}

		public LinkedTreeNode<I> rightChild()
		{
			return rightChild;
		}

		public I element()
		{
			return element;
		}

		public LinkedTreeNode<I> parent()
		{
			return parent;
		}

		public String toString(int lvl)
		{

			String ret = "";
			for(int i=0;i<lvl;i++)
			{
				ret+="\t";
			}
			if (element != null)
			{
				ret += "[h:" + height + "|" + element.toString() + "]\n";
			}
			else
			{
				ret += "[h:" + height + "|" + "NULL]\n";
			}
			if (leftChild != null)
			{
				ret += leftChild.toString(lvl+1);
			}
			if (rightChild != null)
			{
				ret += rightChild.toString(lvl+1);
			}
			return ret;
		}
		

		public int height()
		{
			return height;
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

		private LinkedTreeNode<I> parent;
		private LinkedTreeNode<I> leftChild;
		private LinkedTreeNode<I> rightChild;
		private int height;
		private I element;
		private PositionContainer<I> container;

	}

	private static class ChildrenIterator<I> implements Iterator<Position<I>>
	{

		public ChildrenIterator(LinkedTreeNode<I> parent)
		{
			leftChild = parent.leftChild();
			rightChild = parent.rightChild();
		}

		public boolean hasNext()
		{
			return leftChild != null || rightChild != null;
		}

		public Position<I> next() throws NoSuchElementException
		{
			LinkedTreeNode<I> ret;
			if (leftChild != null)
			{
				ret = leftChild;
				leftChild = null;
			}
			else if (rightChild != null)
			{
				ret = rightChild;
				rightChild = null;
			}
			else
			{
				throw new NoSuchElementException("No next element in iterator.");
			}
			return ret;
		}

		public void remove()
		{
			// ...?

		}

		private LinkedTreeNode<I> leftChild, rightChild;

	}

	private static class PositionIterator<I> implements Iterator<Position<I>>
	{
		public PositionIterator(BinaryTree<I> t)
		{
			hasNext = true;
			tree = t;
			next = t.root();
			tour = new EulerTour<I>()
			{

				@Override
				public void visitBelow(Position<I> p)
				{
				}

				@Override
				public void visitLeft(Position<I> p)
				{
					if (p != next)
					{
						stop();
					}
				}

				@Override
				public void visitRight(Position<I> p)
				{
					if (tree.isRoot(p))
					{
						hasNext = false;
					}
				}
			};
		}

		public boolean hasNext()
		{
			return hasNext;
		}

		public Position<I> next() throws NoSuchElementException
		{
			if (!hasNext())
			{
				throw new NoSuchElementException();
			}
			Position<I> ret = next;

			tour.start();
			tour.tourLeft(tree, next);
			next = tour.position();
			return ret;
		}

		public void remove()
		{
			// ...?
		}

		private Position<I> next;
		private EulerTour<I> tour;
		private BinaryTree<I> tree;
		private boolean hasNext;

	}

	private static class ElementIterator<I> implements Iterator<I>
	{
		public ElementIterator(BinaryTree<I> tree)
		{
			pit = new PositionIterator<I>(tree);
		}

		public boolean hasNext()
		{
			return pit.hasNext();
		}

		public I next() throws NoSuchElementException
		{
			return pit.next().element();
		}

		public void remove()
		{
			// ...?
		}

		private PositionIterator<I> pit;

	}

	public LinkedBinaryTree()
	{
		this.root = new LinkedTreeNode<E>(this);
		size = 1;
	}

	public void expand(Position<E> externalNode)
			throws NoExternalNodeException, InvalidPositionException
	{
		if (externalNode.container() != this)
		{
			throw new InvalidPositionException();
		}
		if (!isExternal(externalNode))
		{
			throw new NoExternalNodeException();
		}
		LinkedTreeNode<E> n = (LinkedTreeNode<E>) externalNode;
		n.setLeftChild(new LinkedTreeNode<E>(null, n, this));
		n.setRightChild(new LinkedTreeNode<E>(null, n, this));

		size += 2;
		updateHeight(n);

	}

	public Iterator<Position<E>> children(Position<E> p)
			throws InvalidPositionException
	{
		if (p.container() != this)
		{
			throw new InvalidPositionException();
		}
		return new ChildrenIterator<E>((LinkedTreeNode<E>) p);
	}

	public Iterator<E> elements()
	{
		return new ElementIterator<E>(this);
	}

	public boolean isEmpty()
	{
		return size() == 0;
	}

	public boolean isExternal(Position<E> p) throws InvalidPositionException
	{
		if (p.container() != this)
		{
			throw new InvalidPositionException();
		}
		LinkedTreeNode<E> n = (LinkedTreeNode<E>) p;
		return n.leftChild() == null && n.rightChild() == null;
	}

	public boolean isInternal(Position<E> p) throws InvalidPositionException
	{
		if (p.container() != this)
		{
			throw new InvalidPositionException();
		}
		LinkedTreeNode<E> n = (LinkedTreeNode<E>) p;
		return n.leftChild() != null || n.rightChild() != null;
	}

	public boolean isRoot(Position<E> p) throws InvalidPositionException
	{
		if (p.container() != this)
		{
			throw new InvalidPositionException();
		}
		return p == root();
	}

	public Position<E> parent(Position<E> p) throws NoSuchPositionException,
			InvalidPositionException
	{
		if (p.container() != this)
		{
			throw new InvalidPositionException();
		}
		if (isRoot(p))
		{
			throw new NoSuchPositionException();
		}
		LinkedTreeNode<E> n = (LinkedTreeNode<E>) p;
		return n.parent;
	}

	public Iterator<Position<E>> positions()
	{
		return new PositionIterator<E>(this);
	}

	public E replaceElement(Position<E> p, E e) throws InvalidPositionException
	{
		if (p.container() != this)
		{
			throw new InvalidPositionException();
		}
		LinkedTreeNode<E> n = (LinkedTreeNode<E>) p;
		E ret = n.element();
		n.setElement(e);
		return ret;
	}

	public Position<E> root()
	{
		return root;
	}

	public int size()
	{
		return size;
	}

	public void swapElements(Position<E> p, Position<E> q)
			throws InvalidPositionException
	{
		if (p.container() != this || q.container() != this)
		{
			throw new InvalidPositionException();
		}
		LinkedTreeNode<E> n1 = (LinkedTreeNode<E>) p;
		LinkedTreeNode<E> n2 = (LinkedTreeNode<E>) q;

		E tmp = n1.element();
		n1.setElement(n2.element());
		n2.setElement(tmp);

	}

	public void swapPositions(Position<E> p1, Position<E> p2)
			throws InvalidPositionException
	{
		if (p1.container() != this || p2.container() != this)
		{
			throw new InvalidPositionException();
		}
		if (isRoot(p1) && isRoot(p2))
		{
			// swap the root with itself? Nonsense!
			return;
		}
		else if (!isRoot(p1) && isRoot(p2))
		{
			swapPositions(p2, p1);
			return;
		}

		// now p1 might be root or might be not, p2 is not root

		LinkedTreeNode<E> node1 = (LinkedTreeNode<E>) p1;
		LinkedTreeNode<E> node2 = (LinkedTreeNode<E>) p2;

		LinkedTreeNode<E> parent1 = node1.parent();
		LinkedTreeNode<E> parent2 = node2.parent();

		LinkedTreeNode<E> lc1 = node1.leftChild();
		LinkedTreeNode<E> lc2 = node2.leftChild();

		LinkedTreeNode<E> rc1 = node1.rightChild();
		LinkedTreeNode<E> rc2 = node2.rightChild();

		if (lc1 != node2)
		{
			if (lc1!=null)
			{
				lc1.parent = node2;
			}
			node2.leftChild = lc1;
		}
		else
		{
			node2.leftChild = node1;
			node1.parent = node2;
		}

		if (rc1 != node2)
		{
			if (rc1!=null)
			{
				rc1.parent = node2;
			}
			node2.rightChild = rc1;
		}
		else
		{
			node2.rightChild = node1;
			node1.parent = node2;
		}

		if (lc2 != node1)
		{
			if (lc2!=null)
			{
				lc2.parent = node1;
			}
			node1.leftChild = lc2;
		}
		else
		{
		node1.leftChild = node2;
		node2.parent = node1;
		}
		
		if (rc2 != node1)
		{
			if (rc2!=null)
			{
				rc2.parent = node1;
			}
			node1.rightChild = rc2;
		}
		else
		{
		node1.rightChild = node2;
		node2.parent = node1;
		}
		
		if (parent1 != node2)
		{
			if (!isRoot(node1))
			{
				if(parent1.leftChild==node1)
				{
					parent1.leftChild=node2;
				}
				else
				{
					parent1.rightChild=node2;
				}
			}
			else
			{
				root = node2;
			}
			node2.parent = parent1;
		}
		
		if (parent2 != node1)
		{
			if(parent2.leftChild==node2)
			{
				parent2.leftChild=node1;
			}
			else
			{
				parent2.rightChild=node1;
			}
			node1.parent=parent2;
		}
		
		int tmp = node1.height;
		node1.height=node2.height;
		node2.height=tmp;
	}

	public Position<E> leftChild(Position<E> p) throws NoSuchPositionException,
			InvalidPositionException
	{
		if (p.container() != this)
		{
			throw new InvalidPositionException();
		}
		if (((LinkedTreeNode<E>) p).leftChild() == null)
		{
			throw new NoSuchPositionException();
		}
		return ((LinkedTreeNode<E>) p).leftChild();
	}

	public Position<E> rightChild(Position<E> p)
			throws InvalidPositionException, NoSuchPositionException
	{
		if (p.container() != this)
		{
			throw new InvalidPositionException();
		}
		if (((LinkedTreeNode<E>) p).rightChild() == null)
		{
			throw new NoSuchPositionException();
		}
		return ((LinkedTreeNode<E>) p).rightChild();
	}

	public Position<E> sibling(Position<E> p) throws NoSuchPositionException,
			InvalidPositionException
	{
		if (p.container() != this)
		{
			throw new InvalidPositionException();
		}
		if (isRoot(p))
		{
			throw new NoSuchPositionException();
		}
		LinkedTreeNode<E> n = (LinkedTreeNode<E>) p;
		if (n.parent.leftChild() == n)
		{
			n = n.parent.rightChild();
		}
		else
		{
			n = n.parent.leftChild();
		}
		return n;
	}

	@Override
	public String toString()
	{
		return root.toString(0);
	}

	protected void setRightChild(Position<E> p, Position<E> q) throws InvalidPositionException
	{
		if(p.container()!=this)
		{
			throw new InvalidPositionException();
		}
		if (descendantOf(p, q))
		{
			throw new InvalidPositionException();
		}
		LinkedTreeNode<E> node1 = (LinkedTreeNode<E>) p;
		LinkedTreeNode<E> node2 = (q==null)?null:(LinkedTreeNode<E>) q;

		node1.setRightChild(node2);
		if (node2 != null)
		{
			node2.setParent(node1);
		}
		updateHeight(node1);

	}

	protected void setLeftChild(Position<E> p, Position<E> q)
	{
		if(p.container()!=this)
		{
			throw new InvalidPositionException();
		}
		if (descendantOf(p, q))
		{
			throw new InvalidPositionException();
		}
		LinkedTreeNode<E> node1 = (LinkedTreeNode<E>) p;
		LinkedTreeNode<E> node2 = (q==null)?null:(LinkedTreeNode<E>) q;

		node1.setLeftChild(node2);
		if (node2 != null)
		{
			node2.setParent(node1);
		}
		updateHeight(node1);
	}

	public void moveSubTree(Position<E> newLoc, Position<E> subT)
			throws InvalidPositionException
	{
		if (newLoc.container() != this || subT.container() != this)
		{
			throw new InvalidPositionException();
		}

		if (leftChild(parent(subT)) == subT)
		{
			setLeftChild(parent(subT), new LinkedTreeNode<E>(
					(LinkedTreeNode<E>) parent(subT), this));
		}
		else
		{
			setRightChild(parent(subT), new LinkedTreeNode<E>(
					(LinkedTreeNode<E>) parent(subT), this));
		}

		if (!isRoot(newLoc))
		{
			if (leftChild(parent(newLoc)) == newLoc)
			{
				setLeftChild(parent(newLoc), subT);
			}
			else
			{
				setRightChild(parent(newLoc), subT);
			}
		}
		else
		{
			root = (LinkedTreeNode<E>) subT;
			((LinkedTreeNode<E>)subT).setParent(null);
		}
	}

	public int height(Position<E> p) throws InvalidPositionException
	{
		if (p.container() != this)
		{
			throw new InvalidPositionException();
		}
		return ((LinkedTreeNode<E>) p).height();
	}

	public void updateHeight(Position<E> p) throws InvalidPositionException
	{
		if (p.container() != this)
		{
			throw new InvalidPositionException();
		}
		LinkedTreeNode<E> node = (LinkedTreeNode<E>) p;

		while (!isRoot(node))
		{
			if (isExternal(node))
			{
				node.height = 0;
			}
			else
			{
				node.height = ((LinkedTreeNode<E>) higherChild(node)).height + 1;
			}
			node = (LinkedTreeNode<E>) parent(node);
		}
		if (isExternal(node))
		{
			node.height = 0;
		}
		else
		{
			node.height = ((LinkedTreeNode<E>) higherChild(node)).height + 1;
		}
	}

	public Position<E> higherChild(Position<E> p)
			throws InvalidPositionException, NoSuchPositionException
	{
		if (p.container() != this)
		{
			throw new InvalidPositionException();
		}
		if (isExternal(p))
		{
			throw new NoSuchPositionException();
		}
		if (height(leftChild(p)) > height(rightChild(p)))
		{
			return leftChild(p);
		}
		else
		{
			return rightChild(p);
		}
	}

	public Iterator<E> iterator()
	{
		return elements();
	}

	@Override
	public void remove(Position<E> p) throws InvalidPositionException
	{
		if (p.container() != this)
		{
			throw new InvalidPositionException();
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
			Position<E> q = inOrderNext(p);
			swapPositions(p, q);
			moveSubTree(q, rightChild(q));
		}

		size -= 2;
		p.invalidate();
	}

	protected Position<E> inOrderNext(final Position<E> p)
			throws InvalidPositionException
	{
		if (p.container() != this)
		{
			throw new InvalidPositionException();
		}
		EulerTour<E> e = new EulerTour<E>()
		{

			@Override
			public void visitBelow(Position<E> q)
			{
				if (p != q && isInternal(q))
				{
					stop();
				}
			}

			@Override
			public void visitLeft(Position<E> q)
			{
			}

			@Override
			public void visitRight(Position<E> q)
			{
			}
		};
		e.start();
		e.tourBelow(this, p);
		return e.position();
	}
	
	/**
	 * Checks if p is a descendant of q. 
	 */
	public boolean descendantOf(Position<E> p, Position<E> q)
	{
		if (p == null || q == null)
		{
			return false;
		}
		
		Position<E> qLeftChild = ((LinkedTreeNode<E>) q).leftChild;
		Position<E> qRightChild = ((LinkedTreeNode<E>) q).rightChild;
		
		return ((p == qLeftChild || descendantOf(p, qLeftChild))
				|| (p == qRightChild || descendantOf(p, qRightChild)));
	}
	
	public boolean isValid()
	{
		return subTreeValid(root);
	}
	
	public boolean subTreeValid(Position<E> p)
	{
		LinkedTreeNode<E> pn = (LinkedTreeNode<E>) p;
		if (pn.container != this)
		{
			return false;
		}
		if (pn.leftChild != null && pn.rightChild != null)
		{
			if (descendantOf(p, p))
			{
				return false;
			}
			if (pn.leftChild.parent != pn || pn.rightChild.parent != pn)
			{
				return false;
			}
			return (subTreeValid(pn.leftChild) && subTreeValid(pn.rightChild));
		} else
		{
			return true;
		}
	}

	private LinkedTreeNode<E> root;
	protected int size;

}
