package org.gumtree.msw.model.structure;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSFacet;
import org.apache.xerces.xs.XSMultiValueFacet;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.gumtree.msw.model.structure.XsTypeHelper.BigIntegerSerializer;
import org.gumtree.msw.model.structure.XsTypeHelper.BooleanSerializer;
import org.gumtree.msw.model.structure.XsTypeHelper.ByteSerializer;
import org.gumtree.msw.model.structure.XsTypeHelper.DoubleSerializer;
import org.gumtree.msw.model.structure.XsTypeHelper.FloatSerializer;
import org.gumtree.msw.model.structure.XsTypeHelper.IntegerSerializer;
import org.gumtree.msw.model.structure.XsTypeHelper.LongSerializer;
import org.gumtree.msw.model.structure.XsTypeHelper.ShortSerializer;
import org.gumtree.msw.model.structure.XsTypeHelper.StringSerializer;

public class FacetList {
	// finals
	private static final FacetList EMPTY = new FacetList();
	
	// fields
	private final Iterable<IFacet> facets;

	// construction
	private FacetList() {
		facets = new ArrayList<>();
	}
	private FacetList(Iterable<IFacet> facets) {
		this.facets = facets;
	}
	// helper
	static FacetList create(XSSimpleTypeDefinition type) {
		ArrayList<IFacet> facets = new ArrayList<IFacet>();
		
		// single-value facet
		XSObjectList svFacetList = type.getFacets();
		for (int i = 0; i < svFacetList.getLength(); i++) {
			IFacet facet = createFacet(type, (XSFacet)svFacetList.item(i));
			if (facet != null)
				facets.add(facet);
		}

		// multi-value facets
		XSObjectList mvFacetList = type.getMultiValueFacets();
		for (int i = 0; i < mvFacetList.getLength(); i++) {
			IFacet facet = createFacet(type, (XSMultiValueFacet)mvFacetList.item(i));
			if (facet != null)
				facets.add(facet);
		}
		
		if (facets.size() == 0)
			return FacetList.EMPTY;
		
		facets.trimToSize();
		return new FacetList(facets);
	}

	// methods
	public boolean validate(Object value) {
		for (IFacet facet : facets)
			if (!facet.validate(value))
				return false;
		
		return true;
	}

	// helper
	private static IFacet createFacet(XSSimpleTypeDefinition type, XSFacet facet) {
		switch (facet.getFacetKind()) {
		case XSSimpleTypeDefinition.FACET_LENGTH:
			return new LengthFacet(type, facet);
		case XSSimpleTypeDefinition.FACET_MINLENGTH:
			return new MinLengthFacet(type, facet);
		case XSSimpleTypeDefinition.FACET_MAXLENGTH:
			return new MaxLengthFacet(type, facet);
		case XSSimpleTypeDefinition.FACET_PATTERN:
			throw new Error(); // pattern facet should be a multi-value facet
		case XSSimpleTypeDefinition.FACET_WHITESPACE:
			return null; // ignore
		case XSSimpleTypeDefinition.FACET_MAXEXCLUSIVE:
			return new MaxExclusiveFacet(type, facet);
		case XSSimpleTypeDefinition.FACET_MAXINCLUSIVE:
			return new MaxInclusiveFacet(type, facet);
		case XSSimpleTypeDefinition.FACET_MINEXCLUSIVE:
			return new MinExclusiveFacet(type, facet);
		case XSSimpleTypeDefinition.FACET_MININCLUSIVE:
			return new MinInclusiveFacet(type, facet);
		case XSSimpleTypeDefinition.FACET_TOTALDIGITS:
			System.out.println("WARNING: \"FACET_TOTALDIGITS\" is not supported");
			return null;
		case XSSimpleTypeDefinition.FACET_FRACTIONDIGITS:
			System.out.println("WARNING: \"FACET_FRACTIONDIGITS\" is not supported");
			return null;
		case XSSimpleTypeDefinition.FACET_ENUMERATION:
			throw new Error(); // enumeration facet should be a multi-value
								// facet
		default:
			return null;
		}
	}
	private static IFacet createFacet(XSSimpleTypeDefinition type, XSMultiValueFacet facet) {
		switch (facet.getFacetKind()) {
		case XSSimpleTypeDefinition.FACET_LENGTH:
			throw new Error();
		case XSSimpleTypeDefinition.FACET_MINLENGTH:
			throw new Error();
		case XSSimpleTypeDefinition.FACET_MAXLENGTH:
			throw new Error();
		case XSSimpleTypeDefinition.FACET_PATTERN:
			return new PatternFacet(type, facet);
		case XSSimpleTypeDefinition.FACET_WHITESPACE:
			throw new Error();
		case XSSimpleTypeDefinition.FACET_MAXEXCLUSIVE:
			throw new Error();
		case XSSimpleTypeDefinition.FACET_MAXINCLUSIVE:
			throw new Error();
		case XSSimpleTypeDefinition.FACET_MINEXCLUSIVE:
			throw new Error();
		case XSSimpleTypeDefinition.FACET_MININCLUSIVE:
			throw new Error();
		case XSSimpleTypeDefinition.FACET_TOTALDIGITS:
			throw new Error();
		case XSSimpleTypeDefinition.FACET_FRACTIONDIGITS:
			throw new Error();
		case XSSimpleTypeDefinition.FACET_ENUMERATION:
			return new EnumerationFacet(type, facet);
		default:
			return null;
		}
	}

