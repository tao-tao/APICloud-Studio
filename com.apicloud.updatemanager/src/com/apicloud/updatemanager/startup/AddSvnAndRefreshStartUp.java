/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.updatemanager.startup;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;

import com.apicloud.authentication.AuthenticActivator;
import com.apicloud.commons.model.Feature;
import com.apicloud.networkservice.NetWorkService;
import com.apicloud.updatemanager.Messages;

import org.tigris.subversion.subclipse.core.util.SVNUtil;

public class AddSvnAndRefreshStartUp implements IStartup {
	private String userName;
	private String ip;
	private String cookie;
	private NetWorkService network_instance;
	@Override
	public void earlyStartup() {
		Job job = new WorkspaceJob("refresh svn view")
		{
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor)
					throws CoreException {
				monitor.beginTask("refresh", 4);
				initData();
				monitor.worked(1);
				saveFeatureInfo(ip, userName, cookie);
				monitor.worked(1);
				addSvnToView(ip, userName, cookie);
				monitor.worked(1);
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.SHORT);
		job.setSystem(true);
		job.schedule(200L);
	}

	private void initData() {
		network_instance=NetWorkService.getInstance();
		Properties p =AuthenticActivator.getProperties();
		userName = p.getProperty("username");
		cookie = p.getProperty("cookie");
		ip =  p.getProperty("ip");
		
	}
	private void saveFeatureInfo(String ip, String username, String cookie) {
		List<Feature> features = new ArrayList<Feature>();
		try {
			String message =network_instance.getFeatureInfo(cookie,ip);
			JSONObject json;
				json = new JSONObject(message);
				String status = json.getString("status");
				if(status.equals("0")) {
				} else {
					JSONArray body = json.getJSONArray("body");
					for(int i = 0; i < body.length(); i++) {
						Feature f = new Feature();
						JSONObject feature = (JSONObject)body.get(i);
						f.setName(feature.getString("mdName"));
						f.setType(feature.getString("mdType"));
						f.setIos(true);
						f.setAndroid(true);
						if("0".equals(feature.getString("mdPlatform"))) {
							f.setAndroid(false);
						}
						if("1".equals(feature.getString("mdPlatform"))) {
							f.setIos(false);
						}
						f.setDesc(feature.getString("mdDescription"));
						features.add(f);
					}
				}
				Feature.saveXml(features, AuthenticActivator.getFeatureFile());
			} catch (JSONException e) {
				e.printStackTrace();
			}
	}

	
	
	private void addSvnToView(final String ip, final String userName, final String cookie) {
		try {
			String message = network_instance.getSvnList(userName, cookie,ip);
			JSONObject json;
			json = new JSONObject(message);
			String status = json.getString("status");
			if(status.equals("0")) {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						MessageDialog.openError(null, Messages.ERROR,
								Messages.SYNCSVNERRORINFO);
					}
					
				});
			
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
					SVNUtil.addSVNToView((String)body.get(i));
				}
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
	}
}
