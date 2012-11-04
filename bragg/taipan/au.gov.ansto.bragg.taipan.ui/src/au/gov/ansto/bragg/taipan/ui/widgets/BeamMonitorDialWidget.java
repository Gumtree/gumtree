package au.gov.ansto.bragg.taipan.ui.widgets;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Point;
import java.net.URI;
import java.text.NumberFormat;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.gumnix.sics.control.ISicsMonitor;
import org.gumtree.gumnix.sics.ui.widgets.AbstractSicsWidget;
import org.gumtree.service.dataaccess.IDataHandler;
import org.gumtree.util.messaging.DelayEventHandler;
import org.gumtree.util.messaging.IDelayEventExecutor;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.dial.DialBackground;
import org.jfree.chart.plot.dial.DialCap;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.dial.DialPointer.Pointer;
import org.jfree.chart.plot.dial.DialTextAnnotation;
import org.jfree.chart.plot.dial.StandardDialFrame;
import org.jfree.chart.plot.dial.StandardDialRange;
import org.jfree.chart.plot.dial.StandardDialScale;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.StandardGradientPaintTransformer;
import org.osgi.service.event.Event;

@SuppressWarnings("restriction")
public class BeamMonitorDialWidget extends AbstractSicsWidget {

	private DelayEventHandler eventHandler;

	private long maximumValue;

	private UIContext context;

	@Inject
	@Named("devicePath")
	@Optional
	private String devicePath;

	@Inject
	@Named("unit")
	@Optional
	private String unit;
	
	@Inject
	private IDelayEventExecutor delayEventExecutor;
	
	private long maximumDisplayValue;

	public BeamMonitorDialWidget(Composite parent, int style) {
		super(parent, style);
		context = new UIContext();
		setMaximumDisplayValue(10);
		context.dataset = new DefaultValueDataset(0);
	}

	@Override
	protected void handleRender() {
		GridLayoutFactory.swtDefaults().applyTo(this);
		Composite embedComposite = getWidgetFactory().createComposite(this,
				SWT.EMBEDDED);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).hint(SWT.DEFAULT, 150)
				.grab(true, true).applyTo(embedComposite);
		Frame frame = SWT_AWT.new_Frame(embedComposite);
		createChart();
		context.chartpanel = new ChartPanel(context.chart);
		frame.add(context.chartpanel);

