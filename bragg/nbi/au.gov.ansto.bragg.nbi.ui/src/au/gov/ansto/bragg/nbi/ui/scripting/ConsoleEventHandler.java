/**
 * 
 */
package au.gov.ansto.bragg.nbi.ui.scripting;

import org.gumtree.util.messaging.EventHandler;
import org.osgi.service.event.Event;

/**
 * @author nxi
 *
 */
public class ConsoleEventHandler extends EventHandler {

	public ConsoleEventHandler() {
		super("");
	}
	
	public ConsoleEventHandler(String topic){
		super(topic);
		activate();
	}
	
	@Override
	public void handleEvent(Event event) {
		System.out.println("event caught");
		System.out.println(event.getProperty("sentMessage"));
	}
}
