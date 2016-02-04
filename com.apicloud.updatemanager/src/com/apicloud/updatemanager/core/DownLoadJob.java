/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.updatemanager.core;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.json.JSONException;

import com.apicloud.updatemanager.Messages;

public class DownLoadJob extends WorkspaceJob {
	
	private int id;
	public DownLoadJob(String name,int id) {
		super(name);
		this.id=id;
		setName(Messages.DOWNLOADOFFLOADER);
	}
	
	@Override
	protected void canceling() {
		super.canceling();
		done(Status.CANCEL_STATUS);
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		boolean ret = false;
		CheckUpateManager manager=CheckUpateManager.getInstance();
		try {
			ret = manager.updateLoder(id,monitor);
		} catch(JSONException e){
			e.printStackTrace();
		}
		if (ret == false) {
			return Status.CANCEL_STATUS;
		}
		else {
			return Status.OK_STATUS;
		}
	}
}
