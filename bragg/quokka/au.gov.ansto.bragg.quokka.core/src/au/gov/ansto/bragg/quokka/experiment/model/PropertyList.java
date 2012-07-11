package au.gov.ansto.bragg.quokka.experiment.model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Property list is an list with change notification support. This list is
 * normally used as an Java bean properties, so that when content of the list
 * changed, it's owner's listener will be notified.
 * 
 * @param <E>
 */
public class PropertyList<E> extends ArrayList<E> {

	private static final long serialVersionUID = -8001179660420902411L;

	private AbstractModelObject modelObject;

	private String propertyName;

	public PropertyList(AbstractModelObject modelObject, String propertyName) {
		this.modelObject = modelObject;
		this.propertyName = propertyName;
	}

	public void clear() {
		super.clear();
		modelObject.firePropertyChange(propertyName, null, null);
	}

	public boolean add(E e) {
		boolean result = super.add(e);
		modelObject.firePropertyChange(propertyName, null, e);
		return result;
	}

	public void add(int index, E element) {
		super.add(index, element);
		modelObject.firePropertyChange(propertyName, null, element);
	}

	public boolean addAll(int index, Collection<? extends E> c) {
		boolean result = super.addAll(index, c);
		if (result) {
			modelObject.firePropertyChange(propertyName, null, this);
		}
		return result;
	}

	public boolean addAll(Collection<? extends E> c) {
		boolean result = super.addAll(c);
		if (result) {
			modelObject.firePropertyChange(propertyName, null, this);
		}
		return result;
	}

	public E remove(int index) {
		E result = super.remove(index);
		modelObject.firePropertyChange(propertyName, result, null);
		return result;
	}

	public boolean remove(Object o) {
		boolean result = super.remove(o);
		if (result) {
			modelObject.firePropertyChange(propertyName, o, null);
		}
		return result;
	}

	public E set(int index, E element) {
		E result = super.set(index, element);
		modelObject.firePropertyChange(propertyName, result, element);
		return result;
	}
	
	// This swap method will only notify the structural change once
	// Uses this method instead of Collectiions.swap() to avoid unnecessary
	// property change calls from the set() method
    public void swap(int i, int j) {
    	super.set(i, super.set(j, get(i)));
    	modelObject.firePropertyChange(propertyName, null, null);
    }
    
	@SuppressWarnings("unchecked")
	public E[] toArray(Class<E> clazz) {
		return super.toArray((E[])Array.newInstance(clazz, size()));
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PropertyList [modelObject=");
		builder.append(modelObject);
		builder.append(", propertyName=");
		builder.append(propertyName);
		builder.append("]");
		return builder.toString();
	}
	
}
