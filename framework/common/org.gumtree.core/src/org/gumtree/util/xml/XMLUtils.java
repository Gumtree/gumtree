package org.gumtree.util.xml;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLUtils {

	public static Node getFirstChild(Node node, String childName) {
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node child = nodeList.item(i);
			if (child.getNodeName().equals(childName)) {
				return child;
			}
		}
		return null;
	}
	
	public static String getAttribute(Node node, String attributeName) {
		Node attribute = node.getAttributes().getNamedItem(attributeName);
		if (attribute != null) {
			return attribute.getNodeValue();
		}
		return null;
	}
	
	
	public static Collection<Node> createNodeCollection(final NodeList nodeList) {
		// http://www.java2s.com/Code/Java/XML/WrapNodeListtoCollection.htm
		// Written by Tomer Gabel under the Apache License Version 2.0
		return new Collection<Node>() {
			@Override
			public int size() {
				return nodeList.getLength();
			}

			@Override
			public boolean isEmpty() {
				return nodeList.getLength() > 0;
			}

			@Override
			public boolean contains(final Object o) {
				if (o == null || !(o instanceof Node))
					return false;
				for (int i = 0; i < nodeList.getLength(); ++i)
					if (o == nodeList.item(i))
						return true;
				return false;
			}

			@Override
			public Iterator<Node> iterator() {
				return new Iterator<Node>() {
					private int index = 0;

					@Override
					public boolean hasNext() {
						return nodeList.getLength() > this.index;
					}

					@Override
					public Node next() {
						if (this.index >= nodeList.getLength())
							throw new NoSuchElementException();
						return nodeList.item(this.index++);
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}

			@Override
			public Object[] toArray() {
				final Node[] array = new Node[nodeList.getLength()];
				for (int i = 0; i < array.length; ++i)
					array[i] = nodeList.item(i);
				return array;
			}

			@Override
			@SuppressWarnings({ "unchecked" })
			public <T> T[] toArray(final T[] a) throws ArrayStoreException {
				if (!a.getClass().getComponentType()
						.isAssignableFrom(Node.class))
					throw new ArrayStoreException(a.getClass()
							.getComponentType().getName()
							+ " is not the same or a supertype of Node");

				if (a.length >= nodeList.getLength()) {
					for (int i = 0; i < nodeList.getLength(); ++i)
						a[i] = (T) nodeList.item(i);
					if (a.length > nodeList.getLength())
						a[nodeList.getLength()] = null;
					return a;
				}

				return (T[]) toArray();
			}

			@Override
			public boolean add(final Node node) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean remove(final Object o) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean containsAll(final Collection<?> c) {
				for (final Object o : c)
					if (!this.contains(o))
						return false;
				return true;
			}

			@Override
			public boolean addAll(final Collection<? extends Node> c) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean removeAll(final Collection<?> c) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean retainAll(final Collection<?> c) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void clear() {
				throw new UnsupportedOperationException();
			}
		};
	}

	private XMLUtils() {
		super();
	}

}
