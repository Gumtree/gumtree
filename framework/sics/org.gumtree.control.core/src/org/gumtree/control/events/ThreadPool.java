package org.gumtree.control.events;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPool {

	private static final int POOL_SIZE = 1;
	
	private ThreadPoolExecutor executor;
	
	public ThreadPool() {
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(POOL_SIZE);
	}

	public void run(Runnable runnable) {
		executor.submit(runnable);
	}
	
	public int getCurrentPoolSize() {
		return executor.getPoolSize();
	}
	
}
