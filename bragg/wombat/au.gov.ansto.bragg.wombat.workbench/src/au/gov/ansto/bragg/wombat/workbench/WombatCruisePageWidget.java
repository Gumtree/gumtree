package au.gov.ansto.bragg.wombat.workbench;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.nebula.widgets.pgroup.ChevronsToggleRenderer;
import org.eclipse.nebula.widgets.pgroup.PGroup;
import org.eclipse.nebula.widgets.pgroup.SimpleGroupStrategy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.gumtree.gumnix.sics.ui.widgets.HMVetoGadget;
import org.gumtree.gumnix.sics.widgets.swt.DeviceStatusWidget;
import org.gumtree.gumnix.sics.widgets.swt.EnvironmentControlWidget;
import org.gumtree.gumnix.sics.widgets.swt.ShutterStatusWidget;
import org.gumtree.gumnix.sics.widgets.swt.SicsStatusWidget;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.ui.cruise.support.AbstractCruisePageWidget;
import org.gumtree.util.messaging.IDelayEventExecutor;
import org.gumtree.util.messaging.ReducedDelayEventExecutor;

import au.gov.ansto.bragg.nbi.ui.core.SharedImage;
import au.gov.ansto.bragg.nbi.workbench.ReactorStatusWidget;
import au.gov.ansto.bragg.wombat.workbench.internal.InternalImage;

@SuppressWarnings("restriction")
public class WombatCruisePageWidget extends AbstractCruisePageWidget {

	private IEclipseContext eclipseContext;

	private IDelayEventExecutor delayEventExecutor;

	@Inject
	private IDataAccessManager dataAccessManager;

	public WombatCruisePageWidget(Composite parent, int style) {
		super(parent, style);
	}

