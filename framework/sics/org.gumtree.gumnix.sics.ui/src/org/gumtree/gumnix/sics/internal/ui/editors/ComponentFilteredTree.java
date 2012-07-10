package org.gumtree.gumnix.sics.internal.ui.editors;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.core.SicsUtils;
import org.gumtree.gumnix.sics.internal.ui.Activator;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.gumnix.sics.ui.componentview.VariableContentProvider;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.Property;

public class ComponentFilteredTree {

	private static Image pathImage;

	private static Image variableImage;

	static {
		if(Activator.getDefault() != null) {
			pathImage = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/obj16/package_obj.gif").createImage();
			variableImage = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/obj16/plugin_obj.gif").createImage();
		}
	}

	private String selectionPropertyId;

	private String selectionPropertyValue;

	private String variable;

//	private Component selectedComponent;

	private FilteredTree tree;

	public ComponentFilteredTree(String selectionPropertyId, String selectionPropertyValue) {
		super();
		Assert.isNotNull(selectionPropertyId);
		Assert.isNotNull(selectionPropertyValue);
		this.selectionPropertyId = selectionPropertyId;
		this.selectionPropertyValue = selectionPropertyValue;
	}

	public void createTreeControl(Composite parent) {
		parent.setLayout(new GridLayout());
		tree = new FilteredTree(parent, SWT.SINGLE | SWT.BORDER, new PatternFilter());
		tree.getViewer().setContentProvider(new VariableContentProvider(selectionPropertyId, selectionPropertyValue));
		tree.getViewer().setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				if(element instanceof Component) {
					return ((Component)element).getId();
				}
				return element.toString();
			}
			 public Image getImage(Object element) {
				 if(element instanceof Component) {
					 Component component = (Component)element;
					 Property property = SicsUtils.getProperty(component, selectionPropertyId);
					 if(property != null && property.getValue().contains(selectionPropertyValue)) {
						return variableImage;
					 }
					 return pathImage;
				 }
				 return null;
			 }
		});
		try {
			tree.getViewer().setInput(SicsCore.getSicsManager().service().getOnlineModel());
		} catch (SicsIOException e) {
			e.printStackTrace();
		}
		tree.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object selection = ((IStructuredSelection)event.getSelection()).getFirstElement();
				if(selection instanceof Component) {
					Component component = (Component)selection;
//					Property property = SicsUtils.getProperty(component, selectionPropertyId);
//					if(property != null && property.getValue().contains(selectionPropertyValue)) {
//						selectedComponent = (Component)selection;
//					}
					String sicsdev = SicsUtils.getPropertyFirstValue(component, "sicsdev");
					if(sicsdev != null) {
						variable = sicsdev;
					}
				}
			}
		});
		tree.getViewer().expandAll();
	}

	public String getSelectionPropertyId() {
		return selectionPropertyId;
	}

	public String getSelectionPropertyValue() {
		return selectionPropertyValue;
	}

//	public Component getSelectedComponent() {
//		return selectedComponent;
//	}

	public void addDoubleClickListener(IDoubleClickListener listener) {
		if(tree != null) {
			tree.getViewer().addDoubleClickListener(listener);
		}
	}

	public String getSelectedVariable() {
		return variable;
	}

}
