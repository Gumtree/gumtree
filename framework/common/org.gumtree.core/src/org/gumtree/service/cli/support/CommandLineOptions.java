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

package org.gumtree.service.cli.support;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.gumtree.service.cli.ICommandLineOptions;

/**
 * Default implementation of command line options.
 * 
 * @author Tony Lam
 * @since 1.0
 * 
 */
public class CommandLineOptions implements ICommandLineOptions {

	private Map<String, String> options;

	public CommandLineOptions() {
		// use the Eclipse platform argument list as default
		this(Platform.getApplicationArgs());
	}

	public CommandLineOptions(String[] args) {
		options = new HashMap<String, String>();
		processArguments(args);
	}

	private void processArguments(String[] args) {
		String previousOption = null;
		for (String arg : args) {
			// beginning of the parsing
			if (previousOption == null) {
				if (arg.startsWith("-") && arg.length() > 1) {
					// found first option
					previousOption = arg.substring(1);
				} else {
					// illegal option
					continue;
				}
			} else {
				if (arg.startsWith("-") && arg.length() > 1) {
					// found option but not argument
					options.put(previousOption, null);
					previousOption = arg.substring(1);
				} else if (arg.startsWith("-") && arg.length() == 1) {
					// found illegal option argument
					options.put(previousOption, null);
					previousOption = null;
				} else {
					// found legal option argument
					options.put(previousOption, arg);
					previousOption = null;
				}
			}
		}
		// stores final option with no argument
		if (previousOption != null) {
			options.put(previousOption, null);
			previousOption = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.gumtree.core.ICommandLineOptions#getOptionValue(java.lang.String)
	 */
	public String getOptionValue(String optionId) {
		return options.get(optionId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gumtree.core.ICommandLineOptions#hasOption(java.lang.String)
	 */
	public boolean hasOption(String optionId) {
		return options.containsKey(optionId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.gumtree.core.ICommandLineOptions#hasOptionValue(java.lang.String)
	 */
	public boolean hasOptionValue(String optionId) {
		return (getOptionValue(optionId) != null);
	}

}
