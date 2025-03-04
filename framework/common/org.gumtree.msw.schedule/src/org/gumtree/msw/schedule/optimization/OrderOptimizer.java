package org.gumtree.msw.schedule.optimization;

import java.util.HashMap;
import java.util.Map;

import org.gumtree.msw.elements.Element;
import org.gumtree.msw.schedule.ScheduledAspect;
import org.gumtree.msw.schedule.ScheduledNode;

class OrderOptimizer {
	// fields
	private final Map<Element, SortState> targets;
	
	// construction
	public OrderOptimizer(Iterable<Element> targets) {
		this.targets = new HashMap<>();
		for (Element element : targets)
			this.targets.put(element, new SortState());
	}

	// methods
	public void apply(ScheduledAspect aspect) {
		if (aspect != null)
			apply(aspect, aspect.getNode());
	}
	private void apply(ScheduledAspect aspect, ScheduledNode node) {
		if (!node.isEnabled())
			return;
		
		SortState state = targets.get(node.getSourceElement());
		if ((state != null) && !ScheduleOptimizer.allDisabled(node.getNodes())) {
			state.flip();
			state.apply(node);
		}
		
		if (node.isAspectLeaf())
			apply(aspect.getLinkAt(node));
		else
			for (ScheduledNode subNode : node.getNodes())
				apply(aspect, subNode);
	}
}
