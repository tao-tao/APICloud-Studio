/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.commons.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class UtilActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.apicloud.commons.util"; //$NON-NLS-1$
	
	public static Logger logger=null;
	
	// The shared instance
	private static UtilActivator plugin;	

	/**
	 * The constructor
	 */
	public UtilActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		plugin = this;
		logger=customLog();
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
	}
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static UtilActivator getDefault() {
		return plugin;
	}	
	
	public Logger customLog(){
		Logger log=null;
		try {
			String path=IDEUtil.getInstallPath()+"logs";
			File file=new File(path);
			file.mkdir();
			log = Logger.getLogger("APICloud");
			log.setLevel(Level.INFO);
			FileHandler fileHandler = new FileHandler(path+"/ide.log");
			fileHandler.setFormatter(new XMLFormatter());
			log.addHandler(fileHandler);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return log;
	}

}
