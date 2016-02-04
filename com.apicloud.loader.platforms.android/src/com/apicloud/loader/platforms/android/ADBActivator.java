/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.loader.platforms.android;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import com.apicloud.commons.util.OS;
import com.apicloud.commons.util.UtilActivator;

import java.io.File;
import java.util.logging.Logger;

import org.eclipse.core.runtime.FileLocator;

/**
 * The activator class controls the plug-in life cycle
 */
public class ADBActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.apicloud.loader.platforms.android"; //$NON-NLS-1$

	// The shared instance
	private static ADBActivator plugin;
	
	// The Logger instance
	public static final Logger log = UtilActivator.logger;
	
	public static String ADB_PATH = null;
	private static String ANDROID_BASE = null;
	private static String ANDROID_VERSION_PATH = null;
	
	private static boolean hasBaseLoader=false;
	private static boolean hasAppLoader=false;
	
	/**
	 * The constructor
	 */
	public ADBActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		plugin = this;
	    Bundle bundle = bundleContext.getBundle();
	    

	    if (OS.isWindows()) {
	    	ADB_PATH = new File(FileLocator.toFileURL(bundle.getResource("tools/adb.exe")).getFile()).getAbsolutePath();
	      } else {
		    ADB_PATH = new File(FileLocator.toFileURL(bundle.getResource("tools/adb")).getFile()).getAbsolutePath();
		    String[] cmds = { "chmod", "+x", ADB_PATH };
	        Runtime.getRuntime().exec(cmds);
	      }
	    
	    if(bundle.getResource("base/load.apk")==null) {
	    	ANDROID_BASE = new File(FileLocator.toFileURL(bundle.getResource("base")).getFile()).getAbsolutePath()+"//load.apk";
	    	setHasBaseLoader(false);
	    } else {
		    File file = new File(FileLocator.toFileURL(bundle.getResource("base/load.apk")).getFile());
			ANDROID_BASE = file.getAbsolutePath();
	    	setHasBaseLoader(true);
	    }
	    ANDROID_VERSION_PATH = new File(FileLocator.toFileURL(bundle.getResource("base/version.txt")).getFile()).getAbsolutePath();
	    log.info("ADB_PATH:" + ADB_PATH);
	    log.info("ANDROID_BASE:" + ANDROID_BASE);
	    
		try {
			ADBService.getADBService().start();
		} catch (ADBException adbEx) {
			UtilActivator.logger.info("start adb service failed.");
		}
	    
	}
	
	public static boolean isHasBaseLoader() {
		return hasBaseLoader;
	}
	public static void setHasBaseLoader(boolean hasBaseLoader) {
		ADBActivator.hasBaseLoader = hasBaseLoader;
	}	

	public static boolean isHasAppLoader() {
		return hasAppLoader;
	}
	public static void setHasAppLoader(boolean hasAppLoader) {
		ADBActivator.hasAppLoader = hasAppLoader;
	}	
	
	public static String getUpdateBasePath() {
		return ANDROID_BASE;
	}
	
	public static String getUpdateVersion() {
		return ANDROID_VERSION_PATH;
	}
	
	public static String getBasePath() {
		return ANDROID_BASE;
	}
	
	public static String getADBPath() {
		return ADB_PATH;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		ADBService.getADBService().kill();
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ADBActivator getDefault() {
		return plugin;
	}
}
