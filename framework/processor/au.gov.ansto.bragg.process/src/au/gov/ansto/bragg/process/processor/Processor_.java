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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import au.gov.ansto.bragg.process.agent.Agent;
import au.gov.ansto.bragg.process.agent.ProcessorAgent;
import au.gov.ansto.bragg.process.common.Common_;
import au.gov.ansto.bragg.process.common.exception.IllegalNameSetException;
import au.gov.ansto.bragg.process.configuration.InConfiguration;
import au.gov.ansto.bragg.process.configuration.OutConfiguration;
import au.gov.ansto.bragg.process.configuration.ProcessorConfiguration;
import au.gov.ansto.bragg.process.configuration.VarConfiguration;
import au.gov.ansto.bragg.process.exception.ProcessFailedException;
import au.gov.ansto.bragg.process.exception.ProcessorChainException;
import au.gov.ansto.bragg.process.port.In;
import au.gov.ansto.bragg.process.port.In_;
import au.gov.ansto.bragg.process.port.Out;
import au.gov.ansto.bragg.process.port.Out_;
import au.gov.ansto.bragg.process.port.Port;
import au.gov.ansto.bragg.process.port.TunerPortListener;
import au.gov.ansto.bragg.process.port.Var;
import au.gov.ansto.bragg.process.port.Var_;
import au.gov.ansto.bragg.process.util.CicadaLog;

public class Processor_ extends Common_ implements Processor {

	public static final long serialVersionUID = 1L;

	protected Class<?> clazz = null;
	protected Processor parent;
//	protected int recipeID;
//	protected String name;
	protected List<String> methodNameList = new LinkedList<String>();
	protected List<In> inList = new LinkedList<In>();
	protected List<Out> outList = new LinkedList<Out>();
	protected List<Var> varList = new LinkedList<Var>();
	protected int numberOfIns = 0;
	protected int numberOfOuts = 0;
	protected int numberOfVars = 0;
	protected int numberOfInTokens = 0;
	protected int numberOfOutTokens = 0;
	protected int numberOfVarTokens = 0;
	protected boolean locked = false;
	protected Method method = null;
	protected Object instance = null;
	protected boolean running = false;
	protected Agent agent = null;
	protected String version;
	protected ProcessorStatus status = ProcessorStatus.Ready;
	protected boolean isForcedTransfer = false;
	public enum ProcessorStatus{Inprogress, Done, Error, Interrupted, Ready};
	//************************* test *************************
	Double y = 0.;

	public Processor_(){
		super();
	}

	public Processor_(final ProcessorConfiguration configuration, Processor parent) 
	throws ProcessorChainException  
	{
		this();
		try {
			setName(configuration.getName());
		} catch (IllegalNameSetException e) {
			throw new ProcessorChainException("failed to load the processor " + configuration.getName() +
					", check the recipe file", e);
		}
		try {
			setClazz(configuration.getClassType());
		} catch (ClassNotFoundException e) {
			throw new ProcessorChainException("failed to load the processor " + configuration.getName() +
					", can not find the class " + configuration.getClassType(), e);
		}
		setVersion(configuration.getVersionNumber());
		createInstance();
		createIns(configuration.getInConfigurationList());
		createOuts(configuration.getOutConfigurationList());
		createVars(configuration.getVarConfigurationList());
//		setRecipeID(configuration.getReceipeID());
		resetInToken();
		setParent(parent);
		if (configuration.getConfigurationType() == "processor configuration"){
			loadMethod();
		}
	}

	public void addAllPorts(List<Port> portArray) {
		if (inList != null){
			for (Iterator<In> iter = inList.iterator(); iter.hasNext();){
				portArray.add(iter.next());
			}
		}
		if (outList != null){
			for (Iterator<Out> iter = outList.iterator(); iter.hasNext();){
				portArray.add(iter.next());			
			}
		}
		if (varList != null){
			for (Iterator<Var> iter = varList.iterator(); iter.hasNext();){
				portArray.add(iter.next());
			}
		}
	}

	public void addAllProcessors(List<Processor> processorArray, List<Port> portArray) 
	throws NullPointerException {
		addAllPorts(portArray);
		processorArray.add(this);
	}

