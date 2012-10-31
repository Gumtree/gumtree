package org.gumtree.util.akka;

import akka.actor.AbstractExtensionId;
import akka.actor.ExtendedActorSystem;
import akka.actor.Extension;
import akka.actor.ExtensionId;
import akka.actor.ExtensionIdProvider;
import akka.actor.IOManager;

public class IOManagerExtension extends AbstractExtensionId<IOManager>
		implements ExtensionIdProvider {

	public final static IOManagerExtension PROVIDER = new IOManagerExtension();

	@Override
	public IOManager createExtension(ExtendedActorSystem actorSystem) {
		return new IOManager(actorSystem);
	}

	@Override
	public synchronized ExtensionId<? extends Extension> lookup() {
		return IOManagerExtension.PROVIDER;
	}

}
