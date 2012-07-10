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
package au.gov.ansto.bragg.cicada.core.internal;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.gumtree.data.Factory;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.cicada.core.Algorithm;
import au.gov.ansto.bragg.cicada.core.AlgorithmConfiguration;
import au.gov.ansto.bragg.cicada.core.AlgorithmInput;
import au.gov.ansto.bragg.cicada.core.AlgorithmManager;
import au.gov.ansto.bragg.cicada.core.AlgorithmStatusListener;
import au.gov.ansto.bragg.cicada.core.Exporter;
import au.gov.ansto.bragg.cicada.core.Format;
import au.gov.ansto.bragg.cicada.core.ThreadExceptionHandler;
import au.gov.ansto.bragg.cicada.core.AlgorithmConfiguration.AlgorithmType;
import au.gov.ansto.bragg.cicada.core.exception.ConfigurationException;
import au.gov.ansto.bragg.cicada.core.exception.ExportException;
import au.gov.ansto.bragg.cicada.core.exception.FailedToCloneException;
import au.gov.ansto.bragg.cicada.core.exception.LoadAlgorithmFileFailedException;
import au.gov.ansto.bragg.cicada.core.exception.SetTunerException;
import au.gov.ansto.bragg.cicada.core.exception.TransferFailedException;
import au.gov.ansto.bragg.cicada.core.extension.AlgorithmSet;
import au.gov.ansto.bragg.cicada.dam.core.DataManager;
import au.gov.ansto.bragg.cicada.dam.core.DataManagerFactory;
import au.gov.ansto.bragg.cicada.export.Exporter_;
import au.gov.ansto.bragg.cicada.export.Formater_;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.process.agent.Agent;
import au.gov.ansto.bragg.process.agent.AgentListener;
import au.gov.ansto.bragg.process.agent.ProcessorAgent;
import au.gov.ansto.bragg.process.common.Common_;
import au.gov.ansto.bragg.process.common.exception.IllegalNameSetException;
import au.gov.ansto.bragg.process.configuration.FrameworkConfiguration;
import au.gov.ansto.bragg.process.exception.ProcessFailedException;
import au.gov.ansto.bragg.process.exception.ProcessorChainException;
import au.gov.ansto.bragg.process.factory.ConfigurationFactory;
import au.gov.ansto.bragg.process.factory.ProcessorFactory;
import au.gov.ansto.bragg.process.parse.Parse;
import au.gov.ansto.bragg.process.port.In;
import au.gov.ansto.bragg.process.port.Port;
import au.gov.ansto.bragg.process.port.Tuner;
import au.gov.ansto.bragg.process.port.Tuner_;
import au.gov.ansto.bragg.process.port.Var;
import au.gov.ansto.bragg.process.processor.Framework;
import au.gov.ansto.bragg.process.processor.Sink;
import au.gov.ansto.bragg.process.processor.Processor_.ProcessorStatus;
import au.gov.ansto.bragg.process.util.SortedArrayList;

/**
 * @author nxi
 * Created on 13/02/2007, 2:09:24 PM
 * Last modified 13/02/2007, 2:09:24 PM
 * 
 */
public class Algorithm_ extends Common_ implements Algorithm {

	public final static long serialVersionUID = 1L;

	/*
	 * framework: a handle of the framework of the algorithm;
	 * receipeID: override of the id property;
	 * name: override of the name property;
	 * classID: the algorithm id from the receipe file;
	 * version: version property from the receipe file;
	 * helpURL: URL location as a string of help file;
	 * shortDescription: a short description of the algorithm from the receipe file;
	 * icon: icon location of the algoirthm;
	 * runnning: running flag of the processor framework;
	 * frameworkConfiguration: the framework configration object;
	 * currentSignal: current signal of the algorithm;
	 * currentSourcePort: the source port that takes the current signal;
	 * currentSinkPort: the current sink port that passing the signal output;
	 * currentSignalList: the list of the current source signals;
	 * currentSourcePortList: the list of the source ports that take the current signal list;
	 * historySignalList: the history list of the signal list;
	 * tunerList: the list of the tuners of the algorithm;
	 * agentList: the list of the agents of the algorithm; 
	 */
	protected Framework framework = null;
	protected int receipeID;
//	protected String name;
	protected String classID = null;
	protected String version = null;
	protected String helpURL = null;
	protected String shortDescription = null;
	protected String icon = null;
	protected String filename = null;
	protected boolean isRunning = false;

