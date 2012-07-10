/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.ui.plot;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.vis.dataset.XYErrorDataset;
import org.gumtree.vis.gdm.dataset.Hist2DDataset;
import org.gumtree.vis.interfaces.IHist2D;
import org.gumtree.vis.interfaces.IPlot;
import org.gumtree.vis.interfaces.IPlot1D;
import org.gumtree.vis.interfaces.IXYErrorDataset;
import org.gumtree.vis.interfaces.IXYErrorSeries;
import org.gumtree.vis.mask.AbstractMask;
import org.gumtree.vis.mask.IMaskEventListener;
import org.gumtree.vis.swt.PlotComposite;

import au.gov.ansto.bragg.cicada.core.Exporter;
import au.gov.ansto.bragg.cicada.core.exception.ExportException;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.kakadu.core.UIAlgorithmManager;
import au.gov.ansto.bragg.kakadu.core.data.DataType;
import au.gov.ansto.bragg.kakadu.core.data.Operation;
import au.gov.ansto.bragg.kakadu.core.data.OperationStatus;
import au.gov.ansto.bragg.kakadu.ui.SWTResourceManager;
import au.gov.ansto.bragg.kakadu.ui.region.RegionEventListener;
import au.gov.ansto.bragg.kakadu.ui.region.RegionParameter;
import au.gov.ansto.bragg.kakadu.ui.util.Util;

/**
 * The class provides controller for data visualization. 
 * 
 * @author Danil Klimontov (dak)
 */
public class Plot extends Composite {
	final private static String IS_SKIP_ENABLED_PROPERTY_LABLE = "kakadu.plot.isSkipEnabled";
	private static int idCounter = 0;
	private int id;
	
	private EmptyPlotComposite emptyPlotComposit;
	private SchemaPlotComposite schemaPlotComposite;
	private CalculationPlotComposite calculationPlotComposite;
	private StatusBarComposite statusBarComposite;
	
	private IPlot currentKurandaWidget;
	private PlotType currentPlotType = PlotType.EmptyPlot;
	private DataType currentDataType = DataType.Undefined;
	
	private Composite propertiesComposite;
	private ScrolledComposite scrolledComposite;
	private Composite plotAreaComposite;
	private PlotComposite plotComposite;
//	private ToolItem propertiesToolItem;
//	private ToolItem viewControlToolItem;
	private ToolItem fittingControlToolItem;
	private ToolItem maskControlToolItem;
	private ToolItem exportPictureToolItem;
	private ToolItem exportDataToolItem;
//	private ToolItem intervalToolItem;
	private ToolItem printToolItem;
//	private ExpandBar plotPropertiesExpandBar;
	private SashForm sashForm;
	private SashForm innerSash;
	private StackLayout plotLayout;
	private Operation operaton;
//	private 
	
	private boolean isInnerPlotRegionProcessing = false;
	private boolean isInnerUIRegionProcessing = false;
	private boolean isMaskingEnabled = false;
	private boolean isSchemaMode;
	private boolean isExportMenuInitialized = false;
	private String forceTitle = null;
	private String title;
//	private ParameterRegionManager parameterRegionManager;
	private ToolBar plotToolsToolbar;
	private ToolBar plotDataToolbar;
	private ToolBar navigateDataToolbar;
	private boolean isQuickRemoveEnabled = false;
	private boolean isIntervalEnabled = false;
	private RegionParameter regionParameter;
	private RegionEventListener regionEventListener;
	private final MultiPlotDataManager multiPlotDataManager = new MultiPlotDataManager(this);

	/** P <plotRegionId, uiRegion> */
	private final Map<String, AbstractMask> maskMap = new HashMap<String, AbstractMask>();
	final private List<PlotPropertyChangeListener> plotPropertyChangeListeners = new ArrayList<PlotPropertyChangeListener>();
	

	private PlotRegionListener plotRegionListener = new PlotRegionListener();
//	final ChartMouseListener pointLocatorListener = new ChartMouseListener() {
//		public void locationUpdated(double x, double y, double val) {
//			statusBarComposite.setCursorLocation(x, y, val);
//		}
//
//		@Override
//		public void chartMouseClicked(ChartMouseEvent event) {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public void chartMouseMoved(ChartMouseEvent event) {
//			// TODO Auto-generated method stub
//			statusBarComposite.setCursorLocation(event.get);
//		}
//	};
//	private au.gov.ansto.bragg.kakadu.core.data.region.RegionListener uiRegionListener = 
//		new au.gov.ansto.bragg.kakadu.core.data.region.RegionListener() {
//		
//		public void regionAdded(UIRegion region) {
//			if (!isInnerUIRegionProcessing) {
//				addMask(region);
//			}
//		}
//		public void regionRemoved(UIRegion region) {
//			if (!isInnerUIRegionProcessing) {
//				removeMask(region);
//			}
//		}
//		public void regionUpdated(UIRegion region) {
//			if (!isInnerUIRegionProcessing) {
//				updateMask(region);
//			}
//		}
//	};

	private ViewPlotPropertiesComposite viewPlotPropertiesComposite;
	private FitPlotPropertiesComposite fitPlotPropertiesComposite;
	private MaskPlotPropertiesComposite maskPlotPropertiesComposite;
//	private StatisticPlotPropertiesComposite statisticPlotPropertiesComposite;
//	private MultiPlotPropertiesComposite multiPlotPropertiesComposite;

//	private ExpandItem dataExpandItem;
//	private ExpandItem maskExpandItem;
//	private ExpandItem viewExpandItem;
//	private ExpandItem fitExpandItem;
//	private ExpandItem statisticExpandItem;
	private Menu exportDataMenu;
	private ToolItem resetPlotToolItem;
	private ToolItem logarithmToolItem;
//	private ToolItem backgroundColorToolItem;
	private ToolItem copyToolItem;
//	private ToolItem navigateBackToolItem;
//	private ToolItem navigateForwardToolItem;

	/**
	 * Creates a new Plot instance.
	 * @param parent parent Composite.
	 * @param style SWT styles for the Plot object.
	 * @param algorithmTask algorithm task for the plot.
	 */
	public Plot(Composite parent, int style) {
		super(parent, style);
//		Rectangle size = get;
//		int width = size.width;
//		int height = size.height;
//		if (width >height)
//			size.width = height;
//		else 
//			size.height = width;
//		this.setBounds(size);
		id = idCounter++;
		SWTResourceManager.registerResourceUser(parent);
		String isInveralEnabledProperty = System.getProperty(IS_SKIP_ENABLED_PROPERTY_LABLE);
		if (isInveralEnabledProperty != null){
			isIntervalEnabled = Boolean.valueOf(isInveralEnabledProperty);
		} 
	}
	
	/**
	 * Gets an unique id of the plot. 
	 * @return id of the plot.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Initialize all UI components and listeners.
	 * The method must be called manually.
	 * 
	 * @param algorithmTask
	 */
	public void init(PlotType plotType) {

		initUI(plotType);
		initListeners();
		// [Tony] [2008-12-12] Set plot type during init phrase
		setPlotType(plotType);
//		initExportAction();
		layout();
	}


