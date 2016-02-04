/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.updatemanager.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import com.apicloud.authentication.AuthenticActivator;
import com.apicloud.commons.model.IDEUpdateModel;
import com.apicloud.commons.util.DownLoadUtil;
import com.apicloud.commons.util.FileUtil;
import com.apicloud.commons.util.IDEUtil;
import com.apicloud.commons.util.UtilActivator;
import com.apicloud.commons.util.ZipUtil;
import com.apicloud.commons.util.OS;
import com.apicloud.loader.platforms.android.ADBActivator;
import com.apicloud.loader.platforms.ios.ANBActivator;
import com.apicloud.updatemanager.Activator;
import com.apicloud.updatemanager.Messages;
import com.apicloud.updatemanager.dialog.IDEUpdateDialog;

public class CheckUpateManager {
	public static boolean isBaseUpdate;
	public static boolean reStart;
	public static boolean isUpdate;
	public static boolean isFinished;
	private boolean isAuto;
	private String oUpdateUrl;
	private String aUpdateUrl;
	private String iUpdateUrl;
	private String bUpdateUrl;
	private String iloaderVersion;
	private String aloaderVersion;
	private String thirdVersion;
	private String basicVersion;
	private String updateType;
	private String third_updateType;
	private String basic_updateType;
	private String ip;
	private IDEUpdateDialog dialog;

	public void setAuto(boolean isAuto) {
		this.isAuto = isAuto;
	}

	private CheckUpateManager() {
	}

	private static CheckUpateManager manager = null;

	public static CheckUpateManager getInstance() {
		if (manager == null) {
			manager = new CheckUpateManager();
		}
		return manager;
	}

