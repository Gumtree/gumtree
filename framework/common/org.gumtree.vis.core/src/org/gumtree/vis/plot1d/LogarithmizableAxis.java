/*******************************************************************************
 * Copyright (c) 2010 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 ******************************************************************************/
package org.gumtree.vis.plot1d;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Formatter;
import java.util.List;

import org.gumtree.vis.dataset.XYErrorDataset;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.axis.Tick;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

/**
 * A numerical axis that uses a logarithmic scale.
 */
public class LogarithmizableAxis extends NumberAxis {

    /** For serialization. */
    private static final long serialVersionUID = 2502918599004103054L;

    /** Useful constant for log(10). */
    public static final double LOG10_VALUE = Math.log(10.0);

    /** Smallest arbitrarily-close-to-zero value allowed. */
    public static final double SMALL_LOG_VALUE = 1e-8; //1e-100;

    /** Flag set true to allow negative values in data. */
    protected boolean allowNegativesFlag = false;

    /**
     * Flag set true make axis throw exception if any values are
     * <= 0 and 'allowNegativesFlag' is false.
     */
    protected boolean strictValuesFlag = true;

    /** Number formatter for generating numeric strings. */
    protected final NumberFormat numberFormatterObj
        = NumberFormat.getInstance();

    /** Flag set true for "1e#"-style tick labels. */
    protected boolean expTickLabelsFlag = false;

    /** Flag set true for "10^n"-style tick labels. */
    protected boolean log10TickLabelsFlag = false;

    /** True to make 'autoAdjustRange()' select "10^n" values. */
    protected boolean autoRangeNextLogFlag = false;

    /** Helper flag for log axis processing. */
    protected boolean smallLogFlag = false;

    private boolean isLogarithmic = true;
    /**
     * Creates a new axis.
     *
     * @param label  the axis label.
     */
    public LogarithmizableAxis(String label) {
        super(label);
        setupNumberFmtObj();      //setup number formatter obj
    }

    /**
     * Sets the 'allowNegativesFlag' flag; true to allow negative values
     * in data, false to be able to plot positive values arbitrarily close to
     * zero.
     *
     * @param flgVal  the new value of the flag.
     */
    public void setAllowNegativesFlag(boolean flgVal) {
        this.allowNegativesFlag = flgVal;
    }

    /**
     * Returns the 'allowNegativesFlag' flag; true to allow negative values
     * in data, false to be able to plot positive values arbitrarily close
     * to zero.
     *
     * @return The flag.
     */
    public boolean getAllowNegativesFlag() {
        return this.allowNegativesFlag;
    }

    /**
     * Sets the 'strictValuesFlag' flag; if true and 'allowNegativesFlag'
     * is false then this axis will throw a runtime exception if any of its
     * values are less than or equal to zero; if false then the axis will
     * adjust for values less than or equal to zero as needed.
     *
     * @param flgVal true for strict enforcement.
     */
    public void setStrictValuesFlag(boolean flgVal) {
        this.strictValuesFlag = flgVal;
    }

    /**
     * Returns the 'strictValuesFlag' flag; if true and 'allowNegativesFlag'
     * is false then this axis will throw a runtime exception if any of its
     * values are less than or equal to zero; if false then the axis will
     * adjust for values less than or equal to zero as needed.
     *
     * @return <code>true</code> if strict enforcement is enabled.
     */
    public boolean getStrictValuesFlag() {
        return this.strictValuesFlag;
    }

    /**
     * Sets the 'expTickLabelsFlag' flag.  If the 'log10TickLabelsFlag'
     * is false then this will set whether or not "1e#"-style tick labels
     * are used.  The default is to use regular numeric tick labels.
     *
     * @param flgVal true for "1e#"-style tick labels, false for
     * log10 or regular numeric tick labels.
     */
    public void setExpTickLabelsFlag(boolean flgVal) {
        this.expTickLabelsFlag = flgVal;
        setupNumberFmtObj();             //setup number formatter obj
    }

    /**
     * Returns the 'expTickLabelsFlag' flag.
     *
     * @return <code>true</code> for "1e#"-style tick labels,
     *         <code>false</code> for log10 or regular numeric tick labels.
     */
    public boolean getExpTickLabelsFlag() {
      return this.expTickLabelsFlag;
    }

    /**
     * Sets the 'log10TickLabelsFlag' flag.  The default value is false.
     *
     * @param flag true for "10^n"-style tick labels, false for "1e#"-style
     * or regular numeric tick labels.
     */
    public void setLog10TickLabelsFlag(boolean flag) {
        this.log10TickLabelsFlag = flag;
    }

    /**
     * Returns the 'log10TickLabelsFlag' flag.
     *
     * @return <code>true</code> for "10^n"-style tick labels,
     *         <code>false</code> for "1e#"-style or regular numeric tick
     *         labels.
     */
    public boolean getLog10TickLabelsFlag() {
        return this.log10TickLabelsFlag;
    }

    /**
     * Sets the 'autoRangeNextLogFlag' flag.  This determines whether or
     * not the 'autoAdjustRange()' method will select the next "10^n"
     * values when determining the upper and lower bounds.  The default
     * value is false.
     *
     * @param flag <code>true</code> to make the 'autoAdjustRange()'
     * method select the next "10^n" values, <code>false</code> to not.
     */
    public void setAutoRangeNextLogFlag(boolean flag) {
        this.autoRangeNextLogFlag = flag;
    }

    /**
     * Returns the 'autoRangeNextLogFlag' flag.
     *
     * @return <code>true</code> if the 'autoAdjustRange()' method will
     * select the next "10^n" values, <code>false</code> if not.
     */
    public boolean getAutoRangeNextLogFlag() {
        return this.autoRangeNextLogFlag;
    }

    /**
     * Overridden version that calls original and then sets up flag for
     * log axis processing.
     *
     * @param range  the new range.
     */
    public void setRange(Range range) {
        super.setRange(range);      // call parent method
        if (isLogarithmic) {
        	setupSmallLogFlag();        // setup flag based on bounds values
        }
    }

    /**
     * Sets up flag for log axis processing.  Set true if negative values
     * not allowed and the lower bound is between 0 and 10.
     */
    protected void setupSmallLogFlag() {
        // set flag true if negative values not allowed and the
        // lower bound is between 0 and 10:
        double lowerVal = getRange().getLowerBound();
//        this.smallLogFlag = (!this.allowNegativesFlag && lowerVal < 10.0
//                && lowerVal > 0.0);
        this.smallLogFlag = (lowerVal < 10.0 && lowerVal >= 0.0);
    }

    /**
     * Sets up the number formatter object according to the
     * 'expTickLabelsFlag' flag.
     */
    protected void setupNumberFmtObj() {
        if (this.numberFormatterObj instanceof DecimalFormat) {
            //setup for "1e#"-style tick labels or regular
            // numeric tick labels, depending on flag:
            ((DecimalFormat) this.numberFormatterObj).applyPattern(
                    this.expTickLabelsFlag ? "0E0" : "0.###");
        }
    }

    /**
     * Returns the log10 value, depending on if values between 0 and
     * 1 are being plotted.  If negative values are not allowed and
     * the lower bound is between 0 and 10 then a normal log is
     * returned; otherwise the returned value is adjusted if the
     * given value is less than 10.
     *
     * @param val the value.
     *
     * @return log<sub>10</sub>(val).
     *
     * @see #switchedPow10(double)
     */
    protected double switchedLog10(double val) {
        return this.smallLogFlag ? Math.log(val)
                / LOG10_VALUE : adjustedLog10(val);
    }

    /**
     * Returns a power of 10, depending on if values between 0 and
     * 1 are being plotted.  If negative values are not allowed and
     * the lower bound is between 0 and 10 then a normal power is
     * returned; otherwise the returned value is adjusted if the
     * given value is less than 1.
     *
     * @param val the value.
     *
     * @return 10<sup>val</sup>.
     *
     * @since 1.0.5
     * @see #switchedLog10(double)
     */
    public double switchedPow10(double val) {
        return this.smallLogFlag ? Math.pow(10.0, val) : adjustedPow10(val);
    }

    /**
     * Returns an adjusted log10 value for graphing purposes.  The first
     * adjustment is that negative values are changed to positive during
     * the calculations, and then the answer is negated at the end.  The
     * second is that, for values less than 10, an increasingly large
     * (0 to 1) scaling factor is added such that at 0 the value is
     * adjusted to 1, resulting in a returned result of 0.
     *
     * @param val  value for which log10 should be calculated.
     *
     * @return An adjusted log<sub>10</sub>(val).
     *
     * @see #adjustedPow10(double)
     */
    public double adjustedLog10(double val) {
        boolean negFlag = (val < 0.0);
        if (negFlag) {
            val = -val;          // if negative then set flag and make positive
        }
        if (val < 10.0) {                // if < 10 then
            val += (10.0 - val) / 10.0;  //increase so 0 translates to 0
        }
        //return value; negate if original value was negative:
        double res = Math.log(val) / LOG10_VALUE;
        return negFlag ? (-res) : res;
    }

