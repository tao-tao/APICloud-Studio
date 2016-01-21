/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.commons.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

public class ZipUtil {

	public static void zip(String path, List<File> files) throws IOException {
		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(   
				new FileOutputStream(path), 1024));   
		for(File f : files) {
			String zipName = f.getName();
			if(!f.getName().contains(".png")) { 
				zipName = f.getName() + ".xml"; 
			}
			ZipEntry ze = new ZipEntry(zipName);
			ze.setTime(f.lastModified());
		    DataInputStream dis = new DataInputStream(new BufferedInputStream(   
		                new FileInputStream(f)));   
	        zos.putNextEntry(ze);   
	        int c;   
	        while ((c = dis.read()) != -1) {   
	            zos.write(c);   
	        }   
		}
		zos.setEncoding("gbk");    
        zos.closeEntry();   
        zos.close();  
	}
	//	解压zip文件
	public static void unzip(String zipFilePath, String targetPath) throws IOException {
	       OutputStream os = null;
	       InputStream is = null;
	       ZipFile zipFile = null;
	       try {
	           zipFile = new ZipFile(zipFilePath);
	           String directoryPath = "";
	           if (null == targetPath || "".equals(targetPath)) {
	               directoryPath = zipFilePath.substring(0, zipFilePath
	                       .lastIndexOf("."));
	           } else {
	               directoryPath = targetPath;
	           }
	           @SuppressWarnings("rawtypes")
			Enumeration entryEnum = zipFile.getEntries();
	           if (null != entryEnum) {
	               ZipEntry zipEntry = null;
	               while (entryEnum.hasMoreElements()) {
	                   zipEntry = (ZipEntry) entryEnum.nextElement();
	                   if (zipEntry.isDirectory()) {
	                		   File file = new File(directoryPath + File.separator
		                               + zipEntry.getName());
	                	   if(!file.exists()) {
	                		   file.mkdirs();
	                	   }
	                       continue;
	                   }
	                   if (zipEntry.getSize() > 0) {
	                       File targetFile = FileUtil.buildFile(directoryPath
	                               + File.separator + zipEntry.getName(), false);
	                       os = new BufferedOutputStream(new FileOutputStream(
	                               targetFile));
	                       is = zipFile.getInputStream(zipEntry);
	                       byte[] buffer = new byte[4096];
	                       int readLen = 0;
	                       while ((readLen = is.read(buffer, 0, 4096)) >= 0) {
	                           os.write(buffer, 0, readLen);
	                       }

	                       os.flush();
	                       os.close();
	                   } else {
	                       FileUtil.buildFile(directoryPath + File.separator
	                               + zipEntry.getName(), true);
	                   }
	               }
	           }
	       } catch (IOException ex) {
	           throw ex;
	       } finally {
	           if(zipFile != null){
	        	   zipFile.close();
	           }
	           if (null != is) {
	               is.close();
	           }
	           if (null != os) {
	               os.close();
	           }
	       }
	   }
}
