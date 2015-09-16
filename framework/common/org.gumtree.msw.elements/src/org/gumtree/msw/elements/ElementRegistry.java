package org.gumtree.msw.elements;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.gumtree.msw.IModelProxy;
import org.gumtree.msw.INotificationLock;
import org.gumtree.msw.util.ModelListenerAdapter;

// used to forward ChangedProperty, AddedListElement and DeletedListElement notifications
public class ElementRegistry {
	// fields
	private final IModelProxy modelProxy;
	private final IElementVisitor registrant;
	private final Map<String, IRegisteredElement> pathToElement;
	private final Map<String, IRegisteredElementList> pathToElementList;
	// helper
	private final Map<Set<IDependencyProperty>, Map<String, IDependencyProperty>> propertyCache;

	// construction
	public ElementRegistry(IModelProxy modelProxy) {
		this.modelProxy = modelProxy;
		this.registrant = new ElementRegistrant();
		
		pathToElement = new HashMap<String, IRegisteredElement>();
		pathToElementList = new HashMap<String, IRegisteredElementList>();
		propertyCache = new HashMap<Set<IDependencyProperty>, Map<String, IDependencyProperty>>();
		
		modelProxy.addListener(new ModelContentListener(
				pathToElement,
				pathToElementList));
	}

	// properties
	public int getCount() {
		return pathToElement.size();
	}
	
	// methods
	public void register(Element element) {
		// notifications are suspended to avoid calls to ModelContentListener during registration
		try (INotificationLock lock = modelProxy.suspendNotifications()) {
			element.accept(registrant);
		}
	}
	// used by ElementRegistrant or RegisteredElementList
	// call to suspendNotifications() is either called from register(element) or
	// it is not needed (e.g. when ModelContentListener is called which locks the proxy)
	private void addElement(IRegisteredElement element) {
		String pathStr = element.getPath().toString();
		pathToElement.put(pathStr, element);	
	}
	private void addElement(IRegisteredElementList elementList) {
		ElementPath path = elementList.getPath();
		String pathStr = path.toString();
		pathToElement.put(pathStr, elementList);
		pathToElementList.put(pathStr, elementList);
		
		for (String elementName : modelProxy.getListElements(path))
			elementList.notifyAddedListElement(elementName);
	}
	private void removeElement(String pathStr) {
		IRegisteredElement registeredElement = pathToElement.remove(pathStr);
		if (registeredElement != null) {
			// in case that path points to an ElementList
			pathToElementList.remove(pathStr);
			
			registeredElement.dispose();
		}
	}
	// helper
	private Map<String, IDependencyProperty> getPropertyLookup(Set<IDependencyProperty> properties) {
		Map<String, IDependencyProperty> result = propertyCache.get(properties);
		if (result == null) {
			result = new HashMap<String, IDependencyProperty>();
			for (IDependencyProperty property : properties)
				result.put(property.getName(), property);
			
			propertyCache.put(properties, result);
		}
		return result;
	}

	// listen to content changes
	private static class ModelContentListener extends ModelListenerAdapter {
		// fields
		private final Map<String, IRegisteredElement> pathToElement;
		private final Map<String, IRegisteredElementList> pathToElementList;
		
		// construction
		public ModelContentListener(Map<String, IRegisteredElement> pathToElement, Map<String, IRegisteredElementList> pathToElementList) {
			this.pathToElement = pathToElement;
			this.pathToElementList = pathToElementList;
		}
		
