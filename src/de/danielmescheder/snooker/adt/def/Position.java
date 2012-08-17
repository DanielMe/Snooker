package de.danielmescheder.snooker.adt.def;

/**
 * The concept of a position is used to abstract and unify the different ways of
 * storing elements in various implementations of a list. This formalizes the
 * intuitive notion of "place" of an element relative to others in the list.
 * 
 * ( Goodrich & Tamassia "Algorithm Design" Chapter 2.2.2 )
 * 
 * @author Daniel Mescheder
 * 
 * @param <E> the Type of the element stored at/in this position
 */
public interface Position<E>
{
	public E element();
	public PositionContainer<E> container();
	public void invalidate();
}
