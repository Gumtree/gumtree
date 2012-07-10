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
 * There is full list of possible operation parameters types.
 *  
 * @author Danil Klimontov (dak)
 */
public enum OperationParameterType {

	Text,
	Number,
	Uri,
	StepDirection,
	Boolean,
	Region,
	Option,
	Position,
	Unknown 
}
