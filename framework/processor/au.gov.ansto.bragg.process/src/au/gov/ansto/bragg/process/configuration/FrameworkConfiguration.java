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
 * @author nxi
 * Created on 05/02/2007, 11:36:19 AM
 * Last modified 05/02/2007, 11:36:19 AM
 * 
 */
public interface FrameworkConfiguration extends CompositeProcessorConfiguration {

	public final int ID = 0;
	public final String NAME = "framework";
	public final String PARENT_NAME = "null";
	public final String CLASS_TYPE = "au.gov.ansto.bragg.algorithm.Framework";
	
	/*
	 * Add a sink configruation to the sink configuration list
	 */
	public void addSinkConfiguration(SinkConfiguration configuration);
		
	/*
	 * Add a source configruation to the source configuration list
	 */
	public void addSourceConfiguration(SourceConfiguration configuration);
	
	/*
	 * Get the agent configuration list
	 */
	public List<AgentConfiguration> getAgentConfigurationList();
	
	/*
	 * Get the sink configuration list
	 */
	public List<SinkConfiguration> getSinkConfigurationList();
	
	/*
	 * Get the source configuration list
	 */
	public List<SourceConfiguration> getSourceConfigurationList();
	
	/*
	 * (non-Javadoc)
	 * @see au.gov.ansto.bragg.process.configuration.ProcessorConfiguration#setAgentConfigurationList(java.util.List)
	 * Set agent configuration list
	 */
	public void setAgentConfigurationList(List<AgentConfiguration> agentConfigurationList);

}
