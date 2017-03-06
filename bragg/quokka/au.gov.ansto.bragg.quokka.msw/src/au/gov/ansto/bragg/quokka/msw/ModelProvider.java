package au.gov.ansto.bragg.quokka.msw;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gumtree.msw.IModelProxy;
import org.gumtree.msw.commands.UndoRedo;
import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.ElementRegistry;
import org.gumtree.msw.util.ModelListenerAdapter;

import au.gov.ansto.bragg.quokka.msw.schedule.CustomInstrumentAction;

public class ModelProvider {
	// fields
	private final IModelProxy modelProxy;
	private final ElementRegistry registry;
	private final UndoRedo undoRedo;
	// content
	private ExperimentDescription experimentDescription;
	private UserList userList;
	private SampleList sampleList;
	private ConfigurationList configurationList;
	private LoopHierarchy loopHierarchy;
	//
	private CustomInstrumentAction customInstrumentAction;
	// listeners
	private final List<IModelProviderListener> listeners = new ArrayList<>();

	// construction
	public ModelProvider(IModelProxy modelProxy) {
		this.modelProxy = modelProxy;
		this.registry = new ElementRegistry(modelProxy);
		this.undoRedo = new UndoRedo(modelProxy);

		fetchModel();
		modelProxy.addListener(new ModelListenerAdapter() {
			@Override
			public void onReset() {
				fetchModel();
				for (IModelProviderListener listener : listeners)
					listener.onReset();
			}
		});
	}
	
	// properties
	public IModelProxy getModelProxy() {
		return modelProxy;
	}
	public ElementRegistry getRegistry() {
		return registry;
	}
	public UndoRedo getUndoRedo() {
		return undoRedo;
	}
	// content
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
	//
	public CustomInstrumentAction getCustomInstrumentAction() {
		if (customInstrumentAction == null)
			customInstrumentAction = new CustomInstrumentAction();
		
		return customInstrumentAction;
	}
	
	// methods
	public void addListener(IModelProviderListener listener) {
		if (listeners.contains(listener))
			throw new Error("listener already exists");
		
		listeners.add(listener);
		listener.onReset();
	}
	public boolean removeListener(IModelProviderListener listener) {
		return listeners.remove(listener);
	}
	
	// helpers
	private void fetchModel() {
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
	}
}
