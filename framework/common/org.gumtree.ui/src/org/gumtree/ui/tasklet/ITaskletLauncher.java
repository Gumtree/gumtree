package org.gumtree.ui.tasklet;

import org.gumtree.core.object.IDisposable;

public interface ITaskletLauncher extends IDisposable {

	public void launchTasklet(IActivatedTasklet tasklet);
	
}
