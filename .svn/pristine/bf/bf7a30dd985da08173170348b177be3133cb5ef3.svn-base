/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.loader.platforms.ios;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.sun.jna.Platform;
import com.sun.jna.Pointer;

import com.apicloud.commons.util.UtilActivator;
import com.apicloud.loader.platforms.ios.semaphore.jna.md.MDLibrary;
import com.apicloud.loader.platforms.ios.semaphore.jna.cf.CFDictionary;
import com.apicloud.loader.platforms.ios.semaphore.jna.cf.CFLibrary;
import com.apicloud.loader.platforms.ios.semaphore.jna.cf.CFString;
import com.apicloud.loader.platforms.ios.semaphore.jna.cf.CFType;
import com.apicloud.loader.platforms.ios.semaphore.util.plist.PList;

public class ANBImplement implements MDLibrary.am_device_notification_callback {

	private static ANBImplement _anbImplement = null;
	private static MDLibrary mdLib = MDLibrary.INSTANCE;
	private static CFLibrary cfLib = CFLibrary.INSTANCE;
	private static Logger log = UtilActivator.logger;
	
	private MDLibrary.afc_connection.ByReference m_afcConnection;
	private Map<String, MDLibrary.afc_connection.ByReference> m_connectionMap = new HashMap<String, MDLibrary.afc_connection.ByReference>();

	public void listen() {
		log.info("start listenning...");
		MDLibrary.am_device_notification.ByReference[] cb = new MDLibrary.am_device_notification.ByReference[1];
		mdLib.AMDeviceNotificationSubscribe(this, 0, 0, 0, cb);
		if (Platform.isMac()) {
			cfLib.CFRunLoopRun();
		}
	}

	public void invoke(MDLibrary.am_device_notification_callback_info info, Pointer arg) {
		if (info.msg == 1) {
			if (this.connect(info.dev)) {
				ANBMsgProcessor.getInstance().handleDeviceConnected(info);
			}
		} else if ((info.msg == 2) || (info.msg == 3)) {
			ANBMsgProcessor.getInstance().handleDeviceDisConnected(info);
		}
	}
	
	public boolean connect(MDLibrary.am_device.ByReference devRef) {
	    int retval = 0;
	    if ((retval = mdLib.AMDeviceConnect(devRef)) != 0) {
	    	log.info("Unable to connect to device [" + devRef.device_id + "]" + ", retval = " + retval);
	    	return false;
	    } else {
	    	log.info("AMDeviceConnect OK");
	    }

	    if ((retval = mdLib.AMDeviceIsPaired(devRef)) != 1) {
	    	log.info("Unable to pair with device [" + devRef.device_id + "]" + ", retval = " + retval);
	    	return false;
	    } else {
	    	log.info("AMDeviceIsPaired OK");
	    }

	    if ((retval = mdLib.AMDeviceValidatePairing(devRef)) != 0) {
	    	log.info("Unable to validate the pairing with device [" + devRef.device_id + "]" + ", retval = " + retval);
	    	return false;
	    } else {
	    	log.info("AMDeviceValidatePairing OK");
	    }

	    if ((retval = mdLib.AMDeviceStartSession(devRef)) != 0) {
	    	log.info("Unable to start session on device [" + devRef.device_id + "]" + ", retval = " + retval);
	    	return false;
	    } else {
	    	log.info("AMDeviceStartSession OK");
	    }
		return true;
	}
	
	public void disconnect(MDLibrary.am_device.ByReference devRef) {
		int retval = 0;
		if ((retval = mdLib.AMDeviceDisconnect(devRef)) != 0) {
			log.info("Unable to disconnct on device [" + devRef.device_id + "]" + ", retval = " + retval);
		}
	}

	public void lookupApplications(IOSDevice device) {
		MDLibrary.CFDictionaryRef.ByReference dict = new MDLibrary.CFDictionaryRef.ByReference();
		int retval = mdLib.AMDeviceLookupApplications(device.getDev(), 0, dict);
		if (retval == 0) {
			CFDictionary dicts = new CFDictionary((dict.key).getPointer());
			ANBProvider.findAppRet = dicts.toPlist();
		} else {
			log.info("AMDeviceLookupApplications failed.");
		}
	}

	public void uninstallApplication(IOSDevice device, String pkgName) {
		if (getAfc_connection() != null) {
			CFString[] keys = { CFString.buildString("PackageType") };
			CFType[] values = { CFString.buildString("Developer") };
			CFDictionary options = CFDictionary.createCFDictionary(keys, values, 1);
			int retval = mdLib.AMDeviceUninstallApplication(getAfc_connection().handle, CFString.buildString(pkgName), options, null, 0);
			if (0 != retval) {
				log.info("AMDeviceUninstallApplication failed.");
			}
			if (!pkgName.equals("com.apicloud.loader")) {
				m_connectionMap.remove(device.getUUID() + pkgName);
			}
		}
	}

