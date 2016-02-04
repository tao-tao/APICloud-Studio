/*******************************************************************************
 * Copyright (c) 2004, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
package org.tigris.subversion.subclipse.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;
import org.tigris.subversion.clientadapter.Activator;
import org.tigris.subversion.subclipse.core.util.InputDialog;
import org.tigris.subversion.subclipse.core.util.RebootConfirmDialog;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;

import com.apicloud.commons.util.OS;






/**
 * Handles the creation of SVNClients
 * 
 * @author Cedric Chabanois (cchab at tigris.org) 
 */
public class SVNClientManager {
	private static String DEFAULT_ADMIN_DIR = ".svn";
    private String svnClientInterface;
    private String svnAdminDir = null;
    private File configDir = null;
    private boolean fetchChangePathOnDemand = true;
    private HashMap<String, ISVNClientAdapter> cachedClients = null;
    public String javahlLibZipPackagePath;
    public static final String UNABLE_TO_LOAD_DEFAULT_CLIENT = "Unable to load default SVN Client";
    
    private boolean isFirst=true;
    public void startup(IProgressMonitor monitor) throws CoreException {
    }
    
    
	public void shutdown(IProgressMonitor monitor) throws CoreException {
	}

    /**
     * set the client interface to use
     * 
     * @param svnClientInterface
     */
    public void setSvnClientInterface(String svnClientInterface) {
    	this.svnClientInterface = svnClientInterface;
    }

    /**
     * get the current svn client interface used
     * @return
     */
    public String getSvnClientInterface() {
        return svnClientInterface;
    }    
    
    public String getSvnAdminDirectory() {
    	if (svnAdminDir == null)
    		return DEFAULT_ADMIN_DIR;
    	return svnAdminDir;
    }
    
	/**
	 * @param configDir The configDir to set.
	 */
	public void setConfigDir(File configDir) {
		this.configDir = configDir;
		if (cachedClients == null) return;
		
		// Update configDir in stored clients
		Set<String> keys = cachedClients.keySet();
		for (String key : keys) {
			ISVNClientAdapter svnClient = cachedClients.get(key);
			if (svnClient != null) {
				try {
					svnClient.setConfigDirectory(configDir);
				} catch (SVNClientException e) {
					break;
				}
			}
		}
	}
    
    /**
     * @return the cached ISVNClientAdapter for the client interface
     * @throws SVNClientException
     */
    public ISVNClientAdapter getSVNClient() throws SVNException {
    	ISVNClientAdapter svnClient = this.getAdapter(svnClientInterface);
    	if (svnClient == null) {
    		svnClient = this.getAdapter(null);
    	}
    	if (svnClient == null)
    		throw new SVNException("No client adapters available.");
    	return svnClient;
    }

	private void setupClientAdapter(ISVNClientAdapter svnClient)
			throws SVNException {
		if (configDir != null) {
    		try {
				svnClient.setConfigDirectory(configDir);
			} catch (SVNClientException e) {
	        	throw SVNException.wrapException(e);
			}
    	} 
    	if (SVNProviderPlugin.getPlugin().getSvnPromptUserPassword() != null)
    	    svnClient.addPasswordCallback(SVNProviderPlugin.getPlugin().getSvnPromptUserPassword());
    	if (svnAdminDir == null)
    		svnAdminDir = svnClient.getAdminDirectoryName();
	}
    
