/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.updatemanager.dialog;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.apicloud.commons.model.IDEUpdateModel;
import com.apicloud.updatemanager.Messages;
import com.apicloud.updatemanager.core.CheckUpateManager;

public class IDEUpdateDialog extends TitleAreaDialog {
	private Composite composite;
	private IDEUpdateModel model;
	private CheckUpateManager manager;
	private Text text;

	
	public IDEUpdateDialog(Shell parentShell, CheckUpateManager manager) {
		super(parentShell);
		this.manager = manager;
		setShellStyle(SWT.CLOSE | SWT.RESIZE);
	}
	
	public IDEUpdateDialog(Shell parentShell, IDEUpdateModel model, CheckUpateManager manager) {
		super(parentShell);
		this.model = model;
		this.manager = manager;
		setShellStyle(SWT.CLOSE | SWT.RESIZE);
	}

	@Override
	public int open() {
		setBlockOnOpen(false);
		int i = super.open();
		setMessage(model.getDate());
		return i;
		
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setText(Messages.UPDATEMESSAGE);
		super.configureShell(newShell);
	}

	@Override
	public boolean isHelpAvailable() {
		return false;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("IDE Version " + model.getVersion());
		
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		
		composite = new Composite(scrolledComposite, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setEnabled(false);
		text = new Text(composite, SWT.NONE | SWT.MULTI);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		text.setFont(new Font(null, new FontData("Console", 14, SWT.NORMAL)));
		text.setText(model.getText() == null ? "" : model.getText());
		GridData gd_composite_1 = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_composite_1.heightHint = 60;
		scrolledComposite.setContent(composite);
		scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		Link link = new Link(container, SWT.NONE);
		link.setText("<a>\u67E5\u770B\u66F4\u591A\u66F4\u65B0\u8BF4\u660E</a>");
		link.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				String url = "http://"+ manager.getIp() +"/ide/component/logV2";
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
		return area;
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(500, 500);
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == 0) {
				manager.IDEUpdate();
			
		} 
		setMessage("1.0.8");
		super.buttonPressed(buttonId);
	}
	
	public IDEUpdateModel getModel() {
		return model;
	}

	public void setModel(IDEUpdateModel model) {
		this.model = model;
	}
}
