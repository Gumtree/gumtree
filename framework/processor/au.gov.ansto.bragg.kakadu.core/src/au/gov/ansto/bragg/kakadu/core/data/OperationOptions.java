/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.core.data;

import au.gov.ansto.bragg.process.port.Tuner;

/**
 * The class describes Operation options.
 * The options are used to control operations processing.
 * 
 * @author Danil Klimontov (dak)
 */
public class OperationOptions {
	private boolean skipped;
	private boolean enabled;
	private boolean stopAfterComplete;
	private boolean plotAfterComplete;
	private Tuner skipTuner;
	private Tuner enableTuner;
	private Tuner stopAfterCompleteTuner;
	
	/**
	 * Gets plot after complete flag state.
	 * Defines whether UI should update current plot 
	 * by the result of the operation.
	 * @return true if UI should be updated or false otherwise.
	 */
	public boolean isPlotAfterComplete() {
		return plotAfterComplete;
	}
	
	/**
	 * Sets plot after complete flag state.
	 * Defines whether UI should update current plot 
	 * by the result of the operation.
	 * @param plotAfterComplete true if UI should be updated or false otherwise.
	 */
	public void setPlotAfterComplete(boolean plotAfterComplete) {
		this.plotAfterComplete = plotAfterComplete;
	}
	
	/**
	 * Gets operation skipped flag state.
	 * Defines whether the operation should be skipped 
	 * in time of algorithm execution.
	 * @return true if the operation should be skipped or false otherwise.
	 */
	public boolean isSkipped() {
		return skipped;
	}
	
	public boolean isEnabled(){
		return enabled;
	}
	/**
	 * Sets operation skipped flag state.
	 * Defines whether the operation should be skipped 
	 * in time of algorithm execution.
	 * @param skipped true if the operation should be skipped or false otherwise.
	 */
	public void setSkipped(boolean skipped) {
		this.skipped = skipped;
	}
	
	/**
	 * Gets stop after complete flag state.
	 * Defines whether algorithm execution  should be stopped 
	 * after the operation has been complete.
	 * @return true if the algorithm should be stopped or false otherwise.
	 */
	public boolean isStopAfterComplete() {
		return stopAfterComplete;
	}

	/**
	 * Sets stop after complete flag state.
	 * Defines whether algorithm execution  should be stopped 
	 * after the operation has been complete.
	 * @param stopAfterComplete true if the algorithm should be stopped or false otherwise.
	 */
	public void setStopAfterComplete(boolean stopAfterComplete) {
		this.stopAfterComplete = stopAfterComplete;
	}

	public boolean isSkipSupported() {
		return skipTuner != null;
	}

	public boolean isEnableSupported() {
		return enableTuner != null;
	}

	public boolean isStopAfterCompleteSupported() {
		return stopAfterCompleteTuner != null;
	}

	public void setSkipTuner(Tuner tuner) {
		this.skipTuner = tuner;
		skipped = new Boolean(tuner.getSignal().toString()).booleanValue();
	}

	public void setStopAfterCompleteTuner(Tuner tuner) {
		this.stopAfterCompleteTuner = tuner;
		stopAfterComplete = new Boolean(tuner.getSignal().toString()).booleanValue();
	}

	public Tuner getSkipTuner() {
		return skipTuner;
	}

	public Tuner getEnableTuner() {
		return enableTuner;
	}

	public Tuner getStopAfterCompleteTuner() {
		return stopAfterCompleteTuner;
	}

	public void updateValuesFromServer() {
		if (skipTuner != null) {
			setSkipTuner(skipTuner);
		}
		if (enableTuner != null)
			setEnableTuner(enableTuner);
		if (stopAfterCompleteTuner != null) {
			setStopAfterCompleteTuner(stopAfterCompleteTuner);
		}
	}

	public String getTitle(){
		return skipTuner.getLabel();
	}

	public void setEnableTuner(Tuner tuner) {
		// TODO Auto-generated method stub
		this.enableTuner = tuner;
		enabled = new Boolean(tuner.getSignal().toString()).booleanValue();
	}
}
