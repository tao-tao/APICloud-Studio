/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.ui.wizards;

 import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
 
 public class NewFileWizardPage extends WizardPage
 {
   private Text containerText;
   private Text fileText;
   private ISelection selection;
 
   private String[] fileExtends = null;
 
   public NewFileWizardPage(ISelection selection, NewFileWizard fileWizard)
   {
     super("wizardPage");
     setTitle("create"+ " " + fileWizard.getFileType() + " " + "File Wizard");
     StringBuffer sb = new StringBuffer();
     if (!fileWizard.getFileExt().isEmpty()) {
       this.fileExtends = fileWizard.getFileExt().toLowerCase().split("\\,");
       for (String fileExtend : this.fileExtends) {
         sb.append("*." + fileExtend + " ");
       }
     }
     setDescription("This wizard creates a new file with " + sb.toString() + "extension");
     this.selection = selection;
   }
 
   public void createControl(Composite parent)
   {
     Composite container = new Composite(parent, 0);
     GridLayout layout = new GridLayout();
     container.setLayout(layout);
     layout.numColumns = 3;
     layout.verticalSpacing = 9;
     Label label = new Label(container, 0);
     label.setText("&Container:");
 
     this.containerText = new Text(container, 2052);
     this.containerText.setEditable(false);
     GridData gd = new GridData(768);
     this.containerText.setLayoutData(gd);
     this.containerText.addModifyListener(new ModifyListener() {
       public void modifyText(ModifyEvent e) {
         NewFileWizardPage.this.dialogChanged();
       }
     });
     Button button = new Button(container, 8);
     button.setText("Browse...");
     button.addSelectionListener(new SelectionAdapter() {
       public void widgetSelected(SelectionEvent e) {
         NewFileWizardPage.this.handleBrowse();
       }
     });
     label = new Label(container, 0);
     label.setText("&File name:");
 
     this.fileText = new Text(container, 2052);
     gd = new GridData(768);
     this.fileText.setLayoutData(gd);
     this.fileText.addModifyListener(new ModifyListener() {
       public void modifyText(ModifyEvent e) {
         NewFileWizardPage.this.dialogChanged();
       }
     });
     initialize();
     dialogChanged();
     setControl(container);
     new Label(container, 0);
   }
 
   private void initialize()
   {
     if ((this.selection != null) && (!(this.selection.isEmpty())) && 
       (this.selection instanceof IStructuredSelection)) {
       IStructuredSelection ssel = (IStructuredSelection)this.selection;
       if (ssel.size() > 1)
         return;
       Object obj = ssel.getFirstElement();
       if (obj instanceof IResource)
       {
         IContainer container;
         if (obj instanceof IContainer)
           container = (IContainer)obj;
         else
           container = ((IResource)obj).getParent();
         this.containerText.setText(container.getFullPath().toString());
       }
     }
     if ((this.fileExtends != null) && (this.fileExtends.length > 0))
       this.fileText.setText("new_file." + this.fileExtends[0]);
     else {
       this.fileText.setText("new_file");
     }
     this.fileText.setFocus();
     this.fileText.setSelection(0, "new_file".length());
   }
 
   private void handleBrowse()
   {
     ContainerSelectionDialog dialog = new ContainerSelectionDialog(
       getShell(), ResourcesPlugin.getWorkspace().getRoot(), false, 
       "Select new file container");
     if (dialog.open() == 0) {
       Object[] result = dialog.getResult();
       if (result.length == 1)
         this.containerText.setText(((Path)result[0]).toString());
     }
   }
 
   private void dialogChanged()
   {
     IResource container = ResourcesPlugin.getWorkspace().getRoot()
       .findMember(new Path(getContainerName()));
     String fileName = getFileName();
 
     if (getContainerName().length() == 0) {
       updateStatus("File container must be specified");
       return;
     }
     if ((container == null) || 
       ((container.getType() & 0x6) == 0)) {
       updateStatus("File container must exist");
       return;
     }
     if (!(container.isAccessible())) {
       updateStatus("Project must be writable");
       return;
     }
 
     if (fileName.length() == 0) {
       updateStatus("File name must be specified");
       return;
     }
     if (fileName.replace('\\', '/').indexOf(47, 1) > 0) {
       updateStatus("File name must be valid");
       return;
     }
     int dotLoc = fileName.lastIndexOf(46);
     if (dotLoc != -1) {
       String ext = fileName.substring(dotLoc + 1);
       if ((this.fileExtends != null) && (this.fileExtends.length > 0)) {
         List<String> extendsList = Arrays.asList(this.fileExtends);
         if (!(extendsList.contains(ext.toLowerCase()))) {
           StringBuffer sb = new StringBuffer();
           for (String fileExtend : this.fileExtends) {
             sb.append("*." + fileExtend + " ");
           }
           updateStatus("File extension must be \"" + sb.toString().trim() + "\"");
           return;
         }
       }
     }
 
     if (container instanceof IProject) {
       IProject project = (IProject)container;
       if (project.getFile(fileName).exists()) {
         updateStatus("File Already Exsit");
         return;
       }
     } else if (container instanceof IFolder) {
       IFolder folder = (IFolder)container;
       if (folder.getFile(fileName).exists()) {
         updateStatus("File Already Exsit");
         return;
       }
     }
     updateStatus(null);
   }
 
   private void updateStatus(String message) {
     setErrorMessage(message);
     setPageComplete(message == null);
   }
 
   public String getContainerName() {
     return this.containerText.getText();
   }
 
   public String getFileName() {
     return this.fileText.getText();
   }
 }
