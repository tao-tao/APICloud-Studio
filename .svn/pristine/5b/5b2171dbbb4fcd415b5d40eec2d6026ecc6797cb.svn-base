/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.loader.platforms.ios;

import java.util.HashMap;

import com.apicloud.loader.platforms.ios.semaphore.jna.md.MDLibrary;

public class IOSDevice {
	public static HashMap<String, IOSDevice> connectedDevices = new HashMap<String, IOSDevice>();
	
	public String m_name = null;
	public String m_uuid = null;
	private MDLibrary.am_device.ByReference m_dev;

	public IOSDevice(String name, String uuid, MDLibrary.am_device.ByReference dev) {
		this.m_name = name;
		this.m_uuid = uuid;
		this.m_dev = dev;
	}
	
	public static void put(String name, IOSDevice dev) {
		connectedDevices.put(name, dev);
	}
	
	public String getName() {
		return m_name;
	}
	
	public String getUUID() {
		return m_uuid;
	}
	
	public MDLibrary.am_device.ByReference getDev() {
		return m_dev;
	}
}
