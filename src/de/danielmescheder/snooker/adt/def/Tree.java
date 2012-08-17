package de.danielmescheder.snooker.adt.def;

import java.util.Iterator;

import de.danielmescheder.snooker.exception.InvalidPositionException;
import de.danielmescheder.snooker.exception.NoSuchPositionException;


/**
 * A tree is an abstract data type that stores elements hierachically. With the
 * exception of the top element, each element in a tree has a parent element and
 * zero or more children elements.
 * 
 * @author Daniel Mescheder
 * @param E
 *            the type of the elements stored in the tree
 * 
 */
public interface Tree<E> extends PositionContainer<E>
{

	/**
	 * Return the root of the tree
	 * 
	 * @return a position object that has no parents
	 */
	public Position<E> root();

	/**
	 * Return the parent of node p. An error occurs if p is the root.
	 * 
	 * @return a position object holding the parent p
	 * @param p
	 *            the node whose parent is returned
	 * @throws NoSuchPositionException
	 */
	public Position<E> parent(Position<E> p) throws NoSuchPositionException, InvalidPositionException;

	/**
	 * Return an iterator of the children of node p
	 * 
	 * @param p
	 *            the Position object whose children are returned
	 * @return an iterator of Positions
	 */
	public Iterator<Position<E>> children(Position<E> p) throws InvalidPositionException;

	/**
	 * Test whether node p is external. P is external if it has no children
	 * (a.k.a. leaf node).
	 * 
	 * @param p the position which is checked
	 * @return true if p is external, false otherwise.
	 */
	public boolean isExternal(Position<E> p) throws InvalidPositionException;

	/**
	 * Test whether node p is internal. P is internal if it has children.
	 * 
	 * @param p the position which is checked
	 * @return true if p is internal, false otherwise.
	 */
	public boolean isInternal(Position<E> p) throws InvalidPositionException;
	/**
	 * Test whether node p is the root-node. P is the root node if it has no parent.
	 * 
	 * @param p the position which is checked
	 * @return true if p is internal, false otherwise.
	 */
	public boolean isRoot(Position<E> p) throws InvalidPositionException;

}
