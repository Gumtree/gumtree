package au.gov.ansto.bragg.quokka.experiment.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.service.db.LoggingDB;
import org.gumtree.service.db.RecordsFileException;
import org.gumtree.service.directory.IDirectoryService;
import org.gumtree.util.messaging.IListenerManager;
import org.gumtree.util.messaging.ListenerManager;
import org.gumtree.util.messaging.SafeListenerRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.quokka.core.internal.QuokkaCoreProperties;
import au.gov.ansto.bragg.quokka.experiment.model.Acquisition;
import au.gov.ansto.bragg.quokka.experiment.model.AcquisitionSetting;
import au.gov.ansto.bragg.quokka.experiment.model.Experiment;
import au.gov.ansto.bragg.quokka.experiment.model.InstrumentConfig;
import au.gov.ansto.bragg.quokka.experiment.model.Sample;
import au.gov.ansto.bragg.quokka.experiment.report.ExperimentUserReport;
import au.gov.ansto.bragg.quokka.experiment.report.ExperimentUserReportUtils;

public class ExperimentStateManager implements IExperimentStateManager {
	
	private static final Logger logger = LoggerFactory.getLogger(ExperimentStateManager.class);
	
	private Experiment experiment;
	
	private Map<Integer, AcquisitionSetting> settings;
	
	private Map<Integer, Acquisition> acquisitions;
	
	private Map<Integer, Sample> samples; 
	
	private IListenerManager<IExperimentStateListener> listenerManager;
	
	public ExperimentStateManager(Experiment experiment) {
		this.experiment = experiment;
		settings = new HashMap<Integer, AcquisitionSetting>();
		acquisitions = new HashMap<Integer, Acquisition>();
		samples = new HashMap<Integer, Sample>();
		listenerManager = new ListenerManager<IExperimentStateListener>();
	}
	
	public void registerSetting(int runId, AcquisitionSetting setting) {
		settings.put(runId, setting);
	}
	
	public AcquisitionSetting getSetting(int runId) {
		return settings.get(runId);
	}
	
	public void registerAcquisition(int runId, Acquisition acquisition) {
		acquisitions.put(runId, acquisition);
	}
	
	public Acquisition getAcquisition(int runId) {
		return acquisitions.get(runId);
	}
	
	public void registerSample(int runId, Sample sample) {
		samples.put(runId, sample);
	}
	
	public Sample getSample(int runId) {
		return samples.get(runId);
	}
	
	public boolean isFixedSamplePosition() {
		return experiment.isFixedSamplePosition();
	}
	
	public boolean checkTransmissionRunnable(int runId) {
		AcquisitionSetting setting = settings.get(runId);
		if (setting == null) {
			throw new IllegalArgumentException("runId " + runId + " is too large!");
		}
		return setting.isRunTransmission();
	}
	
	public boolean checkScatteringRunnable(int runId) {
		AcquisitionSetting setting = settings.get(runId);
		if (setting == null) {
			throw new IllegalArgumentException("runId " + runId + " is too large!");
		}
		return setting.isRunScattering();
	}
	
	public long getScatteringPreset(int runId) {
		AcquisitionSetting setting = settings.get(runId);
		if (setting == null) {
			throw new IllegalArgumentException("runId " + runId + " is too large!");
		}
		return setting.getPreset();
	}

	public void setRunningTransmission(final int runId, boolean running) {
		AcquisitionSetting setting = settings.get(runId);
		if (setting == null) {
			throw new IllegalArgumentException("runId " + runId + " is too large!");
		}
		setting.setRunningTransmission(running);
		fireUpdate(runId);
	}
	
	public void setRunningScattering(final int runId, boolean running) {
		AcquisitionSetting setting = settings.get(runId);
		if (setting == null) {
			throw new IllegalArgumentException("runId " + runId + " is too large!");
		}
		setting.setRunningScattering(running);
		fireUpdate(runId);
	}
	
