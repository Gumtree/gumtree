package au.gov.ansto.bragg.quokka.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import au.gov.ansto.bragg.quokka.ui.internal.Activator;

public enum InternalImage {

	ADD("/icons/add_obj.gif"),
	DELETE("/icons/delete_edit.gif"),
	UP("/icons/upward_nav.gif"),
	DOWN("/icons/downward_nav.gif"),
	LOCK("/icons/lock.png"),
	UNLOCK("/icons/lock_open.png"),
	PREVIEW("/icons/details_view.gif"),
	SAVE("/icons/save_edit.gif"),
	IMPORT("/icons/importpref_obj.gif"),
	EXPORT("/icons/exportpref_obj.gif"),
	IMAGE("/icons/image.png"),
	LOAD("/icons/download_manager.png"),
	UNDO("/icons/undo_edit.gif"),
	SPLIT("/icons/arrow_divide.png"),
	RIGHT_ARROW("/icons/forward_nav.gif"),
	DOWN_ARROW("/icons/view_menu.gif"),
	FOLDER("/icons/fldr_obj.gif"),
	FILE("/icons/file_obj.gif"),
	EXCEL("/icons/excel16x16.png"),
	COPY("/icons/copy_edit.gif"),
	IMPORT_FILE("/icons/import_file_16x16.png");
	
	private InternalImage(String path) {
		this.path = path;
	}
	
	public Image getImage() {
		return getRegistry().get(name());
	}
	
	public ImageDescriptor getDescriptor() {
		return getRegistry().getDescriptor(name());
	}
	
	public static boolean isInstalled() {
		return registry != null;
	}
	
	public static synchronized void dispose() {
		registry.dispose();
		registry = null;
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
						registry.put(key.name(), Activator
								.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
										key.path()));
					}
				}
			}
		}
		return registry;
	}
	
	private String path;
	
	private static volatile ImageRegistry registry;
	
}
