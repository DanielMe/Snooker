package de.danielmescheder.snooker.adt.def;

import de.danielmescheder.snooker.exception.InvalidPositionException;
import de.danielmescheder.snooker.exception.NoSuchPositionException;

/**
 * A binary tree is an ordered tree in which each node has at most two children.
 * A proper binary tree is a binary tree in which each internal node has exactly
 * two children.
 * 
 * ( Goodrich & Tamassia "Algorithm Design" Chapter 2.3.3 )
 * 
 * @author Daniel Mescheder
 * @param <E>
 */
public interface BinaryTree<E> extends Tree<E>
{
	/**
	 * Return the left child of a node p.
	 * @param p the node whose child is returned
	 * @return the requested Position object containing the left child
	 */
	Position<E> leftChild(Position<E> p) throws InvalidPositionException;
	/**
	 * Return the right child of a node p.
	 * @param p the node whose child is returned
	 * @return the requested Position object containing the right child
	 */	
	Position<E> rightChild(Position<E> p) throws InvalidPositionException;
	/**
	 * Return the sibling of a node p.
	 * @param p the node whose sibling is returned
	 * @return the requested Position object
	 */
	Position<E> sibling(Position<E> p) throws NoSuchPositionException, InvalidPositionException;
}
