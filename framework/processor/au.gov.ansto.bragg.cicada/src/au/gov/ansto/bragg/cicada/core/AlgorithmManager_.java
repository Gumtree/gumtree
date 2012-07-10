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
package au.gov.ansto.bragg.cicada.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.cicada.core.AlgorithmConfiguration.AlgorithmType;
import au.gov.ansto.bragg.cicada.core.exception.ConfigurationException;
import au.gov.ansto.bragg.cicada.core.exception.ExportException;
import au.gov.ansto.bragg.cicada.core.exception.FailedToExecuteException;
import au.gov.ansto.bragg.cicada.core.exception.IllegalFileFormatException;
import au.gov.ansto.bragg.cicada.core.exception.LoadAlgorithmFileFailedException;
import au.gov.ansto.bragg.cicada.core.exception.NoneAlgorithmException;
import au.gov.ansto.bragg.cicada.core.exception.NoneAttributeException;
import au.gov.ansto.bragg.cicada.core.exception.SetTunerException;
import au.gov.ansto.bragg.cicada.core.exception.SubscribeFailException;
import au.gov.ansto.bragg.cicada.core.exception.TransferFailedException;
import au.gov.ansto.bragg.cicada.core.extension.AlgorithmSet;
import au.gov.ansto.bragg.cicada.core.internal.AgentThread;
import au.gov.ansto.bragg.cicada.core.internal.Algorithm_;
import au.gov.ansto.bragg.cicada.core.internal.Configuration;
import au.gov.ansto.bragg.cicada.core.internal.NcDRATask;
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
import au.gov.ansto.bragg.process.exception.IndexOutOfBoundException;
import au.gov.ansto.bragg.process.exception.NullSignalException;
import au.gov.ansto.bragg.process.port.Tuner;
import au.gov.ansto.bragg.process.processor.Sink;
import au.gov.ansto.bragg.process.processor.SinkListener;
import au.gov.ansto.bragg.process.util.SortedArrayList;

/**
 * The implementation of AlgorithmManager interface. Use this class to create
 * instances of Algorithm manager.
 * <p>
 * Example: AlgorithmManager am = new AlgorithmManager();
 * 
 * Created on 20/02/2007, 9:58:24 AM Last modified 17/04/2007, 9:58:24 AM
 * 
 * @see au.gov.ansto.bragg.cicada.core.AlgorithmManager
 * @see au.gov.ansto.bragg.cicada.core.Control
 * @see au.gov.ansto.bragg.cicada.core.Manage
 * @see au.gov.ansto.bragg.cicada.core.Query
 * @see au.gov.ansto.bragg.cicada.core.Subscribe
 * 
 * @author nxi
 * @since V1.4
 */

public class AlgorithmManager_ extends Common_ implements AlgorithmManager {

	public final static long serialVersionUID = 1L;

	/**
	 * @param currentAlgorithmID
	 *            : algorithm ID from the recipe file;
	 * @param algorithmFilename
	 *            : recipe filename of current algorithm;
	 * @param algorithmSetPath
	 *            : the full path of the algorithm set recipe files
	 * @param currentInput
	 *            : Current loaded algorithm input;
	 * @param algorithmFiles
	 *            : File array for available algorithm recipe files;
	 * @param availableAlgorithms
	 *            : available algorithms with title attributes loaded;
	 * @param loadedAlgorithmList
	 *            : List of loaded algorithms;
	 * @param runningAlgorithmList
	 *            : List of running algorithms;
	 * @param exporter
	 *            Exporter member that do exporting task
	 */
	protected int currentAlgorithmID = 0;
	protected String algorithmFilename;
	// protected String dataType;
	protected String algorithmSetPath;
	// protected String signalType;
	// protected Algorithm currentAlgorithm = null;
	protected AlgorithmInput currentInput;
	protected File[] algorithmFiles = null;
	protected Algorithm[] allAvailableAlgorithms = null;
	protected List<Algorithm> loadedAlgorithmList = null;
//	protected List<Algorithm> runningAlgorithmList = null;
	protected Exporter exporter = null;
	protected boolean isDisposed = false;
	protected List<Exporter> exporterList = null;
	private List<Thread> threadList = null;
	public DataManager dataManager = null;
	protected List<AlgorithmSet> algorithmSetList = null;
	protected AlgorithmSet currentAlgorithmSet;

	// protected Thread thread = null;

	// protected List<Object> signalList;

	/**
	 * Default constructor that creates an empty algorithm manager instance with
	 * no arguments.
	 * 
	 * @throws LoadAlgorithmFileFailedException
	 * @throws ConfigurationException
	 * @since V1.0
	 */
	public AlgorithmManager_() throws ConfigurationException,
			LoadAlgorithmFileFailedException {
		loadedAlgorithmList = new ArrayList<Algorithm>();
//		runningAlgorithmList = new ArrayList<Algorithm>();
		threadList = new ArrayList<Thread>();
		// newEmptyAlgorithm();
		loadConfiguration();
		try {
			createExporterList();
		} catch (Exception e) {
			throw new LoadAlgorithmFileFailedException(
					"failed to create exporter list " + e.getMessage());
		}
	}

	/**
	 * Algorithm manager constructor with a directory name parameter. The
	 * constructor will check available recipe files in the path and provide a
	 * list of available algorithm titles.
	 * 
	 * @param directoryName
	 *            the path of algorithm recipe files in xml.
	 * @throws LoadAlgorithmFileFailedException
	 *             failed to create algorithm from recipe file
	 * @deprecated since V2.0
	 */
	public AlgorithmManager_(String directoryName)
			throws LoadAlgorithmFileFailedException {
		loadedAlgorithmList = new ArrayList<Algorithm>();
//		runningAlgorithmList = new ArrayList<Algorithm>();
		loadDirectory(directoryName);
		// newEmptyAlgorithm();
	}

	/**
	 * Add an exporter object to the algorithm manager, with a given format
	 * name.
	 * 
	 * @param formatName
	 *            in String type
	 * @return an extension filename in String type, used for open specific type
	 *         of input file
	 * @throws ClassNotFoundException
	 *             , IllegalArgumentException
	 * @since V1.0
	 */
	public String addExporter(String formatName) throws ExportException {

		Format format = Format.valueOf(formatName);
		if (format == null)
			throw new ExportException("failed to create exporter: must provide format name");
		try {
			exporter = new Exporter_(this, format);
		} catch (Exception e) {
			throw new ExportException("failed to create exporter: can not find the format " + 
				format + ", " + e.getMessage(), e);
		}
		return exporter.getFormater().getExtensionName();
	}

