package org.gumtree.gumnix.sics.internal.ui.controlview;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Widget;
import org.gumtree.gumnix.sics.control.IDeviceController;
import org.gumtree.gumnix.sics.control.IInstrumentController;
import org.gumtree.gumnix.sics.control.IPartController;
import org.gumtree.gumnix.sics.internal.ui.Activator;
import org.gumtree.gumnix.sics.internal.ui.controlview.ControlViewerConstants.Column;
import org.gumtree.ui.util.ITreeNode;
import org.gumtree.ui.util.TreeNode;

import ch.psi.sics.hipadaba.Instrument;

public class InstrumentControllerNode extends TreeNode {

	private static Image imageInstrument;

	static {
		if(Activator.getDefault() != null) {
			imageInstrument = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/obj16/debugt_obj.gif").createImage();
		}
	}

	public InstrumentControllerNode(IInstrumentController controller, StructuredViewer viewer) {
		super(controller, viewer);
	}

	public ITreeNode[] getChildren() {
		Widget item = getViewer().testFindItem(this);
		item.setData("componentController", getController());
		List<ITreeNode> children = new ArrayList<ITreeNode>();
		for(IPartController partController : getController().getChildPartControllers()) {
			children.add(new PartControllerNode(partController, getViewer()));
		}
		for(IDeviceController deviceController : getController().getChildDeviceControllers()) {
			children.add(new DeviceControllerNode(deviceController, getViewer()));
		}
		return children.toArray(new ITreeNode[children.size()]);
	}

	public String getColumnText(int columnIndex) {
		if(columnIndex == Column.NODE.getIndex()) {
			String text = getInstrument().getLabel();
			if(text == null || text.equals(EMPTY_STRING)) {
				text = getInstrument().getId();
			}
			if(text != null) {
				return text;
			}
		}
		return EMPTY_STRING;
	}

	public Image getColumnImage(int columnIndex) {
		if(columnIndex == Column.NODE.getIndex()) {
			return imageInstrument;
		}
		return null;
	}

	private IInstrumentController getController() {
		return (IInstrumentController)getOriginalObject();
	}

	private Instrument getInstrument() {
		return getController().getInstrument();
	}

}
