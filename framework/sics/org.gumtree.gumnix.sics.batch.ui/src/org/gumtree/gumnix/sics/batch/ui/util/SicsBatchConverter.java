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

package org.gumtree.gumnix.sics.batch.ui.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.gumtree.gumnix.sics.batch.ui.commands.SicsVariableCommand;
import org.gumtree.gumnix.sics.batch.ui.model.ISicsBatchScript;

public class SicsBatchConverter {

	private static Map<String, Class<?>> keywordLookup;
	
	static {
		keywordLookup = new HashMap<String, Class<?>>();
		// Sics variable
		keywordLookup.put("email", SicsVariableCommand.class);
		keywordLookup.put("phone", SicsVariableCommand.class);
		keywordLookup.put("sampledescription", SicsVariableCommand.class);
		keywordLookup.put("samplename", SicsVariableCommand.class);
		keywordLookup.put("sampletitle", SicsVariableCommand.class);
		keywordLookup.put("title", SicsVariableCommand.class);
		keywordLookup.put("user", SicsVariableCommand.class);
		// 
	}
	
	public static ISicsBatchScript convertToBatchScript(Reader reader) {
		BufferedReader bufferedReader = new BufferedReader(reader);
		String line = null;
		try {
			while ((line = bufferedReader.readLine()) != null) {
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private SicsBatchConverter() {
		super();
	}
	
}
