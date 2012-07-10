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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.graphics.RGB;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.vis.plot1d.MarkerShape;

import au.gov.ansto.bragg.kakadu.core.data.DataType;
import au.gov.ansto.bragg.kakadu.ui.util.Util;

/**
 * The bean object contains full information for plotting of data object.
 * <p>
 * PlotDataItem can be linked to real time updates by PlotDataReference.
 * In the linked state PlotDataItem data should be updated accordingly.  
 *  
 * @author Danil Klimontov (dak)
 */
public class PlotDataItem implements Cloneable {

	private static int ID_STATIC = 0;
	
	private IGroup data;
	private DataType dataType = DataType.Undefined;
	private String history, legend;
	private long timestamp;
	private String notes = "", title;
	private RGB color = null;
	
	private int id = -1;
	private static int idCounter = 0;
	private int kurandaPlotDataId = -1;
	private int plotId = -1;
	
	private boolean isLinkEnabled = true;
	private boolean isLinked = false;
	private boolean isVisible = true;
	// [GT-113] Marker support
//	private boolean isMarker = false;
	private MarkerShape markerShape = MarkerShape.NONE;
	
	private String filename = null;
	private PlotDataReference plotDataReference;
	private Object plotData;
	
	/**
	 * Creates PlotDataItem without reference.
	 * @param data plotable data 
	 * @param dataType data type of the plotable data
	 */
	public PlotDataItem(IGroup data, DataType dataType) {
		this(data, null, dataType);
	}

	/**
	 * Creates PlotDataItem with reference.
	 * @param data plotable data.
	 * @param dataType data type of the plotable data.
	 * @param plotDataReference reference to existed algorithm task.
	 */
	public PlotDataItem(IGroup data, PlotDataReference plotDataReference, DataType dataType) {
		id = idCounter ++;
		this.dataType = dataType;
		this.plotDataReference = plotDataReference;
		setData(data);
		setLinked(plotDataReference != null);
		
		title = "Plot "+id;
		//update legend
		legend = plotDataReference == null ? getReferenceString() : Util.generateLegendText(plotDataReference);
	}

	/**
	 * Updates inner fields with data  parsed from current data object.
	 */
	private void parseData() {
		//TODO update history
		history = "<no history>";
		//update timestamp
		if (data instanceof au.gov.ansto.bragg.datastructures.core.plot.Plot) {
			au.gov.ansto.bragg.datastructures.core.plot.Plot plotData =
				(au.gov.ansto.bragg.datastructures.core.plot.Plot) data;
			timestamp = plotData.getLastModificationTimeStamp();
			
		} else {
			timestamp = new Date().getTime();
		}
	}

	/**
	 * Gets id of the PlotDataItem.
	 * Id is generated in PlotDataItem object creation time. 
	 * @return id of the PlotDataItem.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Gets plottable data object.
	 * @return the data
	 */
	public IGroup getData() {
		return data;
	}

	/**
	 * Sets plottable data object.
	 * @param data the plottable data for the item.
	 */
	public void setData(IGroup data) {
		this.data = data;
		
		parseData();
	}

	
	/**
	 * Gets color the item should be displayed.
	 * @return the color in RGB format
	 */
	public RGB getColor() {
		return color;
	}

	/**
	 * Sets color the item should be displayed.
	 * @param color the color to set
	 */
	public void setColor(RGB color) {
		this.color = color;
	}

	/**
	 * Gets notes for the item.
	 * @return the notes
	 */
	public String getNotes() {
		return notes;
	}

	/**
	 * Sets notes for the item.
	 * @param notes the notes to set
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}

	/**
	 * Gets title of the item.
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets title of the item.
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Gets id of Kuranda wrapper object which is representing the item on plotting widget.
	 * @return the kurandaPlotDataId or -1 if not defined.
	 * @see IKurandaAPI#getMultiPlotManager()
	 */
	public int getKurandaPlotDataId() {
		return kurandaPlotDataId;
	}

	/**
	 * Sets id of Kuranda wrapper object which is representing the item on plotting widget.
	 * @param kurandaPlotDataId the kurandaPlotDataId to set
	 * @see IKurandaAPI#getMultiPlotManager()
	 */
	public void setKurandaPlotDataId(int kurandaPlotDataId) {
		this.kurandaPlotDataId = kurandaPlotDataId;
	}

