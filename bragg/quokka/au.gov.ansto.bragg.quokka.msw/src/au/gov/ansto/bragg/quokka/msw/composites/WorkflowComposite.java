package au.gov.ansto.bragg.quokka.msw.composites;

import java.io.FileOutputStream;
import java.io.OutputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.gumtree.msw.dummy.DummyModelProxy;
import org.gumtree.msw.dummy.DummyRefIdProvider;
import org.gumtree.msw.model.Model;
import org.gumtree.msw.ui.ImageButton;
import org.gumtree.msw.ui.Resources;
import org.gumtree.msw.util.SynchronizedModel;

import au.gov.ansto.bragg.quokka.msw.ModelProvider;
import au.gov.ansto.bragg.quokka.msw.internal.Activator;

public class WorkflowComposite extends Composite {
	// resources
    private static final Image CATEGORY_BTN_IMAGE_UP = Resources.load("/icons/Button_up.png");
    private static final Image CATEGORY_BTN_IMAGE_OVER = Resources.load("/icons/Button_over.png");
    private static final Image CATEGORY_BTN_IMAGE_DOWN = Resources.load("/icons/Button_down.png");
    private static final Image CATEGORY_BTN_IMAGE_BAR = Resources.load("/icons/Button_bar.png", 40, -1);
    private static final Image CATEGORY_BTN_IMAGE_BEGIN = Resources.load("/icons/Button_begin.png");
    private static final Image CATEGORY_BTN_IMAGE_END = Resources.load("/icons/Button_end.png");
    private static final int CATEGORY_BTN_RADIUS = 21;
	// xsd/xml
	private final static String MSW_XSD = "resources/msw.xsd";
	private final static String MSW_XML = "resources/msw.xml"; // "resources/example.xml"; // 

    // fields
    private ModelProvider modelProvider = new ModelProvider(
    		new DummyModelProxy(new SynchronizedModel(new Model(
    				DummyRefIdProvider.DEFAULT,
    				Activator.getEntry(MSW_XSD),
    				Activator.getEntry(MSW_XML)))));

    // construction
	public WorkflowComposite(Composite parent, int style) {
		super(parent, style);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.verticalSpacing = 0;
		setLayout(gridLayout);
		setBackground(parent.getBackground());
		
		final ToolBar toolBar = new ToolBar(this, SWT.HORIZONTAL);
		toolBar.setBackground(getBackground());

	    ToolItem btnNew = new ToolItem(toolBar, SWT.PUSH);
		btnNew.setImage(Resources.IMAGE_NEW);
		btnNew.setText("New");
		
		ToolItem btnLoad = new ToolItem(toolBar, SWT.PUSH);
		btnLoad.setImage(Resources.IMAGE_LOAD);
		btnLoad.setText("Load");
		
		ToolItem btnSave = new ToolItem(toolBar, SWT.PUSH);
		btnSave.setImage(Resources.IMAGE_SAVE);
		btnSave.setText("Save");
		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);

				fileDialog.setFilterNames(new String[] { "Extensible Markup Language (*.xml)", "All Files (*.*)" });
				fileDialog.setFilterExtensions(new String[] { "*.xml", "*.*" });

