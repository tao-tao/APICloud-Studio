/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.html;

/**
 * A interface to capture the various scopes available during debugging. These need to match the items in the .options
 * file at the root of the plugin
 * 
 * @author Ingo Muschenetz
 */
public interface IDebugScopes
{
	/**
	 * Items related to the content assist process
	 */
	String CONTENT_ASSIST = HTMLPlugin.PLUGIN_ID + "/debug/content_assist"; //$NON-NLS-1$

	/**
	 * Items related to the outline
	 */
	String OUTLINE = HTMLPlugin.PLUGIN_ID + "/debug/outline"; //$NON-NLS-1$
}
