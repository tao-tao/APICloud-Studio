/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.makepackage.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.apicloud.makepackage.core.messages"; //$NON-NLS-1$
	public static String PackageAppItemDialog_CANCEL;
	public static String PackageAppItemDialog_EXCEPTION;
	public static String PackageAppItemDialog_INFO;
	public static String PackageAppItemDialog_MESSAGE_FIVE;
	public static String PackageAppItemDialog_MESSAGE_FOUR;
	public static String PackageAppItemDialog_MESSAGE_ONE;
	public static String PackageAppItemDialog_MESSAGE_THREE;
	public static String PackageAppItemDialog_MESSAGE_TWO;
	public static String PackageAppItemDialog_MUST_BE_A_ICON;
	public static String PackageAppItemDialog_PACKAGE;
	public static String PackageAppItemDialog_PACKAGE_FAIL;
	public static String PackageAppItemDialog_PACKAGE_LOCATION;
	public static String PackageAppItemDialog_PACKAGE_SUCESS;
	public static String PackageAppItemDialog_PACKAGE_TITLE;
	public static String PackageAppItemDialog_PLATMFROM;
	public static String PackageAppItemDialog_PROJECT_NAME;
	public static String PackageAppItemDialog_SUCESS;
	
	public static String PackageJob_CREATE_PACKAGE;
	public static String PackageJob_PACKAGING;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
