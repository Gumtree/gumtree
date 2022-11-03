package org.gumtree.msw.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.ElementList;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IElementListListener;
import org.gumtree.msw.elements.IElementListener;
import org.gumtree.msw.elements.IElementVisitor;

public class ScheduledNode {
	// finals
	public static final Comparator<Integer> ASCENDING_COMPARATOR;
	public static final Comparator<Integer> DESCENDING_COMPARATOR;
	
	// fields
	private boolean isDisposed = false;
	private final ScheduledNode owner;
	// element
	private final Element sourceElement;
	private final Set<IDependencyProperty> properties; // writable properties
	private final Map<IDependencyProperty, Object> values; // TODO values should come from model
	private final Map<IDependencyProperty, Object> defaults;
	private final IDependencyProperty enabledProperty;
	// TODO only applicable for last level (e.g. min/max time and detector counts)
	private Set<IDependencyProperty> readableProperties;
	private Set<IDependencyProperty> modifiableProperties; // writable properties and may include acquisition details
	private boolean ownsAcquisitionDetailProvider = false;
	private AcquisitionDetailProvider acquisitionDetailProvider;	// only nodes that provide requested properties should own a new provider (e.g. Transmission/Scattering)
	private Map<IDependencyProperty, Object> acquisitionValues;		// if a value is not set, get it from acquisitionDetailProvider
	// locked
	private boolean propertiesLocked = false;
	private boolean orderLocked = false;
	private final Map<IDependencyProperty, Object> lockedValues; // TODO values should come from model
	// sub
	private final Set<AcquisitionEntry> acquisitionEntries;
	private final Map<Element, Set<ScheduledNode>> subNodes; // nodes may be duplicated
	private final List<ScheduledNode> listOrder; // TODO order should be loaded from model (via get(path, Element.INDEX))
	private final Map<Element, Integer> elementIndices;
	// listeners
	private SourcePropertyListener propertyListener;
	private SourceListListener listListener;
	// listening support
	private final List<INodeListener> listeners = new ArrayList<>();

	// construction
	static {
		ASCENDING_COMPARATOR = new Comparator<Integer>() {
			@Override
			public int compare(Integer x, Integer y) {
				return Integer.compare(x, y);
			}
		};
		DESCENDING_COMPARATOR = new Comparator<Integer>() {
			@Override
			public int compare(Integer x, Integer y) {
				return Integer.compare(y, x);
			}
		};
	}
	ScheduledNode(ScheduledNode owner, Element sourceElement, Set<IDependencyProperty> properties, Set<AcquisitionEntry> acquisitionEntries) {
		this.owner = owner;
		this.sourceElement = sourceElement;
		this.properties = properties;
		this.readableProperties = sourceElement.getProperties();
		this.modifiableProperties = properties;
		this.acquisitionEntries = acquisitionEntries;
		this.enabledProperty = findEnabledProperty(sourceElement.getProperties());
		
		for (IDependencyProperty property : properties)
			if (!readableProperties.contains(property))
				throw new Error("unknown property");
		
		values = new HashMap<>();
		defaults = new HashMap<>();
		lockedValues = new HashMap<>();
		
		if (acquisitionEntries.isEmpty()) {
			subNodes = null;
			listOrder = null;
			elementIndices = null;
		}
		else {
			subNodes = new HashMap<>();
			listOrder = new ArrayList<>();
			elementIndices = new HashMap<>();
		}
		
		attachListeners();
	}
	private ScheduledNode(ScheduledNode owner, ScheduledNode reference) {
		this.owner = owner;
		sourceElement = reference.sourceElement;
		properties = reference.properties;
		readableProperties = reference.readableProperties;
		modifiableProperties = reference.modifiableProperties;
		acquisitionEntries = reference.acquisitionEntries;
		enabledProperty = reference.enabledProperty;
		
		values = new HashMap<>(reference.values);
		defaults = new HashMap<>(reference.defaults);
		lockedValues = new HashMap<>();

		// reference.isAcquisitionNode
		if (reference.acquisitionValues != null)
			acquisitionValues = new HashMap<>(reference.acquisitionValues); // copy all optional values
		else
			acquisitionValues = null;
		
		if (reference.subNodes == null) {
			subNodes = null;
			listOrder = null;
			elementIndices = null;
		}
		else {
			Map<Element, Set<ScheduledNode>> referenceSubNodes = reference.subNodes;
			List<ScheduledNode> referenceListOrder = reference.listOrder;

			subNodes = new HashMap<>(referenceSubNodes.size());
			listOrder = new ArrayList<>(Collections.<ScheduledNode>nCopies(
					referenceListOrder.size(), null));
			elementIndices = new HashMap<>(reference.elementIndices);
			
			for (Entry<Element, Set<ScheduledNode>> entry : referenceSubNodes.entrySet()) {
				Set<ScheduledNode> subNodeSet = new HashSet<>();
				for (ScheduledNode referenceSubNode : entry.getValue()) {
					int referenceIndex = referenceListOrder.indexOf(referenceSubNode);

					ScheduledNode subNode = new ScheduledNode(this, referenceSubNode);

					subNodeSet.add(subNode);
					listOrder.set(referenceIndex, subNode);
				}
				subNodes.put(entry.getKey(), subNodeSet);
			}
			
			// check that listOrder doesn't contain null
			if (listOrder.contains(null))
				throw new Error("listOrder contains null");
		}

		attachListeners();
	}
	public void dispose() {
		if (isDisposed)
			return;
			
		detachListeners();
		listeners.clear();
		
		if (!isAspectLeaf()) {
			for (ScheduledNode node : listOrder)
				node.dispose();
			
			subNodes.clear();
			listOrder.clear();
			elementIndices.clear();
		}

		if (ownsAcquisitionDetailProvider) {
			acquisitionDetailProvider.dispose();
			ownsAcquisitionDetailProvider = false;
		}
		
		isDisposed = true;
	}
	// helpers
	private static IDependencyProperty findEnabledProperty(Set<IDependencyProperty> properties) {
		for (IDependencyProperty property : properties)
			if (property.matches("enabled", Boolean.class))
				return property;
		
		return null;
	}
	private void attachListeners() {
		if (propertyListener != null)
			throw new Error();

		sourceElement.addElementListener(propertyListener = new SourcePropertyListener(this));
		sourceElement.accept(new IElementVisitor() {
			@Override
			public <TElement extends Element>
			void visit(TElement element) {
				if (!isAspectLeaf())
					throw new Error("branch-node has to be an element list");
			}
			@Override
			public <TElementList extends ElementList<TListElement>, TListElement extends Element>
			void visit(TElementList elementList) {
				if (isAspectLeaf())
					throw new Error("leaf-node cannot be an element list");

				if (listListener != null)
					throw new Error();
				
				elementList.addListListener(listListener = new SourceListListener(ScheduledNode.this));
			}
		});
	}
	private void detachListeners() {
		sourceElement.removeElementListener(propertyListener);
		sourceElement.accept(new IElementVisitor() {
			@Override
			public <TElement extends Element>
			void visit(TElement element) {
				// ignore
			}
			@Override
			public <TElementList extends ElementList<TListElement>, TListElement extends Element>
			void visit(TElementList elementList) {
				elementList.removeListListener(listListener);
			}
		});
	}