	// length
	private static abstract class AbstractLengthFacet implements IFacet {
		// fields
		protected final int length;

		// construction
		public AbstractLengthFacet(XSSimpleTypeDefinition type, XSFacet facet) {
			if (!Objects.equals(String.class, XsTypeHelper.type(type.getBuiltInKind())))
				throw new Error();

			length = Integer.parseInt(facet.getLexicalFacetValue());
		}

		// methods
		@Override
		public boolean validate(Object value) {
			Integer length = determineLength(value);
			if (length == null)
				return false;

			return valid(length);
		}

		protected abstract boolean valid(int value);

		// helper
		private static Integer determineLength(Object value) {
			if (value instanceof CharSequence)
				return ((CharSequence) value).length();

			if (value instanceof Collection<?>)
				return ((Collection<?>) value).size();

			if (value instanceof Iterable<?>) {
				int n = 0;
				for (Iterator<?> itr = ((Iterable<?>) value).iterator(); itr.hasNext(); itr.next())
					n++;
				return n;
			}

			return null;
		}
	}

	private static class LengthFacet extends AbstractLengthFacet {
		// construction
		public LengthFacet(XSSimpleTypeDefinition type, XSFacet facet) {
			super(type, facet);
		}

		// methods
		@Override
		protected boolean valid(int value) {
			return length == value;
		}
	}

	private static class MinLengthFacet extends AbstractLengthFacet {
		// construction
		public MinLengthFacet(XSSimpleTypeDefinition type, XSFacet facet) {
			super(type, facet);
		}

		// methods
		@Override
		protected boolean valid(int value) {
			return length <= value;
		}
	}

	private static class MaxLengthFacet extends AbstractLengthFacet {
		// construction
		public MaxLengthFacet(XSSimpleTypeDefinition type, XSFacet facet) {
			super(type, facet);
		}

		// methods
		@Override
		protected boolean valid(int value) {
			return value <= length;
		}
	}

	// comparison
	private static abstract class ComparisonFacet implements IFacet {
		// fields
		private final GenericReference<?> reference;

		// construction
		public ComparisonFacet(XSSimpleTypeDefinition type, final XSFacet facet) {
			final GenericReference<?>[] reference = new GenericReference<?>[1];
			XsTypeHelper.accept(type.getBuiltInKind(), new XsTypeHelper.IVisitor() {
				@Override
				public void visit(Class<BigInteger> clazz, BigIntegerSerializer serializer) {
					reference[0] = new GenericReference<>(clazz, serializer.deserialize(facet.getLexicalFacetValue()));
				}

				@Override
				public void visit(Class<Long> clazz, LongSerializer serializer) {
					reference[0] = new GenericReference<>(clazz, serializer.deserialize(facet.getLexicalFacetValue()));
				}

				@Override
				public void visit(Class<Integer> clazz, IntegerSerializer serializer) {
					reference[0] = new GenericReference<>(clazz, serializer.deserialize(facet.getLexicalFacetValue()));
				}

				@Override
				public void visit(Class<Short> clazz, ShortSerializer serializer) {
					reference[0] = new GenericReference<>(clazz, serializer.deserialize(facet.getLexicalFacetValue()));
				}

				@Override
				public void visit(Class<Byte> clazz, ByteSerializer serializer) {
					reference[0] = new GenericReference<>(clazz, serializer.deserialize(facet.getLexicalFacetValue()));
				}

				@Override
				public void visit(Class<Double> clazz, DoubleSerializer serializer) {
					reference[0] = new GenericReference<>(clazz, serializer.deserialize(facet.getLexicalFacetValue()));
				}

				@Override
				public void visit(Class<Float> clazz, FloatSerializer serializer) {
					reference[0] = new GenericReference<>(clazz, serializer.deserialize(facet.getLexicalFacetValue()));
				}

				@Override
				public void visit(Class<Boolean> clazz, BooleanSerializer serializer) {
					reference[0] = new GenericReference<>(clazz, serializer.deserialize(facet.getLexicalFacetValue()));
				}

				@Override
				public void visit(Class<String> clazz, StringSerializer serializer) {
					reference[0] = new GenericReference<>(clazz, serializer.deserialize(facet.getLexicalFacetValue()));
				}
			});
			this.reference = reference[0];
		}

