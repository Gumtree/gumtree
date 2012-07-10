/*******************************************************************************
 * Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 ******************************************************************************/
package org.gumtree.data.ansto.fitting;

/**
 * Static values used in GDM.
 * @author nxi Created on 18/06/2008
 */
public final class StaticField {

	/**
	 * Hide the default constructor.
	 */
	private StaticField() {
	}

	/**
	 * @author nxi Name of fitting type.
	 */
	public enum FitterType {
		ChiSquared("Chi2"), 
		LeastSquares("LS"), 
		BinnedMaxLikelihood("bml"), 
		UnbinnedMaxLikelihood(
				"uml");
		private String value;

		FitterType(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	};

	/**
	 * @author nxi Name of function type.
	 */
	public enum FunctionType {
		Gaussian("Gaussian"), 
		Linear("Linear"), 
		Quadratic("Quadratic"), 
		Cubic("Cubic"), 
		GaussianLorentzian("GaussianLorentzian"), 
		Power("Power"), 
		AddFunction("Add New");

		private String value;

		FunctionType(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	};

	/**
	 * @author nxi Name of fitting engine.
	 */
	public enum EnginType {
		jminuit, juncmin
	}

	/**
	 * The wildcard used in dictionary path to replace entry name.
	 */
	public static final Object ENTRY_LABEL = "$entry";
}
