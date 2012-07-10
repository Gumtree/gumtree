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

public class FrameworkConfiguration_ extends CompositeProcessorConfiguration_
		implements FrameworkConfiguration {

	public final static long serialVersionUID = 1L;
	
	protected List<AgentConfiguration> agentConfigurationList = null;
	protected List<SourceConfiguration> sourceConfigurationList = null;
	protected List<SinkConfiguration> sinkConfigurationList = null;
	
	public FrameworkConfiguration_() {
		super(NAME, PARENT_NAME, CLASS_TYPE);
		sourceConfigurationList = new LinkedList<SourceConfiguration>();
		sinkConfigurationList = new LinkedList<SinkConfiguration>();
		// TODO Auto-generated constructor stub
	}
	
	public FrameworkConfiguration_(String name) {
		super(name, PARENT_NAME, CLASS_TYPE);
		sourceConfigurationList = new LinkedList<SourceConfiguration>();
		sinkConfigurationList = new LinkedList<SinkConfiguration>();
		// TODO Auto-generated constructor stub
	}
	
	public FrameworkConfiguration_(String name, String parentName) {
		super(name, parentName, CLASS_TYPE);
		sourceConfigurationList = new LinkedList<SourceConfiguration>();
		sinkConfigurationList = new LinkedList<SinkConfiguration>();
		// TODO Auto-generated constructor stub
	}

//	public FrameworkConfiguration_(String name) {
//		super(ID, name, PARENT_NAME, CLASS_TYPE);
//		sourceConfigurationList = new LinkedList<SourceConfiguration>();
//		sinkConfigurationList = new LinkedList<SinkConfiguration>();
//		// TODO Auto-generated constructor stub
//	}
	
	public void addSinkConfiguration(SinkConfiguration configuration) {
		// TODO Auto-generated method stub
		sinkConfigurationList.add(configuration);
	}
	
	public void addSourceConfiguration(SourceConfiguration configuration) {
		// TODO Auto-generated method stub
		sourceConfigurationList.add(configuration);
	}

	protected String agentToString(){
		String result = "<agents>\n";
		AgentConfiguration configuration = null;
		for (Iterator<AgentConfiguration> iter = agentConfigurationList.iterator(); iter.hasNext();){
			configuration = iter.next();
			result += "<agent id=\"" + configuration.getID() + "\" name=\"" + configuration.getName() 
			+ "\" principal=\"" + configuration.getPrincipal() + "_" + configuration.getPName() + "\"/>\n";
		}
		result += "</agents>\n";
		return result;
	}
	
	public List<AgentConfiguration> getAgentConfigurationList(){
		return agentConfigurationList;
	}

	public List<SinkConfiguration> getSinkConfigurationList() {
		// TODO Auto-generated method stub
		return sinkConfigurationList;
	}

	public List<SourceConfiguration> getSourceConfigurationList() {
		// TODO Auto-generated method stub
		return sourceConfigurationList;
	}

	public void setAgentConfigurationList(List<AgentConfiguration> agentConfigurationList){
		this.agentConfigurationList = agentConfigurationList;
	}

	protected String sinkToString(){
		String result = "<sink>\n";
		for (Iterator<SinkConfiguration> iter = sinkConfigurationList.iterator(); iter.hasNext();){
			result += iter.next().toString();
		}
		result += "</sink>\n";
		return result;
	}

	protected String sourceToString(){
		String result = "<sources>\n";
		for (Iterator<SourceConfiguration> iter = sourceConfigurationList.iterator(); iter.hasNext();){
			result += iter.next().toString();
		}
		result += "</sources>\n";
		return result;
	}

	public String toString(){
		String result = "<framework>\n";
		result += portsToString();
		result += processorsToString();
		result += connectorsToString();
		result += agentToString();
		result += sourceToString();
		result += sinkToString();
		result += "</framework>\n";
		return result;
	}
}
