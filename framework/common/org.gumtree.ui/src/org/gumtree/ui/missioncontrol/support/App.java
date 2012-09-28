package org.gumtree.ui.missioncontrol.support;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.gumtree.ui.missioncontrol.IApp;

public class App implements IApp {

	private String label;
	
	public App() {
		this("");
	}
	
	public App(String label) {
		this.label = label;
	}
	
	@Override
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	@Override
	public Control creatWidget(Composite parent) {
		return null;
	}
	
}
