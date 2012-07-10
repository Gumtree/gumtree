package org.gumtree.data.ui.jviewers;

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

public class JDatasetViewer extends JPanel {

	private static final long serialVersionUID = 6546847575383521674L;

	public JDatasetViewer() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[]{1.0};
		gridBagLayout.columnWeights = new double[]{1.0};
		setLayout(gridBagLayout);

		// Left browser
		JDatasetBrowser datasetBrowser = new JDatasetBrowser();

		// Right pane
		JObjectViewer objectViewer = new JObjectViewer();
		JAttributeViewer attributeViewer = new JAttributeViewer();
		JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				objectViewer, attributeViewer);
		rightSplitPane.setResizeWeight(0.75);

		JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				datasetBrowser, rightSplitPane);
		mainSplitPane.setResizeWeight(0.25);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		add(mainSplitPane, constraints);
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(
			        UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					JFrame frame = new JFrame("GDM Browser");

					Container contents = frame.getContentPane();
					GridBagLayout gridBagLayout = new GridBagLayout();
					gridBagLayout.rowWeights = new double[]{1.0};
					gridBagLayout.columnWeights = new double[]{1.0};
					contents.setLayout(gridBagLayout);
					
					JDatasetViewer viewer = new JDatasetViewer();
					GridBagConstraints constraints = new GridBagConstraints();
					constraints.fill = GridBagConstraints.BOTH;
					constraints.anchor = GridBagConstraints.CENTER;
					contents.add(viewer, constraints);

					frame.setSize(800, 640);
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
