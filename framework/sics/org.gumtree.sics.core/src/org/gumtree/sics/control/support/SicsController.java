package org.gumtree.sics.control.support;

import static ch.lambdaj.Lambda.*;
import static ch.lambdaj.collection.LambdaCollections.*;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.gumtree.sics.control.ControllerStatus;
import org.gumtree.sics.control.ISicsController;
import org.gumtree.sics.io.ISicsProxy;
import org.gumtree.sics.io.SicsEventBuilder;
import org.gumtree.util.string.IStringProvider;
import org.gumtree.util.string.StringUtils;

import ch.psi.sics.hipadaba.Component;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

public class SicsController implements ISicsController {

	private String id;

	private String deviceId;

	private String path;
	
	private ControllerStatus status;

	private Component componentModel;

	private ISicsProxy proxy;
	
	private ISicsController parent;
	
	private Map<String, ISicsController> childMap;

	public SicsController() {
		super();
		childMap = new HashMap<String, ISicsController>(2);
		this.status = ControllerStatus.OK;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getDeviceId() {
		return deviceId;
	}

	@Override
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	@Override
	public String getPath() {
		if (path == null) {
			if (getParent() != null) {
				String parentPath = getParent().getPath();
				if (!parentPath.equals("/")) {
					// For parent is not the root of the tree
					path = getParent().getPath() + "/" + getId();
				}
			}
			if (path == null) {
				// For controller is a root or parent is a root
				path = "/" + getId();
			}
		}
		return path;
	}

	@Override
	public void setPath(String path) {
		this.path = path;
	}
	
	@Override
	public Component getComponentModel() {
		return componentModel;
	}

	@Override
	public void setComponentModel(Component componentModel) {
		this.componentModel = componentModel;
	}

	@Override
	public ControllerStatus getStatus() {
		return status;
	}

	public void setStatus(ControllerStatus status) {
		this.status = status;
		new SicsEventBuilder(EVENT_TOPIC_STATUS_CHANGE, getProxy().getId())
				.append(EVENT_PROP_CONTROLLER, this)
				.append(EVENT_PROP_STATUS, status).post();
	}

	/*************************************************************************
	 * Components
	 *************************************************************************/
	
	@Override
	public ISicsProxy getProxy() {
		if (proxy == null) {
			if (getParent() != null) {
				proxy = getParent().getProxy();
			}
		}
		return proxy;
	}

	@Override
	public void setProxy(ISicsProxy proxy) {
		this.proxy = proxy;
	}
	
	/*************************************************************************
	 * Structures
	 *************************************************************************/
	
	@Override
	public ISicsController getParent() {
		return parent;
	}

	@Override
	public void setParent(ISicsController parent) {
		this.parent = parent;
	}

	@Override
	public ISicsController getChild(String childControllerId) {
		return childMap.get(childControllerId);
	}
	
	@Override
	public ISicsController[] getChildren() {
		return childMap.values().toArray(new ISicsController[childMap.size()]);
	}

	@Override
	public void addChild(ISicsController child) {
		if (child == null || child.getId() == null) {
			return;
		}
		child.setParent(this);
		childMap.put(child.getId(), child);
	}

	@Override
	public void removeChild(ISicsController child) {
		if (child == null || child.getId() == null) {
			return;
		}
		child.setParent(null);
		childMap.remove(child.getId());
	}
	
	@Override
	public ISicsController findChild(String relativePath) {
		List<String> tokens = StringUtils.split(relativePath, "/");
		ISicsController currentController = this;
		for (String controllerId : tokens) {
			if (StringUtils.isEmpty(controllerId)) {
				continue;
			}
			currentController = currentController.getChild(controllerId);
			if (currentController == null) {
				break;
			}
		}
		if (currentController != null) {
			return currentController;
		} else {
			return null;
		}
	}
	
	@Override
	public ISicsController findChildByDeviceId(String deviceId) {
		ISicsController result = null;
		List<ISicsController> selectedControllers = select(
				with(childMap).values(),
				having(on(ISicsController.class).getDeviceId(),
						equalTo(deviceId)));
		if (selectedControllers.size() > 0) {
			return selectedControllers.get(0);
		} else {
			for (ISicsController child : childMap.values()) {
				result = child.findChildByDeviceId(deviceId);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}
	
	/*************************************************************************
	 * Object life cycle
	 *************************************************************************/
	
	@Override
	@PreDestroy
	public void disposeObject() {
		if (childMap != null) {
			childMap.clear();
			childMap = null;
		}
		proxy = null;
		componentModel = null;
		status = null;
	}
	
	/*************************************************************************
	 * Utilities
	 *************************************************************************/

	protected ToStringHelper getToStringHelper() {
		ToStringHelper toStringHelper = Objects.toStringHelper(this).add("id", getId())
				.add("deviceId", getDeviceId())
				.add("component", getComponentModel())
				.add("path", getPath());
		if (getParent() != null) {
			toStringHelper.add("parent", getParent().getId());
		} else {
			toStringHelper.add("parent", null);
		}
		if (getChildren() != null) {
			String children = StringUtils.formatArray(getChildren(),
					new IStringProvider<ISicsController>() {
						@Override
						public String asString(ISicsController controller) {
							return controller.getId();
						}
					}); 
			toStringHelper.add("children", children);
		} else {
			toStringHelper.add("children", null);
		}
		return toStringHelper;
	}
	
	@Override
	public String toString() {
		return getToStringHelper().toString();
	}

}
