/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.loader.platforms.android;

import java.util.HashMap;

public class AndroidDevice {
	
	public static HashMap<String, AndroidDevice> connectedDevices = new HashMap<String, AndroidDevice>();
	public static String loaderVersion = null;
	
	public String m_name = null;
	
	public AndroidDevice(String name) {
		this.m_name = name;
	}
	
	public static void put(String name, AndroidDevice dev) {
		connectedDevices.put(name, dev);
	}
	public String getUuid() {
		return m_name;
	}
}