	/**
	 * Load configuration file with a given workspace path. The configuration
	 * file is in the <workspacePath>/<cicadaPlutinID>/plugin.xml
	 * 
	 * @throws DocumentException
	 *             , IllegalNameSetException, NoneAttributeException,
	 *             IOException
	 * @deprecated since M2
	 * @since V1.2
	 * @see au.gov.ansto.bragg.cicada.core.AlgorithmManager#loadConfiguration(java.lang.String)
	 * @removed since V2.0
	 * 
	 *          public void loadConfiguration(String workspacePath) throws
	 *          DocumentException, IllegalNameSetException,
	 *          NoneAttributeException, IOException{ Configuration config; try {
	 *          config = new Configuration(workspacePath); } catch
	 *          throw new IOException(e.getMessage()); }
	 *          loadDirectory(config.getPluginPath()); }
	 */

	/**
	 * Add the thread to the thread list of the algorithm manager.
	 * 
	 * @param thread
	 *            algorithm thread instance
	 */
	public void addThread(Thread thread) {
		if (!threadList.contains(thread))
			threadList.add(thread);
	}

	/**
	 * This method do the routine when a transfer exception is thrown.
	 * 
	 * @param algorithm
	 *            object
	 * @param e
	 *            Exception instance
	 * @since V2.0
	 */
	public void catchTransferException(Algorithm algorithm, Exception e) {
//		getRunningAlgorithmList().remove(algorithm);
	}

	/**
	 * This method create the exporter list of the cicada algorithm. Each
	 * exporter in the list is referring to an exporting format.
	 * 
	 * @throws ClassNotFoundException
	 *             format not found
	 * @throws IllegalNameSetException
	 *             wrong format name
	 * @since V2.1
	 */
	private void createExporterList() throws ClassNotFoundException,
			IllegalNameSetException {
		String[] formatName = getExportFormat();
		exporterList = new LinkedList<Exporter>();
		for (int i = 0; i < formatName.length; i++) {
			// Format format = Format.valueOf(formatName[i]);
			// Exporter newExporter = new Exporter_(this, format);
			exporterList
					.add(new Exporter_(this, Format.valueOf(formatName[i])));
		}
	}

	/**
	 * Get an agent from the current algorithm with its ID as the parameter.
	 * 
	 * @param agentID
	 *            in int type
	 * @throws IndexOutOfBoundException
	 *             illegal agent id exception
	 * @throws NoneAlgorithmException
	 *             illegal algorithm handle
	 * 
	 * @deprecated in M2
	 * @since V1.2
	 * @see au.gov.ansto.bragg.process.agent.Agent
	 * @removed since V2.0
	 * 
	 *          public Agent getAgent(int agentID) throws
	 *          IndexOutOfBoundException, NoneAlgorithmException { return
	 *          (Agent)
	 *          SortedArrayList.get(getCurrentAlgorithm().getAgentList(),
	 *          agentID); }
	 */

	/**
	 * This method is called when algorithm manager life cycle is ready to
	 * finish.
	 * <p>
	 * Synchronized for multiple thread access.
	 * 
	 * @see au.gov.ansto.bragg.cicada.core.Control#dispose()
	 * @since V1.0
	 */
	public synchronized void dispose() {
		isDisposed = true;
		// if (thread != null){
		// synchronized(thread){
		// thread.notify();
		// }
		// }
	}

	/**
	 * This method put the algorithm loaded upon a data into garbage collection.
	 * The algorithm handles will be removed from the data, but they will still
	 * be referred by other object if necessary.
	 * 
	 * @since V2.0
	 */
	public void disposeData() {
		if (getCurrentAlgorithm() != null)
			loadedAlgorithmList.remove(getCurrentAlgorithm());
	}

	/**
	 * Call this method to process the processor framework. The method will feed
	 * the current signal into the processor framework, which will trigger the
	 * processing of the processor chain. The processing is in a new thread
	 * which is parallel with the main thread.
	 * 
	 * @throws NoneAlgorithmException
	 *             illegal algorithm handle
	 * @see au.gov.ansto.bragg.cicada.core.Control#execute()
	 * 
	 * @since V1.0
	 */
	public void execute() throws NoneAlgorithmException {
		if (getCurrentAlgorithm() == null)
			throw new NoneAlgorithmException("Algorithm == null");
		else {
//			runningAlgorithmList.add(getCurrentAlgorithm());
			getCurrentAlgorithm().execute(this);
		}
	}

	/**
	 * Use this method to make a given algorithm process.
	 * 
	 * @param algorithm
	 *            an instance of Algorithm
	 * @throws NoneAlgorithmException
	 *             illegal algorithm handle
	 * @reused in V2.6
	 * @since V1.0
	 * 
	 * @see au.gov.ansto.bragg.cicada.core.Algorithm
	 */
	public void execute(Algorithm algorithm) throws NoneAlgorithmException {
		if (algorithm == null)
			throw new NoneAlgorithmException("Algorithm == null");
		else {
//			runningAlgorithmList.add(algorithm);
			algorithm.execute(this);
			// Thread thread = new AlgorithmThread(algorithm, this);
			// thread.start();
		}
	}

	/**
	 * Process multiple algorithms at once. This method will create a thread for
	 * each algorithm in the list to do the processing. The thread will be put
	 * into a list.
	 * 
	 * @param algorithmInputList
	 *            a list of algorithm input with signal field and algorithm
	 *            filed initialized.
	 * @throws NoneAlgorithmException
	 * @throws FailedToExecuteException
	 */
	public void executeAll(List<AlgorithmInput> algorithmInputList)
			throws FailedToExecuteException {
		for (Iterator<AlgorithmInput> iter = algorithmInputList.iterator(); iter
				.hasNext();) {
			AlgorithmInput input = iter.next();
			if (input.isReady()) {
				// setCurrentInput(input);
				try {
					execute(input.algorithm);
				} catch (NoneAlgorithmException e) {
					throw new FailedToExecuteException(
							"Failed to processor algorithm in:\n"
									+ input.getName() + e.getMessage(), e);
				}
			} else
				throw new FailedToExecuteException(
						"Failed to processor algorithm in:\n"
								+ input.toString() + " is not ready, ");
		}
	}

	/**
	 * This method makes the algorithm that contains the agent start processing
	 * from the specified processor in the processor chain. The starting
	 * processor is referred by a given agent. A prerequisite of this method is
	 * the starting processor has the input ports set up before this method is
	 * called.
	 * 
	 * @param agent
	 *            a processor agent, which gives a view of the referred
	 *            processor.
	 * @throws TransferFailedException
	 *             the processing of the algorithm failed
	 * @since V2.4
	 */
	public void executeFrom(Agent agent) throws TransferFailedException {
		if (agent instanceof ProcessorAgent) {
			Thread thread = new AgentThread((ProcessorAgent) agent, this);
			thread.start();
			// try {
			// ((ProcessorAgent) agent).triger();
			// } catch (IllegalArgumentException e) {
			// e.printStackTrace();
			// throw new TunerNotReadyException(e.getMessage());
			// } catch (IllegalAccessException e) {
			// e.printStackTrace();
			// throw new TunerNotReadyException(e.getMessage());
			// } catch (Exception e) {
			// e.printStackTrace();
			// throw new TransferFailedException(e.getMessage());
			// }
		} else
			throw new TransferFailedException(
					"illegal agent to start the processor chain");
	}

