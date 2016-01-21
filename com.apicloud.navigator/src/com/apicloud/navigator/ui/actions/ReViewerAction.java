/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.ui.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.aptana.browser.parts.WebBrowserView;

public class ReViewerAction implements IEditorActionDelegate {
	private IEditorPart targetEditor;
	
	@Override
	public void run(IAction action) {
		IFile file = null;
		IEditorInput input = targetEditor.getEditorInput();
		if(input instanceof IFileEditorInput) {
			file = ((IFileEditorInput) input).getFile();
		}
		String url = file.getLocation().toOSString();
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if(window == null) return;
		IWorkbenchPage  page = window.getActivePage();
		if(page == null) return;
		WebBrowserView part = (WebBrowserView)page.findView("com.aptana.browser.views.webbrowser");
		if(part != null) {
			page.bringToTop(part);
			part.setURL(url);
			return;
		}
		try {
			WebBrowserView view = (WebBrowserView)page.showView("com.aptana.browser.views.webbrowser");
			view.setURL(url);
		} catch (PartInitException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}
	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.targetEditor = targetEditor;
		
	}

}
