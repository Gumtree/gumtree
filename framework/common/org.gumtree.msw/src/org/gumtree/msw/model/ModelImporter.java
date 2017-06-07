package org.gumtree.msw.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gumtree.msw.ICommand;
import org.gumtree.msw.IModel;
import org.gumtree.msw.IModelProxy;
import org.gumtree.msw.IRefIdProvider;
import org.gumtree.msw.RefId;
import org.gumtree.msw.commands.AddListElementCommand;
import org.gumtree.msw.commands.BatchCommand;
import org.gumtree.msw.commands.ChangePropertyCommand;
import org.gumtree.msw.commands.Command;

public class ModelImporter {
	// methods
	public static BatchCommand buildLoadCommand(IModelProxy proxy, Iterable<String> listPath, String rootDefinitionName, boolean isElementList, DataSource xmlSource) {
		return buildLoadCommand(proxy, listPath, rootDefinitionName, isElementList, xmlSource, null);
	}
	public static BatchCommand buildLoadCommand(IModelProxy proxy, Iterable<String> listPath, String rootDefinitionName, boolean isElementList, DataSource xmlSource, Map<String, Object> properties) {
		DataSource xsdSource = proxy.getXsd();
		IRefIdProvider idProvider = proxy.getIdProvider();
		return buildLoadCommand(idProvider, xsdSource, listPath, rootDefinitionName, isElementList, xmlSource, properties);
	}
	public static BatchCommand buildLoadCommand(IRefIdProvider idProvider, DataSource xsdSource, Iterable<String> listPath, String rootDefinitionName, boolean isElementList, DataSource xmlSource) {
		return buildLoadCommand(idProvider, xsdSource, listPath, rootDefinitionName, isElementList, xmlSource, null);
	}
	public static BatchCommand buildLoadCommand(IRefIdProvider idProvider, DataSource xsdSource, Iterable<String> listPath, String rootDefinitionName, boolean isElementList, DataSource xmlSource, Map<String, Object> properties) {
		IModelNode content = ModelLoader.load(idProvider, xsdSource, xmlSource, rootDefinitionName);
		if (content == null)
			return null;
		
		RefId commandId = idProvider.nextId();
		List<Command> commands = new ArrayList<>();

		if (!isElementList) {
			if (properties != null)
				for (Entry<String, Object> entry : properties.entrySet())
					content.changeProperty(entry.getKey(), entry.getValue());
			
			addLoadCommands(commandId, commands, listPath, content);
		}
		else {
			for (IModelNode subNode : content.getNodes()) {
				if (properties != null)
					for (Entry<String, Object> entry : properties.entrySet())
						subNode.changeProperty(entry.getKey(), entry.getValue());

				addLoadCommands(commandId, commands, listPath, subNode);
			}
		}

		if (commands.isEmpty())
			return null;

		return new BatchCommand(commandId, commands.toArray(new ICommand[commands.size()]));
	}
	// helper
	private static void addLoadCommands(RefId commandId, List<Command> commands, Iterable<String> listPath, IModelNode node) {
		String elementName = node.getName();
		Iterable<String> elementPath = createElementPath(listPath, elementName);

		// add element
		commands.add(new AddListElementCommand(
				commandId,
				listPath,
				elementName));
		
		// update properties
		for (IModelNodePropertyInfo property : node.getNodeInfo().getProperties()) {
			String propertyName = property.getName();
			if (!IModelNode.ID.equals(propertyName) && !IModel.INDEX.equals(propertyName))
				if (!property.isDefaultValue())
					commands.add(new ChangePropertyCommand(
							commandId,
							elementPath,
							propertyName,
							property.get()));
		}
		
		// create sub nodes
		for (IModelNode subNode : node.getNodes())
			addLoadCommands(commandId, commands, elementPath, subNode);
	}
	private static Iterable<String> createElementPath(Iterable<String> listPath, String elementName) {
		ArrayList<String> elementPath = new ArrayList<>();
		for (String name : listPath)
			elementPath.add(name);
		elementPath.add(elementName);
		
		elementPath.trimToSize();
		return elementPath;
	}
}
