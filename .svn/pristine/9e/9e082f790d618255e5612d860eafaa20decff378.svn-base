/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.composite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import com.apicloud.resource.Activator;
import com.apicloud.loader.api.IProgressCallBack;
import com.apicloud.loader.platforms.android.ADBService;
import com.apicloud.loader.platforms.android.AndroidDevice;
import com.apicloud.loader.platforms.ios.ANBProvider;
import com.apicloud.loader.platforms.ios.IOSDevice;
import com.apicloud.navigator.dialogs.Messages;
import com.apicloud.commons.model.Config;
import com.apicloud.commons.util.FileUtil;
import com.apicloud.commons.util.IDEUtil;

public class SyncApplicationComposite extends Composite implements IProgressCallBack{
	private AndroidDevice aDev;
	private IOSDevice iDev;
	private Object select;
	private String startInfo;
	private String path;
	private Label message;
	private Label state;
	private ProgressBar progressBar;
	private boolean isFinished = true;

	public boolean isFinished() {
		return isFinished;
	}

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public SyncApplicationComposite(Composite parent, int style) {
		this(parent, style, null, null, "");
	}

	public SyncApplicationComposite(Composite parent, int style, AndroidDevice aDev, IOSDevice iDev, Object select) {
		super(parent, style);
		this.aDev = aDev;
		this.iDev = iDev;
		this.select = select;
		setLayout(new GridLayout(3, false));
		Label lblNewLabel = new Label(this, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				true, 1, 1));

		Composite composite_3 = new Composite(this, SWT.NONE);
		composite_3.setLayout(new GridLayout(2, false));
		composite_3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
				1, 1));

		Label lblNewLabel_2 = new Label(composite_3, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 1, 1));
		assert iDev != null || aDev != null;
		if (iDev != null) {
			lblNewLabel.setImage(Activator.getImage("icons/iphone.png"));
			lblNewLabel_2.setText(iDev.getName());
		}else if (aDev != null){
			lblNewLabel.setImage(Activator.getImage("icons/and.png"));
			lblNewLabel_2.setText(aDev.getUuid());
		}

		message = new Label(composite_3, SWT.NONE);
		message.setAlignment(SWT.CENTER);
		message.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1,
				1));
		message.setText(Messages.WAIT);

		progressBar = new ProgressBar(composite_3, SWT.NONE);
		GridData gd_progressBar = new GridData(SWT.FILL, SWT.CENTER, true,
				false, 2, 1);
		gd_progressBar.heightHint = 8;
		progressBar.setLayoutData(gd_progressBar);
		state = new Label(this, SWT.NONE);
		state.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false,
				1, 1));
		state.setText(Messages.WAIT);
	}

	public void run(CountDownLatch threadSignal) throws Exception {
		try {
			IResource resource = (IResource) select;
			path = resource.getLocation().toOSString();
			File fileToRead = new File(path + "/config.xml");
			Config config = Config.loadXml(new FileInputStream(fileToRead));
			ADBService aService=ADBService.getADBService();
			ANBProvider iService=ANBProvider.getANBProvider();
			String appId=config.getId();
			startInfo = createStartInfodFile(appId);
			if (this.aDev != null) {
				Display.getDefault().syncExec(
						new Runnable() {
							public void run() {
								message.setText(Messages.PREPARATIONRESOURCES); 
								state.setText(Messages.SYNCHRONIZATION);
								progressBar.setSelection(30);
								message.setText(Messages.RESOURCESCOPY);  
							}
						});
				aService.load(aDev.getUuid(), startInfo, appId, path,this);
				Display.getDefault().syncExec(
						new Runnable() {
							public void run() {
								progressBar.setSelection(100);
								message.setText(Messages.AUTOOPENAPP);  
								state.setText(Messages.FINISH);
							}
						});
			
			}

			if (this.iDev != null) {
				iService.connect(this.iDev);
				iService.load(this.iDev, startInfo, appId, path,this);
			}
		}
		catch (ClassCastException e) {
			Display.getDefault().syncExec(
					new Runnable() {
						public void run() {
							state.setText(Messages.FAILE);
							progressBar.setSelection(0);
						}
					});
			e.printStackTrace();
			isFinished = false;
			throw new Exception();
		} 
		catch (final Exception e) {
			Display.getDefault().syncExec(
					new Runnable() {
						public void run() {
							state.setText(Messages.FAILE);
							message.setText(e.getMessage());
							progressBar.setSelection(0);
						}
					});
			e.printStackTrace();
			isFinished = false;
			throw new Exception();
		} finally {
			FileUtil.deleteDirectory(startInfo);
			FileUtil.deleteDirectory(IDEUtil.getInstallPath() + "test/");
			threadSignal.countDown();
		}
		isFinished = true;
	}

	@Override
	protected void checkSubclass() {
	}

	private String createStartInfodFile(String id) {
		String path = IDEUtil.getInstallPath() + "temp";
		java.io.File tempFolder = new java.io.File(path);
		tempFolder.mkdirs();
		java.io.File info = new java.io.File(path + "/startInfo.txt");
		if (info.exists()) {
			info.delete();
		}
		try {
			info.createNewFile();
			FileOutputStream fos = new FileOutputStream(info);
			fos.write(id.getBytes());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return path;
	}
	@Override
	public void dispose() {
		progressBar.dispose();
		super.dispose();
	}

	@Override
	public void done(final int percent,final String msg,final String status) {
		Display.getDefault().syncExec(
			new Runnable() {
				public void run() {
					progressBar.setSelection(percent);
					if(msg!=null)
						message.setText(msg); 
					if(status!=null)
						state.setText(status);
				}
			});
	}
}
