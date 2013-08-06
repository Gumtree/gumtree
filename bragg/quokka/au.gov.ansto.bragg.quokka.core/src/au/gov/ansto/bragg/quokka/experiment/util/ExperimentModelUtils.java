/*****************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *****************************************************************************/

package au.gov.ansto.bragg.quokka.experiment.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.eclipse.core.runtime.Platform;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.core.SicsCore;

import au.gov.ansto.bragg.quokka.core.internal.QuokkaCoreProperties;
import au.gov.ansto.bragg.quokka.experiment.model.Acquisition;
import au.gov.ansto.bragg.quokka.experiment.model.AcquisitionEntry;
import au.gov.ansto.bragg.quokka.experiment.model.AcquisitionSetting;
import au.gov.ansto.bragg.quokka.experiment.model.ControlledAcquisition;
import au.gov.ansto.bragg.quokka.experiment.model.Experiment;
import au.gov.ansto.bragg.quokka.experiment.model.InstrumentConfig;
import au.gov.ansto.bragg.quokka.experiment.model.Sample;
import au.gov.ansto.bragg.quokka.experiment.model.SampleEnvironment;
import au.gov.ansto.bragg.quokka.experiment.model.ScanMode;

import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.CSVPrinter;
import com.thoughtworks.xstream.XStream;

public final class ExperimentModelUtils {
	
	private static final String DEVICE_BM1_RATE = "bm1_event_rate";
	
	private static volatile XStream xStream;
	
	// TODO: simplify and combine XStream
	
	public static XStream getXStream() {
		if (xStream == null) {
			synchronized (ExperimentModelUtils.class) {
				if (xStream == null) {
					xStream = new XStream();
					xStream.autodetectAnnotations(true);
				}
			}
		}
		return xStream;
	}

	public static void markStartTime(Experiment experiment) {
		experiment.setStartTime(Calendar.getInstance().getTime());
	}
	
	// Clear all file references
	public static void clearResult(Experiment experiment) {
		// Clear data file for normal acquisition section
		for (AcquisitionEntry entry : experiment.getNormalAcquisition().getEntries()) {
			for (AcquisitionSetting setting : entry.getConfigSettings().values()) {
				clearSetting(setting);
			}
		}
		// Clear data file for controlled acquisition sections
		for (ControlledAcquisition acquisition : experiment.getAcquisitionGroups()) {
			for (AcquisitionEntry entry : acquisition.getEntries()) {
				for (AcquisitionSetting setting : entry.getConfigSettings().values()) {
					clearSetting(setting);
				}
			}
		}
		// Clear start time
		experiment.setStartTime(null);
	}
	
	private static void clearSetting(AcquisitionSetting setting) {
		setting.setTransmissionDataFile(null);
		setting.setScatteringDataFile(null);
		setting.setRunningTransmission(false);
		setting.setRunningScattering(false);
	}
	
	public static void exportConsoleLog(File reportFolder, String outputText) throws IOException {
		if (!reportFolder.exists()) {
			reportFolder.mkdir();
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		
		// Write output log
		File consoleOuput = new File(reportFolder, "QKK" + format.format(Calendar.getInstance().getTime()) + "_output.txt");
		if (!consoleOuput.exists()) {
			consoleOuput.createNewFile();
		}
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			outputText = outputText.replace("\n", "\r\n");
		}
		FileWriter writer = new FileWriter(consoleOuput);
		writer.append(outputText);
		writer.flush();
		writer.close();
	}
	
