/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.ui.actions;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class AdvancedSettingsAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	@Override
	public void run(IAction action) {
		String[] displayIds = { "com.aptana.theme.preferencePage" };
   PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(window.getShell(), "com.aptana.theme.preferencePage", displayIds, null);
	   dialog.open();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

}
