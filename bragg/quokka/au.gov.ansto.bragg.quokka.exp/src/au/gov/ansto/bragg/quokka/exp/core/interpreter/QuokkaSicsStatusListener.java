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
package au.gov.ansto.bragg.quokka.exp.core.interpreter;

import org.gumtree.gumnix.sics.dom.sics.SicsStatusListener;

import au.gov.ansto.bragg.quokka.exp.core.QuokkaExperiment;

/**
 * @author nxi
 * Created on 29/08/2008
 */
public class QuokkaSicsStatusListener implements SicsStatusListener {

	/**
	 * 
	 */
	public QuokkaSicsStatusListener() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.gumtree.gumnix.sics.dom.sics.SicsStatusListener#getMessage(java.lang.String)
	 */
	public void getMessage(String message) {
		// TODO Auto-generated method stub
		QuokkaExperiment.getInstance().printlnToShell(message);
	}

}
