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
package au.gov.ansto.bragg.nbi.ui.tasks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

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
import au.gov.ansto.bragg.nbi.ui.core.commands.ScanNDCommand;
import au.gov.ansto.bragg.nbi.ui.core.commands.SimpleScanCommandView;
import au.gov.ansto.bragg.nbi.ui.core.commands.SimpleTableScanCommand;
import au.gov.ansto.bragg.nbi.ui.core.commands.TableScanParameter;

/**
 * @author nxi
 * Created on 11/08/2009
 */
public class SimpleTableScanTask extends AbstractScanTask {

	public final static String TASK_TITLE = "Arbitary Table Scan";
	protected static String fileDialogPath;

	/**
	 * 
	 */
	public SimpleTableScanTask() {
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
		int size = 0;
		ISicsCommandElement[] commands = getDataModel().getCommands();
		if (commands.length > 0) {
			ISicsCommandElement command = commands[0];
			size = ((SimpleTableScanCommand) command).getSelectedParameterCount();
		}
		return getTitle() + " " + (size > 0 ? "in " + size + " step" + (size > 1 ? "s" : "") : "") + 
				" (" + getEstimatedTime() + " " + getTimeUnits() + ")";
	}
	
	protected ITaskView createViewInstance() {
		return new SimpleTableScanTaskView();
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
			if (command instanceof SimpleTableScanCommand){
				SimpleTableScanCommand tableCommand = (SimpleTableScanCommand) command;
				FileWriter outputfile = new FileWriter(pickedFile);
				String text = "";
				for (int i = 0; i < tableCommand.getNumberOfMotor(); i ++){
					text += tableCommand.getPNames().get(i) + ",\t";
				}
				text += tableCommand.getScan_mode() + "\n";
				if (tableCommand.getNumberOfMotor() >= 1) {
					text += (tableCommand.getColumn0() ? "1" : "0") + ",\t";
				}
				if (tableCommand.getNumberOfMotor() >= 2) {
					text += (tableCommand.getColumn1() ? "1" : "0") + ",\t";
				}
				if (tableCommand.getNumberOfMotor() >= 3) {
					text += (tableCommand.getColumn2() ? "1" : "0") + ",\t";
				}
				if (tableCommand.getNumberOfMotor() >= 4) {
					text += (tableCommand.getColumn3() ? "1" : "0") + ",\t";
				}
				if (tableCommand.getNumberOfMotor() >= 5) {
					text += (tableCommand.getColumn4() ? "1" : "0") + ",\t";
				}
				if (tableCommand.getNumberOfMotor() >= 6) {
					text += (tableCommand.getColumn5() ? "1" : "0") + ",\t";
				}
				if (tableCommand.getNumberOfMotor() >= 7) {
					text += (tableCommand.getColumn6() ? "1" : "0") + ",\t";
				}
				if (tableCommand.getNumberOfMotor() >= 8) {
					text += (tableCommand.getColumn7() ? "1" : "0") + ",\t";
				}
				if (tableCommand.getNumberOfMotor() >= 9) {
					text += (tableCommand.getColumn8() ? "1" : "0") + ",\t";
				}
				if (tableCommand.getNumberOfMotor() >= 10) {
					text += (tableCommand.getColumn9() ? "1" : "0") + ",\t";
				}
				text += "\n";
				for (AbstractScanParameter parameter : tableCommand.getParameterList()){
					if (parameter instanceof TableScanParameter) {
						TableScanParameter tableParameter = (TableScanParameter) parameter;
						for (int i = 0; i < tableCommand.getNumberOfMotor(); i ++) {
							text += tableParameter.getP(i) + ",\t";
						}
						text += Float.valueOf(tableParameter.getPreset()) + "\n";
					}
				}
				outputfile.write(text);
				outputfile.close();
			}
		}
	}

	protected SimpleTableScanCommand loadTableCommand(final Composite parent, final Label label) {
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
				SimpleTableScanCommand command = new SimpleTableScanCommand();
				reader = new BufferedReader(new FileReader(pickedFile));
				int lineCount = 0;
				int parameterIndex = 0;
				int numberOfMotors = 0;
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
						if (lineCount == 0) {
							if (!containsLetter(line)) {
								MessageDialog.openError(parent.getShell(), "Error Loading File", "Failed to load file " 
										+ filePath + ". Please check the file format.");
								reader.close();
								return null;
							}
							for (int i = 0; i < items.length; i++) {
								items[i] = items[i].trim();
							}
							numberOfMotors = items.length - 1;
							String lastItme = items[items.length - 1].toLowerCase();
							if (lastItme.startsWith("monitor_")) {
								command.setScan_mode(lastItme.toUpperCase());
							} else if (lastItme.equals("time") || lastItme.equals("unlimited") || 
									lastItme.equals("count") || lastItme.equals("frame") || 
									lastItme.equals("period") || lastItme.equals("count_roi") ) {
								command.setScan_mode(lastItme);
							} else {
								command.setScan_mode("time");
								numberOfMotors = items.length;
								newItems.add("time");
							}
							command.setPNames(newItems);
							command.setNumberOfMotor(numberOfMotors);
							lineCount++;
							continue;
						} else if (lineCount == 1) {
							boolean isBoolean = true;
							for (int i = 0; i < items.length; i++) {
								String item = items[i].trim().toLowerCase();
								if (!item.equals("1") && !item.equals("0") && !item.equals("true") 
										&& !item.equals("false")){
									isBoolean = false;
								} else if (item.equals("1")) {
									items[i] = "true";
								} else if (item.equals("0")) {
									items[i] = "false";
								}
							}
							if (isBoolean) {
								try{
									if (numberOfMotors > 0) {
										command.setColumn0(Boolean.valueOf(items[0].trim().toLowerCase()));
									}
									if (numberOfMotors > 1) {
										command.setColumn1(Boolean.valueOf(items[1].trim().toLowerCase()));
									}
									if (numberOfMotors > 2) {
										command.setColumn2(Boolean.valueOf(items[2].trim().toLowerCase()));
									}
									if (numberOfMotors > 3) {
										command.setColumn3(Boolean.valueOf(items[3].trim().toLowerCase()));
									}
									if (numberOfMotors > 4) {
										command.setColumn4(Boolean.valueOf(items[4].trim().toLowerCase()));
									}
									if (numberOfMotors > 5) {
										command.setColumn5(Boolean.valueOf(items[5].trim().toLowerCase()));
									}
									if (numberOfMotors > 6) {
										command.setColumn6(Boolean.valueOf(items[6].trim().toLowerCase()));
									}
									if (numberOfMotors > 7) {
										command.setColumn7(Boolean.valueOf(items[7].trim().toLowerCase()));
									}
									if (numberOfMotors > 8) {
										command.setColumn8(Boolean.valueOf(items[8].trim().toLowerCase()));
									}
									if (numberOfMotors > 9) {
										command.setColumn9(Boolean.valueOf(items[9].trim().toLowerCase()));
									}
								} catch (Exception e) {
								}
								lineCount++;
								continue;
							}
						}
						TableScanParameter parameter = new TableScanParameter();
						parameterIndex ++;
						parameter.setIndex(parameterIndex);
						try {
							if (numberOfMotors > 0) {
								parameter.setP0(Float.valueOf(items[0]));
							}
							if (numberOfMotors > 1) {
								parameter.setP1(Float.valueOf(items[1]));
							}
							if (numberOfMotors > 2) {
								parameter.setP2(Float.valueOf(items[2]));
							}
							if (numberOfMotors > 3) {
								parameter.setP3(Float.valueOf(items[3]));
							}
							if (numberOfMotors > 4) {
								parameter.setP4(Float.valueOf(items[4]));
							}
							if (numberOfMotors > 5) {
								parameter.setP5(Float.valueOf(items[5]));
							}
							if (numberOfMotors > 6) {
								parameter.setP6(Float.valueOf(items[6]));
							}
							if (numberOfMotors > 7) {
								parameter.setP7(Float.valueOf(items[7]));
							}
							if (numberOfMotors > 8) {
								parameter.setP8(Float.valueOf(items[8]));
							}
							if (numberOfMotors > 9) {
								parameter.setP9(Float.valueOf(items[9]));
							}
							parameter.setPreset(Float.valueOf(items[items.length - 1]));
							parameter.setIsSelected(true);
						} catch (Exception e) {
							reader.close();
							throw new Exception("failed to interpret the line: " + line);
						}
						parameter.setCommand(command);
						parameter.setLength(numberOfMotors);
						parameter.setPNames(command.getPNames());
						command.getParameterList().add(parameter);
					}
					lineCount++;
				}
				String filePathString;
				int FILE_NAME_LENGTH = 60;
				if (filePath.length() > FILE_NAME_LENGTH) {
					String spliter = "\\";
					if (filePath.contains("/")) {
						spliter = "/";
					}
					int spliterIndex = filePath.indexOf(spliter, 3);
					if (spliterIndex > 0 && spliterIndex < 50) {
						spliterIndex ++;
						filePathString = filePath.substring(0, spliterIndex) + "..." + filePath.subSequence(filePath.length() - FILE_NAME_LENGTH + spliterIndex, filePath.length());
					} else {
						filePathString = filePath.substring(0, 3) + "..." + filePath.subSequence(filePath.length() - FILE_NAME_LENGTH + 3, filePath.length());;
					}
				} else {
					filePathString = filePath;
				}
				label.setText(filePathString);
				label.setToolTipText(filePath);
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
				MessageDialog.openError(parent.getShell(), "Error", "Failed to load command: " 
						+ error.getLocalizedMessage());
			}
		}
		return null;
	}
	
	private class SimpleTableScanTaskView extends AbstractTaskView{

		private Composite parent;
		private Composite commandArea;
		private Button singleFileRadio;
		@Override
		public void createPartControl(Composite parent) {
			this.parent = parent;
			GridLayoutFactory.swtDefaults().applyTo(parent);
			createTaskUI(parent);
		}

		private void createTaskUI(Composite parent) {
			addLoadButton(parent);
			addSaveButton(parent);
//			createLabelArea(parent);
//			Composite commandComposite = getToolkit().createComposite(parent);
//			GridDataFactory.fillDefaults().grab(true, false).applyTo(commandComposite);
			for (ISicsCommandElement command : getDataModel().getCommands()){
				if (command instanceof SimpleTableScanCommand){
					commandArea = getToolkit().createComposite(parent);
					createCommandUI(commandArea, (SimpleTableScanCommand) command);
//					addCommandListener(command);
				}
			}
			fireRefresh();
		}

		private void addSaveButton(final Composite composite) {
			Composite controlComposite = getToolkit().createComposite(composite);
			GridLayoutFactory.fillDefaults().numColumns(2).applyTo(controlComposite);
			GridDataFactory.fillDefaults().applyTo(controlComposite);
			Button loadButton = getToolkit().createButton(controlComposite, "Save Copy", SWT.PUSH);
			final Label fileLable = getToolkit().createLabel(controlComposite, "");
			GridDataFactory.fillDefaults().applyTo(loadButton);
			GridDataFactory.fillDefaults().indent(0, 4).applyTo(fileLable);
			loadButton.addSelectionListener(new SelectionAdapter() {
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

		private void addLoadButton(final Composite composite) {
			Composite controlComposite = getToolkit().createComposite(composite);
			GridLayoutFactory.fillDefaults().numColumns(2).applyTo(controlComposite);
			GridDataFactory.fillDefaults().applyTo(controlComposite);
			Button loadButton = getToolkit().createButton(controlComposite, "Load Scan Table from File", SWT.PUSH);
			final Label fileLable = getToolkit().createLabel(controlComposite, "");
			GridDataFactory.fillDefaults().applyTo(loadButton);
			GridDataFactory.fillDefaults().indent(0, 4).applyTo(fileLable);
			loadButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					SimpleTableScanCommand command = loadTableCommand(composite, fileLable);
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
		}


		private void createCommandUI(Composite parent, SimpleTableScanCommand command) {
			SimpleScanCommandView commandView = new SimpleScanCommandView(command);
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
						if (arg0.getPropertyName().equals("multiple files")){
							if (!singleFileRadio.isDisposed())
								singleFileRadio.setSelection(false);
						}
					}
				});
		}
		
		private boolean isSingleFile(){
			ISicsCommandElement[] commands = getDataModel().getCommands();
			if (commands.length > 0){
				return ((ScanNDCommand) commands[0]).isSingleFile();
			}
			return false;
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
		return new SimpleTableScanTask();
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
