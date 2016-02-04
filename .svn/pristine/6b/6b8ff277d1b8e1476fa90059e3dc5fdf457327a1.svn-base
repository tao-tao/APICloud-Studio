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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import com.apicloud.commons.model.Config;
import com.apicloud.commons.util.IDEUtil;
import com.apicloud.loader.platforms.android.ADBException;
import com.apicloud.loader.platforms.android.ADBService;
import com.apicloud.loader.platforms.android.AndroidDevice;
import com.apicloud.loader.platforms.ios.IOSDevice;
import com.apicloud.navigator.dialogs.Messages;
import com.apicloud.navigator.dialogs.DeviceNotFoundDialog;
import com.apicloud.navigator.dialogs.RunMobileDialog;
import com.apicloud.navigator.dialogs.SyncApplicationDialog;
import com.apicloud.updatemanager.dialog.CheckLoaderDialog;


public class RunAsAppFormMobile implements IWorkbenchWindowActionDelegate {
	private static final int ALLLOADER = 0;
	private static final int ALOADER = 1;
	private static final int ILOADER = 2;
	private static boolean isHasANDROIDBaseLoader = false;
	private static boolean isHasANDROIDAppLoader = false;
	private static boolean isHasIOSBaseLoader = false;
	private static boolean isHasIOSAppLoader = false;
	private String CUSTOM_ANDROID_BASE;
	private String CuSTOm_IOSROID_BASE;
	private CheckLoaderDialog loaderdialog = null;
	private IStructuredSelection select;
	private Object obj;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void run(IAction action) {
		
		AndroidDevice.connectedDevices.clear();
		List<AndroidDevice> aMobiles = new ArrayList<AndroidDevice>();
		List<IOSDevice>iMobiles=new ArrayList<IOSDevice>();
		try {
			ADBService.getADBService().findDevices();
			
		} catch (ADBException e1) {
			e1.printStackTrace();
		}
		HashMap<String, AndroidDevice> adeviceMap = AndroidDevice.connectedDevices;
		HashMap<String,IOSDevice>ideviceMap=IOSDevice.connectedDevices;
		if (adeviceMap.isEmpty()&&adeviceMap.size()<=0&&ideviceMap.isEmpty()&&ideviceMap.size()<=0) {
			DeviceNotFoundDialog dialog = new DeviceNotFoundDialog(Display
					.getDefault().getActiveShell());
			dialog.open();
			return;
		} else {
			
			Iterator aiterator=adeviceMap.entrySet().iterator();
			while(aiterator.hasNext()){
				Map.Entry<String, AndroidDevice> entry=(Entry<String, AndroidDevice>) aiterator.next();
				aMobiles.add(entry.getValue());
			}

			Iterator  iterator=ideviceMap.entrySet().iterator();
			while(iterator.hasNext()){
				Map.Entry<String, IOSDevice> entry=(Entry<String, IOSDevice>) iterator.next();
				iMobiles.add(entry.getValue());
			}
		
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		projects = FilterProject(projects);
		if (projects.length == 0) {
			MessageDialog.openError(null, Messages.AddFeatureDialog_INFORMATION,
					Messages.CREATEAPP);
			return;

		} else if (projects.length == 1) {
			 obj = projects[0];
			if(validate(getID(obj))){
			final SyncApplicationDialog sad = new SyncApplicationDialog(Display
					.getDefault().getActiveShell(), aMobiles,iMobiles, obj);
			final CountDownLatch threadSignal = new CountDownLatch(
					aMobiles.size()+iMobiles.size());
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
			return;
		}

	}else{
		RunMobileDialog dialog = new RunMobileDialog(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(), projects, aMobiles,iMobiles);
		dialog.open();
		}
		}
	}
	private IProject[] FilterProject(IProject[] projects) {
		List<IProject> list = new ArrayList<IProject>();
		for (IProject p : projects) {
			File config = new File(p.getLocation().toOSString()
					+ File.separator + "config.xml");
			if (config.exists()) {
				list.add(p);
			}
		}
		return list.toArray(new IProject[list.size()]);
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			select = (IStructuredSelection) selection;
		}
	}

	@Override
	public void dispose() {
	}

	@Override
	public void init(IWorkbenchWindow window) {
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
		RunAsAppFormMobile.isHasANDROIDBaseLoader = isHasANDROIDBaseLoader;
	}

	public static boolean isHasANDROIDAppLoader() {
		return isHasANDROIDAppLoader;
	}

	public static void setHasANDROIDAppLoader(boolean isHasANDROIDAppLoader) {
		RunAsAppFormMobile.isHasANDROIDAppLoader = isHasANDROIDAppLoader;
	}

	public static boolean isHasIOSBaseLoader() {
		return isHasIOSBaseLoader;
	}

	public static void setHasIOSBaseLoader(boolean isHasIOSBaseLoader) {
		RunAsAppFormMobile.isHasIOSBaseLoader = isHasIOSBaseLoader;
	}

	public static boolean isHasIOSAppLoader() {
		return isHasIOSAppLoader;
	}

	public static void setHasIOSAppLoader(boolean isHasIOSAppLoader) {
		RunAsAppFormMobile.isHasIOSAppLoader = isHasIOSAppLoader;
	}

	public String getID(Object obj) {
		IProject project = (IProject) obj;
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
	
	public boolean validate(String path){
		Bundle androidbundle = Platform
				.getBundle("com.apicloud.loader.platforms.android");
		Bundle iosbundle = Platform.getBundle("com.apicloud.loader.platforms.ios");
		CUSTOM_ANDROID_BASE = IDEUtil.getInstallPath() + "apploader/"
				+ path + "/load.apk";
		File appaloaderFile = new File(CUSTOM_ANDROID_BASE);
		if (!appaloaderFile.exists()) {
			setHasANDROIDAppLoader(false);
		} else {
			setHasANDROIDAppLoader(true);
		}

		CuSTOm_IOSROID_BASE = IDEUtil.getInstallPath() + "apploader/"
				+ path + "/load.ipa";
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
			if(select!=null){
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
			}else{
				if (hasANDROIDLoader() && hasIOSLoader()) {
					loaderdialog = new CheckLoaderDialog(Display.getCurrent()
							.getActiveShell(), ALLLOADER, obj);
				} else if (hasANDROIDLoader() && (!hasIOSLoader())) {
					loaderdialog = new CheckLoaderDialog(Display.getCurrent()
							.getActiveShell(), ALOADER, obj);
				} else {
					loaderdialog = new CheckLoaderDialog(Display.getCurrent()
							.getActiveShell(), ILOADER, obj);
				}
			}
			loaderdialog.open();
			return false;
		}
		return true;
		}
	}
