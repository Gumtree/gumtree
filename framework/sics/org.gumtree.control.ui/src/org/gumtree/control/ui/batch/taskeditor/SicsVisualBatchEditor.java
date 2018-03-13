/*****************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *****************************************************************************/

package org.gumtree.control.ui.batch.taskeditor;

import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.window.Window;
import org.eclipse.nebula.widgets.pgroup.ext.MenuBasedGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.gumtree.control.batch.tasks.ISicsBatchTask;
import org.gumtree.control.batch.tasks.ISicsCommandBlock;
import org.gumtree.control.ui.batch.SicsBatchUIUtils;
import org.gumtree.control.ui.batch.SicsBatchViewer;
import org.gumtree.control.ui.viewer.InternalImage;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.widgets.swt.util.UIResources;
import org.gumtree.workflow.ui.ITask;
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.IWorkflow;
import org.gumtree.workflow.ui.util.WorkflowFactory;
import org.gumtree.workflow.ui.viewer.AbstractWorkflowViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SicsVisualBatchEditor extends AbstractWorkflowViewer {

	public static final String EXPERIMENT_PROJECT = "Experiment";
	public static final String GUMTREE_FOLDER = "GumtreeOnly";
	public static final String AUTOSAVE_FOLDER = "AutoSaves";
	public static final String TEMP_FILENAME = "TempDontOverwrite.wml";
	protected SicsBatchViewer batchViewer;
	private static final Logger logger = LoggerFactory.getLogger(SicsVisualBatchEditor.class);
	
	private Font boldFont;
	
	private Font normalFont;

	private ScrolledForm designForm;

	protected CTabFolder tabFolder;
	protected Composite designArea;
	protected Button addBlockButton;
	
	protected static String fileDialogPath;
	
	protected Button loadButton;
	protected Button saveButton;
	
	/*************************************************************************
	 * UI Creation methods
	 *************************************************************************/
	
	@Override
	protected void createViewerControl(Composite parent) {
		parent.setLayout(new FillLayout());
		boldFont = UIResources.getDefaultFont(SWT.BOLD);
		normalFont = UIResources.getDefaultFont();
		tabFolder = new CTabFolder(parent, SWT.BOTTOM);
		tabFolder.setLayout(new FillLayout());
		
		/*********************************************************************
		 * Design tab
		 *********************************************************************/
		CTabItem designTab = new CTabItem(tabFolder, SWT.NONE);
		designArea = getToolkit().createComposite(tabFolder);
		designTab.setText("Design");
		designTab.setControl(designArea);
		createDesignArea(designArea);

		/*********************************************************************
		 * Run tab
		 *********************************************************************/
		CTabItem runTab = new CTabItem(tabFolder, SWT.NONE);
		runTab.setText("Run");
		Composite runArea = getToolkit().createComposite(tabFolder);
		runTab.setControl(runArea);
		createRunArea(runArea);
		

		/*********************************************************************
		 * Default setting
		 *********************************************************************/
		// Set bold font
		tabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				for (CTabItem item : tabFolder.getItems()) {
					item.setFont(normalFont);
				}
				tabFolder.getSelection().setFont(boldFont);
			}
		});
		tabFolder.setSelection(designTab);
