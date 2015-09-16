package au.gov.ansto.bragg.quokka.msw.util;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.gumtree.msw.INotificationLock;
import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.ElementList;
import org.gumtree.msw.elements.IDependencyProperty;

import com.Ostermiller.util.CSVParser;

public class CsvTableImporter {
	// methods
	public static <TElement extends Element>
	List<Map<IDependencyProperty, String>> showDialog(Shell shell, ElementList<TElement> elementList, IDependencyProperty ... properties) {
		FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
		
		fileDialog.setFilterNames(new String[] { "Comma-Separated Values (*.csv)", "All Files (*.*)" });
		fileDialog.setFilterExtensions(new String[] { "*.csv", "*.*" });
		
		String filename = fileDialog.open();
		if ((filename != null) && (filename.length() > 0))
			try (FileReader reader = new FileReader(filename)) {
				return importFrom(reader, elementList, properties);
			}
			catch (Exception e) {
				MessageBox dialog = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
				dialog.setText("Warning");
				dialog.setMessage("Unable to import table from selected csv file.");
				dialog.open();
			}
		
		return null;
	}
	public static <TElement extends Element>
	List<Map<IDependencyProperty, String>> importFrom(FileReader reader, ElementList<TElement> elementList, IDependencyProperty ... properties) throws IOException {
		try (INotificationLock lock = elementList.getModelProxy().suspendNotifications()) {
			CSVParser parser = new CSVParser(reader);

			// header
			List<IDependencyProperty> propertyOrder = new ArrayList<>();
			String[] headerLine = parser.getLine();
			if (headerLine == null)
				throw new IOException();
			
			for (String cell : headerLine) {
				IDependencyProperty property = null;
				for (IDependencyProperty p : properties)
					if (p.getName().equalsIgnoreCase(cell)) {
						property = p;
						break;
					}
				
				if ((property != null) && propertyOrder.contains(property))
					property = null; // ignore repeated columns
					
				propertyOrder.add(property);
			}

			// content
			List<Map<IDependencyProperty, String>> content = new ArrayList<>();
			for (String[] line = parser.getLine(); line != null; line = parser.getLine()) {
				if (line.length != propertyOrder.size())
					throw new IOException();

				Map<IDependencyProperty, String> values = new HashMap<>();
				for (int i = 0; i != line.length; i++) {
					IDependencyProperty property = propertyOrder.get(i);
					if (property != null)
						values.put(property, line[i]);
				}
				
				content.add(values);
			}
			
			return content;
		}
	}
}
