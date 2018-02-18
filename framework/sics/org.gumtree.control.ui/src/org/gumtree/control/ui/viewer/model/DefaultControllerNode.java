package org.gumtree.control.ui.viewer.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.events.ISicsControllerListener;
import org.gumtree.control.model.PropertyConstants.ComponentType;
import org.gumtree.control.model.PropertyConstants.ControllerState;
import org.gumtree.control.model.ModelUtils;
import org.gumtree.control.ui.internal.Activator;
import org.gumtree.control.ui.viewer.ControlViewerConstants.Column;
import org.gumtree.ui.util.jface.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultControllerNode extends TreeNode implements ISicsTreeNode {

	private static Image groupImage;

	private static Image grahicGroupImage;

	private static Image instrumentGroupImage;

	private static Image scriptContextImage;
	
	private static Logger logger = LoggerFactory.getLogger(DefaultControllerNode.class);
	
	static {
		if(Activator.getDefault() != null) {
			groupImage = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/obj16/package_obj.gif").createImage();
			grahicGroupImage = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/elcl16/javaassist_co.gif").createImage();
			instrumentGroupImage = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/obj16/debugt_obj.gif").createImage();
			scriptContextImage = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/obj16/native_lib_path_attrib.gif").createImage();
		}
	}

	private Color runningBgColour;
	
	private Color errorBgColour;
	
	private Color statusFgColour;
	
	private Image image;

	private List<ISicsTreeNode> children;
	
	private Map<String, Boolean> visibilityMap;
	
	private ControllerListener listener;

	private INodeSet nodeSet;
	
	public DefaultControllerNode(ISicsController controller) {
		super(controller);
		listener = new ControllerListener();
		controller.addControllerListener(listener);
		runningBgColour = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN);
		statusFgColour = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
		errorBgColour = Display.getDefault().getSystemColor(SWT.COLOR_RED);
	}

	public ISicsTreeNode[] getChildren() {
		if(children == null) {
			children = new ArrayList<ISicsTreeNode>();
			for(ISicsController childController : getController().getChildren()) {
//				ISicsTreeNode childNode = (ISicsTreeNode)Platform.getAdapterManager().getAdapter(childController, ISicsTreeNode.class);
				ISicsTreeNode childNode = (ISicsTreeNode)ControllerNodeFactory.getControllerNode(childController, ISicsTreeNode.class);
				childNode.setNodeSet(getNodeSet());
				// Pass on the visibility
				childNode.setVisibilityMap(getVisibilityMap());
				if(childNode != null && childNode.isVisible()) {
					childNode.setViewer(getViewer());
					children.add(childNode);
				}
			}
		}
		return children.toArray(new ISicsTreeNode[children.size()]);
	}

	public ISicsController getController() {
		return (ISicsController) getOriginalObject();
	}
	
	public Image getColumnImage(int columnIndex) {
		if(columnIndex == 0) {
			if(image == null) {
				ComponentType type = ModelUtils.getComponentType(getController().getModel());
				if(type != null) {
					if(type.equals(ComponentType.GRAPH_SET)) {
						image = grahicGroupImage;
					} else if(type.equals(ComponentType.INSTRUMENT)) {
						image = instrumentGroupImage;
					} else if (type.equals(ComponentType.SCRIPT_CONTEXT_OBJECT)) {
						image = scriptContextImage;
					} else {
						image = groupImage;
					}
				} else {
					image = groupImage;
				}
			}
			return image;
		}
		return null;
	}

	public String getColumnText(int columnIndex) {
		if (columnIndex == 0) {
			if (getNodeSet() != null && getNodeSet().hasAlias(getController())) {
				return getNodeSet().getAlias(getController());
			} else {
				return getController().getId();
			}
		} else if (columnIndex == Column.STATUS.getIndex()) {
			if (getController().getState().equals(ControllerState.BUSY)) {
				return "busy";
			} else if (getController().getState().equals(ControllerState.ERROR)) {
				return "error";
			}
		}
		return EMPTY_STRING;
	}

	public Color getColumnBackground(int columnIndex) {
		if (columnIndex == Column.STATUS.getIndex()) {
			if (getController().getState().equals(ControllerState.BUSY)) {
				return runningBgColour;
			} else if (getController().getState().equals(ControllerState.ERROR)) {
				return errorBgColour;
			}
		}
		return null;
	}
	
	public Color getColumnForeground(int columnIndex) {
		if (columnIndex == Column.STATUS.getIndex()) {
			if (getController().getState().equals(ControllerState.BUSY) ||
					(getController().getState().equals(ControllerState.ERROR))) {
				return statusFgColour;
			}
		}
		return null;
	}
	
	public Map<String, Boolean> getVisibilityMap() {
		return visibilityMap;
	}
	
	public void setVisibilityMap(Map<String, Boolean> visibilityMap) {
		this.visibilityMap = visibilityMap;
		if (visibilityMap != null) {
			Boolean visibility = visibilityMap.get(getController().getPath());
			if (visibility != null) {
				logger.debug("Set visibility: " + getController().getPath() + " = " + visibility);
				setVisible(visibility);
			}
		}
	}
	
	public void dispose() {
		if (children != null) {
			for (ISicsTreeNode node : children) {
				node.dispose();
			}
			children.clear();
			children = null;
		}
		getController().removeControllerListener(listener);
		super.dispose();
	}
	
	public String toString() {
		return "[DefaultControllerNode] : " + getController().getPath();
	}
	
	private class ControllerListener implements ISicsControllerListener {

		@Override
		public void updateState(ControllerState oldState, ControllerState newState) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (getViewer() != null && !getViewer().getControl().isDisposed()) {
						getViewer().refresh(DefaultControllerNode.this);
					}
				}
			});
		}

		@Override
		public void updateValue(Object oldValue, Object newValue) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void updateEnabled(boolean isEnabled) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void updateTarget(Object oldValue, Object newValue) {
			// TODO Auto-generated method stub
			
		}
	}

	public INodeSet getNodeSet() {
		return nodeSet;
	}

	public void setNodeSet(INodeSet nodeSet) {
		this.nodeSet = nodeSet;	
	}

	public String getPath() {
		return getController().getPath();
	}
	
}