    /**
     * Returns an adjusted power of 10 value for graphing purposes.  The first
     * adjustment is that negative values are changed to positive during
     * the calculations, and then the answer is negated at the end.  The
     * second is that, for values less than 1, a progressive logarithmic
     * offset is subtracted such that at 0 the returned result is also 0.
     *
     * @param val  value for which power of 10 should be calculated.
     *
     * @return An adjusted 10<sup>val</sup>.
     *
     * @since 1.0.5
     * @see #adjustedLog10(double)
     */
    public double adjustedPow10(double val) {
        boolean negFlag = (val < 0.0);
        if (negFlag) {
            val = -val; // if negative then set flag and make positive
        }
        double res;
        if (val < 1.0) {
            res = (Math.pow(10, val + 1.0) - 10.0) / 9.0; //invert adjustLog10
        }
        else {
            res = Math.pow(10, val);
        }
        return negFlag ? (-res) : res;
    }

    /**
     * Returns the largest (closest to positive infinity) double value that is
     * not greater than the argument, is equal to a mathematical integer and
     * satisfying the condition that log base 10 of the value is an integer
     * (i.e., the value returned will be a power of 10: 1, 10, 100, 1000, etc.).
     *
     * @param lower a double value below which a floor will be calcualted.
     *
     * @return 10<sup>N</sup> with N .. { 1 ... }
     */
    protected double computeLogFloor(double lower) {

        double logFloor;
        if (this.allowNegativesFlag) {
            //negative values are allowed
            if (lower > 10.0) {   //parameter value is > 10
                // The Math.log() function is based on e not 10.
                logFloor = Math.log(lower) / LOG10_VALUE;
                logFloor = Math.floor(logFloor);
                logFloor = Math.pow(10, logFloor);
            }
            else if (lower < -10.0) {   //parameter value is < -10
                //calculate log using positive value:
                logFloor = Math.log(-lower) / LOG10_VALUE;
                //calculate floor using negative value:
                logFloor = Math.floor(-logFloor);
                //calculate power using positive value; then negate
                logFloor = -Math.pow(10, -logFloor);
            }
            else {
                //parameter value is -10 > val < 10
                logFloor = Math.floor(lower);   //use as-is
            }
        }
        else {
            //negative values not allowed
            if (lower > 0.0) {   //parameter value is > 0
                // The Math.log() function is based on e not 10.
                logFloor = Math.log(lower) / LOG10_VALUE;
                logFloor = Math.floor(logFloor);
                logFloor = Math.pow(10, logFloor);
            }
            else {
                //parameter value is <= 0
                logFloor = Math.floor(lower);   //use as-is
            }
        }
        return logFloor;
    }

    /**
     * Returns the smallest (closest to negative infinity) double value that is
     * not less than the argument, is equal to a mathematical integer and
     * satisfying the condition that log base 10 of the value is an integer
     * (i.e., the value returned will be a power of 10: 1, 10, 100, 1000, etc.).
     *
     * @param upper a double value above which a ceiling will be calcualted.
     *
     * @return 10<sup>N</sup> with N .. { 1 ... }
     */
    protected double computeLogCeil(double upper) {

        double logCeil;
        if (this.allowNegativesFlag) {
            //negative values are allowed
            if (upper > 10.0) {
                //parameter value is > 10
                // The Math.log() function is based on e not 10.
                logCeil = Math.log(upper) / LOG10_VALUE;
                logCeil = Math.ceil(logCeil);
                logCeil = Math.pow(10, logCeil);
            }
            else if (upper < -10.0) {
                //parameter value is < -10
                //calculate log using positive value:
                logCeil = Math.log(-upper) / LOG10_VALUE;
                //calculate ceil using negative value:
                logCeil = Math.ceil(-logCeil);
                //calculate power using positive value; then negate
                logCeil = -Math.pow(10, -logCeil);
            }
            else {
               //parameter value is -10 > val < 10
               logCeil = Math.ceil(upper);     //use as-is
            }
        }
        else {
            //negative values not allowed
            if (upper > 0.0) {
                //parameter value is > 0
                // The Math.log() function is based on e not 10.
                logCeil = Math.log(upper) / LOG10_VALUE;
                logCeil = Math.ceil(logCeil);
                logCeil = Math.pow(10, logCeil);
            }
            else {
                //parameter value is <= 0
                logCeil = Math.ceil(upper);     //use as-is
            }
        }
        return logCeil;
    }

    @Override
    protected void setAutoRange(boolean auto, boolean notify) {
    	if (isAutoRange() == auto) {
    		if (auto) {
    			autoAdjustRange();
    		} 
    		if (notify) {
                notifyListeners(new AxisChangeEvent(this));
            }
    	} else {
    		super.setAutoRange(auto, notify);
    	}
    }
    
    @Override
    public void autoAdjustRange() {
    	if (isLogarithmic) {
    		autoAdjustLogRange();
    	} else {
    		super.autoAdjustRange();
    	}
    }
    
    /**
     * Rescales the axis to ensure that all data is visible.
     */
    public void autoAdjustLogRange() {

        Plot plot = getPlot();
        if (plot == null) {
            return;  // no plot, no data.
        }

        if (plot instanceof ValueAxisPlot) {
            ValueAxisPlot vap = (ValueAxisPlot) plot;

            double lower;
            Range r = vap.getDataRange(this);
            if (r == null) {
                   //no real data present
                r = getDefaultAutoRange();
                lower = r.getLowerBound();    //get lower bound value
            }
            else {
                //actual data is present
                lower = r.getLowerBound();    //get lower bound value
                if (this.strictValuesFlag
                        && !this.allowNegativesFlag && lower <= 0.0) {
                    //strict flag set, allow-negatives not set and values <= 0
                    throw new RuntimeException("Values less than or equal to "
                            + "zero not allowed with logarithmic axis");
                }
            }

            //apply lower margin by decreasing lower bound:
            final double lowerMargin;
            if (lower > 0.0 && (lowerMargin = getLowerMargin()) > 0.0) {
                   //lower bound and margin OK; get log10 of lower bound
                final double logLower = (Math.log(lower) / LOG10_VALUE);
                double logAbs;      //get absolute value of log10 value
                if ((logAbs = Math.abs(logLower)) < 1.0) {
                    logAbs = 1.0;     //if less than 1.0 then make it 1.0
                }              //subtract out margin and get exponential value:
                lower = Math.pow(10, (logLower - (logAbs * lowerMargin)));
            }
            
            // skip values = 0 for log scale.
            if (lower < 10.0 && lower == 0) {
                double minPositive = Double.NaN;
            	if (plot instanceof XYPlot) {
            		XYDataset dataset = ((XYPlot) plot).getDataset();
            		if (dataset instanceof XYErrorDataset) {
            			minPositive = ((XYErrorDataset) dataset).getMinPositiveValue();
            		}
            	}
            	if (!Double.isNaN(minPositive)) {
            		lower = minPositive < SMALL_LOG_VALUE ? SMALL_LOG_VALUE : minPositive;
                    final double margin;
                    if (lower > 0.0 && (margin = getLowerMargin()) > 0.0) {
                           //lower bound and margin OK; get log10 of lower bound
                        final double logLower = (Math.log(lower) / LOG10_VALUE);
                        double logAbs;      //get absolute value of log10 value
                        if ((logAbs = Math.abs(logLower)) < 1.0) {
                            logAbs = 1.0;     //if less than 1.0 then make it 1.0
                        }              //subtract out margin and get exponential value:
                        lower = Math.pow(10, (logLower - (logAbs * margin)));
                    }
            	}
            }
            //if flag then change to log version of lowest value
            // to make range begin at a 10^n value:
            if (this.autoRangeNextLogFlag) {
                lower = computeLogFloor(lower);
            }

            if (!this.allowNegativesFlag && lower >= 0.0
                    && lower < SMALL_LOG_VALUE) {
                //negatives not allowed and lower range bound is zero
                lower = r.getLowerBound();    //use data range bound instead
            }

            double upper = r.getUpperBound();

             //apply upper margin by increasing upper bound:
            final double upperMargin;
            if (upper > 0.0 && (upperMargin = getUpperMargin()) > 0.0) {
                   //upper bound and margin OK; get log10 of upper bound
                final double logUpper = (Math.log(upper) / LOG10_VALUE);
                double logAbs;      //get absolute value of log10 value
                if ((logAbs = Math.abs(logUpper)) < 1.0) {
                    logAbs = 1.0;     //if less than 1.0 then make it 1.0
                }              //add in margin and get exponential value:
                upper = Math.pow(10, (logUpper + (logAbs * upperMargin)));
            }

            if (!this.allowNegativesFlag && upper < 1.0 && upper > 0.0
                    && lower > 0.0) {
                //negatives not allowed and upper bound between 0 & 1
                //round up to nearest significant digit for bound:
                //get negative exponent:
                double expVal = Math.log(upper) / LOG10_VALUE;
                expVal = Math.ceil(-expVal + 0.001); //get positive exponent
                expVal = Math.pow(10, expVal);      //create multiplier value
                //multiply, round up, and divide for bound value:
                upper = (expVal > 0.0) ? Math.ceil(upper * expVal) / expVal
                    : Math.ceil(upper);
            } else if (upper < 1.0 && upper > 0.0) {
            	double expVal = Math.log(upper) / LOG10_VALUE;
                expVal = Math.ceil(-expVal + 0.001); //get positive exponent
                expVal = Math.pow(10, expVal);      //create multiplier value
                //multiply, round up, and divide for bound value:
                upper = (expVal > 0.0) ? Math.round(upper * expVal) / expVal
                    : Math.ceil(upper);
            } else {
                //negatives allowed or upper bound not between 0 & 1
                //if flag then change to log version of highest value to
                // make range begin at a 10^n value; else use nearest int
                upper = (this.autoRangeNextLogFlag) ? computeLogCeil(upper)
                    : Math.ceil(upper);
            }
            // ensure the autorange is at least <minRange> in size...
            double minRange = getAutoRangeMinimumSize();
            if (upper - lower < minRange) {
                upper = (upper + lower + minRange) / 2;
                lower = (upper + lower - minRange) / 2;
                //if autorange still below minimum then adjust by 1%
                // (can be needed when minRange is very small):
                if (upper - lower < minRange) {
                    double absUpper = Math.abs(upper);
                    //need to account for case where upper==0.0
                    double adjVal = (absUpper > SMALL_LOG_VALUE) ? absUpper
                        / 100.0 : 0.01;
                    upper = (upper + lower + adjVal) / 2;
                    lower = (upper + lower - adjVal) / 2;
                }
            }

//            setRange(new Range(Math.min(lower, upper), Math.max(lower, upper)), false, false);
            if (lower <= upper) {
            	setRange(new Range(lower, upper), false, false);
            }
            setupSmallLogFlag();       //setup flag based on bounds values
        }
    }

