package org.gumtree.control.ui.viewer;

import org.gumtree.control.events.ThreadPool;

public class ControlRunner {

	private static final int CONTROL_UI_THREAD_POOL_SIZE = 10;
	private ThreadPool threadPool;
	
	public ControlRunner() {
		threadPool = new ThreadPool(CONTROL_UI_THREAD_POOL_SIZE);
	}
	
	public void delayedProcess(Runnable runnable) {
		threadPool.run(runnable);
	}
	
}
