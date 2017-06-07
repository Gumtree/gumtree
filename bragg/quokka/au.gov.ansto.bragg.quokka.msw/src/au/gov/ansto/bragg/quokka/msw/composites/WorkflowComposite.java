package au.gov.ansto.bragg.quokka.msw.composites;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
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
import au.gov.ansto.bragg.quokka.msw.util.LockStateManager;

public class WorkflowComposite extends Composite {
	// resources
    private static final Image CATEGORY_BTN_IMAGE_BAR = Resources.load("/icons/Button_bar.png", 40, -1);
    private static final Image CATEGORY_BTN_IMAGE_BEGIN = Resources.load("/icons/Button_begin.png");
    private static final Image CATEGORY_BTN_IMAGE_END = Resources.load("/icons/Button_end.png");
    private static final int CATEGORY_BTN_RADIUS = 21;
    // green
    private static final Image CATEGORY_BTN_IMAGE_GREEN_UP = Resources.load("/icons/Button_green_up.png");
    private static final Image CATEGORY_BTN_IMAGE_GREEN_OVER = Resources.load("/icons/Button_green_over.png");
    private static final Image CATEGORY_BTN_IMAGE_GREEN_DOWN = Resources.load("/icons/Button_green_down.png");
    // blue
    private static final Image CATEGORY_BTN_IMAGE_BLUE_UP = Resources.load("/icons/Button_blue_up.png");
    private static final Image CATEGORY_BTN_IMAGE_BLUE_OVER = Resources.load("/icons/Button_blue_over.png");
    private static final Image CATEGORY_BTN_IMAGE_BLUE_DOWN = Resources.load("/icons/Button_blue_down.png");
	// xsd/xml
	private static final String MSW_XSD = "resources/msw.xsd";
	private static final String MSW_XML = "resources/msw.xml"; // "resources/example.xml"; // 

    // fields
    private final LockStateManager lockStateManager;
    private final ModelProvider modelProvider = new ModelProvider(
    		new DummyModelProxy(new SynchronizedModel(new Model(
    				DummyRefIdProvider.DEFAULT,
    				Activator.getEntry(MSW_XSD),
    				Activator.getEntry(MSW_XML)))));

