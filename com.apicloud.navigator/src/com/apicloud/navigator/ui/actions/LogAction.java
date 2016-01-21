/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.apicloud.loader.platforms.android.ADBException;
import com.apicloud.loader.platforms.android.ADBService;

public class LogAction implements IWorkbenchWindowActionDelegate {
	private static final String ID = "org.eclipse.ui.console.ConsoleView";
	private IWorkbenchWindow window;
	@Override
	public void run(IAction action) {
		try {
			ADBService.getADBService().syncLog();
		} catch (ADBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		IWorkbenchPage page = window.getActivePage();
		try {
			if(page != null) {
				ViewPart view = (ViewPart)page.findView(ID);
				if(view == null) {
					page.showView(ID);
				}
			}
		} catch (PartInitException e) {
			e.printStackTrace();
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
		this.window = window;

	}

}
