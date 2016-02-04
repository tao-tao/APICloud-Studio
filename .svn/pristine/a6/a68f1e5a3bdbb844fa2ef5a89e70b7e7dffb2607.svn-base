/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.updatemanager.core;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.json.JSONException;
import org.json.JSONObject;
import org.eclipse.core.runtime.OperationCanceledException;

import com.apicloud.authentication.AuthenticActivator;
import com.apicloud.commons.model.Config;
import com.apicloud.commons.model.CustomLoaderModel;
import com.apicloud.commons.util.DownLoadUtil;
import com.apicloud.commons.util.FileUtil;
import com.apicloud.commons.util.IDEUtil;
import com.apicloud.updatemanager.Activator;
import com.apicloud.updatemanager.Messages;

public class CustomLoaderAction implements IObjectActionDelegate {
	private IStructuredSelection select;
	private String path;
	private String ip;
	private String id;
	private String cookie;
	private String pkgId;
	private String apackageName;
	private String ipackageName;
	private String version;
	private String android_log;
	private String ios_log;
	private boolean isTrue = true;
	private Job job;
	private Map<String, String> compileInfo = new HashMap<String, String>();
	private Object obj;

	public CustomLoaderAction() {
	}

	public CustomLoaderAction(IStructuredSelection select) {
		this.select = select;
	}

	public CustomLoaderAction(Object obj) {
		this.obj = obj;
	}

	@Override
	public void run(IAction action) {
		initInfo();
		compileLoader();
	}

