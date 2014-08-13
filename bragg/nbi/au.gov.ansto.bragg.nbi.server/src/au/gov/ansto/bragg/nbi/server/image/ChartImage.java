/**
 * 
 */
package au.gov.ansto.bragg.nbi.server.image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.gumtree.vis.awt.PlotFactory;
import org.gumtree.vis.interfaces.IDataset;
import org.gumtree.vis.interfaces.ITimeSeriesSet;
import org.gumtree.vis.interfaces.IXYErrorDataset;
import org.gumtree.vis.interfaces.IXYZDataset;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;

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
				} 
			}
			update();
		} catch (Exception e) {
			e.printStackTrace();
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
