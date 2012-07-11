/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
*******************************************************************************/
package au.gov.ansto.bragg.wombat.ui.views;

import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.terminal.CommunicationAdapterException;
import org.gumtree.ui.terminal.support.CommandLineTerminal;

import au.gov.ansto.bragg.wombat.ui.internal.ExperimentPerspective;

/**
 * @author nxi
 * Created on 19/02/2009
 */
public class SicsTerminalView extends CommandLineTerminal {

	/**
	 * 
	 */
	public SicsTerminalView() {
		// TODO Auto-generated constructor stub
		super();
	}

	/* (non-Javadoc)
	 * @see org.gumtree.ui.internal.terminal.CommandLineTerminal#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
		super.createPartControl(parent);
		try {
			selectCommunicationAdapter(ExperimentPerspective.SICS_TELNET_ADAPTOR_ID);
			connect();
		} catch (CommunicationAdapterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
