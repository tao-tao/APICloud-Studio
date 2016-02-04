/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.loader.platforms.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.aptana.core.util.PlatformUtil;

import com.apicloud.loader.api.APIProcessBuilder;

public final class ADBCmdProcessor {
	
	public static void forceKillProcess(String process) throws IOException, UnsupportedOperationException {
        if (PlatformUtil.isWindows()) {
        		Runtime.getRuntime().exec("taskkill /IM " + process + ".exe");
        } else if (PlatformUtil.isMac()) {
        		Runtime.getRuntime().exec("killall " + process);
        } else if (PlatformUtil.isLinux()) {
        		Runtime.getRuntime().exec("killall " + process);
        } else {
        		throw new UnsupportedOperationException("Action not available for platform ");
        }
	}
	
	public static void callProcess(ADBCommand cmd) throws ADBException {
		APIProcessBuilder apiProcessUtil = APIProcessBuilder.getLocal();
		apiProcessUtil.clear();
		apiProcessUtil.setExecutable(cmd.m_executable);
		apiProcessUtil.setParameters(cmd.m_parameters);
		Process theProcess = null;
		InputStream theInput = null;
		InputStreamReader theInputReader = null;
		BufferedReader theBufferedReader = null;
		String theInputStr = null;
		try {
			theProcess = apiProcessUtil.call();
			theInput = theProcess.getInputStream();
			theInputReader = new InputStreamReader(theInput, "UTF-8");
			theBufferedReader = new BufferedReader(theInputReader);
			while ((theInputStr = theBufferedReader.readLine()) != null) {
				try {
					ADBCmdOutputParser.parse(cmd.m_type, theInputStr);
				} catch (ADBException adbEx) {
					theProcess.destroy();
					throw adbEx;
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
	}
}
