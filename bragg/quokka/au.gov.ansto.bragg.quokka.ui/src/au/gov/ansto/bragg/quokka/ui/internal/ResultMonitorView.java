package au.gov.ansto.bragg.quokka.ui.internal;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.service.directory.IDirectoryService;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.util.messaging.EventHandler;
import org.osgi.service.event.Event;

import au.gov.ansto.bragg.quokka.experiment.report.ExperimentUserReport;
import au.gov.ansto.bragg.quokka.experiment.report.ExperimentUserReportUtils;

public class ResultMonitorView extends ViewPart {

	private EventHandler eventHandler;

	public ResultMonitorView() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayoutFactory.fillDefaults().applyTo(parent);
		final Text text = new Text(parent, SWT.MULTI | SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, true).applyTo(text);

		eventHandler = new EventHandler(IDirectoryService.EVENT_TOPIC_BIND) {
			@Override
			public void handleEvent(Event event) {
				if (event.getProperty(IDirectoryService.EVENT_PROP_NAME)
						.equals(ExperimentUserReport.class.getName())) {
					Object object = event
							.getProperty(IDirectoryService.EVENT_PROP_OBJECT);
					if (object instanceof ExperimentUserReport) {
						final String xml = ExperimentUserReportUtils
								.getXStream().toXML(object);
						SafeUIRunner.asyncExec(new SafeRunnable() {
							@Override
							public void run() throws Exception {
								text.setText("");
								text.setText(xml);
							}
						});
					}
				}
			}
		};
	}

	@Override
	public void setFocus() {
	}

	public void dispose() {
		if (eventHandler != null) {
			eventHandler.deactivate();
			eventHandler = null;
		}
		super.dispose();
	}

}
