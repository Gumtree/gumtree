package org.gumtree.gumnix.sics.control;

import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.core.SicsCoreException;
import org.gumtree.gumnix.sics.io.SicsIOException;

import ch.psi.sics.hipadaba.Component;

public interface IControllerMap {

	public IComponentController getBaseController();

	public Component getBaseComponent();

	public IComponentController getController(IControllerKey key) throws SicsCoreException;

	public Component getComponent(IControllerKey key) throws SicsCoreException;

	public int getIntData(IControllerKey key) throws SicsIOException, SicsCoreException;

	public void setIntData(IControllerKey key, int data) throws SicsIOException, SicsCoreException;

	public float getFloatData(IControllerKey key) throws SicsIOException, SicsCoreException;

	public void setFloatData(IControllerKey key, float data) throws SicsIOException, SicsCoreException;

	public String getStringData(IControllerKey key) throws SicsIOException, SicsCoreException;

	public void setStringData(IControllerKey key, String data) throws SicsIOException, SicsCoreException;

	public int[] getIntArray(IControllerKey key) throws SicsIOException, SicsCoreException;

	public float[] getFloatArray(IControllerKey key) throws SicsIOException, SicsCoreException;

}
