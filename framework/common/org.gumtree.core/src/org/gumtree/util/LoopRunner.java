/*******************************************************************************
 * Copyright (c) 2012 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bragg Institute - initial API and implementation
 ******************************************************************************/

package org.gumtree.util;

public class LoopRunner {

	public static final int NO_TIME_OUT = -1;
	
	public static final int TIME_OUT = 1000;

	public static final int WAIT_TIME = 10;

	public static LoopRunnerStatus run(ILoopExitCondition condition) {
		return run(condition, TIME_OUT, WAIT_TIME);
	}

	public static LoopRunnerStatus run(ILoopExitCondition condition, int timeOut) {
		return run(condition, timeOut, WAIT_TIME);
	}
	
	public static LoopRunnerStatus run(ILoopExitCondition condition, int timeOut, int waitTime) {
		if(condition == null) {
			return LoopRunnerStatus.ERROR;
		}
		int count = 0;
		while(true) {
			if(condition.getExitCondition()) {
				break;
			}
			try {
				Thread.sleep(waitTime);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			count += waitTime;
			if(timeOut != NO_TIME_OUT && count >= timeOut) {
				return LoopRunnerStatus.TIMEOUT;
			}
		}
		return LoopRunnerStatus.OK;
	}
	
	private LoopRunner() {
		super();
	}
	
}
