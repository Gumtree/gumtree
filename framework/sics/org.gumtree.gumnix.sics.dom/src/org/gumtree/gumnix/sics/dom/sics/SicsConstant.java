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
package org.gumtree.gumnix.sics.dom.sics;

/**
 * @author nxi
 * Created on 29/08/2008
 */
public class SicsConstant {

	public final static String HMSCAN_PATH = "/commands/scan/hmscan";
	public final static String HMSCAN_SCANVARIABLE_PATH = HMSCAN_PATH + "/scan_variable";
	public final static String HMSCAN_SCANSTART_PATH = HMSCAN_PATH + "/scan_start";
	public final static String HMSCAN_SCANINCREMENT_PATH = HMSCAN_PATH + "/scan_increment";
	public final static String HMSCAN_NUMBEROFPOINTS_PATH = HMSCAN_PATH + "/NP";
	public final static String HMSCAN_MODE_PATH = HMSCAN_PATH + "/mode";
	public final static String HMSCAN_PRESET_PATH = HMSCAN_PATH + "/preset";
	public final static String HMSCAN_CHANNEL_PATH = HMSCAN_PATH + "/channel";
	public final static String HMSCAN_STATUS_PATH = HMSCAN_PATH + "/feedback/status";
	public final static String HMSCAN_VARIABLE_VALUE_PATH = HMSCAN_PATH + "/feedback/scan_variable_value";
	public final static String SCAN_FILENAME_PATH = "/experiment/file_name";
	public final static String RUNSCAN_PATH = "/commands/scan/runscan";
	public final static String RUNSCAN_SCANVARIABLE_PATH = RUNSCAN_PATH + "/scan_variable";
	public final static String RUNSCAN_SCANSTART_PATH = RUNSCAN_PATH + "/scan_start";
	public final static String RUNSCAN_SCANSTOP_PATH = RUNSCAN_PATH + "/scan_stop";
	public final static String RUNSCAN_NUMBEROFPOINTS_PATH = RUNSCAN_PATH + "/numpoints";
	public final static String RUNSCAN_MODE_PATH = RUNSCAN_PATH + "/mode";
	public final static String RUNSCAN_PRESET_PATH = RUNSCAN_PATH + "/preset";
	public final static String RUNSCAN_DATATYPE_PATH = RUNSCAN_PATH + "/datatype";
	public final static String RUNSCAN_SAVETYPE_PATH = RUNSCAN_PATH + "/savetype";
	public final static String RUNSCAN_FORCE_PATH = RUNSCAN_PATH + "/force";
	public final static String RUNSCAN_STATUS_PATH = RUNSCAN_PATH + "/feedback/status";
	public final static String RUNSCAN_VARIABLE_VALUE_PATH = RUNSCAN_PATH + "/feedback/scan_variable_value";
	public static final int TIME_OUT = 1000;
	
	public static final int TIME_INTERVAL = 10;
}
