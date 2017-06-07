package au.gov.ansto.bragg.quokka.msw;

import java.util.Set;

import org.gumtree.msw.commands.AddListElementCommand;
import org.gumtree.msw.elements.DependencyProperty;
import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.IDependencyProperty;

public class SetPoint extends Element {
	// property names
	public static final DependencyProperty<SetPoint, Boolean> ENABLED = new DependencyProperty<>("Enabled", Boolean.class);
	public static final DependencyProperty<SetPoint, Double> VALUE = new DependencyProperty<>("Value", Double.class);
	public static final DependencyProperty<SetPoint, Long> WAIT_PERIOD = new DependencyProperty<>("WaitPeriod", Long.class); // seconds
	// property set
	public static final Set<IDependencyProperty> PROPERTIES = DependencyProperty.createSet(
			INDEX, ENABLED, VALUE, WAIT_PERIOD);

	// construction
	SetPoint(Environment parent, String elementName) {
		super(parent, elementName);
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
	public double getValue() {
		return (double)get(VALUE);
	}
	public void setValue(double value) {
		set(VALUE, value);
	}
	public long getWaitPeriod() {
		return (long)get(WAIT_PERIOD);
	}
	public void setWaitPeriod(long value) {
		set(WAIT_PERIOD, value);
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
	public void add() {
		command(new AddListElementCommand(
				nextId(),
				getPath().getRoot(),
				SetPoint.class.getSimpleName() + nextId().toString(),
				getIndex()));
	}
}
