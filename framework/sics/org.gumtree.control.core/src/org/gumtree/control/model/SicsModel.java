package org.gumtree.control.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.ISicsModel;
import org.gumtree.control.core.ISicsProxy;
import org.gumtree.control.events.ISicsModelListener;
import org.gumtree.control.imp.SicsProxy;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.SICS;

public class SicsModel implements ISicsModel {

//	private Document modelXML;
//	private Transformer transformer;
	private String modelXML;
	private List<ISicsController> controllers;
	private SICS baseModel;
	private ISicsProxy sicsProxy;
	
	public SicsModel(ISicsProxy sicsProxy) {

		this.sicsProxy = sicsProxy;
//		File inputFile = new File("C:\\Gumtree\\docs\\GumtreeXML\\hipadaba.xml");
//        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//        modelXML = dBuilder.parse(inputFile);
        
//        model = ModelUtils.loadSICSModel("C:\\Gumtree\\docs\\GumtreeXML\\hipadaba.xml");
//		gumtreeModel = documentBuilder.newDocument();

	}

	public void setBase(SICS baseModel) {
		this.baseModel = baseModel;
		getControllerList();
	}
	
	public void loadFromFile(String filename) throws IOException {
		baseModel = ModelUtils.loadSICSModel(filename);
		getControllerList();
	}

	public void loadFromString(String xml) throws IOException {
		baseModel = ModelUtils.deserialiseSICSModel(xml.getBytes());
		modelXML = xml;
		getControllerList();
	}

//	public Document getModelXML() throws ParserConfigurationException {
//		
//		return modelXML;
//		
//	}
	
	public String getModelXML() {
		
//		synchronized (this) {
//			if (transformer == null) {
//				TransformerFactory transFactory = TransformerFactory.newInstance();
//				transformer = transFactory.newTransformer();
//				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
//			}
//
//			DOMSource domSource = new DOMSource(modelXML);
//			StringWriter buffer = new StringWriter();
//			transformer.transform(domSource, new StreamResult(buffer));
//			return buffer.toString();
//			
//		}
		return modelXML;

	}
	
	private List<ISicsController> getControllerList() {
		synchronized (this) {
			if(controllers == null) {
				controllers = new ArrayList<ISicsController>();
				// create controllers
				for(Component childComponent : (List<Component>)getBase().getComponent()) {
					//				logger.debug("Creating top level child controller for " + childComponent.getId());
					//				Object controller = Platform.getAdapterManager().getAdapter(childComponent, IComponentController.class);
					//				IComponentController controller = ComponentControllerFactory1.createController(childComponent);
					ISicsController controller = ModelUtils.createComponentController(sicsProxy, childComponent);
					if(controller != null) {
						controllers.add(controller);
						// Listen to status changes
//						controller.addComponentListener(new DirectComponentListener());
					}
				}
				// initialise the controllers once the hierarchy has been constructed
//				for(ISicsController childController : controllers) {
//					initialiseComponent(childController);
//				}
				
//				// Also initialise custom sics objects
//				for (ISicsController controller : getControllerFactory()
//						.createSicsObjectControllers()) {
//					customControllers.put(controller.getId(), controller);
//				}
			}
			return controllers;
		}
	}

//	private void initialiseComponent(ISicsController controller) {
////		getLogger().debug("Start initialising " + controller.getPath());
//		// recurrsively initialise child controllers first
//		for(ISicsController childController : controller.getChildControllers()) {
//			initialiseComponent(childController);
//		}
//		// then initialise the given controller if it supports initialisation
//		if(controller instanceof ComponentController) {
//			((ComponentController)controller).activate();
//		}
//	}
	
	public ISicsController findController(String idOrPath) {
		if (idOrPath.contains("/")) {
			return findControllerByPath(idOrPath);
		} else {
			return findControllerById(idOrPath);
		}
	}
	
	public ISicsController findComponentController(Component component) {
		return findControllerByPath(ModelUtils.getPath(component));
	}

	public ISicsController findControllerByPath(String path) {
		return findChildController(null, path);
	}

	public ISicsController findControllerById(String deviceId) {
		Component component = ModelUtils.findComponentFromSingleProperty(baseModel, "sicsdev", deviceId);
		if(component != null) {
			return findComponentController(component);
		}
		return null;
	}
	
	public ISicsController findChildController(ISicsController controller, String relativePath) {
		Assert.isNotNull(relativePath);
		String[] parts = relativePath.split("/");
		if(parts.length <= 1) {
			return null;
		}
		List<ISicsController> childControllers = null;
		if(controller == null) {
			childControllers = getControllerList();
		} else {
			childControllers = controller.getChildren();
		}
		for(ISicsController childController : childControllers) {
			if(childController.getModel().getId().equals(parts[1])) {
				relativePath = relativePath.substring(parts[1].length() + 1);
				if(relativePath.length() == 0) {
					return childController;
				} else {
					return findChildController(childController, relativePath);
				}
			}
		}
		return null;
	}
	
//	public ISicsController[] getComponentControllers() {
//		return getControllerList().toArray(new ISicsController[getControllerList().size()]);
//	}

	public ISicsController findParentController(ISicsController controller) {
		Component parentComponent = ModelUtils.getComponentParent(controller.getModel());
		if (parentComponent != null) {
			return findComponentController(parentComponent);
		}
		return null;
	}
	
	public SICS getBase() {
		return baseModel;
	}

	@Override
	public void addModelListener(ISicsModelListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeModelListener(ISicsModelListener listener) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public ISicsController[] getSicsControllers() {
		return getControllerList().toArray(new ISicsController[getControllerList().size()]);
	}

	public ModelStatus getStatus() {
		return ModelStatus.OK;
	}
}

