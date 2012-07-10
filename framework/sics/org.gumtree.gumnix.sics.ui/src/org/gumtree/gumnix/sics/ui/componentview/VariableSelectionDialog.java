package org.gumtree.gumnix.sics.ui.componentview;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.core.SicsUtils;
import org.gumtree.gumnix.sics.internal.ui.Activator;
import org.gumtree.gumnix.sics.io.SicsIOException;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.Property;

public class VariableSelectionDialog extends SelectionDialog {

	private static Image pathImage;

	private static Image variableImage;

	static {
		if(Activator.getDefault() != null) {
			pathImage = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/obj16/package_obj.gif").createImage();
			variableImage = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/obj16/plugin_obj.gif").createImage();
		}
	}
	private String variable;

	private String selectionPropertyId;

	private String selectionPropertyValue;

	protected VariableSelectionDialog(Shell parentShell, String selectionPropertyId, String selectionPropertyValue) {
		super(parentShell);
		Assert.isNotNull(selectionPropertyId);
		Assert.isNotNull(selectionPropertyValue);
		this.selectionPropertyId = selectionPropertyId;
		this.selectionPropertyValue = selectionPropertyValue;
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setSize(400, 400);
	}

	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		FilteredTree tree = new FilteredTree(area, SWT.SINGLE | SWT.BORDER, new PatternFilter());
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
					 Property property = SicsUtils.getProperty((Component)element, selectionPropertyId);
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
					String sicsdev = SicsUtils.getPropertyFirstValue(component, "sicsdev");
					if(sicsdev != null) {
						variable = sicsdev;
					}
				}
			}
		});
		tree.getViewer().expandAll();
		return dialogArea;
	}

	public String getSelectedVariable() {
		return variable;
	}
}
