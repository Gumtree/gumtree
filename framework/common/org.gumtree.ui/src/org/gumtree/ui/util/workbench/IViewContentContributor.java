package org.gumtree.ui.util.workbench;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public interface IViewContentContributor {

	public void createContentControl(Composite parent);
	
	public void dispose();
	
	public String getTitle();
	
	public Image getTitleImage();
	
}
