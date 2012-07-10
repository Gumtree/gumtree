package org.gumtree.gumnix.sics.simulator.server;

import org.gumtree.gumnix.sics.simulator.objects.IHNotify;
import org.json.JSONException;
import org.json.JSONObject;

public class StressTestThread extends Thread {

	public StressTestThread() {
		super();
	}
	
	public void run() {
		boolean data = false;
		while (SicsSimulationServer.getDefault() != null) {
			IHNotify notify = (IHNotify)SicsSimulationServer.getDefault().getObjectLibrary().getSicsObject(IHNotify.ID);
			JSONObject jsonObject = new JSONObject();
			data = !data;
			try {
				jsonObject.put("/experiment/title", Boolean.valueOf(data).toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			// Good message
			notify.callback(jsonObject);
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
