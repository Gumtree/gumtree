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
package de.kupzog.ktable;

/**
 * @author Friederich Kupzog
 */
public class KTableCellSelectionAdapter implements KTableCellSelectionListener {
    /**
     * Is called if a non-fixed cell is selected (gets the focus).
     * 
     * @see KTable for an explanation of the term "fixed cells".
     * @param col
     *            the column of the cell
     * @param row
     *            the row of the cell
     * @param statemask
     *            the modifier keys that where pressed when the selection
     *            happened.
     */
    public void cellSelected(int col, int row, int statemask) {}
    /**
     * Is called if a fixed cell is selected (is clicked).
     * 
     * @see KTable for an explanation of the term "fixed cells".
     * @param col
     *            the column of the cell
     * @param row
     *            the row of the cell
     * @param statemask
     *            the modifier keys that where pressed when the selection
     *            happened.
     */
    public void fixedCellSelected(int col, int row, int statemask) {}
}