package au.gov.ansto.bragg.quokka.msw.report;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gumtree.msw.elements.Element;
import org.gumtree.msw.schedule.ScheduledNode;
import org.gumtree.msw.schedule.execution.AcquisitionSummary;
import org.gumtree.msw.schedule.execution.IScheduleWalkerListener;
import org.gumtree.msw.schedule.execution.InitializationSummary;
import org.gumtree.msw.schedule.execution.ParameterChangeSummary;
import org.gumtree.msw.schedule.execution.ScheduleStep;
import org.gumtree.msw.schedule.execution.ScheduleWalker;
import org.gumtree.msw.schedule.execution.Summary;

import au.gov.ansto.bragg.quokka.msw.Configuration;
import au.gov.ansto.bragg.quokka.msw.Environment;
import au.gov.ansto.bragg.quokka.msw.Measurement;
import au.gov.ansto.bragg.quokka.msw.Sample;
import au.gov.ansto.bragg.quokka.msw.SetPoint;

public class ReportProvider {
	// finals
	private static final String QKK = "QKK";
	private static final String EXTENSION = ".nx.hdf";
	
	// fields
	private ScheduleWalker walker;
	private final IScheduleWalkerListener listener;
	// report
	private State state;
	private EnvironmentReport environmentRoot;
	// 
	private final List<IListener> listeners = new ArrayList<>();
	
	// construction
	public ReportProvider() {
		reset();
		
		listener = new IScheduleWalkerListener() {
			@Override
			public void onBeginSchedule() {
				reset();
			}
			@Override
			public void onEndSchedule() {
				complete();
			}
			@Override
			public void onInitialized(InitializationSummary summary) {
				updateExperiment(
						summary.getProposalNumber(),
						summary.getExperimentTitle(),
						summary.getSampleStage());
			}
			@Override
			public void onCleanedUp(Summary summary) {
				// ignore
			}
			@Override
			public void onBeginStep(ScheduleStep step) {
				if (step.isEnabled())
					updateState(step.getScheduledNode());
			}
			@Override
			public void onEndStep(ScheduleStep step) {
				// ignore
			}
			@Override
			public void onBeginChangeParameter(ScheduleStep step) {
				// ignore
			}
			@Override
			public void onEndChangeParameters(ScheduleStep step, ParameterChangeSummary summary) {
				// ignore
			}
			@Override
			public void onBeginPreAcquisition(ScheduleStep step) {
				// ignore
			}
			@Override
			public void onEndPreAcquisition(ScheduleStep step, Summary summary) {
				// ignore
			}
			@Override
			public void onBeginDoAcquisition(ScheduleStep step) {
				// ignore
			}
			@Override
			public void onEndDoAcquisition(ScheduleStep step, AcquisitionSummary summary) {
				appendAcquisition(extractRunId(summary.getFilename()));
			}
			@Override
			public void onBeginPostAcquisition(ScheduleStep step) {
				// ignore
			}
			@Override
			public void onEndPostAcquisition(ScheduleStep step, Summary summary) {
				// ignore
			}
			
			// helper
			private String extractRunId(String filename) {
				if (filename == null)
					return "";

				int qkkIndex = filename.lastIndexOf(QKK);
				if (qkkIndex < 0)
					return filename;
				
				filename = filename.substring(qkkIndex + QKK.length());
				
				if (!filename.toLowerCase().endsWith(EXTENSION))
					return filename;

				return filename.substring(0, filename.length() - EXTENSION.length());
			}
		};
	}

	// properties
	public EnvironmentReport getRootReport() {
		return environmentRoot;
	}
	
