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

package org.gumtree.control.core;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @since 1.0
 */
public interface ISicsReplyData {

	/**
	 * Returns replied data in string form.
	 *
	 * @return string data from sics proxy callback
	 */
	public String getString();

	/**
	 * Returns replied data in boolean form.
	 *
	 * @return boolean data from sics proxy callback
	 */
	public Boolean getBoolean();

	/**
	 * Returns replied data in boolean form.
	 *
	 * @return boolean data from sics proxy callback
	 */
	public Integer getInteger();

	/**
	 * Returns replied data in double form.
	 *
	 * @return double data from sics proxy callback
	 */
	public Double getDouble();

	/**
	 * Returns replied data in long form.
	 *
	 * @return long data from sics proxy callback
	 */
	public Long getLong();

	/**
	 * Returns replied data in JSON array form.
	 *
	 * @return JSON array data from sics proxy callback
	 */
	public JSONArray getArray();

	/**
	 * Returns replied data in JSON object form.
	 *
	 * @return JSON object data from sics proxy callback
	 */
	public JSONObject getJSONObject();

	/**
	 * Returns replied data in Java object form.
	 *
	 * @return Java object data from sics proxy callback
	 */
//	public Object getObject();

	/**
	 * @return
	 */
	public JSONObject getFullReply();

}
