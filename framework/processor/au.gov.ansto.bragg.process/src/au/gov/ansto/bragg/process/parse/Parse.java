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
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

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
	public static Document readFile(URL fileURL) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		Document documentHandle = builder.build(fileURL);

		return documentHandle;
	}

	/*
	 * Return a Document handle from a given address string
	 */
	public static Document readFile(String fileName) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		Document documentHandle = builder.build(fileName);
		return documentHandle;
	}

	/*
	 * Return a Document handle from a given file address
	 */
	public static Document readFile(File fileName) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		Document documentHandle = builder.build(fileName);
		return documentHandle;
	}

	/*
	 * Return an algorithm name from a given file address
	 */
	public static String getAlgorithmName(File filename) throws JDOMException, IOException {
		//		Document documentHandle = readFile(filename);

		String algorithmName = null;
		return algorithmName;
	}

	public static List<ConfigurationItem> parseConfiguration(File filename) throws JDOMException, IOException {
		List<ConfigurationItem> propertyList = new LinkedList<ConfigurationItem>();
		Document document = readFile(filename);
		Element rootElement = document.getRootElement();
		//		List<?> list = rootElement.selectNodes("//extension");
		//		System.out.println(list.toString());
		for (Element item : rootElement.getChildren("extension")) {
			if (item.getAttributeValue("point").matches("au.gov.ansto.bragg.cicada.configuration")) {
				ConfigurationItem configuration = new ConfigurationItem(Integer.valueOf(getAttribute(item, "id")),
						getAttribute(item, "name"), getAttribute(item, "version"), getAttribute(item, "class"),
						getAttribute(item, "default"));
				propertyList.add(configuration);
			}
		}
		return propertyList;
	}

	public static String getAttribute(Element item, String name) {
		String value = "null";
		if (item.getAttributeValue(name) != null)
			value = item.getAttributeValue(name);
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
			throws NullConfigurationPointerException, JDOMException, IOException {
		String frameworkName = rootElement.getAttributeValue("name");
		FrameworkConfiguration framework = new FrameworkConfiguration_(frameworkName);
		//		List<?> list = rootElement.selectNodes("*");
		//		String groupName;
		//		int numberOfAttribute;
		for (Element item : rootElement.getChildren("source")) {
			//			for (Iterator<?> iter = list.iterator(); iter.hasNext();){
			//			System.out.println(item.toString());
			//			groupName = item.getName().toString();
			SourceConfiguration sourceConfiguration = getSourceConfiguration(item, frameworkName);
			framework.addSourceConfiguration(sourceConfiguration);
		}

		for (Element item : rootElement.getChildren("sink")) {
			SinkConfiguration sinkConfiguration = getSinkConfiguration(item, frameworkName);
			framework.addSinkConfiguration(sinkConfiguration);
		}
		for (Element item : rootElement.getChildren("processor")) {
			ProcessorConfiguration processorConfiguration = getProcessorConfiguration(item, frameworkName);
			framework.addProcessorConfiguration(processorConfiguration);
		}
		for (Element item : rootElement.getChildren("composite_processor")) {
			CompositeProcessorConfiguration processorConfiguration = getCompositeProcessorConfiguration(item,
					frameworkName);
			framework.addProcessorConfiguration(processorConfiguration);
		}
		for (Element item : rootElement.getChildren("ins")) {
			framework.setInConfigurationList(getInConfigurationList(item, frameworkName));
		}
		for (Element item : rootElement.getChildren("outs")) {
			framework.setOutConfigurationList(getOutConfigurationList(item, frameworkName));
		}
		for (Element item : rootElement.getChildren("vars")) {
			framework.setVarConfigurationList(getVarConfigurationList(item, frameworkName));
		}
		for (Element item : rootElement.getChildren("connectors")) {
			framework.setConnectorConfigurationList(getConnectorConfigurationList(item, frameworkName));
		}
		for (Element item : rootElement.getChildren("agents")) {
			framework.setAgentConfigurationList(getAgentConfigurationList(item));
		}
		return framework;
	}

	protected static CompositeProcessorConfiguration getCompositeProcessorConfiguration(final Element item,
			final String frameworkName) throws NullConfigurationPointerException, JDOMException, IOException {
		//		int id = Integer.parseInt(item.getAttributeValue("id"));
		String name = item.getAttributeValue("name");
		String classType = item.getAttributeValue("class");
		CompositeProcessorConfiguration_ compositeProcessorConfiguration = new CompositeProcessorConfiguration_(name,
				frameworkName, classType);
		String version = item.getAttributeValue("version");
		if (version != null)
			compositeProcessorConfiguration.setVersionNumber(version);
		else
			compositeProcessorConfiguration.setVersionNumber("0.0.0");
		//		List<?> list = item.selectNodes("*");
		//		String groupName;
		//		for (Iterator<?> iter = list.iterator(); iter.hasNext();){
		for (Element subItem : item.getChildren("processor")) {
			//			System.out.println(subItem.toString());
			//			if (groupName == "processor"){
			ProcessorConfiguration processorConfiguration = getProcessorConfiguration(subItem, name);
			compositeProcessorConfiguration.addProcessorConfiguration(processorConfiguration);
		}
		for (Element subItem : item.getChildren("composite_processor")) {
			CompositeProcessorConfiguration processorConfiguration = getCompositeProcessorConfiguration(subItem, name);
			compositeProcessorConfiguration.addProcessorConfiguration(processorConfiguration);
		}
		for (Element subItem : item.getChildren("ins")) {
			compositeProcessorConfiguration.setInConfigurationList(getInConfigurationList(subItem, name));
		}
		for (Element subItem : item.getChildren("outs")) {
			compositeProcessorConfiguration.setOutConfigurationList(getOutConfigurationList(subItem, name));
		}
		for (Element subItem : item.getChildren("vars")) {
			compositeProcessorConfiguration.setVarConfigurationList(getVarConfigurationList(subItem, name));
		}
		for (Element subItem : item.getChildren("connectors")) {
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
				int id = Integer.parseInt(item.getAttributeValue("id"));
				processorConfiguration = getProcessorConfiguration(item, id);
				configurationList.add(processorConfiguration);
			}
		}
		if (configurationList == null) throw new NullConfigurationPointerException("list");
		return configurationList;
	}
	 */

	protected static SourceConfiguration getSourceConfiguration(final Element item, final String parentName)
			throws JDOMException, IOException {
		SourceConfiguration configuration = new SourceConfiguration_(item.getAttributeValue("name"), parentName,
				item.getAttributeValue("class"), item.getAttributeValue("method"));

		//		List<?> subList = item.selectNodes("*");
		//		String portsName = null;
		List<InConfiguration> inConfigurationList = new LinkedList<InConfiguration>();
		List<OutConfiguration> outConfigurationList = new LinkedList<OutConfiguration>();
		List<VarConfiguration> varConfigurationList = new LinkedList<VarConfiguration>();

		//		for (Iterator<?> subIter = subList.iterator(); subIter.hasNext();){
		for (Element subItem : item.getChildren("ins")) {
			//			portsName = subItem.getName().toString();
			inConfigurationList = getInConfigurationList(subItem, configuration.getName());
		}
		for (Element subItem : item.getChildren("outs")) {
			outConfigurationList = getOutConfigurationList(subItem, configuration.getName());
		}
		for (Element subItem : item.getChildren("vars")) {
			varConfigurationList = getVarConfigurationList(subItem, configuration.getName());
		}

		configuration.setInConfigurationList(inConfigurationList);
		configuration.setOutConfigurationList(outConfigurationList);
		configuration.setVarConfigurationList(varConfigurationList);
		//		System.out.println(configuration.toString());
		return configuration;
	}

	protected static SinkConfiguration getSinkConfiguration(final Element item, final String parentName)
			throws JDOMException, IOException {
		String name = item.getAttributeValue("name");
		String autoPlot = item.getAttributeValue("autoplot");
		String isDefaultSink = item.getAttributeValue("defaultsink");
		SinkConfiguration configuration = new SinkConfiguration_(name, parentName, autoPlot, isDefaultSink);

		//		List<?> subList = item.selectNodes("*");
		//		String portsName = null;
		List<InConfiguration> inConfigurationList = new LinkedList<InConfiguration>();
		List<OutConfiguration> outConfigurationList = new LinkedList<OutConfiguration>();
		List<VarConfiguration> varConfigurationList = new LinkedList<VarConfiguration>();

		//		for (Iterator<?> subIter = subList.iterator(); subIter.hasNext();){
		for (Element subItem : item.getChildren("ins")) {
			//			portsName = subItem.getName().toString();
			inConfigurationList = getInConfigurationList(subItem, name);
		}
		for (Element subItem : item.getChildren("outs")) {
			outConfigurationList = getOutConfigurationList(subItem, name);
		}
		for (Element subItem : item.getChildren("vars")) {
			varConfigurationList = getVarConfigurationList(subItem, name);
		}

		configuration.setInConfigurationList(inConfigurationList);
		configuration.setOutConfigurationList(outConfigurationList);
		configuration.setVarConfigurationList(varConfigurationList);
		//		System.out.println(configuration.toString());
		return configuration;
	}

	protected static ProcessorConfiguration getProcessorConfiguration(final Element item, final String parentName)
			throws JDOMException, IOException {
		String name = item.getAttributeValue("name");
		ProcessorConfiguration configuration = new ProcessorConfiguration_(name, parentName,
				item.getAttributeValue("class"), item.getAttributeValue("method"));
		String version = item.getAttributeValue("version");
		if (version != null)
			configuration.setVersionNumber(version);
		else
			configuration.setVersionNumber("0.0.0");
		//		List<?> subList = item.selectNodes("*");
		//		String portsName = null;
		List<InConfiguration> inConfigurationList = new LinkedList<InConfiguration>();
		List<OutConfiguration> outConfigurationList = new LinkedList<OutConfiguration>();
		List<VarConfiguration> varConfigurationList = new LinkedList<VarConfiguration>();

		//		for (Iterator<?> subIter = subList.iterator(); subIter.hasNext();){
		for (Element subItem : item.getChildren("ins")) {
			//			portsName = subItem.getName().toString();
			inConfigurationList = getInConfigurationList(subItem, name);
		}
		for (Element subItem : item.getChildren("outs")) {
			outConfigurationList = getOutConfigurationList(subItem, name);
		}
		for (Element subItem : item.getChildren("vars")) {
			varConfigurationList = getVarConfigurationList(subItem, name);
		}

		configuration.setInConfigurationList(inConfigurationList);
		configuration.setOutConfigurationList(outConfigurationList);
		configuration.setVarConfigurationList(varConfigurationList);
		//		System.out.println(configuration.toString());
		return configuration;
	}

	protected static List<InConfiguration> getInConfigurationList(final Element element, final String parentName)
			throws JDOMException, IOException {
		//		List<?> list = element.selectNodes("*");
		List<InConfiguration> portList = new LinkedList<InConfiguration>();
		for (Element item : element.getChildren()) {
			portList.add((InConfiguration) getPortConfiguration(item, "in", parentName));
		}
		return portList;
	}

	protected static List<OutConfiguration> getOutConfigurationList(final Element element, final String parentName)
			throws JDOMException, IOException {
		//		List<?> list = element.selectNodes("*");
		List<OutConfiguration> portList = new LinkedList<OutConfiguration>();
		for (Element item : element.getChildren()) {
			portList.add((OutConfiguration) getPortConfiguration(item, "out", parentName));
		}
		return portList;
	}

	protected static List<VarConfiguration> getVarConfigurationList(final Element element, final String parentName)
			throws JDOMException, IOException {
		List<VarConfiguration> portList = new LinkedList<VarConfiguration>();
		for (Element item : element.getChildren()) {
			portList.add((VarConfiguration) getPortConfiguration(item, "var", parentName));
		}
		return portList;
	}

	protected static List<ConnectorConfiguration> getConnectorConfigurationList(final Element element,
			final String parentName) throws JDOMException, IOException {
		List<ConnectorConfiguration> connectorList = new LinkedList<ConnectorConfiguration>();
		for (Element item : element.getChildren()) {
			connectorList.add(getConnectorConfiguration(item, parentName));
		}
		return connectorList;
	}

	protected static List<AgentConfiguration> getAgentConfigurationList(final Element element) {
		List<AgentConfiguration> agentConfigurationList = new ArrayList<AgentConfiguration>();
		for (Element item : element.getChildren()) {
			agentConfigurationList.add(getAgentConfiguration(item));
		}
		return agentConfigurationList;
	}

	protected static PortConfiguration getPortConfiguration(final Element element, final String patternName,
			final String parentName) {
		PortConfiguration configuration = null;
		String name, type;
		int dimension = 0;
		//		id = Integer.parseInt(element.getAttributeValue("id"));
		name = element.getAttributeValue("name");
		type = element.getAttributeValue("type");
		try {
			dimension = Integer.parseInt(element.getAttributeValue("dimension"));
		} catch (Exception ex) {
		}

		if ("in".equals(patternName))
			configuration = new InConfiguration_(name, dimension, type, parentName);
		if ("out".equals(patternName))
			configuration = new OutConfiguration_(name, dimension, type, parentName);
		if ("var".equals(patternName)) {
			String defaultValue = element.getAttributeValue("default_value");
			String label = element.getAttributeValue("label");
			//			System.out.println("var_" + id +": ");
			int ownerID = 0;
			if (element.getAttributeValue("owner") != null)
				ownerID = Integer.parseInt(element.getAttributeValue("owner"));
			String max = element.getAttributeValue("max");
			String min = element.getAttributeValue("min");
			String usage = element.getAttributeValue("usage");
			String options = element.getAttributeValue("option");
			String UIWidth = element.getAttributeValue("UIwidth");
			Map<String, String> attributeMap = new HashMap<String, String>();
			List<Attribute> attributes = element.getAttributes();
			for (Attribute attribute : attributes) {
				attributeMap.put(attribute.getName(), attribute.getValue());
			}
			configuration = new VarConfiguration_(name, dimension, type, parentName, defaultValue, ownerID, max, min,
					usage, label, options, UIWidth, attributeMap);
		}
		return configuration;
	}

	protected static ConnectorConfiguration getConnectorConfiguration(final Element element, final String parentName) {
		ConnectorConfiguration configuration = null;
		String name = "*connector*";
		//		int id;
		String producerID = null, consumerID = null;
		//		id = Integer.parseInt(element.getAttributeValue("id"));
		//		name = element.getAttributeValue("name");
		if (element.getAttributeValue("producer") != null)
			producerID = element.getAttributeValue("producer");
		if (element.getAttributeValue("consumer") != null)
			consumerID = element.getAttributeValue("consumer");
		configuration = new ConnectorConfiguration_(name, producerID, consumerID);
		return configuration;
	}

	protected static AgentConfiguration getAgentConfiguration(final Element element) {
		String name, principal, uiLabel;
		//		int id = 0;
		String pName = null;
		//		id = Integer.parseInt(element.getAttributeValue("id"));
		pName = element.getAttributeValue("pname");
		name = element.getAttributeValue("name");
		principal = element.getAttributeValue("principal");
		uiLabel = element.getAttributeValue("label");
		return new AgentConfiguration_(name, principal, pName, uiLabel);
	}
}
