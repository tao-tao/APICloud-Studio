/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.ui.listener;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.apicloud.authentication.AuthenticActivator;

public class CloudPackageAppItem implements IWorkbenchWindowActionDelegate {
	@Override
	public void run(IAction action) {
		Properties p =AuthenticActivator.getProperties();
		String ip = p.getProperty("ip");
		String url = "http://" + ip +":80/console";
		Desktop desktop = Desktop.getDesktop();
	    if ((Desktop.isDesktopSupported()) && (desktop.isSupported(Desktop.Action.BROWSE))) {
		   try {
	         URI uri = new URI(url);
		         desktop.browse(uri);
		   } catch (URISyntaxException e) {
		        e.printStackTrace();
		   } catch (IOException e) {
		         e.printStackTrace();
	     }
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
	}

}
