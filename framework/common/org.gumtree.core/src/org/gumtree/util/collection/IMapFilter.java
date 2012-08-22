package org.gumtree.util.collection;

public interface IMapFilter<K,V> {

	boolean accept(K key, V value);
	
}
