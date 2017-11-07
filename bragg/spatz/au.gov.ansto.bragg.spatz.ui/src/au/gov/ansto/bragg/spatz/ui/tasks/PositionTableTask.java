/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
*******************************************************************************/
package au.gov.ansto.bragg.spatz.ui.tasks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.gumtree.gumnix.sics.batch.ui.model.ISicsCommandElement;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITask;
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.models.AbstractModelObject;

import au.gov.ansto.bragg.nbi.ui.core.commands.AbstractScanParameter;
import au.gov.ansto.bragg.nbi.ui.tasks.AbstractScanTask;

/**
 * @author nxi
 * Created on 11/08/2009
 */
public class PositionTableTask extends AbstractScanTask {

	public final static String TASK_TITLE = "Position Panel";
	private final static String PREF_POSITION_TABLE = "spatz.positionTable";
	protected static String fileDialogPath;

	/**
	 * 
	 */
	public PositionTableTask() {
		super();
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kowari.exp.task.AbstractKowariScanTask#getTitle()
	 */
	@Override
	public String getTitle() {
		return TASK_TITLE;
	} 

	@Override
	public String getLabel() {
		return getTitle();
	}
	
	protected ITaskView createViewInstance() {
		return new PositionTableTaskView();
	}

	public void savePreference() {
		String value = "";
		for (ISicsCommandElement command : getDataModel().getCommands()){
			if (command instanceof PositionCommand){
				value += ((PositionCommand) command).getPrintable().replace("\n", "//");
			}
		}
		TaskUtils.setPreference(PREF_POSITION_TABLE, value);
		TaskUtils.savePreferenceStore();
	}
	
	public void loadPreference() {
		String value = TaskUtils.getPreference(PREF_POSITION_TABLE);
		if (value == null || value.trim().length() == 0) {
			return;
		}
		String[] lines = value.split("//");
		try {
			PositionCommand command = new PositionCommand();
			int parameterIndex = 0;
			for (String line : lines) {
				if (line != null) {
					line = line.trim();
					if (line.startsWith("#") || line.length() == 0) {
						continue;
					}
					//	line = line.replaceAll("\t", "");
					String spliter = "\\s+";
					if (line.contains(",")) {
						spliter = ",";
					} else if (line.contains("\t")) {
						spliter = "\t";
					}
					String[] items = line.split(spliter);
					ArrayList<String> newItems = new ArrayList<String>();
					for (int i = 0; i < items.length; i ++) {
						if (items[i].trim().length() > 0){
							newItems.add(items[i].trim());
						}
					}
					items = new String[newItems.size()];
					newItems.toArray(items);
					PositionParameter parameter = new PositionParameter();
					parameterIndex ++;
					parameter.setPosition(parameterIndex);
					parameter.setSx(Float.valueOf(items[1]));
					parameter.setSz(Float.valueOf(items[2]));
					parameter.setSth(Float.valueOf(items[3]));
					parameter.setSphi(Float.valueOf(items[4]));
					parameter.setSamplename(String.valueOf(items[5]));
					parameter.setCommand(command);
					command.insertParameter(parameter);
				}
			}
			ISicsCommandElement[] commands = getDataModel().getCommands();
			if (commands.length > 0) {
				getDataModel().removeCommand(commands[0]);
			}
			getDataModel().addCommand(command);
		} catch (Exception error) {
			error.printStackTrace();
		}
	}
	
	protected void saveTableCommand(Composite composite) throws IOException {
		FileDialog dialog = new FileDialog(composite.getShell(), SWT.SAVE);
		if (fileDialogPath == null){
			IWorkspace workspace= ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();
			dialog.setFilterPath(root.getLocation().toOSString());
		} else {
			dialog.setFilterPath(fileDialogPath);
		}
		dialog.setFilterExtensions(new String[]{"*.*"});
		dialog.open();
		if (dialog.getFileName() == null) {
			return;
		}
		String filePath = dialog.getFilterPath() + File.separator + dialog.getFileName();
		File pickedFile = new File(filePath);
		if (pickedFile.exists() && pickedFile.isDirectory())
			throw new IllegalAccessError("can not overwrite a folder");
		else if (pickedFile.exists()){
			if (!MessageDialog.openConfirm(composite.getShell(), "Confirm overwrite", "Do you want to overwrite " + filePath)) {
				return;
			}
		}
		fileDialogPath = pickedFile.getParent();
		
		for (ISicsCommandElement command : getDataModel().getCommands()){
			if (command instanceof PositionCommand){
				PositionCommand tableCommand = (PositionCommand) command;
				FileWriter outputfile = new FileWriter(pickedFile);
				String text = "";
				text = tableCommand.getPrintable();
				text += "\n";
				outputfile.write(text);
				outputfile.close();
			}
		}
	}

	protected PositionCommand loadTableCommand(final Composite parent) {
		FileDialog dialog = new FileDialog(parent.getShell(), SWT.SINGLE);
			if (fileDialogPath == null){
				IWorkspace workspace= ResourcesPlugin.getWorkspace();
				IWorkspaceRoot root = workspace.getRoot();
				dialog.setFilterPath(root.getLocation().toOSString());
			} else {
				dialog.setFilterPath(fileDialogPath);
			}
			dialog.setFilterExtensions(new String[]{"*.*"});
			dialog.open();
			if (dialog.getFileName() == null) {
				return null;
			}
		String filePath = dialog.getFilterPath() + File.separator + dialog.getFileName();
		File pickedFile = new File(filePath);
		if (!pickedFile.exists() || !pickedFile.isFile())
			return null;
		fileDialogPath = pickedFile.getParent();
		if (filePath != null) {
			BufferedReader reader = null;
			try {
				PositionCommand command = new PositionCommand();
				reader = new BufferedReader(new FileReader(pickedFile));
				int parameterIndex = 0;
				while(reader.ready()) {
					String line = reader.readLine();
					if (line != null) {
						line = line.trim();
						if (line.startsWith("#") || line.length() == 0) {
							continue;
						}
						//	line = line.replaceAll("\t", "");
						String spliter = "\\s+";
						if (line.contains(",")) {
							spliter = ",";
						} else if (line.contains("\t")) {
							spliter = "\t";
						}
						String[] items = line.split(spliter);
						ArrayList<String> newItems = new ArrayList<String>();
						for (int i = 0; i < items.length; i ++) {
							if (items[i].trim().length() > 0){
								newItems.add(items[i].trim());
							}
						}
						items = new String[newItems.size()];
						newItems.toArray(items);
						PositionParameter parameter = new PositionParameter();
						parameterIndex ++;
						parameter.setPosition(parameterIndex);
						parameter.setSx(Float.valueOf(items[1]));
						parameter.setSz(Float.valueOf(items[2]));
						parameter.setSth(Float.valueOf(items[3]));
						parameter.setSphi(Float.valueOf(items[4]));
						parameter.setSamplename(String.valueOf(items[5]));
						parameter.setCommand(command);
						command.getParameterList().add(parameter);
					}
				}
				reader.close();
				return command;
			} catch (Exception error) {
				error.printStackTrace();
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
					}
				}
				MessageDialog.openError(parent.getShell(), "Error", "Failed to load position table from file: " 
						+ error.getLocalizedMessage());
			}
		}
		return null;
	}
	
	private class PositionTableTaskView extends AbstractTaskView{

		private Composite parent;
		private Composite commandArea;
		@Override
		public void createPartControl(Composite parent) {
			this.parent = parent;
			GridLayoutFactory.swtDefaults().applyTo(parent);
			createTaskUI(parent);
		}

		private void createTaskUI(Composite parent) {
			addHeaderButton(parent);
//			addSaveButton(parent);
//			createLabelArea(parent);
//			Composite commandComposite = getToolkit().createComposite(parent);
//			GridDataFactory.fillDefaults().grab(true, false).applyTo(commandComposite);
			for (ISicsCommandElement command : getDataModel().getCommands()){
				if (command instanceof PositionCommand){
					commandArea = getToolkit().createComposite(parent);
					createCommandUI(commandArea, (PositionCommand) command);
//					addCommandListener(command);
				}
			}
			fireRefresh();
		}

		private void addHeaderButton(final Composite composite) {
			Composite controlComposite = getToolkit().createComposite(composite);
			GridLayoutFactory.fillDefaults().numColumns(2).applyTo(controlComposite);
			GridDataFactory.fillDefaults().applyTo(controlComposite);
			Button loadButton = getToolkit().createButton(controlComposite, "Load position table from file", SWT.PUSH);
			loadButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					PositionCommand command = loadTableCommand(composite);
					if (command == null) {
						return;
					}
					ISicsCommandElement[] commands = getDataModel().getCommands();
					if (commands.length > 0) {
						getDataModel().removeCommand(commands[0]);
					}
					getDataModel().addCommand(command);
					if (commandArea != null && !commandArea.isDisposed()) {
						for (Control part : commandArea.getChildren()) {
							if (!part.isDisposed()) {
								part.dispose();
							}
						}
					}
//					if (commandArea != null && !commandArea.isDisposed()) {
//						commandArea.dispose();
//					}
					if (commandArea == null) {
						commandArea = getToolkit().createComposite(composite);
					}
					createCommandUI(commandArea, command);
//					commandArea.update();
//					commandArea.layout(true, true);
					fireRefresh();
					notifyPropertyChanged(command, null);
				}
			});
			Button saveButton = getToolkit().createButton(controlComposite, "Save copy", SWT.PUSH);
			GridDataFactory.fillDefaults().applyTo(saveButton);
			saveButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					try {
						saveTableCommand(composite);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}

			});
		}

