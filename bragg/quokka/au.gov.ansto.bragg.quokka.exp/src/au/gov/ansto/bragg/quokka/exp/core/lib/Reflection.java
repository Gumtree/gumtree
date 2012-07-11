/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
*******************************************************************************/
package au.gov.ansto.bragg.quokka.exp.core.lib;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.osgi.framework.internal.core.BundleURLConnection;

/**
 * This class is a collection of utility functions for dealing with
 * {@link java.lang.reflect reflection}.
 * 
 * @see #INSTANCE
 * 
 * @author Joerg Hohwiller (hohwille at users.sourceforge.net)
 */
public class Reflection {

	public static List<String> findClassList(String packageName, boolean includeSubPackages)
	throws IOException {

		Set<String> classSet = findClassNames(packageName, includeSubPackages);
		List<String> classList = new ArrayList<String>();
		for (Iterator<?> iterator = classSet.iterator(); iterator.hasNext();) {
			classList.add((String) iterator.next());
		}
		Collections.sort(classList);
		return classList;
	}

	/**
	 * This method finds all classes that are located in the package identified by
	 * the given <code>packageName</code>.<br>
	 * <b>ATTENTION:</b><br>
	 * This is a relative expensive operation. Depending on your classpath
	 * multiple directories,JAR-, and WAR-files may need to be scanned.
	 * 
	 * @param packageName is the name of the {@link Package} to scan.
	 * @param includeSubPackages - if <code>true</code> all sub-packages of the
	 *        specified {@link Package} will be included in the search.
	 * @return a {@link Set} will the fully qualified names of all requested
	 *         classes.
	 * @throws IOException if the operation failed with an I/O error.
	 */
	public static Set<String> findClassNames(String packageName, boolean includeSubPackages)
	throws IOException {

		Set<String> classSet = new HashSet<String>();
		findClassNames(packageName, includeSubPackages, classSet);
		return classSet;
	}

	/**
	 * This method finds all classes that are located in the package identified by
	 * the given <code>packageName</code>.<br>
	 * <b>ATTENTION:</b><br>
	 * This is a relative expensive operation. Depending on your classpath
	 * multiple directories,JAR-, and WAR-files may need to be scanned.
	 * 
	 * @param packageName is the name of the {@link Package} to scan.
	 * @param includeSubPackages - if <code>true</code> all sub-packages of the
	 *        specified {@link Package} will be included in the search.
	 * @param classSet is where to add the classes.
	 * @throws IOException if the operation failed with an I/O error.
	 */
	public static void findClassNames(String packageName, boolean includeSubPackages, Set<String> classSet)
	throws IOException {

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		String path = packageName.replace('.', '/');
		String pathWithPrefix = path + '/';
		Enumeration<URL> urls = classLoader.getResources(path);
		StringBuilder qualifiedNameBuilder = new StringBuilder(packageName);
		qualifiedNameBuilder.append('.');
		int qualifiedNamePrefixLength = qualifiedNameBuilder.length();
		while (urls.hasMoreElements()) {
			URL packageUrl = urls.nextElement();
			String urlString = URLDecoder.decode(packageUrl.getFile(), "UTF-8");
			String protocol = packageUrl.getProtocol().toLowerCase();
			if ("bundleresource".equals(protocol)){
				BundleURLConnection connection = (BundleURLConnection) packageUrl.openConnection();
//				JarFile jarFile = connection.getJarFile();
				packageUrl = connection.getFileURL();
//				urlString = packageUrl.toString();
				urlString = packageUrl.getPath();
				protocol = packageUrl.getProtocol();
//				JarFile jarFile = new JarFile(fileUrl.getFile());
//				Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
//				while (jarEntryEnumeration.hasMoreElements()) {
//					JarEntry jarEntry = jarEntryEnumeration.nextElement();
//					String absoluteFileName = jarEntry.getName();
//					if (absoluteFileName.endsWith(".class")) {
//						if (absoluteFileName.startsWith("/")) {
//							absoluteFileName.substring(1);
//						}
//						// special treatment for WAR files...
//						// "WEB-INF/lib/" entries should be opened directly in contained jar
//						if (absoluteFileName.startsWith("WEB-INF/classes/")) {
//							// "WEB-INF/classes/".length() == 16
//							absoluteFileName = absoluteFileName.substring(16);
//						}
//						boolean accept = true;
//						if (absoluteFileName.startsWith(pathWithPrefix)) {
//							String qualifiedName = absoluteFileName.replace('/', '.');
//							if (!includeSubPackages) {
//								int index = absoluteFileName.indexOf('/', qualifiedNamePrefixLength + 1);
//								if (index != -1) {
//									accept = false;
//								}
//							}
//							if (accept) {
//								String className = fixClassName(qualifiedName);
//								if (className != null) {
//									classSet.add(className);
//								}
//							}
//						}
//					}
//				}
			} 
			if ("file".equals(protocol)) {
				File packageDirectory = new File(urlString);
//				File packageDirectory = ;
				if (packageDirectory.isDirectory()) {
					if (includeSubPackages) {
						findClassNamesRecursive(packageDirectory, classSet, qualifiedNameBuilder,
								qualifiedNamePrefixLength);
					} else {
						for (String fileName : packageDirectory.list()) {
							String simpleClassName = fixClassName(fileName);
							if (simpleClassName != null) {
								qualifiedNameBuilder.setLength(qualifiedNamePrefixLength);
								qualifiedNameBuilder.append(simpleClassName);
								classSet.add(qualifiedNameBuilder.toString());
							}
						}
					}
				}
			} else if ("jar".equals(protocol)) {
//				somehow the connection has no close method and can NOT be disposed
				JarURLConnection connection = (JarURLConnection) packageUrl.openConnection();
				JarFile jarFile = connection.getJarFile();
				Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
				while (jarEntryEnumeration.hasMoreElements()) {
					JarEntry jarEntry = jarEntryEnumeration.nextElement();
					String absoluteFileName = jarEntry.getName();
					if (absoluteFileName.endsWith(".class")) {
						if (absoluteFileName.startsWith("/")) {
							absoluteFileName.substring(1);
						}
						// special treatment for WAR files...
						// "WEB-INF/lib/" entries should be opened directly in contained jar
						if (absoluteFileName.startsWith("WEB-INF/classes/")) {
							// "WEB-INF/classes/".length() == 16
							absoluteFileName = absoluteFileName.substring(16);
						}
						boolean accept = true;
						if (absoluteFileName.startsWith(pathWithPrefix)) {
							String qualifiedName = absoluteFileName.replace('/', '.');
							if (!includeSubPackages) {
								int index = absoluteFileName.indexOf('/', qualifiedNamePrefixLength + 1);
								if (index != -1) {
									accept = false;
								}
							}
							if (accept) {
								String className = fixClassName(qualifiedName);
								if (className != null) {
									classSet.add(className);
								}
							}
						}
					}
				}
			}
			else {
				// TODO: unknown protocol - log this?
			}
		}
	}