    @Override
    public double valueToJava2D(double value, Rectangle2D area,
    		RectangleEdge edge) {
    	if (isLogarithmic) {
    		return logValueToJava2D(value, area, edge);
    	} else {
    		return super.valueToJava2D(value, area, edge);
    	}
    }
    
    /**
     * Converts a data value to a coordinate in Java2D space, assuming that
     * the axis runs along one edge of the specified plotArea.
     * Note that it is possible for the coordinate to fall outside the
     * plotArea.
     *
     * @param value  the data value.
     * @param plotArea  the area for plotting the data.
     * @param edge  the axis location.
     *
     * @return The Java2D coordinate.
     */
    public double logValueToJava2D(double value, Rectangle2D plotArea,
                                RectangleEdge edge) {

        Range range = getRange();
        double axisMin = switchedLog10(range.getLowerBound());
        double axisMax = switchedLog10(range.getUpperBound());

        double min = 0.0;
        double max = 0.0;
        if (RectangleEdge.isTopOrBottom(edge)) {
            min = plotArea.getMinX();
            max = plotArea.getMaxX();
        }
        else if (RectangleEdge.isLeftOrRight(edge)) {
            min = plotArea.getMaxY();
            max = plotArea.getMinY();
        }

        value = switchedLog10(value);

        if (isInverted()) {
            return max - (((value - axisMin) / (axisMax - axisMin))
                    * (max - min));
        }
        else {
            return min + (((value - axisMin) / (axisMax - axisMin))
                    * (max - min));
        }

    }

    @Override
    public double java2DToValue(double java2dValue, Rectangle2D area,
    		RectangleEdge edge) {
    	if (isLogarithmic) {
    		return java2DToLogValue(java2dValue, area, edge);
    	} else {
    		return super.java2DToValue(java2dValue, area, edge);
    	}
    }
    
    /**
     * Converts a coordinate in Java2D space to the corresponding data
     * value, assuming that the axis runs along one edge of the specified
     * plotArea.
     *
     * @param java2DValue  the coordinate in Java2D space.
     * @param plotArea  the area in which the data is plotted.
     * @param edge  the axis location.
     *
     * @return The data value.
     */
    public double java2DToLogValue(double java2DValue, Rectangle2D plotArea,
                                RectangleEdge edge) {

        Range range = getRange();
        double axisMin = switchedLog10(range.getLowerBound());
        double axisMax = switchedLog10(range.getUpperBound());

        double plotMin = 0.0;
        double plotMax = 0.0;
        if (RectangleEdge.isTopOrBottom(edge)) {
            plotMin = plotArea.getX();
            plotMax = plotArea.getMaxX();
        }
        else if (RectangleEdge.isLeftOrRight(edge)) {
            plotMin = plotArea.getMaxY();
            plotMax = plotArea.getMinY();
        }

        if (isInverted()) {
            return switchedPow10(axisMax - ((java2DValue - plotMin)
                    / (plotMax - plotMin)) * (axisMax - axisMin));
        }
        else {
            return switchedPow10(axisMin + ((java2DValue - plotMin)
                    / (plotMax - plotMin)) * (axisMax - axisMin));
        }
    }

    @Override
    public void zoomRange(double lowerPercent, double upperPercent) {
    	if (isLogarithmic) {
    		logZoomRange(lowerPercent, upperPercent);
    	} else {
    		super.zoomRange(lowerPercent, upperPercent);
    	}
    }
    
    /**
     * Zooms in on the current range.
     *
     * @param lowerPercent  the new lower bound.
     * @param upperPercent  the new upper bound.
     */
    public void logZoomRange(double lowerPercent, double upperPercent) {
        double startLog = switchedLog10(getRange().getLowerBound());
        double lengthLog = switchedLog10(getRange().getUpperBound()) - startLog;
        Range adjusted;

        if (isInverted()) {
            adjusted = new Range(
                    switchedPow10(
                            startLog + (lengthLog * (1 - upperPercent))),
                    switchedPow10(
                            startLog + (lengthLog * (1 - lowerPercent))));
        }
        else {
            adjusted = new Range(
                    switchedPow10(startLog + (lengthLog * lowerPercent)),
                    switchedPow10(startLog + (lengthLog * upperPercent)));
        }

        setRange(adjusted);
    }

    @Override
    protected List refreshTicksHorizontal(Graphics2D g2, Rectangle2D dataArea,
    		RectangleEdge edge) {
    	if (isLogarithmic) {
    		return newRefreshTicksHorizontal(g2, dataArea, edge);
    	} else {
    		return super.refreshTicksHorizontal(g2, dataArea, edge);
    	}
    }
   
