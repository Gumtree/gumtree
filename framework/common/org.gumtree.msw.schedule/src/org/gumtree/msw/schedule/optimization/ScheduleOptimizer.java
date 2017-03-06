package org.gumtree.msw.schedule.optimization;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.ElementList;
import org.gumtree.msw.schedule.ScheduledNode;
import org.gumtree.msw.schedule.Scheduler;

public class ScheduleOptimizer {
	// fields
	private final Scheduler scheduler;
	private final Set<Class<?>> sortableLists;

	// construction
	public ScheduleOptimizer(Scheduler scheduler, Class<?> ... sortableLists) {
		for (Class<?> clazz : sortableLists)
			if (!ElementList.class.isAssignableFrom(clazz))
				throw new IllegalArgumentException();
		
		this.scheduler = scheduler;
		this.sortableLists = asSet(sortableLists);
	}
	
	// methods
	public List<Element> analyze() {
		return new Analyzer(sortableLists, scheduler.getRoot()).result();
	}
	public void optimize(Iterable<Element> targets) {
		for (Element element : targets)
			if (!sortableLists.contains(element.getClass()))
				throw new IllegalArgumentException();
		
		new Optimizer(targets, scheduler.getRoot());
	}

	// helper
	static <T> Set<T> asSet(T[] items) {
		Set<T> set = new HashSet<>();
		for (T item : items)
			set.add(item);
		return set;
	}
	static boolean allDisabled(Iterable<ScheduledNode> nodes) {
		for (ScheduledNode node : nodes)
			if (node.isEnabled())
				return false;
			
		return true;
	}
}
