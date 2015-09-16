package org.gumtree.msw.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.ElementList;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IElementListListener;
import org.gumtree.msw.elements.IElementPropertyListener;
import org.gumtree.msw.schedule.execution.IScheduleProvider;
import org.gumtree.msw.schedule.execution.ScheduleStep;

public class Scheduler {
	// fields
	private final Set<AcquisitionAspect> acquisitionAspects;
	// root node
	private final ElementList<? extends Element> acquisitionRoot;
	private final IElementListListener<Element> acquisitionRootListener;
	// aspects (element refers to ConfigurationList, SampleList or Environments)
	private final List<Element> elements;
	private final Map<Element, Set<ScheduledAspect>> aspects;
	private final Map<Element, ScheduledAspect> templates;
	// helpers
	private final Map<Element, AspectListener> aspectListeners;
	// listening support
	private final List<ISchedulerListener> listeners = new ArrayList<>();
	// ScheduleProvider
	private ScheduleProvider activeScheduleProvider;
	
	// construction
	public Scheduler(ElementList<? extends Element> acquisitionRoot, AcquisitionAspect ... acquisitionAspects) {
		this.acquisitionAspects = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(acquisitionAspects)));
		this.acquisitionRoot = acquisitionRoot;
		this.acquisitionRootListener = new IElementListListener<Element>() {
			@Override
			public void onAddedListElement(Element element) {
				Scheduler.this.onAddedElement(element);
			}
			@Override
			public void onDeletedListElement(Element element) {
				Scheduler.this.onDeletedElement(element);
			}
		};

		elements = new ArrayList<>();
		aspects = new HashMap<>();
		templates = new HashMap<>();

		aspectListeners = new HashMap<>();
		
		acquisitionRoot.addListListener(acquisitionRootListener);
	}
	public void dispose() {
		acquisitionRoot.removeListListener(acquisitionRootListener);
	}
	
	// properties
	public boolean isOperatable() {
		return getAcquisitionCount() > 0;
	}
	public int getAcquisitionCount() {
		int levels = elements.size();
		if (levels == 0)
			return 0;
		
		// if last layer doesn't have any aspects then it is not operatable
		Element elementLast = elements.get(levels - 1);
		if (aspects.get(elementLast).isEmpty())
			return 0;
		
		// if last layer has (only) open leaf nodes then it is operatable
		Set<ScheduledAspect> set1 = new HashSet<>();
		Set<ScheduledAspect> set2 = new HashSet<>();

		set1.add(getRoot());
		if (set1.contains(null))
			return 0;
		
		for (int i = 1, n = elements.size(); i != n; i++) {
			// fetch visible links
			set2.clear();
			for (ScheduledAspect aspect : set1)
				if (aspect != null)
					aspect.fetchVisibleLinks(set2);
			
			// swap
			Set<ScheduledAspect> temp;
			temp = set1;
			set1 = set2;
			set2 = temp;
		}
		
		int result = 0;
		for (ScheduledAspect aspect : set1)
			for (ScheduledNode node : aspect.getLeafNodes())
				if (node.isVisible())
					result++;
		
		return result;
	}
	public ScheduledAspect getRoot() {
		if (!elements.isEmpty())		
			for (ScheduledAspect first : aspects.get(elements.get(0)))
				return first;
		
		return null;
	}
	public Set<ScheduledAspect> getAspects(Element element) {
		return aspects.get(element);
	}
	public List<Element> getElements() {
		return Collections.unmodifiableList(elements);
	}
	
	// methods
	public IScheduleProvider createScheduleProvider() {
		return new ScheduleProvider(this);
	}
	// listeners
	public void addListener(ISchedulerListener listener) {
		if (listeners.contains(listener))
			throw new Error("listener already exists");
		
		listeners.add(listener);
		listener.onNewRoot(getRoot());
	}
	public boolean removeListener(INodeListener listener) {
		return listeners.remove(listener);
	}
	private void raiseOnNewRoot() {
		ScheduledAspect newRoot = getRoot();
		for (ISchedulerListener listener : listeners)
			listener.onNewRoot(newRoot);
	}
	private void raiseOnNewLayer(int level) {
		Set<ScheduledAspect> owners = aspects.get(elements.get(level - 1));
		for (ISchedulerListener listener : listeners)
			listener.onNewLayer(owners);
	}
	private void raiseOnDeletedLayer(int level) {
		Set<ScheduledAspect> owners = aspects.get(elements.get(level - 1));
		for (ISchedulerListener listener : listeners)
			listener.onDeletedLayer(owners);
	}
	private void raiseOnAddedAspects(ScheduledAspect owner, Iterable<ScheduledAspect> aspects) {
		for (ISchedulerListener listener : listeners)
			listener.onAddedAspects(owner, aspects);
	}
	private void raiseOnDuplicatedAspects(ScheduledAspect owner, Iterable<ScheduledAspect> aspects) {
		for (ISchedulerListener listener : listeners)
			listener.onDuplicatedAspects(owner, aspects);
	}
	private void raiseOnDeletedAspects(ScheduledAspect owner, Iterable<ScheduledAspect> aspects) {
		for (ISchedulerListener listener : listeners)
			listener.onDeletedAspects(owner, aspects);
	}
	
	// event handling
	private void onLevelsChanged() {
		raiseOnNewRoot();
		
		/*
		int n = elements.size();
		int[] order = new int[n];
		
		// used for sorting
		Element[] buffer = null;
		
		// loop is used to handle: 0, 1, [3, 2], 4, [7, 5, 6], 8, ... ///////  e.g.: i0=2, in=4
		while (true) {
			int i0 = -1;
			int in = n;
			for (int i = 0; i != n; i++) {
				int levelNew = elements.get(i).getIndex();
				order[i] = levelNew;

				if (i != levelNew) {
					if (i0 == -1)
						i0 = i;
				}
				else {
					if (i0 != -1) {
						in = i;
						break;
					}
				}
			}
			
			if (i0 == -1)
				return; // nothing left to reorder
			
			// boundaries
			int levelPre = i0 - 1;
			Element elementPre = levelPre >= 0 ? elements.get(levelPre) : null;
			
			int levelNext = in;
			Element elementNext = levelNext < n ? elements.get(levelNext) : null;

			// TODO normalize aspects, i.e. remove duplicated and add deleted nodes
			
			// count leaf aspects
			int q = 1;
			for (int i = i0; i != in; i++)
				q *= templates.get(elements.get(i)).getLinkCount();

			// TODO what if affected layer doesn't have links?
			if (q == 0)
				throw new Error("not implemented");
			
			int p = in - i0;
			int[][] linkList = new int[q][p];
			ScheduledAspect[] leafList = new ScheduledAspect[q];
			
			if (elementPre == null) {
				Element element = elements.get(i0);

				// first element should only have one aspect
				for (ScheduledAspect aspect : aspects.get(element))
					fillStructure(aspect, linkList, leafList, 0, p - 1, 0);
				

				// TODO 
				
			}
			
			
			// TODO 
			
			
			// sort elements
			if (buffer == null)
				buffer = new Element[n];
			
			for (int i = i0; i != in; i++)
				buffer[i] = elements.get(i);

			for (int i = i0; i != in; i++)
				elements.set(i, buffer[order[i]]);
		}
	}
	private static int fillStructure(ScheduledAspect aspect, int[][] linkList, ScheduledAspect[] leafList, int d, int r, int index) {
		
		if (d == r)
			for (int i = 0, n = aspect.getLinkCount(); i != n; i++) {
				linkList[index][d] = i;
				leafList[index] = aspect.getLinkAt(i);
				index++;
			}
		else
			for (int i = 0, n = aspect.getLinkCount(); i != n; i++) {
				int index2 = fillStructure(aspect.getLinkAt(i), linkList, leafList, d + 1, r, index);
				while (index < index2)
					linkList[index++][d] = i;
			}
		
		return index;
	*/
	}
	private void onAddedElement(Element element) {
		// create aspect template for element
		ScheduledAspect template = null;
		String elementName = element.getPath().getElementName();
		for (AcquisitionAspect aspect : acquisitionAspects)
			if (elementName.startsWith(aspect.name)) {
				template = new ScheduledAspect(
					aspect,
					element);
				
				break;
			}
		
		// if element name is unknown 
		if (template == null)
			throw new Error("unknown element");
		
		// tmp list is used to identify index of new element
		// (keep in mind that onAddedElement is not called in order)
		List<Element> tmp = new ArrayList<>(elements);
		tmp.add(element);
		Collections.sort(tmp, Element.INDEX_COMPARATOR);
		int level = tmp.indexOf(element);

		// attach index listener
		element.addPropertyListener(new ElementIndexListener(this, element));

		// the first level only has one ScheduledAspect but any following
		// level may have multiple depending on the tree structure
		Set<ScheduledAspect> set = new HashSet<>();
		
		// listener for new aspects
		AspectListener listener = new AspectListener(this, element);
		
		if (level == 0) {
			// create new root layer
			ScheduledAspect aspectNew = template.clone();
			
			if (!elements.isEmpty()) {
				// the new aspect hasn't been inserted yet
				int levelNext = 0;
				Element elementNext = elements.get(levelNext);
				
				// first element should only have one aspect
				for (ScheduledAspect aspectNext : aspects.get(elementNext))
					insertAspect(aspectNew, elementNext, levelNext, aspectNext);
			}
			
			aspectNew.addListener(listener);
			set.add(aspectNew);
		}
		else {
			// the new layer hasn't been inserted yet
			int levelPre = level - 1;
			Element elementPre = elements.get(levelPre);
			
			int levelNext = level;
			Element elementNext = levelNext < elements.size() ? elements.get(levelNext) : null;
			
			// insert new followers into an existing layer
			for (ScheduledAspect aspect : aspects.get(elementPre))
				for (ScheduledNode node : aspect.getLeafNodes()) {
					// create new follower
					ScheduledAspect aspectNew = template.clone();

					// override existing follower (if elementNext is null, aspect shouldn't have any followers)
					if (elementNext != null) {
						ScheduledAspect aspectNext = aspect.getLinkAt(node);
						insertAspect(aspectNew, elementNext, levelNext, aspectNext);
					}
					aspect.setLinkAt(node, aspectNew);

					aspectNew.addListener(listener);
					set.add(aspectNew);
				}
		}

		elements.add(level, element);
		templates.put(element, template);
		aspects.put(element, set);
		aspectListeners.put(element, listener);
		
		if (level == 0)
			raiseOnNewRoot();
		else
			raiseOnNewLayer(level);
	}
	private void onDeletedElement(Element element) {
		int level = elements.indexOf(element);
		if (level == -1)
			return;

		int levelNext = level + 1;
		Element elementNext = levelNext < elements.size() ? elements.get(levelNext) : null;
		
		if (level == 0) {
			// first element should only have one aspect
			for (ScheduledAspect oldAspect : aspects.get(element)) {
				if (elementNext != null) {
					if (oldAspect.hasLeafNodes()) {
						// pass forward first follower from removed aspect
						ScheduledNode oldNode = oldAspect.getFirstLeafNode();
						oldAspect.getLinkAt(oldNode).setParent(null);

						// set first follower to null, so that it won't be disposed
						oldAspect.setLinkAt(oldNode, null);
					}
					else
						// e.g. if environment had no set-points
						createAspectTreeFromTemplate(elementNext, levelNext);
				}
				
				// dispose old aspect and all its followers
				removeAspectTree(element, level, oldAspect);
			}
		}
		else {
			// re-link
			for (ScheduledAspect aspect : aspects.get(elements.get(level - 1)))
				for (ScheduledNode node : aspect.getLeafNodes()) {
					// since current aspect precedes removed aspects it should always have followers (i.e. removed aspects)
					ScheduledAspect oldAspect = aspect.getLinkAt(node);
					if (elementNext != null)
						if (oldAspect.hasLeafNodes()) {
							// pass forward first follower from removed aspect
							ScheduledNode oldNode = oldAspect.getFirstLeafNode();
							aspect.setLinkAt(node, oldAspect.getLinkAt(oldNode));

							// set first follower to null, so that it won't be disposed
							oldAspect.setLinkAt(oldNode, null);
						}
						else
							// e.g. if environment had no set-points
							aspect.setLinkAt(
									node,
									createAspectTreeFromTemplate(elementNext, levelNext));
					else
						aspect.setLinkAt(node, null);
					
					// dispose old aspect and all its followers
					removeAspectTree(element, level, oldAspect);
				}
		}

		// all aspects for given element should have been disposed
		for (ScheduledAspect oldAspect : aspects.get(element))
			if (!oldAspect.isDisposed())
				throw new Error("not all aspects haven been disposed");
		
		// remove element
		elements.remove(level);
		templates.remove(element);
		aspects.remove(element);

		// first element should always only have one aspect
		if (!elements.isEmpty())
			if (aspects.get(elements.get(0)).size() != 1)
				throw new Error("first element does not contain only one aspect");
		
		if (level == 0)
			raiseOnNewRoot();
		else
			raiseOnDeletedLayer(level);
	}
	// link handling
	private void onAddedLinks(Element element, ScheduledAspect aspect, Iterable<ScheduledNode> nodes) {
		int level = elements.indexOf(element);
		if (level == -1)
			return;

		int levelNext = level + 1;
		if (levelNext < elements.size()) {
			Element elementNext = elements.get(levelNext);
			List<ScheduledAspect> newAspects = new ArrayList<>();

			for (ScheduledNode node : nodes) {
				ScheduledAspect newAspect = createAspectTreeFromTemplate(elementNext, levelNext);
				
				newAspects.add(newAspect);
				aspect.setLinkAt(node, newAspect);
			}
			
			raiseOnAddedAspects(aspect, newAspects);
		}
	}
	private void onDuplicatedLinks(Element element, ScheduledAspect aspect, Iterable<IDuplicated<ScheduledNode>> nodes) {
		int level = elements.indexOf(element);
		if (level == -1)
			return;

		int levelNext = level + 1;
		if (levelNext < elements.size()) {
			Element elementNext = elements.get(levelNext);
			List<ScheduledAspect> newAspects = new ArrayList<>();

			for (IDuplicated<ScheduledNode> node : nodes) {
				ScheduledAspect newAspect = cloneAspectTree(elementNext, levelNext, aspect.getLinkAt(node.getOriginal()));

				newAspects.add(newAspect);
				aspect.setLinkAt(node.getDuplicate(), newAspect);
			}
			
			raiseOnDuplicatedAspects(aspect, newAspects);
		}
	}
	private void onDeletedLinks(Element element, ScheduledAspect aspect, Iterable<ScheduledAspect> followers) {
		int level = elements.indexOf(element);
		if (level == -1)
			return;

		int levelNext = level + 1;
		if (levelNext < elements.size()) {
			Element elementNext = elements.get(levelNext);

			for (ScheduledAspect oldAspect : followers)
				removeAspectTree(elementNext, levelNext, oldAspect);
			
			raiseOnDeletedAspects(aspect, followers);
		}
	}
	// helpers
	private ScheduledAspect createAspectTreeFromTemplate(Element element, int level) {
		ScheduledAspect aspect = templates.get(element).clone();

		int levelNext = level + 1;
		if (levelNext < elements.size()) {
			Element elementNext = elements.get(levelNext);

			for (ScheduledNode node : aspect.getLeafNodes())
				aspect.setLinkAt(
						node,
						createAspectTreeFromTemplate(elementNext, levelNext));
		}

		aspect.addListener(aspectListeners.get(element));
		aspects.get(element).add(aspect);

		return aspect;
	}
	private ScheduledAspect cloneAspectTree(Element element, int level, ScheduledAspect reference) {
		if (reference == null)
			return null;
		
		ScheduledAspect aspect = reference.clone();

		int levelNext = level + 1;
		if (levelNext < elements.size()) {
			Element elementNext = elements.get(levelNext);
			
			for (ScheduledNode node : aspect.getLeafNodes())
				aspect.setLinkAt(
						node,
						cloneAspectTree(elementNext, levelNext, aspect.getLinkAt(node)));
		}
		
		aspect.addListener(aspectListeners.get(element));
		aspects.get(element).add(aspect);

		return aspect;
	}
	private void removeAspectTree(Element element, int level, ScheduledAspect aspect) {
		if (aspect == null)
			return;
		
		int levelNext = level + 1;
		if (levelNext < elements.size()) {
			Element elementNext = elements.get(levelNext);
			
			for (ScheduledNode node : aspect.getLeafNodes()) {
				ScheduledAspect link = aspect.getLinkAt(node);
				if (link != null) {
					removeAspectTree(elementNext, levelNext, link);
					aspect.setLinkAt(node, null);
				}
			}
		}
		
		aspects.get(element).remove(aspect);
		aspect.dispose();
	}
	private void insertAspect(ScheduledAspect aspectNew, Element elementNext, int levelNext, ScheduledAspect aspectNext) {
		if (!aspectNew.hasLeafNodes()) {
			// clear all followers
			removeAspectTree(elementNext, levelNext, aspectNext);
		}
		else {
			// set first link to original aspectNext, but make clones for any following links
			boolean takeOriginal = true;
			for (ScheduledNode node : aspectNew.getLeafNodes())
				if (takeOriginal) {
					takeOriginal = false;
					aspectNew.setLinkAt(node, aspectNext);
				}
				else
					aspectNew.setLinkAt(node, cloneAspectTree(elementNext, levelNext, aspectNext));
		}
	}

	// index listener
	private static class ElementIndexListener implements IElementPropertyListener {
		// fields
		private final Scheduler scheduler;
		private final Element element;
		
		// construction
		private ElementIndexListener(Scheduler scheduler, Element element) {
			this.scheduler = scheduler;
			this.element = element;
		}
		
		// methods
		@Override
		public void onChangedProperty(IDependencyProperty property, Object oldValue, Object newValue) {
			if (property == Element.INDEX) {
				int level = scheduler.elements.indexOf(element);
				if (level == -1)
					return;
				
				if (!Objects.equals(level, newValue))
					scheduler.onLevelsChanged();
			}
		}
	}
	
	// aspect notifications
	private static class AspectListener implements IAspectListener {
		// fields
		private final Scheduler scheduler;
		private final Element element;
		
		// construction
		public AspectListener(Scheduler scheduler, Element element) {
			this.scheduler = scheduler;
			this.element = element;
		}

		// methods
		@Override
		public void onInitialize(ScheduledAspect aspect) {
			// ignore
		}
		@Override
		public void onAddedLinks(ScheduledAspect owner, Iterable<ScheduledNode> nodes) {
			scheduler.onAddedLinks(element, owner, nodes);
		}
		@Override
		public void onDuplicatedLinks(ScheduledAspect owner, Iterable<IDuplicated<ScheduledNode>> nodes) {
			scheduler.onDuplicatedLinks(element, owner, nodes);
		}
		@Override
		public void onDeletedLinks(ScheduledAspect owner, Iterable<ScheduledAspect> followers) {
			scheduler.onDeletedLinks(element, owner, followers);
		}
	}

	// just for testing
	private static class ScheduleProvider implements IScheduleProvider {
		// fields
		private final Scheduler scheduler;
		private final Stack<StackItem> stack;
		// look up
		private final List<Element> elementOrder;
		private Element elementLast;
		
		// construction
		public ScheduleProvider(Scheduler scheduler) {
			this.scheduler = scheduler;
			
			stack = new Stack<>();
			elementOrder = new ArrayList<>();
		}
		
		// methods
		@Override
		public boolean initiate() {
			release();
			
			if (!scheduler.isOperatable())
				return false;
			
			// last element is used to determine if acquisition needs to be started
			elementOrder.addAll(scheduler.elements);
			elementLast = elementOrder.get(elementOrder.size() - 1);
			
			scheduler.activeScheduleProvider = this;
			return true;
		}
		@Override
		public void release() {
			stack.clear();
			elementOrder.clear();
			
			// release all locks
			for (Set<ScheduledAspect> aspectSet : scheduler.aspects.values())
				for (ScheduledAspect aspect : aspectSet)
					aspect.getNode().resetLocks(true); // also resets sub-node locks
		}
		@Override
		public ScheduleStep firstStep() {
			if (!stack.isEmpty())
				throw new Error("call initiate() first");
			if (!scheduler.isOperatable())
				throw new Error("not operatable");
			
			if (!isValid())
				return null;
			
			ScheduledAspect rootAspect = scheduler.getRoot();
			ScheduledNode rootNode = rootAspect.getNode();

			// first iteration
			return createScheduleStep(rootAspect, rootNode, true);
		}
		@Override
		public ScheduleStep nextStep() {
			if (stack.isEmpty())
				throw new Error("call firstStep() first");
			
			if (!isValid())
				return null;
			
			StackItem item = stack.peek();
			
			if (!item.node.isAspectLeaf()) {
				ScheduledNode subNode = item.node.getFirstSubNode();
				if (subNode != null) {
					// iteration
					return createScheduleStep(item.aspect, subNode, item.isEnabled);
				}
			}
			else if (item.aspect.getNode().getSourceElement() != elementLast) {
				ScheduledAspect subAspect = item.aspect.getLinkAt(item.node);
				ScheduledNode subNode = subAspect.getNode();
				
				// iteration
				return createScheduleStep(subAspect, subNode, item.isEnabled);
			}
			
			item.node.lockOrder();
			
			while (true) {
				stack.pop();
				if (stack.isEmpty())
					return null;
				
				StackItem preItem = stack.peek();
				if (!preItem.node.isAspectLeaf()) {
					ScheduledNode nextNode = item.node.getNext();
					if (nextNode != null)
						return createScheduleStep(item.aspect, nextNode, preItem.isEnabled);
				}

				preItem.node.lockOrder();
				
				item = preItem;
			}
		}
		
		// reference function
		@SuppressWarnings("unused")
		public void step(List<ScheduledNode> list) {
			ScheduledAspect rootAspect = scheduler.getRoot();
			ScheduledNode rootNode = rootAspect.getNode();
			
			step(list, rootAspect, rootNode);
		}
		public void step(List<ScheduledNode> list, ScheduledAspect aspect, ScheduledNode node) {
			node.lockProperties();
			list.add(node);
			
			if (!node.isAspectLeaf()) {
				ScheduledNode subNode = node.getFirstSubNode();
				while (subNode != null) {
					step(list, aspect, subNode);
					subNode = subNode.getNext();
				}
			}
			else if (aspect.getNode().getSourceElement() != elementLast) {
				ScheduledAspect subAspect = aspect.getLinkAt(node);
				ScheduledNode subNode = subAspect.getNode();
				
				step(list, subAspect, subNode);
			}
			
			node.lockOrder();
		}
		
		// helper
		private ScheduleStep createScheduleStep(ScheduledAspect aspect, ScheduledNode node, boolean isEnabled) {
			isEnabled &= node.isEnabled();
			
			stack.push(new StackItem(aspect, node, isEnabled));
			node.lockProperties();

			return new ScheduleStep(
					node,
					isEnabled,
					node.isAspectLeaf() && (aspect.getNode().getSourceElement() == elementLast));
		}
		private boolean isValid() {
			if (scheduler.activeScheduleProvider != this)
				return false;
			
			if (elementOrder.isEmpty())
				return false;
			
			List<Element> reference = scheduler.elements;
			if (elementOrder.size() != reference.size())
				return false;
			
			for (int i = 0, n = elementOrder.size(); i != n; i++)
				if (elementOrder.get(i) != reference.get(i))
					return false;
			
			return true;			
		}
		
		private static class StackItem {
			// fields
			public final ScheduledAspect aspect;
			public final ScheduledNode node;
			public final boolean isEnabled;

			// construction
			public StackItem(ScheduledAspect aspect, ScheduledNode node, boolean isEnabled) {
				this.aspect = aspect;
				this.node = node;
				this.isEnabled = isEnabled;
			}
		}
	}
}
