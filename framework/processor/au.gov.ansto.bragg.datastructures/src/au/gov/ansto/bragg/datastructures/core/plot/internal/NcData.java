/******************************************************************************* 
 * Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.datastructures.core.plot.internal;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.impl.netcdf.NcAttribute;
import org.gumtree.data.impl.netcdf.NcDataItem;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition;
import au.gov.ansto.bragg.datastructures.core.exception.PlotFactoryException;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Data;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.Variance;

/**
 * @author nxi
 * Created on 06/03/2008
 */
public class NcData extends NcDataItem implements Data {

	public NcData(NcGroup group, String shortName, IArray array, String title, String units)
	throws InvalidArrayTypeException {
		super(group, shortName, array);
		addStringAttribute("signal", "1");
		if (title != null) addStringAttribute("title", title);
		if (units != null) addStringAttribute("units", units);
	}

	public NcData(NcGroup group, String shortName, IArray array, String title, String units,
			IArray varianceArray)
	throws InvalidArrayTypeException {
		this(group, shortName, array, title, units);
		if (varianceArray != null)
			addVariance(varianceArray);
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.plot.Data#addAxis(au.gov.ansto.bragg.datastructures.core.plot.Axis, int)
	 */
	public void addAxis(Axis axis, int dimension) {
		IAttribute axisAttribute = getAttribute("axes");
		String newAxisString = "";
		if (axisAttribute != null){
			String axisNames = axisAttribute.getStringValue();
			String[] axes = axisNames.split(":");
			if (dimension < axes.length){
				axes[dimension] = axis.getShortName();
				for (int i = 0; i < axes.length; i++) {
					newAxisString += axes[i];
					if (i < axes.length - 1)
						newAxisString += ":";
				}
			}else{
				newAxisString = axisNames;
				for(int i = axes.length - 1; i < dimension; i ++) 
					newAxisString += ":";
				newAxisString += axis.getShortName();
			}
			axisAttribute.setStringValue(newAxisString);
		}else{
			for (int i = 0; i < dimension; i++)
				newAxisString += ":";
			newAxisString += axis.getShortName();
			addStringAttribute("axes", newAxisString);
		}
//		Attribute unitsAttribute = findAttribute("units");
//		if (unitsAttribute == null){
//		addStringAttribute("units", axis.getShortName());
//		}
//		else {
//		String unitsValue = unitsAttribute.getStringValue();
//		String[] units = unitsValue.split(":");
//		String newUnitsValue = "";
//		for (int i = 0; i < units.length; i++) {
//		if (dimension == i) 
//		}
//		}
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.plot.Data#getAxisList()
	 */
	public List<Axis> getAxisList() {
		return ((Plot) getParentGroup()).getAxisList();
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.plot.Data#getTitle()
	 */
	public String getTitle() {
		return findAttribute("title").getStringValue();
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.plot.Data#getVariance()
	 */
	public Variance getVariance() {
		IAttribute attribute = getAttribute(StaticDefinition.DATA_VARIANCE_REFERENCE_NAME);
		if (attribute == null) return null;
		return (Variance) getParentGroup().findDataItem(attribute.getStringValue());
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.datastructures.core.plot.Data#removeAxis(au.gov.ansto.bragg.datastructures.core.plot.Axis)
	 */
	public void removeAxis(Axis axis) {
		getParentGroup().removeDataItem(axis);
		IAttribute axesAttribute = getAttribute("axes");
		if (axesAttribute != null){
			String axesString = axesAttribute.getStringValue();
			String[] axes = axesString.split(":");
			for (int i = 0; i < axes.length; i ++){
				if (axes[i].equals(axis.getShortName())){
					axes[i] = "";
				}
			}
			axesString = buildNameVector(axes);
			axesAttribute.setStringValue(axesString);
		}
	}

	private String buildNameVector(String[] axes) {
		String axesValue = "";
		for (int i = 0; i < axes.length; i++) {
			if (axes[i].length() > 0){
				axesValue += axes[i];
				if (i < axes.length -1) axesValue += ":";
			}
		}
		if (axesValue.endsWith(":"))
			axesValue = axesValue.substring(0, axesValue.length() - 1);
		return axesValue;
	}

	public void addAxes(Axis... axes) {
		String axesValue = "";
		for (int i = 0; i < axes.length; i++) {
			axesValue += axes[i];
			if (i < axes.length -1) axesValue += ":";
		}
		IAttribute axesAttribute = getAttribute("axes");
		if (axesAttribute != null)
			axesAttribute.setStringValue(axesValue);
		else addStringAttribute("axes", axesValue);
	}

	public NcData getASlice(int dimension, int value) throws InvalidRangeException {
//		NcData data = new 
		return null;
	}

	public void addVariance(IArray varianceArray) throws InvalidArrayTypeException{
		if (varianceArray != null)
			addVariance(varianceArray, getShortName() + "Variance");
	}

	public void addVariance(IArray varianceArray, String shortName) 
	throws InvalidArrayTypeException{
		NcGroup parent = getParentGroup();
		if (varianceArray != null){
			Variance variance = new NcVariance(parent, shortName, varianceArray, this);
			addOneAttribute(Factory.createAttribute(StaticDefinition.DATA_VARIANCE_REFERENCE_NAME, shortName));
			parent.addDataItem(variance);
		}
	}

	public void reduce() throws PlotFactoryException {
		int[] oldShape = getShape();
		try {
			setCachedData(getData().getArrayUtils().reduce().getArray(), false);
			shape = getData().getShape();
		} catch (Exception e) {
			throw new PlotFactoryException(e);
		}
		Variance variance = getVariance();
		if (variance != null)
			variance.reduce();
		List<?> dimensions = getDimensions();
		if (dimensions.size() == oldShape.length) {
			for (int j = 0; j < oldShape.length; j++) {
					if (oldShape[j] == 1){
					dimensions.remove(j);
				}
			}
		}
		List<Axis> axes = getAxisList();
		List<Axis> toBeRemove = new ArrayList<Axis>();
		for (int i = 0; i < oldShape.length; i ++){
			if (oldShape[i] == 1){
				if (axes.size() > i)
					toBeRemove.add(axes.get(i));
			}
		}
		for (Axis axis: toBeRemove){
			removeAxis(axis);
		}
	}
	
	public void reduceTo(int rank) throws PlotFactoryException{
		int[] oldShape = getShape();
		try {
			setCachedData(getData().getArrayUtils().reduceTo(rank).getArray(), false);
			shape = getData().getShape();
		} catch (Exception e) {
			e.printStackTrace();
			throw new PlotFactoryException(e);
		}
		Variance variance = getVariance();
		if (variance != null)
			variance.reduceTo(rank);
		List<?> dimensions = getDimensions();
		if (dimensions.size() == oldShape.length) {
			for (int j = 0; j < oldShape.length; j++) {
				if (dimensions.size() <= rank)
					break;
				if (oldShape[j] == 1)
					dimensions.remove(j);
			}
		}
		List<Axis> axes = getAxisList();
		List<Axis> toBeRemove = new ArrayList<Axis>();
		for (int i = 0; i < oldShape.length; i ++){
			if (axes.size() - toBeRemove.size() <= rank)
				break;
			if (oldShape[i] == 1){
				if (axes.size() > i)
					if (axes.get(i).getShortName().equals("time_of_flight"))
						toBeRemove.add(axes.get(i));
			}
		}
		for (int i = 0; i < oldShape.length; i ++){
			if (axes.size() - toBeRemove.size() <= rank)
				break;
			if (oldShape[i] == 1){
				if (axes.size() > i)
					toBeRemove.add(axes.get(i));
			}
		}
		for (Axis axis: toBeRemove){
			removeAxis(axis);
		}
	}

	public void setTitle(String title) {
		if (title == null) return; 
		IAttribute titleAttribute = getAttribute("title");
		if (titleAttribute != null)
			titleAttribute.setStringValue(title);
		else 
			addStringAttribute("title", title);
	}

	public void setUnits(String units){
		NcAttribute unitsAttribute = null;
		try {
			unitsAttribute = getAttribute("units");
		} catch (Exception e) {
		}
		if (unitsAttribute == null){ 
			unitsAttribute = new NcAttribute("units", units);
			this.addOneAttribute(unitsAttribute);
		}else{
			unitsAttribute.setStringValue(units);
		}
	}
}
