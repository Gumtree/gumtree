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
package au.gov.ansto.bragg.quokka.model.core;

public class QuokkaConstants {

	public final static int DETECTOR_HORIZONTAL_PIXELS = 192;
	public final static int DETECTOR_VERTICAL_PIXELS = 192;
	public final static int DETECTOR_RESOLUTION = 5;
	public final static int ENTRANCE_DISTANCE = 10000;
	public final static int SAMPLE_DISTANCE = 10000;
	public final static int ENTRANCE_APERTURE_RADIUS = 50;
	public final static int SAMPLE_APERTURE_RADIUS = 30;
	public final static int DEFAULT_BEAMSTOPPER_RADIUS = 50;
	public final static double DEFAULT_WAVELENGTH = 5;
	
	
	public final static String SAp_PATH = "/commands/optics/sample_aperture";
	public final static String SApPosXmm_PATH = "/commands/optics/sample_aperture/size";
	public final static String SApPosZmm_PATH = "/commands/optics/sample_aperture/size";
	public final static String SApShape_PATH = "/commands/optics/sample_aperture/shape";
	
	public final static String SampleNum_PATH = "/commands/sample/select/sampid";
	public final static String SampleSelect_PATH = "/commands/sample/select";
	
	public final static String AttRot_PATH = "/commands/optics/rotary_attenuator";
	public final static String AttRotDeg_PATH = "/commands/optics/rotary_attenuator/angle";
	
	public final static String RotAp_PATH = "/commands/optics/entrance_aperture";
	public final static String RotApDeg_PATH = "/commands/optics/entrance_aperture/angle";
	public final static String RotApShape_PATH = "/commands/optics/entrance_aperture/shape";
	public static final String LambdA = null;
	public static final String LambdaResFWHM = "/instrument/parameters/LambdaResFWHM%";
	public static final String ENTRANCE_APERTURE_X = null;
	public static final String ENTRANCE_APERTURE_Z = null;
	public static final String Guide_PATH = "/commands/optics/guide";
	public static final String GuideConfiguration_PATH = "/commands/optics/guide/configuration";

}
