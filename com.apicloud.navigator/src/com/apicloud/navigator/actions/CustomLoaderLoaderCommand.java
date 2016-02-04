/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.apicloud.commons.model.Config;
import com.apicloud.navigator.ui.actions.CustomLoader;

public class CustomLoaderLoaderCommand extends AbstractHandler{
	private Object obj = null;
	private String id="";
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		CustomLoader customLoader=new CustomLoader();
		
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null) {
			IWorkbenchPage page = (IWorkbenchPage) activeWorkbenchWindow.getActivePage();
			if (page != null) {
				IEditorPart part = page.getActiveEditor();
				if (part != null) {
					IEditorInput input = part.getEditorInput();
					if (input instanceof IFileEditorInput) {
						IFileEditorInput fileInput = (IFileEditorInput) input;
						obj = fileInput.getFile().getProject();
						IResource resource = (IResource) obj;
						String path = resource.getLocation().toOSString();

						File fileToRead = new File(path + File.separator + "config.xml");
						try {
							Config config = Config.loadXml(new FileInputStream(fileToRead));
							 id = config.getId();

						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
						customLoader.initInfo();
						customLoader.compileLoader(id);
						return null;
					}
				}
			}
		}
		customLoader.run(null);
		return null;
	}
}
