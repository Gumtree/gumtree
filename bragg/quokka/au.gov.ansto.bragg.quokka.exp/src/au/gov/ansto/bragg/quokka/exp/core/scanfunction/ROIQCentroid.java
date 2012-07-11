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
package au.gov.ansto.bragg.quokka.exp.core.scanfunction;

import java.io.IOException;

import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.quokka.exp.core.exception.GetDataFailedException;

public class ROIQCentroid extends Function {

	private static ROIQCentroid roiQCentroid = null;

	protected ROIQCentroid() {
		// TODO Auto-generated constructor stub
		super();
		plotTitle = "Q Centroid";
		// TODO Auto-generated constructor stub
	}

	public static ROIQCentroid getInstance(){
		if (roiQCentroid == null) roiQCentroid = new ROIQCentroid();
		return roiQCentroid;
	}
	
	@Override
	public void addData(IGroup databag) throws GetDataFailedException {
		// TODO Auto-generated method stub
		IDataItem qCentoridDataItem = databag.findDataItem( "Q_centroid" );
		IArray qCentroid = null;
		try {
			qCentroid = qCentoridDataItem.getData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new GetDataFailedException("Failed to get data from Group");
		}
		addDoubleData(new Double(qCentroid.getArrayMath().getMaximum()));
	}
	
	@Override
	public String getShortDescription() {
		// TODO Auto-generated method stub
		return "ROIQCentroid: the Q centroid of the selected region of interests.\n";
	}


}
