package org.gumtree.msw.elements;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.gumtree.msw.IModel;
import org.gumtree.msw.IModelProxy;
import org.gumtree.msw.IRefIdProvider;
import org.gumtree.msw.RefId;
import org.gumtree.msw.commands.ChangePropertyCommand;
import org.gumtree.msw.commands.Command;
import org.gumtree.msw.commands.DeleteListElementCommand;
import org.gumtree.msw.commands.DuplicateListElementCommand;

public abstract class Element {
	// property names
	public static final DependencyProperty<Element, Integer> INDEX = new DependencyProperty<>(IModel.INDEX, Integer.class);
	public static final Comparator<Element> INDEX_COMPARATOR = new ElementIndexComparator();
	
	// fields
	private ElementPath path;
	private IModelProxy modelProxy;
	private IRefIdProvider idProvider;
	private final List<IElementPropertyListener> propertyListeners = new ArrayList<>();
	
	// construction
	protected Element(IModelProxy modelProxy, String name) {
		this(
				modelProxy,
				new ElementPath(name));
	}
	protected Element(Element parent, String name) {
		this(
				parent.getModelProxy(),
				new ElementPath(parent.getPath(), name));
	}
	private Element(IModelProxy modelProxy, ElementPath path) {
		this.path = path;
		this.modelProxy = modelProxy;
		this.idProvider = modelProxy.getIdProvider();
	}
	void dispose() {
		propertyListeners.clear();
		
		idProvider = null;
		modelProxy = null;
		path = null;
	}
	
	// properties
	public boolean isValid() {
		return modelProxy != null;
	}
	public ElementPath getPath() {
		return path;
	}
	public IModelProxy getModelProxy() {
		return modelProxy;
	}
	public int getIndex() {
		return (int)get(INDEX);
	}
	public void setIndex(int value) {
		set(INDEX, value);
	}
	public abstract Set<IDependencyProperty> getProperties();
	// getter/setter
	public Object get(IDependencyProperty property) {
		if (!getProperties().contains(property))
			return null;
		
		return modelProxy.getProperty(
				path,
				property.getName());
	}
	public boolean set(IDependencyProperty property, Object newValue) {
		if (!getProperties().contains(property))
			return false; // not applicable
		
		modelProxy.command(new ChangePropertyCommand(
				idProvider.nextId(),
				path,
				property.getName(),
				newValue));
		
		return true;
	}
	
	//  methods
	public void accept(IElementVisitor visitor) {
		visitor.visit(this);
	}
	protected void duplicate() {
		command(new DuplicateListElementCommand(
				nextId(),
				getPath().getRoot(),
				getPath().getElementName(),
				getClass().getSimpleName() + nextId().toString()));
	}
	protected void delete() {
		command(new DeleteListElementCommand(
				nextId(),
				getPath().getRoot(),
				getPath().getElementName()));
	}
	// to string
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append('(').append(getPath()).append(')');
		appendElements(sb);
		return sb.toString();
	}
	protected void appendElements(StringBuilder sb) {
		Set<IDependencyProperty> properties = getProperties();
		if ((properties == null) || properties.isEmpty())
			sb.append("[]");
		else  {
			sb.append('[');
			for (IDependencyProperty property : properties) {
				sb.append(property.getName()).append('=');
				
				Object propertyValue = get(property);
				if (propertyValue instanceof String)
					sb.append('"').append(propertyValue.toString().replaceAll("\"", "\\\"")).append('"');
				else
					sb.append(propertyValue);
				
				sb.append(';');
			}
			sb.setCharAt(sb.length() - 1, ']');
		}
	}
	// listeners
	public void addPropertyListener(IElementPropertyListener listener) {
		if (propertyListeners.contains(listener))
			throw new Error("listener already exists");
		
		propertyListeners.add(listener);
	}
	public boolean removePropertyListener(IElementPropertyListener listener) {
		return propertyListeners.remove(listener);
	}
	// internal
	void notifyChangedProperty(IDependencyProperty property, Object oldValue, Object newValue) {
		for (IElementPropertyListener listener : propertyListeners)
			listener.onChangedProperty(property, oldValue, newValue);
	}
	
	// helper
	protected RefId nextId() {
		return idProvider.nextId();
	}
	protected void command(Command cmd) {
		modelProxy.command(cmd);
	}

	// comparator for element indices
    private static class ElementIndexComparator implements Comparator<Element> {
    	// methods
		@Override
		public int compare(Element element1, Element element2) {
			return Integer.compare(element1.getIndex(), element2.getIndex());
		}
    }
}
