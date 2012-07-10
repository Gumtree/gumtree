/*****************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *****************************************************************************/

package org.gumtree.ui.util.swt;

import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;

public class FloatTextVerifyListener implements VerifyListener {
	
	public void verifyText(VerifyEvent e) {
		String string = e.text;
		char[] chars = new char[string.length()];
		string.getChars(0, chars.length, chars, 0);
	    for (char c : chars) {
	    	if (!('0' <= c && c <= '9') && !(c == '.')) {
	    		e.doit = false;
	    		return;
	    	}
	    }
	}

}
