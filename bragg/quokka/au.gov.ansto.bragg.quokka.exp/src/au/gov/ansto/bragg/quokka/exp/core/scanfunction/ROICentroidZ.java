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

import org.gumtree.data.Factory;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.quokka.exp.core.exception.GetDataFailedException;

/**
 * @author nxi
 *
 */
public class ROICentroidZ extends Function {

	private static ROICentroidZ centroidZ = null;
	
	protected ROICentroidZ() {
		super();
		plotTitle = "CentroidZ";
		// TODO Auto-generated constructor stub
	}

	public static ROICentroidZ getInstance(){
		if (centroidZ == null) centroidZ = new ROICentroidZ();
		return centroidZ;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.exp.core.scanfunction.Function#addData(org.gumtree.data.gdm.core.Group)
	 */
	@Override
	public void addData(IGroup databag) throws GetDataFailedException {
		// TODO Auto-generated method stub
		IDataItem centroidDataItem = databag.findDataItem( "centroid" );
		IArray centroidArray = null;
		try {
			centroidArray = centroidDataItem.getData();
			addDoubleData(centroidArray.getDouble(
					Factory.createIndex(centroidArray.getShape()).set(0)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new GetDataFailedException("Failed to get data from Group");
		}
	}

	@Override
	public String getShortDescription() {
		// TODO Auto-generated method stub
		return "ROICentroidZ: the centroid of the selected region of interests on Z axis.\n";
	}

}