	// methods
	public void bind(ScheduleWalker walker) {
		if (this.walker == walker)
			return;
		
		if (this.walker != null) {
			this.walker.removeListener(listener);
			this.walker = null;
		}
		
		if (walker != null) {
			walker.addListener(listener);
			this.walker = walker;
		}
	}
    // listeners
	public void addListener(IListener listener) {
		if (listeners.contains(listener))
			throw new Error("listener already exists");
		
		listeners.add(listener);
	}
	public boolean removeListener(IListener listener) {
		return listeners.remove(listener);
	}
	// state
	private void reset() {
		state = new State();
		environmentRoot = EnvironmentReport.createDummy();
		
		for (IListener listener : listeners)
			listener.onReset(environmentRoot);
	}
	private void updateExperiment(String proposalNumber, String experimentTitle, String sampleStage) {
		state.updateExperiment(proposalNumber, experimentTitle, sampleStage);
	}
	private void updateState(ScheduledNode node) {
		state.update(node);
	}
	private void appendAcquisition(String runId) {
		if (!state.isValid())
			throw new Error("invalid state");
		
		ConfigurationReport configurationReport = findConfigurationReport(
				state.getExperiment(),
				state.getEnvironments(),
				state.getConfiguration());
		
		SampleReport sampleReport = new SampleReport(
				state.getSample(),
				runId);
		
		if (state.getMeasurement().isScattering())
			configurationReport.appendScatteringRun(
					state.getMeasurement(),
					sampleReport);
		else if (state.getMeasurement().isTransmission())
			configurationReport.appendTransmissionRun(
					state.getMeasurement(),
					sampleReport);
		else
			throw new Error("unexpected measurement");

		for (IListener listener : listeners)
			listener.onUpdated(environmentRoot);
	}
	private void complete() {
		for (IListener listener : listeners)
			listener.onCompleted(environmentRoot);
	}
	private ConfigurationReport findConfigurationReport(ExperimentState experiment, Iterable<EnvironmentState> environments, ConfigurationState configuration) {
		EnvironmentReport environmentReport = environmentRoot;
		for (EnvironmentState environment : environments)
			environmentReport = environmentReport.getEnvironment(environment);
		
		return environmentReport.getConfiguration(experiment, configuration);
	}

	// state

	private static class State {
		// fields
		private ExperimentState lastExperiment;
		private LinkedHashMap<Environment, EnvironmentState> environmentOrder; // LinkedHashMap is used to preserve order
		private ConfigurationState lastConfiguration;
		private MeasurementState lastMeasurement;
		private SampleState lastSample;
		
		// construction
		public State() {
			environmentOrder = new LinkedHashMap<>();
			
			lastExperiment = new ExperimentState();
			lastConfiguration = new ConfigurationState();
			lastMeasurement = new MeasurementState();
			lastSample = new SampleState();
		}
		
		// properties
		public boolean isValid() {
			for (EnvironmentState environment : environmentOrder.values()) {
				if (!environment.isValid())
					return false;

				if (!environment.getSetPoint().isValid())
					return false;
			}

			return
					lastExperiment.isValid() &&
					lastConfiguration.isValid() &&
					lastMeasurement.isValid() &&
					lastSample.isValid();
		}
		public ExperimentState getExperiment() {
			return lastExperiment;
		}
		public Iterable<EnvironmentState> getEnvironments() {
			return environmentOrder.values();
		}
		public ConfigurationState getConfiguration() {
			return lastConfiguration;
		}
		public MeasurementState getMeasurement() {
			return lastMeasurement;
		}
		public SampleState getSample() {
			return lastSample;
		}
		
		// methods
		public void updateExperiment(String proposalNumber, String experimentTitle, String sampleStage) {
			lastExperiment.update(proposalNumber, experimentTitle, sampleStage);
		}
		public void update(ScheduledNode node) {
			Element element = node.getSourceElement();
			if (element == null)
				return;

			if (element instanceof Environment) {
				Environment environment = (Environment)element;
				EnvironmentState lastEnvironment = environmentOrder.get(environment);
				if (lastEnvironment == null)
					environmentOrder.put(environment, lastEnvironment = new EnvironmentState());
				
				lastEnvironment.update(environment, node);
				
				// it is expected that SetPoint is updated after Environment
				lastEnvironment.getSetPoint().invalidate();
			}
			else if (element instanceof SetPoint) {
				EnvironmentState lastEnvironment = environmentOrder.get(node.getOwner().getSourceElement());
				if (lastEnvironment == null)
					throw new Error("unexpected Environment");
				
				lastEnvironment.getSetPoint().update((SetPoint)element, node);
			}
			else if (element instanceof Configuration) {
				lastConfiguration.update((Configuration)element, node);
			}
			else if (element instanceof Measurement) {
				lastMeasurement.update((Measurement)element, node);
			}
			else if (element instanceof Sample) {
				lastSample.update((Sample)element, node);
			}
		}
	}
	
