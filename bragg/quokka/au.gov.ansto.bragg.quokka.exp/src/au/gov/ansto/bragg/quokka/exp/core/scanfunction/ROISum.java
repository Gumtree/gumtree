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

public class ROISum extends Function {

	private static ROISum roiSum = null;
	
	protected ROISum() {
		super();
		plotTitle = "Total Sum";
		// TODO Auto-generated constructor stub
	}

	public static ROISum getInstance(){
		if (roiSum == null) roiSum = new ROISum();
		return roiSum;
	}
	
	@Override
	public void addData(IGroup databag) throws GetDataFailedException {
		// TODO Auto-generated method stub
		IDataItem totalSumDataItem = databag.findDataItem( "total_sum" );
		IArray totalSum = null;
		try {
			totalSum = totalSumDataItem.getData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new GetDataFailedException("Failed to get data from Group");
		}
		addDoubleData(new Double(totalSum.getArrayMath().getMaximum()));
	}
	
	@Override
	public String getShortDescription() {
		// TODO Auto-generated method stub
		return "ROISum: the total sum of counts of the selected region of interests.\n";
	}

}
