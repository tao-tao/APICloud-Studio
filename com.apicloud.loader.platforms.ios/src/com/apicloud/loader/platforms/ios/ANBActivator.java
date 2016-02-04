/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.apicloud.loader.platforms.ios;

import java.io.File;
import java.util.Random;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import com.apicloud.loader.platforms.ios.usbmux.USBMuxCmdProcessor;
import com.apicloud.loader.platforms.ios.usbmux.USBMuxException;
import com.aptana.scripting.ScriptLogger;


/**
 * The activator class controls the plug-in life cycle
 */
public class ANBActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.apicloud.loader.platforms.ios"; //$NON-NLS-1$

	// The shared instance
	private static ANBActivator plugin;
	
	private static String IPHONE_PATH = null;
	private static String IPHONE_BASE = null;
	private static String IPHONE_VERSION_PATH = null;
	private static String IPHONE_CUSTOM_APPID = null;
	private static String TOOLS_PATH = null;
	public static int lPort = 2222;
	
	/**
	 * The constructor
	 */
	public ANBActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		plugin = this;
		Bundle bundle = bundleContext.getBundle();
		IPHONE_PATH = new File(FileLocator.toFileURL(bundle.getResource("base")).getFile()).getAbsolutePath();
		TOOLS_PATH = new File(FileLocator.toFileURL(bundle.getResource("tools")).getFile()).getAbsolutePath();
		if(bundle.getResource("base/load.ipa")==null){
			IPHONE_BASE=new File(FileLocator.toFileURL(bundle.getResource("base")).getFile()).getAbsolutePath()+"//load.ipa";
		}else{
			IPHONE_BASE = new File(FileLocator.toFileURL(bundle.getResource("base/load.ipa")).getFile()).getAbsolutePath();
		}
		IPHONE_VERSION_PATH = new File(FileLocator.toFileURL(bundle.getResource("base/version.txt")).getFile()).getAbsolutePath();
		IPHONE_CUSTOM_APPID = new File(FileLocator.toFileURL(bundle.getResource("base/customappid.txt")).getFile()).getAbsolutePath();
		
		//USBMuxCmdProcessor.callProcess();
		Job startUSBMuxdJob = new Job("Start USBMuxd Daemon") {
			protected IStatus run(IProgressMonitor monitor) {
			
					try {
						boolean failed = true;
						while (failed) {
							failed = ! USBMuxCmdProcessor.callProcess(""+lPort);
							if (failed)
								lPort = new Random().nextInt(10000) + 2222;
						}

					} catch (USBMuxException e) {
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}
				
				return Status.OK_STATUS;
			}
		};
		
		startUSBMuxdJob.setSystem(true);
		startUSBMuxdJob.schedule(500L);
		startUSBMuxdJob.addJobChangeListener(new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				if (event.getResult().isOK()) {
					ScriptLogger.logError("iOS \u65AD\u5F00\u8FDE\u63A5...");
					
				}
			}
		});
		
		Thread shutdown = new Thread(new Runnable() {
			public void run() {
				try {
					USBMuxCmdProcessor.forceKillProcess("python");
				} catch (Exception ex) {
				}
			}
		});
		Runtime.getRuntime().addShutdownHook(shutdown);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		//USBMuxCmdProcessor.forceKillProcess("python");
		
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ANBActivator getDefault() {
		return plugin;
	}
	
	public static String getUpdateBasePath() {
		return IPHONE_BASE;
	}	
	
	public static String getUpdateVersion() {
		return IPHONE_VERSION_PATH;
	}
	
	public static String getCustomAPPIDPath() {
		return IPHONE_CUSTOM_APPID;
	}
	
	public static String getBasePath() {
		return IPHONE_BASE;
	}
	
	public static String getAppLoaderPath() {
		return IPHONE_PATH;
	}
	
	public static String getToolsPath() {
		return TOOLS_PATH;
	}
}
