package au.gov.ansto.bragg.quokka.msw.composites;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.ui.IModelBinding;
import org.gumtree.msw.ui.ModelBinder;
import org.gumtree.msw.ui.Resources;
import org.gumtree.msw.ui.ktable.ButtonInfo;
import org.gumtree.msw.ui.ktable.CheckableCellRenderer;
import org.gumtree.msw.ui.ktable.ElementTableModel;
import org.gumtree.msw.ui.ktable.ElementTableModel.ColumnDefinition;
import org.gumtree.msw.ui.ktable.IButtonListener;
import org.gumtree.msw.ui.ktable.KTable;
import org.gumtree.msw.ui.ktable.KTableCellEditor;
import org.gumtree.msw.ui.ktable.NameCellRenderer;
import org.gumtree.msw.ui.ktable.SWTX;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorCheckbox;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorText2;
import org.gumtree.msw.ui.ktable.renderers.DefaultCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.TextCellRenderer;

import au.gov.ansto.bragg.quokka.msw.ExperimentDescription;
import au.gov.ansto.bragg.quokka.msw.IModelProviderListener;
import au.gov.ansto.bragg.quokka.msw.ModelProvider;
import au.gov.ansto.bragg.quokka.msw.User;
import au.gov.ansto.bragg.quokka.msw.UserList;
import au.gov.ansto.bragg.quokka.msw.util.CsvTable;
import au.gov.ansto.bragg.quokka.msw.util.ExperimentDescriptionLoader;
import au.gov.ansto.bragg.quokka.msw.util.IExperimentDescriptionLoaderListener;

public class ExperimentComposite extends Composite {
	// finals
	private static final String proposalCode = "proposalCode";
	private static final String exptTitle = "exptTitle";
	private static final String principalSci = "principalSci";
	private static final String otherSci = "otherSci";
	private static final String localSci = "localSci";
	
	// fields
	private final Text txtProposalNumber;
	private final Text txtExperimentTitle;
	private final ElementTableModel<UserList, User> tableModel;

