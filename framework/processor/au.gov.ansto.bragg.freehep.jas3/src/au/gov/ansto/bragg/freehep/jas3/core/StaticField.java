/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
*******************************************************************************/
package au.gov.ansto.bragg.freehep.jas3.core;


/**
 * @author nxi
 * Created on 18/06/2008
 */
public class StaticField {

	public enum FitterType{
		ChiSquared ("Chi2"),
		LeastSquares ("LS"),
		BinnedMaxLikelihood ("bml"),
		UnbinnedMaxLikelihood ("uml");
		private String value;
		
		FitterType(String value){
			this.value = value;
		}
		
		public String getValue(){
			return value;
		}
	};
	
	public enum FunctionType{
		Gaussian ("Gaussian"),
		Linear ("Linear"),
		Quadratic ("Quadratic"), 
		Cubic ("Cubic"),
		GaussianLorentzian ("GaussianLorentzian"),
		Power ("Power"),
		AddFunction ("Add New");
		
		private String value;
		
		FunctionType(String value){
			this.value = value;
		}
		
		public String getValue(){
			return value;
		}
	};
	
	public enum EnginType{jminuit, juncmin};
}
