package org.gumtree.msw.model;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gumtree.msw.ICommand;
import org.gumtree.msw.IModel;
import org.gumtree.msw.IModelListener;
import org.gumtree.msw.IRefIdProvider;
import org.gumtree.msw.RefId;
import org.gumtree.msw.commands.GainControlCommand;

// should only by called from single thread (use SynchronizedModel if necessary)
public class Model implements IModel {
	// fields
	private final IRefIdProvider idProvider;
	private final DataSource xsdSource;
	private final DataSource xmlSource;
	// model
	private IModelNode root;
	private IModelNodeListener nodeListener;
	// clients
	private int clientId; // client which has control
	private List<IModelListener> listeners;
	
	// construction
	public Model(IRefIdProvider idProvider, DataSource xsdSource, DataSource xmlSource) {
		this.idProvider = idProvider;
		this.xsdSource = xsdSource;
		this.xmlSource = xmlSource;

		nodeListener = new ModelNodeListener();
		
		clientId = RefId.NONE_ID;
		listeners = new ArrayList<IModelListener>();
		
		reset();
	}
	
	// properties
	@Override
	public IRefIdProvider getIdProvider() {
		return idProvider;
	}
	@Override
	public DataSource getXsd() {
		return xsdSource;
	}
	
	// control
	@Override
	public boolean hasControl(int clientId) {
		return this.clientId == clientId;
	}
	@Override
	public boolean gainControl(int newClientId) {
		if (clientId != newClientId) {
			int oldClientId = this.clientId;
			clientId = newClientId;
			
			try {
				if (oldClientId != RefId.NONE_ID)
					for (IModelListener listener : listeners)
						listener.onReleasedControl(oldClientId);
			}
			finally {
				for (IModelListener listener : listeners)
					listener.onGainedControl(newClientId);
			}
		}
		return true;
	}
	@Override
	public boolean releaseControl(int clientId) {
		if (this.clientId == clientId) {
			this.clientId = RefId.NONE_ID;

			for (IModelListener listener : listeners)
				listener.onReleasedControl(clientId);
			
			return true;
		}
		return false;
	}

	// content
	@Override
	public boolean reset() {
		return deserializeFrom(xmlSource);
	}
	// properties
	@Override
	public Object getProperty(Iterable<String> elementPath, String property) {
		// find node
		IModelNode modelNode = root.findNode(elementPath);
		if (modelNode == null)
			return null; // node doesn't exist
		
		return modelNode.getProperty(property);
	}
	@Override
	public Map<String, Object> getProperties(Iterable<String> elementPath) {
		// find node
		IModelNode modelNode = root.findNode(elementPath);
		if (modelNode == null)
			return null; // node doesn't exist

		return modelNode.getProperties();
	}
	@Override
	public boolean changeProperty(Iterable<String> elementPath, String property, Object newValue, boolean parseValue) {
		// find node
		IModelNode modelNode = root.findNode(elementPath);
		if (modelNode == null)
			return false; // node doesn't exist
		
		// return false if property doesn't exist, cannot be changed or new value coudn't be parsed 
		if (parseValue) {
			if (!(newValue instanceof String) || !modelNode.parseProperty(property, (String)newValue))
				return false;
		}
		else {
			if (!modelNode.changeProperty(property, newValue))
				return false; // property doesn't exist or cannot be changed
		}
	
		return true;
	}
	// list elements
	@Override
	public Iterable<String> getListElements(Iterable<String> listPath) {
		// find node
		IModelNode modelNode = root.findNode(listPath);
		if (modelNode == null)
			return null; // node doesn't exist

		return modelNode.getListElements();
	}
	@Override
	public boolean addListElement(Iterable<String> listPath, String elementName, int targetIndex) {
		// find node
		IModelNode modelNode = root.findNode(listPath);
		if (modelNode == null)
			return false; // node doesn't exist
		if (!modelNode.addListElement(elementName, targetIndex))
			return false; // element could not be added

		return true;
	}
	@Override
	public boolean duplicateListElement(Iterable<String> listPath, String originalElementName, String newElementName) {
		// find node
		IModelNode modelNode = root.findNode(listPath);
		if (modelNode == null)
			return false; // node doesn't exist
		if (!modelNode.duplicateListElement(originalElementName, newElementName))
			return false; // element could not be added

		return true;
	}
	@Override
	public boolean deleteListElement(Iterable<String> listPath, String elementName) {
		// find node
		IModelNode modelNode = root.findNode(listPath);
		if (modelNode == null)
			return false; // node doesn't exist
		if (!modelNode.deleteListElement(elementName))
			return false; // element could not be deleted

		return true;
	}
	@Override
	public boolean recoverListElement(Iterable<String> listPath, String elementName) {
		// find node
		IModelNode modelNode = root.findNode(listPath);
		if (modelNode == null)
			return false; // node doesn't exist
		if (!modelNode.recoverListElement(elementName))
			return false; // element could not be deleted

		return true;
	}