		// methods
		@Override
		public boolean validate(Object value) {
			Integer c = reference.compareTo(value);
			if (c == null)
				return false;

			return valid(c);
		}

		protected abstract boolean valid(int c);
	}

	private static class MaxExclusiveFacet extends ComparisonFacet {
		// construction
		public MaxExclusiveFacet(XSSimpleTypeDefinition type, XSFacet facet) {
			super(type, facet);
		}

		// methods
		@Override
		protected boolean valid(int c) {
			return 0 < c;
		}
	}

	private static class MaxInclusiveFacet extends ComparisonFacet {
		// construction
		public MaxInclusiveFacet(XSSimpleTypeDefinition type, XSFacet facet) {
			super(type, facet);
		}

		// methods
		@Override
		protected boolean valid(int c) {
			return 0 <= c;
		}
	}

	private static class MinExclusiveFacet extends ComparisonFacet {
		// construction
		public MinExclusiveFacet(XSSimpleTypeDefinition type, XSFacet facet) {
			super(type, facet);
		}

		// methods
		@Override
		protected boolean valid(int c) {
			return 0 > c;
		}
	}

	private static class MinInclusiveFacet extends ComparisonFacet {
		// construction
		public MinInclusiveFacet(XSSimpleTypeDefinition type, XSFacet facet) {
			super(type, facet);
		}

		// methods
		@Override
		protected boolean valid(int c) {
			return 0 >= c;
		}
	}

	// multi-value facets
	private static class PatternFacet implements IFacet {
		// fields
		private final XsTypeHelper.ISerializer serializer;
		private final List<Pattern> patterns;

		// construction
		public PatternFacet(XSSimpleTypeDefinition type, XSMultiValueFacet facet) {
			StringList list = facet.getLexicalFacetValues();

			serializer = XsTypeHelper.serializer(type.getBuiltInKind());
			patterns = new ArrayList<>(list.getLength());
			for (int i = 0; i < list.getLength(); i++)
				patterns.add(Pattern.compile(list.item(i)));
		}

		// methods
		@Override
		public boolean validate(Object value) {
			String s = serializer.serialize(value);
			for (Pattern pattern : patterns)
				if (!pattern.matcher(s).matches())
					return false;

			return true;
		}
	}

	private static class EnumerationFacet implements IFacet {
		// fields
		private final XsTypeHelper.ISerializer serializer;
		private final Set<String> set;

		// construction
		public EnumerationFacet(XSSimpleTypeDefinition type, XSMultiValueFacet facet) {
			StringList list = facet.getLexicalFacetValues();
			
			serializer = XsTypeHelper.serializer(type.getBuiltInKind());
			set = new HashSet<>();
			for (int i = 0; i < list.getLength(); i++)
				set.add(list.item(i));
		}

		// methods
		@Override
		public boolean validate(Object value) {
			return set.contains(serializer.serialize(value));
		}
	}

	// helpers
	private static final class GenericReference<T> {
		// fields
		private final Class<T> clazz;
		private final Comparable<T> reference;

		// construction
		public GenericReference(Class<T> clazz, Comparable<T> reference) {
			this.clazz = clazz;
			this.reference = reference;
		}

		// methods
		public Integer compareTo(Object value) {
			if (!clazz.isInstance(value))
				return null;

			return reference.compareTo(clazz.cast(value));
		}
	}
}
