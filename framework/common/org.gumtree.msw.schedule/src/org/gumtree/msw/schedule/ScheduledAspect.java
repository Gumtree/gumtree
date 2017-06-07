package org.gumtree.msw.schedule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.IDependencyProperty;

public class ScheduledAspect {
	// fields
	private ScheduledAspect parent = null;
	private boolean isDisposed = false;
	private final ScheduledNode node;
	private final Map<ScheduledNode, ScheduledAspect> links;
	 // only applicable for last level
	private boolean isAcquisitionAspect;
	// listening support
	private final List<IAspectListener> listeners = new ArrayList<>();
	
	// construction
	public ScheduledAspect(AcquisitionAspect acquisitionAspect, Element sourceElement) {
		this(new ScheduledNode(
				null,
				sourceElement,
				acquisitionAspect.properties,
				acquisitionAspect.entries));
	}
	private ScheduledAspect(ScheduledAspect reference) {
		this(reference.node.clone());

		// this clone cannot set acquisition details, because it doesn't know its parent yet
		
		// clone links
		List<ScheduledNode> referenceLeafNodes = new ArrayList<>();
		List<ScheduledNode> newLeafNodes = new ArrayList<>();
		collectLeafNodes(reference.node, referenceLeafNodes);
		collectLeafNodes(node, newLeafNodes);

		for (int i = 0, n = referenceLeafNodes.size(); i != n; i++) {
			ScheduledAspect subReference = reference.links.get(referenceLeafNodes.get(i));
			if (subReference != null) {
				ScheduledNode leafNode = newLeafNodes.get(i);
				ScheduledAspect subAspect = new ScheduledAspect(subReference);
				subAspect.setParent(this);
				
				links.put(leafNode, subAspect);
			}
		}
	}
	private ScheduledAspect(ScheduledNode node) {
		this.node = node;
		this.links = new HashMap<>();
		this.isAcquisitionAspect = false;
		
		node.addListener(new BranchNodeListener(this));
	}
	public void dispose() {
		if (isDisposed)
			return;

		node.dispose();
		
		parent = null;
		isDisposed = true;
	}
	
	// properties
	public boolean isDisposed() {
		return isDisposed;
	}
	public ScheduledNode getNode() {
		return node;
	}
	public boolean hasLeafNodes() {
		return !links.isEmpty();
	}
	public ScheduledNode getFirstLeafNode() {
		ScheduledNode first = findFirstLeafNode(node);

		if ((first != null) && !first.isVisible() && node.isVisible()) {
			// visible leaf node has priority
			ScheduledNode visible = findFirstLeafNodeVisible(node);
			if (visible != null)
				return visible;
		}
		
		return first;
	}
	public Set<ScheduledNode> getLeafNodes() {
		return links.keySet();
	}
	public Iterable<ScheduledAspect> getLinks() {
		return links.values();
	}
	public ScheduledAspect getLinkAt(ScheduledNode leafNode) {
		return links.get(leafNode);
	}
	public void setLinkAt(ScheduledNode leafNode, ScheduledAspect value) {
		setLinkAt(leafNode, value, false);
	}
	public void setLinkAt(ScheduledNode leafNode, ScheduledAspect value, boolean updateAcquisitionDetailProvider) {
		if (!links.containsKey(leafNode))
			throw new Error("doesn't contain leaf node");

		links.put(leafNode, value);
		if (value != null) {
			value.setParent(this);
			if (updateAcquisitionDetailProvider)
				value.resetAcquisitionDetailProvider(leafNode.getAcquisitionDetailProvider());
		}
	}
	public ScheduledAspect getParent() {
		return parent;
	}
	public void setParent(ScheduledAspect value) {
		parent = value;
	}
	public ScheduledNode getLeafNode(ScheduledAspect follower) {
		for (Entry<ScheduledNode, ScheduledAspect> entry : links.entrySet())
			if (entry.getValue() == follower)
				return entry.getKey();

		return null;
	}
	
