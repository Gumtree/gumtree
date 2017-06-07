package au.gov.ansto.bragg.quokka.msw.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LockStateManager {
	// finals
	private final String KEY = "quokkarox";
	
	// fields
	private final AtomicBoolean locked;
	private final List<IListener> listeners;
	// dialog
	private final Shell shell;
	private final Runnable deniedDialog;
	private boolean visible;
	
	// properties
	public boolean isLocked() {
		return locked.get();
	}
	
	// construction
	public LockStateManager(final Shell shell) {
		this.shell = shell;
		this.deniedDialog = new Runnable() {
			@Override
			public void run() {
				if (!visible)
					try {
						visible = true;
						MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
						messageBox.setText("Information");
						messageBox.setMessage("Access Denied");
						messageBox.open();
					}
					finally {
						visible = false;
					}
			}
		};
		
		locked = new AtomicBoolean(true);
		listeners = new ArrayList<>();
	}
	
	// methods
	public void lock() {
		if (locked.compareAndSet(false, true))
			onLocked();
	}
	public boolean unlock() {
		PasswordRequestDialog dialog = new PasswordRequestDialog(shell);
		if (dialog.open() == PasswordRequestDialog.OK)
			if (Objects.equals(dialog.getPassword(), KEY)) {
				if (locked.compareAndSet(true, false))
					onUnlocked();
				
				return !locked.get();
			}
			else {
				deniedDialog.run();
			}
		
		return false;
	}
	public void showDeniedDialog() {
		shell.getDisplay().asyncExec(deniedDialog);
	}
	// listeners
	public void addListener(IListener listener) {
		if (listeners.contains(listener))
			throw new Error("listener already exists");
		
		listeners.add(listener);
		
		if (locked.get())
			listener.onLocked();
		else
			listener.onUnlocked();
	}
	public boolean removeListener(IListener listener) {
		return listeners.remove(listener);
	}
	// helpers
	private void onLocked() {
		for (IListener listener : listeners)
			listener.onLocked();
	}
	private void onUnlocked() {
		for (IListener listener : listeners)
			listener.onUnlocked();
	}
	
	// listener
	public static interface IListener {
		// methods
		public void onLocked();
		public void onUnlocked();
	}
	
	// dialog
	private static class PasswordRequestDialog extends TitleAreaDialog {
		// fields
		private Text passwordField;
		private String password;
		
		// construction
		public PasswordRequestDialog(Shell parentShell) {
			super(parentShell);
			password = "";
		}

		// properties
		public String getPassword() {
			return password;
		}
		@Override
		protected Point getInitialSize() {
			return new Point(480, 200);
		}
		
		// methods
		@Override
		public void create() {
			super.create();

			setTitle("Authentication");
			setMessage(
					"In order to unlock all features please enter the required password.",
					IMessageProvider.WARNING);
		}
		@Override
		protected Control createDialogArea(Composite parent) {
			Composite area = (Composite)super.createDialogArea(parent);

			Composite container = new Composite(area, SWT.NONE);
			container.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
			container.setLayout(new GridLayout(2, false));

	        Label label = new Label(container, SWT.RIGHT);
	        label.setText("Password:");
	        
	        passwordField = new Text(container, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
	        GridData gd_passwordField = new GridData(SWT.LEFT, SWT.CENTER, false, false);
	        gd_passwordField.widthHint = 150;
	        passwordField.setLayoutData(gd_passwordField);

			return area;
		}
		@Override
		protected void okPressed() {
			password = passwordField.getText();
			super.okPressed();
		}
		@Override
		protected void cancelPressed() {
			passwordField.setText("");
			super.cancelPressed();
		}
	}
}
