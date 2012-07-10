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
package org.gumtree.vis.plot1d;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.Formatter;
import java.util.List;

import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

/**
 * @author nxi
 * Created on 22/04/2009
 */
public class KLogarithmicAxis extends LogarithmicAxis {

	/**
	 * @param label
	 */
	public KLogarithmicAxis(String label) {
		super(label);
	}

	/* (non-Javadoc)
	 * @see org.jfree.chart.axis.LogarithmicAxis#refreshTicksHorizontal(java.awt.Graphics2D, java.awt.geom.Rectangle2D, org.jfree.ui.RectangleEdge)
	 */
	@Override
	protected List refreshTicksHorizontal(Graphics2D g2, Rectangle2D dataArea,
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
                    	if (definition > 1)
                    		tickLabel = Long.toString((long) Math.rint(tickVal));
                    	else
                    		tickLabel = (new Formatter()).format("%." + numberOfDigits + "f", tickVal).toString();
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

	/* (non-Javadoc)
	 * @see org.jfree.chart.axis.LogarithmicAxis#refreshTicksVertical(java.awt.Graphics2D, java.awt.geom.Rectangle2D, org.jfree.ui.RectangleEdge)
	 */
	@Override
	protected List refreshTicksVertical(Graphics2D g2, Rectangle2D dataArea,
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

	protected List getAllTicksVertical(Graphics2D g2, Rectangle2D dataArea,
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

    private String createTickLabel(double tickVal, int index) {
    	String tickLabel;
    	String initial = "1";
//    	if (tickVal > 1)
    		initial = String.valueOf((int) (tickVal / Math.pow(10, index)));
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
