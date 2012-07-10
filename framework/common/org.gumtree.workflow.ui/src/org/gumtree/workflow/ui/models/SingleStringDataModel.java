package org.gumtree.workflow.ui.models;

public class SingleStringDataModel extends AbstractModelObject {

	private String string;
	
	public SingleStringDataModel() {
		super();
	}
	
	public SingleStringDataModel(String string) {
		super();
		this.string = string;
	}
	
	public String getString() {
		if (string == null) {
			string = "";
		}
		return string;
	}

	public void setString(String string) {
		String oldValue = this.string;
		this.string = string;
		firePropertyChange("string", oldValue, string);
	}
	
}
