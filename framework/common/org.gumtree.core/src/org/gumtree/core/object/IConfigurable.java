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

package org.gumtree.core.object;

import org.gumtree.util.collection.IParameters;

/**
 * IConfigurable is a marker interface for object which may be managed by object
 * life cycle. For example, it needs managed way to set parameters, and
 * activation when parameters are set.
 * 
 * @author Tony Lam
 * @since 1.4
 * 
 */
public interface IConfigurable {

	public void afterParametersSet();

	public IParameters getParameters();

	public void setParameters(IParameters parameters);

}
