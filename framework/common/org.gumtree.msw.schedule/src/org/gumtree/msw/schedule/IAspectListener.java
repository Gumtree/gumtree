package org.gumtree.msw.schedule;

interface IAspectListener {
	// methods
	public void onInitialize(ScheduledAspect aspect);
	public void onAddedLinks(ScheduledAspect owner, Iterable<ScheduledNode> nodes);
	public void onDuplicatedLinks(ScheduledAspect owner, Iterable<IDuplicated<ScheduledNode>> nodes);
	public void onDeletedLinks(ScheduledAspect owner, Iterable<ScheduledAspect> followers);
}
