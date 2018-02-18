package org.gumtree.control.ui.viewer.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.gumtree.control.core.ISicsController;
import org.gumtree.control.ui.viewer.EntryType;
import org.gumtree.control.ui.viewer.FilterEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NodeSet implements INodeSet {

	private static final int DEFAULT_WIDTH = 250;
	
	private static final Logger logger = LoggerFactory.getLogger(NodeSet.class);
	
	private static volatile DocumentBuilder builder;
	
	private String title;

	private List<IFilterEntry> entries;

	private SetType setType = SetType.TREE; 
	
	private String[] columns;
	
	private int width = DEFAULT_WIDTH;
	
	private Map<String, String> aliasMap;
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public SetType getSetType() {
		return setType;
	}

	public void setSetType(SetType setType) {
		this.setType = setType;
	}

	public String[] getColumns() {
		return columns;
		
	}

	public int getLabelColumnWidth() {
		return width;
	}
	
	public void setLabelColumnWidth(int width) {
		this.width = width;
	}

	public void setColumns(String columns) {
		if (columns != null) {
			this.columns = columns.split(",");
			for (int i = 0; i < this.columns.length; i++) {
				this.columns[i] = this.columns[i].trim();
			}
		}
	}

	public List<IFilterEntry> getEntries() {
		if (entries == null) {
			entries = new ArrayList<IFilterEntry>();
		}
		return entries;
	}
	
	// Checks the visibility purely based on the filter entries,
	// and takes no assumption on either its children are visible or not
	public boolean isVisible(ISicsController controller) {
		return isVisible(controller.getPath());
	}
	
	public boolean isVisible(String path) {
		boolean isVisible = false;
		String[] paths = path.split("/");
		for (IFilterEntry entry : getEntries()) {
			String[] filterPaths = entry.getEntryAsParts();
			if (paths.length < filterPaths.length) {
				// Filter is too specific ... ie, no match
				continue;
			}
			if (getSetType().equals(SetType.FLAT) && paths.length > filterPaths.length) {
				// Special case for flat type:
				// We don't want to match children 
				continue;
			}
				
			// Start matching
			boolean isMatched = true;
			for (int i = 0; i < filterPaths.length; i++) {
				if (filterPaths[i].equals("*")) {
					continue;
				}
				if (filterPaths[i].equals(paths[i])) {
					continue;
				}
				isMatched = false;
				break;
			}
			if (isMatched) {
				isVisible = entry.getType().equals(EntryType.INCLUDE);
			}
		}
		return isVisible;
	}
	

	public String getAlias(ISicsController controller) {
		return getAliasMap().get(controller.getPath());
	}

	public boolean hasAlias(ISicsController controller) {
		return getAliasMap().get(controller.getPath()) != null;
	}

	public void setAlias(String path, String alias) {
		getAliasMap().put(path, alias);
	}
	
	private Map<String, String> getAliasMap() {
		if (aliasMap == null) {
			aliasMap = new HashMap<String, String>();
		}
		return aliasMap;
	}

	public static NodeSet read(InputStream inputStream) throws Exception {
		return parse(getBuilder().parse(inputStream));
	}
	
	@SuppressWarnings("unchecked")
	public static NodeSet parse(Document doc) {
		NodeSet filter = new NodeSet();
		NodeList nodeList = doc.getDocumentElement().getChildNodes();
		// TODO: refactor with Op4j
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (!(node instanceof Element)) {
				continue;
			}
			Element element = (Element) node;
			if (element.getNodeName().equals("title")) {
				filter.setTitle(element.getTextContent());
			} else if (element.getNodeName().equals("type")) {
				if (element.getTextContent().equalsIgnoreCase("tree")) {
					filter.setSetType(SetType.TREE);
				} else if (element.getTextContent().equalsIgnoreCase("subtree")) {
					filter.setSetType(SetType.SUBTREE);
				} else if (element.getTextContent().equalsIgnoreCase("flat")) {
					filter.setSetType(SetType.FLAT);
				}
			} else if (element.getNodeName().equals("columns")) {
				filter.setColumns(element.getTextContent());
			} else if (element.getNodeName().equals("include")) {
				filter.getEntries().add(FilterEntry.createIncludeEntry(element.getTextContent()));
				Attr alias = element.getAttributeNode("alias");
				if (alias != null) {
					filter.setAlias(element.getTextContent(), alias.getValue());
				}
			} else if (element.getNodeName().equals("exclude")) {
				filter.getEntries().add(FilterEntry.createExcludeEntry(element.getTextContent()));
			} else if (element.getNodeName().equals("width")) {
				filter.setLabelColumnWidth(Integer.parseInt(element.getTextContent()));
			}
		}
		return filter;
	}
	
	private static DocumentBuilder getBuilder() {
		if (builder == null) {
			synchronized (NodeSet.class) {
				if (builder == null) {
					try {
						builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					} catch (Exception e) {
						logger.error("Failed to create document builder.", e);
					}
				}
			}
		}
		return builder;
	}
	
}
