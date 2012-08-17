package de.danielmescheder.snooker.adt.def;

import de.danielmescheder.snooker.exception.EmptyListException;
import de.danielmescheder.snooker.exception.InvalidPositionException;
import de.danielmescheder.snooker.exception.PositionOutOfRangeException;

/**
 * A list uses the concept of a {@link Position} to encapsulate the idea of
 * nodes in a list. The list allows to refer to relative positions in a list,
 * starting at the beginning or end, and to be able to move incrementally up or
 * down the list.
 * 
 * (Goodrich & Tamassia "Algorithm Design" Chapter 2.2.2)
 * 
 * @author Daniel Mescheder
 * 
 * @param <E>
 *            The type of the elements stored in the list.
 */
public interface List<E> extends PositionContainer<E>, Iterable<E>
{
	/**
	 * Return the position of the first element of the list. An error occurs if
	 * the list is empty.
	 * 
	 * @return the position of the first element.
	 * @throws EmptyListException
	 */
	public Position<E> first() throws EmptyListException;

	/**
	 * Return the position of the last element of the list. An error occurs if
	 * the list is empty.
	 * 
	 * @return the position of the last element.
	 * @throws EmptyListException
	 */
	public Position<E> last() throws EmptyListException;

	/**
	 * Return a boolean value indicating whether the given position is the first
	 * one in the list.
	 * 
	 * @param p
	 *            the position that is to be checked
	 * @return p is first = true; p is not first = false
	 */
	public boolean isFirst(Position<E> p) throws InvalidPositionException;

	/**
	 * Return a boolean value indicating whether the given position is the last
	 * in the list.
	 * 
	 * @param p
	 *            the position that is to be checked
	 * @return p is last = true; p is not last = false
	 */
	public boolean isLast(Position<E> p) throws InvalidPositionException;

	/**
	 * Return the position of the element of the list preceding the one at
	 * position p. An error occurs if p is the first position.
	 * 
	 * @param p
	 *            the referenced position
	 * @return the position of the element preceding p
	 * @throws PositionOutOfRangeException
	 */
	public Position<E> before(Position<E> p) throws PositionOutOfRangeException, InvalidPositionException;

	/**
	 * Return the position of the element of the list following the one at
	 * position p. An error occurs if p is the last position.
	 * 
	 * @param p
	 *            the referenced position
	 * @return the position of the element following p
	 * @throws PositionOutOfRangeException
	 */
	public Position<E> after(Position<E> p) throws PositionOutOfRangeException, InvalidPositionException;

	/**
	 * Insert a new element e into the list as the first element
	 * @param e the new element
	 */
	public void insertFirst(E e);

	/**
	 * Insert a new element e into the list as the last element
	 * @param e the new element
	 */
	public void insertLast(E e);

	/**
	 * Insert a new element e into the list before position p.
	 * @param p the referenced position
	 * @param e the new element
	 */
	public void insertBefore(Position<E> p, E e) throws InvalidPositionException;

	/**
	 * Insert a new element e into the list after position p.
	 * @param p the referenced position
	 * @param e the new element
	 */
	public void insertAfter(Position<E> p, E e) throws InvalidPositionException;
}
