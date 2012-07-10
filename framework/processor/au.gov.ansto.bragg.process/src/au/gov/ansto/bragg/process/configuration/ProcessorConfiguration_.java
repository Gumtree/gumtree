/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
*******************************************************************************/
package au.gov.ansto.bragg.process.configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ProcessorConfiguration_ extends Configuration_ implements
		ProcessorConfiguration {

	public final static long serialVersionUID = 1L;
	
	protected String classType;
	protected String parentName = DEFAULT_PARENTNAME;
//	protected List<String> methodNameList;
	protected List<InConfiguration> inConfigurationList;
	protected List<OutConfiguration> outConfigurationList;
	protected List<VarConfiguration> varConfigurationList;
	protected String methodName;
	protected String versionNumber;

	public ProcessorConfiguration_(){
		super();
		initList();
	}
	
	private void initList() {
		// TODO Auto-generated method stub
		inConfigurationList = new ArrayList<InConfiguration>();
		outConfigurationList = new ArrayList<OutConfiguration>();
		varConfigurationList = new ArrayList<VarConfiguration>();
	}

	public ProcessorConfiguration_(String name){
		super(name);
		initList();
	}

	public ProcessorConfiguration_(final String name, final String parentName){
		this(name);
		setParentName(parentName);
	}
	
	public ProcessorConfiguration_(final String name, final String parentName, final String classType){
		this(name);
		setParentName(parentName);
		setClassType(classType);
	}
	
	public ProcessorConfiguration_(final String name, final String parentName, final String classType	, final String methodName){
		this(name);
		setParentName(parentName);
		setClassType(classType);
		setMethodName(methodName);
	}
	
	public void addInConfiguration(final InConfiguration inConfiguration) {
		// TODO Auto-generated method stub
		inConfigurationList.add(inConfiguration);
	}
	
	public void addOutConfiguration(final OutConfiguration outConfiguration) {
		// TODO Auto-generated method stub
		outConfigurationList.add(outConfiguration);
	}
	
	public void addVarConfiguration(final VarConfiguration varConfiguration) {
		// TODO Auto-generated method stub
		varConfigurationList.add(varConfiguration);
	}
	
	public String getClassType() {
		// TODO Auto-generated method stub
		return classType;
	}

/*	
	public void addMethodName(final String methodName) {
		// TODO Auto-generated method stub
		methodNameList.add(methodName);
	}
*/
	
	public String getConfigurationType(){
		return "processor configuration";
	}

	public List<ConnectorConfiguration> getConnectorConfigurationList() {
		return null;
	}
	
	public List<InConfiguration> getInConfigurationList() {
		// TODO Auto-generated method stub
		return inConfigurationList;
	}

	public String getMethodName(){
		return methodName;
	}
	
	public List<OutConfiguration> getOutConfigurationList() {
		// TODO Auto-generated method stub
		return outConfigurationList;
	}

	public String getParentName(){
		return parentName;
	}
	
	public List<ProcessorConfiguration> getProcessorConfigurationList() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<VarConfiguration> getVarConfigurationList() {
		// TODO Auto-generated method stub
		return varConfigurationList;
	}
	
	public void setAgentConfigurationList(List<AgentConfiguration> agentConfigurationList){};

	protected String portsToString(){
		String result = super.toString();
		result += "<class>" + this.getClassType() + "</class>\n"; 
		result += "<parent_name>" + getParentName() + "</parent_name>\n";
		result += "<ins>\n";
		for (Iterator<InConfiguration> iter = inConfigurationList.iterator(); iter.hasNext();){
			InConfiguration configuration = iter.next();
			result += configuration.toString();
		}
		result += "</ins>\n";
		result += "<outs>\n";
		for (Iterator<OutConfiguration> iter = outConfigurationList.iterator(); iter.hasNext();){
			OutConfiguration configuration = iter.next();
			result += configuration.toString();
		}
		result += "</outs>\n";
		result += "<vars>\n";
		for (Iterator<VarConfiguration> iter = varConfigurationList.iterator(); iter.hasNext();){
			VarConfiguration configuration = iter.next();
			result += configuration.toString();
		}
		result += "</vars>\n";
		return result;		
	}

	public void setClassType(final String classType) {
		// TODO Auto-generated method stub
		this.classType = classType;
	}
	
	public void setInConfigurationList(List<InConfiguration> inConfigurationList){
		this.inConfigurationList = inConfigurationList;
	}
	
	public void setMethodName(final String methodName) {
		this.methodName = methodName;
	}

	public void setOutConfigurationList(List<OutConfiguration> outConfigurationList){
		this.outConfigurationList = outConfigurationList;
	}
	
	public void setParentName(String parentName){
		this.parentName = parentName;
	}
	
	public void setVarConfigurationList(List<VarConfiguration> varConfigurationList){
		this.varConfigurationList = varConfigurationList;
	}
	
	public String toString(){
		String result = "<processor_configuration>\n";
		result += portsToString();
		result += "</processor_configuration>\n";
		return result;
	}

	public String getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(String versionNumber) {
		this.versionNumber = versionNumber;
	}
}
