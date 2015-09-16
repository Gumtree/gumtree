package au.gov.ansto.bragg.quokka.msw.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.gumtree.msw.INotificationLock;
import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.ElementList;
import org.gumtree.msw.elements.IDependencyProperty;

import com.Ostermiller.util.CSVPrinter;

public class CsvTableExporter {
	// methods
	public static <TElement extends Element>
	void showDialog(Shell shell, ElementList<TElement> elementList, IDependencyProperty ... properties) {
		FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
		
		fileDialog.setFilterNames(new String[] { "Comma-Separated Values (*.csv)", "All Files (*.*)" });
		fileDialog.setFilterExtensions(new String[] { "*.csv", "*.*" });
		
		String filename = fileDialog.open();
		if ((filename != null) && (filename.length() > 0))
			try (FileWriter writer = new FileWriter(filename)) {
				exportTo(writer, elementList, properties);
			}
			catch (Exception e) {
				MessageBox dialog = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
				dialog.setText("Warning");
				dialog.setMessage("Unable to export table to selected csv file.");
				dialog.open();
			}
	}
	public static <TElement extends Element>
	void exportTo(Writer writer, ElementList<TElement> elementList, IDependencyProperty ... properties) throws IOException {
		try (INotificationLock lock = elementList.getModelProxy().suspendNotifications()) {
			CSVPrinter printer = new CSVPrinter(writer);
			printer.setAutoFlush(false);
			
			List<TElement> elements = new ArrayList<>();
			elementList.fetchElements(elements);
			Collections.sort(elements, Element.INDEX_COMPARATOR);

			// header
			for (IDependencyProperty property : properties)
				printer.write(property.getName());
			printer.writeln();
			
			// content
			for (TElement element : elements) {
				for (IDependencyProperty property : properties)
					printer.write(String.valueOf(element.get(property)));
				printer.writeln();
			}

			printer.flush();
		}
	}
}
