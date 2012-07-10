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
package org.gumtree.data.nexus.ui.viewers;

import java.util.List;

import javax.swing.UIManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.gumtree.data.interfaces.IContainer;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.nexus.IAxis;
import org.gumtree.data.nexus.INXDataset;
import org.gumtree.data.nexus.INXdata;
import org.gumtree.data.nexus.INXentry;
import org.gumtree.data.nexus.ISignal;
import org.gumtree.data.nexus.IVariance;
import org.gumtree.data.ui.viewers.DataItemViewer;
import org.gumtree.data.ui.viewers.DatasetBrowser;
import org.gumtree.data.ui.viewers.DatasetViewer;

/**
 * @author nxi
 *
 */
public class NexusViewer extends DatasetViewer {

	public static Display newDisplay;
	static NexusViewer instance;
	/**
	 * @param parent
	 * @param style
	 */
	public NexusViewer(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	protected DatasetBrowser createDatasetBrowser(Composite parent) {
		NexusBrowser datasetBrowser = new NexusBrowser(parent,
				SWT.BORDER);
		return datasetBrowser;
	}
	
	@Override
	protected void performSelectionChanged(Object selection) {
		INXdata data = null;
		IContainer attributeFocus = null;
		if (selection instanceof INXDataset) {
			data = ((INXDataset) selection).getNXroot().getDefaultData();
			attributeFocus = data.getRootGroup();
		} else if (selection instanceof INXentry) {
			data = ((INXentry) selection).getData();
			attributeFocus = data.getRootGroup();
		} else if (selection instanceof INXdata) {
			data = (INXdata) selection;
			attributeFocus = data;
		} else if (selection instanceof ISignal) {
			IGroup group = ((ISignal) selection).getParentGroup();
			if (group instanceof INXdata) {
				data = (INXdata) group;
			}
			attributeFocus = (ISignal) selection;
		}
		if (data != null) {
			ISignal singal = data.getSignal();
			getDataItemViewer().setDataItem(singal);
			List<IAxis> axes = data.getAxisList();
			((NexusItemViewer) getDataItemViewer()).setAxes(axes);
			IVariance variance = data.getVariance();
			((NexusItemViewer) getDataItemViewer()).setVarianceItem(variance);
			getAttributeViewer().setContainer(attributeFocus);
			getDictionaryViewer().setDataset(attributeFocus.getDataset());
		} else {
			((NexusItemViewer) getDataItemViewer()).setAxes(null);
			super.performSelectionChanged(selection);
		}
	}
	
	public static NexusViewer openInNewShell() {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(
					        UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}
				Display display = getNewDisplay();
				Shell shell = new Shell(display);
				shell.setText("Nexus Data Browser");
				shell.setLayout(new FillLayout());
				shell.setSize(800, 640);

				// Form version
//				FormComposite formComposite = new FormComposite(shell, SWT.NONE);		
//				formComposite.setLayout(new FillLayout());
//				new DatasetViewer(formComposite, SWT.NONE);

				// SWT version
				instance = new NexusViewer(shell, SWT.NONE);
				
				shell.open();
				display.sleep();
//				try {
//					Thread.sleep(200);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				while (!shell.isDisposed()) {
					if (!display.readAndDispatch())
						display.sleep();
				}
				display.dispose();

			}
		});
		thread.start();
		int sleepTime = 0;
		while (instance == null && sleepTime < 5000) {
			try {
				Thread.sleep(200);
				sleepTime += 200;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		NexusViewer ins = instance;
		instance = null;
		return ins;
	}
	
	public static Display getNewDisplay() {
		if (newDisplay == null) {
			newDisplay = new Display();
		}
		return newDisplay;
	}
	
	public String getTitle() {
		return "data browser";
	}
	
	protected DataItemViewer createDataItemViewer(Composite parent) {
		return new NexusItemViewer(parent, SWT.NONE);
	}

}
