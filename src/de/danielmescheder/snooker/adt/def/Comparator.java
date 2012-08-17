package de.danielmescheder.snooker.adt.def;

/**
 * A comparator provides methods to compare objects. This is more flexible than
 * relying on comparison rules provided by the objects itself. 
 * ( Goodrich & Tamassia "Algorithm Design" Chapter 2.4.1 )
 * 
 * @author Daniel Mescheder
 * 
 * @param <E> the type of the compared objects
 */
public interface Comparator<E>
{
	/**
	 * True if and only if a is less than b
	 * @param a first argument
	 * @param b second argment
	 * @return a < b
	 */
	public boolean isLess(E a, E b);
	
	/**
	 * True if and only if a is less than or equal to b
	 * @param a first argument
	 * @param b second argment
	 * @return a <= b
	 */
	public boolean isLessOrEqual(E a, E b);
	
	/**
	 * True if and only if a is equal to b
	 * @param a first argument
	 * @param b second argment
	 * @return a = b
	 */
	public boolean isEqual(E a, E b);
	
	/**
	 * True if and only if a is greater than or equal to b
	 * @param a first argument
	 * @param b second argment
	 * @return a >= b
	 */
	public boolean isGreaterOrEqual(E a, E b);
	
	/**
	 * True if and only if a is greater than b
	 * @param a first argument
	 * @param b second argment
	 * @return a > b
	 */
	public boolean isGreater(E a, E b);
	
	/**
	 * True if a can be compared
	 * @param a the object that is checked
	 * @return can a be compared?
	 */
	public boolean isComparable(E a);
}
