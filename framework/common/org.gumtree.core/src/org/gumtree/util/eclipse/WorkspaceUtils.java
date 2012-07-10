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

package org.gumtree.util.eclipse;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.gumtree.core.CoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WorkspaceUtils provides support to manipulate the workspace for GumTree data
 * storage.
 * 
 * @since 1.5
 * 
 */
public final class WorkspaceUtils {

	private static Logger logger = LoggerFactory
			.getLogger(WorkspaceUtils.class);

	private static Lock lock = new ReentrantLock();

	/**
	 * Creates and opens a project in the workspace for storing GumTree related
	 * data. If project was created or opened, it has no effect.
	 * 
	 * @return true if new project is created, false otherwise
	 * @throws CoreException
	 *             when project operation is failed
	 */
	public static IProject createWorkspaceProject() throws CoreException {
		IProject project = null;
		CoreException exception = null;
		try {
			lock.lock();
			project = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(CoreProperties.WORKSPACE_PROJECT.getValue());
			if (!project.exists()) {
				project.create(new NullProgressMonitor());
			}
			if (!project.isOpen()) {
				project.open(new NullProgressMonitor());
			}
		} catch (CoreException e) {
			logger.error("Failed to create workspace project "
					+ CoreProperties.WORKSPACE_PROJECT.getValue(), e);
			exception = e;
		} finally {
			lock.unlock();
			if (exception != null) {
				throw exception;
			}
		}
		return project;
	}

	/**
	 * Creates a folder under the GumTree workspace project. It has no effect if
	 * the folder has already existed. The supplied path can be heirarchical,
	 * for example, both "/a/b/c" and "a/b/c" will create a three level depth
	 * folder structure.
	 * 
	 * @param path
	 * @return the end folder created
	 * @throws CoreException
	 */
	public static IFolder createWorkspaceFolder(String path)
			throws CoreException {
		IFolder folder = null;
		CoreException exception = null;
		try {
			lock.lock();
			IContainer container = createWorkspaceProject();
			// Crop starting forward slash
			if (path.startsWith("/")) {
				path = path.substring(1);
			}
			// Create i
			for (String subpath : path.split("/")) {
				subpath = subpath.trim();
				container = folder = container.getFolder(new Path(subpath));
				if (!folder.exists()) {
					folder.create(IResource.NONE, true,
							new NullProgressMonitor());
				}
			}
		} catch (CoreException e) {
			logger.error("Failed to create workspace folder " + path, e);
			exception = e;
		} finally {
			lock.unlock();
			if (exception != null) {
				throw exception;
			}
		}
		return folder;
	}

	public static IFolder getFolder(String path) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(CoreProperties.WORKSPACE_PROJECT.getValue());
		// Crop starting forward slash
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		return project.getFolder(path);
	}
	
	public static void refreshFolder(IFolder folder) throws CoreException {
		folder.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
	}
	
	private WorkspaceUtils() {
		super();
	}

}