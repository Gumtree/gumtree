package org.gumtree.gumnix.sics.core;

import org.gumtree.gumnix.sics.io.SicsIOException;

import ch.psi.sics.hipadaba.SICS;

public interface ISicsCoreService {

	public SICS getOnlineModel() throws SicsIOException;

//	/**
//	 * Returns the registered instrument profile to be connected.
//	 * If multiple instrument profiles are registered in the system,
//	 * only one profile (first profile that is loaded) will be returned.
//	 * If users specified the default from command line (see {@link ISicsCoreService#OPTION_SICS_INSTRUMENT}),
//	 * and it is registered in the system, it will get returned as the default.
//	 *
//	 * @return the default instrument profile in the system, null if no profile is registered.
//	 */
//	public IInstrumentProfile getCurrentInstrumentProfile();
//
//	public void setCurrentInstrumentProfile(IInstrumentProfile profile);
//
//	public IInstrumentProfile[] getRegisteredProfiles();
//
//	public IInstrumentProfile getInstrumentProfile(String id);

}
