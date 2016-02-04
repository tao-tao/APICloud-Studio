/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.commons.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DecimalFormat;

import org.eclipse.core.runtime.SubProgressMonitor;
import org.json.JSONException;
import org.json.JSONObject;

import com.apicloud.networkservice.GetBigFileMD5;

public class DownLoadUtil {
	public static void downZip(String uri, String fileName) throws IOException {
		FileOutputStream fos = null;
		BufferedInputStream bis = null;
		HttpURLConnection httpUrl = null;
		URL url = null;
		byte[] buf = new byte[8096];
		int line = 0;

		url = new URL(uri);
		httpUrl = (HttpURLConnection) url.openConnection();
		httpUrl.connect();
		bis = new BufferedInputStream(httpUrl.getInputStream());
		File file = new File(fileName);
		if (!file.exists()) {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			file.createNewFile();
		}
		fos = new FileOutputStream(fileName);
		while ((line = bis.read(buf)) != -1) {
			fos.write(buf, 0, line);
		}

		fos.close();
		bis.close();
		httpUrl.disconnect();
	}

	public static boolean downZip1(String uri, String fileName,
			SubProgressMonitor monitor) throws IOException {
		FileOutputStream fos = null;
		BufferedInputStream bis = null;
		HttpURLConnection httpUrl = null;
		File file;
		URL url = null;
		byte[] buf = new byte[8096];
		int line = 0;
		int index = 0;
		String fileNameInURI = uri.substring(uri.lastIndexOf('/') + 1);
		
		try {
			url = new URL(uri);
			httpUrl = (HttpURLConnection) url.openConnection();
			httpUrl.setReadTimeout(8000);
			int fileSize = httpUrl.getContentLength();
			double current = 0.0;
			double all = 0.0;
			httpUrl.connect();
			monitor.beginTask("download", fileSize);
			bis = new BufferedInputStream(httpUrl.getInputStream());
			file = new File(fileName);
			if (!file.exists()) {
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				file.createNewFile();
			}
			fos = new FileOutputStream(fileName);
			while ((line = bis.read(buf)) != -1) {
				if (monitor.isCanceled()) {
					throw new IOException();
				}
				fos.write(buf, 0, line);
				index += line;
				monitor.worked(line);
				DecimalFormat df = new DecimalFormat("0.00");
				current = index;
				current /= 1000;
				String current1 = df.format(current);
				all = fileSize;
				all /= 1000;
				String all1 = df.format(all);
				monitor.subTask(" " + current1 + "kb/" + all1 + "kb");
			}

			fos.close();

			bis.close();
			httpUrl.disconnect();
			
			if (current < all)
				return false;
			else {
				if (fileNameInURI.length()<35) {
					String urlDomain = uri.substring(0, uri.indexOf('/', 7));
					String urlPostString = urlDomain+"/getFileMD5";
					
					String md5_server_told = null;
					String md5_compute = GetBigFileMD5.getMD5(file);
					
					URL urlPost = new URL(urlPostString);
					URLConnection connection = urlPost.openConnection();
					connection.setDoOutput(true);
					connection.setReadTimeout(5000);
					connection.setConnectTimeout(5000);
					connection.setRequestProperty("Content-Type",
							"application/x-www-form-urlencoded");
					
					PrintWriter out = new PrintWriter(connection.getOutputStream());
					out.print("fileName");
					out.print('=');
					out.print(URLEncoder.encode(fileNameInURI, "UTF-8"));
					out.flush();
					
					// Get the response
					StringBuffer answer = new StringBuffer();
					BufferedReader reader = new BufferedReader(new InputStreamReader(
							connection.getInputStream(), "UTF-8"));
					String lineL;
					while ((lineL = reader.readLine()) != null) {
						answer.append(lineL);
					}
					reader.close();
					
					try {
						JSONObject json = new JSONObject(answer.toString());
						String status = json.getString("status");
						if (status.equals("1")) {
							md5_server_told = json.getString("result");
							if (md5_compute.equals(md5_server_told))
								return true;
							else 
								return false;
						} else
							return false;
						
					} catch (JSONException e)
					{
						return false;
					}
				} else {
					String md5_compute = GetBigFileMD5.getMD5(file);
					if (md5_compute.equals(fileNameInURI.substring(0,fileNameInURI.lastIndexOf('.')))) {
						return true;
					} else {
						return false;
					}
				}
			}
		} catch (MalformedURLException e) {
			throw new IOException("更新地址错误！");
		} catch (IOException e) {
			fos.close();
			bis.close();
			httpUrl.disconnect();			
			return false;
		}
	}
}
