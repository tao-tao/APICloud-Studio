/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.ui.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.apicloud.commons.model.Config;
import com.apicloud.commons.util.FileUtil;
import com.apicloud.commons.util.IDEUtil;
import com.apicloud.navigator.dialogs.Messages;

public class RemoveAppLoaderAction implements IObjectActionDelegate {
	private IStructuredSelection selection;
	private File file;
	private String id;
	private OutputStreamWriter outputStreamWriter;

	@Override
	public void run(IAction action) {
		if (file != null) {
			FileUtil.deleteDirectory((file.getAbsolutePath()));
			Properties p=com.apicloud.commons.model.Activator.getAppLoaderProperties();
			 p.remove(id);
			 com.apicloud.commons.model.Activator.getLoaderMap().remove(id);
			writeFile(p,com.apicloud.commons.model.Activator.getLoaderFile());
			try {
				ResourcesPlugin.getWorkspace().getRoot()
						.refreshLocal(IResource.DEPTH_INFINITE, null);
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						MessageDialog
								.openInformation(Display.getCurrent()
										.getActiveShell(),
										Messages.REMOVESUCCESS,
										Messages.LOADERREMOVED);
					}
				});

			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection != null && selection instanceof IStructuredSelection) {
			this.selection = (IStructuredSelection) selection;
			if (action != null) {
				action.setEnabled(isEnabled());
			}
		}
	}

	private boolean isEnabled() {
		String path;
		if (selection != null) {
			Object obj = selection.getFirstElement();
			IResource resource = (IResource) obj;
			if (resource != null) {
				path = resource.getLocation().toOSString();

				File fileToRead = new File(path + File.separator + "config.xml");
				try {
					Config config = Config.loadXml(new FileInputStream(
							fileToRead));
					id = config.getId();
					file = new File(IDEUtil.getInstallPath() + "/apploader/"
							+ id);

					if (file.exists()) {
						return true;
					}

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}

		return false;

	}

	private void writeFile(Properties p, File file) {
		try {
			outputStreamWriter = new OutputStreamWriter(new FileOutputStream(
					file));
			p.store(outputStreamWriter, null);
			close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void close() {
		try {
			if (outputStreamWriter != null) {
				outputStreamWriter.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

}
