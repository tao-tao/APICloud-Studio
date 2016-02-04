/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.loader.platforms.ios;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.apicloud.commons.model.CustomLoaderModel;
import com.apicloud.commons.util.FileUtil;
import com.apicloud.commons.util.IDEUtil;
import com.apicloud.loader.api.IProgressCallBack;
import com.apicloud.loader.platforms.ios.semaphore.util.plist.DictionaryElement;
import com.apicloud.loader.platforms.ios.semaphore.util.plist.PElement;
import com.apicloud.loader.platforms.ios.semaphore.util.plist.PList;
import com.aptana.scripting.ScriptLogger;

public class ANBProvider {

	public static ANBProvider fANBProvider = null;
	private static String version;
	private static Map<String, CustomLoaderModel> loadMap = com.apicloud.commons.model.Activator.getLoaderMap();
	public static PList findAppRet = null;
	public boolean logListenning = false;
//	Thread logger_ios = null;
	Thread logger_ios = null;
	Runnable log_runnable = new Runnable() {
		public void run() {
			System.out.println("begin to get log from iPhone/iPad");
			
			for (;;) 
			{
				URL url = null;
				HttpURLConnection conn = null;
				String line = "";
				BufferedReader reader = null;
				try {
					url = new URL("http://127.0.0.1:"+ANBActivator.lPort);
					conn = (HttpURLConnection) url.openConnection();
					conn.setConnectTimeout(20000);
					conn.setRequestProperty("Content-Type",
							"application/x-www-form-urlencoded");

					// 接收数据
					conn.getErrorStream();
					conn.getResponseCode();
					InputStream input = conn.getInputStream();
					reader = new BufferedReader(new InputStreamReader(input, "utf-8"));
					
						String sTotalString = "";
						while ((line = reader.readLine()) != null) {
							sTotalString += line;
						}
/*						String log = "";
						int index = sTotalString.indexOf(":") + 2;
						if (sTotalString.charAt(0) == 'I') {
							log = sTotalString.substring(index);
							ScriptLogger.logInfo(log);
						} else if (sTotalString.charAt(0) == 'E') {
							log = sTotalString.substring(index);
							ScriptLogger.logError(log);
						} else if (sTotalString.charAt(0) == 'W') {
							log = sTotalString.substring(index);
							ScriptLogger.logWarning(log);
						} else*/
						if (sTotalString != null && sTotalString.equals("[]") == false) {
							JSONArray json;
							json = new JSONArray(sTotalString);
							for (int i=0; i<json.length();i++){
								ScriptLogger.logInfo("[iOS log] "+json.getString(i));
							}
						}
						
						try {
							reader.close();
						} catch (IOException e) {
							// TODO 自动生成的 catch 块
							e.printStackTrace();
						}
						if (conn != null)
							conn.disconnect();
					
				} catch (NoRouteToHostException e) {
					e.printStackTrace();
				
				} catch (ConnectException e) {
					// MessageDialog.openError(null, "server error", "please wait ");
					e.printStackTrace();
				} catch (SocketTimeoutException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				} finally {

					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}
				}

			}
		}
	};
	
	@SuppressWarnings("deprecation")
	public void syncLog() throws ANBException {
		//if (logListenning == false) {
		    ScriptLogger.logError("开启iOS日志输出...");
		    logListenning = true;
		    if (logger_ios != null)
		    	logger_ios.stop();
		    logger_ios = new Thread(log_runnable);
			logger_ios.start();
			
		//}
			
	}
	
	@SuppressWarnings("deprecation")
	public void stopLog() throws ANBException {
		//if (logListenning) {
			ScriptLogger.logError("停止iOS日志输出...");
			if (logger_ios != null)
				logger_ios.stop();
			logger_ios = null;
			logListenning = false;
		//}
	}

	public void find() throws ANBException {
		ANBMsgProcessor.getInstance().processMsg(new ANBMessage(ANBMessage.MSG_TYPE_FINDING_DEVICE));
	}
	
	public void connect(IOSDevice dev) {
		ANBImplement.getInstance().disconnect(dev.getDev());
		ANBImplement.getInstance().connect(dev.getDev());
	}

	public void load(IOSDevice dev, String startInfo, String appid,
			String wgtPath, IProgressCallBack callBack)
			throws ANBException {
		String pkgName = "com.apicloud.loader";
//		ANBProvider.getANBProvider().stopLog();
		if (checkInstalled(dev, appid)) {
			callBack.done(80, Messages.AppExistSyncing, Messages.Syncing);
			// push wgt
			if (appid != null && loadMap != null) {
				CustomLoaderModel theLoader = loadMap.get(appid);
				if (theLoader != null) {
					pkgName = theLoader.getIpackagename();
				}
			}
			ANBMessage arrestMsg = new ANBMessage(ANBMessage.MSG_TYPE_START_ARREST, dev);
			arrestMsg.m_pkgName = pkgName;
			ANBMsgProcessor.getInstance().processMsg(arrestMsg);
			
			// push widget
			ANBMessage pushWgtMsg = new ANBMessage(ANBMessage.MSG_TYPE_PUSH_WGT, dev);
			pushWgtMsg.m_appId = appid;
			pushWgtMsg.m_pkgName = pkgName;
			pushWgtMsg.m_wgtPath = wgtPath;
			ANBMsgProcessor.getInstance().processMsg(pushWgtMsg);
		
			// push resource
			ANBMessage pushResMsg = new ANBMessage(ANBMessage.MSG_TYPE_PUSH_RES, dev);
			pushResMsg.m_resPath = startInfo + "/startInfo.txt";
			ANBMsgProcessor.getInstance().processMsg(pushResMsg);
			callBack.done(95, null, null);

			callBack.done(100, "", "");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			callBack.done(100, Messages.IOSFinish, Messages.Finish);
			if (!pkgName.equals("com.apicloud.loader")) {
				createCustomAPPID(appid, ANBActivator.getCustomAPPIDPath());
			}
		} else {
			// clear connections.
			ANBImplement.getInstance().getConnectionMap().clear();
			
			// update progress.
			callBack.done(30, Messages.InstallApp, Messages.Syncing);
			
			String loaderPath = ANBActivator.getBasePath();
			if (appid != null && loadMap != null) {
				CustomLoaderModel theLoader = loadMap.get(appid);
				if (theLoader != null) {
					pkgName = theLoader.getIpackagename();
				}
				File file = new File(IDEUtil.getInstallPath() + "/apploader/" + appid + "/load.ipa");
				if (file.exists()) {
					loaderPath = file.getAbsolutePath();
				}
			}
			
			// start connection service
			startService(dev, "com.apple.afc");
			
			// transfer package.
			ANBMessage transferMsg = new ANBMessage(ANBMessage.MSG_TYPE_TRANSFER_APP);
			transferMsg.m_pkgPath = loaderPath;
			ANBMsgProcessor.getInstance().processMsg(transferMsg);

			// start install service
			startService(dev, "com.apple.mobile.installation_proxy");
			
			// installApplication
			ANBMessage installMsg = new ANBMessage(ANBMessage.MSG_TYPE_INSTALL_APP);
			installMsg.m_pkgPath = loaderPath;
			ANBMsgProcessor.getInstance().processMsg(installMsg);
			
			// update progress.
			callBack.done(85, Messages.SyncingResource, Messages.Syncing);

			// push widget
			ANBMessage pushWgtMsg = new ANBMessage(ANBMessage.MSG_TYPE_PUSH_WGT, dev);
			pushWgtMsg.m_appId = appid;
			pushWgtMsg.m_pkgName = pkgName;
			pushWgtMsg.m_wgtPath = wgtPath;
			ANBMsgProcessor.getInstance().processMsg(pushWgtMsg);

			// push resource
			ANBMessage pushResMsg = new ANBMessage(ANBMessage.MSG_TYPE_PUSH_RES, dev);
			pushResMsg.m_resPath = startInfo + "/startInfo.txt";
			ANBMsgProcessor.getInstance().processMsg(pushResMsg);
			callBack.done(95, Messages.SyncingResource, Messages.Syncing);

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			callBack.done(100, Messages.AutoOpenApp, Messages.Finish);
			if (!pkgName.equals("com.apicloud.loader")) {
				createCustomAPPID(appid, ANBActivator.getCustomAPPIDPath());
			}
		}
	}
	
	public static void createCustomAPPID(String appID, String path) {
		if (appID == null) {
			return;
		}
		java.io.File info = new java.io.File(path);
		if (info.exists()) {
			info.delete();
		}
		try {
			info.createNewFile();
			FileOutputStream fos = new FileOutputStream(info);
			fos.write(appID.getBytes());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean checkInstalled(IOSDevice dev, final String appId) throws ANBException {
		String pkgName = "com.apicloud.loader";
		String pkgVersion =FileUtil.readVersion(com.apicloud.loader.platforms.ios.ANBActivator.getUpdateVersion());
		
		if (dev != null) {
			if (appId != null && loadMap != null) {
				CustomLoaderModel theLoader = loadMap.get(appId);
				if (theLoader != null) {
					pkgName = theLoader.getIpackagename();
					pkgVersion = theLoader.getVersion();
				}
			}
			
			if (!pkgName.equals("com.apicloud.loader")) {
				String oldCustomAppID = FileUtil.readCustomAPPID(ANBActivator.getCustomAPPIDPath());
				if (FileUtil.compare(appId, oldCustomAppID) != 0) {
					// start install service
					startService(dev, "com.apple.mobile.installation_proxy");
		
					// unInstall application
					ANBMessage msg = new ANBMessage(ANBMessage.MSG_TYPE_UNINSTALL_APP);
					msg.m_pkgName = pkgName;
					msg.m_dev = dev;
					ANBMsgProcessor.getInstance().processMsg(msg);
					return false;
				} 
			}
			ANBProvider.findAppRet = null;
			ANBMsgProcessor.getInstance().processMsg(new ANBMessage(ANBMessage.MSG_TYPE_FIND_APP, dev));
			DictionaryElement de = findAppRet.getValue();
			for (String key : de.keySet()) {
				if (key.isEmpty() || !key.equals(pkgName)) {
					continue;
				}
				getAppVersion(de.get(key));
				int reslut = FileUtil.compare(version, pkgVersion);
				if (reslut >= 0) {
					return true;
				} else {
					// start install service
					startService(dev, "com.apple.mobile.installation_proxy");
		
					// unInstall application
					ANBMessage msg = new ANBMessage(ANBMessage.MSG_TYPE_UNINSTALL_APP);
					msg.m_pkgName = pkgName;
					ANBMsgProcessor.getInstance().processMsg(msg);
					return false;
				}
			}
		}
		return false;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void getAppVersion(PElement pElement) {
		Map<String, PElement> maps = pElement.asDict();
		for (String mapKey : maps.keySet()) {
			if (mapKey.equals("CFBundleVersion")) {
				PElement element = (PElement) maps.get(mapKey);
				version = element.getValue().toString();
				return;
			}
		}
	}

	public void startService(IOSDevice dev, String serviceName) throws ANBException {
		ANBMessage startServiceMsg = new ANBMessage(ANBMessage.MSG_TYPE_START_SERVICE, dev);
		startServiceMsg.m_serviceName = serviceName;
		ANBMsgProcessor.getInstance().processMsg(startServiceMsg);
	}

	public synchronized static ANBProvider getANBProvider() {
		if (fANBProvider == null) {
			fANBProvider = new ANBProvider();
			fANBProvider.logListenning = false;
		}
		return fANBProvider;
	}
}
