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
package au.gov.ansto.bragg.datastructures.core.common;

import org.gumtree.data.interfaces.IDataItem;

/**
 * @author nxi
 * Created on 06/03/2008
 */
public interface Log extends IDataItem {

	public static String PROCESSING_LOG_PREFIX = "Processed with:";
	public static String COPYING_LOG_PREFIX = "Copied from:";
	/**
	 * Return the latest change time stamp.
	 * @return String object
	 * Created on 06/03/2008
	 */
	public String getLastModificationTimeStamp();
	
	/**
	 * Append the given text to the log. Update the latest change time stamp.
	 * @param logContent
	 * Created on 06/03/2008
	 */
	public void appendLog(String logContent);
	
	/**
	 * Choose whether append the log with a time stamp.
	 * @param logContent String
	 * @param doTimeStamp true or false
	 * Created on 04/12/2008
	 */
	public void appendLog(String logContent, boolean doTimeStamp);
	
	/**
	 * Return the time stamp for creating the log.
	 * @return String object
	 * Created on 06/03/2008
	 */
	public String getCreationTimeStamp();
}
