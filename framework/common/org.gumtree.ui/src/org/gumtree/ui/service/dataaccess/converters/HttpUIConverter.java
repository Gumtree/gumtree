package org.gumtree.ui.service.dataaccess.converters;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.gumtree.service.dataaccess.DataAccessException;
import org.gumtree.service.dataaccess.IDataConverter;
import org.gumtree.service.dataaccess.RepresentationNotSupportedException;

public class HttpUIConverter implements IDataConverter<GetMethod> {

	private static final List<Class<?>> SUPPORTED_FORMATS = Arrays.asList(new Class<?>[] {
			ImageData.class, Image.class });
	
	@SuppressWarnings("unchecked")
	public <T> T convert(GetMethod getMethod, Class<T> representation,
			Map<String, Object> properties)
			throws RepresentationNotSupportedException {
		// Quick check
		if (!getSupportedRepresentations().contains(representation)) {
			throw new RepresentationNotSupportedException();
		}
		try {
			if (representation.equals(ImageData.class)) {
				return (T) new ImageData(getMethod.getResponseBodyAsStream());
			} else if (representation.equals(ImageData.class)) {
				return (T) new Image(Display.getDefault(), new ImageData(
						getMethod.getResponseBodyAsStream()));
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
