package org.gumtree.gumnix.sics.control;

import org.gumtree.gumnix.sics.io.SicsIOException;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.Instrument;

public interface ISicsInstrumentControl {

	public Instrument getModel() throws SicsIOException ;

	public IInstrumentController getInstrumentController();

	public IComponentController findComponentController(String path);

	public IComponentController findComponentController(Component component);

}
