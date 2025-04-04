/*
 * Copyright (C) 2004 by Friederich Kupzog Elektronik & Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html

    Author: Friederich Kupzog
    fkmk@kupzog.de
    www.kupzog.de/fkmk
*/
package org.gumtree.msw.ui.ktable;

/**
 * @author Friederich Kupzog
 */
public interface KTableCellDoubleClickListener {

	/**
	 * Is called if a non-fixed cell is double clicked.
	 * @see KTable for an explanation of the term "fixed cells".
	 * @param col
	 * the column of the cell
	 * @param row
	 * the row of the cell
	 * @param statemask
	 * the modifier keys that where pressed when the selection happened.
	 */
	public void cellDoubleClicked(int col, int row, int statemask);

	/**
	 * Is called if a fixed cell is double clicked .
	 * @see KTable for an explanation of the term "fixed cells".
	 * @param col
	 * the column of the cell
	 * @param row
	 * the row of the cell
	 * @param statemask
	 * the modifier keys that where pressed when the selection happened.
	 */
	public void fixedCellDoubleClicked(int col, int row, int statemask);

}
