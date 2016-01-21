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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.apicloud.resource.Activator;

public class DeviceNotFoundDialog extends Dialog {
	public static final int DNF_KEY_ANDROID = 1;
	public static final int DNF_KEY_IOS = 2;
	public static final int DNF_KEY_CANCEL = 3;
	private Cursor cursor;
	private Shell parentShell;

	public DeviceNotFoundDialog(Shell parentShell) {
		super(parentShell);
		this.parentShell = parentShell;
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.SYNCPROJECT);
		newShell.setSize(480, 310);
		Rectangle parentBounds = parentShell.getBounds();
		Rectangle shellBounds = newShell.getBounds();

		newShell.setLocation(parentBounds.x
				+ (parentBounds.width - shellBounds.width) / 2, parentBounds.y
				+ (parentBounds.height - shellBounds.height) / 2);
	}

	protected void createButtonsForButtonBar(Composite parent) {
		Button btn = createButton(parent, DNF_KEY_CANCEL,
				Messages.DeviceNotFoundDialog_CANCEL, true);
		btn.setFocus();
	}

	protected Control createDialogArea(Composite parent) {
		Display display = PlatformUI.getWorkbench().getDisplay();
		cursor = new Cursor(display, SWT.CURSOR_HAND);
		parent.getShell().setText(Messages.DeviceNotFoundDialog_RUN);
		Composite composite = new Composite(parent, 0);
		composite.setLayoutData(new GridData(1808));
		composite.setLayout(null);

		Label lableAndroid = new Label(composite, SWT.NONE);
		lableAndroid.setCursor(cursor);
		lableAndroid.setBounds(10, 90, 207, 101);
		lableAndroid.setImage(Activator.getImage("icons/label_android.png"));
		lableAndroid.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				openHelperPage(Messages.DeviceNotFoundDialog_ADB);
				close();
			}
		});

		Label labelIos = new Label(composite, SWT.NONE);
		labelIos.setCursor(cursor);
		labelIos.setBounds(251, 90, 208, 101);
		labelIos.setImage(Activator.getImage("icons/label_ios.png"));
		labelIos.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				openHelperPage(Messages.DeviceNotFoundDialog_ITUNES);
				close();
			}
		});

		Label labelWarn = new Label(composite, SWT.NONE);
		labelWarn.setBounds(132, 25, 207, 25);
		labelWarn.setImage(Activator.getImage("icons/label_warn.png"));

		Label labelConfirm = new Label(composite, SWT.NONE);
		labelConfirm.setBounds(10, 67, 158, 17);
		labelConfirm.setText(Messages.DeviceNotFoundDialog_CONFIRM);

		Label labelMore = new Label(composite, SWT.NONE);
		labelMore.setBounds(10, 204, 189, 17);
		labelMore.setText(Messages.DeviceNotFoundDialog_FIND_MORE);

		Link link = new Link(composite, SWT.NONE);
		link.setBounds(205, 204, 110, 17);
		link.setText(Messages.DeviceNotFoundDialog_HELP);
		link.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				String url = "http://community.apicloud.com/bbs/forum.php?mod=viewthread&tid=1125&page=1&extra=#pid5171";
				openHelperPage(url);
			}

		});
		return composite;
	}
	
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case DNF_KEY_CANCEL:
			cancelPressed();
			break;
		case DNF_KEY_ANDROID:
			openHelperPage(Messages.DeviceNotFoundDialog_ADB);
			close();
			break;
		case DNF_KEY_IOS:
			openHelperPage(Messages.DeviceNotFoundDialog_ITUNES);
			close();
			break;
		default:
			break;
		}
	}

	private void openHelperPage(String page) {
		try {
			URI pageURL = new URI(page);
			Desktop.getDesktop().browse(pageURL);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean close() {
		if (cursor != null) {
			cursor.dispose();
		}
		return super.close();
	}

}
