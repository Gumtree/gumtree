package org.gumtree.service.actorsystem.support;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gumtree.core.CoreProperties;
import org.gumtree.service.actorsystem.IActorSystemService;
import org.gumtree.util.eclipse.OsgiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.actors.threadpool.helpers.Utils;
import akka.actor.ActorRefFactory;
import akka.actor.ActorSystem;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ActorSystemService implements IActorSystemService {

	private static final Logger logger = LoggerFactory
			.getLogger(ActorSystemService.class);

	private ActorSystem system;

	public void activate() {
		try {
			// Actor config
			String path = OsgiUtils
					.findFilePath("akka.actor", "reference.conf");
			Config actorConfig = ConfigFactory.parseFile(new File(path));

			// Remote config
			path = OsgiUtils.findFilePath("akka.remote", "reference.conf");
			Config remoteConfig = ConfigFactory.parseFile(new File(path));
			remoteConfig = remoteConfig.withFallback(actorConfig);

			// Custom config
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put("akka.actor.provider",
					"akka.remote.RemoteActorRefProvider");
			properties.put("akka.remote.netty.hostname", InetAddress
					.getLocalHost().getHostAddress());
			properties.put("akka.remote.netty.port",
					CoreProperties.AKKA_PORT.getInt());
			properties.put("akka.loglevel",
					CoreProperties.AKKA_DEBUG_LEVEL.getValue());
			List<String> handlers = new ArrayList<String>();
			handlers.add("akka.event.slf4j.Slf4jEventHandler");
			properties.put("akka.event-handlers", handlers);
			Config config = ConfigFactory.parseMap(properties);
			config = config.withFallback(remoteConfig);

			// Create actor system
			system = ActorSystem.create("ActorSystem", config);
		} catch (Exception e) {
			logger.error("Failed to create actor system", e);
		}
	}

	public void deactivate() {
		if (system != null) {
			system.shutdown();
			system = null;
		}
	}

	@Override
	public ActorRefFactory getActorSystem() {
		return system;
	}

}
