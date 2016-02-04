/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.ui.listener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import com.apicloud.commons.util.IDEUtil;
import com.apicloud.makepackage.ui.PackageAppItemDialog;
import com.apicloud.navigator.dialogs.Messages;
import com.apicloud.updatemanager.dialog.CheckLoaderDialog;

public class PackageAppItem implements IWorkbenchWindowActionDelegate {
	
	private static final int ALLLOADER = 0;
	private static final int ALOADER = 1;
	private static final int ILOADER = 2;
	
	private static boolean isHasANDROIDBaseLoader = false;
	private static boolean isHasANDROIDAppLoader = false;
	private static boolean isHasIOSBaseLoader = false;
	private static boolean isHasIOSAppLoader = false;
	private String CUSTOM_ANDROID_BASE;
	private String CuSTOm_IOSROID_BASE;
	
	private IStructuredSelection select;
	@Override
	public void run(IAction action) {
		
		CheckLoaderDialog loaderdialog = null;
		
		Bundle androidbundle = Platform
				.getBundle("com.apicloud.loader.platforms.android");
		Bundle iosbundle=Platform.getBundle("com.apicloud.loader.platforms.ios");
		
		 CUSTOM_ANDROID_BASE = IDEUtil.getInstallPath() + "apploader/load.apk";
		    File appaloaderFile=new File(CUSTOM_ANDROID_BASE);
		    if(!appaloaderFile.exists()){
		    	setHasANDROIDAppLoader(false);
		    }else{
		    setHasANDROIDAppLoader(true);
		    }
		    
		    CuSTOm_IOSROID_BASE = IDEUtil.getInstallPath() + "apploader/load.ipa";
		    File appiloaderFile=new File(CuSTOm_IOSROID_BASE);
		    if(!appiloaderFile.exists()){
		    	setHasIOSAppLoader(false);
		    }else{
		    	setHasIOSAppLoader(true);
		    }
		
		if (androidbundle.getResource("base/load.apk") == null) {
			setHasANDROIDBaseLoader(false);
		}else{
			setHasANDROIDBaseLoader(true);
		} if(iosbundle.getResource("base/load.ipa")==null){
			setHasIOSBaseLoader(false);
		}else{
			setHasIOSBaseLoader(true);
		}

		if (hasANDROIDLoader() || hasIOSLoader()) {

			if (hasANDROIDLoader() && hasIOSLoader()) {
				loaderdialog = new CheckLoaderDialog(Display.getCurrent()
						.getActiveShell(), ALLLOADER,select);
			} else if (hasANDROIDLoader() && (!hasIOSLoader())) {
				loaderdialog = new CheckLoaderDialog(Display.getCurrent()
						.getActiveShell(), ALOADER,select);
			} else {
				loaderdialog = new CheckLoaderDialog(Display.getCurrent()
						.getActiveShell(), ILOADER,select);
			}

			loaderdialog.open();
			return;
		}

		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		projects = FilterProject(projects);
		if(projects.length == 0) {
			MessageDialog.openError(null, Messages.AddFeatureDialog_INFORMATION, 
					Messages.CREATEAPP);
			return;
		}
		PackageAppItemDialog dialog = new PackageAppItemDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), projects);
		dialog.open();
	}

	private IProject[] FilterProject(IProject[] projects) {
		List<IProject> list = new ArrayList<IProject>();
		for(IProject p : projects) {
			File config = new File(p.getLocation().toOSString()+ File.separator + "config.xml");
			if(config.exists()) {
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

	public static boolean hasANDROIDLoader(){
		if((!isHasANDROIDBaseLoader()) && (!isHasANDROIDAppLoader())){
			return true;
		}
		return false;
	}
	
	public static boolean hasIOSLoader(){
		if((!isHasIOSBaseLoader())&&(!isHasIOSAppLoader())){
			return true;
		}
		return false;
	}

	public static boolean isHasANDROIDBaseLoader() {
		return isHasANDROIDBaseLoader;
	}

	public static void setHasANDROIDBaseLoader(boolean isHasANDROIDBaseLoader) {
		PackageAppItem.isHasANDROIDBaseLoader = isHasANDROIDBaseLoader;
	}

	public static boolean isHasANDROIDAppLoader() {
		return isHasANDROIDAppLoader;
	}

	public static void setHasANDROIDAppLoader(boolean isHasANDROIDAppLoader) {
		PackageAppItem.isHasANDROIDAppLoader = isHasANDROIDAppLoader;
	}

	public static boolean isHasIOSBaseLoader() {
		return isHasIOSBaseLoader;
	}

	public static void setHasIOSBaseLoader(boolean isHasIOSBaseLoader) {
		PackageAppItem.isHasIOSBaseLoader = isHasIOSBaseLoader;
	}

	public static boolean isHasIOSAppLoader() {
		return isHasIOSAppLoader;
	}

	public static void setHasIOSAppLoader(boolean isHasIOSAppLoader) {
		PackageAppItem.isHasIOSAppLoader = isHasIOSAppLoader;
	}
}