	/**
	 * Gets id of Plot composite for the item. 
	 * @return the plotId
	 * @see Plot
	 */
	public int getPlotId() {
		return plotId;
	}

	/**
	 * Sets id of Plot composite for the item. 
	 * @param plotId the plotId to set
	 * @see Plot
	 */
	public void setPlotId(int plotId) {
		this.plotId = plotId;
	}

	/**
	 * Gets isLinked sate for the item.
	 * The PlotDataItem if defined as linked if the PlotDataItem should be updated with plottable data from referenced operation.
	 * @return the isLinked
	 */
	public boolean isLinked() {
		return isLinked;
	}

	/**
	 * @param isLinked the isLinked to set
	 */
	public void setLinked(boolean isLinked) {
		this.isLinked = isLinked;
	}
	
	/**
	 * Gets link enabled flag. Liking can be disabled 
	 * if referred Algorithm task does not exist any more.
	 * @return the isLinkEnabled flag
	 */
	public boolean isLinkEnabled() {
		return isLinkEnabled;
	}

	/**
	 * @param isLinkEnabled the isLinkEnabled to set
	 */
	public void setLinkEnabled(boolean isLinkEnabled) {
		this.isLinkEnabled = isLinkEnabled;
	}

	/**
	 * @return the isVisible
	 */
	public boolean isVisible() {
		return isVisible;
	}

	/**
	 * @param isVisible the isVisible to set
	 */
	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	/**
	 * @return the isMarker
	 */
	public MarkerShape getMarkerShape() {
		return markerShape;
	}

	/**
	 * @param isMarker the isMarker to set
	 */
	public void setMarkerShape(MarkerShape markerShape) {
		this.markerShape = markerShape;
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * @return the plotDataReference
	 */
	public PlotDataReference getPlotDataReference() {
		return plotDataReference;
	}

	/**
	 * @param plotDataReference the plotDataReference to set
	 */
//	public void setPlotDataReference(PlotDataReference plotDataReference) {
//		this.plotDataReference = plotDataReference;
//	}

	/**
	 * @return the history
	 */
	public String getHistory() {
		if (data instanceof au.gov.ansto.bragg.datastructures.core.plot.Plot)
			return ((au.gov.ansto.bragg.datastructures.core.plot.Plot) data).getLogString();
		else 
			return ((NcGroup) data).getLog();
	}

	/**
	 * @return the legend
	 */
	public String getLegend() {
		return legend;
	}

	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}
	
	/**
	 * @return the dataType
	 */
	public DataType getDataType() {
		return dataType;
	}

	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	public String getReferenceString() {
		if (filename != null) {
			return filename;
		} else if(plotDataReference != null) {
			return "T" + plotDataReference.getTaskId() +
				".O" + plotDataReference.getOperationIndex() +
				".D" + plotDataReference.getDataItemIndex();
		}
		return "<no reference>";
	}

	public PlotDataItem clone() {
		try {
			final PlotDataItem clone = (PlotDataItem) super.clone();
			clone.id = idCounter++;
			clone.kurandaPlotDataId = -1;
			clone.plotDataReference = plotDataReference != null ? 
					new PlotDataReference(plotDataReference.getTaskId(), 
							plotDataReference.getOperationIndex(), 
							plotDataReference.getDataItemIndex()) : null;
			
			return clone;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	private PlotDataItem parent = null;
	private List<PlotDataItem> children = new ArrayList<PlotDataItem>();

	/**
	 * @return the parent
	 */
	public PlotDataItem getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(PlotDataItem parent) {
		this.parent = parent;
	}

	/**
	 * @return the children
	 */
	public List<PlotDataItem> getChildren() {
		return new ArrayList<PlotDataItem>(children);
	}

	/**
	 * @param children the children to set
	 */
	public void addChild(PlotDataItem child) {
		children.add(child);
	}
	
	/**
	 * 
	 * @return
	 */
	public int getChildrenCount() {
		return children.size();
	}
	
	public static int getNextID() {
		return ID_STATIC++;
	}

	/**
	 * @param plotData the plotData to set
	 */
	public void setPlotData(Object plotData) {
		this.plotData = plotData;
	}

	/**
	 * @return the plotData
	 */
	public Object getPlotData() {
		return plotData;
	}
	
}
