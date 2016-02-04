/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.apicloud.commons.model.Config;
import com.apicloud.commons.model.Feature;
import com.apicloud.navigator.ui.editors.ConfigEditor;


public class CreateFeatureDialog extends TitleAreaDialog {
	private Config config;
	private Feature feature;
	private TreeViewer treeViewer;
	private Text featureNameText;
	private ConfigEditor editor;
	private Shell parentShell;
	public CreateFeatureDialog(Shell parentShell, ConfigEditor editor, TreeViewer treeViewer, Config config) {
		super(parentShell);
		this.config = config;
		this.treeViewer = treeViewer;
		this.editor = editor;
		this.parentShell=parentShell;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.CreateFeatureDialog_CREARE_FEATURE); 
		
		Rectangle parentBounds = parentShell.getBounds();
		Rectangle shellBounds = newShell.getBounds();
		newShell.setLocation(parentBounds.x
				+ (parentBounds.width - shellBounds.width) / 2, parentBounds.y
				+ (parentBounds.height - shellBounds.height) / 2);
	}
	
	@Override
	public boolean isHelpAvailable() {
		return false;
	}
	@Override
	protected Control createDialogArea(Composite parent) {
		setMessage(Messages.CreateFeatureDialog_INPUT_FEATURE_NAME);
		Composite com = new Composite(parent, SWT.NONE);
		com.setLayout(new GridLayout(2, false));
		com.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label featureNameLabel = new Label(com, SWT.NONE);
		featureNameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		featureNameLabel.setText(Messages.CreateFeatureDialog_NAME); 

		this.featureNameText = new Text(com, SWT.BORDER);
		this.featureNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		return parent;
	}
	
	@Override
	protected void buttonPressed(int buttonId) {
		setErrorMessage(null);
		if (buttonId == IDialogConstants.OK_ID) {
			if ("".equals(this.featureNameText.getText())) { //$NON-NLS-1$
				setErrorMessage(Messages.CreateFeatureDialog_FEATURE_NMAE_NOT_NULL); 
				return;
			}
			for(Feature feature : config.getFeatures()) {
				if(feature.getName().equals(this.featureNameText.getText())) {
					setErrorMessage(Messages.CreateFeatureDialog_FEATURE_NAME_DUP); 
					return;
				}
			}
			feature = new Feature();
			feature.setName(this.featureNameText.getText());
			config.addFeature(feature);
			TreeNode node = new TreeNode(feature);
			treeViewer.setInput(config.createTreeNode());
			treeViewer.collapseAll();
			StructuredSelection selection = new StructuredSelection(node);
			treeViewer.setSelection(selection, true);
			treeViewer.refresh();
			editor.setDirty(true);
			editor.change();
		} 
		super.buttonPressed(buttonId);
	}
}
