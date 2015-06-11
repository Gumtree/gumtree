package org.gumtree.data.ui.viewers.internal;

import javax.swing.ImageIcon;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public enum InternalImage {

	OPEN("/icons/eclipse/fldr_obj.gif"),
	REMOVE("/icons/eclipse/delete_edit.gif"),
	FILE("/icons/eclipse/file_obj.gif"),
	GROUP("/icons/eclipse/cfldr_obj.gif"),
	DATA_ITEM("/icons/eclipse/prop_ps.gif"),
	REFRESH("/icons/refresh16x16.png"),
	COPY("/icons/copy16x16.png"),
	SAVE("/icons/picture_save16x16.png"),
	PRINT("/icons/printer16x16.png"),
	SENDLOG("/icons/sendlog.gif"),
	EXPORT("/icons/table_export_16x16.png"),
	SETTING("/icons/setting_16x16.png"),
	LOG("/icons/logarithm.gif"),
	ROTATE("/icons/rotate_page_blue_16x16.png"),
	HELP("/icons/question_16x16.png"),
	TEXT_INPUT("/icons/text_input_16.png"),
	THREE_D("/icons/3d_16x16.png");

	private InternalImage(String path) {
		this.path = path;
	}

	public Image getImage() {
		return getRegistry().get(name());
	}

	public ImageDescriptor getDescriptor() {
		return getRegistry().getDescriptor(name());
	}

	public ImageIcon createImageIcon() {
        java.net.URL imgURL = Activator.class.getResource(path());
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            return null;
        }
    }
	
	public static boolean isInstalled() {
		return registry != null;
	}

	public static synchronized void dispose() {
		if (registry != null) {
			registry.dispose();
			registry = null;
		}
	}
	
	private String path() {
		return path;
	}

	private static ImageRegistry getRegistry() {
		if (registry == null) {
			synchronized (InternalImage.class) {
				if (registry == null) {
					registry = new ImageRegistry(Display.getDefault());
					for (InternalImage key : values()) {
						
							registry.put(key.name(),
									ImageDescriptor.createFromFile(
											Activator.class, key.path()));
					}
				}
			}
		}
		return registry;
	}

	private String path;

	private static volatile ImageRegistry registry;

}
