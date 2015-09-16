package au.gov.ansto.bragg.quokka.msw;

import java.util.Set;

import org.gumtree.msw.elements.DependencyProperty;
import org.gumtree.msw.elements.ElementList;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IListElementFactory;

public class Environment extends ElementList<SetPoint> {
	// property names
	public static final DependencyProperty<Environment, String> NAME = new DependencyProperty<>("Name", String.class);
	public static final DependencyProperty<Environment, String> DESCRIPTION = new DependencyProperty<>("Description", String.class);
	// property set
	public static final Set<IDependencyProperty> PROPERTIES = DependencyProperty.createSet(
			INDEX, NAME, DESCRIPTION);

	// fields
	private final IListElementFactory<SetPoint> elementFactory = new IListElementFactory<SetPoint>() {
		private final String elementPrefix = SetPoint.class.getSimpleName() + '#';
		@Override
		public SetPoint create(String elementName) {
			if (elementName.startsWith(elementPrefix))
				return new SetPoint(Environment.this, elementName);

			return null;
		}
	};
	
	// construction
	Environment(LoopHierarchy parent, String elementName) {
		super(parent, elementName);
	}

	// properties
	@Override
	public Set<IDependencyProperty> getProperties() {
		return PROPERTIES;
	}
	public String getName() {
		return (String)get(NAME);
	}
	public void setName(String value) {
		set(NAME, value);
	}
	public String getDescription() {
		return (String)get(DESCRIPTION);
	}
	public void setDescription(String value) {
		set(DESCRIPTION, value);
	}

	// methods
	@Override
	public IListElementFactory<SetPoint> getElementFactory() {
		return elementFactory;
	}
	@Override
	public void duplicate() {
		super.duplicate();
	}
	@Override
	public void delete() {
		super.delete();
	}
	@Override
	public void clear() {
		super.clear();
	}
	public void addSetPoint() {
		add(SetPoint.class);
	}
	public void addSetPoint(int index) {
		add(SetPoint.class, index);
	}
	public void enableAll() {
		batchSet(SetPoint.ENABLED, true);
	}
	public void disableAll() {
		batchSet(SetPoint.ENABLED, false);
	}
}
