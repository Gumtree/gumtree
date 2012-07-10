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

import org.gumtree.data.interfaces.IGroup;

/**
 * The listener notifies about data changes for an operation.
 * 
 * @author Danil Klimontov (dak)
 */
public interface OperationDataListener {

	/**
	 * Notifies that output operation data was updated.
	 * @param operation an operation which has been updated 
	 * @param oldData old data object
	 * @param newData new data object
	 */
	void outputDataUpdated(Operation operation, IGroup oldData, IGroup newData);
}
