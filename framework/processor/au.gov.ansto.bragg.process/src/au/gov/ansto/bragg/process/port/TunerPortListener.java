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
package au.gov.ansto.bragg.process.port;

import java.util.List;

/**
 * @author nxi
 * Created on 26/11/2008
 */
public abstract class TunerPortListener {

	private Tuner tuner;
	
	public TunerPortListener(Tuner tuner){
		this.tuner = tuner;
	}
	
	public abstract void updateUIValue(final Object value);

	public abstract void updateUIMax(final Object max);
	
	public abstract void updateUIMin(final Object min);
	
	public abstract void updateUIOptions(final List<?> options);
	/**
	 * @param tuner the tuner to set
	 */
	public void setTunerPort(Tuner tuner) {
		this.tuner = tuner;
	}

	/**
	 * @return the tunerPort
	 */
	public Tuner getTuner() {
		return tuner;
	}

	public void updateValue(final Object value){
		if (!(value == null && tuner.getSignal() == null) && 
				(tuner.getSignal() == null || !tuner.getSignal().equals(value)))
			tuner.updateValue(value);
		updateUIValue(value);
	}
	
	public void updateMax(final Object max) {
		tuner.setMax(max);
		updateUIMax(max);
	}

	public void updateMin(final Object min) {
		tuner.setMin(min);
		updateUIMin(min);
	}

	public void updateOptions(final List<?> options) {
		tuner.setOptions(options);
		updateUIOptions(options);
	}
	
	
}
