package org.gumtree.ui.util.workbench;

import org.eclipse.ui.IViewPart;

public interface IContentView extends IViewPart {

	public IViewContentContributor getContentContributor();
	
	public void setContentContributor(IViewContentContributor contributor);
	
}