	protected FrameworkConfiguration frameworkConfiguration = null;
	protected AlgorithmInput currentSignal = null;
	protected Port currentSourcePort = null;
	protected Port currentSinkPort = null;
	protected List<In> currentSourcePortList = null;
//	protected List<Object> historySignalList = null;
	protected List<Tuner> tunerList = null;
	protected List<Agent> agentList = null;
	protected boolean hidden = false;
	protected AlgorithmSet algorithmSet = null;
	protected List<ThreadExceptionHandler> exceptionHandlerList = null;
	protected boolean hasInPort;
	protected String defaultSinkName;
	protected AlgorithmConfiguration configuration;
	private Thread algorithmThread;
	private List<AlgorithmStatusListener> statusListenerList;
	protected AlgorithmType algorithmType = AlgorithmType.analysis;
	private AlgorithmStatus algorithmStatus;
	/**
	 * Default constructor.
	 */
	public Algorithm_() 
//	throws IllegalNameSetException
	{
		super();
		try {
			this.setName(DEFAULT_NAME);
		} catch (IllegalNameSetException e) {
		}
		exceptionHandlerList = new ArrayList<ThreadExceptionHandler>();
//		historySignalList = new LinkedList<Object>();
//		currentSignalList = new ArrayList<Object>();
	}

	public Algorithm_(Algorithm algorithm) throws FailedToCloneException{
		this();
		try {
			this.loadAlgorithm(algorithm.getFilename());
		} catch (LoadAlgorithmFileFailedException e) {
			throw new FailedToCloneException("can not parse the recipe file " + algorithm.getFilename() 
					+ ": " + e.getMessage(), e);
		}
		List<Tuner> sourceTunerList = algorithm.getTunerArray();
		Iterator<Tuner> sourceTunerIter = sourceTunerList.iterator();
		for (Iterator<Tuner> iter = this.tunerList.iterator(); iter.hasNext();){
			if (sourceTunerIter.hasNext()){
				Tuner sourceTuner = sourceTunerIter.next();
				try {
					iter.next().setSignal(sourceTuner.getSignal());
				} catch (Exception e) {
					throw new FailedToCloneException("can not initiate tuner " + sourceTuner.getCoreName() +
							"\n" + e.getMessage(), e);
				} 
			}else 
				break;
		}
		List<Sink> sinkList = getSinkList();
		if (sinkList.size() > 0)
			defaultSinkName = sinkList.get(sinkList.size() - 1).getName();
	}

	/**
	 * Constructor with an algorithm input parameter.
	 * @param input in type of AlgorithmInput
	 */
	public Algorithm_(AlgorithmInput input){
		this.currentSignal = input;
	}

	/**
	 * Create algorithm structure from recipe file. The algorithm is not build
	 * upon any Algorithm Input data. It only contain title and some other
	 * general information
	 * 
	 * @param filename
	 * @throws LoadAlgorithmFileFailedException 
	 */
	public Algorithm_(File filename) throws LoadAlgorithmFileFailedException{
		this();
		loadAlgorithmTitle(filename);
	}

	public Algorithm clone() {
		Algorithm algorithm = null;
		try {
			algorithm = new Algorithm_(this);
		} catch (FailedToCloneException e) {
			algorithm = new Algorithm_();
		}
		return algorithm;
	}

	/**
	 * Create the agent list specified in the recipe file.
	 * @throws LoadAlgorithmFileFailedException 
	 */
	protected void createAgentList() throws LoadAlgorithmFileFailedException {
		if (getFramework() == null) 
			throw new NullPointerException("no framework pointer");
		if (getFramework().getAgentList() == null) 
			throw new NullPointerException("no agent list pointer");
		agentList = getFramework().getAgentList();

		int id = 0;
		if (agentList != null) {
			for (Iterator<Agent> iter = agentList.iterator(); iter.hasNext();){
				Agent agent = iter.next();
				final int index = id ++;
				AgentListener agentListener = new AgentListener(){

					public void onChange(Agent agent) {
						String status = "";
						status = agent.getStatus();
						if (status.equals("Inprogress")) 
							fireStatusChanged(AlgorithmStatus.Running, index);
						else if (status.equals("Error"))
							fireStatusChanged(AlgorithmStatus.Error, index);
						else if (status.equals("Interrupted"))
							fireStatusChanged(AlgorithmStatus.Interrupt, index);
						else 
							fireStatusChanged(AlgorithmStatus.Idle, index);
					}
				};
//				iter.next().setPrincipal(getFramework());
//				System.out.println(agent.toString());
				try {
					agent.setPrincipal(getFramework());
				} catch (Exception e) {
					throw new LoadAlgorithmFileFailedException("can not set agent " + agent.getName() + ": "
							+ e.getMessage(), e);
				} 
				agent.subscribe(agentListener);
			}
		}
	}

