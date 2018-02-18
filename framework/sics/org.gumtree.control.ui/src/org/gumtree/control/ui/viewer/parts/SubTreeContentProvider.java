package org.gumtree.control.ui.viewer.parts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.Platform;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.ISicsModel;
import org.gumtree.control.model.ModelStatus;
import org.gumtree.control.ui.viewer.EntryType;
import org.gumtree.control.ui.viewer.model.ControllerNodeFactory;
import org.gumtree.control.ui.viewer.model.IFilterEntry;
import org.gumtree.control.ui.viewer.model.INodeSet;
import org.gumtree.control.ui.viewer.model.ISicsTreeNode;
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
		if (inputElement instanceof ISicsModel) {
			if (visibleControllers == null) {
				ISicsModel sicsControllerNode = (ISicsModel) inputElement;
			
				Map<String, ISicsTreeNode> initialList = new ConcurrentHashMap<String, ISicsTreeNode>();
				for (ISicsController controller : sicsControllerNode.getSicsControllers()) {
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

	private void processVisibility(ISicsController controller, Map<String, ISicsTreeNode> buffer) {
		if (getFilter().isVisible(controller)) {
//			ISicsTreeNode node = (ISicsTreeNode)Platform.getAdapterManager().getAdapter(controller, ISicsTreeNode.class);
			ISicsTreeNode node = (ISicsTreeNode)ControllerNodeFactory.getControllerNode(controller, ISicsTreeNode.class);
			node.setNodeSet(getFilter());
			node.setViewer(getViewer());
			buffer.put(node.getPath(), node);
		} else {
			// Only continue to search visible sub elements if the current node is not visible 
			for (ISicsController child : controller.getChildren()) {
				processVisibility(child, buffer);
			}
		}
	}
	
	public void componentStatusChanged(ModelStatus newStatus) {
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
