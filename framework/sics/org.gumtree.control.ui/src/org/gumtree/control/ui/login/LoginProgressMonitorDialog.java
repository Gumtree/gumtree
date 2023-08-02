package org.gumtree.control.ui.login;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.gumtree.control.core.ISicsConnectionContext;
import org.gumtree.control.core.SicsManager;

import ch.psi.sics.hipadaba.SICS;

public class LoginProgressMonitorDialog extends ProgressMonitorDialog {

	public LoginProgressMonitorDialog(Shell parent) {
		super(parent);
	}

	protected Point getInitialLocation(Point initialSize) {
		Shell topLevelShell = getTopLevelShell();
		Point centerPoint = Geometry.centerPoint(topLevelShell.getClientArea());
		return new Point(centerPoint.x - (initialSize.x / 2), centerPoint.y - (initialSize.y / 2));
	}
	
	private Shell getTopLevelShell() {
		Shell shell = getShell();
		while(shell.getParent() != null && shell.getParent() instanceof Shell) {
			shell = (Shell) shell.getParent();
		}
		return shell;
	}
	
	public void runDialog(final ISicsConnectionContext context) throws InvocationTargetException, InterruptedException {
		run(true, false, new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				monitor.beginTask("Communicating with SICS...", IProgressMonitor.UNKNOWN);
				monitor.subTask("Logging into the server");
				try {
					SicsManager.getSicsProxy().connect(context.getServerAddress(), 
							context.getPublisherAddress());
				} catch (Exception e) {
//					throw new InvocationTargetException(e, e.getMessage());
					e.printStackTrace();
				}
//				monitor.subTask("Setting instrument profile");
//				SicsCore.getSicsManager().service().setCurrentInstrumentProfile(profile);
				monitor.subTask("Fetching SICS model from the server");
				// Cause the UI thread to load the hipadaba model first
				SICS sics = null;
				int counter = 0;
				Exception exp = null;
				while (sics == null && counter < 60) {
					try {
						sics = SicsManager.getSicsModel().getBase();
					} catch (Exception e) {
						exp = e;
						counter++;
						try {
							Thread.sleep(200);
						} catch (InterruptedException e1) {
						}
					}
				}
				if (sics == null) {
					throw new InvocationTargetException(exp, exp.getMessage());
				}
				monitor.done();
			}
		});
	}
	
}
