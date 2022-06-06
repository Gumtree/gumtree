/*******************************************************************************
 * Copyright (c) 2010 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 ******************************************************************************/
package au.gov.ansto.bragg.nbi.ui.realtime.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.gumtree.control.core.ISicsProxy;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.ui.batch.SicsBatchUIUtils;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.LoopRunner;

/**
 * @author nxi
 *
 */
public class ControlRealtimeDataViewer extends Composite {

	private static final String SICS_REALTIME_MONITOR_PROPERTY_NAME = "sics.realtime.monitor.devices";
	private RealtimeDataComposite viewer;

	public ControlRealtimeDataViewer(Composite parent, int style) {
		super(parent, style);
		viewer = new RealtimeDataComposite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(viewer);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer);
//		SicsRealtimeRourceProvider provider = new SicsRealtimeRourceProvider();
//		provider.setFilter(getDeviceFilter());
//		viewer.setResourceProvider(provider);
		initSicsListener();
	}

	private void initSicsListener() {
		Thread thread = new Thread(new Runnable(){
			
			@Override
			public void run() {
				LoopRunner.run(new ILoopExitCondition() {
					
					@Override
					public boolean getExitCondition() {
						ISicsProxy proxy = SicsManager.getSicsProxy();
						if (proxy != null && proxy.isConnected()) {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
							}
							Display.getDefault().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									ControlRealtimeRourceProvider provider = new ControlRealtimeRourceProvider();
									provider.setFilter(getDeviceFilter());
									viewer.setResourceProvider(provider);
									viewer.updateResourceCombo();
								}
							});
							viewer.startUpdateThread();
							return true;
						}
						return viewer.isDisposed();
					}
				}, -1, 1000);
			}
			
		});
		thread.start();
	}

	public static List<String> getDeviceFilter(){
		String propertyString = System.getProperty(SICS_REALTIME_MONITOR_PROPERTY_NAME);
		if (propertyString != null) {
			String[] items = propertyString.split(",");
			List<String> filter = new ArrayList<String>();
			for (String item : items) {
				if (item.trim().length() > 0) {
					filter.add(item.trim());
				}
			}
			return filter;
		} else {
			String[] drivables = SicsBatchUIUtils.getSicsDrivableIds();
			return Arrays.asList(drivables);
		}
	}
	
	@Override
	public void dispose() {
		if (viewer != null && !viewer.isDisposed()) {
			viewer.dispose();
			viewer = null;
		}
		super.dispose();
	}
}
