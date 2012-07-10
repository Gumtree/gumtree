/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lindsay Winkler (Bragg Institute) - initial implementation
 *******************************************************************************/

package au.gov.ansto.bragg.common.dra.algolib.rebinning;

import java.util.Comparator;

/**
 * Compare two <code>Interval</code>s for sorting purposes on the basis
 * of their starting points only.
 *  
 * @author lwi
 */
public class DataBinStartPointComparator implements Comparator {

	public int compare(Object arg0, Object arg1) {
		DataBin bin0 = (DataBin) arg0;
		DataBin bin1 = (DataBin) arg1;
		
		double difference = bin0.getStart() - bin1.getStart(); 
		if (difference < 0) {
			return -1;
		} else if (difference > 0) { 
			return 1;
		} else {
			return 0;
		}
	}

}
