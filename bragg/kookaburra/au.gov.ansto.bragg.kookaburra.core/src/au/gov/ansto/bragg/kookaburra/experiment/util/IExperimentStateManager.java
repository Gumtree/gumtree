package au.gov.ansto.bragg.kookaburra.experiment.util;

import au.gov.ansto.bragg.kookaburra.experiment.model.Acquisition;
import au.gov.ansto.bragg.kookaburra.experiment.model.AcquisitionSetting;

public interface IExperimentStateManager {

	public void registerSetting(int runId, AcquisitionSetting setting);
	
	public AcquisitionSetting getSetting(int runId);
	
	public void registerAcquisition(int runId, Acquisition acquisition);
	
	public Acquisition getAcquisition(int runId);
	
	public boolean isFixedSamplePosition();
	
	public boolean checkTransmissionRunnable(int runId);
	
	public boolean checkScatteringRunnable(int runId);
	
	public void setRunningTransmission(int runId, boolean running);
	
	public void setRunningScattering(int runId, boolean running);
	
	public void setTransmissionDetails(int runId, String dataFile, float wavelength, float att, float l1, float l2);
	
	public void setScatteringDetails(int runId, String dataFile, float wavelength, float att, float l1, float l2);
	
	public long getScatteringPreset(int runId);
	
}