	/**
	 * This method finds the recursively scans the given
	 * <code>packageDirectory</code> for {@link Class} files and adds their
	 * according Java names to the given <code>classSet</code>.
	 * 
	 * @param packageDirectory is the directory representing the {@link Package}.
	 * @param classSet is where to add the Java {@link Class}-names to.
	 * @param qualifiedNameBuilder is a {@link StringBuilder} containing the
	 *        qualified prefix (the {@link Package} with a trailing dot).
	 * @param qualifiedNamePrefixLength the length of the prefix used to rest the
	 *        string-builder after reuse.
	 */
	private static void findClassNamesRecursive(File packageDirectory, Set<String> classSet,
			StringBuilder qualifiedNameBuilder, int qualifiedNamePrefixLength) {

		for (File childFile : packageDirectory.listFiles()) {
			String fileName = childFile.getName();
			if (childFile.isDirectory()) {
				qualifiedNameBuilder.setLength(qualifiedNamePrefixLength);
				StringBuilder subBuilder = new StringBuilder(qualifiedNameBuilder);
				subBuilder.append(fileName);
				subBuilder.append('.');
				findClassNamesRecursive(childFile, classSet, subBuilder, subBuilder.length());
			} else {
				String simpleClassName = fixClassName(fileName);
				if (simpleClassName != null) {
					qualifiedNameBuilder.setLength(qualifiedNamePrefixLength);
					qualifiedNameBuilder.append(simpleClassName);
					classSet.add(qualifiedNameBuilder.toString());
				}
			}
		}
	}

	/**
	 * This method checks and transforms the filename of a potential {@link Class}
	 * given by <code>fileName</code>.
	 * 
	 * @param fileName is the filename.
	 * @return the according Java {@link Class#getName() class-name} for the given
	 *         <code>fileName</code> if it is a class-file that is no anonymous
	 *         {@link Class}, else <code>null</code>.
	 */
	private static String fixClassName(String fileName) {

		if (fileName.endsWith(".class")) {
			// remove extension (".class".length() == 6)
			String nameWithoutExtension = fileName.substring(0, fileName.length() - 6);
			// handle inner classes...
			/*
			 * int lastDollar = nameWithoutExtension.lastIndexOf('$'); if (lastDollar >
			 * 0) { char innerClassStart = nameWithoutExtension.charAt(lastDollar +
			 * 1); if ((innerClassStart >= '0') && (innerClassStart <= '9')) { //
			 * ignore anonymous class } else { return
			 * nameWithoutExtension.replace('$', '.'); } } else { return
			 * nameWithoutExtension; }
			 */
			return nameWithoutExtension;
		}
		return null;
	}

}
