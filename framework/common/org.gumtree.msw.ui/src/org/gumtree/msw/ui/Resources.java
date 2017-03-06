package org.gumtree.msw.ui;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;

public final class Resources {
	// colors
	public static final Color COLOR_DEFAULT = SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND);
	public static final Color COLOR_DISABLED = SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND);
	public static final Color COLOR_EDITING = SWTResourceManager.getColor(255, 251, 164);
	public static final Color COLOR_ERROR = SWTResourceManager.getColor(255, 164, 164);
	
	// images
	public static final Image IMAGE_NEW = load("/icons/new.png");
	public static final Image IMAGE_NEW_GRAY = load("/icons/new-gray.png");
	public static final Image IMAGE_SAVE = load("/icons/drive-download.png");
	public static final Image IMAGE_LOAD = load("/icons/drive-upload.png");
    
	public static final Image IMAGE_LOCK_OPEN = load("/icons/lock_open.png");
	public static final Image IMAGE_LOCK_CLOSED = load("/icons/lock_closed.png");
	public static final Image IMAGE_LOCK_SMALL = load("/icons/lock_small.gif");

	public static final Image IMAGE_UNDO = load("/icons/arrow-curve-180-left.png");
	public static final Image IMAGE_REDO = load("/icons/arrow-curve.png");

	public static final Image IMAGE_DISK = load("/icons/disk.png");
	public static final Image IMAGE_PLAY = load("/icons/control.png");
	public static final Image IMAGE_RESET = load("/icons/arrow-circle-225-left.png");

	public static final Image IMAGE_PLUS = load("/icons/plus.png");
	public static final Image IMAGE_CROSS = load("/icons/cross.png");
	public static final Image IMAGE_COPY = load("/icons/document-copy.png");

	public static final Image IMAGE_TICK = load("/icons/tick.png");
	public static final Image IMAGE_PLUS_SMALL = load("/icons/plus-small.png");
	public static final Image IMAGE_PLUS_SMALL_GRAY = load("/icons/plus-small-gray.png");
	public static final Image IMAGE_CROSS_SMALL = load("/icons/cross-small.png");
	public static final Image IMAGE_CROSS_SMALL_GRAY = load("/icons/cross-small-gray.png");
	public static final Image IMAGE_MINUS_SMALL = load("/icons/minus-small.png");
	public static final Image IMAGE_MINUS_SMALL_GRAY = load("/icons/minus-small-gray.png");
	public static final Image IMAGE_COPY_SMALL = load("/icons/copy-small.png");
	public static final Image IMAGE_COPY_SMALL_GRAY = load("/icons/copy-small-gray.png");
	public static final Image IMAGE_PLAY_SMALL = load("/icons/control-small.png");
	public static final Image IMAGE_PLAY_SMALL_GRAY = load("/icons/control-small-gray.png");

	public static final Image IMAGE_CHECKED = load("/icons/ui-check.png");
	public static final Image IMAGE_BOX_CHECKED = load("/icons/ui-check-box.png");
	public static final Image IMAGE_BOX_UNCHECKED = load("/icons/ui-check-box-uncheck.png");

	public static final Image IMAGE_SETTINGS_DROPDOWN = load("/icons/wrench-screwdriver-dropdown.png");
	
	public static final Image IMAGE_EXPORT = load("/icons/exportpref_obj.gif");
	public static final Image IMAGE_IMPORT = load("/icons/importpref_obj.gif");
	public static final Image IMAGE_IMPORT_FILE = load("/icons/import_file_16x16.png");

	public static final Image IMAGE_BIN = load("/icons/bin.png");
	public static final Image IMAGE_LOAD_POSITION = load("/icons/drive-download-green.png");

	public static final Image IMAGE_BULLSEYE = load("/icons/bullseye.png"); // geolocation.png
	public static final Image IMAGE_BULLSEYE_GRAY = load("/icons/bullseye_gray.png");

	public static final Image IMAGE_EXPANDED = load("/icons/expanded.png");
	public static final Image IMAGE_COLLAPSED = load("/icons/collapsed.png");
	public static final Image IMAGE_DOWN = load("/icons/down.png");

	public static final Image IMAGE_FOLDER = load("/icons/folder-small-horizontal.png");
	public static final Image IMAGE_CONFIGURATION = load("/icons/document-small-list.png");

	public static final Image IMAGE_AUTOFILL = load("/icons/wand.png");
	public static final Image IMAGE_REFRESH = load("/icons/arrow-circle-double-135.png");
	public static final Image IMAGE_OPTIMIZE = load("/icons/chart-up-color.png");
	public static final Image IMAGE_GENERATE = load("/icons/generate.png");
	
	// helpers
	public static Image load(String name) {
    	return load(name, -1, -1);
    }
	public static Image load(String name, int width, int height) {
    	InputStream stream = Resources.class.getResourceAsStream(name);
    	if (stream != null) {
    		try {
    			Image img = new Image(Display.getCurrent(), stream);
    			if (img != null) {
    				boolean scale = false;

    				if (width != -1)
    					scale = true;
    				else
    					width = img.getImageData().width;

    				if (height != -1)
    					scale = true;
    				else
    					height = img.getImageData().height;

    				if (!scale)
    					return img;
    				else
    					return new Image(
    							img.getDevice(),
    							img.getImageData().scaledTo(width, height));
    			}
    		}
	    	finally {
	    		try {
	    			stream.close();
	    		}
	    		catch (IOException e) {
	    		}
	    	}
    	}
    	return null;
    }
}