	protected void createIns(final List<InConfiguration> configurationList) throws ProcessorChainException 
	{
		if (configurationList != null){
			Iterator<InConfiguration> iter = configurationList.iterator();
			while(iter.hasNext()){
				InConfiguration configuration = iter.next();
				In_ in = null;
				try {
					in = new In_(configuration, this);
				} catch (Exception e) {
					throw new ProcessorChainException("failed to create in port " + configuration.getName() 
							+ ", " + e.getMessage(), e);
				}
				inList.add(in);
			}
		}
	}

	protected void createOuts(final List<OutConfiguration> configurationList) 
	throws ProcessorChainException{
		if (configurationList != null){
			Iterator<OutConfiguration> iter = configurationList.iterator();
			while(iter.hasNext()){
				Out out = null;
				OutConfiguration configuration = iter.next();
				try {
					out = new Out_(configuration, this);
				} catch (Exception e) {
					throw new ProcessorChainException("failed to create in port " + configuration.getName() 
							+ ", " + e.getMessage(), e);
				} 
				outList.add(out);
			}
		}
	}

	protected void createVars(final List<VarConfiguration> configurationList) {
		if (configurationList != null){
			Iterator<VarConfiguration> iter = configurationList.iterator();
			while(iter.hasNext()){
				VarConfiguration configuration = iter.next();
				Var var = null;
				try {
					var = new Var_(configuration, this);
				} catch (Exception e) {
					e.printStackTrace();
				}
				varList.add(var);
			}
		}
	}

	public Class<?> getClassType(){
		return clazz;
	}

	public List<In> getInList(){
		return inList;
	}

	protected Method getMethod(){
		return method;
	}

	public String getMethodName(){
		String result = null;
		if (getMethod() == null) result = "null";
		else result = getMethod().getName(); 
		return result;
	}

	/*
	public String getName(){
		return name;
	}
	 */

	public List<Out> getOutList(){
		return outList;
	}

	public Processor getParent(){
		return parent;
	}

	public String getProcessorType(){
		return "processor";
	}

//	public int getRecipeID(){
//	return recipeID;
//	}

	public Object getSignal() throws NullPointerException{
		if (getOutList().get(0) == null) 
			throw new NullPointerException("null output signal");
		return getOutList().get(0).getSignal();
	}

	public ProcessorStatus getStatus(){
//		return running? "InProgress":"Done";
		return status;
	}

	public List<Var> getVarList(){
		return varList;
	}

	/*
	 * Called by IN ports of this processor.
	 * When called, check if all IN singals are ready.
	 * If true, call transfer().   
	 */
	public void inTokenSignalReady() throws ProcessFailedException, ProcessorChainException {
		numberOfInTokens--;
		if (numberOfInTokens == 0){
			transfer();
		}
	}

	protected void lock(){
		locked = true;
	}

	public boolean lockStatus(){
		return locked;
	}

	public String portsToString(){
		String result = "<ins>\n";
		if (getInList() != null){
			for (Iterator<In> iter = getInList().iterator(); iter.hasNext();){
				result += iter.next().toString();
			}
		}
		result += "<number_of_in_tokens>" + numberOfInTokens + "</number_of_in_tokens>\n";
		result += "</ins>\n<outs>\n";
		if (getOutList() != null){
			for (Iterator<Out> iter = getOutList().iterator(); iter.hasNext();){
				result += iter.next().toString();
			}
		}
		result += "</outs>\n<vars>\n";
		if (getVarList() != null){
			for (Iterator<Var> iter = getVarList().iterator(); iter.hasNext();){
				result += iter.next().toString();
			}
		}
		result += "</vars>\n";
//		result += "<test result>" + y + "</test result>\n";
		return result;
	}

	protected void resetInToken() throws ProcessorChainException {
		if (isForcedTransfer){
			isForcedTransfer = false;
			return;
		}
		if (inList == null) 
			numberOfInTokens = 0;
		else 
			numberOfInTokens = inList.size();
		if (getInList() != null){
			for (In in : getInList()){
				try {
					in.releaseToken();
				} catch (Exception e) {
					throw new ProcessorChainException("Failed to process the chain", e);
				} 
			}
		}
	}

	public void setClassType(Class<?> clazz){
		this.clazz = clazz;
	}

	protected void setClazz(String classType) throws ClassNotFoundException{
//		ClassLoader loader = ClassLoader.getSystemClassLoader();
		ClassLoader loader = Processor_.class.getClassLoader();
		clazz = loader.loadClass(classType);
	}
	