				String filename = fileDialog.open();
				if ((filename != null) && (filename.length() > 0)) {
					boolean succeeded = false;
					try (OutputStream stream = new FileOutputStream(filename)) {
						succeeded = modelProvider.getModelProxy().serializeTo(null, stream);
					}
					catch (Exception e2) {
					}
					if (!succeeded) {
						MessageBox dialog = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
						dialog.setText("Warning");
						dialog.setMessage("Unable to export to selected xml file.");
						dialog.open();
					}
				}
			}
		});
	    
	    new ToolItem(toolBar, SWT.SEPARATOR);
		
	    ToolItem btnUnlock = new ToolItem(toolBar, SWT.CHECK);
		btnUnlock.setImage(Resources.IMAGE_LOCK_CLOSED);
		btnUnlock.setText("Unlock");

	    new ToolItem(toolBar, SWT.SEPARATOR);
	    
	    final ToolItem btnUndo = new ToolItem(toolBar, SWT.DROP_DOWN);
		btnUndo.setImage(Resources.IMAGE_UNDO);
		btnUndo.setText("Undo");
		btnUndo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				modelProvider.getUndoRedo().undo();
			}
		});
		
		ToolItem btnRedo = new ToolItem(toolBar, SWT.DROP_DOWN);
		btnRedo.setImage(Resources.IMAGE_REDO);
		btnRedo.setText("Redo");
		btnRedo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				modelProvider.getUndoRedo().redo();
			}
		});
		

	    DropdownSelectionListener listenerOne = new DropdownSelectionListener(btnRedo);
	    listenerOne.add("Option One for One");
	    listenerOne.add("Option Two for One");
	    listenerOne.add("Option Three for One");
	    btnRedo.addSelectionListener(listenerOne);
	    
		/*
		final Menu menu1 = new Menu(this.getShell(), SWT.POP_UP);
		for (int i=0; i<8; i++) {
			MenuItem item = new MenuItem (menu1, SWT.PUSH);
			item.setText ("Item " + i);
		}
		btnUndo.addListener(SWT.Selection, new Listener () {
			@Override
			public void handleEvent (Event event) {
				if (event.detail == SWT.ARROW) {
					Rectangle rect = btnUndo.getBounds();
					Point pt = new Point (rect.x, rect.y + rect.height);
					pt = toolBar.toDisplay(pt);
					menu1.setLocation (pt.x, pt.y);
					menu1.setVisible (true);
				}
			}
		});

		final Menu menu2 = new Menu(this.getShell(), SWT.POP_UP);
		for (int i=0; i<8; i++) {
			MenuItem item = new MenuItem (menu2, SWT.PUSH);
			item.setText ("Item " + i);
		}		
		btnRedo.addListener(SWT.Selection, new Listener () {
			@Override
			public void handleEvent (Event event) {
				if (event.detail == SWT.ARROW) {
					Rectangle rect = btnUndo.getBounds();
					Point pt = new Point (rect.x, rect.y + rect.height);
					pt = toolBar.toDisplay(pt);
					menu2.setLocation (pt.x, pt.y);
					menu2.setVisible (true);
				}
			}
		});
		*/

		/*
		Composite cmpButtons = new Composite(this, SWT.NONE);
		cmpButtons.setLayout(new GridLayout(6, true));
		cmpButtons.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));

		Button btnNew = new Button(cmpButtons, SWT.NONE);
		btnNew.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnNew.setImage(Icons.NEW);
		btnNew.setText("New");
		
		Button btnLoad = new Button(cmpButtons, SWT.NONE);
		btnLoad.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnLoad.setImage(Icons.LOAD);
		btnLoad.setText("Load");
		
		Button btnSave = new Button(cmpButtons, SWT.NONE);
		btnSave.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnSave.setImage(Icons.SAVE);
		btnSave.setText("Save");
		
		Button btnUnlock = new Button(cmpButtons, SWT.NONE);
		btnUnlock.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnUnlock.setImage(Icons.LOCK_CLOSED);
		btnUnlock.setText("Unlock");
		
		Button btnUndo = new Button(cmpButtons, SWT.NONE);
		btnUndo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				modelProvider.getUndoRedo().undo();
			}
		});
		btnUndo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnUndo.setBounds(0, 0, 75, 25);
		btnUndo.setImage(Icons.UNDO);
		btnUndo.setText("Undo");
		
		Button btnRedo = new Button(cmpButtons, SWT.NONE);
		btnRedo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				modelProvider.getUndoRedo().redo();
			}
		});
		btnRedo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnRedo.setImage(Icons.REDO);
		btnRedo.setText("Redo");
		
		*/
		
		Composite cmpTitle = new Composite(this, SWT.NONE);
		cmpTitle.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		GridLayout gl_cmpTitle = new GridLayout();
		gl_cmpTitle.marginHeight = 0;
		gl_cmpTitle.marginWidth = 0;
		gl_cmpTitle.verticalSpacing = 0;
		cmpTitle.setLayout(gl_cmpTitle);
		cmpTitle.setBackground(getBackground());
		
		Label lblTitle = new Label(cmpTitle, SWT.NONE);
		lblTitle.setBackground(getBackground());
		lblTitle.setAlignment(SWT.CENTER);
		lblTitle.setFont(SWTResourceManager.getFont("Calibri", 18, SWT.BOLD));
		lblTitle.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblTitle.setText("Multi Sample Workflow");
		
		Composite cmpTabs = new Composite(cmpTitle, SWT.NONE);
		cmpTabs.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		cmpTabs.setBackground(getBackground());
		
		GridLayout gl_cmpTabs = new GridLayout(13, false);
		gl_cmpTabs.marginWidth = 0;
		gl_cmpTabs.marginHeight = 15;
		gl_cmpTabs.horizontalSpacing = 0;
		gl_cmpTabs.verticalSpacing = 0;
		cmpTabs.setLayout(gl_cmpTabs);

		Label lblBarBegin = new Label(cmpTabs, SWT.NONE);
		lblBarBegin.setBackground(getBackground());
		lblBarBegin.setImage(CATEGORY_BTN_IMAGE_BEGIN);
		lblBarBegin.pack();

		Label lblBar0 = new Label(cmpTabs, SWT.NONE);
		lblBar0.setBackground(getBackground());
		lblBar0.setImage(CATEGORY_BTN_IMAGE_BAR);
		lblBar0.pack();
		
		ImageButton btnUsers = new ImageButton(cmpTabs, SWT.NONE);
		btnUsers.setBackground(getBackground());
		btnUsers.setText("Experiment");
		btnUsers.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.BOLD));
		btnUsers.setImageUp(CATEGORY_BTN_IMAGE_UP);
		btnUsers.setImageOver(CATEGORY_BTN_IMAGE_OVER);
		btnUsers.setImageDown(CATEGORY_BTN_IMAGE_DOWN);
		addTriggerAreas(btnUsers);

		Label lblBar1 = new Label(cmpTabs, SWT.NONE);
		lblBar1.setBackground(getBackground());
		lblBar1.setImage(CATEGORY_BTN_IMAGE_BAR);
		lblBar1.pack();

		ImageButton btnSamples = new ImageButton(cmpTabs, SWT.NONE);
		btnSamples.setBackground(getBackground());
		btnSamples.setText("Samples");
		btnSamples.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.BOLD));
		btnSamples.setImageUp(CATEGORY_BTN_IMAGE_UP);
		btnSamples.setImageOver(CATEGORY_BTN_IMAGE_OVER);
		btnSamples.setImageDown(CATEGORY_BTN_IMAGE_DOWN);
		addTriggerAreas(btnSamples);

		Label lblBar2 = new Label(cmpTabs, SWT.NONE);
		lblBar2.setBackground(getBackground());
		lblBar2.setImage(CATEGORY_BTN_IMAGE_BAR);
		lblBar2.pack();

		ImageButton btnConfigurations = new ImageButton(cmpTabs, SWT.NONE);
		btnConfigurations.setBackground(getBackground());
		btnConfigurations.setText("Configurations");
		btnConfigurations.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.BOLD));
		btnConfigurations.setImageUp(CATEGORY_BTN_IMAGE_UP);
		btnConfigurations.setImageOver(CATEGORY_BTN_IMAGE_OVER);
		btnConfigurations.setImageDown(CATEGORY_BTN_IMAGE_DOWN);
		addTriggerAreas(btnConfigurations);

		Label lblBar3 = new Label(cmpTabs, SWT.NONE);
		lblBar3.setBackground(getBackground());
		lblBar3.setImage(CATEGORY_BTN_IMAGE_BAR);
		lblBar3.pack();

		ImageButton btnEnvironments = new ImageButton(cmpTabs, SWT.NONE);
		btnEnvironments.setBackground(getBackground());
		btnEnvironments.setText("Environments");
		btnEnvironments.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.BOLD));
		btnEnvironments.setImageUp(CATEGORY_BTN_IMAGE_UP);
		btnEnvironments.setImageOver(CATEGORY_BTN_IMAGE_OVER);
		btnEnvironments.setImageDown(CATEGORY_BTN_IMAGE_DOWN);
		addTriggerAreas(btnEnvironments);

		Label lblBar4 = new Label(cmpTabs, SWT.NONE);
		lblBar4.setBackground(getBackground());
		lblBar4.setImage(CATEGORY_BTN_IMAGE_BAR);
		lblBar4.pack();

		ImageButton btnAcquisitions = new ImageButton(cmpTabs, SWT.NONE);
		btnAcquisitions.setBackground(getBackground());
		btnAcquisitions.setText("Acquisition");
		btnAcquisitions.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.BOLD));
		btnAcquisitions.setImageUp(CATEGORY_BTN_IMAGE_UP);
		btnAcquisitions.setImageOver(CATEGORY_BTN_IMAGE_OVER);
		btnAcquisitions.setImageDown(CATEGORY_BTN_IMAGE_DOWN);
		addTriggerAreas(btnAcquisitions);

		Label lblBar5 = new Label(cmpTabs, SWT.NONE);
		lblBar5.setBackground(getBackground());
		lblBar5.setImage(CATEGORY_BTN_IMAGE_BAR);
		lblBar5.pack();

		Label lblBarEnd = new Label(cmpTabs, SWT.NONE);
		lblBarEnd.setBackground(getBackground());
		lblBarEnd.setImage(CATEGORY_BTN_IMAGE_END);
		lblBarEnd.pack();
		
		final ScrolledComposite cmpMain = new ScrolledComposite(this, SWT.H_SCROLL | SWT.V_SCROLL);
		cmpMain.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		cmpMain.setBackground(getBackground());
		cmpMain.setExpandHorizontal(true);
		cmpMain.setExpandVertical(true);
		
		final ExperimentComposite cmpExperiment = new ExperimentComposite(cmpMain, modelProvider);
		final SamplesComposite cmpSamples = new SamplesComposite(cmpMain, modelProvider);
		final ConfigurationsComposite cmpConfigurations = new ConfigurationsComposite(cmpMain, modelProvider);
		final AcquisitionComposite cmpAcquisitions = new AcquisitionComposite(cmpMain, modelProvider);

		cmpMain.setContent(cmpExperiment);
		cmpMain.setMinSize(cmpExperiment.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		btnUsers.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (cmpMain.getContent() != cmpExperiment) {
					cmpMain.setContent(cmpExperiment);
					cmpMain.setMinSize(cmpExperiment.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				}
			}
		});
		btnSamples.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (cmpMain.getContent() != cmpSamples) {
					cmpMain.setContent(cmpSamples);
					cmpMain.setMinSize(cmpSamples.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				}
			}
		});
		btnConfigurations.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {		
				if (cmpMain.getContent() != cmpConfigurations) {		
					cmpMain.setContent(cmpConfigurations);
					cmpMain.setMinSize(cmpConfigurations.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				}
			}
		});
		btnAcquisitions.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {		
				if (cmpMain.getContent() != cmpAcquisitions) {		
					cmpMain.setContent(cmpAcquisitions);
					// update acquisition table dimensions
					cmpAcquisitions.layout();
					cmpMain.setMinSize(cmpAcquisitions.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				}
			}
		});
	}

	// properties
	public ModelProvider getModelProvider() {
		return modelProvider;
	}
	
	// methods
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	// helpers
	private static void addTriggerAreas(ImageButton btn) {
		btn.addCircularTriggerArea(25, 25, CATEGORY_BTN_RADIUS);
		btn.addCircularTriggerArea(76, 25, CATEGORY_BTN_RADIUS);
		btn.addRectangularTriggerArea(25, 25 - CATEGORY_BTN_RADIUS, 51, 2 * CATEGORY_BTN_RADIUS);
	}


	class DropdownSelectionListener extends SelectionAdapter {
		private ToolItem dropdown;

		private Menu menu;

		public DropdownSelectionListener(ToolItem dropdown) {
			this.dropdown = dropdown;
			menu = new Menu(dropdown.getParent().getShell());
		}

		public void add(String item) {
			MenuItem menuItem = new MenuItem(menu, SWT.NONE);
			menuItem.setText(item);
			menuItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					//System.out.println("pressed");

					MenuItem selected = (MenuItem) event.widget;
					dropdown.setText(selected.getText());
				}
			});
		}

		public void widgetSelected(SelectionEvent event) {
			if (event.detail == SWT.ARROW) {
				ToolItem item = (ToolItem) event.widget;
				Rectangle rect = item.getBounds();
				Point pt = item.getParent().toDisplay(new Point(rect.x, rect.y));
				menu.setLocation(pt.x, pt.y + rect.height);
				menu.setVisible(true);
			} else {
				//System.out.println(dropdown.getText() + " Pressed");
			}
		}
	}
}
