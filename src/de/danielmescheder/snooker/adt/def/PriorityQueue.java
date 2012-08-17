package de.danielmescheder.snooker.adt.def;

import de.danielmescheder.snooker.adt.impl.KeyElementPair;

/**
 * A priority queue is a container of elements, each having an associated key
 * that is provided at the time the element is inserted. The name
 * "priority queue" comes from the fact that keys determine the "priority" used
 * to pick elements to be removed.
 * 
 *  ( Goodrich & Tamassia "Algorithm Design" Chapter 2.4.1 )
 * 
 * @author Daniel Mescheder
 * 
 */
public interface PriorityQueue<K, E>
{
	/**
	 * Insert an element with key into the queue.
	 * 
	 * @param key
	 *            the key that is used for the comparison of the elements
	 * @param element
	 *            the item that is stored
	 */
	public Position<KeyElementPair<K, E>> insertItem(K key, E element);

	/**
	 * Returns and removes the element with the smallest key.
	 * 
	 * @return the element with the smallest key
	 */
	public E removeMin();

	/**
	 * Gives the number of elements stored in the queue
	 * 
	 * @return the size of the queue
	 */
	public int size();

	/**
	 * Returns whether the queue is empty.
	 * 
	 * @return true if there are zero elements.
	 */
	public boolean isEmpty();

	/**
	 * Returns the element with the smallest key without removing it.
	 * 
	 * @return The element with the smallest key.
	 */
	public E minElement();
	
	/**
	 * Returns the smallest key in the list.
	 * 
	 * @return the smallest key.
	 */
	public K minKey();
}
