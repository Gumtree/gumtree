package org.gumtree.msw.model;

import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.gumtree.msw.IRefIdProvider;
import org.gumtree.msw.model.structure.ModelInfoLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ModelLoader {
	// finals
	public final static String MSW_SCHEMA_NS_URI = "http://www.gumtree.org/msw";
	
	// fields
	private Validator validator;
	private DocumentBuilder documentBuilder;
	private IModelNodeInfo rootNodeInfo;
	private IModelNode rootNode;
	// source
	private final DataSource xsdSource;
	private final DataSource xmlSource;
	
	// construction
	public static IModelNode load(IRefIdProvider idProvider, DataSource xsdSource, DataSource xmlSource) {
		return load(idProvider, xsdSource, xmlSource, null);
	}
	public static IModelNode load(IRefIdProvider idProvider, DataSource xsdSource, DataSource xmlSource, String rootDefinitionName) {
		ModelLoader loader = new ModelLoader(xsdSource, xmlSource);
		
		if (!loader.createValidator())
			return null;

		if (!loader.createDocumentBuilder())
			return null;

		if (!loader.createRootNodeInfo(rootDefinitionName))
			return null;

		if (!loader.createModelInstance(idProvider))
			return null;
		
		return loader.getRootNode();
	}
	private ModelLoader(DataSource xsdSource, DataSource xmlSource) {
		this.xsdSource = xsdSource;
		this.xmlSource = xmlSource;
	}

	// properties
	public IModelNode getRootNode() {
		return rootNode;
	}
	
	// methods
	private boolean createValidator() {
		try {
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

			StreamSource schemaSource = new StreamSource(xsdSource.createStream());
			validator = schemaFactory.newSchema(schemaSource).newValidator();
			return true;
		}
		catch (SAXException e) {
			e.printStackTrace();
			return false;
		}
	}
	private boolean createDocumentBuilder() {
		try {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			
			builderFactory.setNamespaceAware(true);
			documentBuilder = builderFactory.newDocumentBuilder();
			return true;
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();
			return false;
		}
	}
	private boolean createRootNodeInfo(String rootDefinitionName) {
		rootNodeInfo = ModelInfoLoader.loadFromStream(xsdSource.createStream(), rootDefinitionName);
		return rootNodeInfo != null;
	}
	private boolean createModelInstance(IRefIdProvider idProvider) {
		try {
			// validate xml
			StreamSource source = new StreamSource(xmlSource.createStream());
			validator.validate(source);
						
			// load xml
		    Document document = documentBuilder.parse(xmlSource.createStream());
			document.normalize();
			
			IRefIdMapper idMapper = new DeserializationIdMapper(idProvider);
			Element rootElement = document.getDocumentElement();
			if (!rootNodeInfo.deserialize(rootElement, idMapper))
				return false;

			// node name
			String nodeName = rootNodeInfo.getName();
			IModelNodePropertyInfo idInfo = rootNodeInfo.getProperty(IModelNode.ID);
			if (idInfo != null)
				nodeName += idInfo.get().toString();
			
			ModelNode node = new ModelNode(rootNodeInfo, nodeName);
			createSubNodes(node, rootElement, idMapper);
			rootNode = node;
			return true;
		}
		catch (SAXException | IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	private static void createSubNodes(ModelNode owner, Element element, IRefIdMapper idMapper) {
		IModelNodeInfo nodeInfo = owner.getNodeInfo();
		
		NodeList childNodes = element.getChildNodes();
		for (int i = 0; i != childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element)childNode;
				
				IModelNodeInfo subNodeInfo = nodeInfo.loadSubNodeInfo(childElement.getTagName());
				if (subNodeInfo == null) {
					// SetupScript is an xml node but it doesn't correlate to a ModelNode
				}
				else if (subNodeInfo.deserialize(childElement, idMapper)) {
					String nodeName = subNodeInfo.getName();
					IModelNodePropertyInfo idInfo = subNodeInfo.getProperty(IModelNode.ID);
					if (idInfo != null)
						nodeName += idInfo.get().toString();
					
					if (owner.appendChild(subNodeInfo, nodeName))
						createSubNodes(owner.getSub(nodeName), childElement, idMapper);
				}
			}
		}
	}
}
