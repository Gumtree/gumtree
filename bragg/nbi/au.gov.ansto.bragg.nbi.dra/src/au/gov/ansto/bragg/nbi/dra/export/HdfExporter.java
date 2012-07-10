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
package au.gov.ansto.bragg.nbi.dra.export;

import java.net.URI;

import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.cicada.export.HdfExport;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 30/03/2009
 */
public class HdfExporter extends ConcreteProcessor {

	private IGroup inputGroup;
	private IGroup outputGroup;
	private Boolean isEnabled = true;
	private Boolean stop = false;
	private URI saveUri;
	
	public HdfExporter(){
	}
	@Override
	public Boolean process() throws Exception {
		outputGroup = inputGroup;
		if (inputGroup == null || !isEnabled){
			return stop;
		}
		HdfExport export = new HdfExport();
		export.signalExport(saveUri, inputGroup);
		return stop;
	}
	/**
	 * @return the outputGroup
	 */
	public IGroup getOutputGroup() {
		return outputGroup;
	}
	/**
	 * @param inputGroup the inputGroup to set
	 */
	public void setInputGroup(IGroup inputGroup) {
		this.inputGroup = inputGroup;
	}
	/**
	 * @param isEnabled the isEnabled to set
	 */
	public void setIsEnabled(Boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	/**
	 * @param stop the stop to set
	 */
	public void setStop(Boolean stop) {
		this.stop = stop;
	}
	/**
	 * @param saveUri the saveUri to set
	 */
	public void setSaveUri(URI saveUri) {
		this.saveUri = saveUri;
	}
	
	
}
