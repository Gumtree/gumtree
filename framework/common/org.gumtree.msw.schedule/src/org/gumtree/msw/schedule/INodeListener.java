package org.gumtree.msw.schedule;

import org.gumtree.msw.elements.IDependencyProperty;

public interface INodeListener {
	// methods
	public void onChangedProperty(ScheduledNode owner, IDependencyProperty property, Object oldValue, Object newValue);
	public void onAddedSubNode(ScheduledNode owner, ScheduledNode subNode);
	public void onDeletedSubNode(ScheduledNode owner, ScheduledNode subNode);
	public void onDuplicatedNode(ScheduledNode owner, ScheduledNode original, ScheduledNode duplicate);
	// locks
	public void onPropertiesLocked();
	public void onOrderLocked();
	public void onUnlocked();
	// additional
	public void onVisibilityChanged(ScheduledNode owner, boolean newValue);
}
