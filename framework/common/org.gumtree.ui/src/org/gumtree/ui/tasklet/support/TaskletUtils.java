package org.gumtree.ui.tasklet.support;

import org.gumtree.ui.tasklet.ITasklet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class TaskletUtils {

	public static String serialiseTasklet(ITasklet tasklet) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(tasklet);
	}

	public static ITasklet deserialiseTasklet(String text) {
		Gson gson = new Gson();
		return gson.fromJson(text, Tasklet.class);
	}

	private TaskletUtils() {
		super();
	}

}
