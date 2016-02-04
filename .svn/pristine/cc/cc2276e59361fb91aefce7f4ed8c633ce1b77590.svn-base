/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.ui;

import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
import org.json.JSONException;
import org.json.JSONObject;

import com.apicloud.authentication.AuthenticActivator;
import com.apicloud.navigator.dialogs.ResultMessage;


public class SignInButton extends WorkbenchWindowControlContribution {

	private String userName;
	private String password;
	private String cookie;
	private String ip;
	private ResultMessage shell;
	private String imagePath;
	public SignInButton() {
		initData();
	}

	public SignInButton(String id) {
		super(id);
		initData();
	}

	@Override
	protected Control createControl(Composite parent) {
		Composite main = new Composite(parent, 0);
		
	     parent.setLayout(GridLayoutFactory.fillDefaults().create());
	    main.setLayoutData(GridDataFactory.swtDefaults().grab(false, false).create());
	
	    main.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).spacing(0, 0).create());
	    ToolBar toolbar = new ToolBar(main, 0);
	    toolbar.setLayoutData(GridDataFactory.swtDefaults().create());
	     ToolItem homeItem = new ToolItem(toolbar, SWT.PUSH);
	    	 homeItem.setImage(AuthenticActivator.getImage("icons/signin.png"));
	   
	    homeItem.addSelectionListener(new SelectionAdapter()
	     {

	public void widgetSelected(SelectionEvent e) {
		int x = 214;
 		try {
 			String message=com.apicloud.navigator.Activator.network_instance.sigInMsg(userName, password, cookie,ip);
			 JSONObject json;
				json = new JSONObject(message);
				String code = json.getString("code");
				if(code.equals("1")) {
					imagePath = "icons/success.png";
				} else if(code.equals("2")) {
					imagePath = "icons/signedIn.png";
				}else {
					x = 244;
					imagePath = "icons/fail.png";
				}
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
    	 
			Display display = Display.getDefault();
			Rectangle ret = display.getActiveShell().getBounds();
			e.getSource();
			if(shell == null) {
				 shell = new ResultMessage(display, imagePath);
				 shell.setBounds((ret.width - x) / 2, (ret.height - 62) / 2, x,
							62);
					shell.open();
					shell.layout();
					Timer timer = new Timer();
					timer.schedule(new TimerTask() {
						
						@Override
						public void run() {
							Display.getDefault().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									shell.dispose();
									shell =null;
								}
							});
						
						}
					}, 2000);
			}
     }
	});
	    
	    return main;
	}

	private void initData() {
		Properties p = AuthenticActivator.getProperties();
		userName = p.getProperty("username");
		password = p.getProperty("password");
		cookie = p.getProperty("cookie");
		setIp(p.getProperty("ip"));
		
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
}