	/**
	 * Get a loaded algorithm handle from the loaded algorithm list.
	 * 
	 * @return handle of algorithm instance
	 * @deprecated in M2
	 * @since V1.2
	 * @removed since V2.0
	 * 
	 *          public Algorithm getLoadedAlgorithm(int algorithmID) throws
	 *          IndexOutOfBoundException { return (Algorithm)
	 *          SortedArrayList.get(loadedAlgorithmList, algorithmID); }
	 */

	/**
	 * Use this method to find the algorithm container of a given agent. This
	 * method search the loaded algorithm list to find which algorithm handles
	 * the agent. If the container of the agent is not managed by this algorithm
	 * manager, it will return null.
	 * 
	 * @return algorithm instance in type of Algorithm
	 * @since V2.4
	 */
	public Algorithm findAlgorithmWithAgent(Agent agent) {
		Algorithm returnAlgorithm = null;
		for (Iterator<Algorithm> iter = loadedAlgorithmList.iterator(); iter
				.hasNext();) {
			Algorithm algorithm = iter.next();
			for (Iterator<Agent> agentIter = algorithm.getAgentList()
					.iterator(); agentIter.hasNext();) {
				if (agent.equals(agentIter.next())) {
					returnAlgorithm = algorithm;
					break;
				}
			}
			if (returnAlgorithm != null)
				break;
		}
		return returnAlgorithm;
	}

	/**
	 * Get a running algorithm handle from the running algorithm list.
	 * 
	 * @param algorithmID
	 *            in int type
	 * @return handle of algorithm instance
	 * @throws IndexOutOfBoundException
	 * 
	 * @deprecated in M1
	 * @since V1.1
	 * @removed since V2.0
	 * 
	 *          public Algorithm getRunningAlgorithm(int algorithmID) throws
	 *          IndexOutOfBoundException { return (Algorithm)
	 *          SortedArrayList.get(runningAlgorithmList, algorithmID); }
	 */

	/**
	 * Get an agent from a given algorithm with its ID as the parameter.
	 * 
	 * @param algorithm
	 *            an instance of Algorithm
	 * @param agentID
	 *            in int type
	 * @throws IndexOutOfBoundException
	 *             illegal agent id exception
	 * @throws NoneAlgorithmException
	 *             illegal algorithm handle
	 * @deprecated in M2
	 * 
	 * @since V1.0
	 * @see au.gov.ansto.bragg.process.agent.Agent
	 * @see au.gov.ansto.bragg.cicada.core.Query#getAgent(Algorithm, int)
	 */
	public Agent getAgent(Algorithm algorithm, int agentID) {
		try {
			return (Agent) SortedArrayList.get(algorithm.getAgentList(), agentID);
		} catch (IndexOutOfBoundException e) {
			return null;
		}
	}

	/**
	 * This method will return the signal type described in the cicada
	 * configuration file. It is a class name in String object. Since M1, it
	 * will be a sub-class of au.gov.ansto.bragg.process.signal.SignalType
	 * 
	 * @return String object
	 * 
	 * @see au.gov.ansto.bragg.cicada.core.AlgorithmManager#getSingalType()
	 * @see au.gov.ansto.bragg.process.signal.SignalType Get the signal type of
	 *      current loaded instrument.
	 */
	// public String getSingalType() {
	// return signalType;
	// }
	/**
	 * Get the list of agents of the algorithm currently loaded.
	 * 
	 * @throws NoneAlgorithmException
	 *             illegal algorithm handle
	 * @see au.gov.ansto.bragg.process.agent.Agent
	 * 
	 * @since V1.0
	 */
	public List<Agent> getAgentList() throws NoneAlgorithmException {
		if (getCurrentAlgorithm() == null)
			throw new NoneAlgorithmException("no loaded algorithm");
		return getCurrentAlgorithm().getAgentList();
	}

	/**
	 * Get the agent list of a given algorithm. Use this method to get the agent
	 * list and work on agents.
	 * 
	 * @param algorithm
	 *            an instance of Algorithm
	 * 
	 * @see au.gov.ansto.bragg.process.agent.Agent
	 * @see au.gov.ansto.bragg.cicada.core.Query#getAgentList(au.gov.ansto.bragg.cicada.core.Algorithm)
	 * @since V1.0
	 */
	public List<Agent> getAgentList(Algorithm algorithm) {
		return algorithm.getAgentList();
	}

	/**
	 * Get the instrument folder name. It is the path of the folder containing
	 * algorithm recipe file group.
	 * 
	 * @return path in String
	 * 
	 * @see au.gov.ansto.bragg.cicada.core.AlgorithmManager#getAlgorithmSetPath()
	 * @since V2.0
	 */
	public String getAlgorithmSetPath() {
		return algorithmSetPath;
	}

	/*
	 * Get signal of a sink agent;
	 * 
	 * public Object getSinkSignal(Sink sink) { return sink.getSignal(); }
	 */

	/**
	 * Get the tuner list of current algorithm loaded.
	 * 
	 * @param tunerID
	 *            in int type
	 * @return Tuner instance handle
	 * @throws IndexOutOfBoundException
	 *             illegal agent id exception
	 * @throws NoneAlgorithmException
	 *             illegal algorithm handle
	 * @since V1.0
	 * @removed since V2.0
	 * 
	 *          public Tuner getTuner(int tunerID) throws
	 *          IndexOutOfBoundException, NoneAlgorithmException { return
	 *          (Tuner) SortedArrayList.get(getCurrentAlgorithm()
	 *          .getTunerArray(), tunerID); }
	 */

	/**
	 * Get the list of available algorithms class.
	 * 
	 * @return array of algorithm instances
	 * @see Algorithm
	 * 
	 * @since V1.0
	 */
	public Algorithm[] getAvailableAlgorithmList() {
		List<Algorithm> algorithmList = new ArrayList<Algorithm>();
		for (int i = 0; i < allAvailableAlgorithms.length; i++) {
			if (!allAvailableAlgorithms[i].isHidden()) {
				algorithmList.add(allAvailableAlgorithms[i]);
			}
		}
		Algorithm[] algorithms = new Algorithm[algorithmList.size()];
		for (int i = 0; i < algorithms.length; i++) {
			algorithms[i] = algorithmList.get(i);
		}
		return algorithms;
		// return allAvailableAlgorithms;
	}

	/**
	 * This method get the current loaded algorithm handle. If no algorithm is
	 * loaded, the method an exception.
	 * 
	 * @return an instance of Algorithm
	 * 
	 * @see Algorithm
	 * @see au.gov.ansto.bragg.cicada.core.AlgorithmManager#getCurrentAlgorithm()
	 * @since V1.0
	 */
	public Algorithm getCurrentAlgorithm() {
		if (currentInput == null)
			return null;
		// else if (currentSignal.getAlgorithm() == null) throw new
		// NoneAlgorithmException();
		return currentInput.getAlgorithm();
		// if (currentAlgorithm == null) throw new NoneAlgorithmException();
		// return currentAlgorithm;
	}

