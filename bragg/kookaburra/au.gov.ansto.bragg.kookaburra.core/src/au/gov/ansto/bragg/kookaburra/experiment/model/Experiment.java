package au.gov.ansto.bragg.kookaburra.experiment.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Experiment is the top level model for the Kookaburra experiment workflow.
 * 
 */
public class Experiment extends AbstractModelObject {

	public static final String PROP_SAMPLES = "samples";
	
	public static final String PROP_CONTROLLED_ACQUISITION = "controlledAcquisition";
	
	public static final String PROP_SAMPLE_ENVIRONMENTS = "sampleEnvironments";
	
	public static final String PROP_ACQUISITION_GROUPS = "acquisitionGroups";
	
	public static final String PROP_INSTRUMENT_CONFIGS = "instrumentConfigs";
	
	/*************************************************************************
	 * Pre-configured settings
	 *************************************************************************/
	// Experiment title
	private String title;

	// User details
	private User user;

	// Sample details
	private PropertyList<Sample> samples;

	// Available sample environment controllers
	private PropertyList<String> sampleEnvControllerIds;

	// Quick solution in v1.4.x: provide feature
	// to fix sample in a pre-defined position
	private boolean isFixedSamplePosition;
	
	// Optional sensitivity file name
	private String sensitivityFile;

	// Optional dark current file name
	private String darkCurrentFile;
	
	// Optional directory destination for exporting user report
	private String userReportDirectory;
	
	/*************************************************************************
	 * Dynamic settings (objects created and destroyed during configuration)
	 *************************************************************************/
	// Participated instrument configurations (Q ranges)
	private PropertyList<InstrumentConfig> instrumentConfigs;

	// Selected sample environments for this experiment
	private PropertyList<SampleEnvironment> sampleEnvironments;
	
	private Date startTime;
	
	/*************************************************************************
	 * Acquisition settings
	 *************************************************************************/
	// Flag for controlled sample environment acquisition
	private boolean controlledEnvironment;
	
	// Settings for normal acquisition
	private NormalAcquisition normalAcquisition;
	
	// Settings for controlled acquisition
	private PropertyList<ControlledAcquisition> acquisitionGroups;
	
