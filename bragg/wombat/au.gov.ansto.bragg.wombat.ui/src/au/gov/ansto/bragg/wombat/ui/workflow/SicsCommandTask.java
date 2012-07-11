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

package au.gov.ansto.bragg.wombat.ui.workflow;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.workflow.ui.AbstractTask;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.WorkflowException;

import au.gov.ansto.bragg.wombat.sics.ICommandArg;
import au.gov.ansto.bragg.wombat.sics.IUserCommand;
import au.gov.ansto.bragg.wombat.sics.IUserCommandManager;
import au.gov.ansto.bragg.wombat.ui.internal.InternalImage;

public class SicsCommandTask extends AbstractTask {

	private ITaskView taskView;
	@Override
	protected Object createModelInstance() {
		return null;
	}

	public String getScripts(){
		return taskView.toString();
	}
	
	@Override
	protected ITaskView createViewInstance() {
		taskView = new SicsCommandTaskView();
		return taskView;
	}

	@Override
	protected Object run(Object input) throws WorkflowException {
		return null;
	}

	private class SicsCommandTaskView extends AbstractTaskView {

		private Composite topParent;
		private List<SectionContext> sections = new ArrayList<SectionContext>();
		
		public void createPartControl(Composite parent) {
			topParent = parent;
			parent.setLayout(new GridLayout());
			
			Composite commandSection = getToolkit().createComposite(parent);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(commandSection);
			createNewCommandSection(commandSection);
			
//			Label separator = getToolkit().createSeparator(parent, SWT.HORIZONTAL);
//			GridDataFactory.fillDefaults().grab(true, false).applyTo(separator);
//			
//			commandSection = getToolkit().createComposite(parent);
//			GridDataFactory.fillDefaults().grab(true, false).applyTo(commandSection);
//			createNewCommandSection(commandSection);
			
			// Button section
			Composite buttonSection = getToolkit().createComposite(parent);
			createButtonSection(buttonSection);
		}
		
