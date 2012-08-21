package org.gumtree.ui.tasklet.support;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.gumtree.ui.internal.Activator;
import org.gumtree.ui.tasklet.ITasklet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskletPersistor {

	private static final Logger logger = LoggerFactory
			.getLogger(TaskletPersistor.class);

	public List<ITasklet> loadTasklet() {
		List<ITasklet> tasklets = new ArrayList<ITasklet>();
		File folder = Activator.getDefault().getStateLocation()
				.addFileExtension("/tasklet").toFile();
		if (!folder.exists()) {
			return tasklets;
		}
		for (File file : folder.listFiles()) {
			if (file.isFile() && file.getName().endsWith(".txt")) {
				try {
					String data = FileUtils.readFileToString(file);
					tasklets.add(TaskletUtilities.deserialiseTasklet(data));
				} catch (Exception e) {
					logger.error("Failed to load " + file.getName());
				}
			}
		}
		return tasklets;
	}

	public void saveTasklet(ITasklet tasklet) throws Exception {
		// Serialise
		String taskletString = TaskletUtilities.serialiseTasklet(tasklet);
		File file = getTaskletStorage(tasklet);
		FileUtils.writeStringToFile(file, taskletString);
	}

	public void removeTasklet(ITasklet tasklet) {
		File file = getTaskletStorage(tasklet);
		FileUtils.deleteQuietly(file);
	}

	private File getTaskletStorage(ITasklet tasklet) {
		return Activator.getDefault().getStateLocation()
				.addFileExtension("/tasklet/" + tasklet.getLabel() + ".txt")
				.toFile();
	}

}
