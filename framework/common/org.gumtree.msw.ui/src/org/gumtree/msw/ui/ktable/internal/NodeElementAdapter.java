package org.gumtree.msw.ui.ktable.internal;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IElementListener;
import org.gumtree.msw.schedule.INodeListener;
import org.gumtree.msw.schedule.ScheduledNode;

public class NodeElementAdapter implements IElementAdapter {
	// fields
	private final NodeListener nodeListener;
	
	// construction
	public NodeElementAdapter(ScheduledNode node) {
		nodeListener = new NodeListener(node);
	}
	
	// methods
	@Override
	public Object get(IDependencyProperty property) {
		return nodeListener.getNode().get(property);
	}
	@Override
	public boolean validate(IDependencyProperty property, Object newValue) {
		return nodeListener.getNode().validate(property, newValue);
	}
	
	// listeners
	@Override
	public void addPropertyListener(IElementListener listener) {
		nodeListener.addPropertyListener(listener);
	}
	@Override
	public boolean removePropertyListener(IElementListener listener) {
		return nodeListener.removePropertyListener(listener);
	}
	
	// helper
	private static class NodeListener implements INodeListener {
		// fields
		private final ScheduledNode node;
		private final List<IElementListener> listeners;
		
		// construction
		public NodeListener(ScheduledNode node) {
			this.node = node;
			this.listeners = new ArrayList<>();
		}
		
		// properties
		public ScheduledNode getNode() {
			return node;
		}
		
		// methods
		public void addPropertyListener(IElementListener listener) {
			if (listeners.size() == 0)
				node.addListener(this);
			else if (listeners.contains(listener))
				throw new Error("listener already exists");
			
			try {
				listeners.add(listener);
			}
			finally {
				if (listeners.size() == 0)
					node.removeListener(this);
			}
		}
		public boolean removePropertyListener(IElementListener listener) {
			try {
				return listeners.remove(listener);
			}
			finally {
				// ensure that this NodeListener is removed from the target node
				// after all listeners have been removed
				if (listeners.size() == 0)
					node.removeListener(this);
			}
		}
		// listening
		@Override
		public void onChangedProperty(ScheduledNode owner, IDependencyProperty property, Object oldValue, Object newValue) {
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
