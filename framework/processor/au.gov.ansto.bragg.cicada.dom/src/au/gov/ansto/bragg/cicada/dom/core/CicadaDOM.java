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
package au.gov.ansto.bragg.cicada.dom.core;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.cicada.core.Algorithm;
import au.gov.ansto.bragg.cicada.core.AlgorithmInput;
import au.gov.ansto.bragg.cicada.core.AlgorithmManager;
import au.gov.ansto.bragg.cicada.core.AlgorithmManager_;
import au.gov.ansto.bragg.cicada.core.exception.ConfigurationException;
import au.gov.ansto.bragg.cicada.core.exception.ExportException;
import au.gov.ansto.bragg.cicada.core.exception.IllegalFileFormatException;
import au.gov.ansto.bragg.cicada.core.exception.LoadAlgorithmFileFailedException;
import au.gov.ansto.bragg.cicada.core.exception.NoneAlgorithmException;
import au.gov.ansto.bragg.cicada.core.exception.SetTunerException;
import au.gov.ansto.bragg.cicada.core.exception.SinkSignalException;
import au.gov.ansto.bragg.cicada.core.exception.TransferFailedException;
import au.gov.ansto.bragg.cicada.core.extension.AlgorithmSet;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.process.exception.ProcessFailedException;
import au.gov.ansto.bragg.process.exception.ProcessorChainException;
import au.gov.ansto.bragg.process.port.Tuner;
import au.gov.ansto.bragg.process.processor.Sink;

public class CicadaDOM {

	AlgorithmManager algorithmManager = null;
//	OneDVis pattern = null;
//	TwoDVis map = null;
	
	public CicadaDOM() throws ConfigurationException, LoadAlgorithmFileFailedException{
		super();
		algorithmManager = new AlgorithmManager_();
//		IAnalysisFactory  analysisFactory = IAnalysisFactory.create();
//		IFitFactory       fitFactory = analysisFactory.createFitFactory();
//		IFitter jminuit = fitFactory.createFitter("Chi2","jminuit");
	}
	
	public String listAvailableAlgorithms(){
		Algorithm[] algorithms = algorithmManager.getAllAvailableAlgorithms();
//		String[] algorithmString = new String[algorithms.length];
		String message = "";
		for (int i = 0; i < algorithms.length; i ++){
//			algorithmString[i] = algorithms[i].getName();
			String thisMessage = i + " -- " + algorithms[i].getName() + " : " + 
				algorithms[i].getShortDescription();
			System.out.println(thisMessage);
			message += thisMessage + "\n";
		}
		return message;
	}
	
	public String[] loadDir(final String dirPath, final String extensionName){
		File dir = new File(dirPath);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(extensionName);
			}
		};
