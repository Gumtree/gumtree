package org.gumtree.widgets.swt.util;

import org.eclipse.jface.util.SafeRunnable;

public abstract class ParameterizedSafeRunnable<T> extends SafeRunnable {

	private T context;

	public ParameterizedSafeRunnable(T context) {
		super();
		this.context = context;
	}

	public ParameterizedSafeRunnable(T context, String message) {
		super(message);
		this.context = context;
	}

	@Override
	public void run() throws Exception {
		run(getContext());
	}

	public abstract void run(T context) throws Exception;

	public T getContext() {
		return context;
	}

}
