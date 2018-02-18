package org.gumtree.control.ui.viewer.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.ISicsModel;
import org.gumtree.control.events.ISicsModelListener;
import org.gumtree.control.model.ModelStatus;
import org.gumtree.control.ui.internal.Activator;
import org.gumtree.control.ui.viewer.ControlViewerConstants.Column;
import org.gumtree.ui.util.jface.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SicsModelNode extends TreeNode implements ISicsTreeNode {

	private static final String LABEL_SICS = "SIC Server";

	private static Image imageServer;

	private static Logger logger = LoggerFactory.getLogger(SicsModelNode.class);
	
	private List<ISicsTreeNode> children;
	
	private Color runningBgColour;
	
	private Color errorBgColour;
	
	private Color statusFgColour;
	
	private ISicsModelListener listener;
	
	private Map<String, Boolean> visibilityMap;
	
	private INodeSet filter; 
	
	static {
		if(Activator.getDefault() != null) {
			imageServer = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/obj16/server.gif").createImage();
		}
	}

	public SicsModelNode(ISicsModel model, INodeSet filter) {
		super(model);
		this.filter = filter;
		createVisibilityMap(filter);
		runningBgColour = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN);
		statusFgColour = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
		errorBgColour = Display.getDefault().getSystemColor(SWT.COLOR_RED);
		listener = new ControllerListener();
		model.addModelListener(listener);
	}
	
	private void createVisibilityMap(INodeSet filter) {
		visibilityMap = new HashMap<String, Boolean>();
		if (filter == null) {
			// everything is visible, so we give no hint to downstream node (ie empty map)
		} else {
			for (ISicsController child : getModel().getSicsControllers()) {
				processVisibility(filter, child, visibilityMap);
			}
		}
		setVisibilityMap(visibilityMap);
	}
	
	// Preprocess the visibility
	// It uses the visibility from both the filter and its children
	private boolean processVisibility(INodeSet filter, ISicsController controller, Map<String, Boolean> visibilityMap) {
		// Assume children is not visible
		boolean isChildrenVisible = false;
		// Find the visibility of the children first
		for (ISicsController child : controller.getChildren()) {
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
			for(ISicsController childController : getModel().getSicsControllers()) {
//				ISicsTreeNode childNode = (ISicsTreeNode)Platform.getAdapterManager().getAdapter(childController, ISicsTreeNode.class);
				ISicsTreeNode childNode = (ISicsTreeNode)ControllerNodeFactory.getControllerNode(childController, ISicsTreeNode.class);
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

	public ISicsModel getModel() {
		return (ISicsModel) getOriginalObject();
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
			if (getModel().getStatus().equals(ModelStatus.RUNNING)) {
				return "running";
			} else if (getModel().getStatus().equals(ModelStatus.ERROR)) {
				return "error";
			}
		}
		return EMPTY_STRING;
	}

	public Color getColumnBackground(int columnIndex) {
		if (columnIndex == Column.STATUS.getIndex()) {
			if (getModel().getStatus().equals(ModelStatus.RUNNING)) {
				return runningBgColour;
			} else if (getModel().getStatus().equals(ModelStatus.ERROR)) {
				return errorBgColour;
			}
		}
		return null;
	}
	
	public Color getColumnForeground(int columnIndex) {
		if (columnIndex == Column.STATUS.getIndex()) {
			if (getModel().getStatus().equals(ModelStatus.RUNNING) ||
					(getModel().getStatus().equals(ModelStatus.ERROR))) {
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
		getModel().removeModelListener(listener);
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

	private class ControllerListener implements ISicsModelListener {
		
		public void statusChanged(ModelStatus newStatus) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (getViewer() != null && !getViewer().getControl().isDisposed()) {
						getViewer().refresh(SicsModelNode.this);
					}
				}
			});
		}

		public void controllerInterrupted() {
		}

		@Override
		public void update(String path, Object oldValue, Object newValue) {
			// TODO Auto-generated method stub
			
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
