/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.ui.listener;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.widgets.Text;

import com.apicloud.commons.model.Config;
import com.apicloud.navigator.ui.editors.ConfigEditor;

public class ContentSrcTextModifyListener extends TextModifyListener {

	public ContentSrcTextModifyListener(ConfigEditor editor, Text text,
			Config config, ControlDecoration decoration) {
		super(editor, text, config, decoration);
	}

	@Override
	protected void setContent(String contentSrc) {
		super.config.setContentSrc(contentSrc);
	}
}
