/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.ui.builder;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.apicloud.commons.model.Config;
import com.apicloud.commons.util.StringUtils;
import com.apicloud.navigator.dialogs.Messages;
import com.apicloud.navigator.ui.editors.ConfigEditor;
import com.apicloud.navigator.ui.listener.AuthorEmailTextModifyListener;
import com.apicloud.navigator.ui.listener.AuthorHrefTextModifyListener;
import com.apicloud.navigator.ui.listener.AuthorNameTextModifyListener;
import com.apicloud.navigator.ui.listener.ContentSrcTextModifyListener;
import com.apicloud.navigator.ui.listener.NameTextModifyListener;

public class GeneralSectionBuilder implements ISectionBuilder<Config> {
	private final FormToolkit formToolkit;
	private final Composite composite;
	private Text appNameText;
	private Text authorNameText;
	private Text authorEmailText;
	private Text authorHrefText;
	private Text ContentSrcText;
	private Text descriptionText;
	private ConfigEditor editor;
	private ControlDecoration appNameDecoration;
	private ControlDecoration authorNameDecoration;
	private ControlDecoration authorEmailDecoration;
	private ControlDecoration authorHrefDecoration;
	private ControlDecoration ContentSrcDecoration;

	public GeneralSectionBuilder(FormToolkit formToolkit, Composite composite, ConfigEditor editor) {
		this.formToolkit = formToolkit;
		this.composite = composite;
		this.editor = editor;
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void buildUI() {
		Section generalSection = formToolkit.createSection(composite,
				Section.TWISTIE | Section.TITLE_BAR);
		GridData gd_generalSection = new GridData(SWT.FILL, SWT.FILL, true,
				false, 1, 1);
		gd_generalSection.heightHint = 240;
		generalSection.setLayoutData(gd_generalSection);
		formToolkit.paintBordersFor(generalSection);
		generalSection.setText(Messages.BASICATTRIBUTE);
		generalSection.setExpanded(true);

		Composite generalComposite = new Composite(generalSection, SWT.NONE);
		formToolkit.adapt(generalComposite);
		formToolkit.paintBordersFor(generalComposite);
		generalSection.setClient(generalComposite);
		generalComposite.setLayout(new GridLayout(4, false));
		FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault()  
                .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);  
        String decorationDescription = Messages.NULLERROR;  
        Image decorationImage = fieldDecoration.getImage(); 

		Label appNameLabel = formToolkit.createLabel(generalComposite,	Messages.APPNAME, SWT.NONE);
		appNameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		appNameText = formToolkit.createText(generalComposite, StringUtils.EMPTY_STRING, SWT.NONE);
		appNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,	false, 1, 1));
		appNameDecoration = createTextErrorDecoration(appNameText, decorationDescription, decorationImage);  
		
		Label lblNewLabel_1 = formToolkit.createLabel(generalComposite,	Messages.AUTHERNAME, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		authorNameText = formToolkit.createText(generalComposite, StringUtils.EMPTY_STRING, SWT.NONE);
		authorNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		authorNameDecoration = createTextErrorDecoration(authorNameText, decorationDescription, decorationImage);  

		Label lblNewLabel_2 = formToolkit.createLabel(generalComposite,Messages.AUTHEREMAIL, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		authorEmailText = formToolkit.createText(generalComposite, StringUtils.EMPTY_STRING, SWT.NONE);
		authorEmailText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		authorEmailDecoration = createTextErrorDecoration(authorEmailText, decorationDescription, decorationImage);  

		Label lblNewLabel_3 = formToolkit.createLabel(generalComposite,Messages.AUTHORIZEDCONNECTION, SWT.NONE);
		lblNewLabel_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		authorHrefText = formToolkit.createText(generalComposite, StringUtils.EMPTY_STRING, SWT.NONE);
		authorHrefText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, 	false, 1, 1));
		authorHrefDecoration = createTextErrorDecoration(authorHrefText, decorationDescription, decorationImage);

		Label lblNewLabel_4 = formToolkit.createLabel(generalComposite,Messages.STARTPAGE, SWT.NONE);
		lblNewLabel_4.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
		ContentSrcText = formToolkit.createText(generalComposite, StringUtils.EMPTY_STRING, SWT.NONE);
		ContentSrcText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		ContentSrcDecoration = createTextErrorDecoration(ContentSrcText, decorationDescription, decorationImage);

		Label lblNewLabel_6 = new Label(generalComposite, SWT.NONE);
		lblNewLabel_6.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		formToolkit.adapt(lblNewLabel_6, true, true);
		lblNewLabel_6.setText("\u63CF\u8FF0");
		descriptionText = new Text(generalComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		GridData gd_descriptionText = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_descriptionText.heightHint = 49;
		descriptionText.setLayoutData(gd_descriptionText);
		formToolkit.adapt(descriptionText, true, true);
	}

	private ControlDecoration createTextErrorDecoration(Text text, String decorationDescription, Image decorationImage) {
		ControlDecoration decoration = new ControlDecoration(text, SWT.RIGHT);
		decoration.setImage(decorationImage);  
		decoration.setDescriptionText(decorationDescription);  
		decoration.hide();
		return decoration;
	}

	@Override
	public void bindData(final Config config) {
		bindValue(config);
		bindListener(config);
	}

	private void bindValue(final Config config) {
		appNameText.setText(config.getName());
		authorNameText.setText(config.getAuthorName());
		authorEmailText.setText(config.getAuthorEmail());
		authorHrefText.setText(config.getAuthorHref());
		ContentSrcText.setText(config.getContentSrc());
		descriptionText.setText(config.getDesc());
	}
	
	private void bindListener(final Config config) {
		appNameText.addModifyListener(new NameTextModifyListener(editor, appNameText, config, appNameDecoration));
		authorNameText.addModifyListener(new AuthorNameTextModifyListener(editor, authorNameText, config, authorNameDecoration));
		authorEmailText.addModifyListener(new AuthorEmailTextModifyListener(editor, authorEmailText, config, authorEmailDecoration));
		authorHrefText.addModifyListener(new AuthorHrefTextModifyListener(editor, authorHrefText, config, authorHrefDecoration));
		ContentSrcText.addModifyListener(new ContentSrcTextModifyListener(editor, ContentSrcText, config, ContentSrcDecoration));
		addDescriptionModifyListener(config);
	}

	private void addDescriptionModifyListener(final Config config) {
		descriptionText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				config.setDesc(descriptionText.getText());
				editor.setDirty(true);
				editor.change();
			}
		});
	}
	
	public boolean hasErrorText() {
		return appNameDecoration.isVisible();
	}

	public void setValue(Config config) {
		appNameText.setText(config.getName());
		authorNameText.setText(config.getAuthorName());
		authorEmailText.setText(config.getAuthorEmail());
		authorHrefText.setText(config.getAuthorHref());
		ContentSrcText.setText(config.getContentSrc());
		descriptionText.setText(config.getDesc());
	}
	
}