    // construction
	public WorkflowComposite(Composite parent, int style) {
		super(parent, style);
		
		lockStateManager = new LockStateManager(getShell());
		
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.verticalSpacing = 0;
		setLayout(gridLayout);
		setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		
		final ToolBar toolBar = new ToolBar(this, SWT.HORIZONTAL);
		toolBar.setBackground(getBackground());

	    ToolItem btnNew = new ToolItem(toolBar, SWT.PUSH);
		btnNew.setImage(Resources.IMAGE_NEW);
		btnNew.setText("New");
		btnNew.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				modelProvider.getModelProxy().reset();
			}
		});
		
		ToolItem btnLoad = new ToolItem(toolBar, SWT.PUSH);
		btnLoad.setImage(Resources.IMAGE_LOAD);
		btnLoad.setText("Load");
		btnLoad.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);

				fileDialog.setFilterNames(new String[] { "Extensible Markup Language (*.xml)", "All Files (*.*)" });
				fileDialog.setFilterExtensions(new String[] { "*.xml", "*.*" });

				String filename = fileDialog.open();
				if ((filename != null) && (filename.length() > 0)) {
					boolean succeeded = false;
					try (InputStream stream = new FileInputStream(filename)) {
						succeeded = modelProvider.getModelProxy().deserializeFrom(stream);
					}
					catch (Exception e2) {
					}
					if (!succeeded) {
						MessageBox dialog = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
						dialog.setText("Warning");
						dialog.setMessage("Unable to load to selected xml file.");
						dialog.open();
					}
				}
			}
		});
		
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
		
	    final ToolItem btnUnlock = new ToolItem(toolBar, SWT.PUSH);
		btnUnlock.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!lockStateManager.isLocked())
					lockStateManager.lock();
				else
					lockStateManager.unlock();
			}
		});
		lockStateManager.addListener(new LockStateManager.IListener() {
			@Override
			public void onLocked() {
				btnUnlock.setImage(Resources.IMAGE_LOCK_CLOSED);
				btnUnlock.setText("Unlock");
			}
			@Override
			public void onUnlocked() {
				btnUnlock.setImage(Resources.IMAGE_LOCK_OPEN);
				btnUnlock.setText("Lock");
			}
		});

	    new ToolItem(toolBar, SWT.SEPARATOR);
	    
	    final ToolItem btnUndo = new ToolItem(toolBar, SWT.PUSH); // SWT.DROP_DOWN);
		btnUndo.setImage(Resources.IMAGE_UNDO);
		btnUndo.setText("Undo");
		btnUndo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				modelProvider.getUndoRedo().undo();
			}
		});
		
		final ToolItem btnRedo = new ToolItem(toolBar, SWT.PUSH); // SWT.DROP_DOWN);
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
		
		final ImageButton btnUsers = new ImageButton(cmpTabs, SWT.NONE);
		btnUsers.setBackground(getBackground());
		btnUsers.setText("Experiment");
		btnUsers.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.BOLD));
		unselectedButton(btnUsers);
		addTriggerAreas(btnUsers);

		Label lblBar1 = new Label(cmpTabs, SWT.NONE);
		lblBar1.setBackground(getBackground());
		lblBar1.setImage(CATEGORY_BTN_IMAGE_BAR);
		lblBar1.pack();

		final ImageButton btnSamples = new ImageButton(cmpTabs, SWT.NONE);
		btnSamples.setBackground(getBackground());
		btnSamples.setText("Samples");
		btnSamples.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.BOLD));
		unselectedButton(btnSamples);
		addTriggerAreas(btnSamples);

		Label lblBar2 = new Label(cmpTabs, SWT.NONE);
		lblBar2.setBackground(getBackground());
		lblBar2.setImage(CATEGORY_BTN_IMAGE_BAR);
		lblBar2.pack();

		final ImageButton btnConfigurations = new ImageButton(cmpTabs, SWT.NONE);
		btnConfigurations.setBackground(getBackground());
		btnConfigurations.setText("Configurations");
		btnConfigurations.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.BOLD));
		unselectedButton(btnConfigurations);
		addTriggerAreas(btnConfigurations);

		Label lblBar3 = new Label(cmpTabs, SWT.NONE);
		lblBar3.setBackground(getBackground());
		lblBar3.setImage(CATEGORY_BTN_IMAGE_BAR);
		lblBar3.pack();

		final ImageButton btnEnvironments = new ImageButton(cmpTabs, SWT.NONE);
		btnEnvironments.setBackground(getBackground());
		btnEnvironments.setText("Environments");
		btnEnvironments.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.BOLD));
		unselectedButton(btnEnvironments);
		addTriggerAreas(btnEnvironments);

		Label lblBar4 = new Label(cmpTabs, SWT.NONE);
		lblBar4.setBackground(getBackground());
		lblBar4.setImage(CATEGORY_BTN_IMAGE_BAR);
		lblBar4.pack();

		final ImageButton btnAcquisitions = new ImageButton(cmpTabs, SWT.NONE);
		btnAcquisitions.setBackground(getBackground());
		btnAcquisitions.setText("Acquisition");
		btnAcquisitions.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.BOLD));
		unselectedButton(btnAcquisitions);
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
		
		final ExperimentComposite cmpExperiment = new ExperimentComposite(cmpMain, modelProvider, lockStateManager);
		final SamplesComposite cmpSamples = new SamplesComposite(cmpMain, modelProvider, lockStateManager);
		final ConfigurationsComposite cmpConfigurations = new ConfigurationsComposite(cmpMain, modelProvider, lockStateManager);
		final EnvironmentsComposite cmpEnvironments = new EnvironmentsComposite(cmpMain, modelProvider, lockStateManager);
		final AcquisitionComposite cmpAcquisitions = new AcquisitionComposite(cmpMain, modelProvider, lockStateManager);

		btnUsers.addListener(
				SWT.Selection,
				new ButtonListener(
						cmpMain, cmpExperiment,
						btnUndo, btnRedo, true,
						btnUsers,
						btnSamples, btnConfigurations, btnEnvironments, btnAcquisitions));

		btnSamples.addListener(
				SWT.Selection,
				new ButtonListener(
						cmpMain, cmpSamples,
						btnUndo, btnRedo, true,
						btnSamples,
						btnUsers, btnConfigurations, btnEnvironments, btnAcquisitions));

		btnConfigurations.addListener(
				SWT.Selection,
				new ButtonListener(
						cmpMain, cmpConfigurations,
						btnUndo, btnRedo, true,
						btnConfigurations,
						btnUsers, btnSamples, btnEnvironments, btnAcquisitions));

		btnEnvironments.addListener(
				SWT.Selection,
				new ButtonListener(
						cmpMain, cmpEnvironments,
						btnUndo, btnRedo, true,
						btnEnvironments,
						btnUsers, btnSamples, btnConfigurations, btnAcquisitions));

		btnAcquisitions.addListener(
				SWT.Selection,
				new ButtonListener(
						cmpMain, cmpAcquisitions,
						btnUndo, btnRedo, false,
						btnAcquisitions,
						btnUsers, btnSamples, btnConfigurations, btnEnvironments));

		btnUsers.notifyListeners(SWT.Selection, new Event());
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
	private static void updateSelectedButton(ImageButton selected, ImageButton ... unselected) {
		selectedButton(selected);
		for (ImageButton button : unselected)
			unselectedButton(button);
	}
	private static void selectedButton(ImageButton btn) {
		btn.setImageUp(CATEGORY_BTN_IMAGE_BLUE_UP);
		btn.setImageOver(CATEGORY_BTN_IMAGE_BLUE_OVER);
		btn.setImageDown(CATEGORY_BTN_IMAGE_BLUE_DOWN);
		btn.redraw();
	}
	private static void unselectedButton(ImageButton btn) {
		btn.setImageUp(CATEGORY_BTN_IMAGE_GREEN_UP);
		btn.setImageOver(CATEGORY_BTN_IMAGE_GREEN_OVER);
		btn.setImageDown(CATEGORY_BTN_IMAGE_GREEN_DOWN);
		btn.redraw();
	}
	private static void addTriggerAreas(ImageButton btn) {
		btn.addCircularTriggerArea(25, 25, CATEGORY_BTN_RADIUS);
		btn.addCircularTriggerArea(76, 25, CATEGORY_BTN_RADIUS);
		btn.addRectangularTriggerArea(25, 25 - CATEGORY_BTN_RADIUS, 51, 2 * CATEGORY_BTN_RADIUS);
	}
	
	class ButtonListener implements Listener {
		// fields
		private final ScrolledComposite cmpMain;
		private final Composite cmpTarget;
		// undo/redo
		private final ToolItem btnUndo;
		private final ToolItem btnRedo;
		private final boolean undoRedo;
		// buttons
		private final ImageButton btnSelected;
		private final ImageButton[] btnUnselected;
		
		// construction
		public ButtonListener(ScrolledComposite cmpMain, Composite cmpTarget, ToolItem btnUndo, ToolItem btnRedo, boolean undoRedo, ImageButton btnSelected, ImageButton ... btnUnselected) {
			this.cmpMain = cmpMain;
			this.cmpTarget = cmpTarget;
			
			this.btnUndo = btnUndo;
			this.btnRedo = btnRedo;
			this.undoRedo = undoRedo;

			this.btnSelected = btnSelected;
			this.btnUnselected = btnUnselected;
		}
		
		// event handling
		@Override
		public void handleEvent(Event event) {
			if (cmpMain.getContent() != cmpTarget) {
				btnUndo.setEnabled(undoRedo);
				btnRedo.setEnabled(undoRedo);

				updateSelectedButton(btnSelected, btnUnselected);
				
				cmpMain.setContent(cmpTarget);
				
				cmpTarget.layout();
				cmpMain.setMinSize(cmpTarget.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		}
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
