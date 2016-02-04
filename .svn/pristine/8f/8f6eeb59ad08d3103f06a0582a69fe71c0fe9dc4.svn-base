/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.loader.platforms.ios.usbmux;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.aptana.core.util.PlatformUtil;

import com.apicloud.commons.util.OS;
import com.apicloud.loader.api.APIProcessBuilder;
import com.apicloud.loader.platforms.ios.ANBActivator;

public final class USBMuxCmdProcessor {
	
	public static void forceKillProcess(String process) throws IOException, UnsupportedOperationException {
        if (PlatformUtil.isWindows()) {
        		Runtime.getRuntime().exec("taskkill /F /IM " + process + ".exe");
        } else if (PlatformUtil.isMac()) {
        		Runtime.getRuntime().exec("killall " + process);
        } else if (PlatformUtil.isLinux()) {
        		Runtime.getRuntime().exec("killall " + process);
        } else {
        		throw new UnsupportedOperationException("Action not available for platform ");
        }
	}
	
	  public static boolean findProcess(String processName) {
		    BufferedReader bufferedReader = null;
		    try {
		      Process proc = Runtime.getRuntime().exec("tasklist /FI \"IMAGENAME eq " + processName + "\"");
		      bufferedReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		      String line = null;
		      while ((line = bufferedReader.readLine()) != null) {
		        if (line.contains(processName)) {
		          return true;
		        }
		      }
		      return false;
		    } catch (Exception ex) {
		      ex.printStackTrace();
		      return false;
		    } finally {
		      if (bufferedReader != null) {
		        try {
		          bufferedReader.close();
		        } catch (Exception ex) {}
		      }
		    }
		  }
	
	public static boolean callProcess(String lport) throws USBMuxException {
		Process theProcess = null;
		InputStream theInput = null;
		InputStreamReader theInputReader = null;
		BufferedReader theBufferedReader = null;
		String theInputStr = null;
		
		List commands_tcprelay = new ArrayList();
		
		if (OS.isWindows()) {
			if (findProcess("python.exe"))
				return true;
			String pythonCmd = ANBActivator.getToolsPath()+"\\python.exe";
			commands_tcprelay.add(pythonCmd);
			commands_tcprelay.add(ANBActivator.getToolsPath()+"\\tcprelay.py");
		} else {  /*Mac OS X*/
			if (findProcess("python"))
				return true;
			commands_tcprelay.add("/usr/bin/python");
			commands_tcprelay.add(ANBActivator.getToolsPath()+"/tcprelay.py");
		}
				
		commands_tcprelay.add("-t");
		commands_tcprelay.add("80:"+lport);
	 	
	 	ProcessBuilder pb = new ProcessBuilder((String[])commands_tcprelay.toArray(new String[commands_tcprelay.size()]));
	 	pb.redirectErrorStream(true);
		
		try {
			
			//pb.directory(new File(ANBActivator.getToolsPath()));
			theProcess = pb.start();
	    	 
			theInput = theProcess.getInputStream();
			theInputReader = new InputStreamReader(theInput, "UTF-8");
			theBufferedReader = new BufferedReader(theInputReader);
			while ((theInputStr = theBufferedReader.readLine()) != null) {
				try {
					USBMuxCmdOutputParser.parse(theInputStr);
				} catch (USBMuxException portEx) {
					theProcess.destroy();
					return false;
				}
			}
		} catch (IOException ex) {
			//do nothing.
		} finally {
			if (theBufferedReader != null) {
				try {
					theBufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}
}
