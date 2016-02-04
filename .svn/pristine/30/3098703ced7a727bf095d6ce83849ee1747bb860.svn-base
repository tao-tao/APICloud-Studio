/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.ui.actions;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.json.JSONException;
import org.json.JSONObject;
import org.tigris.subversion.subclipse.core.util.SVNUtil;

import com.apicloud.authentication.AuthenticActivator;
import com.apicloud.commons.model.Config;
import com.apicloud.commons.model.CustomLoaderModel;
import com.apicloud.commons.util.DownLoadUtil;
import com.apicloud.commons.util.FileUtil;
import com.apicloud.commons.util.IDEUtil;
import com.apicloud.navigator.dialogs.CustomerLoaderDialog;
import com.apicloud.navigator.dialogs.Messages;
import com.apicloud.networkservice.ConnectionUtil;

public class CustomLoader implements IWorkbenchWindowActionDelegate {
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
	
	@Override
	public void run(IAction action) {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		projects = FilterProject(projects);
		if (projects.length == 0) {
			MessageDialog.openError(null, Messages.AddFeatureDialog_INFORMATION,
					Messages.CREATEAPP);
			return;

		} else if (projects.length == 1) {
			Object obj = projects[0];

			IResource resource = (IResource) obj;
			// String name=resource.getFullPath().toString().substring(1);
			path = resource.getLocation().toOSString();

			File fileToRead = new File(path + File.separator + "config.xml");
			try {
				Config config = Config.loadXml(new FileInputStream(fileToRead));
				id = config.getId();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			initInfo();
			compileLoader(id);

		} else {
			CustomerLoaderDialog dialog = new CustomerLoaderDialog(PlatformUI
					.getWorkbench().getActiveWorkbenchWindow().getShell(),
					projects);
			dialog.open();
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

	@Override
	public void dispose() {

	}

	@Override
	public void init(IWorkbenchWindow window) {

	}
	
	public void initInfo() {
		Properties p = AuthenticActivator.getProperties();
		this.ip = p.getProperty("ip");
		this.cookie = p.getProperty("cookie");
	}

	private IProject[] FilterProject(IProject[] projects) {
		List<IProject> list = new ArrayList<IProject>();
		for (IProject p : projects) {
			File config = new File(p.getLocation().toOSString()
					+ File.separator + "config.xml");
			if (config.exists()) {
				list.add(p);
			}
		}
		return list.toArray(new IProject[list.size()]);
	}

	public void compileLoader(String id) {
		String message = com.apicloud.navigator.Activator.network_instance.addUnpack(id, cookie,ip);
		JSONObject json;
		try {
			json = new JSONObject(message);
			String status = json.getString("status");
			if (status.equals("0")) {
				MessageDialog
						.openError(Display.getDefault().getActiveShell(),
								Messages.ERROR,
								Messages.SERVICEBUSY);
			} else if (status.equals("error")) {
				MessageDialog
						.openError(
								Display.getDefault().getActiveShell(),
								Messages.ERROR,
								Messages.COMPILEERROR);
			} else {
				String body;
				body = json.getString("body");
				JSONObject result = new JSONObject(body);
				pkgId = result.getString("pkgId");
				System.err.println(pkgId);
				compileAndDownloadAppLoader(id);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void compileAndDownloadAppLoader(final String id) {
		job = new WorkspaceJob(Messages.CUSTOMERLOADER) {
			private String androidST;
			private String iosST;
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor)
					throws CoreException {
				monitor.beginTask("package Resource:", 101);
				monitor.setTaskName(Messages.LOADERPATH);
				int i = 0;
				try {
					while (true) {
						if (monitor.isCanceled()) {
							return close();
						}
						String message = ConnectionUtil.sendPostRequest(
								"http://" + ip + "/getLoaderState", "pkgId="
										+ URLEncoder.encode(pkgId, "UTF-8"),
								cookie);
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
											Messages.ERROR, errorStr);
								}
							});
							return Status.CANCEL_STATUS;
						} else {
							String body = json.getString("body");
							JSONObject result = new JSONObject(body);
							androidST = result.getString("androidST");
							iosST = result.getString("iosST");

							if (androidST.equals("0") || iosST.equals("0")) {
								monitor.subTask(Messages.COMPILEREADY);
								if (i == 50) {
									continue;
								} else {
									i++;
									Thread.sleep(1000);
									monitor.worked(1);
								}
							} else if (androidST.equals("7")
									|| iosST.equals("7")) {

								Display.getDefault().syncExec(new Runnable() {

									@Override
									public void run() {
										MessageDialog
												.openError(Display
														.getCurrent()
														.getActiveShell(),
														"Error",
														Messages.COMMITTOCLOUD);
										
										
									}
								});
								isTrue=false;
								break;
							} else if (androidST.equals("1")
									&& iosST.equals("1")) {
								monitor.subTask(Messages.STARTTOCOMPILE);
								android_log = result.getString("android_log");
								ios_log = result.getString("ios_log");
								String apkPath = result.getString("apkPath");
								String ipaPath = result.getString("ipaPath");
								apackageName = result.getString("packageName");
								ipackageName = result.getString("iosAppIds");
								version = result.getString("version");
								if (i != 50) {
									i++;
									monitor.worked(1);
									continue;
								}
								File file = new File(IDEUtil.getInstallPath()
										+ "/apploader");
								if (!file.exists()) {
									file.mkdirs();
								}
								String apkpath=IDEUtil.getInstallPath()
										+ "/apploader/" + id
										+ "/load.apk";
								File loaderFile=new File(apkpath);
								if(loaderFile.exists()){
//								FileUtil.deleteFile(path);
									apkpath=IDEUtil.getInstallPath()
											+ "/apploadertemp/" + id
											+ "/load.apk";
								}
								monitor.subTask(Messages.ANDROIDDOWNLOAD);
								DownLoadUtil.downZip1(apkPath,
										apkpath,// 下载路径需要修改为/apploder/appid/load.apk
										new SubProgressMonitor(monitor, 25));
								monitor.subTask("\u4E0B\u8F7DIOS Loader\u4E2D...");
								String ipapath=IDEUtil.getInstallPath()
										+ "/apploader/" + id
										+ "/load.ipa";
								File ipafile=new File(IDEUtil.getInstallPath()
										+ "/apploader/" + id
										+ "/load.ipa");
								if(ipafile.exists()){
//									FileUtil.deleteFile(path);
									ipapath=IDEUtil.getInstallPath()
												+ "/apploadertemp/" + id
												+ "/load.ipa";
									}
								
								DownLoadUtil.downZip1(ipaPath,
										ipapath,
										new SubProgressMonitor(monitor, 25));
								String tempapkfilePath=IDEUtil.getInstallPath()
										+ "/apploadertemp/" + id
										+ "/load.apk";
								String sourceapkPath=IDEUtil.getInstallPath()
										+ "/apploader/" + id
										+ "/load.apk";
								String tempipafilePath=IDEUtil.getInstallPath()
										+ "/apploadertemp/" + id
										+ "/load.apk";
								String sourceipaPath=IDEUtil.getInstallPath()
										+ "/apploader/" + id
										+ "/load.apk";
								String tempfolder=IDEUtil.getInstallPath()
										+ "/apploadertemp";
								File tempapkFile=new File(tempapkfilePath);
								File tempipaFile=new File(tempipafilePath);
								if(tempapkFile.exists()||tempipaFile.exists()){
									FileUtil.deleteFile(sourceapkPath);
									FileUtil.copyFile(tempapkfilePath, sourceapkPath);
									FileUtil.deleteFile(sourceipaPath);
									FileUtil.copyFile(tempipafilePath, sourceipaPath);
									
									FileUtil.deleteDirectory(tempfolder);
								}
								saveApploaderInfo(id);
								isTrue=true;
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
											message = Messages.UNKNOWERROR;
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
					return closeAndShowMessage(Messages.SERVICEBUSY);
				} catch (JSONException e1) {
					return closeAndShowMessage(Messages.DATAANALYSISERROR);
				} catch (IOException e) {
					return closeAndShowMessage(Messages.SERVICEDATAERROR);
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
				if (event.getResult().isOK()&&isTrue)
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							MessageDialog.openInformation(null,
									Messages.PackageAppItemDialog_SUCESS,
									Messages.CUSTOMERSUCCESS);
						}
					});
				else
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							MessageDialog.openError(null,
									Messages.PackageAppItemDialog_EXCEPTION,
									Messages.COMPILEFAILURE);
						}
					});
			}
		});


	}

	private void saveApploaderInfo(String id) {
		Properties p = com.apicloud.commons.model.Activator.getAppLoaderProperties();
		String value = apackageName + "|" + ipackageName + "&" + version;
		p.put(id, value);
		CustomLoaderModel model = new CustomLoaderModel();
		model.setApackagename(apackageName);
		model.setIpackagename(ipackageName);
		model.setVersion(version);
		com.apicloud.commons.model.Activator.getLoaderMap().put(id, model);
		com.apicloud.commons.model.Activator.storeloader(p);
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
}
