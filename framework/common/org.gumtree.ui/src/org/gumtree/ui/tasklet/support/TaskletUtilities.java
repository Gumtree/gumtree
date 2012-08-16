package org.gumtree.ui.tasklet.support;

import org.eclipse.e4.ui.model.application.ui.advanced.MAdvancedFactory;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.gumtree.ui.tasklet.ITasklet;
import org.gumtree.ui.util.workbench.WorkbenchUtils;

import com.google.gson.Gson;

@SuppressWarnings("restriction")
public final class TaskletUtilities {

	public static MPerspectiveStack getMPerspectiveStack(MWindow mWindow) {
		return WorkbenchUtils.getFirstChild(mWindow, MPerspectiveStack.class);
	}

	public static MPerspective createMPerspective(
			MPerspectiveStack mPerspectiveStack, String label) {
		MPerspective mPerspective = MAdvancedFactory.INSTANCE
				.createPerspective();
		mPerspective.setLabel(label);
		mPerspectiveStack.getChildren().add(mPerspective);
		return mPerspective;
	}

	public static String serialiseTasklet(ITasklet tasklet) {
		Gson gson = new Gson();
		return gson.toJson(tasklet);
	}

	public static ITasklet deserialiseTasklet(String text) {
		Gson gson = new Gson();
		return gson.fromJson(text, Tasklet.class);
	}

	private TaskletUtilities() {
		super();
	}

}
