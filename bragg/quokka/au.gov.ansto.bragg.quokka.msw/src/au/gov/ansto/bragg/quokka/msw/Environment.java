package au.gov.ansto.bragg.quokka.msw;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gumtree.msw.ICommand;
import org.gumtree.msw.RefId;
import org.gumtree.msw.commands.AddListElementCommand;
import org.gumtree.msw.commands.BatchCommand;
import org.gumtree.msw.commands.ChangePropertyCommand;
import org.gumtree.msw.commands.Command;
import org.gumtree.msw.elements.DependencyProperty;
import org.gumtree.msw.elements.ElementList;
import org.gumtree.msw.elements.ElementPath;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IListElementFactory;

public class Environment extends ElementList<SetPoint> {
	// property names
	public static final DependencyProperty<Environment, String> NAME = new DependencyProperty<>("Name", String.class);
	public static final DependencyProperty<Environment, String> DESCRIPTION = new DependencyProperty<>("Description", String.class);
	public static final DependencyProperty<Environment, String> SETUP_SCRIPT = new DependencyProperty<>("SetupScript", String.class);
	public static final DependencyProperty<Environment, String> DRIVE_SCRIPT = new DependencyProperty<>("DriveScript", String.class);
	// property set
	public static final Set<IDependencyProperty> PROPERTIES = DependencyProperty.createSet(
			INDEX, NAME, DESCRIPTION, SETUP_SCRIPT, DRIVE_SCRIPT);

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
	public String getSetupScript() {
		return (String)get(SETUP_SCRIPT);
	}
	public void setSetupScript(String value) {
		set(SETUP_SCRIPT, value);
	}
	public String getDriveScript() {
		return (String)get(DRIVE_SCRIPT);
	}
	public void setDriveScript(String value) {
		set(DRIVE_SCRIPT, value);
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
	public void replaceSetPoints(Iterable<Map<IDependencyProperty, Object>> setPoints) {
		replaceAll(SetPoint.class, setPoints);
	}
	public void enableAll() {
		batchSet(SetPoint.ENABLED, true);
	}
	public void disableAll() {
		batchSet(SetPoint.ENABLED, false);
	}
	public void applyTemplate(String setupScript, String driveScript) {
		RefId id = nextId();
		ElementPath path = getPath();

		command(new BatchCommand(
				id,
				new ChangePropertyCommand(
						id,
						path,
						SETUP_SCRIPT.getName(),
						setupScript),
				new ChangePropertyCommand(
						id,
						path,
						DRIVE_SCRIPT.getName(),
						driveScript)));
	}
	public void generate(double from, double to, int steps, long wait) {
		RefId id = nextId();
		ElementPath path = getPath();
		
		List<Command> commands = new ArrayList<>();
		
		String setPointClassName = SetPoint.class.getSimpleName();
		for (int index = 0; index < steps; index++) {
			String elementName = setPointClassName + nextId().toString();
			ElementPath elementPath = new ElementPath(path, elementName);

			commands.add(new AddListElementCommand(
					id,
					path,
					elementName));

			commands.add(new ChangePropertyCommand(
					id,
					elementPath,
					SetPoint.VALUE.getName(),
					from + (to - from) * index / Math.max(1, steps - 1)));

			commands.add(new ChangePropertyCommand(
					id,
					elementPath,
					SetPoint.WAIT_PERIOD.getName(),
					wait));
		}

		command(new BatchCommand(id, commands.toArray(new ICommand[commands.size()])));
	}
}
