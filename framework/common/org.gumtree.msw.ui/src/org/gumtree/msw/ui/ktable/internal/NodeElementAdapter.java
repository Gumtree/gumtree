package org.gumtree.msw.ui.ktable.internal;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IElementPropertyListener;
import org.gumtree.msw.schedule.INodeListener;
import org.gumtree.msw.schedule.ScheduledNode;

public class NodeElementAdapter implements IElementAdapter {
	// fields
	private final ScheduledNode node;
	private final NodeListener nodeListener;
	
	// construction
	public NodeElementAdapter(ScheduledNode node) {
		this.node = node;
		this.nodeListener = new NodeListener();
		
		node.addListener(nodeListener);
	}
	
	// properties
	public Object get(IDependencyProperty property) {
		return node.get(property);
	}
	
	// listeners
	public void addPropertyListener(IElementPropertyListener listener) {
		nodeListener.addPropertyListener(listener);
	}
	public boolean removePropertyListener(IElementPropertyListener listener) {
		return nodeListener.removePropertyListener(listener);
	}
	
	// helper
	private static class NodeListener implements INodeListener {
		// fields
		private List<IElementPropertyListener> listeners;
		
		// construction
		public NodeListener() {
			listeners = new ArrayList<>();
		}
		
		// methods
		public void addPropertyListener(IElementPropertyListener listener) {
			if (listeners.contains(listener))
				throw new Error("listener already exists");
			
			listeners.add(listener);
		}
		public boolean removePropertyListener(IElementPropertyListener listener) {
			return listeners.remove(listener);
		}
		// listening
		@Override
		public void onChangedProperty(ScheduledNode owner, IDependencyProperty property, Object oldValue, Object newValue) {
			for (IElementPropertyListener listener : listeners)
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
