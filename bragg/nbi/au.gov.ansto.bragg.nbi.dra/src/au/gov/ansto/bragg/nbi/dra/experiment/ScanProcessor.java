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
package au.gov.ansto.bragg.nbi.dra.experiment;

import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 26/08/2008
 */
public class ScanProcessor extends ConcreteProcessor {

	private Double ScanProcessor_scanVar;
	private Double ScanProcessor_startPosition;
	private Double ScanProcessor_stopPosition;
	private Integer ScanProcessor_numPoints;
	private String ScanProcessor_mode;
	private Double ScanProcessor_preset;
	private Integer ScanProcessor_channel;
	private Boolean ScanProcessor_stop = false;
	private IGroup ScanProcessor_resultEntry;

	private Integer currentPoint = 0;
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.nbi.dra.core.ConcreteProcessor#process()
	 */
	public Boolean process() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