	public static void loadSamplesFromCSV(Experiment experiment, String filename) throws IOException {
		FileReader reader = new FileReader(filename);
		String[][] data = CSVParser.parse(reader);
		int startingLine = 1;	// header
		for (int i = 0; i < data.length; i++) {
			// Don't process any further if there is not enough sample
			if (i >= experiment.getSamples().size()) {
				break;
			}
			Sample sample = experiment.getSamples().get(i);
			int line = i + startingLine;
			// We only process 4 columns
			if (data[line].length > 0) {
				sample.setType(SampleType.valueOf(data[line][0].toUpperCase()));
			}
			if (data[line].length > 1) {
				sample.setName(data[line][1]);
			}
			if (data[line].length > 2) {
				try {
					sample.setThickness(Float.parseFloat(data[line][2]));
				} catch (NumberFormatException e) {
					// We only throw reader IO exception
				}
			}
			if (data[line].length > 3) {
				sample.setDescription(data[line][3]);
			}
		}
	}
	
	public static void saveSamplesToCSV(Experiment experiment, String filename) throws IOException {
		FileWriter writer = new FileWriter(filename);
		CSVPrinter printer = new CSVPrinter(writer);
		// Header
		printer.writeln(new String[] {"Type", "Name", "Thickness", "Description"});
		printer.writeln();
		// Print
		for (Sample sample : experiment.getSamples()) {
			printer.writeln(new String[] {sample.getType().name(), sample.getName(), Float.toString(sample.getThickness()), sample.getDescription()});
		}
		writer.flush();
		writer.close();
	}

	public static void saveExperimentToExcel(Experiment experiment, String filename) throws IOException {
			Workbook workbook = new HSSFWorkbook();
		Sheet sheet = workbook.createSheet("Quokka Multi-Sample Scan");
		int rowCounter = 0;
		if (experiment.isControlledEnvironment()) {
			for (ControlledAcquisition acquisition : experiment.getAcquisitionGroups()) {
				for (SampleEnvironment sampleEnvironment : experiment.getSampleEnvironments()) {
					Row row = sheet.createRow(rowCounter++);
					Cell cell = row.createCell(0);
					cell.setCellValue(sampleEnvironment.getControllerId());
					cell = row.createCell(1);
					cell.setCellValue(acquisition.getEnvSettings().get(sampleEnvironment).getPreset());
				}
				rowCounter = saveAcquisitionToExcel(acquisition, sheet, rowCounter);
			}
		} else {
			rowCounter = saveAcquisitionToExcel(experiment.getNormalAcquisition(), sheet, rowCounter);
		}
		FileOutputStream fileOut = new FileOutputStream(filename);
	    workbook.write(fileOut);
	    fileOut.close();
	}
	
	private static int saveAcquisitionToExcel(Acquisition acquisition, Sheet sheet, int rowCounter) {
		// 1st header
		Row row = sheet.createRow(rowCounter++);
		int colIndex = 5;
		for (InstrumentConfig config : acquisition.getExperiment().getInstrumentConfigs()) {
			Cell cell = row.createCell(colIndex);
			cell = row.createCell(colIndex);
			cell.setCellValue(config.getName());
			colIndex += 5;
		}
				
		// 2nd header
		row = sheet.createRow(rowCounter++);
		Cell cell = row.createCell(0);
		cell.setCellValue("Sequence");
		cell = row.createCell(1);
		cell.setCellValue("Position");
		cell = row.createCell(2);
		cell.setCellValue("Sample Name");
		cell = row.createCell(3);
		cell.setCellValue("Thickness");
		colIndex = 4;
		for (InstrumentConfig config : acquisition.getExperiment().getInstrumentConfigs()) {
			cell = row.createCell(colIndex++);
			cell = row.createCell(colIndex++);
			cell.setCellValue("Transmission");
			cell = row.createCell(colIndex++);
			cell = row.createCell(colIndex++);
			cell.setCellValue("Scattering");
			cell = row.createCell(colIndex++);
			cell.setCellValue("Preset (sec)");
		}
		
		// Content
		int sequence = 1;
		for (AcquisitionEntry entry : acquisition.getEntries()) {
			row = sheet.createRow(rowCounter++);
			cell = row.createCell(0);
			cell.setCellValue(sequence);
			cell = row.createCell(1);
			cell.setCellValue(entry.getSample().getPosition());
			cell = row.createCell(2);
			cell.setCellValue(entry.getSample().getName());
			cell = row.createCell(3);
			cell.setCellValue(entry.getSample().getThickness());
			colIndex = 4;
			for (InstrumentConfig config : acquisition.getExperiment().getInstrumentConfigs()) {
				AcquisitionSetting setting = entry.getConfigSettings().get(config);
				cell = row.createCell(colIndex++);
				if (setting.isRunTransmission()) {
					cell.setCellValue("X");	
				}
				cell = row.createCell(colIndex++);
				cell.setCellValue(setting.getTransmissionDataFile());
				cell = row.createCell(colIndex++);
				if (setting.isRunScattering()) {
					cell.setCellValue("X");	
				}
				cell = row.createCell(colIndex++);
				cell.setCellValue(setting.getScatteringDataFile());
				cell = row.createCell(colIndex++);
				cell.setCellValue(setting.getPreset());
			}
			sequence++;
		}
		return ++rowCounter;
	}

