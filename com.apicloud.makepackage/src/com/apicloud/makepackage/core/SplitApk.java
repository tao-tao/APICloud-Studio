/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.makepackage.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.apicloud.commons.util.FileUtil;
import com.apicloud.commons.util.IDEUtil;
import com.apicloud.commons.util.OS;

public class SplitApk {
	String toolsPath;
	String sourceApk;
	String keyFile;
	String keyPasswd;
	String appId;
	String cachePath;
	String outPath;
	String widgetPath;
	String jrePath;
	String pngPath;
	String name;
	String fullscreen;

	public SplitApk(String sourceApk, String keyFile, String keyPasswd,
			String toolsPath, String appId, String outPath, String widgetPath,
			String pngPath, String appName,String fullscreen) {
		this.sourceApk = sourceApk;
		this.keyFile = keyFile;
		this.keyPasswd = keyPasswd;
		this.toolsPath = toolsPath;
		this.appId = appId;
		this.outPath = outPath;
		this.widgetPath = widgetPath;
		this.jrePath = IDEUtil.getInstallPath() + "jre";
		this.pngPath = pngPath;
		this.name = appName;
		this.fullscreen =fullscreen;
		this.cachePath = System.getProperties().getProperty("user.home")
				+ File.separator + "uztools" + File.separator + appId + File.separator + "android" + File.separator;
		File file = new File(cachePath);
		if (!file.exists()) {
			file.mkdirs();
		}
	}
	
	public void mySplit() throws Exception {
		String java_home = null;
		String path = null;
		String cmdUnpack = null;
		if(OS.isWindows()) { 
			java_home = "cmd.exe /C set JAVA_HOME=" + jrePath;
			path = "&&set PATH=" + jrePath + "\\bin;" + toolsPath;
			cmdUnpack = "java -jar \"" + toolsPath
					+ "apktool.jar\" d -f -s \"" + sourceApk + "\" \"" + cachePath+"\\\"";
			// 解压 /C 执行字符串指定的命令然后终断
			UzTools.runCmd(java_home + path + "&&" + cmdUnpack);
		}else {
			cmdUnpack = "java -jar " + toolsPath
					+ "apktool.jar d -f -s " + sourceApk + " " + cachePath+"/";
			UzTools.runCmd(cmdUnpack);
		}

		String f_mani = cachePath + "AndroidManifest.xml";
		System.out.println("f_mani="+f_mani);
		Xml.updateAndroidManifestXML(f_mani, appId, f_mani);
		Map<String,String> map = new HashMap<String, String>();
		Xml.getFeatureApiKey(widgetPath+"/config.xml", map);
		List<String> metadataList = new ArrayList<String>();
		if(map.containsKey("baiduMap")){
			String metadata = "<meta-data android:name=\"com.baidu.lbsapi.API_KEY\" android:value=\""+map.get("baiduMap")+"\" />";
			metadataList.add(metadata);
		}
		StringBuffer destContent = new StringBuffer();
		StringBuffer newContent = new StringBuffer();
		String	label = "</application>";
		int index_s = 0;
		for (String metadata : metadataList) {
			System.out.println("metadata = "+metadata);
			newContent.append("\n" + metadata);
		}
		destContent = UzTools.readFile(f_mani);
		
		if (newContent.length() > 0) {
		
			index_s = destContent.indexOf(label);
			if (index_s != -1) {
				destContent.insert(index_s - 1, newContent);
			}

		}
		UzTools.writeFile(f_mani, destContent);
		String strings;
		String styles;
		if(OS.isWindows()) { 
			strings = cachePath + "\\res\\values\\strings.xml";
			styles = cachePath + "\\res\\values\\styles.xml";			
		} else {
			strings = cachePath + "/res/values/strings.xml";
			styles = cachePath + "/res/values/styles.xml";
		}
		Xml.updateStringXML(strings, "/resources/string[@name='app_name']", name, strings);
		Xml.updateStringXML(styles, "/resources/style[@name='AppTheme']/item[@name='android:windowFullscreen']", fullscreen, styles);
		
		pngCopy();
		if(OS.isWindows()) {
			String cmddel = String.format("cmd.exe /C rd /s/q %s", "\""+cachePath
					+ "assets\\widget\\\" &&xcopy \"" + widgetPath + "\" \""
					+ cachePath + "assets\\widget\\\" /e /y");
			UzTools.runCmd(cmddel);
			FileUtil.deleteDirectory(cachePath + "assets\\widget\\icon\\");
			FileUtil.deleteDirectory(cachePath + "assets\\widget\\launch\\");			
		} else {
			FileUtil.deleteDirectory(cachePath + "assets/widget/");
			String cmd_copy = "cp -r " + widgetPath + " "
					+ cachePath + "assets/widget/";
			UzTools.runCmd(cmd_copy);
			FileUtil.deleteDirectory(cachePath + "assets/widget/icon/");
			FileUtil.deleteDirectory(cachePath + "assets/widget/launch/");			
		}
		System.out.println("==INFO 4.2. == 准备打包: ");

		if(OS.isWindows()) {
			String unsignApk = "\""+cachePath + "_un.apk\"";
			String signApk = "\""+cachePath + "signApk.apk\"";
			String cmdPack = String.format("java -jar \"" + toolsPath
					+ "apktool.jar\" b %s %s", "\""+cachePath+"\\\"", unsignApk);
			String cmd = "&&java -classpath \"" + toolsPath
					+ "tools.jar\" sun.security.tools.JarSigner -keystore \""
					+ keyFile + "\" -storepass " + keyPasswd + " -signedjar "
					+ signApk + " " + unsignApk + " uzmap.keystore";
			String cmd_android = "\"" + toolsPath + "zipalign.exe\"  -v 4 "
					+ signApk + " \"" + outPath + appId + ".apk\"";
			UzTools.runCmd(java_home + path + "&&" + cmdPack + cmd + "&&"
					+ cmd_android);
		} else {
			String unsignApk = cachePath + "_un.apk";
			String signApk = cachePath + "signApk.apk";
			String cmdPack = String.format("cd " + toolsPath
					+ ";sh apktool b %s %s", cachePath+"/", unsignApk);
			String cmd = ";java -classpath \"" + toolsPath
					+ "tools.jar\" sun.security.tools.JarSigner -keystore \""
					+ keyFile + "\" -storepass " + keyPasswd + " -signedjar "
					+ signApk + " " + unsignApk + " uzmap.keystore";			
			
			String cmd_android =";" + toolsPath + "zipalign  -v 4 "
					+ signApk +" " +outPath + appId + ".apk";
			String[] cmds = { "/bin/sh", "-c", cmdPack+cmd+cmd_android };
			UzTools.runCmd(cmds );
		}
	}

