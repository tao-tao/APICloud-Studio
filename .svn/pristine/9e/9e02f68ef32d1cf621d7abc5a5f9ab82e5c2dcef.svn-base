/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.ui.delegate;

import java.util.Properties;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;

import com.apicloud.authentication.AuthenticActivator;
import com.apicloud.navigator.dialogs.Messages;

public class SyncSVNPathActionDelegate implements
		IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	private String userName;
	private String cookie;
	private String ip;
	@Override
	public void run(IAction action) {
		Job job = new WorkspaceJob("refresh svn view")
		{
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor)
					throws CoreException {
				monitor.beginTask("refresh", 2);
				initData();
				monitor.worked(1);
				addSvnToView();
				monitor.worked(2);
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.SHORT);
		job.setSystem(true);
		job.schedule(200L);
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
	
	private void initData() {
		Properties p = AuthenticActivator.getProperties();
		userName = p.getProperty("username");
		cookie = p.getProperty("cookie");
		setIp(p.getProperty("ip"));
		
	}
	
	private void addSvnToView() {
			try {
				String message =com.apicloud.navigator.Activator.network_instance.getSvnList(userName, cookie,ip);
				 JSONObject json;
					json = new JSONObject(message);
					String status = json.getString("status");
					if(status.equals("0")) {
						MessageDialog.openError(window.getShell(),Messages.ERROR,
								Messages.SVNFAILURE);
					} else {
						JSONArray body = json.getJSONArray("body");
						SVNProviderPlugin provider = SVNProviderPlugin.getPlugin();
						if(provider == null) {
							System.err.println("svn delete error");
						}
						for(ISVNRepositoryLocation location :  provider.getRepositories().getKnownRepositories(new NullProgressMonitor())) {
							try {
								provider.getRepositories().disposeRepository(location);
							} catch (SVNException e) {
								e.printStackTrace();
							}
						}
						for(int i = 0; i < body.length(); i++) {
							 updateSVNPath((String)body.get(i));
						}
					}
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
	}
	
	private void updateSVNPath(String url) {
		Properties properties = new Properties();
		properties.setProperty("url", url);
		final ISVNRepositoryLocation[] root = new ISVNRepositoryLocation[1];
		SVNProviderPlugin provider = SVNProviderPlugin.getPlugin();
		try {
			root[0] = provider.getRepositories().createRepository(properties);
			// Validate the connection info.  This process also determines the rootURL
			provider.getRepositories().addOrUpdateRepository(root[0]);
		} catch (TeamException e) {
			e.printStackTrace();
		}
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
}