	public boolean updateLoder(final int id, IProgressMonitor monitor)
			throws JSONException {
		boolean downloadCompleteAPK = false;
		boolean downloadCompleteIPA = false;
		Bundle androidbundle = Platform
				.getBundle("com.apicloud.loader.platforms.android");
		Bundle iosbundle = Platform
				.getBundle("com.apicloud.loader.platforms.ios");
		String andoridloadpath = "";
		String iosloadpath = "";
		try {
			andoridloadpath = new File(FileLocator.toFileURL(
					androidbundle.getResource("base/version.txt")).getFile())
					.getAbsolutePath();
			iosloadpath = new File(FileLocator.toFileURL(
					iosbundle.getResource("base/version.txt")).getFile())
					.getAbsolutePath();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if (thirdVersion == null || !reStart) {
			thirdVersion = IDEUpdateModel.VERSION;
		}

		if(ip == null){
			ip = getIp();
		}

		String message = Activator.network_instance
				.getUpdateLoaderMsg(thirdVersion,ip);
		JSONObject json = new JSONObject(message);
		String status = json.getString("status");
		if (status.equals("1")) {
			String body = json.getString("result");
			JSONObject result = new JSONObject(body);
			IDEUpdateModel model = new IDEUpdateModel();
			model.setVersion(IDEUpdateModel.VERSION);

			try {
				iUpdateUrl = result.getString("iloaderUrl");
			} catch (JSONException e) {
				e.printStackTrace();
			}

			aUpdateUrl = result.getString("aloaderUrl");

			String dropinsPath = IDEUtil.getInstallPath() + "dropins";
			monitor.beginTask(Messages.DOWNLOADOFFICIALLOADER,
					IProgressMonitor.UNKNOWN);
			try {
				if (iUpdateUrl != null && !"".equals(iUpdateUrl)
						&& (id == 0 || id == 2)) {
					reStart = false;
					downloadCompleteIPA = DownLoadUtil.downZip1(iUpdateUrl,
							com.apicloud.loader.platforms.ios.ANBActivator
									.getUpdateBasePath(),
							new SubProgressMonitor(monitor, 300));
					createVersionTxt(getIloaderVersion(), iosloadpath);
					monitor.worked(30);
				} else {
					downloadCompleteIPA = true;
				}

				if (aUpdateUrl != null && !"".equals(aUpdateUrl)
						&& (id == 0 || id == 1)) {
					reStart = false;
					downloadCompleteAPK = DownLoadUtil.downZip1(aUpdateUrl,
							com.apicloud.loader.platforms.android.ADBActivator
									.getUpdateBasePath(),
							new SubProgressMonitor(monitor, 300));
					createVersionTxt(getAloaderVersion(), andoridloadpath);
					monitor.worked(30);
				} else {
					downloadCompleteAPK = true;
				}

				refreshBundleInfo(
						dropinsPath + "/bundles.info",
						IDEUtil.getInstallPath()
								+ "configuration/org.eclipse.equinox.simpleconfigurator/bundles.info");
			} catch (Exception e) {

			}

			monitor.worked(30);
			monitor.done();
		}

		if (downloadCompleteIPA && downloadCompleteAPK) {
			Activator
					.getDefault()
					.getLog()
					.log(new Status(IStatus.OK, Activator.PLUGIN_ID, 0, "ok",
							null));

			return true;
		} else {
			Activator
					.getDefault()
					.getLog()
					.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 1,
							"fail", null));

			if (downloadCompleteIPA == false) {
				/* delete damaged ipa */
				File file = new File(
						com.apicloud.loader.platforms.ios.ANBActivator
								.getUpdateBasePath());
				if (file.exists())
					file.delete();
			}

			if (downloadCompleteAPK == false) {
				/* delete damaged apk */
				File file = new File(
						com.apicloud.loader.platforms.android.ADBActivator
								.getUpdateBasePath());
				if (file.exists())
					file.delete();
			}

			return false;
		}
	}

	private void refreshBundleInfo(String sourcePath, String targetPaht)
			throws IOException {

		copyFile(new File(sourcePath), new File(targetPaht));

	}
	
	private boolean isJava_32bit() {
		Properties props = System.getProperties();
		Iterator iter = props.keySet().iterator();
		String bitNum = null;
		while (iter.hasNext()) {
			String key = (String)iter.next();
			if (key.equals("sun.arch.data.model"))
				bitNum = "-d"+props.getProperty(key);
		}
		if (bitNum.equals("-d32")) 
			return true;
		return false;
	}

	private IDEUpdateModel getIDEUpdate() throws JSONException {
		if (thirdVersion == null || !reStart) {
			thirdVersion = IDEUpdateModel.VERSION;
		}
		String message="";
		String ip=AuthenticActivator.getProperties().getProperty("ip");
		if(OS.isWindows()){
			message = Activator.network_instance.getUpdateMsg(thirdVersion,
					ADBActivator.getUpdateVersion(),
					ANBActivator.getUpdateVersion(), getBasePath(),ip);
		}else{
			message=Activator.network_instance.getMAcUpdateMsg(thirdVersion,
					ADBActivator.getUpdateVersion(),
					ANBActivator.getUpdateVersion(), getBasePath(),ip);
		}
		
		UtilActivator.logger.info(getBasePath());
		System.out.println(message);
		JSONObject json = new JSONObject(message);
		String status = json.getString("status");
		if (status.equals("1")) {
			String body = json.getString("result");

			JSONObject result = new JSONObject(body);
			IDEUpdateModel model = new IDEUpdateModel();
			model.setVersion(IDEUpdateModel.VERSION);
			long time = result.getLong("ct");
			Date date = new Date(time);
			String d = DateFormat.getDateInstance(DateFormat.DEFAULT).format(
					date);
			model.setDate(d);
			String desc = "";
			String baseDesc = null;
			try {
				if (OS.isWindows()) {

					basic_updateType = result.getString("basicForceStatus");
				} else {
					if (isJava_32bit())
					    basic_updateType = result.getString("basic_32macForceStatus");
					else
						basic_updateType = result.getString("basic_macForceStatus");
				}
			} catch (JSONException e) {
				basic_updateType = "-1";
			}
			try {
				if (OS.isWindows()) {
					updateType = result.getString("thirdForceStatus");
				} else {
					updateType = result.getString("third_macForceStatus");
				}

			} catch (JSONException e) {

			}
			if (basic_updateType != "-1") {
				model.setType(basic_updateType);
			} else {
				model.setType(updateType);
			}
			try {
				if (OS.isWindows()) {
					baseDesc = result.getString("basicUpdateInfo");
				} else {
					if(isJava_32bit())
					    baseDesc = result.getString("basic_32macUpdateInfo");
					else
						baseDesc = result.getString("basic_macUpdateInfo");
				}
			} catch (JSONException e) {
			}
			try {
				if (OS.isWindows()) {
					desc += result.getString("thirdUpdateInfo");
				} else {
					desc += result.getString("third_macUpdateInfo");
				}
			} catch (JSONException e) {
			}
			try {
				desc += result.getString("aloaderUpdateInfo");
			} catch (JSONException e) {
			}
			try {
				desc += result.getString("iloaderUpdateInfo");
			} catch (JSONException e) {
			}
			model.setText(baseDesc == null ? desc : baseDesc);
			try {
				if (OS.isWindows()) {
					bUpdateUrl = result.getString("basicUrl");
				} else {
					if(isJava_32bit())
					    bUpdateUrl = result.getString("basic_32macUrl");
					else
						bUpdateUrl = result.getString("basic_macUrl");
				}
				isBaseUpdate = true;
			} catch (JSONException e) {
			}
			try {
				if (OS.isWindows()) {
					oUpdateUrl = result.getString("thirdUrl");
				} else {
					oUpdateUrl = result.getString("third_macUrl");
				}

			} catch (JSONException e) {
			}
			try {
				iUpdateUrl = result.getString("iloaderUrl");
			} catch (JSONException e) {
			}
			try {
				aUpdateUrl = result.getString("aloaderUrl");
			} catch (JSONException e) {
			}
			try {
				iloaderVersion = result.getString("iloaderVersion");
			} catch (JSONException e) {
			}
			try {
				aloaderVersion = result.getString("aloaderVersion");
			} catch (JSONException e) {
			}
			try {
				if (OS.isWindows()) {
					thirdVersion = result.getString("thirdVersion");
				} else {
					thirdVersion = result.getString("third_macVersion");
				}
				model.setVersion(thirdVersion);
			} catch (JSONException e) {
			}
			try {
				if (OS.isWindows()) {
					basicVersion = result.getString("basicVersion");
				} else {
					if(isJava_32bit())
						basicVersion = result.getString("basic_32macVersion");
					else
						basicVersion = result.getString("basic_macVersion");
				}
				String version = FileUtil.readVersion(IDEUtil.getInstallPath()
						+ "download/update.txt");
				if (basicVersion.equals(version)) {
					return null;
				}
				model.setVersion(basicVersion);
			} catch (JSONException e) {
			}

			return model;
		}

		return null;
	}

	private String getBasePath() {
		return IDEUtil.getInstallPath() + "download/version.txt";
	}

	public void checkIDEUpdate() {
		if (isFinished) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(null,
							Messages.TIPINFORMATION, Messages.UPDATEINFO);
				}
			});
			return;
		}
		if (!isUpdate) {
			isUpdate = true;
			Job job = new WorkspaceJob("check out") {
				@Override
				public IStatus runInWorkspace(IProgressMonitor monitor)
						throws CoreException {
					IDEUpdateModel model = null;

					try {
						model = getIDEUpdate();
					} catch (JSONException e) {
						return Status.CANCEL_STATUS;
					}

					if (model == null || oUpdateUrl == null
							&& iUpdateUrl == null && aUpdateUrl == null
							&& bUpdateUrl == null) {
						if (!isAuto) {
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									MessageDialog.openInformation(null,
											Messages.TIPINFORMATION,
											Messages.LATESTVERSION);
								}
							});
						}
						return Status.OK_STATUS;
					}
					if (dialog == null) {
						dialog = new IDEUpdateDialog(null, model,
								CheckUpateManager.this);
					}
					dialog.setModel(model);
					
					if ("1".equals(basic_updateType)||"1".equals(third_updateType)) {
						IDEUpdate();
					} else {

						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								dialog.open();
							}
						});

					}
					return Status.OK_STATUS;
				}

			};
			job.schedule(100L);
			job.addJobChangeListener(new JobChangeAdapter() {
				public void done(IJobChangeEvent event) {
					isUpdate = false;
				}
			});
		}
	}

	public void IDEUpdate() {
		if (!isFinished) {
			isFinished = true;
			Job job = new WorkspaceJob("update") {

				@Override
				public IStatus runInWorkspace(IProgressMonitor monitor)
						throws CoreException {
					try {

						String dropinsPath = IDEUtil.getInstallPath()
								+ "dropins";
						String jarPath = IDEUtil.getInstallPath()
								+ "UZTools/update.jar";
						String downloadPath = IDEUtil.getInstallPath()
								+ "download/base.zip";
						String oZipName = dropinsPath + "/oUpdate.zip";
						// String bZipName = dropinsPath + "/bUpdate.zip";
						int fileSize = 1020;
						if (bUpdateUrl != null && !"".equals(bUpdateUrl)) {
							monitor.beginTask("Download Resource:", fileSize);
							
							boolean finished = DownLoadUtil.downZip1(bUpdateUrl, downloadPath,
									new SubProgressMonitor(monitor, 1000));
							
							if (finished == false) {
								FileUtil.deleteFile(downloadPath);
								throw new org.eclipse.core.runtime.OperationCanceledException();
							}
							
							createVersionTxt(basicVersion,
									IDEUtil.getInstallPath()
											+ "download/update.txt");
							monitor.worked(10);
							createVersionTxt(basicVersion, getBasePath());
							isBaseUpdate = true;
							monitor.worked(10);
							monitor.setTaskName("Finish");
							monitor.done();
							return Status.OK_STATUS;
						}
						monitor.beginTask("Download Resource", fileSize);
						if (iUpdateUrl != null && !"".equals(iUpdateUrl)) {
							reStart = false;
							boolean finished = DownLoadUtil.downZip1(
											iUpdateUrl,
											com.apicloud.loader.platforms.ios.ANBActivator
													.getUpdateBasePath(),
											new SubProgressMonitor(monitor, 300));
							if (finished == false) {
								FileUtil.deleteFile(com.apicloud.loader.platforms.ios.ANBActivator
										.getUpdateBasePath());
								throw new org.eclipse.core.runtime.OperationCanceledException();
							}
							createVersionTxt(
									iloaderVersion,
									com.apicloud.loader.platforms.ios.ANBActivator
											.getUpdateVersion());
							monitor.worked(30);
						} else {
							monitor.worked(330);
						}
						if (aUpdateUrl != null && !"".equals(aUpdateUrl)) {
							reStart = false;
							boolean finished = DownLoadUtil.downZip1(
											aUpdateUrl,
											com.apicloud.loader.platforms.android.ADBActivator
													.getUpdateBasePath(),
											new SubProgressMonitor(monitor, 300));
							if (finished == false) {
								FileUtil.deleteFile(com.apicloud.loader.platforms.android.ADBActivator
										.getUpdateBasePath());
								throw new org.eclipse.core.runtime.OperationCanceledException();
							}
							createVersionTxt(
									aloaderVersion,
									com.apicloud.loader.platforms.android.ADBActivator
											.getUpdateVersion());
							monitor.worked(30);
						} else {
							monitor.worked(330);
						}
						if (oUpdateUrl != null && !"".equals(oUpdateUrl)) {
							reStart = true;
							boolean finished = DownLoadUtil.downZip1(oUpdateUrl, oZipName,
									new SubProgressMonitor(monitor, 300));
							if (finished == false) {
								FileUtil.deleteFile(oZipName);
								throw new org.eclipse.core.runtime.OperationCanceledException();
							}
							
							ZipUtil.unzip(oZipName, dropinsPath);
							updateJar(dropinsPath + "/update.jar", jarPath);
							createVersionTxt(thirdVersion,
									IDEUtil.getInstallPath()
											+ "download/osgi.txt");
							monitor.worked(30);
						} else {
							monitor.worked(330);
						}
						refreshBundleInfo(
								dropinsPath + "/bundles.info",
								IDEUtil.getInstallPath()
										+ "configuration/org.eclipse.equinox.simpleconfigurator/bundles.info");
						monitor.worked(30);
						monitor.done();
					} catch (IOException ex) {
						ex.printStackTrace();
						Activator
								.getDefault()
								.getLog()
								.log(new Status(IStatus.OK,
										Activator.PLUGIN_ID, 0,
										ex.getMessage(), null));
						return Status.CANCEL_STATUS;
					}
					Activator
							.getDefault()
							.getLog()
							.log(new Status(IStatus.OK, Activator.PLUGIN_ID, 0,
									"ok", null));

					return Status.OK_STATUS;
				}

				private void updateJar(String source, String target)
						throws IOException {
					File sourceFile = new File(source);
					if (sourceFile.exists()) {
						File targetFile = new File(target);
						copyFile(sourceFile, targetFile);
						sourceFile.delete();
					}
				}

				private void refreshBundleInfo(String sourcePath,
						String targetPaht) throws IOException {
					copyFile(new File(sourcePath), new File(targetPaht));
				}
			};
			job.setUser(false);
			job.schedule(1000L);
			job.addJobChangeListener(new JobChangeAdapter() {
				public void done(IJobChangeEvent event) {
					if (event.getResult().isOK()) {
						if (isBaseUpdate) {
							isBaseUpdate = false;
							isFinished = false;
							bUpdateUrl = null;
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									if (MessageDialog.openConfirm(null,
											Messages.FINISH,
											Messages.FINISHINFO)) {
										runExe(basicVersion);

									}
								}
							});
							return;
						}
						if (!reStart) {
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									isFinished = false;
									reStart = false;
									oUpdateUrl = null;
									iUpdateUrl = null;
									aUpdateUrl = null;
									MessageDialog.openInformation(null,
											Messages.FINISH,
											Messages.UPDATEFINISHED);
								}
							});
							return;
						}
						isFinished = false;
						oUpdateUrl = null;
						iUpdateUrl = null;
						aUpdateUrl = null;
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								if (MessageDialog.openConfirm(null,
										Messages.FINISH,
										Messages.FINISHEDTORESTART)) {
									runExe();
								}
							}
						});
					} else {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								MessageDialog
										.openInformation(null,
												Messages.FAILURE,
												Messages.FAILUREINIFO);
							}
						});
						isFinished = false;
					}
				}

			});
		}
	}

	public void runExe(String version) {
		String ide_Home = IDEUtil.getInstallPath();
		String jrePath = ide_Home + "jre";
		String java_home = "cmd.exe /C set JAVA_HOME=" + jrePath;
		String path = "&&set PATH="
				+ jrePath
				+ "\\bin;C:\\WINDOWS\\system32;C:\\WINDOWS;C:\\WINDOWS\\System32\\Wbem";
		String jarPath = ide_Home + "UZTools" + File.separator + "update.jar";
		String className = "com.apicloud.exe.update.UpdateIDE";
		String cmd = "java -cp " + "\"" + jarPath + "\"" + " " + className
				+ " " + "\"" + ide_Home + "/\"";
		try {
			createVersionTxt(version, getBasePath());
			if (OS.isWindows()) {
				Runtime.getRuntime().exec(java_home + path + "&&" + cmd);
			} else {
				cmd = "java -cp " + jarPath + " " + className + " " + ide_Home
						+ "/";
				Runtime.getRuntime().exec(cmd);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void runExe() {
		String ide_Home = IDEUtil.getInstallPath();
		String jrePath = ide_Home + "jre";
		String java_home = "cmd.exe /C set JAVA_HOME=" + jrePath;
		String path = "&&set PATH="
				+ jrePath
				+ "\\bin;C:\\WINDOWS\\system32;C:\\WINDOWS;C:\\WINDOWS\\System32\\Wbem";
		String jarPath = ide_Home + "UZTools" + File.separator + "update.jar";
		String className = "com.apicloud.exe.update.UpdateIDE";
		String cmd = "java -cp " + "\"" + jarPath + "\"" + " " + className
				+ " " + "\"" + ide_Home + "/\"" + " 1";
		try {
			if (OS.isWindows()) {
				Runtime.getRuntime().exec(java_home + path + "&&" + cmd);
			} else {
				cmd = "java -cp " + jarPath + " " + className + " " + ide_Home
						+ "/" + " 1";
				Runtime.getRuntime().exec(cmd);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getIp() {
		if (ip == null) {
			Properties p = AuthenticActivator.getProperties();
			ip = p.getProperty("ip");
		}
		return ip;
	}

	private void createVersionTxt(String id, String path) {
		if (id == null) {
			return;
		}
		java.io.File info = new java.io.File(path);
		if (info.exists()) {
			info.delete();
		}
		try {
			info.createNewFile();
			FileOutputStream fos = new FileOutputStream(info);
			fos.write(id.getBytes());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getIloaderVersion() {
		return iloaderVersion;
	}

	public void setIloaderVersion(String iloaderVersion) {
		this.iloaderVersion = iloaderVersion;
	}

	public String getAloaderVersion() {
		return aloaderVersion;
	}

	public void setAloaderVersion(String aloaderVersion) {
		this.aloaderVersion = aloaderVersion;
	}

	private void copyFile(File sourcefile, File targetFile) throws IOException {
		if (!sourcefile.exists()) {
			return;
		}
		if (targetFile.exists()) {
			targetFile.delete();
		}
		FileInputStream input = new FileInputStream(sourcefile);
		BufferedInputStream inbuff = new BufferedInputStream(input);
		FileOutputStream out = new FileOutputStream(targetFile);
		BufferedOutputStream outbuff = new BufferedOutputStream(out);
		byte[] b = new byte[1024];
		int len = 0;
		while ((len = inbuff.read(b)) != -1) {
			outbuff.write(b, 0, len);
		}
		outbuff.flush();
		inbuff.close();
		outbuff.close();
		out.close();
		input.close();
	}
}
