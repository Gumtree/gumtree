package org.gumtree.workflow.ui.config;

import org.gumtree.util.PropertiesHelper;
import org.gumtree.util.collection.IParameters;
import org.gumtree.util.collection.Parameters;

import com.google.common.base.Objects;

public class TaskConfig {

	private IParameters parameters;
	
	private String classname;
	
	private Object dataModel;
	
	public TaskConfig() {
		super();
	}

	public TaskConfig(String classname) {
		super();
		setClassname(classname);
	}
	
	public String getClassname() {
		return classname;
	}

	public void setClassname(String classname) {
		this.classname = PropertiesHelper.substitueWithProperties(classname);
	}

	public Object getDataModel() {
		return dataModel;
	}

	public void setDataModel(Object dataModel) {
		this.dataModel = dataModel;
	}

	public IParameters getParameters() {
		if (parameters == null) {
			parameters = new Parameters();
		}
		return parameters;
	}

	public void setParameters(IParameters parameters) {
		this.parameters = parameters;
	}
	
	public String toString() {
		return Objects.toStringHelper(this).add("classname", getClassname())
				.toString();
	}
	
}
