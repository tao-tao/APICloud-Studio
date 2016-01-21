/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.loader.platforms.ios.usbmux;

import com.aptana.scripting.ScriptLogger;

public class USBMuxCmdOutputParser {

	public static void parse(String input) throws USBMuxException {
		
		if (input.isEmpty()) {
			return;
		}
		String theInput = input.trim();
		if ("error: device not found".equals(input.toLowerCase())) {
			throw new USBMuxException("设备未连接");
		}
		if ("Error type 3".equals(theInput)) {
			throw new USBMuxException("请删除手机中的Apploader后在尝试");
		}
		if (theInput.contains("Invalid argument")) {
			throw new USBMuxException("包含中文名的文件");
		}
		if (theInput.contains("Permission denied")) {	
			//throw new ADBException("没有权限");
		}
		if ((theInput.startsWith("*")) && (theInput.endsWith("*"))) {
			return;
		}

	}
}
