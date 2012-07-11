/**
 * 
 */
package au.gov.ansto.bragg.quokka.dra.online.util;

import java.io.File;

import au.gov.ansto.bragg.quokka.experiment.report.ExperimentConfig;
import au.gov.ansto.bragg.quokka.experiment.report.ExperimentUserReport;
import au.gov.ansto.bragg.quokka.experiment.report.ExperimentUserReportUtils;
import au.gov.ansto.bragg.quokka.experiment.report.SampleResult;
import au.gov.ansto.bragg.quokka.experiment.util.SampleType;


/**
 * @author nxi
 *
 */
public class ScatteringResult {

	public static final String SICS_DATA_PATH = "sics.data.path";
	public static final String FILENAME_PRIFIX = "QKK";
	public static final String FILENAME_SUFFIX = ".nx.hdf";
	private String preferedFolderName;
	private String folderName;
	private String sampleScatteringFilename;
	private String transmissionFilename;
	private String emptyCellTransmissionFilename;
	private String emptyCellScatteringFilename;
	private String emptyBeamTransmissionFilename;
	private String emptyBeamScatteringFilename;
	private String backgroundTransmissionFilename;
	private String darkCurrentScatteringFilename;
	private String sensitivityFilename;
	private String configurationName;
	private float lambda; 
	private float l1;
	private float l2;
	
	public ScatteringResult(){
		
	}
	
	public ScatteringResult(ExperimentUserReport experiment, SampleResult sampleScattering, String folderPath){
		ExperimentConfig config = ExperimentUserReportUtils.getExperimentConfig(experiment, sampleScattering);
		if (config == null)
			return;
		this.configurationName = config.getName();
		preferedFolderName = folderPath;
		folderName = System.getProperty(SICS_DATA_PATH);
		if (folderName == null || folderName.trim().length() == 0)
			folderName = "W:/commissioning";
		sampleScatteringFilename = createFilenameWithPath(sampleScattering);
		transmissionFilename = createFilenameWithPath(ExperimentUserReportUtils.getTransmission(
				config, sampleScattering, SampleType.SAMPLE));
//		emptyCellTransmissionFilename = createFilenameWithPath(ExperimentUserReportUtils.getTransmission(
//				config, sampleScattering, SampleType.EMPTY_CELL));
		emptyCellTransmissionFilename = createFilenameWithPath(config.getEmptyCellTransmissionRunId());
//		emptyCellScatteringFilename = createFilenameWithPath(ExperimentUserReportUtils.getScattering(
//				config, sampleScattering, SampleType.EMPTY_CELL));
		emptyCellScatteringFilename = createFilenameWithPath(config.getEmptyCellScatteringRunId());
		emptyBeamScatteringFilename = createFilenameWithPath(ExperimentUserReportUtils.getScattering(
				config, sampleScattering, SampleType.EMPTY_BEAM));
//		emptyBeamTransmissionFilename = createFilenameWithPath(ExperimentUserReportUtils.getTransmission(
//				config, sampleScattering, SampleType.EMPTY_BEAM));
		emptyBeamTransmissionFilename = createFilenameWithPath(config.getEmptyBeamTransmissionRunId());
		backgroundTransmissionFilename = createFilenameWithPath(ExperimentUserReportUtils.getTransmission(
				config, sampleScattering, SampleType.DARK_CURRENT));
		darkCurrentScatteringFilename = createFilenameWithPath(ExperimentUserReportUtils.getScattering(
				config, sampleScattering, SampleType.DARK_CURRENT));
		sensitivityFilename = experiment.getSensitivityFile();
		lambda = config.getLambda();
		l1 = config.getL1();
		l2 = config.getL2();
	}
	
	public ScatteringResult(ExperimentUserReport experiment, SampleResult sampleScattering){
		this(experiment, sampleScattering, null);
	}
	
