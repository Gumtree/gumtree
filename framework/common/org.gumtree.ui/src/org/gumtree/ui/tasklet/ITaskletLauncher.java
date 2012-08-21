package org.gumtree.ui.tasklet;

import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;

@SuppressWarnings("restriction")
public interface ITaskletLauncher {

	public void launchTasklet(IActivatedTasklet tasklet);
	
}
