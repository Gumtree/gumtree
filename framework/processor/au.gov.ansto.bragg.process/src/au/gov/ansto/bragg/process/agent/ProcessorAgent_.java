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
package au.gov.ansto.bragg.process.agent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import au.gov.ansto.bragg.process.common.Common;
import au.gov.ansto.bragg.process.configuration.AgentConfiguration;
import au.gov.ansto.bragg.process.exception.IndexOutOfBoundException;
import au.gov.ansto.bragg.process.exception.NullPrincipalException;
import au.gov.ansto.bragg.process.exception.ProcessFailedException;
import au.gov.ansto.bragg.process.exception.ProcessorChainException;
import au.gov.ansto.bragg.process.port.Tuner;
import au.gov.ansto.bragg.process.port.Var;
import au.gov.ansto.bragg.process.processor.Framework;
import au.gov.ansto.bragg.process.processor.Processor;
import au.gov.ansto.bragg.process.processor.Sink;
import au.gov.ansto.bragg.process.processor.Processor_.ProcessorStatus;
import au.gov.ansto.bragg.process.util.SortedArrayList;

/**
 * @author nxi
 * Created on 23/02/2007, 4:47:13 PM
 * Last modified 23/02/2007, 4:47:13 PM
 * 
 */
public class ProcessorAgent_ extends Agent_ implements ProcessorAgent {

	public final static long serialVersionUID = 1L;
//	Processor principal = null;
	
	public ProcessorAgent_(AgentConfiguration configuration){
		super(configuration);
	}
	
	protected Processor getPrincipal() throws NullPrincipalException{
		return (Processor) principal;
	}
	
	public void setPrincipal(Framework framework) throws IndexOutOfBoundException, NullPrincipalException{
		super.setPrincipal(SortedArrayList.getProcessorFromName(framework.getProcessorArray(), pName));
		getPrincipal().setAgent(this);
	}

	public String getStatus() {
//		return getPrincipal().isRunning()?"Running":"Stop";
		try {
			return getPrincipal().getStatus().name();
		} catch (NullPrincipalException e) {
			return "Error";
		}
	}
	
	public String toString(){
		String result = "<agent id=\"" + getID() + "\" name=\"" + getName() + "\" principal=\"" + getPrincipalName() + "\">\n";
//		result += "<receipe_id>" + getReceipeID() + "</receipe_id>\n";
		result += "<description>" + getDescription() + "</description>\n";
		result += "<pname>" + getPName() + "</pname>\n";
		if (principal != null){
		result += "<status>" + ((Processor) principal).getStatus() + "</status>\n";
		result += "<signal>" + ((Processor) principal).getSignal().toString() + "</signal>\n";
		}
		result += "</agent>\n";
		return result;
	}
	
	public List<Tuner> getTuners(){
		List<Tuner> tunerList = new ArrayList<Tuner>();
		List<Var> varList = ((Processor) principal).getVarList();
		for (Iterator<Var> varIter = varList.iterator(); varIter.hasNext();){
			Var var = varIter.next();
			if (!var.getUsage().equals("hidden"))
				tunerList.add(var.getTuner());
		}
		return tunerList;
	}

	private List<Tuner> getTuners(String usage){
		List<Tuner> tunerList = new ArrayList<Tuner>();
		List<Var> varList = ((Processor) principal).getVarList();
		for (Iterator<Var> varIter = varList.iterator(); varIter.hasNext();){
			Var var = varIter.next();
			if (!var.getUsage().equals("hidden")){
				Tuner tuner = var.getTuner();
				if (tuner != null && tuner.getUsage().equals(usage)) 
					tunerList.add(tuner);
			}
		}
		return tunerList;
	}
	
	public Tuner getTuner(String tunerName){
		List<Var> varList = ((Processor) principal).getVarList();
		for (Iterator<Var> varIter = varList.iterator(); varIter.hasNext();){
			Var var = varIter.next();
			Tuner tuner = var.getTuner();
			if (tuner.getName().equals(tunerName))
				return tuner;
		}
		return null;
	}
	
	public List<Tuner> getOptions() {
		return getTuners("option");
	}

	public List<Tuner> getParameters() {
		return getTuners("parameter");
	}
	
	public void setPrincipal(Common principal) {
		super.setPrincipal(principal);
		((Processor) principal).setAgent(this);
	}

	public void trigger() throws ProcessFailedException, ProcessorChainException {
		try {
			getPrincipal().triggerFromAgent();
		} catch (NullPrincipalException e) {
			throw new ProcessorChainException("can not find processor " + getPrincipalName());
		}
	}

	public List<Sink> getSinkList() {
		if (principal != null){
//			List<Sink> sinkList = ((Processor) principal).getSinkList();
//			for (Iterator iterator = sinkList.iterator(); iterator.hasNext();) {
//				Sink sink = (Sink) iterator.next();
//				Object structure = sink.getProperty("dataStructureType");
//				if (structure != null)
//					System.out.println("sink type of " + sink.getName() + " : " + structure.toString());				
//				Object property = sink.getProperty("dataDimensionType");
//				if (property != null)
//					System.out.println("sink type of " + sink.getName() + " : " + property.toString());
//			}
			return ((Processor) principal).getSinkList();
		}
		return null;
	}

	public List<Sink> getAutoPlotSinkList(){
		final List<Sink> sinkList = getSinkList();
		List<Sink> autoPlotSinkList = new ArrayList<Sink>();
		for (Sink sink : sinkList){
			if (sink.isAutoPlot())
				autoPlotSinkList.add(sink);
		}
		return autoPlotSinkList;
	}
	
	public List<Tuner> getRegionTuners() {
		return getTuners("region");
	}

	public String getVersion(){
		return ((Processor) principal).getVersion();
	}

	public ProcessorStatus getProcessorStatus() {
		try {
			return getPrincipal().getStatus();
		} catch (NullPrincipalException e) {
			e.printStackTrace();
		}
		return ProcessorStatus.Error;
	}
	
	public void setInterruptStatus(){
		try {
			getPrincipal().setInterruptStatus();
		} catch (NullPrincipalException e) {
			e.printStackTrace();
		}
	}

	public boolean isReprocessable() {
		return ((Processor) principal).isReprocessable();
	}
	
}
