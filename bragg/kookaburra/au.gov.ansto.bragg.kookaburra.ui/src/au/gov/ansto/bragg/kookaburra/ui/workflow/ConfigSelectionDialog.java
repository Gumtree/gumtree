package au.gov.ansto.bragg.kookaburra.ui.workflow;

import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

import au.gov.ansto.bragg.kookaburra.experiment.model.InstrumentConfigTemplate;
import au.gov.ansto.bragg.kookaburra.experiment.util.ExperimentModelUtils;
import au.gov.ansto.bragg.kookaburra.ui.internal.InternalImage;

public class ConfigSelectionDialog extends MessageDialog {

	private IFolder baseDirectory;
	
	private boolean isNewConfig = false;
	
	private IFile selectedConfig;
	
	private TreeViewer treeViewer;
	
	public ConfigSelectionDialog(Shell parentShell) {
		super(parentShell, "Select instrument configuration", null,
				"Select a new instrument configuration: ", NONE, new String[] {
						IDialogConstants.OK_LABEL,
						IDialogConstants.CANCEL_LABEL }, 0);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/*************************************************************************
	 * Attributes
	 *************************************************************************/
	
	public IFolder getBaseDirectory() {
		return baseDirectory;
	}
	
	public void setBaseDirectory(IFolder baseDirectory) {
		this.baseDirectory = baseDirectory;
	}
	
	public boolean isNewConfig() {
		return isNewConfig;
	}
	
	public IFile getSelectedConfig() {
		return selectedConfig;
	}
	
	/*************************************************************************
	 * UI
	 *************************************************************************/
	
	protected Control createCustomArea(Composite parent) {
		Composite mainArea = new Composite(parent, SWT.NONE);
		mainArea.setLayout(new GridLayout(3, false));
		
		
		Button templateButton = new Button(mainArea, SWT.RADIO);
		templateButton.setText("From an existing configuration");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(templateButton);
		templateButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				treeViewer.getTree().setEnabled(true);
				isNewConfig = false;
			}
		});

		
		Label label = new Label(mainArea, SWT.NONE);
		label.setText("                  ");
		
		Button newButton = new Button(mainArea, SWT.RADIO);
		newButton.setText("New an empty configuration");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(newButton);
		newButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				treeViewer.getTree().setEnabled(false);
				isNewConfig = true;
			}
		});
		
		treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.VIRTUAL | SWT.SINGLE | SWT.FULL_SELECTION | SWT.V_SCROLL);
		Tree tree = treeViewer.getTree();
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).hint(800, 400).span(3, 1).applyTo(tree);
		tree.setHeaderVisible(true);
		treeViewer.setContentProvider(new ConfigContentProvider());
		treeViewer.setUseHashlookup(true);
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object selection = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (selection instanceof IFile) {
					selectedConfig = (IFile) selection;
				} else {
					selectedConfig = null;
				}
			}
		});
		
		TreeViewerColumn nameColumn = new TreeViewerColumn(treeViewer, SWT.LEFT);
		nameColumn.getColumn().setText("Name");
		nameColumn.getColumn().setWidth(350);
		nameColumn.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				return ((IResource) element).getName();
			}
			public Image getImage(Object element) {
				if (element instanceof IFile) {
					return InternalImage.FILE.getImage();
				} else if (element instanceof IFolder) {
					return InternalImage.FOLDER.getImage();
				}
				return null;
			}
		});
		
		TreeViewerColumn dateColumn = new TreeViewerColumn(treeViewer, SWT.LEFT);
		dateColumn.getColumn().setText("Date");	
		dateColumn.getColumn().setWidth(80);
		dateColumn.setLabelProvider(new ColumnLabelProvider() {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			public String getText(Object element) {
				if (element instanceof IFile) {
					return format.format(new Date(((IFile) element).getLocalTimeStamp()));	
				}
				return "";
			}
		});
		
		TreeViewerColumn noteColumn = new TreeViewerColumn(treeViewer, SWT.LEFT);
		noteColumn.getColumn().setText("Note");
		noteColumn.getColumn().setWidth(350);
		noteColumn.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				if (element instanceof IFile) {
					try {
						URI uri = ((IFile) element).getLocationURI();
						FileReader reader = new FileReader(new File(uri));
						Object object = ExperimentModelUtils.getXStream().fromXML(reader);
						if (object instanceof InstrumentConfigTemplate) {
							return ((InstrumentConfigTemplate) object).getDescription();
						}
					} catch (Exception e) {
					}
				}
				return "";
			}
		});
		
		treeViewer.setInput(getBaseDirectory());
		
		
		return mainArea;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(1, false));

		ConfigSelectionDialog dialog = new ConfigSelectionDialog(shell);
		dialog.open();

		while (dialog.getShell() != null && !dialog.getShell().isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	class ConfigContentProvider implements ILazyTreeContentProvider {
		
		private TreeViewer viewer;
		
		public ConfigContentProvider() {
		}
		
		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (viewer instanceof TreeViewer) {
				this.viewer = (TreeViewer) viewer;
			}
		}

		@Override
		public void updateElement(Object parent, int index) {
			// For efficiency we only update once
			if (parent instanceof IFolder && index <= 0) {
				IFolder folder = (IFolder) parent;
				int i = 0;
				for (IResource resource : getMembers(folder)) {
					viewer.replace(folder, i, resource);
					updateChildCount(resource, 0);
					i++;
				}
			}
		}

		@Override
		public void updateChildCount(Object element, int currentChildCount) {
			if (element instanceof IFolder) {
				IFolder folder = (IFolder) element;
				try {
					viewer.setChildCount(folder, folder.members().length);
				} catch (CoreException e) {
				}
			}
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}
		
		// Sort resource members with folders first
		private List<IResource> getMembers(IFolder folder) {
			TreeSet<IResource> folders = new TreeSet<IResource>(new Comparator<IResource>() {
				@Override
				public int compare(IResource o1, IResource o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			TreeSet<IResource> files = new TreeSet<IResource>(new Comparator<IResource>() {
				@Override
				public int compare(IResource o1, IResource o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			try {
				for (IResource member : folder.members()) {
					if (member instanceof IFolder) {
						folders.add(member);
					} else if (member instanceof IFile) {
						files.add(member);
					}
				}
			} catch (CoreException e) {
			}
			List<IResource> resources = new ArrayList<IResource>();
			resources.addAll(folders);
			resources.addAll(files);
			return resources;
		}
		
	}
	
}
