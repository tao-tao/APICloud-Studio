/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;

public class RunSimulatorDialog extends Dialog {

	public RunSimulatorDialog(Shell parentShell, IProject[] projects) {
		super(parentShell);
	}
}

class ProjectLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		IProject p = (IProject)element;
		return p.getName();
	}
}
