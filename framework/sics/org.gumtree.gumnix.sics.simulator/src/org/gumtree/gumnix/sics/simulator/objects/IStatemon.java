package org.gumtree.gumnix.sics.simulator.objects;

public interface IStatemon {

	public static final String ID = "statemon";

	public void interestCallback(Object data);

	public void hdbInterestCallback(Object data);
}
