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
package au.gov.ansto.bragg.cicada.core;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.cicada.core.exception.ConfigurationException;

/**
 * @author nxi
 * Created on 04/06/2008
 */
public interface DRATask extends IGroup {

	public List<URI> getDataSourceList() throws URISyntaxException;
	
	public String getAlgorithmName();
	
	public String getAlgorithmVersion();
	
	public String getAlgorithmSetId();
	
	public IGroup getAlgorithmConfiguration() throws ConfigurationException;
	
	public void addDataSource(URI fileURI, String entryName) throws ConfigurationException;
	
	public List<String> getEntryIDs(URI uri);
	
	public boolean isValid();
}
