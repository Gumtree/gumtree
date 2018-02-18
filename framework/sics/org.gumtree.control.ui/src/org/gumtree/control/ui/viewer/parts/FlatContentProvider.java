package org.gumtree.control.ui.viewer.parts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.ISicsModel;
import org.gumtree.control.ui.viewer.EntryType;
import org.gumtree.control.ui.viewer.model.ControllerNodeFactory;
import org.gumtree.control.ui.viewer.model.IFilterEntry;
import org.gumtree.control.ui.viewer.model.INodeSet;
import org.gumtree.control.ui.viewer.model.ISicsTreeNode;

public class FlatContentProvider extends ControlViewerContentProvider {

	private List<ISicsTreeNode> visibleControllers;

	public FlatContentProvider(INodeSet filter) {
		super(filter);
	}

	public Object[] getChildren(Object parentElement) {
		return EMPTY_ARRAY;
	}

	public boolean hasChildren(Object element) {
		// No child for this flat structure
		return false;
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
		}
		for (ISicsController child : controller.getChildren()) {
			processVisibility(child, buffer);
		}
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
