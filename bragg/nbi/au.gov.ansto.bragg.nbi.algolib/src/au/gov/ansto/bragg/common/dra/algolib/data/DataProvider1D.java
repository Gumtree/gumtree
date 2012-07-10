/*******************************************************************************
 * Copyright (c) 2004  Australian Nuclear Science and Technology Organisation.
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * GumTree Platform is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * Contributors:
 *     Hugh Rayner (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package au.gov.ansto.bragg.common.dra.algolib.data;

import java.util.Date;

import au.gov.ansto.bragg.common.dra.algolib.plot.PlotDataShaped;
import au.gov.ansto.bragg.common.dra.algolib.plot.PlotData1D;

/**
 * An interface used to provide data
 * to SeriesTabViewer
 * @author hrz
 *
 */
public interface DataProvider1D extends DataProvider{
	/**
	 * @return true if the provider can produce a shaped
	 * data set (for shaped 2d plots).
	 */
	public boolean canDoShapedDataSet();
	/**
	 * Generates and returns the shaped data set.
	 * @return {intensity values, x values, y values}
	 */
	public PlotDataShaped getShapedDataSet();
	/**
	 * Returns a single 1d data set.
	 * @param index The index of the set to retrieve.
	 * @return The data set.
	 */
    public PlotData1D getDataSet1D(int index);
    /**
     * Returns the z coordinate of a particular data set.
     * Used for 3d plots.
     * @param index The index of the data set to retrieve.
     * @return The z coordinate.
     */
    public double getDataSetZ(int index);
    /**
     * @return The number of available 'normal' data sets.
     * Does not include data sets like depth average (which
     * is normally indexed as -1).
     */
	public int getDataSetCount1D();
	/**
	 * @return The X values of the ancillary plot, if applicable.
	 */
	public double[] getAncillaryX();
	/**
	 * @return The Y values of the ancillary plot, if applicable.
	 */
	public double[] getAncillaryY();
	/**
	 * @return When the data provider was last updated.
	 */
	public Date lastUpdate();
	/**
	 * @return The labels to be used for a 2d plot.
	 */
	public String[] twoDLabels();
	/**
	 * Retrieves the values for the supplimentary readouts.
	 * @param labels An array to store the labels in.
	 * @param mins An array to store the 'zero' values in.
	 * @param mults An array to store the multiplication factors
	 * in.
	 */
	public void getSupValues(String[] labels, double[] mins, double[] mults);
	public PlotData1D getDuplicity();
}