	// construction
	public ExperimentComposite(Composite parent, final ModelProvider modelProvider) {
		super(parent, SWT.BORDER);
		
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 10;
		gridLayout.marginHeight = 10;
		gridLayout.horizontalSpacing = 0;
		setLayout(gridLayout);
		setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));

		Composite cmpContent = new Composite(this, SWT.NONE);
		cmpContent.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, true, 1, 1));
		cmpContent.setLayout(new GridLayout(1, false));
		cmpContent.setBackground(getBackground());
		
		final ExperimentDescriptionLoader experimentLoader = new ExperimentDescriptionLoader("QUOKKA");
		experimentLoader.addListener(new IExperimentDescriptionLoaderListener() {
			@Override
			public void onLoaded(final Map<String, String> response) {
				if (response.containsKey(proposalCode))
					modelProvider.getExperimentDescription().setProposalNumber(response.get(proposalCode));
				if (response.containsKey(exptTitle))
					modelProvider.getExperimentDescription().setExperimentTitle(response.get(exptTitle));

				List<Map<IDependencyProperty, Object>> users = new ArrayList<>();
				addUser(response, users, principalSci);
				addUser(response, users, otherSci);
				addUser(response, users, localSci);

				modelProvider.getUserList().replaceUsers(users);
			}
			// helper
			private void addUser(Map<String, String> response, List<Map<IDependencyProperty, Object>> users, String type) {
				if (!response.containsKey(type))
					return;

				String name = response.get(type);
				if ((name == null) || name.isEmpty())
					return;

				Map<IDependencyProperty, Object> properties = new HashMap<>();
				properties.put(User.NAME, name);
				users.add(properties);
			}
		});

		Composite cmpExperiment = new Composite(cmpContent, SWT.NONE);
		cmpExperiment.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		cmpExperiment.setLayout(new GridLayout(3, false));
		cmpExperiment.setBackground(getBackground());
		
		Label lblProposalNumber = new Label(cmpExperiment, SWT.NONE);
		GridData gd_lblProposalNumber = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		gd_lblProposalNumber.verticalIndent = 3;
		lblProposalNumber.setLayoutData(gd_lblProposalNumber);
		lblProposalNumber.setText("Proposal Number:");
		lblProposalNumber.setBackground(getBackground());
		
		txtProposalNumber = new Text(cmpExperiment, SWT.BORDER);
		GridData gd_txtProposalNumber = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtProposalNumber.minimumWidth = 450;
		txtProposalNumber.setLayoutData(gd_txtProposalNumber);
		txtProposalNumber.setText("");
		
		Button btnAutofill = new Button(cmpExperiment, SWT.NONE);
		btnAutofill.setToolTipText("fetch experiment information from portal");
		GridData gd_btnAutofill = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
		gd_btnAutofill.heightHint = 21;
		btnAutofill.setLayoutData(gd_btnAutofill);
		btnAutofill.setImage(Resources.IMAGE_AUTOFILL);
		btnAutofill.setText("Autofill");
		btnAutofill.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				experimentLoader.load(getShell());
			}
		});
		
		Label lblExperimentTitle = new Label(cmpExperiment, SWT.NONE);
		GridData gd_lblExperimentTitle = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		gd_lblExperimentTitle.verticalIndent = 3;
		lblExperimentTitle.setLayoutData(gd_lblExperimentTitle);
		lblExperimentTitle.setText("Experiment Title:");
		lblExperimentTitle.setBackground(getBackground());
		
		txtExperimentTitle = new Text(cmpExperiment, SWT.BORDER);
		txtExperimentTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtExperimentTitle.setText("");
		
		Label lblUsers = new Label(cmpContent, SWT.NONE);
		lblUsers.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.BOLD));
		lblUsers.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblUsers.setText("Users");

		KTable tblUsers = new KTable(cmpContent, SWTX.EDIT_ON_KEY | SWT.V_SCROLL | SWT.H_SCROLL);
		tblUsers.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, true, 1, 1));
		tblUsers.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
		
		// menu
		Menu menu = new Menu(this);
	    MenuItem menuItem;

	    // add new/saved
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Add New");
	    menuItem.setImage(Resources.IMAGE_PLUS);
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				modelProvider.getUserList().addUser();
			}
		});

	    // TODO user lookup
	    //menuItem = new MenuItem(menu, SWT.NONE);
	    //menuItem.setText("Add Saved...");
	    //menuItem.setImage(Resources.IMAGE_IMPORT_FILE);
	    //menuItem.setEnabled(false);

	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Remove All");
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				modelProvider.getUserList().clear();
			}
		});
	    
	    // enable/disable
	    new MenuItem(menu, SWT.SEPARATOR);
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Enable All");
	    menuItem.setImage(Resources.IMAGE_BOX_CHECKED);
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				modelProvider.getUserList().enableAll();
			}
		});
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Disable All");
	    menuItem.setImage(Resources.IMAGE_BOX_UNCHECKED);
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				modelProvider.getUserList().disableAll();
			}
		});
	    
	    // import/export
	    new MenuItem(menu, SWT.SEPARATOR);
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("CSV Import");
	    menuItem.setImage(Resources.IMAGE_IMPORT);
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				List<Map<IDependencyProperty, Object>> content = CsvTable.showImportDialog(
						getShell(),
						User.ENABLED,
						User.NAME,
						User.PHONE,
						User.EMAIL);
				
				if (content != null)
					modelProvider.getUserList().replaceUsers(content);
			}
		});
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("CSV Export");
	    menuItem.setImage(Resources.IMAGE_EXPORT);
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CsvTable.showExportDialog(
						getShell(),
						modelProvider.getUserList(),
						User.ENABLED,
						User.NAME,
						User.PHONE,
						User.EMAIL);
			}
		});

	    tableModel = createTableModel(tblUsers, menu, modelProvider);
	    
	    modelProvider.addListener(new IModelProviderListener() {
	    	// fields
		    final List<IModelBinding> modelBindings = new ArrayList<>();
		    final DataBindingContext bindingContext = new DataBindingContext();

		    // event handling
			@Override
			public void onReset() {
				// clear all previous bindings
				for (IModelBinding binding : modelBindings)
					binding.dispose();
				
				modelBindings.clear();
				
				initDataBindings(modelProvider, bindingContext, modelBindings);
			}
		});
	}
	
	// methods
	private void initDataBindings(ModelProvider modelProvider, DataBindingContext bindingContext, List<IModelBinding> modelBindings) {
		// source
		ExperimentDescription experimentDescription = modelProvider.getExperimentDescription();
		UserList userList = modelProvider.getUserList();

		// setup table
		tableModel.updateSource(userList);

		// data binding
		modelBindings.add(
			ModelBinder.createTextBinding(
					bindingContext,
					txtProposalNumber,
					experimentDescription,
					ExperimentDescription.PROPOSAL_NUMBER));

		modelBindings.add(
			ModelBinder.createTextBinding(
					bindingContext,
					txtExperimentTitle,
					experimentDescription,
					ExperimentDescription.EXPERIMENT_TITLE));
	}
	private static ElementTableModel<UserList, User> createTableModel(final KTable table, Menu menu, final ModelProvider modelProvider) {
		// cell rendering
    	DefaultCellRenderer checkableRenderer = new CheckableCellRenderer(CheckableCellRenderer.INDICATION_FOCUS | TextCellRenderer.INDICATION_COPYABLE);
    	DefaultCellRenderer nameRenderer = new NameCellRenderer(TextCellRenderer.INDICATION_FOCUS | TextCellRenderer.INDICATION_COPYABLE); 
    	DefaultCellRenderer textRenderer = new TextCellRenderer(TextCellRenderer.INDICATION_FOCUS | TextCellRenderer.INDICATION_COPYABLE);
    	
    	// cell editing
    	KTableCellEditor checkableEditor = new KTableCellEditorCheckbox();
    	KTableCellEditor textEditor = new KTableCellEditorText2();
    	
    	// buttons
    	IButtonListener<User> addButtonListener = new IButtonListener<User>() {
			@Override
			public void onClicked(int col, int row, User user) {
				modelProvider.getUserList().addUser(user.getIndex());
			}
		};
    	IButtonListener<User> duplicateButtonListener = new IButtonListener<User>() {
			@Override
			public void onClicked(int col, int row, User user) {
				user.duplicate();
			}
		};
    	IButtonListener<User> deleteButtonListener = new IButtonListener<User>() {
			@Override
			public void onClicked(int col, int row, User user) {
				user.delete();
			}
		};

    	// construction
    	ElementTableModel<UserList, User> model = new ElementTableModel<UserList, User>(
    			table,
    			menu,
				"add, duplicate or delete user details",
		    	Arrays.asList(
		    			new ButtonInfo<User>(Resources.IMAGE_PLUS_SMALL_GRAY, Resources.IMAGE_PLUS_SMALL, addButtonListener),
		    			new ButtonInfo<User>(Resources.IMAGE_COPY_SMALL_GRAY, Resources.IMAGE_COPY_SMALL, duplicateButtonListener),
		    			new ButtonInfo<User>(Resources.IMAGE_MINUS_SMALL_GRAY, Resources.IMAGE_MINUS_SMALL, deleteButtonListener)),
		    	Arrays.asList(
		    			new ColumnDefinition("", 30, User.ENABLED, checkableRenderer, checkableEditor),
		    			new ColumnDefinition("Name", 200, User.NAME, nameRenderer, textEditor),
		    			new ColumnDefinition("Phone", 200, User.PHONE, textRenderer, textEditor),
		    			new ColumnDefinition("Email", 200, User.EMAIL, textRenderer, textEditor)));
    	
    	table.setModel(model);
    	table.setNumRowsVisibleInPreferredSize(8);
    	
    	return model;
	}
}
