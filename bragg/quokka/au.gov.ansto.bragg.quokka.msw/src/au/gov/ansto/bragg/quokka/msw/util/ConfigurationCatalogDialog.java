package au.gov.ansto.bragg.quokka.msw.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.regex.Pattern;

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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.gumtree.msw.ui.ktable.FilesystemModel;
import org.gumtree.msw.ui.ktable.KTable;
import org.gumtree.msw.ui.ktable.SWTX;
import org.gumtree.util.eclipse.WorkspaceUtils;

public class ConfigurationCatalogDialog extends TitleAreaDialog {
	// finals
	public final static Path INSTRUMENT_CONFIG_ROOT;
	
	// fields
	private FilesystemModel model;
	private Iterable<Path> configurations = Collections.emptyList();

	// construction
	static {
		Path root;
		try {
			root = Paths.get(WorkspaceUtils.getFolder("/Quokka/Instrument_Config").getLocation().toString());
		}
		catch (IllegalStateException e) {
			if (!e.getMessage().equals("Workspace is closed."))
				throw e;

			root = Paths.get("D:/Ansto/runtime-QuokkaExperimentWorkbench(DEV).product/Gumtree_Workspace/Quokka/Instrument_Config/");
		}
		INSTRUMENT_CONFIG_ROOT = root;
	}
	public ConfigurationCatalogDialog(Shell parentShell) {
		super(parentShell);
	}

	// properties
	public Iterable<Path> getConfigurations() {
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
				"Select all configurations you would like to add to the table.",
				IMessageProvider.INFORMATION);
		
		Button ok = getButton(IDialogConstants.OK_ID);
		if (ok != null)
		  ok.setEnabled(false);
	}
	@Override
	public boolean close() {
		if (model != null)
			model.setRoot(null);
		return super.close();
	}
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite)super.createDialogArea(parent);

		Composite container = new Composite(area, SWT.NONE);
		GridData gd_container = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_container.heightHint = 700;
		container.setLayoutData(gd_container);
		container.setLayout(new GridLayout(1, false));

		KTable tblConfigurations = new KTable(container, SWTX.EDIT_ON_KEY | SWT.V_SCROLL | SWT.H_SCROLL);
		tblConfigurations.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
		tblConfigurations.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

	    model = new FilesystemModel(tblConfigurations, Pattern.compile("^.*\\.[xX][mM][lL]$"), "New", ".xml", new byte[0]); // look for XML files // new files have XML extension and are empty
	    model.setRoot(INSTRUMENT_CONFIG_ROOT);
		model.addListener(new FilesystemModel.IListener() {
			@Override
			public void onSelectionChanged(int count) {
				Button ok = getButton(IDialogConstants.OK_ID);
				if (ok != null)
				  ok.setEnabled(count > 0);
			}
		});

		tblConfigurations.setModel(model);

		// if table receives TRAVERSE_ESCAPE any changed edit will reset, or nothing will happen
		// to make it consistent, if shell has focus, TRAVERSE_ESCAPE shouldn't do anything
		parent.getShell().addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_ESCAPE)
					e.doit = false;
			}
		});
		
		return area;
	}
	@Override
	protected void okPressed() {
		if (model.getSelectedCount() > 32) {
			final String newLine = System.getProperty("line.separator");
			
			MessageBox dialog = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
			dialog.setText("Warning");
			dialog.setMessage("You have selected more than 32 configurations!" + newLine + newLine + "Please reduce the number of selected configurations.");
			dialog.open();
			return;
		}
		configurations = model.getSelectedFiles();
		super.okPressed();
	}
}
