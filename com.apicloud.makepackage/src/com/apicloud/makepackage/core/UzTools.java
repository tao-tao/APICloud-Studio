/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.makepackage.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.apicloud.commons.util.FileUtil;
import com.apicloud.makepackage.Activator;


public class UzTools {
	public static String UZ_CACHE_PATH = System.getProperties().getProperty("user.home")
			+ "/uztools/";

	public static void main(String[] args) throws Exception {
		FileUtil.deleteDirectory(UZ_CACHE_PATH);
	}

	public static void makePackage(int type, String installPath,
			String outPath, String widgetPath, String appId, String pngPath,
			String appName,String fullscreen) throws Exception {
		String toolsPath = installPath + "UZTools/";
		String baseApkPath = com.apicloud.loader.platforms.android.ADBActivator.getBasePath();
		String keystorePath = toolsPath + "uzmap.keystore";
		String keystorePathPass = "123456";
		String baseIpaPath = com.apicloud.loader.platforms.ios.ANBActivator.getBasePath();
		File outPathFile = new File(outPath);
		if (!outPathFile.exists()) {
			outPathFile.mkdirs();
		}
		if (type == 1) {
			File outFile = new File(outPath + appId + ".apk");
			if (outFile.exists()) {
				outFile.delete();
			}
			SplitApk splitApk = new SplitApk(baseApkPath, keystorePath,
					keystorePathPass, toolsPath, appId, outPath, widgetPath,
					pngPath, appName,fullscreen);
			splitApk.mySplit();
		} else if (type == 2) {
			File outFile = new File(outPath + appId + ".ipa");
			if (outFile.exists()) {
				outFile.delete();
			}
			SplitIpa splitIpa = new SplitIpa(toolsPath, baseIpaPath, appId,
					widgetPath, outPath, pngPath, appName,fullscreen);
			splitIpa.mySplit();
		} else if (type == 3) {
			File outApkFile = new File(outPath + appId + ".apk");
			if (outApkFile.exists()) {
				outApkFile.delete();
			}
			SplitApk splitApk = new SplitApk(baseApkPath, keystorePath,
					keystorePathPass, toolsPath, appId, outPath, widgetPath,
					pngPath, appName,fullscreen);
			splitApk.mySplit();
			File outIpaFile = new File(outPath + appId + ".ipa");
			if (outIpaFile.exists()) {
				outIpaFile.delete();
			}
			SplitIpa splitIpa = new SplitIpa(toolsPath, baseIpaPath, appId,
					widgetPath, outPath, pngPath, appName,fullscreen);
			splitIpa.mySplit();
		}
		FileUtil.deleteDirectory(UZ_CACHE_PATH);
	}

	public static void runCmd(String cmd) throws Exception {
		printlnLog(cmd);
		System.out.println("cmd = " + cmd);
		Runtime rt = Runtime.getRuntime();
		BufferedReader br = null;
		InputStreamReader isr = null;
		Process p = rt.exec(cmd);
		isr = new InputStreamReader(p.getInputStream(), "gbk");
		br = new BufferedReader(isr);
		String msg = null;
		while ((msg = br.readLine()) != null) {
			printlnLog(msg);
		}
		closeResource(br, isr);
	}
	public static void runCmd(String[] cmd) throws Exception {
		System.out.println("cmd = " + cmd);
		Runtime rt = Runtime.getRuntime();
		BufferedReader br = null;
		InputStreamReader isr = null;
		Process p = rt.exec(cmd);
		isr = new InputStreamReader(p.getInputStream(), "gbk");
		br = new BufferedReader(isr);
		String msg = null;
		while ((msg = br.readLine()) != null) {
			printlnLog(msg);
		}
		closeResource(br, isr);
	}
	private static void closeResource(BufferedReader br, InputStreamReader isr)
			throws IOException {
		if (isr != null) {
			isr.close();
		}
		if (br != null) {
			br.close();
		}
	}
	public static void printlnLog(String msg) {
		Activator activator = Activator.getDefault();
		if (activator != null) {
			activator.getLog().log(
					new Status(IStatus.OK, Activator.PLUGIN_ID, 0, msg, null));
		}
		System.out.println(msg);
	}

	public static StringBuffer readFile(String file) {
		StringBuffer buffer = new StringBuffer();
		String encoding = "UTF-8";

		File destf = new File(file);
		try {
			if ((destf.isFile()) && (destf.exists())) {
				InputStreamReader read = new InputStreamReader(
						new FileInputStream(destf), encoding);

				BufferedReader bufferedReader = new BufferedReader(read);
				String content;
				while ((content = bufferedReader.readLine()) != null) {

					buffer.append(content);
					buffer.append("\n");
				}
				bufferedReader.close();
				read.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return buffer;
	}

	public static void writeFile(String file, StringBuffer buffer) {
		String encoding = "UTF-8";
		try {
			OutputStreamWriter fw = new OutputStreamWriter(
					new FileOutputStream(file), encoding);

			BufferedWriter writer = new BufferedWriter(fw);
			writer.write(buffer.toString());
			writer.close();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
