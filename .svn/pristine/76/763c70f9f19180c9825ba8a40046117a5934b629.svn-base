/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.actions;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.apicloud.loader.platforms.android.AndroidDevice;
import com.apicloud.loader.platforms.ios.IOSDevice;
import com.apicloud.navigator.dialogs.DeviceNotFoundDialog;
import com.apicloud.navigator.dialogs.SyncApplicationDialog;
import com.apicloud.navigator.handlers.RunAsAppFormDeviceHandler;
import com.apicloud.navigator.ui.actions.RunAsAppFormMobile;

public class RunAsAppFormMobileCommand extends AbstractHandler  {
	private Object obj = null;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		RunAsAppFormDeviceHandler deviceHandler=RunAsAppFormDeviceHandler.getInstance();
		List<AndroidDevice> aMobiles =deviceHandler.getAndroidDevice();
		List<IOSDevice> iMobiles =deviceHandler.getIosDevice();
		if(aMobiles.size()<=0&&iMobiles.size()<=0){
			DeviceNotFoundDialog dialog = new DeviceNotFoundDialog(Display
					.getDefault().getActiveShell());
			dialog.open();
			return null;
		}

		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null) {
			IWorkbenchPage page = (IWorkbenchPage) activeWorkbenchWindow.getActivePage();
			if (page != null) {
				IEditorPart part = page.getActiveEditor();
				if (part != null) {
					IEditorInput input = part.getEditorInput();
					if (input instanceof IFileEditorInput) {
						IFileEditorInput fileInput = (IFileEditorInput) input;
						obj = fileInput.getFile().getProject();
						final SyncApplicationDialog sad = new SyncApplicationDialog(
								null, aMobiles, iMobiles, obj);
						final CountDownLatch threadSignal = new CountDownLatch(
								aMobiles.size() + iMobiles.size());
						sad.open();
						sad.run(threadSignal);
						Job job = new WorkspaceJob("") {
							@Override
							public IStatus runInWorkspace(
									IProgressMonitor monitor)
									throws CoreException {
								try {
									threadSignal.await();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								sad.finish();
								return Status.OK_STATUS;
							}
						};
						job.schedule();
						return null;
					}
				}
			}
		}
		RunAsAppFormMobile action = new RunAsAppFormMobile();
		action.run(null);
		return null;
	}
}
