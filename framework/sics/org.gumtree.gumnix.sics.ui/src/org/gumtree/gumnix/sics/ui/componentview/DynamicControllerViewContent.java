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

package org.gumtree.gumnix.sics.ui.componentview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.pshelf.PShelf;
import org.eclipse.nebula.widgets.pshelf.PShelfItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.tools.jconsole.Plotter;

public class DynamicControllerViewContent implements IComponentViewContent {

	private static Logger logger = LoggerFactory.getLogger(DynamicControllerViewContent.class);
	
	private FormToolkit toolkit;

	private IDynamicController controller;
	
	private IDynamicController upperlimit;
	
	private IDynamicController lowerlimit;
	
	private Plotter plotter;
	
	private volatile Thread blinker;

	public void createPartControl(Composite parent, IComponentController controller) {
		Assert.isTrue(controller instanceof IDynamicController);
		this.controller = (IDynamicController)controller;
		toolkit = new FormToolkit(parent.getDisplay());
		parent.setLayout(new FillLayout());
		PShelf shelf = new PShelf(parent, SWT.BORDER);
//		shelf.setRenderer(new RedmondShelfRenderer());
		PShelfItem itemInfo = new PShelfItem(shelf,SWT.NONE);
		itemInfo.setText("Component Info");
		itemInfo.getBody().setLayout(new FillLayout());
		createInfoSection(itemInfo.getBody());

		PShelfItem itemMonitor = new PShelfItem(shelf,SWT.NONE);
		itemMonitor.setText("Device Property Monitor");
			itemMonitor.getBody().setLayout(new FillLayout());
		getToolkit().adapt(itemMonitor.getBody());
		createMonitorSection(itemMonitor.getBody());

		shelf.setSelection(itemMonitor);
	}

	private void createInfoSection(Composite parent) {
		ScrolledForm form = getToolkit().createScrolledForm(parent);
		form.getBody().setLayout(new GridLayout());
		getToolkit().createLabel(form.getBody(), "Component Id: " + getController().getComponent().getId());
		getToolkit().createLabel(form.getBody(), "Component Path: " +  getController().getPath());
	}

	private void createMonitorSection(Composite parent) {
		parent.setLayout(new GridLayout(2, false));
		
		Label label = getToolkit().createLabel(parent, "Range");
		GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).grab(true, false).applyTo(label);
		
		final Combo combo = new Combo(parent, SWT.READ_ONLY);
		combo.setItems(Plotter.rangeNames);
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (plotter != null) {
					plotter.setViewRange(Plotter.rangeValues[combo.getSelectionIndex()]);
				}
			}
		});
		// Select the last one as default
		combo.select(Plotter.rangeNames.length - 1);
		getToolkit().adapt(combo);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).grab(true, false).applyTo(combo);
		
		Composite embeddedComposite = getToolkit().createComposite(parent, SWT.EMBEDDED);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).span(2, 1).applyTo(embeddedComposite);
		Frame frame = SWT_AWT.new_Frame(embeddedComposite);
		frame.setLayout(new BorderLayout());
		plotter = new Plotter();
		frame.add("Center", plotter);
		// Use the last range as default
		plotter.setViewRange(Plotter.rangeValues[Plotter.rangeValues.length - 1]);		
		plotter.createSequence("value", "Value", Color.BLUE, true);
		
		try {
			upperlimit = (IDynamicController) controller.getChildController("/softupperlim");
			lowerlimit = (IDynamicController) controller.getChildController("/softlowerlim");
		} catch (Exception e) {
			logger.error("Failed to retrieve limits.", e);
		}
		
		if (upperlimit != null && lowerlimit != null) {
			plotter.createSequence("upper", "Upper Limit", Color.RED, true);
			plotter.createSequence("lower", "Lower Limit", Color.RED, true);
		}
		plotter.setDecimals(3);

		// Use dashboard MVC instead??
		Thread monitoringThread = new Thread(new Runnable() {
			public void run() {
				Thread thisThread = Thread.currentThread();
				while (blinker == thisThread) {
					try {
						if (plotter != null && controller != null) {
							long value = Math.round(controller.getValue().getFloatData() * 1000);
							if (upperlimit != null && lowerlimit != null) {
								long upperlim = Math.round(upperlimit.getValue().getFloatData() * 1000);
								long lowerlim = Math.round(lowerlimit.getValue().getFloatData() * 1000);
								plotter.addValues(System.currentTimeMillis(), value, upperlim, lowerlim);
							} else {
								plotter.addValues(System.currentTimeMillis(), value);
							}
						}
						Thread.sleep(3000);
					} catch (Exception e) {
						logger.error("Error occured when updating plotter.", e);
						// stop
						blinker = null;	
					}
				}
			}			
		});
		blinker = monitoringThread;
		monitoringThread.start();
	}

	public void dispose() {
		plotter = null;
		controller = null;
		blinker = null;
	}

	public IDynamicController getController() {
		return controller;
	}

	private FormToolkit getToolkit() {
		return toolkit;
	}

}