	private static abstract class ElementState {
		// fields
		private boolean valid;
		
		// construction
		public ElementState() {
			valid = false;
		}
		
		// properties
		public boolean isValid() {
			return valid;
		}
		
		// methods
		public void setValid() {
			valid = true;
		}
		public void invalidate() {
			valid = false;
		}
	}
	
	private static class ExperimentState extends ElementState {
		// fields
		//private String proposalNumber;
		//private String experimentTitle;
		private String sampleStage;

		// properties
		//public String getProposalNumber() {
		//	return proposalNumber;
		//}
		//public String getExperimentTitle() {
		//	return experimentTitle;
		//}
		public String getSampleStage() {
			return sampleStage;
		}
		
		// methods
		public void update(String proposalNumber, String experimentTitle, String sampleStage) {
			//this.proposalNumber = proposalNumber;
			//this.experimentTitle = experimentTitle;
			this.sampleStage = sampleStage;

			setValid();
		}
	}
	
	private static class EnvironmentState extends ElementState {
		// fields
		private ScheduledNode node;
		private String name; // name might have been changed in Schedule-Table
		private SetPointState setPoint;
		
		// construction
		public EnvironmentState() {
			setPoint = new SetPointState();
		}
		
		// properties
		public ScheduledNode getNode() {
			return node;
		}
		public String getName() {
			return name;
		}
		public SetPointState getSetPoint() {
			return setPoint;
		}
		
		// methods
		public void update(Environment element, ScheduledNode node) {
			if (node.getSourceElement() != element)
				throw new IllegalArgumentException();

			this.node = node;
			this.name = (String)node.get(Environment.NAME);

			setValid();
		}
	}
	
	private static class SetPointState extends ElementState {
		// fields
		private double value;
		
		// properties
		public double getValue() {
			return value;
		}
		
		// methods
		public void update(SetPoint element, ScheduledNode node) {
			if (node.getSourceElement() != element)
				throw new IllegalArgumentException();

			this.value = (Double)node.get(SetPoint.VALUE);

			setValid();
		}
	}
	
	private static class ConfigurationState extends ElementState {
		// fields
		private ScheduledNode node;
		private String name; // name might have been changed in Schedule-Table
		
		// properties
		public ScheduledNode getNode() {
			return node;
		}
		public String getName() {
			return name;
		}
		
		// methods
		public void update(Configuration element, ScheduledNode node) {
			if (node.getSourceElement() != element)
				throw new IllegalArgumentException();
			
			this.node = node;
			this.name = (String)node.get(Configuration.NAME);

			setValid();
		}
	}
	
	private static class MeasurementState extends ElementState {
		// fields
		private ScheduledNode node;
		private String name; // name might have been changed in Schedule-Table
		private boolean isTransmission;
		
		// properties
		public ScheduledNode getNode() {
			return node;
		}
		public String getName() {
			return name;
		}
		public boolean isTransmission() {
			return isTransmission;
		}
		public boolean isScattering() {
			return !isTransmission;
		}
		
		// methods
		public void update(Measurement element, ScheduledNode node) {
			if (node.getSourceElement() != element)
				throw new IllegalArgumentException();
			
			this.node = node;
			this.name = (String)node.get(Measurement.NAME);
			
			if (element.getPath().getElementName().startsWith(Measurement.TRANSMISSION))
				isTransmission = true;
			else if (element.getPath().getElementName().startsWith(Measurement.SCATTERING))
				isTransmission = false;
			else
				throw new IllegalArgumentException();

			setValid();
		}
	}
	
