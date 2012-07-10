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
package au.gov.ansto.bragg.datastructures.core.common.internal;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.impl.netcdf.NcDataItem;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.common.Log;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition;

/**
 * @author nxi
 * Created on 06/03/2008
 */
public class NcLog extends NcDataItem implements Log {

	public final static String CREATION_TIME_STAMP_NAME = "creation_time";
	public final static String LAST_MODIFICATION_TIME_STAMP_NAME = "last_modification_time";
	
	public NcLog(IGroup group, String shortName, String logContent) throws InvalidArrayTypeException {
		super((NcGroup) group, shortName, Factory.createArray((DateFormat.getInstance().format(
				new Date()) + " ** " + logContent + "\n").toCharArray()));
		addStringAttribute(StaticDefinition.DATA_STRUCTURE_TYPE, DataStructureType.log.name());
//		addStringAttribute(CREATION_TIME_STAMP_NAME, DateFormat.getInstance().format(new Date()));
		addStringAttribute(CREATION_TIME_STAMP_NAME, String.valueOf(System.currentTimeMillis()));
		setLastModificationTimeStamp();
		// TODO Auto-generated constructor stub
	}

	public void appendLog(String logContent) {
		appendLog(logContent, true);
	}
	
	public void appendLog(String logContent, boolean doTimeStamp) {
		// TODO Auto-generated method stub
		IArray oldContent = null;
		String newContent;
		String timeStamp = "";
		if (doTimeStamp)
			timeStamp = DateFormat.getInstance().format(new Date()) + " ** ";
		try {
			oldContent = this.getData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		if (oldContent != null){
			newContent = oldContent.toString() + timeStamp + logContent + "\n";
		}else newContent =  timeStamp + logContent + "\n";
		try {
			setCachedData(Factory.createArray(newContent.toCharArray()), false);
		} catch (InvalidArrayTypeException e) {
			// TODO Auto-generated catch block
		}
		addStringAttribute("modification_time", String.valueOf(System.currentTimeMillis()));
		setLastModificationTimeStamp();
	}

	public String getCreationTimeStamp() {
		// TODO Auto-generated method stub
		IAttribute timeStamp = getAttribute(CREATION_TIME_STAMP_NAME);
		if (timeStamp != null) return timeStamp.getStringValue();
		return null;
	}

	public String getLastModificationTimeStamp() {
		// TODO Auto-generated method stub
		IAttribute timeStamp = getAttribute(LAST_MODIFICATION_TIME_STAMP_NAME);
		if (timeStamp != null) return timeStamp.getStringValue();
		return null;
	}

	public void setLastModificationTimeStamp(){
		IAttribute timeStamp = getAttribute(LAST_MODIFICATION_TIME_STAMP_NAME);
		if (timeStamp == null) {
			addStringAttribute(LAST_MODIFICATION_TIME_STAMP_NAME, 
					String.valueOf(System.currentTimeMillis()));
			return;
		}
		timeStamp.setStringValue(String.valueOf(System.currentTimeMillis()));
	}
}
