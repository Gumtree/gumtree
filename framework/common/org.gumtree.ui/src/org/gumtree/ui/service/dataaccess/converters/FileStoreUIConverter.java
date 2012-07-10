package org.gumtree.ui.service.dataaccess.converters;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.gumtree.service.dataaccess.DataAccessException;
import org.gumtree.service.dataaccess.IDataConverter;
import org.gumtree.service.dataaccess.RepresentationNotSupportedException;

public class FileStoreUIConverter implements IDataConverter<IFileStore> {

	private static final List<Class<?>> SUPPORTED_FORMATS = Arrays.asList(new Class<?>[] {
			ImageData.class, Image.class, ImageDescriptor.class });

	@SuppressWarnings("unchecked")
	public <T> T convert(IFileStore fileStore, Class<T> representation,
			Map<String, Object> properties)
			throws RepresentationNotSupportedException {
		// Quick check
		if (!getSupportedRepresentations().contains(representation)) {
			throw new RepresentationNotSupportedException();
		}
		try {
			if (representation.equals(ImageData.class)) {
				return (T) new ImageData(fileStore.openInputStream(EFS.NONE, null));
			} else if (representation.equals(Image.class)) {
				return (T) new Image(Display.getDefault(), new ImageData(
						fileStore.openInputStream(EFS.NONE, null)));
			} else if (representation.equals(ImageDescriptor.class)) {
				return (T) ImageDescriptor.createFromURL(fileStore
						.toLocalFile(EFS.NONE, new NullProgressMonitor())
						.toURI().toURL());
			}
		} catch (Exception e) {
			throw new DataAccessException(e);
		}
		throw new RepresentationNotSupportedException();
	}

	public List<Class<?>> getSupportedRepresentations() {
		return SUPPORTED_FORMATS;
	}
	
}
