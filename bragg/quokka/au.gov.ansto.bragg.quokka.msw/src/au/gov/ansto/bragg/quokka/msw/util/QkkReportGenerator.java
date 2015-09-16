package au.gov.ansto.bragg.quokka.msw.util;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.gumtree.msw.schedule.ScheduledNode;
import org.gumtree.msw.schedule.execution.AcquisitionSummary;
import org.gumtree.msw.schedule.execution.IScheduleWalkerListener;
import org.gumtree.msw.schedule.execution.ParameterChangeSummary;
import org.gumtree.msw.schedule.execution.ScheduleStep;
import org.gumtree.msw.schedule.execution.Summary;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ansto.bragg.quokka.msw.Configuration;
import au.gov.ansto.bragg.quokka.msw.Measurement;
import au.gov.ansto.bragg.quokka.msw.Sample;

public class QkkReportGenerator implements IScheduleWalkerListener {
	// finals
	private static final String QKK = "QKK";
	private static final String EXTENSION = ".nx.hdf";
	
	// fields
	private final List<ConfigurationReport> configurations;
	private final String pathIntermediate;
	private final String pathFinal;
	// state
	private ConfigurationReport lastConfigurationReport;
	private MeasurementReport lastMeasurementReport;
	private Map<String, Object> lastSampleParameters;
	
	// construction
	public QkkReportGenerator(String pathIntermediate, String pathFinal) {
		configurations = new ArrayList<>();
		
		this.pathIntermediate = pathIntermediate;
		this.pathFinal = pathFinal;
	}
	
	// methods
	@Override
	public void onBeginSchedule() {
		configurations.clear();
		
		lastConfigurationReport = null;
		lastMeasurementReport = null;
	}
	@Override
	public void onEndSchedule() {
		serializeTo(configurations, pathFinal);
	}
	// step
	@Override
	public void onBeginStep(ScheduleStep step) {
		ScheduledNode node = step.getScheduledNode();
		org.gumtree.msw.elements.Element element = node.getSourceElement();
		if (element == null)
			return;
		
		if (element instanceof Sample) {
			lastSampleParameters = step.getParameters();
		}
		else if (element instanceof Measurement) {
			lastMeasurementReport = new MeasurementReport((String)node.get(Measurement.NAME));
			if (lastConfigurationReport != null)
				lastConfigurationReport.addMeasurement(lastMeasurementReport);
		}
		else if (element instanceof Configuration) {
			lastConfigurationReport = new ConfigurationReport((String)node.get(Configuration.NAME));
			configurations.add(lastConfigurationReport);
		}
	}
	@Override
	public void onEndStep(ScheduleStep step) {
		// ignore
	}
	// parameters
	@Override
	public void onBeginChangeParameter(ScheduleStep step) {
		// ignore
	}
	@Override
	public void onEndChangeParameters(ScheduleStep step, ParameterChangeSummary summary) {
		// ignore
	}
	// acquisition
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
		if ((lastMeasurementReport != null) && (lastSampleParameters != null)) {
			Object name = lastSampleParameters.get(Sample.NAME.getName());
			Object position = lastSampleParameters.get(Sample.POSITION.getName());
			
			if ((name instanceof String) && (position instanceof Double)) {
				lastMeasurementReport.addSample(new SampleReport(
						(double)position,
						(String)name,
						summary.getFilename()));
				serializeTo(configurations, pathIntermediate);
			}
		}
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
	private static void serializeTo(Iterable<ConfigurationReport> configurations, String path) {
		try {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
	
			Document document = documentBuilder.newDocument();
			document.setXmlStandalone(true);
			
			Element root = document.createElement("report");
			for (ConfigurationReport configurationReport : configurations)
				if (!configurationReport.isEmpty()) {
					configurationReport.normalize();
					serialize(document, root, configurationReport);
				}
		
			document.appendChild(root);

			// write to buffer
			Writer stringWriter = new StringWriter();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();			
			Transformer transformer = transformerFactory.newTransformer();
			
		    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
			
			try (FileOutputStream stream = new FileOutputStream(path)) {
				try (Writer writer = new OutputStreamWriter(stream)) {
					// copy line by line
					BufferedReader reader = new BufferedReader(new StringReader(stringWriter.toString()));
					final String newLine = System.getProperty("line.separator");
					
					String line = reader.readLine();
					if (line != null) {
						writer.append(line.replace("><", '>' + newLine + '<'));
						while (null != (line = reader.readLine())) {
							writer.append(newLine);
							writer.append(line);
						}
					}
				}
			}
		}
		catch (ParserConfigurationException | IOException | TransformerException e) {
			e.printStackTrace();
		}
	}
	private static void serialize(Document document, Element root, ConfigurationReport configurationReport) {
		Element configuration = document.createElement("config");
		
		Attr attr = document.createAttribute("name");
		attr.setValue(configurationReport.getName());
		configuration.setAttributeNode(attr);
		
		for (MeasurementReport measurementReport : configurationReport.getMeasurements())
			serialize(document, configuration, measurementReport);
		
		Element mtRunId = document.createElement("emptyBeamTransmissionRunId");
		mtRunId.setTextContent(getRunId(configurationReport.getEmptyBeamTransmissionFilename()));
		configuration.appendChild(mtRunId);
		
		root.appendChild(configuration);
	}
	private static void serialize(Document document, Element root, MeasurementReport measurementReport) {
		Element measurement = document.createElement(measurementReport.getName().toLowerCase());

		for (SampleReport sampleReport : measurementReport.getSamples())
			serialize(document, measurement, sampleReport);

		root.appendChild(measurement);
	}
	private static void serialize(Document document, Element root, SampleReport sampleReport) {
		Element sample = document.createElement("sample");

		Attr attr;
		
		attr = document.createAttribute("name");
		attr.setValue(sampleReport.getName());
		sample.setAttributeNode(attr);

		attr = document.createAttribute("position");
		attr.setValue(Double.toString(sampleReport.getPosition()));
		sample.setAttributeNode(attr);

		attr = document.createAttribute("runId");
		attr.setValue(getRunId(sampleReport.getFilename()));
		sample.setAttributeNode(attr);
		
		root.appendChild(sample);
	}
	private static String getRunId(String filename) {
		if (filename == null)
			return "";

		int qkkIndex = filename.lastIndexOf(QKK);
		if (qkkIndex < 0)
			return filename;
		
		filename = filename.substring(qkkIndex + QKK.length());
		
		if (!filename.endsWith(EXTENSION))
			return filename;

		return filename.substring(0, filename.length() - EXTENSION.length());
	}

