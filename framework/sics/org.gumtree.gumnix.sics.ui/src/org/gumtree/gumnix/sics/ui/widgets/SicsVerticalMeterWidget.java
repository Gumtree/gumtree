package org.gumtree.gumnix.sics.ui.widgets;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.net.URI;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.ui.util.SafeUIRunner;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.dial.ArcDialFrame;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.dial.StandardDialScale;
import org.jfree.data.general.DefaultValueDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SicsVerticalMeterWidget extends AbstractSicsWidget {

	private static final Logger logger = LoggerFactory.getLogger(SicsVerticalMeterWidget.class);
	
	private DefaultValueDataset dataset;

	private Job scheduler;

	@Inject
	@Named("deviceURI")
	@Optional
	private URI deviceURI;

	@Inject
	@Named("updateInterval")
	@Optional
	public long updateInterval = 1000;
	
	@Inject
	@Named("upperBound")
	@Optional
	public long upperBound = 100000;

	public SicsVerticalMeterWidget(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	protected void handleRender() {
		setLayout(new FillLayout(SWT.VERTICAL));
		Composite embedComposite = getToolkit().createComposite(this,
				SWT.EMBEDDED);
		Frame frame = SWT_AWT.new_Frame(embedComposite);

		dataset = new DefaultValueDataset(0D);
		DialPlot dialplot = new DialPlot();
		dialplot.setView(0.83D, 0.405D, 0.132D, 0.19D);
		dialplot.setBackgroundPaint(Color.WHITE);
		dialplot.setDataset(dataset);

		ArcDialFrame arcdialframe = new ArcDialFrame(-10D, 20D);
		arcdialframe.setInnerRadius(0.69999999999999996D);
		arcdialframe.setOuterRadius(0.90000000000000002D);
		arcdialframe.setForegroundPaint(Color.BLACK);
		arcdialframe.setStroke(new BasicStroke(2F));
		dialplot.setDialFrame(arcdialframe);

		StandardDialScale standarddialscale = new StandardDialScale(0.0D, upperBound,
				-8D, 16D, upperBound / 4, 4);
		standarddialscale.setTickRadius(0.81999999999999995D);
		standarddialscale.setTickLabelOffset(-0.040000000000000001D);
		standarddialscale.setTickLabelFont(new Font("Dialog", 0, 15));
		standarddialscale.setTickLabelPaint(Color.WHITE);
		standarddialscale.setMajorTickPaint(Color.WHITE);
		standarddialscale.setMinorTickPaint(Color.WHITE);
		dialplot.addScale(0, standarddialscale);

		org.jfree.chart.plot.dial.DialPointer.Pin pin = new org.jfree.chart.plot.dial.DialPointer.Pin();
		pin.setRadius(0.83999999999999997D);
		dialplot.addLayer(pin);

		JFreeChart jfreechart = new JFreeChart(dialplot);
		ChartPanel chartpanel = new ChartPanel(jfreechart);
		frame.add(chartpanel);

		// Scheduling
		scheduler = new Job("Fetch SICS data") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					float value = getDataAccessManager().get(getDeviceURI(),
							Float.class);
					if (dataset != null) {
						dataset.setValue(value);
					}
				} catch (Exception e) {

				}
				schedule(getUpdateInterval());
				return Status.OK_STATUS;
			}
		};
		scheduler.schedule();
	}

	public void updateUI() {
		if (dataset == null) {
			return;
		}
		if (SicsCore.getDefaultProxy().isConnected()) {
			dataset.setValue(10.0f);
		} else {
			dataset.setValue(0.0f);
		}
		scheduler = null;
	}

	@Override
	protected void handleSicsConnect() {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				updateUI();
			}
		});
	}

	@Override
	protected void handleSicsDisconnect() {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				updateUI();
			}
		});
	}

	private void disposeWidget() {
		dataset = null;
		deviceURI = null;
	}

	/*************************************************************************
	 * Properties
	 *************************************************************************/

	public URI getDeviceURI() {
		return deviceURI;
	}

	public void setDeviceURI(URI deviceURI) {
		this.deviceURI = deviceURI;
	}

	public long getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(long updateInterval) {
		this.updateInterval = updateInterval;
	}

	@Override
	public void afterParametersSet() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void widgetDispose() {
		disposeWidget();
	}
}
