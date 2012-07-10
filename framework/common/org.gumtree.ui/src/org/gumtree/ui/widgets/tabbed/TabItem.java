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

package org.gumtree.ui.widgets.tabbed;

import org.eclipse.swt.graphics.Image;

public class TabItem implements ITabItem {

	private String text;
	
	private Image image;
	
	private Object source;
	
	public TabItem(String text, Image image, Object source) {
		this.text = text;
		this.image = image;
		this.source = source;
	}
	
	public Image getImage() {
		return image;
	}

	public String getText() {
		return text;
	}

	public boolean isIndented() {
		return true;
	}

	public boolean isSelected() {
		return false;
	}

	public Object getSource() {
		return source;
	}
	
}
