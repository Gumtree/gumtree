package org.gumtree.msw.elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gumtree.msw.ICommand;
import org.gumtree.msw.IModelProxy;
import org.gumtree.msw.INotificationLock;
import org.gumtree.msw.RefId;
import org.gumtree.msw.commands.AddListElementCommand;
import org.gumtree.msw.commands.BatchCommand;
import org.gumtree.msw.commands.ChangePropertyCommand;
import org.gumtree.msw.commands.ClearElementListCommand;
import org.gumtree.msw.commands.Command;

public abstract class ElementList<TListElement extends Element> extends Element {
	// fields
	private final Set<TListElement> listElements = new HashSet<TListElement>();	// used to avoid dependency on ElementRegistry
	private final List<IElementListListener<? super TListElement>> listListeners = new ArrayList<>();
	
	// construction
	protected ElementList(IModelProxy modelProxy, String name) {
		super(modelProxy, name);
	}
	protected ElementList(Element parent, String name) {
		super(parent, name);
	}
	@Override
	void dispose() {
		listElements.clear();
		listListeners.clear();
		super.dispose();
	}
	
	// methods
	@Override
	public void accept(IElementVisitor visitor) {
		visitor.visit(this);
	}
	public abstract IListElementFactory<TListElement> getElementFactory();
	public void fetchElements(Collection<? super TListElement> elements) {
		try (INotificationLock lock = getModelProxy().suspendNotifications()) {
			elements.clear();
			elements.addAll(listElements);
		}
	}
	// clear
	protected void clear() {
		command(new ClearElementListCommand(
				nextId(),
				getPath()));
	}
	// add
	protected <TElement extends TListElement>
	void add(Class<TElement> elementType) {
		add(elementType, Integer.MAX_VALUE);
	}
	protected <TElement extends TListElement>
	void add(Class<TElement> elementType, int targetIndex) {
		command(new AddListElementCommand(
				nextId(),
				getPath(),
				elementType.getSimpleName() + nextId().toString(),
				targetIndex));
	}
	// replace all
	protected <TElement extends TListElement>
	void replaceAll(Class<TElement> elementType, Iterable<Map<IDependencyProperty, Object>> content) {
		RefId id = nextId();
		ElementPath path = getPath();
		
		List<Command> commands = new ArrayList<>();
		commands.add(new ClearElementListCommand(id, path));

		int index = 0;
		String className = elementType.getSimpleName();
		for (Map<IDependencyProperty, Object> elementInfo : content) {
			String elementName = className + nextId().toString();
			ElementPath elementPath = new ElementPath(path, elementName);
			
			commands.add(new AddListElementCommand(
					id,
					path,
					elementName,
					index++));
			
			for (Map.Entry<IDependencyProperty, Object> entry : elementInfo.entrySet())
				commands.add(new ChangePropertyCommand(
						id,
						elementPath,
						entry.getKey().getName(),
						entry.getValue()));
		}
		
		command(new BatchCommand(id, commands.toArray(new ICommand[commands.size()])));
	}
	// batch process
	protected boolean batchSet(IDependencyProperty property, Object newValue) {
		RefId id = nextId();
		List<Command> commands = new ArrayList<>();
		boolean applicable = false;
		
		try (INotificationLock lock = getModelProxy().suspendNotifications()) {
			for (TListElement element : listElements)
				if (element.getProperties().contains(property)) {
					applicable = true;
					commands.add(new ChangePropertyCommand(
							id,
							element.getPath(),
							property.getName(),
							newValue));
				}
		}
		
		if (!commands.isEmpty())
			command(new BatchCommand(id, commands.toArray(new ICommand[commands.size()])));
		
		return applicable;
	}
	// to string
	@Override
	protected void appendElements(StringBuilder sb) {
		super.appendElements(sb);
		
		List<TListElement> elements = new ArrayList<TListElement>();
		fetchElements(elements);

		if (elements.isEmpty())
			sb.append("{}");
		else {
			Collections.sort(elements, INDEX_COMPARATOR);
			
			sb.append('{');
			for (Element element : elements)
				sb.append(element).append(';');
			sb.setCharAt(sb.length() - 1, '}');
		}
	}
	
	// listeners	
	public void addListListener(IElementListListener<? super TListElement> listener) {
		try (INotificationLock lock = getModelProxy().suspendNotifications()) {
			if (listListeners.contains(listener))
				throw new Error("listener already exists");
			
			listListeners.add(listener);
			for (TListElement element : listElements)
				listener.onAddedListElement(element);
		}
	}
	public boolean removeListListener(IElementListListener<? super TListElement> listener) {
		return listListeners.remove(listener);
	}
	// internal
	void notifyAddedListElement(TListElement element) {
		if (listElements.add(element))
			for (IElementListListener<? super TListElement> listener : listListeners)
				listener.onAddedListElement(element);
	}
	void notifyDeletedListElement(TListElement element) {
		if (listElements.remove(element))
			for (IElementListListener<? super TListElement> listener : listListeners)
				listener.onDeletedListElement(element);
	}
}
