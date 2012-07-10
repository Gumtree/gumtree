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

package org.gumtree.workflow.ui.viewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SearchPattern;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.gumtree.core.object.ObjectCreateException;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.ui.util.resource.UIResourceManager;
import org.gumtree.ui.util.resource.UIResources;
import org.gumtree.ui.util.workbench.AbstractPartControlProvider;
import org.gumtree.workflow.ui.IWorkflow;
import org.gumtree.workflow.ui.internal.Activator;
import org.gumtree.workflow.ui.internal.InternalImage;
import org.gumtree.workflow.ui.util.ITaskDescriptor;
import org.gumtree.workflow.ui.util.ITaskRegistry;
import org.gumtree.workflow.ui.util.WorkflowUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskToolbar extends AbstractPartControlProvider {

	private static final String DEFAULT_FILTER_TEXT = "type filter text";
	
	private static Logger logger = LoggerFactory.getLogger(TaskToolbar.class);

	private FormToolkit toolkit;
	
	private ITaskRegistry taskRegistry;
	
	private Text filterText;
	
	private FormText descText;
	
	private CLabel taskLabel;
	
	private ScrolledForm descForm;
	
	private Cursor handCursor;
	
	private UIResourceManager resourceManager;
	
	private IWorkflow workflow;
	
	private TableViewer taskViewer;
	
	private SearchPattern searchPattern;
	
	private Job filterJob;
	
	public TaskToolbar(IWorkflow workflow) {
		this.workflow = workflow;
	}
	
	public void createControl(Composite parent) {
		/*********************************************************************
		 * Initialise
		 *********************************************************************/
		resourceManager = new UIResourceManager(Activator.PLUGIN_ID, parent);
		handCursor = UIResources.getSystemCursor(SWT.CURSOR_HAND);
		searchPattern = new SearchPattern();
		
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		getToolkit().adapt(sashForm);
		Composite composite = getToolkit().createComposite(sashForm);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(composite);
		
		/*********************************************************************
		 * Row 1 - filter text
		 *********************************************************************/
		filterText = getToolkit().createText(composite, DEFAULT_FILTER_TEXT, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(filterText);
		
		Label clearButton = getToolkit().createLabel(composite, "");
		clearButton.setImage(InternalImage.CLEAR_EDIT.getImage());
		clearButton.setCursor(handCursor);
		
		/*********************************************************************
		 * Row 2 - tag filter
		 *********************************************************************/
		final ComboViewer comboViewer = new ComboViewer(composite, SWT.READ_ONLY);
		comboViewer.getCombo().setVisibleItemCount(25);
		comboViewer.setContentProvider(new ArrayContentProvider());
		comboViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				// Replace first letter to upper case
				return element.toString().substring(0, 1).toUpperCase()
						+ element.toString().substring(1);
			}
		});
		List<String> tags = new ArrayList<String>(Arrays.asList(getTaskRegistry().getAvailableTags()));
		tags.add(0, "All");
		comboViewer.setInput(tags);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(comboViewer.getControl());
		
		/*********************************************************************
		 * Row 3 - task viewer
		 *********************************************************************/
		taskViewer = new TableViewer(composite, SWT.V_SCROLL | SWT.BORDER);
