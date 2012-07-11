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

package au.gov.ansto.bragg.wombat.ui.internal.status;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.gumnix.sics.ui.ISicsStatusContent;

public class WombatStatusContent implements ISicsStatusContent {


	public void createContentControl(Composite parent) {
		parent.setLayout(new FillLayout());
		Browser browser = new Browser(parent, SWT.NONE);
//		browser.getBrowser().setUrl("D:/dev/workspace/gumtree/plugins/au.gov.ansto.bragg.nbi.status.web.client/www/au.gov.ansto.bragg.nbi.status.web.StatusViewer/StatusViewer.html");
		// Should be changed to the NBI status page in the future
		browser.setUrl("http://www.ansto.gov.au/bragg");
	}

}
