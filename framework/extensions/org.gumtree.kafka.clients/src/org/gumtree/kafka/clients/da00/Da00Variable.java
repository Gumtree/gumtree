package org.gumtree.kafka.clients.da00;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * One named variable of a da00 data array, such as the signal or an axis. The
 * data is kept flat in row-major (C) order; use {@link #getShape()} for its
 * dimensions.
 * <p>
 * {@link #getData()} returns the natural Java array for the type: byte[],
 * short[], int[], long[], float[] or double[] for the signed and float types,
 * and String for c_string. Unsigned types are widened so the values stay
 * positive: uint8 to short[], uint16 to int[], uint32 to long[]. uint64 comes
 * back as long[] holding the same bits, so values of 2^63 or more appear
 * negative. {@link #getDataAsDouble()} converts any numeric type to double[].
 */
public final class Da00Variable {

	private static final Charset UTF8 = Charset.forName("UTF-8");

	// vtable offsets of the fields in table da00_Variable
	private static final int NAME = 4;
	private static final int UNIT = 6;
	private static final int LABEL = 8;
	private static final int SOURCE = 10;
	private static final int DATA_TYPE = 12;
	private static final int AXES = 14;
	private static final int SHAPE = 16;
	private static final int DATA = 18;

	private final String name;

	private final String unit;

	private final String label;

	private final String source;

	private final Da00DataType dataType;

	private final String[] axes;

	private final long[] shape;

	private final byte[] rawData;

	Da00Variable(FlatBufferTable table) {
		name = table.getString(NAME);
		unit = table.getString(UNIT);
		label = table.getString(LABEL);
		source = table.getString(SOURCE);
		dataType = Da00DataType.fromCode(table.getByte(DATA_TYPE, (byte) 0) & 0xFF);
		axes = table.getStringVector(AXES);
		shape = table.getLongVector(SHAPE);
		rawData = table.getByteVector(DATA);
	}

	public String getName() {
		return name;
	}

	/** @return the unit, or null if not given */
	public String getUnit() {
		return unit;
	}

	/** @return the label, or null if not given */
	public String getLabel() {
		return label;
	}

	/** @return the source, or null if not given */
	public String getSource() {
		return source;
	}

	public Da00DataType getDataType() {
		return dataType;
	}

	/** @return the names of the dimensions, outermost first; empty for a scalar */
	public String[] getAxes() {
		return axes.clone();
	}

	/** @return the size of each dimension, outermost first */
	public long[] getShape() {
		return shape.clone();
	}

	/** @return the shape as int[], convenient for Java and Jython arrays */
	public int[] getShapeAsInt() {
		int[] result = new int[shape.length];
		for (int i = 0; i < shape.length; i++) {
			result[i] = (int) shape[i];
		}
		return result;
	}

	/** @return the data bytes exactly as stored */
	public byte[] getRawData() {
		return rawData.clone();
	}

	/** @return the number of elements in the data */
	public int getSize() {
		if (dataType == Da00DataType.C_STRING) {
			return 1;
		}
		return dataType.getSize() == 0 ? 0 : rawData.length / dataType.getSize();
	}

	public boolean isString() {
		return dataType == Da00DataType.C_STRING;
	}

	/**
	 * @return true if this is a single value with no axes; the Python library
	 *         then returns a plain number instead of an array
	 */
	public boolean isScalar() {
		return axes.length == 0 && !isString() && getSize() == 1;
	}

	/** @return the data as a flat array of the natural type (see class notes) */
	public Object getData() {
		ByteBuffer buffer = ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN);
		int n = getSize();
		switch (dataType) {
		case INT8:
			return rawData.clone();
		case UINT8: {
			short[] values = new short[n];
			for (int i = 0; i < n; i++) {
				values[i] = (short) (rawData[i] & 0xFF);
			}
			return values;
		}
		case INT16: {
			short[] values = new short[n];
			buffer.asShortBuffer().get(values);
			return values;
		}
		case UINT16: {
			int[] values = new int[n];
			for (int i = 0; i < n; i++) {
				values[i] = buffer.getShort(i * 2) & 0xFFFF;
			}
			return values;
		}
		case INT32: {
			int[] values = new int[n];
			buffer.asIntBuffer().get(values);
			return values;
		}
		case UINT32: {
			long[] values = new long[n];
			for (int i = 0; i < n; i++) {
				values[i] = buffer.getInt(i * 4) & 0xFFFFFFFFL;
			}
			return values;
		}
		case INT64:
		case UINT64: {
			long[] values = new long[n];
			buffer.asLongBuffer().get(values);
			return values;
		}
		case FLOAT32: {
			float[] values = new float[n];
			buffer.asFloatBuffer().get(values);
			return values;
		}
		case FLOAT64: {
			double[] values = new double[n];
			buffer.asDoubleBuffer().get(values);
			return values;
		}
		case C_STRING:
			return new String(rawData, UTF8);
		default:
			return null;
		}
	}

	/**
	 * @return the numeric data converted to a flat double[] (uint64 values of 2^63
	 *         or more are converted correctly)
	 * @throws IllegalStateException for string or untyped data
	 */
	public double[] getDataAsDouble() {
		if (dataType == Da00DataType.C_STRING || dataType == Da00DataType.NONE) {
			throw new IllegalStateException("Variable " + name + " has no numeric data (" + dataType + ")");
		}
		ByteBuffer buffer = ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN);
		int n = getSize();
		double[] values = new double[n];
		for (int i = 0; i < n; i++) {
			values[i] = element(buffer, i);
		}
		return values;
	}

	/**
	 * Converts 2D numeric data to double[] with its two dimensions swapped, for
	 * example to turn data stored as [x][y] into the [y][x] (row = y) order that
	 * an image plot expects. The result has shape {shape[1], shape[0]}.
	 *
	 * @throws IllegalStateException if the data is not 2D and numeric
	 */
	public double[] getDataAsDoubleTransposed() {
		if (shape.length != 2) {
			throw new IllegalStateException("Variable " + name + " is not 2D: shape " + Arrays.toString(shape));
		}
		double[] values = getDataAsDouble();
		int rows = (int) shape[0];
		int cols = (int) shape[1];
		if (values.length != rows * cols) {
			throw new IllegalStateException("Variable " + name + " has " + values.length
					+ " elements but shape " + Arrays.toString(shape));
		}
		double[] result = new double[values.length];
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				result[c * rows + r] = values[r * cols + c];
			}
		}
		return result;
	}

	/**
	 * @return the value of a scalar, or the first element of an array
	 * @throws IllegalStateException for string, untyped or empty data
	 */
	public double getValue() {
		if (dataType == Da00DataType.C_STRING || dataType == Da00DataType.NONE || getSize() == 0) {
			throw new IllegalStateException("Variable " + name + " has no numeric value");
		}
		return element(ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN), 0);
	}

	/** @return the string value of c_string data */
	public String getString() {
		if (dataType != Da00DataType.C_STRING) {
			throw new IllegalStateException("Variable " + name + " is not a string (" + dataType + ")");
		}
		return new String(rawData, UTF8);
	}

	private double element(ByteBuffer buffer, int i) {
		switch (dataType) {
		case INT8:
			return buffer.get(i);
		case UINT8:
			return buffer.get(i) & 0xFF;
		case INT16:
			return buffer.getShort(i * 2);
		case UINT16:
			return buffer.getShort(i * 2) & 0xFFFF;
		case INT32:
			return buffer.getInt(i * 4);
		case UINT32:
			return buffer.getInt(i * 4) & 0xFFFFFFFFL;
		case INT64:
			return buffer.getLong(i * 8);
		case UINT64: {
			long bits = buffer.getLong(i * 8);
			if (bits >= 0) {
				return bits;
			}
			// Halve keeping the lowest bit as a sticky bit, so it rounds once
			return (double) ((bits >>> 1) | (bits & 1L)) * 2.0;
		}
		case FLOAT32:
			return buffer.getFloat(i * 4);
		case FLOAT64:
			return buffer.getDouble(i * 8);
		default:
			throw new IllegalStateException("Variable " + name + " has no numeric data (" + dataType + ")");
		}
	}

	@Override
	public String toString() {
		StringBuilder text = new StringBuilder(name);
		text.append(" (").append(dataType.name().toLowerCase());
		if (isString()) {
			text.append(") = \"").append(getString()).append('"');
			return text.toString();
		}
		if (axes.length > 0) {
			text.append(", ").append(Arrays.toString(axes)).append(' ').append(Arrays.toString(shape));
		}
		text.append(')');
		if (unit != null) {
			text.append(" [").append(unit).append(']');
		}
		if (isScalar()) {
			text.append(" = ").append(getValue());
		}
		return text.toString();
	}

}
