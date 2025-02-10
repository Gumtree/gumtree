package au.gov.ansto.bragg.koala.ui;

import java.io.IOException;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.gumtree.ui.service.multimonitor.IMultiMonitorManager;
import org.gumtree.ui.service.multimonitor.support.MultiMonitorManager;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.util.ISystemProperty;
import org.gumtree.util.SystemProperty;
import org.osgi.framework.BundleContext;

import com.github.jaiimageio.impl.plugins.tiff.TIFFImageReaderSpi;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriterSpi;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "au.gov.ansto.bragg.koala.ui"; //$NON-NLS-1$

	public static final String NAME_PROP_ID = "koala.propId";
	public static final String NAME_USER_NAME = "koala.userName";
	public static final String NAME_LOCAL_SCI = "koala.localSci";
	public static final String NAME_OP_MODE = "koala.operationMode";
	public static final String NAME_ERASURE_TIME = "koala.erasureTime";
	public static final String NAME_SAMPLE_NAME = "koala.sampleName";
	public static final String NAME_FILENAME = "koala.filename";
	public static final String NAME_COMMENTS = "koala.comments";
	public static final String NAME_PROP_FOLDER = "koala.proposalFolder";
	public static final String NAME_SX_ALIGN = "koala.sxAlignValue";
	public static final String NAME_SY_ALIGN = "koala.syAlignValue";
	public static final String NAME_SZ_ALIGN = "koala.szAlignValue";
	public static final String NAME_MJPEG_MMPERPIXEL_LEFT = "koala.mmPerPixelX";
	public static final String NAME_MJPEG_MMPERPIXEL_RIGHT = "koala.mmPerPixelY";
	public static final String BEAM_CENTRE_LEFT = "gumtree.koala.beamCentreLeft";
	public static final String BEAM_CENTRE_RIGHT = "gumtree.koala.beamCentreRight";
	public static final String NAME_JOEY_MODE = "koala.joeyMode";
	public static final String NAME_PASS_DISABLED = "gumtree.koala.passDisabled";
	public static final String PHI = "\u03A6".toLowerCase();
	public static final String CHI = "\u03A7".toLowerCase();
//	public static final String PHI_HTML = "&#632;";
//	public static final String CHI_HTML = "&#967;";
	public static final String PHI_HTML = "&phi;";
	public static final String CHI_HTML = "&chi;";
	public static final String TAU = "\u03C4";
	public static final String MAINTENANCE_FILTER = "MaintenanceFilter.xml";
	public static final String PROP_SAVING_PATH = "gumtree.koala.proposalFolder";
	
	// The shared instance
	private static Activator plugin;
	
	private static Font fontLarge;
	private static Font fontMiddle;
	private static Cursor handCursor;
	private static Cursor busyCursor;
	private static Cursor defaultCursor;
	private static Color lightColor;
	private static Color lightForgroundColor;
	private static Color veryLightForgroundColor;
	private static Color backgroundColor;
	private static Color highlightBackgroundColor;
	private static Color forgroundColor;
	private static Color highlightColor;
	private static Color warningColor;
	private static Color busyColor;
	private static Color idleColor;
	private static Color busyForgroundColor;
	private static Color runningBackgoundColor;
	private static Color runningForgroundColor;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		SafeUIRunner.syncExec(new ISafeRunnable() {
			public void handleException(Throwable exception) {
			}
			@SuppressWarnings("restriction")
			public void run() throws Exception {
				Display currentDisplay = getWorkbench().getDisplay();
				Font systemFont = currentDisplay.getSystemFont();
				FontData[] fD = systemFont.getFontData();
				fD[0].setHeight(32);
				fontLarge = new Font(currentDisplay, fD[0]);
				fD[0].setHeight(16);
				fontMiddle = new Font(currentDisplay, fD[0]);
				handCursor = new Cursor(currentDisplay, SWT.CURSOR_HAND);
				busyCursor = new Cursor(currentDisplay, SWT.CURSOR_WAIT);
				defaultCursor = currentDisplay.getSystemCursor(SWT.CURSOR_ARROW);
				lightColor = Display.getDefault().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
				backgroundColor = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
				highlightBackgroundColor = new Color(Display.getDefault(), 161, 194, 241);
				forgroundColor = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
				highlightColor = Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED);
				warningColor = Display.getDefault().getSystemColor(SWT.COLOR_RED);
				busyColor = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN);
				busyForgroundColor = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
				idleColor = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
				lightForgroundColor = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);
				veryLightForgroundColor = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
				runningBackgoundColor = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN);
				runningForgroundColor = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
			}
		});
		
		IIORegistry.getDefaultInstance().registerServiceProvider(
	             new TIFFImageReaderSpi(), ImageReaderSpi.class);
		IIORegistry.getDefaultInstance().registerServiceProvider(
	             new TIFFImageWriterSpi(), ImageWriterSpi.class);

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static Font getLargeFont() {
		return fontLarge;
	}
	
	public static Font getMiddleFont() {
		return fontMiddle;
	}

	public static Cursor getHandCursor() {
		return handCursor;
	}
	public static Cursor getBusyCursor() {
		return busyCursor;
	}
	public static Cursor getDefaultCursor() {
		return defaultCursor;
	}
	public static Color getLightColor() {
		return lightColor;
	}
	public static Color getBackgroundColor() {
		return backgroundColor;
	}
	public static Color getHighlightBackgroundColor() {
		return highlightBackgroundColor;
	}
	public static Color getForgroundColor() {
		return forgroundColor;
	}
	public static Color getLightForgroundColor() {
		return lightForgroundColor;
	}
	public static Color getVeryLightForgroundColor() {
		return veryLightForgroundColor;
	}
	public static Color getHighlightColor() {
		return highlightColor;
	}
	public static Color getWarningColor() {
		return warningColor;
	}
	public static Color getBusyColor() {
		return busyColor;
	}
	public static Color getIdleColor() {
		return idleColor;
	}
	public static Color getRunningBackgoundColor() {
		return runningBackgoundColor;
	}
	public static Color getRunningForgroundColor() {
		return runningForgroundColor;
	}
	public static int getMonitorWidth() {
		IMultiMonitorManager mmManager = new MultiMonitorManager();
		return mmManager.getMonitorWidth();
	}
	
	public static void setPreference(String name, String value){
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setValue(name, value);
	}

	public static void flushPreferenceStore(){
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		if (store != null && store.needsSaving()
				&& store instanceof IPersistentPreferenceStore) {
			try {
				((IPersistentPreferenceStore) store).save();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static String getPreference(String name) {
		if (name.contains(":")){
			String[] pairs = name.split(":");
			return Platform.getPreferencesService().getString(
					pairs[0], pairs[1], "", null).trim();
		} else {
			return Platform.getPreferencesService().getString(
					Activator.PLUGIN_ID, name, "", null).trim();
		}
	}
	
	public static boolean isPassDisabled() {
		try {
			return Boolean.valueOf(System.getProperty(NAME_PASS_DISABLED));
		} catch (Exception e) {
			return false;
		}
	}

	public static Color getBusyForgroundColor() {
		return busyForgroundColor;
	}
}
