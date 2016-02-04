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
		if (input.toLowerCase().contains("socket error: [Errno 10013]")) {
			throw new USBMuxException("端口已经被占用");
		}

	}
}
