/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.apicloud.authentication.AuthenticActivator;
import com.apicloud.commons.model.Config;
import com.apicloud.commons.model.Feature;
import com.apicloud.commons.model.Param;
import com.apicloud.navigator.ui.editors.ConfigEditor;

public class AddFeatureDialog extends Dialog {
	private Text text_urlScheme;
	private Text text_apiKey;
	private Label lblParamkey;
	private Label lblParamvalue;
	private TabFolder tabFolder;
	private Feature selectFeature;
	private Config config;
	private Feature feature;
	private TreeViewer treeViewer;
	private ConfigEditor editor;
	private List<Feature> features;
	private Shell parentShell;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public AddFeatureDialog(Shell parentShell, ConfigEditor editor, TreeViewer treeViewer, Config config) {
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

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));
		tabFolder = new TabFolder(container, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TabItem ui_LayoutItem = new TabItem(tabFolder, SWT.NONE);
		ui_LayoutItem.setText(Messages.AddFeatureDialog_UI);
		TableViewer UIViewer = createTable(tabFolder, ui_LayoutItem);
		UIViewer.setInput(getFeatureByType(Messages.AddFeatureDialog_THREE));
		
		TabItem navigationMenuItem = new TabItem(tabFolder, SWT.NONE);
		navigationMenuItem.setText(Messages.AddFeatureDialog_VIDEO);
		TableViewer videoViewer = createTable(tabFolder, navigationMenuItem);
		videoViewer.setInput(getFeatureByType(Messages.AddFeatureDialog_FOUR));
		
		TabItem extendedItem = new TabItem(tabFolder, SWT.NONE);
		extendedItem.setText(Messages.AddFeatureDialog_3RD);
		TableViewer _3rdItemViewer = createTable(tabFolder, extendedItem);
		_3rdItemViewer.setInput(getFeatureByType(Messages.AddFeatureDialog_FIVE));
		
		TabItem open_sdk_Item = new TabItem(tabFolder, SWT.NONE);
		open_sdk_Item.setText(Messages.AddFeatureDialog_CUSTOM);
		TableViewer customViewer = createTable(tabFolder, open_sdk_Item);
		customViewer.setInput(getFeatureByType(Messages.AddFeatureDialog_EIGHT));
		
		TabItem deviceAccessItem = new TabItem(tabFolder, SWT.NONE);
		deviceAccessItem.setText(Messages.AddFeatureDialog_INTERACTION);
		TableViewer InteractionViewer = createTable(tabFolder, deviceAccessItem);
		InteractionViewer.setInput(getFeatureByType(Messages.AddFeatureDialog_TWO));
		
		TabItem cloud_ServerItem = new TabItem(tabFolder, SWT.NONE);
		cloud_ServerItem.setText(Messages.AddFeatureDialog_CLOUDSERVER);
		TableViewer cloud_ServerViewer = createTable(tabFolder, cloud_ServerItem);
		cloud_ServerViewer.setInput(getFeatureByType(Messages.AddFeatureDialog_NINE));
		 
		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayout(new GridLayout(4, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		lblParamkey = new Label(composite, SWT.NONE);
		lblParamkey.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblParamkey.setText("       urlScheme");
		lblParamkey.setVisible(false);
		
		text_urlScheme = new Text(composite, SWT.BORDER);
		text_urlScheme.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		text_urlScheme.setVisible(false);
		
		lblParamvalue = new Label(composite, SWT.NONE);
		lblParamvalue.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblParamvalue.setText("     apiKey");
		lblParamvalue.setVisible(false);
		
		text_apiKey = new Text(composite, SWT.BORDER);
		text_apiKey.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		text_apiKey.setVisible(false);
		return container;
	}

	private TableViewer createTable(TabFolder tabFolder, TabItem systemItem) {
		TableViewer tableViewer = new TableViewer(tabFolder, SWT.BORDER | SWT.FULL_SELECTION);
		final Table table = tableViewer.getTable();
		table.addListener(SWT.MeasureItem, new Listener() {
			@Override
			public void handleEvent(Event event) {
				event.height = 30;
				
			}
		});
		table.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				TableItem[] ti = table.getSelection();
				Feature f = (Feature)ti[0].getData();
				selectFeature = f;
				lblParamkey.setText("       urlScheme");
				lblParamvalue.setText("     apiKey");
				if(f.getName().equals("weiXin") || f.getName().equals("sinaWeiBo") || f.getName().equals("qq")) {
					lblParamkey.setVisible(true);
					text_urlScheme.setVisible(true);
					lblParamvalue.setVisible(true);
					text_apiKey.setVisible(true);
				}else if(f.getName().equals("baiduLocation") ){
					lblParamkey.setVisible(false);
					text_urlScheme.setVisible(false);
					lblParamvalue.setVisible(true);
					text_apiKey.setVisible(true);
				}else if(f.getName().equals("aliPay") ){
					lblParamkey.setVisible(true);
					text_urlScheme.setVisible(true);
					lblParamvalue.setVisible(false);
					text_apiKey.setVisible(false);
				}else if(f.getName().equals("baiduMap") ){
					lblParamkey.setText("androidAPIKey");
					lblParamvalue.setText("iosAPIKey");
					lblParamkey.setVisible(true);
					text_urlScheme.setVisible(true);
					lblParamvalue.setVisible(true);
					text_apiKey.setVisible(true);
				}else {
					lblParamkey.setVisible(false);
					text_urlScheme.setVisible(false);
					lblParamvalue.setVisible(false);
					text_apiKey.setVisible(false);
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		systemItem.setControl(tableViewer.getTable());
		TableViewerColumn tableViewerColumn_name = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tableColumn_name = tableViewerColumn_name.getColumn();
		tableColumn_name.setWidth(150);
		tableViewerColumn_name.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Feature)element).getName();
			}

		});
		
		TableViewerColumn tableViewerColumn_desc = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tableColumn_desc = tableViewerColumn_desc.getColumn();
		tableColumn_desc.setWidth(310);
		tableViewerColumn_desc.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Feature)element).getDesc();
			}

		});
		TableViewerColumn tableViewerColumn_ios = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tableColumn_ios = tableViewerColumn_ios.getColumn();
		tableColumn_ios.setWidth(20);
		tableViewerColumn_ios.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return null;
			}
			@Override
			public Image getImage(Object element) {
				return ((Feature)element).isIos() ? AuthenticActivator
						.getImage("icons/ios.png") : super.getImage(element);
			}
		});
		
		TableViewerColumn tableViewerColumn_android = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tableColumn_android = tableViewerColumn_android.getColumn();
		tableColumn_android.setWidth(20);
		tableViewerColumn_android.setLabelProvider(new ColumnLabelProvider() {
			
			@Override
			public String getText(Object element) {
				return null;
			}
			@Override
			public Image getImage(Object element) {
				return ((Feature)element).isAndroid() ? AuthenticActivator
						.getImage("icons/android.png") : super.getImage(element);
			}
		});
		
		tableViewer.setContentProvider(new ArrayContentProvider());
		return tableViewer;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if(buttonId == IDialogConstants.OK_ID) {
			if(selectFeature == null) {
				MessageDialog.openInformation(getShell(), Messages.AddFeatureDialog_INFORMATION, Messages.AddFeatureDialog_MESSAGE);
				return;
			}
			for(Feature feature : config.getFeatures()) {
				if(feature.getName().equals(selectFeature.getName())) {
					MessageDialog.openInformation(getShell(), Messages.AddFeatureDialog_INFORMATION, Messages.CreateFeatureDialog_FEATURE_NAME_DUP);
					return;
				}
			}
			if(text_urlScheme.isVisible() && text_urlScheme.getText().isEmpty()) {
				MessageDialog.openInformation(getShell(), Messages.AddFeatureDialog_INFORMATION, lblParamkey.getText().trim() +Messages.AddFeatureDialog_MESSAGE_NULL);
				return;
			}
			if(text_apiKey.isVisible() && text_apiKey.getText().isEmpty()) {
				MessageDialog.openInformation(getShell(), Messages.AddFeatureDialog_INFORMATION, lblParamvalue.getText().trim() +Messages.AddFeatureDialog_MESSAGE_NULL);
				return;
			}
			feature = new Feature();
			feature.setName(selectFeature.getName());
			if(text_urlScheme.isVisible()) {
				Param p = new Param();
				p.setName("urlScheme");
				if(feature.getName().equals("baiduMap")) {
					p.setName("android_api_key");
				}
				
				p.setValue(text_urlScheme.getText());
				feature.getParams().add(p);
			}
			if(text_apiKey.isVisible()) {
				Param p = new Param();
				p.setName("apiKey");
				if(feature.getName().equals("baiduMap")) {
					p.setName("ios_api_key");
				}
				p.setValue(text_apiKey.getText());
				feature.getParams().add(p);
			}
			
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

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(563, 453);
	}

	private List<Feature> getFeatureByType(String type) {
		List<Feature> features = new ArrayList<Feature>();
		for(Feature feature : getAllFeature()) {
			if(type.equals(feature.getType())) {
				features.add(feature);
			}
		}
		return features;
	}
	private List<Feature> getAllFeature() {
		if(features == null) {
			features = Feature.loadXml(AuthenticActivator.getFeatureFile());
		}
		return features;
	}
}