	protected void initUI(PlotType plotType) {
		GridLayout gridLayout = new GridLayout ();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		setLayout (gridLayout);

		GridData data = null;
		Composite controlAreaComposite = new Composite(this, SWT.NONE);
		GridLayout topCompositeLayout = new GridLayout ();
		topCompositeLayout.marginWidth = 0;
		topCompositeLayout.marginHeight = 0;
		topCompositeLayout.verticalSpacing = 0;
		topCompositeLayout.horizontalSpacing = 0;
		topCompositeLayout.numColumns = 2;
		controlAreaComposite.setLayout (topCompositeLayout);
		
		plotDataToolbar = new ToolBar (controlAreaComposite, SWT.HORIZONTAL);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessVerticalSpace = true;
		data.grabExcessHorizontalSpace = true;
		plotDataToolbar.setLayoutData (data);
		
//		propertiesToolItem = new ToolItem (plotDataToolbar, SWT.CHECK);
////		propertiesToolItem.setText ("Plot Properties");
//		propertiesToolItem.setText ("Data");
//		propertiesToolItem.setToolTipText("Control the plot data");
//
//		viewControlToolItem = new ToolItem (plotDataToolbar, SWT.CHECK);
//		viewControlToolItem.setText("View");
//		viewControlToolItem.setToolTipText("Control the view properties");

		fittingControlToolItem = new ToolItem (plotDataToolbar, SWT.CHECK);
		fittingControlToolItem.setText("Fitting");
		fittingControlToolItem.setToolTipText("Control curve fitting");

		maskControlToolItem = new ToolItem (plotDataToolbar, SWT.CHECK);
		maskControlToolItem.setText("Mask");
		maskControlToolItem.setToolTipText("ROI and mask control");

//		if (isIntervalEnabled){
//			intervalToolItem = new ToolItem (plotDataToolbar, SWT.NONE);
//			intervalToolItem.setText("Skip");
//			intervalToolItem.setToolTipText("Set skip frequency");
//			final Menu intervalMenu = new Menu(intervalToolItem.getParent());
//			MenuItem nullIntervalMenuItem = new MenuItem(intervalMenu, SWT.RADIO);
//			nullIntervalMenuItem.setText("none");
//			nullIntervalMenuItem.setSelection(true);
//			nullIntervalMenuItem.addSelectionListener(new SelectionListener() {
//				
//				@Override
//				public void widgetSelected(SelectionEvent e) {
////					currentKurandaWidget.setHorizontalInterval(0);
//					try {
////						currentKurandaWidget.refreshPlot();
//						
//					} catch (Exception e1) {
//						handleException(e1);
//					}
//				}
//				
//				@Override
//				public void widgetDefaultSelected(SelectionEvent e) {
//				}
//			});
//			MenuItem skip1MenuItem = new MenuItem(intervalMenu, SWT.RADIO);
//			skip1MenuItem.setText("next 1");
//			skip1MenuItem.addSelectionListener(new SelectionListener() {
//				
//				@Override
//				public void widgetSelected(SelectionEvent e) {
////					currentKurandaWidget.setHorizontalInterval(0);
//					try {
////						currentKurandaWidget.refreshPlot();
//						
//					} catch (Exception e1) {
//						handleException(e1);
//					}				}
//				
//				@Override
//				public void widgetDefaultSelected(SelectionEvent e) {
//				}
//			});
//			MenuItem skip2MenuItem = new MenuItem(intervalMenu, SWT.RADIO);
//			skip2MenuItem.setText("next 2");
//			skip2MenuItem.addSelectionListener(new SelectionListener() {
//				
//				@Override
//				public void widgetSelected(SelectionEvent e) {
////					currentKurandaWidget.setHorizontalInterval(0);
//					try {
////						currentKurandaWidget.refreshPlot();
//						
//					} catch (Exception e1) {
//						handleException(e1);
//					}				}
//				
//				@Override
//				public void widgetDefaultSelected(SelectionEvent e) {
//				}
//			});
//			MenuItem skip3MenuItem = new MenuItem(intervalMenu, SWT.RADIO);
//			skip3MenuItem.setText("next 3");
//			skip3MenuItem.addSelectionListener(new SelectionListener() {
//				
//				@Override
//				public void widgetSelected(SelectionEvent e) {
////					currentKurandaWidget.setHorizontalInterval(0);
//					try {
////						currentKurandaWidget.refreshPlot();
//						
//					} catch (Exception e1) {
//						handleException(e1);
//					}
//				}
//				
//				@Override
//				public void widgetDefaultSelected(SelectionEvent e) {
//				}
//			});
//			MenuItem skip4MenuItem = new MenuItem(intervalMenu, SWT.RADIO);
//			skip4MenuItem.setText("next 4");
//			skip4MenuItem.addSelectionListener(new SelectionListener() {
//				
//				@Override
//				public void widgetSelected(SelectionEvent e) {
////					currentKurandaWidget.setHorizontalInterval(0);
//					try {
////						currentKurandaWidget.refreshPlot();
//						
//					} catch (Exception e1) {
//						handleException(e1);
//					}
//				}
//				
//				@Override
//				public void widgetDefaultSelected(SelectionEvent e) {
//				}
//			});
//			MenuItem skip5MenuItem = new MenuItem(intervalMenu, SWT.RADIO);
//			skip5MenuItem.setText("next 5");
//			skip5MenuItem.addSelectionListener(new SelectionListener() {
//				
//				@Override
//				public void widgetSelected(SelectionEvent e) {
////					currentKurandaWidget.setHorizontalInterval(0);
//					try {
////						currentKurandaWidget.refreshPlot();
//						
//					} catch (Exception e1) {
//						handleException(e1);
//					}
//				}
//				
//				@Override
//				public void widgetDefaultSelected(SelectionEvent e) {
//				}
//			});
//			MenuItem skip6MenuItem = new MenuItem(intervalMenu, SWT.RADIO);
//			skip6MenuItem.setText("next 6");
//			skip6MenuItem.addSelectionListener(new SelectionListener() {
//				
//				@Override
//				public void widgetSelected(SelectionEvent e) {
////					currentKurandaWidget.setHorizontalInterval(0);
//					try {
////						currentKurandaWidget.refreshPlot();
//						
//					} catch (Exception e1) {
//						handleException(e1);
//					}
//				}
//				
//				@Override
//				public void widgetDefaultSelected(SelectionEvent e) {
//				}
//			});
//			intervalToolItem.addSelectionListener(new SelectionListener() {
//				
//				@Override
//				public void widgetSelected(SelectionEvent e) {
//					ToolItem item = (ToolItem) e.widget;
//					Rectangle rect = item.getBounds();
//					Point pt = item.getParent()
//							.toDisplay(new Point(rect.x, rect.y));
//					intervalMenu.setLocation(pt.x, pt.y + rect.height);
//					intervalMenu.setVisible(true);					
//				}
//				
//				@Override
//				public void widgetDefaultSelected(SelectionEvent e) {
//				}
//			});
//		}
		
		exportDataToolItem = new ToolItem (plotDataToolbar, SWT.NONE);
		exportDataToolItem.setText("Export");
		exportDataToolItem.setToolTipText("Export selected Plot Data Item to file");
//		exportDataToolItem.setImage(SWTResourceManager.getImage("icons/export.gif", plotToolbar));
		
		exportDataMenu = new Menu(exportDataToolItem.getParent());

		navigateDataToolbar = new ToolBar (controlAreaComposite, SWT.HORIZONTAL);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		navigateDataToolbar.setLayoutData (data);

//		navigateBackToolItem = new ToolItem (navigateDataToolbar, SWT.NONE);
//		navigateBackToolItem.setToolTipText("Navigate backward");
//		navigateBackToolItem.setImage(SWTResourceManager.getImage("icons/nav_backward.gif"));
//		navigateBackToolItem.setDisabledImage(SWTResourceManager.getImage("icons/nav_backward_dis.gif"));
//		navigateBackToolItem.setEnabled(false);
//		
//		navigateForwardToolItem = new ToolItem (navigateDataToolbar, SWT.NONE);
//		navigateForwardToolItem.setToolTipText("Navigate forward");
//		navigateForwardToolItem.setImage(SWTResourceManager.getImage("icons/nav_forward.gif"));
//		navigateForwardToolItem.setDisabledImage(SWTResourceManager.getImage("icons/nav_forward_dis.gif"));
//		navigateForwardToolItem.setEnabled(false);
		
//		plotDataToolbar.pack();
		//main sash
		sashForm = new SashForm(this, SWT.HORIZONTAL);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		sashForm.setLayoutData (data);
		
		scrolledComposite = new ScrolledComposite(sashForm, SWT.BORDER | SWT.V_SCROLL);
		propertiesComposite = new Composite(scrolledComposite, SWT.NONE);
//		propertiesComposite = new Composite(sashForm, SWT.BORDER | SWT.V_SCROLL);
		GridLayout propertiesCompositeLayout = new GridLayout ();
		propertiesCompositeLayout.marginWidth = 0;
		propertiesCompositeLayout.marginHeight = 0;
		propertiesCompositeLayout.verticalSpacing = 0;
		propertiesCompositeLayout.horizontalSpacing = 0;
		propertiesCompositeLayout.numColumns = 1;
		propertiesComposite.setLayout(propertiesCompositeLayout);
		
		innerSash = new SashForm(propertiesComposite, SWT.VERTICAL);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = false;
		innerSash.setLayoutData (data);
		
//		plotPropertiesExpandBar = new ExpandBar(propertiesComposite, SWT.V_SCROLL);
//		plotPropertiesExpandBar.addMouseWheelListener(new MouseWheelListener(){
//
//			public void mouseScrolled(MouseEvent arg0) {
//				
//				plotPropertiesExpandBar.
//			}
//			
//		});
		maskPlotPropertiesComposite = new MaskPlotPropertiesComposite(innerSash, SWT.NONE, this);
		viewPlotPropertiesComposite = new ViewPlotPropertiesComposite(innerSash, SWT.NONE, this);
		fitPlotPropertiesComposite = new FitPlotPropertiesComposite(innerSash, SWT.NONE, this);
//		statisticPlotPropertiesComposite = new StatisticPlotPropertiesComposite(innerSash, SWT.NONE, this);
		
//		dataExpandItem = new ExpandItem (plotPropertiesExpandBar, SWT.NONE);
//		dataExpandItem.setText("Plot data");
//		dataExpandItem.setHeight(multiPlotPropertiesComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
//		dataExpandItem.setControl(multiPlotPropertiesComposite);
//		dataExpandItem.setExpanded(true);
//
//		viewExpandItem = new ExpandItem (plotPropertiesExpandBar, SWT.NONE);
//		viewExpandItem.setText("View");
//		viewExpandItem.setHeight(viewPlotPropertiesComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
//		viewExpandItem.setControl(viewPlotPropertiesComposite);
//
//		fitExpandItem = new ExpandItem (plotPropertiesExpandBar, SWT.NONE);
//		fitExpandItem.setText("Fitting");
//		fitExpandItem.setHeight(fitPlotPropertiesComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
//		fitExpandItem.setControl(fitPlotPropertiesComposite);
////		fitPlotPropertiesComposite.setExpandItem(fitExpandItem);
//
//		maskExpandItem = new ExpandItem (plotPropertiesExpandBar, SWT.NONE);
//		maskExpandItem.setText("Mask");
//		maskExpandItem.setHeight(maskPlotPropertiesComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
//		maskExpandItem.setControl(maskPlotPropertiesComposite);
//		plotPropertiesExpandBar.addExpandListener(new ExpandListener(){
//
//			public void itemCollapsed(ExpandEvent arg0) {
//				
//				
//			}
//
//			public void itemExpanded(ExpandEvent arg0) {
//				
//				((MaskPlotPropertiesComposite) maskPlotPropertiesComposite).initMaskEnabled();
//			}
//		});
		//			maskExpandItem.setImage(image);

//		updatePlotPropertiesBar();
		

		plotAreaComposite = new Composite(sashForm, SWT.NONE);
		GridLayout rightFormCompositeLayout = new GridLayout ();
		rightFormCompositeLayout.marginWidth = 0;
		rightFormCompositeLayout.marginHeight = 0;
		rightFormCompositeLayout.verticalSpacing = 0;
		rightFormCompositeLayout.horizontalSpacing = 0;
		rightFormCompositeLayout.numColumns = 2;
		plotAreaComposite.setLayout (rightFormCompositeLayout);


		plotToolsToolbar = new ToolBar (plotAreaComposite, SWT.VERTICAL);
		data = new GridData ();
		data.verticalAlignment = GridData.FILL;
		data.grabExcessVerticalSpace = true;
		plotToolsToolbar.setLayoutData (data);

		resetPlotToolItem = new ToolItem (plotToolsToolbar, SWT.NONE);
		resetPlotToolItem.setToolTipText("Reset");
		resetPlotToolItem.setImage(SWTResourceManager.getImage("icons/refresh16x16.png"));

		logarithmToolItem = new ToolItem (plotToolsToolbar, SWT.NONE);
		logarithmToolItem.setToolTipText("Toggle logarithm scale");
		logarithmToolItem.setImage(SWTResourceManager.getImage("icons/logarithm.gif"));

//		backgroundColorToolItem = new ToolItem (plotToolsToolbar, SWT.NONE);
////		backgroundColorToolItem.setToolTipText("Change background colour");
//		backgroundColorToolItem.setImage(SWTResourceManager.getImage("icons/colorscm16x16.png"));

		copyToolItem = new ToolItem (plotToolsToolbar, SWT.NONE);
		copyToolItem.setToolTipText("Copy");
		copyToolItem.setImage(SWTResourceManager.getImage("icons/copy16x16.png"));

		exportPictureToolItem = new ToolItem (plotToolsToolbar, SWT.NONE);
//		exportPictureToolItem.setText ("Export Picture");
		exportPictureToolItem.setToolTipText("Save");
		exportPictureToolItem.setImage(SWTResourceManager.getImage("icons/picture_save16x16.png"));

		printToolItem = new ToolItem (plotToolsToolbar, SWT.NONE);
//		printToolItem.setText("Print");
		printToolItem.setToolTipText("Print");
		printToolItem.setImage(SWTResourceManager.getImage("icons/printer16x16.png"));

//		multiPlotPropertiesComposite = new MultiPlotPropertiesComposite(innerSash, SWT.NONE, this);

//		plotComposite = new Composite(plotAreaComposite, SWT.NONE);
		plotComposite = new PlotComposite(plotAreaComposite, SWT.NONE);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		plotComposite.setLayoutData (data);

		plotLayout = new StackLayout();
		plotComposite.setLayout(plotLayout);

		statusBarComposite = new StatusBarComposite(plotAreaComposite, SWT.BORDER);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 2;
		statusBarComposite.setLayoutData (data);

		initKurandaWidget(plotType);

		emptyPlotComposit = new EmptyPlotComposite(plotComposite, SWT.NONE);
		plotLayout.topControl = emptyPlotComposit;
		
		schemaPlotComposite = new SchemaPlotComposite(plotComposite, SWT.NONE, this);
		calculationPlotComposite = new CalculationPlotComposite(plotComposite, SWT.NONE, this);
		
//		new MultiPlotPropertiesComposite(sashForm, SWT.NONE);
		innerSash.setWeights(new int[]{20, 20, 20});
		innerSash.setMaximizedControl(maskPlotPropertiesComposite);
		propertiesComposite.pack();
		scrolledComposite.setContent(propertiesComposite);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				innerSash.layout();
				propertiesComposite.pack();
				propertiesComposite.layout();
				propertiesComposite.update();
				Rectangle r = scrolledComposite.getClientArea();
				scrolledComposite.setMinSize(innerSash.computeSize(r.width,
						SWT.DEFAULT));
			}
		});
	    
		sashForm.setWeights(new int[]{25, 75});
		sashForm.setMaximizedControl(plotAreaComposite);
		
		updateUIComponents();
	}

	protected void updatePlotPropertiesBar() {
		//TODO review the  method... do we need this modification now?
		switch (currentPlotType) {
		default:
		case EmptyPlot:
//			if (dataExpandItem == null || dataExpandItem.isDisposed()) {
//				dataExpandItem = new ExpandItem (plotPropertiesExpandBar, SWT.NONE);
//			}
//			if (dataExpandItem.getControl() != dataSourcePlotPropertiesComposite) {
//				dataExpandItem.setText("Data");
//				dataExpandItem.setHeight(dataSourcePlotPropertiesComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
//				dataExpandItem.setControl(dataSourcePlotPropertiesComposite);
//				if (multiPlotPropertiesComposite != null && !multiPlotPropertiesComposite.isDisposed()) {
//					multiPlotPropertiesComposite.setSize(0,0);
//				}
//			}

//			if (viewExpandItem == null || viewExpandItem.isDisposed()) {
//				viewExpandItem = new ExpandItem (plotPropertiesExpandBar, SWT.NONE);
//				viewExpandItem.setText("View");
//				viewExpandItem.setHeight(viewPlotPropertiesComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
//				viewExpandItem.setControl(viewPlotPropertiesComposite);
//			}
//			
//			if (fitExpandItem == null || fitExpandItem.isDisposed()) {
//				fitExpandItem = new ExpandItem (plotPropertiesExpandBar, SWT.NONE);
//				fitExpandItem.setText("Fitting");
//				fitExpandItem.setHeight(fitPlotPropertiesComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
//				fitExpandItem.setControl(fitPlotPropertiesComposite);
//			}
//
//			if (maskExpandItem == null || maskExpandItem.isDisposed()) {
//				maskExpandItem = new ExpandItem (plotPropertiesExpandBar, SWT.NONE);
//				maskExpandItem.setText("Mask");
//				maskExpandItem.setHeight(maskPlotPropertiesComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
//				maskExpandItem.setControl(maskPlotPropertiesComposite);
//	//			maskExpandItem.setImage(image);
//			}
//			
//			if (statisticExpandItem == null || statisticExpandItem.isDisposed()) {
//				statisticExpandItem = new ExpandItem (plotPropertiesExpandBar, SWT.NONE);
//				statisticExpandItem.setText("Statistic");
//				statisticExpandItem.setHeight(150);
////			statisticExpandItem.setHeight(statisticPlotPropertiesComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
//				statisticExpandItem.setControl(statisticPlotPropertiesComposite);
//			}

			break;
		case IntensityPlot:
		case OffsetPlot:
		case OverlayPlot:
//			if (dataExpandItem == null || dataExpandItem.isDisposed()) {
//				dataExpandItem = new ExpandItem (plotPropertiesExpandBar, SWT.NONE);
//			}
//			if (dataExpandItem.getControl() != multiPlotPropertiesComposite) {
//				dataExpandItem.setText("Plot data");
//				dataExpandItem.setHeight(multiPlotPropertiesComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
//				dataExpandItem.setControl(multiPlotPropertiesComposite);
////				if (dataSourcePlotPropertiesComposite != null && !dataSourcePlotPropertiesComposite.isDisposed()) {
////					dataSourcePlotPropertiesComposite.setSize(0,0);
////				}
//				dataExpandItem.setExpanded(true);
//			}
//
//			if (viewExpandItem == null || viewExpandItem.isDisposed()) {
//				viewExpandItem = new ExpandItem (plotPropertiesExpandBar, SWT.NONE);
//				viewExpandItem.setText("View");
//				viewExpandItem.setHeight(viewPlotPropertiesComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
//				viewExpandItem.setControl(viewPlotPropertiesComposite);
//			}
//
//			if (fitExpandItem == null || fitExpandItem.isDisposed()) {
//				fitExpandItem = new ExpandItem (plotPropertiesExpandBar, SWT.NONE);
//				fitExpandItem.setText("Fitting");
//				fitExpandItem.setHeight(fitPlotPropertiesComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
//				fitExpandItem.setControl(fitPlotPropertiesComposite);
//			}
////			if (maskExpandItem != null && !maskExpandItem.isDisposed()) {
////				maskExpandItem.dispose();
////				maskPlotPropertiesComposite.setSize(0, 0);
////			}
//			if (maskExpandItem == null || maskExpandItem.isDisposed()) {
//				maskExpandItem = new ExpandItem (plotPropertiesExpandBar, SWT.NONE);
//				maskExpandItem.setText("Mask");
//				maskExpandItem.setHeight(maskPlotPropertiesComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
//				maskExpandItem.setControl(maskPlotPropertiesComposite);
//	//			maskExpandItem.setImage(image);
//			}
//
//
//			if (statisticExpandItem != null && !statisticExpandItem.isDisposed()) {
//				statisticExpandItem.dispose();
//				statisticPlotPropertiesComposite.setSize(0,0);
//			}

			break;
		}
		
//		plotPropertiesExpandBar.layout();
	}

	public void forceEnableMask(){
		maskPlotPropertiesComposite.setMaskEnabled(true);
	}
	
	protected void initListeners() {
//		propertiesToolItem.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
////				if (propertiesToolItem.getSelection()) {
////					sashForm.setMaximizedControl(null);
////				} else {
////					sashForm.setMaximizedControl(plotAreaComposite);
////				}
//				setToolItemSelection(propertiesToolItem);
//			}
//
//		});

//		viewControlToolItem.addSelectionListener(new SelectionListener(){
//
//			public void widgetDefaultSelected(SelectionEvent arg0) {
//				
//				
//			}
//
//			public void widgetSelected(SelectionEvent arg0) {
//				
//				setToolItemSelection(viewControlToolItem);
//			}});
		
		fittingControlToolItem.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				
			}

			public void widgetSelected(SelectionEvent arg0) {
				
				setToolItemSelection(fittingControlToolItem);
			}});

		maskControlToolItem.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				
			}

			public void widgetSelected(SelectionEvent arg0) {
				
				setToolItemSelection(maskControlToolItem);
			}});

		exportPictureToolItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				exportPlotAsPictureToFile();
			}
		});
		
		exportDataToolItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// Determine where to put the dropdown list
				if (! isExportMenuInitialized){
					try {
						initExportAction();
					} catch (ExportException e1) {
						
						handleException(e1);
					}
				}
				ToolItem item = (ToolItem) e.widget;
				Rectangle rect = item.getBounds();
				Point pt = item.getParent()
						.toDisplay(new Point(rect.x, rect.y));
				exportDataMenu.setLocation(pt.x, pt.y + rect.height);
				exportDataMenu.setVisible(true);
			}
		});
		
		printToolItem.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}

			public void widgetSelected(SelectionEvent arg0) {
				Thread newThread = new Thread(new Runnable() {

					@Override
					public void run() {
						currentKurandaWidget.createChartPrintJob();
					}
				});
				newThread.start();
			}});

		logarithmToolItem.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				
			}

			public void widgetSelected(SelectionEvent arg0) {
				if (currentKurandaWidget instanceof IPlot1D) {
//					try {
//						List<AbstractDataSource> dataSourceWrappers = getCurrentPlotWidget().getMultiPlotManager(
//						).getAllDataSourceWrappers();
//						for (AbstractDataSource dataSourceWrapper : dataSourceWrappers){
//							if (dataSourceWrapper instanceof PlotData2D)
//								((PlotData2D) dataSourceWrapper).toggleLogScale();
//						}
//					} catch (KurandaException e) {
//						
//						handleException(e);
//					}
					((IPlot1D) currentKurandaWidget).setLogarithmYEnabled(
							!((IPlot1D) currentKurandaWidget).isLogarithmYEnabled());
				} else if (currentKurandaWidget instanceof IHist2D) {
//					try {
//						currentKurandaWidget.toggleLogColorScale();
//						getViewPlotPropertiesComposite().updateUI();
//					} catch (KurandaException e) {
//						
//						handleException(e);
//					}
					((IHist2D) currentKurandaWidget).setLogarithmScaleEnabled(
							!((IHist2D) currentKurandaWidget).isLogarithmScaleEnabled());
					currentKurandaWidget.updatePlot();
				}
			}});

		copyToolItem.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				
			}

			public void widgetSelected(SelectionEvent arg0) {
				currentKurandaWidget.doCopy();
				
			}});
		
		resetPlotToolItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (currentKurandaWidget != null) {
					currentKurandaWidget.restoreAutoBounds();
					currentKurandaWidget.repaint();
				}
			}
		});
		
