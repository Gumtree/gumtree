package org.gumtree.kafka.clients.da00;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A message in the ESS "da00" data array format (FlatBuffers, file identifier
 * "da00"), such as the histograms published by just-bin-it. This is the Java
 * equivalent of {@code deserialise_da00} in
 * https://github.com/ess-dmsc/python-streaming-data-types
 * (streaming_data_types/dataarray_da00.py).
 * <p>
 * From Jython:
 *
 * <pre>
 * from org.gumtree.kafka.clients.da00 import Da00DataArray
 * array = Da00DataArray.deserialise(message.getValue())
 * signal = array.getVariable('signal')
 * values = signal.getDataAsDouble()   # flat, row-major
 * shape = signal.getShapeAsInt()
 * </pre>
 */
public final class Da00DataArray {

	public static final String FILE_IDENTIFIER = "da00";

	// vtable offsets of the fields in table da00_DataArray
	private static final int SOURCE_NAME = 4;
	private static final int TIMESTAMP = 6;
	private static final int DATA = 8;

	private final String sourceName;

	private final long timestampNs;

	private final List<Da00Variable> variables;

	private Da00DataArray(String sourceName, long timestampNs, List<Da00Variable> variables) {
		this.sourceName = sourceName;
		this.timestampNs = timestampNs;
		this.variables = Collections.unmodifiableList(variables);
	}

	/**
	 * @return true if the buffer carries the da00 file identifier
	 */
	public static boolean isDa00(byte[] buffer) {
		return FILE_IDENTIFIER.equals(FlatBufferTable.fileIdentifier(buffer));
	}

	/**
	 * Decodes a da00 message.
	 *
	 * @throws IllegalArgumentException
	 *             if the buffer is not da00 or is corrupt
	 */
	public static Da00DataArray deserialise(byte[] buffer) {
		if (buffer == null || buffer.length < 8) {
			throw new IllegalArgumentException("Buffer too short for a FlatBuffers message");
		}
		String identifier = FlatBufferTable.fileIdentifier(buffer);
		if (!FILE_IDENTIFIER.equals(identifier)) {
			throw new IllegalArgumentException(
					"Incorrect schema: expected " + FILE_IDENTIFIER + " but got " + identifier);
		}
		FlatBufferTable root = FlatBufferTable.root(buffer);
		String sourceName = root.getString(SOURCE_NAME);
		long timestampNs = root.getLong(TIMESTAMP, 0L);
		int count = root.vectorLength(DATA);
		List<Da00Variable> variables = new ArrayList<Da00Variable>(count);
		for (int j = 0; j < count; j++) {
			variables.add(new Da00Variable(root.getTable(DATA, j)));
		}
		return new Da00DataArray(sourceName, timestampNs, variables);
	}

	public String getSourceName() {
		return sourceName;
	}

	/** @return the timestamp in nanoseconds since the Unix epoch */
	public long getTimestampNs() {
		return timestampNs;
	}

	/** @return the timestamp in milliseconds since the Unix epoch */
	public long getTimestampMs() {
		return timestampNs / 1000000L;
	}

	public List<Da00Variable> getVariables() {
		return variables;
	}

	/** @return the variable with this name, or null if there is none */
	public Da00Variable getVariable(String name) {
		for (Da00Variable variable : variables) {
			if (variable.getName() != null && variable.getName().equals(name)) {
				return variable;
			}
		}
		return null;
	}

	/** @return the names of all variables, in order */
	public List<String> getVariableNames() {
		List<String> names = new ArrayList<String>(variables.size());
		for (Da00Variable variable : variables) {
			names.add(variable.getName());
		}
		return names;
	}

	@Override
	public String toString() {
		StringBuilder text = new StringBuilder("da00 ");
		text.append(sourceName).append(" @ ").append(timestampNs).append(" ns");
		for (Da00Variable variable : variables) {
			text.append("\n  ").append(variable);
		}
		return text.toString();
	}

}