		// content
		@Override
		public void onReset() {
			// TODO Auto-generated method stub
		}
		// properties
		@Override
		public void onChangedProperty(Iterable<String> elementPath, String property, Object oldValue, Object newValue) {
			IRegisteredElement element = pathToElement.get(ElementPath.toString(elementPath));
			if (element != null)
				element.notifyChangedProperty(property, oldValue, newValue);
		}
		// list elements
		@Override
		public void onAddedListElement(Iterable<String> listPath, String elementName) {
			IRegisteredElementList elementList = pathToElementList.get(ElementPath.toString(listPath));
			if (elementList != null)
				elementList.notifyAddedListElement(elementName);
		}
		@Override
		public void onDeletedListElement(Iterable<String> listPath, String elementName) {
			IRegisteredElementList elementList = pathToElementList.get(ElementPath.toString(listPath));
			if (elementList != null)
				elementList.notifyDeletedListElement(elementName);
		}
		@Override
		public void onRecoveredListElement(Iterable<String> listPath, String elementName) {
			IRegisteredElementList elementList = pathToElementList.get(ElementPath.toString(listPath));
			if (elementList != null)
				elementList.notifyAddedListElement(elementName);
		}
	}
	
	// used to register elements
	private class ElementRegistrant implements IElementVisitor {	
		// methods
		@Override
		public <TElement extends Element>
		void visit(TElement element) {
			addElement(new RegisteredElement<TElement>(
					element));		
		}
		@Override
		public <TElementList extends ElementList<TListElement>, TListElement extends Element>
		void visit(TElementList elementList) {
			addElement(new RegisteredElementList<TElementList, TListElement>(
					elementList));
		}
	}
	
	// element
	private static interface IRegisteredElement {
		// properties
		public ElementPath getPath();

		// construction
		public void dispose();
		
		// methods
		public void notifyChangedProperty(String property, Object oldValue, Object newValue);
	}
	private class RegisteredElement<TElement extends Element> implements IRegisteredElement {
		// fields
		private final Map<String, IDependencyProperty> properties;
		private final TElement element;
		
		// construction
		public RegisteredElement(TElement element) {			
			this.properties = getPropertyLookup(element.getProperties());
			this.element = element;
		}
		@Override
		public void dispose() {
			element.dispose();
		}
		
		// properties
		@Override
		public ElementPath getPath() {
			return element.getPath();
		}
		protected TElement getElement() {
			return element;
		}
		
		// methods
		@Override
		public void notifyChangedProperty(String property, Object oldValue, Object newValue) {
			IDependencyProperty p = properties.get(property);
			if ((p != null) && p.getPropertyType().isInstance(newValue)) {
				// forward notification
				element.notifyChangedProperty(p, oldValue, newValue);
			}
		}
	}
	
	// list
	private static interface IRegisteredElementList extends IRegisteredElement {
		// methods
		public void notifyAddedListElement(String elementName);
		public void notifyDeletedListElement(String elementName);
	}
	private class RegisteredElementList<TElementList extends ElementList<TListElement>, TListElement extends Element> extends RegisteredElement<TElementList> implements IRegisteredElementList {
		// fields
		private final Map<String, TListElement> listElements;
		
		// construction
		public RegisteredElementList(TElementList elementList) {
			super(elementList);
			this.listElements = new HashMap<String, TListElement>();
		}
		@Override
		public void dispose() {
			for (TListElement listElement : listElements.values())
				removeElement(listElement.getPath().toString());
			
			super.dispose();
		}

		// methods
		@Override
		public void notifyAddedListElement(String elementName) {
			if (listElements.containsKey(elementName))
				return;
			
			// register
			TElementList elementList = getElement();
			TListElement listElement = elementList.getElementFactory().create(elementName);
			if (listElement == null)
				return;
			
			listElement.accept(registrant);
			
			// insert element
			listElements.put(elementName, listElement);
			
			// forward notification
			elementList.notifyAddedListElement(listElement);
		}
		@Override
		public void notifyDeletedListElement(String elementName) {
			TListElement listElement = listElements.remove(elementName);
			if (listElement == null)
				return;

			// forward notification
			TElementList elementList = getElement();
			elementList.notifyDeletedListElement(listElement);
			
			// remove element
			removeElement(listElement.getPath().toString());
		}
	}
}
