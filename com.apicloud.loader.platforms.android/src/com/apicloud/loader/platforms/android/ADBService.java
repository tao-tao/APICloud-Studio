/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.loader.platforms.android;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import com.aptana.scripting.ScriptLogger;
import com.aptana.scripting.ui.ScriptingConsole;

import com.apicloud.commons.util.FileUtil;
import com.apicloud.commons.util.IDEUtil;
import com.apicloud.commons.util.OS;
import com.apicloud.commons.model.CustomLoaderModel;
import com.apicloud.loader.api.IProgressCallBack;
import com.apicloud.networkservice.ConnectionUtil;

public class ADBService{
	public static ADBService fADBService = null;
	private static final String LOADER_WGT_PATH = "//sdcard//UZMap//wgt//";
	private static final String LOADER_WGT_USER_PATH = "//sdcard//UZMap//A6965066952332//";
	private static Map<String, CustomLoaderModel> loadMap = com.apicloud.commons.model.Activator
			.getLoaderMap();
	
	public boolean isStarted = false;
	public boolean logListenning = false;
	Thread logger_ios = null;

	public void start() throws ADBException {
		if (this.isStarted) {
			return;
		}
		ADBCommand startCmd = new ADBCommand(ADBCommand.CMD_TYPE_START);
		ADBCmdProcessor.callProcess(startCmd);
		this.isStarted = true;
		Thread shutdown = new Thread(new Runnable() {
			public void run() {
				try {
					ADBCmdProcessor.forceKillProcess("adb");
					ADBService.getADBService().isStarted = false;
				} catch (Exception ex) {
				}
			}
		});
		Runtime.getRuntime().addShutdownHook(shutdown);
	}

	public void kill() throws ADBException {
		ADBCommand killCmd = new ADBCommand(ADBCommand.CMD_TYPE_KILL);
		ADBCmdProcessor.callProcess(killCmd);
		this.isStarted = false;
	}

	public void findDevices() throws ADBException {
		ADBCmdProcessor.callProcess(new ADBCommand(ADBCommand.CMD_TYPE_DEVICES));
		Set<String> devNames = AndroidDevice.connectedDevices.keySet();
		for (String name : devNames) {
			System.out.println(name);
		}
	}

