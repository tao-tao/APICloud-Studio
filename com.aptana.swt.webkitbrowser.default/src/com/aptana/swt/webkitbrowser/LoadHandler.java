/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.swt.webkitbrowser;

import org.cef.browser.CefBrowser;
import org.cef.handler.CefLoadHandler;
import org.eclipse.swt.browser.ProgressEvent;

final class LoadHandler implements CefLoadHandler  
{
	private final WebKitBrowser browser;
	private boolean m_isLoading;
	private boolean m_ready;

	public LoadHandler(WebKitBrowser browser) {
		this.browser = browser;
	}
	
	public void onLoadingStateChange(CefBrowser browser, boolean isLoading,
			boolean canGoBack, boolean canGoForward) {
		this.m_isLoading = isLoading;
	}

	public void onLoadStart(CefBrowser cefBrowser, int frameIdentifer) {
		if (!this.m_isLoading) {
			return;
		}
		if (this.m_ready) {
			this.m_ready = false;
			ProgressEvent event = new ProgressEvent(browser);
			event.current = 0;
			event.total = 100;
			browser.notifyListeners(event);
		}
	}

	public void onLoadEnd(CefBrowser cefBrowser, int frameIdentifier,
			int httpStatusCode) {
		if (!this.m_ready) {
			ProgressEvent event = new ProgressEvent(browser);
			if (!(m_isLoading)) {
				event.current = 100;
				event.total = 100;
				m_ready = true;
				browser.notifyListeners(event);
			}
		}
	}

	public void onLoadError(CefBrowser browser, int frameIdentifer,
			CefLoadHandler.ErrorCode errorCode, String errorText,
			String failedUrl) {
		browser.stopLoad();
	}
}