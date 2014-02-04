package au.gov.ansto.bragg.taipan.workbench.interal;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.widgets.swt.ExtendedComposite;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CorrectedMeterPlot;
import org.jfree.chart.plot.DialShape;
import org.jfree.chart.plot.MeterInterval;
import org.jfree.data.Range;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.ui.RectangleInsets;

import au.gov.ansto.bragg.taipan.workbench.interal.CounterService.IMonitorEventListener;

public class CounterMeterWidget extends ExtendedComposite {

	private static final Color FORGROUND_COLOR = new Color(224,198,147);
	private static final Color MID_RANGE_COLOR = new Color(255, 144, 0);
//	private static final int PLOT_SIZE = 150;
	private static final Font DEFAULT_TITLE_FONT = new Font("Tahoma", Font.PLAIN, 16);
	
	private DefaultValueDataset monitorDataset;
	private DefaultValueDataset detectorDataset;
	private CorrectedMeterPlot monitorMeter;
	private CorrectedMeterPlot detectorMeter;
	private IMonitorEventListener monitorListener;
	private IMonitorEventListener detectorListener;
	private int PLOT_SIZE = 180;
	
	public CounterMeterWidget(Composite parent, int style) {
		super(parent, style | SWT.EMBEDDED);
		GridLayoutFactory.swtDefaults().margins(0, 0).spacing(0, 0).numColumns(2).applyTo(this);
		monitorDataset = new DefaultValueDataset(0);
		detectorDataset = new DefaultValueDataset(0);
		handleRender(this);
		CounterService.addMonitorListener(new IMonitorEventListener() {
			
			@Override
			public void update(double value) {
				monitorDataset.setValue(value);
			}
		});
		CounterService.addDetectorListener(new IMonitorEventListener() {
			
			@Override
			public void update(double value) {
				detectorDataset.setValue(value);
			}
		});
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				disposeWidget();
			}
		});
	}

	@Override
	protected void disposeWidget() {
		CounterService.removeMonitorListener(monitorListener);
		CounterService.removeDetectorListener(detectorListener);
	}

	private void handleRender(Composite parent) {
		Composite sub1 = new Composite(parent, SWT.EMBEDDED | SWT.INHERIT_FORCE);
		FillLayout layout = new FillLayout(SWT.FILL);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.spacing = 0;
		sub1.setLayout(layout);
		GridDataFactory.swtDefaults().indent(0, 0).grab(true, false).hint(120, 120).applyTo(sub1);

		Composite sub2 = new Composite(parent, SWT.EMBEDDED | SWT.INHERIT_FORCE);
		sub2.setLayout(layout);
		GridDataFactory.swtDefaults().indent(0, 0).grab(true, false).hint(120, 120).applyTo(sub2);

		monitorMeter = makeMeterWidget(monitorDataset, 6);
		detectorMeter = makeMeterWidget(detectorDataset, 5);
		
		JFreeChart monitorChart = new JFreeChart("Monitor", DEFAULT_TITLE_FONT, monitorMeter, false);
		monitorChart.setBackgroundImage(convertToAWT(getBackgroundImage().getImageData()));
		monitorChart.setBackgroundImageAlpha(1f);
		monitorChart.getTitle().setPaint(Color.WHITE);
		monitorChart.setPadding(RectangleInsets.ZERO_INSETS);
		JFreeChart detectorChart = new JFreeChart("Detector", DEFAULT_TITLE_FONT, detectorMeter, false);
		detectorChart.setBackgroundImage(convertToAWT(getBackgroundImage().getImageData()));
		detectorChart.setBackgroundImageAlpha(1f);
		detectorChart.getTitle().setPaint(Color.WHITE);
		detectorChart.setPadding(RectangleInsets.ZERO_INSETS);
		
		ChartPanel cpanel1 = new ChartPanel(monitorChart, PLOT_SIZE, PLOT_SIZE,
				PLOT_SIZE, PLOT_SIZE, PLOT_SIZE, PLOT_SIZE, 
				false, false, false, false, false, false);
		
		ChartPanel cpanel2 = new ChartPanel(detectorChart, PLOT_SIZE, PLOT_SIZE,
				PLOT_SIZE, PLOT_SIZE, PLOT_SIZE, PLOT_SIZE, 
				false, false, false, false, false, false);
		
		Frame frame1 = SWT_AWT.new_Frame(sub1);
		frame1.add(cpanel1);

		Frame frame2 = SWT_AWT.new_Frame(sub2);
		frame2.add(cpanel2);

	}
	
	private CorrectedMeterPlot makeMeterWidget(DefaultValueDataset dataset, int rangeMax) {
		CorrectedMeterPlot plot = new CorrectedMeterPlot(dataset);
//		MeterInterval interval = new MeterInterval("All", new Range(0.0, 2.0), FORGROUND_COLOR, new BasicStroke(2.0f), null);
		plot.addInterval(new MeterInterval("All", new Range(0, rangeMax), FORGROUND_COLOR, new BasicStroke(2.0f), null));
		plot.addInterval(new MeterInterval("Mid", new Range(rangeMax * 1 / 3, rangeMax * 2 / 3), MID_RANGE_COLOR, new BasicStroke(2.0f), null));
		plot.addInterval(new MeterInterval("High", new Range(rangeMax * 2 / 3, rangeMax), Color.RED, new BasicStroke(2.0f), null));
//		plot.addInterval(interval);
        
//        plot.addInterval(new MeterInterval("High", new Range(8.0, 10.0), Color.RED, new BasicStroke(2.0f), null));
        
//        plot.addInterval(new MeterInterval("Low", new Range(0.0, 2.0), Color.GREEN, new BasicStroke(2.0f), null));
        //plot.addInterval(new MeterInterval("Low", new Range(100.00, 250.0), Color.GREEN, new BasicStroke(2.0f), null));
        
        //plot.addInterval(new MeterInterval("CS", new Range(48.68, 65.48), Color.RED, new BasicStroke(2.0f), null));
        
        plot.setDialShape(DialShape.CIRCLE);
//      plot.setDialBackgroundPaint(new Color(218,187,133));
//      plot.setDialBackgroundPaint(new Color(0, 0, b));
//        plot.setDialBackgroundPaint(Color.BLACK);
        plot.setBackgroundImage(convertToAWT(getBackgroundImage().getImageData()));
        plot.setDialBackgroundPaint(null);
        plot.setDialOutlinePaint(FORGROUND_COLOR);
        plot.setForegroundAlpha(1f);
//        plot.setDrawBorder(true);
        plot.setInsets(RectangleInsets.ZERO_INSETS);

        plot.setUnits("");
        plot.setValuePaint(FORGROUND_COLOR);
        plot.setValueFont(DEFAULT_TITLE_FONT);

        plot.setTickLabelsVisible(true);
        plot.setTickLabelsVisible(true);
        plot.setOutlineStroke(new BasicStroke(2f));
        plot.setTickSize(1);
//        plot.setTickLabelPaint(new Color(168,166,255));
        plot.setTickLabelPaint(FORGROUND_COLOR);
        Font font = new Font("Tahoma", Font.PLAIN, 14);
        plot.setTickLabelFont(font);

//        plot.setTickLabelFont(plot.getTickLabelFont().deriveFont(20));
        
        
        
        //plot.getIntervals().a
        
        
        plot.setRange(new Range(0, rangeMax));
        
//        plot.setMeterAngle(180);
//        plot.setNeedlePaint(new Color(168,166,255));
        plot.setNeedlePaint(FORGROUND_COLOR);
        //plot.
        

        plot.setTickPaint(FORGROUND_COLOR);
        return plot;
	}
	
	static BufferedImage convertToAWT(ImageData data) {
        ColorModel colorModel = null;
        PaletteData palette = data.palette;
        if (palette.isDirect) {
            BufferedImage bufferedImage = new BufferedImage(data.width,
                    data.height, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    int pixel = data.getPixel(x, y);
                    RGB rgb = palette.getRGB(pixel);
                    bufferedImage.setRGB(x, y, data.getAlpha(x, y) << 24
                            | rgb.red << 16 | rgb.green << 8 | rgb.blue);
                }
            }
            return bufferedImage;
        } else {
            RGB[] rgbs = palette.getRGBs();
            byte[] red = new byte[rgbs.length];
            byte[] green = new byte[rgbs.length];
            byte[] blue = new byte[rgbs.length];
            for (int i = 0; i < rgbs.length; i++) {
                RGB rgb = rgbs[i];
                red[i] = (byte) rgb.red;
                green[i] = (byte) rgb.green;
                blue[i] = (byte) rgb.blue;
            }
            if (data.transparentPixel != -1) {
                colorModel = new IndexColorModel(data.depth, rgbs.length, red,
                        green, blue, data.transparentPixel);
            } else {
                colorModel = new IndexColorModel(data.depth, rgbs.length, red,
                        green, blue);
            }
            BufferedImage bufferedImage = new BufferedImage(colorModel,
                    colorModel.createCompatibleWritableRaster(data.width,
                            data.height), false, null);
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[1];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    int pixel = data.getPixel(x, y);
                    pixelArray[0] = pixel;
                    raster.setPixel(x, y, pixelArray);
                }
            }
            return bufferedImage;
        }
    }
	
}
