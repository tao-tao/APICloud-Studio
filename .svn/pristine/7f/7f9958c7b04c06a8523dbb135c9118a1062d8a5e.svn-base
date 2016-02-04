/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.loader.platforms.ios;

import com.sun.jna.Platform;

public class IOSDeviceListenerService implements Runnable {
	private static IOSDeviceListenerService _instance = new IOSDeviceListenerService();
	private Thread runner;

	private IOSDeviceListenerService() {
		init();
	}
	
	public static IOSDeviceListenerService getInstance() {
		return _instance;
	}

	private void init() {
		this.runner = new Thread(this);
	}

	public void startService() {
		if (this.runner.isAlive()) {
			stopService();
			this.runner = new Thread(this);
		}
		this.runner.start();
	}

	public void stopService() {
		try {
			this.runner.interrupt();
				try {
					this.runner.join(6000L);
				} catch (Exception ignored) {
				}
		} catch (Exception ignored) {
		}
	}
	
	public void run() {
		ANBImplement.getInstance().listen();
		while ((!(Thread.interrupted())) && (Platform.isLinux())) {
			try {
				Thread.sleep(5000L);
			} catch (Exception ignored) {
			}
		}
	}
}