	protected void createInstance() throws ProcessorChainException {
		try {
			instance = clazz.getConstructor(new Class[]{}).newInstance(new Object[]{});
		} catch (Exception e) {
			throw new ProcessorChainException("failed to load the processor " +
					", the class " + clazz.getCanonicalName() + " does not have a default constructor", e);
		} 
	}

	public void setField(String fieldName, Object signal, Class<?> type) throws ProcessorChainException {
		String firstLetter = fieldName.substring(0, 1);
		String captalLetter = firstLetter.toUpperCase();	
		String methodName = "set" + captalLetter + fieldName.substring(1, fieldName.length());
//		Class<?> fieldType;
//		try {
//			fieldType = getFieldType(fieldName);
//		} catch (IndexOutOfBoundException e) {
//			e.printStackTrace();
//			throw new IllegalArgumentException("field " + fieldName + " does not exist");
//		}
//		if (fieldType == null) throw new IllegalArgumentException("field " + fieldName + " does not exist");
//		Method setFieldMethod = instance.getClass().getMethod(methodName, new Class[]{signal.getClass()});
		Method setFieldMethod = null;
		try {
			setFieldMethod = instance.getClass().getMethod(methodName, new Class[]{type});
		} catch (Exception e) {
		}
		try {
			if (setFieldMethod != null)
				setFieldMethod.invoke(instance, new Object[]{signal});
		} catch (Exception e) {
			throw new ProcessorChainException("failed to set " + fieldName + " property of the processor " +
					clazz.getCanonicalName() + "; " + e.getMessage(), e);
		} 
	}
	
	public void addTunerPortListener(String varName, TunerPortListener listener){
		if (instance instanceof ConcreteProcessor)
			((ConcreteProcessor) instance).addVarListener(varName, listener);
	}
	
	public void removeTunerPortListener(String varName, TunerPortListener listener){
		if (instance instanceof ConcreteProcessor)
			((ConcreteProcessor) instance).removeVarListener(varName, listener);
	}
//	private Class<?> getFieldType(String fieldName) throws IndexOutOfBoundException{
//		List<Port> portList = new LinkedList<Port>();
//		portList.addAll(varList);
//		portList.addAll(inList);
//		Port port = SortedArrayList.getPortFromName(portList, fieldName);
//		return port.getType();
//	}
	
	public Object getField(String fieldName) throws ProcessorChainException {
		if (fieldName.contains("."))
			fieldName = fieldName.substring(fieldName.lastIndexOf(".") + 1);
		String firstLetter = fieldName.substring(0, 1);
		String captalLetter = firstLetter.toUpperCase();	
		String methodName = "get" + captalLetter + fieldName.substring(1, fieldName.length());
		try {
			Method getFieldMethod = instance.getClass().getMethod(methodName, new Class[]{});
			return getFieldMethod.invoke(instance, new Object[]{});	
		} catch (Exception e) {
			throw new ProcessorChainException("failed to get " + fieldName + " property from the processor " +
					clazz.getCanonicalName() + ": " + e.getMessage(), e);
		}
	}
	
	public void setLock(boolean lockStatus){
		locked = lockStatus;
	}

	/*	
	protected void setMethodNameList(List<String> methodNameList){
		this.methodNameList = methodNameList;
	}
	 */
	public void loadMethod() throws ProcessorChainException {
		String methodName = "process";
		setMethod(methodName);
	}

	public void setMethod(final String methodName) throws ProcessorChainException {
		Class<?>[] arguments = new Class[0];
		try {
			method = instance.getClass().getMethod(methodName, arguments);
		} catch (Exception e) {
			throw new ProcessorChainException("failed to load the processor " + clazz.getCanonicalName() + 
					", " + "the processor does not have a method called '" + "process()': " + e.getMessage(), e);
		}
	}

	/*
	public void setName(String name){
		this.name = name;
	}
	 */

	public void setParent(Processor parent){
		this.parent = parent;
	}

//	public void setRecipeID(int receipeID){
//	this.recipeID = receipeID;
//	}

	public String toString(){
		String result = "<processor id=\"" + getID() + "\" name=\"" + getName() +
		"\" class=\"" + getClassType().getName() + "\" version=\"" + this.getVersion() + "\">\n";
//		result += "<method>" + this.getMethodName() + "</method>\n";
//		result += "<receipe_id>" + getRecipeID() + "</receipe_id>\n";
		result += portsToString();
		result += "</processor>\n";
		return result;
	}

