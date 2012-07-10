package org.gumtree.gumnix.sics.ui.componentview;

import java.awt.Color;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.gumtree.gumnix.sics.control.controllers.ComponentDataFormatException;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.control.controllers.IOneDDataController;
import org.gumtree.gumnix.sics.control.events.DynamicControllerCallbackAdapter;
import org.gumtree.gumnix.sics.control.events.DynamicControllerListenerAdapter;
import org.gumtree.gumnix.sics.control.events.IDynamicControllerCallback;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.vis.core.plot1d.PlotData1D;
import org.gumtree.vis.ui.onedplot.OneDVis;

public class OneDGraphicDataViewContent implements IComponentViewContent {

	private IOneDDataController controller;

	private FormToolkit toolkit;

	private OneDVis oneDVis;

	private IDynamicControllerCallback valueCallback;

	private float[] data;

	private float[] axis;

	public void createPartControl(Composite parent, IComponentController controller) {
		Assert.isLegal(controller instanceof IOneDDataController);
		this.controller = (IOneDDataController)controller;
		toolkit = new FormToolkit(parent.getDisplay());
		parent.setLayout(new FillLayout());
		createOneDGraph(parent);

		IDynamicController pointController = (IDynamicController)SicsCore.getSicsController().findComponentController(controller.getPath() + "/point");
		pointController.addComponentListener(new DynamicControllerListenerAdapter() {
			public void valueChanged(IDynamicController controller, IComponentData newValue) {
				try {
					IDynamicController axisController = (IDynamicController)SicsCore.getSicsController().findComponentController(getController().getPath() + "/axis");
					axisController.getValue(getValueCallback());
					IDynamicController dataController = (IDynamicController)SicsCore.getSicsController().findComponentController(getController().getPath() + "/data");
					dataController.getValue(getValueCallback());
				} catch (SicsIOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void createOneDGraph(Composite parent) {
		oneDVis = new OneDVis(parent, SWT.NONE);
	}

	public void dispose() {
	}

	public IOneDDataController getController() {
		return controller;
	}

	private FormToolkit getToolkit() {
		return toolkit;
	}

	private IDynamicControllerCallback getValueCallback() {
		if(valueCallback == null) {
			valueCallback = new DynamicControllerCallbackAdapter() {
				public void handleGetValueCallback(IDynamicController controller, IComponentData value) {
					try {
						if(controller.getComponent().getId().equals("axis")) {
//							axis = value.getFloatArrayData();
							axis = controller.getValue(true).getFloatArrayData();
							System.out.println("Axis:");
							for(float f : axis) {
								System.out.println(f);
							}
							if(data != null && axis.length == data.length) {
								updatePlot();
							}
						} else if(controller.getComponent().getId().equals("data")) {
//							data = value.getFloatArrayData();
							data = controller.getValue(true).getFloatArrayData();
							System.out.println("Data:");
							for(float f : data) {
								System.out.println(f);
							}
							if(axis != null && axis.length == data.length) {
								updatePlot();
							}
						}
					} catch (ComponentDataFormatException e) {
						e.printStackTrace();
					} catch (SicsIOException e) {
						e.printStackTrace();
					}
				}
			};
		}
		return valueCallback;
	}

	private void updatePlot() {
		if(oneDVis != null && !oneDVis.isDisposed()) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					PlotData1D plotData = new PlotData1D(axis, data, null, Color.BLUE, "Beam Monitor Scan Plot");
					System.out.println("updated");
//					PlotData1D data = new PlotData1D(getController().getAxisset(true), getController().getDataset(true), null, Color.BLUE, "Beam Monitor Scan Plot");
					oneDVis.clear();
					oneDVis.setPlotData(plotData);
				}
			});
		}
	}

}
