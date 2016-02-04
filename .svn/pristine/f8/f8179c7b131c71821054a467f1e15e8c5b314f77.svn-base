/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.dialogs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.osgi.framework.Bundle;

import com.apicloud.commons.model.Config;
import com.apicloud.commons.util.IDEUtil;
import com.apicloud.commons.util.ResourceManager;
import com.apicloud.loader.platforms.android.AndroidDevice;
import com.apicloud.loader.platforms.ios.IOSDevice;
import com.apicloud.updatemanager.dialog.CheckLoaderDialog;

public class RunMobileDialog extends Dialog {
	private final FormToolkit formToolkit = new FormToolkit(
			Display.getDefault());
	private ComboViewer list;
	private IProject[] projects;
	private Shell parentShell;
	private IStructuredSelection select;
	
	private static final int ALLLOADER = 0;
	private static final int ALOADER = 1;
	private static final int ILOADER = 2;
	
	private static boolean isHasANDROIDBaseLoader = false;
	private static boolean isHasANDROIDAppLoader = false;
	private static boolean isHasIOSBaseLoader = false;
	private static boolean isHasIOSAppLoader = false;
	private String CUSTOM_ANDROID_BASE;
	private String CuSTOm_IOSROID_BASE;
	private CheckLoaderDialog loaderdialog = null;
	private List<AndroidDevice> aMobiles;
	private List<IOSDevice>iMobiles;

