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

package au.gov.ansto.bragg.platypus.ui.widgets;

import org.eclipse.swt.widgets.Composite;

import au.gov.ansto.bragg.nbi.ui.widgets.HMImageDisplayWidget;
import au.gov.ansto.bragg.nbi.ui.widgets.HMImageMode;

public class PlatypusHMImageDisplayWidget extends HMImageDisplayWidget {

	private HMImageMode imageMode;
	
	public PlatypusHMImageDisplayWidget(Composite parent, int style) {
		super(parent, style);
	}

	protected void widgetDispose() {
		imageMode = null;
		super.widgetDispose();
	}
	
	public HMImageMode getImageMode() {
		if (imageMode == null) {
			setImageMode(PlatypusHMImageMode.values()[0]);
		}
		return imageMode;
	}
	
	public void setImageMode(HMImageMode imageMode) {
		this.imageMode = imageMode;
	}
	
}
