package org.gumtree.dae.server.restlet;

import org.gumtree.core.management.IManageableBean;

public class DaeRestletMBean implements IManageableBean {

	public DaeRestletMBean() {
	}

	public long getCacheExpiry() {
		return DaeRestletProperties.CACHE_EXPIRY.getLong();
	}

	public void setCacheExpiry(long cacheExpiry) {
		DaeRestletProperties.CACHE_EXPIRY.setLong(cacheExpiry).save();
	}

	public int getCacheSize() {
		return DaeRestletProperties.CACHE_SIZE.getInt();
	}

	public void setCacheSize(int cacheSize) {
		DaeRestletProperties.CACHE_SIZE.setInt(cacheSize).save();
	}

	public void restoreDefaults() {
		DaeRestletProperties.CACHE_EXPIRY.reset();
		DaeRestletProperties.CACHE_SIZE.reset();
	}

	@Override
	public String getRegistrationKey() {
		return "org.gumtree.dae:type=Restlet";
	}

}