	/**
	 * Constructor.
	 */
	public Experiment() {
		super();
		// Set up change listener to update acquisition settings
		addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				updateEntries(event);
			}
		});
	}

	/**
	 * Returns experiment title.
	 * 
	 * @return title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the title for this experiment.
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		String oldValue = this.title;
		this.title = title;
		firePropertyChange("title", oldValue, title);
	}

	/**
	 * Returns user details of this experiment model.
	 * 
	 * @return user
	 */
	public User getUser() {
		if (user == null) {
			user = new User();
		}
		return user;
	}

	/**
	 * Sets user details for this experiment model.
	 * 
	 * @param user
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * Returns a list of sample 
	 * @return
	 */
	public PropertyList<Sample> getSamples() {
		if (samples == null) {
			samples = new PropertyList<Sample>(this, PROP_SAMPLES);
		}
		return samples;
	}

	/**
	 * Returns if the experiment uses a fixed sample position.
	 * 
	 * @return
	 */
	public boolean isFixedSamplePosition() {
		return isFixedSamplePosition;
	}

	/**
	 * Sets to use a fixed sample position in the experiment.
	 * 
	 * @param isFixedSamplePosition
	 */
	public void setFixedSamplePosition(boolean isFixedSamplePosition) {
		boolean oldValue = this.isFixedSamplePosition;
		this.isFixedSamplePosition = isFixedSamplePosition;
		firePropertyChange("fixedSamplePosition", oldValue, isFixedSamplePosition);
	}

	/**
	 * Returns a list of available sample environment controller ids.
	 * 
	 * @return
	 */
	public List<String> getSampleEnvControllerIds() {
		if (sampleEnvControllerIds == null) {
			sampleEnvControllerIds = new PropertyList<String>(this,
					"sampleEnvControllerIds");
		}
		return sampleEnvControllerIds;
	}

	/**
	 * Returns the sensitivity file name associate to this experiment.
	 *  
	 * @return
	 */
	public String getSensitivityFile() {
		return sensitivityFile;
	}

	/**
	 * Sets the sensitivity file.
	 * 
	 * @param sensitivityFile
	 */
	public void setSensitivityFile(String sensitivityFile) {
		String oldValue = this.sensitivityFile;
		this.sensitivityFile = sensitivityFile;
		firePropertyChange("sensitivityFile", oldValue, sensitivityFile);
	}

	/**
	 * Returns the dark current file name associate to this experiment.
	 * 
	 * @return
	 */
	public String getDarkCurrentFile() {
		return darkCurrentFile;
	}

	/**
	 * Sets the dark current file.
	 * 
	 * @param darkCurrentFile
	 */
	public void setDarkCurrentFile(String darkCurrentFile) {
		String oldValue = this.darkCurrentFile;
		this.darkCurrentFile = darkCurrentFile;
		firePropertyChange("darkCurrentFile", oldValue, darkCurrentFile);
	}
	
	/**
	 * Returns user report directory for this experiment.
	 * 
	 * @return
	 */
	public String getUserReportDirectory() {
		return userReportDirectory;
	}

	/**
	 * Sets the user report directory.
	 * 
	 * @param userReportDirectory
	 */
	public void setUserReportDirectory(String userReportDirectory) {
		String oldValue = this.userReportDirectory;
		this.userReportDirectory = userReportDirectory;
		firePropertyChange("userReportDirectory", oldValue, userReportDirectory);
	}

	/**
	 * Returns a list of instrument configs (Q ranges) configured for this
	 * experiment.
	 * 
	 * @return
	 */
	public PropertyList<InstrumentConfig> getInstrumentConfigs() {
		if (instrumentConfigs == null) {
			instrumentConfigs = new PropertyList<InstrumentConfig>(this,
					PROP_INSTRUMENT_CONFIGS);
		}
		return instrumentConfigs;
	}

	/**
	 * Returns a list of included sample environment in this experiment.
	 * 
	 * @return
	 */
	public PropertyList<SampleEnvironment> getSampleEnvironments() {
		if (sampleEnvironments == null) {
			sampleEnvironments = new PropertyList<SampleEnvironment>(this,
					PROP_SAMPLE_ENVIRONMENTS);
		}
		return sampleEnvironments;
	}
	
	/**
	 * Returns if this experiment is sample environment controlled.
	 * 
	 * @return
	 */
	public boolean isControlledEnvironment() {
		return controlledEnvironment;
	}

	/**
	 * Sets this experiment to sample environment controlled mode.
	 * 
	 * @param isControlledAcquisition
	 */
	public void setControlledAcquisition(boolean controlledAcquisition) {
		boolean oldValue = this.controlledEnvironment;
		this.controlledEnvironment = controlledAcquisition;
		firePropertyChange(PROP_CONTROLLED_ACQUISITION, oldValue, controlledAcquisition);
	}

	public NormalAcquisition getNormalAcquisition() {
		if (normalAcquisition == null) {
			normalAcquisition = new NormalAcquisition(this);
		}
		return normalAcquisition;
	}

	/**
	 * Returns a list of scan at different sample environment settings.
	 * 
	 * @return
	 */
	public PropertyList<ControlledAcquisition> getAcquisitionGroups() {
		if (acquisitionGroups == null) {
			acquisitionGroups = new PropertyList<ControlledAcquisition>(this,
					PROP_ACQUISITION_GROUPS);
		}
		return acquisitionGroups;
	}
	
	/*************************************************************************
	 * Helper methods
	 *************************************************************************/
	public boolean hasEnvironmentControllers() {
		return getSampleEnvControllerIds().size() > 0;
	}
	
	public ControlledAcquisition findControlledAcquisition(Map<SampleEnvironment, SampleEnvironmentPreset> envSettings) {
		for (ControlledAcquisition acquisition : getAcquisitionGroups()) {
			// Source
			Map<SampleEnvironment, SampleEnvironmentPreset> s1 = envSettings;
			// Target
			Map<SampleEnvironment, SampleEnvironmentPreset> s2 = acquisition.getEnvSettings();
			if (s1.equals(s2)) {
				return acquisition;
			}
		}
		return null;
	}
	
	private void updateEntries(PropertyChangeEvent event) {
		if (event.getPropertyName().equals(PROP_SAMPLES)) {
			final Sample sample = (Sample) event.getNewValue();
			// Add as acquisition entry
			if (sample.isRunnable()) {
				// handle in add-to-the-end way for this kind of notification
				getNormalAcquisition().handleNewSample((Sample) event.getNewValue(), false);
				for (ControlledAcquisition controlledAcquisition : getAcquisitionGroups()) {
					controlledAcquisition.handleNewSample((Sample) event.getNewValue(), false);
				}
			}
			// Listen for further changes
			sample.addPropertyChangeListener("runnable", new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if (sample.isRunnable()) {
						// handle in ordered way for this kind of notification
						// Normal environment
						getNormalAcquisition().handleNewSample(sample, true);
						for (ControlledAcquisition controlledAcquisition : getAcquisitionGroups()) {
							 controlledAcquisition.handleNewSample(sample, true);
						}
					} else {
						// Controlled environment
						getNormalAcquisition().handleRemovedSample(sample);
						for (ControlledAcquisition controlledAcquisition : getAcquisitionGroups()) {
							 controlledAcquisition.handleRemovedSample(sample);
						}
					}
				}
			});
		} else if (event.getPropertyName().equals(PROP_INSTRUMENT_CONFIGS)) {
			if (event.getNewValue() != null) {
				// Update new
				getNormalAcquisition().handleNewConfig((InstrumentConfig) event.getNewValue());
				for (ControlledAcquisition controlledAcquisition : getAcquisitionGroups()) {
					 controlledAcquisition.handleNewConfig((InstrumentConfig) event.getNewValue());
				}
			} else {
				// Update new
				getNormalAcquisition().handleRemovedConfig((InstrumentConfig) event.getOldValue());
				for (ControlledAcquisition controlledAcquisition : getAcquisitionGroups()) {
					 controlledAcquisition.handleRemovedConfig((InstrumentConfig) event.getOldValue());
				}
			}
		} else if (event.getPropertyName().equals(PROP_SAMPLE_ENVIRONMENTS)) {
			updateControlledAcquisition();
		}
	}
	
	protected void updateControlledAcquisition() {
		// Start a new one
		getAcquisitionGroups().clear();
		// Create buffer with enough dimension
		SampleEnvironmentPreset[][] settings = new SampleEnvironmentPreset[getSampleEnvironments().size()][];
		for (int i = 0; i < settings.length; i++) {
			List<SampleEnvironmentPreset> presetEntrys = getSampleEnvironments().get(i).getPresets();
			SampleEnvironmentPreset[] presetArray = new SampleEnvironmentPreset[presetEntrys.size()];
			for (int j = 0; j < presetArray.length; j++) {
				presetArray[j] = presetEntrys.get(j);
			}
			settings[i] = presetArray;
		}
		// Recursively create acquisition group
		createControlledAcquisition(settings, 0, 0, new HashMap<SampleEnvironment, SampleEnvironmentPreset>());
		// Update sample
		for (Sample sample : getSamples()) {
			if (sample.isRunnable()) {
				for (ControlledAcquisition controlledAcquisition : getAcquisitionGroups()) {
					controlledAcquisition.handleNewSample(sample, true);
				}
			}
		}
		// Update config
		for (InstrumentConfig config : getInstrumentConfigs()) {
			for (ControlledAcquisition controlledAcquisition : getAcquisitionGroups()) {
				 controlledAcquisition.handleNewConfig(config);
			}
		}
	}
	
	private void createControlledAcquisition(SampleEnvironmentPreset[][] settings,
			int currentEnvIndex, int currentValueIndex,
			Map<SampleEnvironment, SampleEnvironmentPreset> buffer) {
		if (currentValueIndex == settings[currentEnvIndex].length) {
			// End of value dimension
			return;
		}
		
		Map<SampleEnvironment, SampleEnvironmentPreset> newBuffer = new HashMap<SampleEnvironment, SampleEnvironmentPreset>(buffer);
		newBuffer.put(getSampleEnvironments().get(currentEnvIndex), settings[currentEnvIndex][currentValueIndex]);
		
		if (currentEnvIndex == (settings.length - 1)) {
			// Reaches end of sample environment dimension
			ControlledAcquisition controlledAcquisition = new ControlledAcquisition(this, newBuffer);
			getAcquisitionGroups().add(controlledAcquisition);
		} else {
			// Go vertically in the sample environment dimension
			createControlledAcquisition(settings, currentEnvIndex + 1, 0, newBuffer);
		}
		
		// Go horizontally in value dimension
		createControlledAcquisition(settings, currentEnvIndex, currentValueIndex + 1, buffer);
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getStartTime() {
		return startTime;
	}

	@Override
	public String toString() {
		return "Experiment [acquisitionGroups=" + acquisitionGroups
				+ ", controlledEnvironment=" + controlledEnvironment
				+ ", darkCurrentFile=" + darkCurrentFile
				+ ", instrumentConfigs=" + instrumentConfigs
				+ ", isFixedSamplePosition=" + isFixedSamplePosition
				+ ", normalAcquisition=" + normalAcquisition
				+ ", sampleEnvControllerIds=" + sampleEnvControllerIds
				+ ", sampleEnvironments=" + sampleEnvironments + ", samples="
				+ samples + ", sensitivityFile=" + sensitivityFile
				+ ", startTime=" + startTime + ", title=" + title + ", user="
				+ user + ", userReportDirectory=" + userReportDirectory + "]";
	}
}
