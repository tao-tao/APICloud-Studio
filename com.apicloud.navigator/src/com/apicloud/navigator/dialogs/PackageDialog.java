/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.dialogs;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.apicloud.commons.util.IDEUtil;
import com.apicloud.commons.util.OS;
import com.apicloud.commons.util.ResourceManager;
import com.apicloud.makepackage.core.PackageJob;


public class PackageDialog extends Dialog {
	private final FormToolkit formToolkit = new FormToolkit(
			Display.getDefault());
	private Text appNameText;
	private String name;
	private String id;
	private String path;
	private Button button_iphone;
	private Button button_android;
	private String test;
	private String appName;
	private String fullScreen;
	private String ide_home;
	private Shell parentShell;
	public PackageDialog(Shell parentShell, String name, String id, String path,String appName, String fullScreen) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX);
		
		this.name = name;
		this.path = path;
		this.id = id;
		this.appName = appName;
		this.fullScreen = fullScreen;
		this.parentShell=parentShell;
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.PackageAppItemDialog_PACKAGE_LOCATION);
		newShell.setSize(358, 400);
		Rectangle parentBounds = parentShell.getBounds();
		Rectangle shellBounds = newShell.getBounds();
		newShell.setLocation(parentBounds.x
				+ (parentBounds.width - shellBounds.width) / 2, parentBounds.y
				+ (parentBounds.height - shellBounds.height) / 2);
	}

	protected void createButtonsForButtonBar(Composite parent) {
		Button button = createButton(parent, 0, Messages.PackageAppItemDialog_PACKAGE, true);
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
		composite_1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true,
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

		Label lblNewLabel_14 = new Label(composite_6, SWT.NONE);
		lblNewLabel_14.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		formToolkit.adapt(lblNewLabel_14, true, true);
		lblNewLabel_14.setText(Messages.APPNAME);

		appNameText = new Text(composite_6, SWT.BORDER | SWT.READ_ONLY);
		GridData gd_text = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_text.widthHint = 181;
		appNameText.setLayoutData(gd_text);
		appNameText.setText(this.name);
		appNameText.setEnabled(false);
		formToolkit.adapt(appNameText, true, true);

		Composite composite_7 = new Composite(composite, SWT.NONE);
		composite_7.setLayout(null);
		GridData gd_composite_7 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_composite_7.heightHint = 28;
		composite_7.setLayoutData(gd_composite_7);
		formToolkit.adapt(composite_7);
		formToolkit.paintBordersFor(composite_7);

		Label lblNewLabel_15 = new Label(composite_7, SWT.NONE);
		lblNewLabel_15.setBounds(38, 8, 57, 17);
		formToolkit.adapt(lblNewLabel_15, true, true);
		lblNewLabel_15.setText(Messages.PackageAppItemDialog_PLATMFROM);

		button_iphone = new Button(composite_7, SWT.CHECK);
		button_iphone.setBounds(96, 8, 106, 17);
		button_iphone.setSelection(true);
		button_iphone.setText("IOS                ");
		formToolkit.adapt(button_iphone, true, true);

		button_android = new Button(composite_7, SWT.CHECK);
		button_android.setBounds(212, 8, 106, 17);
		button_android.setSelection(true);
		button_android.setText("Android");
		formToolkit.adapt(button_android, true, true);
		
		Group grpIde = new Group(composite, SWT.NONE);
		grpIde.setText(Messages.PackageAppItemDialog_PACKAGE_TITLE);
		grpIde.setLayout(new GridLayout(1, false));
		grpIde.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		formToolkit.adapt(grpIde);
		formToolkit.paintBordersFor(grpIde);
		
		Label lblNewLabel = new Label(grpIde, SWT.NONE);
		formToolkit.adapt(lblNewLabel, true, true);
		lblNewLabel.setText(Messages.PackageAppItemDialog_MESSAGE_ONE);
		
		Label lblNewLabel_1 = new Label(grpIde, SWT.NONE);
		formToolkit.adapt(lblNewLabel_1, true, true);
		lblNewLabel_1.setText(Messages.PackageAppItemDialog_MESSAGE_TWO);
		
		Label lblNewLabel_2 = new Label(grpIde, SWT.NONE);
		formToolkit.adapt(lblNewLabel_2, true, true);
		lblNewLabel_2.setText(Messages.PackageAppItemDialog_MESSAGE_THREE);
		
		Label lblNewLabel_ = new Label(grpIde, SWT.NONE);
		formToolkit.adapt(lblNewLabel_, true, true);
		lblNewLabel_.setText(Messages.PackageAppItemDialog_MESSAGE_FOUR);
		
		Link link = new Link(grpIde, SWT.NONE);
		formToolkit.adapt(link, true, true);
		link.setText(Messages.PackageAppItemDialog_MESSAGE_FIVE);
		
		Label l = new Label(composite, SWT.NONE);
		formToolkit.adapt(l, true, true);
		l.setText(""); 
		l = new Label(composite, SWT.NONE);
		formToolkit.adapt(l, true, true);
		l.setText(""); 
		l = new Label(composite, SWT.NONE);
		formToolkit.adapt(l, true, true);
		l.setText(""); 
		
		return parent;
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == 0) {
			if (!new File(path + File.separator + "icon" + File.separator + "icon150x150.png").exists()) {
				MessageDialog.openInformation(
								null,
								Messages.AddFeatureDialog_INFORMATION,
								Messages.PackageAppItemDialog_MUST_BE_A_ICON);
				return;
			}
			if (!new File(path + File.separator + "launch" + File.separator + "launch1080x1920.png").exists()) {
				MessageDialog.openInformation(
								null,
								Messages.AddFeatureDialog_INFORMATION,
								Messages.PackageAppItemDialog_MUST_BE_B_ICON);
				return;
			}
			if (!button_iphone.getSelection() && !button_android.getSelection()) {
				return;
			}
			test = IDEUtil.getInstallPath();
			java.io.File file = new java.io.File(test);
			ide_home = test;
			test = file.getParent() + File.separator;
			int i = 0;
			if (button_android.getSelection()) {
				i = i + 1;
			}
			if (button_iphone.getSelection()) {
				i = i + 2;
			}
			PackageJob job = new PackageJob(i, test, ide_home, path, id, appName, fullScreen);
			job.setUser(true);
			job.schedule();
			job.addJobChangeListener(new JobChangeAdapter() {
				public void done(IJobChangeEvent event) {
					if (event.getResult().isOK())
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								MessageDialog.openInformation(null,
									Messages.PackageAppItemDialog_SUCESS,
										Messages.PackageAppItemDialog_PACKAGE_SUCESS);
								openPackageFolder(test + File.separator + "package");
							}
						});
					else
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								MessageDialog.openError(null,
										Messages.PackageAppItemDialog_EXCEPTION,
										Messages.PackageAppItemDialog_PACKAGE_FAIL);
							}
						});
				}
			});

			close();
			close();
		} else if (buttonId == 1) {
			close();
		}
	}
	private void openPackageFolder(String input) {
		try {
			if(OS.isWindows()) {
				String[] cmd = new String[5];
				cmd[0] = "cmd";
				cmd[1] = "/c";
				cmd[2] = "start";
				cmd[3] = " ";
				cmd[4] = input;
				
				Runtime.getRuntime().exec(cmd);
			}else {
				Runtime.getRuntime().exec("open " + input);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public boolean close() {
		dispose();
		return super.close();
	}
	
	private void dispose() {
		ResourceManager.disposeImages();
	}
	
}
