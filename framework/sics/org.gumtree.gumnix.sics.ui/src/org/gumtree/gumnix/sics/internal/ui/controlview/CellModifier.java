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

package org.gumtree.gumnix.sics.internal.ui.controlview;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.TreeItem;
import org.gumtree.gumnix.sics.control.controllers.ComponentData;
import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.gumnix.sics.ui.util.DynamicControllerNode;

public class CellModifier implements ICellModifier {
	
	private StructuredViewer viewer;

	private boolean enabled;
	
	public CellModifier(StructuredViewer viewer) {
		this.viewer = viewer;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean canModify(Object element, String property) {
		if(element instanceof DynamicControllerNode) {
			return enabled;
		}
		return false;
	}

	public Object getValue(Object element, String property) {
		if(element instanceof DynamicControllerNode) {
			IDynamicController controller = ((DynamicControllerNode)element).getDynamicController();
			try {
				IComponentData data = controller.getTargetValue();
				if(data == null) {
					return "";
				}
				return data.getStringData();
			} catch (SicsIOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	public void modify(Object element, String property, Object value) {
		Object node = ((TreeItem)element).getData();
		if(node instanceof DynamicControllerNode && value instanceof String) {
			String newTargetValue = (String) value;
			IDynamicController controller = ((DynamicControllerNode)node).getDynamicController();
			String oldTargetValue = null;
			try {
				oldTargetValue = controller.getTargetValue().getSicsString();	
			} catch (SicsIOException e) {
				e.printStackTrace();
			}
			boolean shouldCommit = true;
			try {
				// Do not commit if no changes in new / old value and at same current value 
				if (controller.getValue().getSicsString().equals(oldTargetValue)) {
					if (newTargetValue.equals(oldTargetValue)) {
						shouldCommit = false;
					}
				}
				if (shouldCommit) {
					controller.setTargetValue(ComponentData.createStringData(newTargetValue));
					controller.commitTargetValue(null);					
				}
			} catch (SicsIOException e) {
				e.printStackTrace();
			}
			viewer.refresh(node);
		}
	}

}
