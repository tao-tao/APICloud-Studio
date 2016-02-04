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
import java.io.IOException;
import java.io.InputStreamReader;

import com.apicloud.commons.util.FileUtil;
import com.apicloud.commons.util.OS;
import com.dd.plist.PropertyListParser;

public class SplitIpa {
	String toolsPath;
	String sourceIpa;
	String appId;
	String widgetPath;
	String cachePath;
	String curPath;
	String outPath;
	String pngPath;
	String name;
	String fullscreen;

	public SplitIpa(String toolsPath, String sourceIpa, String appId,
			String widgetPath, String outPath, String pngPath, String appName,String fullscreen) {
		this.curPath = new File("").getAbsolutePath();
		this.toolsPath = toolsPath;
		this.sourceIpa = sourceIpa;
		this.appId = appId;
		this.widgetPath = widgetPath;
		this.outPath = outPath;
		this.pngPath = pngPath;
		this.name = appName;
		this.fullscreen = fullscreen;
		if(OS.isWindows()) {
			cachePath = System.getProperties().getProperty("user.home")
				+ "\\uztools\\" + appId + "\\ios\\";
		} else {
			cachePath = new File("").getAbsolutePath();
		}
		File file = new File(cachePath);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	public void mySplit() throws Exception {
		String cmdunzip = null;
		String cmdzip = null;
		String cmd_copy = null;
		if(OS.isWindows()) {
			cmdunzip = "\"" + toolsPath + "7za.exe\" x -y \"" + sourceIpa
					+ "\" -o\"" + cachePath + "\"";
			cmdzip= "\"" + toolsPath + "7za.exe\" a \"" + cachePath + "\\"
					+ appId + ".zip\" \"" + cachePath + "\\Payload\\\"";
			cmd_copy = "xcopy \"" + widgetPath + "\" \"" + cachePath
					+ "Payload\\UZApp.app\\widget\\\" /e /y";
		} else {
			cmdunzip = "unzip -o " + sourceIpa +" -d " + cachePath;
			cmdzip = "zip -r " + cachePath + "/" + appId + ".zip  " +  "Payload/";
			cmd_copy = "cp -rf " + widgetPath + "/  " + cachePath + "/Payload/UZApp.app/widget/";
			runCmd("rm -rf "+ cachePath + "/Payload/");
		}
		
		runCmd(cmdunzip);
		String f_mani = cachePath + "/Info.xml";
		File manifest = new File(f_mani);
		if (manifest.exists()) {
			manifest.delete();
		}
		manifest.createNewFile();
		try {
			PropertyListParser.convertToXml(new File(cachePath
					+ "/Payload/UZApp.app/Info.plist"), manifest);
			IPAModifyPlist plist = new IPAModifyPlist();
			plist.setPlistFile(f_mani);
			//plist.setCFBundleIdentifier("com.apicloud." + appId);
			plist.setCFBundleDisplayName(name);
//			if("true".equals(fullscreen)){
//				plist.setUIStatusBarHidden(true);
//			}else{
//				plist.setUIStatusBarHidden(false);
//			}
			plist.savePlistFile();

		} catch (Exception e) {
			e.printStackTrace();
		}
		if(OS.isWindows()) {
		    FileUtil.deleteDirectory(cachePath + "Payload\\UZApp.app\\widget\\");
		} else {
		    runCmd("rm -rf "+ cachePath + "/Payload/UZApp.app/widget/");
		}
		System.out.println("to copy");
		System.out.println("cmd = " + cmd_copy);
		runCmd(cmd_copy);
		
		pngCopy();
		
		File infoFile;
		if(OS.isWindows()) {
			infoFile = new File(cachePath + "Payload\\UZApp.app\\Info.plist");

		} else {
			infoFile = new File(cachePath + "/Payload/UZApp.app/Info.plist");

		}
		if (infoFile.exists()) {
			infoFile.delete();
		}
		try {
			PropertyListParser.convertToBinary(new File(f_mani), infoFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("to zip");
		runCmd(cmdzip);

		File zip = new File(cachePath + "/" + appId + ".zip");
		File ipa = new File(outPath + "/" + appId + ".ipa");
		zip.renameTo(ipa);
	}

	public static void runCmd(String cmd) {
		Runtime rt = Runtime.getRuntime();
		BufferedReader br = null;
		InputStreamReader isr = null;
		try {
			Process p = rt.exec(cmd);
			isr = new InputStreamReader(p.getInputStream());
			br = new BufferedReader(isr);
			String msg = null;
			while ((msg = br.readLine()) != null) {
				System.out.println(msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (isr != null) {
					isr.close();
				}
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void pngCopy() throws Exception {
		String iconPath = pngPath + "icon/";
		String lanchPath = pngPath + "launch/";
		File iconFile = new File(iconPath);
		String[] icons = iconFile.list();
		try {
		for (String iocn : icons) {
			if ("57x57.png".equals(iocn)) {
				FileUtil.copyFile(iconPath + "57x57.png", cachePath
						+ "Payload/UZApp.app/AppIcon57x57.png");
			} else if ("72x72.png".equals(iocn)) {
				FileUtil.copyFile(iconPath + "72x72.png", cachePath
						+ "Payload/UZApp.app/AppIcon72x72~ipad.png");
			} else if ("114x114.png".equals(iocn)) {
				FileUtil.copyFile(iconPath + "114x114.png", cachePath
						+ "Payload/UZApp.app/AppIcon57x57@2x.png");
			}else if("144x144.png".equals(iocn)){
				FileUtil.copyFile(iconPath+"144x144.png", cachePath+"Payload/UZApp.app/AppIcon72x72@2x~ipad.png");
			}else if("120x120.png".equals(iocn)){
				FileUtil.copyFile(iconPath+"120x120.png", cachePath+"Payload/UZApp.app/AppIcon60x60@2x.png");
			}else if("180x180.png".equals(iocn)){
				FileUtil.copyFile(iconPath+"180x180.png", cachePath+"Payload/UZApp.app/AppIcon60x60@3x.png");
			}else if("76x76.png".equals(iocn)){
				FileUtil.copyFile(iconPath+"76x76.png", cachePath+"Payload/UZApp.app/AppIcon76x76~ipad.png");
			}else if("152x152.png".equals(iocn)){
				FileUtil.copyFile(iconPath+"152x152.png", cachePath+"Payload/UZApp.app/AppIcon76x76@2x~ipad.png");
			}
			
		}
		}
		catch (NullPointerException e) {
			
		}
		File lanchFile = new File(lanchPath);
		String[] lanchs = lanchFile.list();
		try {
		for (String lanch : lanchs) {
			if ("640x960.png".equals(lanch)) {
				FileUtil.copyFile(lanchPath + "640x960.png", cachePath
						+ "Payload/UZApp.app/LaunchImage@2x.png");
				FileUtil.copyFile(lanchPath + "640x960.png", cachePath
						+ "Payload/UZApp.app/LaunchImage-700@2x.png");
			} else if ("640x1136.png".equals(lanch)) {
				FileUtil.copyFile(lanchPath + "640x1136.png", cachePath
						+ "Payload/UZApp.app/LaunchImage-568h@2x.png");
				FileUtil.copyFile(lanchPath + "640x1136.png", cachePath
						+ "Payload/UZApp.app/LaunchImage-700-568h@2x.png");
			} else if ("320x480.png".equals(lanch)) {
				FileUtil.copyFile(lanchPath + "320x480.png", cachePath
						+ "Payload/UZApp.app/LaunchImage.png");
			}else if ("750x1334.png".equals(lanch)) {
				FileUtil.copyFile(lanchPath + "750x1334.png", cachePath
						+ "Payload/UZApp.app/LaunchImage-800-667h@2x.png");
			}else if ("1242x2208.png".equals(lanch)) {
				FileUtil.copyFile(lanchPath + "1242x2208.png", cachePath
						+ "Payload/UZApp.app/LaunchImage-800-Portrait-736h@3x.png");
			}else if ("768x1024.png".equals(lanch)) {
				
				FileUtil.copyFile(lanchPath + "768x1024.png", cachePath
						+ "Payload/UZApp.app/LaunchImage-700-Portrait~ipad.png");
				FileUtil.copyFile(lanchPath + "768x1024.png", cachePath
						+ "Payload/UZApp.app/LaunchImage-Portrait~ipad.png");
			}else if ("1536x2048.png".equals(lanch)) {
				FileUtil.copyFile(lanchPath + "1536x2048.png", cachePath
						+ "Payload/UZApp.app/LaunchImage-Portrait@2x~ipad.png");
				FileUtil.copyFile(lanchPath + "1536x2048.png", cachePath
						+ "Payload/UZApp.app/LaunchImage-700-Portrait@2x~ipad.png");
			}
		}
		} 
		catch (NullPointerException e) {
			
		}
	}
}
