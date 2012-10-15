package org.gumtree.data.core.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.osgi.framework.Bundle;

public class DataTestUtils {

	public static final String ID_PLUGIN = "org.gumtree.data.core.tests";

	/**
	 * Returns the file store handle for a particular file resource from a
	 * plugin.
	 * 
	 * @param pluginId
	 *            id of the plugin where file is located
	 * @param relativePath
	 *            file path relative to the plugin location
	 * @return file store handle to the specific file resource
	 * @throws CoreException
	 *             when plugin cannot be found
	 */
	public static IFileStore find(String pluginId, String relativePath)
			throws CoreException {
		Bundle bundle = Platform.getBundle(pluginId);
		if (bundle == null) {
			throw new CoreException(new Status(IStatus.ERROR, ID_PLUGIN,
					IStatus.ERROR, "Cannot find bundle.", null));
		}
		URL fileURL = bundle.getEntry(relativePath);
		URI fileURI = null;
		try {
			fileURI = URIUtil.toURI(FileLocator.toFileURL(fileURL).getFile());
			return EFS.getStore(fileURI);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, ID_PLUGIN,
					IStatus.ERROR, "Cannot find file store for "
							+ fileURL.toString(), e));
		}
	}

	public static void compareArrays(IArray original, IArray copy) {
		// Test factory name
		assertEquals(original.getFactoryName(), copy.getFactoryName());
		// Test rank
		assertEquals(original.getRank(), copy.getRank());
		// Test shape
		assertArrayEquals(original.getShape(), copy.getShape());
		// Test type
		assertEquals(original.getElementType(), copy.getElementType());
		// Test content
		IArrayIterator iterArray1 = original.getIterator();
		IArrayIterator iterArray2 = copy.getIterator();
		long i = 0;
		while (iterArray1.hasNext() || iterArray2.hasNext()) {
			Object obj1 = iterArray1.getObjectNext();
			Object obj2 = iterArray2.getObjectNext();
			assertEquals(
					"Invalid operation on compared content: original_cell[" + i
							+ "] = " + obj1 + " compared to copy_cell[" + i
							+ "] =  " + obj2, obj1, obj2);
			i++;
		}
	}

	private DataTestUtils() {
		super();
	}

}