	// experiment
	@Override
	public boolean startExperiment() {
		//changeProperty("Experiment", "state", State.STARTED);

		for (IModelListener listener : listeners)
			listener.onStartedExperiment();
		
		return true;
	}
	@Override
	public boolean pauseExperiment() {
		//changeProperty("Experiment", "state", State.PAUSED);

		for (IModelListener listener : listeners)
			listener.onPausedExperiment();

		return true;
	}
	@Override
	public boolean stopExperiment() {
		//changeProperty("Experiment", "state", State.STOPPED);

		for (IModelListener listener : listeners)
			listener.onStoppedExperiment();
		
		return true;
	}

	// command
	public boolean command(ICommand command) {
		if ((command.getId().getSourceId() == clientId) || (command instanceof GainControlCommand)) {
			ICommand undoCommand = command.execute(this);
			if (undoCommand != null) {
				for (IModelListener listener : listeners)
					listener.onCommandSucceeded(command, undoCommand);

				return true;
			}
		}
		
		// TODO only forward this to the sender of the failed command
		for (IModelListener listener : listeners)
			listener.onCommandFailed(command);

		return false;
	}

	// serialization
	@Override
	public boolean serializeTo(Iterable<String> elementPath, OutputStream stream) {
		// find node
		IModelNode modelNode = root.findNode(elementPath);
		if (modelNode == null)
			return false; // node doesn't exist
		
		try (Writer writer = new OutputStreamWriter(stream)) {
			return new ModelSaver(modelNode).serializeTo(writer);
		} catch (Exception e) {
		}
		return false;
	}
	//@Override
	//public boolean deserializeFrom(InputStream stream) {
	//	return deserializeFrom(new DataSource(stream));
	//}
	public boolean deserializeFrom(DataSource xmlSource) {
		/*
		DataSource xmlSource = new DataSource(stream);
		IModelNode content = ModelLoader.load(idProvider, xsdSource, xmlSource, modelNode.getNodeInfo().getName());
		if (content == null)
			return false;
		
		modelNode.assign(content);
		return true;*/

		// reload everything from files
		IModelNode newRoot = ModelLoader.load(idProvider, xsdSource, xmlSource);
		
		if (newRoot == null)
			return false;
		
		if (root != null)
			root.dispose();
		
		root = newRoot;
		for (IModelListener listener : listeners)
			listener.onReset();
		
		root.addListener(nodeListener);
		return true;
	}
	
	// listeners
	@Override
	public void addListener(IModelListener listener) {
		if (listeners.contains(listener))
			throw new Error("listener already exists");
		
		listeners.add(listener);
	}
	@Override
	public boolean removeListener(IModelListener listener) {
		return listeners.remove(listener);
	}

	// listen to node changes and forward them to external listeners
	private class ModelNodeListener implements IModelNodeListener {
		// methods
		@Override
		public void onNewListNode(IModelNode node) {
			node.addListener(this);
		}
		// properties
		@Override
		public void onChangedProperty(IModelNode node, String property, Object oldValue, Object newValue) {
			Iterable<String> nodePath = node.getPath();
			for (IModelListener listener : listeners)
				listener.onChangedProperty(nodePath, property, oldValue, newValue);
		}
		// list elements
		@Override
		public void onAddedListNode(IModelNode listNode, IModelNode subNode) {
			String subNodeName = subNode.getName();
			Iterable<String> listPath = listNode.getPath();
			for (IModelListener listener : listeners)
				listener.onAddedListElement(listPath, subNodeName);
		}
		@Override
		public void onDeletedListNode(IModelNode listNode, IModelNode subNode) {
			String subNodeName = subNode.getName();
			Iterable<String> listPath = listNode.getPath();
			for (IModelListener listener : listeners)
				listener.onDeletedListElement(listPath, subNodeName);
		}
		@Override
		public void onRecoveredListNode(IModelNode listNode, IModelNode subNode) {
			String subNodeName = subNode.getName();
			Iterable<String> listPath = listNode.getPath();
			for (IModelListener listener : listeners)
				listener.onRecoveredListElement(listPath, subNodeName);			
		}
	}
}

