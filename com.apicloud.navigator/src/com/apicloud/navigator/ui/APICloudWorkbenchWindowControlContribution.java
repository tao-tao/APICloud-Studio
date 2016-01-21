/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.ui;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.PerspectiveBarManager;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

import com.apicloud.authentication.AuthenticActivator;
import com.apicloud.authentication.Messages;
import com.apicloud.commons.util.FileUtil;
import com.apicloud.commons.util.IDEUtil;
import com.apicloud.commons.util.OS;

@SuppressWarnings("restriction")
public class APICloudWorkbenchWindowControlContribution extends WorkbenchWindowControlContribution{
	private Link accountLink;

	public APICloudWorkbenchWindowControlContribution() {
	}

	public APICloudWorkbenchWindowControlContribution(String id) {
		super(id);
	}

	@Override
	protected Control createControl(Composite parent) {
		Composite main = new Composite(parent, 0);

		parent.setLayout(GridLayoutFactory.fillDefaults().create());
		main.setLayoutData(GridDataFactory.swtDefaults().grab(true, false)
				.create());

		main.setLayout(GridLayoutFactory.fillDefaults().numColumns(2)
				.spacing(0, 0).create());
		ToolBar toolbar = new ToolBar(main, 0);
		toolbar.setLayoutData(GridDataFactory.swtDefaults().create());
		ToolItem homeItem = new ToolItem(toolbar, SWT.PUSH);
		if (AuthenticActivator.isConnection()) {
			homeItem.setImage(AuthenticActivator
					.getImage(Messages.WorkbenchWindowControlContribution1_0));
		} else {
			homeItem.setImage(AuthenticActivator
					.getImage(Messages.WorkbenchWindowControlContribution1_1));
		}

		homeItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!(MessageDialog
						.openConfirm(
								getWorkbenchWindow().getShell(),
								Messages.WorkbenchWindowControlContribution1_EXIT_AND_LOG_OFF,
								Messages.WorkbenchWindowControlContribution1_EXIT_MESSAGE)))
					return;
				deleteUserLoginInfo();
				deleteUserSvnInfo();
				restart();
			}

		});

		this.accountLink = new Link(main, 0);
		this.accountLink.setLayoutData(GridDataFactory.swtDefaults()
				.grab(true, false).create());
		Properties p = AuthenticActivator.getProperties();
		String userName = p
				.getProperty(Messages.WorkbenchWindowControlContribution1_4) == null ? Messages.WorkbenchWindowControlContribution1_5
				: p.getProperty(Messages.WorkbenchWindowControlContribution1_6);
		this.accountLink.setText("<a>" + userName + "</a>");

		this.accountLink.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!(MessageDialog
						.openConfirm(
								getWorkbenchWindow().getShell(),
								Messages.WorkbenchWindowControlContribution1_EXIT_AND_LOG_OFF,
								Messages.WorkbenchWindowControlContribution1_EXIT_MESSAGE)))
					return;
				deleteUserLoginInfo();
				deleteUserSvnInfo();
				restart();
			}
		});
		
		final IWorkbench w=PlatformUI.getWorkbench();
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {  
		        public void run() {  
		        	try {
						PlatformUI.getWorkbench().showPerspective("com.apicloud.navigator.perspective", w.getActiveWorkbenchWindow());
					  PerspectiveBarManager barmanager=((WorkbenchWindow)PlatformUI.getWorkbench().getActiveWorkbenchWindow()).getPerspectiveBar();
		    		 if(barmanager!=null){
		    			 barmanager.dispose();
		    		 }
		    	} catch (WorkbenchException e) {
					e.printStackTrace();
				}
				
		    }  
		}); 
		return main;
	}

	private void restart() {
		String ide_Home = IDEUtil.getInstallPath();
		String jarPath = ide_Home + "UZTools" + File.separator + "update.jar";
		String className = "com.apicloud.exe.update.UpdateIDE";
		String cmd = "java -cp " + "\"" + jarPath + "\"" + " " + className
				+ " " + "\"" + ide_Home + "/\"" + " 1";
		try {
			if (OS.isWindows()) {
				PlatformUI.getWorkbench().restart();
			} else {
				cmd = "java -cp " + jarPath + " " + className + " " + ide_Home
						+ "/" + " 1 2";
				Runtime.getRuntime().exec(cmd);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void deleteUserLoginInfo() {
		Properties p = AuthenticActivator.getProperties();
		p.remove(Messages.WorkbenchWindowControlContribution1_12);
		p.remove(Messages.WorkbenchWindowControlContribution1_13);
		p.remove(Messages.WorkbenchWindowControlContribution1_14);
		AuthenticActivator.store(p);
	}

	public static boolean deleteUserSvnInfo() {
		String path = "";
		String path2="";
		if (OS.isWindows()) {
			path = System
					.getProperty(Messages.WorkbenchWindowControlContribution1_15)
					+ Messages.WorkbenchWindowControlContribution1_svn;
		}else{
			path=System.getProperty("user.home")+Messages.WorkbenchWindowControlContribution1_svn2;
			path2=System.getProperty("user.home")+Messages.WorkbenchWindowControlContribution1_mac_svn;
			FileUtil.delAllFiles(path2);
		}
		FileUtil.delAllFiles(path);
		return true;
	}
}
