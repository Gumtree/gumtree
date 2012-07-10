package org.gumtree.vis.hist2d;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.TitleChangeEvent;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.ui.RectangleEdge;

public class PaintScaleLegend2D extends PaintScaleLegend 
implements MouseListener, MouseMotionListener{


	/**
	 * 
	 */
	private static final long serialVersionUID = 2611325244717419754L;

	/** The paint scale (never <code>null</code>). */
    private ColorPaintScale scale;

    
//    private double lowerBoundPercent = 0;
//    private double upperBoundPercent = 1;
    private transient Paint regionBarPaint;
    private transient Stroke regionBarStroke;
//    private float regionBarCorrection;
    private Rectangle2D target;
    private int boundaryDragIndicator = Cursor.DEFAULT_CURSOR;
    /**
     * Creates a new instance.
     *
     * @param scale  the scale (<code>null</code> not permitted).
     * @param axis  the axis (<code>null</code> not permitted).
     */
    public PaintScaleLegend2D(PaintScale scale, ValueAxis axis) {
        super(scale, axis);
        setScale(scale);
        regionBarPaint = Color.black;
        regionBarStroke = new BasicStroke(1f);
    }


    @Override
    public void setScale(PaintScale scale) {
    	if (scale == null || !(scale instanceof ColorPaintScale)) {
            throw new IllegalArgumentException("Wrong 'scale' argument.");
        }
        this.scale = (ColorPaintScale) scale;
        notifyListeners(new TitleChangeEvent(this));
    }
    
    @Override
    public PaintScale getScale() {
    	// TODO Auto-generated method stub
    	return scale;
    }
    
    /**
     * Draws the legend within the specified area.
     *
     * @param g2  the graphics target (<code>null</code> not permitted).
     * @param area  the drawing area (<code>null</code> not permitted).
     * @param params  drawing parameters (ignored here).
     *
     * @return <code>null</code>.
     */
    public Object draw(Graphics2D g2, Rectangle2D area, Object params) {
        Rectangle2D target = (Rectangle2D) area.clone();
        target = trimMargin(target);
    	this.target = target;
        if (getBackgroundPaint() != null) {
            g2.setPaint(getBackgroundPaint());
            g2.fill(target);
        }
        getFrame().draw(g2, target);
        getFrame().getInsets().trim(target);
        target = trimPadding(target);
        double base = getAxis().getLowerBound();
        double increment = getAxis().getRange().getLength() / getSubdivisionCount();
        Rectangle2D r = new Rectangle2D.Double();

        if (RectangleEdge.isTopOrBottom(getPosition())) {
            RectangleEdge axisEdge = Plot.resolveRangeAxisLocation(
                    getAxisLocation(), PlotOrientation.HORIZONTAL);
            if (axisEdge == RectangleEdge.TOP) {
                for (int i = 0; i < getSubdivisionCount(); i++) {
                    double v = base + (i * increment);
                    Paint p = this.scale.getPaint(v);
                    double vv0 = getAxis().valueToJava2D(v, target,
                            RectangleEdge.TOP);
                    double vv1 = getAxis().valueToJava2D(v + increment, target,
                            RectangleEdge.TOP);
                    double ww = Math.abs(vv1 - vv0) + 1.0;
                    r.setRect(Math.min(vv0, vv1), target.getMaxY()
                            - getStripWidth(), ww, getStripWidth());
                    g2.setPaint(p);
                    g2.fill(r);
                }
                if (isStripOutlineVisible()) {
                    g2.setPaint(getStripOutlinePaint());
                    g2.setStroke(getStripOutlineStroke());
                    g2.draw(new Rectangle2D.Double(target.getMinX(),
                            target.getMaxY() - getStripWidth(),
                            target.getWidth(), getStripWidth()));
                }
                getAxis().draw(g2, target.getMaxY() - getStripWidth()
                        - this.getAxisOffset(), target, target, RectangleEdge.TOP,
                        null);
            }
            else if (axisEdge == RectangleEdge.BOTTOM) {
                for (int i = 0; i < getSubdivisionCount(); i++) {
                    double v = base + (i * increment);
                    Paint p = this.scale.getPaint(v);
                    double vv0 = getAxis().valueToJava2D(v, target,
                            RectangleEdge.BOTTOM);
                    double vv1 = getAxis().valueToJava2D(v + increment, target,
                            RectangleEdge.BOTTOM);
                    double ww = Math.abs(vv1 - vv0) + 1.0;
                    r.setRect(Math.min(vv0, vv1), target.getMinY(), ww,
                            getStripWidth());
                    g2.setPaint(p);
                    g2.fill(r);
                }
                if (isStripOutlineVisible()) {
                    g2.setPaint(getStripOutlinePaint());
                    g2.setStroke(getStripOutlineStroke());
                    g2.draw(new Rectangle2D.Double(target.getMinX(),
                            target.getMinY(), target.getWidth(),
                            getStripWidth()));
                }
                getAxis().draw(g2, target.getMinY() + getStripWidth()
                        + this.getAxisOffset(), target, target,
                        RectangleEdge.BOTTOM, null);
            }
        }
        else {
            RectangleEdge axisEdge = Plot.resolveRangeAxisLocation(
                    getAxisLocation(), PlotOrientation.VERTICAL);
            if (axisEdge == RectangleEdge.LEFT) {
                double upperBound = target.getMinY() + target.getHeight() * (1 - scale.getUpperBoundPercent());
                double lowerBound = target.getMaxY() - target.getHeight() * scale.getLowerBoundPercent();
//            	Rectangle2D newTarget = new Rectangle2D.Double(target.getMinX(), upperBound, 
//            			target.getWidth(), lowerBound - upperBound);
                for (int i = 0; i < getSubdivisionCount(); i++) {
                    double v = base + (i * increment);
                    Paint p = this.scale.getPaint(v);
                    double vv0 = getAxis().valueToJava2D(v, target,
                            RectangleEdge.LEFT);
                    double vv1 = getAxis().valueToJava2D(v + increment, target,
                            RectangleEdge.LEFT);
                    double hh = Math.abs(vv1 - vv0) + 1.0;
                    r.setRect(target.getMaxX() - getStripWidth(),
                            Math.min(vv0, vv1), getStripWidth(), hh);
                    g2.setPaint(p);
                    g2.fill(r);
                }
                if (isStripOutlineVisible()) {
                    g2.setPaint(getStripOutlinePaint());
                    g2.setStroke(getStripOutlineStroke());
                    g2.draw(new Rectangle2D.Double(target.getMaxX()
                            - getStripWidth(), target.getMinY(), getStripWidth(),
                            target.getHeight()));
                }
                getAxis().draw(g2, target.getMaxX() - getStripWidth()
                        - this.getAxisOffset(), target, target, RectangleEdge.LEFT,
                        null);
                g2.setPaint(regionBarPaint);
                g2.setStroke(regionBarStroke);
                g2.fill(new Rectangle2D.Double(target.getMaxX() - getStripWidth() - 7, 
                		upperBound - 2, 4, 4));
                g2.drawLine((int) (target.getMaxX() - getStripWidth() - getStripWidth()), (int) (upperBound), 
                		(int) (target.getMaxX() - getStripWidth() - getStripWidth()), (int) upperBound + 2);
                g2.drawLine((int) target.getMaxX() - 1, (int) upperBound, 
                		(int) target.getMaxX() - 1, (int) upperBound + 2);
                g2.drawLine((int) (target.getMaxX() - getStripWidth() - getStripWidth()), (int) upperBound, 
                		(int) target.getMaxX() - 1, (int) upperBound);
                g2.fill(new Rectangle2D.Double(target.getMaxX() - getStripWidth() - 7, 
                		lowerBound - 2, 4, 4));
                g2.drawLine((int) (target.getMaxX() - getStripWidth() - getStripWidth()), (int) lowerBound - 2, 
                		(int) (target.getMaxX() - getStripWidth() - getStripWidth()), (int) lowerBound);
                g2.drawLine((int) target.getMaxX() - 1, (int) lowerBound - 2, 
                		(int) target.getMaxX() - 1, (int) lowerBound);
                g2.drawLine((int) (target.getMaxX() - getStripWidth() - getStripWidth()), (int) lowerBound, 
                		(int) target.getMaxX() - 1, (int) lowerBound);
            }
            else if (axisEdge == RectangleEdge.RIGHT) {
                for (int i = 0; i < getSubdivisionCount(); i++) {
                    double v = base + (i * increment);
                    Paint p = this.scale.getPaint(v);
                    double vv0 = getAxis().valueToJava2D(v, target,
                            RectangleEdge.LEFT);
                    double vv1 = getAxis().valueToJava2D(v + increment, target,
                            RectangleEdge.LEFT);
                    double hh = Math.abs(vv1 - vv0) + 1.0;
                    r.setRect(target.getMinX(), Math.min(vv0, vv1),
                            getStripWidth(), hh);
                    g2.setPaint(p);
                    g2.fill(r);
                }
                if (isStripOutlineVisible()) {
                    g2.setPaint(getStripOutlinePaint());
                    g2.setStroke(getStripOutlineStroke());
                    g2.draw(new Rectangle2D.Double(target.getMinX(),
                            target.getMinY(), getStripWidth(),
                            target.getHeight()));
                }
                getAxis().draw(g2, target.getMinX() + getStripWidth()
                        + this.getAxisOffset(), target, target, RectangleEdge.RIGHT,
                        null);
            }
        }
        return null;
    }

  	@Override
  	public void mouseDragged(MouseEvent e) {
  		if (target != null && target.getMinX() <= e.getX() && target.getMaxX() >= e.getX() && 
  				target.getMinY() <= e.getY() + 10 && target.getMaxY() >= e.getY() - 10) {
  			if (boundaryDragIndicator == Cursor.S_RESIZE_CURSOR) {
  				double upperBoundPercent = (target.getMaxY() - e.getY()) / target.getHeight();
  				if (upperBoundPercent < 0) {
  					upperBoundPercent = 0;
  				}
  				if (upperBoundPercent > 1) {
  					upperBoundPercent = 1;
  				}
  				scale.setUpperBoundPercent(upperBoundPercent);
  				if (scale.getLowerBoundPercent() > upperBoundPercent) {
  					scale.setLowerBoundPercent(upperBoundPercent);
  				}
  			}
  			if (boundaryDragIndicator == Cursor.N_RESIZE_CURSOR) {
  				double lowerBoundPercent = (target.getMaxY() - e.getY()) / target.getHeight();
  				if (lowerBoundPercent < 0) {
  					lowerBoundPercent = 0;
  				}
  				if (lowerBoundPercent > 1) {
  					lowerBoundPercent = 1;
  				}
  				scale.setLowerBoundPercent(lowerBoundPercent);
  				if (scale.getUpperBoundPercent() < lowerBoundPercent) {
  					scale.setUpperBoundPercent(lowerBoundPercent);
  				}
  			}
  			//		if (target.contains(e.getPoint())) {
  			//			e.getComponent().setCursor(Cursor.getPredefinedCursor(findCursor(e)));
  			//		}
  			//		e.getComponent().repaint();
  			notifyListeners(new TitleChangeEvent(this));
  		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (target != null && target.getMinX() <= e.getX() && target.getMaxX() >= e.getX() && 
				target.getMinY() <= e.getY() + 2 && target.getMaxY() >= e.getY() - 2) {
			e.getComponent().setCursor(Cursor.getPredefinedCursor(findCursor(e)));
		}		
	}

	private int findCursor(MouseEvent e) {
		double upperBound = target.getMinY() + target.getHeight() * (1 - scale.getUpperBoundPercent());
		double lowerBound = target.getMaxY() - target.getHeight() * scale.getLowerBoundPercent();
		if (e.getY() > upperBound - 4 && e.getY() < upperBound + 4) {
			return Cursor.S_RESIZE_CURSOR;
		}
		if (e.getY() > lowerBound - 4 && e.getY() < lowerBound + 4) {
			return Cursor.N_RESIZE_CURSOR;
		}
		return Cursor.DEFAULT_CURSOR;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (target != null && e.getClickCount() > 1 && target.contains(e.getPoint())) {
			scale.resetBoundPercentage();
			notifyListeners(new TitleChangeEvent(this));
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		boundaryDragIndicator = findCursor(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		boundaryDragIndicator = Cursor.DEFAULT_CURSOR;
	}
}