	// Configuration Report
	private static class ConfigurationReport {
		// fields
		private final String name;
		private final List<MeasurementReport> measurements;
		private String emptyBeamTransmissionFilename;
		
		// construction
		public ConfigurationReport(String name) {
			this.name = name;
			this.measurements = new ArrayList<>();
			
			emptyBeamTransmissionFilename = null;
		}
		
		// properties
		public boolean isEmpty() {			
			for (MeasurementReport measurementReport : measurements)
				if (!measurementReport.isEmpty())
					return false;

			return true;
		}
		public String getName() {
			return name;
		}
		public String getEmptyBeamTransmissionFilename() {
			return emptyBeamTransmissionFilename;
		}
		public Iterable<MeasurementReport> getMeasurements() {
			return measurements;
		}
		
		// methods
		public void addMeasurement(MeasurementReport measurement) {
			measurements.add(measurement);
		}
		public void normalize() {
			Map<Double, Integer> totalPositions = new HashMap<>();
			Map<Double, Integer> newPositions = new HashMap<>();
			
			// identify all used sample positions and count how often each position is used  
			for (MeasurementReport measurementReport : measurements) {				
				newPositions.clear();
				measurementReport.removeDummies();
				for (SampleReport sampleReport : measurementReport.getSamples()) {
					Integer n = newPositions.get(sampleReport.getPosition());
					if (n == null)
						n = 0;
					newPositions.put(sampleReport.getPosition(), n + 1);
				}
				
				for (Entry<Double, Integer> entry : newPositions.entrySet())
					if (!totalPositions.containsKey(entry.getKey()))
						totalPositions.put(
								entry.getKey(),
								entry.getValue());
					else
						totalPositions.put(
								entry.getKey(),
								Math.max(entry.getValue(), totalPositions.get(entry.getKey())));
			}
			
			// ensure each measurement report is ordered and has the same number of rows
			List<Double> orderedPositions = new ArrayList<>(totalPositions.keySet());
			Collections.sort(orderedPositions);
			for (MeasurementReport measurementReport : measurements)
				measurementReport.normalize(orderedPositions, totalPositions);

			// determine empty beam
			emptyBeamTransmissionFilename = null;
			for (MeasurementReport measurementReport : measurements) {				
				if (Measurement.TRANSMISSION.equals(measurementReport.getName()))
					for (SampleReport sampleReport : measurementReport.getSamples())
						if (Sample.EMPTY_BEAM.equals(sampleReport.getName())) {
							emptyBeamTransmissionFilename = sampleReport.getFilename();
							return;
						}
			}
		}
	}

	// Measurement Report
	private static class MeasurementReport {
		// fields
		private final String name; // transmission or scattering
		private final List<SampleReport> samples;
		
		// construction
		public MeasurementReport(String name) {
			this.name = name;
			this.samples = new ArrayList<>();
		}
		
		// properties
		public boolean isEmpty() {
			return samples.isEmpty();
		}
		public String getName() {
			return name;
		}
		public Iterable<SampleReport> getSamples() {
			return samples;
		}
		
		// methods
		public void addSample(SampleReport sample) {
			samples.add(sample);
		}
		public void removeDummies() {
			for (int n = samples.size(); n > 0; n--)
				if (samples.get(n - 1).isDummy())
					samples.remove(n - 1);
		}
		public void normalize(List<Double> positions, Map<Double, Integer> counts) {
			Collections.sort(samples, new Comparator<SampleReport>() {
				@Override
				public int compare(SampleReport s1, SampleReport s2) {
					return Double.compare(s1.getPosition(), s2.getPosition());
				}
			});
			
			int i = 0;
			for (double position : positions)
				for (int count = counts.get(position); count > 0; count--, i++)
					if ((i >= samples.size()) || (position != samples.get(i).getPosition())) {
						// dummy report is needed
						SampleReport reference = i == 0 ? null : samples.get(i - 1);
						if ((reference != null) && (position == reference.getPosition()))
							samples.add(i, new SampleReport(
									reference.getPosition(),
									reference.getName(),
									reference.getFilename(),
									true)); // copy report
						else
							samples.add(i, new SampleReport(
									position,
									true)); // add blank report
					}
		}
	}
	
	// Sample Report
	private static class SampleReport {
		// fields
		private final String name;
		private final double position;
		private final String filename;
		private final boolean dummy;
		
		// construction
		public SampleReport(double position, boolean dummy) {
			this(position, "", "", true);
		}
		public SampleReport(double position, String name, String filename) {
			this(position, name, filename, false);
		}
		public SampleReport(double position, String name, String filename, boolean dummy) {
			this.name = name;
			this.position = position;
			this.filename = filename;
			this.dummy = dummy;
		}
		
		// properties
		public String getName() {
			return name;
		}
		public double getPosition() {
			return position;
		}
		public String getFilename() {
			return filename;
		}
		public boolean isDummy() {
			return dummy;
		}
	}
}