//		private void addLoadButton(final Composite composite) {
//			Composite controlComposite = getToolkit().createComposite(composite);
//			GridLayoutFactory.fillDefaults().numColumns(2).applyTo(controlComposite);
//			GridDataFactory.fillDefaults().applyTo(controlComposite);
//		}


		private void createCommandUI(Composite parent, PositionCommand command) {
			PositionCommandView commandView = new PositionCommandView(command);
			commandView.setTaskView(this);
			commandView.createPartControl(parent);
			addCommandListener(command);
		}

		
		private void addCommandListener(final ISicsCommandElement command) {
			if (command instanceof AbstractModelObject)
				((AbstractModelObject) command).addPropertyChangeListener(new PropertyChangeListener() {
					
					public void propertyChange(PropertyChangeEvent arg0) {
						notifyPropertyChanged(command, arg0);
						if (arg0.getPropertyName().equals("parameter_add")){
							Object parameter = arg0.getNewValue();
							if (parameter instanceof AbstractScanParameter)
								((AbstractScanParameter) parameter).addPropertyChangeListener(this);
						}
						if (arg0.getPropertyName().equals("parameter_remove")){
//							Object parameter = arg0.getNewValue();
//							if (!singleFileRadio.isDisposed())
//								singleFileRadio.setSelection(isSingleFile());
						}
						savePreference();
					}
				});
		}
		
		@Override
		public void fireRefresh() {
			parent.update();
			parent.getParent().layout(parent.getChildren());
			super.fireRefresh();
		}
	}

	@Override
	public ITask newThisTask() {
		return new PositionTableTask();
	}

	public static boolean containsLetter(String s) {
		if ( s == null )
			return false;
		boolean letterFound = false;
		for (int i = 0; !letterFound && i < s.length(); i++)
			letterFound = letterFound
			|| Character.isLetter(s.charAt(i));
		return letterFound;
	} 
}
