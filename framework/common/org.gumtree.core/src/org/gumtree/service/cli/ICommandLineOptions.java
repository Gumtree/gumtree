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

package org.gumtree.service.cli;
/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

import org.gumtree.core.service.IService;

/**
 * ICommandLineOptions stores a list of command line arguments set
 * by the user.  Arguments are retrieved from the Eclipse platform,
 * and further processed by ICommandLineOptions.
 *
 * @since 1.0
 */
public interface ICommandLineOptions extends IService {

	/**
	 * Returns if the specified option exists.
	 *
	 * @param optionId option string
	 * @return true if option exists; false otherwise
	 */
	public boolean hasOption(String optionId);

	/**
	 * Returns value associated with the specified option.
	 * If value does not exist for the given option or if the
	 * option is missing, it returns null.
	 *
	 * @param optionId option string
	 * @return a single string value for the specified option
	 */
	public String getOptionValue(String optionId);

	/**
	 * Tests if the specified value has value associate with it.
	 *
	 * @param optionId option string
	 * @return true if option has a specified value; false otherwise
	 */
	public boolean hasOptionValue(String optionId);

}