	/**
	 * Create the tuner list of the algorithm for every exposed Var port.
	 * @throws ProcessorChainException 
	 */
	protected void createTunerList() throws ProcessorChainException {
		tunerList = new ArrayList<Tuner>();
		if (getFramework().getVarList() != null){
			for (Var var : getFramework().getVarList()){
				Tuner tuner = new Tuner_();

				tuner.setConsumer(var);
				tunerList.add(tuner);
				try {
					tuner.setSignal(var.getDefaultValue());
				} catch (ProcessFailedException e) {
					throw new ProcessorChainException(e);
				}
			}
		}
	}

	/**
	 * This method get the agent list of the algorithm.
	 * @return a list of agents. 
	 */
	public List<Agent> getAgentList(){
		return agentList;		
	}

	public List<ProcessorAgent> getProcessorAgentList(){
		List<ProcessorAgent> processorAgentList = new ArrayList<ProcessorAgent>();
		for (Iterator<?> iterator = agentList.iterator(); iterator
				.hasNext();) {
			ProcessorAgent agent = (ProcessorAgent) iterator.next();
			if (agent instanceof ProcessorAgent) 
				processorAgentList.add((ProcessorAgent) agent);
		}
		return processorAgentList;
	}
	
	/**
	 * Return the algorithm id specified in the recipe file. 
	 * @return Class id in String type.
	 */
	public String getClassID(){
		return classID;
	}

	/**
	 * This method return the current input signal, which is a wrap of raw signal 
	 * and algorithm loaded on the signal.
	 * @return current signal in AlgorithmInput type
	 * @since V1.2
	 */
	public AlgorithmInput getCurrentSignal(){
		return currentSignal;
	}

	/**
	 * This method return the file name of the algorithm recipe file.
	 * @return file name in String type
	 */
	public String getFilename(){
		return filename;
	}

	/**
	 * This method return the framework of the algorithm, which contains the processor chain. 
	 * @return framework of the processor chain specified in the recipe file
	 */
	protected Framework getFramework(){
		return framework;
	}

	/*
	public int getReceipeID(){
		return receipeID;
	}
	 */	

	/*
	public void loadSignal(Object signal){
		currentSignal = signal;
	}
	 */

	/*
	public String getName(){
		return name;
	}
	 */

	/**
	 * This method retrieve the URL of the help file location.
	 * @return help file location in URL type
	 */
	public String getHelpURL(){
		return helpURL;
	}

	/**
	 * This method returns the icon file name of the algorithm
	 */
	public String getIcon(){
		return icon;
	}

	/**
	 * This method returns a short description of the algorithm in the recipe file.
	 */
	public String getShortDescription(){
		return shortDescription;
	}

	/**
	 * This method return the sink list of the algorithm.
	 * @return list of sink
	 * @see au.gov.ansto.bragg.cicada.core.Algorithm#getSinkList()
	 */
	public List<Sink> getSinkList() {
		if (getFramework() == null) return null;
//		List<Sink> sinkList = getFramework().getSinkList();
//		for (Iterator iterator = sinkList.iterator(); iterator.hasNext();) {
//			Sink sink = (Sink) iterator.next();
//			Object property = sink.getProperty("dataDimensionType");
//			System.out.println(sink.getName() + " : " + property.toString());
//		}
		return getFramework().getSinkList();
	}

	/**
	 * This method retrieve the signal of the sink.
	 * @return general signal in Object instance
	 * @throws NullPointerException can not find the signal
	 */
	public Object getSinkSignal() throws NullPointerException{
		if (currentSinkPort == null) 
			throw new NullPointerException("no sink port loaded");
		return currentSinkPort.getSignal();
	}

	/**
	 * Find the tuner with a given name.
	 * @param tunerName a String object
	 */
	public Tuner getTuner(String tunerName){
		if (tunerList != null){
			for (Tuner tuner : tunerList){
				if (tuner.getName().equals(tunerName))
					return tuner;
			}
		}
		return null;
	}
	
	/**
	 * This method get the tuner list of the current loaded algorithm.
	 * @return list of tuners
	 */
	public List<Tuner> getTunerArray(){
		return tunerList;
	}