    private ISVNClientAdapter getAdapter(String key) throws SVNException {
    	ISVNClientAdapter client = null;
    	if (key == null) {
    		key = "default";
     	}
    	if (cachedClients != null) // See if we have cached a client
    		client = (ISVNClientAdapter) cachedClients.get(key);
    	if (client == null) {
    		if (!key.equals("default"))
    			try{
    				client = Activator.getDefault().getClientAdapter(svnClientInterface);

    			}catch(Exception e){
    				System.out.println("22222");
    			}
    		
    		if (client == null)
    			client = Activator.getDefault().getAnyClientAdapter();
    		if (client == null){
    			if (OS.isWindows()) {
	    	/*		Display.getDefault().syncExec(new Runnable() {
	    				public void run() {
	    					InputDialog dialog = new InputDialog(Display.getDefault()
	    							.getActiveShell(), Messages.SVNINSTALLINFO,
	    							Messages.SVNINSTALLINFO1, "", null);
	    					dialog.open();
	    					isFirst=false;
	    				}
	    			});*/
	    			
	    			throw new SVNException(UNABLE_TO_LOAD_DEFAULT_CLIENT);
    			} else {
	    			Display.getDefault().syncExec(new Runnable() {
	    				public void run() {
	    					InputDialog dialog = new InputDialog(Display.getDefault()
	    							.getActiveShell(), Messages.SVNINSTALLINFO,
	    							Messages.SVNINSTALLINFO1, "", null);
	    					int ret = dialog.open();
	    					if (ret == Window.OK) {
	    						
	    					     Bundle iosbundle=Platform.getBundle("com.apicloud.commons.lib");
	    					     try {
	    					    	 	javahlLibZipPackagePath =  new File(FileLocator.toFileURL(iosbundle.getResource("lib")).getFile()).getAbsolutePath();
	//    					    	 	commands_tar.add(javahlLibZipPackagePath);
	    					     } catch (IOException e) {
	    								e.printStackTrace();
	    						}
	    					     
	    					   //get the password user typed.
	    						String password = dialog.getValue();
	    						
	    						//write password to file
	    						String sPath = javahlLibZipPackagePath+"/pwd.txt";
	    						
	    					      BufferedWriter utput;
	    					      try {
	    					    	  	File f = new File(sPath);
	    					    	  	if (f.exists()) {
	    					    	  	} else {
	    					    	  		if (f.createNewFile()) {
	    					    	  		} else {
	    					    	  		}
	    					    	  	}
	    					    	  	//FileWriter ff = new FileWriter(f);
	    					    	  	java.io.FileOutputStream writerStream = new java.io.FileOutputStream(sPath);
	    					    	  	utput = new BufferedWriter(new java.io.OutputStreamWriter(writerStream, "US-ASCII"));
	    					    	  	utput.write(password+System.getProperty("line.separator"));
	    					    	  	utput.close();
	
	    					      } catch (Exception e) {
	    					    	  	e.printStackTrace();
	
	    					      }
	    						
	    						
	    						File file = new File("/opt/local/lib");
	    					     if (false == file.exists()) {
	    					    	 try {
	    					    		File workingDir = new File(javahlLibZipPackagePath);
										Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "chmod +x *.sh"}, null, workingDir);
										int status = p.waitFor();
										if (status != 0) {
											Display.getDefault().syncExec(new Runnable() {
					    	       				public void run() {
					    	       					MessageDialog.openError(Display.getDefault()
					    	       							.getActiveShell(), Messages.SVNINSTALLINFO3,
					    	       							Messages.SVNINSTALLINFO3);
					    	       					
					    	       				}
					    	        		   });
										}
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
	    					    	 
	    					    	 //construct terminal command ----  echo "$password"|sudo -S mkdir -p /opt/local/lib
	    					    	 	List commands_mkd = new ArrayList();
	
	    					    	 	commands_mkd.add("/bin/bash");
	    					    	 	commands_mkd.add("-c");
	    						     
	    					    	 	commands_mkd.add("./newdir.sh");
	    					    	 	
	    					    	 	ProcessBuilder pb = new ProcessBuilder((String[])commands_mkd.toArray(new String[commands_mkd.size()]));
	    					    	 	pb.redirectErrorStream(true);
	    					    	    BufferedReader reader = null;
	    					    	     Process process = null;
	    					    	     
	    					    	     try {
	    					    	    	 pb.directory(new File(javahlLibZipPackagePath));
	    					    	    	 process = pb.start();
	    										
	    					    	         InputStream is = process.getInputStream();
	    					    	         reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	    					    	         String line = null;
	    					    	          while ((line = reader.readLine()) != null) {
	
	    					    	        	   if(line.contains("incorrect password")) {
	//    					    	        		   throw new OperateException("\u5305\u542B\u4E2D\u6587\u540D\u7684\u6587\u4EF6!");
	    					    	        		   Display.getDefault().syncExec(new Runnable() {
	    					    	       				public void run() {
	    					    	       					InputDialog dialog = new InputDialog(Display.getDefault()
	    					    	       							.getActiveShell(), Messages.SVNINSTALLINFO,
	    					    	       							Messages.SVNINSTALLINFO2, "", null);
	    					    	       					dialog.open();
	    					    	       				}
	    					    	        		   });
	    					    	        		   break;
	    					    	        	   }
	    					    	          }
	    					    	     } catch (IOException e)
	    					    	       {
	//    					    	         throw new OperateException(e.getMessage());
	    					    	       } finally {
	    					    	         if (reader != null)
	    					    	           try {
	    					    	             reader.close();
	    					    	           } catch (IOException e) {
	    					    	             e.printStackTrace();
	    					    	           }
	    					    	       }
	    					    	 	
	    					     }
	    			     
	    					     
	    			    	 		//construct terminal command ----  sudo -S tar xvfz lib.tgz  <pwd.txt
	    			    	 		List commands_tar = new ArrayList();
	
	    			    	 		commands_tar.add("/bin/bash");
	    			    	 		commands_tar.add("-c");
	    					     
	    					     commands_tar.add("./untar.sh");
	    					     
	    					     System.out.println("commands :"+ commands_tar);
	    			    	 	
	    			    	 		ProcessBuilder pb = new ProcessBuilder((String[])commands_tar.toArray(new String[commands_tar.size()]));
	    			    	 		pb.redirectErrorStream(true);
	    			    	 		BufferedReader reader = null;
	    			    	 		Process process = null;
	    			    	     
	    			    	 		try {
	    			    	 			pb.command((String[])commands_tar.toArray(new String[commands_tar.size()]));
	    			    	 			//pb.directory(new File("/opt/local"));
	    			    	 			pb.directory(new File(javahlLibZipPackagePath));
	    			    	 			process = pb.start();
	    								
	    			    	 			InputStream is = process.getInputStream();
	    			    	 			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	    			    	 			String line = null;
	    			    	 			while ((line = reader.readLine()) != null) {
	
	    			    	 				if(line.contains("incorrect password")) {
	//    			    	        		 	  throw new OperateException("\u5305\u542B\u4E2D\u6587\u540D\u7684\u6587\u4EF6!");
	    			    	 					Display.getDefault().syncExec(new Runnable() {
	    			    	 						public void run() {
	    			    	 							InputDialog dialog = new InputDialog(Display.getDefault()
	    			    	 									.getActiveShell(), Messages.SVNINSTALLINFO,
	    			    	 									Messages.SVNINSTALLINFO2, "", null);
	    			    	 							dialog.open();
	    			    	 						}
	    			    	 					});
	    			    	 					break;
	    			    	 				}
	    			    	 			}
	    			    	 		} catch (IOException e)
	    			    	 		{
	//    			    	         throw new OperateException(e.getMessage());
	    			    	 			process.destroy();
	    			    	             
	    			    	             System.out.println(e.getMessage());
	    			    	 		} finally {
	    			    	 			final String cmd = "rm -f  "+javahlLibZipPackagePath+"/pwd.txt";
	    			    	 			try {
	    									Runtime.getRuntime().exec(cmd);
	    								} catch (IOException e1) {
	    									// TODO Auto-generated catch block
	    									e1.printStackTrace();
	    								}
	    			    	 			Display.getDefault().syncExec(new Runnable() {
	    									public void run() {
	    										RebootConfirmDialog dialog1=new RebootConfirmDialog(Display.getDefault().getActiveShell(), "\u91CD\u542F\u786E\u8BA4", "\u8BF7\u52A1\u5FC5\u91CD\u542FIDE\uFF0C\u5426\u5219\u65E0\u6CD5\u548C\u4E91\u7AEF\u4EE3\u7801\u540C\u6B65", "123", null);
	    										dialog1.open();
	    										
	    									}
	    								});
	    			    	 			if (reader != null)
	    			    	 				try {
	    			    	 					reader.close();
	    			    	 				} catch (IOException e) {
	    			    	 					e.printStackTrace();
	    			    	 				}
	    			    	 		}
	
	    						}
	    					}
	    				});
	    		
	    				throw new SVNException("\u786E\u8BA4\u4FDD\u5B58\u5F53\u524D\u4FE1\u606F","");
	    			}
    			}   //end if IDEUtil.isOsWindows()
    		
    		setupClientAdapter(client);
    		if (client.isThreadsafe())
    			cacheClient(key, client);
    	}
    	return client;
    }

	/**
	 * @return Returns the fetchChangePathOnDemand.
	 */
	public boolean isFetchChangePathOnDemand() {
		return fetchChangePathOnDemand;
	}
	/**
	 * @param fetchChangePathOnDemand The fetchChangePathOnDemand to set.
	 */
	
	public void setFetchChangePathOnDemand(
			boolean fetchChangePathOnDemand) {
		this.fetchChangePathOnDemand = fetchChangePathOnDemand;
	}
	
	public void returnSVNClient(ISVNClientAdapter client) {
		if (client == null || client.isThreadsafe())
			return;
		// For non-threadsafe clients we are done with the object so 
		// let it clean up any resources it has allocated.
		client.dispose();
		client = null;
	}
	
	private void cacheClient(String key, ISVNClientAdapter client){
		if (cachedClients == null)
			cachedClients = new HashMap<String, ISVNClientAdapter>();
		cachedClients.put(key, client);
	}
	
}
