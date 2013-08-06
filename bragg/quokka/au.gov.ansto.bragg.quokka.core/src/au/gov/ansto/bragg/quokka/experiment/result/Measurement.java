/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package au.gov.ansto.bragg.quokka.experiment.result;

import au.gov.ansto.bragg.quokka.experiment.model.ScanMode;

public abstract class Measurement {

	private ScanMode mode;
	
	private long preset;
	
	private float att;
	
	private String filename;
	
	public Measurement() {
		super();
	}

	public ScanMode getMode() {
		return mode;
	}

	public void setMode(ScanMode mode) {
		this.mode = mode;
	}

	public long getPreset() {
		return preset;
	}

	public void setPreset(long preset) {
		this.preset = preset;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public float getAtt() {
		return att;
	}

	public void setAtt(float att) {
		this.att = att;
	}
	
}
