package au.gov.ansto.bragg.quokka.msw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.gumtree.msw.INotificationLock;
import org.gumtree.msw.RefId;
import org.gumtree.msw.commands.AddListElementCommand;
import org.gumtree.msw.commands.BatchCommand;
import org.gumtree.msw.commands.ChangePropertyCommand;
import org.gumtree.msw.commands.ClearElementListCommand;
import org.gumtree.msw.commands.Command;
import org.gumtree.msw.elements.DependencyProperty;
import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.ElementList;
import org.gumtree.msw.elements.ElementPath;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IListElementFactory;

// SampleList can only be created by server but can be moved (via index) by client 
public class SampleList extends ElementList<Sample> {
	// property names
	public static final DependencyProperty<SampleList, String> DESCRIPTION = new DependencyProperty<>("Description", String.class);
	// property set
	public static final Set<IDependencyProperty> PROPERTIES = DependencyProperty.createSet(
			INDEX, DESCRIPTION);

	// fields
	private static final Map<String, Integer> DEFAULT_SAMPLE_POSITIONS;
	private final IListElementFactory<Sample> elementFactory = new IListElementFactory<Sample>() {
		private final String elementPrefix = Sample.class.getSimpleName() + '#';
		@Override
		public Sample create(String elementName) {
			if (elementName.startsWith(elementPrefix))
				return new Sample(SampleList.this, elementName);

			return null;
		}
	};
	
	// construction
	static {
		DEFAULT_SAMPLE_POSITIONS = new HashMap<>();
		DEFAULT_SAMPLE_POSITIONS.put(ExperimentDescription.MANUAL_POSITIONS, 1);
		DEFAULT_SAMPLE_POSITIONS.put(ExperimentDescription.FIXED_POSITION, 1);
		DEFAULT_SAMPLE_POSITIONS.put(ExperimentDescription.RHEOMETER, 2);
		DEFAULT_SAMPLE_POSITIONS.put(ExperimentDescription.ROTATING_5_POSITIONS, 5);
		DEFAULT_SAMPLE_POSITIONS.put(ExperimentDescription.LINEAR_10_POSITIONS, 10);
		DEFAULT_SAMPLE_POSITIONS.put(ExperimentDescription.LINEAR_12_POSITIONS, 12);
		DEFAULT_SAMPLE_POSITIONS.put(ExperimentDescription.LINEAR_20_POSITIONS, 20);
	}
	SampleList(LoopHierarchy parent) {
		super(parent, SampleList.class.getSimpleName());
	}

	// properties
	@Override
	public Set<IDependencyProperty> getProperties() {
		return PROPERTIES;
	}
	public String getDescription() {
		return (String)get(DESCRIPTION);
	}
	public void setDescription(String value) {
		set(DESCRIPTION, value);
	}

