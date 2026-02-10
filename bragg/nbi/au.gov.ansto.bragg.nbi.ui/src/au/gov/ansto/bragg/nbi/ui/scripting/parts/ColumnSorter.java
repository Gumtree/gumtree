/**
 * 
 */
package au.gov.ansto.bragg.nbi.ui.scripting.parts;

import java.util.Comparator;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

/**
 * @author nxi
 *
 */
public class ColumnSorter extends ViewerComparator {

	public final static int SORT_ASCENDING = 0;
	public final static int SORT_DESCENDING = 1;
	private int columnIndex = 0;
	private int sortDirection = SORT_ASCENDING;
	
	public ColumnSorter() {
	}

	public ColumnSorter(Comparator<? super String> comparator) {
		super(comparator);
	}
	
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (viewer instanceof TableViewer) {
			TableViewer tableViewer = (TableViewer) viewer;
			ColumnLabelProvider labelProvider = (ColumnLabelProvider) tableViewer.getLabelProvider(columnIndex);
			int result = getComparator().compare(labelProvider.getText(e1), labelProvider.getText(e2));
			if (sortDirection == 1) {
				return -result;
			}
			return result;
		}
		return 0;
	}
	
	public int getColumnIndex() {
		return columnIndex;
	}

	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}

	public int getSortDirection() {
		return sortDirection;
	}

	public void setSortDirection(int sortDirection) {
		this.sortDirection = sortDirection;
	}
	
	
}
