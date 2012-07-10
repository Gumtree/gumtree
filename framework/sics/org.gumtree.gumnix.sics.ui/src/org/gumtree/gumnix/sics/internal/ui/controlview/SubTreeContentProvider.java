package org.gumtree.gumnix.sics.internal.ui.controlview;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.Platform;
import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.control.ISicsController;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.ui.controlview.EntryType;
import org.gumtree.gumnix.sics.ui.controlview.IFilterEntry;
import org.gumtree.gumnix.sics.ui.controlview.INodeSet;
import org.gumtree.gumnix.sics.ui.util.ISicsTreeNode;
import org.gumtree.ui.util.jface.ITreeNode;

// Like Flat structure, but we also provide child under each flat node
public class SubTreeContentProvider extends ControlViewerContentProvider {

	private List<ISicsTreeNode> visibleControllers;

	public SubTreeContentProvider(INodeSet filter) {
		super(filter);
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ITreeNode) {
			ITreeNode parentNode = (ITreeNode)parentElement;
			return parentNode.getChildren();
		}
		return EMPTY_ARRAY;
	}
	
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof ISicsController) {
			if (visibleControllers == null) {
				ISicsController sicsControllerNode = (ISicsController) inputElement;
			
				Map<String, ISicsTreeNode> initialList = new ConcurrentHashMap<String, ISicsTreeNode>();
				for (IComponentController controller : sicsControllerNode.getComponentControllers()) {
					// Recursively find visible controllers
					processVisibility(controller, initialList);
				}
				
				// Rearrange order
				visibleControllers = new ArrayList<ISicsTreeNode>();
				for (IFilterEntry entry : getFilter().getEntries()) {
					if (entry.getType().equals(EntryType.INCLUDE)) {
						for (Entry<String, ISicsTreeNode> nodeEntry : initialList.entrySet()) {
							if (nodeEntry.getKey().startsWith(entry.getEntry())) {
								visibleControllers.add(nodeEntry.getValue());
								initialList.remove(nodeEntry.getKey());
							}
						}
					}
				}
				// Handle left over
				visibleControllers.addAll(initialList.values());
			}
			
			// Return result
			return visibleControllers.toArray(new ISicsTreeNode[visibleControllers.size()]);
		}
		return EMPTY_ARRAY;
	}

	private void processVisibility(IComponentController controller, Map<String, ISicsTreeNode> buffer) {
		if (getFilter().isVisible(controller)) {
			ISicsTreeNode node = (ISicsTreeNode)Platform.getAdapterManager().getAdapter(controller, ISicsTreeNode.class);
			node.setNodeSet(getFilter());
			node.setViewer(getViewer());
			buffer.put(node.getPath(), node);
		} else {
			// Only continue to search visible sub elements if the current node is not visible 
			for (IComponentController child : controller.getChildControllers()) {
				processVisibility(child, buffer);
			}
		}
	}
	
	public void componentStatusChanged(ControllerStatus newStatus) {
		// Do nothing??
	}

	public void dispose() {
		if (visibleControllers != null) {
			for (ISicsTreeNode node : visibleControllers) {
				node.dispose();
			}
			visibleControllers.clear();
			visibleControllers = null;
		}
		super.dispose();
	}
	
}
