package de.danielmescheder.snooker.adt.def;

import de.danielmescheder.snooker.exception.InvalidPositionException;
import de.danielmescheder.snooker.exception.RankOutOfRangeException;

/**
 * The Sequence generalizes the concepts of a {@link List} and a {@link Vector}
 * It includes all the methods of the @ Vector} and the {@link List} and
 * therefore provides access to its elements using both ranks and positions.
 * This makes it a versatile data structure for a wide variety of applications.
 * 
 * It provides furthermore two "bridging" methods that connect ranks and positions.
 * 
 * ( Goodrich & Tamassia "Algorithm Design" Chapter 2.2.3 )
 * 
 * @author Daniel Mescheder
 * 
 * @param <E>
 *            the type of the elements stored in a sequence
 */
public interface Sequence<E> extends List<E>, Vector<E>
{
	/**
	 * Return the position of the element with rank r.
	 * An error occurs if the rank is not within the list.
	 * @return Position of element at r
	 * @throws RankOutOfRangeException
	 */
	public Position<E> atRank(int r) throws RankOutOfRangeException;
	
	/**
	 * Return the rank of the element at position p
	 * @param p the referenced position
	 * @return the rank of the position
	 */
	public int rankOf(Position<E> p) throws InvalidPositionException;
	
}
