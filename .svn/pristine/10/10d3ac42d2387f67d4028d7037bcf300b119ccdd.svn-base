/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.commons.util;

import org.eclipse.core.runtime.Platform;

public class IDEUtil {
	public static String getInstallPath() {
		if(OS.isWindows()) {
			String installPath = Platform.getInstallLocation().getURL().getPath();
			installPath = installPath.substring(1, installPath.length());
			installPath = installPath.replace("file:/", "");
			return installPath;
		} else {
			return Platform.getInstallLocation().getURL().getPath();
		}
	}
}
