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

package au.gov.ansto.bragg.nbi.ui.tasks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.gumtree.core.object.ObjectCreateException;
import org.gumtree.gumnix.sics.batch.ui.CommandBlockTask;
import org.gumtree.gumnix.sics.batch.ui.model.ISicsCommandElement;
import org.gumtree.gumnix.sics.batch.ui.model.SicsCommandType;
import org.gumtree.gumnix.sics.batch.ui.util.SicsBatchUIUtils;
import org.gumtree.gumnix.sics.batch.ui.util.SicsCommandFactory;
import org.gumtree.gumnix.sics.batch.ui.views.ISicsCommandView;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.models.AbstractModelObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.nbi.ui.tasks.AbstractScanTask.ITaskPropertyChangeListener;




public class SicsBlockTask extends CommandBlockTask {
	private static final Logger logger = LoggerFactory.getLogger(CommandBlockTask.class);
	private List<ITaskPropertyChangeListener> taskPropertyChangeListeners = 
		new ArrayList<ITaskPropertyChangeListener>();
	private static final String PARAM_EDITABLE = "editable";
	public static final String TITLE = "Sics Command";

	protected ITaskView createViewInstance() {
		return new CommandBlockTaskView();
	}
	
	private class CommandBlockTaskView extends AbstractTaskView {

		private static final int WIDTH_LABEL = 70;
		
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
			isEditable = getParameters().get(PARAM_EDITABLE, boolean.class, true);
			
			/*****************************************************************
			 * Add command button 
			 *****************************************************************/
			Button addButton = null;
			if (isEditable()) {
				addButton = getToolkit().createButton(parent, "", SWT.PUSH);
				try {
					addButton.setImage(SicsBatchUIUtils.getBatchEditorImage("ADD"));
				} catch (FileNotFoundException e2) {
					LoggerFactory.getLogger(this.getClass()).error("can not find ADD image", e2);
				}
			}
			
			/*****************************************************************
			 * Block status area
			 *****************************************************************/
			Composite statusArea = getToolkit().createComposite(parent);
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(statusArea);
			StackLayout stackLayout = new StackLayout();
			statusArea.setLayout(stackLayout);
			Label statusText = getToolkit().createLabel(statusArea, "");
//			statusText.setFont(boldFont);
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
						addCommandUI(commandArea, SicsCommandType.DRIVABLE, -1);
					}
				});
			}
			
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
				ISicsCommandElement command = SicsCommandFactory.createCommand(type);
				// Create custom view
				ISicsCommandView<? extends ISicsCommandElement> commandView = 
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
		
		private void addCommandUI(Composite parent, ISicsCommandElement command, 
				ISicsCommandView<? extends ISicsCommandElement> commandView) {
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
//			final Label label = getToolkit().createLabel(parent, " " + type.getLabel() + ": ", SWT.SHADOW_ETCHED_OUT);
			final CLabel label = new CLabel(parent, SWT.SHADOW_ETCHED_OUT);
			label.setText(" " + type.getLabel() + ": ");
//			GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).indent(4, 8).applyTo(label);
//			label.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
//			label.setBackground(resourceManager.createColor(type.getDefaultRGB()));
//			label.setFont(boldFont);
			try {
				label.setImage(SicsBatchUIUtils.getBatchEditorImage("MENU"));
			} catch (FileNotFoundException e2) {
				LoggerFactory.getLogger(this.getClass()).error("can not find ADD image", e2);
			}
			label.setFont(new Font(label.getFont().getDevice(), new FontData[]{new FontData("Courier New", 9, SWT.BOLD)}));
			label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).indent(0, 3).hint(120, SWT.DEFAULT).applyTo(label);
			// Create menu
			final Menu menu = createMenu(label, command, parent);
			// Show mouse pointer
