package au.gov.ansto.bragg.quokka.msw;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.gumtree.msw.ICommand;
import org.gumtree.msw.RefId;
import org.gumtree.msw.commands.AddListElementCommand;
import org.gumtree.msw.commands.BatchCommand;
import org.gumtree.msw.commands.ChangePropertyCommand;
import org.gumtree.msw.commands.ClearElementListCommand;
import org.gumtree.msw.commands.Command;
import org.gumtree.msw.elements.DependencyProperty;
import org.gumtree.msw.elements.ElementList;
import org.gumtree.msw.elements.ElementPath;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IListElementFactory;
import org.gumtree.msw.model.ModelImporter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

// ConfigurationList can only be created by server but can be moved (via index) by client 
public class ConfigurationList extends ElementList<Configuration> {
	// property names
	public static final DependencyProperty<ConfigurationList, String> DESCRIPTION = new DependencyProperty<>("Description", String.class);
	// property set
	public static final Set<IDependencyProperty> PROPERTIES = DependencyProperty.createSet(
			INDEX, DESCRIPTION);

	// fields
	private final IListElementFactory<Configuration> elementFactory = new IListElementFactory<Configuration>() {
		private final String elementPrefix = Configuration.class.getSimpleName() + '#';
		@Override
		public Configuration create(String elementName) {
			if (elementName.startsWith(elementPrefix))
				return new Configuration(ConfigurationList.this, elementName);

			return null;
		}
	};

	// construction
	ConfigurationList(LoopHierarchy parent) {
		super(parent, ConfigurationList.class.getSimpleName());
	}

	// properties
	@Override
	public Set<IDependencyProperty> getProperties() {
		return PROPERTIES;
	}
	public String getDescription() {
		return (String)get(DESCRIPTION);
	}
	public void setDescription(String value) {
		set(DESCRIPTION, value);
	}