	/**
	 * Implementation of getCurrentSignal() method in
	 * au.gov.ansto.bragg.cicada.core.AlgorithmManager The processor framework
	 * is subject to take any type of signal as the input. However in cicada,
	 * the signal is designed to be instrument specific signal type.
	 * 
	 * @return signal in Object container
	 * @since V1.0
	 */
	public AlgorithmInput getCurrentInput() {
		// if (currentInput == null) throw new
		// NullSignalException("null signal");
		return currentInput;
	}

	/**
	 * Return the raw signal of the data handled by the algorithm.
	 * 
	 * @since V2.0
	 */
	public IGroup getCurrentInputData() {
		return currentInput.getDatabag();
	}

	/**
	 * This method will call the find default attributes algorithm in the
	 * algorithm set. The default attributes are listed in a ascii file that
	 * specifies attribute names to load in that algorithm.
	 * 
	 * @return attribute name-value pairs in Map<String, Object> type.
	 * @throws NoneAttributeException 
	 * @since V2.4
	 */
	public Map<String, Object> getDefaultAttributes(IGroup databag) throws NoneAttributeException {
		Map<String, Object> attributes = new LinkedHashMap<String, Object>();
		if (databag.isRoot())
			attributes.put("number_of_entry", ((NcGroup) databag).getEntries().size());
		try {
			String filename = getAlgorithmSetPath() + "/default_attributes.txt";
			BufferedReader bufferReader = new BufferedReader(new FileReader(
					new File(filename)));
			if (bufferReader == null)
				throw new SignalNotAvailableException("failed to read from path table");
			while (bufferReader.ready()) {
				// String[] temp = bufferReader.readLine().split("=");
				String attributeName = bufferReader.readLine();
				if (attributeName.trim().startsWith("#"))
					continue;
				String location = databag.getLocation();
				if (attributeName.equals("location")){
					File locationFile = new File(location);
					attributes.put("location", locationFile.getAbsolutePath());
					continue;
				}
				// if (!location.startsWith("/")) location = "/" + location;
				// URI uri = new URI("file:" + location + "#" + attributeName);
				URI uri = ConverterLib.path2URI(location);
				uri = new URI(uri.toString() + "#" + attributeName);
				// System.out.println(uri.toString());
				// Object attributeValue = databag.findObject(attributeName);
//				Object attributeValue = dataManager.getObject(uri);
				Object attributeValue = databag.getContainer(attributeName);
				if (attributeValue != null) {
					if (attributeValue instanceof IDataItem)
						attributeValue = ((IDataItem) attributeValue).getData();
					if (attributeValue instanceof IAttribute)
						attributeValue = ((IAttribute) attributeValue)
								.getValue();
					attributes.put(attributeName, attributeValue);
				}
			}
//			attributes.put("data_location", databag.getLocation());

		} catch (Exception ex) {
			LoggerFactory.getLogger(this.getClass()).error("Can not load attributes", ex);
			throw new NoneAttributeException("failed to read default attribute: " + ex.getMessage(), ex);
		}
		return attributes;
	}

	/**
	 * This method will call the find entry algorithm in the general algorithm
	 * set. Find entry algorithm will be processed in the current algorithm
	 * manager thread and return an entry list.
	 * 
	 * @return list of entries.
	 * @since V2.4
	 */
	public List<IGroup> getEntryList(IGroup databag) {
		return ((NcGroup) databag).getEntries();
	}

	/**
	 * This method returns the exporter list of the processor chain.
	 * 
	 * @return exporter list in List object
	 * @since V2.1
	 */
	public List<Exporter> getExporterList() {
		return exporterList;
	}

	/**
	 * Read instrument recipe file in the instrument folder, get signal type
	 * from a line.
	 * 
	 * @throws IOException
	 * @since V1.0
	 * @removed since V2.0
	 */
	// protected void loadSignalType() throws IOException {
	// File instrumentFile = new File(instrumentDir + "/instrument");
	// BufferedReader reader = new BufferedReader(new FileReader(
	// instrumentFile));
	// String currentLine = null;
	// while ((currentLine = reader.readLine()) != null) {
	// if (currentLine.contains("signal_type")) {
	// signalType = currentLine.substring(
	// currentLine.indexOf("=") + 1, currentLine.length());
	// // System.out.println(signalType);
	// }
	// }
	// }
	/**
	 * Create a new empty algorithm in the algorithm list.
	 * 
	 * @throws IllegalNameSetException
	 * @see au.gov.ansto.bragg.cicada.core.AlgorithmManager#newEmptyAlgorithm()
	 * @since V1.0
	 * @removed since V2.0
	 */
	// public void newEmptyAlgorithm() throws IllegalNameSetException {
	// Algorithm algorithm = new Algorithm_();
	// // algorithm.setReceipeID(numberOfAlgorithms);
	// loadedAlgorithmList.add(algorithm);
	// currentAlgorithm = algorithm;
	// }
	/**
	 * Get the format names from the current exporter.
	 * 
	 * @return array of String
	 * @see au.gov.ansto.bragg.cicada.core.Control#getExportFormat()
	 * @since V1.0
	 */
	public String[] getExportFormat() {
		Format[] formatArray = Format.values();
		String[] formatNameArray = new String[formatArray.length];
		for (int i = 0; i < formatArray.length; i++) {
			formatNameArray[i] = formatArray[i].toString();
		}
		return formatNameArray;
	}

	/**
	 * Get the loaded algorithms in Array object.
	 * 
	 * @return array of algorithm instances
	 * 
	 * @since V1.0
	 */
	public Algorithm[] getLoadedAlgorithmList() {
		if (loadedAlgorithmList == null)
			return null;
		Algorithm[] loadedAlgorithmArray = new Algorithm_[loadedAlgorithmList
				.size()];
		int loadedAlgorithmID = 0;
		for (Iterator<Algorithm> iter = loadedAlgorithmList.iterator(); iter
				.hasNext(); loadedAlgorithmID++) {
			loadedAlgorithmArray[loadedAlgorithmID] = iter.next();
		}
		return loadedAlgorithmArray;
	}

	/**
	 * Save a loaded algorithm to a file.
	 * 
	 * 
	 * public void save(Algorithm algorithm, File algorithmFilename) { }
	 */

	/**
	 * Get the running algorithms in Array object.
	 * 
	 * @return array of algorithm instances
	 * 
	 * @since V1.0
	 */
	public List<Algorithm> getRunningAlgorithmList() {
		// Algorithm[] runningAlgorithmArray = new
		// Algorithm_[runningAlgorithmList
		// .size()];
		// int runningAlgorithmID = 0;
		// for (Iterator<Algorithm> iter = runningAlgorithmList.iterator(); iter
		// .hasNext(); runningAlgorithmID++) {
		// runningAlgorithmArray[runningAlgorithmID] = iter.next();
		// }
		// return runningAlgorithmArray;
//		return runningAlgorithmList;
		return null;
	}

