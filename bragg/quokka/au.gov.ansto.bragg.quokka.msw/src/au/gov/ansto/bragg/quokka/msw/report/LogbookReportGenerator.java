package au.gov.ansto.bragg.quokka.msw.report;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.gumtree.msw.ui.util.AlphanumericComparator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ansto.bragg.quokka.msw.report.ReportProvider.ConfigurationReport;
import au.gov.ansto.bragg.quokka.msw.report.ReportProvider.EnvironmentReport;
import au.gov.ansto.bragg.quokka.msw.report.ReportProvider.SampleReport;
import au.gov.ansto.bragg.quokka.msw.report.ReportProvider.ScatteringReport;
import au.gov.ansto.bragg.quokka.msw.report.ReportProvider.TransmissionReport;

public class LogbookReportGenerator {
	// finals
	private static final String EMPTY_CELL_CONTENT = Character.toString((char)160); // Non-breaking space: &nbsp;
	
	// construction
	private LogbookReportGenerator() {
	}
	
	// methods (returns map: name->table
	public static Iterable<TableInfo> createHtmlTables(EnvironmentReport rootReport) {
		Iterable<EnvironmentInfo> summary = analyze(rootReport);

		List<TableInfo> result = new ArrayList<>();
		try {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();

			for (EnvironmentInfo environment : summary)
				if (!environment.isEmpty()) {
					// configuration tables
					for (ConfigurationInfo configuration : environment.getConfigurations()) {
						if (!configuration.isEmpty()) {
							TableInfo table = createConfigurationTable(documentBuilder, environment, configuration);
							if (table != null)
								result.add(table);
						}
					}
					
					// scattering table
					{
						TableInfo table = createScatteringTable(documentBuilder, environment);
						if (table != null)
							result.add(table);
					}
				}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	// configuration table
	private static TableInfo createConfigurationTable(DocumentBuilder documentBuilder, EnvironmentInfo environment, ConfigurationInfo configuration) {
		Document document = documentBuilder.newDocument();
		document.setXmlStandalone(true);
		
		Element root = document.createElement("table");
		serializeConfigurationTableHeader(document, root, environment, configuration);
		serializeConfigurationTableContent(document, root, configuration);
		document.appendChild(decorateTable(document, root));

		String name = configuration.getName();
		if (!environment.getName().isEmpty())
			name = environment.getName() + "/" + name;
		
		return createTableInfo(name, document);
	}
	private static void serializeConfigurationTableHeader(Document document, Element root, EnvironmentInfo environment, ConfigurationInfo configuration) {
		if (!environment.getName().isEmpty()) {
			int colspan =
					3 + // sample
					Math.max(
						1,
						configuration.getTransmissionRuns().size() + configuration.getScatteringRuns().size());
			
			Element header0 = document.createElement("tr");
			Element cell = document.createElement("th");
			cell.setAttribute("colspan", String.valueOf(colspan));
			cell.setTextContent(getStringOrEmpty(environment.getName()));
			header0.appendChild(cell);
			root.appendChild(header0);
		}

		Element header1 = document.createElement("tr");
		{
			Element cell = document.createElement("th");
			cell.setAttribute("colspan", "3"); // (position, name, thickness)
			cell.setTextContent("Sample");
			header1.appendChild(cell);
		}
		{
			int colspan = Math.max(
					1,
					configuration.getTransmissionRuns().size() + configuration.getScatteringRuns().size());

			Element cell = document.createElement("th");
			cell.setAttribute("colspan", String.valueOf(colspan));
			cell.setTextContent(getStringOrEmpty(configuration.getName()));
			header1.appendChild(cell);
		}
		root.appendChild(header1);
		
		Element header2 = document.createElement("tr");
		{
			Element cell = document.createElement("th");
			cell.setTextContent("Position");
			header2.appendChild(cell);
		}
		{
			Element cell = document.createElement("th");
			cell.setTextContent("Name");
			header2.appendChild(cell);
		}
		{
			Element cell = document.createElement("th");
			cell.setTextContent("Thickness");
			header2.appendChild(cell);
		}
		for (MeasurementInfo transmission : configuration.getTransmissionRuns()) {
			Element cell = document.createElement("th");
			cell.setTextContent(getStringOrEmpty(transmission.getName()));
			header2.appendChild(cell);
		}
		for (MeasurementInfo scattering : configuration.getScatteringRuns()) {
			Element cell = document.createElement("th");
			cell.setTextContent(getStringOrEmpty(scattering.getName()));
			header2.appendChild(cell);
		}
		root.appendChild(header2);
	}
	private static void serializeConfigurationTableContent(Document document, Element root, ConfigurationInfo configuration) {
		Map<Double, SampleInfo> samples = new HashMap<>();
		List<Map<Double, String>> content = new ArrayList<>();
		
		for (MeasurementInfo transmission : configuration.getTransmissionRuns())
			content.add(createConfigurationTableColumn(samples, transmission));

		for (MeasurementInfo scattering : configuration.getScatteringRuns())
			content.add(createConfigurationTableColumn(samples, scattering));
		
		List<Double> positions = new ArrayList<>(samples.keySet());
		Collections.sort(positions);
		
		for (Double position : positions) {
			Element row = document.createElement("tr");
			// position, name, thickness
			{
				SampleInfo sample = samples.get(position);
				
				Element positionCell = document.createElement("td");
				positionCell.setTextContent(String.valueOf(position));
				row.appendChild(positionCell);

				Element nameCell = document.createElement("td");
				nameCell.setTextContent(getStringOrEmpty(sample.getName()));
				row.appendChild(nameCell);

				Element thicknessCell = document.createElement("td");
				thicknessCell.setTextContent(String.valueOf(sample.getThickness()));
				row.appendChild(thicknessCell);
			}
			// runId(s)
			for (Map<Double, String> column : content) {
				Element cell = document.createElement("td");
				if (column.containsKey(position))
					cell.setTextContent(getStringOrEmpty(column.get(position)));
				else
					cell.setTextContent(EMPTY_CELL_CONTENT);
				row.appendChild(cell);
			}
			root.appendChild(row);
		}
	}
	// scattering table
	private static TableInfo createScatteringTable(DocumentBuilder documentBuilder, EnvironmentInfo environment) {
		Document document = documentBuilder.newDocument();
		document.setXmlStandalone(true);
		
		Element root = document.createElement("table");
		serializeScatteringTableHeader(document, root, environment);
		serializeScatteringTableContent(document, root, environment);
		document.appendChild(decorateTable(document, root));

		return createTableInfo(environment.getName(), document);
	}
	private static void serializeScatteringTableHeader(Document document, Element root, EnvironmentInfo environment) {
		if (!environment.getName().isEmpty()) {
			int colspan = 3; // sample
			for (ConfigurationInfo configuration : environment.getConfigurations())
				colspan += configuration.getScatteringRuns().size();
			
			Element header0 = document.createElement("tr");
			Element cell = document.createElement("th");
			cell.setAttribute("colspan", String.valueOf(colspan));
			cell.setTextContent(getStringOrEmpty(environment.getName()));
			header0.appendChild(cell);
			root.appendChild(header0);
		}
		
		Element header1 = document.createElement("tr");
		{
			Element cell = document.createElement("th");
			cell.setAttribute("colspan", "3"); // (position, name, thickness)
			cell.setTextContent("Sample");
			header1.appendChild(cell);
		}
		for (ConfigurationInfo configuration : environment.getConfigurations()) {
			int colspan = configuration.getScatteringRuns().size();
			if (colspan > 0) {
				Element cell = document.createElement("th");
				cell.setAttribute("colspan", String.valueOf(colspan));
				cell.setTextContent(getStringOrEmpty(configuration.getName()));
				header1.appendChild(cell);
			}
		}
		root.appendChild(header1);

		Element header2 = document.createElement("tr");
		{
			Element cell = document.createElement("th");
			cell.setTextContent("Position");
			header2.appendChild(cell);
		}
		{
			Element cell = document.createElement("th");
			cell.setTextContent("Name");
			header2.appendChild(cell);
		}
		{
			Element cell = document.createElement("th");
			cell.setTextContent("Thickness");
			header2.appendChild(cell);
		}
		for (ConfigurationInfo configuration : environment.getConfigurations()) {
			for (MeasurementInfo scattering : configuration.getScatteringRuns()) {
				Element cell = document.createElement("th");
				cell.setTextContent(getStringOrEmpty(scattering.getName()));
				header2.appendChild(cell);
			}
		}
		root.appendChild(header2);
	}
	private static void serializeScatteringTableContent(Document document, Element root, EnvironmentInfo environment) {
		Map<Double, SampleInfo> samples = new HashMap<>();
		List<Map<Double, String>> content = new ArrayList<>();

		for (ConfigurationInfo configuration : environment.getConfigurations())
			for (MeasurementInfo scattering : configuration.getScatteringRuns())
				content.add(createConfigurationTableColumn(samples, scattering));

		List<Double> positions = new ArrayList<>(samples.keySet());
		Collections.sort(positions);
		
		for (Double position : positions) {
			Element row = document.createElement("tr");
			// position, name, thickness
			{
				SampleInfo sample = samples.get(position);
				
				Element positionCell = document.createElement("td");
				positionCell.setTextContent(String.valueOf(position));
				row.appendChild(positionCell);

				Element nameCell = document.createElement("td");
				nameCell.setTextContent(getStringOrEmpty(sample.getName()));
				row.appendChild(nameCell);

				Element thicknessCell = document.createElement("td");
				thicknessCell.setTextContent(String.valueOf(sample.getThickness()));
				row.appendChild(thicknessCell);
			}
			// runId(s)
			for (Map<Double, String> column : content) {
				Element cell = document.createElement("td");
				if (column.containsKey(position))
					cell.setTextContent(getStringOrEmpty(column.get(position)));
				else
					cell.setTextContent(EMPTY_CELL_CONTENT);
				row.appendChild(cell);
			}
			root.appendChild(row);
		}
	}
	
	// helper
	private static Map<Double, String> createConfigurationTableColumn(Map<Double, SampleInfo> samples, MeasurementInfo measurement) {
		Map<Double, String> column = new HashMap<>();
		StringBuilder buffer = new StringBuilder();
		
		for (Entry<Double, SampleInfo> entry : measurement.getSamples().entrySet()) {
			Double position = entry.getKey();
			SampleInfo sampleInfo = entry.getValue();
			
			if (!samples.containsKey(position))
				samples.put(position, sampleInfo);

			buffer.setLength(0);
			for (String runId : sampleInfo.getRunIds())
				if (buffer.length() == 0)
					buffer.append(runId);
				else
					buffer.append(", ").append(runId);
			
			column.put(position, buffer.toString());
		}
		return column;
	}
	private static Element decorateTable(Document document, Element table) {
		table.setAttribute("align", "center");
		table.setAttribute("border", "1");
		table.setAttribute("cellpadding", "2");
		table.setAttribute("cellspacing", "0");
		table.setAttribute("class", "xmlTable");
		table.setAttribute("style", "table-layout:fixed; width:100%; word-wrap:break-word");

		Element root = document.createElement("div");
		root.appendChild(table);
		
		return root;
	}
	private static String getStringOrEmpty(String value) {
		if ((value == null) || value.isEmpty())
			return EMPTY_CELL_CONTENT;
		
		return value;
	}
	private static TableInfo createTableInfo(String name, Document document) {
		try {
			// write to buffer
			Writer stringWriter = new StringWriter();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();			
			Transformer transformer = transformerFactory.newTransformer();
			
		    transformer.setOutputProperty(OutputKeys.METHOD, "html");
		    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		    //transformer.setOutputProperty(OutputKeys.ENCODING, "US-ASCII"); // is used to make '°' in AttenuationAngle work (for some reason transformer with UTF-8 doesn't seem to work properly)
		    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		    
			transformer.transform(
					new DOMSource(document),
					new StreamResult(stringWriter));

			return new TableInfo(name, stringWriter.toString());
		}
		catch (TransformerException e) {
			e.printStackTrace();
			return null;
		}
	}
	// analyze
	private static Iterable<EnvironmentInfo> analyze(EnvironmentReport rootReport) {
		List<EnvironmentInfo> result = new ArrayList<>();
		if (!rootReport.isEmpty())
			analyzeEnvironmentReport(result, rootReport, "");
		return result;
	}
	private static void analyzeEnvironmentReport(List<EnvironmentInfo> result, EnvironmentReport report, String environmentSpecifier) {
		if (!report.isDummy()) {
			if (!environmentSpecifier.isEmpty())
				environmentSpecifier += '/';

			// collapse environment settings into one string
			environmentSpecifier += String.format("%s(%s)", report.getName(), Double.toString(report.getValue()));
		}

		if (report.containsEnvironments()) {
			for (EnvironmentReport subReport : report.getSubEnvironments())
				analyzeEnvironmentReport(result, subReport, environmentSpecifier);
		}
		else if (report.containsConfigurations()) {
			EnvironmentInfo info = new EnvironmentInfo(environmentSpecifier);
			analyzeConfigurationReports(info, report.getConfigurations());
			if (!info.isEmpty())
				result.add(info);
		}
	}
	private static void analyzeConfigurationReports(EnvironmentInfo environment, Iterable<ConfigurationReport> reports) {
		for (ConfigurationReport report : reports)
			if (!report.isEmpty()) {
				ConfigurationInfo info = new ConfigurationInfo(report.getName());
				analyzeTransmissionReports(info, report.getTransmissionReports());
				analyzeScatteringReports(info, report.getScatteringReports());
				environment.addConfiguration(info);
			}
	}
	private static void analyzeTransmissionReports(ConfigurationInfo configuration, Iterable<TransmissionReport> reports) {
		for (TransmissionReport report : reports)
			if (!report.isEmpty()) {
				MeasurementInfo info = new MeasurementInfo(report.getName());
				analyzeSampleReports(info, report.getSamples());
				configuration.addTransmissionRun(info);
			}
	}
	private static void analyzeScatteringReports(ConfigurationInfo configuration, Iterable<ScatteringReport> reports) {
		for (ScatteringReport report : reports)
			if (!report.isEmpty()) {
				MeasurementInfo info = new MeasurementInfo(report.getName());
				analyzeSampleReports(info, report.getSamples());
				configuration.addScatteringRun(info);
			}
	}
	private static void analyzeSampleReports(MeasurementInfo measurement, Iterable<SampleReport> reports) {
		for (SampleReport report : reports)
			if (!report.isEmpty()) {
				SampleInfo info = measurement.getSample(report.getPosition());
				if (info == null)
					measurement.addSample(report.getPosition(), info = new SampleInfo(report.getName(), report.getThickness()));
				
				info.addRun(report.getRunId());
			}
	}
	
	// result
	public static class TableInfo {
		// fields
		private final String name;
		private final String content;
		
		// construction
		public TableInfo(String name, String content) {
			this.name = name;
			this.content = content;
		}

		// properties
		public String getName() {
			return name;
		}
		public String getContent() {
			return content;
		}
	}
	
	// internal
	private static class EnvironmentInfo {
		// fields
		private final String name;
		private final List<ConfigurationInfo> configurations;
		
		// construction
		public EnvironmentInfo(String name) {
			this.name = name;
			this.configurations = new ArrayList<>();
		}
		
		// properties
		public String getName() {
			return name;
		}
		public boolean isEmpty() {
			return configurations.isEmpty();
		}
		public Iterable<ConfigurationInfo> getConfigurations() {
			return configurations;
		}

		// methods
		public void addConfiguration(ConfigurationInfo configuration) {
			configurations.add(configuration);
		}
	}
	private static class ConfigurationInfo {
		// fields
		private final String name;
		private final List<MeasurementInfo> transmissionRuns;
		private final List<MeasurementInfo> scatteringRuns;
		
		// construction
		public ConfigurationInfo(String name) {
			this.name = name;

			transmissionRuns = new ArrayList<>();
			scatteringRuns = new ArrayList<>();
		}
		
		// properties
		public String getName() {
			return name;
		}
		public boolean isEmpty() {
			for (MeasurementInfo info : transmissionRuns)
				if (!info.isEmpty())
					return false;
			
			for (MeasurementInfo info : scatteringRuns)
				if (!info.isEmpty())
					return false;
			
			return true;
		}
		public List<MeasurementInfo> getTransmissionRuns() {
			return transmissionRuns;
		}
		public List<MeasurementInfo> getScatteringRuns() {
			return scatteringRuns;
		}
		
		// methods
		public void addTransmissionRun(MeasurementInfo info) {
			transmissionRuns.add(info);
		}
		public void addScatteringRun(MeasurementInfo info) {
			scatteringRuns.add(info);
		}
	}
	private static class MeasurementInfo {
		// fields
		private final String name;
		private final Map<Double, SampleInfo> samples;
		
		// construction
		public MeasurementInfo(String name) {
			this.name = name;
			
			samples = new HashMap<>();
		}

		// properties
		public String getName() {
			return name;
		}
		public boolean isEmpty() {
			for (SampleInfo info : samples.values())
				if (!info.isEmpty())
					return false;
			
			return true;
		}
		public SampleInfo getSample(double position) {
			return samples.get(position);
		}
		public Map<Double, SampleInfo> getSamples() {
			return samples;
		}
		
		// methods
		public void addSample(double position, SampleInfo sample) {
			samples.put(position, sample);
		}
	}
	private static class SampleInfo {
		// fields
		private final String name;
		private final double thickness;
		private final List<String> runIds;
		
		// construction
		public SampleInfo(String name, double thickness) {
			this.name = name;
			this.thickness = thickness;
			
			runIds = new ArrayList<>();
		}

		// properties
		public String getName() {
			return name;
		}
		public double getThickness() {
			return thickness;
		}
		public boolean isEmpty() {
			return runIds.isEmpty();
		}
		public Iterable<String> getRunIds() {
			return runIds;
		}

		// methods
		public void addRun(String runId) {
			runIds.add(runId);
			Collections.sort(runIds, AlphanumericComparator.DEFAULT); 
		}
	}
}
