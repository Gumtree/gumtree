package org.gumtree.msw.schedule.optimization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gumtree.msw.elements.Element;
import org.gumtree.msw.schedule.ScheduledAspect;
import org.gumtree.msw.schedule.ScheduledNode;

class Analyzer {
	// fields
	private final Set<Class<?>> sortableLists;
	private final Map<Element, SortState> sortStates;
	private final List<Element> result;
	
	// construction
	public Analyzer(Set<Class<?>> sortableLists, ScheduledAspect aspect) {
		this.sortableLists = sortableLists;
		this.sortStates = new HashMap<>();
		this.result = new ArrayList<>();
		
		analyze(aspect);
		
		// deepest element (in schedule tree) should come last
		Collections.reverse(result);
	}
	
	// properties
	public List<Element> result() {
		return result;
	}

	// methods
	private void analyze(ScheduledAspect aspect) {
		if (aspect != null)
			analyze(aspect, aspect.getNode());
	}
	private void analyze(ScheduledAspect aspect, ScheduledNode node) {
		if (!node.isEnabled())
			return;
		
		Element element = node.getSourceElement();
		if (!result.contains(element) && sortableLists.contains(element.getClass())) {				
			SortState state = sortStates.get(element);
			if (state == null)
				sortStates.put(element, state = new SortState());
			
			if (!ScheduleOptimizer.allDisabled(node.getNodes())) {
				state.flip();
				state.update(node, false); // exclude disabled nodes
			}
			
			if (!state.isSorted())
				result.add(element);
		}
		
		if (node.isAspectLeaf())
			analyze(aspect.getLinkAt(node));
		else
			for (ScheduledNode subNode : node.getNodes())
				analyze(aspect, subNode);
	}
}
