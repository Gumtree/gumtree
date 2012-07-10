/*******************************************************************************
 * Copyright (c) 2012 Australian Nuclear Science and Technology Organisation,
 * Synchrotron SOLEIL and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     Norman XIONG (Bragg Institute) - initial API and implementation
 *     Clément RODRIGUEZ (SOLEIL) - initial API and implementation
 *     Tony LAM (Bragg Institute) - implementation
 ******************************************************************************/

package org.gumtree.data.utils;

import java.util.Map;

/**
 * @brief The IFactorManager register all plug-ins from class path.
 * 
 * This interface define how a plug-in once found can be instantiated using the factory manager.
 */

import org.gumtree.data.IFactory;

public interface IFactoryManager {

	public void registerFactory(String name, IFactory factory);
	
	public IFactory getFactory();
	
	public IFactory getFactory(String name);
	
	public Map<String, IFactory> getFactoryRegistry();
	
}
