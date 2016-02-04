/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.dialogs;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import com.apicloud.loader.platforms.android.AndroidDevice;
import com.apicloud.loader.platforms.ios.IOSDevice;
import com.apicloud.navigator.composite.SyncApplicationComposite;
import com.apicloud.resource.Activator;

public class SyncApplicationDialog extends Dialog {
	public static boolean isOpen = false;
	private Composite composite;
	private List<AndroidDevice> aMobiles;
	private List<IOSDevice> iMobiles;
	private Object select;
	private Button okButton;
	private Label inforLabel;
	private Cursor cursor;
	private Shell parentShell;
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public SyncApplicationDialog(Shell parentShell, List<AndroidDevice> amobiles, List<IOSDevice> imobiles,Object select) {
		super(parentShell);
		this.aMobiles = amobiles;
		this.iMobiles = imobiles;
		
		this.select = select;
		setShellStyle(SWT.CLOSE );
		this.parentShell=parentShell;
		
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.SYNCPROJECT);
		newShell.setSize(506, 500);
		if(parentShell!=null){
			
			Rectangle parentBounds = parentShell.getBounds();
			Rectangle shellBounds = newShell.getBounds();
			newShell.setLocation(parentBounds.x
					+ (parentBounds.width - shellBounds.width) / 2, parentBounds.y
					+ (parentBounds.height - shellBounds.height) / 2);
		}
	}
	
	@Override
	public int open() {
		setBlockOnOpen(false);
		int i = super.open();
		return i;
		
	}

	
	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Display display = PlatformUI.getWorkbench().getDisplay();
		cursor = new Cursor(display, SWT.CURSOR_HAND);
		Composite area = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) area.getLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		Composite container = new Composite(area, SWT.NONE);
		FillLayout fl_container = new FillLayout(SWT.HORIZONTAL);
		container.setLayout(fl_container);
		GridData gd_container = new GridData(GridData.FILL_BOTH);
		container.setLayoutData(gd_container);
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(container, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		
		composite = new Composite(scrolledComposite, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridData gd_label = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_label.heightHint = 66;
		inforLabel = new Label(composite, SWT.NONE);
		inforLabel .setCursor(cursor);
		inforLabel.setLayoutData(gd_label);
		inforLabel.setImage(Activator.getImage("icons/tishi.png")); //$NON-NLS-1$
		inforLabel.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				handleButtonCancelWidgetSelected();
			}

			private void handleButtonCancelWidgetSelected() {
				String url = "http://community.apicloud.com/bbs/forum.php?mod=viewthread&tid=1125&page=1&extra=#pid5171";
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
		
		inforLabel.addMouseTrackListener(new MouseTrackListener() {
			@Override
			public void mouseHover(MouseEvent e) {
				inforLabel.setImage(Activator
						.getImage("icons/tishi1.png"));
			}

			@Override
			public void mouseExit(MouseEvent e) {
				inforLabel.setImage(Activator
						.getImage("icons/tishi.png"));
			}

			@Override
			public void mouseEnter(MouseEvent e) {
				inforLabel.setImage(Activator
						.getImage("icons/tishi1.png"));
			}
		});
		
		GridData gd_composite_1 = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_composite_1.heightHint = 60;
		for(AndroidDevice m : aMobiles) {
			Composite c = new SyncApplicationComposite(composite, SWT.NONE, m, null, select);
			c.setLayoutData(gd_composite_1);
		}
		for(IOSDevice m:iMobiles){
			Composite c = new SyncApplicationComposite(composite, SWT.NONE, null, m, select);
			c.setLayoutData(gd_composite_1);
		}
		
		scrolledComposite.setContent(composite);
		scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		return area;
	}

	public void run(final CountDownLatch threadSignal) {
		for(Control c :composite.getChildren()) {
			if(c instanceof SyncApplicationComposite) {
				final SyncApplicationComposite sac = (SyncApplicationComposite)c;
				Job job = new WorkspaceJob("")
				{
					@Override
					public IStatus runInWorkspace(IProgressMonitor monitor)
							throws CoreException {
						try {
							sac.run(threadSignal);
						} catch (Exception e) {
							return Status.CANCEL_STATUS;
						}
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}
		}
		
	}

	public void finish() {
		Display.getDefault().syncExec(
				new Runnable() {
					public void run() {
						okButton.setEnabled(true);
						for(Control c :composite.getChildren()) {
							if(c instanceof SyncApplicationComposite) {
								SyncApplicationComposite sac = (SyncApplicationComposite)c;
								if(!sac.isFinished()) {
									return;
								}
							}
						}
						close();
					}
				});
	}
	
	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, Messages.PackageAppItemDialog_SUCESS,
				true);
		okButton.setEnabled(false);
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == 0 && isOpen) {
			isOpen = false;
			openAppLoaderFolder();
		} 
		super.buttonPressed(buttonId);
	}

	private void openAppLoaderFolder() {
		try {
			String[] cmd = new String[5];
			cmd[0] = "cmd";
			cmd[1] = "/c";
			cmd[2] = "start";
			cmd[3] = " ";
			cmd[4] = com.apicloud.loader.platforms.ios.ANBActivator.getAppLoaderPath();

			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public boolean close() {
		 if(cursor != null) {
			   cursor.dispose();
		   }
		return super.close();
	}
	   

}
