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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import com.apicloud.commons.model.Config;
import com.apicloud.commons.model.Preference;
import com.apicloud.commons.util.IDEUtil;
import com.apicloud.navigator.dialogs.Messages;
import com.apicloud.navigator.dialogs.PackageDialog;
import com.apicloud.updatemanager.dialog.CheckLoaderDialog;

public class CreateMoblePackageAction implements IObjectActionDelegate {
	private IStructuredSelection select;
	private String path;
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
		Object obj = select.getFirstElement();
		CheckLoaderDialog loaderdialog = null;
		Bundle androidbundle = Platform
				.getBundle("com.apicloud.loader.platforms.android");
		Bundle iosbundle=Platform.getBundle("com.apicloud.loader.platforms.ios");
		
		 CUSTOM_ANDROID_BASE = IDEUtil.getInstallPath() + "apploader/"+getID()+"/load.apk";
		    File appaloaderFile=new File(CUSTOM_ANDROID_BASE);
		    if(!appaloaderFile.exists()){
		    	setHasANDROIDAppLoader(false);
		    }else{
		    setHasANDROIDAppLoader(true);
		    }
		    
		    CuSTOm_IOSROID_BASE = IDEUtil.getInstallPath() + "apploader/"+getID()+"/load.ipa";
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
		} 
		if(iosbundle.getResource("base/load.ipa")==null){
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
		
		try {
			IResource resource = (IResource) obj;
			path = resource.getLocation().toOSString();
			try {
				File fileToRead = new File(path + File.separator + "config.xml");
				Config config = Config.loadXml(new FileInputStream(fileToRead));
				String fullScreen = "false";
				for(Preference preference : config.getPreferences()) {
					if(preference.getName().equals("fullScreen")) {
						fullScreen = preference.getValue();
					}
				}
				PackageDialog dialog = new PackageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), config.getName(), config.getId(), path, config.getName(), fullScreen);
				dialog.open();
			} catch (Exception e) {
				MessageDialog.openError(null, Messages.PATHERROR, 
						Messages.CONFIGINFO);
				e.printStackTrace();
			}
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
	}
	
	public String getID(){
		IProject project = (IProject) select.getFirstElement();
		path =project.getLocation().toString();
		String id ="";
		File fileToRead = new File(path + File.separator + "config.xml");
		try {
			Config config = Config.loadXml(new FileInputStream(fileToRead));
			 id = config.getId();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return id;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			select = (IStructuredSelection) selection;
		}
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		
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
		CreateMoblePackageAction.isHasANDROIDBaseLoader = isHasANDROIDBaseLoader;
	}

	public static boolean isHasANDROIDAppLoader() {
		return isHasANDROIDAppLoader;
	}

	public static void setHasANDROIDAppLoader(boolean isHasANDROIDAppLoader) {
		CreateMoblePackageAction.isHasANDROIDAppLoader = isHasANDROIDAppLoader;
	}

	public static boolean isHasIOSBaseLoader() {
		return isHasIOSBaseLoader;
	}

	public static void setHasIOSBaseLoader(boolean isHasIOSBaseLoader) {
		CreateMoblePackageAction.isHasIOSBaseLoader = isHasIOSBaseLoader;
	}

	public static boolean isHasIOSAppLoader() {
		return isHasIOSAppLoader;
	}

	public static void setHasIOSAppLoader(boolean isHasIOSAppLoader) {
		CreateMoblePackageAction.isHasIOSAppLoader = isHasIOSAppLoader;
	}
}
