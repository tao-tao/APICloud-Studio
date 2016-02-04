/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.js.internal.core.parsing.sdoc.model;

import java.util.List;

public class TypeTag extends TagWithTypes
{
	/**
	 * TypeTag
	 * 
	 * @param types
	 * @param text
	 */
	public TypeTag(List<Type> types, String text)
	{
		super(TagType.TYPE, types, text);
	}
}
