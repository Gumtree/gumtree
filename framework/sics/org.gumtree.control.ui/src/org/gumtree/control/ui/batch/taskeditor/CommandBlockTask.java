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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.gumtree.control.batch.tasks.ISicsCommand;
import org.gumtree.control.ui.batch.DndTransferData;
import org.gumtree.control.ui.batch.SicsBatchUIUtils;
import org.gumtree.control.ui.batch.command.ISicsCommandView;
import org.gumtree.control.ui.batch.command.SicsCommandFactory;
import org.gumtree.control.ui.batch.command.SicsCommandType;
import org.gumtree.control.ui.internal.Activator;
import org.gumtree.control.ui.viewer.InternalImage;
import org.gumtree.core.object.ObjectCreateException;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.widgets.swt.util.UIResourceManager;
import org.gumtree.widgets.swt.util.UIResources;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.IControllableTask;
import org.gumtree.workflow.ui.ITaskView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandBlockTask extends AbstractCommandBlockTask {
	
	private static final Logger logger = LoggerFactory.getLogger(CommandBlockTask.class);
	
	private static final String PARAM_EDITABLE = "editable";
	private boolean isDirty = false;
	protected ITaskView createViewInstance() {
		return new CommandBlockTaskView();
	}
	
	private class CommandBlockTaskView extends AbstractTaskView {

		private static final int WIDTH_LABEL = 70;
		
		private UIResourceManager resourceManager;
		
		private Font boldFont;
		
		private Composite commandsArea;
		
		private boolean isEditable; 
		
		/*********************************************************************
		 * UI creation methods 
		 *********************************************************************/
		
		public void createPartControl(Composite parent) {
			/*****************************************************************
			 * Preparation
			 *****************************************************************/
			parent.setLayout(new GridLayout(2, false));
			resourceManager = new UIResourceManager(Activator.PLUGIN_ID, parent);
			boldFont = UIResources.getDefaultFont(SWT.BOLD);
			isEditable = getParameters().get(PARAM_EDITABLE, boolean.class, true);
			
			/*****************************************************************
			 * Add command button 
			 *****************************************************************/
			Button addButton = null;
			if (isEditable()) {
				addButton = getToolkit().createButton(parent, "", SWT.PUSH);
				addButton.setImage(InternalImage.ADD.getImage());
			}
			
			/*****************************************************************
			 * Block status area
			 *****************************************************************/
			Composite statusArea = getToolkit().createComposite(parent);
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(statusArea);
			StackLayout stackLayout = new StackLayout();
			statusArea.setLayout(stackLayout);
			Label statusText = getToolkit().createLabel(statusArea, "");
			statusText.setFont(boldFont);
//			ProgressBar progressBar = new ProgressBar(statusArea, SWT.NONE);
			stackLayout.topControl = statusText;
			if (isEditable()) {
				statusText.setText("Press + to add command");
			}
			
			
			/*****************************************************************
			 * Commands area
			 *****************************************************************/
			commandsArea = getToolkit().createComposite(parent);
			GridLayoutFactory.fillDefaults().margins(0, 0).spacing(1, 1).applyTo(commandsArea);
			GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(commandsArea);
			// We use this to minimise the initial size of block UI
			Label separator = getToolkit().createLabel(commandsArea, "", SWT.SEPARATOR | SWT.HORIZONTAL);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(separator);
			
			/*****************************************************************
			 * Button menu
			 *****************************************************************/
//			final Menu menu = createCreationMenu(addButton, commandsArea);
			
			if (addButton != null) {
				addButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
//						menu.setVisible(true);
//						Point location = addButton.toDisplay(addButton.getLocation());
//						menu.setLocation(location.x, location.y + addButton.getSize().y);
						
						Composite commandArea = getToolkit().createComposite(commandsArea);
						addCommandUI(commandArea, SicsCommandType.LINE_SCRIPT, -1);
					}
				});
			}
			
			//Add DND drop
			DropTarget dropTarget = new DropTarget(commandsArea, DND.DROP_MOVE);
			dropTarget.setTransfer(new Transfer[]{LocalSelectionTransfer.getTransfer()});
			dropTarget.addDropListener(new DropTargetAdapter(){
				@Override
				public void drop(DropTargetEvent event) {
					if (event.data instanceof StructuredSelection){
						if (((StructuredSelection) event.data).getFirstElement() 
								instanceof DndTransferData) {
							DndTransferData transferData = (DndTransferData) (
									(StructuredSelection) event.data).getFirstElement();
							Object parent = transferData.getParent();
							Object child = transferData.getChild();
							if (parent == getTask()){
								Point relativePoint = commandsArea.toControl(
										new Point(event.x, event.y));
								int index = 0;
								for (Control control : commandsArea.getChildren()){
									//								if (control instanceof Label && PARAM_EDITABLE.equals(control.getData())){
									if (control instanceof Composite) {
										if (relativePoint.y < (control.getBounds().y 
												+ control.getBounds().height)) {
											break;
										}
										index++;
									}
								}
								if (child instanceof ISicsCommand){
									ISicsCommand command = (ISicsCommand) child;

									int currentIndex = getDataModel().indexOf(command);
									// No effect if the command is the same element
									if (currentIndex == index) {
										return;
									}
									/*************************************************************
									 * Model reconstruction
									 *************************************************************/
									// Remove from its original position
									getDataModel().removeCommand(command);
									// Insert to its new position
									getDataModel().insertCommand(index, command);
									isDirty = true;
									/*************************************************************
									 * UI reconstruction
									 *************************************************************/
//									for (Control control : commandsArea.getChildren()) {
//										control.dispose();
//									}
									// Update UI
//									fireRefresh();

								}
							}
						}
					}
				}
			});
			
			/*****************************************************************
			 * UI reconstruction
			 *****************************************************************/
			reconstructUI(commandsArea);
		}
		
		/*********************************************************************
		 * Command creation methods 
		 *********************************************************************/
		
		private void addCommandUI(Composite parent, SicsCommandType type, int location) {
			try {
				/*************************************************************
				 * Model creation
	   		     *************************************************************/
				ISicsCommand command = SicsCommandFactory.createCommand(type);
				// Create custom view
				ISicsCommandView<? extends ISicsCommand> commandView = 
					SicsBatchUIUtils.createCommandView(command);
				commandView.setTaskView(this);
				if (location < 0)
					// Add to model
					getDataModel().addCommand(command);
				else
					getDataModel().insertCommand(location, command);
				// Create UI
				addCommandUI(parent, command, commandView);
			} catch (ObjectCreateException e) {
				createErrorUI(parent, type);
				logger.error("Failed to add command UI.", e);
			}
		}
		
		private void addCommandUI(Composite parent, final ISicsCommand command, 
				ISicsCommandView<? extends ISicsCommand> commandView) {
			// [Tony] [2009-3-20] Strange bug: parent's menu is dispose previously
			// We must reset menu to make UI creation valid
			parent.setMenu(new Menu(parent));
			GridDataFactory.fillDefaults().grab(true, false).applyTo(parent);
			GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(2).equalWidth(false).applyTo(parent);
			SicsCommandType type = SicsCommandType.getType(command.getClass()); 
			
			/*****************************************************************
			 * Label and menu creation
			 *****************************************************************/
			// Create label
			final Label label = getToolkit().createLabel(parent, " " + type.getLabel() + ": ");
			label.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			label.setBackground(resourceManager.createColor(type.getDefaultRGB()));
			label.setFont(boldFont);
			label.setData(PARAM_EDITABLE);
			GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).hint(WIDTH_LABEL, SWT.DEFAULT).applyTo(label);
			// Create menu
			final Menu menu = createMenu(label, command, parent);
			// Show mouse pointer
			if (isEditable()) {
				label.setCursor(UIResources.getSystemCursor(SWT.CURSOR_HAND));
			}
			// Show menu
			label.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDown(MouseEvent e) {
					if (!isEditable()) {
						return;
					}
					if (e.button == 3) {
						menu.setVisible(true);
						Point location = label.toDisplay(label.getLocation());
						menu.setLocation(location.x, location.y + label.getSize().y);
					}
				}
			});
			
			// Add DnD Drag
