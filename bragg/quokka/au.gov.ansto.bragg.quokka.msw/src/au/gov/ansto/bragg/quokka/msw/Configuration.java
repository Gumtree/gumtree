package au.gov.ansto.bragg.quokka.msw;

import java.util.Set;

import org.gumtree.msw.elements.DependencyProperty;
import org.gumtree.msw.elements.ElementList;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IListElementFactory;

public class Configuration extends ElementList<Measurement> {
	// property names
	public static final DependencyProperty<Configuration, Boolean> ENABLED = new DependencyProperty<>("Enabled", Boolean.class);
	public static final DependencyProperty<Configuration, String> NAME = new DependencyProperty<>("Name", String.class);
	public static final DependencyProperty<Configuration, String> GROUP = new DependencyProperty<>("Group", String.class);
	public static final DependencyProperty<Configuration, String> DESCRIPTION = new DependencyProperty<>("Description", String.class);
	public static final DependencyProperty<Configuration, String> SETUP_SCRIPT = new DependencyProperty<>("SetupScript", String.class);
	// property set
	public static final Set<IDependencyProperty> PROPERTIES = DependencyProperty.createSet(
			INDEX, ENABLED, NAME, GROUP, DESCRIPTION, SETUP_SCRIPT);
	
	// fields
	private final IListElementFactory<Measurement> elementFactory = new IListElementFactory<Measurement>() {
		@Override
		public Measurement create(String elementName) {
			return new Measurement(Configuration.this, elementName);
		}
	};

	// construction
	Configuration(ConfigurationList parent, String name) {
		super(parent, name);
	}

	// properties
	@Override
	public Set<IDependencyProperty> getProperties() {
		return PROPERTIES;
	}
	public boolean getEnabled() {
		return (boolean)get(ENABLED);
	}
	public void setEnabled(boolean value) {
		set(ENABLED, value);
	}
	public String getName() {
		return (String)get(NAME);
	}
	public void setName(String value) {
		set(NAME, value);
	}
	public String getGroup() {
		return (String)get(GROUP);
	}
	public void setGroup(String value) {
		set(GROUP, value);
	}
	public String getDescription() {
		return (String)get(DESCRIPTION);
	}
	public void setDescription(String value) {
		set(DESCRIPTION, value);
	}
	public String getSetupScript() {
		return (String)get(SETUP_SCRIPT);
	}
	public void setSetupScript(String value) {
		set(SETUP_SCRIPT, value);
	}

	// methods
	@Override
	public IListElementFactory<Measurement> getElementFactory() {
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
	public void addMeasurement() {
		add(Measurement.class);
	}
	public void addMeasurement(int index) {
		add(Measurement.class, index);
	}
}
