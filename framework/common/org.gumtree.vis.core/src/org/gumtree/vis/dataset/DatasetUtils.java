/**
 * 
 */
package org.gumtree.vis.dataset;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.gumtree.vis.interfaces.ITimeSeriesSet;
import org.gumtree.vis.interfaces.IXYErrorDataset;
import org.gumtree.vis.interfaces.IXYErrorSeries;
import org.gumtree.vis.interfaces.IXYZDataset;

/**
 * @author nxi
 *
 */
public class DatasetUtils {

	public enum ExportFormat{XYSIGMA, XYZ, TIMESERIES}
	
	public static void export(IXYErrorDataset dataset, BufferedWriter writer, ExportFormat format) throws IOException {
		if (format == ExportFormat.XYSIGMA) {
			if (dataset != null && dataset.getSeriesCount() > 0) {
				writer.append("# title=" + dataset.getTitle() + "\t series_count=" + dataset.getSeriesCount() + "\n");
				writer.append("# Y: title=" + dataset.getYTitle() + "\t units=" + dataset.getYUnits() + "\n");
				writer.append("# X: title=" + dataset.getXTitle() + "\t units=" + dataset.getXUnits() + "\n");
				for (int i = 0; i < dataset.getSeriesCount(); i++) {
					IXYErrorSeries series = dataset.getSeries().get(i);
					writer.append("# series_" + i + "\n");
					writer.append("# X \t Y \t Sigma\n");
					for (int j = 0; j < series.getItemCount(); j++) {
						writer.append(series.getX(j).floatValue() + "\t " + series.getY(j).floatValue() + "\t " + (float) series.getYError(j) + "\n");
					}
					writer.append("\n");
					writer.flush();
				}
			}
		} else {
			throw new UnsupportedOperationException(String.valueOf(format) + " format is not supported.");
		}
	}
	
	public static void export(IXYZDataset dataset, BufferedWriter writer, ExportFormat format) throws IOException {
		if (format == ExportFormat.XYZ) {
			if (dataset != null) {
				writer.append("# title=" + dataset.getTitle() + "\t shape=[" + dataset.getYSize(0) + ", " + dataset.getXSize(0)+ "]\n");
				writer.append("# Z: " + dataset.getZTitle() + "\n");
				writer.append("# Y: " + dataset.getYTitle() + "\n");
				writer.append("# X: " + dataset.getXTitle() + "\n");
				writer.append("# Y \t X \t Z\n");
				for (int i = 0; i < dataset.getItemCount(0); i++) {
					writer.append(dataset.getY(0, i).floatValue() + "\t " + dataset.getX(0, i).floatValue() + "\t " + (float) dataset.getZValue(0, i) + "\n");
				}
				writer.flush();
			}
		} else {
			throw new UnsupportedOperationException(String.valueOf(format) + " format is not supported.");
		}
	}
	
	public static void export(ITimeSeriesSet dataset, BufferedWriter writer, ExportFormat format) throws IOException {
		if (format == ExportFormat.TIMESERIES) {
			if (dataset != null) {
				writer.append("# title=" + dataset.getYTitle() + "\n");
				writer.append("# Time \t\t\t\t" + dataset.getYTitle() + "\n");
				SimpleDateFormat dateFormat = new SimpleDateFormat("EEE d MMM yyyy HH:mm:ss");
				for (int i = 0; i < dataset.getItemCount(0); i++) {
					Date date = new Date(dataset.getX(0, i).longValue());
					writer.append(dateFormat.format(date) + "\t " + dataset.getY(0, i).floatValue() + "\n");
				}
				writer.append("\n");
				writer.flush();
			}
		} else {
			throw new UnsupportedOperationException(String.valueOf(format) + " format is not supported.");
		}
	}
}
