package au.gov.ansto.bragg.quokka.msw.composites;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
import org.gumtree.msw.ui.ModelBinder;
import org.gumtree.msw.ui.Resources;
import org.gumtree.msw.ui.ktable.ButtonInfo;
import org.gumtree.msw.ui.ktable.CheckableCellRenderer;
import org.gumtree.msw.ui.ktable.IButtonListener;
import org.gumtree.msw.ui.ktable.ElementTableModel;
import org.gumtree.msw.ui.ktable.ElementTableModel.ColumnDefinition;
import org.gumtree.msw.ui.ktable.NameCellRenderer;

import au.gov.ansto.bragg.quokka.msw.ExperimentDescription;
import au.gov.ansto.bragg.quokka.msw.ModelProvider;
import au.gov.ansto.bragg.quokka.msw.User;
import au.gov.ansto.bragg.quokka.msw.UserList;
import au.gov.ansto.bragg.quokka.msw.util.CsvTableExporter;
import au.gov.ansto.bragg.quokka.msw.util.CsvTableImporter;
import au.gov.ansto.bragg.quokka.msw.util.ExperimentDescriptionLoader;
import au.gov.ansto.bragg.quokka.msw.util.IExperimentDescriptionLoaderListener;
import org.gumtree.msw.ui.ktable.KTable;
import org.gumtree.msw.ui.ktable.KTableCellEditor;
import org.gumtree.msw.ui.ktable.SWTX;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorCheckbox;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorText2;
import org.gumtree.msw.ui.ktable.renderers.DefaultCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.TextCellRenderer;

public class ExperimentComposite extends Composite {
	// finals
	private static final String proposalCode = "proposalCode";
	private static final String exptTitle = "exptTitle";
	private static final String principalSci = "principalSci";
	private static final String otherSci = "otherSci";
	private static final String localSci = "localSci";
	
	// fields
	private final KTable tblUsers;
	private final Menu menu;
	private ExperimentDescription experimentDescription;
	private UserList userList;
	private Text txtProposalNumber;
	private Text txtExperimentTitle;

	// construction
	public ExperimentComposite(Composite parent, ModelProvider provider) {
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
				getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (response.containsKey(proposalCode))
							experimentDescription.setProposalNumber(response.get(proposalCode));
						if (response.containsKey(exptTitle))
							experimentDescription.setExperimentTitle(response.get(exptTitle));

						List<Map<IDependencyProperty, Object>> users = new ArrayList<>();
						addUser(response, users, principalSci);
						addUser(response, users, otherSci);
						addUser(response, users, localSci);

						userList.replaceUsers(users);
					}
				});
			}
			// helper
			private void addUser(Map<String, String> response, List<Map<IDependencyProperty, Object>> users, String type) {
				if (!response.containsKey(type))
					return;

				String name = response.get(type);
				if ((name == null) || "".equals(name))
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
				experimentLoader.load();
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

		tblUsers = new KTable(cmpContent, SWTX.EDIT_ON_KEY | SWT.V_SCROLL | SWT.H_SCROLL);
		tblUsers.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, true, 1, 1));
		tblUsers.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
		
		// menu
	    menu = new Menu(this);
	    MenuItem menuItem;

	    // add new/saved
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Add New");
	    menuItem.setImage(Resources.IMAGE_PLUS);
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				userList.addUser();
			}
		});

	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Add Saved...");
	    menuItem.setImage(Resources.IMAGE_IMPORT_FILE);
	    menuItem.setEnabled(false);

	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Remove All");
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				userList.clear();
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
				userList.enableAll();
			}
		});
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Disable All");
	    menuItem.setImage(Resources.IMAGE_BOX_UNCHECKED);
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				userList.disableAll();
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
				List<Map<IDependencyProperty, String>> content = CsvTableImporter.showDialog(
						getShell(),
						userList,
						User.ENABLED,
						User.NAME,
						User.PHONE,
						User.EMAIL);
				
				if (content != null)
					userList.replaceUsers(content, true);
			}
		});
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("CSV Export");
	    menuItem.setImage(Resources.IMAGE_EXPORT);
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CsvTableExporter.showDialog(
						getShell(),
						userList,
						User.ENABLED,
						User.NAME,
						User.PHONE,
						User.EMAIL);
			}
		});
	    
	    initDataBindings(provider);
	}
	
	// methods
	private void initDataBindings(ModelProvider provider) {
		if (provider == null)
			return;
		
		experimentDescription = provider.getExperimentDescription();
		userList = provider.getUserList();

		// setup table
		createTableModel(tblUsers, userList, menu);
		
		// data binding
		DataBindingContext bindingContext = new DataBindingContext();
		
		ModelBinder.createTextBinding(
				bindingContext,
				txtProposalNumber,
				experimentDescription,
				ExperimentDescription.PROPOSAL_NUMBER);
		ModelBinder.createTextBinding(
				bindingContext,
				txtExperimentTitle,
				experimentDescription,
				ExperimentDescription.EXPERIMENT_TITLE);
	}
	private static ElementTableModel<UserList, User> createTableModel(KTable table, final UserList userList, Menu menu) {
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
				userList.addUser(user.getIndex());
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
    			userList,
    			menu,
				"add, duplicate or delete user details",
		    	Arrays.asList(
		    			new ButtonInfo<User>(Resources.IMAGE_PLUS_SMALL_GRAY, Resources.IMAGE_PLUS_SMALL, addButtonListener),
		    			new ButtonInfo<User>(Resources.IMAGE_COPY_SMALL_GRAY, Resources.IMAGE_COPY_SMALL, duplicateButtonListener),
		    			new ButtonInfo<User>(Resources.IMAGE_MINUS_SMALL_GRAY, Resources.IMAGE_MINUS_SMALL, deleteButtonListener)),
		    	Arrays.asList(
		    			new ColumnDefinition(User.ENABLED, "", 30, checkableRenderer, checkableEditor),
		    			new ColumnDefinition(User.NAME, "Name", 200, nameRenderer, textEditor),
		    			new ColumnDefinition(User.PHONE, "Phone", 200, textRenderer, textEditor),
		    			new ColumnDefinition(User.EMAIL, "Email", 200, textRenderer, textEditor)));
    	
    	table.setModel(model);
    	table.setNumRowsVisibleInPreferredSize(8);
    	return model;
	}
}