	/**
	 * This method return a list tuner group of an algorithm.
	 */
	public List<List<Tuner>> getTunerGroupList(){
		List<List<Tuner>> groupList = new LinkedList<List<Tuner>>();
		List<Tuner> tunerGroup = new LinkedList<Tuner>();
		List<Integer> ownerIDList = new ArrayList<Integer>();
		if (tunerList != null){
			for (Iterator<Tuner> iter = tunerList.iterator(); iter.hasNext();){
				Tuner tuner = iter.next();
				Integer ownerID = null;
				if (ownerIDList != null){
					for (Iterator<Integer> subIter = ownerIDList.iterator(); subIter.hasNext();){
						Integer nextInteger = subIter.next();
						if (nextInteger.intValue() == tuner.getOwnerID()) ownerID = nextInteger;
					}
					if (ownerID == null) {
						tunerGroup = new LinkedList<Tuner>();
						tunerGroup.add(tuner);
						groupList.add(tunerGroup);
						ownerID = new Integer(tuner.getOwnerID());
						ownerIDList.add(ownerID);
					}else{
						int index = ownerIDList.indexOf(ownerID);
						groupList.get(index).add(tuner);
					}
				}
			}
		}
		return groupList;
	}

	/**
	 * This method get the version of the algorithm recipe file.
	 * @return version number in String type
	 */
	public String getVersion(){
		return version;
	}

	public boolean isEmpty() {
		if (framework == null) return true;
		return false;
	}

	public boolean isHidden(){
		return hidden;
	}

	public void loadAlgorithm(File filename) throws LoadAlgorithmFileFailedException{
		loadAlgorithm(filename.getAbsolutePath());
//		Document file = loadAlgorithmTitle(filename);
//		try {
//			frameworkConfiguration = ConfigurationFactory.createConfiguration(file);
//		} catch (Exception e) {
//			throw new LoadAlgorithmFileFailedException("failed to read the algorithm configuration file" + 
//					file.getName() + "\n" + e.getMessage(), e);
//		}
//		try {
//			framework = ProcessorFactory.loadAlgorithm(frameworkConfiguration);
//		} catch (Exception e) {
//			throw new LoadAlgorithmFileFailedException("failed to create the processor framework for algorithm "
//					+ getName() + "\n" + e.getMessage(), e);
//		} 
//		try {
//			currentSourcePort = framework.getInList().get(0);	
//		} catch (Exception e) {
//		}
//		try {
//			currentSinkPort = framework.getOutList().get(0);
//		} catch (Exception e) {
//		}
//		currentSourcePortList = framework.getInList();
//		try {
//			setName(framework.getName());
//		} catch (IllegalNameSetException e) {
//			throw new LoadAlgorithmFileFailedException(e.getMessage() +
//			": illegal framework name");
//		}
//		try {
//			createTunerList();
//		} catch (ProcessorChainException e) {
//			throw new LoadAlgorithmFileFailedException("failed to load algorithm " + getName() + 
//					"\n" + e.getMessage(), e);
//		} 
//		createAgentList();
	}

	/*
	public void multiSignalTransfer() throws NullPointerException, NullMethodException,
	IllegalAccessException, InvocationTargetException, InstantiationException{
//		if (currentSignalList == null) throw new NullPointerException("no input signal");
//		Iterator<Object> signalIter = currentSignalList.iterator();
//		for (Iterator<In> portIter = currentSourcePortList.iterator(); portIter.hasNext();){
////			In_ thisPort = portIter.next();
////			Object thisSignal = signalIter.next();
////			thisPort.setCach(thisSignal);
//			portIter.next().setCach(signalIter.next());
////			System.out.println("sect cach for a signal: " + thisPort.toString() + thisSignal.toString());
//		}
//		currentSourcePortList.get(0).setCach(currentSignal.getDatabag());
		transfer();
	}
	 */

	public void loadAlgorithm(String fileLocation) throws LoadAlgorithmFileFailedException 
	{
		try {
			fileLocation = ConverterLib.path2URI(fileLocation).getPath();
		} catch (FileAccessException e1) {
			throw new LoadAlgorithmFileFailedException("can not find the algorithm configuration file " + 
					fileLocation);
		}
		Document file = loadAlgorithmTitle(new File(fileLocation));
		try {
			file = Parse.readFile(fileLocation);
		} catch (DocumentException e) {
			throw new LoadAlgorithmFileFailedException("can not find the algorithm configuration file " + 
					fileLocation);
		}
		try {
			frameworkConfiguration = ConfigurationFactory.createConfiguration(file);
		} catch (Exception e) {
			throw new LoadAlgorithmFileFailedException("failed to read the algorithm configuration file" + 
					file.getName() + "\n" + e.getMessage(), e);
		}
		try {
			framework = ProcessorFactory.loadAlgorithm(frameworkConfiguration);
		} catch (Exception e) {
			throw new LoadAlgorithmFileFailedException("failed to create the processor framework for algorithm "
					+ getName() + "\n" + e.getMessage(), e);
		} 
		try {
			currentSourcePort = framework.getInList().get(0);	
		} catch (Exception e) {
		}
		try {
			currentSinkPort = framework.getOutList().get(0);
		} catch (Exception e) {
		}
		currentSourcePortList = framework.getInList();
		try {
			setName(framework.getName());
		} catch (IllegalNameSetException e) {
			throw new LoadAlgorithmFileFailedException(e.getMessage() +
			": illegal framework name");
		}
		try {
			createTunerList();
		} catch (Exception e) {
			throw new LoadAlgorithmFileFailedException("failed to load algorithm " + getName() + 
					"\n" + e.getMessage(), e);
		} 
		createAgentList();
		filename = fileLocation;
	}