//			label.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
			int operations = DND.DROP_MOVE;
			DragSource dragSource = new DragSource(label, operations);

			LocalSelectionTransfer transferObject = LocalSelectionTransfer.getTransfer();

			Transfer[] types = new Transfer[] {transferObject};
			dragSource.setTransfer(types);
			final IControllableTask parentTask = getTask();
			dragSource.addDragListener(new DragSourceAdapter() {
				
				@Override
				public void dragFinished(DragSourceEvent event) {
					LocalSelectionTransfer.getTransfer().setSelection(null);
					// Reconstruct UI
					if (isDirty) {
						for (Control control : commandsArea.getChildren()) {
							control.dispose();
						}
						reconstructUI(commandsArea);
					}
				}
				@Override
				public void dragSetData(DragSourceEvent event) {
					if (LocalSelectionTransfer.getTransfer().isSupportedType(event.dataType)) {
						DndTransferData transferData = new DndTransferData();
						transferData.setParent(parentTask);
						transferData.setChild(command);
						LocalSelectionTransfer.getTransfer().setSelection(
								new StructuredSelection(transferData));
					}
				}
			});

			// Bind with UI
			parent.setData(command);
			
			/*****************************************************************
			 * View UI creation
			 *****************************************************************/
			Composite commandViewArea = getToolkit().createComposite(parent);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(commandViewArea);
			commandView.createPartControl(commandViewArea);
			
			// Update UI
			fireRefresh();
		}

		private Menu createMenu(Control parent, final ISicsCommand command, final Composite commandArea) {
			Menu menu = new Menu(parent);
			parent.setMenu(menu);
			SicsCommandType type = SicsCommandType.getType(command.getClass());
			
			// Command type selection
			for (final SicsCommandType typeValue : SicsCommandType.values()) {
				final MenuItem typeItem = new MenuItem(menu, SWT.CHECK);
				typeItem.setText(typeValue.getLabel() + ": ");
				typeItem.setImage(typeValue.getImage());
				if (typeValue.equals(type)) {
					// For current one, make a selection
					typeItem.setSelection(true);
					typeItem.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							// Always select
							typeItem.setSelection(true);
						}
					});
				} else {
					// Else we add change logic
					typeItem.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							changeCommandUI(commandArea, typeValue);
						}
					});
				}
			}
			
			// Separator
			new MenuItem(menu, SWT.SEPARATOR);
			
			// Delete
			MenuItem deleteItem = new MenuItem(menu, SWT.PUSH);
			deleteItem.setText("Delete");
			deleteItem.setImage(InternalImage.DELETE.getImage());
			deleteItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					SafeUIRunner.asyncExec(new SafeRunnable() {
						public void run() throws Exception {
							removeCommandUI(commandArea, command);
						}
					});
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
					moveCommandUp(commandArea, command);
				}
			});
			
			// Move down
			MenuItem moveDownItem = new MenuItem(menu, SWT.PUSH);
			moveDownItem.setText("Move down");
			moveDownItem.setImage(InternalImage.DOWN.getImage());
			moveDownItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					moveCommandDown(commandArea, command);
				}
			});
			
			return menu;
		}
		
		private void createErrorUI(Composite parent, SicsCommandType type) {
			parent.setLayout(new GridLayout());
			getToolkit().createLabel(parent, "Failed to create UI for command type: " + type.getLabel());
			// Update UI
			fireRefresh();
		}

		// Reconstruct the whole UI
		private void reconstructUI(Composite parent) {
			for (ISicsCommand command : getDataModel().getCommands()) {
				Composite commandArea = getToolkit().createComposite(parent);
				try {
					// Create custom view
					ISicsCommandView<? extends ISicsCommand> commandView = 
						SicsBatchUIUtils.createCommandView(command);
					commandView.setTaskView(this);
					addCommandUI(commandArea, command, commandView);
				} catch (ObjectCreateException e) {
					createErrorUI(commandArea, SicsCommandType.getType(command.getClass()));
					logger.error("Failed to add command UI.", e);
				}
			}
			isDirty = false;
		}
		
		/*********************************************************************
		 * Command modification methods
		 *********************************************************************/
		
		private void removeCommandUI(Composite commandArea, ISicsCommand command) {
			// Remove UI
			commandArea.dispose();
			// Remove from model
			getDataModel().removeCommand(command);
			// Update UI
			fireRefresh();
		}
		
		private void changeCommandUI(Composite commandArea, SicsCommandType type) {
			// Get rid of old UI
			for (Control child : commandArea.getChildren()) {
				child.dispose();
			}
			// Remove command
			int commandIndex = getDataModel().indexOf((ISicsCommand) commandArea.getData());
			getDataModel().removeCommand((ISicsCommand) commandArea.getData());
			// Creat new UI
			addCommandUI(commandArea, type, commandIndex);

		}
		
		private void moveCommandUp(Composite commandArea, ISicsCommand command) {
			int currentIndex = getDataModel().indexOf(command);
			// No effect if the command is the first element
			if (currentIndex > 0) {
				/*************************************************************
				 * Model reconstruction
				 *************************************************************/
				// Remove from its original position
				getDataModel().removeCommand(command);
				// Insert to its new position
				getDataModel().insertCommand(currentIndex - 1, command);
				/*************************************************************
				 * UI reconstruction
				 *************************************************************/
				for (Control child : commandsArea.getChildren()) {
					child.dispose();
				}
				// Reconstruct UI
				reconstructUI(commandsArea);
				// Update UI
				fireRefresh();
			}
		}
		
		private void moveCommandDown(Composite commandArea, ISicsCommand command) {
			int currentIndex = getDataModel().indexOf(command);
			// No effect if the command is the last element
			if (currentIndex != getDataModel().size() - 1) {
				/*************************************************************
				 * Model reconstruction
				 *************************************************************/
				// Remove from its original position
				getDataModel().removeCommand(command);
				// Insert to its new position
				getDataModel().insertCommand(currentIndex + 1, command);
				/*************************************************************
				 * UI reconstruction
				 *************************************************************/
				for (Control child : commandsArea.getChildren()) {
					child.dispose();
				}
				// Reconstruct UI
				reconstructUI(commandsArea);
				// Update UI
				fireRefresh();
			}
		}
		
		/*********************************************************************
		 * Helper methods
		 *********************************************************************/
		
		private boolean isEditable() {
			return isEditable;	
		}
		
		/*********************************************************************
		 * Dispose methods
		 *********************************************************************/
		
		public void dispose() {
			boldFont = null;
			super.dispose();
		}
		
	}

	public Class<?>[] getInputTypes() {
		return null;
	}
	
}
