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
package au.gov.ansto.bragg.kakadu.ui.plot;

import org.eclipse.swt.graphics.RGB;
import org.gumtree.data.interfaces.IGroup;

/**
 * The data item for an element in MapSet or Pattern set.
 * @author Danil Klimontov (dak)
 */
public class ChildPlotDataItem {

	private final PlotDataItem parent;
	private final int dataFrameIndex;
	private int kurandaPlotDataId;
	private int id;
	private RGB color;

	public ChildPlotDataItem(PlotDataItem parentPlotDataItem, int dataFrameIndex) {
		this.parent = parentPlotDataItem;
		this.dataFrameIndex = dataFrameIndex;
	}

//	@Override
//	public PlotDataItem clone() {
//		return super.clone();
//	}
//
	public RGB getColor() {
		return color;
	}

	/**
	 * @return the parent
	 */
	public PlotDataItem getParent() {
		return parent;
	}

	/**
	 * @return the dataFrameIndex
	 */
	public int getDataFrameIndex() {
		return dataFrameIndex;
	}

	public IGroup getData() {
		return parent.getData();
	}

	public String getFilename() {
		return parent.getFilename();
	}

	public String getHistory() {
		return parent.getHistory();
	}

	public int getId() {
		return id;
	}

	public int getKurandaPlotDataId() {
		return kurandaPlotDataId;
	}

	public String getLegend() {
		return parent.getLegend();
	}

	public String getNotes() {
		return parent.getNotes();
	}

	public PlotDataReference getPlotDataReference() {
		return parent.getPlotDataReference();
	}

	public int getPlotId() {
		return parent.getPlotId();
	}

	public String getReferenceString() {
		return parent.getReferenceString();
	}

	public long getTimestamp() {
		return parent.getTimestamp();
	}

	public String getTitle() {
		return parent.getTitle() + " [" + dataFrameIndex + "]";
	}

	public boolean isLinked() {
		return parent.isLinked();
	}

	public void setColor(RGB color) {
		this.color = color;
	}

	public void setData(IGroup data) {
		parent.setData(data);
	}

	public void setFilename(String filename) {
		parent.setFilename(filename);
	}

	public void setKurandaPlotDataId(int kurandaPlotDataId) {
		this.kurandaPlotDataId = kurandaPlotDataId;
	}

	public void setLinked(boolean isLinked) {
		parent.setLinked(isLinked);
	}

	public void setNotes(String notes) {
		parent.setNotes(notes);
	}

	public void setPlotId(int plotId) {
		parent.setPlotId(plotId);
	}

	public void setTitle(String title) {
		parent.setTitle(title);
	}

	
	
}