//		tabFolder.setSelection(runTab);
		designTab.setFont(boldFont);
		Transfer[] transfers = new Transfer[] { FileTransfer.getInstance() };
		DropTarget dropTarget = new DropTarget(parent, DND.DROP_MOVE);
	    dropTarget.setTransfer(transfers);
	    dropTarget.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent event) {
				Object adaptable = event.data;
				if(adaptable instanceof String[]) {
					try {
						String filename = ((String[]) adaptable)[0];
						InputStream input = new FileInputStream(filename);
						IWorkflow workflow = WorkflowFactory.createWorkflow(input);
						input.close();
						setWorkflow(workflow);
						refreshUI();
					} catch (Exception error) {
						LoggerFactory.getLogger(this.getClass()).error("Cannot open file ", error);
					}
				}
			}
		});
	}

	private void createDesignArea(Composite parent) {
		/*********************************************************************
		 * Design Area
		 *********************************************************************/
		GridLayoutFactory.swtDefaults().margins(1, 1).numColumns(3).applyTo(parent);
		designForm = getToolkit().createScrolledForm(parent);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).span(3, 1).applyTo(designForm);
		createCommandBlocksArea(designForm.getBody());
		
		/*********************************************************************
		 * Add command block button
		 *********************************************************************/
		addBlockButton = getToolkit().createButton(parent, "Add Command Block", SWT.PUSH);
		addBlockButton.setImage(InternalImage.ADD.getImage());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(addBlockButton);
		addBlockButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addCommandBlock();
			}
		});
		
		/*********************************************************************
		 * Save button
		 *********************************************************************/
		saveButton = getToolkit().createButton(parent, "Save", SWT.PUSH);
		saveButton.setImage(InternalImage.SAVE.getImage());
