/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.loader.platforms.android;

import org.eclipse.ui.IStartup;

import com.apicloud.commons.util.UtilActivator;

public class ADBStartUp implements IStartup {
	
	public void earlyStartup() {
		try {
			ADBService.getADBService().start();
		} catch (ADBException adbEx) {
			UtilActivator.logger.info("start adb service failed.");
		}
	}
}
