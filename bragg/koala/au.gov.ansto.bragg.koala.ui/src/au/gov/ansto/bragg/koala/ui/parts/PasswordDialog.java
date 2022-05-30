/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import au.gov.ansto.bragg.koala.ui.Activator;

/**
 * @author nxi
 *
 */
public class PasswordDialog extends Dialog {
    private Text txtUser;
    private Text txtPassword;
    private String user = "";
    private String password = "";

    public PasswordDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginRight = 16;
        layout.marginLeft = 16;
        layout.marginTop = 16;
        container.setLayout(layout);

        Label title = new Label(container, SWT.NONE);
        title.setText("Please login as instrument scientist.");
        title.setFont(Activator.getMiddleFont());
        title.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
                2, 1));
        Label space = new Label(container, SWT.NONE);
        space.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
                2, 1));
        
        Label lblUser = new Label(container, SWT.NONE);
        lblUser.setText("Username:");
        lblUser.setFont(Activator.getMiddleFont());

        txtUser = new Text(container, SWT.BORDER);
        txtUser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
                1, 1));
        txtUser.setText(user);
        txtUser.setFont(Activator.getMiddleFont());
        txtUser.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
	            Text textWidget = (Text) e.getSource();
	            String userText = textWidget.getText();
	            user = userText;
			}
		});

        Label lblPassword = new Label(container, SWT.NONE);
        GridData gridDataPasswordLabel = new GridData(SWT.LEFT, SWT.CENTER, false,false);
        gridDataPasswordLabel.horizontalIndent = 1;
        lblPassword.setLayoutData(gridDataPasswordLabel);
        lblPassword.setText("Password:");
        lblPassword.setFont(Activator.getMiddleFont());

        txtPassword = new Text(container, SWT.BORDER| SWT.PASSWORD);
        txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        txtPassword.setText(password);
        txtPassword.setFont(Activator.getMiddleFont());
        txtPassword.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				Text textWidget = (Text) e.getSource();
	            String passwordText = textWidget.getText();
	            password = passwordText;
			}
		});
        return container;
    }

    // override method to use "Login" as label for the OK button
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        Button okButton = createButton(parent, IDialogConstants.OK_ID, "Login", true);
        okButton.setFont(Activator.getMiddleFont());
        Button ccButton = createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
        ccButton.setFont(Activator.getMiddleFont());
    }

    @Override
    protected Point getInitialSize() {
        return new Point(600, 400);
    }

    @Override
    protected void okPressed() {
        user = txtUser.getText();
        password = txtPassword.getText();
        super.okPressed();
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}