	public void setTransmissionDetails(final int runId, String dataFile,
			float wavelength, float att, float l1, float l2) {
		AcquisitionSetting setting = settings.get(runId);
		if (setting == null) {
			throw new IllegalArgumentException("runId " + runId + " is too large!");
		}
		
		// Update file association on config
		Sample sample = samples.get(runId);
		if (sample.getType().equals(SampleType.EMPTY_BEAM)) {
			setting.getConfig().setEmptyBeamTransmissionDataFile(dataFile);	
		} else if (sample.getType().equals(SampleType.EMPTY_CELL)) {
			setting.getConfig().setEmptyCellTransmissionDataFile(dataFile);
		}
		
		setting.setTransmissionDataFile(dataFile);
		setting.setTransmissionWavelength(wavelength);
		setting.setTransmissionAttenuation(att);
		setting.setTransmissionL1(l1);
		setting.setTransmissionL2(l2);
		fireUpdate(runId);
	}
	
	public void setScatteringDetails(final int runId, String dataFile,
			float wavelength, float att, float l1, float l2) {
		AcquisitionSetting setting = settings.get(runId);
		if (setting == null) {
			throw new IllegalArgumentException("runId " + runId + " is too large!");
		}
		
		// Update file association on config
		Sample sample = samples.get(runId);
		if (sample.getType().equals(SampleType.EMPTY_CELL)) {
			setting.getConfig().setEmptyCellScatteringDataFile(dataFile);	
		}
		
		setting.setScatteringDataFile(dataFile);
		setting.setScatteringWavelength(wavelength);
		setting.setScatteringAttenuation(att);
		setting.setScatteringL1(l1);
		setting.setScatteringL2(l2);
		fireUpdate(runId);
	}
	
	public void setConfigSetFinished(final int runId){
		AcquisitionSetting setting = settings.get(runId);
		InstrumentConfig config = setting.getConfig();
		try {
			LoggingDB.getInstance().appendTableEntry("MSW result", ExperimentUserReportUtils.exportAcquisitionTable(getAcquisition(runId), config));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RecordsFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	public void setAcquistionStarted(){
		try {
			LoggingDB.getInstance().appendTableEntry("MSW result", ExperimentUserReportUtils.createExperimentInfoTable(experiment));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RecordsFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void fireUpdate(final int runId) {
		logger.info("Generating intermediate report for run " + runId + ".");
		// Update result object
		// [2009-11-12] To be removed later
//		ExperimentResult result = ExperimentResultUtils.createExperimentResult(experiment);
//		GTPlatform.getDirectoryService().bind(ExperimentResult.class.getName(), result);
		// Update
		ExperimentUserReport report = ExperimentUserReportUtils.createExperimentUserReport(experiment);
		ServiceUtils.getService(IDirectoryService.class).bind(ExperimentUserReport.class.getName(), report);
		// Notify
		listenerManager.asyncInvokeListeners(new SafeListenerRunnable<IExperimentStateListener>() {
			@Override
			public void run(IExperimentStateListener listener) throws Exception {
				listener.stateUpdated(runId);
			}			
		});
		// [GUMTREE-579]Incremental report persistence
		try {
			URI reportFolderURI = new URI(QuokkaCoreProperties.REPORT_LOCATION.getValue());
			File reportFolder = EFS.getStore(reportFolderURI).toLocalFile(EFS.NONE, new NullProgressMonitor());
			File currentFolder = new File(reportFolder, "current");
			ExperimentUserReportUtils.exportUserReport(currentFolder, report, "QKK_current_report.xml");
			logger.info("Intermediate report saved.");
		} catch (Exception e) {
			logger.error("Failed to generate incremental report.", e);
		}
	}
	
	public void addListener(IExperimentStateListener listener) {
		listenerManager.addListenerObject(listener);
	}
	
	public void removeListener(IExperimentStateListener listener) {
		listenerManager.removeListenerObject(listener);
	}
		
}