	// methods
	public void fetchVisibleLinks(Collection<ScheduledAspect> aspects) {
		for (Entry<ScheduledNode, ScheduledAspect> entry : links.entrySet())
			if (entry.getKey().isVisible())
				aspects.add(entry.getValue());
	}
	@Override
	public ScheduledAspect clone() {
		if (isDisposed)
			throw new Error("already disposed");
		
		return new ScheduledAspect(this);
	}
	public void enableAll() {
		if (isDisposed)
			throw new Error("already disposed");
		
		node.enableAll();
		
		for (ScheduledAspect link : links.values())
			if (link != null)
				link.enableAll();
	}
	public void disableAll() {
		if (isDisposed)
			throw new Error("already disposed");

		node.disableAll();
		
		for (ScheduledAspect link : links.values())
			if (link != null)
				link.disableAll();
	}
	// acquisition details
	public void resetAcquisitionDetailProvider(AcquisitionDetailProvider acquisitionDetailProvider) {
		node.resetAcquisitionDetailProvider(acquisitionDetailProvider);

		for (Entry<ScheduledNode, ScheduledAspect> entry : links.entrySet()) {
			ScheduledAspect aspect = entry.getValue();
			if (aspect != null)
				aspect.resetAcquisitionDetailProvider(entry.getKey().getAcquisitionDetailProvider());
		}
	}
	public void updateAcquisitionState(boolean isAcquisitionAspect) {
		if (this.isAcquisitionAspect == isAcquisitionAspect)
			return;
		
		this.isAcquisitionAspect = isAcquisitionAspect;
		for (ScheduledNode node : links.keySet())
			node.updateAcquisitionState(isAcquisitionAspect);
	}
	// event handling
	private void onAddedSubNode(ScheduledNode subNode) {
		// find leaf nodes
		List<ScheduledNode> leafNodes = new ArrayList<>();
		collectLeafNodes(subNode, leafNodes);

		// add new links
		for (ScheduledNode leafNode : leafNodes) {
			// add link
			if (links.put(leafNode, null) != null)
				throw new Error("leaf node already registered");
		}

		// call listeners
		if (!leafNodes.isEmpty())
			for (IAspectListener listener : listeners)
				listener.onAddedLinks(this, leafNodes);
	}
	private void onDeletedSubNode(ScheduledNode subNode) {
		// find leaf nodes
		List<ScheduledNode> leafNodes = new ArrayList<>();
		collectLeafNodes(subNode, leafNodes);
		
		// delete old links
		List<ScheduledAspect> aspects = new ArrayList<>();
		for (ScheduledNode leafNode : leafNodes) {
			ScheduledAspect aspect = links.remove(leafNode);
			if (aspect != null)
				aspects.add(aspect);
		}

		// call listeners
		if (!aspects.isEmpty())
			for (IAspectListener listener : listeners)
				listener.onDeletedLinks(this, aspects);
	}
	private void onDuplicatedNode(ScheduledNode original, ScheduledNode duplicate) {
		// create leaf node pairs
		List<ScheduledNode> originalLeafNodes = new ArrayList<>();
		List<ScheduledNode> duplicateLeafNodes = new ArrayList<>();
		collectLeafNodes(original, originalLeafNodes);
		collectLeafNodes(duplicate, duplicateLeafNodes);
		
		List<IDuplicated<ScheduledNode>> pairs = new ArrayList<>();
		for (int i = 0, n = originalLeafNodes.size(); i != n; i++) {
			ScheduledNode originalLeafNode = originalLeafNodes.get(i);
			ScheduledNode duplicateLeafNode = duplicateLeafNodes.get(i);
			
			pairs.add(new Duplicated(originalLeafNode, duplicateLeafNode));

			// add link
			if (links.put(duplicateLeafNode, null) != null)
				throw new Error("leaf node already registered");
		}

		// call listeners
		if (!pairs.isEmpty())
			for (IAspectListener listener : listeners)
				listener.onDuplicatedLinks(this, pairs);
	}
	
