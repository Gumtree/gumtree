package org.gumtree.gumnix.sics.internal.core;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.gumtree.core.util.eclipse.EclipseUtils;
import org.gumtree.core.util.eclipse.ExtensionRegistryConstants;
import org.gumtree.gumnix.sics.core.IInstrumentProfile;

public class InstrumentProfile implements IInstrumentProfile {

	private IConfigurationElement element;

	private String id;

	private String label;

	private URL imageURL;

	protected InstrumentProfile(IConfigurationElement element) {
		this.element = element;
	}

	public String getId() {
		if (id == null) {
			id = element.getAttribute(ExtensionRegistryConstants.ATTRIBUTE_ID);
		}
		return id;
	}

	public String getLabel() {
		if (label == null) {
			label = element
					.getAttribute(ExtensionRegistryConstants.ATTRIBUTE_LABEL);
		}
		return label;
	}

	public String getProperty(String name) {
		for(IConfigurationElement child : element.getChildren(InstrumentProfileRegistryConstants.ELEMENT_PROPERTY)) {
			if(child.getAttribute(InstrumentProfileRegistryConstants.ATTRIBUTE_NAME).equals(name)) {
				return child.getAttribute(InstrumentProfileRegistryConstants.ATTRIBUTE_VALUE);
			}
		}
		return null;
	}

	public URL getImageURL() {
		if(imageURL == null) {
			String filePath = element.getAttribute(InstrumentProfileRegistryConstants.ATTRIBUTE_IMAGE);
//			Bundle bundle = Platform.getBundle(element.getNamespaceIdentifier());
//			imageURL = FileLocator.find(bundle, new Path(filePath), null);
			try {
//				imageURL = FileLocator.toFileURL(imageURL);
				IFileStore store = EclipseUtils.find(element.getNamespaceIdentifier(), filePath);
				if(store != null) {
					imageURL = store.toURI().toURL();
				}
			} catch (IOException e) {
				e.printStackTrace();
//			} catch (InvalidRegistryObjectException e) {
//				e.printStackTrace();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return imageURL;
	}

	public String getProperty(ConfigProperty configProperty) {
		return getProperty(configProperty.getName());
	}

	public boolean isDefault() {
		return false;
	}

}
