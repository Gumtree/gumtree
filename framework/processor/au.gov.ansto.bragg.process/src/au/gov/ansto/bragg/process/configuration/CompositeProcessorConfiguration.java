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
 * Created on 02/02/2007, 5:10:42 PM
 * Last modified 02/02/2007, 5:10:42 PM
 * 
 */
public interface CompositeProcessorConfiguration extends ProcessorConfiguration {

	//	public List<SinkConfiguration> getSinkConfigurationList();
//	public List<AgentConfiguration> getAgentConfigurationList();
//	public List<SourceConfiguration> getSourceConfigurationList();
//	public void addSourceConfiguration(ProcessorConfiguration configuration);
	public void addProcessorConfiguration(ProcessorConfiguration configuration);
	public List<ConnectorConfiguration> getConnectorConfigurationList();
	public List<ProcessorConfiguration> getProcessorConfigurationList();
	public void setConnectorConfigurationList(List<ConnectorConfiguration> connectorConfigurationList);
}