	public void startService(IOSDevice device, String serviceName) throws ANBException {
		MDLibrary.afc_connection.ByReference afc_connection = new MDLibrary.afc_connection.ByReference();
		int retval = mdLib.AMDeviceStartService(device.getDev(), CFString.buildString(serviceName), afc_connection, 0);
		if (retval == 0) {
			setAfc_connection(afc_connection);
		} else {
			log.info("AMDeviceStartService failed.");
			if (retval == -402653057) {
				throw new ANBException("请用USB线将设备 " + device.getName() + " 连接到电脑上!");
			}
		}
	}
	
	public void installApplication(String pkgPath) {
		if (getAfc_connection() != null) {
			CFString[] keys = {CFString.buildString("PackageType")};
			CFType[] values = {CFString.buildString("Developer")};
			CFDictionary options = CFDictionary.createCFDictionary(keys, values, 1);
			CFString path = CFString.buildString(pkgPath);
			int retval = mdLib.AMDeviceInstallApplication(getAfc_connection().handle, path, options, null, 0);
			if (retval != 0) {
				log.info("AMDeviceInstallApplication failed.");
			}
		}
	}

	public void transferApplication(String pkgPath) throws ANBException {
		if (getAfc_connection() != null) {
			int retval = mdLib.AMDeviceTransferApplication(getAfc_connection().handle, CFString.buildString(pkgPath), 0, null, 0);
			if (retval != 0) {
				log.info("AMDeviceTransferApplication failed.");
				throw new ANBException("transferApplication package failed!");
			}
		} 
	}

	public void pushWgt(IOSDevice device, String appId, String pkgName, String wgtPath) throws ANBException {
		MDLibrary.afc_connection.ByReference afc_connection = startHouseArrestService(device, pkgName);
		if (afc_connection != null) {
			MDLibrary.afc_connection.ByReference[] afc_connections = new MDLibrary.afc_connection.ByReference[1];
			int retval = mdLib.AFCConnectionOpen(afc_connection.handle, 0, afc_connections);
			if (retval == 0) {
				this.m_afcConnection = afc_connections[0];
			} else {
				log.info("AFCConnectionOpen failed in pushWgt.");
			}
			syncHouseArrest(wgtPath, appId);
		}
	}
	
	public void pushRes(String resPath) {
		if (getAfc_connection() != null) {
			File infoFile = new File(resPath);
			if (!(infoFile.exists())) {
				return;
			}
			MDLibrary.afc_connection.ByReference afc_connection = getAfc_connection();
			String targetPath = "/Documents/uzfs/A6965066952332/";
			byte[] targetPathByte = toAFCByte(targetPath.getBytes(Charset.forName("UTF-8")));
			MDLibrary.afc_directory.ByReference afcd = openDirectory(afc_connection, targetPathByte);
			if (afcd == null) {
				int retval = mdLib.AFCDirectoryCreate(afc_connection, targetPathByte);
				if (0 != retval) {
					log.info("AFCDirectoryCreate failed in pushRes");
				}
			}
			writeFile(afc_connection, targetPath, infoFile, "/startInfo.txt");
		}
	}
	
	public MDLibrary.afc_connection.ByReference startHouseArrestService(IOSDevice device, String pkgName) {
		MDLibrary.afc_connection.ByReference acf_connection = m_connectionMap.get(device.getUUID() + pkgName);
		
		if (acf_connection != null) {
			return acf_connection;
		}
		acf_connection = new MDLibrary.afc_connection.ByReference();
		CFDictionary dict = new CFDictionary();
		int retval = mdLib.AMDeviceStartHouseArrestService(device.getDev(), CFString.buildString(pkgName), dict, acf_connection, 0);
		if (retval == 0) {
			setAfc_connection(acf_connection);
			m_connectionMap.put(device.getUUID() + pkgName, acf_connection);
		} else {
			log.info("AMDeviceStartHouseArrestService failed.");
		}
		return acf_connection;
	}
	
	private MDLibrary.afc_directory.ByReference openDirectory(MDLibrary.afc_connection afcs, byte[] pathByte) {
		MDLibrary.afc_directory.ByReference[] afc_directorys = new MDLibrary.afc_directory.ByReference[1];
		int retval = mdLib.AFCDirectoryOpen(afcs, pathByte, afc_directorys);
		if (retval != 0) {
			return null;
		}
		return afc_directorys[0];
	}
	
	public void syncHouseArrest(String wgtPath, String appid) {
		if (getAfc_connection() != null) {
			String targetPath = "/Documents/uzfs/wgt/" + appid + "/";
			byte[] targetPathByte = toAFCByte(targetPath.getBytes(Charset.forName("utf-8")));
			MDLibrary.afc_directory.ByReference afcd = openDirectory(this.m_afcConnection, targetPathByte);
			if (afcd == null) {
				int retval = mdLib.AFCDirectoryCreate(this.m_afcConnection, targetPathByte);
				if (retval != 0) {
					log.info("AFCDirectoryCreate failed in syncHouseArrest.");
				}
			}
			File f = new File(wgtPath);
			doSync(wgtPath, getAfc_connection(), targetPath, f);
		}
	}