	public static void refineExperimentFromExcel(Experiment experiment,
			String filename) throws IOException {
		FileInputStream input = new FileInputStream(filename);
		refineExperimentFromExcel(experiment, input);
	}
	// [Tony][2012-06-27][GUMTREE-847]
	public static void refineExperimentFromExcel(Experiment experiment,
			InputStream input) throws IOException {
		// Sample environment is not supported at this stage
		if (experiment.isControlledEnvironment()) {
			return;
		}
		// Clear existing acquisition entries
		experiment.getNormalAcquisition().getEntries().clear();
		// Read from a Excel file
		Workbook workbook = new HSSFWorkbook(input);
		Sheet sheet = workbook.getSheetAt(0);
		// Start from row 3
		for (int i = 2; i < sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);
			int samplePosition = Integer.parseInt(row.getCell(1).getStringCellValue());
			Sample sample = experiment.getSamples().get(samplePosition);
			AcquisitionEntry entry = new AcquisitionEntry(sample);
			experiment.getNormalAcquisition().getEntries().add(entry);
		}
	}
	
	public static long calculateEstimatedRunTime(Experiment experiment) {
		long counter = 0;
		if (experiment.isControlledEnvironment()) {
			for (ControlledAcquisition acquistion : experiment.getAcquisitionGroups()) {
				for (AcquisitionEntry entry : acquistion.getEntries()) {
					counter += calculateEstimatedRunTime(entry);
				}
			}
		} else {
			for (AcquisitionEntry entry : experiment.getNormalAcquisition().getEntries()) {
				counter += calculateEstimatedRunTime(entry);
			}
		}
		return counter;
	}
	
	// NOTE: this only estimate the data collection time
	// TODO: add estimated time for
	// * Configuration changing time
	// * Voltage controller time
	// * Sample environment drive time
	private static long calculateEstimatedRunTime(AcquisitionEntry entry) {
		long counter = 0;
		for (Entry<InstrumentConfig, AcquisitionSetting> settingEntry : entry.getConfigSettings().entrySet()) {
			InstrumentConfig config = settingEntry.getKey();
			AcquisitionSetting setting = settingEntry.getValue();
			// Add transmission time
			if (setting.isRunTransmission()) {
				if (config.getTransmissionMode() == ScanMode.TIME) {
					// time mode
					counter += config.getTransmissionPreset();
				} else if (config.getTransmissionMode() == ScanMode.COUNTS) {
					// count mode
					counter += 0; // unknown
				} else if (config.getTransmissionMode() == ScanMode.BM1) {
					// MONITOR_1 mode
					counter += config.getTransmissionPreset() / getBeamMonitorRate();
				}
			}
			// Add scattering time
			if (setting.isRunScattering()) {
				if (config.getMode() == ScanMode.TIME) {
					// time mode
					counter += setting.getPreset();
				} else if (config.getMode() == ScanMode.COUNTS) {
					// count mode
					counter += 0; // unknown
				} else if (config.getMode() == ScanMode.BM1) {
					// MONITOR_1 mode
					counter += setting.getPreset() / getBeamMonitorRate();
				}
			}
		}
		return counter;
	}
	
	public static long calculateEstimatedConfigTime(Experiment experiment) {
		// Average of 20 min for config change (5 min voltage ramp x 2 + 15 min 1/2 tank detector move)
		long counter = experiment.getInstrumentConfigs().size() * 25 * 60;
		
		// Beamstop move time (20 sec up or down for config)
		counter += experiment.getInstrumentConfigs().size() * 20 * 2;
		
		if (experiment.isControlledEnvironment()) {
			for (ControlledAcquisition acquistion : experiment.getAcquisitionGroups()) {
				for (AcquisitionEntry entry : acquistion.getEntries()) {
					counter += calculateEstimatedConfigTime(entry);
				}
			}
		} else {
			for (AcquisitionEntry entry : experiment.getNormalAcquisition().getEntries()) {
				counter += calculateEstimatedConfigTime(entry);
			}
		}
		return counter;
	}
	
	private static long calculateEstimatedConfigTime(AcquisitionEntry entry) {
		long counter = 0;
		for (Entry<InstrumentConfig, AcquisitionSetting> settingEntry : entry.getConfigSettings().entrySet()) {
			InstrumentConfig config = settingEntry.getKey();
			AcquisitionSetting setting = settingEntry.getValue();
			// Add scattering time
			if (setting.isRunScattering()) {
				// [GUMTREE-800]
				// Attenuation is in the step of 30 deg, and we assume each step takes 35 sec
				counter = (config.getStartingAttenuation() / 30) * 35;
			}
		}
		return counter;
	}
	
	// [GUMTREE-406] Use real beam monitor reading for estimation
	private static Long getBeamMonitorRate() {
		try {
			IDynamicController rateController = (IDynamicController) SicsCore
					.getSicsController().findDeviceController(DEVICE_BM1_RATE);
			float rate = rateController.getValue().getFloatData();
			long result = Math.round((double) rate);
			// Avoid divide by zero
			if (result <= 0) {
				return QuokkaCoreProperties.EXPECTED_MONITOR_RATE.getLong();
			}
			return result;
		} catch (Exception e) {
			// Use default
			return QuokkaCoreProperties.EXPECTED_MONITOR_RATE.getLong();
		}
	}
	
