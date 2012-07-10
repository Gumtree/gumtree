package org.gumtree.gumnix.sics.internal.core;

import org.gumtree.gumnix.sics.core.ISicsCoreService;
import org.gumtree.gumnix.sics.core.ISicsManager;
import org.gumtree.gumnix.sics.io.SicsIOException;

import ch.psi.sics.hipadaba.SICS;

public class SicsCoreService implements ISicsCoreService {

	private ISicsManager manager;

//	private InstrumentProfileRegistry registry;

//	private IInstrumentProfile profile;
	
	private SicsModelLoader loader;

	public SicsCoreService(ISicsManager manager) {
		this.manager = manager;
		this.loader = new SicsModelLoader(manager);
	}

	public ISicsManager getManager() {
		return manager;
	}

//	private InstrumentProfileRegistry getRegistry() {
//		if(registry == null) {
//			registry = new InstrumentProfileRegistry();
//		}
//		return registry;
//	}

	public SICS getOnlineModel() throws SicsIOException {
		return loader.getModel();
	}

//	public IInstrumentProfile getCurrentInstrumentProfile() {
//		return profile;
//	}
//
//	public void setCurrentInstrumentProfile(IInstrumentProfile profile) {
//		Assert.isNotNull(profile);
//		this.profile = profile;
//	}

//	public IInstrumentProfile[] getRegisteredProfiles() {
//		return getRegistry().getRegisteredProfiles();
//	}
//
//	public IInstrumentProfile getInstrumentProfile(String id) {
//		return getRegistry().getInstrumentDescriptor(id);
//	}

}
