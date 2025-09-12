/**
 * 
 */
package au.gov.ansto.bragg.nbi.ui.scripting.parts;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IResource;
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
	private String[] filenames;
	private Shell shell;
	private FileDialog singleLoadDialog;
	private FileDialog multiLoadDialog;
	private FileDialog saveFileDialog;
	private DirectoryDialog saveFolderDialog;

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
		return selectSaveFile(extNames, null, null);
	}

	private synchronized DirectoryDialog getSaveFolderDialog() {
		if (saveFolderDialog == null) {
			saveFolderDialog = new DirectoryDialog(shell, SWT.MULTI);
		}
		return saveFolderDialog;
	}
	
	public String selectSaveFolder(){
		setActionPerformed(false);
		shell.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				DirectoryDialog dialog = getSaveFolderDialog();
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

	public String selectLoadFile(final List<String> extNames) {
		return selectLoadFile(extNames, null);
	}
	
	private synchronized FileDialog getSingleLoadDialog() {
		if (singleLoadDialog == null) {
			singleLoadDialog = new FileDialog(shell, SWT.SINGLE);
		}
		return singleLoadDialog;
	}

	public String selectLoadFile(final List<String> extNames, final String workspacePath) {
		setActionPerformed(false);
		shell.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				FileDialog dialog = getSingleLoadDialog();
				IWorkspace workspace= ResourcesPlugin.getWorkspace();
				IWorkspaceRoot root = workspace.getRoot();
				IResource resource = null;
				if (workspacePath != null) {
					resource = root.findMember(workspacePath);					
				}
				if (resource != null) {
					dialog.setFilterPath(resource.getLocation().toOSString());
				} else {
					if (fileDialogPath == null){
	 					dialog.setFilterPath(root.getLocation().toOSString());						
					} else {
						dialog.setFilterPath(fileDialogPath);
					}
				}
 				if (extNames != null && extNames.size() > 0) {
 					String[] extArray = new String[extNames.size()];
 					dialog.setFilterExtensions(extNames.toArray(extArray));
 				}
 				String filePath = dialog.open();
 				setFilePath(filePath);
 				if (workspacePath == null){
 	 				fileDialogPath = filePath; 					
 				}
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
	
	public String[] selectLoadMultiFile(final List<String> extNames) {
		return selectLoadMultiFile(extNames, null);
	}
	
	private synchronized FileDialog getMultiLoadDialog() {
		if (multiLoadDialog == null) {
			multiLoadDialog = new FileDialog(shell, SWT.MULTI);
		}
		return multiLoadDialog;
	}

	public String[] selectLoadMultiFile(final List<String> extNames, final String workspacePath) {
		setActionPerformed(false);
		shell.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				FileDialog dialog = getMultiLoadDialog();
				IWorkspace workspace= ResourcesPlugin.getWorkspace();
				IWorkspaceRoot root = workspace.getRoot();
				IResource resource = null;
				if (workspacePath != null) {
					resource = root.findMember(workspacePath);					
				}
				if (resource != null) {
					dialog.setFilterPath(resource.getLocation().toOSString());
				} else {
					if (fileDialogPath == null){
	 					dialog.setFilterPath(root.getLocation().toOSString());						
					} else {
						dialog.setFilterPath(fileDialogPath);
					}
				}
 				if (extNames != null && extNames.size() > 0) {
 					String[] extArray = new String[extNames.size()];
 					dialog.setFilterExtensions(extNames.toArray(extArray));
 				}
 				String filePath = dialog.open();
 				String[] filenames = dialog.getFileNames();
 				setFilenames(filenames);
 				if (workspacePath == null){
 	 				fileDialogPath = filePath; 					
 				}
 				for (int i = 0; i < filenames.length; i++) {
					filenames[i] = dialog.getFilterPath() + File.separator + filenames[i];
				}
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
		return getFilenames();
	}
	
	public List<String> selectLoadMultiFileList(final List<String> extNames) {
		return Arrays.asList(selectLoadMultiFile(extNames, null));
	}

	public List<String> selectLoadMultiFileList(final List<String> extNames, final String workspacePath) {
		return Arrays.asList(selectLoadMultiFile(extNames, workspacePath));
	}

	private synchronized FileDialog getSaveFileDialog() {
		if (saveFileDialog == null) {
			saveFileDialog = new FileDialog(shell, SWT.SAVE);
		}
		return saveFileDialog;
	}
	
	public String selectSaveFile(final List<String> extNames, final String workspacePath, final String filename) {
		setActionPerformed(false);
		shell.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				FileDialog dialog = getSaveFileDialog();
				IWorkspace workspace= ResourcesPlugin.getWorkspace();
				IWorkspaceRoot root = workspace.getRoot();
				IResource resource = null;
				if (workspacePath != null) {
					resource = root.findMember(workspacePath);					
				}
				if (resource != null) {
					dialog.setFilterPath(resource.getLocation().toOSString());
				} else {
					if (fileDialogPath == null){
	 					dialog.setFilterPath(root.getLocation().toOSString());						
					} else {
						dialog.setFilterPath(fileDialogPath);
					}
				}
 				if (extNames != null && extNames.size() > 0) {
 					String[] extArray = new String[extNames.size()];
 					dialog.setFilterExtensions(extNames.toArray(extArray));
 				}
 				if (filename != null) {
 					dialog.setFileName(filename);
 				}
 				String filePath = dialog.open();
 				setFilePath(filePath);
 				if (workspacePath == null){
 	 				fileDialogPath = filePath; 					
 				}
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
	
	public String[] getFilenames() {
		return filenames;
	}
	
	public void setFilenames(String[] filenames) {
		this.filenames = filenames;
	}
	
	public String getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}
}
