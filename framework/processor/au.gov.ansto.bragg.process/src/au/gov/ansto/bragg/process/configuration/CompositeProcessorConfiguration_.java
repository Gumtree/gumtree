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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CompositeProcessorConfiguration_ extends ProcessorConfiguration_
		implements CompositeProcessorConfiguration {

	public final static long serialVersionUID = 1L;
	
	protected List<ProcessorConfiguration> processorConfigurationList = null;
	protected List<ConnectorConfiguration> connectorConfigurationList = null;
	
	public CompositeProcessorConfiguration_() {
		// TODO Auto-generated constructor stub
		super();
		processorConfigurationList = new LinkedList<ProcessorConfiguration>();
		connectorConfigurationList = new LinkedList<ConnectorConfiguration>();
	}

	public CompositeProcessorConfiguration_(final String name) {
		super(name);
		processorConfigurationList = new LinkedList<ProcessorConfiguration>();
		connectorConfigurationList = new LinkedList<ConnectorConfiguration>();
		// TODO Auto-generated constructor stub
	}

	public CompositeProcessorConfiguration_(final String name, final String parentName) {
		super(name);
		setParentName(parentName);
		processorConfigurationList = new LinkedList<ProcessorConfiguration>();
		connectorConfigurationList = new LinkedList<ConnectorConfiguration>();
		// TODO Auto-generated constructor stub
	}

	public CompositeProcessorConfiguration_(final String name, final String parentName, final String classType) {
		super(name);
		setParentName(parentName);
		processorConfigurationList = new LinkedList<ProcessorConfiguration>();
		connectorConfigurationList = new LinkedList<ConnectorConfiguration>();
		setClassType(classType);
		// TODO Auto-generated constructor stub
	}

	public void addConnectorConfiguration(ConnectorConfiguration configuration){
		connectorConfigurationList.add(configuration);
	}
	
	public void addProcessorConfiguration(ProcessorConfiguration configuration) {
		// TODO Auto-generated method stub
		processorConfigurationList.add(configuration);
	}

	/*
	public void addSourceConfiguration(ProcessorConfiguration configuration) {
		// TODO Auto-generated method stub
//		processorConfigurationList.add(configuration);
	}
	*/

	public void setConnectorConfigurationList(List<ConnectorConfiguration> connectorConfigurationList){
		this.connectorConfigurationList = connectorConfigurationList;
	}
	
	public List<ConnectorConfiguration> getConnectorConfigurationList() {
		// TODO Auto-generated method stub
		return connectorConfigurationList;
	}
	
	public List<ProcessorConfiguration> getProcessorConfigurationList() {
		// TODO Auto-generated method stub
		return processorConfigurationList;
	}

	/*
	public List<SourceConfiguration> getSourceConfigurationList() {
		// TODO Auto-generated method stub
		return null;
	}
	*/

	/*
	public List<SinkConfiguration> getSinkConfigurationList() {
		// TODO Auto-generated method stub
		return null;
	}
	*/

	/*
	public List<AgentConfiguration> getAgentConfigurationList(){
		return new ArrayList<AgentConfiguration>();
	}
	*/
	
	public String getConfigurationType(){
		return "composite processor configuration";
	}

	protected String processorsToString(){
		String result = "";
		for (Iterator<ProcessorConfiguration> iter = processorConfigurationList.iterator(); iter.hasNext();){
			result += iter.next().toString();
		}
		return result;
	}

	protected String connectorsToString(){
		String result = "<connectors>\n";
		for (Iterator<ConnectorConfiguration> iter = connectorConfigurationList.iterator(); iter.hasNext();){
			result += iter.next().toString();
		}
		result += "</connectors>\n";
		return result;
	}
	
	public String toString(){
		String result = "<composite_processor_configuration>\n";
		result += portsToString();
		result += processorsToString();
		result += connectorsToString();
		result += "</composite_processor_configuration>\n";
		return result;
	}
}