	public void pngCopy() throws Exception {
		if(OS.isWindows()) {
			String iconPath = pngPath + "icon\\";
			String lanchPath = pngPath + "launch\\";
			File iconFile = new File(iconPath);
			String[] icons = iconFile.list();
			try {
				for (String iocn : icons) {
					if ("72x72.png".equals(iocn)) {
						FileUtil.copyFile(iconPath + "72x72.png", cachePath
								+ "res\\drawable-mdpi\\uz_icon.png");
						FileUtil.copyFile(iconPath + "72x72.png", cachePath
								+ "res\\drawable-hdpi\\uz_icon.png");
					} else if ("96x96.png".equals(iocn)) {
						FileUtil.copyFile(iconPath + "96x96.png", cachePath
								+ "res\\drawable-xhdpi\\uz_icon.png");
					} else if ("144x144.png".equals(iocn)) {
						FileUtil.copyFile(iconPath + "144x144.png", cachePath
								+ "res\\drawable-xxhdpi\\uz_icon.png");
					}
				}
			}
			catch (NullPointerException e) {
			}
			File lanchFile = new File(lanchPath);
			String[] lanchs = lanchFile.list();
			try {
				for (String lanch : lanchs) {
					if ("480x800.png".equals(lanch)) {
						FileUtil.copyFile(lanchPath + "480x800.png", cachePath
								+ "res\\drawable-mdpi\\uz_splash_bg.png");
						FileUtil.copyFile(lanchPath + "480x800.png", cachePath
								+ "res\\drawable-hdpi\\uz_splash_bg.png");
					} else if ("720x1280.png".equals(lanch)) {
						FileUtil.copyFile(lanchPath + "720x1280.png", cachePath
								+ "res\\drawable-xhdpi\\uz_splash_bg.png");
					} else if ("1080x1920.png".equals(lanch)) {
						FileUtil.copyFile(lanchPath + "1080x1920.png", cachePath
								+ "res\\drawable-xxhdpi\\uz_splash_bg.png");
					}
				}
			}
			catch (NullPointerException e) {
			}
		} else {
			String iconPath = pngPath + "icon/";
			String lanchPath = pngPath + "launch/";
			File iconFile = new File(iconPath);
			String[] icons = iconFile.list();
			try {
				for (String iocn : icons) {
					if ("72x72.png".equals(iocn)) {
						FileUtil.copyFile(iconPath + "72x72.png", cachePath
								+ "res/drawable-mdpi/uz_icon.png");
						FileUtil.copyFile(iconPath + "72x72.png", cachePath
								+ "res/drawable-hdpi/uz_icon.png");
					} else if ("96x96.png".equals(iocn)) {
						FileUtil.copyFile(iconPath + "96x96.png", cachePath
								+ "res/drawable-xhdpi/uz_icon.png");
					} else if ("144x144.png".equals(iocn)) {
						FileUtil.copyFile(iconPath + "144x144.png", cachePath
								+ "res/drawable-xxhdpi/uz_icon.png");
					}
				}
			}
			catch (NullPointerException e) {
			}
			File lanchFile = new File(lanchPath);
			String[] lanchs = lanchFile.list();
			try {
				for (String lanch : lanchs) {
					if ("480x800.png".equals(lanch)) {
						FileUtil.copyFile(lanchPath + "480x800.png", cachePath
								+ "res/drawable-mdpi/uz_splash_bg.png");
						FileUtil.copyFile(lanchPath + "480x800.png", cachePath
								+ "res/drawable-hdpi/uz_splash_bg.png");
					} else if ("720x1280.png".equals(lanch)) {
						FileUtil.copyFile(lanchPath + "720x1280.png", cachePath
								+ "res/drawable-xhdpi/uz_splash_bg.png");
					} else if ("1080x1920.png".equals(lanch)) {
						FileUtil.copyFile(lanchPath + "1080x1920.png", cachePath
								+ "res/drawable-xxhdpi/uz_splash_bg.png");
					}
				}
			}
			catch (NullPointerException e) {
			}
			
		}
	}

	public void updateFile(String file, String file_back, String target,
			String replacement) throws Exception {

		FileUtil.copyFile(file, file_back);

		BufferedReader br = null;
		OutputStreamWriter osw = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file_back), "UTF-8"));
			String line = null;
			StringBuffer sb = new StringBuffer();
			while ((line = br.readLine()) != null) {
				if (line.contains(target)) {
					line = line.replace(target, replacement);
				}
				sb.append(line + "\n");
			}

			// 写回文件
			osw = new OutputStreamWriter(new FileOutputStream(file, false),"UTF-8");
			osw.write(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
				if (osw != null) {
					osw.close();
				}
				File back_file = new File(file_back);
				if (back_file.exists()) {
					back_file.delete();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}