/*******************************************************************************
 * Copyright (c) 2010 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 ******************************************************************************/
package org.gumtree.data.nexus.netcdf;

/**
 * Constants used in NeXus model.
 * 
 * @author nxi
 * 
 */
public final class NXConstants {

	/**
	 * Hide default constructor.
	 */
	private NXConstants() {
	}

	/**
	 * Label name used in NeXus class.
	 */
	public static final String GLOBAL_NEXUS_CLASS_LABEL = "NX_class";
	/**
	 * Label name used in NeXus signals.
	 */
	public static final String GLOBAL_SIGNAL_CLASS_LABEL = "signal";
	/**
	 * Current NeXus version used.
	 */
	public static final String GLOBAL_NEXUS_VERSION = "4.1.0";

	/**
	 * Current HDF library version.
	 */
	public static final String ROOT_HDF_VERSION_LABEL = "HDF5_Version";
	/**
	 * Label of NeXus version.
	 */
	public static final String ROOT_NEXUS_VERSION_LABEL = "NeXus_version";
	/**
	 * Label of file time.
	 */
	public static final String ROOT_FILE_TIME_LABEL = "file_time";

	/**
	 * Name of NeXus entry class.
	 */
	public static final String ENTRY_NEXUS_CLASS_VALUE = "NXentry";

	/**
	 * Name of NeXus data class.
	 */
	public static final String DATA_NEXUS_CLASS_VALUE = "NXdata";
	/**
	 * Name of NeXus instrument class.
	 */
	public static final String INSTRUMENT_NEXUS_CLASS_VALUE = "NXinstrument";
	/**
	 * Name of NeXus user class.
	 */
	public static final String USER_NEXUS_CLASS_VALUE = "NXuser";
	/**
	 * Name of NeXus sample class.
	 */
	public static final String SAMPLE_NEXUS_CLASS_VALUE = "NXsample";
	/**
	 * Name of NeXus monitor class.
	 */
	public static final String MONITOR_NEXUS_CLASS_VALUE = "NXmonitor";
	/**
	 * Name of NeXus note class.
	 */
	public static final String NOTE_NEXUS_CLASS_VALUE = "NXnote";

	/**
	 * Label name of signal axes attribute.
	 */
	public static final String SIGNAL_AXES_LABEL = "axes";

	/**
	 * Label value of NeXus signal.
	 */
	public static final String SIGNAL_CLASS_VALUE = "1";

	/**
	 * Label value of NeXus signal.
	 */
	public static final String VARANCE_CLASS_VALUE = "variance";

	/**
	 * Label name for title attribute.
	 */
	public static final String TITLE_ATTRIBUTE_NAME = "title";

}