//		taskViewer.getTable().setFont(resourceManager.createFont(SWT.BOLD));
		taskViewer.setContentProvider(new ArrayContentProvider());
		taskViewer.setLabelProvider(new LabelProvider() {
			public Image getImage(Object element) {
				return ((ITaskDescriptor) element).getIcon();
			}
			public String getText(Object element) {
				return " " + ((ITaskDescriptor) element).getLabel();
			}
		});
		taskViewer.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object e1, Object e2) {
				return ((ITaskDescriptor) e1).getLabel().compareTo(((ITaskDescriptor) e2).getLabel());
			}
		});
		taskViewer.addDragSupport(DND.DROP_MOVE,
				new Transfer[] { LocalSelectionTransfer.getTransfer() },
				new DragSourceAdapter() {
					public void dragFinished(DragSourceEvent event) {
						LocalSelectionTransfer.getTransfer().setSelection(null);
					}
					public void dragSetData(DragSourceEvent event) {
						if (LocalSelectionTransfer.getTransfer().isSupportedType(event.dataType)) {
							LocalSelectionTransfer.getTransfer().setSelection(taskViewer.getSelection());
						}
					}
			
		});
		GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(taskViewer.getControl());		
		
		/*********************************************************************
		 * Row 4 - task description
		 *********************************************************************/
		descForm = getToolkit().createScrolledForm(sashForm);
		GridLayoutFactory.swtDefaults().margins(0, 0).spacing(0, 0).applyTo(descForm.getBody());
		
		taskLabel = new CLabel(descForm.getBody(), SWT.WRAP);
		getToolkit().adapt(taskLabel);
		taskLabel.setFont(resourceManager.createDefaultFont(10, SWT.BOLD));
		GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 32).applyTo(taskLabel);
		
		descText = getToolkit().createFormText(descForm.getBody(), false);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(descText);
		sashForm.setWeights(new int[] {3, 1} );
		
		/*********************************************************************
		 * Widget logic
		 *********************************************************************/
		// Ensure default filter text is highlighted
		filterText.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (filterText.getText() != null && filterText.getText().equals(DEFAULT_FILTER_TEXT)) {
					filterText.selectAll();
				}
			}
		});
		
		// Clear filter text
		clearButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				filterText.setText("");
			}
		});
		
		// Append new task based on double click
		taskViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				ITaskDescriptor desc = (ITaskDescriptor) ((IStructuredSelection) taskViewer.getSelection()).getFirstElement();
				addNewTask(desc);
			}
		});
		
		// Update description based on selection
		taskViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ITaskDescriptor desc = (ITaskDescriptor) ((IStructuredSelection) event.getSelection()).getFirstElement();
				updateTaskDescription(desc, taskLabel, descText);
				// To ensure the form text wraps
				GridDataFactory.fillDefaults().grab(true, true).hint(descForm.getSize().x, SWT.DEFAULT).applyTo(descText);
				descForm.layout(true, true);
				descForm.reflow(true);
			}
		});
		
		filterText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String tag = (String) ((IStructuredSelection) comboViewer.getSelection()).getFirstElement();
				setTasks(tag, filterText.getText());
			}
		});
		
		// Apply filter on tag
		comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				String tag = (String) ((IStructuredSelection) event.getSelection()).getFirstElement();
				setTasks(tag, filterText.getText());
			}			
		});
		
		/*********************************************************************
		 * Initialise widget value
		 *********************************************************************/
		comboViewer.getCombo().select(0);
		taskViewer.setInput(taskRegistry.getAllTaskDescriptors());
	}

	public void dispose() {
		if (toolkit != null) {
			toolkit.dispose();
			toolkit = null;
		}
		if (filterJob != null) {
			filterJob.cancel();
			filterJob = null;
		}
		taskViewer = null;
		searchPattern = null;
		handCursor = null;
		resourceManager = null;
		filterText = null;
	}

	public void setFocus() {
		// Ensure default filter text is highlighted
		filterText.selectAll();
		filterText.setFocus();
	}

	private void setTasks(final String tag, final String pattern) {
		if (filterJob != null) {
			filterJob.cancel();
		}
		
		// Use multithreading for running the filter logic
		filterJob = new Job("Task filter") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				// Avoid using this help text as pattern
				if (pattern.equals(DEFAULT_FILTER_TEXT)) {
					searchPattern.setPattern("");
				} else {
					searchPattern.setPattern("*" + pattern);
				}
				final List<ITaskDescriptor> descs = new CopyOnWriteArrayList<ITaskDescriptor>();
				if (tag.equals("All")) {
					descs.addAll(Arrays.asList(taskRegistry.getAllTaskDescriptors()));
				} else {
					descs.addAll(Arrays.asList(taskRegistry.getTaskDescriptorsByTag(tag)));
				}
				
				for (ITaskDescriptor desc : descs) {
					if (!searchPattern.matches(desc.getLabel()) && !searchPattern.matches(desc.getDescription())) {
						descs.remove(desc);
					}
				}
				SafeUIRunner.asyncExec(new SafeRunnable() {
					public void run() throws Exception {
						taskViewer.setInput(descs.toArray(new ITaskDescriptor[descs.size()]));
					}					
				});
				return Status.OK_STATUS;
			}			
		};
		
		filterJob.schedule();
	}
	
	public static void updateTaskDescription(ITaskDescriptor desc, CLabel label, FormText text) {
		if (desc != null) {
			label.setImage(desc.getLargeIcon());
			label.setText(desc.getLabel());
		
			StringBuilder sb = new StringBuilder();
			sb.append("<form>");
			sb.append("<p>"); 
			sb.append("</p>");
			sb.append("<p>");
			sb.append(desc.getDescription());
			sb.append("</p>");
			sb.append("<p>"); 
			sb.append("</p>");
			sb.append("<p>");
			sb.append("<b>Provider:</b>" + " " + desc.getProvider());;
			sb.append("</p>");
			sb.append("</form>");
			text.setText(sb.toString(), true, false);
		} else {
			label.setImage(null);
			label.setText("");
			text.setText("", false, false);
		}
	}
	
	protected FormToolkit getToolkit() {
		if (toolkit == null) {
			toolkit = new FormToolkit(Display.getDefault());
		}
		return toolkit;
	}
	
	protected IWorkflow getWorkflow() {
		return workflow;
	}
	
	protected ITaskRegistry getTaskRegistry() {
		if (taskRegistry == null) {
			taskRegistry = ServiceUtils.getService(ITaskRegistry.class);
		} return taskRegistry;
	}
	
	private void addNewTask(ITaskDescriptor taskDesc) {
		try {
			WorkflowUtils.addNewTask(getWorkflow(), taskDesc);
		} catch (ObjectCreateException e) {
			logger.error("Failed to create task " + taskDesc.getClassname(), e);
		}
	}
	
}
