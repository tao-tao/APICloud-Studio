/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.ui.delegate;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

public class OpenAPICloudWizardActionDelegate implements
		IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	public static IEditorInput input;

	@Override
	public void run(IAction action) {
		IWorkbenchPage page = window.getActivePage();
		getInput();
		IEditorPart part = page.findEditor(input);
		if(part != null ) {
			page.bringToTop(part);
			return;
		}
		open(page);
		
	}

	public static void open(IWorkbenchPage page) {
		try {
			IDE.openEditor(page , input, "com.apicloud.navigator.APICloudWizard");
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	public static void getInput() {
		if(input == null) {
			input = new IEditorInput() {
				@SuppressWarnings("rawtypes")
				public Object getAdapter(Class adapter) {
					return null;
				}
				public String getToolTipText() {
					return "test";
				}
				public IPersistableElement getPersistable() {
					return null;
				}
				public String getName() {
					return "uz";
				}
				public ImageDescriptor getImageDescriptor() {
					return null;
				}
				public boolean exists() {
					return true;
				}
			};
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
