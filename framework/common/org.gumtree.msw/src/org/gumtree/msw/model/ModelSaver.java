package org.gumtree.msw.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ModelSaver {
	// fields
	private ModelNode rootNode;
	private DocumentBuilder documentBuilder;
	
	// construction
	public ModelSaver(IModelNode rootNode) {
		if (rootNode instanceof ModelNode)
			this.rootNode = (ModelNode)rootNode;

		try {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			
			builderFactory.setNamespaceAware(true);
			documentBuilder = builderFactory.newDocumentBuilder();
			
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();
			this.rootNode = null;
		}
	}
	
	// methods
	public boolean serializeTo(Writer writer) {
		if (rootNode == null)
			return false;
		
		Document document = documentBuilder.newDocument();
		document.setXmlStandalone(true);
		document.appendChild(document.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"msw.xsl\""));
		document.appendChild(serialize(document, rootNode, new SerializationIdMapper()));

		// write to buffer
		Writer stringWriter = new StringWriter();
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();			
			Transformer transformer = transformerFactory.newTransformer();
		    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		    //transformer.setOutputProperty(OutputKeys.ENCODING, "US-ASCII"); // is used to make '°' in AttenuationAngle work (for some reason transformer with UTF-8 doesn't seem to work properly)
		    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		    // create string
			transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
		}
		catch (TransformerException e) {
			e.printStackTrace();
			return false;
		}
		
		// insert proper line endings
		BufferedReader reader = new BufferedReader(new StringReader(stringWriter.toString()));
		try {
			final String newLine = System.getProperty("line.separator");
			
			String line = reader.readLine();
			if (line == null)
				return false;
			
			writer.append(line.replace("><", '>' + newLine + '<'));
			while (null != (line = reader.readLine())) {
				writer.append(newLine);
				writer.append(line);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}		
		
		return true;
	}
	
	// helper
	private static Element serialize(Document document, ModelNode node, IRefIdMapper idMapper) {
		Element result = node.getNodeInfo().serialize(document, idMapper);
		
		for (String elementName : node.getListElements())
			result.appendChild(serialize(document, node.getSub(elementName), idMapper));
			
		return result;
	}
}
