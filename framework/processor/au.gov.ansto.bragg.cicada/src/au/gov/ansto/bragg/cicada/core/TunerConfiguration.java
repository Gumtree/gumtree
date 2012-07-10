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

import java.io.IOException;
import java.util.List;

import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.interfaces.IGroup;

/**
 * @author nxi
 * Created on 08/05/2008
 */
public interface TunerConfiguration extends IGroup {

	public String getName();
	
	public String getRecipeID();
	
	public String getAlgorithmName();
	
	public List<String> getTunerNameList();
	
	public Object getTunerValue(String tunerName);
	
	public IGroup getValueGroup(String valueName);
	
	public void addTunerConfiguration(String tunerName, String tunerValue) 
	throws InvalidArrayTypeException;

	public void addTunerConfiguration(String tunerName, IGroup tunerValue) 
	throws InvalidArrayTypeException;

	public void setDefaultSinkName(String tunerValue) throws InvalidArrayTypeException;

	public String getDefaultSinkName() throws IOException;
	
}