	public void loadAlgorithm(URL fileLocation) throws LoadAlgorithmFileFailedException	{
		loadAlgorithm(fileLocation.getPath());
//		Document file = loadAlgorithmTitle(new File(fileLocation.getFile()));
//		try {
//			file = Parse.readFile(fileLocation);
//		} catch (DocumentException e) {
////			e.printStackTrace();
//			throw new LoadAlgorithmFileFailedException(e.getMessage() + 
//			": failed to parse the recipe file");
//		}
//		try {
//			frameworkConfiguration = ConfigurationFactory.createConfiguration(file);
//		} catch (Exception e) {
////			e.printStackTrace();
//			throw new LoadAlgorithmFileFailedException(e.getMessage() + 
//			": failed to load cicada configuration file");
//		}
//		try {
//			framework = ProcessorFactory.loadAlgorithm(frameworkConfiguration);
//		} catch (Exception e) {
////			e.printStackTrace();
//			throw new LoadAlgorithmFileFailedException(e.getMessage() + 
//			": failed to create the processor framework");
//		} 
//		try {
//			currentSourcePort = framework.getInList().get(0);	
//		} catch (Exception e) {
//		}
//		try {
//			currentSinkPort = framework.getOutList().get(0);
//		} catch (Exception e) {
//		}
//		currentSourcePortList = framework.getInList();
//		try {
//			setName(framework.getName());
//		} catch (IllegalNameSetException e) {
////			e.printStackTrace();
//			throw new LoadAlgorithmFileFailedException(e.getMessage() +
//			": illegal framework name");
//		}
//		try {
//			createTunerList();
//		} catch (Exception e) {
////			e.printStackTrace();
//			throw new LoadAlgorithmFileFailedException(e.getMessage() + 
//			": failed to create tuner list");
//		} 
//		try {
//			createAgentList();
//		} catch (Exception e) {
////			e.printStackTrace();
//			throw new LoadAlgorithmFileFailedException(e.getMessage() + 
//			": failed to create agent list");
//		}
//		filename = fileLocation.getPath();
	}

	public Document loadAlgorithmTitle(File algorithmFile) throws LoadAlgorithmFileFailedException{ 
		this.filename = algorithmFile.getPath();
		Document file = null;
		try {
			file = Parse.readFile(algorithmFile);
		} catch (DocumentException e) {
			throw new LoadAlgorithmFileFailedException("failed to load the algorithm from configuration file "
					+ algorithmFile.getName() + ": " + e.getMessage(), e);
		}
		Element rootElement = file.getRootElement();
		classID = rootElement.attributeValue("id");
		try {
			setName(rootElement.attributeValue("name"));
		} catch (IllegalNameSetException e) {
			throw new LoadAlgorithmFileFailedException(e.getMessage() + ": illegal algorithm name");
		}
		version = rootElement.attributeValue("version");
		helpURL = rootElement.attributeValue("help_url");
		shortDescription = rootElement.attributeValue("short_description");
		icon = rootElement.attributeValue("icon");
		try{
			algorithmType = AlgorithmType.valueOf(rootElement.attributeValue("type"));
		}catch (Exception e) {}
		String hiddenValue = rootElement.attributeValue("hidden");
		if (hiddenValue != null) 
			hidden = Boolean.valueOf(hiddenValue);
		if (classID == null) 
			throw new LoadAlgorithmFileFailedException("classID not found in receipe file");
//		if (icon == null) 
//			throw new LoadAlgorithmFileFailedException("icon path not found in receipe file");
		hasInPort = checkSourcePortExistence(rootElement);
		return file;
	}

	private boolean checkSourcePortExistence(Element rootElement) {
//		List<InConfiguration> inConfigurationList = new LinkedList<InConfiguration>();
		for (Iterator<?> iter = rootElement.elementIterator("ins"); iter.hasNext();){
			Element ins = (Element)iter.next();
			if (ins.hasContent()) 
				return true;
		}
		return false;
	}

	/*
	public void setReceipeID(int receipeID){
		this.receipeID = receipeID;
	}
	 */

	/*
	public void setName(String name){
		this.name = name; 
	}
	 */

	public void resetRunningFlag(){
		isRunning = false;
	}

