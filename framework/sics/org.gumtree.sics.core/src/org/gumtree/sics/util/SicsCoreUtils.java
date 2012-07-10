package org.gumtree.sics.util;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.eclipse.core.runtime.ISafeRunnable;
import org.gumtree.sics.io.ISicsProxy;
import org.gumtree.util.eclipse.FilterBuilder;

public final class SicsCoreUtils {

	public static Executor executor = Executors.newFixedThreadPool(3);

	public static void execute(final ISafeRunnable runnable) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					runnable.run();
				} catch (Throwable exception) {
					runnable.handleException(exception);
				}
			}
		});
	}
	
	public static String createProxyFilter(ISicsProxy proxy) {
		return createProxyFilter(proxy.getId());
	}
	
	public static String createProxyFilter(String proxyId) {
		return new FilterBuilder(ISicsProxy.EVENT_PROP_PROXY, proxyId).get();
	}

	private SicsCoreUtils() {
		super();
	}

}
