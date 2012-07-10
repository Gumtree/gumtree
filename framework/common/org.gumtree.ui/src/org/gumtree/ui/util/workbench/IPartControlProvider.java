package org.gumtree.ui.util.workbench;

import org.eclipse.swt.widgets.Composite;

public interface IPartControlProvider {

	public void createPartControl(Composite parent);
	
	public boolean isDisposed();
	
	public void dispose();
	
	public void setFocus();

}
