package org.gumtree.gumnix.sics.control.controllers;

import org.gumtree.gumnix.sics.core.SicsCoreException;
import org.gumtree.gumnix.sics.io.SicsIOException;

public interface IScanStatus {

	public String getFilename() throws SicsIOException, SicsCoreException;

	public String getMode() throws SicsIOException, SicsCoreException;

	public float getPreset() throws SicsIOException, SicsCoreException;

	public float getScanVariableValue() throws SicsIOException, SicsCoreException;

	public int getCurrentScanPoint() throws SicsIOException, SicsCoreException;

	public int getCount() throws SicsIOException, SicsCoreException;

}
