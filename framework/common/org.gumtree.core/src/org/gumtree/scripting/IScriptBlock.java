/*******************************************************************************
 * Copyright (c) 2012 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bragg Institute - initial API and implementation
 ******************************************************************************/

package org.gumtree.scripting;

import java.io.Reader;

public interface IScriptBlock {

	public boolean isSkip();
	
	public void setSkip(boolean skip);
	
	public void append(String script);
	
	// Used by the script engine
	public Reader getReader();
	
	// For script export
	public String getScript();
	
	// Called when script block has been successfully executed
	public void postProcess();
	
}
