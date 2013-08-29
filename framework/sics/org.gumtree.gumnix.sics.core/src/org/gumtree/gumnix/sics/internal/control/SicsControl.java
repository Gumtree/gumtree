package org.gumtree.gumnix.sics.internal.control;

import org.gumtree.gumnix.sics.control.ISicsBatchControl;
import org.gumtree.gumnix.sics.control.ISicsControl;
import org.gumtree.gumnix.sics.control.ISicsController;
import org.gumtree.gumnix.sics.core.ISicsManager;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.psi.sics.hipadaba.SICS;

public class SicsControl implements ISicsControl {

	private static Logger logger;

	private ISicsManager manager;

	private ISicsBatchControl batchControl;

	private ISicsController controller;

	private boolean isLocked = false;
	
	public SicsControl(ISicsManager manager) {
		this.manager = manager;
	}

	public ISicsController getSicsController() {
		synchronized (this) {
			if(controller == null && !isLocked) {
				try {
					isLocked = true;
					System.err.println("try to create new controller");
					SICS model = manager.service().getOnlineModel();
					controller = new SicsController(model);
					isLocked = false;
				} catch (SicsIOException e) {
					// No need to log exception trace stack here because it's very usual
					// to get this exception during GumTree intialisation phase.
					getLogger().warn("Cannot retrieve online model for creating SICS controller");
					isLocked = false;
				} finally {
					isLocked = false;
				}
			}
			return controller;
		}
	}

	public ISicsBatchControl batch() {
		if(batchControl == null) {
			batchControl = new SicsBatchControl(manager);
		}
		return batchControl;
	}

	private static Logger getLogger() {
		if(logger == null) {
			logger = LoggerFactory.getLogger(SicsControl.class);
		}
		return logger;
	}

}
