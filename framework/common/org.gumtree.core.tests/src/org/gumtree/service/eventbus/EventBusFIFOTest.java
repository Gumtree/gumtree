package org.gumtree.service.eventbus;

import java.text.SimpleDateFormat;

import junit.framework.TestCase;

import org.gumtree.service.eventbus.support.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventBusFIFOTest extends TestCase {

	private static final Logger logger = LoggerFactory
			.getLogger(EventBusFIFOTest.class);

	public void testFIFO() throws Exception {
		IEventBus eventBus = new EventBus();
		IEventHandler<LabelEvent> handler = new IEventHandler<LabelEvent>() {
			public void handleEvent(LabelEvent event) {
				SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss.SSS");
				logger.info(format.format(event.getTime()) + " "
						+ event.getLabel());
			}
		};
		eventBus.subscribe(handler);
		for (int i = 0; i < 10; i++) {
			IEvent event = new LabelEvent(this, i + "");
			Thread.sleep(1);
			eventBus.postEvent(event);
		}
		Thread.sleep(1000);
	}

}
