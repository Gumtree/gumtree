package org.gumtree.ui.dashboard.model;

import java.util.ArrayList;
import java.util.List;

public class Dashboard {

	private String title;
	
	private String layoutConstraints;
	
	private String rowConstraints;
	
	private String colConstraints;
	
	private List<Widget> widgets;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLayoutConstraints() {
		return layoutConstraints;
	}

	public void setLayoutConstraints(String layoutConstraints) {
		this.layoutConstraints = layoutConstraints;
	}

	public String getRowConstraints() {
		return rowConstraints;
	}

	public void setRowConstraints(String rowConstraints) {
		this.rowConstraints = rowConstraints;
	}

	public String getColConstraints() {
		return colConstraints;
	}

	public void setColConstraints(String colConstraints) {
		this.colConstraints = colConstraints;
	}

	public List<Widget> getWidgets() {
		if (widgets == null) {
			widgets = new ArrayList<Widget>();
		}
		return widgets;
	}
	
}
