/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.updatemanager;


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.apicloud.networkservice.NetWorkService;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "com.apicloud.updatemanager"; //$NON-NLS-1$
	// The shared instance
	private static Activator plugin;
	
	private static boolean isConnection = false;
	
	public static NetWorkService network_instance;
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		network_instance=NetWorkService.getInstance();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	public static boolean isConnection() {
		return isConnection;
	}
	static public void setConnection(boolean isConnection) {
		Activator.isConnection = isConnection;
	}
	
	public static ImageDescriptor getImageDescriptor(String imagePath) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, imagePath);
	}
	
	
	
}
