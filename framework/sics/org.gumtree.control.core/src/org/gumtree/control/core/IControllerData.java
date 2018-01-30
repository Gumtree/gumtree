package org.gumtree.control.core;

import org.gumtree.control.exception.SicsModelException;

import ch.psi.sics.hipadaba.DataType;

public interface IControllerData {

	public DataType getDataType();

	public String getStringData();

	public int getIntData() throws SicsModelException;

	public float getFloatData() throws SicsModelException;

	public int[] getIntArrayData() throws SicsModelException;

	public float[] getFloatArrayData() throws SicsModelException;

	public String getSicsString();

}
