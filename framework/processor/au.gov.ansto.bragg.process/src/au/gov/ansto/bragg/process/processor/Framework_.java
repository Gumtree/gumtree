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
package au.gov.ansto.bragg.process.processor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import au.gov.ansto.bragg.process.agent.Agent;
import au.gov.ansto.bragg.process.agent.PortAgent_;
import au.gov.ansto.bragg.process.agent.ProcessorAgent_;
import au.gov.ansto.bragg.process.configuration.AgentConfiguration;
import au.gov.ansto.bragg.process.configuration.ConnectorConfiguration;
import au.gov.ansto.bragg.process.configuration.FrameworkConfiguration;
import au.gov.ansto.bragg.process.configuration.ProcessorConfiguration;
import au.gov.ansto.bragg.process.configuration.SinkConfiguration;
import au.gov.ansto.bragg.process.configuration.SourceConfiguration;
import au.gov.ansto.bragg.process.exception.IndexOutOfBoundException;
import au.gov.ansto.bragg.process.exception.ProcessorChainException;
import au.gov.ansto.bragg.process.port.Port;
import au.gov.ansto.bragg.process.util.SortedArrayList;

public class Framework_ extends CompositeProcessor_ implements Framework {

	public static final long serialVersionUID = 1L;

	protected List<Port> portArray;
	protected List<Processor> processorArray;
	protected List<Agent> agentList;
	protected List<Source> sourceList;
	protected List<Sink> sinkList;

	public Framework_(FrameworkConfiguration configuration) throws ProcessorChainException {
		super(configuration, null);
		setParent(this);
		createSourceList(configuration.getSourceConfigurationList());
		createSinkList(configuration.getSinkConfigurationList());
		createPropertyArray();
		createConnection(configuration, getPortArray());
		createAgentList(configuration.getAgentConfigurationList());
	}		

	public void addAllProcessors(List<Processor> processorArray, List<Port> portArray){
		addAllPorts(portArray);
		if (sourceList != null){
			for (Iterator<Source> iter = sourceList.iterator(); iter.hasNext();){
				iter.next().addAllPorts(portArray);
			}
		}
		if (processorList != null){
			for (Iterator<Processor> iter = processorList.iterator(); iter.hasNext();){
				iter.next().addAllProcessors(processorArray, portArray);
			}
		}
		if (sinkList != null){
			for (Iterator<Sink> iter = sinkList.iterator(); iter.hasNext();){
				iter.next().addAllPorts(portArray);
			}
		}
	}

	protected void createAgentList(List<AgentConfiguration> agentConfigurationList){
		AgentConfiguration configuration = null;
		agentList = new ArrayList<Agent>();
		Agent agent = null;
		if (agentConfigurationList == null)
			return;
		for (Iterator<AgentConfiguration> iter = agentConfigurationList.iterator(); iter.hasNext();){
			configuration = iter.next();
//			System.out.println(configuration.getPrincipal());
			if (configuration.getPrincipal().equals("ProcessorAgent"))
				agent = new ProcessorAgent_(configuration);
			if (configuration.getPrincipal().matches("PortAgent"))
				agent = new PortAgent_(configuration);
//			System.out.println(agent.getID());
			agentList.add(agent);
		}
	}

	protected void createConnection(final ProcessorConfiguration configuration, final List<Port> portArray) 
	throws ProcessorChainException 
	{
		if (configuration.getConfigurationType() == "processor configuration") return;
		if (configuration.getConfigurationType() == "composite processor configuration"){
			List<ConnectorConfiguration> connectorList = configuration.getConnectorConfigurationList();
			for (Iterator<ConnectorConfiguration> iter = connectorList.iterator(); iter.hasNext();){
				ConnectorConfiguration connector = iter.next();
				String producerID = connector.getProducer();
				String consumerID = connector.getConsumer();
				Port producer = null;
				Port consumer = null;
				try {
					producer = SortedArrayList.getPortFromReceipeName(portArray, producerID);
				} catch (IndexOutOfBoundException e) {
					throw new ProcessorChainException("can not find port " + producerID + ": " + 
							e.getMessage(), e);
				}
				try {
					consumer = SortedArrayList.getPortFromReceipeName(portArray, consumerID);
				} catch (IndexOutOfBoundException e) {
					throw new ProcessorChainException("can not find port " + consumerID + ": " + 
							e.getMessage(), e);
				}
				producer.addConsumer(consumer);
				consumer.setProducer(producer);
			}
			List<ProcessorConfiguration> processorConfigurationList = configuration.getProcessorConfigurationList();
			for (Iterator<ProcessorConfiguration> iter = processorConfigurationList.iterator(); iter.hasNext();){
				createConnection(iter.next(), portArray);
			}		
		}
	}

	protected void createPropertyArray(){
		portArray = new ArrayList<Port>();
		processorArray = new ArrayList<Processor>();
		this.addAllProcessors(processorArray, portArray);
	}

	protected void createSinkList(List<SinkConfiguration> sinkConfigurationList) 
	throws ProcessorChainException{
		SinkConfiguration configuration = null;
		sinkList = new ArrayList<Sink>();
		Sink sink = null;
		for (Iterator<SinkConfiguration> iter = sinkConfigurationList.iterator(); iter.hasNext();){
			configuration = iter.next();
			sink = new Sink_(configuration, this);
			sinkList.add(sink);
		}
	}

	protected void createSourceList(List<SourceConfiguration> sourceConfigurationList) 
	throws ProcessorChainException {
		SourceConfiguration configuration = null;
		sourceList = new ArrayList<Source>();
		Source source = null;
		for (Iterator<SourceConfiguration> iter = sourceConfigurationList.iterator(); iter.hasNext();){
			configuration = iter.next();
			source = new Source_(configuration, this);
			sourceList.add(source);
		}
	}

	public List<Agent> getAgentList(){
		return agentList;
	}

	public List<Port> getPortArray(){
		return portArray;
	}

	public List<Processor> getProcessorArray(){
		return processorArray;
	}

	public List<Sink> getSinkList(){
		return sinkList;
	}

	public List<Source> getSourceList(){
		return sourceList;
	}

	protected String sinkToString(){
		String result = "";
		if (sinkList != null){
			for (Iterator<Sink> iter = sinkList.iterator(); iter.hasNext();){
				result += iter.next().toString();
			}
		}
		return result;
	}

	protected String sourceToString(){
		String result = "";
		if (sourceList != null){
			for (Iterator<Source> iter = sourceList.iterator(); iter.hasNext();){
				result += iter.next().toString();
			}
		}
		return result;
	}

	public String toString(){
		String result = "<framework>\n" + sourceToString() + portsToString() + processorsToString() + sinkToString();
		result += "</framework>\n";
		return result;
	}

	public Sink getDefaultSink(){
		if (sinkList != null && sinkList.size() > 0){
			for (Sink sink : sinkList){
				if (sink.isDefault())
					return sink;
			}
			return sinkList.get(sinkList.size() - 1);
		}
		return null;
	}
	
	public void dispose(){
		super.dispose();
		portArray.clear();
		for (Processor processor : processorArray)
			processor.dispose();
		processorArray.clear();
		agentList.clear();
		if (sourceList != null){
			for (Source source : sourceList)
				source.dispose();
			sourceList.clear();
		}
		if (sinkList != null){
			for (Sink sink : sinkList)
				sink.dispose();
			sinkList.clear();
		}
	}
}
