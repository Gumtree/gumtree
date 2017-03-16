package org.gumtree.data.nexus;

import hep.aida.IFitter;
import hep.aida.IFunction;

import java.io.IOException;
import java.util.Map;

import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.nexus.fitting.DimensionNotSupportedException;
import org.gumtree.data.nexus.fitting.FitterException;
import org.gumtree.data.nexus.fitting.StaticField;
import org.gumtree.data.nexus.fitting.StaticField.EnginType;
import org.gumtree.data.nexus.fitting.StaticField.FitterType;
import org.gumtree.data.nexus.fitting.StaticField.FunctionType;

public interface INexusFitter {

	public abstract void setDimension(int dimension) throws FitterException;

	public abstract int getDimension();

	public abstract void parse(String functionText);

	public abstract String getDescription();

	public abstract void setDescription(String description);

	public abstract String getTitle();

	public abstract void setTitle(String title);

	public abstract FitterType getFitterType();

	public abstract EnginType getEnginType();

	public abstract String getFunctionText();

	public abstract Map<String, Double> getParameters();

	public abstract Map<String, Double> getFitErrors();

	public abstract double getParameterValue(String name);

	public abstract double getFitError(String name);

	public abstract FunctionType getFunctionType();

	public abstract void setFunctionType(FunctionType functionType);

	public abstract void createHistogram(INXdata data) throws IOException,
			FitterException;

	public abstract void createHistogram(INXdata data, double minX, double maxX)
			throws IOException, FitterException;

	public abstract void setParameterValue(String name, double value);

	public abstract void setParameters();

	public abstract void fit() throws IOException, InvalidArrayTypeException;

	public abstract void setParameterBounds(String name, double lowest,
			double highest);

	public abstract void setParameterFixed(String name, boolean isFixed);

//	public abstract void updatePlotResult() throws IOException,
//			InvalidArrayTypeException;

	public abstract void createPlotResult() throws IOException,
			InvalidArrayTypeException;

	public abstract INXdata getResult() throws IOException;

	public abstract int getResolutionMultiple();

	public abstract void setResolutionMultiple(int resolutionMultiple);

	public abstract double getQuality();

	public abstract boolean isInverse();

	public abstract void setInverse(boolean inverse)
			throws DimensionNotSupportedException, IOException, FitterException;

	public abstract boolean isInverseAllowed();

	public abstract IGroup toGDMGroup();

	public abstract void reset();

}