	// properties
	public boolean getPropertiesLocked() {
		return propertiesLocked;
	}
	public boolean getOrderLocked() {
		return orderLocked;
	}
	public ScheduledNode getOwner() {
		return owner;
	}
	public ScheduledNode getNext() {
		if (isAspectRoot())
			return null;
		
		List<ScheduledNode> siblings = owner.listOrder;
		int index = siblings.indexOf(this);
		if (index == -1)
			return null;
		
		index++;
		if (index >= siblings.size())
			return null;
		
		return siblings.get(index);
	}
	public ScheduledNode getFirstSubNode() {
		if (isAspectLeaf())
			return null;
		
		if (listOrder.size() == 0)
			return null;
		
		return listOrder.get(0);
	}
	public Element getSourceElement() {
		return sourceElement;
	}
	public Set<IDependencyProperty> getProperties() {
		return readableProperties;
	}
	public Set<IDependencyProperty> getModifiableProperties() {
		return modifiableProperties;
	}
	public int getIndex() {
		return isAspectRoot() ? 0 : owner.listOrder.indexOf(this);
	}
	public int getSize() {
		return isAspectLeaf() ? 0 : listOrder.size();
	}
	public int getVisibleSize() {
		if (isAspectLeaf() || !isVisible())
			return 0;
		
		int result = 0;
		for (ScheduledNode subNode : listOrder)
			if (subNode.isVisible())
				result++;
		return result;
	}
	public boolean isThisVisible() {
		if ((enabledProperty != null) &&
			!Objects.equals(Boolean.TRUE, sourceElement.get(enabledProperty)))
			return false;
		
		return true;
	}
	public boolean isVisible() {
		if (!isThisVisible())
			return false;
		
		return isAspectRoot() || owner.isVisible();
	}
	public boolean isEnabled() {
		return (enabledProperty == null) || (boolean)get(enabledProperty);
	}
	public boolean canBeDisabled() {
		return enabledProperty != null;
	}
	public void setEnabled(Object value) {
		if (canBeDisabled())
			set(enabledProperty, value);
	}
	public boolean isAspectRoot() {
		// AspectRoot cannot be deleted and its index cannot be changed
		return owner == null;
	}
	public boolean isAspectLeaf() {
		return subNodes == null;
	}
	public boolean isModified(IDependencyProperty property) {
		if (property == Element.INDEX)
			return !Objects.equals(
					getIndex(),
					sourceElement.get(property));
		
		if (propertiesLocked)
			return !Objects.equals(
					lockedValues.get(property),
					getDefault(property));
		else
			return values.containsKey(property) ||
					// isAcquisitionNode => all acquisition details can be modified
					(acquisitionValues != null) && acquisitionValues.containsKey(property);
	}
	public Iterable<ScheduledNode> getNodes() {
		return listOrder;
	}
	// acquisition details
	public AcquisitionDetailProvider getAcquisitionDetailProvider() {
		return acquisitionDetailProvider;
	}
	// getter/setter
	public Object get(IDependencyProperty property) {
		if (property == Element.INDEX)
			return owner == null ? 0 : owner.getVisibleIndex(this);
		
		if (propertiesLocked)
			return lockedValues.get(property);
		if (values.containsKey(property))
			return values.get(property);
		if (defaults.containsKey(property))
			return defaults.get(property);

		if (acquisitionValues != null) { // isAcquisitionNode
			if (acquisitionValues.containsKey(property))
				return acquisitionValues.get(property);
			
			ScheduledNode sourceNode = acquisitionDetailProvider.getNode(property);
			if ((sourceNode != null) && (sourceNode != this))
				return sourceNode.get(property);
		}

		return sourceElement.get(property);
	}
	public boolean validate(IDependencyProperty property, Object newValue) {
		if (!modifiableProperties.contains(property))
			return false;
		
		if (property == Element.INDEX)
			return !propertiesLocked && owner.validateIndex(this, newValue);
		
		newValue = property.getPropertyType().cast(newValue);
		
		if (propertiesLocked)
			return Objects.equals(newValue, lockedValues.get(property));
		
		if (sourceElement.getProperties().contains(property))
			return sourceElement.validate(property, newValue);

		if (acquisitionValues != null) { // isAcquisitionNode
			ScheduledNode sourceNode = acquisitionDetailProvider.getNode(property);
			if ((sourceNode != null) && (sourceNode != this))
				return sourceNode.sourceElement.validate(property, newValue);
		}
		
		return false;
	}
	public boolean set(IDependencyProperty property, Object newValue) {
		if (!validate(property, newValue))
			return false;
		
		if (property == Element.INDEX)
			return owner.setIndex(this, newValue);
		
		Object oldValue;
		newValue = property.getPropertyType().cast(newValue);
		
		boolean modified = false;
		if (values.containsKey(property)){
			oldValue = values.put(property, newValue);
			modified = !Objects.equals(newValue, oldValue);
		}
		else if ((acquisitionValues != null) && acquisitionValues.containsKey(property)) {
			oldValue = acquisitionValues.put(property, newValue);
			modified = !Objects.equals(newValue, oldValue);
		}
		else {
			oldValue = getDefault(property);
			modified = !Objects.equals(newValue, oldValue);
			if (modified)
				// if this node is an acquisition node and provides an acquisition detail, then store it in values
				if ((acquisitionValues != null) &&
						(acquisitionDetailProvider.getProperties().contains(property)) &&
						(acquisitionDetailProvider.getNode(property) != this))
					acquisitionValues.put(property, newValue);
				else
					values.put(property, newValue);
		}

		if (modified) {
			// check if property has been "unmodified"
			if (Objects.equals(newValue, getDefault(property))) {
				values.remove(property);
				if (acquisitionValues != null)
					acquisitionValues.remove(property);
			}
			
			raiseOnChangedProperty(this, property, oldValue, newValue);
			return true;
		}
		else
			return false;
	}
	public Object getDefault(IDependencyProperty property) {
		if (defaults.containsKey(property))
			return defaults.get(property);
		
		if (acquisitionValues != null) { // isAcquisitionNode
			ScheduledNode sourceNode = acquisitionDetailProvider.getNode(property);
			if ((sourceNode != null) && (sourceNode != this))
				return sourceNode.get(property);
		}
		
		return sourceElement.get(property);
	}
	public boolean setDefault(IDependencyProperty property, Object newDefault) {
		// defaults for acquisition details cannot be set, that's why properties is used and not modifiableProperties
		if (!properties.contains(property))
			return false;
		if (property == Element.INDEX)
			return false;

		newDefault = property.getPropertyType().cast(newDefault);
		
		Object oldValue = get(property);
		defaults.put(property, newDefault);
		Object newValue = get(property);
		
		if (!Objects.equals(oldValue, newValue))
			raiseOnChangedProperty(this, property, oldValue, newValue);

		return true;
	}
	public boolean clearDefault(IDependencyProperty property) {
		if (!defaults.containsKey(property))
			return false;

		Object oldValue = get(property);
		defaults.remove(property);
		Object newValue = get(property);
		
		if (!Objects.equals(oldValue, newValue))
			raiseOnChangedProperty(this, property, oldValue, newValue);
		
		return true;
	}