//		String[] files = dir.list(filter);
		File[] fileArray = dir.listFiles(filter);
		String[] files = new String[fileArray.length];
		for (int i = 0; i < files.length; i++) {
			files[i] = fileArray[i].toURI().getPath();
		}
		return files;
	}
	
	public IGroup loadDataFromURI(URI uri) throws IllegalFileFormatException {
		IGroup data = null;
		 data = algorithmManager.loadDataFromFile(uri);
		 algorithmManager.setCurrentInputData(data);
		return data;		
	}
	
	public IGroup loadDataFromFile(String filename) throws IllegalFileFormatException {
		URI uri = null;
		IGroup data = null;
		try{
//			String uriString = null;
//			if (filename.startsWith("/")) uriString = "file:" + filename;
//			else uriString = "file:/" + filename;
//			uri = new URI(uriString);
			uri = ConverterLib.path2URI(filename);
		} catch (FileAccessException e){
			throw new IllegalFileFormatException("Can not parse the file path:" + e.getMessage(), e);
		}
		 data = algorithmManager.loadDataFromFile(uri);
//		 algorithmManager.setCurrentInputData(data);
		return data;
	}
	
	public String listEntry(IGroup rootGroup){
		String result = "";
		int counter = 0;
		List<IGroup> entries = algorithmManager.getEntryList(rootGroup);
		for (Iterator<IGroup> iter = entries.iterator(); iter.hasNext();){
			result += counter + " -- " + iter.next().getShortName() + "\n";
			counter ++;
		}
		return result;
	}
	
	public IGroup loadEntry(IGroup rootGroup, int i){
		List<IGroup> entries = algorithmManager.getEntryList(rootGroup);
		if (i >= entries.size()){
			System.out.println("no such entry.");
			return null;
		}
		IGroup databag = entries.get(i);
		algorithmManager.setCurrentInputData(databag);
		return databag; 
	}
	
	public IGroup importIgorData(String filename) throws IllegalFileFormatException{
		URI uri = null;
		IGroup data = null;
		try{
			String uriString = null;
			if (filename.startsWith("/")) uriString = "file:" + filename;
			else uriString = "file:/" + filename;
			uri = new URI(uriString);
		} catch (URISyntaxException e){
			throw new IllegalFileFormatException("Can not parse the file path:" + e.getMessage(), e);
		}
		 data = algorithmManager.importIgorData(uri);
		return data;
	}
	
	public AlgorithmInput loadInputData(IGroup inputData){
		algorithmManager.setCurrentInputData(inputData);
		return algorithmManager.getCurrentInput();
	}
	
	
	public void hdfExport(IGroup signal, String filename) 
	throws ExportException, IllegalFileFormatException{
		URI fileURI = null;
		try {
			fileURI = new URI(filename);
		} catch (URISyntaxException e) {
			throw new IllegalFileFormatException("failed to access file " + filename + ": " + 
					e.getMessage(), e);
		}
		algorithmManager.addExporter("hdf");
		algorithmManager.signalExport(fileURI, signal);
	}
	
	public Algorithm loadAlgorithm(Integer algorithmID) 
	throws LoadAlgorithmFileFailedException, ConfigurationException{
		Algorithm[] algorithms = algorithmManager.getAllAvailableAlgorithms();
		if (algorithmID < 0 || algorithmID >= algorithms.length){
			System.out.println("No such algorithm id.");
			return null;
		}
		algorithmManager.loadAlgorithm(algorithms[algorithmID]);
//		Algorithm algorithm = algorithmManager.getCurrentAlgorithm();
		return algorithmManager.getCurrentAlgorithm();
	}
	
	public Algorithm loadAlgorithm(String algorithmName) throws NoneAlgorithmException{
//		Algorithm[] algorithms = algorithmManager.getAvailableAlgorithmList();
		Algorithm[] algorithms = algorithmManager.getAllAvailableAlgorithms();
		for (int i = 0; i < algorithms.length; i ++){
			if (algorithms[i].getName().equals(algorithmName)){
				try{
					return loadAlgorithm(i);
				}catch (Exception e) {
					throw new NoneAlgorithmException(
							"can not load the target algorithm\n" + e.getMessage(), e);
				}
			}
		}
		System.out.println("Can not find such algorithm.");
		throw new NoneAlgorithmException("algorithm not available");
	}
	
