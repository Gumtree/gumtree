package org.gumtree.gumnix.sics.control.controllers;

import org.gumtree.gumnix.sics.control.ControllerMap;
import org.gumtree.gumnix.sics.control.IControllerKey;
import org.gumtree.gumnix.sics.control.IControllerMap;
import org.gumtree.gumnix.sics.control.events.DynamicControllerListenerAdapter;
import org.gumtree.gumnix.sics.control.events.IComponentControllerListener;
import org.gumtree.gumnix.sics.control.events.IDynamicControllerListener;
import org.gumtree.gumnix.sics.control.events.IScanControllerListener;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.core.SicsCoreException;
import org.gumtree.gumnix.sics.core.SicsUtils;
import org.gumtree.gumnix.sics.io.ISicsProxy;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.util.messaging.SafeListenerRunnable;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.Property;

public class ScanController extends CommandController implements
		IScanController {

	private enum ComponentNode implements IControllerKey {
		// Control variables
		SCAN_VARIABLE("/scan_variable"),
		SCAN_START("/scan_start"),
		SCAN_INCREMENT("/scan_increment"),
		NP("/NP"),
		MODE("/mode"),
		PRESET("/preset"),
		CHANNEL("/channel"),
		// Feedback variables
		FEEDBACK_FILENAME("/feedback/filename"),
		FEEDBACK_MODE("/feedback/mode"),
		FEEDBACK_PRESET("/feedback/preset"),
		FEEDBACK_SCAN_VARIABLE_VALUE("/feedback/scan_variable/value"),
		FEEDBACK_NP("/feedback/NP/current"),
		FEEDBACK_COUNTS("/feedback/counts");
		private ComponentNode(String relativePath) {
			this.relativePath = relativePath;
		}
		public String getRelativePath() {
			return relativePath;
		}
		private String relativePath;
	}

	private IScanConfig config;

	private IScanStatus status;

	private IControllerMap controllerMap;

	private IDynamicControllerListener configListener;

	private IDynamicControllerListener statusListener;

	private String[] modes;

	public ScanController(Component component) {
		super(component);
	}

	public void activate() {
		// TODO: proper error handling
		try {
			initialiseListeners();
		} catch (SicsIOException e) {
			getLogger().error("Cannot initialise listeners.", e);
		} catch (SicsCoreException e) {
			getLogger().error("Cannot initialise listeners.", e);
		}
	}

	public IScanConfig config() {
		if(config == null) {
			config = new ScanConfig();
		}
		return config;
	}

	public IScanStatus status() {
		if(status == null) {
			status = new ScanStatus();
		}
		return status;
	}

	private IControllerMap getControllerMap() {
		if(controllerMap == null) {
			controllerMap = new ControllerMap(this);
		}
		return controllerMap;
	}

	private void initialiseListeners() throws SicsIOException, SicsCoreException {
		configListener = new DynamicControllerListenerAdapter() {
			public void valueChanged(IDynamicController controller, IComponentData newValue) {
				getListenerManager().asyncInvokeListeners(
					new SafeListenerRunnable<IComponentControllerListener>() {
						public void run(IComponentControllerListener listener) throws Exception {
							if(listener instanceof IScanControllerListener) {
								config().update();
								((IScanControllerListener) listener).scanConfigUpdated();
							}
						}
				});
			}
		};
		getControllerMap().getController(ComponentNode.SCAN_VARIABLE).addComponentListener(configListener);
		getControllerMap().getController(ComponentNode.SCAN_START).addComponentListener(configListener);
		getControllerMap().getController(ComponentNode.SCAN_INCREMENT).addComponentListener(configListener);
		getControllerMap().getController(ComponentNode.NP).addComponentListener(configListener);
		getControllerMap().getController(ComponentNode.MODE).addComponentListener(configListener);
		getControllerMap().getController(ComponentNode.PRESET).addComponentListener(configListener);
		getControllerMap().getController(ComponentNode.CHANNEL).addComponentListener(configListener);
		statusListener = new DynamicControllerListenerAdapter() {
			public void valueChanged(IDynamicController controller, IComponentData newValue) {
				getListenerManager().asyncInvokeListeners(
					new SafeListenerRunnable<IComponentControllerListener>() {
						public void run(IComponentControllerListener listener) throws Exception {
							if(listener instanceof IScanControllerListener) {
								((IScanControllerListener) listener).scanStatusUpdated();
							}
						}
				});
			}
		};
		getControllerMap().getController(ComponentNode.FEEDBACK_FILENAME).addComponentListener(statusListener);
		getControllerMap().getController(ComponentNode.FEEDBACK_MODE).addComponentListener(statusListener);
		getControllerMap().getController(ComponentNode.FEEDBACK_PRESET).addComponentListener(statusListener);
		getControllerMap().getController(ComponentNode.FEEDBACK_SCAN_VARIABLE_VALUE).addComponentListener(statusListener);
		getControllerMap().getController(ComponentNode.FEEDBACK_NP).addComponentListener(statusListener);
		getControllerMap().getController(ComponentNode.FEEDBACK_COUNTS).addComponentListener(statusListener);
		//TODO: should I remove listener when it is disposed?
	}

	private class ScanConfig implements IScanConfig {
		private String mode;
		private String variable;
		private float startValue;
		private float increment;
		private int numberOfPoint;
		private float preset;
		private int channel;
		private String scanVariableType;
		private Integer minCount;
		private Integer maxCount;

		private ScanConfig() {
			try {
				update();
			} catch (SicsIOException e) {
				e.printStackTrace();
			} catch (SicsCoreException e) {
				e.printStackTrace();
			}
		}

		public String getMode() {
			return mode;
		}
		public void setMode(String mode) {
			this.mode = mode;
		}
		public String getVariable() {
			return variable;
		}
		public void setVariable(String variable) {
			this.variable = variable;
		}
		public float getStartValue() {
			return startValue;
		}
		public void setStartValue(float startValue) {
			this.startValue = startValue;
		}
		public float getIncrement() {
			return increment;
		}
		public void setIncrement(float increment) {
			this.increment = increment;
		}
		public int getNumberOfPoint() {
			return numberOfPoint;
		}
		public void setNumberOfPoint(int numberOfPoint) {
			this.numberOfPoint = numberOfPoint;
		}
		public float getPreset() {
			return preset;
		}
		public void setPreset(float preset) {
			this.preset = preset;
		}
		public int getChannel() {
			return channel;
		}
		public void setChannel(int channel) {
			this.channel = channel;
		}

		public String[] getAvailableModes() throws SicsCoreException {
			if(modes == null) {
				Property property = SicsUtils.getProperty(getControllerMap().getComponent(ComponentNode.MODE), "values");
				if(property != null) {
					modes = (String[])property.getValue().toArray(new String[property.getValue().size()]);
				} else {
					throw new SicsCoreException("Property does not existed.");
				}
			}
			return modes;
		}

		public int getMaximumChannelSize() throws SicsCoreException {
			if(maxCount == null) {
				try {
					maxCount = Integer.parseInt(SicsUtils.getPropertyFirstValue(getControllerMap().getComponent(ComponentNode.CHANNEL), "max"));
				} catch (NumberFormatException e) {
					throw new SicsCoreException("Property is not a valid integer number.", e);
				}
			}
			return maxCount;
		}

		public int getMinimumChannelSize() throws SicsCoreException {
			if(minCount == null) {
				try {
					minCount = Integer.parseInt(SicsUtils.getPropertyFirstValue(getControllerMap().getComponent(ComponentNode.CHANNEL), "min"));
				} catch (NumberFormatException e) {
					throw new SicsCoreException("Property is not a valid integer number.", e);
				}
			}
			return minCount;
		}

		public String getScanVariableType() throws SicsCoreException{
			if(scanVariableType == null) {
				scanVariableType = SicsUtils.getPropertyFirstValue(getControllerMap().getComponent(ComponentNode.SCAN_VARIABLE), "argtype");
			}
			return scanVariableType;
		}

		public synchronized void commit() throws SicsIOException, SicsCoreException {
			getControllerMap().setStringData(ComponentNode.MODE, getMode());
			getControllerMap().setStringData(ComponentNode.SCAN_VARIABLE, getVariable());
			getControllerMap().setFloatData(ComponentNode.SCAN_START, getStartValue());
			getControllerMap().setFloatData(ComponentNode.SCAN_INCREMENT, getIncrement());
			getControllerMap().setIntData(ComponentNode.NP, getNumberOfPoint());
			getControllerMap().setFloatData(ComponentNode.PRESET, getPreset());
			getControllerMap().setIntData(ComponentNode.CHANNEL, getChannel());
		}

		public void update() throws SicsIOException, SicsCoreException {
			setMode(getControllerMap().getStringData(ComponentNode.MODE));
			setVariable(getControllerMap().getStringData(ComponentNode.SCAN_VARIABLE));
			setStartValue(getControllerMap().getFloatData(ComponentNode.SCAN_START));
			setIncrement(getControllerMap().getFloatData(ComponentNode.SCAN_INCREMENT));
			setNumberOfPoint(getControllerMap().getIntData(ComponentNode.NP));
			setPreset(getControllerMap().getFloatData(ComponentNode.PRESET));
			setChannel(getControllerMap().getIntData(ComponentNode.CHANNEL));
		}

//		public int getRepeat() {
//			// TODO Auto-generated method stub
//			return 0;
//		}
//
//		public void setRepeat(int times) {
//			// TODO Auto-generated method stub
//
//		}

	}

	private class ScanStatus implements IScanStatus {
		public String getFilename() throws SicsIOException, SicsCoreException {
			return getControllerMap().getStringData(ComponentNode.FEEDBACK_FILENAME);
		}

		public String getMode() throws SicsIOException, SicsCoreException {
			return getControllerMap().getStringData(ComponentNode.FEEDBACK_MODE);
		}

		public float getPreset() throws SicsIOException, SicsCoreException {
			return getControllerMap().getFloatData(ComponentNode.FEEDBACK_PRESET);
		}

		public float getScanVariableValue() throws SicsIOException, SicsCoreException {
			return getControllerMap().getFloatData(ComponentNode.FEEDBACK_SCAN_VARIABLE_VALUE);
		}

		public int getCurrentScanPoint() throws SicsIOException, SicsCoreException {
			return getControllerMap().getIntData(ComponentNode.FEEDBACK_NP);
		}

		public int getCount() throws SicsIOException, SicsCoreException {
			return getControllerMap().getIntData(ComponentNode.FEEDBACK_COUNTS);
		}
	}

	public void asyncExecute() throws SicsIOException {
		// run scan on a different channel
		SicsCore.getDefaultProxy().send("hset " + getPath() + " start", null, ISicsProxy.CHANNEL_SCAN);
//		SicsCore.getDefaultProxy().send("hset " + getPath() + " start", null);
	}

	public String toString() {
		return "[ScanController] : " + getPath();
	}

	public static boolean hasValidChildComponents(Component component) {
		for(ComponentNode node : ComponentNode.values()) {
			if(SicsUtils.getDescendantComponent(component, node.getRelativePath()) == null) {
				return false;
			}
		}
		return true;
	}

}
