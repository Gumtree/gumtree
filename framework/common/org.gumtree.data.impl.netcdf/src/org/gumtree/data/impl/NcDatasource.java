package org.gumtree.data.impl;

import java.net.URI;

import org.gumtree.data.IDatasource;

public class NcDatasource implements IDatasource {

	@Override
	public String getFactoryName() {
		return NcFactory.NAME;
	}

	@Override
	public boolean isReadable(URI target) {
		// [ANSTO][Tony][2012-05-03][TODO] Implement better logic to detect URI
		return true;
	}

	@Override
	public boolean isProducer(URI target) {
		// [ANSTO][Tony][2012-05-03][TODO] Implement better logic to detect URI
		return true;
	}

	@Override
	public boolean isBrowsable(URI target) {
		// [ANSTO][Tony][2012-05-03][TODO] Implement better logic to detect URI
		return true;
	}

	@Override
	public boolean isExperiment(URI target) {
		// [ANSTO][Tony][2012-05-03][TODO] Implement better logic to detect URI
		return true;
	}

}
