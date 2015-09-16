package org.gumtree.msw.schedule;

import java.util.Set;

public interface ISchedulerListener {
	// methods
	public void onNewRoot(ScheduledAspect root);
	public void onNewLayer(Set<ScheduledAspect> owners);
	public void onDeletedLayer(Set<ScheduledAspect> owners);
	// aspects
	public void onAddedAspects(ScheduledAspect owner, Iterable<ScheduledAspect> aspects);
	public void onDuplicatedAspects(ScheduledAspect owner, Iterable<ScheduledAspect> aspects);
	public void onDeletedAspects(ScheduledAspect owner, Iterable<ScheduledAspect> aspects);
}