//		backgroundColorToolItem.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				if (currentKurandaWidget != null) {
//			        ColorDialog dialog = new ColorDialog(getShell());
//			        
//			        RGB rgb = currentKurandaWidget.getPlotBackgroundColor();
//			        if (rgb != null) {
//						dialog.setRGB(rgb);
//					}
//			        rgb = dialog.open();
//
//			        if (rgb != null) {
//						currentKurandaWidget.setPlotBackgroundColor(rgb.red, rgb.green, rgb.blue);
//						try {
//							currentKurandaWidget.refreshPlot();
//						} catch (KurandaException e1) {
//							handleException(e1);
//						}
//					}
//				}
//			}
//		});
		
//		navigateBackToolItem.addSelectionListener(new SelectionListener(){
//
//			public void widgetDefaultSelected(SelectionEvent arg0) {
//				
//				
//			}
//
//			public void widgetSelected(SelectionEvent arg0) {
//				
//				List<PlotDataItem> plotDataItems = multiPlotDataManager.getPlotDataItems();
//				if (plotDataItems != null && plotDataItems.size() > 0){
//					PlotDataItem currentItem = multiPlotDataManager.getSingleVisiblePlotDataItem();
//					if (currentItem == null) return;
//					int currentIndex = plotDataItems.indexOf(currentItem);
//
//					setSingleItemVisible(plotDataItems, currentIndex);
////					if (currentIndex > 0){
////						PlotDataItem dataItem = plotDataItems.get(currentIndex - 1);
////						if (dataItem.getChildrenCount() > 0){
////							if (currentIndex == 1)
////								return;
////							
////						}
////						multiPlotDataManager.setSingleItemVisible(
////								plotDataItems.get(currentIndex - 1));
////						updateTitle();
////					}
//				}
//			}
//			
//		});
//		
//		navigateForwardToolItem.addSelectionListener(new SelectionListener(){
//
//			public void widgetDefaultSelected(SelectionEvent arg0) {
//				
//			}
//
//			public void widgetSelected(SelectionEvent arg0) {
//				List<PlotDataItem> plotDataItems = multiPlotDataManager.getPlotDataItems();
//				if (plotDataItems != null && plotDataItems.size() > 0){
//					PlotDataItem currentItem = multiPlotDataManager.getSingleVisiblePlotDataItem();
//					if (currentItem == null) return;
//					int currentIndex = plotDataItems.indexOf(currentItem);
//					if (currentIndex < 0)
//						return;
//					if (currentIndex < plotDataItems.size() - 1){
//						multiPlotDataManager.setSingleItemVisible(
//								plotDataItems.get(currentIndex + 1));
//						updateTitle();
//					}
//				}
//			}
//			
//		});
		
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				disposeUI();
			}
		});
		
		getMultiPlotDataManager().addMultiPlotDataListener(new MultiPlotDataListener() {
			public void itemAdded(PlotDataItem plotDataItem) {
				updateTitle();
				updateStatusBar();
//				updateNavigationButton();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				addDataItemRemoveButton(plotDataItem);
			}
			public void itemRemoved(PlotDataItem plotDataItem) {
				updateTitle();
				updateStatusBar();
//				updateNavigationButton();
			}
			public void itemUpdated(PlotDataItem plotDataItem) {
				updateTitle();
				updateStatusBar();
//				updateNavigationButton();
			}
		});	

	}
	
	private void setSingleItemVisible(List<PlotDataItem> plotDataItems, int currentIndex){
		if (currentIndex >= 0){
			PlotDataItem dataItem = plotDataItems.get(currentIndex);
			if (dataItem.getChildrenCount() > 0){
				if (currentIndex == 0)
					return;
				setSingleItemVisible(plotDataItems, currentIndex - 1);
			}else{
				multiPlotDataManager.setSingleItemVisible(dataItem);
				updateTitle();
			}
		}
	}
	
	private void setToolItemSelection(ToolItem checkToolItem) {
		boolean isItemSelected = checkToolItem.getSelection();
		if (isItemSelected){
			resetOtherToolItem(checkToolItem);
			setInnerSash(checkToolItem);
			sashForm.setMaximizedControl(null);
		}else{
			updateSashForm();
		}
		
	}

