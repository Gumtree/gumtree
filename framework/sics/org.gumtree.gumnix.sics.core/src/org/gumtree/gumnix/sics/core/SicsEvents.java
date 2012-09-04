package org.gumtree.gumnix.sics.core;

public class SicsEvents {

	public static final String TOPIC_BASE = "org/gumtree/gumnix/sics";

	public static final String SEP = "/";
	
	public static final class Proxy {

		public static final String TOPIC_ALL = TOPIC_BASE + "/proxy/*";

		public static final String TOPIC_CONNECTED = TOPIC_BASE
				+ "/proxy/connected";

		public static final String TOPIC_DISCONNECTED = TOPIC_BASE
				+ "/proxy/disconnected";

		public static final String PROXY = "proxy";

		public static final String PROXY_ID = "proxyId";

	}

	public static final class Server {

		public static final String TOPIC_ALL = TOPIC_BASE + "/server/*";

		public static final String TOPIC_SERVER_STATUS = TOPIC_BASE
				+ "/server/status";

		public static final String STATUS = "status";

	}
	
	public static final class HNotify {
		
		public static final String TOPIC_ALL = TOPIC_BASE + "/hnotify/*";
		
		public static final String TOPIC_HNOTIFY = TOPIC_BASE + "/hnotify";
		
		public static final String VALUE = "value";
		
	}

}