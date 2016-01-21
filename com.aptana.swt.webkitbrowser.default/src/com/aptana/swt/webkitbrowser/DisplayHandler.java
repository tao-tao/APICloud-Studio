package com.aptana.swt.webkitbrowser;

import org.cef.browser.CefBrowser;
import org.cef.handler.CefDisplayHandler;
import org.eclipse.swt.browser.LocationEvent;


final class DisplayHandler implements CefDisplayHandler {
	private final WebKitBrowser webkitbrowser;

	public DisplayHandler(WebKitBrowser browser) {
		webkitbrowser = browser;
	}

	public void onAddressChange(CefBrowser browser, String url) {
		LocationEvent locevent = new LocationEvent(webkitbrowser);
		
		locevent.location = url;

		webkitbrowser.notifyListeners(locevent);
	}

	@Override
	public void onTitleChange(CefBrowser browser, String title) {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public boolean onTooltip(CefBrowser browser, String text) {
		// TODO 自动生成的方法存根
		return false;
	}

	@Override
	public void onStatusMessage(CefBrowser browser, String value) {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public boolean onConsoleMessage(CefBrowser browser, String message,
			String source, int line) {
		// TODO 自动生成的方法存根
		return false;
	}
}
