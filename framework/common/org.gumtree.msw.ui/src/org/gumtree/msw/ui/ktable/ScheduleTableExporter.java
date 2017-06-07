package org.gumtree.msw.ui.ktable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.swt.graphics.RGB;
import org.gumtree.msw.schedule.ScheduledAspect;
import org.gumtree.msw.schedule.ScheduledNode;
import org.gumtree.msw.ui.ktable.ScheduleTableModel.AcquisitionDetail;
import org.gumtree.msw.ui.ktable.ScheduleTableModel.CellDefinition;
import org.gumtree.msw.ui.ktable.ScheduleTableModel.IAuxiliaryColumnInfo;
import org.gumtree.msw.ui.ktable.ScheduleTableModel.NodeInfo;
import org.gumtree.msw.ui.ktable.ScheduleTableModel.PropertyCellDefinition;
import org.gumtree.msw.ui.ktable.ScheduleTableModel.RowDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ScheduleTableExporter {
	// finals
	private static final String EMPTY_CELL_CONTENT = Character.toString((char)160); // Non-breaking space: &nbsp;
	private static final String BALLOT_BOX_CHECKED = "&#9745;"; // "&#9746;";
	private static final String BALLOT_BOX_UNCHECKED = "&#9744;";
	private static final String BALLOT_BOX_CHECKED_KEY = "[[CHECKED]]";
	private static final String BALLOT_BOX_UNCHECKED_KEY = "[[UNCHECKED]]";
	private static final String DISABLED_COLOR = "#808080";

	// construction
	private ScheduleTableExporter() {
	}
	
	// methods
	public static boolean save(ScheduleTableModel model, File file) {
		return save(model, file, true);
	}
	public static boolean save(ScheduleTableModel model, File file, boolean inclAuxiliaryColumns) {
		try (FileOutputStream stream = new FileOutputStream(file)) {
			try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream))) {
				writeln(writer, "<!DOCTYPE html>");
				writeln(writer, "<html>");
				writeln(writer, "<head>");
				writeln(writer, "<style>");
				writeln(writer, "body {");
				writeln(writer, "    font-family: Arial, Helvetica, sans-serif;");
				writeln(writer, "}");
				writeln(writer, "table, th, td {");
				writeln(writer, "    border-collapse: collapse;");
				writeln(writer, "    border: 1px solid black;");
				writeln(writer, "    font-size: 11px;");
				writeln(writer, "}");
				writeln(writer, "table {");
				writeln(writer, "    margin-top: 15px;");
				writeln(writer, "}");
				writeln(writer, "td, th {");
				writeln(writer, "    text-align: left;");
				writeln(writer, "    padding: 0.2rem 4px;");
				writeln(writer, "}");
				writeln(writer, "th {");
				writeln(writer, "    background-color: #DDDDDD;");
				writeln(writer, "}");
				writeln(writer, "</style>");
				writeln(writer, "</head>");
				writeln(writer, "<body>");
				writeln(writer, createTable(model, inclAuxiliaryColumns));
				writeln(writer, "</body>");
				writeln(writer, "</html>");
			}
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	private static void writeln(BufferedWriter writer, String line) throws IOException {
		writer.write(line);
		writer.newLine();
	}

	// helper
	private static String createTable(ScheduleTableModel model, boolean inclAuxiliaryColumns) throws ParserConfigurationException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();

		Document table = createDocument(model, inclAuxiliaryColumns, documentBuilder);
		
		return serialize(table);
	}
	private static Document createDocument(ScheduleTableModel model, boolean inclAuxiliaryColumns, DocumentBuilder documentBuilder) {
		Document document = documentBuilder.newDocument();
		document.setXmlStandalone(true);
		
		int initialDepth = 0;
		int dataColumnSpan = dataColumnSpan(model.getScheduler().getRoot(), model.getRowDefinitions(), initialDepth);
		
		Element root = document.createElement("table");
		serializeTableHeader(
				document,
				root,
				dataColumnSpan,
				model.getVisibleDetails(),
				model.getAuxiliaryColumns(),
				inclAuxiliaryColumns);
		serializeTableContent(
				document,
				root,
				dataColumnSpan,
				model.getVisibleDetails(),
				model.getAuxiliaryColumns(),
				model.getRowDefinitions(),
				model.getNodes(),
				model.getScheduler().getRoot(),
				inclAuxiliaryColumns,
				initialDepth);
		document.appendChild(decorateTable(document, root));
		
		return document;
	}
	private static String serialize(Document document) {
		try {
			// write to buffer
			Writer stringWriter = new StringWriter();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();			
			Transformer transformer = transformerFactory.newTransformer();
			
		    transformer.setOutputProperty(OutputKeys.METHOD, "html");
		    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		    //transformer.setOutputProperty(OutputKeys.ENCODING, "US-ASCII"); // is used to make degree symbol in AttenuationAngle work (for some reason transformer with UTF-8 doesn't seem to work properly)
		    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		    
			transformer.transform(
					new DOMSource(document),
					new StreamResult(stringWriter));

			return stringWriter
					.toString()
					.replace(BALLOT_BOX_UNCHECKED_KEY, BALLOT_BOX_UNCHECKED)
					.replace(BALLOT_BOX_CHECKED_KEY, BALLOT_BOX_CHECKED);
		}
		catch (TransformerException e) {
			e.printStackTrace();
			return null;
		}
	}
	private static Element decorateTable(Document document, Element table) {
		table.setAttribute("align", "center");
		table.setAttribute("border", "1");
		table.setAttribute("cellpadding", "2");
		table.setAttribute("cellspacing", "0");
		table.setAttribute("class", "xmlTable");
		table.setAttribute("style", "table-layout:fixed; width:100%; word-wrap:nowrap; white-space:nowrap");

		Element root = document.createElement("div");
		root.appendChild(table);
		
		return root;
	}
	// table
	private static void serializeTableHeader(Document document, Element root, int dataColumnSpan, Iterable<AcquisitionDetail> details, Iterable<IAuxiliaryColumnInfo> auxiliaryColumns, boolean inclAuxiliaryColumns) {
		Element row = document.createElement("tr");
		
		// acquisition tree
		Element dataColumn = document.createElement("th");
		dataColumn.setAttribute("colspan", String.valueOf(dataColumnSpan));
		dataColumn.setTextContent("Acquisition Tree");
		row.appendChild(dataColumn);
		
		// details
		for (AcquisitionDetail detail : details) {
			int detailColumnSpan = 0;
			for (CellDefinition cell : detail.getCells())
				detailColumnSpan += cell.getColumnSpan();
			
			Element column = document.createElement("th");
			column.setAttribute("colspan", String.valueOf(detailColumnSpan));
			column.setTextContent(detail.getTitle());
			row.appendChild(column);
		}
		// auxiliary
		if (inclAuxiliaryColumns)
			for (IAuxiliaryColumnInfo auxiliary : auxiliaryColumns) {
				Element column = document.createElement("th");
				column.setAttribute("style", String.format("width:%dpx", auxiliary.getWidth()));
				column.setTextContent(auxiliary.getTitle());
				row.appendChild(column);
			}
		
		root.appendChild(row);
	}
	private static void serializeTableContent(
			Document document,
			Element root,
			int dataColumnSpan,
			Iterable<AcquisitionDetail> details,
			Iterable<IAuxiliaryColumnInfo> auxiliaryColumns,
			Map<Class<?>, RowDefinition> rowDefinitions,
			Map<ScheduledNode, NodeInfo> nodeMap,
			ScheduledAspect aspect,
			boolean inclAuxiliaryColumns,
			int depth) {
		if (aspect != null)
			serializeTableContent(
					document,
					root,
					dataColumnSpan,
					details,
					auxiliaryColumns,
					rowDefinitions,
					nodeMap,
					aspect,
					aspect.getNode(),
					inclAuxiliaryColumns,
					depth);
	}
	private static void serializeTableContent(
			Document document,
			Element root,
			int dataColumnSpan,
			Iterable<AcquisitionDetail> details,
			Iterable<IAuxiliaryColumnInfo> auxiliaryColumns,
			Map<Class<?>, RowDefinition> rowDefinitions,
			Map<ScheduledNode, NodeInfo> nodeMap,
			ScheduledAspect aspect,
			ScheduledNode node,
			boolean inclAuxiliaryColumns,
			int depth) {
		RowDefinition rowDefinition = rowDefinitions.get(node.getSourceElement().getClass());

		int nextDepth = depth;
		if (rowDefinition != null) {
			nextDepth = depth + 1;
			
			List<CellDefinition> cells = rowDefinition.getCells();

			boolean sizable = false;
			Map<CellDefinition, Integer> columnSpans = new HashMap<>();
			for (CellDefinition cell : cells) {
				columnSpans.put(cell, cell.getColumnSpan());
				sizable |= cell.isSizable(); // check if any column size can be increased
			}

			int available = dataColumnSpan - (depth + rowDefinition.getColumnSpan() + (node.canBeDisabled() ? 1 : 0));
			if (sizable) {
				for (CellDefinition cell : cells)
					if (cell.isSizable()) {
						// increase column span
						columnSpans.put(cell, columnSpans.get(cell) + available);
						available = 0;
						break;
					}
			}

			boolean enabled = true;
			String bkgColor = getHtmlColorStr(rowDefinition.getColor());
			
			NodeInfo nodeInfo = nodeMap.get(node);
			if (nodeInfo != null && !nodeInfo.isTreeEnabled())
				enabled = false;
			
			Element row = document.createElement("tr");
			if (depth > 0) {
				Element spacer = document.createElement("td");
				if (!node.canBeDisabled()) {
					spacer.setAttribute("colspan", String.valueOf(depth));
					spacer.setTextContent(EMPTY_CELL_CONTENT);
				}
				else {
					spacer.setAttribute("colspan", String.valueOf(depth + 1));
					spacer.setAttribute("style", "font-size:14px; padding: 0px 4px; text-align: right");
					spacer.setTextContent(node.isEnabled() ? BALLOT_BOX_CHECKED_KEY : BALLOT_BOX_UNCHECKED_KEY);
				}
				row.appendChild(spacer);
			}
			else if (node.canBeDisabled()) {
				Element content = document.createElement("td");
				content.setAttribute("style", "font-size:14px; padding: 0px 4px; text-align: right");
				content.setTextContent(node.isEnabled() ? BALLOT_BOX_CHECKED_KEY : BALLOT_BOX_UNCHECKED_KEY);
				row.appendChild(content);
			}
			for (CellDefinition cell : cells) {
				Element content = document.createElement("td");
				content.setAttribute("bgcolor", bkgColor);
				content.setAttribute("colspan", String.valueOf(columnSpans.get(cell)));
				if (cell.getCellRenderer() != null) {
					int alignment = cell.getCellRenderer().getAlignment();
					if ((alignment & SWTX.ALIGN_HORIZONTAL_CENTER) == SWTX.ALIGN_HORIZONTAL_CENTER)
						content.setAttribute("style", "text-align:center");
					if ((alignment & SWTX.ALIGN_HORIZONTAL_RIGHT) == SWTX.ALIGN_HORIZONTAL_RIGHT)
						content.setAttribute("style", "text-align:right");
				}
				Object value = cell.getValue(node);
				if (value instanceof Boolean) {
					content.setAttribute("style", "font-size:14px; padding: 0px 4px; text-align: right");
					setTextContent(document, content, enabled && cell.isEnabled(node), Objects.equals(value, Boolean.TRUE) ? BALLOT_BOX_CHECKED_KEY : BALLOT_BOX_UNCHECKED_KEY);
				}
				else {
					setTextContent(document, content, enabled && cell.isEnabled(node), getStringOrEmpty(value));
				}
				row.appendChild(content);
			}
			if (available > 0) {
				Element spacer = document.createElement("td");
				spacer.setAttribute("bgcolor", bkgColor);
				spacer.setAttribute("colspan", String.valueOf(available));
				spacer.setTextContent(EMPTY_CELL_CONTENT);
				row.appendChild(spacer);
			}

			// details
			for (AcquisitionDetail detail : details) {
				for (CellDefinition cell : detail.getCells()) {
					Element content = document.createElement("td");
					content.setAttribute("bgcolor", bkgColor);
					content.setAttribute("colspan", String.valueOf(cell.getColumnSpan()));
					
					if ((cell instanceof PropertyCellDefinition) &&
							!node.getProperties().contains(((PropertyCellDefinition)cell).getProperty())) {
						content.setTextContent(EMPTY_CELL_CONTENT);
					}
					else {
						if (cell.getCellRenderer() != null) {
							int alignment = cell.getCellRenderer().getAlignment();
							if ((alignment & SWTX.ALIGN_HORIZONTAL_CENTER) == SWTX.ALIGN_HORIZONTAL_CENTER)
								content.setAttribute("style", "text-align:center");
							if ((alignment & SWTX.ALIGN_HORIZONTAL_RIGHT) == SWTX.ALIGN_HORIZONTAL_RIGHT)
								content.setAttribute("style", "text-align:right");
						}
						Object value = cell.getValue(node);
						if (value instanceof Boolean) {
							content.setAttribute("style", "font-size:14px; padding: 0px 4px; text-align: center");
							setTextContent(document, content, enabled && cell.isEnabled(node), Objects.equals(value, Boolean.TRUE) ? BALLOT_BOX_CHECKED_KEY : BALLOT_BOX_UNCHECKED_KEY);
						}
						else {
							setTextContent(document, content, enabled && cell.isEnabled(node), getStringOrEmpty(value));
						}
					}
					row.appendChild(content);
				}
			}
			
			// auxiliary
			if (inclAuxiliaryColumns)
				for (IAuxiliaryColumnInfo auxiliary : auxiliaryColumns) {
					Element content = document.createElement("td");
					content.setAttribute("bgcolor", bkgColor);
					if (nodeInfo != null)
						setTextContent(document, content, enabled, getStringOrEmpty(auxiliary.getExportableContent(nodeInfo)));
					else
						content.setTextContent(EMPTY_CELL_CONTENT);
					row.appendChild(content);
				}
			
			root.appendChild(row);
		}
		
		if (node.isAspectLeaf())
			serializeTableContent(document, root, dataColumnSpan, details, auxiliaryColumns, rowDefinitions, nodeMap, aspect.getLinkAt(node), inclAuxiliaryColumns, nextDepth);
		else
			for (ScheduledNode subNode : node.getNodes())
				if (subNode.isThisVisible())
					serializeTableContent(document, root, dataColumnSpan, details, auxiliaryColumns, rowDefinitions, nodeMap, aspect, subNode, inclAuxiliaryColumns, nextDepth);
	}
	private static int dataColumnSpan(ScheduledAspect aspect, Map<Class<?>, RowDefinition> rowDefinitions, int depth) {
		if (aspect != null)
			return dataColumnSpan(aspect, aspect.getNode(), rowDefinitions, depth);
		
		return 0;
	}
	private static int dataColumnSpan(ScheduledAspect aspect, ScheduledNode node, Map<Class<?>, RowDefinition> rowDefinitions, int depth) {
		RowDefinition rowDefinition = rowDefinitions.get(node.getSourceElement().getClass());
		
		int nextDepth = depth;
		int result = 0;
		if (rowDefinition != null) {
			nextDepth = depth + 1;
			result = depth + rowDefinition.getColumnSpan() + (node.canBeDisabled() ? 1 : 0);
		}

		if (node.isAspectLeaf())
			result = Math.max(result, dataColumnSpan(aspect.getLinkAt(node), rowDefinitions, nextDepth));
		else
			for (ScheduledNode subNode : node.getNodes())
				if (subNode.isThisVisible())
					result = Math.max(result, dataColumnSpan(aspect, subNode, rowDefinitions, nextDepth));
		
		return result;
	}
	private static String getStringOrEmpty(Object value) {
		if (value == null)
			return EMPTY_CELL_CONTENT;
			
		String str = String.valueOf(value);
		if ((str == null) || str.isEmpty())
			return EMPTY_CELL_CONTENT;
		
		return str;
	}
	private static String getHtmlColorStr(RGB color) {
		return
				'#' +
				Integer.toHexString(color.red) + 
				Integer.toHexString(color.green) + 
				Integer.toHexString(color.blue);
	}
	private static void setTextContent(Document document, Element content, boolean enabled, String value) {
		if (enabled) {
			content.setTextContent(value);
		}
		else {
			Element text = document.createElement("font");
			text.setAttribute("color", DISABLED_COLOR);
			text.setTextContent(value);
			content.appendChild(text);
		}
	}
}
