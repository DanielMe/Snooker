package de.danielmescheder.snooker.adt.impl;

public class KeyElementPair<K, E>
{
	public KeyElementPair(K key, E element)
	{
		this.key = key;
		this.element = element;
	}
	
	public K key()
	{
		return key;
	}
	
	public E element()
	{
		return element;
	}
	
	public void setKey(K key)
	{
		this.key = key;
	}
	
	public void setElement(E element)
	{
		this.element = element;
	}
	
	@Override
	public String toString()
	{
		return key.toString()+":"+element.toString();
	}
	
	private K key;
	private E element;
}