	/*
	public void setVarPort(int portID, Object value){
		Port port = null;
		try{
			port = (Port) SortedArrayList.get(framework.getPortArray(), portID);
//			System.out.println(port.toString());
		}catch(Exception ex){
			System.out.println(ex.getMessage());
		}
		port.setCach(value);
	}
	 */

	protected void resetTunerChangeFlag(){
		for (Iterator<Tuner> iter = tunerList.iterator(); iter.hasNext();){
			iter.next().resetChangeFlag();
		}
	}

	public void setCurrentSignal(AlgorithmInput signal){
		currentSignal = signal;

	}

	public void setFilenameTuner(String filename) 
	throws SetTunerException
	{
		if (tunerList != null){
			for (Iterator<Tuner> iter = tunerList.iterator(); iter.hasNext();){
				Tuner tuner = iter.next();
				if (tuner.getName().contains("filename"))
					try {
						tuner.setSignal(filename);
					} catch (Exception e) {
//						e.printStackTrace();
						throw new SetTunerException(e.getMessage() + 
						": failed to set tuner");
					} 
			}
		}
	}

	public void setRunningFlag(){
		isRunning = true;
	}

	public void setUnchangedTuners() throws SetTunerException {
		for (Iterator<Tuner> iter = tunerList.iterator(); iter.hasNext();){
			Tuner tuner = iter.next();
			if (!tuner.isChanged()){
				try {
					tuner.setSignal(tuner.getSignal());
				} catch (Exception e) {
					e.printStackTrace();
					throw new SetTunerException(e);
				} 
			}
		}
	}

	public void setChangedTuners() throws SetTunerException {
		for (Iterator<Tuner> iter = tunerList.iterator(); iter.hasNext();){
			Tuner tuner = iter.next();
			if (tuner.isChanged()){
				try {
					tuner.setSignal(tuner.getSignal());
				} catch (Exception e) {
					e.printStackTrace();
					throw new SetTunerException(e);
				} 
			}
		}
	}

	protected void setVarPort(Port port, Object value) throws SetTunerException 
	{
		try {
			port.setCach(value);
		} catch (Exception e) {
			throw new SetTunerException("failed to set tuner" + port.getName() + "\n" + e.getMessage(), e);
		} 
	}

	@Override
	public String toString(){
		String result = "<algorithm id=\"" + getID() + "\" name=\"" + getName() + "\">\n";
		result += "<Class_ID>" + getClassID() + "</Class_ID>\n";
		result += "<version>" + getVersion() + "</version>\n";
		result += "<help_url>" + getHelpURL() + "</help_url>\n";
		result += "<short_description>" + getShortDescription() + "</short_description>\n";
		result += "<icon>" + getIcon() + "</icon>\n";
		if (framework != null){
			result += framework.toString();
			if (tunerList != null){
				result += "<tuners>\n";
				for (Iterator<Tuner> iter = tunerList.iterator(); iter.hasNext();) 
					result += iter.next().toString();
				result += "</tuners>\n";
			}
			if (agentList != null){
				result += "<agents>\n";
				for (Iterator<Agent> iter = agentList.iterator(); iter.hasNext();) 
					result += iter.next().toString();
				result += "</agents>\n";
			}
		}
		result += "</algorithm>\n";
		return result;
	}
	
	public void transfer() throws TransferFailedException {
//		ControlSignal controlSignal = ControlSignal.execute;
//		currentSignalList = new LinkedList<Object>();
//		if (currentSignal != null)
//		currentSignalList.add(currentSignal);
//		currentSignalList.add(controlSignal);
////		currentSourcePort.setCach(currentSignal);
//		multiSignalTransfer();
//		currentSignalList.remove(controlSignal);
		fireStatusChanged(AlgorithmStatus.Running);
		if (currentSignal.getDatabag() != null && currentSourcePortList.size() > 0){
			try {
				currentSourcePortList.get(0).setCach(currentSignal.getDatabag());
				setChangedTuners();
			} catch (ProcessFailedException e) {
				preHandleException();
				throw new TransferFailedException("failed to run the algorithm " + getName() + 
						": one of the step failed.\n" + e.getMessage(), e);
			} catch (ProcessorChainException e) {
				preHandleException();
				throw new TransferFailedException("failed to run the algorithm " + getName() + 
						": the chain structure has a fault.\n" +
						e.getMessage(), e);
			} catch (SetTunerException e) {
				throw new TransferFailedException("failed to run the algorithm " + getName() + 
						": can not set the tuner.\n" + e.getMessage(), e);
			} 
		}else{
			try{
				List<ProcessorAgent> agentList = getProcessorAgentList();
				if (agentList.size() > 0){
					ProcessorAgent agent = agentList.get(0);
					agent.trigger();
				}else 
					throw new TransferFailedException("no start point");				
			}catch (Exception e) {
				preHandleException();
				throw new TransferFailedException(e);
			}
		}
		fireStatusChanged(AlgorithmStatus.End);
		resetTunerChangeFlag();
	}

