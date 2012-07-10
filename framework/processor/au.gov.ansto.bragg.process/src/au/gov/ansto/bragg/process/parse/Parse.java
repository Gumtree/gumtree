/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
*******************************************************************************/
package au.gov.ansto.bragg.process.parse;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import au.gov.ansto.bragg.process.configuration.AgentConfiguration;
import au.gov.ansto.bragg.process.configuration.AgentConfiguration_;
import au.gov.ansto.bragg.process.configuration.CompositeProcessorConfiguration;
import au.gov.ansto.bragg.process.configuration.CompositeProcessorConfiguration_;
import au.gov.ansto.bragg.process.configuration.ConnectorConfiguration;
import au.gov.ansto.bragg.process.configuration.ConnectorConfiguration_;
import au.gov.ansto.bragg.process.configuration.FrameworkConfiguration;
import au.gov.ansto.bragg.process.configuration.FrameworkConfiguration_;
import au.gov.ansto.bragg.process.configuration.InConfiguration;
import au.gov.ansto.bragg.process.configuration.InConfiguration_;
import au.gov.ansto.bragg.process.configuration.OutConfiguration;
import au.gov.ansto.bragg.process.configuration.OutConfiguration_;
import au.gov.ansto.bragg.process.configuration.PortConfiguration;
import au.gov.ansto.bragg.process.configuration.ProcessorConfiguration;
import au.gov.ansto.bragg.process.configuration.ProcessorConfiguration_;
import au.gov.ansto.bragg.process.configuration.SinkConfiguration;
import au.gov.ansto.bragg.process.configuration.SinkConfiguration_;
import au.gov.ansto.bragg.process.configuration.SourceConfiguration;
import au.gov.ansto.bragg.process.configuration.SourceConfiguration_;
import au.gov.ansto.bragg.process.configuration.VarConfiguration;
import au.gov.ansto.bragg.process.configuration.VarConfiguration_;
import au.gov.ansto.bragg.process.factory.exception.NullConfigurationPointerException;

/**
 * @author nxi
 *
 */
public class Parse {

	public static final long serialVersionUID = 1L;

	/*
	 * Return a Document handle from a given URL address
	 */
	public static Document readFile(URL fileURL) throws DocumentException {
		SAXReader fileHandle = new SAXReader();
		Document documentHandle = fileHandle.read(fileURL);
		
		return documentHandle;
	}

	/*
	 * Return a Document handle from a given address string
	 */
	public static Document readFile(String fileName) throws DocumentException {
		SAXReader fileHandle = new SAXReader();
		Document documentHandle = fileHandle.read(fileName);
		return documentHandle;
	}

	/*
	 * Return a Document handle from a given file address
	 */
	public static Document readFile(File fileName) throws DocumentException  {
		SAXReader fileHandle = new SAXReader();
		Document documentHandle = null;
		try{
			documentHandle = fileHandle.read(fileName);
		}catch (DocumentException e) {
			// TODO: handle exception
			try {
				documentHandle = fileHandle.read(fileName.toURL());
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				throw new DocumentException(e1);
			}
		}
		return documentHandle;
	}

	/*
	 * Return an algorithm name from a given file address
	 */
	public static String getAlgorithmName(File filename) throws DocumentException{
//		Document documentHandle = readFile(filename);

		String algorithmName = null;
		return algorithmName;
	}

	public static List<ConfigurationItem> parseConfiguration(File filename) throws DocumentException{
		List<ConfigurationItem> propertyList = new LinkedList<ConfigurationItem>();
		Document document = readFile(filename);
		Element rootElement = document.getRootElement();
//		List<?> list = rootElement.selectNodes("//extension");
//		System.out.println(list.toString());
		for (Iterator<?> iter = rootElement.elementIterator("extension"); iter.hasNext();){
			Element item = (Element)iter.next();
			if (item.getName().equals("extension") && item.attributeValue("point").matches("au.gov.ansto.bragg.cicada.configuration")){
				ConfigurationItem configuration = new ConfigurationItem(Integer.valueOf(getAttribute(item, "id")),
						getAttribute(item, "name"), getAttribute(item, "version"),
						getAttribute(item, "class"), getAttribute(item, "default"));
				propertyList.add(configuration);
			}
		}
		return propertyList;
	}

