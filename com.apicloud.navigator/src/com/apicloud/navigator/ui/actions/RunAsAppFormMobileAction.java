/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.ui.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.osgi.framework.Bundle;

import com.apicloud.commons.model.Config;
import com.apicloud.loader.platforms.android.AndroidDevice;
import com.apicloud.loader.platforms.ios.ANBActivator;
import com.apicloud.loader.platforms.ios.IOSDevice;
import com.apicloud.updatemanager.dialog.CheckLoaderDialog;
import com.apicloud.navigator.Activator;
import com.apicloud.navigator.dialogs.DeviceNotFoundDialog;
import com.apicloud.navigator.dialogs.SyncApplicationDialog;
import com.apicloud.navigator.handlers.RunAsAppFormDeviceHandler;
import com.apicloud.commons.util.IDEUtil;

public class RunAsAppFormMobileAction implements IObjectActionDelegate {
	private IStructuredSelection select;
	private static final int ALLLOADER = 0;
	private static final int ALOADER = 1;
	private static final int ILOADER = 2;
	private static boolean isHasANDROIDBaseLoader = false;
	private static boolean isHasANDROIDAppLoader = false;
	private static boolean isHasIOSBaseLoader = false;
	private static boolean isHasIOSAppLoader = false;
	private String CUSTOM_ANDROID_BASE;
	private String CuSTOm_IOSROID_BASE;

	public void run(IAction action) {
		CheckLoaderDialog loaderdialog = null;
		RunAsAppFormDeviceHandler deviceHandler=RunAsAppFormDeviceHandler.getInstance();
		List<AndroidDevice> aMobiles =deviceHandler.getAndroidDevice();
		List<IOSDevice> iMobiles =deviceHandler.getIosDevice();
		Activator
		.getDefault()
		.getLog()
		.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, 
				"when sync,  found ios device num = " + iMobiles.size(), null));
		if(aMobiles.size()<=0&&iMobiles.size()<=0){
			DeviceNotFoundDialog dialog = new DeviceNotFoundDialog(Display
					.getDefault().getActiveShell());
			dialog.open();
			return;
		}

		Bundle androidbundle = Platform
				.getBundle("com.apicloud.loader.platforms.android");
		Bundle iosbundle = Platform.getBundle("com.apicloud.loader.platforms.ios");

		CUSTOM_ANDROID_BASE = IDEUtil.getInstallPath() + "apploader/"
				+ getID() + "/load.apk";
		File appaloaderFile = new File(CUSTOM_ANDROID_BASE);
		if (!appaloaderFile.exists()) {
			setHasANDROIDAppLoader(false);
		} else {
			setHasANDROIDAppLoader(true);
		}

		CuSTOm_IOSROID_BASE = IDEUtil.getInstallPath() + "apploader/"
				+ getID() + "/load.ipa";
		File appiloaderFile = new File(CuSTOm_IOSROID_BASE);
		if (!appiloaderFile.exists()) {
			setHasIOSAppLoader(false);
		} else {
			setHasIOSAppLoader(true);
		}

		if (androidbundle.getResource("base/load.apk") == null) {
			setHasANDROIDBaseLoader(false);
		} else {
			setHasANDROIDBaseLoader(true);
		}
		if (iosbundle.getResource("base/load.ipa") == null) {
			setHasIOSBaseLoader(false);
		} else {
			setHasIOSBaseLoader(true);
		}

		if (hasANDROIDLoader() || hasIOSLoader()) {
			if (hasANDROIDLoader() && hasIOSLoader()) {
				loaderdialog = new CheckLoaderDialog(Display.getCurrent()
						.getActiveShell(), ALLLOADER, select);
			} else if (hasANDROIDLoader() && (!hasIOSLoader())) {
				loaderdialog = new CheckLoaderDialog(Display.getCurrent()
						.getActiveShell(), ALOADER, select);
			} else {
				loaderdialog = new CheckLoaderDialog(Display.getCurrent()
						.getActiveShell(), ILOADER, select);
			}
			loaderdialog.open();
			return;
		}

		Object obj = select.getFirstElement();

		Shell shell = Display.getDefault().getActiveShell();
		final SyncApplicationDialog sad = new SyncApplicationDialog(shell,
				aMobiles, iMobiles, obj);
		final CountDownLatch threadSignal = new CountDownLatch(aMobiles.size()
				+ iMobiles.size());
		sad.open();
		sad.run(threadSignal);
		Job job = new WorkspaceJob("") {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor)
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

	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			select = (IStructuredSelection) selection;
		}
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public static boolean hasANDROIDLoader() {
		if ((!isHasANDROIDBaseLoader()) && (!isHasANDROIDAppLoader())) {
			return true;
		}
		return false;
	}

	public static boolean hasIOSLoader() {
		if ((!isHasIOSBaseLoader()) && (!isHasIOSAppLoader())) {
			return true;
		}
		return false;
	}

	public static boolean isHasANDROIDBaseLoader() {
		return isHasANDROIDBaseLoader;
	}

	public static void setHasANDROIDBaseLoader(boolean isHasANDROIDBaseLoader) {
		RunAsAppFormMobileAction.isHasANDROIDBaseLoader = isHasANDROIDBaseLoader;
	}

	public static boolean isHasANDROIDAppLoader() {
		return isHasANDROIDAppLoader;
	}

	public static void setHasANDROIDAppLoader(boolean isHasANDROIDAppLoader) {
		RunAsAppFormMobileAction.isHasANDROIDAppLoader = isHasANDROIDAppLoader;
	}

	public static boolean isHasIOSBaseLoader() {
		return isHasIOSBaseLoader;
	}

	public static void setHasIOSBaseLoader(boolean isHasIOSBaseLoader) {
		RunAsAppFormMobileAction.isHasIOSBaseLoader = isHasIOSBaseLoader;
	}

	public static boolean isHasIOSAppLoader() {
		return isHasIOSAppLoader;
	}

	public static void setHasIOSAppLoader(boolean isHasIOSAppLoader) {
		RunAsAppFormMobileAction.isHasIOSAppLoader = isHasIOSAppLoader;
	}

	public String getID() {
		IProject project = (IProject) select.getFirstElement();
		String path = project.getLocation().toString();
		String id = "";
		File fileToRead = new File(path + File.separator + "config.xml");
		try {
			Config config = Config.loadXml(new FileInputStream(fileToRead));
			id = config.getId();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return id;
	}

}
