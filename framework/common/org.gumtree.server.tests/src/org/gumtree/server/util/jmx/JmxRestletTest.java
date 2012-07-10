package org.gumtree.server.util.jmx;

import static org.junit.Assert.*;

import java.io.IOException;

import org.gumtree.server.restlet.jmx.JmxRestlet;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Status;

public class JmxRestletTest {

	@Test
	public void testBasicRequest() throws JSONException, IOException {
		JmxRestlet restlet = new JmxRestlet();
		Request request = new Request(Method.GET, "/");
		Response response = new Response(request);
		restlet.handle(request, response);
		JSONObject result = new JSONObject(response.getEntity().getText());
		// Check status is OK
		assertEquals(Status.SUCCESS_OK, response.getStatus());
		// Check content exists by looking at the time attribute
		assertNotNull(result.get("time"));
	}
	
}
