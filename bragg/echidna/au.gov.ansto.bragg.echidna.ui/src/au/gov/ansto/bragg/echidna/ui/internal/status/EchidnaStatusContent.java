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

package au.gov.ansto.bragg.echidna.ui.internal.status;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.pgroup.PGroup;
import org.eclipse.nebula.widgets.pgroup.RectangleGroupStrategy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.gumtree.gumnix.sics.core.IInstrumentProfile;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.ui.ISicsStatusContent;

import au.gov.ansto.bragg.echidna.ui.EchidnaUIConstants;
import au.gov.ansto.bragg.echidna.ui.internal.Activator;

@Deprecated
public class EchidnaStatusContent implements ISicsStatusContent {

	private static Image IMAGE_MONOCHROMATOR;

	private static Image IMAGE_SLITS;

	static {
		if(Activator.getDefault() != null) {
			IMAGE_MONOCHROMATOR = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/wizban/meterchartsuperimposedimage.gif").createImage();
			IMAGE_SLITS = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/wizban/piechartwithdepthimage.gif").createImage();
		}
	}

	public void createContentControl(Composite parent) {
		parent.setLayout(new GridLayout(2, true));

		PGroup monoGroup = new PGroup(parent, SWT.SMOOTH);
		monoGroup.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		monoGroup.setStrategy(new RectangleGroupStrategy());
		monoGroup.setToggleRenderer(null);
		monoGroup.setImage(IMAGE_MONOCHROMATOR);
		monoGroup.setImagePosition(SWT.TOP | SWT.LEFT);
		monoGroup.setText("Monochromator");
		monoGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		PGroup slitGroup = new PGroup(parent, SWT.SMOOTH);
		slitGroup.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		slitGroup.setStrategy(new RectangleGroupStrategy());
		slitGroup.setToggleRenderer(null);
		slitGroup.setImage(IMAGE_SLITS);
		slitGroup.setImagePosition(SWT.TOP | SWT.LEFT);
		slitGroup.setText("Slit System");
		slitGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

//		PGroup hmGroup = new PGroup(parent, SWT.SMOOTH);
//		hmGroup.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
//		hmGroup.setStrategy(new RectangleGroupStrategy());
//		hmGroup.setToggleRenderer(null);
//		hmGroup.setText("Histogram");
//		hmGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//		hmGroup.setLayout(new FillLayout());
//		Browser hmBrowser = new Browser(hmGroup, SWT.BORDER);

		PGroup plcGroup = new PGroup(parent, SWT.SMOOTH);
		plcGroup.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		plcGroup.setStrategy(new RectangleGroupStrategy());
		plcGroup.setToggleRenderer(null);
		plcGroup.setText("PLC System");
		plcGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		plcGroup.setLayout(new FillLayout());
		Browser pclBrowser = new Browser(plcGroup, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).span(2, 1).applyTo(plcGroup);

//		IInstrumentProfile profile = SicsCore.getSicsManager().service().getCurrentInstrumentProfile();
//		if(profile != null) {
////			String hmURL = profile.getProperty(EchidnaUIConstants.PROP_HISTOGRAM_URL);
////			if(hmURL != null) {
////				hmBrowser.setUrl(profile.getProperty(EchidnaUIConstants.PROP_HISTOGRAM_URL));
////			}
//			String pclURL = profile.getProperty(EchidnaUIConstants.PROP_PLC_URL);
//			if(pclURL != null) {
//				pclBrowser.setUrl(pclURL);
//			}
//		}

	}

}