//	public void setDataViewSelection(boolean isSelected){
//		propertiesToolItem.setSelection(isSelected);
//		setToolItemSelection(propertiesToolItem);
//	}
	
	private void updateSashForm() {
		boolean isAnyCheckToolItemChecked = 
//			propertiesToolItem.getSelection() || viewControlToolItem.getSelection() || 
			fittingControlToolItem.getSelection() 
			|| maskControlToolItem.getSelection();
		if (!isAnyCheckToolItemChecked){
			Control maximizedControl = sashForm.getMaximizedControl();
			if (maximizedControl == null)
				sashForm.setMaximizedControl(plotAreaComposite);
		}
	}

	private void setInnerSash(ToolItem checkToolItem){
//		if (checkToolItem == propertiesToolItem){
//			innerSash.setMaximizedControl(multiPlotPropertiesComposite);
//			return;
//		}
//		if (checkToolItem == viewControlToolItem){
//			viewPlotPropertiesComposite.updateUI();
//			innerSash.setMaximizedControl(viewPlotPropertiesComposite);
//			return;
//		}
		if (checkToolItem == fittingControlToolItem){
			innerSash.setMaximizedControl(fitPlotPropertiesComposite);
			return;
		}
		if (checkToolItem == maskControlToolItem)
			innerSash.setMaximizedControl(maskPlotPropertiesComposite);
	}
	
	private void resetOtherToolItem(ToolItem checkToolItem) {
//		if (checkToolItem != propertiesToolItem)
//			if (propertiesToolItem.getSelection())
//				propertiesToolItem.setSelection(false);
//		if (checkToolItem != viewControlToolItem)
//			if (viewControlToolItem.getSelection())
//				viewControlToolItem.setSelection(false);
		if (checkToolItem != fittingControlToolItem)
			if (fittingControlToolItem.getSelection())
				fittingControlToolItem.setSelection(false);
		if (checkToolItem != maskControlToolItem)
			if (maskControlToolItem.getSelection())
				maskControlToolItem.setSelection(false);
	}

