package org.gumtree.gumnix.sics.control.controllers;

import org.gumtree.gumnix.sics.core.SicsCoreException;
import org.gumtree.gumnix.sics.io.SicsIOException;

public interface IScanConfig {

	public String getMode();

	public void setMode(String mode);

	public String getVariable();

	public void setVariable(String variable);

	public float getStartValue();

	public void setStartValue(float startValue);

	public float getIncrement();

	public void setIncrement(float increment);

	public int getNumberOfPoint();

	public void setNumberOfPoint(int numberOfPoint);

	public float getPreset();

	public void setPreset(float preset);

	public int getChannel();

	public void setChannel(int channel);

//	public int getRepeat();
//
//	public void setRepeat(int times);

	public void commit() throws SicsIOException, SicsCoreException;

	public void update() throws SicsIOException, SicsCoreException;

	public String[] getAvailableModes() throws SicsCoreException;

	public String getScanVariableType() throws SicsCoreException;

	public int getMinimumChannelSize() throws SicsCoreException;

	public int getMaximumChannelSize() throws SicsCoreException;

}
