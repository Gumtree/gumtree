package org.gumtree.msw.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
	public static ICommand buildLoadCommand(IModelProxy proxy, Iterable<String> listPath, String rootDefinitionName, boolean isElementList, InputStream stream) {
		DataSource xsdSource = proxy.getXsd();
		DataSource xmlSource = new DataSource(stream);
		IRefIdProvider idProvider = proxy.getIdProvider();

		IModelNode content = ModelLoader.load(idProvider, xsdSource, xmlSource, rootDefinitionName);
		if (content == null)
			return null;

		RefId commandId = idProvider.nextId();
		List<Command> commands = new ArrayList<>();

		if (!isElementList)
			addLoadCommands(commandId, commands, listPath, content);
		else
			for (IModelNode subNode : content.getNodes())
				addLoadCommands(commandId, commands, listPath, subNode);

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
