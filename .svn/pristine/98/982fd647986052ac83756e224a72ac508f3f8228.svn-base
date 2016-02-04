/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.start;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.progress.UIJob;
import com.apicloud.authentication.AuthenticActivator;
import com.apicloud.commons.util.IDEUtil;
import com.apicloud.navigator.composite.ThemeUIComposite;
import com.aptana.swt.webkitbrowser.WebKitBrowser4Mac;

@SuppressWarnings("restriction")
public class APICloudWizardStart implements IStartup{

	@Override
	public void earlyStartup() {
		Job menuJob = new UIJob("")
		{
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				String resPath = IDEUtil.getInstallPath() + "dropins\\res";
				File file = new File(resPath);
				if(file.exists()) {
					IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if (activeWorkbenchWindow != null) {
						if (activeWorkbenchWindow instanceof WorkbenchWindow)
						{
							WorkbenchWindow workbenchWindow = (WorkbenchWindow) activeWorkbenchWindow;
							workbenchWindow.setCoolBarVisible(false);
							workbenchWindow.setCoolBarVisible(true);
						}
						WorkbenchPage page = (WorkbenchPage) activeWorkbenchWindow.getActivePage();
						if (page != null) {
							page.resetPerspective();
						}
					}
					file.delete();
				}
				return Status.OK_STATUS;
			}
		};
		menuJob.schedule(300L);
		Properties p = AuthenticActivator.getProperties();
		String first = p.getProperty("first");
		if(first.equals("1")) {
			Display.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					WebKitBrowser4Mac browser = new WebKitBrowser4Mac(Display.getDefault().getActiveShell(), 0);
					browser.setJavascriptEnabled(true);
					browser.setUrl("http://www.apicloud.com/ide/ideStart.html");
					
				}
			});
			return ;
		}
		Job job = new UIJob("")
		{
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setMaximized(true);
				String url;
				try {
					ArrayContentProvider.getInstance();
					url = FileLocator.toFileURL(
					        super.getClass().getResource("/content/start.html")).toString();
					new ThemeUIComposite(Display.getDefault().getActiveShell(), url, 800, 600, false);
				} catch (IOException e) {
					e.printStackTrace();
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule(300L);
		p.put("first", "1");
		AuthenticActivator.store(p);
	}

}
