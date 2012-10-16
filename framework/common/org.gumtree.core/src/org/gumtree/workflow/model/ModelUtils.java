package org.gumtree.workflow.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.graph.GraphAdapterBuilder;

public final class ModelUtils {

	static {
		GsonBuilder gsonBuilder = new GsonBuilder();
		// Graph adapter
		new GraphAdapterBuilder().addType(ElementModel.class)
				.addType(WorkflowModel.class).addType(TaskModel.class)
				.registerOn(gsonBuilder);
		// Create gson
		gson = gsonBuilder.setPrettyPrinting().create();
	}

	private static Gson gson;

	/*************************************************************************
	 * Serialisation methods
	 *************************************************************************/

	public static Gson getGson() {
		return gson;
	}

	public static String modelToString(WorkflowModel model) {
		return getGson().toJson(model);
	}

	public static WorkflowModel stringToModel(String modelString) {
		return getGson().fromJson(modelString, WorkflowModel.class);
	}

	private ModelUtils() {
		super();
	}

}