//	public void loadAlgorithm() throws LoadAlgorithmFileFailedException, ConfigurationException{
//		loadAlgorithm(5);
//	}
	
	public String listTuners() throws NoneAlgorithmException{
		Tuner[] tuners = null;
		String message = "";
		tuners = algorithmManager.getTunerList();
		for (int i = 0; i < tuners.length; i++) {
			String thisMessage = i + " -- " + tuners[i].getName() + " : " + tuners[i].getType();
			System.out.println(thisMessage);
			message += thisMessage + "\n";
		}
		return message;
	}
	
	public void setTuner(Integer tunerID, Object value) 
	throws NoneAlgorithmException, ProcessorChainException, ProcessFailedException{
		Tuner[] tuners = null;
		tuners = algorithmManager.getTunerList();
		if (tunerID < 0 || tunerID >= tuners.length){
			System.out.println("Can not find such tuner.");
			return;
		}
		Tuner tuner = tuners[tunerID];
		tuner.setSignal(value);
	}

	public void setTuner(String tunerName, Object value) 
	throws ProcessorChainException, ProcessFailedException, NoneAlgorithmException {
		Tuner[] tuners = null;
		tuners = algorithmManager.getTunerList();
		boolean isTunerAvailable = false;
		for (int i = 0; i < tuners.length; i++) {
			Tuner tuner = tuners[i];
			if (tuner.getCoreName().equals(tunerName)){
				isTunerAvailable = true;
				tuner.setSignal(value);
				break;
			}
		}
		if (!isTunerAvailable)
			throw new NoSuchFieldError("Can not find such tuner : " + tunerName);
	}
	
	public Object process() throws SetTunerException, TransferFailedException, NoneAlgorithmException{
		Algorithm algorithm = algorithmManager.getCurrentAlgorithm();
		if (algorithm == null){
			System.out.println("No algorithm loaded.\nPlease load an algorithm first.");
			return null;
		}
		algorithm.setUnchangedTuners();
		algorithm.transfer();
		List<Sink> sinks = null;
		sinks = algorithmManager.getSinkList();
		if (sinks.size() == 0) {
			System.out.println("No available output signal.");
			return null;
		}
		Sink sink = sinks.get(sinks.size() - 1);
		Object result = sink.getSignal();
		return result;
	}
	
	public String listResults() throws NoneAlgorithmException{
		List<Sink> sinks = null;
		String message = "";
		sinks = algorithmManager.getSinkList();
		if (sinks.size() == 0) {
			String thisMessage = "No available output signal.";
			System.out.println(thisMessage);
			return thisMessage;
		}
		int resultID = 0;
		for (Iterator<?> iterator = sinks.iterator(); iterator.hasNext();) {
			Sink sink = (Sink) iterator.next();
			String thisMessage = resultID + " -- " + sink.getName() + " : " + 
			sink.getSignal().getClass().toString();
			System.out.println(thisMessage);
			message += thisMessage + "\n";
			resultID ++;
		}
		return message;
	}
	
	public Object getDefaultResult() throws SignalNotAvailableException {
		return algorithmManager.getCurrentAlgorithm().getDefaultAlgorithmResult();
	}
	
	public Object getResult(Integer resultID) throws NoneAlgorithmException, SinkSignalException{
		List<Sink> sinks = null;
		sinks = algorithmManager.getSinkList();
		if (sinks.size() == 0) {
			throw new SinkSignalException("No available output signal.");
		}
		if (resultID < 0 || resultID >= sinks.size()){
			throw new SinkSignalException("No such result ID.");
		}
		Sink sink = sinks.get(resultID);
		Object result = sink.getSignal();
		if (result == null){
			throw new SinkSignalException("Result signal is not available.");
		}
		return result;
	}
	
	public void saveResult(Integer resultID, String path) 
	throws NoneAlgorithmException, SinkSignalException, ExportException, IllegalAccessException{
		URI fileURI = null;
		try {
			fileURI = new URI(path);
		} catch (URISyntaxException e) {
			throw new IllegalAccessException("can not access the file " + path);
		}
		algorithmManager.addExporter("text");
		Object signal = getResult(resultID);
		algorithmManager.signalExport(fileURI, signal);
	}
	
//	public void plotResult(Object result){
////		if (result instanceof Array) plot((Array) result, "result", shell);
////		else if (result instanceof DataItem) plot((DataItem) result, shell);
////		else if (result instanceof Group) plot((Group) result, shell);
//		if (result instanceof Group) {
//			PlotSignal.plot(algorithmManager, (Group) result);
//		}
//	}
	
	public String resultToString(IGroup result) throws SinkSignalException{
		String message = "";
		List<?> dataItemList = result.getDataItemList();
		for (Iterator<?> iterator = dataItemList.iterator(); iterator.hasNext();) {
			IDataItem item = (IDataItem) iterator.next();
			try {
				IArray value = item.getData();
				message += item.getShortName() + " = " + value.toString() + "\n";
			} catch (IOException e) {
				throw new SinkSignalException("Can not print the data item value");
			}
		}
		return message;
	}
	
	public void wait(int sec){
		try {
			Thread.currentThread().wait(sec / 1000);
		} catch (InterruptedException e) {
		}
	}
	
	@SuppressWarnings("deprecation")
	public IGroup createGroup(Object javaArray, String name) throws IllegalFileFormatException{
		IDataset dataset = null;
		try {
			dataset = Factory.createEmptyDatasetInstance();
		} catch (Exception e1) {
			throw new IllegalFileFormatException("can not create a group: " + e1.getMessage(), e1);
		}
		IArray array = Factory.createArray(javaArray);
//		Group parent = algorithmManager.getCurrentInputData();
//		Group group = Factory.createGroup(parent.getDataset(), parent, name, true);
//		Group group = Factory.createGroup(null, null, name, true);
		IGroup group = Factory.createGroup(dataset, dataset.getRootGroup(), "data", true);
		IDataItem dataItem = null;
		try {
			dataItem = Factory.createDataItem(group, "data", array);
		} catch (InvalidArrayTypeException e) {
			throw new IllegalFileFormatException("can not create a group from the data: " + e.getMessage(), e);
		}
		((NcGroup) group).buildResultGroup(dataItem);
//		group.addSubgroup(g)
//		group.setShortName(name)
//		group.getShortName()
		return group;
	}

	public IGroup createSubGroup(IGroup parent, String name){
		return Factory.createGroup(parent.getDataset(), parent, name, true);
	}
	
	public void switchAlgorithmSet(String algorithmSetName) throws LoadAlgorithmFileFailedException{
		List<AlgorithmSet> algorithmSetList = algorithmManager.getAlgorithmSetList();
		for (Iterator<?> iterator = algorithmSetList.iterator(); iterator.hasNext();) {
			AlgorithmSet algorithmSet = (AlgorithmSet) iterator.next();
			if (algorithmSet.getName().contains(algorithmSetName)){
				algorithmManager.switchToAlgorithmSet(algorithmSet);
			}
		}
	}
