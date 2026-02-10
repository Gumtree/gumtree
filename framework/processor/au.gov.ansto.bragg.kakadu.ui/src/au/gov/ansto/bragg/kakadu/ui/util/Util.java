/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.ui.util;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.vis.gdm.dataset.ArraySeries;
import org.gumtree.vis.gdm.dataset.Hist2DDataset;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.datastructures.core.exception.StructureTypeException;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.nexus.NexusUtils;
import au.gov.ansto.bragg.kakadu.core.AlgorithmTask;
import au.gov.ansto.bragg.kakadu.ui.Activator;
import au.gov.ansto.bragg.kakadu.ui.ProjectManager;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotDataReference;

/**
 * The class contains collection of common utility methods.
 * 
 * @author dak
 */
public final class Util extends au.gov.ansto.bragg.kakadu.core.Util {
	

	private static File currentDir = new File (".");
	private static File defaultDir = currentDir;
	private static File currentSelectDir = currentDir;
	private static File currentSavingDir = currentDir;
	private static File currentDirectory = currentDir;

	private Util() {
	}

	public static String[] selectFilesFromShell(Shell shell, String extensionPattern, String fileDescription){
		String fileFilterPath = null;
		if (currentSelectDir == defaultDir && currentSelectDir != currentDir)
			currentSelectDir = currentDir;
		try{
			fileFilterPath = currentSelectDir.getCanonicalPath();// + "\\..";
		}    catch(Exception e) {
			e.printStackTrace();
		}
		FileDialog fileDialog = new FileDialog(shell, SWT.MULTI);
		fileDialog.setFilterPath(fileFilterPath);
//		FileDialog fileDialog = new FileDialog(shell, SWT.MULTI);
		
		fileDialog.setText("Select '" + fileDescription + "'");

//		String fileFilterPath = null;
//		try {
//			fileFilterPath = currentDir.getCanonicalPath();// + "\\..";
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

//		fileDialog.setFilterPath(fileFilterPath);

		//define fiilter
		fileDialog.setFilterExtensions(new String[]{extensionPattern, "*.*"});
		fileDialog.setFilterNames(new String[]{ fileDescription, "All files"});

		fileDialog.open();
		String[] fileNames = fileDialog.getFileNames();
		for (int i = 0; i < fileNames.length; i++) {
			fileNames[i] = fileDialog.getFilterPath() + File.separator + fileNames[i];
		}
		if (fileNames.length > 0) {
			currentSelectDir = (new File(fileNames[0])).getParentFile();
			currentDir = currentSelectDir;
		}
		return fileNames;
	}

	public static String selectDirectoryFromShell(Shell shell){
		if (currentDirectory == defaultDir && currentDirectory != currentDir)
			currentDirectory = currentDir;
		DirectoryDialog fileDialog = new DirectoryDialog(shell, SWT.MULTI);
		
		fileDialog.setText("Directory selection");
		fileDialog.setMessage("Select a directory with data files");
		String fileFilterPath = null;
		try{
			fileFilterPath = currentDirectory.getCanonicalPath();// + "\\..";
		}    catch(Exception e) {
			e.printStackTrace();
		}
		fileDialog.setFilterPath(fileFilterPath);

		String pathName = fileDialog.open();
//		String pathName = fileDialog.getFilterPath();
		if (pathName == null)
			return null;
		else {
			currentDirectory = new File(pathName);
			currentDir = currentDirectory;
		}
		return currentDirectory.getAbsolutePath();
	}
	
	public static List<String> getSupportedFileExtentions() {
		ArrayList<String> result = new ArrayList<String>();
		result.add("hdf");
		return result;
	}



	public static double[][] lineUp(double[][][] signal){
		double[][] matrixSignal = new double[signal[0].length][signal.length*signal[0][0].length];
		for (int i = 0; i < signal[0].length; i ++){
			for (int j = 0; j < signal.length; j++){
				for (int k = 0; k< signal[0][0].length; k++)
					matrixSignal[i][j*signal[0][0].length+k] = signal[j][i][k];
			}
		}
		return matrixSignal;
	}

