/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.swt.webkitbrowser;

import java.awt.Frame;
import java.util.ArrayList;

import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.OS;
import org.cef.OSHelper;
import org.cef.browser.CefBrowser;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.browser.CloseWindowListener;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Max Stepanov
 */
public class WebKitBrowser extends Composite {
	
	private CefClient m_client;
	public CefBrowser m_browser;
	private String m_url;
	private ArrayList<ProgressListener> m_plList = new ArrayList<ProgressListener>();
	private ArrayList<LocationListener> m_llList = new ArrayList<LocationListener>();
	private DeveloperTools m_developerTools;
	
	/**
	 * @param parent
	 * @param style
	 */
	public WebKitBrowser(Composite parent, int style) {
		super(parent, SWT.EMBEDDED);
		setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		init();
	}
	
	private void init() {
		OSHelper.initJNIPath();
		this.m_client = CefApp.getInstance().createClient();
		m_client.addLoadHandler(new LoadHandler(this));
		m_client.addDisplayHandler(new DisplayHandler(this));
	}

	@Override
	protected void checkSubclass() {
	}

	public void addCloseWindowListener(CloseWindowListener listener) {
	}

	public void addLocationListener(LocationListener listener) {
		m_llList.add(listener);
	}

	public void addOpenWindowListener(OpenWindowListener listener) {
	}

	public void addProgressListener(ProgressListener listener) {
		m_plList.add(listener);
	}

	public void addStatusTextListener(StatusTextListener listener) {
	}

	public void addTitleListener(TitleListener listener) {
	}

	public boolean back() {
		if (m_browser.canGoBack()) {
			m_browser.goBack();
			return true;
		}
		return false;
	}
	
	protected void checkWidget() {
		super.checkWidget();
	}
	
	public boolean isDisposed() {
		return super.isDisposed();
	}

	public boolean execute(String script) {
		return true;
	}

	public Object evaluate(String script) throws SWTException {
		return null;
	}

	public boolean forward() {
		if (m_browser.canGoForward()) {
			m_browser.goForward();
			return true;
		}
		return false;
	}

	public String getText() {
		return "";
	}

	public String getUrl() {
		String url = m_browser.getURL();
		if ((url == null) || (url.isEmpty())) {
			return m_url;
		}
		return url;
	}

	public boolean isBackEnabled() {
		return((m_browser != null) && (m_browser.canGoBack()));
	}

	public boolean isForwardEnabled() {
		return ((m_browser != null) && (m_browser.canGoForward()));
	}

	public void refresh() {
		m_browser.reload();
	}

	public void removeCloseWindowListener(CloseWindowListener listener) {
	}

	public void removeLocationListener(LocationListener listener) {
		m_llList.remove(listener);
	}

	public void removeOpenWindowListener(OpenWindowListener listener) {
	}

	public void removeProgressListener(ProgressListener listener) {
		m_plList.remove(listener);
	}

	public void removeStatusTextListener(StatusTextListener listener) {
	}

	public void removeTitleListener(TitleListener listener) {
	}

	public boolean setText(String html) {
		return true;
	}

	public boolean setUrl(String url) {
		m_url = url;
		if (this.m_browser == null) {
			this.m_browser = this.m_client.createBrowser((this.m_url != null) ? this.m_url : "about:blank", OS.isLinux(), false);
			Frame frame = SWT_AWT.new_Frame(this);
			frame.add(this.m_browser.getUIComponent());
		}
		if (m_browser.isLoading()) {
			m_browser.stopLoad();
		}
		m_browser.loadURL(url);
		return true;
	}

	public void stop() {
		m_browser.stopLoad();
	}
	
	void notifyListeners(final TypedEvent event) {
		if (isDisposed()) {
			return;
		}

		getDisplay().asyncExec(new Runnable() {
			public void run() {
				callListeners(event);
			}
		});
	}
	
	private void callListeners(TypedEvent event) {
		if (event instanceof ProgressEvent) {
			ProgressEvent progressEvent = (ProgressEvent) event;
			if (progressEvent.current == progressEvent.total) {
				for (ProgressListener listener : this.m_plList) {
					progressEvent.display = getDisplay();
					listener.completed(progressEvent);
				}
			}
		}
		
		if (event instanceof LocationEvent) {
			LocationEvent locationEvent = (LocationEvent) event;
			for (LocationListener listener : this.m_llList)
				listener.changed(locationEvent);
		}
	}
	
	public void openDeveloperTools() {
		if ((m_developerTools == null) || (m_developerTools.isDisposed())) {
			m_developerTools = new DeveloperTools(this);
			m_developerTools.open();
			return;
		}
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				m_developerTools.show();
			}
		});
	}
}