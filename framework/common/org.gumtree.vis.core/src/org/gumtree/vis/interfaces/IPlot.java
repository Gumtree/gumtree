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
package org.gumtree.vis.interfaces;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.List;

import org.gumtree.vis.mask.AbstractMask;
import org.gumtree.vis.mask.IMaskEventListener;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;

/**
 * @author nxi
 *
 */
public interface IPlot {
	
	public abstract void addChartMouseListener(
			ChartMouseListener listener);
	
	public abstract void createChartPrintJob();
	
	public abstract void doCopy();
	
	public abstract void doEditChartProperties();
	
	public abstract void doSaveAs() throws IOException;

	public abstract JFreeChart getChart();
	
	public abstract double getChartX();
	
	/**
	 * @return the chartY
	 */
	public abstract double getChartY();
	
	public abstract IDataset getDataset();
	
	public abstract ValueAxis getHorizontalAxis();
	
	public abstract Image getImage();
	
	public abstract List<AbstractMask> getMasks();
	
	public abstract AbstractMask getSelectedMask();
	
	public abstract TextTitle getTitle();
	
	public abstract ValueAxis getVerticalAxis();
	
	public abstract XYPlot getXYPlot();
	
	public abstract void setAutoUpdate(boolean autoUpdate);
		
	public abstract boolean isHorizontalAxisFlipped();
	
	public abstract boolean isMouseWheelEnabled();
	
	public abstract boolean isToolTipFollowerEnabled();
	
	public abstract boolean isVerticalAxisFlipped();
	
	public abstract void moveSelectedMask(int keyCode);
	
	public abstract void processMouseWheelEvent(MouseWheelEvent awtEvent);
	
	public abstract void removeChartMouseListener(
			ChartMouseListener listener);
	
	public abstract void removeSelectedMask();
	
	public abstract void repaint();
	
	public abstract void restoreAutoBounds();
	
	public abstract void restoreHorizontalBounds();
	
	public abstract void restoreVerticalBounds();
	
	/**
	 * Supported types are "jpg" and "png";
	 * @param filename
	 * @param fileType
	 */
	public abstract void saveTo(String filename, String fileType) throws IOException;
	
	public abstract boolean isAutoUpdate();
	
	public abstract void setBackgroundColor(Color color);

//	public abstract void setComponentOrientation(ComponentOrientation orientation);

	public abstract void setCursor(Cursor awtCursor);

	public abstract void setDataset(IDataset dataset);
	
//	public abstract void setEnabled(boolean isEnabled);

	public abstract void setHorizontalAxisFlipped(boolean isFlipped);
	
	public abstract void setHorizontalAxisTrace(boolean isEnabled);
	
	public abstract void setHorizontalZoomable(boolean isZoomable);

	public abstract void setMouseWheelEnabled(boolean isEnabled);

	public void setSelectedMask(AbstractMask selectedMask);

	public abstract void setToolTipFollowerEnabled(boolean enabled);

	public abstract void setVerticalAxisFlipped(boolean isFlipped);

	public abstract void setVerticalAxisTrace(boolean isEnabled);

	public abstract void setVerticalZoomable(boolean isZoomable);
	
	public abstract void setVisible(boolean isVisible);
	
	
	public abstract void setZoomInFactor(double factor);
	
	public abstract void setZoomOutFactor(double factor);
	public abstract void zoomInBoth(double x, double y);
	
	public abstract void zoomInHorizontal(double x, double y);
	
	public abstract void zoomInVertical(double x, double y);
	
	public abstract void zoomOutBoth(double x, double y);
	
	public abstract void zoomOutHorizontal(double x, double y);
	
	public abstract void zoomOutVertical(double x, double y);
	
	public abstract void draw(Graphics2D g2, Rectangle2D area, 
			double shiftX, double shiftY);
	
	public abstract void updatePlot();
	
	public abstract void updateLabels();
	
	public abstract void addMask(AbstractMask mask);
	
	public abstract void addMasks(List<AbstractMask> maskList);
	
	public abstract void setMaskingEnabled(boolean enabled);
	
	public abstract boolean isMaskingEnabled();
	
	public abstract void removeMask(AbstractMask mask);
	
	public abstract void addMaskEventListener(IMaskEventListener listener);
	
	public abstract void removeMaskEventListener(IMaskEventListener listener);

	public void addShape(Shape shape, Color color);

	public void removeShape(Shape shape);

	public void clearShapes();

	public abstract void setPlotTitle(String title);
	
	public abstract void doHelp();

	public abstract void doExport(IExporter exporter) throws IOException;
	
	public abstract void cleanUp();
	
	public abstract void setLogarithmEnabled(boolean enabled);
	
	public abstract boolean isLogarithmEnabled();

	public abstract IHelpProvider getHelpProvider();
	
	public boolean isShapeEnabled();
	/**
	 * @param isShapeEnabled the isShapeEnabled to set
	 */
	public void setShapeEnabled(boolean isShapeEnabled);

	void addDomainAxisMarker(double x, int height, Color color);

	void addRangeAxisMarker(double y, int width, Color color);

	void addMarker(double x, double y, Color color);

	void removeMarker(double x, double y);
	
	void removeRangeAxisMarker(double y);
	
	void removeDomainAxisMarker(double x);
	
	public void clearMarkers();
	
	public void clearDomainAxisMarkers();
	
	public void clearRangeAxisMarkers();
	
	public boolean isTextInputEnabled();

	public void setTextInputEnabled(boolean isTextInputEnabled);

}