	public static void handleException(Shell shell, Exception e) {
//		e.printStackTrace();
		String errorMessage = "";
		DisplayManager.getDefault().setEnable(true);
		if (e != null && e.getMessage() != null && e.getMessage().contains("ThreadDeath"))
			errorMessage = "User interrupt";
		else{ 
			errorMessage = e.getLocalizedMessage();
			if (errorMessage != null && errorMessage.contains(":"))
				try{
					errorMessage = errorMessage.substring(errorMessage.lastIndexOf(":") + 1);
				}catch (Exception e2) {
				}
		}
		MessageDialog.openError(shell, 
				"Error", 
				"An error ocuured during the operation. " +
				"See detailes for more information. \n\n Details: " + errorMessage + "");
		LoggerFactory.getLogger(Activator.PLUGIN_ID).error(errorMessage, e);
	}

	
	public static String getFilenameFromShell(Shell shell, String extensionName, String fileDescription){
		String fileFilterPath = null;
		if (currentSelectDir == defaultDir && currentSelectDir != currentDir)
			currentSelectDir = currentDir;
		try{
			fileFilterPath = currentSelectDir.getCanonicalPath();// + "\\..";
		}    catch(Exception e) {
			e.printStackTrace();
		}
		FileDialog fileDialog = new FileDialog(shell, SWT.MULTI);
		fileDialog.setFilterPath(fileFilterPath);
		fileDialog.setFilterExtensions(new String[]{extensionName, "*.*"});
		fileDialog.setFilterNames(new String[]{ fileDescription, "Any"});
	
		String firstFile = fileDialog.open();
		String filename = null;
		if(firstFile != null) {
			final File file = new File(firstFile);
			if (file.exists()) {
				currentSelectDir = file.getParentFile();
				currentDir = currentSelectDir;
				filename = fileDialog.getFilterPath() + File.separator + fileDialog.getFileName();
			}
		}
		return filename;
	}

	public static String getFilenameFromShellNoCheck(Shell shell, String[] extensionName, String[] fileDescription){
		String fileFilterPath = null;
		if (currentSelectDir == defaultDir && currentSelectDir != currentDir)
			currentSelectDir = currentDir;
		try{
			fileFilterPath = currentSelectDir.getCanonicalPath();// + "\\..";
		}    catch(Exception e) {
			e.printStackTrace();
		}
		FileDialog fileDialog = new FileDialog(shell, SWT.MULTI);
		fileDialog.setFilterPath(fileFilterPath);
		fileDialog.setFilterExtensions(extensionName);
		fileDialog.setFilterNames(fileDescription);
	
		String firstFile = fileDialog.open();
		String filename = null;
		if(firstFile != null) {
			final File file = new File(firstFile);
			currentSelectDir = file.getParentFile();
			currentDir = currentSelectDir;
			filename = fileDialog.getFilterPath() + File.separator + fileDialog.getFileName();
		}
		return filename;
	}
	
	public static String getFilenameFromShell(Shell shell, String extensionName, 
			String fileDescription, File dir){
		String fileFilterPath = null;
		try{
			fileFilterPath = dir.getCanonicalPath();// + "\\..";
		}    catch(Exception e) {
			e.printStackTrace();
		}
		FileDialog fileDialog = new FileDialog(shell, SWT.MULTI);
		fileDialog.setFilterPath(fileFilterPath);
		fileDialog.setFilterExtensions(new String[]{extensionName, "*.*"});
		fileDialog.setFilterNames(new String[]{ fileDescription, "Any"});
	
		String firstFile = fileDialog.open();
		String filename = null;
		if(firstFile != null) {
			final File file = new File(firstFile);
			if (file.exists()) {
				currentSelectDir = file.getParentFile();
				currentDir = currentSelectDir;
				filename = fileDialog.getFilterPath() + File.separator + fileDialog.getFileName();
			}
		}
		return filename;
	}

	public static String saveFilenameFromShell(Shell shell, String extensionName, String fileDescription){
		return saveFilenameFromShell(shell, null, extensionName, fileDescription);
	}
	
	public static String saveFilenameFromShell(Shell shell, String initName, String extensionName, String fileDescription){
//		File currentDir = new File (".");
		String fileFilterPath = null;
		if (currentSavingDir == defaultDir && currentSavingDir != currentDir)
			currentSavingDir = currentDir;
		try{
			fileFilterPath = currentSavingDir.getCanonicalPath();// + "\\..";
		}    catch(Exception e) {
			e.printStackTrace();
		}
		FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
		fileDialog.setFilterPath(fileFilterPath);
		fileDialog.setFilterExtensions(new String[]{extensionName, "*.*"});
		fileDialog.setFilterNames(new String[]{ fileDescription, "Any"});
		fileDialog.setFileName(initName);
	
		while(true){
			String firstFileName = fileDialog.open();

			if (firstFileName != null) {
				final File file = new File(firstFileName);
				if (!file.exists() 
						|| MessageDialog.openConfirm(shell, "File exists", "File '" + 
								firstFileName + "' already exists, override?")) {
					currentSavingDir = file.getParentFile();
					currentDir = currentSavingDir;
					return firstFileName;
				}
			} else {
				return null;
			}
		}
	}
	
