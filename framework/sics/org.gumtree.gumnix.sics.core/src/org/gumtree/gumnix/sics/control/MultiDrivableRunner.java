package org.gumtree.gumnix.sics.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gumtree.gumnix.sics.control.controllers.DrivableController;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.SicsExecutionException;
import org.gumtree.gumnix.sics.io.SicsIOException;

public class MultiDrivableRunner {

	private Map<DrivableController, DrivableUnit> units;
	
	public MultiDrivableRunner() {
		units = new HashMap<DrivableController, DrivableUnit>();
	}
	
	public void addDrivable(DrivableController controller, float target) {
		DrivableUnit unit = new DrivableUnit();
		unit.controller = controller;
		unit.target = target;
		units.put(controller, unit);
	}
	
	public void removeDrivable(DrivableController controller) {
		units.remove(controller);
	}

	public synchronized void drive() throws Exception {
		// Clear interrupt
		SicsCore.getSicsController().clearInterrupt();
		
		// Start synchronised drive on new threads
		List<Thread> threads = new ArrayList<Thread>();
		for (final DrivableUnit unit : units.values()) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					try {
						unit.controller.drive(unit.target);
					} catch (SicsIOException e) {
						unit.exception = e;
					} catch (SicsExecutionException e) {
						unit.exception = e;
					}
				}
			});
			threads.add(thread);
			thread.start();
		}
		
		// Wait until finished
		for (Thread thread : threads) {
			thread.join();	
		}
		
		// Check exception
		for (DrivableUnit unit : units.values()) {
			if (unit.exception != null) {
				throw unit.exception;
			}
		}
	}
	
	private class DrivableUnit {
		private DrivableController controller;
		private float target;
//		private boolean stopOnError;
		private Exception exception;
	}
	
}
