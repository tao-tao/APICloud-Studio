/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.loader.platforms.ios;

public class ANBMessage {

	public static final int MSG_TYPE_FINDING_DEVICE = 0;
	public static final int MSG_TYPE_FIND_APP = 1;
	public static final int MSG_TYPE_UNINSTALL_APP = 2;
	public static final int MSG_TYPE_START_SERVICE = 3;
	public static final int MSG_TYPE_INSTALL_APP = 4;
	public static final int MSG_TYPE_PUSH_RES = 5;
	public static final int MSG_TYPE_PUSH_WGT = 6;
	public static final int MSG_TYPE_START_ARREST = 7;
	public static final int MSG_TYPE_TRANSFER_APP = 8;
	
	public int m_type;
	public IOSDevice m_dev;
	public String m_appId;
	public String m_pkgName;
	public String m_pkgPath;
	public String m_wgtPath;
	public String m_resPath;
	public String m_serviceName;
	
	public ANBMessage(int type) {
		this.m_type = type;
	}

	public ANBMessage(int type, IOSDevice dev) {
		this.m_type = type;
		this.m_dev = dev;
	}
}
