package de.danielmescheder.snooker.adt.def;

import java.util.Iterator;

import de.danielmescheder.snooker.exception.InvalidPositionException;


public interface PositionContainer<E>
{
	/**
	 * Return the number of nodes in the tree
	 * 
	 * @return the number of positions
	 */
	public int size();

	/**
	 * Determines whether the tree is empty or not
	 * 
	 * @return whether the tree is empty
	 */
	public boolean isEmpty();

	/**
	 * Return an iterator of all the elements stored at nodes of the tree
	 * 
	 * @return an Iterator for the elements
	 */
	public Iterator<E> elements();

	/**
	 * Return an iterator of all the nodes of the tree
	 * 
	 * @return an iterator of the nodes of the tree
	 */
	public Iterator<Position<E>> positions();
	
	/**
	 * Swap the elements stored at the nodes p and q.
	 * @param p first position object involved
	 * @param q second position object involved
	 */
	public void swapElements(Position<E> p, Position<E> q) throws InvalidPositionException;
	/**
	 * Replaces the element in p by e and returns the element
	 * previously stored in p.
	 * @param p The position object whose element will be replaced
	 * @param e The new element that is stored.
	 */
	public E replaceElement(Position<E> p, E e) throws InvalidPositionException;
	
	/**
	 * Remove the element at position p from the list.
	 * @param p the referenced position.
	 */
	public void remove(Position<E> p) throws InvalidPositionException;
}