	protected List newRefreshTicksHorizontal(Graphics2D g2, Rectangle2D dataArea,
			RectangleEdge edge) {
		// TODO Auto-generated method stub
        List ticks = new java.util.ArrayList();
        Range range = getRange();

        //get lower bound value:
        double lowerBoundVal = range.getLowerBound();
              //if small log values and lower bound value too small
              // then set to a small value (don't allow <= 0):
        if (this.smallLogFlag && lowerBoundVal < SMALL_LOG_VALUE) {
            lowerBoundVal = SMALL_LOG_VALUE;
        }

        //get upper bound value
        double upperBoundVal = range.getUpperBound();

        //get log10 version of lower bound and round to integer:
        int iBegCount = (int) Math.rint(switchedLog10(lowerBoundVal));
        //get log10 version of upper bound and round to integer:
        int iEndCount = (int) Math.rint(switchedLog10(upperBoundVal));

//        if (iBegCount == iEndCount && iBegCount >= 0
        if (iBegCount == iEndCount 
                && Math.pow(10, iBegCount) > lowerBoundVal) {
              //only 1 power of 10 value, it's > 0 and its resulting
              // tick value will be larger than lower bound of data
            --iBegCount;       //decrement to generate more ticks
        }

        int numberOfGrids = 0;
        int numberOfTicks = 0;
        NumberTick lastTick = null;

        double currentTickValue;
        String tickLabel;
        boolean zeroTickFlag = false;
        for (int i = iBegCount; i <= iEndCount; i++) {
            //for each power of 10 value; create ten ticks
            for (int j = 0; j < 10; ++j) {
                //for each tick to be displayed
                if (this.smallLogFlag) {
                    //small log values in use; create numeric value for tick
                    currentTickValue = Math.pow(10, i) + (Math.pow(10, i) * j);
                    if (this.expTickLabelsFlag
                        || (i < 0 && currentTickValue > 0.0
                        && currentTickValue < 1.0)) {
                        //showing "1e#"-style ticks or negative exponent
                        // generating tick value between 0 & 1; show fewer
                        if (j == 0 || (i > -4 && (j < 2 || j == 4))
                                   || currentTickValue >= upperBoundVal) {
                          //first tick of series, or not too small a value and
                          // one of first 3 ticks, or last tick to be displayed
                            // set exact number of fractional digits to be shown
                            // (no effect if showing "1e#"-style ticks):
                            this.numberFormatterObj
                                .setMaximumFractionDigits(-i);
                               //create tick label (force use of fmt obj):
                            tickLabel = makeTickLabel(currentTickValue, true);
                        }
                        else {    //no tick label to be shown
//                            tickLabel = "";
                        	if (numberOfTicks == 0){
                        		tickLabel = makeTickLabel(currentTickValue, true);
                        	}else
                        		tickLabel = "";
                        }
                    }
                    else {     //tick value not between 0 & 1
                               //show tick label if it's the first or last in
                               // the set, or if it's 1-5; beyond that show
                               // fewer as the values get larger:
                        tickLabel = (j < 1 || (i < 1 && j < 5) || (j < 4 - i)
                                         || currentTickValue >= upperBoundVal 
                                         || numberOfTicks == 0)
                                         ? makeTickLabel(currentTickValue) : "";
                    }
                }
                else { //not small log values in use; allow for values <= 0
                    if (zeroTickFlag) {   //if did zero tick last iter then
                        --j;              //decrement to do 1.0 tick now
                    }     //calculate power-of-ten value for tick:
                    currentTickValue = (i >= 0)
                        ? Math.pow(10, i) + (Math.pow(10, i) * j)
                        : -(Math.pow(10, -i) - (Math.pow(10, -i - 1) * j));
                    if (!zeroTickFlag) {  // did not do zero tick last iteration
                        if (Math.abs(currentTickValue - 1.0) < 0.0001
                            && lowerBoundVal <= 0.0 && upperBoundVal >= 0.0) {
                            //tick value is 1.0 and 0.0 is within data range
                            currentTickValue = 0.0;     //set tick value to zero
                            zeroTickFlag = true;        //indicate zero tick
                        }
                    }
                    else {     //did zero tick last iteration
                        zeroTickFlag = false;         //clear flag
                    }               //create tick label string:
                               //show tick label if "1e#"-style and it's one
                               // of the first two, if it's the first or last
                               // in the set, or if it's 1-5; beyond that
                               // show fewer as the values get larger:
                    tickLabel = ((this.expTickLabelsFlag && j < 2)
                                || j < 1
                                || (i < 1 && j < 5) || (j < 4 - i)
                                || currentTickValue >= upperBoundVal
                                || numberOfTicks == 0)
                                   ? makeTickLabel(currentTickValue) : "";
                }

                if (currentTickValue > upperBoundVal) {
                	if (lastTick != null){
                		String lastTickText = lastTick.getText();
                		if (lastTickText == null || lastTickText.trim().length() == 0){
                			ticks.remove(lastTick);
                			ticks.add(new NumberTick(lastTick.getValue(), 
                					createTickLabel(lastTick.getValue(), i - 1), lastTick.getTextAnchor(),
                					lastTick.getRotationAnchor(), lastTick.getAngle()));
                		}
                	}
                	if (numberOfTicks < 4){
                		return getAllTicksHorizontal(g2, dataArea, edge);
                    }
                    return ticks;   // if past highest data value then exit
                                    // method
                }

                if (currentTickValue >= lowerBoundVal - SMALL_LOG_VALUE) {
                    //tick value not below lowest data value
                    TextAnchor anchor = null;
                    TextAnchor rotationAnchor = null;
                    double angle = 0.0;
                    if (isVerticalTickLabels()) {
                        anchor = TextAnchor.CENTER_RIGHT;
                        rotationAnchor = TextAnchor.CENTER_RIGHT;
                        if (edge == RectangleEdge.TOP) {
                            angle = Math.PI / 2.0;
                        }
                        else {
                            angle = -Math.PI / 2.0;
                        }
                    }
                    else {
                        if (edge == RectangleEdge.TOP) {
                            anchor = TextAnchor.BOTTOM_CENTER;
                            rotationAnchor = TextAnchor.BOTTOM_CENTER;
                        }
                        else {
                            anchor = TextAnchor.TOP_CENTER;
                            rotationAnchor = TextAnchor.TOP_CENTER;
                        }
                    }

                    lastTick = new NumberTick(new Double(currentTickValue),
                            tickLabel, anchor, rotationAnchor, angle);
                    ticks.add(lastTick);
                    if (tickLabel != null && tickLabel.trim().length() > 0)
                    	numberOfTicks ++;
                    numberOfGrids ++;
                }
            }
        }
        if (numberOfTicks < 4){
        	return getAllTicksHorizontal(g2, dataArea, edge);
        }
        return ticks;	
	}
	
    /**
     * Calculates the positions of the tick labels for the axis, storing the
     * results in the tick label list (ready for drawing).
     *
     * @param g2  the graphics device.
     * @param dataArea  the area in which the plot should be drawn.
     * @param edge  the location of the axis.
     *
     * @return A list of ticks.
     */
    protected List logRefreshTicksHorizontal(Graphics2D g2,
                                          Rectangle2D dataArea,
                                          RectangleEdge edge) {

        List ticks = new java.util.ArrayList();
        Range range = getRange();

        //get lower bound value:
        double lowerBoundVal = range.getLowerBound();
              //if small log values and lower bound value too small
              // then set to a small value (don't allow <= 0):
        if (this.smallLogFlag && lowerBoundVal < SMALL_LOG_VALUE) {
            lowerBoundVal = SMALL_LOG_VALUE;
        }

        //get upper bound value
        double upperBoundVal = range.getUpperBound();

        //get log10 version of lower bound and round to integer:
        int iBegCount = (int) Math.rint(switchedLog10(lowerBoundVal));
        //get log10 version of upper bound and round to integer:
        int iEndCount = (int) Math.rint(switchedLog10(upperBoundVal));

        if (iBegCount == iEndCount && iBegCount > 0
                && Math.pow(10, iBegCount) > lowerBoundVal) {
              //only 1 power of 10 value, it's > 0 and its resulting
              // tick value will be larger than lower bound of data
            --iBegCount;       //decrement to generate more ticks
        }

        double currentTickValue;
        String tickLabel;
        boolean zeroTickFlag = false;
        for (int i = iBegCount; i <= iEndCount; i++) {
            //for each power of 10 value; create ten ticks
            for (int j = 0; j < 10; ++j) {
                //for each tick to be displayed
                if (this.smallLogFlag) {
                    //small log values in use; create numeric value for tick
                    currentTickValue = Math.pow(10, i) + (Math.pow(10, i) * j);
                    if (this.expTickLabelsFlag
                        || (i < 0 && currentTickValue > 0.0
                        && currentTickValue < 1.0)) {
                        //showing "1e#"-style ticks or negative exponent
                        // generating tick value between 0 & 1; show fewer
                        if (j == 0 || (i > -4 && j < 2)
                                   || currentTickValue >= upperBoundVal) {
                          //first tick of series, or not too small a value and
                          // one of first 3 ticks, or last tick to be displayed
                            // set exact number of fractional digits to be shown
                            // (no effect if showing "1e#"-style ticks):
                            this.numberFormatterObj
                                .setMaximumFractionDigits(-i);
                               //create tick label (force use of fmt obj):
                            tickLabel = makeTickLabel(currentTickValue, true);
                        }
                        else {    //no tick label to be shown
                            tickLabel = "";
                        }
                    }
                    else {     //tick value not between 0 & 1
                               //show tick label if it's the first or last in
                               // the set, or if it's 1-5; beyond that show
                               // fewer as the values get larger:
                        tickLabel = (j < 1 || (i < 1 && j < 5) || (j < 4 - i)
                                         || currentTickValue >= upperBoundVal)
                                         ? makeTickLabel(currentTickValue) : "";
                    }
                }
                else { //not small log values in use; allow for values <= 0
                    if (zeroTickFlag) {   //if did zero tick last iter then
                        --j;              //decrement to do 1.0 tick now
                    }     //calculate power-of-ten value for tick:
                    currentTickValue = (i >= 0)
                        ? Math.pow(10, i) + (Math.pow(10, i) * j)
                        : -(Math.pow(10, -i) - (Math.pow(10, -i - 1) * j));
                    if (!zeroTickFlag) {  // did not do zero tick last iteration
                        if (Math.abs(currentTickValue - 1.0) < 0.0001
                            && lowerBoundVal <= 0.0 && upperBoundVal >= 0.0) {
                            //tick value is 1.0 and 0.0 is within data range
                            currentTickValue = 0.0;     //set tick value to zero
                            zeroTickFlag = true;        //indicate zero tick
                        }
                    }
                    else {     //did zero tick last iteration
                        zeroTickFlag = false;         //clear flag
                    }               //create tick label string:
                               //show tick label if "1e#"-style and it's one
                               // of the first two, if it's the first or last
                               // in the set, or if it's 1-5; beyond that
                               // show fewer as the values get larger:
                    tickLabel = ((this.expTickLabelsFlag && j < 2)
                                || j < 1
                                || (i < 1 && j < 5) || (j < 4 - i)
                                || currentTickValue >= upperBoundVal)
                                   ? makeTickLabel(currentTickValue) : "";
                }

                if (currentTickValue > upperBoundVal) {
                    return ticks;   // if past highest data value then exit
                                    // method
                }

                if (currentTickValue >= lowerBoundVal - SMALL_LOG_VALUE) {
                    //tick value not below lowest data value
                    TextAnchor anchor = null;
                    TextAnchor rotationAnchor = null;
                    double angle = 0.0;
                    if (isVerticalTickLabels()) {
                        anchor = TextAnchor.CENTER_RIGHT;
                        rotationAnchor = TextAnchor.CENTER_RIGHT;
                        if (edge == RectangleEdge.TOP) {
                            angle = Math.PI / 2.0;
                        }
                        else {
                            angle = -Math.PI / 2.0;
                        }
                    }
                    else {
                        if (edge == RectangleEdge.TOP) {
                            anchor = TextAnchor.BOTTOM_CENTER;
                            rotationAnchor = TextAnchor.BOTTOM_CENTER;
                        }
                        else {
                            anchor = TextAnchor.TOP_CENTER;
                            rotationAnchor = TextAnchor.TOP_CENTER;
                        }
                    }

                    Tick tick = new NumberTick(new Double(currentTickValue),
                            tickLabel, anchor, rotationAnchor, angle);
                    ticks.add(tick);
                }
            }
        }
        return ticks;

    }