	// methods
	@Override
	public IListElementFactory<Sample> getElementFactory() {
		return elementFactory;
	}
	public void addSample() {
		add(Sample.class);
	}
	public void addSample(int index) {
		add(Sample.class, index);
	}
	public void replaceSamples(ExperimentDescription experimentDescription, Iterable<Map<IDependencyProperty, Object>> samples, IDependencyProperty ... persistentProperties) {
		String sampleStage;
		List<Map<IDependencyProperty, Object>> newSamples;
		
		try (INotificationLock lock = getModelProxy().suspendNotifications()) {
			sampleStage = experimentDescription.getSampleStage();
			if (!DEFAULT_SAMPLE_POSITIONS.containsKey(sampleStage))
				sampleStage = ExperimentDescription.DEFAULT_SAMPLE_STAGE;

			int defaultCount = DEFAULT_SAMPLE_POSITIONS.get(sampleStage);
			List<Sample> oldSamples = new ArrayList<>(defaultCount);
			fetchElements(oldSamples);
			Collections.sort(oldSamples, Element.INDEX_COMPARATOR);
			
			newSamples = new ArrayList<>();
			for (Map<IDependencyProperty, Object> newSampleInfo : samples)
				newSamples.add(newSampleInfo);
			
			if (!ExperimentDescription.MANUAL_POSITIONS.equals(sampleStage)) {
				while (newSamples.size() > defaultCount)
					newSamples.remove(newSamples.size() - 1);
				
				while ((newSamples.size() < defaultCount) && (newSamples.size() < oldSamples.size())) {
					Sample oldSample = oldSamples.get(newSamples.size());
					Map<IDependencyProperty, Object> newSampleInfo = new HashMap<>();
					for (IDependencyProperty property : persistentProperties)
						newSampleInfo.put(property, oldSample.get(property));
					newSamples.add(newSampleInfo);
				}
			}
		}

		RefId id = nextId();
		ElementPath path = getPath();

		List<Command> commands = new ArrayList<>();
		commands.add(new ChangePropertyCommand(
				id,
				experimentDescription.getPath(),
				ExperimentDescription.SAMPLE_STAGE.getName(),
				sampleStage));
		commands.add(new ClearElementListCommand(
				id,
				path));

		int index = 0;
		String className = Sample.class.getSimpleName();
		for (Map<IDependencyProperty, Object> elementInfo : newSamples) {
			String elementName = className + nextId().toString();
			ElementPath elementPath = new ElementPath(path, elementName);
			
			commands.add(new AddListElementCommand(
					id,
					path,
					elementName,
					index++));
			
			for (Map.Entry<IDependencyProperty, Object> entry : elementInfo.entrySet())
				commands.add(new ChangePropertyCommand(
						id,
						elementPath,
						entry.getKey().getName(),
						entry.getValue()));
		}
		
		command(new BatchCommand(id, commands.toArray(new Command[commands.size()])));
	}
	public void enableAll() {
		batchSet(Sample.ENABLED, true);
	}
	public void disableAll() {
		batchSet(Sample.ENABLED, false);
	}
	public void clear(ExperimentDescription experimentDescription) {
		String sampleStage = experimentDescription.getSampleStage();
		if (!DEFAULT_SAMPLE_POSITIONS.containsKey(sampleStage))
			sampleStage = ExperimentDescription.DEFAULT_SAMPLE_STAGE;
		
		setSampleCount(experimentDescription, sampleStage, true);
	}
	public void setSampleStage(ExperimentDescription experimentDescription, String sampleStage) {
		if (Objects.equals(experimentDescription.getSampleStage(), sampleStage))
			return;

		setSampleCount(experimentDescription, sampleStage, false);
	}
	private void setSampleCount(ExperimentDescription experimentDescription, String sampleStage, boolean clear) {
		if (!DEFAULT_SAMPLE_POSITIONS.containsKey(sampleStage))
			return;
		
		List<Sample> oldSamples = new ArrayList<>();
		fetchElements(oldSamples);
		Collections.sort(oldSamples, Element.INDEX_COMPARATOR);
		
		RefId id = nextId();
		ElementPath path = getPath();
		int count = DEFAULT_SAMPLE_POSITIONS.get(sampleStage);
		
		List<Command> commands = new ArrayList<>();
		commands.add(new ChangePropertyCommand(
				id,
				experimentDescription.getPath(),
				ExperimentDescription.SAMPLE_STAGE.getName(),
				sampleStage));
		commands.add(new ClearElementListCommand(
				id,
				path));

		String sampleClassName = Sample.class.getSimpleName();
		for (int index = 0; index < count; index++) {
			String elementName = sampleClassName + nextId().toString();
			ElementPath elementPath = new ElementPath(path, elementName);
			
			commands.add(new AddListElementCommand(
					id,
					path,
					elementName,
					index));

			commands.add(new ChangePropertyCommand(
					id,
					elementPath,
					Sample.POSITION.getName(),
					1.0 + index));
			
			if (!clear && (index < oldSamples.size())) {
				Sample oldSample = oldSamples.get(index);
				commands.add(new ChangePropertyCommand(
						id,
						elementPath,
						Sample.ENABLED.getName(),
						oldSample.getEnabled()));
				commands.add(new ChangePropertyCommand(
						id,
						elementPath,
						Sample.NAME.getName(),
						oldSample.getName()));
				commands.add(new ChangePropertyCommand(
						id,
						elementPath,
						Sample.DESCRIPTION.getName(),
						oldSample.getDescription()));
				commands.add(new ChangePropertyCommand(
						id,
						elementPath,
						Sample.THICKNESS.getName(),
						oldSample.getThickness()));
			}
			else if (count != 1) {
				commands.add(new ChangePropertyCommand(
						id,
						elementPath,
						Sample.ENABLED.getName(),
						false));
			}
		}
		
		command(new BatchCommand(id, commands.toArray(new Command[commands.size()])));
	}
}
