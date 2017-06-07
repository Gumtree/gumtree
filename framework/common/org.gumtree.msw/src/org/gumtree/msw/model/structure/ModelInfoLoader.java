package org.gumtree.msw.model.structure;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.xs.XSAnnotation;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSImplementation;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.gumtree.msw.model.IModelNodeInfo;
import org.gumtree.msw.model.ModelLoader;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;

public final class ModelInfoLoader {
	// finals
	public final static String MODEL_ROOT = "model-root";
	
	// fields
	private DocumentBuilder documentBuilder;
	private ComplexElementDeclaration rootElement;
	// types
	private Map<XSSimpleTypeDefinition, SimpleTypeDefinition> simpleTypeMap;
	private Map<XSComplexTypeDefinition, ComplexTypeDefinition> complexTypeMap;
	// particles
	private Map<XSModelGroup, ModelGroup> modelGroupMap;
	private Map<XSElementDeclaration, ElementDeclaration> elementMap;
	
	// construction
	public static IModelNodeInfo loadFromStream(InputStream xsd, String rootDefinitionName) {
		ModelInfoLoader instance = new ModelInfoLoader();
		if (instance.createDocumentBuilder() && instance.createRootElement(xsd, rootDefinitionName) && instance.validate())
			return new ModelNodeInfo(instance.rootElement);
		else
			return null;
	}
	private ModelInfoLoader() {
		simpleTypeMap = new HashMap<XSSimpleTypeDefinition, SimpleTypeDefinition>();
		complexTypeMap = new HashMap<XSComplexTypeDefinition, ComplexTypeDefinition>();

		modelGroupMap = new HashMap<XSModelGroup, ModelGroup>();
		elementMap = new HashMap<XSElementDeclaration, ElementDeclaration>();
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
	private boolean createRootElement(final InputStream xsd, String rootDefinitionName) {
		try {
			// create schema loader
			DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
			XSImplementation implementation = (XSImplementation)registry.getDOMImplementation("XS-Loader");
			XSLoader schemaLoader = implementation.createXSLoader(null);
			
			// set configuration
			DOMConfiguration config = schemaLoader.getConfig();
			config.setParameter("validate", Boolean.TRUE);
			config.setParameter("error-handler", new ErrorHandler());

			// parse xsd document
			XSModel model = schemaLoader.load(new LSInput() {
				@Override
				public String getSystemId() {
					return null;
				}
				@Override
				public void setSystemId(String systemId) {
				}
				@Override
				public String getStringData() {
					return null;
				}
				@Override
				public void setStringData(String stringData) {
				}
				@Override
				public String getPublicId() {
					return null;
				}
				@Override
				public void setPublicId(String publicId) {
				}
				@Override
				public String getEncoding() {
					return null;
				}
				@Override
				public void setEncoding(String encoding) {
				}
				@Override
				public Reader getCharacterStream() {
					return null;
				}
				@Override
				public void setCharacterStream(Reader characterStream) {
				}
				@Override
				public boolean getCertifiedText() {
					return false;
				}
				@Override
				public void setCertifiedText(boolean certifiedText) {
				}
				@Override
				public InputStream getByteStream() {
					return xsd;
				}
				@Override
				public void setByteStream(InputStream byteStream) {
				}
				@Override
				public String getBaseURI() {
					return null;
				}
				@Override
				public void setBaseURI(String baseURI) {
				}
			});
			List<ElementDeclaration> elements = process(model);

			rootElement = null;
			if (rootDefinitionName == null)
				for (ElementDeclaration element : elements) {
					if ((element instanceof ComplexElementDeclaration) && element.isRoot()) {
						if (rootElement == null)
							rootElement = (ComplexElementDeclaration)element;
						else {
							System.out.println("WARNING: multiple root elements found");
							break;
						}
					}
				}
			else {
				for (ElementDeclaration element : elements)
					if ((element instanceof ComplexElementDeclaration) && rootDefinitionName.equals(element.getName())) {
						if (rootElement == null)
							rootElement = (ComplexElementDeclaration)element;
						else {
							System.out.println("WARNING: multiple root elements found");
							break;
						}
					}
			}
			
			if (rootElement == null)
				System.out.println("WARNING: no root element found");
			else if (!ModelLoader.MSW_SCHEMA_NS_URI.equals(rootElement.getNamespace()))
				System.out.println("WARNING: root element has wrong namespace");

			return true;
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			return false;
		}
	}

	// model processing
	private List<ElementDeclaration> process(XSModel xsModel) {
		XSNamedMap elements = xsModel.getComponents(XSConstants.ELEMENT_DECLARATION);
		
		List<ElementDeclaration> result = new ArrayList<ElementDeclaration>(elements.getLength());
		for (int i = 0; i < elements.getLength(); i++) {
			ElementDeclaration element = process((XSElementDeclaration)elements.item(i), 1, 1);
			if (element != null)
				result.add(element);
		}
		return result;
	}
	// attributes
	private Attribute process(XSAttributeUse item) {
		XSAttributeDeclaration declaration = item.getAttrDeclaration();

		SimpleTypeDefinition typeDefinition = process(declaration.getTypeDefinition());
		return new Attribute(
				declaration.getName(),
				typeDefinition,
				item.getRequired(),
				ConstraintType.from(item.getConstraintType()),
				Value.from(item.getActualVCType(), item.getActualVC(), typeDefinition.getFacets()));
	}
	// types
	private SimpleTypeDefinition process(XSSimpleTypeDefinition item) {
		SimpleTypeDefinition result = simpleTypeMap.get(item);
		if (result == null) {
			result = new SimpleTypeDefinition(
					item.getName(),
					item.getNamespace(),
					XsTypeHelper.type(item.getBuiltInKind()),
					FacetList.create(item));
			
			simpleTypeMap.put(item, result);
		}
		return result;
	}
	private ComplexTypeDefinition process(XSComplexTypeDefinition item) {
		ComplexTypeDefinition result = complexTypeMap.get(item);
		if (result == null) {
			result = new ComplexTypeDefinition(
					item.getName(),
					item.getNamespace());

			// allow recursive references 
			complexTypeMap.put(item, result);
			
			// attributes
			XSObjectList objectList = item.getAttributeUses();
			List<Attribute> attributes = new ArrayList<Attribute>(objectList.getLength());
			for (int i = 0; i < objectList.getLength(); i++) {
				Attribute attribute = process((XSAttributeUse)objectList.item(i));
				if (attribute != null)
					attributes.add(attribute);
			}

			// particle
			Particle particle = null;
			switch (item.getContentType()) {
			case XSComplexTypeDefinition.CONTENTTYPE_EMPTY:
				break;
			case XSComplexTypeDefinition.CONTENTTYPE_ELEMENT:
				particle = process(item.getParticle());
				break;
			// not supported
			case XSComplexTypeDefinition.CONTENTTYPE_SIMPLE:
				System.out.println("WARNING: \"CONTENTTYPE_SIMPLE\" is not supported");
				break;
			case XSComplexTypeDefinition.CONTENTTYPE_MIXED:
				System.out.println("WARNING: \"CONTENTTYPE_MIXED\" is not supported");
				break;
			}
			
			// finalization
			if (!result.finalize(attributes, particle))
				System.out.println("WARNING: finalization failed");
		}
		return result;
	}
	// particles
	private Particle process(XSParticle item) {
		// particle needs to contribute
		if (item.getMaxOccurs() != 0) {
			XSTerm term = item.getTerm();
			switch (term.getType()) {
			case XSConstants.MODEL_GROUP:
				return process((XSModelGroup)term, item.getMinOccurs(), item.getMaxOccurs());
			case XSConstants.ELEMENT_DECLARATION:
				return process((XSElementDeclaration)term, item.getMinOccurs(), item.getMaxOccurs());
			}
		}
		return null;
	}
	private ModelGroup process(XSModelGroup item, int minOccurs, int maxOccurs) {
		ModelGroup result = modelGroupMap.get(item);
		if (result == null) {
			result = new ModelGroup(
					Compositor.from(item.getCompositor()),
					minOccurs,
					maxOccurs);
			
			// allow recursive references 
			modelGroupMap.put(item, result);

			// particles
			XSObjectList objectList = item.getParticles();
			List<Particle> particles = new ArrayList<Particle>(objectList.getLength());
			for (int i = 0; i < objectList.getLength(); i++) {
				Particle particle = process((XSParticle)objectList.item(i));
				if (particle != null)
					particles.add(particle);
			}

			// finalization
			if (!result.finalize(particles))
				System.out.println("WARNING: finalization failed");
		}
		return result;
	}
	private ElementDeclaration process(XSElementDeclaration item, int minOccurs, int maxOccurs) {
		ElementDeclaration temp = elementMap.get(item);
		if (temp != null)
			return temp;
		
		switch (item.getTypeDefinition().getTypeCategory()) {
		case XSTypeDefinition.SIMPLE_TYPE:
			SimpleElementDeclaration simpleElementDeclaration = new SimpleElementDeclaration(
					item.getName(),
					item.getNamespace(),
					minOccurs,
					maxOccurs,
					ConstraintType.from(item.getConstraintType()));
			
			// allow recursive references 
			elementMap.put(item, simpleElementDeclaration);

			// type
			SimpleTypeDefinition simpleType = process((XSSimpleTypeDefinition)item.getTypeDefinition());

			// finalization
			if (!simpleElementDeclaration.finalize(
					simpleType,
					Value.from(item.getActualVCType(), item.getActualVC(), simpleType.getFacets())))
				System.out.println("WARNING: finalization failed");

			return simpleElementDeclaration;
			
		case XSTypeDefinition.COMPLEX_TYPE:
			ComplexElementDeclaration complexElementDeclaration = new ComplexElementDeclaration(
					item.getName(),
					item.getNamespace(),
					minOccurs,
					maxOccurs,
					isRootElement(item));
			
			// allow recursive references 
			elementMap.put(item, complexElementDeclaration);
			
			// type
			ComplexTypeDefinition complexType = process((XSComplexTypeDefinition)item.getTypeDefinition());

			// finalization
			if (!complexElementDeclaration.finalize(complexType))
				System.out.println("WARNING: finalization failed");

			return complexElementDeclaration;
			
		default:
			return null;
		}
	}
	private boolean isRootElement(XSElementDeclaration item) {
		// <xsd:annotation><xsd:appinfo>model-root</xsd:appinfo></xsd:annotation>
		if (item.getScope() == XSConstants.SCOPE_GLOBAL) {
			XSAnnotation annotation = item.getAnnotation();
			if (annotation != null) {
				String xml = annotation.getAnnotationString();
				if (xml.indexOf(MODEL_ROOT) != -1) {
					try {
					    InputSource source = new InputSource(new StringReader(xml));
					    Document document = documentBuilder.parse(source);
						document.normalize();
						
						Element root = document.getDocumentElement();
						NodeList elements = root.getElementsByTagNameNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, "appinfo");
						
						for (int k = 0, n = elements.getLength(); k != n; k++) {
							String appinfo = elements.item(k).getTextContent();
							if ((appinfo != null) && (appinfo.indexOf(MODEL_ROOT) != -1) && MODEL_ROOT.equals(appinfo.trim()))
								return true;
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return false;
	}
	// validation
	public boolean validate() {
		if (rootElement == null)
			return false;
		
		ParticleValidator validator = new ParticleValidator();
		rootElement.accept(validator);
		return validator.successful();
	}
	
	// ErrorHandler
	private static class ErrorHandler implements DOMErrorHandler {
		// methods
		@Override
		public boolean handleError(DOMError error) {
			switch (error.getSeverity()) {
			case DOMError.SEVERITY_WARNING:
				System.out.println("[xs-warning]: " + error.getMessage());
				break;

			case DOMError.SEVERITY_ERROR:
				System.out.println("[xs-error]: " + error.getMessage());
				break;

			case DOMError.SEVERITY_FATAL_ERROR:
				System.out.println("[xs-fatal]: " + error.getMessage());
				break;
			}
			return true;
		}
	}
}
