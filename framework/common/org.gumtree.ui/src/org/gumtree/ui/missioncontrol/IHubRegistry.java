package org.gumtree.ui.missioncontrol;

import java.util.List;

import org.gumtree.core.service.IService;

public interface IHubRegistry extends IService {

	public static final class Events {

		public static final String TOPIC_BASE = "org/gumtree/ui/missioncontrol/hub";

		public static final String TOPIC_ALL = TOPIC_BASE + "/*";

	}

	public List<IHub> getHubs();

	public void addHub(IHub hub);

	public void removeHub(IHub hub);

}
