package org.gumtree.ui.dashboard.model;

import org.gumtree.util.collection.IParameters;
import org.gumtree.util.collection.Parameters;

public class Widget {

	private String classname;
	
	// optional
	private String id;
	
	// optional
	private int style;
	
	// optional
	private String title;
	
	private boolean hideTitleBar;
	
	private boolean collapsible;
	
	private boolean collapsed;
	
	private String layoutData;
	
	private String listenToWidgets;
	
	private IParameters parameters;
	
	public String getClassname() {
		return classname;
	}

	public void setClassname(String classname) {
		this.classname = classname;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getStyle() {
		return style;
	}

	public void setStyle(int style) {
		this.style = style;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isHideTitleBar() {
		return hideTitleBar;
	}

	public void setHideTitleBar(boolean hideTitleBar) {
		this.hideTitleBar = hideTitleBar;
	}

	public String getLayoutData() {
		return layoutData;
	}

	public void setLayoutData(String layoutData) {
		this.layoutData = layoutData;
	}

	public boolean isCollapsible() {
		return collapsible;
	}

	public void setCollapsible(boolean collapsible) {
		this.collapsible = collapsible;
	}

	public boolean isCollapsed() {
		return collapsed;
	}

	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
	}

	public IParameters getParameters() {
		if (parameters == null) {
			parameters = new Parameters();
		}
		return parameters;
	}

	public String getListenToWidgets() {
		return listenToWidgets;
	}

	public void setListenToWidgets(String listenToWidgets) {
		this.listenToWidgets = listenToWidgets;
	}
	
}
