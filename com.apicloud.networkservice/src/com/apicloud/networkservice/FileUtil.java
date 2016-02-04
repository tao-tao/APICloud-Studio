/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.networkservice;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FileUtil {
	public static File buildFile(String fileName, boolean isDirectory) {
        File target = new File(fileName);
        if (isDirectory) {
            target.mkdirs();
        } else {
            if (!target.getParentFile().exists()) {
                target.getParentFile().mkdirs();
                target = new File(target.getAbsolutePath());
            }
        }
        return target;
    }
	
	public static void updateJar(String path, String jarName) {
		File file = new File(path);
		System.out.println("Update path" + file.toString());
		List<File> files = new ArrayList<File>();
		for(File f : file.listFiles()) {
			System.out.println(f.toString());
			if(f.toString().contains(jarName)) {
				files.add(f);
				System.out.println("add compare obj:" + file.toString());
			}
		}
		update(files);
	}

	public static int compare(String s1, String s2){
        if( s1 == null && s2 == null )
            return 0;
        else if( s1 == null )
            return -1;
        else if( s2 == null )
            return 1;

        String[]
            arr1 = s1.split("[^a-zA-Z0-9]+"),
            arr2 = s2.split("[^a-zA-Z0-9]+")
        ;

        int i1, i2, i3;

        for(int ii = 0, max = Math.min(arr1.length, arr2.length); ii <= max; ii++){
            if( ii == arr1.length )
                return ii == arr2.length ? 0 : -1;
            else if( ii == arr2.length )
                return 1;

            try{
                i1 = Integer.parseInt(arr1[ii]);
            }
            catch (Exception x){
                i1 = Integer.MAX_VALUE;
            }

            try{
                i2 = Integer.parseInt(arr2[ii]);
            }
            catch (Exception x){
                i2 = Integer.MAX_VALUE;
            }

            if( i1 != i2 ){
                return i1 - i2;
            }

            i3 = arr1[ii].compareTo(arr2[ii]);

            if( i3 != 0 )
                return i3;
        }

        return 0;
    }
	
	private static void update(List<File> files) {
		File[] lists = files.toArray(new File[files.size()]);
		List<File> deleteFiles = new ArrayList<File>();
		Arrays.sort(lists, new Comparator<File>() {
			public int compare(File f1, File f2) {
				return compare(f1.getAbsoluteFile().toString(), f2.getAbsoluteFile().toString());
			}
			
			private  int compare(String s1, String s2){
		          if( s1 == null && s2 == null )
		              return 0;
		          else if( s1 == null )
		              return -1;
		          else if( s2 == null )
		              return 1;

		          String[]
		              arr1 = s1.split("[^a-zA-Z0-9]+"),
		              arr2 = s2.split("[^a-zA-Z0-9]+")
		          ;

		          int i1, i2, i3;

		          for(int ii = 0, max = Math.min(arr1.length, arr2.length); ii <= max; ii++){
		              if( ii == arr1.length )
		                  return ii == arr2.length ? 0 : -1;
		              else if( ii == arr2.length )
		                  return 1;

		              try{
		                  i1 = Integer.parseInt(arr1[ii]);
		              }
		              catch (Exception x){
		                  i1 = Integer.MAX_VALUE;
		              }

		              try{
		                  i2 = Integer.parseInt(arr2[ii]);
		              }
		              catch (Exception x){
		                  i2 = Integer.MAX_VALUE;
		              }

		              if( i1 != i2 ){
		                  return i1 - i2;
		              }

		              i3 = arr1[ii].compareTo(arr2[ii]);

		              if( i3 != 0 )
		                  return i3;
		          }

		          return 0;
		      }
		});
		int i = 0;
		while(i < lists.length - 1) {
			File f = lists[i];
			deleteFiles.add(f);
			i ++;
		}
		System.out.println("delete length" + deleteFiles.size());
		for(File f : deleteFiles) {
			if(f.isDirectory()) {
				deleteDirectory(f.getAbsolutePath().toString());
			} else {
				deleteFile(f.getAbsolutePath().toString());
			}
		}
	}
  
	public static List<String> readTxtFile(String filePath){
		List<String> lists = new ArrayList<String>();
        try {
                File file=new File(filePath);
                if(file.isFile() && file.exists()){ //file is exists
                    InputStreamReader read = new InputStreamReader(
                    new FileInputStream(file), "UTF-8");//code
                    BufferedReader bufferedReader = new BufferedReader(read);
                    String lineTxt = null;
                    while((lineTxt = bufferedReader.readLine()) != null){
                        System.out.println(lineTxt);
                       lists.add(lineTxt);
                    }
                    read.close();
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lists;
     
    }
	public static String readVersion(String filePath){
		String version = "";
        try {
	        File file=new File(filePath);
	        if(file.isFile() && file.exists()){ //file is exists
	            InputStreamReader read = new InputStreamReader(
	            new FileInputStream(file), "UTF-8");//code
	            BufferedReader bufferedReader = new BufferedReader(read);
	            String lineTxt = null;
	            while((lineTxt = bufferedReader.readLine()) != null){
	                System.out.println(lineTxt);
	              version += lineTxt;
	            }
	            read.close();
        }
        } catch (Exception e) {
            e.printStackTrace();
            return "1.0.0";
        }
        return version;
     
    }
     
	public static boolean deleteDirectory(String dir) {
		if (!dir.endsWith(File.separator)) {
			dir = dir + File.separator;
		}
		File dirFile = new File(dir);
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			System.out.println("delete fail" + dir + "folder is not exit!"); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
		boolean flag = true;
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag) {
					break;
				}
			} else {
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag) {
					break;
				}
			}
		}

		if (!flag) {
			System.out.println("delete folder fail"); //$NON-NLS-1$
			return false;
		}

		if (dirFile.delete()) {
			System.out.println("delete folder" + dir + "success"); //$NON-NLS-1$ //$NON-NLS-2$
			return true;
		} else {
			System.out.println("delete folder" + dir + "fail"); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
	}
	
	public static boolean delAllFiles(String path){
		 boolean flag = false;
	       File file = new File(path);
	       if (!file.exists()) {
	         return flag;
	       }
	       if (!file.isDirectory()) {
	         return flag;
	       }
	       String[] tempList = file.list();
	       File temp = null;
	       for (int i = 0; i < tempList.length; i++) {
	          if (path.endsWith(File.separator)) {
	             temp = new File(path + tempList[i]);
	          } else {
	              temp = new File(path + File.separator + tempList[i]);
	          }
	          if (temp.isFile()) {
	             temp.delete();
	          }
	          if (temp.isDirectory()) {
	        	  delAllFiles(path + "/" + tempList[i]);//先删除文件夹里面的文件
	             flag = true;
	          }
	       }
	       return flag;
	     }


	public static boolean deleteFile(String fileName) {
		File file = new File(fileName);
		if (file.isFile() && file.exists()) {
			file.delete();
			System.out.println("delete file" + fileName + "success"); //$NON-NLS-1$ //$NON-NLS-2$
			return true;
		} else {
			System.out.println("delete file" + fileName + "fail"); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
	}
	public static void copyFile(String sourcefile, String targetFile)
			throws IOException {
		File sourceFile = new File(sourcefile);
		if(sourceFile.exists()) {
			copyFile(sourceFile, new File(targetFile));
		}
	}
	
	public static void copyFile(File sourcefile, File targetFile)
			throws IOException {
		FileInputStream fis = new FileInputStream(sourcefile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		FileOutputStream fos = new FileOutputStream(targetFile);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		byte[] b = new byte[1024];
		int len = 0;
		while ((len = bis.read(b)) != -1) {
			bos.write(b, 0, len);
		}
		bos.flush();
		bis.close();
		bos.close();
		fos.close();
		fis.close();
	}

	public static void copyDirectiory(String sourceDir, String targetDir)
			throws IOException {
		(new File(targetDir)).mkdirs();
		File[] file = (new File(sourceDir)).listFiles();
		if (file == null) {
		}
		for (int i = 0; i < file.length; i++) {
			if (file[i].isFile()) {
				File sourceFile = file[i];
				File targetFile = new File(
						new File(targetDir).getAbsolutePath() + File.separator
								+ file[i].getName());
				copyFile(sourceFile, targetFile);
			}
			if (file[i].isDirectory() && !file[i].getName().startsWith(".")) {
				System.out.println(file[i].getName());
				String sourcePath = sourceDir + "/" + file[i].getName();
				String targetPath = targetDir + "/" + file[i].getName();
				copyDirectiory(sourcePath, targetPath);
			}
		}
	}
	
	
}
