package org.gumtree.app.workbench.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public enum InternalImage {
	CLEAR("/icons/clear_co_gif.gif"),
	ADD_VIEW("/icons/new_wiz.gif"),
//	TERMINAL("/icons/commands/Terminal-16x16.png"),
//	GOOGLE("/icons/commands/google16x16.png"),
//	TRASH("/icons/commands/trash16x16.png");
	APPLICATION("/icons/navigation/appstore.png"),
	SIDE_BAR_HIDE("/icons/navigation/application_side_contract.png"),
	PREFERENCES("/icons/navigation/control2.png"),
	HELP("/icons/navigation/help.png");
	
//	TERMINAL("/icons/navigation/terminal.png");
	
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
						registry.put(key.name(), 
								Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, key.path()));
					}
				}
			}
		}
		return registry;
	}
	
	private String path;
	
	private static volatile ImageRegistry registry;
	
}
