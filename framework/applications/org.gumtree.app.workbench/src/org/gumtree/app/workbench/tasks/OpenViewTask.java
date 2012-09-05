/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.app.workbench.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dialogs.ShowViewDialog;
import org.eclipse.ui.views.IViewDescriptor;
import org.gumtree.app.workbench.internal.Activator;
import org.gumtree.app.workbench.internal.InternalImage;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.widgets.swt.util.UIResourceManager;
import org.gumtree.widgets.swt.util.UIResources;
import org.gumtree.workflow.ui.AbstractTask;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.WorkflowException;

@SuppressWarnings("restriction")
public class OpenViewTask extends AbstractTask {

	@Override
	protected Object createModelInstance() {
		return new ArrayList<IViewDescriptor>();
	}

	@Override
	protected ITaskView createViewInstance() {
		return new OpenViewTaskView();
	}

	@Override
	protected Object run(Object input) throws WorkflowException {
		for (final IViewDescriptor desc : getDataModel()) {
			SafeUIRunner.asyncExec(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(desc.getId());
				}				
			});
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<IViewDescriptor> getDataModel() {
		return (List<IViewDescriptor>) super.getDataModel();
	}
	
	private class OpenViewTaskView extends AbstractTaskView {

		private UIResourceManager resourceManager;
		
		@Override
		public void createPartControl(Composite parent) {
			resourceManager = new UIResourceManager(Activator.PLUGIN_ID, parent);
			GridLayoutFactory.swtDefaults().numColumns(3).applyTo(parent);
			
			Label label = getToolkit().createLabel(parent, "Views: ");
			GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).span(1, 2).applyTo(label);
			label.setFont(UIResources.getDefaultFont(SWT.BOLD));
			
			final TableViewer tableViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
			tableViewer.setContentProvider(new ArrayContentProvider());
			tableViewer.setLabelProvider(new LabelProvider() {
				public Image getImage(Object element) {
					ImageDescriptor desc = ((IViewDescriptor) element).getImageDescriptor();
					Image image = (Image) resourceManager.find(desc);
					if (image == null) {
						image = resourceManager.createImage(desc);
					}
					return image;
				}
				public String getText(Object element) {
					return ((IViewDescriptor) element).getLabel();
				}
			});
			updateInput(tableViewer);
			GridDataFactory.fillDefaults().grab(true, false).span(1, 2).hint(300, 50).applyTo(tableViewer.getControl());
			
			Button selectButton = getToolkit().createButton(parent, "Select", SWT.PUSH);
			selectButton.setImage(InternalImage.ADD_VIEW.getImage());
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).applyTo(selectButton);
			
			Button clearButton = getToolkit().createButton(parent, "Clear", SWT.PUSH);
			clearButton.setImage(InternalImage.CLEAR.getImage());
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).applyTo(clearButton);
			
			selectButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					ShowViewDialog dialog = new ShowViewDialog(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
							PlatformUI.getWorkbench().getViewRegistry());
					int reutrnCode = dialog.open();
					if (reutrnCode == Window.OK) {
						getDataModel().clear();
						getDataModel().addAll(Arrays.asList(dialog.getSelection()));
						updateInput(tableViewer);
					}
				}
			});
			
			clearButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					getDataModel().clear();
					updateInput(tableViewer);
				}
			});
		}
		
		private void updateInput(TableViewer tableViewer) {
			tableViewer.setInput(getDataModel().toArray(new IViewDescriptor[getDataModel().size()]));
		}
		
		public void dispose() {
			resourceManager = null;
			super.dispose();
		}
	}

	public Class<?>[] getInputTypes() {
		return null;
	}

	public Class<?>[] getOutputTypes() {
		return null;
	}
		
}
