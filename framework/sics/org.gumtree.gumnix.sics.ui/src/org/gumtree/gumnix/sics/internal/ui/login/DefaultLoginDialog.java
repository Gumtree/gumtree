package org.gumtree.gumnix.sics.internal.ui.login;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.gumtree.gumnix.sics.core.IInstrumentProfile;
import org.gumtree.gumnix.sics.io.SicsRole;

@Deprecated
public class DefaultLoginDialog extends BaseLoginDialog {

	private Text hostText;

	private Text portText;

	private Text passwordText;

	public DefaultLoginDialog(Shell parentShell, ILoginHandler handler) {
		super(parentShell, handler);
		// Ensure the default is set in this dialog instance
//		IInstrumentProfile[] profiles = SicsCore.getSicsManager().service().getRegisteredProfiles();
//		if(profiles.length > 0) {
//			setInstrumentProfile(profiles[0]);
//		}
		// use command line option to overwrite settings
//		handleCommandLineOptions();
	}

	protected Control createDialogArea(Composite parent) {
		Composite mainComposite = (Composite) super.createDialogArea(parent);
		mainComposite.setLayout(new GridLayout());

		Group instrumentSelectionGroup = new Group(mainComposite, SWT.NONE);
		instrumentSelectionGroup.setText("Select Instrument Profile");
		instrumentSelectionGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createInstrumentSelectionGroup(instrumentSelectionGroup);

		Group loginGroup = new Group(mainComposite, SWT.NONE);
		loginGroup.setText("Login");
		loginGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		createLoginGroup(loginGroup);

		return mainComposite;
	}

	private void createInstrumentSelectionGroup(Composite parent) {
		parent.setLayout(new FillLayout());
		ListViewer listViewer = new ListViewer(parent);
		listViewer.setContentProvider(new ArrayContentProvider());
		listViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				if(element instanceof IInstrumentProfile) {
					IInstrumentProfile profile = (IInstrumentProfile)element;
					return profile.getLabel() != null ? profile.getLabel() : profile.getId();
				}
				return super.getText(element);
			}
		});
		listViewer.setComparator(new ViewerComparator() {
			public int compare(Viewer viewer, Object e1, Object e2) {
				if(e1 instanceof IInstrumentProfile && e2 instanceof IInstrumentProfile) {
					if(((IInstrumentProfile)e1).isDefault()) {
						return -1;
					}
					if(((IInstrumentProfile)e2).isDefault()) {
						return 1;
					}
				}
				return super.compare(viewer, e1, e2);
			}
		});
//		IInstrumentProfile[] profiles = SicsCore.getSicsManager().service().getRegisteredProfiles();
//		listViewer.setInput(profiles);
		listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object selection = ((IStructuredSelection)event.getSelection()).getFirstElement();
				if(selection instanceof IInstrumentProfile) {
//					IInstrumentProfile profile = (IInstrumentProfile)selection;
//					setInstrumentProfile(profile);
//					if(!profile.isDefault()) {
//						String defaultHost = profile.getProperty(ConfigProperty.DEFAULT_HOST);
//						String defaultPort = profile.getProperty(ConfigProperty.DEFAULT_PORT);
//						if(defaultHost != null) {
//							hostText.setText(defaultHost);
//						}
//						if(defaultPort != null) {
//							portText.setText(defaultPort);
//						}
//					}
				}
			}
		});
		// Select default
//		listViewer.setSelection(new StructuredSelection(getInstrumentProfile()));
	}

	private void createLoginGroup(Composite parent) {
		parent.setLayout(new GridLayout(2, false));

		Label label = new Label(parent, SWT.NONE);
		label.setText("Host: ");

		hostText = new Text(parent, SWT.SINGLE
				| SWT.BORDER);
		hostText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//		getBindingContext().bind(hostText, new Property(getConnectionContext(), "host"), null);

		label = new Label(parent, SWT.NONE);
		label.setText("Port: ");

		portText = new Text(parent, SWT.SINGLE
				| SWT.BORDER);
		portText.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {

			}
		});
		portText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//		getBindingContext().bind(portText, new Property(getConnectionContext(), "port"), null);

		Group roleGroup = new Group(parent, SWT.NONE);
		roleGroup.setText("Role");
		GridData roleGroupData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		roleGroupData.horizontalSpan = 2;
		roleGroup.setLayoutData(roleGroupData);
		roleGroup.setLayout(new FillLayout(SWT.HORIZONTAL));

		Button userRoleButton = new Button(roleGroup, SWT.RADIO);
		userRoleButton.setText("User");
		userRoleButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getConnectionContext().setRole(SicsRole.USER);
			}
		});

		Button spyRoleButton = new Button(roleGroup, SWT.RADIO);
		spyRoleButton.setText("Spy");
		spyRoleButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getConnectionContext().setRole(SicsRole.SPY);
			}
		});

		Button managerRoleButton = new Button(roleGroup, SWT.RADIO);
		managerRoleButton.setText("Manager");
		managerRoleButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getConnectionContext().setRole(SicsRole.MANAGER);
			}
		});

		// Select role based on current context
		if(getConnectionContext().getRole().equals(SicsRole.SPY)) {
			spyRoleButton.setSelection(true);
		} else if(getConnectionContext().getRole().equals(SicsRole.MANAGER)) {
			managerRoleButton.setSelection(true);
		} else {
			userRoleButton.setSelection(true);
			getConnectionContext().setRole(SicsRole.USER);
		}

		Label passwordLabel = new Label(parent, SWT.NONE);
		passwordLabel.setText("Password: ");

		passwordText = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
		passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
//		getBindingContext().bind(passwordText, new Property(getConnectionContext(), "password"), null);
		
		Realm.runWithDefault(DisplayRealm.getRealm(PlatformUI.getWorkbench()
				.getDisplay()), new Runnable() {
			public void run() {
				DataBindingContext bindingContext = new DataBindingContext();
				bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(hostText),
						BeanProperties.value("host").observe(getConnectionContext()), 
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(portText),
						BeanProperties.value("port").observe(getConnectionContext()), 
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(passwordText),
						BeanProperties.value("password").observe(getConnectionContext()), 
						new UpdateValueStrategy(), new UpdateValueStrategy());
			}
		});
	}

}
