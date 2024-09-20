/**
 * 
 */
package org.gumtree.control.events;

import org.json.JSONObject;

/**
 * @author nxi
 *
 */
public interface ISicsMessageListener {

	void messageReceived(JSONObject message);
	
}