	public static String getAttribute(Element item, String name){
		String value = "null";
		if (item.attributeValue(name) != null) value = item.attributeValue(name); 
		return value;
	}
	/*
	 * A static method of parsing a receipe file print the receipe file in configuration format
	 *
	public static void parseFile(Document XMLfile, Element rootElement) throws DocumentException{
		List<?> list = rootElement.selectNodes("*");
		String groupName;
		int numberOfAttribute;
		for (Iterator<?> iter = list.iterator(); iter.hasNext();){
			Element item = (Element)iter.next();
			boolean subGroup = false;
			numberOfAttribute = item.attributeCount();
			groupName = item.getName().toString();
			System.out.println("Group item: " + groupName);
			if (numberOfAttribute > 0){
				String attributeName = null, attributeValue = null;
				for (Iterator<?> attributeIter = item.attributeIterator(); attributeIter.hasNext();){
					DefaultAttribute attributeItem = (DefaultAttribute) attributeIter.next();
					attributeName = attributeItem.getName();
					attributeValue = attributeItem.getValue();
					System.out.println("Atrribute item: Name="+ attributeName + " Value="+attributeValue);
				}
				System.out.println();
			}
			else System.out.println();
			if (groupName.endsWith("s") || groupName.matches("processor")) subGroup = true;
			if (subGroup) {
				System.out.println("Subgroup");
				parseFile(XMLfile, item);
			}
		}
	}
	 */

	/*
	 * A static method of parsing a XML receipe file and create a framework configuration object
	 */
	public static FrameworkConfiguration parseFile(Element rootElement) 
	throws NullConfigurationPointerException, DocumentException{
		String frameworkName = rootElement.attributeValue("name");
		FrameworkConfiguration framework = new FrameworkConfiguration_(frameworkName);
//		List<?> list = rootElement.selectNodes("*");
//		String groupName;
//		int numberOfAttribute;
		for (Iterator<?> iter = rootElement.elementIterator("source"); iter.hasNext();){
//			for (Iterator<?> iter = list.iterator(); iter.hasNext();){
			Element item = (Element)iter.next();
//			System.out.println(item.toString());
//			groupName = item.getName().toString();
			SourceConfiguration sourceConfiguration = getSourceConfiguration(item, frameworkName);
			framework.addSourceConfiguration(sourceConfiguration);
		}

		for (Iterator<?> iter = rootElement.elementIterator("sink"); iter.hasNext();){
			Element item = (Element)iter.next();
			SinkConfiguration sinkConfiguration = getSinkConfiguration(item, frameworkName);
			framework.addSinkConfiguration(sinkConfiguration);
		}
		for (Iterator<?> iter = rootElement.elementIterator("processor"); iter.hasNext();){
			Element item = (Element)iter.next();
			ProcessorConfiguration processorConfiguration = getProcessorConfiguration(item, frameworkName);
			framework.addProcessorConfiguration(processorConfiguration);
		}
		for (Iterator<?> iter = rootElement.elementIterator("composite_processor"); iter.hasNext();){
			Element item = (Element)iter.next();
			CompositeProcessorConfiguration processorConfiguration = getCompositeProcessorConfiguration(item, frameworkName);
			framework.addProcessorConfiguration(processorConfiguration);
		}
		for (Iterator<?> iter = rootElement.elementIterator("ins"); iter.hasNext();){
			Element item = (Element)iter.next();
			framework.setInConfigurationList(getInConfigurationList(item, frameworkName));
		}
		for (Iterator<?> iter = rootElement.elementIterator("outs"); iter.hasNext();){
			Element item = (Element)iter.next();
			framework.setOutConfigurationList(getOutConfigurationList(item, frameworkName));
		}
		for (Iterator<?> iter = rootElement.elementIterator("vars"); iter.hasNext();){
			Element item = (Element)iter.next();
			framework.setVarConfigurationList(getVarConfigurationList(item, frameworkName));
		}
		for (Iterator<?> iter = rootElement.elementIterator("connectors"); iter.hasNext();){
			Element item = (Element)iter.next();
			framework.setConnectorConfigurationList(getConnectorConfigurationList(item, frameworkName));
		}
		for (Iterator<?> iter = rootElement.elementIterator("agents"); iter.hasNext();){
			Element item = (Element)iter.next();
			framework.setAgentConfigurationList(getAgentConfigurationList(item));
		}
		return framework;
	}

