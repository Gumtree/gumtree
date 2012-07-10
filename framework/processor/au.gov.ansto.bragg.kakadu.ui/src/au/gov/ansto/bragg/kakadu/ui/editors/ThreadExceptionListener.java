package au.gov.ansto.bragg.kakadu.ui.editors;

import org.eclipse.swt.widgets.Shell;

import au.gov.ansto.bragg.cicada.core.Algorithm;
import au.gov.ansto.bragg.cicada.core.ThreadExceptionHandler;
import au.gov.ansto.bragg.kakadu.ui.util.DisplayManager;
import au.gov.ansto.bragg.kakadu.ui.util.Util;

public class ThreadExceptionListener implements ThreadExceptionHandler {

	Shell shell;
	
	public ThreadExceptionListener(Shell shell) {
		this.shell = shell;
	}

	public void catchException(Algorithm algorithm, final Exception e){
		e.printStackTrace();
		DisplayManager.getDefault().setEnable(true);
//		shell.getDisplay().asyncExec (new Runnable () {
		DisplayManager.getDefault().asyncExec (new Runnable () {
		      public void run () {
//		  		MessageDialog.openError(
//						shell,
//						"Algorithm Task",
//						e.getMessage());
		    	  Util.handleException(shell, e);
		      }
		   });

	}
}
