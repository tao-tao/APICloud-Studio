/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.makepackage.core;

import java.io.File;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.apicloud.commons.util.FileUtil;


public class PackageJob extends WorkspaceJob {
	private String basePath;
	private int index;
	private String widgetPath;
	private String appId;
	private String appName;
	private String fullScreen;
	private String home;

	public PackageJob(int i, String test, String home, String path, String id, String appName, String fullScreen) {
		super(Messages.PackageJob_CREATE_PACKAGE);
		this.basePath = test;
		this.home = home;
		this.index = i;
		this.widgetPath = path;
		this.appId = id;
		this.appName = appName;
		this.fullScreen = fullScreen;
	}

	@Override
	protected void canceling() {
		super.canceling();
		done(Status.CANCEL_STATUS);
		
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor)
			throws CoreException {
		try {
			monitor.beginTask(Messages.PackageJob_PACKAGING, 40000);
			monitor.worked(1000);
			monitor.worked(1000);
			monitor.worked(1000);
			monitor.worked(1000);
			monitor.worked(1000);
			monitor.worked(1000);
			monitor.worked(1000);
			monitor.worked(1000);
			monitor.worked(1000);
			monitor.worked(1000);
			monitor.worked(5000);
			UzTools.makePackage(index, home, basePath 
					+ "package/", widgetPath, appId, basePath + appId + File.separator, appName, fullScreen);  //$NON-NLS-2$
			monitor.worked(20000);
			FileUtil.deleteDirectory(basePath + appId + File.separator); 
			Thread.sleep(200);
			monitor.worked(2000);
			Thread.sleep(200);
			monitor.worked(3000);
			Thread.sleep(500);
			monitor.worked(3000);
			Thread.sleep(200);
			monitor.worked(2000);
			monitor.done();
		} catch (Exception e) {
			e.printStackTrace();
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}


	
}
