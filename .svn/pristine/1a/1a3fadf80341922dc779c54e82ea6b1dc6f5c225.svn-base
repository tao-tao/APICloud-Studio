/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.updatemanager.dialog;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.ide.IDE;

import com.apicloud.updatemanager.Activator;
import com.apicloud.updatemanager.Messages;
import com.apicloud.updatemanager.core.CustomLoaderAction;
import com.apicloud.updatemanager.core.DownLoadJob;

public class CheckLoaderDialog extends Dialog {
	private final FormToolkit formToolkit = new FormToolkit(Display.getDefault());
	private Cursor cursor;
	public static IEditorInput input;
	private final int LEFT_LABEL = 0;
	private Label leftLabel, rightLabel;
	private int id;
	private static final String aboutText =Messages.LOADMISSINFO;
	private IStructuredSelection select;
	private Object obj;

	public CheckLoaderDialog(Shell parentShell, int iloader,
			IStructuredSelection select) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX);
		this.select = select;
	}
	public CheckLoaderDialog(Shell parentShell, int iloader,
			Object obj) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX);
		this.obj = obj;
	}
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.LOADEMISSERROR);
		newShell.setImage(com.apicloud.resource.Activator.getImage("icons/r1.png"));
	}

	private Color getBackgroundColor() {
		return Display.getCurrent().getActiveShell().getBackground();
	}

	@Override
	protected int getShellStyle() {
		return SWT.MIN | SWT.CLOSE;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Color color = getBackgroundColor();

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBackground(color);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Display display = PlatformUI.getWorkbench().getDisplay();
		cursor = new Cursor(display, SWT.CURSOR_HAND);

		Label warnLabel = formToolkit.createLabel(composite, "");
		warnLabel.setBounds(20, 35, 70, 70);

		warnLabel.setImage(com.apicloud.resource.Activator.getImage("icons/5.png"));
		warnLabel.setBackground(color);

		Label topLabel = formToolkit
				.createLabel(composite, aboutText, SWT.WRAP);
		topLabel.setBounds(100, 50, 350, 60);
		topLabel.setFont(new Font(display, "妤蜂綋", 12, SWT.NORMAL));// 璁剧疆鏂囧瓧鐨勫瓧浣撳瓧鍙�
		topLabel.setBackground(color);

		leftLabel = formToolkit.createLabel(composite,
				"\u4e0b\u8f7d\u5b98\u65b9" + "Loader", SWT.PUSH);

		leftLabel.setCursor(cursor);
		leftLabel.setBounds(140, 160, 102, 21);
		leftLabel.setAlignment(SWT.CENTER);
		leftLabel.setBackgroundImage(com.apicloud.resource.Activator.getImage("icons/2.png"));

		leftLabel.addMouseListener(new MouseAdapter() {
			/**
			 * user to download baseloader
			 */
			@Override
			public void mouseUp(MouseEvent e) {

				downloadBaseloader(Display.getDefault().getActiveShell(),
						getId());
				close();

			}
		});

		rightLabel = formToolkit.createLabel(composite, "\u81ea\u5b9a\u4e49"
				+ "Loader");

		rightLabel.setCursor(cursor);
		rightLabel.setBounds(260, 160, 102, 21);
		rightLabel.setAlignment(SWT.CENTER);
		rightLabel.setBackgroundImage(com.apicloud.resource.Activator.getImage("icons/1.png"));
		rightLabel.addMouseListener(new MouseAdapter() {
			/**
			 * user turn to custormerloader
			 */
			@Override
			public void mouseUp(MouseEvent e) {
				CustomLoaderAction action = null;
				if(select!=null){
					action=new CustomLoaderAction(select);
					
				}else if(obj!=null){
					action=new CustomLoaderAction(obj);
				}
				action.run(null);
				close();
			}
		});
		return composite;
	}

	public void changeImage(int labelId) {
		if (labelId == LEFT_LABEL) {

			leftLabel.setBackgroundImage(com.apicloud.resource.Activator.getImage("icons/1.png"));
			rightLabel.setBackgroundImage(com.apicloud.resource.Activator.getImage("icons/2.png"));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			leftLabel.setImage(com.apicloud.resource.Activator.getImage("icons/2.png"));
			rightLabel.setImage(com.apicloud.resource.Activator.getImage("icons/1.png"));
		}
	}

	public IEditorInput getInput() {
		if (input == null) {
			input = new IEditorInput() {
				public Object getAdapter(
						@SuppressWarnings("rawtypes") Class adapter) {
					return null;
				}

				public String getToolTipText() {
					return "custom app loader";
				}

				public IPersistableElement getPersistable() {
					return null;
				}

				public String getName() {
					return "apploader";
				}

				public ImageDescriptor getImageDescriptor() {
					return null;
				}

				public boolean exists() {
					return true;
				}
			};
		}
		return input;
	}

	/**
	 * custom button name
	 */
	protected void createButtonsForButtonBar(Composite parent) {
	}

	protected void buttonPressed(int buttonId) {
	}

	public void downloadBaseloader(final Shell shell, int id) {
		try {
			DownLoadJob downloadjob = new DownLoadJob(Messages.DOWNLOADOFFICIALLOADER, id);
			downloadjob.setUser(true);
			downloadjob.schedule();

			downloadjob.addJobChangeListener(new JobChangeAdapter() {
				public void done(IJobChangeEvent event) {
					if (event.getResult().isOK())
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								MessageDialog.openInformation(null, Messages.DOWNLOADSUCCESS,
										Messages.LOADERDOWNLOADSUCC);
							}
						});
					else
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								MessageDialog.openError(null, Messages.DOWNLOADEXCEPTION, Messages.DOWNLOADERROR);
							}
						});
					close();
				}
			});

			ResourcesPlugin.getWorkspace().build(
					IncrementalProjectBuilder.CLEAN_BUILD, null);
			ResourcesPlugin.getWorkspace().getRoot()
					.refreshLocal(IResource.DEPTH_INFINITE, null);
		com.apicloud.loader.platforms.android.ADBActivator.setHasBaseLoader(true);
		com.apicloud.loader.platforms.android.ADBActivator.setHasAppLoader(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void openEditor() {
		IWorkbenchPage workbenchPage = Activator.getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		try {
			IDE.openEditor(workbenchPage, getInput(),
					"com.apicloud.customapploader");
		} catch (PartInitException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	protected void cancelPressed() {
		super.cancelPressed();
		close();
	}

	@Override
	public boolean close() {
		if (cursor != null) {
			cursor.dispose();
		}
		return super.close();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
