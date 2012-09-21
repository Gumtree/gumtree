package org.gumtree.ui.missioncontrol.support;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gumtree.ui.internal.Activator;
import org.gumtree.ui.missioncontrol.IHub;

public class HubPersistor {
	
	public List<IHub> loadHubs() {
		List<IHub> hubs = new ArrayList<IHub>();
		File folder = Activator.getDefault().getStateLocation()
				.addFileExtension("/hubs").toFile();
//		if (!folder.exists()) {
//			return tasklets;
//		}
//		for (File file : folder.listFiles()) {
//			if (file.isFile() && file.getName().endsWith(".txt")) {
//				try {
//					String data = FileUtils.readFileToString(file);
//					tasklets.add(TaskletUtils.deserialiseTasklet(data));
//				} catch (Exception e) {
//					logger.error("Failed to load " + file.getName());
//				}
//			}
//		}
		return hubs;
	}

}
