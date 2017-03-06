package org.gumtree.msw.schedule.optimization;

import org.gumtree.msw.schedule.ScheduledNode;

class SortState {
	// fields
	private boolean ascending;
	private boolean descending;
	
	// construction
	public SortState() {
		ascending = true;
		descending = true;
	}

	// properties
	public boolean isSorted() {
		return ascending || descending;
	}
	public boolean isAmbiguous() {
		return ascending && descending;
	}
	
	// methods
	public void flip() {
		boolean tmp = ascending;
		ascending = descending;
		descending = tmp;
	}
	public void update(ScheduledNode node, boolean includeDisabled) {
		int reference = -1;
		for (ScheduledNode subNode : node.getNodes())
			if (subNode.isEnabled() || (includeDisabled && subNode.isVisible()))
				if (reference == -1)
					reference = subNode.getSourceElement().getIndex();
				else {
					int nodeIndex = subNode.getSourceElement().getIndex();
					if (nodeIndex < reference)
						ascending = false;
					if (nodeIndex > reference)
						descending = false;
					reference = nodeIndex;
				}
	}
	public void apply(ScheduledNode node) {
		if (node.getSize() <= 1)
			return;
		
		if (isAmbiguous()) {
			// check current sort state
			SortState state = new SortState();
			state.update(node, false);
			
			if (state.isAmbiguous())
				state.update(node, true);

			if (state.isAmbiguous())
				return; // node doesn't need to be sorted
			
			// assign
			ascending = state.ascending;
			descending = state.descending;
		}
		
		if (descending)
			node.sort(ScheduledNode.DESCENDING_COMPARATOR);
		else {
			// if it is ascending or unsorted, set it to ascending
			node.sort(ScheduledNode.ASCENDING_COMPARATOR);
			ascending = true;
		}
	}
}
