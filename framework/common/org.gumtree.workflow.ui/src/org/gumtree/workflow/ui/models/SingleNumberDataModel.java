package org.gumtree.workflow.ui.models;

public class SingleNumberDataModel extends AbstractModelObject {

	private Number number;
	
	public SingleNumberDataModel() {
		super();
	}
	
	public SingleNumberDataModel(Number defaultValue) {
		this();
		number = defaultValue; 
	}
	
	public Number getNumber() {
		if (number == null) {
			number = 0;
		}
		return number;
	}

	public void setNumber(Number number) {
		Number oldValue = this.number;
		this.number = number;
		firePropertyChange("number", oldValue, number);
	}
	
}
