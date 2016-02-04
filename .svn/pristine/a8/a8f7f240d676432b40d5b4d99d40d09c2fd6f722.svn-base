/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.loader.platforms.android;

import com.apicloud.commons.util.OS;

public class ADBCommand {

	public static final int CMD_TYPE_START 				= 0;
	public static final int CMD_TYPE_KILL 				= 1;
	public static final int CMD_TYPE_FORCE_KILL 		= 2;
	public static final int CMD_TYPE_LOGCAT 		    = 3;
	public static final int CMD_TYPE_INSTALL 			= 4;
	public static final int CMD_TYPE_UNINSTALL   		= 5;
	public static final int CMD_TYPE_PUSH        		= 6;
	public static final int CMD_TYPE_REMOVE        		= 7;
	public static final int CMD_TYPE_APP_VERSION 		= 8;
	public static final int CMD_TYPE_DEVICES         	= 9;
	public static final int CMD_TYPE_START_APP         	= 10;
	public static final int CMD_TYPE_STOP_APP         	= 11;
	
	public int m_type;
	public String m_executable;
	public String[] m_parameters;
	
	public ADBCommand(final int type) {
		String[] parameters = null;
		this.m_type = type;
		this.m_executable = ADBActivator.getADBPath();
		switch (type) {
		case CMD_TYPE_START:
			parameters = new String[] {"start-server"};
			break;
		case CMD_TYPE_KILL:
			parameters = new String[] {"kill-server"};
			break;
		case CMD_TYPE_LOGCAT:
			if (OS.isWindows()) {
				parameters = new String[] {"logcat", "|", "grep", "\"^..app3c\""};
			} else {
				parameters = new String[] {"logcat"};
			}
			break;
		case CMD_TYPE_INSTALL:
			parameters =new String[]{};
			break;
		case CMD_TYPE_UNINSTALL:
			parameters=new String[]{"uninstall"};
			break;
		case CMD_TYPE_DEVICES:
			parameters = new String[] {"devices"};
			break;
		default:
			break;
		}
		this.m_parameters = parameters;
	}
	
	public ADBCommand(final int type, final String[] parameters) {
		this.m_type = type;
		this.m_executable = ADBActivator.getADBPath();
		this.m_parameters = parameters;
	}
	
	public ADBCommand(final int type, final String executable, final String[] parameters) {
		this.m_type = type;
		this.m_executable = executable;
		this.m_parameters = parameters;
	}
}
