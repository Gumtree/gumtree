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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import au.gov.ansto.bragg.process.configuration.ProcessorConfiguration;
import au.gov.ansto.bragg.process.exception.ProcessorChainException;
import au.gov.ansto.bragg.process.port.Port;
/**
 * Child class of Processor. A processor that can nest other processors inside.
 * <p>
 * A CompositeProcessor instance is built from an algorithm recipe xml file. It 
 * usually contains multiple processor chains or other composite processors. 
 * A composite processor do not do processing directly. Instead it will triger 
 * its nested processor to do the processing. 
 * <p>
 * A composite process has its own In, Out and Var ports, which will passing
 * their signals to their consumers or taking signals from their producers 
 * respectively.
 * <p>
 * Created on 20/02/2007, 9:58:24 AM
 * <p>
 * Last modified 17/04/2007, 9:58:24 AM
 *
 * @author nxi
 * @version V1.0
 * @since M1
 * @see Processor
 * @see Framework
 */
public class CompositeProcessor_ extends Processor_ implements CompositeProcessor {

	public static final long serialVersionUID = 1L;

	protected List<Processor> processorList = new LinkedList<Processor>();
	protected int numberOfProcessors = 0;

	/*	public CompositeProcessor_(final CompositeProcessorConfiguration_ configuration, final Processor_ parent) {
		super(configuration, parent);
		createProcessorList(configuration.getProcessorConfigurationList());
		System.out.println("Composite processor created from composite." + configuration.getID() + "\n");
	}
	 */

	public CompositeProcessor_(final ProcessorConfiguration configuration, final Processor parent) 
	throws ProcessorChainException	{
		super(configuration, parent);
		createProcessorList(configuration.getProcessorConfigurationList());
	}

	@Override
	public void addAllProcessors(List<Processor> processorArray, List<Port> portArray){
		addAllPorts(portArray);
		processorArray.add(this);
		if (processorList != null){
			for (Iterator<Processor> iter = processorList.iterator(); iter.hasNext();){
				iter.next().addAllProcessors(processorArray, portArray);
			}
		}
	}

	public void addProcessor(Processor processor) {
		processorList.add(processor);
		numberOfProcessors++;
	}

	protected void createProcessorList(final List<ProcessorConfiguration> configurationList) 
	throws ProcessorChainException 
	{
		if (configurationList == null) return;
		Iterator<ProcessorConfiguration> iter = configurationList.iterator();
		ProcessorConfiguration processorConfiguration = null;
		Processor_ processor = null;
		CompositeProcessor_ compositeProcessor = null;
		String processorType = null;
		while(iter.hasNext()){			
			processorConfiguration = iter.next(); 
			processorType = processorConfiguration.getConfigurationType();
			if (processorType == "processor configuration"){
				processor = new Processor_(processorConfiguration, this);
				processorList.add(processor);
			}
			if (processorType == "composite processor configuration"){
				compositeProcessor = new CompositeProcessor_(processorConfiguration, this);
				processorList.add(compositeProcessor);
			}
		}
	}

	public List<Processor> getProcessorList() {
		return processorList;
	}

	@Override
	public String getProcessorType(){
		return "composite processor";
	}

	protected String processorsToString(){
		String result = "";
		if (getProcessorList() != null){
			for (Iterator<Processor> iter = getProcessorList().iterator(); iter.hasNext();){
				result += iter.next().toString();			
			}		
		}
		return result;
	}

	protected String processorsToString(int level){
		String result = "";
		if (getProcessorList() != null){
			for (Iterator<Processor> iter = getProcessorList().iterator(); iter.hasNext();){
				result += iter.next().toString(level - 1);			
			}		
		}
		return result;
	}

	/*
	public void createConnection(final ProcessorConfiguration_ configuration, final List<Port> portArray){
		List<ConnectorConfiguration_> connectorList = configuration.getConnectorConfigurationList();
		for (Iterator<ConnectorConfiguration_> iter = connectorList.iterator(); iter.hasNext();){
			ConnectorConfiguration_ connector = iter.next();
			int producerID = connector.getProducer();
			int consumerID = connector.getConsumer();
			Port producer = (Port) SortedArrayList.get(portArray, producerID);
			Port consumer = (Port) SortedArrayList.get(portArray, consumerID);
			producer.addConsumer(consumer);
			consumer.setProducer(producer);
		}
		List<ProcessorConfiguration_> processorConfigurationList = configuration.getProcessorConfigurationList();
		for (Iterator<Processor> iter = getProcessorList().iterator(); iter.hasNext();){
			iter.next().createConnection(, portArray);
		}		
	}
	 */

	protected void setClazz(String classType) throws ClassNotFoundException{
//		System.out.println(classType);
		/*
		 * The clazz property of a composite processor is not necessary.
		 * Just use CompositeProcessor to set it up.
		 */
		clazz = CompositeProcessor.class;
//		clazz = Class.forName(classType);
//			clazz = Class.forName("au.gov.ansto.bragg.process.util.Adder");
		/*
		Method method = null;
		Object obj = null;
		try{
			obj = clazz.newInstance();
		}catch (Exception ex){
			System.out.println("creating new instance failed." + ex.getMessage());
		}

		try{
		method = obj.getClass().getMethod("aMethod", Double.class);
		}catch (Exception ex){
			System.out.println("Loading method failed." + ex.getMessage());
		}
		Double x = 33.;
		try{
		y = (Double) method.invoke(clazz.getClass(), new Object[] {x});
		}catch (Exception ex){
			System.out.println("Invoking method failed." + ex.getMessage());
		}
		 */
	}

	public String toString(){
		String result = "<composite_processor id=\"" + getID() + "\" name=\"" + getName() +"\" class=\"" + getClassType().getName() + "\">\n";
		result += portsToString();
		result += processorsToString();
		result += "</composite_processor>\n";
		return result;
	}

	public String toString(int level){
		String result = "<composite_processor id=\"" + getID() + "\" name=\"" + getName() +"\" class=\"" + getClassType().getName() + "\">\n";
//		result += "<receipe_id>" + getRecipeID() + "</receipe_id>\n";
		result += portsToString();
		if (level > 0){ 
			result += processorsToString(level);
		}
		result += "</composite_processor>\n";
		return result;
	}

	@Override
	public void transfer() throws ProcessorChainException {
		resetInToken();
	}
	
	@Override
	protected void createInstance(){}
	
	@Override
	public void setField(String fieldName, Object signal, Class<?> type) {}
	
	@Override
	public Object getField(String fieldName) {
		return null;
	}
	
	public List<Sink> getSinkList() {
		List<Sink> sinkList = super.getSinkList();
		for (Iterator<Processor> iter = processorList.iterator(); iter.hasNext();){
			Processor processor = iter.next();
			List<Sink> subSinkList = processor.getSinkList();
			if (subSinkList.size() > 0) sinkList.addAll(subSinkList);
		}
		return sinkList;
	}
	
	public void dispose(){
		super.dispose();
		if (processorList != null){
			for (Processor processor : processorList){
				processor.dispose();
			}
			processorList.clear();
		}
	}
}
