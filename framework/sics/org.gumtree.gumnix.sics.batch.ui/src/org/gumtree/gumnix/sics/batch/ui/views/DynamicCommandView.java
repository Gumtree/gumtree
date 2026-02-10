package org.gumtree.gumnix.sics.batch.ui.views;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.gumtree.gumnix.sics.batch.ui.commands.DynamicCommand;
import org.gumtree.gumnix.sics.batch.ui.definition.ArgType;
import org.gumtree.gumnix.sics.batch.ui.definition.ICommandArg;
import org.gumtree.gumnix.sics.batch.ui.definition.ICompositeCommand;
import org.gumtree.gumnix.sics.batch.ui.definition.IPlainCommand;
import org.gumtree.gumnix.sics.batch.ui.definition.ISicsCommand;
import org.gumtree.gumnix.sics.batch.ui.util.SicsBatchUIUtils;
import org.gumtree.ui.util.SafeUIRunner;

public class DynamicCommandView extends AbstractSicsCommandView<DynamicCommand> {
	
	private DataBindingContext bindingContext;
	
	private FieldDecoration errorDec;
	
	@Override
	protected void createPartControl(Composite parent, DynamicCommand command) {
		bindingContext = new DataBindingContext();
		errorDec = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
		GridLayoutFactory.fillDefaults().margins(0, 0).spacing(10, SWT.DEFAULT).numColumns(2).applyTo(parent);
		
		final ComboViewer commandComboViewer = new ComboViewer(parent, SWT.READ_ONLY);
		commandComboViewer.setContentProvider(new ArrayContentProvider());
		commandComboViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				return ((ISicsCommand) element).getName();
			}
		});
		commandComboViewer.setSorter(new ViewerSorter());
		commandComboViewer.setInput(command.getAvailableCommands());
		commandComboViewer.getCombo().setVisibleItemCount(20);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BEGINNING).hint(WIDTH_COMBO, SWT.DEFAULT).applyTo(commandComboViewer.getCombo());
		
		final Composite variableArea = getToolkit().createComposite(parent);
		GridLayoutFactory.fillDefaults().margins(0, 0).spacing(10, SWT.DEFAULT).applyTo(variableArea);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(variableArea);
		
		commandComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISicsCommand selectedCommand = (ISicsCommand) ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (selectedCommand instanceof ICompositeCommand) {
					creatSubCommandArea(variableArea, (ICompositeCommand) selectedCommand);
				} else if (selectedCommand instanceof IPlainCommand) {
					creatArgumentArea(variableArea, (IPlainCommand) selectedCommand);
				}
				updateUI();
			}
		});
		
		/*********************************************************************
		 * Data binding
		 *********************************************************************/
		Realm.runWithDefault(DisplayRealm.getRealm(Display.getDefault()), new Runnable() {

			public void run() {
				bindingContext.bindValue(
						ViewerProperties.singleSelection().observe(commandComboViewer),
						BeanProperties.value("selectedCommand").observe(getCommand()),
						new UpdateValueStrategy(),
						new UpdateValueStrategy()
				);
				
				/*********************************************************************
				 * Default selection
				 *********************************************************************/
				if (getCommand().getSelectedCommand() == null) {
					if (commandComboViewer.getCombo().getItemCount() > 0) {
						commandComboViewer.setSelection(
								new StructuredSelection(commandComboViewer.getElementAt(0)));
					}
				} else {
					 commandComboViewer.setSelection(new StructuredSelection(getCommand().getSelectedCommand()));
				}
			}
			
		});
	}
	
	private void creatSubCommandArea(Composite parent, final ICompositeCommand command) {
		// Dispose
		for (Control child : parent.getChildren()) {
			child.dispose();
		}
		parent.setMenu(new Menu(parent));
		
		// Relayout
		GridLayoutFactory.fillDefaults().margins(0, 0).spacing(10, SWT.DEFAULT).numColumns(2).applyTo(parent);
		
		//
		final ComboViewer subCommandComboViewer = new ComboViewer(parent, SWT.READ_ONLY);
		subCommandComboViewer.setContentProvider(new ArrayContentProvider());
		subCommandComboViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				return ((IPlainCommand) element).getName();
			}
		});
		subCommandComboViewer.setSorter(new ViewerSorter());
		subCommandComboViewer.setInput(command.getSubCommands());
		subCommandComboViewer.getCombo().setVisibleItemCount(20);
		GridDataFactory.swtDefaults().hint(WIDTH_COMBO, SWT.DEFAULT).applyTo(subCommandComboViewer.getCombo());
		
		final Composite argumentArea = getToolkit().createComposite(parent);
		GridLayoutFactory.fillDefaults().margins(0, 0).spacing(10, SWT.DEFAULT).applyTo(argumentArea);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(argumentArea);
		
		subCommandComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IPlainCommand selectedCommand = (IPlainCommand) ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (selectedCommand != null) {
					// Creates argument area
					creatArgumentArea(argumentArea, (IPlainCommand) selectedCommand);
					// Updates UI
					updateUI();
				}
			}
		});
		
		/*********************************************************************
		 * Data binding
		 *********************************************************************/
		Realm.runWithDefault(DisplayRealm.getRealm(Display.getDefault()), new Runnable() {
			public void run() {
				bindingContext.bindValue(
						ViewerProperties.singleSelection().observe(subCommandComboViewer),
						BeanProperties.value("selectedSubCommand").observe(command),
						new UpdateValueStrategy(),
						new UpdateValueStrategy()
				);
				
				/*********************************************************************
				 * Default selection
				 *********************************************************************/
				if (command.getSelectedSubCommand() == null) {
					if (subCommandComboViewer.getCombo().getItemCount() > 0) {
						subCommandComboViewer.setSelection(
								new StructuredSelection(subCommandComboViewer.getElementAt(0)));
					}
				}
			}
			
		});
	}
	
	private void creatArgumentArea(final Composite parent, final IPlainCommand command) {
		// Dispose
		for (Control child : parent.getChildren()) {
			child.dispose();
		}
		// Special case: add content to keep parent composite non-empty
		if (command.getArguments().length == 0) {
			GridLayoutFactory.fillDefaults().margins(0, 0).spacing(10, SWT.DEFAULT).numColumns(1).applyTo(parent);
			getToolkit().createLabel(parent, "");
		} else {
			GridLayoutFactory.fillDefaults().margins(0, 0).spacing(10, SWT.DEFAULT).numColumns(3).applyTo(parent);
		}
		/*********************************************************************
		 * Data binding
		 *********************************************************************/
		Realm.runWithDefault(DisplayRealm.getRealm(Display.getDefault()), new Runnable() {
			public void run() {
				for (final ICommandArg arg : command.getArguments()) {
					if (arg.getType().equals(ArgType.DRIVABLE)) {
						// Drivable case:
						final ComboViewer drivableComboViewer = new ComboViewer(parent, SWT.READ_ONLY);
						drivableComboViewer.setContentProvider(new ArrayContentProvider());
						drivableComboViewer.setLabelProvider(new LabelProvider());
						drivableComboViewer.setSorter(new ViewerSorter());
						drivableComboViewer.setInput(SicsBatchUIUtils.getSicsDrivableIds());
						
						/*****************************************************
						 * Data binding
						 *****************************************************/
						bindingContext.bindValue(
								ViewerProperties.singleSelection().observe(drivableComboViewer),
								BeanProperties.value("value").observe(arg),
								new UpdateValueStrategy(),
								new UpdateValueStrategy()
						);
						
						/*****************************************************
						 * Default selection
						 *****************************************************/
						if (arg.getValue() == null) {
							if (drivableComboViewer.getCombo().getItemCount() > 0) {
								drivableComboViewer.setSelection(new StructuredSelection(
										drivableComboViewer.getElementAt(drivableComboViewer.getCombo().getItemCount() - 1)));
							}
						}
					} else {
						// Other cases:
						final Text argText = getToolkit().createText(parent, "", SWT.BORDER);
						if (arg.getValue() != null) {
							argText.setText(arg.getValue().toString());
						}
						argText.setToolTipText(arg.getName());
						GridDataFactory.swtDefaults().hint(WIDTH_PARAMETER, SWT.DEFAULT).applyTo(argText);
					
						/*****************************************************
						 * Validation
						 *****************************************************/
						final ControlDecoration controlDec = new ControlDecoration(argText, SWT.LEFT | SWT.BOTTOM);
						argText.addModifyListener(new ModifyListener() {
						public void modifyText(ModifyEvent e) {
								validateText(argText, controlDec, arg);
							}						
						});
						
						/*****************************************************
						 * Data binding
						 *****************************************************/
						bindingContext.bindValue(
								WidgetProperties.text(SWT.Modify).observe(argText),
								BeanProperties.value("value").observe(arg),
								new UpdateValueStrategy(),
								new UpdateValueStrategy()
						);
					}
				}
			}
		});
	}
	
	private void validateText(Text text, ControlDecoration controlDec, ICommandArg arg) {
		// Initially assume no problem
		controlDec.hide();
		
		// Test 1: non-empty field
		if ((text.getText() == null) || text.getText().length() == 0) {
			controlDec.setImage(errorDec.getImage());
			controlDec.setDescriptionText("Argument is empty");
			controlDec.show();
			return;
		}
		
		// Test 2: number type
		if (arg.getType().equals(ArgType.INT)) {
			try {
				Integer.parseInt(text.getText());
			} catch (NumberFormatException nfe) {
				controlDec.setImage(errorDec.getImage());
				controlDec.setDescriptionText("Invalid integer argument value");
				controlDec.show();
				return;
			}
		} else if (arg.getType().equals(ArgType.FLOAT)) {
			try {
				Float.parseFloat(text.getText());
			} catch (NumberFormatException nfe) {
				controlDec.setImage(errorDec.getImage());
				controlDec.setDescriptionText("Invalid float argument value");
				controlDec.show();
				return;
			}
		}
	}
	
	private void updateUI() {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				fireRefresh();
			}			
		});
	}
	
	public void dispose() {
		if (bindingContext != null) {
			bindingContext.dispose();
			bindingContext = null;
		}
		errorDec = null;
		super.dispose();
	}
	
}