	private static class SampleState extends ElementState {
		// fields
		private String name; // name might have been changed in Schedule-Table
		private double position;
		private double thickness;
		
		// properties
		public String getName() {
			return name;
		}
		public double getPosition() {
			return position;
		}
		public double getThickness() {
			return thickness;
		}
		
		// methods
		public void update(Sample element, ScheduledNode node) {
			if (node.getSourceElement() != element)
				throw new IllegalArgumentException();
			
			this.name = (String)node.get(Sample.NAME);
			this.position = (Double)node.get(Sample.POSITION);
			this.thickness = (Double)node.get(Sample.THICKNESS);

			setValid();
		}
	}
	
	// reports
	public static interface IReport {
		// methods
		public String getName();
		public boolean isEmpty();
	}
	
	public static class EnvironmentReport implements IReport {
		// fields
		private final boolean isDummy;
		private final String name;
		private final double value;
		// environment has either a sub-environment or contains configurations
		private Element subEnvironment;
		private LinkedHashMap<Double, EnvironmentReport> environments;
		private LinkedHashMap<ScheduledNode, ConfigurationReport> configurations;
		
		// construction
		private EnvironmentReport() {
			// dummy
			isDummy = true;
			name = null;
			value = Double.NaN;
			environments = null;
			configurations = null;
		}
		// State is private
		private EnvironmentReport(EnvironmentState info) {
			isDummy = false;
			name = info.getName();
			value = info.getSetPoint().getValue();
			environments = null;
			configurations = null;
		}
		
		// helper
		public static EnvironmentReport createDummy() {
			return new EnvironmentReport();
		}
		
		// properties
		@Override
		public String getName() {
			return name;
		}
		@Override
		public boolean isEmpty() {
			if (containsConfigurations())
				for (ConfigurationReport report : configurations.values())
					if (!report.isEmpty())
						return false;

			if (containsEnvironments())
				for (EnvironmentReport report : environments.values())
					if (!report.isEmpty())
						return false;
				
			return true;
		}
		public double getValue() {
			return value;
		}
		public boolean isDummy() {
			return isDummy;
		}
		public boolean containsEnvironments() {
			return environments != null;
		}
		public boolean containsConfigurations() {
			return configurations != null;
		}
		public Iterable<EnvironmentReport> getSubEnvironments() {
			if (!containsEnvironments())
				return null;
			
			return environments.values();
		}
		public Iterable<ConfigurationReport> getConfigurations() {
			if (!containsConfigurations())
				return null;
			
			return configurations.values();
		}

		// methods
		private EnvironmentReport getEnvironment(EnvironmentState info) {
			if (containsConfigurations())
				throw new Error("already contains configurations");
			
			if (environments == null) {
				subEnvironment = info.getNode().getSourceElement();
				environments = new LinkedHashMap<>();
			}
			else if (subEnvironment != info.getNode().getSourceElement())
				throw new Error("element of environment doesn't match");
			
			Double value = info.getSetPoint().getValue();
			EnvironmentReport report = environments.get(value);
			if (report == null)
				environments.put(
						value,
						report = new EnvironmentReport(info));
				
			return report;
		}
		private ConfigurationReport getConfiguration(ExperimentState experiment, ConfigurationState info) {
			if (containsEnvironments())
				throw new Error("already contains environments");
			
			if (configurations == null)
				configurations = new LinkedHashMap<>();
			
			ConfigurationReport report = configurations.get(info.getNode());
			if (report == null)
				configurations.put(
						info.getNode(),
						report = new ConfigurationReport(experiment, info));
			
			return report;
		}
	}
	
	public static class ConfigurationReport implements IReport {
		// fields
		private final String name;
		private final String sampleStage;
		private Map<ScheduledNode, ScatteringReport> scatteringReports;
		private Map<ScheduledNode, TransmissionReport> transmissionReports;
		
