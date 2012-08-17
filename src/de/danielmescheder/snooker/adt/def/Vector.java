package de.danielmescheder.snooker.adt.def;

import java.util.Iterator;

import de.danielmescheder.snooker.exception.RankOutOfRangeException;


/**
 * A vector is a linear sequence that supports access to its elements by their
 * ranks. Rank is a simple yet powerful notion, since it can be used to specify
 * where to insert a new element into a vector or where to remove an old
 * element. For example, we can give the rank that a new element will have after
 * it is inserted (for example, insert at rank 2). We could also use rank to
 * specify an element to be removed (for example, remove element at rank 2).
 * 
 * (Goodrich & Tamassia "Algorithm Design" Chapter 2.2.1)
 * 
 * @author Daniel Mescheder
 * 
 * @param <E>
 *            The type of the elements stored in the Vector
 */
public interface Vector<E>
{
	
	/**
	 * Return whether the Vector is empty or not.
	 * @return true if there is no element in the vector
	 */
	public boolean isEmpty();
	
	/**
	 * Return the number of elements in the vector
	 * @return the size of the vector
	 */
	public int size();
	
	/**
	 * Return the element of the stack with rank r.
	 * An error occurs if r < 0 or r > n-1
	 * @param r the rank that is queried
	 * @return the element at rank r
	 * @throws RankOutOfRangeException
	 */
	public E elemAtRank(int r) throws RankOutOfRangeException;

	/**
	 * Replace the element at rank r with e and return the element
	 * which previously was at rank r.
	 * An error occurs if r < 0 or r > n-1
	 * 
	 * @param r the rank at which the replacement occurs.
	 * @param e the new element at rank r
	 * @return the old element at rank r
	 * @throws RankOutOfRangeException
	 */
	public E replaceAtRank(int r, E e) throws RankOutOfRangeException;

	/**
	 * Insert a new element e into the stack to have rank r.
	 * An error occurs if r < 0 or r > n.
	 * 
	 * @param r the rank at which the insertion takes place.
	 * @param e the element that is inserted.
	 * @throws RankOutOfRangeException
	 */
	public void insertAtRank(int r, E e) throws RankOutOfRangeException;

	/**
	 * Remove the element at rank r from the stack.
	 * An error occurs if r < 0 or r > n.
	 * 
	 * @param r the rank of the element that shall be removed.
	 * @throws RankOutOfRangeException
	 */
	public void removeAtRank(int r) throws RankOutOfRangeException;

	/**
	 * Return an iterator that walks through the elements of this vector.
	 * @return an iterator for the elements of this vector
	 */
	public Iterator<E> elements();

}
