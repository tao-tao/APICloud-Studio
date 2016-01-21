/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.ui.editors;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.ImportExportWizard;
import org.eclipse.ui.part.EditorPart;
import org.json.JSONException;
import org.json.JSONObject;

import com.apicloud.authentication.AuthenticActivator;
import com.apicloud.navigator.composite.UZWizardComposite;
import com.apicloud.navigator.ui.wizards.CreateAPICloudProjectWizard;
import com.aptana.core.util.PlatformUtil;

@SuppressWarnings("restriction")
public class APICloudWizardEditor extends EditorPart {
	private UZWizardComposite browserComposite;
	private String ip;
	public APICloudWizardEditor() {
	}

	@Override
	public void doSave(IProgressMonitor monitor) {

	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		initInfo();
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		 try {
			      String url = FileLocator.toFileURL(
			        super.getClass().getResource("/content/ideStart.html")).toString();
			      parent.setLayout(new FillLayout());
			      if(testConnection()) {
			    	  browserComposite = 
			    		  new UZWizardComposite(parent, SWT.NONE, "http://" + ip + "/ide/ideStart.html");
			      } else {
			    	  browserComposite = 
				    			  new UZWizardComposite(parent, SWT.NONE, url);
			      } 
			      if (PlatformUtil.isWindows()) {
			    	  Browser browser = (Browser)browserComposite.getBrowser();
						browser.addProgressListener(
						         new ProgressListener() {
									@Override
									public void completed(ProgressEvent event) {
										APICloudWizardEditor.this.registerWizardFunction(APICloudWizardEditor.this.browserComposite);
									}
									public void changed(ProgressEvent event) {
									}
								});
					    } else {
					    	Browser browser = (Browser)browserComposite.getBrowser();
					    	browser.addProgressListener(
							         new ProgressListener() {
										@Override
										public void completed(ProgressEvent event) {
											APICloudWizardEditor.this.registerWizardFunction(APICloudWizardEditor.this.browserComposite);
										}
										public void changed(ProgressEvent event) {
										}
									});
					    }
			      
			      this.browserComposite.forceFocus();
			     } catch (Exception e) {
			    	 e.printStackTrace();
			     }
	}

	protected void registerWizardFunction(final UZWizardComposite browserComposite)
	 {
	    browserComposite.registerFunction("create", 
	    	      new UZWizardComposite.IScriptHandler()
	    	    {
	    	      public Object handle(Object[] arguments) {
						CreateAPICloudProjectWizard wizard = new CreateAPICloudProjectWizard();
						wizard.init(PlatformUI.getWorkbench(), StructuredSelection.EMPTY);
						WizardDialog dialog = new WizardDialog(getSite().getShell(), wizard);
						dialog.create();
						dialog.open();
	    	        return null;
	    	      }
	    	    });

	    browserComposite.registerFunction("importApp", 
	    	      new UZWizardComposite.IScriptHandler()
	    	    {
				public Object handle(Object[] arguments) {
	    	    	   ImportExportWizard wizard = new ImportExportWizard(ImportExportWizard.IMPORT);
	    	   		wizard.init(PlatformUI.getWorkbench(), StructuredSelection.EMPTY);

	    	   		IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault().getDialogSettings();
	    	   		IDialogSettings wizardSettings = workbenchSettings.getSection("ImportExportAction"); //$NON-NLS-1$
	    	   		if (wizardSettings == null)
	    	   		{
	    	   			wizardSettings = workbenchSettings.addNewSection("ImportExportAction"); //$NON-NLS-1$
	    	   		}
	    	   		wizard.setDialogSettings(wizardSettings);
	    	   		wizard.setForcePreviousAndNextButtons(true);

	    	   		WizardDialog dialog = new WizardDialog(getSite().getShell(), wizard);
	    	   		dialog.open();
	    	        return null;
	    	      }
	    	    });
	    
	    browserComposite.registerFunction("openurl", 
	    		 new UZWizardComposite.IScriptHandler()
	    {
		public Object handle(Object[] arguments) {
	    	String url = (String)arguments[0];
	    	
	    	if(url != null && !url.equals("")) {
	    		openUrl(url);
	    	}
	      
	        return null;
	      }
		private void openUrl(String url) {
			Desktop desktop = Desktop.getDesktop();
			  if ((Desktop.isDesktopSupported()) && (desktop.isSupported(Desktop.Action.BROWSE))) {
				   try {
			         URI uri = new URI(url);
				         desktop.browse(uri);
				     } catch (URISyntaxException e) {
				        e.printStackTrace();
				    } catch (IOException e) {
				         e.printStackTrace();
			     }
			    }
		}
	    });
	
	}
	
	@Override
	public void setFocus() {
	}
	private void initInfo() {
		Properties p = AuthenticActivator.getProperties();
		 ip = p.getProperty("ip");
	}

  	private boolean testConnection() {
		String msg = com.apicloud.navigator.Activator.network_instance.checkServiceState(ip); //$NON-NLS-1$ //$NON-NLS-2$
		JSONObject json;
		try {
			json = new JSONObject(msg);
			json.getString("status"); //$NON-NLS-1$
		} catch (JSONException e) {
			return true;
		}
		return false;
	}
}