	/**
	 * Set signal for a specific tuner. The tuner will pass it to the according
	 * VAR port.
	 * 
	 * @param signal
	 *            any signal type in Object container
	 * @param tuner
	 *            handle of Tuner instance
	 * @throws NullMethodException
	 *             , IllegalAccessException, InvocationTargetException,
	 *             InstantiationException
	 * @see au.gov.ansto.bragg.process.port.Tuner public void
	 *      setTunnerValue(Object signal, Tuner tuner) throws
	 *      NullMethodException, IllegalAccessException,
	 *      InvocationTargetException, InstantiationException {
	 *      tuner.setSignal(signal); }
	 */

	/**
	 * Get sink list of the algorithm currently loaded. (List of all sinks)
	 * 
	 * @return list of Sink instances
	 * @throws NoneAlgorithmException
	 *             no current algorithm loaded
	 * @see au.gov.ansto.bragg.process.processor.Sink
	 * @since V1.0
	 */
	public List<Sink> getSinkList() throws NoneAlgorithmException {
		if (getCurrentAlgorithm() == null)
			throw new NoneAlgorithmException("no loaded algorithm");
		return getCurrentAlgorithm().getSinkList();
	}

	/**
	 * Get the sink list of a loaded algorithm by an algorithm handle.
	 * 
	 * @return list of Sink instances
	 * @since V1.0
	 */
	public List<Sink> getSinkList(Algorithm algorithm) {
		return algorithm.getSinkList();
	}

	/**
	 * Get the list of all tuners in the current loaded algorithm.
	 * 
	 * @return array of Tuner
	 * @throws NoneAlgorithmException
	 *             illegal algorithm handle
	 * @see au.gov.ansto.bragg.process.port.Tuner
	 * @since V1.0
	 */
	public Tuner[] getTunerList() throws NoneAlgorithmException {
		if (getCurrentAlgorithm() == null)
			throw new NoneAlgorithmException("no loaded algorithm");
		// return (Tuner[]) getCurrentAlgorithm().getTunerArray().toArray();
		List<Tuner> tunerList = getCurrentAlgorithm().getTunerArray();
		Tuner[] tuners = new Tuner[tunerList.size()];
		for (int i = 0; i < tuners.length; i++) {
			tuners[i] = tunerList.get(i);
		}
		return tuners;
	}

	/**
	 * Change current algorithm handle to the given one
	 * 
	 * @return handle of algorithm instance
	 * @see au.gov.ansto.bragg.cicada.core.Manage#switchCurrentAlgorithm(Algorithm)
	 * @since V1.0
	 * @removed since V2.0
	 */
	// public Algorithm switchCurrentAlgorithm(Algorithm algorithm) {
	// return currentAlgorithm = algorithm;
	// return currentAlgorithm;
	// }
	/**
	 * Get the list of the tuners of the given algorithm handle.
	 * 
	 * @param algorithm
	 *            a handle of Algorithm instance
	 * @return array of Tuner
	 * @see au.gov.ansto.bragg.process.port.Tuner
	 * @since V1.0
	 */
	public Tuner[] getTunerList(Algorithm algorithm) {
		List<Tuner> tunerList = algorithm.getTunerArray();
		Tuner[] tunerArray = new Tuner[tunerList.size()];
		int tunerArrayIndex = 0;
		for (Iterator<Tuner> iter = tunerList.iterator(); iter.hasNext(); tunerArrayIndex++) {
			tunerArray[tunerArrayIndex] = iter.next();
		}
		return tunerArray;
	}

	/**
	 * Halt a running algorithm.
	 * 
	 * @param algorithm
	 *            in Algorithm type
	 * @since V1.0
	 */
	public void halt(Algorithm algorithm) {
	}

	/*
	 * Static method of getting a file path from the GUI. Moved to other class,
	 * blocked.
	 * 
	 * public static String getFilenameFromShell(Shell shell) { FileDialog
	 * fileDialog = new FileDialog(shell, SWT.MULTI);
	 * fileDialog.setFilterExtensions(new String[] { "*.hdf", "*.*" });
	 * fileDialog.setFilterNames(new String[] { "hdf5 format", "Any" });
	 * 
	 * String firstFile = fileDialog.open(); String filename = null; if
	 * (firstFile != null) { filename = fileDialog.getFilterPath() + "\\" +
	 * fileDialog.getFileName(); } return filename; }
	 */

	/**
	 * Halt all running algorithms.
	 * 
	 * @since V1.0
	 */
	public void haltAll() {
	}

	/**
	 * Return true if the algorithm manager's life cycle is ready to finish.
	 * 
	 * @return in boolean type
	 * @since V1.0
	 */
	public boolean isDisposed() {
		return isDisposed;
	}

	/**
	 * Load a algorithm from the available algorithm list, with a given
	 * available algorithm ID.
	 * 
	 * @param availableAlgorithm
	 *            in Algorithm type
	 * @return a new Algorithm instance
	 * @throws NullSignalException
	 *             no loaded signal
	 * @throws LoadAlgorithmFileFailedException
	 *             failed to load algorithm from recipe file
	 * @throws ConfigurationException 
	 * @see au.gov.ansto.bragg.cicada.core.Manage#loadAlgorithmFile(String)
	 * @since V1.0
	 */
	public synchronized Algorithm loadAlgorithm(Algorithm availableAlgorithm)
	throws LoadAlgorithmFileFailedException, ConfigurationException {
		// currentAlgorithm.loadAlgorithm(files[availableAlgorithmID]);
		if (getCurrentAlgorithm() != null)
			loadedAlgorithmList.remove(getCurrentAlgorithm());
		if (getCurrentInput() == null) {
			AlgorithmInput input = new AlgorithmInput();
			setCurrentInput(input);
		}
		getCurrentInput().loadAlgorithm(availableAlgorithm);
		// thread = new SignalThread(this);
		// sink.subscribe(thread);
		return getCurrentAlgorithm();
		/*
		 * if (availableAlgorithmID > availableAlgorithms.length) throw new
		 * Exception("Index out of boundary."); else currentAlgorithm =
		 * availableAlgorithms[availableAlgorithmID]; return currentAlgorithm;
		 */
	}

	/**
	 * Load an algorithm from an algorithm recipe file.
	 * 
	 * @param algorithmFilename
	 *            algorithm file name in String
	 * @throws LoadAlgorithmFileFailedException
	 *             failed to load algorithm
	 * @since V1.0
	 */
	public void loadAlgorithmFile(String algorithmFilename)
			throws LoadAlgorithmFileFailedException	{
		// File filename = new File(algorithmFilename);
		// newEmptyAlgorithm();
		// getCurrentAlgorithm().loadAlgorithm(filename);
		if (getCurrentAlgorithm() != null)
			loadedAlgorithmList.remove(getCurrentAlgorithm());
		if (getCurrentInput() == null)
			setCurrentInput(new AlgorithmInput());
		getCurrentInput().loadAlgorithm(algorithmFilename);
	}