    @Override
    protected List refreshTicksVertical(Graphics2D g2, Rectangle2D dataArea,
    		RectangleEdge edge) {
    	if (isLogarithmic) {
    		return newRefreshTicksVertical(g2, dataArea, edge);
    	} else {
    		return super.refreshTicksVertical(g2, dataArea, edge);
    	}
    }
    
	protected List newRefreshTicksVertical(Graphics2D g2, Rectangle2D dataArea,
			RectangleEdge edge) {
        List ticks = new java.util.ArrayList();

        //get lower bound value:
        double lowerBoundVal = getRange().getLowerBound();
        //if small log values and lower bound value too small
        // then set to a small value (don't allow <= 0):
        if (this.smallLogFlag && lowerBoundVal < SMALL_LOG_VALUE) {
            lowerBoundVal = SMALL_LOG_VALUE;
        }
        //get upper bound value
        double upperBoundVal = getRange().getUpperBound();

        //get log10 version of lower bound and round to integer:
        int iBegCount = (int) Math.rint(switchedLog10(lowerBoundVal));
        //get log10 version of upper bound and round to integer:
        int iEndCount = (int) Math.rint(switchedLog10(upperBoundVal));

        if (iBegCount == iEndCount && iBegCount > 0
                && Math.pow(10, iBegCount) > lowerBoundVal) {
              //only 1 power of 10 value, it's > 0 and its resulting
              // tick value will be larger than lower bound of data
            --iBegCount;       //decrement to generate more ticks
        }

//      nxi@ansto: check how many ticks get produced. Put more if less than 4
        int numberOfGrids = 0;
        int numberOfTicks = 0;
        NumberTick lastTick = null;
        
        double tickVal;
        String tickLabel;
//        tickVal = lowerBoundVal;
//        
//        tickLabel = Long.toString((long) Math.rint(tickVal));
//        ticks.add(new NumberTick(new Double(tickVal), tickLabel,
//        		TextAnchor.CENTER_RIGHT, TextAnchor.CENTER_RIGHT, 0.0));
        boolean zeroTickFlag = false;
        for (int i = iBegCount; i <= iEndCount; i++) {
            //for each tick with a label to be displayed
            int jEndCount = 10;
            if (i == iEndCount) {
//                jEndCount = 1;
            }

            for (int j = 0; j < jEndCount; j++) {
                //for each tick to be displayed
                if (this.smallLogFlag) {
                    //small log values in use
                    tickVal = Math.pow(10, i) + (Math.pow(10, i) * j);
                    if (j == 0 || j == 1 || j == 4) {
                        //first tick of group; create label text
                        if (this.log10TickLabelsFlag) {
                            //if flag then
                            tickLabel = "10^" + i;   //create "log10"-type label
                        }
                        else {    //not "log10"-type label
                            if (this.expTickLabelsFlag) {
                                //if flag then
                                tickLabel = "1e" + i;  //create "1e#"-type label
                            }
                            else {    //not "1e#"-type label
                                if (i >= 0) {   // if positive exponent then
                                                // make integer
                                    NumberFormat format
                                        = getNumberFormatOverride();
                                    if (format != null) {
                                        tickLabel = format.format(tickVal);
                                    }
                                    else {
                                        tickLabel = Long.toString((long)
                                                Math.rint(tickVal));
                                    }
                                }
                                else {
                                    //negative exponent; create fractional value
                                    //set exact number of fractional digits to
                                    // be shown:
                                    this.numberFormatterObj
                                        .setMaximumFractionDigits(-i);
                                    //create tick label:
                                    tickLabel = this.numberFormatterObj.format(
                                            tickVal);
                                }
                            }
                        }
                    }
                    else {   //not first tick to be displayed
                    	if (numberOfTicks == 0)
                    		tickLabel = createTickLabel(tickVal, i);
                    	else
                    		tickLabel = "";           //no label
//                        tickLabel = "";     //no tick label
                    }
                }
                else { //not small log values in use; allow for values <= 0
                    if (zeroTickFlag) {      //if did zero tick last iter then
                        --j;
                    }               //decrement to do 1.0 tick now
                    tickVal = (i >= 0) ? Math.pow(10, i) + (Math.pow(10, i) * j)
                             : -(Math.pow(10, -i) - (Math.pow(10, -i - 1) * j));
                    if (j == 0 || j == 1 || j == 4) {  //first tick of group
                        if (!zeroTickFlag) {     // did not do zero tick last
                                                 // iteration
                            if (i > iBegCount && i < iEndCount
                                    && Math.abs(tickVal - 1.0) < 0.0001) {
                                // not first or last tick on graph and value
                                // is 1.0
                                tickVal = 0.0;        //change value to 0.0
                                zeroTickFlag = true;  //indicate zero tick
                                tickLabel = "0";      //create label for tick
                            }
                            else {
                                //first or last tick on graph or value is 1.0
                                //create label for tick:
                            	tickLabel = createTickLabel(tickVal, i);
                            }
                        }
                        else {     // did zero tick last iteration
                        	if (numberOfTicks == 0)
                        		tickLabel = createTickLabel(tickVal, i);
                        	else
                        		tickLabel = "";         //no label
                            zeroTickFlag = false;   //clear flag
                        }
                    }
                    else {       // not first tick of group
                    	if (numberOfTicks == 0)
                    		tickLabel = createTickLabel(tickVal, i);
                    	else
                    		tickLabel = "";           //no label
                        zeroTickFlag = false;     //make sure flag cleared
                    }
                }

                if (tickVal > upperBoundVal) {
                	if (lastTick != null){
                		String lastTickText = lastTick.getText();
                		double value = lastTick.getValue();
                		if (numberOfTicks < 8 || getFirstDigit(value) != 6)
                			if (lastTickText == null || lastTickText.trim().length() == 0){
                				ticks.remove(lastTick);
                				ticks.add(new NumberTick(lastTick.getValue(), 
                						createTickLabel(lastTick.getValue(), i - 1), lastTick.getTextAnchor(),
                						lastTick.getRotationAnchor(), lastTick.getAngle()));
                			}
                	}
                    if (numberOfTicks < 4){
                    	return getAllTicksVertical(g2, dataArea, edge);
                    }
                    return ticks;  //if past highest data value then exit method
                }

                if (tickVal >= lowerBoundVal - SMALL_LOG_VALUE) {
                    //tick value not below lowest data value
                    TextAnchor anchor = null;
                    TextAnchor rotationAnchor = null;
                    double angle = 0.0;
                    if (isVerticalTickLabels()) {
                        if (edge == RectangleEdge.LEFT) {
                            anchor = TextAnchor.BOTTOM_CENTER;
                            rotationAnchor = TextAnchor.BOTTOM_CENTER;
                            angle = -Math.PI / 2.0;
                        }
                        else {
                            anchor = TextAnchor.BOTTOM_CENTER;
                            rotationAnchor = TextAnchor.BOTTOM_CENTER;
                            angle = Math.PI / 2.0;
                        }
                    }
                    else {
                        if (edge == RectangleEdge.LEFT) {
                            anchor = TextAnchor.CENTER_RIGHT;
                            rotationAnchor = TextAnchor.CENTER_RIGHT;
                        }
                        else {
                            anchor = TextAnchor.CENTER_LEFT;
                            rotationAnchor = TextAnchor.CENTER_LEFT;
                        }
                    }
                    // nxi@ansto: create tick object and add to list:
                    lastTick = new NumberTick(new Double(tickVal), tickLabel,
                            anchor, rotationAnchor, angle);
                    ticks.add(lastTick);
                    if (tickLabel != null && tickLabel.trim().length() > 0)
                    	numberOfTicks ++;
                    numberOfGrids ++;
                }
            }
        }
        if (numberOfTicks < 4){
        	return getAllTicksVertical(g2, dataArea, edge);
        }
        return ticks;
    }

