package org.gumtree.app.workbench.support;

import java.util.ArrayList;
import java.util.List;


public class WorkbenchModel {

	private List<PerspectiveModel> perspectiveModels;
	
	public WorkbenchModel() {
	}
	
	public List<PerspectiveModel> getPerspectiveModels() {
		if (perspectiveModels == null) {
			perspectiveModels = new ArrayList<PerspectiveModel>(2);
		}
		return perspectiveModels;
	}
	
	public void setPerspectiveModels(List<PerspectiveModel> perspectiveModels) {
		this.perspectiveModels = perspectiveModels;
	}
	
}
