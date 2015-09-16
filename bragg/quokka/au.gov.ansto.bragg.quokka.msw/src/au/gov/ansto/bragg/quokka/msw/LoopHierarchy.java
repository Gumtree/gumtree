package au.gov.ansto.bragg.quokka.msw;

import java.util.Set;

import org.gumtree.msw.IModelProxy;
import org.gumtree.msw.elements.DependencyProperty;
import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.ElementList;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IListElementFactory;

public class LoopHierarchy extends ElementList<Element> {
	// property set
	public static final Set<IDependencyProperty> PROPERTIES = DependencyProperty.EMPTY_SET;

	// fields
	private final IListElementFactory<Element> elementFactory = new IListElementFactory<Element>() {
		@Override
		public Element create(String elementName) {
			// element name of ConfigurationList and SampleList doesn't contain id
			if (ConfigurationList.class.getSimpleName().equals(elementName))
				return new ConfigurationList(LoopHierarchy.this);
			
			if (SampleList.class.getSimpleName().equals(elementName))
				return new SampleList(LoopHierarchy.this);
			
			return new Environment(LoopHierarchy.this, elementName);
		}
	};

	// construction
	LoopHierarchy(IModelProxy modelProxy) {
		super(modelProxy, LoopHierarchy.class.getSimpleName());
	}

	// properties
	@Override
	public Set<IDependencyProperty> getProperties() {
		return PROPERTIES;
	}
	
	// methods
	@Override
	public IListElementFactory<Element> getElementFactory() {
		return elementFactory;
	}
	public void addEnvironment() {
		// only environments can be added
		add(Environment.class);
	}
	public void addEnvironment(int index) {
		add(Environment.class, index);
	}
}
