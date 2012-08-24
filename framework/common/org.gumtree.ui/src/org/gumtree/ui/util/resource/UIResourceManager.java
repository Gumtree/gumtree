package org.gumtree.ui.util.resource;

import org.eclipse.jface.resource.DeviceResourceDescriptor;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.gumtree.ui.internal.Activator;

public class UIResourceManager extends ResourceManager {

	private String pluginId;

	private LocalResourceManager manager;

	public UIResourceManager() {
		this(Activator.PLUGIN_ID);
	}
	
	public UIResourceManager(Control owner) {
		this(Activator.PLUGIN_ID, owner);
	}
	
	public UIResourceManager(String pluginId) {
		manager = new LocalResourceManager(JFaceResources.getResources());
		this.pluginId = pluginId;
	}
	
	public UIResourceManager(String pluginId, Control owner) {
		manager = new LocalResourceManager(JFaceResources.getResources(), owner);
		this.pluginId = pluginId;
	}
	
	@Override
	public Object create(DeviceResourceDescriptor descriptor) {
		return manager.create(descriptor);
	}

	@Override
	public void destroy(DeviceResourceDescriptor descriptor) {
		manager.destroy(descriptor);
	}

	@Override
	public Object find(DeviceResourceDescriptor descriptor) {
		return manager.find(descriptor);
	}

	@Override
	protected Image getDefaultImage() {
		// This is a bit of hack, because we assume the LocalResourceManager
		// will get its default image when the descriptor is null
		return manager.createImageWithDefault(null);
	}

	@Override
	public Device getDevice() {
		return manager.getDevice();
	}

	public String getPluginId() {
		return pluginId;
	}

	public LocalResourceManager getParentManager() {
		return manager;
	}
	
	public Image createImage(String imageFilePath) {
		ImageDescriptor desc = UIResourceUtils.imageDescriptorFromPlugin(getPluginId(), imageFilePath);
		return getParentManager().createImageWithDefault(desc);
	}
	
	public Font createFont(String fontName, int height, int style) {
		FontDescriptor desc = FontDescriptor.createFrom(fontName, height, style);
		return getParentManager().createFont(desc);
	}
	
	public Font createDefaultFont(int height, int style) {
		return createFont(JFaceResources.getDefaultFont().getFontData()[0].getName(), height, style);
	}
	
	public Font createRelativeFont(int reletiveHeight, int style) {
		return createRelativeFont(JFaceResources.getDefaultFont(), reletiveHeight, style);
	}
	
	public Font createRelativeFont(Font font, int reletiveHeight, int style) {
		String fontName = font.getFontData()[0].getName();
		FontDescriptor desc = FontDescriptor.createFrom(fontName, font.getFontData()[0].getHeight() + reletiveHeight, style);
		return getParentManager().createFont(desc);
	}
	
	public Color createColor(int red, int green, int blue) {
		return getParentManager().createColor(new RGB(red, green, blue));
	}

}
