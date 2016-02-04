/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.composite;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

public class UZWizardComposite extends Composite {

	private Object browser = null;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public UZWizardComposite(Composite parent, int style, String url) {
		this(parent, style, url, null);
	}
	public UZWizardComposite(Composite parent, int style, String url, String cookie) {
		super(parent, style);
		FillLayout layout = new FillLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		setLayout(layout);
		initialBrowser(url, cookie);
	}

	protected void initialBrowser(String url, String cookie) {
		if(cookie != null) {
			 this.browser = new Browser(this, 0);
		      ((Browser)this.browser).setJavascriptEnabled(true);
		}else {
			 this.browser = new Browser(this, 0);
		      ((Browser)this.browser).setJavascriptEnabled(true);
		      ((Browser)this.browser).setUrl(url);
		}
	}
	
    public void redirectTo(String url) {
        if (this.browser != null)
            ((Browser)this.browser).setUrl(url);
    }
  
    public void registerFunction(String funcName, final IScriptHandler handler) {
    	new org.eclipse.swt.browser.BrowserFunction((Browser)getBrowser(), funcName) {
    	    public Object function(Object[] arguments) {
    	        return handler.handle(arguments);
    	    }
    	};
    }
 
    public Object getBrowser(){
        return this.browser;
    }
	
	@Override
	protected void checkSubclass() {
	}
	
	public static abstract interface IScriptHandler {
	    public abstract Object handle(Object[] paramArrayOfObject);
	}
}
