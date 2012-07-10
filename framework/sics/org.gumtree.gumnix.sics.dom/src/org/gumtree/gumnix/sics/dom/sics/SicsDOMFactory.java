package org.gumtree.gumnix.sics.dom.sics;


public class SicsDOMFactory {

	public SicsDOMFactory() {
		super();
	}

	public Object getDOMroot() {
		// This may cause Groovy Monkey editor problem (Outline View exception)
		// if GumTree is not connected to SICS 
//		if(!SicsCore.getDefaultProxy().isConnected()) {
//			throw new RuntimeException("Sics is not connected.");
//		}
		return new SicsDOM();
	}

	public static Object getSicsDOM(){
		return new SicsDOM();
	}
}
