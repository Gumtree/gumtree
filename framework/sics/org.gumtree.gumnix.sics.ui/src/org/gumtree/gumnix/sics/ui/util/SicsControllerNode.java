package org.gumtree.gumnix.sics.ui.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.control.ISicsController;
import org.gumtree.gumnix.sics.control.ISicsControllerListener;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.internal.ui.Activator;
import org.gumtree.gumnix.sics.ui.controlview.INodeSet;
import org.gumtree.gumnix.sics.ui.util.ControlViewerConstants.Column;
import org.gumtree.ui.util.jface.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SicsControllerNode extends TreeNode implements ISicsTreeNode {

	private static final String LABEL_SICS = "SIC Server";

	private static Image imageServer;

	private static Logger logger = LoggerFactory.getLogger(SicsControllerNode.class);
	
	private List<ISicsTreeNode> children;
	
	private Color runningBgColour;
	
	private Color errorBgColour;
	
	private Color statusFgColour;
	
	private ISicsControllerListener listener;
	
	private Map<String, Boolean> visibilityMap;
	
	private INodeSet filter; 
	
	static {
		if(Activator.getDefault() != null) {
			imageServer = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/obj16/server.gif").createImage();
		}
	}

	public SicsControllerNode(ISicsController controller, INodeSet filter) {
		super(controller);
		this.filter = filter;
		createVisibilityMap(filter);
		runningBgColour = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN);
		statusFgColour = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
		errorBgColour = Display.getDefault().getSystemColor(SWT.COLOR_RED);
		listener = new ControllerListener();
		controller.addControllerListener(listener);
	}
	
	private void createVisibilityMap(INodeSet filter) {
		visibilityMap = new HashMap<String, Boolean>();
		if (filter == null) {
			// everything is visible, so we give no hint to downstream node (ie empty map)
		} else {
			for (IComponentController child : getController().getComponentControllers()) {
				processVisibility(filter, child, visibilityMap);
			}
		}
		setVisibilityMap(visibilityMap);
	}
	
	// Preprocess the visibility
	// It uses the visibility from both the filter and its children
	private boolean processVisibility(INodeSet filter, IComponentController controller, Map<String, Boolean> visibilityMap) {
		// Assume children is not visible
		boolean isChildrenVisible = false;
		// Find the visibility of the children first
		for (IComponentController child : controller.getChildControllers()) {
			isChildrenVisible = isChildrenVisible | processVisibility(filter, child, visibilityMap);
		}
		logger.debug("Component visibility: " + controller.getPath() + " = " + filter.isVisible(controller));
		logger.debug("Children  visibility: " + controller.getPath() + " = " + isChildrenVisible);
		
		// We also take account of both node's and children's visibility
		boolean visibility = filter.isVisible(controller) | isChildrenVisible;
		
		logger.debug("Total     visibility: " + controller.getPath() + " = " + visibility);
		
		// Cache result
		visibilityMap.put(controller.getPath(), visibility);
		return visibility;
	}
	
	public ISicsTreeNode[] getChildren() {
		if(children == null) {
			children = new ArrayList<ISicsTreeNode>();
			for(IComponentController childController : getController().getComponentControllers()) {
				ISicsTreeNode childNode = (ISicsTreeNode)Platform.getAdapterManager().getAdapter(childController, ISicsTreeNode.class);
				childNode.setNodeSet(getNodeSet());
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
		return (ISicsController)getOriginalObject();
	}

	public Image getColumnImage(int columnIndex) {
		if(columnIndex == 0) {
			return imageServer;
		}
		return null;
	}

	public String getColumnText(int columnIndex) {
		if(columnIndex == 0) {
			return LABEL_SICS;
		} else if (columnIndex == Column.STATUS.getIndex()) {
			if (getController().getStatus().equals(ControllerStatus.RUNNING)) {
				return "running";
			} else if (getController().getStatus().equals(ControllerStatus.ERROR)) {
				return "error";
			}
		}
		return EMPTY_STRING;
	}

	public Color getColumnBackground(int columnIndex) {
		if (columnIndex == Column.STATUS.getIndex()) {
			if (getController().getStatus().equals(ControllerStatus.RUNNING)) {
				return runningBgColour;
			} else if (getController().getStatus().equals(ControllerStatus.ERROR)) {
				return errorBgColour;
			}
		}
		return null;
	}
	
	public Color getColumnForeground(int columnIndex) {
		if (columnIndex == Column.STATUS.getIndex()) {
			if (getController().getStatus().equals(ControllerStatus.RUNNING) ||
					(getController().getStatus().equals(ControllerStatus.ERROR))) {
				return statusFgColour;
			}
		}
		return null;
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
	
	public Map<String, Boolean> getVisibilityMap() {
		return visibilityMap;
	}
	
	public void setVisibilityMap(Map<String, Boolean> visibilityMap) {
		this.visibilityMap = visibilityMap;
	}
	
	public String toString() {
		return "[SicsControllerNode] : /";
	}

	private class ControllerListener implements ISicsControllerListener {
		
		public void statusChanged(ControllerStatus newStatus) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (getViewer() != null && !getViewer().getControl().isDisposed()) {
						getViewer().refresh(SicsControllerNode.this);
					}
				}
			});
		}

		public void controllerInterrupted() {
		}
		
	}

	public INodeSet getNodeSet() {
		return filter;
	}
	
	public void setNodeSet(INodeSet nodeSet) {
		// Do nothing
		// We can't set more nodeset
	}

	public String getPath() {
		return "/";
	}

}