	public void load(String uuid, String startInfo, String appid,
			String wgtPath,IProgressCallBack callBack) throws ADBException {
		
		// push widget
		ADBCmdProcessor.callProcess(new ADBCommand(ADBCommand.CMD_TYPE_PUSH,
				new String[] { "-s", uuid, "push", wgtPath,
						LOADER_WGT_PATH + appid }));
		callBack.done(70,Messages.CopyingResource,Messages.Syncing);
		
		// push startInfo
		ADBCmdProcessor.callProcess(new ADBCommand(ADBCommand.CMD_TYPE_PUSH,
				new String[] { "-s", uuid, "push", startInfo, LOADER_WGT_USER_PATH }));
		callBack.done(85,Messages.InstallApp,Messages.Syncing);
		
		String pkgName = "com.apicloud.apploader";
		String pkgVersion = FileUtil.readVersion(com.apicloud.loader.platforms.android.ADBActivator.getUpdateVersion());
		String activityName = "com.apicloud.apploader/com.uzmap.pkg.EntranceActivity";
		String loaderPath = ADBActivator.getBasePath();
		String targetPath = "/sdcard/UZMap/";
		
		if (appid != null && loadMap != null) {
			CustomLoaderModel theLoader = loadMap.get(appid);
			if (theLoader != null) {
				pkgName = theLoader.getApackagename();
				pkgVersion = theLoader.getVersion();
				activityName = pkgName + "/com.uzmap.pkg.EntranceActivity";
				File loaderfile = new File(IDEUtil.getInstallPath() + "/apploader/" + appid + "/load.apk");
				if (loaderfile.exists()) {
					loaderPath = loaderfile.getAbsolutePath();
				}
			}
		}
		
		// stop app.
		String[] stopParameters = { "-s", uuid, "shell", "am", "force-stop",
				pkgName };
		ADBCommand stopCommand = new ADBCommand(ADBCommand.CMD_TYPE_STOP_APP, stopParameters);
		ADBCmdProcessor.callProcess(stopCommand);
		
		// check version.
		AndroidDevice.loaderVersion = null;
		String[] parameters = { "-s", uuid, "shell", "dumpsys", "package", pkgName };
		ADBCommand checkVersionCommand = new ADBCommand(
				ADBCommand.CMD_TYPE_APP_VERSION, parameters);
		ADBCmdProcessor.callProcess(checkVersionCommand);
		callBack.done(95,Messages.InstallApp,Messages.Syncing);
		
		int reslut = FileUtil.compare(pkgVersion, AndroidDevice.loaderVersion);
		if (reslut > 0) {
			
			// unInstall app.
			ADBCommand unInstallCommand = new ADBCommand(
					ADBCommand.CMD_TYPE_UNINSTALL, new String[] { "-s",
							uuid, "uninstall", pkgName });
			ADBCmdProcessor.callProcess(unInstallCommand);

			// push apk.
			if (OS.isMacintosh()) {
				targetPath = targetPath + "load.apk";
			}
			ADBCommand pushCommand = new ADBCommand(
					ADBCommand.CMD_TYPE_PUSH, new String[] { "-s", uuid,
							"push", loaderPath, targetPath });
			ADBCmdProcessor.callProcess(pushCommand);

			// install app.
			if (OS.isWindows()) {
				targetPath = targetPath + "load.apk";
			}
			ADBCommand installCommand = new ADBCommand(
					ADBCommand.CMD_TYPE_INSTALL, new String[] { "-s", uuid,
							"shell", "pm", "install", targetPath });
			ADBCmdProcessor.callProcess(installCommand);

			// remove apk.
			ADBCommand rmCommand = new ADBCommand(
					ADBCommand.CMD_TYPE_REMOVE, new String[] { "-s", uuid,
							"shell", "rm", targetPath });
			ADBCmdProcessor.callProcess(rmCommand);
		}
		
		// start app.
		ADBCommand startAppCommand = new ADBCommand(
				ADBCommand.CMD_TYPE_START_APP, new String[]{ "-s", uuid, "shell", "am",
						"start", "-W", "-n", activityName });
		ADBCmdProcessor.callProcess(startAppCommand);
	}

	public void syncLog() throws ADBException {
		if (!logListenning) {
			ScriptingConsole.getInstance().clear();
			ScriptLogger.logError("开启日志监听...");
			

			
			logListenning = true;
			Job startADBJob = new Job("Start ADB log") {
				protected IStatus run(IProgressMonitor monitor) {
					if (isStarted) {
						try {

						    logger_ios = new Thread() {
								public void run() {
									try {
										System.out.println("begin to get log from iPhone");
										for (;;) {
											String message = ConnectionUtil.getConnectionMessage(
													"http://127.0.0.1:2222", null);
											System.out.println("[iPhone log] "+ message);
											ScriptLogger.logError("[iPhone log] "+ message);
											Thread.sleep(1000);
										}
									} catch (InterruptedException e) {
										return;
									}
								}
							};
							logger_ios.start();
							ADBCmdProcessor.callProcess(new ADBCommand(ADBCommand.CMD_TYPE_LOGCAT));
							logListenning = true;
						} catch (ADBException e) {
							e.printStackTrace();
							logListenning = false;
							//throw adbEx;
						}
					}
					return Status.OK_STATUS;
				}
			};
			
			startADBJob.setSystem(true);
			startADBJob.schedule(500L);
			startADBJob.addJobChangeListener(new JobChangeAdapter() {
				public void done(IJobChangeEvent event) {
					if (event.getResult().isOK()) {
						logger_ios.interrupt();
						ScriptLogger.logError("\u65AD\u5F00\u8FDE\u63A5...");
						logListenning = false;
					}
				}
			});

		}
	}

	public synchronized static ADBService getADBService() {
		if (fADBService == null) {
			fADBService = new ADBService();
		}
		return fADBService;
	}
}
