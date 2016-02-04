/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.commons.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.apicloud.commons.util.IDEUtil;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.apicloud.commons.model"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	private static Map<String, CustomLoaderModel> loaderMap = new HashMap<String, CustomLoaderModel>();
	
	/**
	 * The constructor
	 */
	public Activator() {
	}
	
	public static void initMap(Properties p) {
		Iterator<Entry<Object, Object>> it = p.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Object, Object> entry = it.next();
			Object key = entry.getKey();
			Object value = entry.getValue();
			CustomLoaderModel model = new CustomLoaderModel();
			String pkgandversion = (String) value;
			String apackagename = pkgandversion.substring(0,
					pkgandversion.indexOf("|"));
			String ipackagename = pkgandversion.substring(
					pkgandversion.indexOf("|") + 1, pkgandversion.indexOf("&"));
			String version = pkgandversion.substring(
					pkgandversion.indexOf("&") + 1, pkgandversion.length());
			model.setApackagename(apackagename);
			model.setIpackagename(ipackagename);

			model.setVersion(version);
			loaderMap.put((String) key, model);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		Properties p = getAppLoaderProperties();
		initMap(p);
	}
	
	public static Properties getAppLoaderProperties() {
		Properties p = new Properties();
		FileReader reader = null;
		try {
			File file = getLoaderFile();
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
	
	public static File getLoaderFile() {
		String folderpath=IDEUtil.getInstallPath()
				+ "/apploader";
		File folder=new File(folderpath);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		File file=new File(folderpath+"/apploader.properties");
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}
	
	public static void storeloader(Properties p) {
		OutputStream out = null;
		try {
			out = new FileOutputStream(getLoaderFile());
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
	
	public static Map<String, CustomLoaderModel> getLoaderMap() {
		return loaderMap;
	}

	public static void setLoaderMap(Map<String, CustomLoaderModel> loaderMap) {
		Activator.loaderMap = loaderMap;
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
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
