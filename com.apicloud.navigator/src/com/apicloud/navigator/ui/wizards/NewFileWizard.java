/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.ui.wizards;

 import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.osgi.framework.Bundle;

import com.apicloud.navigator.Activator;
import com.apicloud.navigator.dialogs.Messages;

 public abstract class NewFileWizard extends Wizard
   implements INewWizard
 {
   private NewFileWizardPage page = null;
   public static final String TEMPLATE_PATH = "/templates/file/";
   private ISelection selection;
 
   public NewFileWizard()
   {
     setNeedsProgressMonitor(true);
     setWindowTitle(Messages.CREATEFILEWIZARD);
   }
 
   public void addPages()
   {
     this.page = new NewFileWizardPage(this.selection, this);
     addPage(this.page);
   }
 
   public void init(IWorkbench workbench, IStructuredSelection selection)
   {
     this.selection = selection;
   }
 
   public boolean performFinish()
   {
     final String containerName = this.page.getContainerName();
     final String fileName = this.page.getFileName();
     IRunnableWithProgress op = new IRunnableWithProgress() {
       public void run(IProgressMonitor monitor) throws InvocationTargetException {
         try {
           NewFileWizard.this.doFinish(containerName, fileName, monitor);
         } catch (CoreException e) {
           throw new InvocationTargetException(e);
         } finally {
           monitor.done();
         }
       }
     };
     try {
       getContainer().run(true, false, op);
     } catch (InterruptedException localInterruptedException) {
       return false;
     } catch (InvocationTargetException e) {
       Throwable realException = e.getTargetException();
       MessageDialog.openError(getShell(), Messages.WRONG, realException.getMessage());
       return false;
     }
     return true;
   }
 
   private void doFinish(String containerName, String fileName, IProgressMonitor monitor)
     throws CoreException
   {
     monitor.beginTask(Messages.CREATE + fileName, 2);
     IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
     IResource resource = root.findMember(new Path(containerName));
     if ((!(resource.exists())) || (!(resource instanceof IContainer))) {
       throwCoreException(Messages.CONTAINER + containerName + Messages.NONEXISTENT);
     }
     IContainer container = (IContainer)resource;
     IFile file = container.getFile(new Path(fileName));
     try {
       InputStream stream = openContentStream();
       if (file.exists())
         file.setContents(stream, true, true, monitor);
       else {
         file.create(stream, true, monitor);
       }
       stream.close();
     } catch (IOException localIOException) {
     }
     monitor.worked(1);
     monitor.setTaskName(Messages.OPENEDITOR);
     openIde(file);
     monitor.worked(1);
   }
 
   protected void openIde(final IFile file) {
     getShell().getDisplay().asyncExec(new Runnable() {
       public void run() {
         IWorkbenchPage page = 
           PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
         try {
             IDE.openEditor(page, file, true);
         }
         catch (PartInitException localPartInitException)
         {
         }
       }
     });
   }
 
   private InputStream openContentStream()
   {
     String contents = getDefaultTemplate();
     try {
       Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
       if (bundle == null)  return new ByteArrayInputStream("".getBytes());
       URL url = bundle.getResource("/templates/file/" + contents);
       InputStream is = url.openStream();
       int len = is.available();
       byte[] bs = new byte[len];
       is.read(bs);
       String template = new String(bs);
       return new ByteArrayInputStream(template.getBytes());
     }
     catch (Exception e) {
       e.printStackTrace();
       return new ByteArrayInputStream("".getBytes());
     }
   }
 
   private void throwCoreException(String message) throws CoreException {
     IStatus status = 
       new Status(4, Activator.PLUGIN_ID, 0, message, null);
     throw new CoreException(status);
   }
 
   public abstract String getFileType();
 
   public abstract String getFileExt();
 
   public abstract String getDefaultTemplate();
 }
