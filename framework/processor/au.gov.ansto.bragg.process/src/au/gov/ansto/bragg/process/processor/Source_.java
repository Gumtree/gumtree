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

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import au.gov.ansto.bragg.process.common.exception.IllegalNameSetException;
import au.gov.ansto.bragg.process.configuration.SourceConfiguration;
import au.gov.ansto.bragg.process.exception.DimensionOutOfRangeException;
import au.gov.ansto.bragg.process.exception.NullMethodException;
import au.gov.ansto.bragg.process.exception.ProcessFailedException;
import au.gov.ansto.bragg.process.exception.ProcessorChainException;
import au.gov.ansto.bragg.process.port.In;
import au.gov.ansto.bragg.process.port.Out;
import au.gov.ansto.bragg.process.port.Var;


public class Source_ extends Processor_ implements Source {

	public final static long serialVersionUID = 1L;

	public Source_(){
		super();
	}

	public Source_(final SourceConfiguration configuration, Processor_ parent) 
	throws ProcessorChainException {
		super(configuration, parent);
	}

	public void loadMethod() throws ProcessorChainException {
		String methodName = "getSource";
		setMethod(methodName);
	}

	/*
	protected void load() throws NullMethodException{
		if (getMethod() == null){
			throw new NullMethodException("Load method failed.");
		}else{
			try{
				instance = clazz.newInstance();
			}catch(Exception ex){
				System.out.println("create instance failed. " + ex.getMessage());
			}
			Object[] argumentArray = new Object[getInList().size() + getVarList().size() +1];
			int index = 0;
			for (Iterator<In_> iter = getInList().iterator(); iter.hasNext();){
				argumentArray[index] = iter.next().getSignal();
				index++;
			}

			for (Iterator<Var_> iter = getVarList().iterator(); iter.hasNext();){
				argumentArray[index] = iter.next().getSignal();
				index++;
			}

			argumentArray[index] = ControlSignal.loadData;

			List<?> out = null;
			try{
//				out = method.invoke(clazz.getClass(), argumentArray);
				out = (List<?>) method.invoke(clazz, argumentArray);
			}catch (Exception ex){
				System.out.println(instance.toString());
				System.out.println(method.toString());
				for (int i = 0; i < argumentArray.length; i ++){
					System.out.println("Arg no." + i + ": " + argumentArray[i].getClass().getName());
				}
				System.out.println("Invoking method failed." + ex.getMessage());
			}
			Iterator<?> outIter = out.iterator(); 
			for (Iterator<Out_> iter = getOutList().iterator(); iter.hasNext();){
				Out_ consumer = iter.next();
				System.out.println("Processor_" + this.getName() + "_" + this.getID() + " output signal to port_" + consumer.getPortType() + "_" + consumer.getID() + ", value=" + out.toString());
				consumer.setOutput(outIter.next());
			}
			resetInToken();
		}
	}
	 */	

	@Override
	protected void transfer() throws ProcessorChainException, ProcessFailedException {
		if (getMethod() == null){
			throw new ProcessorChainException("failed to load process() method at processor " + getName());
		}else{
			try {
				instance = clazz.newInstance();
			} catch (Exception e) {
				throw new ProcessorChainException("faild to create instance of processor " + 
						clazz.getCanonicalName() + ": " + e.getMessage(), e);
			}
			Object[] argumentArray = new Object[getInList().size() + getVarList().size()];
			int index = 0;
			if (getInList() != null){
				for (Iterator<In> iter = getInList().iterator(); iter.hasNext();){
					argumentArray[index] = iter.next().getSignal();
					index++;
				}
			}
			if (getVarList() != null){
				for (Iterator<Var> iter = getVarList().iterator(); iter.hasNext();){
					argumentArray[index] = iter.next().getSignal();
					index++;
				}
			}
			List<?> out = null;
//			out = method.invoke(clazz.getClass(), argumentArray);
//			out = (List<?>) method.invoke(clazz, argumentArray);
			try {
				out = (List<?>) method.invoke(instance, argumentArray);
			} catch (Exception e) {
				throw new ProcessFailedException("failed to process " + getName() + ": " + e.getMessage(), e);
			} 
			Iterator<?> outIter = out.iterator();
			if (getOutList() != null){
				for (Iterator<Out> iter = getOutList().iterator(); iter.hasNext();){
					Out consumer = iter.next();
					Object signal = outIter.next();
					System.out.println("Processor_" + this.getName() + "_" + this.getID() + 
							" output signal to port_" + consumer.getPortType() + "_" + consumer.getName() 
							+ ", type=" + signal.getClass().toString());
//					if (getInList().get(getInList().size()-1).getSignal() == ControlSignal.loadData)
//						consumer.setOutput(signal);
//					if (getInList().get(getInList().size()-1).getSignal() == ControlSignal.execute)
//						consumer.setCach(signal);
					try {
						consumer.setCach(signal);
					} catch (Exception e) {
						throw new ProcessFailedException("failed to pass the output to " + 
								consumer.getCoreName() + ": " + e.getMessage(), e);
					}
				}
			}
			resetInToken();
		}
	}

}
