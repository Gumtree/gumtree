/* -------------------
 * PanningDemo.java
 * -------------------
 */

package org.jfree.panning;

import java.awt.BorderLayout;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.urls.StandardXYURLGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;

/**
 * A simple demonstration application showing how to create a pannable chart
 */
public class PanningDemo
    extends JFrame
{

    /**
     * Creates a new demo.
     * 
     * @param title the frame title.
     */
    public PanningDemo(String title)
    {
        super(title);

        setLayout(new BorderLayout());

        XYDataset dataset = createDataset();
        JFreeChart chart = createChart(dataset);
        final PanningChartPanel chartPanel = new PanningChartPanel(chart);
//        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        add(chartPanel, BorderLayout.CENTER);
    }

    private static XYSeries createRandomSeries(int noOfElements, String name)
    {
        XYSeries series = new XYSeries(name);
        Random r = new Random();

        double x = 0.0d;
        double y = 0.0d;

        for (int i = 0; i < 3000; i++)
        {
            series.add(x, y);
            x += r.nextDouble();

            if (r.nextBoolean())
            {
                y += r.nextDouble();
            }
            else
            {
                y -= r.nextDouble();
            }
        }
        return series;
    }

    /**
     * Creates a sample dataset.
     * 
     * @return a sample dataset.
     */
    private static XYDataset createDataset()
    {
        XYSeries series1 = createRandomSeries(1000, "First");
        XYSeries series2 = createRandomSeries(1000, "Second");
        XYSeries series3 = createRandomSeries(1000, "Third");

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);
        dataset.addSeries(series2);
        dataset.addSeries(series3);

        return dataset;
    }

    /**
     * Creates a chart.
     * 
     * @param dataset the data for the chart.
     * @return a chart.
     */
    private static JFreeChart createChart(XYDataset dataset)
    {

        // create the chart...
        JFreeChart chart = createXYLineChart("Panning Chart Demo", // chart title
            "X", // x axis label
            "Y", // y axis label
            dataset, // data
            PlotOrientation.VERTICAL, true, // include legend
            false, // tooltips
            false // urls
        );

        // get a reference to the plot for further customisation...
        XYPlot plot = (XYPlot)chart.getPlot();

        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer();
        renderer.setBaseShapesVisible(false);
        renderer.setBaseShapesFilled(false);

        // change the auto tick unit selection to integer units only...
        NumberAxis rangeAxis = (NumberAxis)plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        return chart;

    }

    public static JFreeChart createXYLineChart(String title, String xAxisLabel, String yAxisLabel,
        XYDataset dataset, PlotOrientation orientation, boolean legend, boolean tooltips,
        boolean urls)
    {

        if (orientation == null)
        {
            throw new IllegalArgumentException("Null 'orientation' argument.");
        }
        NumberAxis xAxis = new NumberAxis(xAxisLabel);
        xAxis.setAutoRangeIncludesZero(false);
        NumberAxis yAxis = new NumberAxis(yAxisLabel);
        XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);

        XYPlot plot = new PannableXYPlot(dataset, xAxis, yAxis, renderer);
        plot.setOrientation(orientation);
        if (tooltips)
        {
            renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        }
        if (urls)
        {
            renderer.setURLGenerator(new StandardXYURLGenerator());
        }

        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, legend);
        return chart;

    }

    /**
     * Creates a panel for the demo (used by SuperDemo.java).
     * 
     * @return A panel.
     */
    public static JPanel createDemoPanel()
    {
        JFreeChart chart = createChart(createDataset());
        return new ChartPanel(chart);
    }

    /**
     * Starting point for the demonstration application.
     * 
     * @param args ignored.
     */
    public static void main(String[] args)
    {
        PanningDemo demo = new PanningDemo("JFreeChart: PanningDemo.java");
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);

    }

}