	private int getFirstDigit(double value) {
		double log = Math.log10(value);
		double base = Math.floor(log);
		return (int) (value / Math.pow(10, base));
	}

	private List getAllTicksVertical(Graphics2D g2, Rectangle2D dataArea,
			RectangleEdge edge) {
        List ticks = new java.util.ArrayList();

        //get lower bound value:
        double lowerBoundVal = getRange().getLowerBound();
        //if small log values and lower bound value too small
        // then set to a small value (don't allow <= 0):
        if (this.smallLogFlag && lowerBoundVal < SMALL_LOG_VALUE) {
            lowerBoundVal = SMALL_LOG_VALUE;
        }
        //get upper bound value
        double upperBoundVal = getRange().getUpperBound();

        //get log10 version of lower bound and round to integer:
        int iBegCount = (int) Math.rint(switchedLog10(lowerBoundVal));
        //get log10 version of upper bound and round to integer:
        int iEndCount = (int) Math.rint(switchedLog10(upperBoundVal));

        if (iBegCount == iEndCount && iBegCount > 0
                && Math.pow(10, iBegCount) > lowerBoundVal) {
              //only 1 power of 10 value, it's > 0 and its resulting
              // tick value will be larger than lower bound of data
            --iBegCount;       //decrement to generate more ticks
        }

        int numberOfGrids = 0;
        int numberOfTicks = 0;
        NumberTick lastTick = null;
        double tickVal;
        String tickLabel;
//        tickVal = lowerBoundVal;
//        
//        tickLabel = Long.toString((long) Math.rint(tickVal));
//        ticks.add(new NumberTick(new Double(tickVal), tickLabel,
//        		TextAnchor.CENTER_RIGHT, TextAnchor.CENTER_RIGHT, 0.0));
        boolean zeroTickFlag = false;
        for (int i = iBegCount; i <= iEndCount; i++) {
            //for each tick with a label to be displayed
            int jEndCount = 10;
            if (i == iEndCount) {
//                jEndCount = 1;
            }

            for (int j = 0; j < jEndCount; j++) {
                //for each tick to be displayed
                if (this.smallLogFlag) {
                    //small log values in use
                    tickVal = Math.pow(10, i) + (Math.pow(10, i) * j);
                        //first tick of group; create label text
                        if (this.log10TickLabelsFlag) {
                            //if flag then
                            tickLabel = "10^" + i;   //create "log10"-type label
                        }
                        else {    //not "log10"-type label
                            if (this.expTickLabelsFlag) {
                                //if flag then
                                tickLabel = "1e" + i;  //create "1e#"-type label
                            }
                            else {    //not "1e#"-type label
                                if (i >= 0) {   // if positive exponent then
                                                // make integer
                                    NumberFormat format
                                        = getNumberFormatOverride();
                                    if (format != null) {
                                        tickLabel = format.format(tickVal);
                                    }
                                    else {
                                        tickLabel = Long.toString((long)
                                                Math.rint(tickVal));
                                    }
                                }
                                else {
                                    //negative exponent; create fractional value
                                    //set exact number of fractional digits to
                                    // be shown:
                                    this.numberFormatterObj
                                        .setMaximumFractionDigits(-i);
                                    //create tick label:
                                    tickLabel = this.numberFormatterObj.format(
                                            tickVal);
                                }
                            }
                        }
                }
                else { //not small log values in use; allow for values <= 0
                    if (zeroTickFlag) {      //if did zero tick last iter then
                        --j;
                    }               //decrement to do 1.0 tick now
                    tickVal = (i >= 0) ? Math.pow(10, i) + (Math.pow(10, i) * j)
                             : -(Math.pow(10, -i) - (Math.pow(10, -i - 1) * j));
                        if (!zeroTickFlag) {     // did not do zero tick last
                                                 // iteration
                            if (i > iBegCount && i < iEndCount
                                    && Math.abs(tickVal - 1.0) < 0.0001) {
                                // not first or last tick on graph and value
                                // is 1.0
                                tickVal = 0.0;        //change value to 0.0
                                zeroTickFlag = true;  //indicate zero tick
                                tickLabel = "0";      //create label for tick
                            }
                            else {
                                //first or last tick on graph or value is 1.0
                                //create label for tick:
                            	tickLabel = createTickLabel(tickVal, i);
                            }
                    }
                    else {       // not first tick of group
                   		tickLabel = createTickLabel(tickVal, i);
                    }
                }

                if (tickVal > upperBoundVal) {
                	if (lastTick != null){
                		String lastTickText = lastTick.getText();
                		if (lastTickText == null || lastTickText.trim().length() == 0){
                			ticks.remove(lastTick);
                			ticks.add(new NumberTick(lastTick.getValue(), 
                					createTickLabel(lastTick.getValue(), i - 1), lastTick.getTextAnchor(),
                					lastTick.getRotationAnchor(), lastTick.getAngle()));
                		}
                	}
                    if (ticks.size() < 2){
                    	double definition = Math.abs(lowerBoundVal - upperBoundVal);
                    	int numberOfDigits = 0;
                    	if (definition >= 1)
                    		numberOfDigits = 0;
                    	else {
                    		numberOfDigits = (int) Math.ceil((- Math.log10(definition)));
                    	}
                    	tickVal = lowerBoundVal;
                    	if (definition > 1)
                    		tickLabel = Long.toString((long) Math.rint(tickVal));
                    	else
                    		tickLabel = (new Formatter()).format("%." + numberOfDigits + "f", tickVal).toString();
                    	ticks.add(new NumberTick(new Double(tickVal), tickLabel,
                    			TextAnchor.CENTER_RIGHT, TextAnchor.CENTER_RIGHT, 0.0));
                    	tickVal = upperBoundVal;
                    	if (definition > 1)
                    		tickLabel = Long.toString((long) Math.rint(tickVal));
                    	else
                    		tickLabel = (new Formatter()).format("%." + numberOfDigits + "f", tickVal).toString();
                    	ticks.add(new NumberTick(new Double(tickVal), tickLabel,
                    			TextAnchor.CENTER_RIGHT, TextAnchor.CENTER_RIGHT, 0.0));
                    }
                    return ticks;  //if past highest data value then exit method
                }

                if (tickVal >= lowerBoundVal - SMALL_LOG_VALUE) {
                    //tick value not below lowest data value
                    TextAnchor anchor = null;
                    TextAnchor rotationAnchor = null;
                    double angle = 0.0;
                    if (isVerticalTickLabels()) {
                        if (edge == RectangleEdge.LEFT) {
                            anchor = TextAnchor.BOTTOM_CENTER;
                            rotationAnchor = TextAnchor.BOTTOM_CENTER;
                            angle = -Math.PI / 2.0;
                        }
                        else {
                            anchor = TextAnchor.BOTTOM_CENTER;
                            rotationAnchor = TextAnchor.BOTTOM_CENTER;
                            angle = Math.PI / 2.0;
                        }
                    }
                    else {
                        if (edge == RectangleEdge.LEFT) {
                            anchor = TextAnchor.CENTER_RIGHT;
                            rotationAnchor = TextAnchor.CENTER_RIGHT;
                        }
                        else {
                            anchor = TextAnchor.CENTER_LEFT;
                            rotationAnchor = TextAnchor.CENTER_LEFT;
                        }
                    }
                    //create tick object and add to list:
                    lastTick = new NumberTick(new Double(tickVal), tickLabel,
                            anchor, rotationAnchor, angle);
                    ticks.add(lastTick);
                    if (tickLabel != null && tickLabel.trim().length() > 0)
                    	numberOfTicks ++;
                    numberOfGrids ++;
                }
            }
        }
        if (ticks.size() < 2){
        	double definition = Math.abs(lowerBoundVal - upperBoundVal);
        	int numberOfDigits = 0;
        	if (definition >= 1)
        		numberOfDigits = 0;
        	else {
        		numberOfDigits = (int) Math.ceil((- Math.log10(definition)));
        	}
        	tickVal = lowerBoundVal;
        	if (definition > 1)
        		tickLabel = Long.toString((long) Math.rint(tickVal));
        	else
        		tickLabel = (new Formatter()).format("%." + numberOfDigits + "f", tickVal).toString();
        	ticks.add(new NumberTick(new Double(tickVal), tickLabel,
        			TextAnchor.CENTER_RIGHT, TextAnchor.CENTER_RIGHT, 0.0));
        	tickVal = upperBoundVal;
        	if (definition > 1)
        		tickLabel = Long.toString((long) Math.rint(tickVal));
        	else
        		tickLabel = (new Formatter()).format("%." + numberOfDigits + "f", tickVal).toString();
        	ticks.add(new NumberTick(new Double(tickVal), tickLabel,
        			TextAnchor.CENTER_RIGHT, TextAnchor.CENTER_RIGHT, 0.0));
        }
        return ticks;
    }

