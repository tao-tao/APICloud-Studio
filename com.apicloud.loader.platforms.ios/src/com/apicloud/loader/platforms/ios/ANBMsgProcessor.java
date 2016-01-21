/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.loader.platforms.ios;


import com.apicloud.loader.platforms.ios.semaphore.jna.md.MDLibrary;
import com.apicloud.loader.platforms.ios.semaphore.util.plist.DictionaryElement;
import com.apicloud.loader.platforms.ios.semaphore.util.plist.PList;

public class ANBMsgProcessor {
	
	private static ANBMsgProcessor _anbMsgProcessor = null;
	
	public ANBMsgProcessor () {
	}
	
	public void processMsg(ANBMessage msg) throws ANBException {
		switch(msg.m_type) {
		case ANBMessage.MSG_TYPE_FINDING_DEVICE:
			IOSDeviceListenerService.getInstance().startService();
			break;
		case ANBMessage.MSG_TYPE_FIND_APP:
			ANBImplement.getInstance().lookupApplications(msg.m_dev);
			break;
		case ANBMessage.MSG_TYPE_INSTALL_APP:
			ANBImplement.getInstance().installApplication(msg.m_pkgPath);
			break;
		case ANBMessage.MSG_TYPE_UNINSTALL_APP:
			ANBImplement.getInstance().uninstallApplication(msg.m_dev ,msg.m_pkgName);
			break;
		case ANBMessage.MSG_TYPE_TRANSFER_APP:
			ANBImplement.getInstance().transferApplication(msg.m_pkgPath);
			break;
		case ANBMessage.MSG_TYPE_PUSH_WGT:
			ANBImplement.getInstance().pushWgt(msg.m_dev, msg.m_appId, msg.m_pkgName, msg.m_wgtPath);
			break;
		case ANBMessage.MSG_TYPE_PUSH_RES:
			ANBImplement.getInstance().pushRes(msg.m_resPath);
			break;
		case ANBMessage.MSG_TYPE_START_ARREST:
			ANBImplement.getInstance().startHouseArrestService(msg.m_dev, msg.m_pkgName);
			break;
		case ANBMessage.MSG_TYPE_START_SERVICE:
			ANBImplement.getInstance().startService(msg.m_dev, msg.m_serviceName);
			break;
		default:
			break;
		}
	}
	
	public void handleDeviceConnected(MDLibrary.am_device_notification_callback_info info) {
		PList pList = ANBImplement.getInstance().getDevPList(info.dev);
		DictionaryElement dict = pList.getValue();
		String name = dict.get("DeviceName").asString();
		String uuid = dict.get("UniqueDeviceID").asString();
		IOSDevice dev = new IOSDevice(name, uuid, info.dev);
		IOSDevice.put(name, dev);
	}
	
	public void handleDeviceDisConnected(MDLibrary.am_device_notification_callback_info info) {
		IOSDevice.connectedDevices.clear();
		ANBImplement.getInstance().getConnectionMap().clear();
	}
	
	public synchronized static ANBMsgProcessor getInstance() {
	    if (_anbMsgProcessor == null) {
	    	_anbMsgProcessor = new ANBMsgProcessor();
	    }
	    return _anbMsgProcessor;
	}
}
