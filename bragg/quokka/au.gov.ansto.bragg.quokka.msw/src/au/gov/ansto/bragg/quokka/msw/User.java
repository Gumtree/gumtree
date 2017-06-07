package au.gov.ansto.bragg.quokka.msw;

import java.util.Set;

import org.gumtree.msw.elements.DependencyProperty;
import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.IDependencyProperty;

public class User extends Element {
	// property names
	public static final DependencyProperty<User, Boolean> ENABLED = new DependencyProperty<>("Enabled", Boolean.class);
	public static final DependencyProperty<User, String> NAME = new DependencyProperty<>("Name", String.class);
	public static final DependencyProperty<User, String> PHONE = new DependencyProperty<>("Phone", String.class);
	public static final DependencyProperty<User, String> EMAIL = new DependencyProperty<>("Email", String.class);
	// property set
	public static final Set<IDependencyProperty> PROPERTIES = DependencyProperty.createSet(
			INDEX, ENABLED, NAME, PHONE, EMAIL);
	
	// construction
	User(UserList parent, String name) {
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
	public String getPhone() {
		return (String)get(PHONE);
	}
	public void setPhone(String value) {
		set(PHONE, value);
	}
	public String getEmail() {
		return (String)get(EMAIL);
	}
	public void setEmail(String value) {
		set(EMAIL, value);
	}

	// methods
	@Override
	public void duplicate() {
		super.duplicate();
	}
	@Override
	public void delete() {
		super.delete();
	}
}
