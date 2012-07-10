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

package org.gumtree.service.dataaccess.support;

import java.io.Serializable;

public class DataAccessProviderInfo implements Serializable {

	private static final long serialVersionUID = 1592984306264590647L;

	private String scheme;
	
	private String providerName;
	
	public DataAccessProviderInfo(String scheme, String providerName) {
		this.scheme = scheme;
		this.providerName = providerName;
	}
	
	public String getScheme() {
		return scheme;
	}
	
	public String getProviderName() {
		return providerName;
	}
	
}
