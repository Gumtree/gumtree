package org.gumtree.gumnix.sics.ui.util;

import org.eclipse.swt.graphics.Image;
import org.gumtree.gumnix.sics.control.controllers.IGraphDataController;
import org.gumtree.gumnix.sics.control.controllers.IOneDDataController;
import org.gumtree.gumnix.sics.internal.ui.Activator;

public class GraphicDataControllerNode extends DefaultControllerNode {

	private static Image oneDDataImage;

	private static Image twoDDataImage;

	static {
		if(Activator.getDefault() != null) {
			oneDDataImage = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/obj16/linecharticon.gif").createImage();
			twoDDataImage = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/obj16/piecharticon.gif").createImage();
		}
	}

	private Image image;

	public GraphicDataControllerNode(IGraphDataController controller) {
		super(controller);
	}

	public Image getColumnImage(int columnIndex) {
		if(columnIndex == 0) {
			if(image == null) {
				if(getController() instanceof IOneDDataController) {
					image = oneDDataImage;
				} else {
					image = twoDDataImage;
				}
			}
			return image;
		}
		return null;
	}

	public IGraphDataController getController() {
		return (IGraphDataController)getOriginalObject();
	}

	public String toString() {
		return "[GraphicDataControllerNode] : " + getController().getPath();
	}

}
