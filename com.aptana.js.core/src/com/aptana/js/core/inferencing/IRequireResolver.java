/**
 * Aptana Studio
 * Copyright (c) 2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.js.core.inferencing;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

public interface IRequireResolver
{

	/**
	 * Resolves a module id to the path of the file containing the module.
	 * 
	 * @param moduleId
	 * @return
	 */
	public IPath resolve(String moduleId, IProject project, IPath currentDirectory, IPath indexRoot);

	/**
	 * Does thie resolver apply under the current conditions?
	 * 
	 * @param project
	 * @param currentDirectory
	 * @param indexRoot
	 * @return
	 */
	public boolean applies(IProject project, IPath currentDirectory, IPath indexRoot);
}
