/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.dialogs;

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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.json.JSONException;
import org.json.JSONObject;

import com.apicloud.authentication.AuthenticActivator;
import com.apicloud.commons.model.Config;
import com.apicloud.commons.model.CustomLoaderModel;
import com.apicloud.commons.util.DownLoadUtil;
import com.apicloud.commons.util.IDEUtil;
import com.apicloud.networkservice.NetWorkService;

public class CustomerLoaderDialog extends Dialog {
	private final FormToolkit formToolkit = new FormToolkit(
			Display.getDefault());
	private ComboViewer list;
	private IProject[] projects;
	private Shell parentShell;

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
	private NetWorkService network_instance;
	public CustomerLoaderDialog(Shell parentShell, IProject[] projects) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX);
		this.projects = projects;
		this.parentShell = parentShell;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.COMPILELOADER);

		Rectangle parentBounds = parentShell.getBounds();
		Rectangle shellBounds = newShell.getBounds();
		newShell.setLocation(parentBounds.x
				+ (parentBounds.width - shellBounds.width) / 2, parentBounds.y
				+ (parentBounds.height - shellBounds.height) / 2);
	}

	protected void createButtonsForButtonBar(Composite parent) {
		Button button = createButton(parent, 0,
				Messages.COMPILE, true);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		Button button_1 = createButton(parent, 1,
				Messages.RunSimulatorDialog_EXIT, true);
		button_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);
		comp.setLayout(new GridLayout(1, false));

		Composite composite = new Composite(comp, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));
		formToolkit.adapt(composite);
		formToolkit.paintBordersFor(composite);

		Composite composite_1 = new Composite(composite, SWT.NONE);
		composite_1.setLayout(new GridLayout(1, false));
		composite_1.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true,
				false, 1, 1));
		formToolkit.adapt(composite_1);
		formToolkit.paintBordersFor(composite_1);

		Composite composite_6 = new Composite(composite_1, SWT.NONE);
		composite_6.setLayout(new GridLayout(2, false));
		composite_6.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 1, 1));
		composite_6.setBounds(0, 0, 64, 64);
		formToolkit.adapt(composite_6);
		formToolkit.paintBordersFor(composite_6);

		Label lblNewLabel_project = new Label(composite_6, SWT.NONE);
		lblNewLabel_project.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
				false, false, 1, 1));
		formToolkit.adapt(lblNewLabel_project, true, true);
		lblNewLabel_project.setText(Messages.RunSimulatorDialog_PROJECT_NAME);

		this.list = new ComboViewer(composite_6, SWT.READ_ONLY);
		GridData gd_text1 = new GridData(SWT.FILL, SWT.CENTER, true, false, 1,
				1);
		gd_text1.widthHint = 181;
		this.list.getCombo().setLayoutData(gd_text1);
		this.list.setLabelProvider(new ProjectLabelProvider());
		this.list.setContentProvider(new ArrayContentProvider());
		this.list.setInput(projects);
		this.list.getCombo().select(0);
		formToolkit.adapt(list.getCombo(), true, true);

		return parent;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == 0) {
			StructuredSelection ss = (StructuredSelection) this.list
					.getSelection();
			IProject project = (IProject) ss.getFirstElement();
			path =project.getLocation().toString();
			File fileToRead = new File(path + File.separator + "config.xml");
			try {
				Config config = Config.loadXml(new FileInputStream(fileToRead));
				id = config.getId();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			initInfo();
			compileLoader(id);
			close();
			
			
		} else if (buttonId == 1) {
			close();
		}
	}
	
	private void initInfo() {
		Properties p =AuthenticActivator.getProperties();
		ip=p.getProperty("ip");
		setIp(p.getProperty("ip"));
		cookie = p.getProperty("cookie");
	}

	private void compileLoader(String id) {
		String param = "";
		param = "appId=" + id + "&" + "loader=1";
		String message =NetWorkService.getInstance().addUnpack(id, param,ip);
		JSONObject json;
		try {
			json = new JSONObject(message);
			String status = json.getString("status");
			if (status.equals("0")) {
				MessageDialog
						.openError(Display.getDefault().getActiveShell(),
								Messages.ERROR,
								Messages.SERVICEBUSY);
			} else if(status.equals("error")){
				MessageDialog
				.openError(Display.getDefault().getActiveShell(),
						Messages.ERROR,
						Messages.COMPILEERROR);

			}else {
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
						String message = network_instance.getLoaderState(pkgId, cookie,ip);
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
											"\u5931\u8D25", errorStr);
								}
							});
							return Status.CANCEL_STATUS;
						} else {
							String body = json.getString("body");
							JSONObject result = new JSONObject(body);
							androidST = result.getString("androidST");
							iosST = result.getString("iosST");

							if (androidST.equals("0") || iosST.equals("0")) {
								monitor.subTask(Messages.UNPACKINFO);
								if (i == 50) {
									continue;
								} else {
									i++;
									Thread.sleep(10000);
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
								DownLoadUtil.downZip1(apkPath,
										IDEUtil.getInstallPath()
												+ "/apploader/" + id
												+ "/load.apk",// 下载路径需要修改为/apploder/appid/load.apk
										new SubProgressMonitor(monitor, 25));
								DownLoadUtil.downZip1(ipaPath,
										IDEUtil.getInstallPath()
												+ "/apploader/" + id
												+ "/load.ipa",
										new SubProgressMonitor(monitor, 25));
								saveApploaderInfo();
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
										MessageDialog.openError(
														Display.getCurrent().getActiveShell(),
																Messages.ERROR,
														message
																+ Messages.BROWSERERROR);
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
									Messages.COMPILESUCCESS);
						}
					});
				else
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							MessageDialog.openError(null,
									Messages.PackageAppItemDialog_EXCEPTION,
									Messages.COMPILERROR);
						}
					});
			}
		});


	}

	private void saveApploaderInfo() {
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

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	
}
