/*******************************************************************************
 * Copyright (c) 2012 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bragg Institute - initial API and implementation
 ******************************************************************************/

package org.gumtree.util.bean;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Property list is an list with change notification support. This list is
 * normally used as an Java bean properties, so that when content of the list
 * changed, it's owner's listener will be notified.
 * 
 * @param <E>
 */
public class PropertyList<E> implements List<E> {

	private AbstractModelObject modelObject;

	private String propertyName;
	
	private List<E> storage;

	public PropertyList(AbstractModelObject modelObject, String propertyName) {
		this(modelObject, propertyName, new ArrayList<E>());
	}

	public PropertyList(AbstractModelObject modelObject, String propertyName, List<E> storage) {
		this.modelObject = modelObject;
		this.propertyName = propertyName;
		this.storage = storage;
	}
	
	public String getPropertyName() {
		return propertyName;
	}
	
	public void clear() {
		storage.clear();
		modelObject.firePropertyChange(propertyName, null, null);
	}

	public boolean add(E e) {
		boolean result = storage.add(e);
		modelObject.firePropertyChange(propertyName, null, e);
		return result;
	}

	public void add(int index, E element) {
		storage.add(index, element);
		modelObject.firePropertyChange(propertyName, null, element);
	}

	public boolean addAll(int index, Collection<? extends E> c) {
		boolean result = storage.addAll(index, c);
		if (result) {
			modelObject.firePropertyChange(propertyName, null, this);
		}
		return result;
	}

	public boolean addAll(Collection<? extends E> c) {
		// Optimisation
		if (storage instanceof AbstractCollection<?>) {
			return storage.addAll(c);
		} else {
			boolean result = storage.addAll(c);
			modelObject.firePropertyChange(propertyName, null, this);
			return result;
		}
	}

	public E remove(int index) {
		E result = storage.remove(index);
		modelObject.firePropertyChange(propertyName, result, null);
		return result;
	}

	public boolean removeAll(Collection<?> c) {
		// Optimisation
		if (storage instanceof AbstractCollection<?>) {
			return storage.removeAll(c);
		} else {
			boolean result = storage.removeAll(c);
			modelObject.firePropertyChange(propertyName, null, this);
			return result;
		}
	}
	
	public boolean remove(Object o) {
		boolean result = storage.remove(o);
		if (result) {
			modelObject.firePropertyChange(propertyName, o, null);
		}
		return result;
	}

	public E set(int index, E element) {
		E result = storage.set(index, element);
		modelObject.firePropertyChange(propertyName, result, element);
		return result;
	}

	public boolean contains(Object o) {
		return storage.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return storage.containsAll(c);
	}

	public E get(int index) {
		return storage.get(index);
	}

	public int indexOf(Object o) {
		return storage.indexOf(o);
	}

	public boolean isEmpty() {
		return storage.isEmpty();
	}

	public Iterator<E> iterator() {
		return storage.iterator();
	}

	public int lastIndexOf(Object o) {
		return storage.lastIndexOf(o);
	}

	public ListIterator<E> listIterator() {
		return storage.listIterator();
	}

	public ListIterator<E> listIterator(int index) {
		return storage.listIterator();
	}

	public boolean retainAll(Collection<?> c) {
		return storage.retainAll(c);
	}

	public int size() {
		return storage.size();
	}

	public List<E> subList(int fromIndex, int toIndex) {
		return storage.subList(fromIndex, toIndex);
	}

	public Object[] toArray() {
		return storage.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return storage.toArray(a);
	}
	
	// This swap method will only notify the structural change once
	// Uses this method instead of Collectiions.swap() to avoid unnecessary
	// property change calls from the set() method
    public void swap(int i, int j) {
    	storage.set(i, storage.set(j, get(i)));
    	modelObject.firePropertyChange(propertyName, null, null);
    }
    
    protected List<E> getStorage() {
    	return storage;
    }
    
}
