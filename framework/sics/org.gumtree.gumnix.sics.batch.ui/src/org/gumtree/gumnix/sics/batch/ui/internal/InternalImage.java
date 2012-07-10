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

package org.gumtree.gumnix.sics.batch.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public enum InternalImage {

	ADD("icons/add_obj.gif"),
	QUEUE("icons/add_to_queue.gif"),
	DELETE("icons/delete_edit.gif"),
	UP("icons/upward_nav.gif"),
	DOWN("icons/downward_nav.gif"),
	DARK_GREEN("icons/DarkGreen.png"),
	DARK_ORANGE("icons/DarkOrange.png"),
	MAGENTA("icons/Magenta.png"),
	BLACK("icons/Black.png"),
	BLUE("icons/Blue.png"),
	RED("icons/Red.png"),
	ORANGE("icons/Orange.png"),
	CYAN("icons/Cyan.png"),
	TEXT_EDIT("icons/wordassist_co.gif"),
	SAVE("icons/save_edit.gif"),
	LOAD("icons/fldr_obj.gif"),
	APPEND("icons/insert_16x16.png"),
	PRINT("icons/printer_16x16.png"),
	REMOVE("icons/remove_correction.gif"),
	MENU("icons/view_menu.gif"),
	RUN("icons/exec_obj.gif"),
	FILE("icons/file_obj.gif"),
	BUFFER("icons/public_co.gif"),
	ON("icons/power_on16x16.png"),
	OFF("icons/power_off16x16.png"),
	INTERRUPT("icons/round_stop.png"),
	LIBRARY("icons/showcategory_ps.gif"),
	PLAY("icons/Must-Have/Play_16x16.png"),
	PAUSE("icons/Must-Have/Pause_16x16.png");
	
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
						registry.put(key.name(), Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, key.path()));
					}
				}
			}
		}
		return registry;
	}
	
	private String path;
	
	private static volatile ImageRegistry registry;
	
}
