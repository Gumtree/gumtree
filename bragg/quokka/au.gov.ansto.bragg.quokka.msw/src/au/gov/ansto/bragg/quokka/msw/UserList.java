package au.gov.ansto.bragg.quokka.msw;

import java.util.Map;
import java.util.Set;

import org.gumtree.msw.IModelProxy;
import org.gumtree.msw.elements.DependencyProperty;
import org.gumtree.msw.elements.ElementList;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IListElementFactory;

public class UserList extends ElementList<User> {
	// property set
	public static final Set<IDependencyProperty> PROPERTIES = DependencyProperty.EMPTY_SET;

	// fields
	private final IListElementFactory<User> elementFactory = new IListElementFactory<User>() {
		@Override
		public User create(String elementName) {
			return new User(UserList.this, elementName);
		}
	};
	
	// construction
	UserList(IModelProxy modelProxy) {
		super(modelProxy, UserList.class.getSimpleName());
	}

	// properties
	@Override
	public Set<IDependencyProperty> getProperties() {
		return PROPERTIES;
	}

	// methods
	@Override
	public IListElementFactory<User> getElementFactory() {
		return elementFactory;
	}
	@Override
	public void clear() {
		super.clear();
	}
	public void addUser() {
		add(User.class);
	}
	public void addUser(int index) {
		add(User.class, index);
	}
	public void replaceUsers(Iterable<Map<IDependencyProperty, Object>> users) {
		replaceAll(User.class, users);
	}
	public void replaceUsers(Iterable<Map<IDependencyProperty, String>> users, boolean parse) {
		replaceAll(User.class, users, parse);
	}
	public void enableAll() {
		batchSet(User.ENABLED, true);
	}
	public void disableAll() {
		batchSet(User.ENABLED, false);
	}
}