	public String getSampleScatteringFilename() {
		return sampleScatteringFilename;
	}
	public void setSampleScatteringFilename(String sampleScatteringFilename) {
		this.sampleScatteringFilename = sampleScatteringFilename;
	}
	public String getTransmissionFilename() {
		return transmissionFilename;
	}
	public void setTransmissionFilename(String transmissionFilename) {
		this.transmissionFilename = transmissionFilename;
	}
	public String getEmptyCellTransmissionFilename() {
		return emptyCellTransmissionFilename;
	}
	public void setEmptyCellTransmissionFilename(
			String emptyCellTransmissionFilename) {
		this.emptyCellTransmissionFilename = emptyCellTransmissionFilename;
	}
	public String getEmptyCellScatteringFilename() {
		return emptyCellScatteringFilename;
	}
	public void setEmptyCellScatteringFilename(String emptyCellScatteringFilename) {
		this.emptyCellScatteringFilename = emptyCellScatteringFilename;
	}
	public String getEmptyBeamTransmissionFilename() {
		return emptyBeamTransmissionFilename;
	}
	public void setEmptyBeamTransmissionFilename(
			String emptyBeamTransmissionFilename) {
		this.emptyBeamTransmissionFilename = emptyBeamTransmissionFilename;
	}
	public String getEmptyBeamScatteringFilename() {
		return emptyBeamScatteringFilename;
	}
	public void setEmptyBeamScatteringFilename(String emptyBeamScatteringFilename) {
		this.emptyBeamScatteringFilename = emptyBeamScatteringFilename;
	}
	public String getBackgroundTransmissionFilename() {
		return backgroundTransmissionFilename;
	}
	public void setBackgroundTransmissionFilename(
			String backgroundTransmissionFilename) {
		this.backgroundTransmissionFilename = backgroundTransmissionFilename;
	}
	public String getDarkCurrentScatteringFilename() {
		return darkCurrentScatteringFilename;
	}
	public void setDarkCurrentScatteringFilename(String darkCurrentScatteringFilename) {
		this.darkCurrentScatteringFilename = darkCurrentScatteringFilename;
	}
	public String getSensitivityFilename() {
		return sensitivityFilename;
	}
	public void setSensitivityFilename(String sensitivityFilename) {
		this.sensitivityFilename = sensitivityFilename;
	}
	public float getLambda() {
		return lambda;
	}
	public void setLambda(float lambda) {
		this.lambda = lambda;
	}
	public float getL1() {
		return l1;
	}
	public void setL1(float l1) {
		this.l1 = l1;
	}
	public float getL2() {
		return l2;
	}
	public void setL2(float l2) {
		this.l2 = l2;
	} 
	
	public String getConfigurationName() {
		return configurationName;
	}

	public String createFilenameWithPath(String numberId){
		if (numberId == null || numberId.trim().length() == 0 || numberId.equals("null"))
			return null;
		String filename = FILENAME_PRIFIX + numberId + FILENAME_SUFFIX;
		String fullPath = null;
		if (preferedFolderName != null){
			File folderFile = new File(preferedFolderName);
			if (folderFile.isDirectory()){
				fullPath = folderFile.getAbsolutePath() + "/" + filename;
			}else{
				preferedFolderName = folderFile.getParent();
				fullPath = preferedFolderName + "/" + filename;
			}
			File file = new File(fullPath);
			if (file.exists())
				return fullPath;
		}
		if (folderName != null){
			fullPath = folderName + "/" + filename;
			File file = new File(fullPath);
			if (file.exists())
				return fullPath;
		}
		if (preferedFolderName != null){
			File folderFile = new File(preferedFolderName).getParentFile();
			fullPath = folderFile.getAbsolutePath() + "/" + filename;
			File file = new File(fullPath);
			if (file.exists())
				return fullPath;
		}
		return null;
	}
	
	public String createFilenameWithPath(SampleResult result){
		if (result == null)
			return null;
		return createFilenameWithPath(result.getRunId());
	}

	public void setMasterReport(ExperimentUserReport masterModel) {
		for (ExperimentConfig config : masterModel.getConfigs()){
			if (config.getName().equals(configurationName)){
				if (emptyCellTransmissionFilename == null || 
						emptyCellTransmissionFilename.trim().length() == 0)
					emptyCellTransmissionFilename = createFilenameWithPath(
							config.getEmptyCellTransmissionRunId());
				if (emptyCellScatteringFilename == null || 
						emptyCellScatteringFilename.trim().length() == 0)
					emptyCellScatteringFilename = createFilenameWithPath(
							config.getEmptyCellScatteringRunId());
				if (emptyBeamTransmissionFilename == null || 
						emptyBeamTransmissionFilename.trim().length() == 0)
					emptyBeamTransmissionFilename = createFilenameWithPath(
							config.getEmptyBeamTransmissionRunId());
			}
		}
	}
}
