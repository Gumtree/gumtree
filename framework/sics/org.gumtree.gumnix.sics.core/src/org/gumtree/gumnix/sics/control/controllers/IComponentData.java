package org.gumtree.gumnix.sics.control.controllers;

import ch.psi.sics.hipadaba.DataType;

public interface IComponentData {

	public DataType getDataType();

	public String getStringData();

	public int getIntData() throws ComponentDataFormatException;

	public float getFloatData() throws ComponentDataFormatException;

	public int[] getIntArrayData() throws ComponentDataFormatException;

	public float[] getFloatArrayData() throws ComponentDataFormatException;

	public String getSicsString();

}