//	private long calculateEstimatedRunTime(Experiment experiment) {
//		long totalTimeInSec = 0;
//		for (InstrumentConfig config : experiment.getInstrumentConfigs()) {
//			// Transmission
//			int totalTransmissionTime = 0;
//			long normalisationFactor = 1;
//			if (config.getTransmissionMode().equalsIgnoreCase("Monitor")) {
//				normalisationFactor = getExpectedMonitorRate();
//			}
//			for (AcquisitionSetting setting : config.getSettings()) {
//				if (setting.isDoTransmission()) {
//					totalTransmissionTime += config.getTransmissionPreset();
//				}
//			}
//			totalTransmissionTime /= normalisationFactor;
//			totalTimeInSec += totalTransmissionTime;
//			// Scattering
//			int totalScatteringTime = 0;
//			normalisationFactor = 1;
//			if (config.getTransmissionMode().equalsIgnoreCase("Monitor")) {
//				normalisationFactor = getExpectedMonitorRate();
//			}
//			for (AcquisitionSetting setting : config.getSettings()) {
//				if (setting.isDoScatteringRun()) {
//					totalScatteringTime += setting.getValue();
//				}
//			}
//			totalScatteringTime /= normalisationFactor;
//			totalTimeInSec += totalScatteringTime;
//		}
//		return totalTimeInSec;
//	}
	
	private ExperimentModelUtils() {
		super();
	}
	
}
