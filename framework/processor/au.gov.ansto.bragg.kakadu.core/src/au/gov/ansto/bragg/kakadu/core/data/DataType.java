/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.core.data;

/**
 * Data type list definition.
 * 
 * @author Danil Klimontov (dak)
 */
public enum DataType {

	/**
	 * 1D pattern.
	 */
	Pattern,
	
	/**
	 * 1D pattern set.
	 */
	PatternSet,
	
	/**
	 * 2D map.
	 */
	Map,
	
	/**
	 * 2D map set.
	 */
	MapSet,
	
	/**
	 * 3D volume.
	 */
	Volume,
	
	/**
	 * 3D volume set.
	 */
	VolumeSet,
	
	/**
	 * A list of key-value pairs. 
	 */
	Calculation,
	
	/**
	 * The data type used if data object analysis is fault or data object is null.
	 */
	Undefined
}
