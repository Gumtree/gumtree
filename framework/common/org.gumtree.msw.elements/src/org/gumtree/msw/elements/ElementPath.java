package org.gumtree.msw.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

// immutable object
public final class ElementPath implements Iterable<String> {
	// finals
	private static final char SEPARATOR = '/';
	
	// fields
	private final ElementPath root;
	private final String elementName;
	// cache
	private Iterable<String> pathIterableCache;
	private String pathStringCache;

	// construction
	public ElementPath(String elementName) {
		this(null, elementName);
	}
	public ElementPath(ElementPath root, String elementName) {
		this.root = root;
		this.elementName = elementName;
	}
	
	// properties
	public ElementPath getRoot() {
		return root;
	}
	public String getElementName() {
		return elementName;
	}
	
	// methods
	@Override
	public Iterator<String> iterator() {
		if (pathIterableCache == null) {
			List<String> list = new ArrayList<String>();
			
			list.add(elementName);
			ElementPath r = root;
			while (r != null) {
				list.add(r.elementName);
				r = r.root;
			}
			Collections.reverse(list);
			
			pathIterableCache = Collections.unmodifiableList(list);
		}
		return pathIterableCache.iterator();
	}
	// object
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj instanceof ElementPath) {
			ElementPath l = this;
			ElementPath r = (ElementPath)obj;
			
			while (Objects.equals(l.elementName, r.elementName)) {
				l = l.root;
				r = r.root;
				
				if (l == r)
					return true;
				if ((l == null) || (r == null))
					return false;
			}
		}
		return false;
	}
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	@Override
	public String toString() {
		if (pathStringCache == null) {
			StringBuilder sb = new StringBuilder();
			appendPath(sb);
			pathStringCache = sb.toString();
		}
		return pathStringCache;
	}
	public static String toString(Iterable<String> path) {
		Iterator<String> itr = path.iterator();
		if (!itr.hasNext())
			return null;

		StringBuilder sb = new StringBuilder();
		sb.append(itr.next());
		while (itr.hasNext())
			sb.append(SEPARATOR).append(itr.next());

		return sb.toString();
	}
	// helpers
	private void appendPath(StringBuilder sb) {
		if (root != null) {
			root.appendPath(sb);
			sb.append(SEPARATOR);
		}
		sb.append(elementName);
	}
}
