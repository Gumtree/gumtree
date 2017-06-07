package au.gov.ansto.bragg.quokka.msw;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.gumtree.msw.ICommand;
import org.gumtree.msw.RefId;
import org.gumtree.msw.commands.AddListElementCommand;
import org.gumtree.msw.commands.BatchCommand;
import org.gumtree.msw.commands.ChangePropertyCommand;
import org.gumtree.msw.elements.DependencyProperty;
import org.gumtree.msw.elements.ElementList;
import org.gumtree.msw.elements.ElementPath;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IListElementFactory;
import org.gumtree.msw.model.DataSource;
import org.gumtree.msw.model.ModelImporter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import au.gov.ansto.bragg.quokka.msw.converters.GroupTrimConverter;

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
	public void addConfigurations(Path rootPath, Iterable<Path> configurations) {
		RefId id = nextId();
		ElementPath path = getPath();

		List<ICommand> commands = new ArrayList<>();
		
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
		
		for (Path configuration : configurations) {
			File file = rootPath.resolve(configuration).toFile();
			if (!file.exists())
				continue;

			// determine name for new configuration (without file extension)
			final String XML = ".xml";
			
			String configurationName = configuration.getFileName().toString();
			if (configurationName.toLowerCase().endsWith(XML))
				configurationName = configurationName.substring(0, configurationName.length() - XML.length());

			String groupName = "";
			if (configuration.getParent() != null)
				groupName = GroupTrimConverter.DEFAULT.toModelValue(configuration.getParent().toString());

			if (file.length() == 0) {
				// if file is empty create new configuration
				String elementName = Configuration.class.getSimpleName() + nextId().toString();
				ElementPath elementPath = new ElementPath(path, elementName);
				
				BatchCommand loadCommand = new BatchCommand(
						id,
						new AddListElementCommand(
								id,
								path,
								elementName),
						new ChangePropertyCommand(
								id,
								elementPath,
								Configuration.NAME.getName(),
								configurationName),
						new AddListElementCommand(
								id,
								elementPath,
								Measurement.TRANSMISSION + nextId().toString()),
						new AddListElementCommand(
								id,
								elementPath,
								Measurement.SCATTERING + nextId().toString()));
				
				if (loadCommand != null)
					commands.add(loadCommand);

				continue;
			}

			String description = null;
			String initScript = null;
			String preTransmissionScript = null;
			String preScatteringScript = null;
			String startingAttenuation = null;
			try {
				Document document = documentBuilder.parse(file);
				
				//get the root element
				Element root = document.getDocumentElement();
				if (!"au.gov.ansto.bragg.quokka.experiment.model.InstrumentConfigTemplate".equals(root.getNodeName()) &&
					!"au.gov.ansto.bragg.quokka2.experiment.model.InstrumentConfigTemplate".equals(root.getNodeName())) {
					// it might be a new configuration file
					
					// set name for new configuration
					Map<String, Object> overrideProperties = new HashMap<>();
					overrideProperties.put(Configuration.NAME.getName(), configurationName);
					overrideProperties.put(Configuration.GROUP.getName(), groupName);
					
					// try to load file
					BatchCommand loadCommand = ModelImporter.buildLoadCommand(
							getModelProxy(),
							path,
							Configuration.class.getSimpleName(),
							false,		// isElementList (false because it's a single configuration)
							new DataSource(file),
							overrideProperties);
					
					if (loadCommand != null)
						commands.add(loadCommand);

					continue;
				}

				Node node;
				// description
				node = getFirstOrNull(root.getElementsByTagName("description"));
				if (node != null)
					description = node.getTextContent();
				// initScript
				node = getFirstOrNull(root.getElementsByTagName("initScript"));
				if (node != null)
					initScript = updateScript(node.getTextContent());
				// preTransmissionScript
				node = getFirstOrNull(root.getElementsByTagName("preTransmissionScript"));
				if (node != null)
					preTransmissionScript = updateScript(node.getTextContent());
				// preScatteringScript
				node = getFirstOrNull(root.getElementsByTagName("preScatteringScript"));
				if (node != null)
					preScatteringScript = updateScript(node.getTextContent());
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
			catch (SAXException | IOException | NumberFormatException e) {
				e.printStackTrace();
				continue;
			}
			
			String elementName = Configuration.class.getSimpleName() + nextId().toString();
			ElementPath elementPath = new ElementPath(path, elementName);
			
			commands.add(new AddListElementCommand(
					id,
					path,
					elementName));
		
			commands.add(new ChangePropertyCommand(
					id,
					elementPath,
					Configuration.NAME.getName(),
					configurationName));
		
			commands.add(new ChangePropertyCommand(
					id,
					elementPath,
					Configuration.GROUP.getName(),
					groupName));
			
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
						Integer.parseInt(startingAttenuation)));
		}
		
		if (commands.isEmpty())
			return;
		
		command(new BatchCommand(id, commands.toArray(new ICommand[commands.size()])));
	}
	private Node getFirstOrNull(NodeList list) {
		if (list.getLength() == 0)
			return null;

		return list.item(0);
	}
	public boolean importConfiguration(InputStream stream) {
		BatchCommand loadCommand = ModelImporter.buildLoadCommand(
				getModelProxy(),
				getPath(),
				ConfigurationList.class.getSimpleName(),
				true,		// isElementList
				new DataSource(stream));
		
		if (loadCommand == null)
			return false;
		
		command(loadCommand);
		return true;
	}
	public void enableAll() {
		batchSet(Configuration.ENABLED, true);
	}
	public void disableAll() {
		batchSet(Configuration.ENABLED, false);
	}
	
	// helpers
	private static String updateScript(String script) {
		final String[] guideConfigs = new String[] {
			    "ga", "mt", "lp", "lens",
			    "p1", "p1lp", "p1lens", "g1",
			    "p2", "g2", "p3", "g3", "p4", "g4", "p5", "g5",
			    "p6", "g6", "p7", "g7", "p8", "g8", "p9", "g9"
		};
		
		StringBuilder sb = new StringBuilder();
		for (String line : script.split("(\r\n)|(\n\r)|\n|\r")) {
			String trimmedLine = line.trim();
			// driveAtt(...)
			if (trimmedLine.startsWith("driveAtt(")) {
				line += " # !!! Warning: it is not recommended to manually drive the attenuator";
			}
			// driveGuide(...)
			else if (trimmedLine.startsWith("driveGuide(guideConfig.")) {
				for (String config : guideConfigs) {
					String old = "driveGuide(guideConfig." + config + ")";
					if (trimmedLine.startsWith(old)) {
						line = line.replace(old, "driveGuide('" + config + "')");
						break;
					}
				}
			}
			
			sb.append(String.format("%s%n", line));
		}
		return sb.toString();
	}
}
