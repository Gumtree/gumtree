package org.gumtree.sics.server.vaadin;

import com.vaadin.Application;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

public class SicsWebApplication extends Application {

	private Window main;
	private VerticalLayout mainLayout;
	private Tree tree;
	
	@Override
	public void init() {
		setTheme(Reindeer.THEME_NAME);
		main = new Window("Dynamic Vaadin OSGi Demo");
		mainLayout = (VerticalLayout) main.getContent();
		mainLayout.setMargin(false);
		mainLayout.setStyleName("blue");
		setMainWindow(main);

		mainLayout.setSizeFull();
//		mainLayout.addComponent(getMenu());

		HorizontalLayout header = new HorizontalLayout();

//		header.addComponent(getHeader());
//		header.addComponent(getToolbar());
		mainLayout.addComponent(header);
		
		tree = new Tree("Hipadaba Tree");
		mainLayout.addComponent(tree);
		
		tree.addItem("123");
	}

}
