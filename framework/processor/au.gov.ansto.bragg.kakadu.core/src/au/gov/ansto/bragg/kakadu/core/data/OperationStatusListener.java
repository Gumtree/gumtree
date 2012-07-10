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
 * The listener is used to notify about operation status changes.
 * 
 * @author Danil Klimontov (dak)
 */
public interface OperationStatusListener{

	/**
	 * Notifies that operation status was updated.
	 * 
	 * @param operation an operations which has been updated.
	 * @param newOperationStatus old operation status.
	 * @param newOperationStatus new operation status.
	 */
	void statusUpdated(Operation operation, OperationStatus oldOperationStatus, OperationStatus newOperationStatus);
}
