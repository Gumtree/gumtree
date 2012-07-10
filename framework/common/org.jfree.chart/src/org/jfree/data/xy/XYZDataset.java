/*******************************************************************************
 * Copyright (c) 2010 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 ******************************************************************************/
package org.jfree.data.xy;

/**
 * The interface through which JFreeChart obtains data in the form of (x, y, z)
 * items - used for XY and XYZ plots.
 */
public interface XYZDataset extends XYDataset {

    /**
     * Returns the z-value for the specified series and item.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The z-value (possibly <code>null</code>).
     */
    public Number getZ(int series, int item);

    /**
     * Returns the z-value (as a double primitive) for an item within a series.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The z-value.
     */
    public double getZValue(int series, int item);

    public int getXSize(int series);
    
    public int getYSize(int series);
    
}
