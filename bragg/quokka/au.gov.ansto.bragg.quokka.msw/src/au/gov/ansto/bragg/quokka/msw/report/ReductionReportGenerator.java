package au.gov.ansto.bragg.quokka.msw.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ansto.bragg.quokka.msw.report.ReportProvider.ConfigurationReport;
import au.gov.ansto.bragg.quokka.msw.report.ReportProvider.EnvironmentReport;
import au.gov.ansto.bragg.quokka.msw.report.ReportProvider.SampleReport;
import au.gov.ansto.bragg.quokka.msw.report.ReportProvider.ScatteringReport;
import au.gov.ansto.bragg.quokka.msw.report.ReportProvider.TransmissionReport;

public class ReductionReportGenerator {
	// construction
	private ReductionReportGenerator() {
	}

	// methods
	public static boolean save(EnvironmentReport environmentRoot, File file) {
		if (file == null)
			return false;
		
		try {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
	
			Document document = documentBuilder.newDocument();
			document.setXmlStandalone(true);
			
			Element root = document.createElement("report");
			serialize(document, root, environmentRoot, "");
			document.appendChild(root);

			// write to buffer
			Writer stringWriter = new StringWriter();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();			
			Transformer transformer = transformerFactory.newTransformer();
			
		    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		    //transformer.setOutputProperty(OutputKeys.ENCODING, "US-ASCII"); // is used to make '°' in AttenuationAngle work (for some reason transformer with UTF-8 doesn't seem to work properly)
		    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		    
			transformer.transform(
					new DOMSource(document),
					new StreamResult(stringWriter));
			
			try (FileOutputStream stream = new FileOutputStream(file)) {
				try (Writer writer = new OutputStreamWriter(stream)) {
					// quick fix: ensure that every xml element starts on a new line
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
			
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// serialization
	private static void serialize(Document document, Element root, EnvironmentReport report, String environmentSpecifier) {
		if (!report.isDummy()) {
			if (!environmentSpecifier.isEmpty())
				environmentSpecifier += '/';

			// collapse environment settings into one string
			environmentSpecifier += String.format("%s(%s)", report.getName(), Double.toString(report.getValue()));
		}
		
		if (!report.isEmpty())
			if (report.containsEnvironments()) {
				for (EnvironmentReport subReport : report.getSubEnvironments())
					serialize(document, root, subReport, environmentSpecifier);
			}
			else if (report.containsConfigurations()) {
				if (environmentSpecifier.isEmpty()) {
					// just store configurations without environment node
					serialize(document, root, report.getConfigurations());
				}
				else {
					Element environment = document.createElement("sampleEnvironment");
					environment.setAttribute("name", environmentSpecifier);
					serialize(document, environment, report.getConfigurations());
					root.appendChild(environment);
				}
			}
	}
	private static void serialize(Document document, Element root, Iterable<ConfigurationReport> reports) {
		for (ConfigurationReport report : reports)
			if (!report.isEmpty())
				serialize(document, root, report);
	}
	private static void serialize(Document document, Element root, ConfigurationReport report) {
		Element configuration = document.createElement("config");
		configuration.setAttribute("name", report.getName());

		Element transmission = document.createElement("transmission");
		Element scattering = document.createElement("scattering");
		
		// transmission and scattering element must have same number of samples
		// if there is just one transmission-report apply it to all scattering-reports

		Iterator<TransmissionReport> transmissionReports = report.getTransmissionReports().iterator();
		Iterator<ScatteringReport> scatteringReports = report.getScatteringReports().iterator();
		
		if (!transmissionReports.hasNext()) {
			transmission.setAttribute("name", "");
			
			boolean setAttribute = true;
			while (scatteringReports.hasNext()) {
				ScatteringReport scatteringReport = scatteringReports.next();
				if (setAttribute) {
					setAttribute = false;
					scattering.setAttribute("name", scatteringReport.getName());
				}
				serialize(document, transmission, scattering, scatteringReport);
			}
		}
		else if (!scatteringReports.hasNext()) {
			scattering.setAttribute("name", "");
			
			boolean setAttribute = true;
			while (transmissionReports.hasNext()) {
				TransmissionReport transmissionReport = transmissionReports.next();
				if (setAttribute) {
					setAttribute = false;
					transmission.setAttribute("name", transmissionReport.getName());
				}
				serialize(document, transmission, scattering, transmissionReport);
			}
		}
		else {
			TransmissionReport transmissionReport = transmissionReports.next();
			ScatteringReport scatteringReport = scatteringReports.next();

			transmission.setAttribute("name", transmissionReport.getName());
			scattering.setAttribute("name", scatteringReport.getName());

			boolean repeat;
			do {
				serialize(document, transmission, scattering, transmissionReport, scatteringReport);
				
				repeat = false;
				if (transmissionReports.hasNext()) {
					transmissionReport = transmissionReports.next();
					repeat = true;
				}
				if (scatteringReports.hasNext()) {
					scatteringReport = scatteringReports.next();
					repeat = true;
				}
			} while (repeat);
		}
		
		configuration.appendChild(transmission);
		configuration.appendChild(scattering);
		
		Element mtRunId = document.createElement("emptyBeamTransmissionRunId");
		mtRunId.setTextContent(report.getEmptyBeamTransmissionRunId());
		configuration.appendChild(mtRunId);
		
		root.appendChild(configuration);
	}
	private static void serialize(Document document, Element transmission, Element scattering, TransmissionReport transmissionReport) {
		Map<Double, List<SampleReport>> transmissionSampleMap = createSampleMap(transmissionReport.getSamples());

		List<Double> positions = new ArrayList<>(transmissionSampleMap.keySet());
		Collections.sort(positions);

		for (Double position : positions)
			for (SampleReport transmissionSample : transmissionSampleMap.get(position)) {
				serialize(document, transmission, transmissionSample);
				serializeDummySample(document, scattering, position);
			}
	}
	private static void serialize(Document document, Element transmission, Element scattering, ScatteringReport scatteringReport) {
		Map<Double, List<SampleReport>> scatteringSampleMap = createSampleMap(scatteringReport.getSamples());

		List<Double> positions = new ArrayList<>(scatteringSampleMap.keySet());
		Collections.sort(positions);

		for (Double position : positions)
			for (SampleReport scatteringSample : scatteringSampleMap.get(position)) {
				serializeDummySample(document, transmission, position);
				serialize(document, scattering, scatteringSample);
			}
	}
	private static void serialize(Document document, Element transmission, Element scattering, TransmissionReport transmissionReport, ScatteringReport scatteringReport) {
		Map<Double, List<SampleReport>> transmissionSampleMap = createSampleMap(transmissionReport.getSamples());
		Map<Double, List<SampleReport>> scatteringSampleMap = createSampleMap(scatteringReport.getSamples());

		Set<Double> positionSet = new HashSet<>();
		positionSet.addAll(transmissionSampleMap.keySet());
		positionSet.addAll(scatteringSampleMap.keySet());
		
		List<Double> positions = new ArrayList<>(positionSet);
		Collections.sort(positions);
		
		for (Double position : positions) {
			Iterator<SampleReport> transmissionSamples = createSampleIterator(transmissionSampleMap.get(position));
			Iterator<SampleReport> scatteringSamples = createSampleIterator(scatteringSampleMap.get(position));
			
			SampleReport transmissionSample = null;
			SampleReport scatteringSample = null;
			while (transmissionSamples.hasNext() || scatteringSamples.hasNext()) {
				// iteration
				if (transmissionSamples.hasNext())
					transmissionSample = transmissionSamples.next();
				if (scatteringSamples.hasNext())
					scatteringSample = scatteringSamples.next();

				// serialization
				if (transmissionSample != null)
					serialize(document, transmission, transmissionSample);
				else
					serializeDummySample(document, transmission, position);

				if (scatteringSample != null)
					serialize(document, scattering, scatteringSample);
				else
					serializeDummySample(document, scattering, position);
			}
		}
	}
	private static void serialize(Document document, Element root, SampleReport report) {
		Element sample = document.createElement("sample");
		sample.setAttribute("name", report.getName());
		sample.setAttribute("position", Double.toString(report.getPosition()));
		sample.setAttribute("runId", report.getRunId());
		sample.setAttribute("type", "sample");
		root.appendChild(sample);
	}
	private static void serializeDummySample(Document document, Element root, double position) {
		Element sample = document.createElement("sample");
		sample.setAttribute("name", "");
		sample.setAttribute("position", Double.toString(position));
		sample.setAttribute("runId", "");
		sample.setAttribute("type", "sample");
		root.appendChild(sample);
	}
	
	// helper
	private static Map<Double, List<SampleReport>> createSampleMap(Iterable<SampleReport> sampleReports) {
		Map<Double, List<SampleReport>> result = new HashMap<>();
		
		for (SampleReport report : sampleReports) {
			Double position = report.getPosition();
			List<SampleReport> list = result.get(position);
			if (list == null)
				result.put(position, list = new ArrayList<>(1)); // by default capacity should be 1
			
			list.add(report);
		}
		
		return result;
	}
	private static Iterator<SampleReport> createSampleIterator(List<SampleReport> list) {
		if (list == null)
			return Collections.emptyIterator();
		
		return list.iterator();
	}
}
