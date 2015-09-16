package au.gov.ansto.bragg.quokka.msw.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.gumtree.msw.ui.Resources;
import org.gumtree.msw.ui.ktable.ConfigurationCatalogModel;
import org.gumtree.util.eclipse.WorkspaceUtils;

import org.gumtree.msw.ui.ktable.KTable;
import org.gumtree.msw.ui.ktable.SWTX;

public class ConfigurationCatalogDialog extends TitleAreaDialog {
	// fields
	private ConfigurationCatalogModel model;
	private Iterable<IFile> configurations; // selected configurations

	// construction
	public ConfigurationCatalogDialog(Shell parentShell) {
		super(parentShell);
	}

	// properties
	public Iterable<IFile> getConfigurations() {
		return configurations;
	}
	@Override
	protected boolean isResizable() {
		return false;
	}
	
	// methods
	@Override
	public void create() {
		super.create();

		// getShell().setText("Header");
		setTitle("Configuration Catalog");
		setMessage(
				"Select configurations you would like to add to the table.",
				IMessageProvider.INFORMATION);
		
		Button ok = getButton(IDialogConstants.OK_ID);
		if (ok != null)
		  ok.setEnabled(false);
	}
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite)super.createDialogArea(parent);

		Composite container = new Composite(area, SWT.NONE);
		GridData gd_container = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_container.heightHint = 500;
		container.setLayoutData(gd_container);
		container.setLayout(new GridLayout(1, false));

		KTable tblConfigurations = new KTable(container, SWTX.EDIT_ON_KEY | SWT.V_SCROLL | SWT.H_SCROLL);
		tblConfigurations.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
		tblConfigurations.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// menu
		Menu menu = new Menu(container);
	    MenuItem menuItem;

	    // add new/saved
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Refresh");				// TODO after configurations are moved to server, the table is always up-to-date 
	    menuItem.setImage(Resources.IMAGE_PLUS);

	    model = new ConfigurationCatalogModel(
				tblConfigurations,
				WorkspaceUtils.getFolder("/Quokka/Instrument_Config"),
				menu);
		tblConfigurations.setModel(model);
		
		model.addListener(new ConfigurationCatalogModel.IListener() {
			@Override
			public void onSelectionChanged(int count) {
				Button ok = getButton(IDialogConstants.OK_ID);
				if (ok != null)
				  ok.setEnabled(count > 0);
			}
		});

		// escape should not close dialog
		parent.getShell().addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_ESCAPE)
					e.doit = false;
			}
		});
		
		return area;
	}
	// event handling 
	@Override
	protected void okPressed() {
		configurations = model.getSelected();
		super.okPressed();
	}
}
