/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.ui;

import java.util.HashMap;
import java.util.Vector;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

/**
 * Class to manage SWT resources (Font, Color, Image and Cursor) 
 * to dispose them after use and to reuse existed ones.
 * 
 * @author Danil Klimontov (dak)
 */
public class SWTResourceManager {

	private static HashMap<String, Resource> resources = new HashMap<String, Resource>();
	private static Vector<Widget> users = new Vector<Widget>();
	private static SWTResourceManager instance = new SWTResourceManager();

	private static DisposeListener disposeListener = new DisposeListener() {
		public void widgetDisposed(DisposeEvent e) {
			users.remove(e.getSource());
			if (users.size() == 0)
				dispose();
		}
	};

	/**
	 * Registers widget as a widget which will use resources of the class.
	 * <p>
	 * This method should be called by *all* Widgets which use resources
	 * provided by this SWTResourceManager. When widgets are disposed, they are
	 * removed from the "users" Vector, and when no more registered Widgets are
	 * left, all resources are disposed.
	 * <P>
	 * If this method is not called for all Widgets then it should not be called
	 * at all, and the "dispose" method should be explicitly called after all
	 * resources are no longer being used.
	 */
	public static void registerResourceUser(Widget widget) {
		if (users.contains(widget))
			return;
		users.add(widget);
		widget.addDisposeListener(disposeListener);
	}

	/**
	 * Disposes all resources from cache.
	 */
	public static void dispose() {
		for (Resource resource : resources.values()) {
			resource.dispose();
		}
//		Iterator it = resources.keySet().iterator();
//		while (it.hasNext()) {
//			Object resource = resources.get(it.next());
//			if (resource instanceof Font)
//				((Font) resource).dispose();
//			else if (resource instanceof Color)
//				((Color) resource).dispose();
//			else if (resource instanceof Image)
//				((Image) resource).dispose();
//			else if (resource instanceof Cursor)
//				((Cursor) resource).dispose();
//		}
		resources.clear();
	}

	public static Font getFont(String name, int size, int style) {
		return getFont(name, size, style, false, false);
	}

	public static Font getFont(String name, int size, int style,
			boolean strikeout, boolean underline) {
		String fontName = name + "|" + size + "|" + style + "|" + strikeout
				+ "|" + underline;
		if (resources.containsKey(fontName))
			return (Font) resources.get(fontName);
		FontData fd = new FontData(name, size, style);
		if (strikeout || underline) {
			try {
				Class lfCls = Class
						.forName("org.eclipse.swt.internal.win32.LOGFONT");
				Object lf = FontData.class.getField("data").get(fd);
				if (lf != null && lfCls != null) {
					if (strikeout)
						lfCls.getField("lfStrikeOut").set(lf,
								new Byte((byte) 1));
					if (underline)
						lfCls.getField("lfUnderline").set(lf,
								new Byte((byte) 1));
				}
			} catch (Throwable e) {
				System.err.println("Unable to set underline or strikeout"
						+ " (probably on a non-Windows platform). " + e);
			}
		}
		Font font = new Font(Display.getDefault(), fd);
		resources.put(fontName, font);
		return font;
	}

	public static Image getImage(String url, Control widget) {
		Image img = getImage(url);
		if (img != null && widget != null)
			img.setBackground(widget.getBackground());
		return img;
	}

	public static Image getImage(String url) {
		try {
			url = url.replace('\\', '/');
			if (url.startsWith("/"))
				url = url.substring(1);
			if (resources.containsKey(url))
				return (Image) resources.get(url);
			Image img = new Image(Display.getDefault(), instance.getClass()
					.getClassLoader().getResourceAsStream(url));
			if (img != null)
				resources.put(url, img);
			return img;
		} catch (Exception e) {
			System.err
					.println("SWTResourceManager.getImage: Error getting image "
							+ url + ", " + e);
			return null;
		}
	}

	public static Color getColor(int red, int green, int blue) {
		String name = "COLOR:" + red + "," + green + "," + blue;
		if (resources.containsKey(name))
			return (Color) resources.get(name);
		Color color = new Color(Display.getDefault(), red, green, blue);
		resources.put(name, color);
		return color;
	}

	public static Cursor getCursor(int type) {
		String name = "CURSOR:" + type;
		if (resources.containsKey(name))
			return (Cursor) resources.get(name);
		Cursor cursor = new Cursor(Display.getDefault(), type);
		resources.put(name, cursor);
		return cursor;
	}

}