//		saveButton.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				FileDialog dialog = new FileDialog(designForm.getShell(), SWT.SAVE);
//				String filePath = dialog.open();
//				if (filePath != null) {
//					try {
//						OutputStream out = new FileOutputStream(filePath);
//						WorkflowFactory.saveWorkflow(getWorkflow(), out);
//						out.flush();
//						out.close();
//					} catch (Exception error) {
//						logger.error("Cannot save file " + filePath, error);
//					}
//				}
//			}
//		});
		saveButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {

                  Shell parentShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                  SaveAsDialog dialog = new SaveAsDialog(parentShell);
                  dialog.open();
                  IPath filePath = dialog.getResult();
                  if (filePath != null) {
                	  // Fix file extension
                	  if (filePath.getFileExtension() == null) {
                		  filePath = filePath.addFileExtension("wml");
                	  }
                	  IWorkspace workspace= ResourcesPlugin.getWorkspace();
                	  IFile file= workspace.getRoot().getFile(filePath);
                	  try {
                		  OutputStream out = new FileOutputStream(file.getLocation().toFile());
                		  WorkflowFactory.saveWorkflow(getWorkflow(), out);
                		  out.flush();
                		  out.close();
                		  // Refresh UI
                		  file.getParent().refreshLocal(1, new NullProgressMonitor());
                	  } catch (Exception error) {
                		  logger.error("Cannot save file " + file.getName(), error);
                	  }
                  }
            }

      });


		
		/*********************************************************************
		 * Load button
		 *********************************************************************/
		loadButton = getToolkit().createButton(parent, "Load", SWT.PUSH);
		loadButton.setImage(InternalImage.LOAD.getImage());
		loadButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
					
 				FileDialog dialog = new FileDialog(designForm.getShell(), SWT.OPEN);
 				if (fileDialogPath == null){
 					IWorkspace workspace= ResourcesPlugin.getWorkspace();
 					IWorkspaceRoot root = workspace.getRoot();
 					dialog.setFilterPath(root.getLocation().toOSString());
 				}else
 					dialog.setFilterPath(fileDialogPath);
 				dialog.setFilterExtensions(new String[]{"*.wml"});
 				dialog.open();
				String filePath = dialog.getFilterPath() + "/" + dialog.getFileName();
				File pickedFile = new File(filePath);
				if (!pickedFile.exists() || !pickedFile.isFile())
					return;
				fileDialogPath = pickedFile.getParent();
				if (filePath != null) {
					try {
						InputStream input = new FileInputStream(filePath);
						IWorkflow workflow = WorkflowFactory.createWorkflow(input);
						input.close();
						setWorkflow(workflow);
						refreshUI();
					} catch (Exception error) {
						logger.error("Cannot open file " + filePath, error);
					}
				}
			}
		});
	}
	
	protected void createCommandBlocksArea(Composite parent) {
		/*********************************************************************
		 * Dispose all UI and task views
		 *********************************************************************/
		for (Control child : parent.getChildren()) {
			child.dispose();
		}
		for (ITaskView taskView : getTaskViews()) {
			destoryTaskView(taskView);
		}
		getTaskViews().clear();
		
		GridLayoutFactory.swtDefaults().margins(1, 1).applyTo(parent);
		
		/*********************************************************************
		 * Reconstruct command blocks
		 *********************************************************************/
		for (final ITask task : getWorkflow().getTasks()) {
			if (task instanceof CommandBlockTask) {
				// Create holder group
				final MenuBasedGroup group = new MenuBasedGroup(parent, SWT.SMOOTH);
				group.setClickExpandEnabled(true);
				group.setLayout(new FillLayout());
				getToolkit().adapt(group);
				GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
				
				// Header display
				final ISicsCommandBlock commandBlock = ((CommandBlockTask) task).getDataModel();
				if (commandBlock.getName() != null) {
					group.setText(commandBlock.getName());
				}
				
				// Create menu
				Menu menu = group.getMenu();
				
				// Set Name
				MenuItem editItem = new MenuItem(menu, SWT.PUSH);
				editItem.setText("Edit name");
				editItem.setImage(InternalImage.TEXT_EDIT.getImage());
				editItem.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						InputDialog inputDialog = new InputDialog(
								designForm.getShell(),
								"Command block name",
								"Edit command block name",
								commandBlock.getName(),
								null
						);
						if (inputDialog.open() == Window.OK) {
							commandBlock.setName(inputDialog.getValue());
							if (inputDialog.getValue() != null) {
								group.setText(inputDialog.getValue());
							}
						}
					}
				});
				
				// Separator
				new MenuItem(menu, SWT.SEPARATOR);
				
				// Remove operation
				MenuItem removeItem = new MenuItem(menu, SWT.PUSH);
				removeItem.setText("Remove");
				removeItem.setImage(InternalImage.DELETE.getImage());
				removeItem.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						removeCommandBlock(group, (CommandBlockTask) task);
					}
				});
				
				// Separator
				new MenuItem(menu, SWT.SEPARATOR);
				
				// Move up
				MenuItem moveUpItem = new MenuItem(menu, SWT.PUSH);
				moveUpItem.setText("Move up");
				moveUpItem.setImage(InternalImage.UP.getImage());
				moveUpItem.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						moveCommandBlockUp((CommandBlockTask) task);
					}
				});
				
				// Move down
				MenuItem moveDownItem = new MenuItem(menu, SWT.PUSH);
				moveDownItem.setText("Move down");
				moveDownItem.setImage(InternalImage.DOWN.getImage());
				moveDownItem.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						moveCommandBlockDown((CommandBlockTask) task);
					}
				});
				
				// Create task view
				Composite viewArea = getToolkit().createComposite(group);
				ITaskView taskView = createTaskView(task);
				taskView.createPartControl(viewArea);
			}
		}
	}
	
	protected void createRunArea(final Composite parent) {
		parent.setLayout(new FillLayout());
//		final SicsBatchViewer batchViewer = new SicsBatchViewer();
//		batchViewer.createPartControl(parent);
		tabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// This tab is selected
				if (batchViewer == null){
					batchViewer = new SicsBatchViewer();
					batchViewer.createPartControl(parent);
				}
				if (tabFolder.getSelectionIndex() == 1) {
					String script = getGeneratedScript();
					if (script.trim().length() > 0)
						saveTempWorkflow(TEMP_FILENAME);
						batchViewer.setCommandText(script);
				}
			}
		});
	}
	
	
	/*************************************************************************
	 * Structure logics
	 *************************************************************************/
	
	protected void addCommandBlock() {
		// Add to the workflow model
		getWorkflow().addTask(new CommandBlockTask());
		// Refresh UI
		refreshUI();
	}
	
	protected void removeCommandBlock(MenuBasedGroup group, CommandBlockTask task) {
		// Remove UI
		group.dispose();
		// Remove from model
		if (getBatchScript() != null) {
			getBatchScript().removeCommandBlock(task.getDataModel());
		}
		getWorkflow().removeTask(task);
		// Refresh UI
		refreshUI();
	}
	
	protected void moveCommandBlockUp(CommandBlockTask task) {
		int currentIndex = getWorkflow().getTasks().indexOf(task);
		if (currentIndex > 0) {
			// Update workflow structure
//			getWorkflow().removeTask(task);
//			getWorkflow().insertTask(currentIndex - 1, task);
			
			getWorkflow().swapTask(task, getWorkflow().getTasks().get(currentIndex - 1));
			// Update data model
			if (getBatchScript() != null) {
				getBatchScript().removeCommandBlock(task.getDataModel());
				getBatchScript().insertCommandBlock(currentIndex - 1, task.getDataModel());
			}
			// Refresh UI
			refreshUI();
		}
	}
	
	protected void moveCommandBlockDown(CommandBlockTask task) {
		int currentIndex = getWorkflow().getTasks().indexOf(task);
		if (currentIndex < getWorkflow().getTasks().size() - 1) {
			// Update workflow structure
//			getWorkflow().removeTask(task);
//			getWorkflow().insertTask(currentIndex + 1, task);
			getWorkflow().swapTask(task, getWorkflow().getTasks().get(currentIndex + 1));
			// Update data model
			if (getBatchScript() != null) {
				getBatchScript().removeCommandBlock(task.getDataModel());
				getBatchScript().insertCommandBlock(currentIndex + 1, task.getDataModel());
			}
			// Refresh UI
			refreshUI();
		}
	}
	
	/*************************************************************************
	 * UI infrastructure methods
	 *************************************************************************/
	
	protected ISicsBatchTask getBatchScript() {
		return getWorkflow().getContext().getSingleValue(ISicsBatchTask.class);
	}
	
	protected String getGeneratedScript() {
		ISicsBatchTask batchScript = getBatchScript();
		if (batchScript != null) {
			return batchScript.toScript();
		}
		return "";
	}
	
	protected void refreshUI() {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				createCommandBlocksArea(designForm.getBody());
				designForm.getBody().layout(true, true);
				designForm.reflow(true);
			}			
		});
	}
	
	@Override
	protected void handleTaskViewRefreshEvent(ITaskView taskView) {
		if (designForm != null && !designForm.isDisposed()) {
			// Refresh UI
			SafeUIRunner.asyncExec(new SafeRunnable() {
				public void run() throws Exception {
					if (designForm != null && !designForm.isDisposed()) {
						designForm.layout(true, true);
						designForm.reflow(true);
					}
				}
			});
		}
	}

	public void dispose() {
		boldFont = null;
		normalFont = null;
		designForm = null;
		super.dispose();
	}
	
	protected void saveTempWorkflow(String filename){
		
		try {
			IFolder folder = getProjectFolder(EXPERIMENT_PROJECT, AUTOSAVE_FOLDER);
			IFile file = folder.getFile(filename);
//			if (file.exists())
//				throw new FitterException("function with the same name already exists");
    		  OutputStream out = new FileOutputStream(file.getLocation().toFile());
    		  WorkflowFactory.saveWorkflow(getWorkflow(), out);
    		  out.flush();
    		  out.close();
    		  // Refresh UI
    		  file.getParent().refreshLocal(1, new NullProgressMonitor());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static IFolder getProjectFolder(String projectName, String folderName) throws CoreException{
		final IWorkspace workspace = getWorkspace();
		final IProject project = workspace.getRoot().getProject(projectName);
		if( !project.exists() )
			project.create( null );
		project.open( null );
//		final IProject project = workspace.getRoot().getProject( SCRIPTS_PROJECT );
		IFolder folder = project.getFolder(new Path(folderName));
		if (! folder.exists()){
			try{
				folder.create( IResource.NONE, true, null );
			}catch (Exception e) {
				project.refreshLocal(1, new NullProgressMonitor());
			}
		}
		return folder;
	}

	public void clearAll(){
		setWorkflow(SicsBatchUIUtils.createDefaultWorkflow());
		refreshUI();
	}
}