	public static String saveFilenameFromShell(Shell shell, String extensionName, String fileDescription,
			File dir){
//		File currentDir = new File (".");
		String fileFilterPath = null;
		try{
			fileFilterPath = dir.getCanonicalPath();// + "\\..";
		}    catch(Exception e) {
			e.printStackTrace();
		}
		FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
		fileDialog.setFilterPath(fileFilterPath);
		fileDialog.setFilterExtensions(new String[]{"*."+extensionName, "*.*"});
		fileDialog.setFilterNames(new String[]{ fileDescription, "Any"});
	
		while(true){
			String firstFileName = fileDialog.open();

			if (firstFileName != null) {
				final File file = new File(firstFileName);
				if (!file.exists() 
						|| MessageDialog.openConfirm(shell, "File exists", "File '" + 
								firstFileName + "' already exists, override?")) {
					currentSavingDir = file.getParentFile();
					currentDir = currentSavingDir;
					return firstFileName;
				}
			} else {
				return null;
			}
		}
	}
	
	/**
	 * Gets file name of specified type  
	 * @param shell shell reference
	 * @param extensionNames array of extensions to be used for filtering. 
	 * Example: new String[]{"*.bmp", "*.gif", "*.jpg", "*.png", "*.tif"} 
	 * @param extensionDescriptions array of extension descriptions to be used for filtering. 
	 * Example: new String[]{"Windows Bitmap (*.bmp)", 
							"Graphics Interchange Format (*.gif)", "JPEG File Interchange Format (*.jpg)", 
							"Portable Network Graphics (*.png)", "Tag Image File Format (*.tif)"}
	 * @return full canonical path to selected file name of null if operation was canceled.
	 */
	public static String getSaveFilenameFromShell(Shell shell, String[] extensionNames, String[] extensionDescriptions){
		String fileFilterPath = null;
		if (currentSavingDir == defaultDir && currentSavingDir != currentDir)
			currentSavingDir = currentDir;
		try{
			fileFilterPath = currentSavingDir.getCanonicalPath();
		}    catch(Exception e) {
			e.printStackTrace();
		}
		FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
		fileDialog.setFilterPath(fileFilterPath);
		fileDialog.setFilterExtensions(extensionNames);
		fileDialog.setFilterNames(extensionDescriptions);
	
		while(true){
			String firstFileName = fileDialog.open();

			if (firstFileName != null) {
				final File file = new File(firstFileName);
				if (!file.exists() 
						|| MessageDialog.openConfirm(shell, "File exists", "File '" + 
								firstFileName + "' already exists, override?")) {
					currentSavingDir = file.getParentFile();
					currentDir = currentSavingDir;
					return firstFileName;
				}
			} else {
				return null;
			}
		}
	}
	
