package org.gumtree.gumnix.sics.ui.componentview;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.gumtree.gumnix.sics.core.SicsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.Property;
import ch.psi.sics.hipadaba.SICS;

public class VariableContentProvider implements ITreeContentProvider {

	private static Logger logger;

	private static Object[] EMPTY_ARRAY = new Object[0];

	private String selectionPropertyId;

	private String selectionPropertyValue;

	public VariableContentProvider(String selectionPropertyId, String selectionPropertyValue) {
		this.selectionPropertyId = selectionPropertyId;
		this.selectionPropertyValue = selectionPropertyValue;
	}

	public Object[] getChildren(Object parentElement) {
		List<Component> children = null;
		if(parentElement instanceof SICS) {
			children = (List<Component>)((SICS)parentElement).getComponent();
			getLogger().debug("Checking SICS");
		} else if(parentElement instanceof Component) {
			children = (List<Component>)((Component)parentElement).getComponent();
			getLogger().debug("Checking component " + ((Component)parentElement).getId());
		}
		if(children != null) {
			List<Component> buffer = new ArrayList<Component>();
			for(Component component :children) {
				if(hasSelectableComponent(component)) {
					buffer.add(component);
				}
			}
			return buffer.toArray(new Component[buffer.size()]);
		}
		return EMPTY_ARRAY;
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length != 0;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	private boolean hasSelectableComponent(Component component) {
		getLogger().debug("Checking matching component: " + component.getId());
		// if itself matches the selection criteria, then keep it
		String value = SicsUtils.getPropertyFirstValue(component, selectionPropertyId);
		if(value != null && value.equals(selectionPropertyValue)) {
			getLogger().debug("Component matched: " + component.getId());
			return true;
		}
		// otherwise see if any descendant matches the criteria....if yes....keep it
		for(Component childComponent : (List<Component>)component.getComponent()) {
			boolean hasMatchingDecendant = hasSelectableComponent(childComponent);
			if(hasMatchingDecendant) {
				return true;
			}
		}
		// if non of the component in this branch matches....get lost
		return false;
	}

	private static Logger getLogger() {
		if(logger == null) {
			logger = LoggerFactory.getLogger(VariableContentProvider.class);
		}
		return logger;
	}

}
