package org.gumtree.msw.schedule.optimization;

import java.util.Set;

import org.gumtree.msw.schedule.ScheduledAspect;
import org.gumtree.msw.schedule.ScheduledNode;

class OrderReseter {
	// fields
	private final Set<Class<?>> sortableLists;
	
	// construction
	public OrderReseter(Set<Class<?>> sortableLists) {
		this.sortableLists = sortableLists;
	}
	
	// methods
	public void apply(ScheduledAspect aspect) {
		if (aspect != null)
			apply(aspect, aspect.getNode());
	}
	private void apply(ScheduledAspect aspect, ScheduledNode node) {
		if (!node.isEnabled())
			return;
		
		if (sortableLists.contains(node.getSourceElement().getClass()) &&
				!ScheduleOptimizer.allDisabled(node.getNodes()))
			node.sort(ScheduledNode.ASCENDING_COMPARATOR);

		if (node.isAspectLeaf())
			apply(aspect.getLinkAt(node));
		else
			for (ScheduledNode subNode : node.getNodes())
				apply(aspect, subNode);
	}
}