	private void preHandleException(){
		resetTunerChangeFlag();
		fireStatusChanged(AlgorithmStatus.Error);
	}
	/**
	 * This method do the routine when a transfer exception is thrown. 
	 * @param algorithm object
	 * @param e Exception instance
	 * @since V2.0 
	 */
	public void catchTransferException(Exception e) {
		if (exceptionHandlerList != null) {
			for (Iterator<?> iterator = exceptionHandlerList.iterator(); iterator.hasNext();) {
				ThreadExceptionHandler handler = (ThreadExceptionHandler) iterator.next();
				handler.catchException(this, e);
			}
		}
	}

	public void subscribeExceptionHandler(ThreadExceptionHandler handler) {
//		this.exceptionCatcher = catcher;
		if (exceptionHandlerList == null) 
			exceptionHandlerList = new ArrayList<ThreadExceptionHandler>(); 
		exceptionHandlerList.removeAll(exceptionHandlerList);
		if (!exceptionHandlerList.contains(handler)) exceptionHandlerList.add(handler);
	}

	public List<ThreadExceptionHandler> getExceptionHandlerList() {
		return exceptionHandlerList;
	}

	public void unSubscribeExceptionHandler(ThreadExceptionHandler handler) {
		if(exceptionHandlerList != null) exceptionHandlerList.remove(handler);
	}

	public Port getCurrentSourcePort() {
		return currentSourcePort;
	}

	public boolean hasInPort() {
		return hasInPort;
	}

	public void exportConfiguration(URI fileURI, String configurationName) 
	throws ExportException, ConfigurationException 
{
		AlgorithmConfiguration configuration = new AlgorithmConfiguration(this, fileURI, 
				configurationName);
		this.configuration = configuration;
		Exporter exporter = null;
		try{
			exporter = new Exporter_(new Formater_(Format.xml));
		}catch (Exception e) {
			return;
		}
		try {
			exporter.signalExport(configuration.toGDMGroup(), fileURI);
		} catch (ExportException e) {
			throw e;
		}
	}

	public void exportPartialConfiguration(URI fileURI, String configurationName, String... tunerNames) 
	throws ExportException, ConfigurationException {
		
		AlgorithmConfiguration configuration = new AlgorithmConfiguration(this, fileURI, 
				configurationName, tunerNames);
		this.configuration = configuration;
		Exporter exporter = null;
		try{
			exporter = new Exporter_(new Formater_(Format.xml));
		}catch (Exception e) {
			return;
		}
		exporter.signalExport(configuration.toGDMGroup(), fileURI);		
	}
	
	public void exportConfiguration(){
//		exportConfiguration()
	}
	
	/**
	 * @param defaultSinkName the defaultSinkName to set
	 */
	public void setDefaultSinkName(String defaultSinkName) {
		this.defaultSinkName = defaultSinkName;
	}

	public String getDefaultSinkName() {
		return defaultSinkName;
	}

	public void loadConfiguration(URI fileURI) throws LoadAlgorithmFileFailedException, 
	ConfigurationException {
		AlgorithmConfiguration configuration = null;
		DataManager dataManager = DataManagerFactory.getDataManager(null);
		IGroup rootGroup = null;
		try {
			rootGroup = dataManager.getGroup(fileURI);
		} catch (Exception e1) {
			throw new LoadAlgorithmFileFailedException("can not read from the target, check the file format");
		} 
		List<?> groupList = rootGroup.getGroupList();
		if (groupList.size() == 0 || !(groupList.get(0) instanceof IGroup)) 
			throw new LoadAlgorithmFileFailedException("not an algorithm configuration");
		try {
			configuration = new AlgorithmConfiguration((IGroup) groupList.get(0), fileURI);
		} catch (Exception e) {
			throw new LoadAlgorithmFileFailedException(e.getMessage() + 
			": failed to load cicada configuration file");
		}
		loadConfiguration(configuration);
	}

