/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import com.apicloud.loader.platforms.android.ADBException;
import com.apicloud.loader.platforms.android.ADBService;
import com.apicloud.loader.platforms.android.AndroidDevice;
import com.apicloud.loader.platforms.ios.IOSDevice;

public  class RunAsAppFormDeviceHandler {
	private static RunAsAppFormDeviceHandler appdevicehandler;
	
	public synchronized static RunAsAppFormDeviceHandler getInstance(){
		if(appdevicehandler==null){
			appdevicehandler=new RunAsAppFormDeviceHandler();
		}
		return appdevicehandler;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public  List<AndroidDevice> getAndroidDevice(){
		AndroidDevice.connectedDevices.clear();
		List<AndroidDevice> aMobiles = new ArrayList<AndroidDevice>();
		try {
			ADBService.getADBService().findDevices();
		} catch (ADBException e1) {
			e1.printStackTrace();
		}
		HashMap<String, AndroidDevice> adeviceMap = AndroidDevice.connectedDevices;
		if(adeviceMap.size()>0){
			Iterator aiterator = adeviceMap.entrySet().iterator();
			while (aiterator.hasNext()) {
				Map.Entry<String, AndroidDevice> entry = (Entry<String, AndroidDevice>) aiterator
						.next();
				aMobiles.add(entry.getValue());
			}
		}
		
		return aMobiles;
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public  List<IOSDevice> getIosDevice(){
		List<IOSDevice> iMobiles = new ArrayList<IOSDevice>();
		HashMap<String, IOSDevice> ideviceMap = IOSDevice.connectedDevices;
		if(ideviceMap.size()>0){
			Iterator iterator = ideviceMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, IOSDevice> entry = (Entry<String, IOSDevice>) iterator
						.next();
				iMobiles.add(entry.getValue());
			}
		}
		return iMobiles;
	}
}
