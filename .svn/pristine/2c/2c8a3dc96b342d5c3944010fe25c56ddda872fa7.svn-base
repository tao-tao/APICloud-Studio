package com.apicloud.exe.update;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

public class UpdateIDE {
	private static String ideHome;
	public static void main(String[] args) {
		ideHome = args[0];
		if(args.length == 1) {
			baseUpdate();
		} else if(args.length == 3) {
			restartIDE();
			return;
		}else  {
			incrementalUpdate();
		}
	}

	private static void restartIDE() {
		try {
			closeIDE();
			Thread.sleep(500);
			closeADB();
			Thread.sleep(500);
			startIDE();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
	}

	private static void incrementalUpdate() {
		try {
			closeIDE();
			System.out.println("close IDE");
			Thread.sleep(500);
			closeADB();
			System.out.println("close ADB");
			Thread.sleep(500);
			 copyJar();
			 updateOSGIFolder();
			deleteUpdatFile();
			
			startIDE();
		}catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
		
	}

	private static void baseUpdate() {
		try {
			closeIDE();
			Thread.sleep(200);
			closeADB();
			Thread.sleep(200);
			if(isOsWindows()) {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
	    	} 
			 ProgressBar bar = new ProgressBar();
			deleteResource();
			unZip();
			bar.stop();
			bar.finish();
			Thread.sleep(200);
			bar.close();
			deleteUpdatFile();
			
			startIDE();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
	}

	private static void deleteUpdatFile() {
		deleteFile( ideHome + "download" + File.separator + "update.txt");
		deleteFile( ideHome + "download" + File.separator + "osgi.txt");
	}

	private static void unZip() {
		try {
			unzip(ideHome + "download" + File.separator + "base.zip", ideHome);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void updateOSGIFolder() throws IOException {
		deleteDirectory(ideHome + "configuration" + File.separator + "org.eclipse.osgi");
		deleteDirectory(ideHome + "configuration" + File.separator + "org.eclipse.core.runtime");
		deleteDirectory(ideHome + "configuration" + File.separator + "org.eclipse.update");
		refreshBundleInfo( ideHome + "dropins" + File.separator + "bundles.info", ideHome + "configuration" + File.separator + "org.eclipse.equinox.simpleconfigurator" + File.separator + "bundles.info");
		deleteFile(ideHome + "dropins"  + File.separator + "bundles.info");
	}
	
	private static void refreshBundleInfo(String sourcePath, String targetPaht) throws IOException {
		copyFile(new File(sourcePath), new File(targetPaht));
		
	}

	private static void deleteResource() {
		deleteDirectory(ideHome + "configuration");
		deleteDirectory(ideHome + "dropins");
		deleteDirectory(ideHome + "plugins");
	}
	
	private static void copyJar() {
		String dropinsThirdPath = ideHome + "dropins" + File.separator + "third";
		System.out.println("dropinsThirdPath = "+dropinsThirdPath);
		List<String> names = readTxtFile(dropinsThirdPath  + File.separator + "update.txt");
		for(String name : names) {
			updateJar(dropinsThirdPath, name, ideHome + "dropins");
		}
		System.out.println("deleteDirectory "+dropinsThirdPath);
		deleteDirectory(dropinsThirdPath);
	}

	private static void closeIDE() {
	    try {
	    	Process p = null;
		    	if(isOsWindows()) {
		    		p = Runtime.getRuntime().exec("taskkill /f /im \"APICloud.exe\"");
		    	} else {
		    		p = Runtime.getRuntime().exec("killall APICloud");
		    	}
	        InputStreamReader isr = new InputStreamReader(p.getInputStream(), "gbk");
	        InputStreamReader isr1 = new InputStreamReader(p.getErrorStream(), "gbk");
	        BufferedReader br = new BufferedReader(isr);
	        BufferedReader br1 = new BufferedReader(isr1);
			String msg = null;
			while ((msg = br.readLine()) != null) {
					System.out.println(msg);
			}
			while ((msg = br1.readLine()) != null) {
				System.out.println(msg);
		}
	    } catch(IOException e) {
	        e.printStackTrace();
	    }
	}
	
	private static void closeADB() {
	    try {
	        Process p = null;
	        if(isOsWindows()) {
				  p = Runtime.getRuntime()
				   .exec("taskkill /f /im \"adb.exe\"");
			   } else {
				   p = Runtime.getRuntime()
					.exec("killall adb");
			   }
	        InputStreamReader isr = new InputStreamReader(p.getInputStream(), "gbk");
	        InputStreamReader isr1 = new InputStreamReader(p.getErrorStream(), "gbk");
	        BufferedReader br = new BufferedReader(isr);
	        BufferedReader br1 = new BufferedReader(isr1);
			String msg = null;
			while ((msg = br.readLine()) != null) {
					System.out.println(msg);
			}
			while ((msg = br1.readLine()) != null) {
				System.out.println(msg);
		}
	    } catch(IOException e) {
	        e.printStackTrace();
	    }
	}

	private static void startIDE() {
		try {
			if(isOsWindows()) {
				String startCmd ="\"" + ideHome  + "APICloud.exe\"";
				Runtime.getRuntime().exec("cmd /c " + startCmd);
			} else {
				Runtime.getRuntime().exec("open " + ideHome + "APICloud.app");
			}
		        
		    }
		    catch (IOException ex) {
		      System.out.println(ex);
		    }
	}
	
	private static List<String> readTxtFile(String filePath){
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
	
	public static boolean isOsWindows() {
        try {
            return System.getProperty("os.name").toLowerCase().startsWith("win");
        } catch (SecurityException ex) {
            return false;
        }
	}
	
	public static void updateJar(String path, String jarName, String dropinsPath) {
		File file = new File(path);
		System.out.println("Update path:" + file.toString());
		List<File> files = new ArrayList<File>();
		for(File f : file.listFiles()) {
			System.out.println(f.toString());
			if(f.toString().contains(jarName)) {
				//files.add(f);
				try {
					copyFile(f, new File(dropinsPath+File.separator + f.getName()));
				} catch (IOException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
				System.out.println("add compare obj:" + file.toString());
			}
		}
		//update(files);
	}
	
	private static void update(List<File> files) {
		File[] lists = files.toArray(new File[files.size()]);
		List<File> deleteFiles = new ArrayList<File>();
		Arrays.sort(lists, new Comparator<File>() {
			@Override
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
		System.out.println("sort list");
		System.out.println(lists.length);
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
			System.out.println("delete file" + f.toString());
		}
	}
	
	private static void copyFile(File sourcefile, File targetFile)
			throws IOException {
		if(!sourcefile.exists()) {
			return;
		}
		if(targetFile.isFile() && targetFile.exists()) {
			System.out.println("delete file"+targetFile.toString());
			targetFile.delete();
		}
		
		if(targetFile.isDirectory() && targetFile.exists()) {
			System.out.println("delete directory"+targetFile.toString());
			//targetFile.delete();
			deleteDirectory(targetFile.toString());
		}
		
		
		if (sourcefile.isFile()) {
			System.out.println("copy file" + sourcefile.toString());
			FileInputStream input = new FileInputStream(sourcefile);
			BufferedInputStream inbuff = new BufferedInputStream(input);
			FileOutputStream out = new FileOutputStream(targetFile);
			BufferedOutputStream outbuff = new BufferedOutputStream(out);
			byte[] b = new byte[1024];
			int len = 0;
			while ((len = inbuff.read(b)) != -1) {
				outbuff.write(b, 0, len);
			}
			outbuff.flush();
			inbuff.close();
			outbuff.close();
			out.close();
			input.close();
		} else {
	        if (!targetFile.exists()) {  
	        	System.out.println("mkdir " + targetFile.toString());
	        	targetFile.mkdir();  
	        }  
	        String files[] = sourcefile.list();  
	        for (String file : files) {  
	            File srcFile = new File(sourcefile, file);  
	            File destFile = new File(targetFile, file);  
	            // 递归复制  
	            copyFile(srcFile, destFile);  
	        } 
		}

	}
	
		
	private static boolean deleteDirectory(String dir) {
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

	private static boolean deleteFile(String fileName) {
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
	
	@SuppressWarnings("rawtypes")
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
	                       File targetFile = buildFile(directoryPath
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
	                       buildFile(directoryPath + File.separator
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
}

class ProgressBar implements ActionListener, ChangeListener {

    JFrame frame = null;
    JProgressBar progressbar;
    JLabel label;
    Timer timer;

 	public void close() {
	 	frame.dispose();
 	}
 
 	public void stop() {
	  	timer.stop();
 	}

 	public void finish() {
	 	progressbar.setValue(100);
 	}

    public ProgressBar() {
       frame = new JFrame("APICloud Studio\u66F4\u65B0\u7BA1\u7406\u5668");
       frame.setBounds(100, 100, 400, 130);
       frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
       frame.setResizable(false);
       Image image= null;
		try {
		  	InputStream is=UpdateIDE.class.getResourceAsStream("32_32.png");    
			image = ImageIO.read(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(image != null) {
			frame.setIconImage(image);
		}

       Container contentPanel = frame.getContentPane();
       JLabel label_txt = new JLabel("\u7A0B\u5E8F\u66F4\u65B0\u4E2D\uFF0C\u8BF7\u4E0D\u8981\u624B\u52A8\u542F\u52A8APICloud Studio\u4EE5\u514D\u66F4\u65B0\u5931\u8D25\uFF01", JLabel.CENTER);
      
       label_txt.setForeground(Color.red);
       label = new JLabel("", JLabel.CENTER);
       progressbar = new JProgressBar();
       progressbar.setOrientation(JProgressBar.HORIZONTAL);
       progressbar.setMinimum(0);
       progressbar.setMaximum(100);
       progressbar.setValue(0);
       progressbar.setStringPainted(true);
       progressbar.addChangeListener(this);
       progressbar.setPreferredSize(new Dimension(300, 20));
       progressbar.setBorderPainted(true);
       progressbar.setBackground(Color.green);

       timer = new Timer(700, this);
       contentPanel.add(label_txt, BorderLayout.NORTH);
       contentPanel.add(label, BorderLayout.CENTER);
       contentPanel.add(progressbar, BorderLayout.SOUTH);
       frame.setVisible(true);
       timer.start();
    }

	@Override
	public void stateChanged(ChangeEvent e) {
	       int value = progressbar.getValue();

	       if (e.getSource() == progressbar) {
	           label.setText("\u7A0D\u540E\u4F1A\u91CD\u65B0\u542F\u52A8APICloud Studio\u3002\u66F4\u65B0\u8FDB\u5EA6\uFF1A" + Integer.toString(value) + "%");
	           label.setForeground(Color.blue);
	       }
	}

	@Override
	public void actionPerformed(ActionEvent e) {

	    if (e.getSource() == timer) {
	        int value = progressbar.getValue();
	        if (value < 99) {
	            progressbar.setValue(++value);
	        } else {
	            timer.stop();
	        }
	    }
	}
}