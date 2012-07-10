package org.gumtree.app.workbench.support;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.IPerspectiveDescriptor;

import com.google.common.base.Objects;

public class PerspectiveModel {

	private IPerspectiveDescriptor perspective;

	private List<WorkbenchElementModel> workbenchElementModels;
	
	private WorkbenchModel parent;

	public PerspectiveModel() {
	}

	public PerspectiveModel(IPerspectiveDescriptor perspective, WorkbenchModel parent) {
		setPerspective(perspective);
		setParent(parent);
	}
	
	public IPerspectiveDescriptor getPerspective() {
		return perspective;
	}

	public void setPerspective(IPerspectiveDescriptor perspective) {
		this.perspective = perspective;
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

	public WorkbenchModel getParent() {
		return parent;
	}

	public void setParent(WorkbenchModel parent) {
		this.parent = parent;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("perspective", getPerspective().getLabel()).toString();
	}

}
