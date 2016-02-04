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
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
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
import com.apicloud.commons.model.Param;
import com.apicloud.navigator.ui.editors.ConfigEditor;

public class CreateParamDialog extends TitleAreaDialog {
	private Config config;
	private TreeViewer treeViewer;
	private Text paramNameText;
	private Text paramValueText;
	private ConfigEditor editor;
	private ComboViewer  list;
	private Shell parentShell;
	public CreateParamDialog(Shell parentShell, ConfigEditor editor, TreeViewer treeViewer, Config config) {
		super(parentShell);
		this.config = config;
		this.treeViewer = treeViewer;
		this.editor = editor;
		this.parentShell=parentShell;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.CreateParamDialog_CREATE_PARAM); 
		
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
		setMessage(Messages.CreateParamDialog_INPUT_PARAM_NAME);
		Composite com = new Composite(parent, SWT.NONE);
		com.setLayout(new GridLayout(2, false));
		com.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label featureLabel = new Label(com, SWT.NONE);
		featureLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		featureLabel.setText("feature");  //$NON-NLS-1$
		this.list = new ComboViewer(com, SWT.NONE);
		this.list.setLabelProvider(new LabelProvider());
		this.list.setContentProvider(new ArrayContentProvider());
		this.list.setInput(config.getFeatures());
		this.list.getCombo().select(0);
		
		Label paramNameLabel = new Label(com, SWT.NONE);
		paramNameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		paramNameLabel.setText(Messages.CreateParamDialog_NAME); 

		this.paramNameText = new Text(com, SWT.BORDER);
		this.paramNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label paramValueLabel = new Label(com, SWT.NONE);
		paramValueLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		paramValueLabel.setText(Messages.CreateParamDialog_VALUE); 

		this.paramValueText = new Text(com, SWT.BORDER);
		this.paramValueText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		return parent;
	}
	
	@Override
	protected void buttonPressed(int buttonId) {
		setErrorMessage(null);
		if (buttonId == IDialogConstants.OK_ID) {
			if ("".equals(this.paramNameText.getText())) { //$NON-NLS-1$
				setErrorMessage("param\u540D\u4E0D\u80FD\u4E3A\u7A7A"); //$NON-NLS-1$
				return;
			}
			if ("".equals(this.paramValueText.getText())) { //$NON-NLS-1$
				setErrorMessage("param\u503C\u4E0D\u80FD\u4E3A\u7A7A"); //$NON-NLS-1$
				return;
			}
			StructuredSelection ss = (StructuredSelection)list.getSelection();
			Feature feature = (Feature)ss.getFirstElement();
			
			for(Param param : feature.getParams()) {
				if(param.getName().equals(this.paramNameText.getText())) {
					setErrorMessage(Messages.PARAMNAMEREPEAT); //$NON-NLS-1$
					return;
				}
			}
			Param p = new Param();
			p.setName(this.paramNameText.getText());
			p.setValue(this.paramValueText.getText());
			
			feature.addParams(p);
			TreeNode node = new TreeNode(p);
			node.setParent(new TreeNode(feature));
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
