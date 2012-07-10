package org.gumtree.gumnix.sics.ui.util;

import org.eclipse.swt.graphics.Image;
import org.gumtree.gumnix.sics.control.controllers.ICommandController;
import org.gumtree.gumnix.sics.internal.ui.Activator;
import org.gumtree.gumnix.sics.ui.util.ControlViewerConstants.Column;

public class CommandControllerNode extends DefaultControllerNode {

	private static Image imageCommand;

	static {
		if(Activator.getDefault() != null) {
			imageCommand = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/obj16/class_obj.gif").createImage();
		}
	}

	public CommandControllerNode(ICommandController controller) {
		super(controller);
	}

	public String getColumnText(int columnIndex) {
		if (columnIndex == Column.TARGET.getIndex()) {
			return "Run";
		}
		return super.getColumnText(columnIndex);
	}
	
	public Image getColumnImage(int columnIndex) {
		if(columnIndex == 0) {
			return imageCommand;
		}
		return null;
	}

	public ICommandController getCommandController() {
		return (ICommandController)getOriginalObject();
	}
	
	public String toString() {
		return "[CommandControllerNode] : " + getController().getPath();
	}

}
