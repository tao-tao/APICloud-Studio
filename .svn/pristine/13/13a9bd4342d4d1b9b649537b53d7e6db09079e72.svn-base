/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.swt.webkitbrowser;

import java.awt.Frame;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class DeveloperTools {
	private Shell m_shell;
	private WebKitBrowser m_browser;

	public DeveloperTools(WebKitBrowser browser) {
		this.m_browser = browser;
	}

	public void open() {
		Display display = m_browser.getDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				if ((m_shell != null) && (!(m_shell.isDisposed()))) {
					m_shell.forceFocus();
				} else {
					m_shell = new Shell(m_browser.getShell(), SWT.SHELL_TRIM); 
					m_shell.setLayout(new FillLayout());
					m_shell.setSize(800, 600);
					m_browser.addDisposeListener(new DisposeListener() {
						public void widgetDisposed(DisposeEvent e) {
							m_shell.dispose();
						}
					});
					m_shell.addDisposeListener(new DisposeListener() {
						public void widgetDisposed(DisposeEvent e) {
							m_browser.m_browser.getDevTools().close();
						}
					});
					Frame frame = SWT_AWT.new_Frame(new Composite(m_shell, SWT.CENTER));
					frame.add(m_browser.m_browser.getDevTools().getUIComponent());
					m_shell.open();
				}
			}
		});
	}
	
	public void show() {
		if (m_shell != null) {
			m_shell.setVisible(true);
			m_shell.setMinimized(false);
			m_shell.setFocus();
		}
	}
	
	public boolean isDisposed() {
		return ((m_shell == null) || (m_shell.isDisposed()));
	}
}
