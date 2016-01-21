/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.commons.util;

public class OS {
	private static OSType osType = OSType.OSUndefined;

	public static final boolean isWindows() {
		return (getOSType() == OSType.OSWindows);
	}

	public static final boolean isMacintosh() {
		return (getOSType() == OSType.OSMacintosh);
	}

	public static final boolean isLinux() {
		return (getOSType() == OSType.OSLinux);
	}

	private static final OSType getOSType() {
		if (osType == OSType.OSUndefined) {
			String os = System.getProperty("os.name").toLowerCase();
			if (os.startsWith("windows"))
				osType = OSType.OSWindows;
			else if (os.startsWith("linux"))
				osType = OSType.OSLinux;
			else if (os.startsWith("mac"))
				osType = OSType.OSMacintosh;
			else
				osType = OSType.OSUnknown;
		}
		return osType;
	}

	private static enum OSType {
		OSUndefined, OSLinux, OSWindows, OSMacintosh, OSUnknown;
	}
}