	public WombatCruisePageWidget render() {
		GridLayoutFactory.swtDefaults().applyTo(this);
		getEclipseContext().set(IDelayEventExecutor.class,
				getDelayEventExecutor());
		
		PGroup sourceGroup = createGroup("REACTOR SOURCE",
				SharedImage.REACTOR.getImage());
		ReactorStatusWidget reactorWidget = new ReactorStatusWidget(sourceGroup, SWT.NONE);
		reactorWidget.addDevice("reactorPower", "Power", "MW");
		reactorWidget.createWidgetArea();
//		DeviceStatusWidget deviceStatusWidget = new DeviceStatusWidget(
//				sourceGroup, SWT.NONE);
//		deviceStatusWidget.addDevice("/instrument/source/power", "Power",
//				SharedImage.POWER.getImage(), null);
		configureWidget(reactorWidget);
		sourceGroup.setExpanded(false);
		reactorWidget.setExpandingEnabled(true);


		// Shutter Status
		PGroup shutterGroup = createGroup("SHUTTER STATUS",
				SharedImage.SHUTTER.getImage());
		ShutterStatusWidget shutterStatuswidget = new ShutterStatusWidget(
				shutterGroup, SWT.NONE);
		configureWidget(shutterStatuswidget);

		// SICS status
		PGroup sicsStatusGroup = createGroup("SICS STATUS", null);
		SicsStatusWidget statusWidget = new SicsStatusWidget(sicsStatusGroup,
				SWT.NONE);
		ContextInjectionFactory.inject(statusWidget, getEclipseContext());
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(statusWidget);

		// Pause Counter
		PGroup pauseGroup = createGroup("PAUSE COUNTING",
				SharedImage.SHUTTER.getImage());
		HMVetoGadget pauseStatuswidget = new HMVetoGadget(
				pauseGroup, SWT.NONE);
		configureWidget(pauseStatuswidget);
//		pauseStatuswidget.render();

		// Experiment info
		PGroup infoGroup = createGroup("EXPERIMENT INFO",
				InternalImage.EXPERIMENT_INFO.getImage());
		DeviceStatusWidget deviceStatusWidget = new DeviceStatusWidget(infoGroup, SWT.NONE);
		deviceStatusWidget.addDevice("/experiment/title", "Proposal", null, "")
				.addDevice("/sample/name", "Sample", null, "")
				.addDevice("/user/name", "User", null, "");
		configureWidget(deviceStatusWidget);

		// Experiment Status
		PGroup statusGroup = createGroup("MOTOR STATUS",
				InternalImage.EXPERIMENT_STATUS.getImage());
		deviceStatusWidget = new DeviceStatusWidget(statusGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/instrument/crystal/takeoff_angle", "mtth")
				.addDevice("/instrument/crystal/rotate", "mom")
				.addDevice("/instrument/monochromator/mf2", "mf2")
				.addSeparator()
				.addDevice("/sample/azimuthal_angle", "stth")
				.addDevice("/sample/rotate", "som")
				.addSeparator()
				.addDevice("/instrument/collimator/oct", "oct")
				;
		configureWidget(deviceStatusWidget);

		PGroup sampleGroup = createGroup("SAMPLE STAGE",
				InternalImage.SAMPLE.getImage());
		deviceStatusWidget = new DeviceStatusWidget(sampleGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/sample/translate_x", "sx")
				.addDevice("/sample/translate_y", "sy")
				.addDevice("/sample/chi", "schi")
				.addDevice("/sample/phi", "sphi")
				;
		configureWidget(deviceStatusWidget);

		PGroup cradleGroup = createGroup("EULER CRADLE",
				InternalImage.CRADLE.getImage());
		deviceStatusWidget = new DeviceStatusWidget(cradleGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/sample/euler_omega", "eom")
				.addDevice("/sample/euler_chi", "echi")
				.addDevice("/sample/euler_phi", "ephi")
				;
		configureWidget(deviceStatusWidget);

		// Monitor Event Rate
		PGroup monitorGroup = createGroup("NEUTRON COUNTS",
				InternalImage.MONITOR.getImage());
		deviceStatusWidget = new DeviceStatusWidget(monitorGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/monitor/bm1_counts", "BM1 Counts", null, "")
				.addDevice("/monitor/bm2_counts", "BM2 Counts", null, "")
				.addDevice("/monitor/bm3_counts", "BM3 Counts", null, "")
				.addDevice("/instrument/detector/total_counts", "Total Detector", null, "")
				;
		configureWidget(deviceStatusWidget);

		// Environment Group
		PGroup environmentGroup = createGroup("ENVIRONMENT CONTROLLERS",
				InternalImage.FURNACE.getImage());
		EnvironmentControlWidget controlWidget = new EnvironmentControlWidget(environmentGroup, SWT.NONE);
		configureWidget(controlWidget);

		return this;
	}

	@Override
	protected void disposeWidget() {
		if (delayEventExecutor != null) {
			delayEventExecutor.deactivate();
			delayEventExecutor = null;
		}
		eclipseContext = null;
		dataAccessManager = null;
	}

	/*************************************************************************
	 * Components
	 *************************************************************************/

	public IDataAccessManager getDataAccessManager() {
		return dataAccessManager;
	}

	public void setDataAccessManager(IDataAccessManager dataAccessManager) {
		this.dataAccessManager = dataAccessManager;
	}

	public IEclipseContext getEclipseContext() {
		return eclipseContext;
	}

	@Inject
	public void setEclipseContext(IEclipseContext eclipseContext) {
		this.eclipseContext = eclipseContext;
	}

	/*************************************************************************
	 * Utilities
	 *************************************************************************/

	protected PGroup createGroup(String text, Image image) {
		PGroup group = new PGroup(this, SWT.NONE);
		group.setStrategy(new SimpleGroupStrategy());
		group.setToggleRenderer(new ChevronsToggleRenderer());
		group.setText(text);
		group.setFont(JFaceResources.getFontRegistry().getBold(
				JFaceResources.DEFAULT_FONT));
		group.setImage(image);
		group.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		group.setLinePosition(SWT.CENTER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(group);
		GridLayoutFactory.swtDefaults().numColumns(1).spacing(1, 1)
				.margins(10, 0).applyTo(group);
		return group;
	}

	protected void configureWidget(Control widget) {
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(widget);
		if (getEclipseContext() != null) {
			ContextInjectionFactory.inject(widget, getEclipseContext());
		}
	}

	public IDelayEventExecutor getDelayEventExecutor() {
		if (delayEventExecutor == null) {
			delayEventExecutor = new ReducedDelayEventExecutor(1000).activate();
		}
		return delayEventExecutor;
	}

	public void setDelayEventExecutor(IDelayEventExecutor delayEventExecutor) {
		this.delayEventExecutor = delayEventExecutor;
	}

}
