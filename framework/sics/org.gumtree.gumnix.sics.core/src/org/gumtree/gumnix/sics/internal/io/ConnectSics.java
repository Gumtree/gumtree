package org.gumtree.gumnix.sics.internal.io;

import org.gumtree.gumnix.sics.io.ISicsConnectionContext;
import org.gumtree.gumnix.sics.io.ISicsReplyData;
import org.gumtree.gumnix.sics.io.ISicsProxy;
import org.gumtree.gumnix.sics.io.SicsCallbackAdapter;
import org.gumtree.gumnix.sics.io.SicsConnectionContext;
import org.gumtree.gumnix.sics.io.SicsExecutionException;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.gumnix.sics.io.SicsRole;

public class ConnectSics {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ISicsProxy proxy = new SicsProxy();
		ISicsConnectionContext context = new SicsConnectionContext("localhost", 60103, SicsRole.USER, "sydney");
		try {
			proxy.login(context);
			proxy.send("hlist /", new SicsCallbackAdapter() {
				public void receiveReply(ISicsReplyData response) {
//					System.out.println(response.getObject().toString());
					setCallbackCompleted(true);
				}
			});
			proxy.disconnect();
		} catch (SicsIOException e) {
			e.printStackTrace();
		} catch (SicsExecutionException e) {
			e.printStackTrace();
		}
	}

}