//	protected void updateNavigationButton() {
//		
//		List<PlotDataItem> plotDataItems = multiPlotDataManager.getPlotDataItems();
//		if (plotDataItems.size() > 1){
//			if (plotDataItems.get(0).getDataType() == DataType.Map || plotDataItems.get(0).getDataType() == DataType.MapSet){
//				navigateBackToolItem.setEnabled(true);
//				navigateForwardToolItem.setEnabled(true);
//			}
//		}
//	}

	protected void addDataItemRemoveButton(final PlotDataItem dataItem) {
		if (isQuickRemoveEnabled && dataItem.getDataType() == DataType.Pattern && !dataItem.isLinked()){
			final ToolItem removeButtonItem = new ToolItem(navigateDataToolbar, SWT.PUSH);
			//		removeButtonItem.setText("X");
			try{
				removeButtonItem.setImage(getCheckedImage(findDataItemColor(dataItem)));
			} catch (Exception e) {
				handleException(new Exception("The color of the plot is not ready"));
			}
			removeButtonItem.setToolTipText("Remove curve " + ((au.gov.ansto.bragg.datastructures.core.plot.Plot) 
					dataItem.getData()).getTitle());
			
			
			final MultiPlotDataListener multiPlotDataListener = new MultiPlotDataListener() {
				
				public void itemUpdated(PlotDataItem plotDataItem) {
					if (plotDataItem == dataItem){
//						final AbstractDataSource dataSourceWrapper = getCurrentPlotWidget().getMultiPlotManager(
//						).getDataSourceWrapper(dataItem.getKurandaPlotDataId());
//						Display.getCurrent().asyncExec(new Runnable() {
//							
//							@Override
//							public void run() {
//								Image image = removeButtonItem.getImage();
//								if (image != null) {
//									Color color = image.getBackground();
//									if (color != null) {
//										color.dispose();
//									}
//									image.dispose();
//								}
//								removeButtonItem.setImage(getCheckedImage(dataSourceWrapper.getColor()));
//							}
//						});
					}
				}
				
				public void itemRemoved(PlotDataItem plotDataItem) {
					if (dataItem == plotDataItem){
						multiPlotDataManager.removeMultiPlotDataListener(this);
						Image image = removeButtonItem.getImage();
						if (image != null) {
							Color color = image.getBackground();
							if (color != null) {
								color.dispose();
							}
							image.dispose();
						}
						removeButtonItem.dispose();
					}
				}
				
				public void itemAdded(PlotDataItem plotDataItem) {
				}
			};
			removeButtonItem.addSelectionListener(new SelectionListener() {
				
				public void widgetSelected(SelectionEvent arg0) {
					multiPlotDataManager.removeMultiPlotDataListener(multiPlotDataListener);
					multiPlotDataManager.removePlotDataItem(dataItem);
					removeButtonItem.removeSelectionListener(this);
					removeButtonItem.dispose();
				}
				
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
			});
			multiPlotDataManager.addMultiPlotDataListener(multiPlotDataListener);
			layout(navigateDataToolbar.getParent().getChildren());
		}
	}
	
	private RGB findDataItemColor(PlotDataItem item) {
		switch (getCurrentPlotType()) {
		case OffsetPlot:
		case OverlayPlot:
			IXYErrorDataset dataset1D = (IXYErrorDataset) getCurrentPlotWidget().getDataset();
			List<IXYErrorSeries> seriesList = dataset1D.getSeries();
			for (IXYErrorSeries series : seriesList) {
				if (series == item.getPlotData()) {
					java.awt.Color color = ((IPlot1D) getCurrentPlotWidget()).getCurveColor(series);
					return new RGB(color.getRed(), color.getGreen(), color.getBlue());
				}
			}
			break;

		default:
			break;
		}
		return null;
	}
	
	/**
	 * Compose title with current title mode and values.
	 */
	public void updateTitle() {
		StringBuilder titleStringBuilder = new StringBuilder();
		if (viewPlotPropertiesComposite.isTitleGenerated()) {
			if (forceTitle != null && forceTitle.trim().length() > 0)
				titleStringBuilder.append(forceTitle);
			else if (currentPlotType == PlotType.IntensityPlot && currentDataType == DataType.Map
					|| currentDataType == DataType.MapSet) {
				final PlotDataItem singleVisiblePlotDataItem = multiPlotDataManager
						.getSingleVisiblePlotDataItem();
				if (singleVisiblePlotDataItem != null) {
					titleStringBuilder.append(((au.gov.ansto.bragg.datastructures.core.plot.Plot) 
							singleVisiblePlotDataItem.getData()).getTitle());
//					if (viewPlotPropertiesComposite.isTitleIncludeReference()) {
//						titleStringBuilder.append('[');
//						titleStringBuilder.append(singleVisiblePlotDataItem.getReferenceString());
//						titleStringBuilder.append(']');
//					}
//					titleStringBuilder.append("; ");
				}
			} else {
				int toSkip = 0;
				for (PlotDataItem plotDataItem : multiPlotDataManager.getPlotDataItems()) {
//					titleStringBuilder.append(plotDataItem.getTitle());
//					if (viewPlotPropertiesComposite.isTitleIncludeReference()) {
//						titleStringBuilder.append('[');
//						titleStringBuilder.append(plotDataItem.getReferenceString());
//						titleStringBuilder.append(']');
//					}
//					AlgorithmTask task = ProjectManager.getAlgorithmTask(plotDataItem.getPlotDataReference().
//							getTaskId());
//					String sourceDataPath = task.getAlgorithmInputs().get(task.getSelectedDataItemIndex()).
//							getDatabag().getLocation();
					if (toSkip > 0){
						toSkip--;
						continue;
					}
					toSkip = plotDataItem.getChildrenCount();
					String copyingLog = null;
					if (plotDataItem.getData() instanceof au.gov.ansto.bragg.datastructures.core.plot.Plot){
						copyingLog = ((au.gov.ansto.bragg.datastructures.core.plot.Plot) plotDataItem.
								getData()).getTitle();
					}
						
					titleStringBuilder.append(copyingLog);
					titleStringBuilder.append("; ");
				}
			}
		} else {
			titleStringBuilder.append(viewPlotPropertiesComposite.getCustomTitle());
		}
		setTitle(titleStringBuilder.toString());
	}

	private void initExportAction() throws ExportException {
		List<Exporter> exporterList = null;
//		if (multiPlotDataManager.getPlotDataItems() != null && 
//				multiPlotDataManager.getPlotDataItems().size() > 0)
//			if (multiPlotDataManager.getPlotDataItems().get(0).getDataType() == DataType.Pattern ||
//					multiPlotDataManager.getPlotDataItems().get(0).getDataType() == DataType.PatternSet)
//				exporterList = UIAlgorithmManager.getAvailable1DExporterList();
//			else
//				exporterList = UIAlgorithmManager.getAvailableMultiDExporterList();
//		else
//			exporterList = UIAlgorithmManager.getAvailableExporterList();
		switch (currentDataType) {
		case Pattern:
			exporterList = UIAlgorithmManager.getAvailable1DExporterList();
			break;
		case PatternSet:
			exporterList = UIAlgorithmManager.getAvailable1DExporterList();
			break;
		case Undefined:
			exporterList = UIAlgorithmManager.getAvailableExporterList();
			break;
		default:
			exporterList = UIAlgorithmManager.getAvailableMultiDExporterList();
			break;
		}
		for (Exporter exporter : exporterList) {
			MenuItem menuItem = new MenuItem (exportDataMenu, SWT.PUSH);
			menuItem.setText (exporter.getFormater().getName());
			menuItem.addSelectionListener(new ExportListener(exporter));
		}
		isExportMenuInitialized = true;
	}

	/**
	 * Initiate export of currently displayed plot to file. 
	 */
	protected void exportPlotAsPictureToFile() {
		if (currentKurandaWidget == null) {
			return;
		}

		Thread newThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					currentKurandaWidget.doSaveAs();
				} catch (IOException e) {
					handleException(e);
				}
			}
		});
		newThread.start();
