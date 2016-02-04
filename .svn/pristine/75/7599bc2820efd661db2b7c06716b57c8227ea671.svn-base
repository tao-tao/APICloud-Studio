/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.resource;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

import com.apicloud.commons.model.CustomLoaderModel;

public class Activator extends AbstractUIPlugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "com.apicloud.resource"; //$NON-NLS-1$

	private static BundleContext context;
	private static Activator plugin;
	
	private static ImageRegistry imageRegistry;
	private static Map<String, CustomLoaderModel> loaderMap = new HashMap<String, CustomLoaderModel>();

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		super.start(context);
		plugin = this;
		imageRegistry = plugin.getImageRegistry();
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		imageRegistry = null;
		plugin = null;
		Activator.context = null;
		super.stop(bundleContext);
	}
	public static Image getImage(String key) {
		Image image = imageRegistry.get(key);
		if (image == null) {
			imageRegistry.put(key, getImageDescriptor(key));
			image = imageRegistry.get(key);
		}
		return image;
	}

	public static ImageDescriptor getImageDescriptor(String imagePath) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, imagePath);
	}

	public static Map<String, CustomLoaderModel> getLoaderMap() {
		return loaderMap;
	}
	
}