//	
//	private void plot(final Array array, final String title, final Shell shell){
//		int[] shape = array.getShape();
//		
//		if (shape.length == 1){
//			if (shape[0] == 1){
////				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
////					public void run() {
////						text.setText(array.getStorage().toString());
////					}
////				});
//			}else if (shape[0] == 2){
//				double[] point = (double[]) array.copyTo1DJavaArray();
//				final int y = Integer.parseInt(String.valueOf(Math.round(point[0])));
//				final int x = Integer.parseInt(String.valueOf(Math.round(point[1])));
//				System.out.println("center is: y=" + y + ", x =" + x + " (real value: " + point[0] + " " + point[1] + ")");
//				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
//					public void run() {
//						addBrightPoint(y, x);
//					}
//				});
//			}
//			else{
//				double[] x = new double[shape[0]];
//				double[] y = (double[]) array.copyTo1DJavaArray();
//				double[] err = new double[shape[0]];
//				for (int i = 0; i < shape[0]; i++){
//					x[i] = i + 1;
//					err[i] = Math.sqrt(y[i]);
//				}
//				final PlotData1D patternData = 
//					new PlotData1D(x, y, err, java.awt.Color.blue, title);
//				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
//					public void run() {
//						if (pattern == null) drawPattern(patternData);
//						else{
//							pattern.setTitle(title);
//							pattern.setPlotData(patternData);
//						}
//					}
//				});
//			}
//		}else if (shape.length == 2){
//			if (shape[0] <= 3){
//				double[] x = (double[]) array.slice(0, 2).copyTo1DJavaArray();
//				double[] y = (double[]) array.slice(0, 0).copyTo1DJavaArray();
//				double[] err = (double[]) array.slice(0, 1).copyTo1DJavaArray();
////				double[] err = null;
////				if (shape[0] < 3){
////					err = new double[shape[1]];
////					for (int i = 0; i < shape[1]; i ++) err[i] = 0;
////				}else err = (double[]) array.slice(0, 2).copyTo1DJavaArray();
//				final PlotData1D patternData = 
//					new PlotData1D(x, y, err, java.awt.Color.blue, title);
//				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
//					public void run() {				
//						if (pattern == null) drawPattern(patternData);
//						else{
//							pattern.setTitle(title);
//							pattern.setPlotData(patternData);
//						}
//					}
//				});
//			}else{
//				final double[][] mapData = get2DDouble(array);
//				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
//					public void run() {
//						if (map == null) drawMap(mapData, title);
//						else {
//							map.setTitle(title);
////							g3d.setTitle("Plot 3D Graph");
////							graph.setPlotData(new double[][] {{1, 2, 3,2}, {3, 2, 1,2}, {4, 5, 3,2},{8,7,5,2},{6,3,1,2}});
////							map.setSize(mapData.length, mapData[0].length);
//							map.canScaleAxes();
//							map.setPlotData(mapData,true);	
////							map.redraw();
//							map.maskManipulator().setMaximum(mapData[0].length);
//							map.maskManipulator().setMaximumVal(mapData.length);
////							map.getData();
//						}
//					}
//				});
//			}
//		}else if (shape.length == 3){
//			double[][][] cubeData = get3DDouble(array);
////			double[][] stickedData = getMatrixSignal(cubeData);
//			final double[][] oneSlice = cubeData[0];
////			drawMap(stickedData, variable.getName());
//			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
//				public void run() {
//					if (map == null) drawMap(oneSlice, title, shell);
//					else {
//						map.setTitle(title);
////						g3d.setTitle("Plot 3D Graph");
////						graph.setPlotData(new double[][] {{1, 2, 3,2}, {3, 2, 1,2}, {4, 5, 3,2},{8,7,5,2},{6,3,1,2}});
//						map.setPlotData(oneSlice,true);	
//
//						map.maskManipulator().setMaximum(oneSlice[0].length);
//						map.maskManipulator().setMaximumVal(oneSlice.length);
////						map.getData();
//					}
//				}
//			});
//		}
//	}
//	
//	private void drawPattern(PlotData1D patternData){
//
//
////		for one D display			
//		pattern = new OneDVis(composite, SWT.NONE);	
//		pattern.setTitle(patternData.name);
//		pattern.setLabels("2\u03B8", "N_n/pixel (1.22)");   // \u03B8 is theta unicode
//		pattern.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//		pattern.clear();
//		pattern.setPlotData(patternData);
////		OneDVisFit odf = new OneDVisFit(onedgraph);
////		onedgraph.addFilter(odf);		
//
//	}
//	
//	private void drawMap(double[][] mapData, String name, Shell shell){
////		mapData = createMapData();
////		Text tx = new Text(composite, SWT.BORDER);
////		tx.setText("no");
////		tx.setSize(250, 60);
////		FontData fontData = new FontData();
////		fontData.setHeight(32);
////		tx.setFont(new Font(composite.getDisplay(), fontData));
//		Composite composite = new Composite(shell, SWT.NONE);
//
//		map = new TwoDVis(composite, SWT.NONE);
//		map.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));	
//		map.setTitle(name);
//		map.setAxes("xTube", "yChannel", "Number of Neutrons");
//		map.canScaleAxes();
////		g3d.setTitle("Plot 3D Graph");
////		graph.setPlotData(new double[][] {{1, 2, 3,2}, {3, 2, 1,2}, {4, 5, 3,2},{8,7,5,2},{6,3,1,2}});
////		map.setSize(mapData.length, mapData[0].length);		
//		map.setPlotData(mapData,true);	
//
//		map.maskUI();
//		map.maskManipulator();
////		parent.setSize(ycount, nTubes);
//
//		map.maskManipulator().setMaximum(mapData[0].length);
//		map.maskManipulator().setMaximumVal(mapData.length);
//		map.getData();
//		map.readouts();
//
//////		graph.setSize(nycount, nycount);
//////		graph.addMouseListener( m);
//		map.setRedraw(true);
//		map.setCapture(true);	
////		this.mapData = mapData;
////		map.setLocation(x, y)
//	}
//	
//	double[][][] get3DDouble(Array array){
//		int[] shape = array.getShape();
//		double[][][] data = new double[shape[0]][shape[1]][shape[2]];
////		Index3D= index = new Index3D(shape);
//		Index index = Factory.createIndex(shape);
//		for (int i = 0; i < shape[0]; i ++){
//			index.set0(i);
//			for (int j = 0; j < shape[1]; j++){
//				index.set1(j);
//				for (int k = 0; k < shape[2]; k++){
//					index.set2(k);
//					data[i][j][k] = array.getDouble(index);
////					System.out.println(index.toString() + " " + i + " " + j + " " + k + " " + data[i][j][k]);
//				}
//			}
//		}
//		return data;
//	}
//
//	double[][] get2DDouble(Array array){
//		int[] shape = array.getShape();
//		double[][] data = new double[shape[0]][shape[1]];
////		Index index = new Index2D(shape);
//		Index index = Factory.createIndex(shape);
//		for (int i = 0; i < shape[0]; i ++){
//			index.set0(i);
//			for (int j = 0; j < shape[1]; j++){
//				index.set1(j);
//				data[i][j] = array.getDouble(index);
//			}
//		}
//		return data;
//	}
//
//	double[] get1DDouble(Array array){
//		int[] shape = array.getShape();
//		double[] data = new double[shape[0]];
////		Index index = new Index1D(shape);
//		Index index = Factory.createIndex(shape);
//		for (int i = 0; i < shape[0]; i ++){
//			index.set0(i);
//			data[i] = array.getDouble(index);
//		}
//		return data;
//	}	
//
//	long[] get1DLong(Array array){
//		int[] shape = array.getShape();
//		long[] data = new long[shape[0]];
////		Index index = new Index1D(shape);
//		Index index = Factory.createIndex(shape);
//		for (int i = 0; i < shape[0]; i ++){
//			index.set(i);
//			data[i] = array.getLong(index);
//		}
//		return data;
//	}	
//
//	Double getDouble(Array array){
////		return array.getDouble(new Index0D(new int[]{0}));
//		return array.getDouble(Factory.createIndex(new int[]{0}));
//	}

	public String toString(Object obj){
		return obj.toString();
	}
	
	public AlgorithmManager getAlgorithmManager(){
		return algorithmManager;
	}
}
