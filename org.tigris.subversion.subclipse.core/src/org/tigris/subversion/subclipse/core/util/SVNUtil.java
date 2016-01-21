package org.tigris.subversion.subclipse.core.util;

import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.TeamException;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.repo.SVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;




public class SVNUtil {
	
	public static void addSVNToView(String url) {
//		添加svn信息到svn视图
		Properties properties = new Properties();
		properties.setProperty("url", url);
		final ISVNRepositoryLocation[] root = new ISVNRepositoryLocation[1];
		SVNProviderPlugin provider = SVNProviderPlugin.getPlugin();
		if(provider == null) {
		}
		try {
			 if(!provider.getRepositories().exactMatchExists(url)) {
				 root[0] = provider.getRepositories().createRepository(properties);
				 provider.getRepositories().addOrUpdateRepository(root[0]);
			 }
		} catch (TeamException e) {
			SVNProviderPlugin.log(new Status(IStatus.OK, SVNProviderPlugin.ID, 0, e.getMessage(), null));
			e.printStackTrace();
		}
	}

//	同步创建好的工程到svn
	public static void syncProjectToSVN(IProject project, String userName, String passWord, String url) throws TeamException{
		ISVNRepositoryLocation location = null;
		boolean createDirectory = true;
		 try {
			location = SVNRepositoryLocation.fromString(url);
			SVNWorkspaceRoot.shareProject(location, project, "" + project.getName(), "first import", createDirectory, new NullProgressMonitor());
			project.refreshLocal(IProject.DEPTH_INFINITE, new NullProgressMonitor());
		} catch (org.tigris.subversion.subclipse.core.SVNException e) {
			e.printStackTrace();
		}catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
}
