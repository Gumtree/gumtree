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

package org.gumtree.sics.util;

import org.gumtree.security.EncryptionUtils;
import org.gumtree.sics.io.ISicsConnectionContext;
import org.gumtree.sics.io.SicsConnectionContext;
import org.gumtree.sics.io.SicsRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SicsIOUtils {

	private static final Logger logger = LoggerFactory.getLogger(SicsIOUtils.class);
	
	public static ISicsConnectionContext createContextFromSystemProperties() {
		// Decode password if it is encrypted
		String password = null;
		if (SicsCoreProperties.PASSWORD_ENCRYPTED.getBoolean()) {
			try {
				password = EncryptionUtils.decryptBase64(SicsCoreProperties.PASSWORD.getValue());
			} catch (Exception e) {
				logger.error("Failed to decrypt SICS password from system properties", e);
			}
		}
		return new SicsConnectionContext(
				SicsCoreProperties.SERVER_HOST.getValue(),
				SicsCoreProperties.SERVER_PORT.getInt(),
				SicsRole.getRole(SicsCoreProperties.ROLE.getValue()),
				password == null ? SicsCoreProperties.PASSWORD.getValue() : password);
	}

	private SicsIOUtils() {
		super();
	}

}
