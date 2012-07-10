package org.gumtree.data.ui.viewers;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.data.IDataProvider;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.vis.gdm.utils.Factory;
import org.gumtree.vis.interfaces.IDataset;
import org.gumtree.vis.interfaces.IPlot;
import org.gumtree.vis.interfaces.IPlot1D;
import org.gumtree.vis.swt.PlotComposite;

public class ArrayViewer extends ExtendedComposite {

	public enum ViewerLayout {
		HORIZONTAL, VERTICAL, AUTOMATIC
	}

	private ViewerLayout viewerLayout;

	private IArray array;

	private int frame = -1;

	private int layer = -1;
	
	private String name;
	
	private Composite tableAreaComposite;
	
	private Composite plotAreaComposite;

	public ArrayViewer(Composite parent, int style) {
		super(parent, style);
		viewerLayout = ViewerLayout.AUTOMATIC;

		setLayout(new FillLayout());
		SashForm sashForm = getWidgetFactory().createSashForm(this, SWT.HORIZONTAL);

		tableAreaComposite = getWidgetFactory().createComposite(sashForm);
		plotAreaComposite = getWidgetFactory().createComposite(sashForm);

		sashForm.setWeights(new int[] { 1, 1 });
	}

	public void setArray(IArray array) {
		setArray(array, 0, 0, "data");
	}
	
	public void setArray(IArray array, String name) {
		setArray(array, 0, 0, name);
	}
	
	public void setArray(IArray array, int frame, int layer) {
		setArray(array, frame, layer, "data");
	}
	
	public void setArray(IArray array, int frame, int layer, String name) {
		// Update only if input is changed
		if (array != null && !array.equals(this.array)
				&& !(this.frame == frame && this.layer == layer)) {
			this.array = array;
			this.frame = frame;
			this.layer = layer;
			this.name = name;
			Display display = Display.getDefault();
			if (display == null || display.isDisposed()) {
				return;
			}
			display.asyncExec(new Runnable() {
				public void run() {
					updateViewer();
				}
			});
		}
	}

	private void updateViewer() {
		// Dispose old controls
		if (tableAreaComposite == null) {
			return;
		}
		for (Control child : tableAreaComposite.getChildren()) {
			child.dispose();
		}
		tableAreaComposite.setLayout(new FillLayout());
		
		if (array.getRank() == 1) {
			NatTable natTable = new NatTable(tableAreaComposite, new ArrayGridLayer(new IDataProvider() {
				public void setDataValue(int columnIndex, int rowIndex,
						Object newValue) {
					throw new UnsupportedOperationException();
				}

				public int getRowCount() {
					return array.getShape()[0];
				}

				public Object getDataValue(int columnIndex, int rowIndex) {
					return array.getObject(array.getIndex().set(rowIndex)).toString();
				}

				public int getColumnCount() {
					return 1;
				}
			}));
		} else if (array.getRank() == 2) {
			NatTable natTable = new NatTable(tableAreaComposite, new ArrayGridLayer(new IDataProvider() {
				public void setDataValue(int columnIndex, int rowIndex,
						Object newValue) {
					throw new UnsupportedOperationException();
				}

				public int getRowCount() {
					return array.getShape()[0];
				}

				public Object getDataValue(int columnIndex, int rowIndex) {
					return array.getObject(array.getIndex().set(rowIndex, columnIndex)).toString();
				}

				public int getColumnCount() {
					return array.getShape()[1];
				}
			}));
		} else if (array.getRank() == 3) {
			NatTable natTable = new NatTable(tableAreaComposite, new ArrayGridLayer(new IDataProvider() {
				public void setDataValue(int columnIndex, int rowIndex,
						Object newValue) {
					throw new UnsupportedOperationException();
				}

				public int getRowCount() {
					return array.getShape()[1];
				}

				public Object getDataValue(int columnIndex, int rowIndex) {
					return array.getObject(array.getIndex().set(layer, rowIndex, columnIndex)).toString();
				}

				public int getColumnCount() {
					return array.getShape()[2];
				}
			}));
		} else if (array.getRank() == 4) {
			NatTable natTable = new NatTable(tableAreaComposite, new ArrayGridLayer(new IDataProvider() {
				public void setDataValue(int columnIndex, int rowIndex,
						Object newValue) {
					throw new UnsupportedOperationException();
				}

				public int getRowCount() {
					return array.getShape()[2];
				}

				public Object getDataValue(int columnIndex, int rowIndex) {
					return array.getObject(array.getIndex().set(frame, layer, rowIndex, columnIndex)).toString();
				}

				public int getColumnCount() {
					return array.getShape()[3];
				}
			}));
		}
		
		// Dispose old controls
		for (Control child : plotAreaComposite.getChildren()) {
			child.dispose();
		}
		plotAreaComposite.setLayout(new FillLayout());
		try {
			if (array.getRank() <= 2) {
				IDataset visDataset = createPlotDataset();
				PlotComposite plotComposite = new PlotComposite(plotAreaComposite, SWT.NONE);
				plotComposite.setDataset(visDataset);
				IPlot plot = plotComposite.getPlot();
				setErrorBarEnabled(plot);
			} else {
				IDataset visDataset = createPlotDataset();
				PlotComposite plotComposite = new PlotComposite(plotAreaComposite, SWT.NONE);
				plotComposite.setDataset(visDataset);
			}
		} catch (ShapeNotMatchException e) {
			e.printStackTrace();
		}
		
		tableAreaComposite.layout(true, true);
		plotAreaComposite.layout(true, true);
	}

	protected void setErrorBarEnabled(IPlot plot) {
		if (plot instanceof IPlot1D) {
			((IPlot1D) plot).setErrorBarEnabled(false);
		}
	}

	public ViewerLayout getViewerLayout() {
		return viewerLayout;
	}

	public void setViewerLayout(ViewerLayout viewerLayout) {
		this.viewerLayout = viewerLayout;
	}

	protected IDataset createPlotDataset() throws ShapeNotMatchException {
		if (array.getRank() <= 2) {
			return Factory.createDataset(name, array);
		} else {
			return Factory.create2DDataset(array, frame, layer);
		}
	}
	
	public IArray getArray() {
		return array;
	}
	
	@Override
	protected void disposeWidget() {
		viewerLayout = null;
		array = null;
		
		if (tableAreaComposite != null && !tableAreaComposite.isDisposed()) {
			for (Control child : tableAreaComposite.getChildren()) {
				child.dispose();
			}
			tableAreaComposite.dispose();
			tableAreaComposite = null;
		}
		if (plotAreaComposite != null && !plotAreaComposite.isDisposed()) {
			for (Control child : plotAreaComposite.getChildren()) {
				child.dispose();
			}
			plotAreaComposite.dispose();
			plotAreaComposite = null;
		}
	}

	public int getFrameId() {
		return frame;
	}
	
	public int getLayerId() {
		return layer;
	}
}