//		Composite c = (Composite) currentKurandaWidget;
//
//
//		// create and populate image object
//		final Point size = c.getSize();
//		final Image image = new Image(c.getDisplay(), size.x, size.y);
//		GC gc = new GC(c);
//		gc.copyArea(image, 0, 0);
//
//		final ImageData imageData = image.getImageData();
//
//		
///* 
// * Copy to clipboard functionality does not work due to miss of implementation of ImageTransfer in base SWT plugin.
// * Current implementation's data in a clipboard cannot be recognized by third party applications.
// */
//		
////		Clipboard clipboard = new Clipboard(Display.getDefault());
////		ImageTransfer transfer = ImageTransfer.getInstance();
////		clipboard.setContents(new Object[]{image.getImageData()}, new Transfer[]{transfer});
////		clipboard.dispose();
//
//		
//		//get file name. Provide the list of supported formats
//		final String saveFilename = Util.getSaveFilenameFromShell(c.getShell(), 
//				new String[]{"*.bmp",
//				//			"*.gif", 
//							"*.jpg", 
//							"*.png"
//				//			, "*.tif"
//							}, 
//				new String[]{"Windows Bitmap (*.bmp)", 
////							"Graphics Interchange Format (*.gif)", 
//							"JPEG File Interchange Format (*.jpg)", 
//							"Portable Network Graphics (*.png)"
////							, "Tag Image File Format (*.tif)"
//							});
//		
//		if (saveFilename != null) {
//			ImageLoader imageLoader = new ImageLoader();
//			imageLoader.data = new ImageData[] {imageData};
//			
//			int fileFormat = SWT.IMAGE_PNG;
//			String fileExt = saveFilename.substring(saveFilename.lastIndexOf('.') + 1);
//			if (fileExt.equalsIgnoreCase("jpg")) {
//				fileFormat = SWT.IMAGE_JPEG;
//			} else if (fileExt.equalsIgnoreCase("png")) {
//				fileFormat = SWT.IMAGE_PNG;
//			} else if (fileExt.equalsIgnoreCase("gif")) {
//				fileFormat = SWT.IMAGE_GIF;
//			} else if (fileExt.equalsIgnoreCase("tif")) {
//				fileFormat = SWT.IMAGE_TIFF;
//			} else if (fileExt.equalsIgnoreCase("bmp")) {
//				fileFormat = SWT.IMAGE_BMP;
//			} else {
//				fileFormat = SWT.IMAGE_PNG;		
//			}
//			
//			imageLoader.save(saveFilename, fileFormat);
//		}
//		
//		
//		image.dispose();
//		gc.dispose();
	
		
	}
	
	/**
	 * Dispose all UI handlers before dispose the widget. 
	 * Use the method to dispose all UI resources. 
	 */
	protected void disposeUI() {
//		Composite kurandaComposite = (Composite) currentKurandaWidget;
//		if (!kurandaComposite.isDisposed())
//			kurandaComposite.dispose();
//		currentKurandaWidget = null;
		multiPlotDataManager.disposeResources();
//		if (!multiPlotPropertiesComposite.isDisposed())
//			multiPlotPropertiesComposite.dispose();
		currentKurandaWidget = null;
	}

	
	public void setMaskEnabled(boolean enabled) {
		boolean oldIsMaskingEnabled = isMaskingEnabled;
		isMaskingEnabled = enabled;
		if (oldIsMaskingEnabled != isMaskingEnabled) {

			//TODO add/remove region listeners
			
			updatePlotMasks();
		}
		
		currentKurandaWidget.setMaskingEnabled(enabled);
	}

	public void enableMask(){
		isMaskingEnabled = true;
		currentKurandaWidget.setMaskingEnabled(isMaskingEnabled && regionParameter != null);
	}
	/**
	 * Sets data to be displayed to the Plot.
	 * @param plotData plot data object. Null is excepted.
	 * @throws KurandaException throws if any problems with visualization.
	 */
//	public void setPlotData(Group plotData) throws KurandaException {
//		this.plotData = plotData;
//
//		if (currentKurandaWidget != null) {
//			currentKurandaWidget.getMultiPlotManager().clearAllDataSourceWrappers();
//		}
//		
//		final DataType dataType = au.gov.ansto.bragg.kakadu.core.Util.getDataType(plotData);
//		setCurrentDataType(dataType);
//		setPlotType(getDefaultPlotType(currentDataType));
//		
//		//set plot data to current plot Widget
//		if (isSchemaMode) {
//			schemaPlotComposite.setSchemaData(plotData);
//		} else if (currentDataType == DataType.Calculation) {
//			calculationPlotComposite.setCalculationData(plotData);
//		} else if (currentKurandaWidget != null) {
//			currentKurandaWidget.setDataSource(plotData);
//			currentKurandaWidget.refreshPlot();
//		}
//		
//		updatePlotMasks();
//		plotComposite.redraw();
//		
//		updateStatistic();
//		updateFlipAxis();
//		updateLegend(); 
//		
//		updateUIComponents();
//	}

	/**
	 * Sets current data type to be processed be the plot.
	 * For multi plot the data type applied for each data item.
	 * @param dataType data type
	 */
	public void setCurrentDataType(final DataType dataType) {
		currentDataType = dataType;
	}

	public void updateUIComponents() {
		viewPlotPropertiesComposite.setFlipAxiesEnabled(currentDataType != DataType.Calculation);
		viewPlotPropertiesComposite.setSchemaModeEnabled(currentDataType == DataType.Pattern);
		viewPlotPropertiesComposite.setShowErrorEnabled(currentDataType == DataType.Pattern);
		viewPlotPropertiesComposite.setLogYEnabled(currentDataType == DataType.Pattern);
		viewPlotPropertiesComposite.setLogXEnabled(currentDataType == DataType.Pattern);
		exportPictureToolItem.setEnabled(!isSchemaMode && currentKurandaWidget != null);
//		exportDataToolItem.setEnabled(multiPlotPropertiesComposite != null && (multiPlotPropertiesComposite.getSelectedPlotDataItem() != null
//				|| multiPlotDataManager.getPlotDataItems().size() >= 1));
	}
	
	public PlotType getCurrentPlotType() {
		return currentPlotType;
	}
	
	public DataType getCurrentDataType() {
		return currentDataType;
	}
	
	public void setPlotType(PlotType plotType) {
		final PlotType previousPlotType = currentPlotType;
		currentPlotType = plotType;
	
		try {
			switch (plotType) {
			case OffsetPlot:
			case OverlayPlot:
				plotComposite.setDataset(new XYErrorDataset());
				currentKurandaWidget = plotComposite.getPlot();
//				currentKurandaWidget.setPointFollowerEnabled(true);
				statusBarComposite.setValueAndZoomEnabled(false);
				break;
			case IntensityPlot:
				plotComposite.setDataset(new Hist2DDataset());
				currentKurandaWidget = plotComposite.getPlot();
//				currentKurandaWidget.setPointFollowerEnabled(false);
//				currentKurandaWidget.setPixelAspectRatio(mode);
				break;
			case SurfacePlot:
//				currentKurandaWidget.setPlotType(AbstractWidget.PLOT3DSURFACE);
				break;
			default:
//				currentKurandaWidget.setPlotType(AbstractWidget.PLOTEMPTY);
				break;
			}
		} catch (Exception e1) {
			handleException(e1);
		}

		resetStatusBar();
		updateLayout();
		
		//to show/hide controls specific for the current PlotType 
//		viewPlotPropertiesComposite.updateUI();
		
		firePlotPropertyChangeListeners(PlotPropertyChangeListener.PLOT_TYPE_PROPERTY_ID, previousPlotType, plotType);
	}
	
	public MultiPlotDataManager getMultiPlotDataManager() {
		return multiPlotDataManager;
	}
	
//	MultiPlotPropertiesComposite getMultiPlotPropertiesComposite() {
//		return multiPlotPropertiesComposite;
//	}
	
	ViewPlotPropertiesComposite getViewPlotPropertiesComposite() {
		return viewPlotPropertiesComposite;
	}
	
	/**
	 * Sets schema mode for the plot to show data in a table.
	 * @param isSchemaMode <code>true</code> if schema mode has to be loaded.
	 */
	public void setSchemaMode(boolean isSchemaMode) {
		this.isSchemaMode = isSchemaMode;
		
		if (isSchemaMode) {
//			schemaPlotComposite.setSchemaData(multiPlotPropertiesComposite.getSelectedPlotDataItem().getData());
			// TODO: find selected series
			PlotDataItem dataItem = null;
			if (dataItem == null)
				dataItem = multiPlotDataManager.getSingleVisiblePlotDataItem();
			if (dataItem == null){
				List<PlotDataItem> dataItemList = multiPlotDataManager.getPlotDataItems();
				if (dataItemList != null && dataItemList.size() > 0){
					PlotDataItem item = dataItemList.get(0);
					if (item.getChildrenCount() > 0)
							dataItem = item.getChildren().get(0);
					else
						dataItem = item;
				}				
			}
			if (dataItem != null)
				schemaPlotComposite.setSchemaData(dataItem.getData());
		}
		
		updateLayout();
	}

	/**
	 * Sets schema mode for the plot to show data in a table.
	 * @param isSchemaMode <code>true</code> if schema mode has to be loaded.
	 */
	public void setMetadataMode(boolean isSchemaMode) {
		this.isSchemaMode = isSchemaMode;
		
//		if (isSchemaMode && multiPlotPropertiesComposite != null && multiPlotPropertiesComposite.getSelectedPlotDataItem() != null) {
//			schemaPlotComposite.setMataDataSchema(multiPlotPropertiesComposite.getSelectedPlotDataItem().getData());
//		}
		
		updateLayout();
	}

	/**
	 * Loads plotting ui control dependent to view mode and data type. 
	 */
	private void updateLayout() {
		plotLayout.topControl = isSchemaMode ? schemaPlotComposite :
					 currentDataType == DataType.Calculation ? calculationPlotComposite :
						 currentKurandaWidget != null ? plotComposite : 
							 emptyPlotComposit;
		plotComposite.layout();
	}
	
	
//	private void updateFlipAxis() {
//		if (currentKurandaWidget != null) {
//			viewPlotPropertiesComposite.setXAxisFlip(viewPlotPropertiesComposite.isXAxisFlip());
//		}
//	}