	private void doSync(String wgtPath, MDLibrary.afc_connection.ByReference afc_connection, String targetPath, File file) {
		String dirPath = "";
		if (file.isDirectory()) {
			File[] internalFS = file.listFiles();
			for (File f : internalFS) {
				if (!(f.exists())) {
					continue; // skip not exists
				}
				if (f.isDirectory()) {
					String path = f.getPath();
					path = path.replace(wgtPath, "");
					path = path.replace("\\", "/");
					if (path.startsWith("/")) {
						path = path.substring(1);
					}
					dirPath = targetPath + path + "/";
					byte[] dirPathByte = toAFCByte(dirPath.getBytes(Charset.forName("utf-8")));
					MDLibrary.afc_directory.ByReference afcd = openDirectory(afc_connection, dirPathByte);
					if (afcd == null) {
						int retval = mdLib.AFCDirectoryCreate(afc_connection, dirPathByte);
						if (retval != 0) {
							log.info("AFCDirectoryCreate failed in doSync.");
						}
					}
					doSync(wgtPath, afc_connection, targetPath, f);
				} else {
					if (!(f.getName().equals(".")) && !(f.getName().equals("..")) && !(f.getName().equals(".svn"))) {
						String path = f.getPath();
						path = path.replace(wgtPath, "");
						path = path.replace("\\", "/");
						if (path.startsWith("/")) {
							path = path.substring(1);
						}
						writeFile(afc_connection, targetPath, f, path);
					}
				}
			}
		}
	}

	private void writeFile(MDLibrary.afc_connection.ByReference afc_connection,
			String targetPath, File file, String fileName) {
		String filePath = targetPath + fileName;
		byte[] filePathByte = toAFCByte(filePath.getBytes(Charset.forName("utf-8")));
		byte[] fileDataByte = readFileByBytes(file);
		if (fileDataByte == null) {
			return;
		}
		MDLibrary.afc_file_ref.ByReference afc_file_ref = new MDLibrary.afc_file_ref.ByReference();
		int retval = mdLib.AFCFileRefOpen(afc_connection, filePathByte, 3L, afc_file_ref);
		if (retval == 0) {
			retval = mdLib.AFCFileRefWrite(afc_connection, afc_file_ref._cnt, fileDataByte, fileDataByte.length);
			if (retval != 0) {
				log.info("AFCFileRefWrite failed in writeFile.");
			}
			mdLib.AFCFileRefClose(afc_connection, afc_file_ref._cnt);
		} else {
			log.info("AFCFileRefOpen failed in writeFile.");
		}
	}

	private byte[] toAFCByte(byte[] s) {
		byte[] b = new byte[s.length + 1];
		System.arraycopy(s, 0, b, 0, s.length);
		b[s.length] = 0;
		return b;
	}

	public static byte[] readFileByBytes(File file) {
		byte[] tempbytes  = null;
        int byteRead = 0;  
        int byteAvailable = 0;
		FileInputStream in = null;
		ByteArrayOutputStream out = null;
		try {
			in = new FileInputStream(file);
			byteAvailable = in.available();
			if (byteAvailable <= 0) {
				return null;
			}
			tempbytes = new byte[byteAvailable];
			out = new ByteArrayOutputStream(byteAvailable);
			while ((byteRead = in.read(tempbytes)) != -1) {
				out.write(tempbytes, 0, byteRead);
			}
		} catch (FileNotFoundException e) {
			log.info("read startInfo failed in readFileByBytes.");
		} catch (IOException e) {
			log.info("read startInfo failed in readFileByBytes.");
		} finally {
			try {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
			} catch (IOException e) {
			}
		}
		return out.toByteArray();
	}

	public PList getDevPList(MDLibrary.am_device.ByReference dev) {
		CFType value = dev.getValue(null);
		if (value != null) {
			CFDictionary dict = new CFDictionary(value.getPointer());
			if (dict != null) {
				return dict.toPlist();
			}
		}
		return null;
	}

	public synchronized static ANBImplement getInstance() {
		if (_anbImplement == null) {
			_anbImplement = new ANBImplement();
		}
		return _anbImplement;
	}

	public MDLibrary.afc_connection.ByReference getAfc_connection() {
		return m_afcConnection;
	}

	public void setAfc_connection(
			MDLibrary.afc_connection.ByReference afc_connection) {
		this.m_afcConnection = afc_connection;
	}

	public Map<String, MDLibrary.afc_connection.ByReference> getConnectionMap() {
		return m_connectionMap;
	}
}