	protected static CompositeProcessorConfiguration getCompositeProcessorConfiguration(final Element item, final String frameworkName) 
	throws NullConfigurationPointerException, DocumentException{
//		int id = Integer.parseInt(item.attributeValue("id"));
		String name = item.attributeValue("name");
		String classType = item.attributeValue("class");
		CompositeProcessorConfiguration_ compositeProcessorConfiguration = new CompositeProcessorConfiguration_(name, frameworkName, classType);	
		String version = item.attributeValue("version");
		if (version != null) compositeProcessorConfiguration.setVersionNumber(version);
		else compositeProcessorConfiguration.setVersionNumber("0.0.0");
//		List<?> list = item.selectNodes("*");
//		String groupName;
//		for (Iterator<?> iter = list.iterator(); iter.hasNext();){
		for (Iterator<?> iter = item.elementIterator("processor"); iter.hasNext();){
			Element subItem = (Element)iter.next();
//			System.out.println(subItem.toString());
//			if (groupName == "processor"){
			ProcessorConfiguration processorConfiguration = getProcessorConfiguration(subItem, name);
			compositeProcessorConfiguration.addProcessorConfiguration(processorConfiguration);
		}
		for (Iterator<?> iter = item.elementIterator("composite_processor"); iter.hasNext();){
			Element subItem = (Element)iter.next();
			CompositeProcessorConfiguration processorConfiguration = getCompositeProcessorConfiguration(subItem, name);
			compositeProcessorConfiguration.addProcessorConfiguration(processorConfiguration);
		}
		for (Iterator<?> iter = item.elementIterator("ins"); iter.hasNext();){
			Element subItem = (Element)iter.next();
			compositeProcessorConfiguration.setInConfigurationList(getInConfigurationList(subItem, name));
		}
		for (Iterator<?> iter = item.elementIterator("outs"); iter.hasNext();){
			Element subItem = (Element)iter.next();
			compositeProcessorConfiguration.setOutConfigurationList(getOutConfigurationList(subItem, name));
		}
		for (Iterator<?> iter = item.elementIterator("vars"); iter.hasNext();){
			Element subItem = (Element)iter.next();
			compositeProcessorConfiguration.setVarConfigurationList(getVarConfigurationList(subItem, name));
		}
		for (Iterator<?> iter = item.elementIterator("connectors"); iter.hasNext();){
			Element subItem = (Element)iter.next();
			compositeProcessorConfiguration.setConnectorConfigurationList(getConnectorConfigurationList(subItem, name));
		}
		return compositeProcessorConfiguration;
	}

	/*
	public static List<ProcessorConfiguration_> getCompositeConfiguration(Element rootElement) throws NullConfigurationPointerException{
		List list = rootElement.selectNodes("*");
		String groupName;
		int numberOfAttribute;
		List<ProcessorConfiguration_> configurationList = new LinkedList();
		configurationList = new ArrayList();

		for (Iterator iter = list.iterator(); iter.hasNext();){
			Element item = (Element)iter.next();
			boolean subGroup = false;
			numberOfAttribute = item.attributeCount();
			groupName = item.getName().toString();			
			if (groupName == "processor"){
				ProcessorConfiguration_ processorConfiguration = null;
				int id = Integer.parseInt(item.attributeValue("id"));
				processorConfiguration = getProcessorConfiguration(item, id);
				configurationList.add(processorConfiguration);
			}
		}
		if (configurationList == null) throw new NullConfigurationPointerException("list");
		return configurationList;
	}
	 */

	protected static SourceConfiguration getSourceConfiguration(final Element item, final String parentName) 
	throws DocumentException{
		SourceConfiguration configuration = new SourceConfiguration_(item.attributeValue("name"), parentName, item.attributeValue("class"), item.attributeValue("method"));

//		List<?> subList = item.selectNodes("*");
//		String portsName = null;
		List<InConfiguration> inConfigurationList = new LinkedList<InConfiguration>();
		List<OutConfiguration> outConfigurationList = new LinkedList<OutConfiguration>();
		List<VarConfiguration> varConfigurationList = new LinkedList<VarConfiguration>();

//		for (Iterator<?> subIter = subList.iterator(); subIter.hasNext();){
		for (Iterator<?> iter = item.elementIterator("ins"); iter.hasNext();){
			Element subItem = (Element) iter.next(); 
//			portsName = subItem.getName().toString();
			inConfigurationList = getInConfigurationList(subItem, configuration.getName());
		}
		for (Iterator<?> iter = item.elementIterator("outs"); iter.hasNext();){
			Element subItem = (Element) iter.next(); 
			outConfigurationList = getOutConfigurationList(subItem, configuration.getName());
		}
		for (Iterator<?> iter = item.elementIterator("vars"); iter.hasNext();){
			Element subItem = (Element) iter.next(); 
			varConfigurationList = getVarConfigurationList(subItem, configuration.getName()); 
		}

		configuration.setInConfigurationList(inConfigurationList);
		configuration.setOutConfigurationList(outConfigurationList);
		configuration.setVarConfigurationList(varConfigurationList);
//		System.out.println(configuration.toString());
		return configuration;
	}