	public String toString(int level){
		String result = "<processor id=\"" + getID() + "\" name=\"" + getName() +
		"\" class=\"" + getClassType().getName() + "\" version=\"" + this.getVersion() + " \">\n";
//		result += "<receipe_id>" + getRecipeID() + "</receipe_id>\n";
		result += portsToString();
		result += "</processor>\n";
		return result;
	}

	protected void transfer() throws ProcessFailedException, ProcessorChainException {
		if (getMethod() == null){
			throw new ProcessFailedException("Load method failed.");
		}else{
			setStatus(ProcessorStatus.Inprogress);
			setRunning(true);
			Boolean stopFlag;
			try{
				long time = System.currentTimeMillis();
				stopFlag = (Boolean) method.invoke(instance, new Object[]{});
				System.err.println(getName()+ " process time = " + String.valueOf(
						System.currentTimeMillis() - time));
			}catch (Exception e) {
				Throwable targetException = e;
				if (e instanceof InvocationTargetException)
					targetException = ((InvocationTargetException) e).getTargetException();
				setStatus(ProcessorStatus.Error);
				setRunning(false);
				try {
					resetInToken();
				} catch (ProcessorChainException e1) {
					CicadaLog.getLog().error("reset ins token failed: " + e1.getMessage(), e1);
				}
				String pName = getName();
				if (getAgent() != null)
					pName = getAgent().getLabel();
				throw new ProcessFailedException("failed to process " + pName + ": " + targetException.getMessage(), targetException);
			}
//			Iterator<?> outIter = out.iterator();
			setStatus(ProcessorStatus.Done);
			setRunning(false);
			try {
				resetInToken();
			} catch (ProcessorChainException e1) {
				CicadaLog.getLog().error("reset ins token failed", e1);
			}
			System.out.println("finished transfering " + getName());
			Object outputSignal = null;
			if (getOutList()!= null){
				for (Iterator<Out> iter = getOutList().iterator(); iter.hasNext();){
					Out consumer = iter.next();
					try {
						outputSignal = getField(consumer.getName());
					} catch (Exception e) {
						e.printStackTrace();
						throw new ProcessorChainException("failed to access field " + consumer.getName());
					} 
//					if (outputSignal != null){
						System.out.println("Processor_" + this.getName() + "_" + this.getID() + 
								" output signal to port_" + consumer.getPortType() + "_" + 
								consumer.getName() + ", type=" + (
										outputSignal == null ? "null" : outputSignal.getClass().toString()));
						try {
							if (stopFlag)
								consumer.setOutputToConsumer(outputSignal);
							else
								consumer.setCach(outputSignal);
						} catch (Exception e) {
							throw new ProcessFailedException("failed to pass the output to " + 
									consumer.getCoreName() + ": " + e.getMessage(), e);
						}
//					}
				}
			}
		}
	}

	private void setStatus(ProcessorStatus status) {
		this.status = status;
	}

	public void setInterruptStatus(){
		setStatus(ProcessorStatus.Interrupted);
	}
	
	public boolean isRunning(){
		return running;
	}

	protected void unlock(){
		locked = false;
	}

	public Agent getAgent() {
		return agent;
	}

	public void setAgent(ProcessorAgent agent) {
		this.agent = agent;
	}

	protected void setRunning(boolean running) {
		this.running = running;
		if (agent != null) 
				agent.statusTransfer();
	}

	public void triggerFromAgent() throws ProcessFailedException, ProcessorChainException{
		isForcedTransfer = true;
		transfer();
	}

	public List<Sink> getSinkList() {
		List<Sink> sinkList = new ArrayList<Sink>();
		for (Iterator<Out> iter = outList.iterator(); iter.hasNext();){
			Out out = iter.next();
			Sink sink = out.getSink();
			if (sink != null) sinkList.add(sink);
		}
		return sinkList;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isReprocessable() {
		boolean isReprocessable = true;
		try {
			Method isReprocessableMethod = instance.getClass().getMethod(
					ConcreteProcessor.ISREPROCESSABLE_METHOD_NAME, new Class[]{});
			isReprocessable = (Boolean) isReprocessableMethod.invoke(instance, new Object[]{});
		} catch (Exception e) {
		}
		return isReprocessable;
	}

	public void dispose(){
		clazz = null;
		parent = null;
//		protected int recipeID;
//		protected String name;
		methodNameList.clear();
		inList.clear();
		outList.clear();
		varList.clear();
		method = null;
		instance = null;
		agent = null;
	}
}
