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
 * There is operation statuses.
 * 
 * @author Danil Klimontov (dak)
 */
public enum OperationStatus {
	/**
	 * The status indicates the operation ready to be executed.
	 */
	Ready,

	/**
	 * The status indicates the operation was successfully executed.
	 */
	Done,

	/**
	 * The status indicates the operation was skipped during algorithm execution.
	 */
	Skipped,
	
	/**
	 * The status indicates the operation's parameters was modified and was not applied yet.
	 * After applying of the changes {@link #Active} status should be set.
	 */
	Modified,
	
	/**
	 * The status indicates the operation is in progress of execution.
	 */
	InProgress,
	
	/**
	 * The status indicates there was an error during operation execution.
	 */
	Error, Interrupted
}