	protected static SinkConfiguration getSinkConfiguration(final Element item, final String parentName) 
	throws DocumentException{
		String name = item.attributeValue("name");
		String autoPlot = item.attributeValue("autoplot");
		String isDefaultSink = item.attributeValue("defaultsink");
		SinkConfiguration configuration = new SinkConfiguration_(name, parentName, 
				autoPlot, isDefaultSink);

//		List<?> subList = item.selectNodes("*");
//		String portsName = null;
		List<InConfiguration> inConfigurationList = new LinkedList<InConfiguration>();
		List<OutConfiguration> outConfigurationList = new LinkedList<OutConfiguration>();
		List<VarConfiguration> varConfigurationList = new LinkedList<VarConfiguration>();

//		for (Iterator<?> subIter = subList.iterator(); subIter.hasNext();){
		for (Iterator<?> iter = item.elementIterator("ins"); iter.hasNext();){
			Element subItem = (Element) iter.next(); 
//			portsName = subItem.getName().toString();
			inConfigurationList = getInConfigurationList(subItem, name);
		}
		for (Iterator<?> iter = item.elementIterator("outs"); iter.hasNext();){
			Element subItem = (Element) iter.next(); 
			outConfigurationList = getOutConfigurationList(subItem, name);
		}
		for (Iterator<?> iter = item.elementIterator("vars"); iter.hasNext();){
			Element subItem = (Element) iter.next(); 
			varConfigurationList = getVarConfigurationList(subItem, name); 
		}

		configuration.setInConfigurationList(inConfigurationList);
		configuration.setOutConfigurationList(outConfigurationList);
		configuration.setVarConfigurationList(varConfigurationList);
//		System.out.println(configuration.toString());
		return configuration;
	}

	protected static ProcessorConfiguration getProcessorConfiguration(final Element item, final String parentName) 
	throws DocumentException{
		String name = item.attributeValue("name");
		ProcessorConfiguration configuration = new ProcessorConfiguration_(name, parentName, item.attributeValue("class"), item.attributeValue("method"));
		String version = item.attributeValue("version");
		if (version != null) configuration.setVersionNumber(version);
		else configuration.setVersionNumber("0.0.0");
//		List<?> subList = item.selectNodes("*");
//		String portsName = null;
		List<InConfiguration> inConfigurationList = new LinkedList<InConfiguration>();
		List<OutConfiguration> outConfigurationList = new LinkedList<OutConfiguration>();
		List<VarConfiguration> varConfigurationList = new LinkedList<VarConfiguration>();

//		for (Iterator<?> subIter = subList.iterator(); subIter.hasNext();){
		for (Iterator<?> iter = item.elementIterator("ins"); iter.hasNext();){
			Element subItem = (Element) iter.next(); 
//			portsName = subItem.getName().toString();
			inConfigurationList = getInConfigurationList(subItem, name);
		}
		for (Iterator<?> iter = item.elementIterator("outs"); iter.hasNext();){
			Element subItem = (Element) iter.next(); 
			outConfigurationList = getOutConfigurationList(subItem, name);
		}
		for (Iterator<?> iter = item.elementIterator("vars"); iter.hasNext();){
			Element subItem = (Element) iter.next(); 
			varConfigurationList = getVarConfigurationList(subItem, name); 
		}

		configuration.setInConfigurationList(inConfigurationList);
		configuration.setOutConfigurationList(outConfigurationList);
		configuration.setVarConfigurationList(varConfigurationList);
//		System.out.println(configuration.toString());
		return configuration;
	}

	protected static List<InConfiguration> getInConfigurationList(final Element element, final String parentName) 
	throws DocumentException{
//		List<?> list = element.selectNodes("*");
		List<InConfiguration> portList = new LinkedList<InConfiguration>();
		for (Iterator<?> iter = element.elementIterator(); iter.hasNext();){
			Element item = (Element) iter.next();
			portList.add((InConfiguration) getPortConfiguration(item, "in", parentName));
		}
		return portList;
	}