    /**
     * Calculates the positions of the tick labels for the axis, storing the
     * results in the tick label list (ready for drawing).
     *
     * @param g2  the graphics device.
     * @param dataArea  the area in which the plot should be drawn.
     * @param edge  the location of the axis.
     *
     * @return A list of ticks.
     */
    protected List logRefreshTicksVertical(Graphics2D g2,
                                        Rectangle2D dataArea,
                                        RectangleEdge edge) {
        List ticks = new java.util.ArrayList();

        //get lower bound value:
        double lowerBoundVal = getRange().getLowerBound();
        //if small log values and lower bound value too small
        // then set to a small value (don't allow <= 0):
        if (this.smallLogFlag && lowerBoundVal < SMALL_LOG_VALUE) {
            lowerBoundVal = SMALL_LOG_VALUE;
        }
        //get upper bound value
        double upperBoundVal = getRange().getUpperBound();

        //get log10 version of lower bound and round to integer:
        int iBegCount = (int) Math.rint(switchedLog10(lowerBoundVal));
        //get log10 version of upper bound and round to integer:
        int iEndCount = (int) Math.rint(switchedLog10(upperBoundVal));

        if (iBegCount == iEndCount && iBegCount > 0
                && Math.pow(10, iBegCount) > lowerBoundVal) {
              //only 1 power of 10 value, it's > 0 and its resulting
              // tick value will be larger than lower bound of data
            --iBegCount;       //decrement to generate more ticks
        }

        double tickVal;
        String tickLabel;
        boolean zeroTickFlag = false;
        for (int i = iBegCount; i <= iEndCount; i++) {
            //for each tick with a label to be displayed
            int jEndCount = 10;
            if (i == iEndCount) {
                jEndCount = 1;
            }

            for (int j = 0; j < jEndCount; j++) {
                //for each tick to be displayed
                if (this.smallLogFlag) {
                    //small log values in use
                    tickVal = Math.pow(10, i) + (Math.pow(10, i) * j);
                    if (j == 0) {
                        //first tick of group; create label text
                        if (this.log10TickLabelsFlag) {
                            //if flag then
                            tickLabel = "10^" + i;   //create "log10"-type label
                        }
                        else {    //not "log10"-type label
                            if (this.expTickLabelsFlag) {
                                //if flag then
                                tickLabel = "1e" + i;  //create "1e#"-type label
                            }
                            else {    //not "1e#"-type label
                                if (i >= 0) {   // if positive exponent then
                                                // make integer
                                    NumberFormat format
                                        = getNumberFormatOverride();
                                    if (format != null) {
                                        tickLabel = format.format(tickVal);
                                    }
                                    else {
                                        tickLabel = Long.toString((long)
                                                Math.rint(tickVal));
                                    }
                                }
                                else {
                                    //negative exponent; create fractional value
                                    //set exact number of fractional digits to
                                    // be shown:
                                    this.numberFormatterObj
                                        .setMaximumFractionDigits(-i);
                                    //create tick label:
                                    tickLabel = this.numberFormatterObj.format(
                                            tickVal);
                                }
                            }
                        }
                    }
                    else {   //not first tick to be displayed
                        tickLabel = "";     //no tick label
                    }
                }
                else { //not small log values in use; allow for values <= 0
                    if (zeroTickFlag) {      //if did zero tick last iter then
                        --j;
                    }               //decrement to do 1.0 tick now
                    tickVal = (i >= 0) ? Math.pow(10, i) + (Math.pow(10, i) * j)
                             : -(Math.pow(10, -i) - (Math.pow(10, -i - 1) * j));
                    if (j == 0) {  //first tick of group
                        if (!zeroTickFlag) {     // did not do zero tick last
                                                 // iteration
                            if (i > iBegCount && i < iEndCount
                                    && Math.abs(tickVal - 1.0) < 0.0001) {
                                // not first or last tick on graph and value
                                // is 1.0
                                tickVal = 0.0;        //change value to 0.0
                                zeroTickFlag = true;  //indicate zero tick
                                tickLabel = "0";      //create label for tick
                            }
                            else {
                                //first or last tick on graph or value is 1.0
                                //create label for tick:
                                if (this.log10TickLabelsFlag) {
                                       //create "log10"-type label
                                    tickLabel = (((i < 0) ? "-" : "")
                                            + "10^" + Math.abs(i));
                                }
                                else {
                                    if (this.expTickLabelsFlag) {
                                           //create "1e#"-type label
                                        tickLabel = (((i < 0) ? "-" : "")
                                                + "1e" + Math.abs(i));
                                    }
                                    else {
                                        NumberFormat format
                                            = getNumberFormatOverride();
                                        if (format != null) {
                                            tickLabel = format.format(tickVal);
                                        }
                                        else {
                                            tickLabel =  Long.toString(
                                                    (long) Math.rint(tickVal));
                                        }
                                    }
                                }
                            }
                        }
                        else {     // did zero tick last iteration
                            tickLabel = "";         //no label
                            zeroTickFlag = false;   //clear flag
                        }
                    }
                    else {       // not first tick of group
                        tickLabel = "";           //no label
                        zeroTickFlag = false;     //make sure flag cleared
                    }
                }

                if (tickVal > upperBoundVal) {
                    return ticks;  //if past highest data value then exit method
                }

                if (tickVal >= lowerBoundVal - SMALL_LOG_VALUE) {
                    //tick value not below lowest data value
                    TextAnchor anchor = null;
                    TextAnchor rotationAnchor = null;
                    double angle = 0.0;
                    if (isVerticalTickLabels()) {
                        if (edge == RectangleEdge.LEFT) {
                            anchor = TextAnchor.BOTTOM_CENTER;
                            rotationAnchor = TextAnchor.BOTTOM_CENTER;
                            angle = -Math.PI / 2.0;
                        }
                        else {
                            anchor = TextAnchor.BOTTOM_CENTER;
                            rotationAnchor = TextAnchor.BOTTOM_CENTER;
                            angle = Math.PI / 2.0;
                        }
                    }
                    else {
                        if (edge == RectangleEdge.LEFT) {
                            anchor = TextAnchor.CENTER_RIGHT;
                            rotationAnchor = TextAnchor.CENTER_RIGHT;
                        }
                        else {
                            anchor = TextAnchor.CENTER_LEFT;
                            rotationAnchor = TextAnchor.CENTER_LEFT;
                        }
                    }
                    //create tick object and add to list:
                    ticks.add(new NumberTick(new Double(tickVal), tickLabel,
                            anchor, rotationAnchor, angle));
                }
            }
        }
        return ticks;
    }

    /**
     * Converts the given value to a tick label string.
     *
     * @param val the value to convert.
     * @param forceFmtFlag true to force the number-formatter object
     * to be used.
     *
     * @return The tick label string.
     */
    protected String makeTickLabel(double val, boolean forceFmtFlag) {
        if (this.expTickLabelsFlag || forceFmtFlag) {
            //using exponents or force-formatter flag is set
            // (convert 'E' to lower-case 'e'):
            return this.numberFormatterObj.format(val).toLowerCase();
        }
        return getTickUnit().valueToString(val);
    }

    /**
     * Converts the given value to a tick label string.
     * @param val the value to convert.
     *
     * @return The tick label string.
     */
    protected String makeTickLabel(double val) {
        return makeTickLabel(val, false);
    }

	/**
	 * @return the isLogarithmic
	 */
	public boolean isLogarithmic() {
		return isLogarithmic;
	}

	/**
	 * @param isLogarithmic the isLogarithmic to set
	 */
	public void setLogarithmic(boolean isLogarithmic) {
		if (this.isLogarithmic != isLogarithmic) {
			this.isLogarithmic = isLogarithmic;
			fireChangeEvent();
		}
	}
    