		final Combo combo = getWidgetFactory().createCombo(this,
				SWT.READ_ONLY | SWT.RIGHT);
		combo.setBackground(getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));
		combo.setItems(new String[] { "x10", "x100", "x1000", "x10000", "x100000" });
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String maxValue = combo.getItem(combo.getSelectionIndex())
						.substring(1);
				setMaximumValue(Long.parseLong(maxValue));
				updateUI(context.deviceValue);
			}
		});
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(combo);

		eventHandler = new DelayEventHandler(ISicsMonitor.EVENT_TOPIC_HNOTIFY
				+ getDevicePath(), getDelayEventExecutor()) {
			@Override
			public void handleDelayEvent(Event event) {
				try {
					String value = (String) event
							.getProperty(ISicsMonitor.EVENT_PROP_VALUE);
					context.deviceValue = Float.parseFloat(value);
					updateUI(context.deviceValue);
				} catch (Exception e) {
				}
			}
		};
		
		// Default setting
		setMaximumValue(1000);
		combo.select(2);
	}

	protected void createChart() {
		DialPlot dialplot = new DialPlot();
		dialplot.setDataset(context.dataset);
		dialplot.setDialFrame(new StandardDialFrame());
		dialplot.setBackground(new DialBackground());

		DialTextAnnotation dialtextannotation = new DialTextAnnotation(
				getUnit());
		dialtextannotation.setFont(new Font("Dialog", 1, 14));
		dialtextannotation.setRadius(0.69999999999999996D);
		dialplot.addLayer(dialtextannotation);

		context.standarddialscale = new StandardDialScale();
		context.standarddialscale.setLowerBound(0);
		context.standarddialscale.setUpperBound(getMaximumDisplayValue());
		context.standarddialscale.setStartAngle(-120D);
		context.standarddialscale.setExtent(-300D);
		context.standarddialscale.setMajorTickIncrement(1);
		context.standarddialscale.setMinorTickCount(1);
		context.standarddialscale.setTickRadius(0.88D);
		context.standarddialscale.setTickLabelOffset(0.14999999999999999D);
		context.standarddialscale.setTickLabelFont(new Font("Dialog", 0, 14));
		context.standarddialscale.setTickLabelFormatter(NumberFormat
				.getIntegerInstance());
		dialplot.addScale(0, context.standarddialscale);

		dialplot.addPointer(new org.jfree.chart.plot.dial.DialPointer.Pin());
		DialCap dialcap = new DialCap();
		dialplot.setCap(dialcap);

		context.standarddialrangeLow = new StandardDialRange();
		context.standarddialrangeLow.setPaint(Color.RED);
		context.standarddialrangeLow.setInnerRadius(0.52000000000000002D);
		context.standarddialrangeLow.setOuterRadius(0.55000000000000004D);
		context.standarddialrangeLow.setLowerBound(0);
		context.standarddialrangeLow.setUpperBound(getMaximumDisplayValue() * 0.5);
		dialplot.addLayer(context.standarddialrangeLow);

		context.standarddialrangeMid = new StandardDialRange();
		context.standarddialrangeMid.setPaint(Color.ORANGE);
		context.standarddialrangeMid.setInnerRadius(0.52000000000000002D);
		context.standarddialrangeMid.setOuterRadius(0.55000000000000004D);
		context.standarddialrangeMid.setLowerBound(getMaximumDisplayValue() * 0.5);
		context.standarddialrangeMid.setUpperBound(getMaximumDisplayValue() * 0.75);
		dialplot.addLayer(context.standarddialrangeMid);

		context.standarddialrangeHigh = new StandardDialRange();
		context.standarddialrangeHigh.setPaint(Color.GREEN);
		context.standarddialrangeHigh.setInnerRadius(0.52000000000000002D);
		context.standarddialrangeHigh.setOuterRadius(0.55000000000000004D);
		context.standarddialrangeHigh.setLowerBound(getMaximumDisplayValue() * 0.75);
		context.standarddialrangeHigh.setUpperBound(getMaximumDisplayValue());
		dialplot.addLayer(context.standarddialrangeHigh);

		GradientPaint gradientpaint = new GradientPaint(new Point(), new Color(
				255, 255, 255), new Point(), new Color(170, 170, 220));
		DialBackground dialbackground = new DialBackground(gradientpaint);
		dialbackground
				.setGradientPaintTransformer(new StandardGradientPaintTransformer(
						GradientPaintTransformType.VERTICAL));
		dialplot.setBackground(dialbackground);
		dialplot.removePointer(0);
		dialplot.addPointer(new Pointer());

		context.chart = new JFreeChart(dialplot);
	}

	private void updateUI(float deviceValue) {
		if (context != null && context.dataset != null) {
			float displayValue = deviceValue / getMaximumValue();
			if (displayValue > 10.0) {
				displayValue = 10.0f;
			}
			context.dataset.setValue(displayValue);
		}
	}

	@Override
	protected void handleSicsConnect() {
		// Fetch initial value and start event listening
		getDataAccessManager().get(URI.create("sics://hdb" + getDevicePath()),
				Float.class, new IDataHandler<Float>() {
					@Override
					public void handleData(URI uri, Float data) {
						updateUI(data);
						if (eventHandler != null) {
							eventHandler.activate();
						}
					}

					@Override
					public void handleError(URI uri, Exception exception) {
					}
				});
	}

	@Override
	protected void handleSicsDisconnect() {
		if (eventHandler != null) {
			eventHandler.deactivate();
		}
		updateUI(0);
	}

	@Override
	protected void disposeWidget() {
		if (eventHandler != null) {
			eventHandler.deactivate();
			eventHandler = null;
		}
		context = null;
		devicePath = null;
		delayEventExecutor = null;
	}

	private class UIContext {
		float deviceValue;
		DefaultValueDataset dataset;
		JFreeChart chart;
		ChartPanel chartpanel;
		StandardDialScale standarddialscale;
		StandardDialRange standarddialrangeLow;
		StandardDialRange standarddialrangeMid;
		StandardDialRange standarddialrangeHigh;
	}

	/*************************************************************************
	 * Properties
	 *************************************************************************/

	public String getDevicePath() {
		return devicePath;
	}

	public void setDevicePath(String devicePath) {
		this.devicePath = devicePath;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public IDelayEventExecutor getDelayEventExecutor() {
		return delayEventExecutor;
	}

	public void setDelayEventExecutor(IDelayEventExecutor delayEventExecutor) {
		this.delayEventExecutor = delayEventExecutor;
	}

	public long getMaximumValue() {
		return maximumValue;
	}

	public void setMaximumValue(long maximumValue) {
		this.maximumValue = maximumValue;
	}

	public long getMaximumDisplayValue() {
		return maximumDisplayValue;
	}

	public void setMaximumDisplayValue(long maximumDisplayValue) {
		this.maximumDisplayValue = maximumDisplayValue;
	}

	public JFreeChart getChart() {
		return context.chart;
	}

}