	// helpers
	private ScheduledNode findFirstLeafNode(ScheduledNode node) {
		if (node.isAspectLeaf())
			return node;

		for (ScheduledNode subNode : node.getNodes()) {
			ScheduledNode leafNode = findFirstLeafNode(subNode);
			if (leafNode != null)
				return leafNode;
		}

		return null;
	}
	private ScheduledNode findFirstLeafNodeVisible(ScheduledNode node) {
		if (!node.isVisible())
			return null;
		
		if (node.isAspectLeaf())
			return node;

		for (ScheduledNode subNode : node.getNodes()) {
			ScheduledNode leafNode = findFirstLeafNodeVisible(subNode);
			if (leafNode != null)
				return leafNode;
		}

		return null;
	}
	private void collectLeafNodes(ScheduledNode node, List<ScheduledNode> leafNodes) {
		if (node.isAspectLeaf())
			leafNodes.add(node);
		else
			for (ScheduledNode subNode : node.getNodes())
				collectLeafNodes(subNode, leafNodes);
	}

	// listeners
	void addListener(IAspectListener listener) {
		if (listeners.contains(listener))
			throw new Error("listener already exists");
		
		listeners.add(listener);
		listener.onInitialize(this);
	}
	boolean removeListener(IAspectListener listener) {
		return listeners.remove(listener);
	}

	private static class BranchNodeListener implements INodeListener {
		// fields
		private final ScheduledAspect aspect;
		private boolean ignoreAddedSubNode = false;
		
		// construction
		public BranchNodeListener(ScheduledAspect aspect) {
			this.aspect = aspect;
		}
		
		// methods
		@Override
		public void onChangedProperty(ScheduledNode owner, IDependencyProperty property, Object oldValue, Object newValue) {
			// ignore
		}
		@Override
		public void onAddedSubNode(ScheduledNode owner, ScheduledNode subNode) {
			// leaf nodes don't have sub-nodes
			if (!subNode.isAspectLeaf()) {
				boolean oldValue = ignoreAddedSubNode;
				try {
					ignoreAddedSubNode = true;
					subNode.addListener(this);
				}
				finally {
					ignoreAddedSubNode = oldValue;
				}
			}
			else if (aspect.isAcquisitionAspect)
				subNode.updateAcquisitionState(true);
			
			if (!ignoreAddedSubNode)
				aspect.onAddedSubNode(subNode);
		}
		@Override
		public void onDeletedSubNode(ScheduledNode owner, ScheduledNode subNode) {
			aspect.onDeletedSubNode(subNode);
		}
		@Override
		public void onDuplicatedNode(ScheduledNode owner, ScheduledNode original, ScheduledNode duplicate) {
			boolean oldValue = ignoreAddedSubNode;
			try {
				ignoreAddedSubNode = true;
				duplicate.addListener(this);
			}
			finally {
				ignoreAddedSubNode = oldValue;
			}
			
			aspect.onDuplicatedNode(original, duplicate);
		}
		// locks
		@Override
		public void onPropertiesLocked() {
			// ignore
		}
		@Override
		public void onOrderLocked() {
			// ignore
		}
		@Override
		public void onUnlocked() {
			// ignore
		}
		// additional
		@Override
		public void onVisibilityChanged(ScheduledNode owner, boolean newValue) {
			// ignore
		}
	}
	
	private static class Duplicated implements IDuplicated<ScheduledNode> {
		// fields
		private final ScheduledNode original;
		private final ScheduledNode duplicate;
		
		// construction
		public Duplicated(ScheduledNode original, ScheduledNode duplicate) {
			this.original = original;
			this.duplicate = duplicate;
		}

		// methods
		@Override
		public ScheduledNode getOriginal() {
			return original;
		}
		@Override
		public ScheduledNode getDuplicate() {
			return duplicate;
		}
	}
}
