package au.gov.ansto.bragg.quokka.webserver.vaadin;

import com.vaadin.Application;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

public class QuokkaWebApplication extends Application {

	private static final long serialVersionUID = 1552513534357749056L;

	private Window window;
	
	@Override
	public void init() {
		setTheme(Reindeer.THEME_NAME);
		window = new Window("Quokka Dashboard");
		setMainWindow(window);
	}

}
