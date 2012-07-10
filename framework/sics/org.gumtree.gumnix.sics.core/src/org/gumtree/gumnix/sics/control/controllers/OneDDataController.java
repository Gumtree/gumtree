package org.gumtree.gumnix.sics.control.controllers;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.gumtree.gumnix.sics.control.ControllerMap;
import org.gumtree.gumnix.sics.control.IControllerKey;
import org.gumtree.gumnix.sics.control.IControllerMap;
import org.gumtree.gumnix.sics.control.events.DynamicControllerListenerAdapter;
import org.gumtree.gumnix.sics.control.events.IDynamicControllerListener;
import org.gumtree.gumnix.sics.core.SicsCoreException;
import org.gumtree.gumnix.sics.io.SicsIOException;

import ch.psi.sics.hipadaba.Component;

public class OneDDataController extends ComplexController implements IOneDDataController {

	private enum ComponentNode implements IControllerKey {
		DIM("/dim"), AXIS("/axis"), DATA("/data"), LAST_AXIS("/lastaxis"), LAST_DATA("/lastdata"), POINT("/point");
		private ComponentNode(String relativePath) {
			this.relativePath = relativePath;
		}
		public String getRelativePath() {
			return relativePath;
		}
		private String relativePath;
	}

	private float[] dataset;

	private float[] axisset;

	private Integer dimension;

	private IDynamicControllerListener listener;

	private IControllerMap controllerMap;

	public OneDDataController(Component component) {
		super(component);
	}
	
	public void preInitialise() {
	}

	public void postInitialise() {
	}
	
	public void activate() {
		try {
			initialiseListeners();
		} catch (SicsIOException e) {
			getLogger().error("Cannot initialise listeners.", e);
		} catch (SicsCoreException e) {
			getLogger().error("Cannot initialise listeners.", e);
		}
	}

	private void initialiseListeners() throws SicsIOException, SicsCoreException {
		listener = new DynamicControllerListenerAdapter() {
			public void valueChanged(final IDynamicController controller, final IComponentData newValue) {
				SafeRunner.run(new ISafeRunnable() {
					public void handleException(Throwable exception) {
					}
					public void run() throws Exception {
						System.out.println(controller.getPath() + " changed");
//						if(controller.equals(getControllerMap().getController(ComponentNode.DIM))) {
//							if(newValue.getIntData() == dimension.intValue()) {
//								System.out.println("no dim change");
//							} else {
//								System.out.println("dim changed");
//								dimension = newValue.getIntData();
//							}
//						}
					}
				});

			}
		};
		for(ComponentNode node : ComponentNode.values()) {
			getControllerMap().getController(node).addComponentListener(listener);
		}
	}

	public int getRank() {
		return 1;
	}

	public float[] getDataset() throws SicsIOException, SicsCoreException {
		return getDataset(false);
	}

	public float[] getDataset(boolean update) throws SicsIOException, SicsCoreException {
		if(update || dataset == null) {
			dataset = getControllerMap().getFloatArray(ComponentNode.DATA);
		}
		return dataset;
	}

	public float[] getAxisset() throws SicsIOException, SicsCoreException {
		return getAxisset(false);
	}

	public float[] getAxisset(boolean update) throws SicsIOException, SicsCoreException {
		if(update || axisset == null) {
			axisset = getControllerMap().getFloatArray(ComponentNode.AXIS);
		}
		return axisset;
	}

	public int getDimension() throws SicsIOException, SicsCoreException {
		if(dimension == null) {
			dimension = getControllerMap().getIntData(ComponentNode.DIM);
		}
		return dimension;
	}

	private IControllerMap getControllerMap() throws SicsIOException {
		if(controllerMap == null) {
			controllerMap = new ControllerMap(this);
		}
		return controllerMap;
	}

	public String toString() {
		return "[OneDDataController] : " + getPath();
	}

}
