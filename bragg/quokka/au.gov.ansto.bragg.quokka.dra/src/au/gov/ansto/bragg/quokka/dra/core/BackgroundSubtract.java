/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong - initial API and implementation
*    Paul Hathaway - updates, smoothing, threshold, error propagation
*******************************************************************************/
package au.gov.ansto.bragg.quokka.dra.core;

import java.io.IOException;
import java.net.URI;

import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.math.EData;

import au.gov.ansto.bragg.datastructures.core.exception.StructureTypeException;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.nexus.NexusUtils;

public class BackgroundSubtract implements ConcreteProcessor {

	private Plot plot_in;
	private Plot plot_out;
	
	private Boolean backgroundSubtract_skip = true;
	private Boolean backgroundSubtract_stop = false;
	
	private boolean useThreshold = false;
	private double  threshold = 0.0;
	
	private boolean useSmoothing = false;
	private int     smoothingRadius = 1;
	
	enum Scaler { TIME, PRESET, BM1_COUNTS, BM2_COUNTS, BM3_COUNTS }
	private Scaler scaleMode = Scaler.TIME;
	private EData<Double> scale;
	private EData<Double> counter;
	
	private URI bkgFile;
	
	public Boolean process() throws Exception {
		if (backgroundSubtract_skip) {
			plot_out = plot_in;
		} else {
			Plot background = fetchBackground(bkgFile);
			scale = fetchScale(plot_in, scaleMode);
			counter = fetchScale(background, scaleMode);

			if (useSmoothing) {
				background = applySmoothing(background,smoothingRadius);
			}
			
			background.scale( -(scale.getData()/counter.getData()), 
							   (scale.getVariance()/counter.getVariance()) );
			plot_out = plot_in.add(background);
			
			if (useThreshold) {
				plot_out = applyThreshold(plot_out,threshold);
			}
		}
		
		return backgroundSubtract_stop;
	}
	
	private Plot fetchBackground(URI bkgfile) throws Exception {
		Plot background;
		try {		
			background = (Plot) NexusUtils.getNexusData(bkgfile);
		} catch (StructureTypeException ste) {
			throw new Exception(ste.getMessage());
		}
		return background;
	}
	
	private EData<Double> fetchScale(Plot plot, Scaler mode) throws Exception {
		Double scaler = 1.0;
		Double variance = 0.0;

		String modeString = mode.toString().toLowerCase(null);

		try {
			IArray item = plot.getDataItem(modeString).getData();
			scaler = item.getArrayMath().getMaximum();
		} catch (IOException ioe) {
			throw new Exception(ioe.getMessage());
		}

		switch (mode) {
			case TIME:
				// use time attribute value from dictionary:
				//    /entry/data/time, or
				//    /entry/instrument/detector/time
				variance = 0.0;
				break;
		
			case PRESET:
				// use preset assuming(!) monitor was used
			case BM1_COUNTS:
			case BM2_COUNTS:
			case BM3_COUNTS:
				// use beam monitor count
				variance = scaler;
				break;
		
			default:
				// throw exception ? 
				break;
		}
		return new EData<Double>(scaler,variance);
	}
	
	private Plot applyThreshold(Plot plot, double threshold) {
		/** TODO: Implement lower threshold - allows to set 0 threshold to 
		 *         prevent negative numbers
		 */
		return plot;
	}
	
	private Plot applySmoothing(Plot plot, int smoothingRadius) {
		/** TODO: Implement smoothing filter with radius parameter
		 */
		return plot;
	}
	
	/** Processor Options ------------------------------------------------ */

	public Boolean getBackgroundSubtract_skip() {
		return backgroundSubtract_skip;
	}

	public void setBackgroundSubtract_skip(Boolean backgroundSubtract_skip) {
		this.backgroundSubtract_skip = backgroundSubtract_skip;
	}

	public Boolean getBackgroundSubtract_stop() {
		return backgroundSubtract_stop;
	}

	public void setBackgroundSubtract_stop(Boolean backgroundSubtract_stop) {
		this.backgroundSubtract_stop = backgroundSubtract_stop;
	}

	/** Var Ports -------------------------------------------------------- */

	public Scaler getScaler() {
		return scaleMode;
	}

	public void setScaler(Scaler mode) {
		this.scaleMode = mode;
	}

	public boolean isUseThreshold() {
		return useThreshold;
	}

	public void setUseThreshold(boolean useThreshold) {
		this.useThreshold = useThreshold;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public boolean isUseSmoothing() {
		return useSmoothing;
	}

	public void setUseSmoothing(boolean useSmoothing) {
		this.useSmoothing = useSmoothing;
	}

	public int getSmoothingRadius() {
		return smoothingRadius;
	}

	public void setSmoothingRadius(int smoothingRadius) {
		this.smoothingRadius = smoothingRadius;
	}

	public Scaler getScaleMode() {
		return scaleMode;
	}

	public void setScaleMode(Scaler scaleMode) {
		this.scaleMode = scaleMode;
	}

	public String getBackgroundURI() {
		return bkgFile.toString();
	}

	public void setBackgroundURI(String bkgFilename) {
		this.bkgFile = URI.create(bkgFilename);
	}

	/** In Ports --------------------------------------------------------- */
	public void setPlot_in(Plot plot_in) {
		this.plot_in = plot_in;
	}

	/** Out Ports -------------------------------------------------------- */
	public Plot getPlot_out() {
		return plot_out;
	}
}