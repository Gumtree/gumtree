package org.gumtree.control.ui.viewer;

import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.gumtree.control.core.IControllerData;
import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.model.ControllerData;
import org.gumtree.control.model.ModelUtils;
import org.gumtree.control.ui.viewer.ControlViewerConstants.Column;
import org.gumtree.control.ui.viewer.model.CommandControllerNode;
import org.gumtree.control.ui.viewer.model.DynamicControllerNode;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.Property;

public class TargetEditingSupport extends EditingSupport {

	private int coloumnIndex;
	
	private boolean enabled;
	
	private TextCellEditor textCellEditor;
	
	private ComboBoxCellEditor comboCellEditor;
	
	private RunDialogCellEditor runDialogCellEditor;
	
	private static ControlRunner controlRunner;

	private static synchronized ControlRunner getControlRunner() {
		if (controlRunner == null) {
			controlRunner = new ControlRunner();
		}
		return controlRunner;
	}
	
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
		
		Component component	= ((DynamicControllerNode) element).getController().getModel();
		Property prop = ModelUtils.getProperty(component, "values");
		if (prop != null) {
			comboCellEditor.setItems(prop.getValue().toArray(new String[prop.getValue().size()]));
			return comboCellEditor;
		} else {
			String type = ModelUtils.getPropertyFirstValue(component, "argtype");
			if (type != null && type.equals("drivable")) {
				comboCellEditor.setItems(ModelUtils.getSicsDrivableIds(SicsManager.getSicsProxy()));
				return comboCellEditor;
			}
		}
		
		return textCellEditor;
	}

	protected Object getValue(Object element) {
		if(element instanceof DynamicControllerNode) {
			IDynamicController controller = ((DynamicControllerNode)element).getDynamicController();
			IControllerData data = null;
			try {
				data = controller.getTargetValue();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(data == null) {
				return "";
			}
			Component component	= ((DynamicControllerNode) element).getController().getModel();
			// Values
			Property prop = ModelUtils.getProperty(component, "values");
			if (prop != null) {
				int index = prop.getValue().indexOf(data.getStringData());
				if (index == -1) {
					index = 0;
				}
				return index;
			}
			// Drivable args
			String type = ModelUtils.getPropertyFirstValue(component, "argtype");
			if (type != null && type.equals("drivable")) {
				List<String> drivables = ModelUtils.getSicsDrivableIdList(SicsManager.getSicsProxy());
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
			final IControllerData data = ControllerData.createStringData(newTargetValue);
			if (shouldCommit) {
				getControlRunner().delayedProcess(new Runnable() {
					
					@Override
					public void run() {
						try {
							IDynamicController controller = ((DynamicControllerNode) element).getDynamicController();
							controller.setTargetValue(data);
							controller.commitTargetValue();					
						} catch (SicsException e) {
							e.printStackTrace();
						}
					}
				});
			}
			
			/*****************************************************************
			 * Refresh UI
			 *****************************************************************/
			getViewer().refresh(element);
		}
	}

}
