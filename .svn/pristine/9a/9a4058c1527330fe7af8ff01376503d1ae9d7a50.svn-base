/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.ui.listener;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;

import com.apicloud.commons.model.Config;
import com.apicloud.commons.util.StringUtils;
import com.apicloud.navigator.ui.editors.ConfigEditor;

public abstract class TextModifyListener implements ModifyListener {
	protected Text text;
	protected Config config;
	private ConfigEditor editor;
	private ControlDecoration decoration;
	public TextModifyListener(ConfigEditor editor, Text text, Config config, ControlDecoration decoration) {
		this.text = text;
		this.config = config;
		this.editor = editor;
		this.decoration = decoration;
	}
	
	@Override
	public void modifyText(ModifyEvent e) {
		if(isEmptyOfTextValue()) {
			decoration.show();
		}else{
			decoration.hide();
		}
		setContentAndSave();
	}
	
	private void setContentAndSave() {
		setContent(text.getText());
		editor.setDirty(true);
		editor.change();
	}
	
	abstract protected void setContent(String texValue);
	
	private boolean isEmptyOfTextValue() {
		return text.getText().equals(StringUtils.EMPTY_STRING);
	}

}
