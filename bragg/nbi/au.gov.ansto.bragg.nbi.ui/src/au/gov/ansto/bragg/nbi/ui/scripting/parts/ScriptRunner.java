/**
 * 
 */
package au.gov.ansto.bragg.nbi.ui.scripting.parts;

import java.util.List;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * @author nxi
 *
 */
public class ScriptRunner {

	protected static String fileDialogPath;
	private String scriptPath;
	private boolean actionPerformed = false;
	private boolean isConfirmed;
	private String folderPath;
	private String filePath;
	private Shell shell;

	public ScriptRunner(Shell shell) {
		this.shell = shell;
	}
	
	/**
	 * @return the scriptPath
	 */
	public String getScriptPath() {
		return scriptPath;
	}

	/**
	 * @param scriptPath the scriptPath to set
	 */
	public void setScriptPath(String scriptPath) {
		this.scriptPath = scriptPath;
	}
	
	public boolean openConfirm(final String msg){
		setActionPerformed(false);
		shell.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				isConfirmed = MessageDialog.openConfirm(shell, "Please Confirm", msg);
				System.out.println("confirmed directly");
				setActionPerformed(true);
			}
		});
		
		while (!isActionPerformed()){
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				System.out.println("can't wait");
			}
		}
		return isConfirmed();
	}

	public boolean openQuestion(final String msg){
		setActionPerformed(false);
		shell.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				isConfirmed = MessageDialog.openQuestion(shell, "Gumtree Dialog", msg);
				setActionPerformed(true);
			}
		});
		
		while (!isActionPerformed()){
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				System.out.println("can't wait");
			}
		}
		return isConfirmed();
	}
	
	public void openError(final String msg){
		setActionPerformed(false);
		shell.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				MessageDialog.openError(shell, "Error", msg);
				setActionPerformed(true);
			}
		});
		
		while (!isActionPerformed()){
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				System.out.println("can't wait");
			}
		}
	}
	
	public void openInformation(final String msg){
		setActionPerformed(false);
		shell.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				MessageDialog.openInformation(shell, "Gumtree Information", msg);
				setActionPerformed(true);
			}
		});
		
		while (!isActionPerformed()){
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				System.out.println("can't wait");
			}
		}
	}
	
	public void openWarning(final String msg){
		setActionPerformed(false);
		shell.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				MessageDialog.openWarning(shell, "Warning", msg);
				setActionPerformed(true);
			}
		});
		
		while (!isActionPerformed()){
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				System.out.println("can't wait");
			}
		}
	}
	
	public String selectSaveFile(final List<String> extNames){
		setActionPerformed(false);
		shell.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				FileDialog dialog = new FileDialog(shell, SWT.SINGLE);
 				if (fileDialogPath == null){
 					IWorkspace workspace= ResourcesPlugin.getWorkspace();
 					IWorkspaceRoot root = workspace.getRoot();
 					dialog.setFilterPath(root.getLocation().toOSString());
 				} else {
 					dialog.setFilterPath(fileDialogPath);
 				}
 				if (extNames != null && extNames.size() > 0) {
 					String[] extArray = new String[extNames.size()];
 					dialog.setFilterExtensions(extNames.toArray(extArray));
 				}
 				String filePath = dialog.open();
 				setFilePath(filePath);
 				fileDialogPath = filePath;
				setActionPerformed(true);
			}
		});
		
		while (!isActionPerformed()){
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				System.out.println("can't wait");
			}
		}
		return getFilePath();
	}

	public String selectSaveFolder(){
		setActionPerformed(false);
		shell.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				DirectoryDialog dialog = new DirectoryDialog(shell, SWT.MULTI);
 				if (fileDialogPath == null){
 					IWorkspace workspace= ResourcesPlugin.getWorkspace();
 					IWorkspaceRoot root = workspace.getRoot();
 					dialog.setFilterPath(root.getLocation().toOSString());
 				} else {
 					dialog.setFilterPath(fileDialogPath);
 				}
 				String filePath = dialog.open();
 				setFolderPath(filePath);
 				fileDialogPath = filePath;
				setActionPerformed(true);
			}
		});
		
		while (!isActionPerformed()){
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				System.out.println("can't wait");
			}
		}
		return getFolderPath();
	}

	public boolean isActionPerformed() {
		return actionPerformed;
	}

	public void setActionPerformed(boolean actionPerformed) {
		this.actionPerformed = actionPerformed;
	}
	
	public boolean isConfirmed() {
		return isConfirmed;
	}

	public String getFilePath() {
		return filePath;
	}
	
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public String getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}
}
