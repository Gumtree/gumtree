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

package au.gov.ansto.bragg.errorpropagation;

import org.gumtree.data.interfaces.IArray;

/**
 * Encapsulation of an <code>Array</code> of values along with
 * associated measurement errors.
 *  
 * @author lwi
 */
public class ArrayWithVariance {

	private IArray data;
	private IArray variance;
	
	public ArrayWithVariance(IArray data, IArray error) {
		this.data = data;
		this.variance = error;
	}
	
	public IArray getData() {
		return data;
	}
	
	public IArray getVariance() {
		return variance;
	}
}
