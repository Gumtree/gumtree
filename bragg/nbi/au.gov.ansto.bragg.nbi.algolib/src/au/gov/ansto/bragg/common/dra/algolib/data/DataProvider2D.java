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

/**
 * An interface to provide data for TwoDTabViewer
 * @author hrz
 *
 */
public interface DataProvider2D extends DataProvider {
	/**
	 * Gets a particular data set by index.
	 * @param index The data set to retrieve.
	 * @return The data set.
	 */
    double[][] getDataSet(int index);
	/**
	 * Gets the number of data sets available to be plotted.
	 * @return The number of data sets.
	 */
	public int getDataSetCount();
	/**
	 * Returns the scaling of the data in the following format:
	 * xMin, yMin, xMax, yMax
	 * @param index The data set to consider.
	 * @return The scales.
	 */
	public double[] getScales(int index);
	void setCorrectSensitivity(boolean selection);
}
