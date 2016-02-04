/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.loader.platforms.ios;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IStartup;


public class ANBStartUp implements IStartup {
	
	public void earlyStartup() {
		IOSDevice.connectedDevices.clear();
		ANBImplement.getInstance().getConnectionMap().clear();
		ANBProvider privider = ANBProvider.getANBProvider();
		try {
			privider.find();
			ANBActivator
			.getDefault()
			.getLog()
			.log(new Status(IStatus.ERROR, ANBActivator.PLUGIN_ID, 0, 
					"earlyStartup() find device ", null));
		} catch (ANBException e) {
			e.printStackTrace();
		}
	}
}
