package au.gov.ansto.bragg.quokka.msw.util;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.gumtree.msw.INotificationLock;
import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.ElementList;
import org.gumtree.msw.elements.IDependencyProperty;

import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.CSVPrinter;

public class CsvTable {
	// finals
	private static final Map<Class<?>, ISerializer> serializers;
	
	// construction
	static {
		serializers = new HashMap<Class<?>, ISerializer>();
		serializers.put(String.class, new StringSerializer());
		serializers.put(Boolean.class, new BooleanSerializer());
		serializers.put(Float.class, new FloatSerializer());
		serializers.put(Double.class, new DoubleSerializer());
		serializers.put(Byte.class, new ByteSerializer());
		serializers.put(Short.class, new ShortSerializer());
		serializers.put(Integer.class, new IntegerSerializer());
		serializers.put(Long.class, new LongSerializer());
		serializers.put(BigInteger.class, new BigIntegerSerializer());
	}
	
	// methods
	private static boolean validateProperties(IDependencyProperty[] properties) {
		for (IDependencyProperty property : properties)
			if (!serializers.containsKey(property.getPropertyType()))
				return false;
		
		return true;
	}
	// export
	public static <TElement extends Element>
	void showExportDialog(Shell shell, ElementList<TElement> elementList, IDependencyProperty ... properties) {
		if (!validateProperties(properties)) {
			MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			dialog.setText("Warning");
			dialog.setMessage("Not all properties are supported");
			dialog.open();
		}
		
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
		// prepare lookup
		PropertySerializer[] lookup = new PropertySerializer[properties.length];
		for (int i = 0; i != properties.length; i++) {
			IDependencyProperty property = properties[i];
			ISerializer serializer = serializers.get(property.getPropertyType());
			
			if (serializer != null)
				lookup[i] = new PropertySerializer(property, serializer);
		}
		
		try (INotificationLock lock = elementList.getModelProxy().suspendNotifications()) {
			CSVPrinter printer = new CSVPrinter(writer);
			printer.setAutoFlush(false);
			
			List<TElement> elements = new ArrayList<>();
			elementList.fetchElements(elements);
			Collections.sort(elements, Element.INDEX_COMPARATOR);

			// header
			for (PropertySerializer l : lookup)
				if (l != null)
					printer.write(l.property.getName());
			printer.writeln();
			
			// content
			for (TElement element : elements) {
				for (PropertySerializer l : lookup)
					if (l != null)
						printer.write(l.serializer.serialize(element.get(l.property)));
				printer.writeln();
			}

			printer.flush();
		}
	}
	// import
	public static <TElement extends Element>
	List<Map<IDependencyProperty, Object>> showImportDialog(Shell shell, IDependencyProperty ... properties) {
		if (!validateProperties(properties)) {
			MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			dialog.setText("Warning");
			dialog.setMessage("Not all properties are supported.");
			dialog.open();
			return null;
		}
		
		FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
		
		fileDialog.setFilterNames(new String[] { "Comma-Separated Values (*.csv)", "All Files (*.*)" });
		fileDialog.setFilterExtensions(new String[] { "*.csv", "*.*" });
		
		String filename = fileDialog.open();
		if ((filename != null) && (filename.length() > 0))
			try (FileReader reader = new FileReader(filename)) {
				return importFrom(reader, properties);
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
	List<Map<IDependencyProperty, Object>> importFrom(FileReader reader, IDependencyProperty ... properties) throws IOException {
		CSVParser parser = new CSVParser(reader);

		// header
		List<PropertySerializer> lookup = new ArrayList<>();
		
		String[] headerLine = parser.getLine();
		if (headerLine == null)
			throw new IOException();
		
		Set<IDependencyProperty> propertySet = new HashSet<>(Arrays.asList(properties));
		for (String cell : headerLine) {
			PropertySerializer l = null;
			for (IDependencyProperty property : propertySet)
				if (property.getName().equalsIgnoreCase(cell)) {
					ISerializer serializer = serializers.get(property.getPropertyType());
					if (serializer != null)
						l = new PropertySerializer(property, serializer);

					propertySet.remove(property);
					break;
				}
			
			lookup.add(l);
		}

		// content
		List<Map<IDependencyProperty, Object>> content = new ArrayList<>();
		for (String[] line = parser.getLine(); line != null; line = parser.getLine()) {
			if (line.length != lookup.size())
				throw new IOException();

			Map<IDependencyProperty, Object> values = new HashMap<>();
			for (int i = 0; i != line.length; i++) {
				PropertySerializer l = lookup.get(i);
				if (l != null)
					values.put(l.property, l.serializer.deserialize(line[i]));
			}
			
			content.add(values);
		}
		
		return content;
	}

	// Serialization
	private static interface ISerializer {
		public String serialize(Object value);
		public Object deserialize(String value) throws IllegalArgumentException;
	}
	
	// key value pair
	private static class PropertySerializer {
		// fields
		public final IDependencyProperty property;
		public final ISerializer serializer;
		
		// construction
		public PropertySerializer(IDependencyProperty property, ISerializer serializer) {
			this.property = property;
			this.serializer = serializer;
		}
	}
	
	// implementation
	private static class StringSerializer implements ISerializer {
		@Override
		public String serialize(Object value) {
			return (String)value;
		}
		@Override
		public String deserialize(String value) {
			return value;
		}
	}
	private static class BooleanSerializer implements ISerializer {
		@Override
		public String serialize(Object value) {
			return (boolean)value ? "true" : "false";
		}
		@Override
		public Boolean deserialize(String value) throws IllegalArgumentException {
			if ("true".equalsIgnoreCase(value))
				return Boolean.TRUE;
			if ("false".equalsIgnoreCase(value))
				return Boolean.FALSE;

			throw new IllegalArgumentException();
		}
	}
	// numeric
	public static abstract class NumericSerializer implements ISerializer {
		@Override
		public String serialize(Object value) {
			if (value != null)
				return value.toString();
			else
				return null;
		}
	}
	private static class FloatSerializer extends NumericSerializer {
		@Override
		public Float deserialize(String value) throws IllegalArgumentException {
			return Float.parseFloat(value);
		}
	}
	private static class DoubleSerializer extends NumericSerializer {
		@Override
		public Double deserialize(String value) throws IllegalArgumentException {
			return Double.parseDouble(value);
		}
	}
	private static class ByteSerializer extends NumericSerializer {
		@Override
		public Byte deserialize(String value) throws IllegalArgumentException {
			return Byte.parseByte(value);
		}
	}
	private static class ShortSerializer extends NumericSerializer {
		@Override
		public Short deserialize(String value) throws IllegalArgumentException {
			return Short.parseShort(value);
		}
	}
	private static class IntegerSerializer extends NumericSerializer {
		@Override
		public Integer deserialize(String value) throws IllegalArgumentException {
			return Integer.parseInt(value);
		}
	}
	private static class LongSerializer extends NumericSerializer {
		@Override
		public Long deserialize(String value) throws IllegalArgumentException {
			return Long.parseLong(value);
		}
	}
	private static class BigIntegerSerializer extends NumericSerializer {
		@Override
		public BigInteger deserialize(String value) throws IllegalArgumentException {
			return new BigInteger(value);
		}
	}
}
