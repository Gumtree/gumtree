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
package au.gov.ansto.bragg.kakadu.ui.plot;

import au.gov.ansto.bragg.process.agent.Agent;

/**
 * The class describes a reference to Algorithm task for data object of an operation.
 * 
 * @author Danil Klimontov (dak)
 */
public class PlotDataReference {
	private int taskId;
	private int operationIndex;
	private int dataItemIndex;
	
	public PlotDataReference(int taskId, int operationIndex,
			int dataItemIndex) {
		this.taskId = taskId;
		this.operationIndex = operationIndex;
		this.dataItemIndex = dataItemIndex;
	}
	/**
	 * @return the taskId
	 */
	public int getTaskId() {
		return taskId;
	}
	/**
	 * @return the operation index
	 */
	public int getOperationIndex() {
		return operationIndex;
	}
	/**
	 * @return the dataItemIndex
	 */
	public int getDataItemIndex() {
		return dataItemIndex;
	}
	
	public void setDataItemIndex(int dataItemIndex) {
		this.dataItemIndex = dataItemIndex;
	}
}
