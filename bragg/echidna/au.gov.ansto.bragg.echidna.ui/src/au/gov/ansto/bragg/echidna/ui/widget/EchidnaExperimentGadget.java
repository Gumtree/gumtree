/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Norman Xiong (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package au.gov.ansto.bragg.echidna.ui.widget;

import java.net.URI;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.gumnix.sics.ui.widgets.DeviceStatusGadget;

public class EchidnaExperimentGadget extends DeviceStatusGadget {

	private final static URI titleURI = URI.create("sics://hdb/experiment/title");
	private final static URI sampleNameURI = URI.create("sics://hdb/sample/name");
	private final static URI userNameURI = URI.create("sics://hdb/user/name");
	
	public EchidnaExperimentGadget(Composite parent, int style) {
		super(parent, SHOW_UNIT);
		setDeviceURIs(titleURI + ","
				 + sampleNameURI + ","
				 + userNameURI);
		setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				URI uri = (URI) element;
				if (uri.equals(titleURI)) {
					return "Proposal";
				} else if (uri.equals(sampleNameURI)) {
					return "Sample";
				} else if (uri.equals(userNameURI)) {
					return "User";
				} 
				return "";
			}
		});
	}

}
