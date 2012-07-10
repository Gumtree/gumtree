package org.gumtree.gumnix.sics.control.controllers;

import org.gumtree.gumnix.sics.control.events.IDynamicControllerCallback;
import org.gumtree.gumnix.sics.io.SicsIOException;

import ch.psi.sics.hipadaba.DataType;

/**
 * @since 1.0
 */
public interface IDynamicController extends IComponentController {

	/**
	 * Blocking operation.
	 *
	 * @throws SicsIOException
	 */
	public IComponentData getValue() throws SicsIOException;

	public IComponentData getValue(boolean update) throws SicsIOException;

	/**
	 * Same as calling getValue(callback, false)
	 *
	 * @param callback
	 * @throws SicsIOException
	 */
	public void getValue(IDynamicControllerCallback callback) throws SicsIOException;

	public void getValue(IDynamicControllerCallback callback, boolean update) throws SicsIOException;

	public IComponentData getTargetValue() throws SicsIOException;

	public void getTargetValue(final IDynamicControllerCallback callback) throws SicsIOException;
	
	public void setTargetValue(IComponentData newValue);

//	public void setTargetValue(IComponentData newValue, IDynamicControllerCallback errorCallback) throws SicsIOException;

	/**
	 * Commits the current target value (if available) to SICS by setting the hipadaba object.
	 *
	 * @param errorCallback
	 * @throws SicsIOException
	 */
	public boolean commitTargetValue(IDynamicControllerCallback errorCallback) throws SicsIOException;

	public String getErrorMessage();
	
	
	/**
	 * Clear error message and reset the status to OK.
	 * This is used to acknowledge error and reset the controller to its normal state.
	 */
	public void clearError();
	
	/**
	 * Returns the hipadaba type.
	 *
	 * @return
	 * @deprecated
	 */
	public DataType getDataType();

	/**
	 * Returns the time that the controller has last updated its value by the server.
	 * It returns less then zero if value has not yet been updated.
	 * 
	 * @return time in a long value
	 */
	public long getLastValueUpdated();
	
	public boolean isEnabled();
	
}
