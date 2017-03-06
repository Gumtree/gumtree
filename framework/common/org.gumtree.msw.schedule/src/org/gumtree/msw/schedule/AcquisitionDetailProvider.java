package org.gumtree.msw.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IElementListener;

class AcquisitionDetailProvider {
	// fields
	private final Set<IDependencyProperty> properties;
	private final Map<IDependencyProperty, ScheduledNode> sources;
	private final List<IElementListener> listeners;
	private final INodeListener nodeListener;
	
	// construction
	public AcquisitionDetailProvider(Set<IDependencyProperty> properties) {
		this.properties = properties;
		this.sources = new HashMap<>();
		this.listeners = new ArrayList<>();
		this.nodeListener = null;
	}
	public AcquisitionDetailProvider(AcquisitionDetailProvider owner, ScheduledNode source) {
		properties = owner.properties;
		sources = new HashMap<>();
		listeners = new ArrayList<>();
		nodeListener = new NodeListener(sources, listeners);

		// used to check to which nodes a listener has been added
		Set<ScheduledNode> nodesRegistered = new HashSet<>();

		// check if new source provides properties of interest
		for (IDependencyProperty property : source.getProperties())
			if (properties.contains(property)) {
				sources.put(property, source);
				if (!nodesRegistered.contains(source)) {
					nodesRegistered.add(source);
					source.addListener(nodeListener);
				}
			}
		
		// get remaining properties from sources from owner
		for (Entry<IDependencyProperty, ScheduledNode> entry : owner.sources.entrySet()) {
			IDependencyProperty property = entry.getKey();
			ScheduledNode node = entry.getValue();
			if (!sources.containsKey(property)) {
				sources.put(property, node);
				if (!nodesRegistered.contains(node)) {
					nodesRegistered.add(node);
					node.addListener(nodeListener);
				}
			}
		}
	}
	void dispose() {
		Set<ScheduledNode> nodesUnregistered = new HashSet<>();
		for (ScheduledNode source : sources.values())
			if (!nodesUnregistered.contains(source)) {
				nodesUnregistered.add(source);
				source.removeListener(nodeListener);
			}

		sources.clear();
		listeners.clear();
	}
	
	// properties
	public Set<IDependencyProperty> getProperties() {
		return properties;
	}
	public Object get(IDependencyProperty property) {
		ScheduledNode node = sources.get(property); // returns null if map doesn't contain given property
		if (node != null)
			return node.get(property);
		else
			return null;
	}
	public ScheduledNode getNode(IDependencyProperty property) {
		return sources.get(property);
	}

	// methods
	public boolean containsAny(Set<IDependencyProperty> properties) {
		for (IDependencyProperty property : this.properties)
			if (properties.contains(property))
				return true;
		
		return false;
	}
	// listeners
	public void addListener(IElementListener listener) {
		if (listeners.contains(listener))
			throw new Error("listener already exists");
		
		listeners.add(listener);
	}
	public boolean removeListener(IElementListener listener) {
		return listeners.remove(listener);
	}
	
	// node listener
	private static class NodeListener implements INodeListener {
		// fields
		private final Map<IDependencyProperty, ScheduledNode> sources;
		private final List<IElementListener> listeners;
		
		// construction
		NodeListener(Map<IDependencyProperty, ScheduledNode> sources, List<IElementListener> listeners) {
			this.sources = sources;
			this.listeners = listeners;
		}

		// listening
		@Override
		public void onChangedProperty(ScheduledNode owner, IDependencyProperty property, Object oldValue, Object newValue) {
			if (sources.get(property) == owner)
				for (IElementListener listener : listeners)
					listener.onChangedProperty(property, oldValue, newValue);
		}
		// ignored
		@Override
		public void onAddedSubNode(ScheduledNode owner, ScheduledNode subNode) {
		}
		@Override
		public void onDeletedSubNode(ScheduledNode owner, ScheduledNode subNode) {
		}
		@Override
		public void onDuplicatedNode(ScheduledNode owner, ScheduledNode original, ScheduledNode duplicate) {
		}
		@Override
		public void onPropertiesLocked() {
		}
		@Override
		public void onOrderLocked() {
		}
		@Override
		public void onUnlocked() {
		}
		@Override
		public void onVisibilityChanged(ScheduledNode owner, boolean newValue) {
		}
	}
}
