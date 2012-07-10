/*******************************************************************************
 * Copyright (c) 2012 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bragg Institute - initial API and implementation
 ******************************************************************************/

package org.gumtree.util.eclipse;

import java.util.ArrayList;
import java.util.List;

public class FilterBuilder {

	private IFilterMember member; 
	
	public FilterBuilder() {
	}
	
	public FilterBuilder(String key, String value) {
		member = new FilterPair(key, value);
	}
	
	public FilterBuilder and(String key, String value) {
		if (member instanceof FilterAndMember) {
			((FilterAndMember) member).addMember(new FilterPair(key, value));
		} else {
			member = new FilterAndMember().addMember(member).addMember(new FilterPair(key, value));
		}
		return this;
	}
	
	public FilterBuilder or(String key, String value) {
		if (member instanceof FilterOrMember) {
			((FilterOrMember) member).addMember(new FilterPair(key, value));
		} else {
			member = new FilterOrMember().addMember(member).addMember(new FilterPair(key, value));
		}
		return this;
	}
	
	public FilterBuilder not() {
		member = new FilterNotMember(member);
		return this;
	}
	
	public String get() {
		return member.toString();
	}
	
	public interface IFilterMember {
		public String toString();
	}
	
	public class FilterPair implements IFilterMember {

		private String key;
		
		private String value;
		
		public FilterPair(String key, String value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return "(" + getKey() + "=" + getValue() + ")";
		}
		
	}
	
	public class FilterNotMember implements IFilterMember {

		private IFilterMember member;
		
		public FilterNotMember(IFilterMember member) {
			this.member = member;
		}
		
		public IFilterMember getMember() {
			return member;
		}
		
		@Override
		public String toString() {
			return "(!" + getMember() + ")";
		}
		
	}
	
	public class FilterAndMember implements IFilterMember {

		private List<IFilterMember> members;
		
		public FilterAndMember() {
			members = new ArrayList<FilterBuilder.IFilterMember>(2);
		}
		
		public FilterAndMember addMember(IFilterMember member) {
			members.add(member);
			return this;
		}
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("(&");
			for (IFilterMember innerMember : members) {
				builder.append(innerMember.toString());
			}
			builder.append(")");
			return builder.toString();
		}
		
	}
	
	public class FilterOrMember implements IFilterMember {

		private List<IFilterMember> members;
		
		public FilterOrMember() {
			members = new ArrayList<FilterBuilder.IFilterMember>(2);
		}
		
		public FilterOrMember addMember(IFilterMember member) {
			members.add(member);
			return this;
		}
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("(|");
			for (IFilterMember innerMember : members) {
				builder.append(innerMember.toString());
			}
			builder.append(")");
			return builder.toString();
		}
	}

}
