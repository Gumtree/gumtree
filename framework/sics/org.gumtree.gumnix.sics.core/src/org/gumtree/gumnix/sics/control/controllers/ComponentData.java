package org.gumtree.gumnix.sics.control.controllers;

import org.eclipse.core.runtime.Assert;
import org.json.JSONArray;
import org.json.JSONException;

import ch.psi.sics.hipadaba.DataType;

public class ComponentData implements IComponentData {

	private String sicsString;

	private DataType dataType;

	public ComponentData(String sicsString, DataType dataType) {
		Assert.isNotNull(sicsString);
		Assert.isNotNull(dataType);
		this.sicsString = sicsString.trim();
		this.dataType = dataType;
	}
	
	public ComponentData(IComponentData data) {
		this.sicsString = data.getSicsString();
		this.dataType = data.getDataType();
	}
	
	public String getStringData() {
		return sicsString;
	}

	public DataType getDataType() {
		return dataType;
	}

	public float getFloatData() throws ComponentDataFormatException {
		try {
			return Float.parseFloat(sicsString);
		} catch (NumberFormatException e) {
			throw new ComponentDataFormatException("Cannot return float value", e);
		}
	}

	public int getIntData() throws ComponentDataFormatException {
		// Assume string is an int and we try to convert
		Integer result = null;
		try {
			 result = Integer.valueOf(sicsString);
		} catch (NumberFormatException e) {
		}
		// Try again if data is a float string
		if (result == null) {
			try {
				 result = Math.round(Float.valueOf(sicsString));
			} catch (NumberFormatException e) {
			}	
		}
		if (result == null) {
			throw new ComponentDataFormatException("Cannot return int value");
		}
		return result.intValue();
	}

	public int[] getIntArrayData() throws ComponentDataFormatException {
		if(!(getDataType().equals(DataType.INTAR_LITERAL) || getDataType().equals(DataType.INTVARAR_LITERAL))) {
			throw new ComponentDataFormatException("Cannot retrieve int array data for type " + getDataType().getName());
		}
		try {
			JSONArray array = new JSONArray(getSicsString());
			int[] intArray = new int[array.length()];
			for(int i = 0; i < intArray.length; i++) {
				intArray[i] = array.getInt(i);
			}
			return intArray;
		} catch (JSONException e) {
			throw new ComponentDataFormatException("Cannot retrieve int array data", e);
		}
	}

	public float[] getFloatArrayData() throws ComponentDataFormatException {
		if(!(getDataType().equals(DataType.FLOATAR_LITERAL) || getDataType().equals(DataType.FLOATVARAR_LITERAL))) {
			throw new ComponentDataFormatException("Cannot retrieve float array data for type " + getDataType().getName());
		}
		try {
			JSONArray array = new JSONArray(getSicsString());
			float[] floatArray = new float[array.length()];
			for(int i = 0; i < floatArray.length; i++) {
				floatArray[i] = (float)array.getDouble(i);
			}
			return floatArray;
		} catch (JSONException e) {
			throw new ComponentDataFormatException("Cannot retrieve float array data", e);
		}
	}

	public String getSicsString() {
		return sicsString;
	}

	public String toString() {
		return "[" + getDataType() + "]" + getSicsString();
	}

	public boolean equals(Object obj) {
		if(obj instanceof IComponentData) {
			return ((IComponentData)obj).getDataType().equals(getDataType()) && ((IComponentData)obj).getSicsString().equals(getSicsString());
		}
		return false;
	}

	public static IComponentData createStringData(String data) {
		return new ComponentData(data, DataType.TEXT_LITERAL);
	}

	public static IComponentData createIntData(int data) {
		return new ComponentData(Integer.toString(data), DataType.INT_LITERAL);
	}

	// [Tony] We can't fit long into int, so we treat it as float 
	public static IComponentData createLongData(long data) {
		return new ComponentData(Long.toString(data), DataType.FLOAT_LITERAL);
	}

	public static IComponentData createFloatData(float data) {
		return new ComponentData(Float.toString(data), DataType.FLOAT_LITERAL);
	}
	
	public static IComponentData createEmptyData() {
		return new ComponentData("", DataType.NONE_LITERAL);
	}
	
	public static IComponentData createData(String data) {
		return createStringData(data);
	}

	public static IComponentData createData(int data) {
		return createIntData(data);
	}

	public static IComponentData createData(float data) {
		return createFloatData(data);
	}
	
	public static IComponentData createData(long data) {
		return createLongData(data);
	}
	
}