	/**
	 * Load an algorithm to a list of GroupData signals. Return a list of
	 * algorithm inputs, each of which has a field of GroupData signal and a
	 * field of copy of the same algorithm.
	 * 
	 * @param dataList
	 *            a list of signals in GroupData type
	 * @param algorithm
	 *            an available Algorithm instance to be applied to multiple
	 *            inputs
	 * @return a list of algorithm inputs
	 * @throws LoadAlgorithmFileFailedException
	 *             failed to load the available algorithm
	 * @throws ConfigurationException 
	 * @since V2.6
	 */
	public List<AlgorithmInput> loadAlgorithmOnMultipleData(List<IGroup> dataList, Algorithm availableAlgorithm)
	throws LoadAlgorithmFileFailedException, ConfigurationException {
		List<AlgorithmInput> inputList = new ArrayList<AlgorithmInput>();
		if (dataList.size() == 0) {
			AlgorithmInput input = new AlgorithmInput();
			input = new AlgorithmInput();
			if (availableAlgorithm.isEmpty())
				input.loadAlgorithm(availableAlgorithm);
			else
				input.setAlgorithm(availableAlgorithm.clone());
			loadedAlgorithmList.add(input.getAlgorithm());
		} else {
			for (Iterator<IGroup> iter = dataList.iterator(); iter.hasNext();) {
				AlgorithmInput input = null;
				if (availableAlgorithm.isEmpty()) {
					input = new AlgorithmInput(iter.next());
					input.loadAlgorithm(availableAlgorithm);
				} else
					input = new AlgorithmInput(iter.next(), availableAlgorithm
							.clone());
				inputList.add(input);
				loadedAlgorithmList.add(input.getAlgorithm());
			}
		}
		return inputList;
	}

	/**
	 * The method will load a configuration file in the cicada project folder.
	 * The configuration file name the plugin.xml file in the plugin folder. It
	 * specifies the algorithm set to be loaded by the algorithm manager.
	 * <p>
	 * Built on 03 May 07
	 * 
	 * @throws ConfigurationException
	 *             failed to load the configuration file
	 * @throws LoadAlgorithmFileFailedException
	 *             failed to load algorithm from recipe file
	 * 
	 * @since V1.2
	 * @reduced visibility since V2.0
	 */
	private void loadConfiguration() throws ConfigurationException, LoadAlgorithmFileFailedException {
		Configuration config;
		try {
			/**
			 * comments added on 03 May 07 Passing an absolute path to the
			 * configuration constructor is replaced by calling a none argument
			 * constructor which load a file with a relative path
			 */
			// config = new Configuration(workspacePath);
			config = new Configuration();
		} catch (IOException e) {
			throw new ConfigurationException("failed to create empty configuration: " + e.getMessage()
					+ ": illegal URL of configuration file", e);
		}
		loadDirectory(config.getPluginPath());
		currentAlgorithmSet = config.getDefaultAlgorithmSet();
		algorithmSetList = config.getAlgorithmSetList();
	}

	/**
	 * This method load a databag from a given file path. The file path is a
	 * full path of an hdf file. The return signal of the method is an instance
	 * of GroupData, which is a databag that contains the structure of the hdf
	 * file, e.g., nested groups and cached variables. When this method are
	 * called, only group and variable structures are read. The data storages
	 * are not loaded into the memory.
	 * 
	 * @param uri
	 *            path of the group data
	 * @return databag in GroupData type
	 * @see au.gov.ansto.bragg.data.fileaccess.databag.CachedVariable
	 * @throws IllegalFileFormatException
	 *             illegal access to the file system
	 * @since V2.0
	 */
	public IGroup loadDataFromFile(URI uri) throws IllegalFileFormatException {
		// Group groupData = Group.loadFile(filename);
		IGroup groupData = null;
		String filename = uri.getPath();
		if (filename.endsWith("ASC")) {
			try {
				return importIgorData(uri);
			} catch (Exception e1) {
				throw new IllegalFileFormatException("failed to read ASC file " + uri.getPath() + 
						"\n" + e1.getMessage(), e1);
			}
		}
		try {
			groupData = dataManager.getGroup(uri);
		} catch (Exception e) {
			throw new IllegalFileFormatException("failed to load Nexus file " + uri.getPath() + "\n"
					+ e.getMessage(), e);
		}
		return groupData;
	}

	/**
	 * Load instrument algorithms directories. If there is a file with a name of
	 * instrument, regard it as an instrument folder.
	 * 
	 * @param directoryName
	 *            a path of algorithm directory containing recipe files, in
	 *            String
	 * @throws LoadAlgorithmFileFailedException
	 *             failed to load algorithm directory
	 * @see au.gov.ansto.bragg.cicada.core.AlgorithmManager#loadDirectory(java.lang.String)
	 * @since V1.0
	 */
	public final void loadDirectory(String directoryName)
			throws LoadAlgorithmFileFailedException {
		// throws IllegalNameSetException, DocumentException,
		// NoneAttributeException, IOException {
		File dir = new File(directoryName);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
		};
		algorithmFiles = dir.listFiles(filter);
		// allAvailableAlgorithms = new Algorithm[algorithmFiles.length];
		List<Algorithm> algorithmList = new ArrayList<Algorithm>();
		for (int i = 0; i < algorithmFiles.length; i++) {
			// System.out.println(files[i].getName());
			try{
				Algorithm algorithm = new Algorithm_(algorithmFiles[i]);
				algorithmList.add(algorithm);
			}catch (Exception e) {
			}
			// availableAlgorithms[i].setReceipeID(i);
			//availableAlgorithms[i].setName(Parse_.getAlgorithmName(files[i]));
		}
		allAvailableAlgorithms = new Algorithm[algorithmList.size()];
		int index = 0;
		for (Algorithm algorithm : algorithmList) {
			allAvailableAlgorithms[index] = algorithm;
			index++;
		}

