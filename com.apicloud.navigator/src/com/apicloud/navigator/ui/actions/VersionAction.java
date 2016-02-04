/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.apicloud.commons.model.IDEUpdateModel;
import com.apicloud.commons.util.FileUtil;

public class VersionAction implements IWorkbenchWindowActionDelegate {
	IWorkbenchWindow windows;
	@Override
	public void run(IAction action) {
		MessageDialog.openInformation(windows.getShell(), "Version", 
						 "APICloud Studio Version " + IDEUpdateModel.VERSION+ "\n"
						+ "Android_AppLoader Version " + FileUtil.readVersion(com.apicloud.loader.platforms.android.ADBActivator.getUpdateVersion()) + "\n"
						+ "IOS_AppLoader Version " + FileUtil.readVersion(com.apicloud.loader.platforms.ios.ANBActivator.getUpdateVersion()));
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.windows = window;
	}
}
