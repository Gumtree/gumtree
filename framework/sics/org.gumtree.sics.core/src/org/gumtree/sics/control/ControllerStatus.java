/*******************************************************************************
 * Copyright (c) 2006 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.sics.control;

/**
 * ComponentStatus indicates the current status of the component.
 * 
 * @since 1.0
 * 
 */
public enum ControllerStatus {

	OK(2), RUNNING(1), ERROR(0);

	private ControllerStatus(int priority) {
		this.priority = priority;
	}

	/**
	 * Returns the preset priority of this status object.
	 * 
	 * @return priority of the component status
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Compares a give status to this status object.
	 * 
	 * @param anotherStatus
	 *            status for comparison
	 * @return true if this status is more important than the give status; false
	 *         otherwise
	 */
	public boolean isMoreImportantThan(ControllerStatus anotherStatus) {
		return getPriority() < anotherStatus.getPriority();
	}

	private int priority;

}
