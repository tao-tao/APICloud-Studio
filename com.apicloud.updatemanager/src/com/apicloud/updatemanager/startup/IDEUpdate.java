/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.updatemanager.startup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;

import com.apicloud.commons.util.IDEUtil;
import com.apicloud.commons.util.OS;
import com.apicloud.updatemanager.core.CheckUpateManager;
import com.apicloud.updatemanager.Messages;
public class IDEUpdate implements IStartup {

	@Override
	public void earlyStartup() {
			PlatformUI.getWorkbench().addWorkbenchListener(new IWorkbenchListener() {
				
				@Override
				public boolean preShutdown(IWorkbench workbench, boolean forced) {
					if(CheckUpateManager.isFinished) {
						if(MessageDialog.openConfirm(null,
								Messages.TIPINFORMATION,
								Messages.IDEUPDATEDINFO)) {
							
							return true;
						}
						return false;
					}
					return true;
				}
				
				@Override
				public void postShutdown(IWorkbench workbench) {
					if(!OS.isWindows()) {
						try {
							Runtime.getRuntime().exec("killall APICloud");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			});
			
			Job job = new WorkspaceJob("check")
			{
				@Override
				public IStatus runInWorkspace(IProgressMonitor monitor)
						throws CoreException {
					CheckUpateManager  manager = CheckUpateManager.getInstance();
					String updatePath = IDEUtil.getInstallPath() + "download/update.txt";
					String updateOSGIPath = IDEUtil.getInstallPath() + "download/osgi.txt";
					File f = new File(IDEUtil.getInstallPath() + "download/base.zip");
					 File file=new File(updatePath);
					 File osgiFile = new File(updateOSGIPath);
		             if(file.exists()){ 
		            	 String update = readVersion(updatePath);
		            	 new File(updatePath).delete();
						manager.runExe(update);
						return Status.OK_STATUS;
		             }
		             if(f.exists()) {
		            	 f.delete();
		             }
		             if(osgiFile.exists()){ 
		            	 new File(updateOSGIPath).delete();
						manager.runExe();
						return Status.OK_STATUS;
		             }
					manager.setAuto(true);
					manager.checkIDEUpdate();
					return Status.OK_STATUS;
				}
			};
			job.setPriority(Job.SHORT);
			job.setSystem(true);
			job.schedule(1000L);
			
		}
		
		private String readVersion(String filePath){
			String version = "";
	        try {
	        	File file=new File(filePath);
	            InputStreamReader read = new InputStreamReader(
	            new FileInputStream(file), "UTF-8");
	            BufferedReader bufferedReader = new BufferedReader(read);
	            String lineTxt = null;
	            while((lineTxt = bufferedReader.readLine()) != null){
	              version += lineTxt;
	            }
	            read.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	            return "1.0.0";
	        }
	        return version;
	    }
	}
