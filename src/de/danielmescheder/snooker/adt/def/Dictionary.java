package de.danielmescheder.snooker.adt.def;

import de.danielmescheder.snooker.adt.impl.KeyElementPair;

/**
 * A dictionary stores key-element pairs (k,e), which will be called items,
 * where k is the key and e is the element. We can use the key as an identifier
 * that is assigned by an application or user to an associated element.
 * 
 * ( Goodrich & Tamassia "Algorithm Design" Chapter 2.5.1 )
 * 
 * @author Daniel Mescheder
 * 
 * @param <K>
 *            The type of the stored keys
 * @param <E>
 *            The type of the stored elements
 */
public interface Dictionary<K, E>
{
	/**
	 * Insert an item with element and key into the dictionary.
	 * 
	 * @param key
	 *            the key of the new item
	 * @param element
	 *            the element of the new item
	 */
	public Position<KeyElementPair<K,E>> insertItem(K key, E element);

	/**
	 * If the dictionary contains an item with the specified key, then return
	 * the element of such an item.
	 * 
	 * @param key
	 *            the key that will be searched
	 * @return the element that is associated with the given key
	 */
	public E findElement(K key);

	/**
	 * Remove an item with the given key from the dictionary and return the
	 * associated element.
	 * 
	 * @param key
	 *            the key that will be searched
	 * @return the element that is associated with the given key
	 */
	public E removeElement(K key);

}
