package org.tigris.subversion.subclipse.ui.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import org.eclipse.core.resources.ResourcesPlugin;

public class SVNUtil {

	public static Properties getSVNInfoProperties(String name) {
		Properties p = new Properties();
		FileReader reader = null;
		try {
			File file = getSVNINfoFIle(name);
			if (file.exists()) {
				reader = new FileReader(file);
				p.load(reader);

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return p;
	}

	public static File getSVNINfoFIle(String name) {
		File file = new File(ResourcesPlugin.getWorkspace().getRoot()
				.getLocation().toString()
				+ "/" + name + "/svnInfo.properties");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}

	public static void storeSVNInfo(Properties p, String name) {
		OutputStream out = null;
		try {
			out = new FileOutputStream(getSVNINfoFIle(name));
			p.store(out, null);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
	public static void saveSVnurlInfo(String name,String appId) {
		Properties p = org.tigris.subversion.subclipse.ui.util.SVNUtil.getSVNInfoProperties(name);
		p.put("APPID", appId);
		org.tigris.subversion.subclipse.ui.util.SVNUtil.storeSVNInfo(p, name);
	}
}