//	private void updateStatistic() {
//		if (plotData == null || currentDataType == DataType.Calculation) {
//			return;
//		}
//		statisticPlotPropertiesComposite.removeAllItems();
//		
//		//parse statistic info
//		
//		try {
//			final Group groupData = (Group) plotData;
//			final DataItem signal = groupData.findSignal();
//
//			
//			final List<?> attributes = signal.getAttributes();
//			for (Iterator iterator = attributes.iterator(); iterator.hasNext();) {
//				Object attribute = iterator.next();
//				if (attribute instanceof Attribute) {
//					Attribute ncAttribute = (Attribute) attribute;
//					final String name = ncAttribute.getName();
//					final String stringValue = ncAttribute.getStringValue();
//					statisticPlotPropertiesComposite.addItem(name, stringValue);
//				}
//			}
//		} catch (Exception e) {
//			handleException(e);
//		}
//	}

	public void setTitle(String title) {
		String oldTitle = this.title;
		this.title = title;
		firePlotPropertyChangeListeners(PlotPropertyChangeListener.TITLE_PROPERTY_ID,
				oldTitle , title);
		
		if (currentKurandaWidget != null) {
			currentKurandaWidget.getTitle().setText(title);
		} else {
			emptyPlotComposit.setTitle(title);
		}
	}
	
	private void loadMaskList(List<AbstractMask> regions) {
		if (currentKurandaWidget != null) {
			if (regions != null) {
				for (AbstractMask region : regions) {
					addMask(region);
				}
			}
		}
	}

	/**
	 * @param region
	 * @return
	 * @throws KurandaException
	 */
	private void addMask(AbstractMask region) {
		if (currentKurandaWidget == null) {
			return;
		}

		isInnerPlotRegionProcessing = true;

		if (currentKurandaWidget instanceof IHist2D) {
			try {
//				RectangleMask mask = new RectangleMask(true);
//				mask.setRectangleFrame(new Rectangle2D.Double(region.getXMin(), 
//						region.getYMin(), region.getXMax() - region.getXMin(), 
//						region.getYMax() - region.getYMin()));
				currentKurandaWidget.addMask(region);

				maskMap.put(region.getName(), region);

				currentKurandaWidget.repaint();
			} catch (Exception e) {
				handleException(e);
			}
		} else if (currentKurandaWidget instanceof IPlot1D) {
			try {
//				RangeMask mask = new RangeMask(true);
//				mask.setBoundary(region.getXMin(), region.getXMax());
				currentKurandaWidget.addMask(region);

				maskMap.put(region.getName(), region);

				currentKurandaWidget.repaint();
			} catch (Exception e) {
				handleException(e);
			}
		}
		isInnerPlotRegionProcessing = false;
	}
	
	private void removeMask(AbstractMask region) {
		isInnerPlotRegionProcessing = true;

		for (Iterator<String> iterator = maskMap.keySet().iterator(); iterator.hasNext();) {
			String regionId = iterator.next();
			final AbstractMask aRegion = maskMap.get(regionId);
			if (aRegion == region) {
				try {
//					currentKurandaWidget.removeMask(regionId);
					currentKurandaWidget.removeMask(findMask(regionId));
					currentKurandaWidget.repaint();
					
					maskMap.remove(regionId);
				} catch (Exception e) {
					handleException(e);
				}
				break;
			}
			
		}
		isInnerPlotRegionProcessing = false;
	}
	
	private AbstractMask findMask(String name) {
		for (AbstractMask mask : currentKurandaWidget.getMasks()) {
			if (mask.getName().equals(name)) {
				return mask;
			}
		}
		return null;
	}
	
	private void updateMask(AbstractMask region) {
		isInnerPlotRegionProcessing = true;

		for (Iterator<String> iterator = maskMap.keySet().iterator(); iterator.hasNext();) {
			String regionId = iterator.next();
			final AbstractMask aRegion = maskMap.get(regionId);
			if (aRegion == region) {
				try {
					AbstractMask mask = findMask(regionId);
					mask.setName(region.getName());
//					if (mask instanceof Abstract2DMask) {
//						((Abstract2DMask) mask).setRectangleFrame(new Rectangle2D.Double(
//								region.getXMin(), region.getYMin(), region.getXMax() - region.getXMin(), 
//								region.getYMax() - region.getYMin()));
//					}
					currentKurandaWidget.repaint();
					maskMap.remove(regionId);
					maskMap.put(region.getName(), region);
				} catch (Exception e) {
					handleException(e);
				}
				break;
			}
			
		}
		isInnerPlotRegionProcessing = false;
	}
	
	public void removeAllMasks() {
		if (currentKurandaWidget != null) {
			isInnerPlotRegionProcessing = true;
			try {
				for (AbstractMask mask : currentKurandaWidget.getMasks()) {
					currentKurandaWidget.removeMask(mask);
				}
				currentKurandaWidget.repaint();
				
				maskMap.clear();
			} catch (Exception e) {
				handleException(e);
			}
			isInnerPlotRegionProcessing = false;
		}
	}
	
	private void initKurandaWidget(PlotType plotType) {
		if (currentKurandaWidget == null) {
			
			try {
				switch (plotType) {
					case OffsetPlot:
					case OverlayPlot:
//						currentKurandaWidget = new JFreeChart1DPlotWidget(plotComposite,
//								SWT.NONE);
						plotComposite.setDataset(new XYErrorDataset());
						currentKurandaWidget = plotComposite.getPlot();
						break;
					case IntensityPlot:
						plotComposite.setDataset(new Hist2DDataset());
						currentKurandaWidget = plotComposite.getPlot();
						break;
					default:
						plotComposite.setDataset(new XYErrorDataset());
						currentKurandaWidget = plotComposite.getPlot();
				}
			} catch (Exception e) {
				handleException(e);
			}
			
//			currentKurandaWidget.setPointFollowerEnabled(true);
//			currentKurandaWidget.setRegionNamesVisible(true);
			currentKurandaWidget.addMaskEventListener(plotRegionListener);
//			currentKurandaWidget.addPointLocatorListener(pointLocatorListener);
//			currentKurandaWidget.addZoomListener(new IZoomListener() {
//				public void zoomEvent(ZoomData zoomData) {
//					statusBarComposite.setZoomInfo(zoomData.getZoomMagnificationLevelX(), zoomData.getZoomMagnificationLevelY());
//				}
//			});
			
			
			//init DnD
//			DropTarget dropTarget = new DropTarget((Control)currentKurandaWidget, DND.DROP_MOVE);
//			dropTarget.setTransfer(new Transfer[] { PlotDataItemTransfer.getInstance(), PlotDataReferenceTransfer.getInstance() });
//			dropTarget.addDropListener(new DropTargetAdapter() {
//			});

		}
	}


	private void resetStatusBar() {
		statusBarComposite.setZoomInfo(100);
		statusBarComposite.setCursorLocation(0, 0);
	}
	
	/**
	 * Updates common status of PlotDataItems in status bar.
	 */
	public void updateStatusBar() {
		OperationStatus commonStatus = OperationStatus.Ready;
		final List<PlotDataItem> plotDataItems = multiPlotDataManager.getPlotDataItems();
		for (PlotDataItem plotDataItem : plotDataItems) {
			if (plotDataItem.isLinked()) {
				final Operation operation = PlotManager
						.getOperation(plotDataItem.getPlotDataReference());
				if (operation != null) {
					final OperationStatus status = operation.getStatus();
					if (status.ordinal() > commonStatus.ordinal()) {
						//show more the most last element in enumeration 
						commonStatus = status;
					}
				}
			}
		}
		statusBarComposite.setDataStatus(commonStatus.toString());
	}

	public static PlotType getDefaultPlotType(DataType dataType) {
		switch (dataType) {
		case Pattern:
		case PatternSet:
			return PlotType.OverlayPlot;
		case Map:
		case MapSet:
			return PlotType.IntensityPlot;
		case Calculation:
			return PlotType.SchemaPlot;

		case Undefined:
		default:
			return PlotType.EmptyPlot;
		}
	}

	private final class PlotRegionListener implements IMaskEventListener {
		public void maskAdded(final AbstractMask mask) {
//			System.out.println("PlotRegionListener.regionAdded()"
//					+ mask.getName());
//			if (!isInnerPlotRegionProcessing) {
//				
//				if (mask instanceof Abstract2DMask) {
//					final Rectangle2D region = ((Abstract2DMask) mask).getRectangleFrame();
//				DisplayManager.getDefault().asyncExec(new Runnable() {
//					public void run() {
//						isInnerUIRegionProcessing = true;
//						
//						UIRegion uiRegion = new UIRegion(mask.isInclusive(), region.getMinX(),
//								region.getMinY(), region.getMaxX(), region.getMaxY());
//						
////						uiRegion.setName("Mask " + uiRegion.getId());
//						
//						parameterRegionManager.addRegion(uiRegion);
//						
//						mask.setName(uiRegion.getName());
//						
//						maskMap.put(mask.getName(), uiRegion);
//						
//						
//						isInnerUIRegionProcessing = false;
//
//						try {
//							currentKurandaWidget.repaint();
//						} catch (Exception e) {
//							handleException(e);
//						}
//						
//					}
//				});
//				}
//			}
			
			if (regionParameter != null) {
				regionParameter.addMask(mask);
			}
		}

		public void maskRemoved(final AbstractMask mask) {
//			System.out.println("PlotRegionListener.regionRemoved()"
//					+ mask.getName());
//			if (!isInnerPlotRegionProcessing) {
//				DisplayManager.getDefault().asyncExec(new Runnable() {
//					public void run() {
//						isInnerUIRegionProcessing = true;
//						final UIRegion uiRegion = maskMap.remove(mask.getName());
//						if (uiRegion != null) {
//							parameterRegionManager.removeRegion(uiRegion);
//						}
//						isInnerUIRegionProcessing = false;
//					}
//				});
//			}

			regionParameter.removeMask(mask);
		}

		public void maskUpdated(final AbstractMask mask) {
//			System.out.println("PlotRegionListener.regionUpdated() " + mask.getName());
//			
//			if (!isInnerPlotRegionProcessing) {
//				if (mask instanceof Abstract2DMask) {
//					final Rectangle2D region = ((Abstract2DMask) mask).getRectangleFrame();
//				DisplayManager.getDefault().asyncExec(new Runnable() {
//					public void run() {
//						isInnerUIRegionProcessing = true;
//						final UIRegion uiRegion = maskMap.get(mask.getName());
//						if (uiRegion != null) {
//							parameterRegionManager.updateRegion(uiRegion.getId(), 
//									uiRegion.getName(), uiRegion.isInclusive(), 
//									region.getMinX(), region.getMinY(), 
//									region.getMaxX(), region.getMaxY());
//						}
//						isInnerUIRegionProcessing = false;
//					}
//				});
//				}
//			}
			
			regionParameter.fireMaskUpdatedEvent(mask);
		}
	}
	
	public void handleException(Throwable throwable) {
		throwable.printStackTrace();
		showErrorMessage(throwable.getMessage());
	}
	
	private void showErrorMessage(String message) {
		MessageDialog.openError(
			getShell(),
			"Plot",
			message);
	}



	private static final class EmptyPlotComposite extends Composite {
		private Label title;
		private Label message;

		private EmptyPlotComposite(Composite parent, int style) {
			super(parent, style);
			setLayout(new GridLayout());
			
			title = new Label(this, SWT.NONE);
			title.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
			
			message = new Label(this, SWT.NONE);
			message.setText("Data processing is in progress now or there are no displayable data for the operation. " +
					"\nWait please for result or select another operation.\nPress 'Run Algorithm' button to run algorithm again.");
			message.setLayoutData(new GridData(GridData.CENTER, GridData.CENTER, true, true));
		}

		public String getTitle() {
			return title.getText();
		}

		public void setTitle(String title) {
			this.title.setText(title);
			layout();
		}
		
		
	}
	
