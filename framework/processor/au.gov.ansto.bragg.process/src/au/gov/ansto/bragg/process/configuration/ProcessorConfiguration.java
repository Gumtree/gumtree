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
package au.gov.ansto.bragg.process.configuration;

import java.util.List;
/**
 * @author  nxi  Created on 01/02/2007, 11:08:36 AM  Last modified 01/02/2007, 11:08:36 AM
 */
public interface ProcessorConfiguration extends Configuration {

	public final static String DEFAULT_PARENTNAME = "frame";
	
	/*
	 * Add an IN port configuration to the configuration list
	 */
	public void addInConfiguration(final InConfiguration inConfiguration);
	
	/*
	 * Add an OUT port configuration to the configuration list
	 */
	public void addOutConfiguration(final OutConfiguration outConfiguration);

	/*
	 * Add an VAR port configuration to the configuration list
	 */
	public void addVarConfiguration(final VarConfiguration varConfiguration);
	
	/*
	 * Get the class name of which this processor will create an instance 
	 */
	public String getClassType();
	
	/*
	 * Get the configuration type, for example, processor configuration
	 */
	public String getConfigurationType();
	
	/*
	 * Get the connector configuration list
	 */
	public List<ConnectorConfiguration> getConnectorConfigurationList();
	
	/*
	 * Get the IN port configuration list
	 */
	public List<InConfiguration> getInConfigurationList();
	
	/*
	 * Get the method name which will be called for processing the processor
	 */
	public String getMethodName();
	
	/*
	 * Get the method name which will be called by the processor 
	 */
	public void setMethodName(final String methodName);
	
	/*
	 * Get the OUT port configuration list
	 */
	public List<OutConfiguration> getOutConfigurationList();
	
	public String getParentName();
	
	public List<ProcessorConfiguration> getProcessorConfigurationList();
	/*
	 * Get the VAR port configuration list
	 */
	public List<VarConfiguration> getVarConfigurationList();

	public void setAgentConfigurationList(List<AgentConfiguration> agentConfigurationList);
	/*
	 * Set the class name of which the processor will create an instance 
	 */
	public void setClassType(String classType);

	public void setInConfigurationList(List<InConfiguration> inConfigurationList);
	
	public void setOutConfigurationList(List<OutConfiguration> outConfigurationList);
	
	public void setParentName(String parentName);
	
	public void setVarConfigurationList(List<VarConfiguration> varConfigurationList);
	
	public String getVersionNumber();
	
	public void setVersionNumber(String versionNumber);

}
