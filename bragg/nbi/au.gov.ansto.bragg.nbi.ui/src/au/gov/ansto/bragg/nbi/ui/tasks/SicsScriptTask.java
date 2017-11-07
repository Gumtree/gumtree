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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.gumtree.core.object.ObjectCreateException;
import org.gumtree.gumnix.sics.batch.ui.CommandBlockTask;
import org.gumtree.gumnix.sics.batch.ui.model.ISicsCommandElement;
import org.gumtree.gumnix.sics.batch.ui.model.SicsCommandType;
import org.gumtree.gumnix.sics.batch.ui.util.SicsBatchUIUtils;
import org.gumtree.gumnix.sics.batch.ui.util.SicsCommandFactory;
import org.gumtree.gumnix.sics.batch.ui.views.ISicsCommandView;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.models.AbstractModelObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.nbi.ui.tasks.AbstractScanTask.ITaskPropertyChangeListener;




public class SicsScriptTask extends CommandBlockTask {
	private static final Logger logger = LoggerFactory.getLogger(CommandBlockTask.class);
	private List<ITaskPropertyChangeListener> taskPropertyChangeListeners = 
		new ArrayList<ITaskPropertyChangeListener>();
	private static final String PARAM_EDITABLE = "editable";
	public static final String TITLE = "Sics Script";

	protected ITaskView createViewInstance() {
		return new CommandBlockTaskView();
	}
	
	private class CommandBlockTaskView extends AbstractTaskView {

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
			 * Block status area
			 *****************************************************************/
			Composite statusArea = getToolkit().createComposite(parent);
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(statusArea);
			StackLayout stackLayout = new StackLayout();
			statusArea.setLayout(stackLayout);
//			Label statusText = getToolkit().createLabel(statusArea, "");
//			stackLayout.topControl = statusText;
//			if (isEditable()) {
//				statusText.setText("Press + to add command");
//			}
			
			
			/*****************************************************************
			 * Commands area
			 *****************************************************************/
			commandsArea = getToolkit().createComposite(parent);
			GridLayoutFactory.fillDefaults().margins(0, 0).spacing(1, 1).applyTo(commandsArea);
			GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(commandsArea);
			// We use this to minimise the initial size of block UI
//			Label separator = getToolkit().createLabel(commandsArea, "", SWT.SEPARATOR | SWT.HORIZONTAL);
//			GridDataFactory.fillDefaults().grab(true, false).applyTo(separator);
			
			/*****************************************************************
			 * Button menu
			 *****************************************************************/
//			final Menu menu = createCreationMenu(addButton, commandsArea);
			
//			if (addButton != null) {
//				addButton.addSelectionListener(new SelectionAdapter() {
//					public void widgetSelected(SelectionEvent e) {
////						menu.setVisible(true);
////						Point location = addButton.toDisplay(addButton.getLocation());
////						menu.setLocation(location.x, location.y + addButton.getSize().y);
//						
//						Composite commandArea = getToolkit().createComposite(commandsArea);
//						addCommandUI(commandArea, SicsCommandType.DRIVABLE, -1);
//					}
//				});
//			}
			Composite innerArea = getToolkit().createComposite(commandsArea);
			addCommandUI(innerArea, SicsCommandType.SCRIPT, -1);
			
			/*****************************************************************
			 * UI reconstruction
			 *****************************************************************/
//			reconstructUI(commandsArea);
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
//			SicsCommandType type = SicsCommandType.getType(command.getClass()); 
			
			/*****************************************************************
			 * Label and menu creation
			 *****************************************************************/
			// Create label
//			final CLabel label = new CLabel(parent, SWT.SHADOW_ETCHED_OUT);
//			label.setText(" " + type.getLabel() + ": ");
//			try {
//				label.setImage(SicsBatchUIUtils.getBatchEditorImage("MENU"));
//			} catch (FileNotFoundException e2) {
//				LoggerFactory.getLogger(this.getClass()).error("can not find ADD image", e2);
//			}
//			label.setFont(new Font(label.getFont().getDevice(), new FontData[]{new FontData("Courier New", 9, SWT.BOLD)}));
//			label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
//			GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).indent(0, 3).hint(120, SWT.DEFAULT).applyTo(label);
//			// Create menu
//			final Menu menu = createMenu(label, command, parent);
//			// Show mouse pointer
////			if (isEditable()) {
////				label.setCursor(handCursor);
////			}
//			// Show menu
//			label.addMouseListener(new MouseAdapter() {
//				@Override
//				public void mouseDown(MouseEvent e) {
//					if (!isEditable()) {
//						return;
//					}
//					menu.setVisible(true);
//					Point location = label.toDisplay(label.getLocation());
//					menu.setLocation(location.x, location.y + label.getSize().y);
//				}
//			});
			
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

		
		private void createErrorUI(Composite parent, SicsCommandType type) {
			parent.setLayout(new GridLayout());
			getToolkit().createLabel(parent, "Failed to create UI for command type: " + type.getLabel());
			// Update UI
			fireRefresh();
		}

		// Reconstruct the whole UI
//		private void reconstructUI(Composite parent) {
//			for (ISicsCommandElement command : getDataModel().getCommands()) {
//				Composite commandArea = getToolkit().createComposite(parent);
//				try {
//					// Create custom view
//					ISicsCommandView<? extends ISicsCommandElement> commandView = 
//						SicsBatchUIUtils.createCommandView(command);
//					commandView.setTaskView(this);
//					addCommandUI(commandArea, command, commandView);
//				} catch (ObjectCreateException e) {
//					createErrorUI(commandArea, SicsCommandType.getType(command.getClass()));
//					logger.error("Failed to add command UI.", e);
//				}
//			}
//		}
		
		
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
//		String title = "Sics Command";
//		ISicsCommandElement[] commands = getDataModel().getCommands();
//		if (commands == null || commands.length == 0)
//			return title;
//		else {
//			String description = commands[0].toScript() + (commands.length > 1 ? " ..." : "");
//			if (description.contains("\n"))
//				description = description.substring(0, description.indexOf("\n") - 1) + " ...";
//			if (description.length() > 34)
//				description = description.substring(0, 30);
//			return title +  " {" + description + "}";
//		}
		return TITLE;
	}
	@Override
	public String getLabel() {
		return getTitle();
	}
//	@Override
//	public String getLabel() {
//		return getTitle();
//	}
}
