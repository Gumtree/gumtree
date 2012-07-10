package org.gumtree.util.eclipse;

import junit.framework.Assert;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.gumtree.core.CoreProperties;
import org.junit.Test;

public class WorkspaceUtilsTest {

	@Test
	public void testCreateWorkspaceProject() throws CoreException {
		// Start from scratch
		clearWorkspaceProject();
		
		// Create new workspace project
		IProject worksapceProject = WorkspaceUtils.createWorkspaceProject();
		Assert.assertEquals(CoreProperties.WORKSPACE_PROJECT.getValue(), worksapceProject.getName());
		Assert.assertTrue(worksapceProject.exists());
	}
	
	@Test
	public void testRepeatativeWorkspaceProjectCreation() throws CoreException {
		// Start from scratch
		clearWorkspaceProject();
		
		// Create new workspace project
		IProject worksapceProject = WorkspaceUtils.createWorkspaceProject();
		Assert.assertEquals(CoreProperties.WORKSPACE_PROJECT.getValue(), worksapceProject.getName());
		Assert.assertTrue(worksapceProject.isAccessible());
		
		// Repeat creation
		worksapceProject = WorkspaceUtils.createWorkspaceProject();
		worksapceProject = WorkspaceUtils.createWorkspaceProject();
		worksapceProject = WorkspaceUtils.createWorkspaceProject();
		worksapceProject = WorkspaceUtils.createWorkspaceProject();
		worksapceProject = WorkspaceUtils.createWorkspaceProject();
		
		Assert.assertEquals(CoreProperties.WORKSPACE_PROJECT.getValue(), worksapceProject.getName());
		Assert.assertTrue(worksapceProject.isAccessible());
	}
	
	@Test
	public void testCreateSimpleWorkspaceFolderFromEmptyProject() throws CoreException {
		// Start from scratch
		clearWorkspaceProject();
		
		IFolder folder = WorkspaceUtils.createWorkspaceFolder("abc");
		Assert.assertEquals("abc", folder.getName());
		Assert.assertTrue(folder.exists());
	}
	
	@Test
	public void testCreateSimpleWorkspaceFolderFromExistingProject() throws CoreException {
		// Start from scratch
		clearWorkspaceProject();
		
		// Create project
		WorkspaceUtils.createWorkspaceProject();
		
		IFolder folder = WorkspaceUtils.createWorkspaceFolder("abc");
		Assert.assertEquals("abc", folder.getName());
		Assert.assertTrue(folder.exists());
	}
	
	@Test
	public void testRepeatativeWorkspaceFolderCreation() throws CoreException {
		// Start from scratch
		clearWorkspaceProject();
		
		IFolder folder = WorkspaceUtils.createWorkspaceFolder("abc");
		Assert.assertEquals("abc", folder.getName());
		Assert.assertTrue(folder.exists());
		
		// Repeat creation
		folder = WorkspaceUtils.createWorkspaceFolder("abc");
		folder = WorkspaceUtils.createWorkspaceFolder("abc");
		folder = WorkspaceUtils.createWorkspaceFolder("abc");
		folder = WorkspaceUtils.createWorkspaceFolder("abc");
		folder = WorkspaceUtils.createWorkspaceFolder("abc");
		
		Assert.assertEquals("abc", folder.getName());
		Assert.assertTrue(folder.exists());
	}
	
	@Test
	public void testCreateHeirarchicalWorkspaceFolder() throws CoreException {
		// Start from scratch
		clearWorkspaceProject();
		
		IFolder folder = WorkspaceUtils.createWorkspaceFolder("a/b/c");
		Assert.assertEquals("c", folder.getName());
		Assert.assertTrue(folder.exists());
		
		// Start again
		clearWorkspaceProject();
		
		folder = WorkspaceUtils.createWorkspaceFolder("/a/b/c");
		Assert.assertEquals("c", folder.getName());
		Assert.assertTrue(folder.exists());
		
		// Create another folder on the same tree
		folder = WorkspaceUtils.createWorkspaceFolder("a/b/d");
		Assert.assertEquals("d", folder.getName());
		Assert.assertTrue(folder.exists());
	}
	
	@Test
	public void testGetFolder() throws CoreException {
		// Start from scratch
		clearWorkspaceProject();
		WorkspaceUtils.createWorkspaceFolder("a/b/c");
		
		IFolder folder = WorkspaceUtils.getFolder("a/b/c");
		Assert.assertEquals("c", folder.getName());
		Assert.assertTrue(folder.exists());
		
		folder = WorkspaceUtils.getFolder("/a/b/c");
		Assert.assertEquals("c", folder.getName());
		Assert.assertTrue(folder.exists());
		
		folder = WorkspaceUtils.getFolder("a/b");
		Assert.assertEquals("b", folder.getName());
		Assert.assertTrue(folder.exists());
		
		folder = WorkspaceUtils.getFolder("a/x");
		Assert.assertEquals("x", folder.getName());
		Assert.assertFalse(folder.exists());
	}
	
	@Test
	public void testRefreshFolder() throws CoreException {
		// Start from current
		IFolder folder = WorkspaceUtils.createWorkspaceFolder("a/b/c");
		Assert.assertEquals("c", folder.getName());
		Assert.assertTrue(folder.exists());
		
		// Refresh (test if it throws any exception)
		WorkspaceUtils.refreshFolder(folder);
	}
	
	private void clearWorkspaceProject() throws CoreException {
		// Start from clean workspace
		IProject existingProject = ResourcesPlugin.getWorkspace().getRoot().getProject(CoreProperties.WORKSPACE_PROJECT.getValue());
		if (existingProject.exists()) {
			existingProject.delete(true, new NullProgressMonitor());
		}
		Assert.assertFalse(existingProject.exists());
	}
	
}
