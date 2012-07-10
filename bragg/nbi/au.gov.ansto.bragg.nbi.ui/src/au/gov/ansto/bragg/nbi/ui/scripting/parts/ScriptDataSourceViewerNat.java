/**
 * 
 */
package au.gov.ansto.bragg.nbi.ui.scripting.parts;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.edit.editor.CheckBoxCellEditor;
import net.sourceforge.nattable.grid.data.DefaultCornerDataProvider;
import net.sourceforge.nattable.grid.layer.DefaultGridLayer;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.gumtree.data.interfaces.IDataset;

import au.gov.ansto.bragg.nbi.ui.internal.InternalImage;

/**
 * @author nxi
 *
 */
public class ScriptDataSourceViewerNat extends Composite {

	private final static String[] COLUMN_TITLES = new String[]{"Sample name=sample_name", 
								"Title=title"};

	private CoolBar coolbar;
	private CoolItem coolItem;
	private ToolBar controlToolBar;
	private ToolItem openFileToolItem;
	private ToolItem openFolderToolItem;

	/**
	 * @param parent
	 * @param style
	 */
	public ScriptDataSourceViewerNat(Composite parent, int style) {
		super(parent, style);
		setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		GridLayoutFactory.fillDefaults().spacing(0, 0).margins(0, 0).applyTo(this);
		createToolboxComposite(this);
		createFileTableComposite(this);
	}

	private void createToolboxComposite(Composite parent) {
		coolbar = new CoolBar(parent, SWT.BORDER | SWT.FLAT);
		coolbar.setBackgroundMode(SWT.INHERIT_DEFAULT);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(coolbar);
		
		coolItem = new CoolItem(coolbar, SWT.DROP_DOWN);
		controlToolBar = new ToolBar(coolbar, SWT.FLAT);
		coolItem.setControl(controlToolBar);
		
		openFileToolItem = new ToolItem(controlToolBar, SWT.PUSH);
		openFileToolItem.setToolTipText("Open file");
		openFileToolItem.setImage(InternalImage.ADD_ITEM.getImage());
		
		openFolderToolItem = new ToolItem(controlToolBar, SWT.PUSH);
		openFolderToolItem.setToolTipText("Open folder");
		openFolderToolItem.setImage(InternalImage.ADD_DIR.getImage());
		
		
	    resizeCoolBar();
	}

	private void createFileTableComposite(
			Composite parent) {
		Composite fileTableComposite = new Composite(parent, SWT.BORDER);
		fileTableComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
		GridLayoutFactory.fillDefaults().applyTo(fileTableComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(fileTableComposite);
		NatTable natTable = new NatTable(fileTableComposite, new FileSourceLayout(
				new BodyDataProvider(null), new RowHeaderDataProvider(fileTableComposite)));
		GridLayoutFactory.fillDefaults().applyTo(natTable);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
		natTable.setBackgroundMode(SWT.INHERIT_DEFAULT);
	}

	private void resizeCoolBar() {
	    Point toolBar1Size = controlToolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
	    Point coolBar1Size = coolItem.computeSize(toolBar1Size.x,
	        toolBar1Size.y);
	    coolItem.setSize(coolBar1Size);
	}
	
	private class FileSourceLayout extends DefaultGridLayer {

		public FileSourceLayout(IDataProvider bodyDataProvider, IDataProvider rowHeaderDataProvider) {
			super(true);
			IDataProvider columnHeaderDataProvider = new ColumnHeaderDataProvider();
			IDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
			
			init(bodyDataProvider, columnHeaderDataProvider, rowHeaderDataProvider, cornerDataProvider);
		}
		
		private class ColumnHeaderDataProvider implements IDataProvider {

			
			public ColumnHeaderDataProvider() {
			}
			
			public Object getDataValue(int columnIndex, int rowIndex) {
				if (columnIndex == 0) {
					return "ID";
				} else {
					return COLUMN_TITLES[columnIndex - 1].split("=")[0];
				}
			}

			public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
				throw new UnsupportedOperationException();
			}

			public int getColumnCount() {
				return COLUMN_TITLES.length + 1;
			}

			public int getRowCount() {
				return 1;
			}
			
		}
		
	}
	
	private class RowHeaderDataProvider implements IDataProvider {

		private Composite parent;
		public RowHeaderDataProvider(Composite parent) {
			this.parent = parent;
		}
		
		public Object getDataValue(int columnIndex, int rowIndex) {
			return new CheckBoxCellEditor();
		}

		public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
			throw new UnsupportedOperationException();
		}

		public int getColumnCount() {
			return 1;
		}

		public int getRowCount() {
			return 1;
		}
		
	}

	private class BodyDataProvider implements IDataProvider {
		
		private IDataset dataset;
		
		public BodyDataProvider(IDataset dataset) {
			this.dataset = dataset;
		}
		
		public Object getDataValue(int columnIndex, int rowIndex) {
			if (columnIndex == 0) {
				return new CheckBoxCellEditor();
			} else {
				return COLUMN_TITLES[columnIndex - 1].split("=")[0];
			}
		}

		public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
			throw new UnsupportedOperationException();
		}

		public int getColumnCount() {
			return COLUMN_TITLES.length + 1;
		}

		public int getRowCount() {
			return 2;
		}
	}
}
