/*******************************************************************************
 * Copyright (c) 2012 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bragg Institute - initial API and implementation
 ******************************************************************************/

package org.gumtree.service.dataaccess.converters;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.gumtree.service.dataaccess.DataAccessException;
import org.gumtree.service.dataaccess.IDataConverter;
import org.gumtree.service.dataaccess.RepresentationNotSupportedException;

public class FileStoreGerenalConverter implements IDataConverter<IFileStore> {

	private static final List<Class<?>> SUPPORTED_FORMATS = Arrays.asList(new Class<?>[] {
			IFileStore.class, IFileInfo.class, File.class, InputStream.class,
			OutputStream.class, String.class });
	
	@SuppressWarnings("unchecked")
	public <T> T convert(IFileStore fileStore, Class<T> representation,
			Map<String, Object> properties) {
		// Quick check
		if (!getSupportedRepresentations().contains(representation)) {
			throw new RepresentationNotSupportedException();
		}
		// Convert
		try {
			if (representation.equals(IFileStore.class)) {
				return (T) fileStore;
			} else if (representation.equals(IFileInfo.class)) {
				return (T) fileStore.fetchInfo();
			} else if (representation.equals(File.class)) {
				return (T) fileStore.toLocalFile(EFS.NONE, null);
			} else if (representation.equals(InputStream.class)) {
				return (T) fileStore.openInputStream(EFS.NONE, null);
			} else if (representation.equals(OutputStream.class)) {
				return (T) fileStore.openOutputStream(EFS.NONE, null);
			} else if (representation.equals(String.class)) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(fileStore.openInputStream(EFS.NONE, null)));
				StringBuilder builder = new StringBuilder();
				String line = null;
				while((line = reader.readLine()) != null) {
					builder.append(line);
					builder.append("\n");
				}
				reader.close();
				return (T) builder.toString();
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
