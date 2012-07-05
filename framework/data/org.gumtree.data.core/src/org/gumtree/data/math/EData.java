/*******************************************************************************
 * Copyright (c) 2012 Australian Nuclear Science and Technology Organisation,
 * Synchrotron SOLEIL and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     Norman XIONG (Bragg Institute) - initial API and implementation
 *     Clément RODRIGUEZ (SOLEIL) - initial API and implementation
 *     Tony LAM (Bragg Institute) - implementation
 ******************************************************************************/

package org.gumtree.data.math;

/**
 * The data wrapper for error propagation mathematics.
 * 
 * @param <T> the parameter class type
 * @author nxi 
 */
public class EData<T> {

	/**
	 * Data storage.
	 */
	private T data;
	/**
	 * Variance data storage.
	 */
	private T variance;

	/**
	 * Constructor fully parameterised.
	 * 
     * @param data T type
     * @param variance T type
	 */
	public EData(final T data, final T variance) {
		this.data = data;
		this.variance = variance;
	}

	/**
	 * Return the data field.
	 * 
     * @return a generic type 
	 */
	public T getData() {
		return data;
	}

	/**
	 * Return the variance field.
	 * 
     * @return a generic type 
	 */
	public T getVariance() {
		return variance;
	}
}
