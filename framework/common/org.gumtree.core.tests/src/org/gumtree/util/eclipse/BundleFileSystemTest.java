package org.gumtree.util.eclipse;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.gumtree.core.tests.internal.Activator;
import org.junit.Test;

public class BundleFileSystemTest {

	@Test
	public void testGetFileStore() throws CoreException {
		BundleFileSystem fileSystem = new BundleFileSystem();
		// Get the manifest file
		IFileStore fileStore = fileSystem.getStore(URI.create("bundle://" + Activator.PLUGIN_ID + "/META-INF/MANIFEST.MF"));
		File file = fileStore.toLocalFile(EFS.NONE, new NullProgressMonitor());
		// Test if the file store points to an existing file
		assertTrue(file.exists());
	}
	
}
