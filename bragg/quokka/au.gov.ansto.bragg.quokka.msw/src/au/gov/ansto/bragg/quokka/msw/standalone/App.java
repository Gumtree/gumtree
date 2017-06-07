package au.gov.ansto.bragg.quokka.msw.standalone;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import au.gov.ansto.bragg.quokka.msw.composites.WorkflowComposite;

public class App {
	// main
	public static void main(String[] args) {
		Display display = Display.getDefault();
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				Display display = Display.getDefault();
				Shell shell = new Shell();
				shell.setSize(1200, 900);
				shell.setText("SWT Application");
				GridLayout gl_shell = new GridLayout(1, false);
				gl_shell.horizontalSpacing = 0;
				gl_shell.verticalSpacing = 0;
				gl_shell.marginWidth = 0;
				gl_shell.marginHeight = 0;
				shell.setLayout(gl_shell);
				
				shell.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

				WorkflowComposite wfl = new WorkflowComposite(shell, SWT.NONE);
				wfl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
				
				shell.open();
				shell.layout();
				while (!shell.isDisposed()) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}
			}
		});
	}
}
