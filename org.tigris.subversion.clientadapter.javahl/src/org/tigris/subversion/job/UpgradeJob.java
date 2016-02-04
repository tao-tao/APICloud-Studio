package org.tigris.subversion.job;

import java.io.File;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.javahl.AbstractJhlClientAdapter;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapter;


public class UpgradeJob extends WorkspaceJob{
	private File path;

	public UpgradeJob(String name,File path) {
		super(name);
		setName("upgrade...");
		this.path=path;
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor)
			throws CoreException {
		
		

		try {
		monitor.beginTask("begin upgrade...", 40000);
	/*	monitor.worked(1000);
		monitor.worked(1000);
		monitor.worked(1000);
		monitor.worked(1000);
		monitor.worked(1000);
		monitor.worked(1000);
		monitor.worked(1000);*/
		AbstractJhlClientAdapter adapter=new JhlClientAdapter();
		
		adapter.upgrade(path);
		
		monitor.worked(1000);
		monitor.worked(1000);
		monitor.worked(1000);
		monitor.worked(5000);
		monitor.worked(20000);
		Thread.sleep(200);
		monitor.worked(2000);
		Thread.sleep(200);
		monitor.worked(3000);
			Thread.sleep(500);
		monitor.worked(3000);
		Thread.sleep(200);
		monitor.worked(2000);
		monitor.done();
	
		
		} catch (InterruptedException e) {
			e.printStackTrace();
			return Status.CANCEL_STATUS;
		} catch (SVNClientException e) {
			e.printStackTrace();
			return Status.CANCEL_STATUS;
		}
		
		return Status.OK_STATUS;
	}

}
