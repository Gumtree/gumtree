package org.gumtree.gumnix.sics.control.controllers;

import org.gumtree.gumnix.sics.core.SicsCoreException;
import org.gumtree.gumnix.sics.io.SicsIOException;

public interface IOneDDataController extends IGraphDataController {

	public int getDimension() throws SicsIOException, SicsCoreException;

	public float[] getDataset() throws SicsIOException, SicsCoreException;

	public float[] getDataset(boolean update) throws SicsIOException, SicsCoreException;

	public float[] getAxisset() throws SicsIOException, SicsCoreException;

	public float[] getAxisset(boolean update) throws SicsIOException, SicsCoreException;

}