		this.algorithmSetPath = directoryName;
		dataManager = DataManagerFactory.getDataManager(getAlgorithmSetPath()
				+ "/path_table.txt");
		// loadSignalType();
	}

	// public String getSignalFilename() {
	// return currentSignal.getFilename();
	// }

	/**
	 * Load an algorithm as a protocol to apply on multiple inputs.
	 * 
	 * @param availableAlgorithm
	 *            algorithm structure from the available algorithm list.
	 * @return initialized algorithm instance
	 * @throws LoadAlgorithmFileFailedException
	 *             failed to load the algorithm
	 */
	public Algorithm loadProtocol(Algorithm availableAlgorithm)
			throws LoadAlgorithmFileFailedException {
		Algorithm algorithm = new Algorithm_();
		algorithm.loadAlgorithm(availableAlgorithm.getFilename());
		return algorithm;
	}

	/**
	 * Reload a loaded algorithm.
	 * 
	 * @param algorithm
	 *            in Algorithm type
	 * @since V1.0
	 */
	public void reload(Algorithm algorithm) {
	}

	/**
	 * Remove the thread from the thread list of the algorithm manager.
	 * 
	 * @param thread
	 *            algorithm thread instance
	 */
	public void removeThread(Thread thread) {
		threadList.remove(thread);
	}

	/**
	 * Reset a loaded algorithm.
	 * 
	 * @param algorithm
	 *            in Algorithm type
	 * @since V1.0
	 */
	public void reset(Algorithm algorithm) {
		// algorithm.
	}

	/**
	 * Reset all loaded algorithms.
	 * 
	 * @since V1.0
	 */
	public void resetAll() {
	}

	/**
	 * Export the result of the processor chain to the given file, with a
	 * specific format created in the exporter.
	 * 
	 * @param filename
	 *            in String type
	 * @throws ExportException
	 * @see au.gov.ansto.bragg.cicada.core.Control#resultExport(java.lang.String)
	 * @since V1.0
	 */
	public void resultExport(URI fileURI) throws ExportException {
		exporter.resultExport(fileURI);
	}

	/**
	 * This method load existing algorithm input into the current signal field
	 * of the algorithm manager.
	 * 
	 * @param signal
	 *            databag signal in type
	 * @since V2.0
	 * @see au.gov.ansto.bragg.cicada.core.AlgorithmManager#getCurrentInput()
	 */
	public void setCurrentInput(AlgorithmInput input) {
		currentInput = input;
	}

	/**
	 * Set current signal. Will wrap the signal in an AlgorithmInput object.
	 * 
	 * @param databag
	 *            in GroupData type
	 * @see au.gov.ansto.bragg.cicada.core.AlgorithmManager#setCurrentInputData(Group)
	 * @since V1.0
	 */
	public void setCurrentInputData(IGroup databag) {

		currentInput = new AlgorithmInput(databag);
		// currentSignal.setDatabag(signal);
		// getCurrentAlgorithm().setCurrentSignal(signal);
	}

	/**
	 * Export any signal to the given file, with a specific format created in
	 * the exporter.
	 * 
	 * @param filename
	 *            in String type
	 * @param signal
	 *            in Object container, or children class of SignalType in cicada
	 * @throws ExportException
	 * @see au.gov.ansto.bragg.cicada.core.Control#signalExport(java.lang.String,
	 *      java.lang.Object)
	 * @since V1.0
	 */
	public void signalExport(URI fileURI, Object signal) throws ExportException {
		exporter.signalExport(signal, fileURI);
	}

	/**
	 * Export any type of signal to a given file, with a specific format created
	 * in the exporter. If transpose is set to true, export the signal in a
	 * transpose mode.
	 * 
	 * @param filename
	 *            in String type
	 * @param signal
	 *            can be any signal for processor framework, but has to be
	 *            SignalType type for cicada
	 * @throws ExportException
	 *             failed to export
	 * @see au.gov.ansto.bragg.cicada.core.Control#signalExport(java.lang.String,
	 *      java.lang.Object, boolean)
	 * @since V1.3
	 */
	public void signalExport(URI fileURI, Object signal, boolean transpose)
			throws ExportException {
		exporter.signalExport(signal, fileURI, transpose);
	}

	/**
	 * Export any signal to the given file, with a specific format created in
	 * the exporter.
	 * 
	 * @param filename
	 *            in String type
	 * @param signal
	 *            in Object container, or children class of SignalType in cicada
	 * @param title
	 *            in String type
	 * @throws ExportException
	 * @see au.gov.ansto.bragg.cicada.core.Control#signalExport(java.lang.String,
	 *      java.lang.Object)
	 * @since V1.2
	 */
	public void signalExport(URI fileURI, Object signal, String title) throws ExportException {
		exporter.signalExport(signal, fileURI, title);
	}

	/**
	 * Subscribe a thread instance to a specific agent. If the agent updates its
	 * signal, it will notify all the subscribed threads and updating routines
	 * will be activated by the threads. This instance will be informed when the
	 * agent status changes.
	 * 
	 * @param receiver
	 *            customized Thread instance
	 * @param agent
	 *            agent handle
	 * @throws SubscribeFailException
	 *             failed to subscribe
	 * @since V2.4
	 */
	public void subscribeSink(AgentListener listener, Agent agent) {
		agent.subscribe(listener);
	}

	/**
	 * Subscribe a variable instance to a specific sink. This instance will be
	 * informed when the sink get new signal.
	 * 
	 * @param listener
	 *            in Thread object
	 * @param sink
	 *            a handle of instance of Sink
	 * @since V1.0
	 * @deprecated for V3.2, use {@link #subscribeSink(SinkListener, Sink)}
	 *             instead
	 */
	public void subscribeSink(Thread listener, Sink sink){
		sink.subscribe(listener);
	}

	public void subscribeSink(SinkListener listener, Sink sink) {
		sink.subscribe(listener);
	}

	/*
	 * public void runEchidna(Shell shell){
	 * Echidna.getDataSignal(getCurrentAlgorithm().getCurrentSignalList(),
	 * ControlSignal.loadData, shell); }
	 * 
	 * public void runEchidna(){
	 * Echidna.getDataSignal(getCurrentAlgorithm().getCurrentSignalList(),
	 * ControlSignal.loadData); }
	 */
	/**
	 * Detail of the algorithm instance. The method will return a String object
	 * that has detail information of the algorithm. For example it contains the
	 * processor framework, the algorithm loaded and so on.
	 * 
	 * @return String object
	 * @see au.gov.ansto.bragg.process.common.Common_#toString()
	 * @since V1.0
	 */
	@Override
	public String toString() {
		String result = "<algorithm_manager>\n";
		for (int i = 0; i < allAvailableAlgorithms.length; i++)
			result += allAvailableAlgorithms[i].toString();
		result += "</algorithm_manager>\n";
		return result;
	}

	/**
	 * Unload a loaded Algorithm from the loaded Algorithm list.
	 * 
	 * @param algorithm
	 *            an Algorithm instance handle
	 * @since V1.0
	 */
	public void unloadAlgorithm(Algorithm algorithm) {
		loadedAlgorithmList.remove(algorithm);
		if (getCurrentAlgorithm() == algorithm) {
			getCurrentInput().unloadAlgorithm();
			currentInput = null;
		}
	}

	/**
	 * Unsubscribe a thread instance from a specific agent. Remove it from the
	 * listener list of the agent.
	 * 
	 * @param receiver
	 *            Thread instance
	 * @param agent
	 *            agent handle
	 * @throws SubscribeFailException
	 *             failed to unsubscribe
	 * @since V2.4
	 */
	public void unsubscribeSink(AgentListener listener, Agent agent) {
		agent.unsubscribe(listener);
	}

	/**
	 * Unsubscribe a variable instance to a specific sink.
	 * 
	 * @param listener
	 *            a Thread instance
	 * @param sink
	 *            a Sink instance
	 * @throws NullPointerException
	 * @see Sink
	 * @since V1.0
	 * @deprecated for V3.2, use {@link #unsubscribeSink(SinkListener, Sink)}
	 *             instead
	 */
	public void unsubscribeSink(Thread listener, Sink sink)
			throws NullPointerException {
		sink.unsubscribe(listener);
	}

	/**
	 * Remove a listener from a sink.
	 * 
	 * @param listener
	 *            SinkListener object
	 * @param sink
	 *            Sink object
	 * @throws NullPointerException
	 *             Created on 22/09/2008
	 */
	public void unsubscribeSink(SinkListener listener, Sink sink)
			throws NullPointerException {
		sink.unsubscribe(listener);
	}

	/**
	 * Set a tuner that shared by a list of algorithms. The tuners should have
	 * the same name as specified by the parameter.
	 * 
	 * @param algorithmInputs
	 *            list of AlgorithmInput object
	 * @param tunerName
	 *            in String type
	 * @param value
	 *            any type of object that can be accepted by the tuners
	 * @throws SetTunerException
	 */
	public void setTunerOfAlgorithms(List<AlgorithmInput> algorithmInputs,
			String tunerName, Object signal) throws SetTunerException {
		for (Iterator<AlgorithmInput> iter = algorithmInputs.iterator(); iter
				.hasNext();) {
			Algorithm algorithm = iter.next().getAlgorithm();
			if (algorithm != null) {
				Tuner tuner = null;
				try {
					tuner = (Tuner) SortedArrayList.getObjectFromName(algorithm
							.getTunerArray(), tunerName);
				} catch (Exception e) {
					e.printStackTrace();
					throw new SetTunerException("failed to set tuner "
							+ tunerName);
				}
				try {
					tuner.setSignal(signal);
				} catch (Exception e) {
//					e.printStackTrace();
					// throw new SetTunerException("failed to set tuner " +
					// tunerName);
					throw new SetTunerException("failed to set tuner, "
							+ e.getMessage(), e);
				}
			}
		}
	}

	public IGroup importIgorData(URI uri) throws IllegalFileFormatException {
		IGroup groupData = null;
		// String importHeaderFile = getAlgorithmSetPath() +
		// "/import_header.txt";
		String importHeaderFile = getAlgorithmSetPath() + "/path_table.txt";
		try {
			groupData = dataManager.importIgorData(uri, importHeaderFile);
		} catch (Exception e) {
			throw new IllegalFileFormatException("failed to load igor data: " + e.getMessage(), e);
		}
		// System.out.println(getAlgorithmSetPath() + "/path_table.txt");
		// groupData.initialiseDictionary(getAlgorithmSetPath() +
		// "/path_table.txt");
		setCurrentInputData(groupData);
		return groupData;
	}

	public List<AlgorithmSet> getAlgorithmSetList() {
		return algorithmSetList;
	}

	public void switchToAlgorithmSet(AlgorithmSet algorithmSet)
			throws LoadAlgorithmFileFailedException {
		try {
			loadDirectory(algorithmSet.getPath());
			currentAlgorithmSet = algorithmSet;
		} catch (FileAccessException e) {
			throw new LoadAlgorithmFileFailedException(e.getMessage());
		}
	}

	// new method

	/**
	 * This method returns a list of all algorithms that are available in the
	 * recipe files folder.
	 * 
	 * @return array of algorithms
	 * @since V2.8
	 */
	public Algorithm[] getAllAvailableAlgorithms() {
		return allAvailableAlgorithms;
	}

	public void exportAlgorithmConfiguration(Algorithm algorithm, URI fileURI,
			String configurationName) throws ExportException {
		try {
			algorithm.exportConfiguration(fileURI, configurationName);
		} catch (Exception e) {
			throw new ExportException(e.getMessage());
		}
	}

	public Exporter getExporter(Format format) throws ExportException {
		Exporter exporter = null;
		for (Exporter existExporter : exporterList) {
			if (existExporter.getFormater().getFormat() == format)
				return existExporter;
		}
		try {
			exporter = new Exporter_(new Formater_(format));
		} catch (Exception e) {
			throw new ExportException("illegal format");
		}
		exporterList.add(exporter);
		return exporter;
	}

	public DRATask createDRATask(String taskName, Algorithm algorithm) throws ConfigurationException {
		DRATask task = null;
		try {
			task = new NcDRATask(Factory.createEmptyDatasetInstance().getRootGroup(),
					taskName, getCurrentAlgorithmSet().getId(), algorithm);
		} catch (IOException e) {
			throw new ConfigurationException("failed to create configuration: " + e.getMessage(), e);
		}
		return task;
	}

	public AlgorithmSet getCurrentAlgorithmSet() {
		return currentAlgorithmSet;
	}

	public DRATask loadDRATask(URI uri) throws IllegalFileFormatException {
		IGroup group = null;
		group = loadDataFromFile(uri);
		if (group.getGroupList().size() == 0)
			return null;
		DRATask task = new NcDRATask((IGroup) group.getGroupList().get(0));
		if (!task.isValid())
			throw new IllegalFileFormatException("failed to load task from " + uri.getPath() + 
					": not a DRA task file");
		return task;
	}

	public Algorithm findAlgorithm(String algorithmSetId, String algorithmName)
			throws LoadAlgorithmFileFailedException {
		AlgorithmSet algorithmSet = null;
		for (AlgorithmSet set : algorithmSetList) {
			if (set.getId().equals(algorithmSetId)) {
				algorithmSet = set;
			}
		}
		if (algorithmSet == null)
			throw new LoadAlgorithmFileFailedException(
					"the algorithm set does not exist: " + algorithmSetId);
		if (currentAlgorithmSet.getId().matches(algorithmSetId))
			switchToAlgorithmSet(algorithmSet);
		return findAlgorithm(algorithmName);
	}

	public Algorithm findAlgorithm(String algorithmName) {
		for (Algorithm algorithm : allAvailableAlgorithms) {
			if (algorithm.getName().equals(algorithmName))
				return algorithm;
		}
		return null;
	}

	public Algorithm[] getAnalysisAlgorithms() {
		List<Algorithm> algorithmList = new ArrayList<Algorithm>();
		Algorithm[] availableAlgorithms = getAvailableAlgorithmList();
		for (int i = 0; i < availableAlgorithms.length; i++) {
			if (availableAlgorithms[i].getAlgorithmType() == AlgorithmType.analysis) {
				algorithmList.add(availableAlgorithms[i]);
			}
		}
		Algorithm[] algorithms = new Algorithm[algorithmList.size()];
		for (int i = 0; i < algorithms.length; i++) {
			algorithms[i] = algorithmList.get(i);
		}
		return algorithms;
	}

	public Algorithm[] getExperimentAlgorithms() {
		List<Algorithm> algorithmList = new ArrayList<Algorithm>();
		for (int i = 0; i < allAvailableAlgorithms.length; i++) {
			if (allAvailableAlgorithms[i].getAlgorithmType() == AlgorithmType.experiment) {
				algorithmList.add(allAvailableAlgorithms[i]);
			}
		}
		Algorithm[] algorithms = new Algorithm[algorithmList.size()];
		for (int i = 0; i < algorithms.length; i++) {
			algorithms[i] = algorithmList.get(i);
		}
		return algorithms;
	}

}
