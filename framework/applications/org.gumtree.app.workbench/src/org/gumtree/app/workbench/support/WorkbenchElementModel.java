package org.gumtree.app.workbench.support;

import java.util.ArrayList;
import java.util.List;

public class WorkbenchElementModel {

	private String label = "";
	
	private List<WorkbenchElementModel> workbenchElementModels;

	public WorkbenchElementModel() {
	}
	
	public WorkbenchElementModel(String label) {
		setLabel(label);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<WorkbenchElementModel> getWorkbenchElementModels() {
		if (workbenchElementModels == null) {
			workbenchElementModels = new ArrayList<WorkbenchElementModel>(2);
		}
		return workbenchElementModels;
	}

	public void setWorkbenchElementModels(
			List<WorkbenchElementModel> workbenchElementModels) {
		this.workbenchElementModels = workbenchElementModels;
	}

}
