/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.ui.wizards;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.UIJob;

import com.apicloud.navigator.Activator;
import com.apicloud.navigator.dialogs.Messages;

public class TempleteFrameWizard extends Wizard
implements INewWizard {
	private String pageName;
	private TempleteFarmeWizardPage page;
	private ISelection selection;
	public TempleteFrameWizard() {
		setWindowTitle(Messages.CREATENEWPAGEWIZARD);
	}

	public boolean canFinish() {
		if (page.isCanFinish()) {
			return true;
		}
		return false;
	}
	
	@Override
	public void addPages() {
		page = new TempleteFarmeWizardPage(selection);
		addPage(page);
	}

	@Override
	 public boolean performFinish()
	   {
	     final String targetPath = this.page.getSourcePath();
	     final String fileName = this.page.getPagePath();
	     pageName = this.page.getPageName();
			Job menuJob = new UIJob("")
			{
				public IStatus runInUIThread(IProgressMonitor monitor)
				{
				    try {
						doFinish(targetPath, fileName, monitor);
					} catch (CoreException e) {
						return Status.CANCEL_STATUS;
					}
					return Status.OK_STATUS;
				}
			};
			menuJob.schedule(300L);
	     return true;
	   }
	 
	   private void doFinish(final String targetPath, final String path, final IProgressMonitor monitor)
	     throws CoreException
	   {
				try {
					String	sourcePath = new File(FileLocator.toFileURL(Platform.getBundle(Activator.PLUGIN_ID).getResource("templates" + File.separator + path)).getFile()).getAbsolutePath();
					File[] files = (new File(sourcePath)).listFiles();
					monitor.beginTask(Messages.CREATE + pageName, files.length + 1);
					for (int i = 0; i < files.length; i++) {
						if (files[i].isFile()) {
							copyFile(files[i], new File(targetPath + File.separator
									+ files[i].getName()));
						}
						if (files[i].isDirectory()&& !files[i].getName().startsWith(".")) {
							String sorceDir = sourcePath + File.separator
									+ files[i].getName();
							String targetDir = targetPath + File.separator
									+ files[i].getName();
							copyDirectiory(sorceDir, targetDir);
						}
						monitor.worked(1);
					}
					page.getIProject().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
					} catch (IOException e) {
						e.printStackTrace();
					}  catch (CoreException e) {
						e.printStackTrace();
					}
				openIde();
				monitor.worked(1);
				monitor.done();
	   }
	   
	   
	   protected void openIde() {
		   Display.getDefault().asyncExec(new Runnable() {
	       public void run() {
	    	   final IFile file  = (IFile)page.getIProject().findMember(new Path("/html/" + pageName + "_window.html"));
	         IWorkbenchPage page = 
	           PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	         try {
	             IDE.openEditor(page, file);
	         }
	         catch (PartInitException localPartInitException)
	         {
	        	 localPartInitException.printStackTrace();
	         }
	       }
	     });
	   }

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		
	}
	
	private void copyWindowFile(File sourcefile, File targetFile)
			throws IOException {
		 FileInputStream fis=new FileInputStream(sourcefile);
	        InputStreamReader isr=new InputStreamReader(fis, "UTF-8");
	        BufferedReader br = new BufferedReader(isr);
	        FileOutputStream fos=new FileOutputStream(targetFile);
	        OutputStreamWriter osw=new OutputStreamWriter(fos, "UTF-8");
	        BufferedWriter  bw=new BufferedWriter(osw);
	        String line="";
	        while ((line=br.readLine())!=null) {
	        	if(line.contains("apicloudFrame")) {
	        		line = line.replace("apicloudFrame", pageName + "_frame");
	        	}
	        	   bw.write(line+"\t\n");
	        }
	        br.close();
	        isr.close();
	        fis.close();
	        bw.close();
	        osw.close();
	        fos.close();

	}
	
	private void copyFile(File sourcefile, File targetFile)
			throws IOException {
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

	}

	private void copyDirectiory(String sourceDir, String targetDir)
			throws IOException {
		(new File(targetDir)).mkdirs();
		File[] file = (new File(sourceDir)).listFiles();
		if (file == null) {
		}
		for (int i = 0; i < file.length; i++) {
			if (file[i].isFile()) {
				 String fileName = file[i].getName();
				 if(fileName.equals("apicloudWindow.html")) {
					 fileName = pageName + "_window.html";
						File sourceFile = file[i];
						File targetFile = new File(
								new File(targetDir).getAbsolutePath() + File.separator
										+ fileName);
						copyWindowFile(sourceFile, targetFile);
					 continue;
				 } 
				 if(fileName.contains("apicloudFrame")) {
					 fileName = fileName.replace("apicloudFrame", pageName + "_frame");
						File sourceFile = file[i];
						File targetFile = new File(
								new File(targetDir).getAbsolutePath() + File.separator
										+ fileName);
						copyWindowFile(sourceFile, targetFile);
						 continue;
				 }
				File sourceFile = file[i];
				File targetFile = new File(
						new File(targetDir).getAbsolutePath() + File.separator
								+ fileName);
				copyFile(sourceFile, targetFile);
			}
			if (file[i].isDirectory() && !file[i].getName().startsWith(".")) {
				String sourcePath = sourceDir + File.separator + file[i].getName();
				String targetPath = targetDir + File.separator + file[i].getName();
				copyDirectiory(sourcePath, targetPath);
			}
		}

	}
}