	private List getAllTicksHorizontal(Graphics2D g2, Rectangle2D dataArea,
			RectangleEdge edge) {
		// TODO Auto-generated method stub
        List ticks = new java.util.ArrayList();
        Range range = getRange();

        //get lower bound value:
        double lowerBoundVal = range.getLowerBound();
              //if small log values and lower bound value too small
              // then set to a small value (don't allow <= 0):
        if (this.smallLogFlag && lowerBoundVal < SMALL_LOG_VALUE) {
            lowerBoundVal = SMALL_LOG_VALUE;
        }

        //get upper bound value
        double upperBoundVal = range.getUpperBound();

        //get log10 version of lower bound and round to integer:
        int iBegCount = (int) Math.rint(switchedLog10(lowerBoundVal));
        //get log10 version of upper bound and round to integer:
        int iEndCount = (int) Math.rint(switchedLog10(upperBoundVal));

//        if (iBegCount == iEndCount && iBegCount >= 0
        if (iBegCount == iEndCount 
                && Math.pow(10, iBegCount) > lowerBoundVal) {
              //only 1 power of 10 value, it's > 0 and its resulting
              // tick value will be larger than lower bound of data
            --iBegCount;       //decrement to generate more ticks
        }

        int numberOfGrids = 0;
        int numberOfTicks = 0;
        NumberTick lastTick = null;

        double currentTickValue;
        String tickLabel;
        boolean zeroTickFlag = false;
        for (int i = iBegCount; i <= iEndCount; i++) {
            //for each power of 10 value; create ten ticks
            for (int j = 0; j < 10; ++j) {
                //for each tick to be displayed
                if (this.smallLogFlag) {
                    //small log values in use; create numeric value for tick
                    currentTickValue = Math.pow(10, i) + (Math.pow(10, i) * j);
                    if (this.expTickLabelsFlag
                        || (i < 0 && currentTickValue > 0.0
                        && currentTickValue < 1.0)) {
                        //showing "1e#"-style ticks or negative exponent
                        // generating tick value between 0 & 1; show fewer
                          //first tick of series, or not too small a value and
                          // one of first 3 ticks, or last tick to be displayed
                            // set exact number of fractional digits to be shown
                            // (no effect if showing "1e#"-style ticks):
                            this.numberFormatterObj
                                .setMaximumFractionDigits(-i);
                               //create tick label (force use of fmt obj):
                            tickLabel = makeTickLabel(currentTickValue, true);
                    }
                    else {     //tick value not between 0 & 1
                               //show tick label if it's the first or last in
                               // the set, or if it's 1-5; beyond that show
                               // fewer as the values get larger:
                        tickLabel = makeTickLabel(currentTickValue);
                    }
                }
                else { //not small log values in use; allow for values <= 0
                    if (zeroTickFlag) {   //if did zero tick last iter then
                        --j;              //decrement to do 1.0 tick now
                    }     //calculate power-of-ten value for tick:
                    currentTickValue = (i >= 0)
                        ? Math.pow(10, i) + (Math.pow(10, i) * j)
                        : -(Math.pow(10, -i) - (Math.pow(10, -i - 1) * j));
                    if (!zeroTickFlag) {  // did not do zero tick last iteration
                        if (Math.abs(currentTickValue - 1.0) < 0.0001
                            && lowerBoundVal <= 0.0 && upperBoundVal >= 0.0) {
                            //tick value is 1.0 and 0.0 is within data range
                            currentTickValue = 0.0;     //set tick value to zero
                            zeroTickFlag = true;        //indicate zero tick
                        }
                    }
                    else {     //did zero tick last iteration
                        zeroTickFlag = false;         //clear flag
                    }               //create tick label string:
                               //show tick label if "1e#"-style and it's one
                               // of the first two, if it's the first or last
                               // in the set, or if it's 1-5; beyond that
                               // show fewer as the values get larger:
                    tickLabel = makeTickLabel(currentTickValue);
                }

                if (currentTickValue > upperBoundVal) {
                	if (lastTick != null){
                		String lastTickText = lastTick.getText();
                		if (lastTickText == null || lastTickText.trim().length() == 0){
                			ticks.remove(lastTick);
                			ticks.add(new NumberTick(lastTick.getValue(), 
                					createTickLabel(lastTick.getValue(), i - 1), lastTick.getTextAnchor(),
                					lastTick.getRotationAnchor(), lastTick.getAngle()));
                		}
                	}
                	if (ticks.size() < 2){
                    	double definition = Math.abs(lowerBoundVal - upperBoundVal);
                    	int numberOfDigits = 0;
                    	if (definition >= 1)
                    		numberOfDigits = 0;
                    	else {
                    		numberOfDigits = (int) Math.ceil((- Math.log10(definition)));
                    	}
                    	if (definition < 2 * Math.pow(10, -numberOfDigits)){
                    		numberOfDigits ++;
                    	}
                    	double tickVal;
                    	tickVal = lowerBoundVal;
                    	if (definition > 1)
                    		tickLabel = Long.toString((long) Math.rint(tickVal));
                    	else
                    		tickLabel = (new Formatter()).format("%." + numberOfDigits + "f", tickVal).toString();
                    	ticks.add(new NumberTick(new Double(tickVal), tickLabel,
                    			TextAnchor.TOP_CENTER, TextAnchor.TOP_CENTER, 0.0));
                    	tickVal = upperBoundVal;
                    	if (definition > 1) {
                    		tickLabel = Long.toString((long) Math.rint(tickVal));
                    	} else {
                    		tickLabel = (new Formatter()).format("%." + numberOfDigits + "f", tickVal).toString();
                    	}
                    	ticks.add(new NumberTick(new Double(tickVal), tickLabel,
                    			TextAnchor.TOP_CENTER, TextAnchor.TOP_CENTER, 0.0));
                    }
                    return ticks;   // if past highest data value then exit
                                    // method
                }

                if (currentTickValue >= lowerBoundVal - SMALL_LOG_VALUE) {
                    //tick value not below lowest data value
                    TextAnchor anchor = null;
                    TextAnchor rotationAnchor = null;
                    double angle = 0.0;
                    if (isVerticalTickLabels()) {
                        anchor = TextAnchor.CENTER_RIGHT;
                        rotationAnchor = TextAnchor.CENTER_RIGHT;
                        if (edge == RectangleEdge.TOP) {
                            angle = Math.PI / 2.0;
                        }
                        else {
                            angle = -Math.PI / 2.0;
                        }
                    }
                    else {
                        if (edge == RectangleEdge.TOP) {
                            anchor = TextAnchor.BOTTOM_CENTER;
                            rotationAnchor = TextAnchor.BOTTOM_CENTER;
                        }
                        else {
                            anchor = TextAnchor.TOP_CENTER;
                            rotationAnchor = TextAnchor.TOP_CENTER;
                        }
                    }

                    lastTick = new NumberTick(new Double(currentTickValue),
                            tickLabel, anchor, rotationAnchor, angle);
                    ticks.add(lastTick);
                    if (tickLabel != null && tickLabel.trim().length() > 0)
                    	numberOfTicks ++;
                    numberOfGrids ++;
                }
            }
        }
        if (ticks.size() < 2){
        	double definition = Math.abs(lowerBoundVal - upperBoundVal);
        	int numberOfDigits = 0;
        	if (definition >= 1)
        		numberOfDigits = 0;
        	else {
        		numberOfDigits = (int) Math.ceil((- Math.log10(definition)));
        	}
        	double tickVal;
        	tickVal = lowerBoundVal;
        	if (definition > 1)
        		tickLabel = Long.toString((long) Math.rint(tickVal));
        	else
        		tickLabel = (new Formatter()).format("%." + numberOfDigits + "f", tickVal).toString();
        	ticks.add(new NumberTick(new Double(tickVal), tickLabel,
        			TextAnchor.TOP_CENTER, TextAnchor.TOP_CENTER, 0.0));
        	tickVal = upperBoundVal;
        	if (definition > 1)
        		tickLabel = Long.toString((long) Math.rint(tickVal));
        	else
        		tickLabel = (new Formatter()).format("%." + numberOfDigits + "f", tickVal).toString();
        	ticks.add(new NumberTick(new Double(tickVal), tickLabel,
        			TextAnchor.TOP_CENTER, TextAnchor.TOP_CENTER, 0.0));
        }
        return ticks;	
	}

	/**
	 * Create tick label 
	 */
    private String createTickLabel(double tickVal, int index) {
    	String tickLabel;
    	String initial = "1";
//    	if (tickVal > 1)
    	double value = tickVal / Math.pow(10, index);
    	if (tickVal > 10) {
    		initial = String.valueOf((int) value);
    	} else {
    		initial = String.format("%.1f", value);
    	}
    	if (this.log10TickLabelsFlag) {
    		//create "log10"-type label
    		tickLabel = (((index < 0) ? "-" : "")
    				+ "10^" + Math.abs(index));
    	}
    	else {
    		if (this.expTickLabelsFlag) {
    			//create "1e#"-type label
    			tickLabel = (((index < 0) ? "-" : "")
    					+ initial + "e" + Math.abs(index));
    		}
    		else {
    			if (tickVal > 1){
    				NumberFormat format
    				= getNumberFormatOverride();
    				if (format != null) {
    					tickLabel = format.format(tickVal);
    				}
    				else {
    					tickLabel =  Long.toString(
    							(long) Math.rint(tickVal));
    				}
    			}else{
    				this.numberFormatterObj.setMaximumFractionDigits(-index);
    				//	create tick label:
    				tickLabel = this.numberFormatterObj.format(tickVal);
    			}
    		}
    	}
    	return tickLabel;
    }

}
