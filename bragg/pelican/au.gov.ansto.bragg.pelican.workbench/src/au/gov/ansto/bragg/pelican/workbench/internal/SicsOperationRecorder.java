package au.gov.ansto.bragg.pelican.workbench.internal;

import org.gumtree.ui.terminal.support.telnet.TelnetAdapter;
import org.gumtree.util.messaging.DelayEventHandler;
import org.gumtree.util.messaging.EventHandler;
import org.gumtree.util.messaging.IDelayEventExecutor;
import org.gumtree.util.messaging.StandardDelayEventExecutor;
import org.osgi.service.event.Event;

public class SicsOperationRecorder {

	private EventHandler eventHandler;

	private IDelayEventExecutor eventExecutor;

	public SicsOperationRecorder() {
		eventExecutor = new StandardDelayEventExecutor(1000);
		eventHandler = new DelayEventHandler(TelnetAdapter.EVENT_TOPIC_TELNET
				+ "/*", eventExecutor) {
			@Override
			public void handleDelayEvent(Event event) {
				System.out.println("SICS message: "
						+ event.getProperty(TelnetAdapter.EVENT_PROP_MESSAGE)
								.toString());
			}
		};
	}

	public void activate() {
		eventExecutor.activate();
		eventHandler.activate();
	}

	public void deactivate() {
		eventHandler.deactivate();
		eventExecutor.deactivate();
	}

}