//			if (isEditable()) {
//				label.setCursor(handCursor);
//			}
			// Show menu
			label.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDown(MouseEvent e) {
					if (!isEditable()) {
						return;
					}
					menu.setVisible(true);
					Point location = label.toDisplay(label.getLocation());
					menu.setLocation(location.x, location.y + label.getSize().y);
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
			addPropertyChangeListener(command);
			
			// Update UI
			fireRefresh();
		}

		private void addPropertyChangeListener(final ISicsCommandElement command) {
			if (command instanceof AbstractModelObject){
				((AbstractModelObject) command).addPropertyChangeListener(new PropertyChangeListener() {

					public void propertyChange(PropertyChangeEvent arg0) {
						notifyPropertyChanged(command, arg0);
					}
				});
			}
		}

		private Menu createMenu(Control parent, final ISicsCommandElement command, final Composite commandArea) {
			Menu menu = new Menu(parent);
			parent.setMenu(menu);
			SicsCommandType type = SicsCommandType.getType(command.getClass());
			
			// Command type selection
			for (final SicsCommandType typeValue : new SicsCommandType[]{SicsCommandType.DRIVABLE, 
					SicsCommandType.SICS_VARIABLE, SicsCommandType.SCRIPT}) {
				if (typeValue.equals(type)) {
					// For current one, make a selection
//					typeItem.setSelection(true);
//					typeItem.addSelectionListener(new SelectionAdapter() {
//						public void widgetSelected(SelectionEvent e) {
//							// Always select
//							typeItem.setSelection(true);
//						}
//					});
				} else {
					// Else we add change logic
					final MenuItem typeItem = new MenuItem(menu, SWT.CHECK);
					typeItem.setText(typeValue.getLabel() + ": ");
//					typeItem.setImage(typeValue.getImage());
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
			try {
				deleteItem.setImage(SicsBatchUIUtils.getBatchEditorImage("REMOVE"));
			} catch (FileNotFoundException e1) {
				LoggerFactory.getLogger(this.getClass()).error("can not find REMOVE image", e1);
			}
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
//			moveUpItem.setImage(InternalImage.UP.getImage());
			try {
				moveUpItem.setImage(SicsBatchUIUtils.getBatchEditorImage("UP"));
			} catch (FileNotFoundException e2) {
				LoggerFactory.getLogger(this.getClass()).error("can not find ADD image", e2);
			}
			moveUpItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					moveCommandUp(commandArea, command);
				}
			});
			
			// Move down
			MenuItem moveDownItem = new MenuItem(menu, SWT.PUSH);
			moveDownItem.setText("Move down");
//			moveDownItem.setImage(InternalImage.DOWN.getImage());
			try {
				moveDownItem.setImage(SicsBatchUIUtils.getBatchEditorImage("DOWN"));
			} catch (FileNotFoundException e2) {
				LoggerFactory.getLogger(this.getClass()).error("can not find ADD image", e2);
			}
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
			for (ISicsCommandElement command : getDataModel().getCommands()) {
				Composite commandArea = getToolkit().createComposite(parent);
				try {
					// Create custom view
					ISicsCommandView<? extends ISicsCommandElement> commandView = 
						SicsBatchUIUtils.createCommandView(command);
					commandView.setTaskView(this);
					addCommandUI(commandArea, command, commandView);
				} catch (ObjectCreateException e) {
					createErrorUI(commandArea, SicsCommandType.getType(command.getClass()));
					logger.error("Failed to add command UI.", e);
				}
			}
		}
		
		/*********************************************************************
		 * Command modification methods
		 *********************************************************************/
		
		private void removeCommandUI(Composite commandArea, ISicsCommandElement command) {
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
			int commandIndex = getDataModel().indexOf((ISicsCommandElement) commandArea.getData());
			getDataModel().removeCommand((ISicsCommandElement) commandArea.getData());
			// Creat new UI
			addCommandUI(commandArea, type, commandIndex);

		}
		
		private void moveCommandUp(Composite commandArea, ISicsCommandElement command) {
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
		
		private void moveCommandDown(Composite commandArea, ISicsCommandElement command) {
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
			super.dispose();
		}
		
	}
	
	public void addPropertyChangeListener(ITaskPropertyChangeListener listener){
		taskPropertyChangeListeners.add(listener);
	}
	
	public void removePropertyChangeListener(ITaskPropertyChangeListener listener){
		taskPropertyChangeListeners.remove(listener);
	}
	
	public void notifyPropertyChanged(ISicsCommandElement command, PropertyChangeEvent event){
		for (ITaskPropertyChangeListener listener : taskPropertyChangeListeners){
			listener.propertyChanged(command, event);
		}
	}
	
	public void clearPropertyChangeListeners(){
		taskPropertyChangeListeners.clear();
	}

	public String getTitle(){
		String title = "Sics Command";
		ISicsCommandElement[] commands = getDataModel().getCommands();
		if (commands == null || commands.length == 0)
			return title;
		else {
			String description = commands[0].toScript() + (commands.length > 1 ? " ..." : "");
			if (description.contains("\n"))
				description = description.substring(0, description.indexOf("\n") - 1) + " ...";
			if (description.length() > 34)
				description = description.substring(0, 30);
			return title +  " {" + description + "}";
		}
	}
	
//	@Override
//	public String getLabel() {
//		return getTitle();
//	}
}
