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
package au.gov.ansto.bragg.nbi.ui.widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.gumtree.gumnix.sics.batch.ui.util.SicsBatchUIUtils;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.LoopRunner;

import au.gov.ansto.bragg.nbi.ui.realtime.RealtimeDataViewer;
import au.gov.ansto.bragg.nbi.ui.realtime.SicsRealtimeRourceProvider;

/**
 * @author nxi
 *
 */
public class SicsRealtimeDataViewer extends Composite {

	private static final String SICS_REALTIME_MONITOR_PROPERTY_NAME = "sics.realtime.monitor.devices";
	private RealtimeDataViewer viewer;

	public SicsRealtimeDataViewer(Composite parent, int style) {
		super(parent, style);
		viewer = new RealtimeDataViewer(this, SWT.NONE);
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
						if (SicsCore.getDefaultProxy() != null && SicsCore.getDefaultProxy().isConnected() && SicsCore.getSicsController() != null){
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
							}
							Display.getDefault().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									SicsRealtimeRourceProvider provider = new SicsRealtimeRourceProvider();
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