	public RunMobileDialog(Shell parentShell, IProject[] projects, List<AndroidDevice> aMobiles,List<IOSDevice>iMobiles) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX);
		this.aMobiles = aMobiles;
		this.iMobiles=iMobiles;
		this.projects = projects;
		this.parentShell=parentShell;
	}
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.RunMobileDialog_SYNC_MOBILE);
		
		Rectangle parentBounds = parentShell.getBounds();
		Rectangle shellBounds = newShell.getBounds();
		newShell.setLocation(parentBounds.x
				+ (parentBounds.width - shellBounds.width) / 2, parentBounds.y
				+ (parentBounds.height - shellBounds.height) / 2);
	}
	protected void createButtonsForButtonBar(Composite parent) {
		Button button = createButton(parent, 0, Messages.RunSimulatorDialog_RUN, true);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		Button button_1 = createButton(parent, 1, Messages.RunSimulatorDialog_EXIT, true);
		button_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
	}

	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);
		comp.setLayout(new GridLayout(1, false));

		Composite composite = new Composite(comp, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));
		formToolkit.adapt(composite);
		formToolkit.paintBordersFor(composite);

		Composite composite_1 = new Composite(composite, SWT.NONE);
		composite_1.setLayout(new GridLayout(1, false));
		composite_1.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true,
				false, 1, 1));
		formToolkit.adapt(composite_1);
		formToolkit.paintBordersFor(composite_1);

		Composite composite_6 = new Composite(composite_1, SWT.NONE);
		composite_6.setLayout(new GridLayout(2, false));
		composite_6.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 1, 1));
		composite_6.setBounds(0, 0, 64, 64);
		formToolkit.adapt(composite_6);
		formToolkit.paintBordersFor(composite_6);
		
		Label lblNewLabel_project = new Label(composite_6, SWT.NONE);
		lblNewLabel_project.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		formToolkit.adapt(lblNewLabel_project, true, true);
		lblNewLabel_project.setText(Messages.RunSimulatorDialog_PROJECT_NAME);

		
		this.list = new ComboViewer(composite_6, SWT.READ_ONLY);
		GridData gd_text1 = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_text1.widthHint = 181;
		this.list.getCombo().setLayoutData(gd_text1);
		this.list.setLabelProvider(new ProjectLabelProvider());
		this.list.setContentProvider(new ArrayContentProvider());
		this.list.setInput(projects);
		this.list.getCombo().select(0);
		formToolkit.adapt(list.getCombo(), true, true);

		return parent;
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == 0) {
			StructuredSelection ss = (StructuredSelection)this.list.getSelection();
			IProject project = (IProject)ss.getFirstElement();
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			if(validate(getID(project),project)){
				
			
			final SyncApplicationDialog sad = new SyncApplicationDialog(shell, aMobiles,iMobiles, project);
			final CountDownLatch threadSignal = new CountDownLatch(aMobiles.size()+iMobiles.size());
			sad.open();
			sad.run(threadSignal);
			Job job = new WorkspaceJob("")
			{
				@Override
				public IStatus runInWorkspace(IProgressMonitor monitor)
						throws CoreException {
					try {
						threadSignal.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					sad.finish();
					return Status.OK_STATUS;
				}
			};
			job.schedule();
			close();
		}else if (buttonId == 1) {
			close();
		}else{
			close();
		}
		} else{
			close();
		}
	}
	
	
	private void dispose() {
		ResourceManager.disposeImages();
	}
	
	@Override
	public boolean close() {
		dispose();
		return super.close();
	}
	

	public boolean validate(String path,Object obj){

		Bundle androidbundle = Platform
				.getBundle("com.apicloud.loader.platforms.android");
		Bundle iosbundle = Platform.getBundle("com.apicloud.loader.platforms.ios");

		CUSTOM_ANDROID_BASE = IDEUtil.getInstallPath() + "apploader/"
				+ path + "/load.apk";
		File appaloaderFile = new File(CUSTOM_ANDROID_BASE);
		if (!appaloaderFile.exists()) {
			setHasANDROIDAppLoader(false);
		} else {
			setHasANDROIDAppLoader(true);
		}

		CuSTOm_IOSROID_BASE = IDEUtil.getInstallPath() + "apploader/"
				+ path + "/load.ipa";
		File appiloaderFile = new File(CuSTOm_IOSROID_BASE);
		if (!appiloaderFile.exists()) {
			setHasIOSAppLoader(false);
		} else {
			setHasIOSAppLoader(true);
		}

		if (androidbundle.getResource("base/load.apk") == null) {
			setHasANDROIDBaseLoader(false);
		} else {
			setHasANDROIDBaseLoader(true);
		}
		if (iosbundle.getResource("base/load.ipa") == null) {
			setHasIOSBaseLoader(false);
		} else {
			setHasIOSBaseLoader(true);
		}

		if (hasANDROIDLoader() || hasIOSLoader()) {
			if(select!=null){
				
			if (hasANDROIDLoader() && hasIOSLoader()) {
				loaderdialog = new CheckLoaderDialog(Display.getCurrent()
						.getActiveShell(), ALLLOADER, select);
			} else if (hasANDROIDLoader() && (!hasIOSLoader())) {
				loaderdialog = new CheckLoaderDialog(Display.getCurrent()
						.getActiveShell(), ALOADER, select);
			} else {
				loaderdialog = new CheckLoaderDialog(Display.getCurrent()
						.getActiveShell(), ILOADER, select);
			}
			}else{

				
				if (hasANDROIDLoader() && hasIOSLoader()) {
					loaderdialog = new CheckLoaderDialog(Display.getCurrent()
							.getActiveShell(), ALLLOADER, obj);
				} else if (hasANDROIDLoader() && (!hasIOSLoader())) {
					loaderdialog = new CheckLoaderDialog(Display.getCurrent()
							.getActiveShell(), ALOADER, obj);
				} else {
					loaderdialog = new CheckLoaderDialog(Display.getCurrent()
							.getActiveShell(), ILOADER, obj);
				}
				
			}
			loaderdialog.open();
			return false;

		}
		return true;

	}
	public String getID(Object obj) {
		IProject project = (IProject) obj;
		String path = project.getLocation().toString();
		String id = "";
		File fileToRead = new File(path + File.separator + "config.xml");
		try {
			Config config = Config.loadXml(new FileInputStream(fileToRead));
			id = config.getId();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return id;
	}
	public static boolean hasANDROIDLoader() {
		if ((!isHasANDROIDBaseLoader()) && (!isHasANDROIDAppLoader())) {
			return true;
		}

		return false;
	}

	public static boolean hasIOSLoader() {
		if ((!isHasIOSBaseLoader()) && (!isHasIOSAppLoader())) {
			return true;
		}
		return false;
	}

	public static boolean isHasANDROIDBaseLoader() {
		return isHasANDROIDBaseLoader;
	}

	public static void setHasANDROIDBaseLoader(boolean isHasANDROIDBaseLoader) {
		RunMobileDialog.isHasANDROIDBaseLoader = isHasANDROIDBaseLoader;
	}

	public static boolean isHasANDROIDAppLoader() {
		return isHasANDROIDAppLoader;
	}

	public static void setHasANDROIDAppLoader(boolean isHasANDROIDAppLoader) {
		RunMobileDialog.isHasANDROIDAppLoader = isHasANDROIDAppLoader;
	}

	public static boolean isHasIOSBaseLoader() {
		return isHasIOSBaseLoader;
	}

	public static void setHasIOSBaseLoader(boolean isHasIOSBaseLoader) {
		RunMobileDialog.isHasIOSBaseLoader = isHasIOSBaseLoader;
	}

	public static boolean isHasIOSAppLoader() {
		return isHasIOSAppLoader;
	}

	public static void setHasIOSAppLoader(boolean isHasIOSAppLoader) {
		RunMobileDialog.isHasIOSAppLoader = isHasIOSAppLoader;
	}

}