	/**
	 * Converts the double value to string presentation.
	 * @param number a number value to be converted.
	 * @param decimalDigits a number of digits after decimal point.
	 * @return string presentation of the number.
	 */
	public static String formatDouble(double number, int decimalDigits) {
		String pattern = "#0.";
		for (int i = 0; i < decimalDigits; i++) {
			pattern += "#";
		}
		final DecimalFormat formatter = new DecimalFormat(pattern);
		return formatter.format(number);
	}
	
	
    /**
     * Creates and returns the color image data for the given control
     * and RGB value. The image's size is either the control's item extent 
     * or the cell editor's default extent, which is 16 pixels square.
     *
     * @param w the control
     * @param color the color
     */
    public static ImageData createColorImage(Control w, RGB color) {

        GC gc = new GC(w);
        FontMetrics fm = gc.getFontMetrics();
        int size = fm.getAscent();
        gc.dispose();

        int indent = 6;
        int extent = 16;
        if (w instanceof Table) {
			extent = ((Table) w).getItemHeight() - 1;
		} else if (w instanceof Tree) {
			extent = ((Tree) w).getItemHeight() - 1;
		} else if (w instanceof org.eclipse.swt.widgets.Tree) {
			extent = ((org.eclipse.swt.widgets.Tree) w).getItemHeight() - 1;
		}

        if (size > extent) {
			size = extent;
		}

        int width = indent + size;
        int height = extent;

        int xoffset = indent;
        int yoffset = (height - size) / 2;

        RGB black = new RGB(0, 0, 0);
        PaletteData dataPalette = new PaletteData(new RGB[] { black, black,
                color });
        ImageData data = new ImageData(width, height, 4, dataPalette);
        data.transparentPixel = 0;

        int end = size - 1;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (x == 0 || y == 0 || x == end || y == end) {
					data.setPixel(x + xoffset, y + yoffset, 1);
				} else {
					data.setPixel(x + xoffset, y + yoffset, 2);
				}
            }
        }

        return data;
    }

	/**
	 * Generate html based styled text to present legend information.
	 * @return styled text to be inserted to StyledText widget.
	 * @see #setStyledTextToWidget(StyledText, String)
	 */
	public static String generateLegendText(PlotDataReference plotDataReference) {
		final int algorithmTaskId = plotDataReference.getTaskId();
		final AlgorithmTask algorithmTask = ProjectManager.getAlgorithmTask(algorithmTaskId);
		if (algorithmTask == null) {
			return "T" + plotDataReference.getTaskId() +
				".O" + plotDataReference.getOperationIndex() +
				".D" + plotDataReference.getDataItemIndex();
		}
		final int dataItemIndex = plotDataReference.getDataItemIndex();
		
		final String algorithmName = algorithmTask.getAlgorithm().getName();
		final String operationUILabel = algorithmTask.getOperationManager(dataItemIndex).getOperation(plotDataReference.getOperationIndex()).getUILabel();
		final String dataItemName = algorithmTask.getDataItems().get(dataItemIndex).getName();
		
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<b>Algorithm Task ID:</b> T");
		stringBuilder.append(algorithmTaskId);
		stringBuilder.append("\n");
		
		stringBuilder.append("<b>Algorithm Name:</b> ");
		stringBuilder.append(algorithmName);
		stringBuilder.append("\n");
		
		stringBuilder.append("<b>Operation:</b> ");
		stringBuilder.append(operationUILabel);
		stringBuilder.append("\n");
		
		stringBuilder.append("<b>Data Item:</b> D");
		stringBuilder.append(dataItemIndex);
		stringBuilder.append(" ");
		stringBuilder.append(dataItemName);
		return stringBuilder.toString();
	}

	/**
	 * Decode b-tags marked text to clear text 
	 * and generate appropriate StyleRange objects for StyledText widget.
	 * @param htmlText b-tags marked text to make it bold 
	 */
	public static void setStyledTextToWidget(StyledText styledTextWidget, String htmlText) {
		List<StyleRange> styleRangeList = new ArrayList<StyleRange>();
		String text = "";
		for(int counter = 0; counter < htmlText.length();) {
			int startIndex = htmlText.indexOf("<b>", counter);
			if (startIndex >= 0) {
				final String readyText = htmlText.substring(counter, startIndex);
				text += readyText; //
				
				startIndex += 3;//shift for <b> tag
				
				int endIndex = htmlText.indexOf("</b>", startIndex);
				final String boldText = htmlText.substring(startIndex, endIndex);
				endIndex += 4;//shift for </b> tag
				
				//create StyleRange
				final int boldStartIndex = text.length();
				text += boldText;
				styleRangeList.add(new StyleRange(boldStartIndex, boldText.length(), null, null, SWT.BOLD));
				
				counter = endIndex;
			} else {
				text += htmlText.substring(counter);
				counter = htmlText.length();
			}
			
		}
		styledTextWidget.setText(text);
		styledTextWidget.setStyleRanges(styleRangeList.toArray(new StyleRange[styleRangeList.size()]));
	}

	public static void setCurrentDir(File dir){
		currentDir = dir;
	}
	
	public static File getCurrentDir(){
		return currentDir;
	}
	
	public static void setCurrentSelectDir(File dir){
		currentSelectDir = dir;
	}
	
	public static File getCurrentSelectDir(){
		return currentSelectDir;
	}
	
	public static void setCurrentSaveDir(File dirFile) {
		currentSavingDir = dirFile;
	}
	
	public static File getCurrentSaveDir() {
		return currentSavingDir;
	}
	
	public static void setCurrentDirectory(File directory) {
		currentDirectory = directory;
	}
	
	public static File getCurrentDirectory() {
		return currentDirectory;
	}
	
	public static Hist2DDataset create2DDataset(IGroup group) throws StructureTypeException {
		Hist2DDataset dataset = new Hist2DDataset();
		if (group instanceof Plot) {
			Plot plot = (Plot) group;
			try {
				IArray z = plot.findSignalArray();
				while (z.getRank() > 2) {
					z = z.getArrayUtils().slice(0, 0).getArray();
				}
				List<Axis> axes = plot.getAxisList();
				Axis xAxis = axes.get(axes.size() - 1);
				Axis yAxis = axes.get(axes.size() - 2);
				IArray x = xAxis.getData();
				IArray y = yAxis.getData();
				dataset.setData(x, y, z);
				dataset.setTitles(xAxis.getTitle() + " (" + xAxis.getUnitsString() + ")", 
						yAxis.getTitle() + " (" + yAxis.getUnitsString() + ")", 
						plot.getTitle());
			} catch (Exception e) {
				throw new StructureTypeException("can not create 2D dataset: " + e.getMessage());
			}
		} else {
			IDataItem signal = NexusUtils.getNexusSignal(group);
			if (signal != null) {
				try{
					IArray z = signal.getData();
					List<IDataItem> axes = NexusUtils.getNexusAxis(group);
					IDataItem xAxis = axes.get(axes.size() - 1);
					IDataItem yAxis = axes.get(axes.size() - 2);
					IArray x = axes.get(axes.size() - 1).getData();
					IArray y = axes.get(axes.size() - 2).getData();
					dataset.setData(x, y, z);
					dataset.setTitles(xAxis.getShortName() + " (" + xAxis.getUnitsString() + ")", 
							yAxis.getShortName() + " (" + yAxis.getUnitsString() + ")", 
							signal.getShortName());
				} catch (Exception e) {
					throw new StructureTypeException("can not create 2D dataset: " + e.getMessage());
				}
			}
		}
		return dataset;
	}
	
	public static ArraySeries createSeries(IGroup group) throws StructureTypeException {
		String name = group.getShortName();
		ArraySeries series = new ArraySeries(name);
		if (group instanceof Plot) {
			Plot plot = (Plot) group;
			try {
				series.setKey(plot.getTitle());
				IArray y = plot.findSignalArray();
				while (y.getRank() > 1) {
					y = y.getArrayUtils().slice(0, 0).getArray();
				}
				List<Axis> axes = plot.getAxisList();
				IArray x = axes.get(axes.size() - 1).getData();
				
				IArray error = plot.findVarianceArray();
				if (error != null) {
					error = error.getArrayMath().toSqrt().getArray();
				}
				series.setData(x, y, error);
			} catch (Exception e) {
				e.printStackTrace();
				throw new StructureTypeException("can not create 2D dataset: " + e.getMessage());
			}
		} else {
			IDataItem signal = NexusUtils.getNexusSignal(group);
			if (signal != null) {
				try{
					IArray y = signal.getData();
					List<IDataItem> axes = NexusUtils.getNexusAxis(group);
					IArray x = axes.get(axes.size() - 1).getData();
					IDataItem varianceItem = NexusUtils.getNexusVariance(group);
					IArray error = null;
					if (varianceItem != null) {
						error = varianceItem.getData().getArrayMath().toSqrt().getArray();
					} else {
						error = y.getArrayMath().toSqrt().getArray();
					}
					series.setData(x, y, error);
				} catch (Exception e) {
					throw new StructureTypeException("can not create 2D dataset: " + e.getMessage());
				}
			}
		}
		return series;
	}
	
	public static void updateSeries(ArraySeries series, IGroup group) throws StructureTypeException {
//		ArraySeries series = new ArraySeries(name);
		if (group instanceof Plot) {
			Plot plot = (Plot) group;
			series.setKey(plot.getTitle());
			try {
				IArray y = plot.findSignalArray();
				List<Axis> axes = plot.getAxisList();
				IArray x = axes.get(axes.size() - 1).getData();
				IArray error = plot.findVarianceArray();
				if (error != null) {
					error = error.getArrayMath().toSqrt().getArray();
				}
				series.setData(x, y, error);
			} catch (Exception e) {
				throw new StructureTypeException("can not create 1D dataset: " + e.getMessage());
			}
		} else {
			IDataItem signal = NexusUtils.getNexusSignal(group);
			if (signal != null) {
				try{
					series.setKey(group.getShortName());
					IArray y = signal.getData();
					List<IDataItem> axes = NexusUtils.getNexusAxis(group);
					IArray x = axes.get(axes.size() - 1).getData();
					IDataItem varianceItem = NexusUtils.getNexusVariance(group);
					IArray error = null;
					if (varianceItem != null) {
						error = varianceItem.getData().getArrayMath().toSqrt().getArray();
					} else {
						error = y.getArrayMath().toSqrt().getArray();
					}
					series.setData(x, y, error);
				} catch (Exception e) {
					throw new StructureTypeException("can not create 2D dataset: " + e.getMessage());
				}
			}
		}
	}
}