//	public ParameterRegionManager getParameterRegionManager() {
//		return parameterRegionManager;
//	}
//
//	public void setParameterRegionManager(ParameterRegionManager parameterRegionManager) {
//		if (this.parameterRegionManager == parameterRegionManager) {
//			return;
//		}
//		
//		if (this.parameterRegionManager != null) {
////			this.parameterRegionManager.removeRegionListener(uiRegionListener);
//		}
//		this.parameterRegionManager = parameterRegionManager;
//		
//		if (this.parameterRegionManager != null) {
////			this.parameterRegionManager.addRegionListener(uiRegionListener);
//		}
//
//		updatePlotMasks();
//	}
	
	public void setRegionParameter(RegionParameter parameter) {
		if (this.regionParameter != null && this.regionParameter != parameter) {
			this.regionParameter.removeRegionListener(regionEventListener);
		}
		this.regionParameter = parameter;
		if (regionParameter != null){
			this.regionEventListener = new RegionEventListener() {
				
				@Override
				public void maskUpdated(AbstractMask mask) {
					currentKurandaWidget.repaint();
				}
				
				@Override
				public void maskRemoved(AbstractMask mask) {
					currentKurandaWidget.removeMask(mask);
					currentKurandaWidget.repaint();
				}
				
				@Override
				public void maskAdded(AbstractMask mask) {
					currentKurandaWidget.addMask(mask);
					currentKurandaWidget.repaint();
				}
			};
			regionParameter.addRegionListener(regionEventListener);
		}
	}
	
	public RegionParameter getRegionParameter() {
		return this.regionParameter;
	}
	
	private void updatePlotMasks() {
		if (currentKurandaWidget != null) {
			removeAllMasks();
			currentKurandaWidget.setMaskingEnabled(isMaskingEnabled && regionParameter != null);
			if (isMaskingEnabled && regionParameter != null) {
				loadMaskList(regionParameter.getMaskList());
			}
		}
	}
	
	/**
	 * Shares current plot widget on package level.
	 * @return current plot widget or null if not loaded yet.
	 */
	public IPlot getCurrentPlotWidget() {
		return currentKurandaWidget;
	}

	public String getTitle() {
		return title;
	}
	
	StatusBarComposite getStatusBarComposite() {
		return statusBarComposite;
	}
	
	
	public void addPlotPropertyChangeListener(PlotPropertyChangeListener plotPropertyChangeListener) {
		plotPropertyChangeListeners.add(plotPropertyChangeListener);
	}
	public void removePlotPropertyChangeListener(PlotPropertyChangeListener plotPropertyChangeListener) {
		plotPropertyChangeListeners.remove(plotPropertyChangeListener);
	}
	public void removeAllPlotPropertyChangeListener() {
		plotPropertyChangeListeners.clear();
	}
	public List<PlotPropertyChangeListener> getChangeListeners() {
		return new ArrayList<PlotPropertyChangeListener>(plotPropertyChangeListeners);
	}
	
	protected void firePlotPropertyChangeListeners(int propertyId, Object oldData, Object newData) {
		for (PlotPropertyChangeListener changeListener : plotPropertyChangeListeners) {
			changeListener.propertyChanged(propertyId, oldData, newData);
		}
	}

	/**
	 * @param operaton the operation to set
	 */
	public void setOperaton(Operation operaton) {
		this.operaton = operaton;
	}

	/**
	 * @return the operaton
	 */
	public Operation getOperaton() {
		return operaton;
	}

	private final class ExportListener implements SelectionListener {
		private Exporter exporter;
		
		public ExportListener(Exporter exporter) {
			this.exporter = exporter;
		}

		public void widgetDefaultSelected(SelectionEvent e) {
		}

		public void widgetSelected(SelectionEvent e) {

			List<PlotDataItem> dataItems = multiPlotDataManager.getPlotDataItems();
			final String formatName = exporter.getFormater().getExtensionName();
			if (dataItems.size() > 1) {
				String folder = Util.selectDirectoryFromShell(getShell());
				if (folder == null || folder.trim().length() == 0)
					return;
				int id = 0;
				for (PlotDataItem item : dataItems) {
					IGroup data = item.getData();
					String location = data.getLocation();
					String name;
					if (location != null) {
						File file = new File(location);
						name = file.getName();
						if (name.contains(".")) {
							name = name.substring(0, name.indexOf("."));
						} 
					} else {
						name = String.valueOf(id);
					}
					try {
						URI fileURI = ConverterLib.path2URI(folder + "/" + name);
						exporter.signalExport(data, fileURI);
					} catch (Exception ex) {
						handleException(ex);
					}
					id++;
				}
			} else if (dataItems.size() > 0) {
				IGroup data = dataItems.get(0).getData();
				if (data != null) {
					String location = data.getLocation();
					String name = null;
					if (location != null) {
						File file = new File(location);
						name = file.getName();
						if (name.contains(".")) {
							name = name.substring(0, name.indexOf("."));
						} 
					} 
					String saveFilename = Util.saveFilenameFromShell(e.widget.getDisplay().
							getActiveShell(), name, formatName, "'" + formatName + "' data file");
					if (saveFilename != null && saveFilename.trim().length() > 0) {
						try {
							URI fileURI = ConverterLib.path2URI(saveFilename);
							exporter.signalExport(data, fileURI);
						} catch (Exception ex) {
							handleException(ex);
						}
					}
				}
			}
		}
	}

	public void setForceTitle(String title){
		forceTitle = title;
	}
	
	public void setRefreshingEnabled(boolean isEnabled){
//		currentKurandaWidget.setsetRefreshingEnabled(isEnabled);
	}
	
	/**
	 * @return the plotToolsToolbar
	 */
	public ToolBar getVerticalToolbar() {
		return plotToolsToolbar;
	}

	public ToolBar getHorizontalToolbar(){
		return plotDataToolbar;
	}
	
	public void refresh(){

		try {
			currentKurandaWidget.repaint();
		} catch (Exception e) {
			handleException(e);
		}
	}
	
	public void setQuickRemoveEnabled(boolean isEnabled){
		isQuickRemoveEnabled = isEnabled;
	}
	
	private Image getCheckedImage(RGB colorRGB) {
	    Image image = new Image(getDisplay(), 16, 16);
	    GC gc = new GC(image);
	    gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
//	    gc.fillOval(0, 0, 16, 16);
	    gc.fillRectangle(0, 0, 16, 16);
	    gc.setForeground(new Color(getDisplay(), colorRGB));
//	    gc.drawRectangle(2, 2, 12, 12);
	    gc.drawLine(3, 3, 13, 13);
	    gc.drawLine(4, 3, 13, 12);
	    gc.drawLine(3, 4, 12, 13);
	    gc.drawLine(13, 3, 3, 13);
	    gc.drawLine(12, 3, 3, 12);
	    gc.drawLine(13, 4, 4, 13);
	    gc.dispose();
	    return image;
	  }
	
	public void setIntervalEnabled(boolean enabled){
		isIntervalEnabled = enabled;
	}
}
