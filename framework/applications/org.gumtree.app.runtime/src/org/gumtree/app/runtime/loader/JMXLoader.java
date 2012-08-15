package org.gumtree.app.runtime.loader;

import org.gumtree.app.runtime.RuntimeProperties;
import org.osgi.framework.BundleContext;
import org.springframework.jmx.support.ConnectorServerFactoryBean;
import org.springframework.remoting.rmi.RmiRegistryFactoryBean;

public class JMXLoader implements IRuntimeLoader {

	private RmiRegistryFactoryBean rmiRegistryFactoryBean;

	private ConnectorServerFactoryBean connectorServerFactoryBean;

	@Override
	public void load(BundleContext context) throws Exception {
		int port = Integer.getInteger(RuntimeProperties.RMI_REGISTRY_PORT,
				60050);

		rmiRegistryFactoryBean = new RmiRegistryFactoryBean();
		rmiRegistryFactoryBean.setAlwaysCreate(true);
		rmiRegistryFactoryBean.setPort(port);
		rmiRegistryFactoryBean.afterPropertiesSet();

		connectorServerFactoryBean = new ConnectorServerFactoryBean();
		connectorServerFactoryBean
				.setServiceUrl("service:jmx:rmi:///jndi/rmi://localhost:"
						+ port + "/jmxrmi");
		connectorServerFactoryBean.afterPropertiesSet();
	}

	@Override
	public void unload(BundleContext context) throws Exception {
		if (rmiRegistryFactoryBean != null) {
			try {
				rmiRegistryFactoryBean.destroy();
			} catch (Exception e) {
			}
			rmiRegistryFactoryBean = null;
		}
		if (connectorServerFactoryBean != null) {
			try {
				connectorServerFactoryBean.destroy();
			} catch (Exception e) {
			}
			connectorServerFactoryBean = null;
		}
	}

}
