package org.gumtree.control.model;

import org.eclipse.core.runtime.Assert;
import org.gumtree.control.core.IControllerData;
import org.gumtree.control.exception.SicsModelException;
import org.json.JSONArray;
import org.json.JSONException;

import ch.psi.sics.hipadaba.DataType;

public class ControllerData implements IControllerData {

	private String sicsString;

	private DataType dataType;

	public ControllerData(String sicsString, DataType dataType) {
		Assert.isNotNull(sicsString);
		Assert.isNotNull(dataType);
		this.sicsString = sicsString.trim();
		this.dataType = dataType;
	}
	
	public ControllerData(IControllerData data) {
		this.sicsString = data.getSicsString();
		this.dataType = data.getDataType();
	}
	
	public String getStringData() {
		return sicsString;
	}

	public DataType getDataType() {
		return dataType;
	}

	public float getFloatData() throws SicsModelException {
		try {
			return Float.parseFloat(sicsString);
		} catch (NumberFormatException e) {
			throw new SicsModelException("Cannot return float value", e);
		}
	}

	public int getIntData() throws SicsModelException {
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
			throw new SicsModelException("Cannot return int value");
		}
		return result.intValue();
	}

	public int[] getIntArrayData() throws SicsModelException {
		if(!(getDataType().equals(DataType.INTAR_LITERAL) || getDataType().equals(DataType.INTVARAR_LITERAL))) {
			throw new SicsModelException("Cannot retrieve int array data for type " + getDataType().getName());
		}
		try {
			JSONArray array = new JSONArray(getSicsString());
			int[] intArray = new int[array.length()];
			for(int i = 0; i < intArray.length; i++) {
				intArray[i] = array.getInt(i);
			}
			return intArray;
		} catch (JSONException e) {
			throw new SicsModelException("Cannot retrieve int array data", e);
		}
	}

	public float[] getFloatArrayData() throws SicsModelException {
		if(!(getDataType().equals(DataType.FLOATAR_LITERAL) || getDataType().equals(DataType.FLOATVARAR_LITERAL))) {
			throw new SicsModelException("Cannot retrieve float array data for type " + getDataType().getName());
		}
		try {
			JSONArray array = new JSONArray(getSicsString());
			float[] floatArray = new float[array.length()];
			for(int i = 0; i < floatArray.length; i++) {
				floatArray[i] = (float)array.getDouble(i);
			}
			return floatArray;
		} catch (JSONException e) {
			throw new SicsModelException("Cannot retrieve float array data", e);
		}
	}

	public String getSicsString() {
		return sicsString;
	}

	public String toString() {
		return "[" + getDataType() + "]" + getSicsString();
	}

	public boolean equals(Object obj) {
		if(obj instanceof IControllerData) {
			return ((IControllerData)obj).getDataType().equals(getDataType()) && ((IControllerData)obj).getSicsString().equals(getSicsString());
		}
		return false;
	}

	public static IControllerData createStringData(String data) {
		return new ControllerData(data, DataType.TEXT_LITERAL);
	}

	public static IControllerData createIntData(int data) {
		return new ControllerData(Integer.toString(data), DataType.INT_LITERAL);
	}

	// [Tony] We can't fit long into int, so we treat it as float 
	public static IControllerData createLongData(long data) {
		return new ControllerData(Long.toString(data), DataType.FLOAT_LITERAL);
	}

	public static IControllerData createFloatData(float data) {
		return new ControllerData(Float.toString(data), DataType.FLOAT_LITERAL);
	}
	
	public static IControllerData createEmptyData() {
		return new ControllerData("", DataType.NONE_LITERAL);
	}
	
	public static IControllerData createData(String data) {
		return createStringData(data);
	}

	public static IControllerData createData(int data) {
		return createIntData(data);
	}

	public static IControllerData createData(float data) {
		return createFloatData(data);
	}
	
	public static IControllerData createData(long data) {
		return createLongData(data);
	}
	
}
