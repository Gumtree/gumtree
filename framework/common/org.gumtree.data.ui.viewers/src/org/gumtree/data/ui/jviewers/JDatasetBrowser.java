package org.gumtree.data.ui.jviewers;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.gumtree.data.Factory;
import org.gumtree.data.interfaces.IContainer;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.ui.viewers.internal.InternalImage;

public class JDatasetBrowser extends JPanel {

	private static final long serialVersionUID = -7671639159412090535L;

	private List<IDataset> datasets;
	
	private DefaultMutableTreeNode rootNode;
	
	private DefaultTreeModel treeModel;
	
	private JTree tree;
	
	public JDatasetBrowser() {
		datasets = new ArrayList<IDataset>();
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{1.0, 1.0};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0};
		setLayout(gridBagLayout);
		
		rootNode = new DefaultMutableTreeNode("Root Node");
		treeModel = new DefaultTreeModel(rootNode);
		
		tree = new JTree(treeModel);
		tree.setRootVisible(false);
		tree.setEditable(true);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridwidth = 2;
		add(tree, constraints);
		
		JButton openButton = new JButton("Open");
		openButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.showOpenDialog(getParent());
				File file = chooser.getSelectedFile();
				if (file != null) {
					try {
						IDataset dataset = Factory.createDatasetInstance(file.toURI());
						// Check duplication
						for (IDataset ds : datasets) {
							if (dataset.getLocation().equals(ds.getLocation())) {
								dataset.close();
								return;
							}
						}
						// If not duplicated, open it
//						dataset.open();
						datasets.add(dataset);
						
						//It is key to invoke this on the TreeModel, and NOT DefaultMutableTreeNode
						DefaultMutableTreeNode childNode = createTreeNode(dataset);
				        treeModel.insertNodeInto(childNode, rootNode, 
				        		rootNode.getChildCount());
				        tree.scrollPathToVisible(new TreePath(childNode.getPath()));
					} catch (Exception e1) {
						// TODO: error handling
						e1.printStackTrace();
					}
				}
			}
		});
		openButton.setIcon(InternalImage.OPEN.createImageIcon());
		GridBagConstraints gbc_openButton = new GridBagConstraints();
		gbc_openButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_openButton.gridx = 0;
		gbc_openButton.gridy = 1;
		add(openButton, gbc_openButton);
		
		JButton removeButton = new JButton("Remove");
		removeButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				
			}
		});
		removeButton.setIcon(InternalImage.REMOVE.createImageIcon());
		GridBagConstraints gbc_removeButton = new GridBagConstraints();
		gbc_removeButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_removeButton.gridx = 1;
		gbc_removeButton.gridy = 1;
		add(removeButton, gbc_removeButton);
	}
	
	private DefaultMutableTreeNode createTreeNode(IDataset dataset) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(dataset) {
			private static final long serialVersionUID = -8369610371226519269L;
			public String toString() {
				return ((IDataset) getUserObject()).getLocation();
			}
		};
		for (IGroup group : dataset.getRootGroup().getGroupList()) {
			ObjectMutableTreeNode groupNode = new ObjectMutableTreeNode(group);
			node.add(groupNode);
			createTreeNode(group, groupNode);
		}
		return node;
	}
	
	private void createTreeNode(IGroup parentGroup, ObjectMutableTreeNode parentNode) {
		for (IGroup group : parentGroup.getGroupList()) {
			ObjectMutableTreeNode groupNode = new ObjectMutableTreeNode(group);
			parentNode.add(groupNode);
			createTreeNode(group, groupNode);
		}
		for (IDataItem dataItem: parentGroup.getDataItemList()) {
			ObjectMutableTreeNode dataItemNode = new ObjectMutableTreeNode(dataItem);
			parentNode.add(dataItemNode);
		}
	}
	
	class ObjectMutableTreeNode extends DefaultMutableTreeNode {
	
		private static final long serialVersionUID = -600715990556682399L;

		public ObjectMutableTreeNode(IContainer object) {
			super(object);
		}
		
		public String toString() {
			return ((IContainer) getUserObject()).getShortName();
		}
	}
	
}
