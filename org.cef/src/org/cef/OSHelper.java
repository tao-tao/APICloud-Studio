package org.cef;

import java.io.File;
import java.net.URL;

import org.cef.CefApp;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

public class OSHelper 
{
	private static boolean hasInitJNIPath;

	public static void initJNIPath() {
		if (!(hasInitJNIPath)) {
			hasInitJNIPath = true;
			try {
				String path = new File(FileLocator.toFileURL(CefApp.class.getResource("/")).toURI()).getAbsolutePath();
				String oldPath = System.getProperty("java.class.path");
				String newPath = oldPath + System.getProperty("path.separator") + path;
				System.setProperty("java.class.path", newPath);
			} catch (Exception e) {
				e.printStackTrace();
			}

			Bundle bundle = Platform.getBundle("org.cef");
			Path path = new Path("/os/win32/x86");
			URL url = FileLocator.find(bundle, path, null);
			try {
				String dir = new File(FileLocator.toFileURL(url).getFile()).getAbsolutePath();
				System.setProperty("cef.path", dir);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