	protected static List<OutConfiguration> getOutConfigurationList(final Element element, final String parentName)
	throws DocumentException{
//		List<?> list = element.selectNodes("*");
		List<OutConfiguration> portList = new LinkedList<OutConfiguration>();
		for (Iterator<?> iter = element.elementIterator(); iter.hasNext();){
			Element item = (Element) iter.next();
			portList.add((OutConfiguration) getPortConfiguration(item, "out", parentName));
		}
		return portList;
	}

	protected static List<VarConfiguration> getVarConfigurationList(final Element element, final String parentName) 
	throws DocumentException{
		List<VarConfiguration> portList = new LinkedList<VarConfiguration>();
		for (Iterator<?> iter = element.elementIterator(); iter.hasNext();){
			Element item = (Element) iter.next();
			portList.add((VarConfiguration) getPortConfiguration(item, "var", parentName));
		}
		return portList;
	}

	protected static List<ConnectorConfiguration> getConnectorConfigurationList(final Element element, final String parentName) 
	throws DocumentException{
		List<ConnectorConfiguration> connectorList = new LinkedList<ConnectorConfiguration>();
		for (Iterator<?> iter = element.elementIterator(); iter.hasNext();){
			Element item = (Element) iter.next();
			connectorList.add((ConnectorConfiguration) getConnectorConfiguration(item, parentName));
		}
		return connectorList;
	}

	protected static List<AgentConfiguration> getAgentConfigurationList(final Element element){
		List<AgentConfiguration> agentConfigurationList = new ArrayList<AgentConfiguration>();
		for (Iterator<?> iter = element.elementIterator(); iter.hasNext();){
			Element item = (Element) iter.next();
			agentConfigurationList.add((AgentConfiguration) getAgentConfiguration(item));
		}
		return agentConfigurationList;
	}

	protected static PortConfiguration getPortConfiguration(final Element element, final String patternName, final String parentName) throws DocumentException{
		PortConfiguration configuration = null;
		String name, type;
		int dimension = 0;
//		id = Integer.parseInt(element.attributeValue("id"));
		name = element.attributeValue("name");
		type = element.attributeValue("type");
		try{
			dimension = Integer.parseInt(element.attributeValue("dimension"));
		}catch(Exception ex){
		}

		if (patternName == "in") 
			configuration = new InConfiguration_(name, dimension, type, parentName); 
		if (patternName == "out")  
			configuration = new OutConfiguration_(name, dimension, type, parentName);
		if (patternName == "var") {
			String defaultValue = element.attributeValue("default_value");
			String label = element.attributeValue("label");
//			System.out.println("var_" + id +": ");
			int ownerID = 0;
			if (element.attributeValue("owner") != null)
				ownerID = Integer.parseInt(element.attributeValue("owner"));
			String max = element.attributeValue("max");
			String min = element.attributeValue("min");
			String usage = element.attributeValue("usage");
			String options = element.attributeValue("option");
			String UIWidth = element.attributeValue("UIwidth");
			Map<String, String> attributeMap = new HashMap<String, String>();
			List<?> attributes = element.attributes();
			for (Object object : attributes){
				if (object instanceof Attribute){
					Attribute attribute = (Attribute) object;
					attributeMap.put(attribute.getName(), attribute.getStringValue());
				}
			}
			configuration = new VarConfiguration_(name, dimension, type, parentName, 
					defaultValue, ownerID, max, min, usage, label, options, UIWidth, attributeMap);
		}
		return configuration;
	}

	protected static ConnectorConfiguration getConnectorConfiguration(final Element element, final String parentName) throws DocumentException{
		ConnectorConfiguration configuration = null;
		String name = "*connector*";
//		int id;
		String producerID = null, consumerID = null;
//		id = Integer.parseInt(element.attributeValue("id"));
//		name = element.attributeValue("name");
		if (element.attributeValue("producer") != null)
			producerID = element.attributeValue("producer");
		if (element.attributeValue("consumer") != null)
			consumerID = element.attributeValue("consumer");
		configuration = new ConnectorConfiguration_(name, producerID, consumerID);
		return configuration;
	}

	protected static AgentConfiguration getAgentConfiguration(final Element element){
		String name, principal, uiLabel;
//		int id = 0;
		String pName = null;
//		id = Integer.parseInt(element.attributeValue("id"));
		pName = element.attributeValue("pname");
		name = element.attributeValue("name");
		principal = element.attributeValue("principal");
		uiLabel = element.attributeValue("label");
		return new AgentConfiguration_(name, principal, pName, uiLabel);
	}
}
