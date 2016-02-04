/* 
 * TinyUmbrella - making iDevice restores possible... 
 * Copyright (C) 2009-2010 semaphore 
 * 
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
  * This program is distributed in the hope that it will be useful, 
  * but WITHOUT ANY WARRANTY; without even the implied warranty of 
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
  * GNU General Public License for more details. 
  * 
  * You should have received a copy of the GNU General Public License 
  * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
  */ 

package com.apicloud.loader.platforms.ios.semaphore.jna.md;

import java.io.File;
import java.util.Properties;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.apicloud.commons.util.UtilActivator;
import com.apicloud.loader.platforms.ios.ANBActivator;
import com.apicloud.loader.platforms.ios.semaphore.jna.cf.CFLibrary;
import com.apicloud.loader.platforms.ios.semaphore.util.Environment;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

public class LibraryFinder {
	
	private static Logger log = UtilActivator.logger;
	private static final MDLibrary MD_INSTANCE = null;
	private static final CFLibrary CF_INSTANCE = null;
	
	static 
	{
		loadPath();
	}
	
	private static boolean loadPath() {
		String mdPath;
		String cfpath;
		if (Platform.isWindows()) {
			try {

				mdPath = getMDPath(true);
				mdPath = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, mdPath, "iTunesMobileDeviceDLL");
				
				cfpath = getCPath(true);
				cfpath = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, cfpath, "InstallDir");

				ANBActivator
				.getDefault()
				.getLog()
				.log(new Status(IStatus.ERROR, ANBActivator.PLUGIN_ID, 0, 
						"loadPath() mdpath = "+mdPath+ " cfpath = "+cfpath, null));

			} catch (Throwable ignored) {
				try {
					mdPath = getMDPath(false);
					mdPath = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, mdPath, "iTunesMobileDeviceDLL");
					
					cfpath = getCPath(false);
					cfpath = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, cfpath, "InstallDir");

				} catch (Throwable ignored1) {
					ignored.printStackTrace();
					ANBActivator
					.getDefault()
					.getLog()
					.log(new Status(IStatus.ERROR, ANBActivator.PLUGIN_ID, 0, 
							"windows registry path not exist", null));
					return false;
				}
			}
			File fMd = new File(mdPath);
			if (!(fMd.exists())) {
				ANBActivator
				.getDefault()
				.getLog()
				.log(new Status(IStatus.ERROR, ANBActivator.PLUGIN_ID, 0, 
						"File path not exist", null));
				return false;
			}			
			String pathEnvVar = System.getenv("Path");
			pathEnvVar = pathEnvVar +   cfpath  + ";" + fMd.getParent() ;
			
			Environment.setEnv("Path", pathEnvVar);
			ANBActivator
			.getDefault()
			.getLog()
			.log(new Status(IStatus.ERROR, ANBActivator.PLUGIN_ID, 0, 
					"Environment.setEnv pathEnvVar = " + pathEnvVar, null));
			
	         String sqlite3 = cfpath + "/SQLite3.dll";
	         File fsql = new File(sqlite3);
	         if (fsql.exists())
	           System.load(cfpath + "/SQLite3.dll");
	         
	         String cfnetwork = cfpath + "/CFNetwork.dll";
	         File fcfn = new File(cfnetwork);
	         if (fcfn.exists())
	           System.load(cfpath + "/CFNetwork.dll");
	    }
	    return true;
	}
	
	public static synchronized MDLibrary getMDLibrary() {
		if (MD_INSTANCE != null) {
		    return MD_INSTANCE;
		}
		if (foundMobileLibrary()) {
		    return ((MDLibrary)Native.loadLibrary(MDLibrary.class));
		}
		log.info("Could not find the MobileDevice Library!");
		return null;
	}
	
	public static synchronized CFLibrary getCFLibrary() {
		if (CF_INSTANCE != null)
			return CF_INSTANCE;
		if (foundCoreLibrary())
		    return ((CFLibrary)Native.loadLibrary(CFLibrary.class));
		log.info("Could not find the CoreFoundation Library!");
		return null;
	}
	
	public static boolean foundMobileLibrary() {
	    return systemLoad(findMobileLibrary());
	}
	
	private static boolean foundCoreLibrary() {
		return systemLoad(findCoreLibrary());
	}
	
	private static String findMobileLibrary() {
		if (Platform.isWindows()) {
			String path;
		    try {
		    	path = getMDPath(true);
		    	path = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, path, "iTunesMobileDeviceDLL");
		    } catch (Exception ignored) {
		    	try {
			    	path = getMDPath(false);
			    	path = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, path, "iTunesMobileDeviceDLL");
		    	} catch (Exception ignored1) {
		    		log.info(ignored.getMessage());
		    		return "";
		    	}
			}
	    	File f = new File(path);
	    	if (f.exists())
	    		return path;
		} else {
			if (Platform.isMac()) {
				return "/System/Library/PrivateFrameworks/MobileDevice.framework/MobileDevice";
			}
		}
		return "";
	}
	   
	private static String findCoreLibrary() {
	    String path="";
	    if (Platform.isWindows()) {
	    	try {
		    	path = getCPath(true);
		       	path = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, path, "InstallDir");
	    	} catch (Exception ignored) {
	    		try {
			    	path = getCPath(false);
			       	path = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, path, "InstallDir");
	    		} catch (Exception ignored1) {
	    			return "";
	    		}
	    	}
	       	path = path + "CoreFoundation.dll";
	       	File f = new File(path);
	       	if (f.exists())
	       		return path;
	    } else if (Platform.isMac()) {
	         return "/System/Library/Frameworks/CoreFoundation.framework/CoreFoundation";
	    }
	    return "";
	}
	  
	private static boolean systemLoad(String path) {
		if (path.isEmpty()) {
			return false;
		}
		File file = new File(path);
	    if (!file.exists()) {
	      	return false;
	    }
    		if (Platform.isWindows()) {
            path = path.replace("\\\\", "\\");
            path = path.replace("\\", "\\\\");
            path = path.replace("/", "\\\\");
    	    } else if (Platform.isMac()){
            path = path.replace("\\\\", "\\");
            path = path.replace("\\", "/");
        	}
	    try {
	    	System.out.println("+++path+++ "+path);
	        System.load(path);
			ANBActivator
			.getDefault()
			.getLog()
			.log(new Status(IStatus.ERROR, ANBActivator.PLUGIN_ID, 0, 
					"System.load() path = "+path, null));
	        return true;
	    } catch (SecurityException se) {
			ANBActivator
			.getDefault()
			.getLog()
			.log(new Status(IStatus.ERROR, ANBActivator.PLUGIN_ID, 0, 
					"System.load() path = "+path + " failed due to SecurityException", null));
	    	
	       	return false;
	    } catch (UnsatisfiedLinkError le) {
			ANBActivator
			.getDefault()
			.getLog()
			.log(new Status(IStatus.ERROR, ANBActivator.PLUGIN_ID, 0, 
					"System.load() path = "+path + " failed due to UnsatisfiedLinkError", null));
	    	
	       	return false;
	    } catch (NullPointerException ne) {
			ANBActivator
			.getDefault()
			.getLog()
			.log(new Status(IStatus.ERROR, ANBActivator.PLUGIN_ID, 0, 
					"System.load() path = "+path + " failed due to NullPointerException", null));
	    	
	       	return false;
	    } catch (Throwable e) {
			ANBActivator
			.getDefault()
			.getLog()
			.log(new Status(IStatus.ERROR, ANBActivator.PLUGIN_ID, 0, 
					"System.load() path = "+path + " failed", null));
	    	
	       	return false;
	    }
	}
	
	private static String getMDPath(boolean isOS64bit) {	
	    if (isOS64bit)
	    	return "SOFTWARE\\Wow6432Node\\Apple Inc.\\Apple Mobile Device Support\\Shared";
	    else 
	    	return "SOFTWARE\\Apple Inc.\\Apple Mobile Device Support\\Shared";
	}
	   
	private static String getCPath(boolean isOS64bit) {
	    if (isOS64bit) {
	       return "SOFTWARE\\Wow6432Node\\Apple Inc.\\Apple Application Support";
	    } else {
	       return "SOFTWARE\\Apple Inc.\\Apple Application Support";
	    }
	}
	  
	static
	{
		Native.setProtected(true);
	}
}
