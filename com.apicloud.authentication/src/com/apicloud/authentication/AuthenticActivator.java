/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.authentication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.apicloud.networkservice.NetWorkService;

/**
 * The activator class controls the plug-in life cycle
 */
public class AuthenticActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.apicloud.authentication"; //$NON-NLS-1$

	// The shared instance
	private static AuthenticActivator plugin;

	private static boolean isConnection = true;

	private static ImageRegistry imageRegistry;

	public static NetWorkService networkInstance = null;
	

	public static boolean isConnection() {
		return isConnection;
	}

	static public void setConnection(boolean isConnection) {
		AuthenticActivator.isConnection = isConnection;
	}

	/**
	 * The constructor
	 */
	public AuthenticActivator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		imageRegistry = plugin.getImageRegistry();
		networkInstance = NetWorkService.getInstance();
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
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
	public static AuthenticActivator getDefault() {
		return plugin;
	}

	public static void loadProperties() {
		org.tigris.subversion.subclipse.ui.SVNUIPlugin.p = getProperties();
	}

	public static Properties getProperties() {
		Properties p = new Properties();
		FileReader reader = null;
		try {
			File file = getFile();
			if (file.exists()) {
				reader = new FileReader(file);
				p.load(reader);

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return p;
	}

	public static void store(Properties p) {
		OutputStream out = null;
		try {
			out = new FileOutputStream(getFile());
			p.store(out, null);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

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

	public static File getFile() {
		return plugin.getStateLocation().append("userInfo.properties").toFile();
	}

	public static File getFeatureFile() {
		return plugin.getStateLocation().append("featureInfo.xml").toFile();
	}
	
}
