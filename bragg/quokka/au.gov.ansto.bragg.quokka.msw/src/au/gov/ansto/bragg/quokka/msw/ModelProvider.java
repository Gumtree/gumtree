package au.gov.ansto.bragg.quokka.msw;

import java.util.HashSet;
import java.util.Set;

import org.gumtree.msw.IModelProxy;
import org.gumtree.msw.commands.UndoRedo;
import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.ElementRegistry;

import au.gov.ansto.bragg.quokka.msw.schedule.CustomInstrumentAction;

public class ModelProvider {
	// fields
	private IModelProxy modelProxy;
	private ElementRegistry registry;
	private ExperimentDescription experimentDescription;
	private UserList userList;
	private SampleList sampleList;
	private ConfigurationList configurationList;
	private LoopHierarchy loopHierarchy;
	private UndoRedo undoRedo;
	//
	private CustomInstrumentAction customInstrumentAction;

	// construction
	public ModelProvider(IModelProxy modelProxy) {
		this.modelProxy = modelProxy;

		registry = new ElementRegistry(modelProxy);
		registry.register(experimentDescription = new ExperimentDescription(modelProxy));
		registry.register(userList = new UserList(modelProxy));
		registry.register(loopHierarchy = new LoopHierarchy(modelProxy));

		// get sampleList and configurationList
		Set<Element> elements = new HashSet<Element>();
		loopHierarchy.fetchElements(elements);
		for (Element element : elements)
			 if (element instanceof ConfigurationList)
				configurationList = (ConfigurationList)element;
			 else if (element instanceof SampleList)
				sampleList = (SampleList)element;
		
		undoRedo = new UndoRedo(modelProxy);
	}
	
	// properties
	public IModelProxy getModelProxy() {
		return modelProxy;
	}
	public ElementRegistry getRegistry() {
		return registry;
	}
	public ExperimentDescription getExperimentDescription() {
		return experimentDescription;
	}
	public UserList getUserList() {
		return userList;
	}
	public SampleList getSampleList() {
		return sampleList;
	}
	public ConfigurationList getConfigurationList() {
		return configurationList;
	}
	public LoopHierarchy getLoopHierarchy() {
		return loopHierarchy;
	}
	public UndoRedo getUndoRedo() {
		return undoRedo;
	}
	public CustomInstrumentAction getCustomInstrumentAction() {
		if (customInstrumentAction == null)
			customInstrumentAction = new CustomInstrumentAction();
		
		return customInstrumentAction;
	}
}
