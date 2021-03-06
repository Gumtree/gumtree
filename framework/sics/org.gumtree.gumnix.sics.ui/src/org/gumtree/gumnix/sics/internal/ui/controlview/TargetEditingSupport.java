package org.gumtree.gumnix.sics.internal.ui.controlview;

import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.gumtree.gumnix.sics.control.controllers.ComponentData;
import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.core.SicsUtils;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.gumnix.sics.ui.util.CommandControllerNode;
import org.gumtree.gumnix.sics.ui.util.DynamicControllerNode;
import org.gumtree.gumnix.sics.ui.util.ControlViewerConstants.Column;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.Property;

public class TargetEditingSupport extends EditingSupport {

	private int coloumnIndex;
	
	private boolean enabled;
	
	private TextCellEditor textCellEditor;
	
	private ComboBoxCellEditor comboCellEditor;
	
	private RunDialogCellEditor runDialogCellEditor;

	public TargetEditingSupport(TreeViewer viewer, int coloumnIndex) {
		super(viewer);
		this.coloumnIndex = coloumnIndex;
		textCellEditor = new TextCellEditor(viewer.getTree());
		// [GT-216] Mask out the following code to avoid the widget disappear bug
		// It seems this bug appears after the Eclipse 3.5 migration.
//		textCellEditor.addListener(new ICellEditorListener() {
//			public void applyEditorValue() {
//				// [Bug Fixed] Manually re-decorate to ensure the image is up-to-date after editing
//				PlatformUI.getWorkbench().getDecoratorManager().update("org.gumtree.gumnix.sics.ui.componentDecorator");
//			}
//			public void cancelEditor() {
//			}
//			public void editorValueChanged(boolean oldValidState,
//					boolean newValidState) {
//			}			
//		});
		comboCellEditor = new ComboBoxCellEditor(viewer.getTree(), new String[0], SWT.NONE);
//		comboCellEditor.addListener(new ICellEditorListener() {
//			public void applyEditorValue() {
//				// [Bug Fixed] Manually re-decorate to ensure the image is up-to-date after editing
//				PlatformUI.getWorkbench().getDecoratorManager().update("org.gumtree.gumnix.sics.ui.componentDecorator");
//			}
//			public void cancelEditor() {
//			}
//			public void editorValueChanged(boolean oldValidState,
//					boolean newValidState) {
//			}			
//		});
		runDialogCellEditor = new RunDialogCellEditor(viewer.getTree(), "Run");
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	protected boolean canEdit(Object element) {
		if (coloumnIndex == Column.TARGET.getIndex()) {
			if (element instanceof DynamicControllerNode) {
				return enabled;
			} else if (element instanceof CommandControllerNode) {
				return true;
			}
		}
		return false;
	}

	protected CellEditor getCellEditor(Object element) {
		if (element instanceof CommandControllerNode) {
			runDialogCellEditor.setCommandController(
					((CommandControllerNode) element).getCommandController()); 
			return runDialogCellEditor;
		}
		
		Component component	= ((DynamicControllerNode) element).getController().getComponent();
		Property prop = SicsUtils.getProperty(component, "values");
		if (prop != null) {
			comboCellEditor.setItems(prop.getValue().toArray(new String[prop.getValue().size()]));
			return comboCellEditor;
		} else {
			String type = SicsUtils.getPropertyFirstValue(component, "argtype");
			if (type != null && type.equals("drivable")) {
				comboCellEditor.setItems(SicsUtils.getSicsDrivableIds());
				return comboCellEditor;
			}
		}
		
		return textCellEditor;
	}

	protected Object getValue(Object element) {
		if(element instanceof DynamicControllerNode) {
			IDynamicController controller = ((DynamicControllerNode)element).getDynamicController();
			IComponentData data = null;
			try {
				data = controller.getTargetValue();
			} catch (SicsIOException e) {
				e.printStackTrace();
			}
			if(data == null) {
				return "";
			}
			Component component	= ((DynamicControllerNode) element).getController().getComponent();
			// Values
			Property prop = SicsUtils.getProperty(component, "values");
			if (prop != null) {
				int index = prop.getValue().indexOf(data.getStringData());
				if (index == -1) {
					index = 0;
				}
				return index;
			}
			// Drivable args
			String type = SicsUtils.getPropertyFirstValue(component, "argtype");
			if (type != null && type.equals("drivable")) {
				List<String> drivables = SicsUtils.getSicsDrivableIdList();
				int index = drivables.indexOf(data.getStringData());
				if (index == -1) {
					index = 0;
				}
				return index;
			} 
			// Otherwise
			return data.getStringData();
		}
		return "";
	}

	protected void setValue(Object element, Object value) {
		if(element instanceof DynamicControllerNode) {
			/*****************************************************************
			 * Resolve value
			 *****************************************************************/
			String newTargetValue = null;
			boolean shouldCommit = true;
			if (value instanceof Integer) {
				// From combo editor
				newTargetValue = ((CCombo) comboCellEditor.getControl()).getText();
			} else {
				// From text editor
				newTargetValue = value.toString();
				shouldCommit = textCellEditor.isDirty();
			}
			
			/*****************************************************************
			 * Commit value
			 *****************************************************************/
			if (shouldCommit) {
				try {
					IDynamicController controller = ((DynamicControllerNode) element).getDynamicController();
					controller.setTargetValue(ComponentData.createStringData(newTargetValue));
					controller.commitTargetValue(null);					
				} catch (SicsIOException e) {
					e.printStackTrace();
				}
			}
			
			/*****************************************************************
			 * Refresh UI
			 *****************************************************************/
			getViewer().refresh(element);
		}
	}

}
