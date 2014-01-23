package org.jfree.chart.plot;

import java.awt.BasicStroke;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org.jfree.data.general.ValueDataset;
import org.jfree.text.TextUtilities;

public class CorrectedMeterPlot extends MeterPlot {

	public CorrectedMeterPlot(ValueDataset dataset) {
		super(dataset);
	}

	@Override
	protected void drawTick(Graphics2D g2, Rectangle2D meterArea, double value) {
		drawTick(g2, meterArea, value, true);
	}
	
	@Override
	protected void drawValueLabel(Graphics2D g2, Rectangle2D area) {
		// TODO Auto-generated method stub
		super.drawValueLabel(g2, area);
	}
	
    /**
     * Draws a tick on the dial.
     *
     * @param g2  the graphics device.
     * @param meterArea  the meter area.
     * @param value  the tick value.
     * @param label  a flag that controls whether or not a value label is drawn.
     */
    protected void drawTick(Graphics2D g2, Rectangle2D meterArea,
                            double value, boolean label) {

        double valueAngle = valueToAngle(value);

        double meterMiddleX = meterArea.getCenterX();
        double meterMiddleY = meterArea.getCenterY();

        g2.setPaint(getTickPaint());
        g2.setStroke(new BasicStroke(2.0f));

        double valueP2X = 0;
        double valueP2Y = 0;

        double radius = (meterArea.getWidth() / 2) + DEFAULT_BORDER_SIZE;
        double radius1 = radius - 10;

        double valueP1X = meterMiddleX
                + (radius * Math.cos(Math.PI * (valueAngle / 180)));
        double valueP1Y = meterMiddleY
                - (radius * Math.sin(Math.PI * (valueAngle / 180)));

        valueP2X = meterMiddleX
                + (radius1 * Math.cos(Math.PI * (valueAngle / 180)));
        valueP2Y = meterMiddleY
                - (radius1 * Math.sin(Math.PI * (valueAngle / 180)));

        Line2D.Double line = new Line2D.Double(valueP1X, valueP1Y, valueP2X,
                valueP2Y);
        g2.draw(line);

        if (label) {

            String tickLabel = getTickLabelFormat().format(value);
            g2.setFont(getTickLabelFont());
            g2.setPaint(getTickLabelPaint());

            FontMetrics fm = g2.getFontMetrics();
            Rectangle2D tickLabelBounds
                = TextUtilities.getTextBounds(tickLabel, g2, fm);

            double radius2 = radius1 - 5;
            
            valueP2X = meterMiddleX
                    + (radius2 * Math.cos(Math.PI * (valueAngle / 180)));
            valueP2Y = meterMiddleY
                    - (radius2 * Math.sin(Math.PI * (valueAngle / 180)));

            double x = valueP2X;
            double y = valueP2Y;
            if (valueAngle < 90) {
            	x = x - tickLabelBounds.getWidth();
            }
            if (valueAngle == 90 || valueAngle == 270) {
            	x = x - tickLabelBounds.getWidth() / 2;
            }
            if (valueAngle > 0 && valueAngle < 180) {
            	y = y + (tickLabelBounds.getHeight() / 1.5) * Math.sin(Math.PI * (valueAngle / 180));
            }
//            if (valueAngle == 90 || valueAngle == 270) {
//                x = x - tickLabelBounds.getWidth() / 2;
//            }
//            else if (valueAngle < 90 || valueAngle > 270) {
//                x = x - tickLabelBounds.getWidth();
//            }
//            if ((valueAngle > 135 && valueAngle < 225)
//                    || valueAngle > 315 || valueAngle < 45) {
//                y = y - tickLabelBounds.getHeight() / 2;
//            }
//            else {
//                y = y + tickLabelBounds.getHeight() / 2;
//            }
            g2.drawString(tickLabel, (float) x, (float) y);
            

//            g2.drawString(tickLabel, (float) valueP2X, (float) valueP2Y);
        }
    }
    
}
