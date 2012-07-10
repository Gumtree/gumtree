package org.gumtree.ui.util.workbench;

import org.eclipse.swt.graphics.Image;

public abstract class AbstractViewContentContributor implements IViewContentContributor {
	
	public void dispose() {
	}

	public String getTitle() {
		return null;
	}

	public Image getTitleImage() {
		return null;
	}

}
