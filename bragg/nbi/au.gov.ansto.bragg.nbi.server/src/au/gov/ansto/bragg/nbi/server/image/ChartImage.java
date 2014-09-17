/**
 * 
 */
package au.gov.ansto.bragg.nbi.server.image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.gumtree.vis.awt.PlotFactory;
import org.gumtree.vis.hist2d.ColorPaintScale;
import org.gumtree.vis.interfaces.IDataset;
import org.gumtree.vis.interfaces.ITimeSeriesSet;
import org.gumtree.vis.interfaces.IXYErrorDataset;
import org.gumtree.vis.interfaces.IXYZDataset;
import org.gumtree.vis.plot1d.LegendPosition;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.title.Title;
import org.jfree.ui.RectangleEdge;

/**
 * @author nxi
 *
 */
public class ChartImage {

	private final static int DEFAULT_WIDTH = 640;
	private final static int DEFAULT_HEIGHT = 320;
	private JFreeChart chart;
	private int width;
	private int height;
	private byte[] imageCache;
	private boolean isUpdated;

	/**
	 * 
	 */
	public ChartImage() {
		this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	public ChartImage(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	public void setDataset(IDataset dataset) {
		try{
			boolean createNewChart = false;
			if (chart != null) {
				IDataset oldDataset = (IDataset) chart.getXYPlot().getDataset();;
				if (oldDataset.getClass() != dataset.getClass()) {
					createNewChart = true;
				}
			} else {
				createNewChart = true;
			}
			if (!createNewChart) {
				chart.getXYPlot().setDataset(dataset);
				if (dataset instanceof IXYZDataset) {
					updatePaintScaleLegend();
				}
			} else {
				if (dataset instanceof IXYErrorDataset) {
					chart = PlotFactory.createXYErrorChart((IXYErrorDataset) dataset);
					LegendTitle legend = chart.getLegend();
					if (legend != null) {
						chart.getLegend().setVisible(false);
					}
//					chart.setBackgroundPaint(Color.BLACK);
//					chart.getXYPlot().setBackgroundPaint(Color.DARK_GRAY);
//					chart.getTitle().setPaint(Color.WHITE);
//					ValueAxis domainAxis = chart.getXYPlot().getDomainAxis();
//					domainAxis.setLabelPaint(Color.WHITE);
//					domainAxis.setTickLabelPaint(Color.LIGHT_GRAY);
//					domainAxis.setTickMarkPaint(Color.LIGHT_GRAY);
//					ValueAxis rangeAxis = chart.getXYPlot().getRangeAxis();
//					rangeAxis.setLabelPaint(Color.WHITE);
//					rangeAxis.setTickLabelPaint(Color.LIGHT_GRAY);
//					rangeAxis.setTickMarkPaint(Color.LIGHT_GRAY);
//					XYItemRenderer renderer = chart.getXYPlot().getRenderer();
//					if (renderer instanceof XYLineAndShapeRenderer) {
//						renderer.setSeriesPaint(0, Color.CYAN);
//					}
				} else if (dataset instanceof IXYZDataset) {
					chart = PlotFactory.createXYBlockChart(((IXYZDataset) dataset));
					LegendTitle legend = chart.getLegend();
					if (legend != null) {
						chart.getLegend().setVisible(false);
					}
//					chart.setBackgroundPaint(Color.BLACK);
//					chart.getXYPlot().setBackgroundPaint(Color.DARK_GRAY);
//					chart.getTitle().setPaint(Color.WHITE);
//					ValueAxis domainAxis = chart.getXYPlot().getDomainAxis();
//					domainAxis.setLabelPaint(Color.WHITE);
//					domainAxis.setTickLabelPaint(Color.LIGHT_GRAY);
//					domainAxis.setTickMarkPaint(Color.LIGHT_GRAY);
//					ValueAxis rangeAxis = chart.getXYPlot().getRangeAxis();
//					rangeAxis.setLabelPaint(Color.WHITE);
//					rangeAxis.setTickLabelPaint(Color.LIGHT_GRAY);
//					rangeAxis.setTickMarkPaint(Color.LIGHT_GRAY);
				} else if (dataset instanceof ITimeSeriesSet) {
					chart = PlotFactory.createTimeChart((ITimeSeriesSet) dataset);
				} else {
					System.err.println(dataset.getClass());
				}
			}
			update();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public PaintScaleLegend getPaintScaleLegend() {
		for (Object object : getChart().getSubtitles()) {
        	Title title = (Title) object;
        	if (title instanceof PaintScaleLegend) {
        		return (PaintScaleLegend) title;
        	}
		}
		return null;
	}
	
	public void setLegendPosition(LegendPosition position) {
		if (position == LegendPosition.NONE) {
        	getChart().getLegend().setVisible(false);
		} else if (position == LegendPosition.BOTTOM) {
        	getChart().getLegend().setVisible(true);
        	getChart().getLegend().setPosition(RectangleEdge.BOTTOM);
		} else if (position == LegendPosition.TOP) {
        	getChart().getLegend().setVisible(true);
        	getChart().getLegend().setPosition(RectangleEdge.TOP);
		} else if (position == LegendPosition.RIGHT) {
	    	getChart().getLegend().setVisible(true);
	    	getChart().getLegend().setPosition(RectangleEdge.RIGHT);
		} else if (position == LegendPosition.LEFT) {
        	getChart().getLegend().setVisible(true);
        	getChart().getLegend().setPosition(RectangleEdge.LEFT);
		} else if (position == LegendPosition.INTERNAL) {
        	getChart().getLegend().setVisible(false);
		}
	}
	
	public void updatePaintScaleLegend() {
		PaintScaleLegend legend = getPaintScaleLegend();
		double max = ((IXYZDataset) getDataset()).getZMax();
		double min = ((IXYZDataset) getDataset()).getZMin();
		if (legend != null) {
			((ColorPaintScale) legend.getScale()).setLowerBound(min);
			((ColorPaintScale) legend.getScale()).setUpperBound(max);
			legend.getAxis().setLowerBound(min);
			legend.getAxis().setUpperBound(max);
		}
	}
	
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public void makeImage() {
		if (chart != null) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				ChartUtilities.writeChartAsPNG(out, chart, width, height);
			} catch (IOException e) {
				e.printStackTrace();
			}
			imageCache = out.toByteArray();
		} 
	}
	public byte[] getImageCache(){
		if (imageCache == null) {
			return new byte[]{};
		}
		return imageCache;
	}
	public JFreeChart getChart() {
		return chart;
	}
	public XYPlot getXYPlot(){
		if (chart != null) {
			return chart.getXYPlot();
		}
		return null;
	}
	public void update(){
		makeImage();
		isUpdated = true;
	}
	public boolean isUpdated(boolean clearFlag){
		if (clearFlag) {
			boolean status = isUpdated;
			isUpdated = false;
			return status;
		}
		return isUpdated;
	}
	public void clearUpdateFlag(){
		isUpdated = false;
	}
	public void clear(){
		chart = null;
		imageCache = null;
		isUpdated = true;
	}
	public IDataset getDataset(){
		if (getXYPlot() != null) {
			return (IDataset) getXYPlot().getDataset();
		}
		return null;
	}
}
