package au.gov.ansto.bragg.kookaburra.ui.workflow;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import au.gov.ansto.bragg.kookaburra.experiment.model.InstrumentConfigTemplate;

public class SaveConfigDialog extends MessageDialog {

	private InstrumentConfigTemplate template;

	public SaveConfigDialog(Shell parentShell, InstrumentConfigTemplate template) {
		super(parentShell, "Save instrument configuration", null, "Enter config name and description", NONE,
				new String[] { IDialogConstants.OK_LABEL,
						IDialogConstants.CANCEL_LABEL }, 0);
		this.template = template;
	}

	protected Control createCustomArea(Composite parent) {
		Composite mainArea = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(mainArea);
		
		mainArea.setLayout(new GridLayout(2, false));
		Label label = new Label(mainArea, SWT.NONE);
		label.setText("Config Name: ");
		final Text nameText = new Text(mainArea, SWT.SINGLE | SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(nameText);
		label = new Label(mainArea, SWT.NONE);
		label.setText("Description: ");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).applyTo(label);
		final Text descriptionText = new Text(mainArea, SWT.SINGLE | SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(descriptionText);

		Realm.runWithDefault(DisplayRealm.getRealm(Display.getDefault()),
				new Runnable() {
					public void run() {
						DataBindingContext bindingContext = new DataBindingContext();
						bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(nameText), 
								BeanProperties.value("name").observe(template), null, null);
						bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(descriptionText), 
								BeanProperties.value("description").observe(template), null, null);
					}
				});
		return mainArea;
	}
	
}
