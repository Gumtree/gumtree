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

import au.gov.ansto.bragg.kakadu.ui.plot.Plot;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotDataItem;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotManager;

/**
 * Provides coding and decoding for transferring PlotDataItem objects.
 * @author Danil Klimontov (dak)
 */
public class PlotDataItemTransfer extends ByteArrayTransfer {
	private static PlotDataItemTransfer instance = new PlotDataItemTransfer();
	private static final String TYPE_NAME = "PlotDataItem-transfer-format";
	private static final int TYPEID = registerType(TYPE_NAME);

	/**
	 * Private constructor.
	 */
	private PlotDataItemTransfer() {
	}

	/**
	 * Returns the singleton OperationTransfer instance.
	 * <p>
	 * @return The OperationTransfer instance.
	 */
	public static PlotDataItemTransfer getInstance() {
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
		byte[] bytes = toByteArray((PlotDataItem) object);
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
	 * Convert the PlotDataItem object to byte array
	 * <pre>
	 * Transfer data is an PlotDataItem object.  Serialized version is:
	 * (int) PlotDataItem id
	 * (int) Plot id
	 * </pre>
	 */
	protected byte[] toByteArray(PlotDataItem plotDataItem) {
		
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(byteOut);

		byte[] bytes = null;

		try {
			/* write PlotDataItem id */
			out.writeInt(plotDataItem.getId());
			/* write Plot Id  */
			out.writeInt(plotDataItem.getPlotId());

			out.close();
			bytes = byteOut.toByteArray();
		} catch (IOException e) {
			//when in doubt send nothing
		}
		return bytes;
	}

	protected PlotDataItem fromByteArray(byte[] bytes) {
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));

		try {
			int plotDataItemId = in.readInt();
			int plotId = in.readInt();
			
			final Plot plot = PlotManager.getPlot(plotId);
			if (plot != null) {
				return plot.getMultiPlotDataManager().getPlotDataItem(plotDataItemId);
			}
		} catch (IOException e) {
			return null;
		}
		return null;
	}
}
