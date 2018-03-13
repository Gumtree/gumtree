/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.control.ui.batch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InstrumentReadyStatus {

	public static InstrumentReadyStatus READY_STATUS = new InstrumentReadyStatus(true, "");
	
	private List<String> messages;
	
	private boolean isReady;
	
	public InstrumentReadyStatus(boolean isReady, String message) {
		messages = new ArrayList<String>(2);
		messages.add(message);
		this.isReady = isReady;
	}
	
	public InstrumentReadyStatus(List<InstrumentReadyStatus> statusList) {
		messages = new ArrayList<String>(2);
		isReady = true;
		for (InstrumentReadyStatus status : statusList) {
			// Aggregates ready flags
			isReady &= status.isInstrumentReady();
			// Aggregates messages
			messages.addAll(Arrays.asList(status.getMessages()));
		}
	}

	public String[] getMessages() {
		return messages.toArray(new String[messages.size()]);
	}

	public boolean isInstrumentReady() {
		return isReady;
	}

	@Override
	public String toString() {
		return "InstrumentReadyStatus [isReady=" + isReady + ", messages="
				+ Arrays.toString(getMessages()).toString() + "]";
	}

}
