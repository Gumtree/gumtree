package org.gumtree.control.ui.batch.taskeditor;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.gumtree.control.ui.batch.SicsBatchUIUtils;
import org.gumtree.control.ui.batch.command.AbstractSicsCommandView;
import org.gumtree.control.ui.batch.command.DevicePropertyCommand;

public class DevicePropertyView extends AbstractSicsCommandView<DevicePropertyCommand> {

private DataBindingContext bindingContext;
	
	private ComboViewer drivableComboViewer;
	
	private ComboViewer propertyComboViewer;
	
	private Text text;
	
	@Override
	public void createPartControl(Composite parent, DevicePropertyCommand command) {
		GridLayoutFactory.fillDefaults().margins(0, 0).spacing(10, SWT.DEFAULT).numColumns(3).applyTo(parent);
		
		/*********************************************************************
		 * Device
		 *********************************************************************/
//		final IDrivableController[] drivables = SicsCommandUtils.getSicsDrivables();
		final String[] drivableIds = SicsBatchUIUtils.getSicsDrivableIds();
		drivableComboViewer = new ComboViewer(parent, SWT.READ_ONLY);
		drivableComboViewer.setContentProvider(new ArrayContentProvider());
		drivableComboViewer.setLabelProvider(new LabelProvider());
		drivableComboViewer.setSorter(new ViewerSorter());
		drivableComboViewer.setInput(drivableIds);
		drivableComboViewer.getCombo().setVisibleItemCount(20);
		GridDataFactory.swtDefaults().hint(WIDTH_COMBO, SWT.DEFAULT).applyTo(drivableComboViewer.getCombo());
		// Update keyComboViewer based on drivableComboViewer selection
		drivableComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				String drivableId = (String) (((IStructuredSelection) event.getSelection())).getFirstElement();
				
				// Update model
				getCommand().setDeviceId(drivableId);
				
				// Update property input
				updatePropertyInput(drivableId);
			}
		});
		
		/*********************************************************************
		 * Key
		 *********************************************************************/
		propertyComboViewer = new ComboViewer(parent, SWT.READ_ONLY);
		propertyComboViewer.setContentProvider(new ArrayContentProvider());
		propertyComboViewer.setLabelProvider(new LabelProvider());
		propertyComboViewer.setSorter(new ViewerSorter());
		propertyComboViewer.getCombo().setVisibleItemCount(20);
		GridDataFactory.swtDefaults().hint(WIDTH_COMBO, SWT.DEFAULT).applyTo(propertyComboViewer.getCombo());
		propertyComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				String propertyId = (String) (((IStructuredSelection) event.getSelection())).getFirstElement();
				
				// Update model
				getCommand().setPropertyId(propertyId);
			}
		});
		
		/*********************************************************************
		 * Argument
		 *********************************************************************/
		text = getToolkit().createText(parent, "", SWT.BORDER);
		text.setToolTipText("Enter device property argument");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(text);
		// Check empty field
		final ControlDecoration controlDec = new ControlDecoration(text, SWT.LEFT | SWT.BOTTOM);
		final FieldDecoration fieldDec = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if ((text.getText() == null) || text.getText().length() == 0) {
					controlDec.setImage(fieldDec.getImage());
					controlDec.setDescriptionText("Device property argument is empty");
					controlDec.show();
				} else {
					controlDec.hide();
				}
			}
		});
		/*********************************************************************
		 * Default setting
		 *********************************************************************/
		selectDeviceId();
		
		/*********************************************************************
		 * Data binding
		 *********************************************************************/
		Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
			public void run() {
				bindingContext = new DataBindingContext();
				
				bindingContext.bindValue(
						SWTObservables.observeText(text, SWT.Modify),
						BeansObservables.observeValue(getCommand(), "value"),
						new UpdateValueStrategy(),
						new UpdateValueStrategy()
				);
			}
		});
	}
	
	private void selectDeviceId() {
		if (getCommand().getDeviceId() != null) {
			drivableComboViewer.setSelection(new StructuredSelection(getCommand().getDeviceId()));
		} else {
			if (drivableComboViewer.getCombo().getItemCount() > 0) {
				drivableComboViewer.setSelection(new StructuredSelection(
						drivableComboViewer.getElementAt(drivableComboViewer.getCombo().getItemCount() - 1)));
			}
		}
	}
	
	private void selectPropertyId() {
		if (getCommand().getPropertyId() != null) {
			propertyComboViewer.setSelection(new StructuredSelection(getCommand().getPropertyId()));
		} else {
			if (propertyComboViewer.getCombo().getItemCount() > 0) {
				propertyComboViewer.setSelection(new StructuredSelection(
						propertyComboViewer.getElementAt(propertyComboViewer.getCombo().getItemCount() - 1)));
			}
		}
	}
	
	private void updatePropertyInput(String drivableId) {
		// Update attribute list
		String[] attributes = SicsBatchUIUtils.getDrivableAttributes(drivableId);
		propertyComboViewer.setInput(attributes);
		// Set default selection
		selectPropertyId();
	}
	
	@Override
	public void dispose() {
		if (bindingContext != null) {
			bindingContext.dispose();
			bindingContext = null;
		}
		drivableComboViewer = null;
		propertyComboViewer = null;
		text = null;
		super.dispose();
	}

//	private class DevicePropertyBindingModel {
//		
//		private getControllerList() {
//			return SicsCommandUtils.getSicsDrivables();
//		}
//	}
	
}