	// methods
	@Override
	public IListElementFactory<Configuration> getElementFactory() {
		return elementFactory;
	}
	@Override
	public void clear() {
		super.clear();
	}
	public void addConfiguration() {
		addConfiguration(Integer.MAX_VALUE);
	}
	public void addConfiguration(int index) {
		RefId id = nextId();
		
		String name = Configuration.class.getSimpleName() + nextId().toString();
		ElementPath path = new ElementPath(getPath(), name);
		
		command(new BatchCommand(
				id,
				new AddListElementCommand(
						id,
						getPath(),
						name,
						index),
				new AddListElementCommand(
						id,
						path,
						Measurement.TRANSMISSION + nextId().toString()),
				new AddListElementCommand(
						id,
						path,
						Measurement.SCATTERING + nextId().toString())));
	}
	public void addConfigurations(Iterable<IFile> configurations) {
		RefId id = nextId();
		ElementPath path = getPath();

		List<Command> commands = new ArrayList<>();
		
		DocumentBuilder documentBuilder;
		try {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			
			builderFactory.setNamespaceAware(true);
			documentBuilder = builderFactory.newDocumentBuilder();
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();
			return;
		}
		
		for (IFile file : configurations) {
			String name = null;
			String description = null;
			String initScript = null;
			String preTransmissionScript = null;
			String preScatteringScript = null;
			String startingAttenuation = null;
			try {
				Document document = documentBuilder.parse(file.getContents());
				
				//get the root element
				Element root = document.getDocumentElement();
				if (!"au.gov.ansto.bragg.quokka.experiment.model.InstrumentConfigTemplate".equals(root.getNodeName()) &&
					!"au.gov.ansto.bragg.quokka2.experiment.model.InstrumentConfigTemplate".equals(root.getNodeName()))
					continue;

				Node node;
				// name (use filename to make it consistent with configuration catalog)
				if ((name == null) || (name.length() == 0)) {
					name = file.getName();
					if (name.endsWith(".xml"))
						name = name.substring(0, name.length() - ".xml".length());
				}
				// description
				node = getFirstOrNull(root.getElementsByTagName("description"));
				if (node != null)
					description = node.getTextContent();
				// initScript
				node = getFirstOrNull(root.getElementsByTagName("initScript"));
				if (node != null)
					initScript = node.getTextContent();
				// preTransmissionScript
				node = getFirstOrNull(root.getElementsByTagName("preTransmissionScript"));
				if (node != null)
					preTransmissionScript = node.getTextContent();
				// preScatteringScript
				node = getFirstOrNull(root.getElementsByTagName("preScatteringScript"));
				if (node != null)
					preScatteringScript = node.getTextContent();
				// startingAttenuation
				node = getFirstOrNull(root.getElementsByTagName("startingAttenuation"));
				if (node != null)
					startingAttenuation = node.getTextContent();
				else {
					node = getFirstOrNull(root.getElementsByTagName("startingAtteunation"));
					if (node != null)
						startingAttenuation = node.getTextContent();
				}
			}
			catch (SAXException | IOException | CoreException | NumberFormatException e) {
				e.printStackTrace();
				continue;
			}
			
			String elementName = Configuration.class.getSimpleName() + nextId().toString();
			ElementPath elementPath = new ElementPath(path, elementName);
			
			commands.add(new AddListElementCommand(
					id,
					path,
					elementName));
			
			if (name != null)
				commands.add(new ChangePropertyCommand(
						id,
						elementPath,
						Configuration.NAME.getName(),
						name));
			
			if (description != null)
				commands.add(new ChangePropertyCommand(
						id,
						elementPath,
						Configuration.DESCRIPTION.getName(),
						description));
			
			if (initScript != null)
				commands.add(new ChangePropertyCommand(
						id,
						elementPath,
						Configuration.SETUP_SCRIPT.getName(),
						initScript));

			String transmissionName = Measurement.TRANSMISSION + nextId().toString();
			ElementPath transmissionPath = new ElementPath(elementPath, transmissionName);
			
			commands.add(new AddListElementCommand(
					id,
					elementPath,
					transmissionName));
			
			if (preTransmissionScript != null)
				commands.add(new ChangePropertyCommand(
						id,
						transmissionPath,
						Measurement.SETUP_SCRIPT.getName(),
						preTransmissionScript));

			String scatteringName = Measurement.SCATTERING + nextId().toString();
			ElementPath scatteringPath = new ElementPath(elementPath, scatteringName);
			
			commands.add(new AddListElementCommand(
					id,
					elementPath,
					scatteringName));
			
			if (preScatteringScript != null)
				commands.add(new ChangePropertyCommand(
						id,
						scatteringPath,
						Measurement.SETUP_SCRIPT.getName(),
						preScatteringScript));
			
			if (startingAttenuation != null)
				commands.add(new ChangePropertyCommand(
						id,
						scatteringPath,
						Measurement.ATTENUATION_ANGLE.getName(),
						startingAttenuation + '°'));
		}
		
		if (commands.isEmpty())
			return;
		
		command(new BatchCommand(id, commands.toArray(new Command[commands.size()])));
	}
	private Node getFirstOrNull(NodeList list) {
		if (list.getLength() == 0)
			return null;

		return list.item(0);
	}
	public boolean saveTo(OutputStream stream) {
		return getModelProxy().serializeTo(getPath(), stream);
	}
	public boolean replaceConfigurations(InputStream stream) {
		ICommand loadCommand = ModelImporter.buildLoadCommand(
				getModelProxy(),
				getPath(),
				ConfigurationList.class.getSimpleName(),
				true,		// isElementList
				stream);
		
		if (loadCommand == null)
			return false;

		RefId id = nextId();
		command(new BatchCommand(
				id,
				new ClearElementListCommand(id, getPath()),
				loadCommand));
		
		return true;
	}
	public boolean importConfiguration(InputStream stream) {
		// TODO Auto-generated method stub
		return false;
	}
	public void enableAll() {
		batchSet(Configuration.ENABLED, true);
	}
	public void disableAll() {
		batchSet(Configuration.ENABLED, false);
	}
}
