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

package org.gumtree.scripting.support;

import java.net.URI;
import java.util.Map;

import javax.script.ScriptEngine;

import org.gumtree.core.service.ServiceUtils;
import org.gumtree.scripting.IScriptingManager;
import org.gumtree.service.dataaccess.DataAccessException;
import org.gumtree.service.dataaccess.InvalidResourceException;
import org.gumtree.service.dataaccess.RepresentationNotSupportedException;
import org.gumtree.service.dataaccess.providers.AbstractDataProvider;

public class ScriptDataProvider extends AbstractDataProvider<Object> {

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(URI uri, Class<T> representation,
			Map<String, Object> properties) throws DataAccessException {
		String engineName = uri.getHost();
		// Remove "/"
		String script = "result = " + uri.getPath().substring(1);
		// TODO: This creates engine on every call...can we make this more efficient?
		ScriptEngine engine = ServiceUtils.getService(IScriptingManager.class).createEngine(engineName);
		try {
			if (engine != null) {
				engine.eval(script);
				Object result = engine.get("result");
				if (representation.isAssignableFrom(result.getClass())) {
					return (T) result;
				} else {
					throw new RepresentationNotSupportedException();
				}
			} else {
				throw new InvalidResourceException("Engine " + engineName + " is unavailable.");
			}
		} catch (Exception e) {
			throw new InvalidResourceException("Failed to execute script: " + script, e);
		}
	}

}