		// construction (State is private)
		private ConfigurationReport(ExperimentState experiment, ConfigurationState info) {
			name = info.getName();
			sampleStage = experiment.getSampleStage();

			scatteringReports = new LinkedHashMap<>();
			transmissionReports = new LinkedHashMap<>();
		}

		// properties
		@Override
		public String getName() {
			return name;
		}
		public String getSampleStage() {
			return sampleStage;
		}
		@Override
		public boolean isEmpty() {
			for (ScatteringReport report : scatteringReports.values())
				if (!report.isEmpty())
					return false;

			for (TransmissionReport report : transmissionReports.values())
				if (!report.isEmpty())
					return false;
			
			return true;
		}
		public Iterable<ScatteringReport> getScatteringReports() {
			return scatteringReports.values();
		}
		public Iterable<TransmissionReport> getTransmissionReports() {
			return transmissionReports.values();
		}
		public String getEmptyBeamTransmissionRunId() {
			for (TransmissionReport transmissionReport : transmissionReports.values())
				for (SampleReport sampleReport : transmissionReport.getSamples())
					if (Sample.EMPTY_BEAM.equals(sampleReport.getName()))
						return sampleReport.getRunId();
			
			return "";
		}

		// methods
		public void appendScatteringRun(MeasurementState measurement, SampleReport sampleReport) {
			ScatteringReport scatteringReport = scatteringReports.get(measurement.getNode());
			if (scatteringReport == null)
				scatteringReports.put(measurement.getNode(), scatteringReport = new ScatteringReport(measurement));
			
			scatteringReport.appendRun(sampleReport);
		}
		public void appendTransmissionRun(MeasurementState measurement, SampleReport sampleReport) {
			TransmissionReport transmissionReport = transmissionReports.get(measurement.getNode());
			if (transmissionReport == null)
				transmissionReports.put(measurement.getNode(), transmissionReport = new TransmissionReport(measurement));
			
			transmissionReport.appendRun(sampleReport);
		}
	}
	
	public static class MeasurementReport implements IReport {
		// fields
		private final String name;
		private final ArrayList<SampleReport> samples;
		
		// construction (State is private)
		protected MeasurementReport(MeasurementState info) {
			name = info.getName();
			samples = new ArrayList<>();
		}
		
		// properties
		@Override
		public String getName() {
			return name;
		}
		@Override
		public boolean isEmpty() {
			for (SampleReport report : samples)
				if (!report.isEmpty())
					return false;
			
			return true;
		}
		public Iterable<SampleReport> getSamples() {
			return samples;
		}

		// methods
		public void appendRun(SampleReport sampleReport) {
			samples.add(sampleReport);
		}
	}

	public static class TransmissionReport extends MeasurementReport {
		// construction (State is private)
		private TransmissionReport(MeasurementState info) {
			super(info);
		}
	}
	
	public static class ScatteringReport extends MeasurementReport {
		// construction (State is private)
		private ScatteringReport(MeasurementState info) {
			super(info);
		}
	}

	public static class SampleReport implements IReport {
		// fields
		private final String name;
		private final double position;
		private final double thickness;
		private final String runId;
		
		// construction (State is private)
		private SampleReport(SampleState info, String runId) {
			this.name = info.getName();
			this.position = info.getPosition();
			this.thickness = info.getThickness();
			this.runId = runId;
		}
		
		// properties
		@Override
		public String getName() {
			return name;
		}
		@Override
		public boolean isEmpty() {
			return false; // by definition, the SampleReport contains the acquisition information
		}
		public double getPosition() {
			return position;
		}
		public double getThickness() {
			return thickness;
		}
		public String getRunId() {
			return runId;
		}
	}
	
	public static interface IListener {
		// methods
		void onReset(EnvironmentReport rootReport);
		void onUpdated(EnvironmentReport rootReport);
		void onCompleted(EnvironmentReport rootReport);
	}
}
