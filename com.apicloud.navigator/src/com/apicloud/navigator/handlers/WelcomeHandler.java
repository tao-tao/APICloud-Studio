/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.handlers;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swt.widgets.Display;

import com.apicloud.navigator.composite.ThemeUIComposite;

public class WelcomeHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			String url = FileLocator.toFileURL(
				        super.getClass().getResource("/content/start.html")).toString();
			new ThemeUIComposite(Display.getDefault().getActiveShell(), url, 800, 600, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


}