	// methods
	public void lockProperties() {
		if (propertiesLocked)
			return;
		
		propertiesLocked = true;
		
		lockedValues.clear();
		lockedValues.putAll(values);
		if (acquisitionValues != null) {
			lockedValues.putAll(acquisitionValues);
			for (IDependencyProperty property : acquisitionDetailProvider.getProperties())
				if (!lockedValues.containsKey(property)) {
					ScheduledNode node = acquisitionDetailProvider.getNode(property);
					if ((node != null) && (node != this))
						lockedValues.put(property, node.get(property));
				}
		}
		
		for (IDependencyProperty property : sourceElement.getProperties())
			if ((property != Element.INDEX) && !lockedValues.containsKey(property))
				lockedValues.put(property, getDefault(property));
		
		for (INodeListener listener : listeners)
			listener.onPropertiesLocked();
	}
	public void lockOrder() {
		if (orderLocked)
			return;
		
		orderLocked = true;
		
		for (INodeListener listener : listeners)
			listener.onOrderLocked();
	}
	public void resetLocks() {
		resetLocks(false);
	}
	public void resetLocks(boolean resetSubNodes) {
		if (!propertiesLocked && !orderLocked)
			return;
		
		propertiesLocked = false;
		orderLocked = false;
		
		try {
			for (IDependencyProperty property : lockedValues.keySet())
				raiseOnChangedProperty(this, property, lockedValues.get(property), get(property));
		}
		finally {
			lockedValues.clear();
		}

		for (INodeListener listener : listeners)
			listener.onUnlocked();
		
		if (resetSubNodes && !isAspectLeaf())
			for (ScheduledNode subNode : listOrder)
				subNode.resetLocks(true);
	}
	@Override
	public ScheduledNode clone() {
		if (isDisposed)
			throw new Error("already disposed");
		
		return new ScheduledNode(owner, this);
	}
	public boolean duplicate() {
		if (isAspectRoot())
			return false;
	
		return owner.duplicate(this);
	}
	public boolean delete() {
		if (propertiesLocked)
			return false;
		
		if (isAspectRoot())
			return false;

		return owner.delete(this);
	}
	public boolean reset(boolean resetSubNodes) {
		if (propertiesLocked)
			return false;
		
		// reset properties if needed
		if (!values.isEmpty()) {
			Map<IDependencyProperty, Object> oldValues = new HashMap<>(values);
			values.clear();
			
			for (IDependencyProperty property : oldValues.keySet())
				raiseOnChangedProperty(this, property, oldValues.get(property), getDefault(property));
		}
		
		if ((acquisitionValues != null) && !acquisitionValues.isEmpty()) {
			Map<IDependencyProperty, Object> oldValues = new HashMap<>(acquisitionValues);
			acquisitionValues.clear();

			for (IDependencyProperty property : oldValues.keySet())
				raiseOnChangedProperty(this, property, oldValues.get(property), acquisitionDetailProvider.get(property));
		}
		
		// a branch-node needs to reset all child-nodes
		if (resetSubNodes)
			if (isAspectLeaf()) {
				// pretend this node is deleted and added, that will create new sub-aspects
				for (INodeListener listener : owner.listeners)
					listener.onDeletedSubNode(owner, this);
				for (INodeListener listener : owner.listeners)
					listener.onAddedSubNode(owner, this);
			}
			else {
				final List<Element> elements = new ArrayList<>(subNodes.keySet());
				Collections.sort(elements, Element.INDEX_COMPARATOR);
				
				// delete all sub nodes
				int i = listOrder.size();
				while (i-- != 0)
					delete(listOrder.get(i));
	
				// rebuild list order
				for (Element element : elements) {
					// each Element needs to link to one ScheduledNode
					ScheduledNode subNode = createSubNode(element);
					
					subNodes.get(element).add(subNode);
					listOrder.add(subNode);
	
					for (INodeListener listener : listeners)
						listener.onAddedSubNode(this, subNode);
				}
			}
		
		return true;
	}
	public void enableAll() {
		setEnabled(true);
		
		if (!isAspectLeaf())
			for (Set<ScheduledNode> subSet : subNodes.values())
				for (ScheduledNode subNode : subSet)
					subNode.enableAll();
	}
	public void disableAll() {
		setEnabled(false);

		if (!isAspectLeaf())
			for (Set<ScheduledNode> subSet : subNodes.values())
				for (ScheduledNode subNode : subSet)
					subNode.disableAll();
	}
	public void setEnabledByPathName(String pathName, boolean isEnabled) {
		System.err.println(getSourceElement().getPath().getElementName());
		if (getSourceElement().getPath().getElementName().startsWith(pathName)){

			setEnabled(isEnabled);

			if (!isAspectLeaf())
				for (Set<ScheduledNode> subSet : subNodes.values())
					for (ScheduledNode subNode : subSet) {
						if (isEnabled) {
							subNode.enableAll();
						} else {
							subNode.disableAll();
						}
					}
		} else {
			if (!isAspectLeaf())
				for (Set<ScheduledNode> subSet : subNodes.values())
					for (ScheduledNode subNode : subSet) {
						subNode.setEnabledByPathName(pathName, isEnabled);
					}
			
		}
	}
	// acquisition details
	public void resetAcquisitionDetailProvider(AcquisitionDetailProvider provider) {
		if (provider == null) {
			throw new Error(); // TODO just for testing
		}
		
		boolean oldOwnsAcquisitionDetailProvider = ownsAcquisitionDetailProvider;
		AcquisitionDetailProvider oldAcquisitionDetailProvider = acquisitionDetailProvider;
		
		if (provider.containsAny(sourceElement.getProperties())) {
			// this node can change acquisition details for sub-nodes (e.g. a transmission-node can change TargetMonitorCounts for all containing leaf nodes)
			acquisitionDetailProvider = new AcquisitionDetailProvider(provider, this);
			ownsAcquisitionDetailProvider = true;
		}
		else {
			acquisitionDetailProvider = provider;
			ownsAcquisitionDetailProvider = false;
		}

		if (!isAspectLeaf())
			for (Set<ScheduledNode> subSet : subNodes.values())
				for (ScheduledNode subNode : subSet)
					subNode.resetAcquisitionDetailProvider(acquisitionDetailProvider);
		
		if (oldOwnsAcquisitionDetailProvider)
			oldAcquisitionDetailProvider.dispose();
	}
	public void updateAcquisitionState(boolean isAcquisitionNode) {
		if (acquisitionDetailProvider == null) {
			throw new Error(); // TODO just for testing
		}
		
		if (isAcquisitionNode == (acquisitionValues != null))
			return; // nothing changes
		
		if (isAcquisitionNode) {
			acquisitionValues = new HashMap<>();
			readableProperties = new HashSet<>(sourceElement.getProperties());
			readableProperties.addAll(acquisitionDetailProvider.getProperties());
			modifiableProperties = new HashSet<>(properties);
			modifiableProperties.addAll(acquisitionDetailProvider.getProperties());

			for (IDependencyProperty property : acquisitionDetailProvider.getProperties())
				if (!properties.contains(property))
					raiseOnChangedProperty(this, property, null, acquisitionDetailProvider.get(property));
		}
		else {
			Map<IDependencyProperty, Object> acquisitionValues = this.acquisitionValues;
			
			this.acquisitionValues = null;
			this.readableProperties = sourceElement.getProperties();
			this.modifiableProperties = properties;
			
			for (IDependencyProperty property : acquisitionDetailProvider.getProperties())
				if (!properties.contains(property))
					if (acquisitionValues.containsKey(property))
						raiseOnChangedProperty(this, property, acquisitionValues.get(property), null);
					else
						raiseOnChangedProperty(this, property, acquisitionDetailProvider.get(property), null);
		}
	}
	// order
	public boolean sort(final Comparator<Integer> comparator) {
		if (orderLocked || propertiesLocked)
			return false;

		// backup old indices (only used for onChangedProperty)
		Map<ScheduledNode, Integer> oldIndices = backupIndices();
		
		// sort
		Collections.sort(listOrder, new Comparator<ScheduledNode>() {
			@Override
			public int compare(ScheduledNode x, ScheduledNode y) {
				return comparator.compare(
						elementIndices.get(x.getSourceElement()),
						elementIndices.get(y.getSourceElement()));
			}
		});

		// notify listeners
		for (int newIndex = 0, n = listOrder.size(); newIndex < n; newIndex++) {
			ScheduledNode changedNode = listOrder.get(newIndex);
			int oldIndex = oldIndices.get(changedNode);
			if (oldIndex != newIndex)
				for (INodeListener listener : changedNode.listeners)
					listener.onChangedProperty(changedNode, Element.INDEX, oldIndex, newIndex);
		}
		
		return true;
	}
	private int getMinIndex() {
		int result = 0;
		if (propertiesLocked)
			for (ScheduledNode node : listOrder)
				if (node.getPropertiesLocked())
					result++;
				else
					break;
		
		return result;
	}
	private int getVisibleIndex(ScheduledNode subNode) {
		int result = 0;
		for (ScheduledNode node : listOrder)
			if (subNode == node)
				return result;
			else if (node.isThisVisible())
				result++;
		
		return -1;
	}
	private boolean validateIndex(ScheduledNode subNode, Object newValue) {
		if (orderLocked)
			return false;
		
		if (!(newValue instanceof Integer))
			return false;
		
		int newIndex = (int)newValue;
		if (newIndex < getMinIndex())
			return false;
		else if (newIndex >= getVisibleSize())
			return false;

		int oldIndex = getVisibleIndex(subNode);
		if (oldIndex == -1)
			return false;
		
		return true;
	}
	private boolean setIndex(ScheduledNode subNode, Object newValue) {
		if (orderLocked)
			return false;
		
		if (!(newValue instanceof Integer))
			return false;
		
		int newIndex = (int)newValue;
		if (newIndex < getMinIndex())
			return false;
		else if (newIndex >= getVisibleSize())
			return false;

		int oldIndex = getVisibleIndex(subNode);
		if (oldIndex == -1)
			return false;
		
		if (oldIndex != newIndex) {
			int oldListIndex = listOrder.indexOf(subNode);
			int newListIndex = -1;
			int visibleIndex = -1; // current visible index
			for (ScheduledNode node : listOrder) {
				newListIndex++;
				if (node.isThisVisible())
					visibleIndex++;
				
				if (visibleIndex == newIndex)
					break;
			}
			
			listOrder.add(newListIndex, listOrder.remove(oldListIndex));
			for (INodeListener listener : subNode.listeners)
				listener.onChangedProperty(subNode, Element.INDEX, oldListIndex, newListIndex);

			int index, indexLast;
			int oldIndexOffset;
			
			if (newListIndex < oldListIndex) {
				index = newListIndex + 1;
				indexLast = oldListIndex;
				oldIndexOffset = -1;
			}
			else {
				index = oldListIndex;
				indexLast = newListIndex - 1;
				oldIndexOffset = +1;
			}

			// update indices
			while (index <= indexLast) {
				ScheduledNode changedNode = listOrder.get(index);
				for (INodeListener listener : changedNode.listeners)
					listener.onChangedProperty(changedNode, Element.INDEX, index + oldIndexOffset, index);
				index++;
			}
		}
		return true;
	}
	private boolean duplicate(ScheduledNode reference) {
		if (orderLocked)
			return false;
		
		int index = listOrder.indexOf(reference);
		if (index == -1)
			return false;
		
		Element element = reference.getSourceElement();
		Set<ScheduledNode> nodeSet = subNodes.get(element);
		
		ScheduledNode subNode = reference.clone();
		subNode.resetAcquisitionDetailProvider(acquisitionDetailProvider);
		
		int minIndex = getMinIndex();
		if (index < minIndex)
			index = minIndex;
		
		nodeSet.add(subNode);
		listOrder.add(index, subNode);

		for (INodeListener listener : listeners)
			listener.onDuplicatedNode(this, reference, subNode);

		index++; // onChangedProperty event is not needed for new element
		int oldIndexOffset = -1;
		
		while (index != listOrder.size()) {
			ScheduledNode changedNode = listOrder.get(index);
			for (INodeListener listener : changedNode.listeners)
				listener.onChangedProperty(changedNode, Element.INDEX, index + oldIndexOffset, index);
			index++;
		}
		
		return true;
	}
	private boolean delete(ScheduledNode subNode) {
		if (orderLocked)
			return false;
		
		int index = listOrder.indexOf(subNode);
		if (index == -1)
			return false;
		
		Element element = subNode.getSourceElement();
		Set<ScheduledNode> nodeSet = subNodes.get(element);

		nodeSet.remove(subNode);
		listOrder.remove(index);

		try {
			for (INodeListener listener : listeners)
				listener.onDeletedSubNode(this, subNode);

			int oldIndexOffset = 1;
			
			while (index != listOrder.size()) {
				ScheduledNode changedNode = listOrder.get(index);
				for (INodeListener listener : changedNode.listeners)
					listener.onChangedProperty(changedNode, Element.INDEX, index + oldIndexOffset, index);
				index++;
			}
		}
		finally {
			subNode.dispose();
		}
		
		return true;
	}
	// event handling
	private void onChangedProperty(IDependencyProperty property, Object oldValue, Object newValue) {
		if (property == Element.INDEX) {
			// e.g. SampleList has no owner, but the source element can change its index
			// which results in reordering of aspects (which is handled in the scheduler)
			if (!propertiesLocked && (owner != null))
				owner.onChangedIndex(this);
		}
		else {
			if (!propertiesLocked &&
				(enabledProperty != null) && (newValue instanceof Boolean) &&
				(enabledProperty == property)) {
				boolean visibility = (boolean)newValue;
				for (INodeListener listener : listeners)
					listener.onVisibilityChanged(this, visibility);
			}

			if (!values.containsKey(property) && !defaults.containsKey(property))
				// even if properties are locked, call listeners so
				// that they know that isModified(property) == true
				if (propertiesLocked)
					oldValue = newValue = get(property);
				
				for (INodeListener listener : listeners)
					listener.onChangedProperty(this, property, oldValue, newValue);
		}
	}
	private void onChangedIndex(ScheduledNode subNode) {
		if (orderLocked) {
			rebuildElementIndices();
			return;
		}
		
		// element that has a new index 
		Element movedElement = subNode.getSourceElement();

		// only perform sorting for first sub-node in the set
		// because onChangedProperty is also called for each duplicate
		// and they all call owner.onChangedIndex(...)
		boolean perform = false; // in case set is empty 
		for (ScheduledNode first : subNodes.get(movedElement)) {
			perform = subNode == first;
			break;
		}
		if (!perform)
			return;
		
		Map<Element, Integer> oldElementIndices = new HashMap<>(elementIndices);
		rebuildElementIndices();
		
		// backup old node indices (only used for onChangedProperty)
		Map<ScheduledNode, Integer> oldIndices = backupIndices();

		// check current order
		if (sort(listOrder, oldElementIndices, elementIndices, ASCENDING_COMPARATOR) ||
			sort(listOrder, oldElementIndices, elementIndices, DESCENDING_COMPARATOR)) {

			// call listeners
			for (int index = 0, n = listOrder.size(); index != n; index++) {
				ScheduledNode changedNode = listOrder.get(index);
				for (INodeListener listener : changedNode.listeners)
					listener.onChangedProperty(changedNode, Element.INDEX, oldIndices.get(changedNode), index);
			}
		}
		else {
			// subNodes are not ordered, therefore ignore new index of source element
		}
	}
	private void onAddedListElement(Element element) {
		// during cloning, subNodes are added before sourceElement.addListListener() is called
		if (subNodes.containsKey(element))
			return;
		
		Set<ScheduledNode> subNodeSet = new HashSet<>();
		subNodes.put(element, subNodeSet);

		rebuildElementIndices();
		
		if (orderLocked)
			return;
		
		ScheduledNode subNode = createSubNode(element);
		subNodeSet.add(subNode);

		// backup old node indices
		Map<ScheduledNode, Integer> oldIndices = backupIndices();
		
		// check current order
		int minIndex = getMinIndex();
		int index;
		if (insertSorted(listOrder, elementIndices, ASCENDING_COMPARATOR, subNode) ||
			insertSorted(listOrder, elementIndices, DESCENDING_COMPARATOR, subNode)) {
			// if insertSorted succeeds then listOrder contains new subNode
			index = listOrder.indexOf(subNode);

			// ensure that element index is valid
			if (index < minIndex) {
				listOrder.remove(index);
				listOrder.add(minIndex, subNode);
			}
		}
		else {
			// subNodes are not ordered
			index = listOrder.size();
			listOrder.add(subNode);
		}

		for (INodeListener listener : listeners)
			listener.onAddedSubNode(this, subNode);

		// onChangedProperty event is not needed for new element
		index++;
		
		while (index != listOrder.size()) {
			ScheduledNode changedNode = listOrder.get(index);
			for (INodeListener listener : changedNode.listeners)
				listener.onChangedProperty(changedNode, Element.INDEX, oldIndices.get(changedNode), index);
			index++;
		}
	}
	private void onDeletedListElement(Element element) {
		Set<ScheduledNode> subNodeSet = subNodes.remove(element);
		rebuildElementIndices();
		
		if ((subNodeSet == null) || subNodeSet.isEmpty())
			return;
		
		// backup old node indices
		Map<ScheduledNode, Integer> oldIndices = backupIndices();

		// find affected nodes
		int i = 0;
		int[] indices = new int[subNodeSet.size()];
		for (ScheduledNode subNode : subNodeSet)
			indices[i++] = listOrder.indexOf(subNode);
		
		// remove nodes in reverse order
		Arrays.sort(indices);
		while (i-- != 0)
			listOrder.remove(indices[i]);

		try {
			for (ScheduledNode subNode : subNodeSet)
				for (INodeListener listener : listeners)
					listener.onDeletedSubNode(this, subNode);

			int index = indices[0];
			while (index != listOrder.size()) {
				ScheduledNode changedNode = listOrder.get(index);
				for (INodeListener listener : changedNode.listeners)
					listener.onChangedProperty(changedNode, Element.INDEX, oldIndices.get(changedNode), index);
				index++;
			}
		}
		finally {
			for (ScheduledNode subNode : subNodeSet)
				subNode.dispose();
		}
	}
	// helpers
	private ScheduledNode createSubNode(Element element) {
		// create aspect template for element
		ScheduledNode subNode = null;
		String elementName = element.getPath().getElementName();
		for (AcquisitionEntry entry : acquisitionEntries)
			if (elementName.startsWith(entry.name)) {
				subNode = new ScheduledNode(
						ScheduledNode.this,
						element,
						entry.properties,
						entry.entries);

				if (acquisitionDetailProvider != null)
					subNode.resetAcquisitionDetailProvider(acquisitionDetailProvider);

				break;
			}
		
		// if element name is unknown 
		if (subNode == null)
			throw new Error("unknown element");
		
		return subNode;
	}
	private void rebuildElementIndices() {
		elementIndices.clear();
		for (Element element : subNodes.keySet())
			elementIndices.put(element, element.getIndex());
	}
	private static boolean sort(List<ScheduledNode> nodes, Map<Element, Integer> oldElementIndices, Map<Element, Integer> newElementIndices, Comparator<Integer> comparator) {
		// check current order
		if (!isOrdered(nodes, oldElementIndices, comparator))
			return false; // subNodes are not ordered

		// only sort if subNodes are not already ordered
		if (!isOrdered(nodes, newElementIndices, comparator))
			Collections.sort(nodes, new ListOrderComparator(newElementIndices, comparator));

		return true;
	}
	private static boolean insertSorted(List<ScheduledNode> nodes, Map<Element, Integer> elementIndices, Comparator<Integer> comparator, ScheduledNode newNode) {
		// check current order
		if (!isOrdered(nodes, elementIndices, comparator))
			return false; // subNodes are not ordered
	
		// add new node and sort collection
		nodes.add(newNode);
		Collections.sort(nodes, new ListOrderComparator(elementIndices, comparator));
		
		return true;
	}
	private static boolean isOrdered(List<ScheduledNode> nodes, Map<Element, Integer> elementIndices, Comparator<Integer> comparator) {
		int index = -1;
		for (ScheduledNode node : nodes) {
			Element element = node.getSourceElement();
			
			int elementIndex = elementIndices.get(element);
			if ((index == -1) || (comparator.compare(index, elementIndex) <= 0))
				index = elementIndex;
			else
				return false;
		}
		
		return true;
	}
	// helper
	private Map<ScheduledNode, Integer> backupIndices() {
		Map<ScheduledNode, Integer> oldIndices = new HashMap<>();
		for (int index = 0, n = listOrder.size(); index != n; index++)
			oldIndices.put(listOrder.get(index), index);
		return oldIndices;
	}
	// to string
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (owner != null)
			sb.append(owner.toString()).append('/');
		