		private void createButtonSection(Composite parent) {
			parent.setLayout(new GridLayout(4, false));
			Button addButton = getToolkit().createButton(parent, "", SWT.PUSH);
			addButton.setImage(InternalImage.ADD.getImage());
			addButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					Label separator = getToolkit().createSeparator(topParent, SWT.HORIZONTAL);
					GridDataFactory.fillDefaults().grab(true, false).applyTo(separator);
					
					Composite commandSection = getToolkit().createComposite(topParent);
					GridDataFactory.fillDefaults().grab(true, false).applyTo(commandSection);
					createNewCommandSection(commandSection);
				}
			});
			
			Button removeButton = getToolkit().createButton(parent, "", SWT.PUSH);
			removeButton.setImage(InternalImage.REMOVE.getImage());
			
			Button upButton = getToolkit().createButton(parent, "", SWT.PUSH);
			upButton.setImage(InternalImage.UP.getImage());
			
			Button downButton = getToolkit().createButton(parent, "", SWT.PUSH);
			downButton.setImage(InternalImage.DOWN.getImage());
		}
		
		private void createNewCommandSection(Composite parent) {
//			parent.setLayout(new GridLayout(2, false));
//			Composite cmdHolder = getToolkit().createComposite(parent);
//			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.TOP).applyTo(cmdHolder);
//			cmdHolder.setLayout(new GridLayout());
//			Composite cmdBlock = getToolkit().createComposite(cmdHolder);
//			cmdBlock.setLayout(new GridLayout());
//			Label label = getToolkit().createLabel(cmdBlock, "Command");
//			ComboViewer comboViewer = new ComboViewer(cmdBlock, SWT.READ_ONLY);
//			comboViewer.setContentProvider(new ArrayContentProvider());
//			comboViewer.setLabelProvider(new LabelProvider() {
//				public String getText(Object element) {
//					if (element instanceof IUserCommand) {
//						return ((IUserCommand) element).getId();
//					}
//					return "";
//				}
//			});
//			final Composite argArea = getToolkit().createComposite(parent);
//			argArea.setLayout(new GridLayout(3, true));
//			GridDataFactory.fillDefaults().grab(true, false).applyTo(argArea);
//			
//			final IUserCommandManager cmdManager = GTPlatform.getServiceManager().getService(IUserCommandManager.class);
//			comboViewer.setInput(cmdManager.getUserCommands());
//			comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
//				public void selectionChanged(SelectionChangedEvent event) {
//					Object selection = ((IStructuredSelection) event.getSelection()).getFirstElement();
//					if (selection instanceof IUserCommand) {
//						IUserCommand userCommand = (IUserCommand) selection;
//						// if != current selection
//						for (Control child : argArea.getChildren()) {
//							child.dispose();
//						}
//						for (ICommandArg arg : userCommand.getCommandArgs()) {
//							Composite argHolder = getToolkit().createComposite(argArea);
//							argHolder.setLayout(new GridLayout());
//							getToolkit().createLabel(argHolder, arg.getId());
//							getToolkit().createText(argHolder, "");
//						}
//						topParent.getParent().getParent().layout(true, true);
//					}
//				}
//			});
//			Object defaultObject = comboViewer.getElementAt(0);
//			if (defaultObject != null) {
//				comboViewer.setSelection(new StructuredSelection(defaultObject));
//			}
			sections.add(new SectionContext(parent, getToolkit()));
		}
		
		public String toString(){
			String sourceCode = "";
			for (SectionContext section : sections){
				String command = section.getCommand();
				if (command != null)
					sourceCode += command + "\n";
			}
			return sourceCode;
		}
	
		class SectionContext {
			ComboViewer comboViewer;
			List<Text> argTextList;

			public SectionContext(final Composite parent, final FormToolkit toolKit) {
				parent.setLayout(new GridLayout(2, false));
				Composite cmdHolder = toolKit.createComposite(parent);
				GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.TOP).applyTo(cmdHolder);
				cmdHolder.setLayout(new GridLayout());
				Composite cmdBlock = toolKit.createComposite(cmdHolder);
				cmdBlock.setLayout(new GridLayout());
				Label label = toolKit.createLabel(cmdBlock, "Command");
				comboViewer = new ComboViewer(cmdBlock, SWT.READ_ONLY);
				comboViewer.setContentProvider(new ArrayContentProvider());
				comboViewer.setLabelProvider(new LabelProvider() {
					public String getText(Object element) {
						if (element instanceof IUserCommand) {
							return ((IUserCommand) element).getId();
						}
						return "";
					}
				});
				final Composite argArea = toolKit.createComposite(parent);
				argArea.setLayout(new GridLayout(3, true));
				GridDataFactory.fillDefaults().grab(true, false).applyTo(argArea);

				final IUserCommandManager cmdManager = ServiceUtils.getService(IUserCommandManager.class);
				comboViewer.setInput(cmdManager.getUserCommands());
				comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						Object selection = ((IStructuredSelection) event.getSelection()).getFirstElement();
						if (selection instanceof IUserCommand) {
							IUserCommand userCommand = (IUserCommand) selection;
							// if != current selection
							for (Control child : argArea.getChildren()) {
								child.dispose();
							}
							argTextList = new ArrayList<Text>();
							for (ICommandArg arg : userCommand.getCommandArgs()) {
								Composite argHolder = toolKit.createComposite(argArea);
								argHolder.setLayout(new GridLayout());
								toolKit.createLabel(argHolder, arg.getId());
								argTextList.add(toolKit.createText(argHolder, ""));
							}
							topParent.getParent().getParent().layout(true, true);
						}
					}
				});
				Object defaultObject = comboViewer.getElementAt(0);
				if (defaultObject != null) {
					comboViewer.setSelection(new StructuredSelection(defaultObject));
				}
			}

			public String getCommand(){
				String command = null;
				Object selectedItem = comboViewer.getElementAt(comboViewer.getCombo().getSelectionIndex());
				if (selectedItem != null)
					if (selectedItem instanceof IUserCommand){
						IUserCommand userCommand = (IUserCommand) selectedItem;
						command = userCommand.getId();
						for (Text textBox : argTextList)
						command += " " + textBox.getText();
				}
				return command;
			}
		}
	}

	public Class<?>[] getInputTypes() {
		return null;
	}

	public Class<?>[] getOutputTypes() {
		return null;
	}
	
}
