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
package au.gov.ansto.bragg.kowari.ui.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.gumtree.gumnix.sics.batch.ui.model.ISicsCommandElement;

import au.gov.ansto.bragg.nbi.ui.core.commands.AbstractScanParameter;
import au.gov.ansto.bragg.nbi.ui.core.commands.SimpleTableScanCommand;
import au.gov.ansto.bragg.nbi.ui.core.commands.TableScanParameter;
import au.gov.ansto.bragg.nbi.ui.tasks.SimpleTableScanTask;


/**
 * @author nxi
 * Created on 11/08/2009
 */
public class KowariTableScanTask extends SimpleTableScanTask {

	private final static String[] FOUR_COLUMN_NAMES = new String[]{"sx", "sy", "sz", "som", "time"};
	private final static String[] SEVEN_COLUMN_NAMES = new String[]{"sx", "sy", "sz", "som", "ga", "gb", "gc", "time"};
	
	private int numberOfMotors = 4;
	
	@Override
	public String getTitle() {
		return (numberOfMotors + 1) + "-Column Table Scan";
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
				//				for (int i = 0; i < tableCommand.getNumberOfMotor(); i ++){
				//					text += tableCommand.getPNames().get(i) + ",\t";
				//				}
				//				text += tableCommand.getScan_mode() + "\n";
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

	@Override
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
						if (lineCount == 0 && SimpleTableScanTask.containsLetter(line)) {
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
							lineCount ++;
							this.numberOfMotors = numberOfMotors;
							continue;
						} else if (lineCount < 2) {
							if (lineCount == 0) {
								command.setScan_mode("time");
								if (items.length == 4 || items.length == 5) {
									numberOfMotors = 4;
									command.setPNames(Arrays.asList(FOUR_COLUMN_NAMES));
								} else if (items.length == 7 || items.length == 8) {
									numberOfMotors = 7;
									command.setPNames(Arrays.asList(SEVEN_COLUMN_NAMES));
								}
								this.numberOfMotors = numberOfMotors;
								command.setNumberOfMotor(numberOfMotors);
							}
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
}
