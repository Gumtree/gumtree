/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.ui.plot.dnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

import au.gov.ansto.bragg.kakadu.ui.plot.PlotDataReference;

/**
 * Transfer object for an operation.
 *  
 * @author Danil Klimontov (dak)
 */
public class PlotDataReferenceTransfer extends ByteArrayTransfer {

	private static PlotDataReferenceTransfer instance = new PlotDataReferenceTransfer();
	private static final String TYPE_NAME = "PlotDataReference-transfer-format";
	private static final int TYPEID = registerType(TYPE_NAME);

	/**
	 * Private constructor.
	 */
	private PlotDataReferenceTransfer() {
	}

	/**
	 * Returns the singleton OperationTransfer instance.
	 * <p>
	 * @return The OperationTransfer instance.
	 */
	public static PlotDataReferenceTransfer getInstance() {
		return instance;
	}

	/*
	 * Method declared on Transfer.
	 */
	protected int[] getTypeIds() {
		return new int[] { TYPEID };
	}

	/*
	 * Method declared on Transfer.
	 */
	protected String[] getTypeNames() {
		return new String[] { TYPE_NAME };
	}

	/*
	 * Method declared on Transfer.
	 */
	protected void javaToNative(Object object, TransferData transferData) {
		byte[] bytes = toByteArray((PlotDataReference) object);
		if (bytes != null)
			super.javaToNative(bytes, transferData);
	}

	/*
	 * Method declared on Transfer.
	 */
	protected Object nativeToJava(TransferData transferData) {
		byte[] bytes = (byte[]) super.nativeToJava(transferData);
		return fromByteArray(bytes);
	}

	/**
	 * Convert the object to byte array
	 * <pre>
	 * Transfer data is an PlotDataReference object.  Serialized version is:
	 * (int) algorithm task id
	 * (int) opearation index
	 * (int) data item index
	 * </pre>
	 */
	protected byte[] toByteArray(PlotDataReference plotDataReference) {
		
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(byteOut);

		byte[] bytes = null;

		try {
			/* write task id */
			out.writeInt(plotDataReference.getTaskId());
			/* write operation index */
			out.writeInt(plotDataReference.getOperationIndex());
			/* write data item index */
			out.writeInt(plotDataReference.getDataItemIndex());

			out.close();
			bytes = byteOut.toByteArray();
		} catch (IOException e) {
			//when in doubt send nothing
		}
		return bytes;
	}

	protected PlotDataReference fromByteArray(byte[] bytes) {
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));

		try {
			int taskId = in.readInt();
			int operationIndex = in.readInt();
			int dataItemIndex = in.readInt();
			return new PlotDataReference(taskId, operationIndex, dataItemIndex);
		} catch (IOException e) {
			return null;
		}
	}
}