	private void initInfo() {
		Properties p = AuthenticActivator.getProperties();
		setIp(p.getProperty("ip"));
		cookie = p.getProperty("cookie");
		Object object = null;
		if (select != null) {
			object = select.getFirstElement();
		} else if (obj != null) {
			object = obj;
		}
		IResource resource = (IResource) object;
		path = resource.getLocation().toOSString();

		File fileToRead = new File(path + File.separator + "config.xml");
		try {
			Config config = Config.loadXml(new FileInputStream(fileToRead));
			id = config.getId();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void compileLoader() {
		String message = Activator.network_instance.addUnpack(id, cookie,ip);
		System.out.println("appid = " + id + " addUnpack response = " + message);
		JSONObject json;
		try {
			json = new JSONObject(message);
			String status = json.getString("status");
			String msg=json.getString("code").trim();
			if (status.equals("0")) {
				MessageDialog.openError(Display.getDefault().getActiveShell(),
						Messages.FAILURE, Messages.SERVICEBUSY);
			} else if (status.equals("error")) {
				MessageDialog.openError(Display.getDefault().getActiveShell(),
						Messages.FAILURE, Messages.COMPILEFAILUREINFO);
			} else if(msg.equals("371")){
				MessageDialog.openError(Display.getDefault().getActiveShell(), Messages.FAILURE, "打包中！");
				
			}else {
				String body;
				body = json.getString("body");
				JSONObject result = new JSONObject(body);
				pkgId = result.getString("pkgId");
				System.err.println(pkgId);
				compileAndDownloadAppLoader();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void compileAndDownloadAppLoader() {
		job = new WorkspaceJob(Messages.CUSTOMERLOADER) {
			private String androidST;
			private String iosST;

			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor)
					throws CoreException {
				monitor.beginTask("package Resource:", 101);
				monitor.setTaskName(Messages.COMPILEFILEPATH);
				int i = 0;
				try {
					while (true) {
						if (monitor.isCanceled()) {
							return close();
						}
						String message = Activator.network_instance
								.getLoaderState(pkgId, cookie,ip);
						System.out.println("getLoaderState response = " + message);
						JSONObject json;
						json = new JSONObject(message);
						String status = json.getString("status");
						if (status.equals("0")) {
							final String errorStr = json.getString("msg");
							Display.getDefault().asyncExec(new Runnable() {

								@Override
								public void run() {
									MessageDialog.openError(Display
											.getCurrent().getActiveShell(),
											Messages.FAILURE, errorStr);
								}
							});
							return Status.CANCEL_STATUS;
						} else {
							String body = json.getString("body");
							JSONObject result = new JSONObject(body);
							androidST = result.getString("androidST");
							iosST = result.getString("iosST");

							if (androidST.equals("0") || iosST.equals("0")) {
								monitor.subTask(Messages.READYTOCOMPILE);
								if (i == 50) {
									continue;
								} else {
									i++;
									Thread.sleep(2000);
									monitor.worked(1);
								}
							} else if (androidST.equals("-2") || iosST.equals("-2")) {
								monitor.subTask(Messages.SERVICEQUEUING);
								if (i == 50) {
									continue;
								} else {
									i++;
									Thread.sleep(2000);
									monitor.worked(1);
								}
							} else if (androidST.equals("7")
									|| iosST.equals("7")) {

								Display.getDefault().syncExec(new Runnable() {

									@Override
									public void run() {
										MessageDialog
												.openError(Display.getCurrent()
														.getActiveShell(),
														"Error",
														Messages.COMMITTOCLOUD);
									}
								});
								isTrue = false;
								break;
							} else if (androidST.equals("1")
									&& iosST.equals("1")) {
								monitor.subTask(Messages.STARTCOMPILE);
								android_log = result.getString("android_log");
								ios_log = result.getString("ios_log");
								String apkPath = result.getString("apkPath");
								String ipaPath = result.getString("ipaPath");
								apackageName = result.getString("packageName");
								ipackageName = result.getString("iosAppIds");
								version = result.getString("version");
								if (i != 50) {
									i++;
									Thread.sleep(2000);
									monitor.worked(1);
									continue;
								}
								File file = new File(IDEUtil.getInstallPath()
										+ "/apploader");
								if (!file.exists()) {
									file.mkdirs();
								}

								monitor.subTask(Messages.DOWNLOADANDROID);

								String apkpath = IDEUtil.getInstallPath()
										+ "/apploader/" + id + "/load.apk";
								File apkfile = new File(apkpath);
								String ipapath = IDEUtil.getInstallPath()
										+ "/apploader/" + id + "/load.ipa";
								File ipafile = new File(ipapath);

								if (ipafile.exists() && apkfile.exists()) {
									// FileUtil.deleteFile(path);

									apkpath = IDEUtil.getInstallPath()
											+ "/apploadertemp/" + id
											+ "/load.apk";

									ipapath = IDEUtil.getInstallPath()
											+ "/apploadertemp/" + id
											+ "/load.ipa";

									boolean finished = DownLoadUtil
											.downZip1(apkPath,
													apkpath,// 下载路径需要修改为/apploder/appid/load.apk
													new SubProgressMonitor(
															monitor, 25));
									if (finished == false) {
										FileUtil.deleteFile(apkpath);
										throw new OperationCanceledException();
									}
									monitor.subTask("\u4E0B\u8F7DIOS Loader\u4E2D...");

									finished = DownLoadUtil
											.downZip1(ipaPath, ipapath,
													new SubProgressMonitor(
															monitor, 25));
									if (finished == false) {
										FileUtil.deleteFile(ipapath);
										throw new OperationCanceledException();
									}

									String tempapkfilePath = IDEUtil
											.getInstallPath()
											+ "/apploadertemp/"
											+ id
											+ "/load.apk";
									String targetapkPath = IDEUtil
											.getInstallPath()
											+ "/apploader/"
											+ id + "/load.apk";
									String tempipafilePath = IDEUtil
											.getInstallPath()
											+ "/apploadertemp/"
											+ id
											+ "/load.ipa";
									String targetipaPath = IDEUtil
											.getInstallPath()
											+ "/apploader/"
											+ id + "/load.ipa";
									String tempfolder = IDEUtil
											.getInstallPath()
											+ "/apploadertemp";
									
										FileUtil.deleteFile(targetapkPath);
										FileUtil.copyFile(tempapkfilePath,
												targetapkPath);
										FileUtil.deleteFile(targetipaPath);
										FileUtil.copyFile(tempipafilePath,
												targetipaPath);

										FileUtil.deleteDirectory(tempfolder);

								}

								else {

									boolean finished = DownLoadUtil
											.downZip1(apkPath,
													apkpath,// 下载路径需要修改为/apploder/appid/load.apk
													new SubProgressMonitor(
															monitor, 25));
									if (finished == false) {
										FileUtil.deleteFile(apkpath);
										throw new OperationCanceledException();
									}
									monitor.subTask("\u4E0B\u8F7DIOS Loader\u4E2D...");

									finished = DownLoadUtil
											.downZip1(ipaPath, ipapath,
													new SubProgressMonitor(
															monitor, 25));
									if (finished == false) {
										FileUtil.deleteFile(ipapath);
										throw new OperationCanceledException();
									}
								}

								saveApploaderInfo();
								isTrue = true;
								break;
							} else {
								Display.getDefault().asyncExec(new Runnable() {
									@Override
									public void run() {
										String androidMessage = compileInfo
												.get(androidST) == null ? ""
												: compileInfo.get(androidST);
										String iosMessage = compileInfo
												.get(iosST) == null ? ""
												: compileInfo.get(iosST);
										String message = androidMessage
												+ iosMessage;
										if (message.equals("")) {
											message = Messages.UNKOWNERROR;
										}
										if (!androidMessage.equals("")) {
											openErrorMessage(android_log);
										}
										if (!iosMessage.equals("")) {
											openErrorMessage(ios_log);
										}
									}
								});
								return Status.CANCEL_STATUS;
							}
						}
					}
					monitor.worked(1);
					monitor.done();
				} catch (UnsupportedEncodingException e) {
					return closeAndShowMessage(Messages.SERVICEBUSYERROR);
				} catch (OperationCanceledException e) {
					return closeAndShowMessage(Messages.DOWNLOADERROR);
				} catch (JSONException e1) {
					return closeAndShowMessage(Messages.DATAANALYERROR);
				} catch (IOException e) {
					return closeAndShowMessage(Messages.SERVICEOBTAINDATAERROR);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				return Status.OK_STATUS;
			}

			private IStatus close() {
				return Status.CANCEL_STATUS;
			}

			private IStatus closeAndShowMessage(final String errorMessage) {
				return Status.CANCEL_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
		job.getState();

		job.addJobChangeListener(new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				if (event.getResult().isOK() && isTrue)
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							MessageDialog.openInformation(null,
									Messages.PACKAGED, Messages.COMPILESUCCESS);
						}
					});
				else
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							MessageDialog.openError(null, Messages.EXCEPTION,
									Messages.COMPILEFAILURE);
						}
					});
			}
		});
	}

	private void saveApploaderInfo() {
		Properties p = com.apicloud.commons.model.Activator
				.getAppLoaderProperties();
		String value = apackageName + "|" + ipackageName + "&" + version;
		p.put(id, value);
		CustomLoaderModel model = new CustomLoaderModel();
		model.setApackagename(apackageName);
		model.setIpackagename(ipackageName);
		model.setVersion(version);
		com.apicloud.commons.model.Activator.getLoaderMap().put(id, model);
		com.apicloud.commons.model.Activator.storeloader(p);
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			select = (IStructuredSelection) selection;
		}
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	private void openErrorMessage(final String url) {
		Desktop desktop = Desktop.getDesktop();
		if ((Desktop.isDesktopSupported())
				&& (desktop.isSupported(Desktop.Action.BROWSE))) {
			try {
				URI uri = new URI(url);
				desktop.browse(uri);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
}
