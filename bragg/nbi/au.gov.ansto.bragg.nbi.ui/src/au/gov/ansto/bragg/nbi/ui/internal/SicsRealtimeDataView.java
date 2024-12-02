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
package au.gov.ansto.bragg.nbi.ui.internal;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import au.gov.ansto.bragg.nbi.core.NBISystemProperties;
import au.gov.ansto.bragg.nbi.ui.realtime.control.ControlRealtimeDataViewer;
import au.gov.ansto.bragg.nbi.ui.widgets.SicsRealtimeDataViewer;

/**
 * @author nxi
 *
 */
public class SicsRealtimeDataView extends ViewPart {

	private Composite viewer;
	/**
	 * 
	 */
	public SicsRealtimeDataView() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		if (NBISystemProperties.USE_NEW_PROXY) {
			viewer = new ControlRealtimeDataViewer(parent, SWT.NONE);
		} else {
			viewer = new SicsRealtimeDataViewer(parent, SWT.NONE);
		}
		GridLayoutFactory.fillDefaults().applyTo(viewer);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer);
//		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		if (viewer != null && !viewer.isDisposed()) {
			try {
				viewer.dispose();
			} catch (Exception e) {
				e.printStackTrace();
			}
			viewer = null;
		}
		super.dispose();
	}

}
