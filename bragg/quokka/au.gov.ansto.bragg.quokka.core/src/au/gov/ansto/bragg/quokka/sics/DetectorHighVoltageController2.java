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

package au.gov.ansto.bragg.quokka.sics;

import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.control.controllers.ISicsObjectController;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.ISicsProxy;
import org.gumtree.gumnix.sics.io.SicsExecutionException;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.LoopRunner;

import au.gov.ansto.bragg.quokka.core.internal.QuokkaCoreProperties;

public class DetectorHighVoltageController2 implements ISicsObjectController {
	
	private static final String DEVICE_DHV1 = "dhv1";
	
	private ControllerStatus status;
	
	private Float value;
	
	public DetectorHighVoltageController2() {
		setStatus(ControllerStatus.OK);
	}
	
	public String getId() {
		return DEVICE_DHV1;
	}
	
	public ControllerStatus getStatus() {
		return status;
	}
	
	protected void setStatus(ControllerStatus status) {
		this.status = status;
	}

	public float getValue() throws SicsIOException {
		return value;
	}

	public void drive(VoltageControllerCommand command) throws SicsIOException, SicsExecutionException {
		// Return straight away under simulation mode
		if (QuokkaCoreProperties.SICS_SIMULATION_MODE.getBoolean()) {
			return;
		}
	}

	
	public void run(VoltageControllerCommand command) throws SicsIOException, SicsExecutionException {
		SicsCore.getSicsController().clearInterrupt();
		
		// Return straight away under simulation mode
		if (QuokkaCoreProperties.SICS_SIMULATION_MODE.getBoolean()) {
			return;
		}
		
		// Wait for 1 sec to stablise
		LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				System.out.println("Loop 1: " + getStatus().name());
				return getStatus().equals(ControllerStatus.OK);
			}
		}, 1000);
//		System.out.println("Sending dhv1 " + command);
		// This command is blockable
		SicsCore.getDefaultProxy().send(DEVICE_DHV1 + " " + command.getCommand(), null, ISicsProxy.CHANNEL_SCAN);
		// Wait for 1 sec to run
		LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return getStatus().equals(ControllerStatus.RUNNING);
			}
		}, 1000);
//		System.out.println("dhv1 is running");
		// Can't get into the next waiting loop too quick
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		// Wait until it has finished
		LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return getStatus().equals(ControllerStatus.OK);
			}
		}, LoopRunner.NO_TIME_OUT);
		if (SicsCore.getSicsController().isInterrupted()) {
			SicsCore.getSicsController().clearInterrupt();
			throw new SicsExecutionException("Interrupted");
		}
		System.out.println("dhv1 is done");
	}
	
}
