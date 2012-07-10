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

package org.gumtree.gumnix.sics.internal.core;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.core.service.IServiceManager;
import org.gumtree.gumnix.sics.core.IInstrumentReadyCriterion;
import org.gumtree.gumnix.sics.core.IInstrumentReadyManager;
import org.gumtree.gumnix.sics.core.IInstrumentReadyStatus;
import org.gumtree.gumnix.sics.core.InstrumentReadyStatus;

public class InstrumentReadyManager implements IInstrumentReadyManager {

	private IServiceManager serverManager;

	public IInstrumentReadyStatus isInstrumentReady() {
		List<IInstrumentReadyStatus> result = new ArrayList<IInstrumentReadyStatus>();
		List<IInstrumentReadyCriterion> criteria = getServerManager()
				.getServices(IInstrumentReadyCriterion.class);
		for (IInstrumentReadyCriterion criterion : criteria) {
			result.add(criterion.checkInstrumentReady());
		}
		return new InstrumentReadyStatus(result);
	}

	public IServiceManager getServerManager() {
		return serverManager;
	}

	public void setServiceManager(IServiceManager serverManager) {
		this.serverManager = serverManager;
	}

}