		sb.append(sourceElement.getClass().getSimpleName());

		return sb.toString();
	}
	
	// listeners
	public void addListener(INodeListener listener) {
		if (listeners.contains(listener))
			throw new Error("listener already exists");
		
		listeners.add(listener);
		
		if (!isAspectLeaf())
			for (ScheduledNode subNode : listOrder)
				listener.onAddedSubNode(this, subNode);
	}
	public boolean removeListener(INodeListener listener) {
		return listeners.remove(listener);
	}
	// helpers
	private void raiseOnChangedProperty(ScheduledNode owner, IDependencyProperty property, Object oldValue, Object newValue) {
		for (INodeListener listener : listeners)
			listener.onChangedProperty(owner, property, oldValue, newValue);
	}
	
	// property listener
	private static class SourcePropertyListener implements IElementListener {
		// fields
		private final ScheduledNode node;
		
		// construction
		public SourcePropertyListener(ScheduledNode node) {
			this.node = node;
		}
		
		// methods
		@Override
		public void onChangedProperty(IDependencyProperty property, Object oldValue, Object newValue) {
			node.onChangedProperty(property, oldValue, newValue);
		}
		@Override
		public void onDisposed() {
			// ignore
		}
	}

	// list listener
	private static class SourceListListener implements IElementListListener<Element> {
		// fields
		private final ScheduledNode node;
		
		// construction
		public SourceListListener(ScheduledNode node) {
			this.node = node;
		}
		
		// methods
		@Override
		public void onAddedListElement(Element element) {
			node.onAddedListElement(element);
		}
		@Override
		public void onDeletedListElement(Element element) {
			node.onDeletedListElement(element);
		}
	}

	// compares elements with cached indices
	private static class ListOrderComparator implements Comparator<ScheduledNode> {
		// fields
		private final Map<Element, Integer> indices;
		private final Comparator<Integer> comparator;
		
		// construction
		public ListOrderComparator(Map<Element, Integer> indices, Comparator<Integer> comparator) {
			this.indices = indices;
			this.comparator = comparator;
		}

		// methods
		@Override
		public int compare(ScheduledNode x, ScheduledNode y) {
			return comparator.compare(indices.get(x.sourceElement), indices.get(y.sourceElement));
		}
	}
}
