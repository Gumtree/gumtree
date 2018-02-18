package org.gumtree.control.core;

import org.json.JSONObject;

public interface ISicsCallback {

	/**
	 * This method is called from client receives output with tag "OUT" and
	 * "STATUS".
	 *
	 * @param response
	 *            output from sics in json format
	 */
	public void receiveReply(JSONObject data);

	/**
	 * This method is called from client receives output with tag "ERROR".
	 *
	 * @param response
	 *            output from sics in json format
	 */
	public void receiveError(JSONObject data);

	/**
	 * This method is called from client receives output with tag "FINISH".
	 *
	 * @param response
	 *            output from sics in json format
	 */
	public void receiveFinish(JSONObject data);

	/**
	 * Returns whether this callback has encountered any error callback.
	 *
	 * @return true if callback received error
	 */
	public boolean hasError();

	/**
	 * If receiveError() is called, class which implements this interface should call
	 * this method to indicate error.
	 *
	 * @param error true for error; false otherwise
	 */
	public void setError(boolean error);

}
