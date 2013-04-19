package au.gov.ansto.bragg.kookaburra.experiment.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExperimentUserReport {

	private String title;
	
	private String name;
	
	private String email;
	
	private String phone;
	
	private String sensitivityFile;
	
	private String darkCurrentFile;
	
	// Use with normal acquisition only
	private List<ExperimentConfig> configs;
	
	// Use with sample environment only
	private List<SampleEnvironmentEntry> sampleEnvs;
	
	private ProcessDetails processDetails;
	
	private Date startTime;
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getSensitivityFile() {
		return sensitivityFile;
	}

	public void setSensitivityFile(String sensitivityFile) {
		this.sensitivityFile = sensitivityFile;
	}

	public String getDarkCurrentFile() {
		return darkCurrentFile;
	}

	public void setDarkCurrentFile(String darkCurrentFile) {
		this.darkCurrentFile = darkCurrentFile;
	}
	
	public List<ExperimentConfig> getConfigs() {
		if (configs == null) {
			configs = new ArrayList<ExperimentConfig>(2);
		}
		return configs;
	}

	public List<SampleEnvironmentEntry> getSampleEnvs() {
		if (sampleEnvs == null) {
			sampleEnvs = new ArrayList<SampleEnvironmentEntry>(2);
		}
		return sampleEnvs;
	}
	
	public ProcessDetails getProcessDetails() {
		return processDetails;
	}

	public void setProcessDetails(ProcessDetails processDetails) {
		this.processDetails = processDetails;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getStartTime() {
		return startTime;
	}
	
}