	public void loadConfiguration(AlgorithmConfiguration configuration) throws ConfigurationException {
		if (!configuration.getAlgorithmName().equals(this.getName()))
			throw new ConfigurationException("configuration not fit");
		List<String> tunerNames = configuration.getTunerNameSet();
		for (String tunerName : tunerNames){
			Object tunerValue = configuration.getConfiguration(tunerName);
			Tuner tuner = null;
			try {
				tuner = (Tuner) SortedArrayList.getObjectFromName(getTunerArray(), tunerName);
			} catch (Exception e) {
			}
			if (tuner != null){
				try {
					if (tunerValue instanceof String)
						tuner.setStringSignal((String) tunerValue);
					else
						tuner.setSignal(tunerValue);
				} catch (Exception e) {
					throw new ConfigurationException("can not set the signal to tuner " + tuner.getName() + 
							"\n" + e.getMessage(), e);
				} 
			}
		}
		String defaultSinkName = configuration.getDefaultSinkName();
		if (defaultSinkName != null) 
			this.defaultSinkName = defaultSinkName;
		this.configuration = configuration;
	}

	public AlgorithmConfiguration getConfiguration() throws ConfigurationException{
		if (configuration == null){
			try {
				IDataset dataset = Factory.createEmptyDatasetInstance();
				configuration = new AlgorithmConfiguration(this, dataset, 
						"algorithmConfiguration");
			} catch (IOException e) {
				throw new ConfigurationException("can not create configuration from the algorithm: " + 
						e.getMessage(), e);
			}
		}
		return configuration;
	}
	
	public boolean isRunning(){
		return isRunning;
	}
	
	public void setConfigurationGroup(IGroup configurationGroup) throws ConfigurationException{
		configuration = new AlgorithmConfiguration(configurationGroup);
	}

	@SuppressWarnings("deprecation")
	public void interrupt() {
		if (algorithmThread != null && algorithmThread.isAlive()) 
//			algorithmThread.interrupt();
			algorithmThread.stop();
		fireStatusChanged(AlgorithmStatus.Interrupt);
		setProcessorStatus(AlgorithmStatus.Interrupt);
		resetTunerChangeFlag();
	}
	
	private void setProcessorStatus(AlgorithmStatus interrupt) {
		List<Agent> agentList = getAgentList();
		for (Agent agent : agentList){
			if (agent instanceof ProcessorAgent){
				if (agent.getStatus() == ProcessorStatus.Inprogress.name()){
					((ProcessorAgent) agent).setInterruptStatus();
					agent.statusTransfer();
				}
			}
		}
	}

	public void execute(AlgorithmManager manager){
//		if (algorithmThread == null || algorithmThread.isAlive())
			algorithmThread = new AlgorithmThread(this, manager);
		algorithmThread.start();
	}
	
	public void addStatusListener(AlgorithmStatusListener listener){
		if (statusListenerList == null)
			statusListenerList = new ArrayList<AlgorithmStatusListener>();
		statusListenerList.add(listener);
	}
	
	public void removeStatusListener(AlgorithmStatusListener listener){
		if (statusListenerList != null)
			statusListenerList.remove(listener);
	}
	
	private void fireStatusChanged(AlgorithmStatus status, int operationIndex){
		if (statusListenerList != null)
			for (AlgorithmStatusListener listener : statusListenerList){
				listener.onStatusChanged(status);
				listener.setStage(operationIndex, status);
			}
	}

	private void fireStatusChanged(AlgorithmStatus status){
		algorithmStatus = status;
		if (statusListenerList != null)
			for (AlgorithmStatusListener listener : statusListenerList){
				listener.onStatusChanged(status);
			}
	}

	/**
	 * @return the algorithmType
	 */
	public AlgorithmType getAlgorithmType() {
		return algorithmType;
	}
	
	public Object getDefaultAlgorithmResult() throws SignalNotAvailableException{
		Sink defaultSink = framework.getDefaultSink();
		if (defaultSink == null)
			throw new SignalNotAvailableException("no sink available");
		return defaultSink.getSignal();
	}
	
	public Tuner findTuner(String tunerName){
		for (Tuner tuner : getTunerArray()){
			if (tuner.getName().equals(tunerName))
				return tuner;
		}
		return null;
	}

	public void dispose() { 
		framework.dispose();
		frameworkConfiguration = null;
		currentSignal = null;
		currentSourcePort = null;
		currentSinkPort = null;
		currentSourcePortList.clear();
//		protected List<Object> historySignalList = null;
		tunerList.clear();
		for (Agent agent : agentList){
			agent.dispose();
		}
		agentList.clear();
		exceptionHandlerList.clear();
		configuration = null;
		algorithmThread = null;
		statusListenerList.clear();
		System.out.println("Algorithm disposed");
	}
	
	public Sink getSink(String sinkName){
		for (Sink sink : getSinkList()){
			if (sink.getName().equals(sinkName))
				return sink;
		}
		return null;
	}

	/**
	 * @return the algorithmStatus
	 */
	public AlgorithmStatus getAlgorithmStatus() {
		return algorithmStatus;
	}
}
