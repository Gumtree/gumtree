package org.gumtree.dae.core.util;

public enum HistogramType {
	
	TOTAL_HISTOGRAM_XY("Total x-y histogram"),
	TOTAL_HISTOGRAM_X("Total x histogram"),
	TOTAL_HISTOGRAM_Y("Total y histogram"),
	
	RATEMAP_XY("x-y Event Rate Map"),
	RATEMAP_X("x Event Rate Map"),
	RATEMAP_Y("y Event Rate Map"),
	
	RAW_TOTAL_HISTOGRAM_XY("Raw total x-y histogram"),
	RAW_TOTAL_HISTOGRAM_X("Raw total x histogram"),
	RAW_TOTAL_HISTOGRAM_Y("Raw total y histogram"),
	
	RAW_FRAME_HISTOGRAM_XY("Raw frame x-y histogram"),
	
	// Legacy
	TOTAL_HISTO_XY("?");
	
	
	private HistogramType(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	private String description;
	
}
