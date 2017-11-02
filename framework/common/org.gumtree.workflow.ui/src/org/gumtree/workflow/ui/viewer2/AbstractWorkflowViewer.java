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

package org.gumtree.workflow.ui.viewer2;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.ui.util.forms.FormControlWidget;
import org.gumtree.widgets.swt.util.UIResources;
import org.gumtree.workflow.ui.IWorkflow;
import org.gumtree.workflow.ui.internal.InternalImage;
import org.gumtree.workflow.ui.util.WorkflowFactory;

public abstract class AbstractWorkflowViewer extends FormControlWidget implements IWorkflowViewer {

	private static final String PROP_SHOW_TASKLIB = "gumtree.sics.showTaskLib";
	private IWorkflow workflow;
	
	private UIContext c;
	
	private List<IWorkflowViewerComponent> components;
	
	public AbstractWorkflowViewer(Composite parent, int style) {
		super(parent, style);
		c = new UIContext();
		components = new ArrayList<IWorkflowViewerComponent>();
	}

	protected void widgetDispose() {
		if (components != null) {
			components.clear();
			components = null;
		}
		workflow = null;
		c = null;
	}

	public void afterParametersSet() {
		if (getWorkflow() == null) {
			setWorkflow(WorkflowFactory.createEmptyWorkflow());
		}
		// Create UI
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				createUI();
			}
		});
	}
	
	public void addViewerComponent(IWorkflowViewerComponent component) {
		components.add(component);
	}
	
	public void removeViewerComponent(IWorkflowViewerComponent component) {
		components.remove(component);
	}
	
	private void createUI() {
		/*********************************************************************
		 * Initialise
		 *********************************************************************/
		GridLayoutFactory.swtDefaults().spacing(2, 2).margins(2, 2).applyTo(this);

		/*********************************************************************
		 * Tool bar and main area
		 *********************************************************************/
		if ((getOriginalStyle() & NO_TOOL) == 0) {
			// Show toolbar 
			c.toolSashForm = new SashForm(this, SWT.HORIZONTAL);
			getToolkit().adapt(c.toolSashForm);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(c.toolSashForm);
			
			Composite toolComposite = getToolkit().createComposite(c.toolSashForm);
			toolComposite.setLayout(new FillLayout());
			setupToolViewer(toolComposite);
			
			Composite mainComposite = getToolkit().createComposite(c.toolSashForm);
			mainComposite.setLayout(new FillLayout());
			createMainArea(mainComposite);
			
			c.toolShown = true;
			boolean showTaskLib = true;
			try {
				showTaskLib = Boolean.valueOf(System.getProperty(PROP_SHOW_TASKLIB));
			} catch (Exception e) {
			}
			if (showTaskLib) {
				c.toolSashForm.setWeights(new int[] { 2, 9 });
			} else {
				c.toolSashForm.setWeights(new int[] { 0, 10 });
			}
		} else {
			// Hide toolbar
			Composite mainComposite = getToolkit().createComposite(this);
			mainComposite.setLayout(new FillLayout());
			GridDataFactory.fillDefaults().grab(true, true).applyTo(mainComposite);
			createMainArea(mainComposite);
		}
		
		/*********************************************************************
		 * Status panel
		 *********************************************************************/
		createStatusArea(this);
		
		/*********************************************************************
		 * Finalise
		 *********************************************************************/
		getParent().layout(true, true);
	}
	
	private void createMainArea(Composite parent) {
		c.mainSashForm = new SashForm(parent, SWT.VERTICAL);
		getToolkit().adapt(c.mainSashForm);

		/*********************************************************************
		 * Main Area
		 *********************************************************************/
		Composite composerArea = getToolkit().createComposite(c.mainSashForm);
		creatComposerArea(composerArea);

		/*********************************************************************
		 * Info Area
		 *********************************************************************/
		CTabFolder tabFolder = new CTabFolder(c.mainSashForm, SWT.BOTTOM | SWT.BORDER);
		getToolkit().adapt(tabFolder);
		
		CTabItem logItem = new CTabItem(tabFolder, SWT.NONE);
		logItem.setText("Log");
		logItem.setImage(InternalImage.LOG_SHOWED.getImage());
		Composite logArea = getToolkit().createComposite(tabFolder);
		logArea.setLayout(new FillLayout());
		setupLogViewer(logArea);
		
		CTabItem contextItem = new CTabItem(tabFolder, SWT.NONE);
		contextItem.setText("Context");
		contextItem.setImage(InternalImage.CONTEXT_SHOWN.getImage());
		Composite contextArea = getToolkit().createComposite(tabFolder);
		contextArea.setLayout(new FillLayout());
		setupContextViewer(contextArea);
		
		tabFolder.setSelection(logItem);
		
		c.mainSashForm.setWeights(new int[] { 10, 0 });
	}
	
	private void creatComposerArea(Composite parent) {
		GridLayoutFactory.fillDefaults().applyTo(parent);

//		// Show header
//		if ((getOriginalStyle() & NO_HEADER) == 0) {
//			Composite headerComposite = new Composite(parent, SWT.NONE) {
//				public void layout (boolean changed, boolean all) {
//					super.layout(changed, all);
//					// Proper layout when the header is rendered
//					getParent().layout(changed, all);
//				}
//			};
//			getToolkit().adapt(headerComposite);
//			headerComposite.setLayout(new FillLayout());
//			setupHeaderControlViewer(headerComposite);
//			if (headerComposite.getChildren().length == 0) {
//				headerComposite.dispose();
//			} else {
//				GridDataFactory.fillDefaults().grab(true, false).applyTo(headerComposite);
//			}
//		}
		
		// Composer area
		Composite composerComposite = getToolkit().createComposite(parent);
		composerComposite.setLayout(new FillLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composerComposite);
		setupComposerViewer(composerComposite);
		
//		// Show footer
//		if ((getOriginalStyle() & NO_FOOTER) == 0) {
//			Composite footerComposite = new Composite(parent, SWT.NONE) {
//				public void layout (boolean changed, boolean all) {
//					super.layout(changed, all);
//					getParent().layout(changed, all);
//				}
//			};
//			getToolkit().adapt(footerComposite);
//			footerComposite.setLayout(new FillLayout());
//			setupFooterControlViewer(footerComposite);
//			if (footerComposite.getChildren().length == 0) {
//				footerComposite.dispose();
//			} else {
//				GridDataFactory.fillDefaults().grab(true, false).applyTo(footerComposite);
//			}
//		}
	}
	
	private void createStatusArea(Composite parent) {
		Composite composite = getToolkit().createComposite(parent);
		GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(7).spacing(2, 2).applyTo(composite);
		
		if ((getOriginalStyle() & NO_TOOL) == 0) {
			final Label toolButton = getToolkit().createLabel(composite, "");
			toolButton.setImage(InternalImage.TOOL_SHOWED.getImage());
			toolButton.setCursor(UIResources.getSystemCursor(SWT.CURSOR_HAND));
			toolButton.addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
					if (c.toolShown && c.toolSashForm != null) {
						c.toolSashForm.setWeights(new int[] { 0, 10 });
						toolButton.setImage(InternalImage.TOOL_HIDED.getImage());
						c.toolShown = false;
					} else if (!c.toolShown && c.toolSashForm != null) {
						c.toolSashForm.setWeights(new int[] { 2, 9 });
						toolButton.setImage(InternalImage.TOOL_SHOWED.getImage());
						c.toolShown = true;
					}
				}
			});
			Label separator = getToolkit().createLabel(composite, "", SWT.SEPARATOR);
			GridDataFactory.swtDefaults().hint(SWT.DEFAULT, 16).applyTo(separator);
		}

		final Label infoButton = getToolkit().createLabel(composite, "");
		infoButton.setImage(InternalImage.LOG_HIDED.getImage());
		infoButton.setCursor(UIResources.getSystemCursor(SWT.CURSOR_HAND));
		infoButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (c.infoShown) {
					c.mainSashForm.setWeights(new int[] { 10, 0 });
					infoButton.setImage(InternalImage.LOG_HIDED.getImage());
					c.infoShown = false;
				} else {
					c.mainSashForm.setWeights(new int[] { 10, 3 });
					infoButton.setImage(InternalImage.LOG_SHOWED.getImage());
					c.infoShown = true;
				}
			}
		});
		c.infoShown = false;
		
		Label separator = getToolkit().createLabel(composite, "", SWT.SEPARATOR);
		GridDataFactory.swtDefaults().hint(SWT.DEFAULT, 16).applyTo(separator);
		
		c.logLabel = new CLabel(composite, SWT.NONE);
		getToolkit().adapt(c.logLabel, false, false);
		c.logLabel.setImage(InternalImage.CHECKED.getImage());
		c.logLabel.setText("Workflow completed");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(c.logLabel);
	}
	
	/*************************************************************************
	 * Getters and setters 
	 *************************************************************************/
	
	public IWorkflow getWorkflow() {
		return workflow;
	}

	public void setWorkflow(IWorkflow workflow) {
		this.workflow = workflow;
		for (IWorkflowViewerComponent component : components) {
			component.setWorkflow(this.workflow);
		}
	}

	/*************************************************************************
	 * Creation methods  
	 *************************************************************************/
	protected IWorkflowViewerComponent createToolViewer(Composite parent) {
		return null;
	}
	
	private void setupToolViewer(Composite parent) {
		IWorkflowViewerComponent toolViewer = createToolViewer(parent);
		configureViewerComponent(toolViewer);
	}
	
	protected IWorkflowViewerComponent createLogViewer(Composite parent) {
		return null;
	}
	
	private void setupLogViewer(Composite parent) {
		IWorkflowViewerComponent logViewer = createLogViewer(parent);
		if (logViewer == null) {
			logViewer = new WorkflowLogViewer(parent, SWT.NONE);
		}
		configureViewerComponent(logViewer);
	}
	
	protected IWorkflowViewerComponent createContextViewer(Composite parent) {
		return null;
	}
	
	private void setupContextViewer(Composite parent) {
		IWorkflowViewerComponent contextViewer = createContextViewer(parent);
		if (contextViewer == null) {
			contextViewer = new WorkflowContextViewer(parent, SWT.NONE);
		}
		configureViewerComponent(contextViewer);
	}
	
	protected IWorkflowViewerComponent createComposerViewer(Composite parent) {
		return null;
	}
	
	private void setupComposerViewer(Composite parent) {
		IWorkflowViewerComponent composerViewer = createComposerViewer(parent);
		if (composerViewer == null) {
			composerViewer = new DefaultWorkflowComposerViewer(parent, SWT.NONE);
		}
		configureViewerComponent(composerViewer);
	}

	private void configureViewerComponent(IWorkflowViewerComponent component) {
		if (component != null) {
			component.setWorkflow(getWorkflow());
			component.setWorkflowViewer(this);
			component.afterParametersSet();
			addViewerComponent(component);
		}
	}
	
	/*************************************************************************
	 * Helper methods and classes
	 *************************************************************************/
	
	private class UIContext {
		private SashForm toolSashForm;
		private SashForm mainSashForm;
		private boolean toolShown;
		private boolean infoShown;
		private CLabel logLabel;
	}
	
}
