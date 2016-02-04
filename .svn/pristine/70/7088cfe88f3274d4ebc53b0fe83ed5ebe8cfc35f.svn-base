/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.loader.platforms.android;

import com.aptana.scripting.ScriptLogger;

public class ADBCmdOutputParser {

	public static void parse(final int type, String input) throws ADBException {
		
		if (input.isEmpty()) {
			return;
		}
		String theInput = input.trim();
		if ("error: device not found".equals(input.toLowerCase())) {
			throw new ADBException("设备未连接");
		}
		if ("Error type 3".equals(theInput)) {
			throw new ADBException("请删除手机中的Apploader后在尝试");
		}
		if (theInput.contains("Invalid argument")) {
			throw new ADBException("包含中文名的文件");
		}
		if (theInput.contains("Permission denied")) {	
			//throw new ADBException("没有权限");
		}
		if ((theInput.startsWith("*")) && (theInput.endsWith("*"))) {
			return;
		}
		switch(type) {
		case ADBCommand.CMD_TYPE_START:
		case ADBCommand.CMD_TYPE_KILL:
			break;
		case ADBCommand.CMD_TYPE_APP_VERSION:
			if (theInput.contains("versionName")) {
				AndroidDevice.loaderVersion = theInput.substring(12);
			}
			break;
		case ADBCommand.CMD_TYPE_LOGCAT:
			int index = theInput.indexOf(":") + 2;
			String log = "";
			if (theInput.contains("app3c")) {
				if (theInput.charAt(0) == 'I') {
					log = theInput.substring(index);
					ScriptLogger.logInfo(log);
				} else if (theInput.charAt(0) == 'E') {
					log = theInput.substring(index);
					ScriptLogger.logError(log);
				} else if (theInput.charAt(0) == 'W') {
					log = theInput.substring(index);
					ScriptLogger.logWarning(log);
				}
				System.out.println(log);
			}
			break;
		case ADBCommand.CMD_TYPE_DEVICES:
				String theDevName = null;
				String[] splits = theInput.split("\t",2);
				if (splits.length == 2 && splits[1].equals("device")) {
					theDevName = splits[0];
				}
				if (theDevName != null) {
					AndroidDevice dev = new AndroidDevice(theDevName);
					AndroidDevice.put(theDevName, dev);
				}
			break;
		default:
			break;
		}
	}
}
