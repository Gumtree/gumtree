package org.gumtree.gumnix.sics.internal.ui.controlview;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.internal.ui.Activator;
import org.gumtree.gumnix.sics.ui.util.ControlViewerConstants.Column;

// Deprecated
public class ItemPaintListener implements Listener {

	private Image disabledRedImage;

	private Image enabledRedImage;

	private Image disabledYellowImage;

	private Image enabledYellowImage;

	private Image disabledGreenImage;

	private Image enabledGreenImage;

	public void handleEvent(Event event) {
		
		// Paint highlight box for target
		if (event.index == Column.TARGET.getIndex() && event.type == SWT.PaintItem) {
			GC gc = event.gc;
			gc.drawRectangle(event.x, event.y, (((Tree) event.widget).getColumn(event.index)).getWidth() - 2, event.height - 1);
		}
		
		// Paint for status event
		if(event.index == Column.STATUS.getIndex() && event.type == SWT.PaintItem) {
			TreeItem item = (TreeItem)event.item;
			Object data = item.getData("componentController");
			if(data != null && data instanceof IComponentController) {
				IComponentController controller = (IComponentController)data;
				GC gc = event.gc;
				if(controller.getStatus().equals(ControllerStatus.OK)) {
					gc.drawImage(getDisabledRedImage(), event.x, event.y);
					gc.drawImage(getDisabledYellowImage(), event.x + 16, event.y);
					gc.drawImage(getEnabledGreenImage(), event.x + 32, event.y);
				} else if(controller.getStatus().equals(ControllerStatus.ERROR)) {
					gc.drawImage(getEnabledRedImage(), event.x, event.y);
					gc.drawImage(getDisabledYellowImage(), event.x + 16, event.y);
					gc.drawImage(getDisabledGreenImage(), event.x + 32, event.y);
				} else if(controller.getStatus().equals(ControllerStatus.RUNNING)) {
					gc.drawImage(getDisabledRedImage(), event.x, event.y);
					gc.drawImage(getEnabledYellowImage(), event.x + 16, event.y);
					gc.drawImage(getDisabledGreenImage(), event.x + 32, event.y);
				}
			}
		} else if(event.index == Column.STATUS.getIndex() && event.type == SWT.PaintItem) {
			TreeItem item = (TreeItem)event.item;
			Object data = item.getData("componentController");
			if(data != null && data instanceof IComponentController) {
				IComponentController controller = (IComponentController)data;
				Rectangle rect = event.getBounds();
				GC gc = event.gc;
				int width = ((Tree)event.widget).getColumn(event.index).getWidth();
				if(controller.getStatus().equals(ControllerStatus.OK)) {
					gc.setBackground(item.getDisplay().getSystemColor(SWT.COLOR_GREEN));
				} else if(controller.getStatus().equals(ControllerStatus.ERROR)) {
					gc.setBackground(item.getDisplay().getSystemColor(SWT.COLOR_RED));
				} else if(controller.getStatus().equals(ControllerStatus.RUNNING)) {
					gc.setBackground(item.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
				}
				gc.fillRectangle(event.x, event.y, width, event.height);
			}
		}
	}

	public Image getDisabledGreenImage() {
		if(disabledGreenImage == null) {
			disabledGreenImage = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/dlcl16/public_co.gif").createImage();
		}
		return disabledGreenImage;
	}

	public Image getDisabledRedImage() {
		if(disabledRedImage == null) {
			disabledRedImage = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/dlcl16/private_co.gif").createImage();
		}
		return disabledRedImage;
	}

	public Image getDisabledYellowImage() {
		if(disabledYellowImage == null) {
			disabledYellowImage = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/dlcl16/protected_co.gif").createImage();
		}
		return disabledYellowImage;
	}

	public Image getEnabledGreenImage() {
		if(enabledGreenImage == null) {
			enabledGreenImage = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/elcl16/public_co.gif").createImage();
		}
		return enabledGreenImage;
	}

	public Image getEnabledRedImage() {
		if(enabledRedImage == null) {
			enabledRedImage = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/elcl16/private_co.gif").createImage();
		}
		return enabledRedImage;
	}

	public Image getEnabledYellowImage() {
		if(enabledYellowImage == null) {
			enabledYellowImage = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/elcl16/protected_co.gif").createImage();
		}
		return enabledYellowImage;
	}
}
