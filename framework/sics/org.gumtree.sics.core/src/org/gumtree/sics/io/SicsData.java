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

package org.gumtree.sics.io;

import org.gumtree.sics.io.SicsCommunicationConstants.JSONTag;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

public class SicsData implements ISicsData {

	private static final Logger logger = LoggerFactory.getLogger(SicsData.class);
	
	private JSONObject data;

	public SicsData(JSONObject data) {
		this.data = data;
	}

	public String getString() {
		try {
			return data.getString(JSONTag.DATA.getText());
		} catch (JSONException e) {
		}
		return null;
	}

	public Boolean getBoolean() {
		try {
			return data.getBoolean(JSONTag.DATA.getText());
		} catch (JSONException e) {
		}
		return null;
	}

	public Integer getInteger() {
		try {
			return data.getInt(JSONTag.DATA.getText());
		} catch (JSONException e) {
		}
		return null;
	}

	public Double getDouble() {
		try {
			return data.getDouble(JSONTag.DATA.getText());
		} catch (JSONException e) {
		}
		return null;
	}

	public Long getLong() {
		try {
			return data.getLong(JSONTag.DATA.getText());
		} catch (JSONException e) {
		}
		return null;
	}

	public JSONArray getArray() {
		try {
			return data.getJSONArray(JSONTag.DATA.getText());
		} catch (JSONException e) {
		}
		return null;
	}

	public JSONObject getJSONObject() {
		try {
			return data.getJSONObject(JSONTag.DATA.getText());
		} catch (JSONException e) {
			// logger.error("Cannot get data from json object", e);
		}
		return null;
	}

	public JSONObject getOriginal() {
		return data;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof ISicsData) {
			return Objects.equal(getOriginal(),
					((ISicsData) object).getOriginal());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getOriginal());
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("data", getOriginal())
				.toString();
	}

	public static ISicsData wrapData(Object data) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(JSONTag.DATA.getText(), data);
		} catch (JSONException e) {
			logger.error("Failed to create SICS data for " + data, e);
		}
		return new SicsData(jsonObject);
	}
	
}
