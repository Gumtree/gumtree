/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.ui.util.resource;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.gumtree.ui.internal.Activator;

public final class UIResources {

	private static UIResourceManager manager;

	public static Font getDefaultFont() {
		return JFaceResources.getDefaultFont();
	}

	public static Font getDefaultFont(int style) {
		if ((style & SWT.BOLD) == SWT.BOLD) {
			return JFaceResources.getFontRegistry().getBold(
					JFaceResources.DEFAULT_FONT);
		} else if ((style & SWT.ITALIC) == SWT.ITALIC) {
			return JFaceResources.getFontRegistry().getItalic(
					JFaceResources.DEFAULT_FONT);
		} else {
			return getDefaultFont();
		}
	}

	public static Cursor getSystemCursor(int style) {
		return ((Display) getManager().getDevice()).getSystemCursor(style);
	}

	private static UIResourceManager getManager() {
		if (manager == null) {
			manager = new UIResourceManager(Activator.PLUGIN_ID);
		}
		return manager;
	}

	private UIResources() {
		super();
	}

}
