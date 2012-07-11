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

package au.gov.ansto.bragg.platypus.ui.widgets;

import au.gov.ansto.bragg.nbi.ui.widgets.HMImageMode;

public enum PlatypusHMImageMode implements HMImageMode {
	
	TOTAL_HISTOGRAM_YT("Total y-t histogram", "&type=TOTAL_HISTOGRAM_YT"),
	TOTAL_HISTOGRAM_Y("Total y histogram", "&type=TOTAL_HISTOGRAM_Y"),
	TOTAL_HISTOGRAM_T("Total t histogram", "&type=TOTAL_HISTOGRAM_T"),
	RATEMAP_YT("y-t Event Rate Map", "&type=RATEMAP_YT"),
	RATEMAP_Y("y Event Rate Map", "&type=RATEMAP_Y"),
	RATEMAP_T("t Event Rate Map", "&type=RATEMAP_T"),
	RAW_TOTAL_HISTOGRAM_XY("Raw total x-y histogram", "&type=RAW_TOTAL_HISTOGRAM_XY"),
	RAW_TOTAL_HISTOGRAM_X("Raw total x histogram", "&type=RAW_TOTAL_HISTOGRAM_X"),
	RAW_TOTAL_HISTOGRAM_Y("Raw total y histogram", "&type=RAW_TOTAL_HISTOGRAM_Y"),
	RAW_FRAME_HISTOGRAM_XY("Raw frame x-y histogram", "&type=RAW_FRAME_HISTOGRAM_XY");
	
	private PlatypusHMImageMode(String label, String query) {
		this.label = label;
		this.query = query;
	}
	
	public String toString() {
		return getLabel();
	}
	
	public String getLabel() {
		return label;
	}
	
	public String getQuery() {
		return query;
	}
	
	public HMImageMode[] getValues() {
		return PlatypusHMImageMode.values();
	}
	
	private String label;
	private String query;

}
