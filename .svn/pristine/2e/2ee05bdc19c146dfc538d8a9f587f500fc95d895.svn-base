/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.ui.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.EditorPart;

import com.apicloud.commons.model.Access;
import com.apicloud.commons.model.Config;
import com.apicloud.commons.model.Feature;
import com.apicloud.commons.model.Param;
import com.apicloud.commons.model.Permission;
import com.apicloud.commons.model.Preference;
import com.apicloud.navigator.Activator;
import com.apicloud.navigator.dialogs.AddFeatureDialog;
import com.apicloud.navigator.dialogs.CreateParamDialog;
import com.apicloud.navigator.dialogs.Messages;
import com.apicloud.navigator.ui.builder.GeneralSectionBuilder;

public class ConfigEditor extends EditorPart {
	private final FormToolkit formToolkit = new FormToolkit(
			Display.getDefault());
	private Config config;
	private boolean dirty;
	private Table table_1;
	private TreeViewer treeViewer;
	private TableViewer tableViewer_1;
	private GeneralSectionBuilder generalSection;
	private TableViewer tableViewer_2;
	private Table table_2;
	private Text appBackgroundText;
	private Text windowBackgroundText;
	private Text frameBackgroundText;
	private Table table_3;
	private TableViewer tableViewer_3;
	private Button pageBounceButton_ok;
	private Button pageBounceButton_cancel;
	private Button hScrollBarEnabledButton_cancel;
	private Button hScrollBarEnabledButton_ok;
	private Button vScrollBarEnabledButton_cancel;
	private Button vScrollBarEnabledButton_ok;
	private Button fullScreenButton_cancel;
	private Button fullScreenButton_ok;
	private Button ios7Button_cancel;
	private Button ios7Button_ok;
	private Button autoLaunchButton_cancel;
	private Button autoLaunchButton_ok;
	private Button autoUpdateButton_ok;
	private Button autoUpdateButton_cancel;
	private Button smartUpdateButton_ok;
	private Button smartUpdateButton_cancel;

	public ConfigEditor(APICloudMapConfigEditor editor, Config config) {
		this.config = config;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
	}

	public void setDirty(boolean flag) {
		this.dirty = flag;
	}

	@Override
	public boolean isDirty() {
		return this.dirty;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(parent,
				SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 1, 1));
		formToolkit.adapt(scrolledComposite);
		formToolkit.paintBordersFor(scrolledComposite);
		scrolledComposite.setMinWidth(600);
		scrolledComposite.setMinHeight(1400);
		scrolledComposite.getVerticalBar().setIncrement(10);

		Composite composite_3 = new Composite(scrolledComposite, SWT.NONE);
		formToolkit.adapt(composite_3);
		formToolkit.paintBordersFor(composite_3);
		composite_3.setLayout(new GridLayout(1, false));

		generalSection = new GeneralSectionBuilder(formToolkit, composite_3,
				this);
		generalSection.buildUI();
		generalSection.bindData(config);

		Section preferenceSection = formToolkit.createSection(composite_3,
				Section.TWISTIE | Section.TITLE_BAR);
		preferenceSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false, 1, 1));
		formToolkit.paintBordersFor(preferenceSection);
		preferenceSection.setText("\u504F\u597D\u8BBE\u7F6E");
		preferenceSection.setExpanded(true);

		Composite preferenceComposite = formToolkit.createComposite(
				preferenceSection, SWT.NONE);
		preferenceComposite.setToolTipText("");
		formToolkit.paintBordersFor(preferenceComposite);
		preferenceSection.setClient(preferenceComposite);
		preferenceComposite.setLayout(new GridLayout(2, false));

		Label appBackground = new Label(preferenceComposite, SWT.NONE);
		appBackground.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true,
				false, 1, 1));
		formToolkit.adapt(appBackground, true, true);
		appBackground.setText("appBackground:");

		Composite appBackgroundComposite = new Composite(preferenceComposite,
				SWT.NONE);
		appBackgroundComposite.setLayout(new GridLayout(1, false));
		appBackgroundComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, false, 1, 1));
		formToolkit.adapt(appBackgroundComposite);
		formToolkit.paintBordersFor(appBackgroundComposite);

		appBackgroundText = new Text(appBackgroundComposite, SWT.BORDER);
		GridData gd_appBackgroundText = new GridData(SWT.FILL, SWT.CENTER,
				false, false, 1, 1);
		gd_appBackgroundText.widthHint = 200;
		appBackgroundText.setLayoutData(gd_appBackgroundText);
		appBackgroundText
				.setText(getPreferenceValue("appBackground", config) == null ? ""
						: getPreferenceValue("appBackground", config));
		appBackgroundText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String value = appBackgroundText.getText() == null ? ""
						: appBackgroundText.getText();
				changePreferenceValue("appBackground", value);
				dirty = true;
				firePropertyChange(PROP_DIRTY);
			}
		});
		formToolkit.adapt(appBackgroundText, true, true);

		Label windowBackground = new Label(preferenceComposite, SWT.NONE);
		windowBackground.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
				false, false, 1, 1));
		formToolkit.adapt(windowBackground, true, true);
		windowBackground.setText("windowBackground:");

		Composite windowBackgroundComposite = new Composite(
				preferenceComposite, SWT.NONE);
		windowBackgroundComposite.setLayout(new GridLayout(1, false));
		windowBackgroundComposite.setLayoutData(new GridData(SWT.FILL,
				SWT.FILL, true, false, 1, 1));
		formToolkit.adapt(windowBackgroundComposite);
		formToolkit.paintBordersFor(windowBackgroundComposite);


		windowBackgroundText = new Text(windowBackgroundComposite, SWT.BORDER);
		GridData gd_windowBackgroundText = new GridData(SWT.FILL, SWT.CENTER,
				false, false, 1, 1);
		gd_windowBackgroundText.widthHint = 200;
		windowBackgroundText.setLayoutData(gd_windowBackgroundText);
		windowBackgroundText
				.setText(getPreferenceValue("windowBackground", config) == null ? ""
						: getPreferenceValue("windowBackground", config));
		windowBackgroundText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String value = windowBackgroundText.getText() == null ? ""
						: windowBackgroundText.getText();
				changePreferenceValue("windowBackground", value);
				dirty = true;
				firePropertyChange(PROP_DIRTY);
			}
		});
		formToolkit.adapt(windowBackgroundText, true, true);

		Label frameBackground = new Label(preferenceComposite, SWT.NONE);
		frameBackground.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
				false, false, 1, 1));
		formToolkit.adapt(frameBackground, true, true);
		frameBackground.setText("frameBackgroundColor:");

		Composite frameBackgroundComposite = new Composite(preferenceComposite,
				SWT.NONE);
		frameBackgroundComposite.setLayout(new GridLayout(6, false));
		frameBackgroundComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, false, 1, 1));
		formToolkit.adapt(frameBackgroundComposite);
		formToolkit.paintBordersFor(frameBackgroundComposite);


		frameBackgroundText = new Text(frameBackgroundComposite, SWT.BORDER);
		GridData gd_frameBackgroundText = new GridData(SWT.FILL, SWT.CENTER,
				false, false, 1, 1);
		gd_frameBackgroundText.widthHint = 200;
		frameBackgroundText.setLayoutData(gd_frameBackgroundText);
		frameBackgroundText
				.setText(getPreferenceValue("frameBackgroundColor", config) == null ? ""
						: getPreferenceValue("frameBackgroundColor", config));
		frameBackgroundText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String value = frameBackgroundText.getText() == null ? ""
						: frameBackgroundText.getText();
				changePreferenceValue("frameBackgroundColor", value);
				dirty = true;
				firePropertyChange(PROP_DIRTY);
			}
		});
		formToolkit.adapt(frameBackgroundText, true, true);

		Label lblNewLabel_4 = new Label(frameBackgroundComposite, SWT.NONE);
		formToolkit.adapt(lblNewLabel_4, true, true);
		new Label(frameBackgroundComposite, SWT.NONE);
		new Label(frameBackgroundComposite, SWT.NONE);
		new Label(frameBackgroundComposite, SWT.NONE);
		new Label(frameBackgroundComposite, SWT.NONE);

		Label pageBounce = new Label(preferenceComposite, SWT.NONE);
		pageBounce.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		formToolkit.adapt(pageBounce, true, true);
		pageBounce.setText("pageBounce:");

		Composite pageBounceComposite = new Composite(preferenceComposite,
				SWT.NONE);
		pageBounceComposite.setLayout(new GridLayout(2, false));
		pageBounceComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, false, 1, 1));
		formToolkit.adapt(pageBounceComposite);
		formToolkit.paintBordersFor(pageBounceComposite);

		pageBounceButton_ok = new Button(pageBounceComposite,
				SWT.RADIO | SWT.RIGHT);
		pageBounceButton_ok.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				changePreferenceValue("pageBounce", "true");
				dirty = true;
				firePropertyChange(PROP_DIRTY);
			}

		});
		pageBounceButton_ok.setAlignment(SWT.LEFT);
		formToolkit.adapt(pageBounceButton_ok, true, true);
		pageBounceButton_ok.setText("true");

		pageBounceButton_cancel = new Button(pageBounceComposite, SWT.RADIO);
		if (Boolean
				.parseBoolean(getPreferenceValue("pageBounce", config) == null ? "false"
						: getPreferenceValue("pageBounce", config))) {
			pageBounceButton_ok.setSelection(true);
			changePreferenceValue("pageBounce", "true");
		} else {
			pageBounceButton_cancel.setSelection(true);
			changePreferenceValue("pageBounce", "false");
		}
		pageBounceButton_cancel.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				changePreferenceValue("pageBounce", "false");
				dirty = true;
				firePropertyChange(PROP_DIRTY);
			}

		});
		formToolkit.adapt(pageBounceButton_cancel, true, true);
		pageBounceButton_cancel.setText("false");

		Label hScrollBarEnabled = new Label(preferenceComposite, SWT.NONE);
		hScrollBarEnabled.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
				false, false, 1, 1));
		formToolkit.adapt(hScrollBarEnabled, true, true);
		hScrollBarEnabled.setText("hScrollBarEnabled:");

		Composite hScrollBarEnabledComposite = new Composite(
				preferenceComposite, SWT.NONE);
		hScrollBarEnabledComposite.setLayout(new GridLayout(2, false));
		hScrollBarEnabledComposite.setLayoutData(new GridData(SWT.FILL,
				SWT.FILL, true, false, 1, 1));
		formToolkit.adapt(hScrollBarEnabledComposite);
		formToolkit.paintBordersFor(hScrollBarEnabledComposite);

		hScrollBarEnabledButton_ok = new Button(
				hScrollBarEnabledComposite, SWT.RADIO | SWT.RIGHT);
		hScrollBarEnabledButton_ok.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				changePreferenceValue("hScrollBarEnabled", "true");
				dirty = true;
				firePropertyChange(PROP_DIRTY);
			}

		});
		hScrollBarEnabledButton_ok.setAlignment(SWT.LEFT);
		formToolkit.adapt(hScrollBarEnabledButton_ok, true, true);
		hScrollBarEnabledButton_ok.setText("true");

		hScrollBarEnabledButton_cancel = new Button(
				hScrollBarEnabledComposite, SWT.RADIO);
		if (Boolean
				.parseBoolean(getPreferenceValue("hScrollBarEnabled", config) == null ? "false"
						: getPreferenceValue("hScrollBarEnabled", config))) {
			hScrollBarEnabledButton_ok.setSelection(true);
			changePreferenceValue("hScrollBarEnabled", "true");
		} else {
			hScrollBarEnabledButton_cancel.setSelection(true);
			changePreferenceValue("hScrollBarEnabled", "false");
		}
		hScrollBarEnabledButton_cancel
				.addSelectionListener(new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						changePreferenceValue("hScrollBarEnabled", "false");
						dirty = true;
						firePropertyChange(PROP_DIRTY);
					}

				});
		formToolkit.adapt(hScrollBarEnabledButton_cancel, true, true);
		hScrollBarEnabledButton_cancel.setText("false");

		Label vScrollBarEnabled = new Label(preferenceComposite, SWT.NONE);
		vScrollBarEnabled.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
				false, false, 1, 1));
		formToolkit.adapt(vScrollBarEnabled, true, true);
		vScrollBarEnabled.setText("vScrollBarEnabled:");

		Composite vScrollBarEnabledComposite = new Composite(
				preferenceComposite, SWT.NONE);
		vScrollBarEnabledComposite.setLayout(new GridLayout(2, false));
		vScrollBarEnabledComposite.setLayoutData(new GridData(SWT.FILL,
				SWT.FILL, true, false, 1, 1));
		formToolkit.adapt(vScrollBarEnabledComposite);
		formToolkit.paintBordersFor(vScrollBarEnabledComposite);

		vScrollBarEnabledButton_ok = new Button(
				vScrollBarEnabledComposite, SWT.RADIO | SWT.RIGHT);
		vScrollBarEnabledButton_ok.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				changePreferenceValue("vScrollBarEnabled", "true");
				dirty = true;
				firePropertyChange(PROP_DIRTY);
			}

		});
		vScrollBarEnabledButton_ok.setAlignment(SWT.LEFT);
		formToolkit.adapt(vScrollBarEnabledButton_ok, true, true);
		vScrollBarEnabledButton_ok.setText("true");

		vScrollBarEnabledButton_cancel = new Button(
				vScrollBarEnabledComposite, SWT.RADIO);
		if (Boolean
				.parseBoolean(getPreferenceValue("vScrollBarEnabled", config) == null ? "false"
						: getPreferenceValue("vScrollBarEnabled", config))) {
			vScrollBarEnabledButton_ok.setSelection(true);
			changePreferenceValue("vScrollBarEnabled", "true");
		} else {
			vScrollBarEnabledButton_cancel.setSelection(true);
			changePreferenceValue("vScrollBarEnabled", "false");
		}
		vScrollBarEnabledButton_cancel
				.addSelectionListener(new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						changePreferenceValue("vScrollBarEnabled", "false");
						dirty = true;
						firePropertyChange(PROP_DIRTY);
					}

				});
		formToolkit.adapt(vScrollBarEnabledButton_cancel, true, true);
		vScrollBarEnabledButton_cancel.setText("false");

		Label fullScreen = new Label(preferenceComposite, SWT.NONE);
		fullScreen.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		formToolkit.adapt(fullScreen, true, true);
		fullScreen.setText("fullScreen:");

		Composite fullScreenComposite = new Composite(preferenceComposite,
				SWT.NONE);
		fullScreenComposite.setLayout(new GridLayout(2, false));
		fullScreenComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, false, 1, 1));
		formToolkit.adapt(fullScreenComposite);
		formToolkit.paintBordersFor(fullScreenComposite);

		fullScreenButton_ok = new Button(fullScreenComposite,
				SWT.RADIO | SWT.RIGHT);
		fullScreenButton_ok.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				changePreferenceValue("fullScreen", "true");
				dirty = true;
				firePropertyChange(PROP_DIRTY);
			}

		});

		fullScreenButton_ok.setAlignment(SWT.LEFT);
		formToolkit.adapt(fullScreenButton_ok, true, true);
		fullScreenButton_ok.setText("true");

		fullScreenButton_cancel = new Button(fullScreenComposite,
				SWT.RADIO);
		if (Boolean
				.parseBoolean(getPreferenceValue("fullScreen", config) == null ? "false"
						: getPreferenceValue("fullScreen", config))) {
			fullScreenButton_ok.setSelection(true);
			changePreferenceValue("fullScreen", "true");
		} else {
			fullScreenButton_cancel.setSelection(true);
			changePreferenceValue("fullScreen", "false");
		}
		fullScreenButton_cancel.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				changePreferenceValue("fullScreen", "false");
				dirty = true;
				firePropertyChange(PROP_DIRTY);
			}

		});
		formToolkit.adapt(fullScreenButton_cancel, true, true);
		fullScreenButton_cancel.setText("false");

		Label ios7 = new Label(preferenceComposite, SWT.NONE);
		ios7.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1,
				1));
		formToolkit.adapt(ios7, true, true);
		ios7.setText("IOS7StatusBarAppearance:");

		Composite ios7Composite = new Composite(preferenceComposite, SWT.NONE);
		ios7Composite.setLayout(new GridLayout(2, false));
		ios7Composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false, 1, 1));
		formToolkit.adapt(ios7Composite);
		formToolkit.paintBordersFor(ios7Composite);

		ios7Button_ok = new Button(ios7Composite, SWT.RADIO
				| SWT.RIGHT);
		ios7Button_ok.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				changePreferenceValue("iOS7StatusBarAppearance", "true");
				dirty = true;
				firePropertyChange(PROP_DIRTY);
			}

		});
		ios7Button_ok.setAlignment(SWT.LEFT);
		formToolkit.adapt(ios7Button_ok, true, true);
		ios7Button_ok.setText("true");

		ios7Button_cancel = new Button(ios7Composite, SWT.RADIO);
		if (Boolean
				.parseBoolean(getPreferenceValue("iOS7StatusBarAppearance", config) == null ? "false"
						: getPreferenceValue("iOS7StatusBarAppearance", config))) {
			ios7Button_ok.setSelection(true);
			changePreferenceValue("iOS7StatusBarAppearance", "true");
		} else {
			ios7Button_cancel.setSelection(true);
			changePreferenceValue("iOS7StatusBarAppearance", "false");
		}
		ios7Button_cancel.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				changePreferenceValue("iOS7StatusBarAppearance", "false");
				dirty = true;
				firePropertyChange(PROP_DIRTY);
			}

		});
		formToolkit.adapt(ios7Button_cancel, true, true);
		ios7Button_cancel.setText("false");

		Label autoLaunch = new Label(preferenceComposite, SWT.NONE);
		autoLaunch.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		formToolkit.adapt(autoLaunch, true, true);
		autoLaunch.setText("autoLaunch:");

		Composite autoLaunchComposite = new Composite(preferenceComposite,
				SWT.NONE);
		autoLaunchComposite.setLayout(new GridLayout(2, false));
		autoLaunchComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, false, 1, 1));
		formToolkit.adapt(autoLaunchComposite);
		formToolkit.paintBordersFor(autoLaunchComposite);

		autoLaunchButton_ok = new Button(autoLaunchComposite,
				SWT.RADIO | SWT.RIGHT);
		autoLaunchButton_ok.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				changePreferenceValue("autoLaunch", "true");
				dirty = true;
				firePropertyChange(PROP_DIRTY);
			}

		});
		autoLaunchButton_ok.setAlignment(SWT.LEFT);
		formToolkit.adapt(autoLaunchButton_ok, true, true);
		autoLaunchButton_ok.setText("true");

		autoLaunchButton_cancel = new Button(autoLaunchComposite,
				SWT.RADIO);
		if (Boolean
				.parseBoolean(getPreferenceValue("autoLaunch", config) == null ? "false"
						: getPreferenceValue("autoLaunch", config))) {
			autoLaunchButton_ok.setSelection(true);
			changePreferenceValue("autoLaunch", "true");
		} else {
			autoLaunchButton_cancel.setSelection(true);
			changePreferenceValue("autoLaunch", "false");
		}
		autoLaunchButton_cancel.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				changePreferenceValue("autoLaunch", "false");
				dirty = true;
				firePropertyChange(PROP_DIRTY);
			}

		});
		formToolkit.adapt(autoLaunchButton_cancel, true, true);
		autoLaunchButton_cancel.setText("false");

		Label autoUpdate = new Label(preferenceComposite, SWT.NONE);
		autoUpdate.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		formToolkit.adapt(autoUpdate, true, true);
		autoUpdate.setText("autoUpdate:");

		Composite autoUpdateComposite = new Composite(preferenceComposite,
				SWT.NONE);
		autoUpdateComposite.setLayout(new GridLayout(2, false));
		autoUpdateComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, false, 1, 1));
		formToolkit.adapt(autoUpdateComposite);
		formToolkit.paintBordersFor(autoUpdateComposite);

		autoUpdateButton_ok = new Button(autoUpdateComposite,
				SWT.RADIO | SWT.RIGHT);
		autoUpdateButton_ok.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				changePreferenceValue("autoUpdate", "true");
				dirty = true;
				firePropertyChange(PROP_DIRTY);
			}

		});
		autoUpdateButton_ok.setAlignment(SWT.LEFT);
		formToolkit.adapt(autoUpdateButton_ok, true, true);
		autoUpdateButton_ok.setText("true");

		autoUpdateButton_cancel = new Button(autoUpdateComposite,
				SWT.RADIO);
		if (Boolean
				.parseBoolean(getPreferenceValue("autoUpdate", config) == null ? "false"
						: getPreferenceValue("autoUpdate", config))) {
			autoUpdateButton_ok.setSelection(true);
			changePreferenceValue("autoUpdate", "true");
		} else {
			autoUpdateButton_cancel.setSelection(true);
			changePreferenceValue("autoUpdate", "false");
		}
		autoUpdateButton_cancel.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				changePreferenceValue("autoUpdate", "false");
				dirty = true;
				firePropertyChange(PROP_DIRTY);
			}

		});
		formToolkit.adapt(autoUpdateButton_cancel, true, true);
		autoUpdateButton_cancel.setText("false");
		
		Label smartUpdate = new Label(preferenceComposite, SWT.NONE);
		smartUpdate.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		formToolkit.adapt(smartUpdate, true, true);
		smartUpdate.setText("smartUpdate:");

		Composite smartUpdateComposite = new Composite(preferenceComposite,
				SWT.NONE);
		smartUpdateComposite.setLayout(new GridLayout(2, false));
		smartUpdateComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, false, 1, 1));
		formToolkit.adapt(smartUpdateComposite);
		formToolkit.paintBordersFor(smartUpdateComposite);

		smartUpdateButton_ok = new Button(smartUpdateComposite,
				SWT.RADIO | SWT.RIGHT);
		smartUpdateButton_ok.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				changePreferenceValue("smartUpdate", "true");
				dirty = true;
				firePropertyChange(PROP_DIRTY);
			}

		});
		smartUpdateButton_ok.setAlignment(SWT.LEFT);
		formToolkit.adapt(smartUpdateButton_ok, true, true);
		smartUpdateButton_ok.setText("true");

		smartUpdateButton_cancel = new Button(smartUpdateComposite,
				SWT.RADIO);
		if (Boolean
				.parseBoolean(getPreferenceValue("smartUpdate", config) == null ? "false"
						: getPreferenceValue("smartUpdate", config))) {
			smartUpdateButton_ok.setSelection(true);
			changePreferenceValue("smartUpdate", "true");
		} else {
			smartUpdateButton_cancel.setSelection(true);
			changePreferenceValue("smartUpdate", "false");
		}
		smartUpdateButton_cancel.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				changePreferenceValue("smartUpdate", "false");
				dirty = true;
				firePropertyChange(PROP_DIRTY);
			}

		});
		formToolkit.adapt(smartUpdateButton_cancel, true, true);
		smartUpdateButton_cancel.setText("false");
		Section featureSection = formToolkit.createSection(composite_3,
				Section.TWISTIE | Section.TITLE_BAR);
		GridData gd_featureSection = new GridData(SWT.FILL, SWT.FILL, true,
				false, 1, 1);
		gd_featureSection.heightHint = 112;
		featureSection.setLayoutData(gd_featureSection);
		formToolkit.paintBordersFor(featureSection);
		featureSection.setText(Messages.FEATURESETUP);
		featureSection.setExpanded(true);

		Composite composite_8 = new Composite(featureSection, SWT.NONE);
		formToolkit.adapt(composite_8);
		formToolkit.paintBordersFor(composite_8);
		featureSection.setClient(composite_8);
		composite_8.setLayout(new GridLayout(1, false));

		Label lblNewLabel_1 = new Label(composite_8, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 1, 1));
		formToolkit.adapt(lblNewLabel_1, true, true);
		lblNewLabel_1.setText(Messages.SYSTEMINFO);

		Composite composite = new Composite(composite_8, SWT.NONE);
		GridData gd_composite = new GridData(SWT.CENTER, SWT.CENTER, true,
				false, 1, 1);
		gd_composite.widthHint = 380;
		composite.setLayoutData(gd_composite);
		formToolkit.adapt(composite);
		formToolkit.paintBordersFor(composite);

		final Label addFeatureLabel = formToolkit.createLabel(composite, "",
				SWT.NONE);
		addFeatureLabel.setImage(Activator.getImage("icons/addfeature.png"));
		addFeatureLabel.setBounds(0, 0, 90, 26);
		addFeatureLabel.addMouseListener(new MouseAdapter() {

			public void mouseUp(MouseEvent e) {
				AddFeatureDialog dialog = new AddFeatureDialog(getSite()
						.getShell(), ConfigEditor.this, treeViewer, config);
				
				dialog.open();
			}
		});
		addFeatureLabel.addMouseTrackListener(new MouseTrackListener() {
			@Override
			public void mouseHover(MouseEvent e) {
				addFeatureLabel.setImage(Activator
						.getImage("icons/addfeaturefocus.png"));
			}

			@Override
			public void mouseExit(MouseEvent e) {
				addFeatureLabel.setImage(Activator
						.getImage("icons/addfeature.png"));
			}

			@Override
			public void mouseEnter(MouseEvent e) {
				addFeatureLabel.setImage(Activator
						.getImage("icons/addfeaturefocus.png"));
			}
		});

		final Label addParamLabel = formToolkit.createLabel(composite, "",
				SWT.NONE);
		addParamLabel.setImage(Activator.getImage("icons/addParam.png"));
		addParamLabel.setBounds(95, 0, 90, 26);
		addParamLabel.addMouseListener(new MouseAdapter() {

			public void mouseUp(MouseEvent e) {
				if (config.getFeatures().size() == 0) {
					MessageDialog
							.openInformation(getSite().getShell(),
									Messages.PackageAppItemDialog_INFO,
									Messages.ADDFEATURE);
					return;
				}
				CreateParamDialog dialog = new CreateParamDialog(getSite()
						.getShell(), ConfigEditor.this, treeViewer, config);
				dialog.open();
			}
		});
		addParamLabel.addMouseTrackListener(new MouseTrackListener() {
			@Override
			public void mouseHover(MouseEvent e) {
				addParamLabel.setImage(Activator
						.getImage("icons/addParamfocus.png"));
			}

			@Override
			public void mouseExit(MouseEvent e) {
				addParamLabel.setImage(Activator.getImage("icons/addParam.png"));
			}

			@Override
			public void mouseEnter(MouseEvent e) {
				addParamLabel.setImage(Activator
						.getImage("icons/addParamfocus.png"));
			}
		});

		final Label deleteNodeLabel = formToolkit.createLabel(composite, "",
				SWT.NONE);
		deleteNodeLabel.setImage(Activator.getImage("icons/deleteNode.png"));
		deleteNodeLabel.setBounds(290, 0, 90, 26);
		deleteNodeLabel.addMouseListener(new MouseAdapter() {

			public void mouseUp(MouseEvent e) {
				if (treeViewer.getSelection() == null)
					return;
				IStructuredSelection ss = (StructuredSelection) treeViewer
						.getSelection();
				if (ss.getFirstElement() instanceof TreeNode) {
					TreeNode node = (TreeNode) ss.getFirstElement();
					if (node.getValue() instanceof Feature) {
						config.getFeatures().remove((Feature) node.getValue());
					}
					if (node.getValue() instanceof Param) {
						TreeNode parent = node.getParent();
						Feature feature = (Feature) parent.getValue();
						Param param = (Param) node.getValue();
						feature.removeParams(param);
					}
					dirty = true;
					firePropertyChange(PROP_DIRTY);
					treeViewer.setInput(config.createTreeNode());
					treeViewer.refresh();
				}
			}
		});
		deleteNodeLabel.addMouseTrackListener(new MouseTrackListener() {
			@Override
			public void mouseHover(MouseEvent e) {
				deleteNodeLabel.setImage(Activator
						.getImage("icons/deleteNodefocus.png"));
			}

			@Override
			public void mouseExit(MouseEvent e) {
				deleteNodeLabel.setImage(Activator
						.getImage("icons/deleteNode.png"));
			}

			@Override
			public void mouseEnter(MouseEvent e) {
				deleteNodeLabel.setImage(Activator
						.getImage("icons/deleteNodefocus.png"));
			}
		});

		treeViewer = new TreeViewer(composite_8, SWT.BORDER
				| SWT.FULL_SELECTION);
		Tree tree = treeViewer.getTree();
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);
		GridData gd_tree = new GridData(SWT.CENTER, SWT.FILL, true, true, 1, 1);
		gd_tree.heightHint = 112;
		tree.setLayoutData(gd_tree);
		formToolkit.paintBordersFor(tree);
		this.treeViewer.setContentProvider(new TreeNodeContentProvider());
		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(treeViewer,
				SWT.NONE);
		TreeColumn trclmnNewColumn = treeViewerColumn.getColumn();
		trclmnNewColumn.setWidth(180);
		trclmnNewColumn.setText("\u540D\u79F0");
		treeViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TreeNode treeNode = TreeNode.class.cast(element);
				if (treeNode.getValue() instanceof Feature) {
					return ((Feature) treeNode.getValue()).getName();
				}
				if (treeNode.getValue() instanceof Param) {
					return ((Param) treeNode.getValue()).getName();
				}
				return element.toString();
			}

		});

		TreeViewerColumn treeViewerColumn_1 = new TreeViewerColumn(treeViewer,
				SWT.NONE);
		TreeColumn trclmnNewColumn_1 = treeViewerColumn_1.getColumn();
		trclmnNewColumn_1.setWidth(180);
		trclmnNewColumn_1.setText("\u503C");
		treeViewerColumn_1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TreeNode treeNode = TreeNode.class.cast(element);
				if (treeNode.getValue() instanceof Feature) {
					return "";
				}
				if (treeNode.getValue() instanceof Param) {
					return ((Param) treeNode.getValue()).getValue();
				}
				return element.toString();
			}

		});
		this.treeViewer.setInput(config.createTreeNode());
		treeViewer.setColumnProperties(new String[] { "name", "value" });
		CellEditor[] treeViewer_editors = new CellEditor[tree.getColumnCount()];
		treeViewer_editors[0] = new TextCellEditor(tree);
		treeViewer_editors[1] = new TextCellEditor(tree);

		treeViewer.setCellEditors(treeViewer_editors);
		treeViewer.setCellModifier(new ICellModifier() {

			@Override
			public boolean canModify(Object element, String property) {
				TreeNode treeNode = TreeNode.class.cast(element);
				if (treeNode.getValue() instanceof Feature
						&& property.equals("value")) { //$NON-NLS-1$
					return false;
				}
				if(property.equals("name"))
					return false;
				return true;
			}

			@Override
			public Object getValue(Object element, String property) {
				Object value = ""; //$NON-NLS-1$
				TreeNode treeNode = TreeNode.class.cast(element);
				if (treeNode.getValue() instanceof Feature) {
					Feature feautre = Feature.class.cast(treeNode.getValue());
					if (property.equals("name")) {
						value = feautre.getName();
					}
				}
				if (treeNode.getValue() instanceof Param) {
					Param param = Param.class.cast(treeNode.getValue());
					if (property.equals("name")) {
						value = param.getName();
					} else if (property.equals("value")) {
						value = param.getValue();
					}
				}
				return value;
			}

			@Override
			public void modify(Object element, String property, Object value) {
				TreeItem item = (TreeItem) element;
				Object o = item.getData();
				TreeNode treeNode = TreeNode.class.cast(o);
				if (treeNode.getValue() instanceof Feature) {
					Feature feautre = Feature.class.cast(treeNode.getValue());
					if (property.equals("name")) {
						if ("".equals(value)) {
							MessageDialog.openInformation(null, Messages.AddFeatureDialog_INFORMATION,
									Messages.CreateFeatureDialog_FEATURE_NMAE_NOT_NULL);
						} else {
							if (!feautre.getName().equals((String) value)) {
								feautre.setName((String) value);
								dirty = true;
								firePropertyChange(PROP_DIRTY);
								treeViewer.refresh();
							}
						}
					}
				}
				if (treeNode.getValue() instanceof Param) {
					Param param = Param.class.cast(treeNode.getValue());
					if (property.equals("name")) {
						if ("".equals(value)) {
							MessageDialog.openInformation(null, Messages.PackageAppItemDialog_INFO,
									Messages.PARAMNAMEISNULL);
						} else {
							if (!param.getName().equals((String) value)) {
								param.setName((String) value);
								dirty = true;
								firePropertyChange(PROP_DIRTY);
								treeViewer.refresh();
							}
						}
					} else if (property.equals("value")) {
						if ("".equals(value)) {
							MessageDialog.openInformation(null, Messages.PackageAppItemDialog_INFO,
									Messages.PARAMNAMEISNULL);
						} else {
							if (!param.getValue().equals((String) value)) {
								param.setValue((String) value);
								dirty = true;
								firePropertyChange(PROP_DIRTY);
								treeViewer.refresh();
							}
						}
					}
				}
			}
		});

		final Section sctnNewSection_1 = formToolkit.createSection(composite_3,
				Section.TWISTIE | Section.TITLE_BAR);
		GridData gd_sctnNewSection_1 = new GridData(SWT.FILL, SWT.FILL, true,
				false, 1, 1);
		gd_sctnNewSection_1.heightHint = 73;
		sctnNewSection_1.setLayoutData(gd_sctnNewSection_1);
		formToolkit.paintBordersFor(sctnNewSection_1);
		sctnNewSection_1.setText(Messages.PERMISSIONSETTINGS);
		sctnNewSection_1.setExpanded(true);

		Composite composite_5 = formToolkit.createComposite(sctnNewSection_1,
				SWT.NONE);
		formToolkit.paintBordersFor(composite_5);
		sctnNewSection_1.setClient(composite_5);
		composite_5.setLayout(new GridLayout(3, false));

		Label lblNewLabel_2 = new Label(composite_5, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true,
				false, 3, 1));
		formToolkit.adapt(lblNewLabel_2, true, true);
		lblNewLabel_2.setText(Messages.PERMISSIONINFO);


		tableViewer_1 = new TableViewer(composite_5, SWT.BORDER
				| SWT.FULL_SELECTION);
		table_1 = tableViewer_1.getTable();
		table_1.setLinesVisible(true);
		table_1.setHeaderVisible(true);
		GridData gd_table_1 = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1,
				1);
		gd_table_1.heightHint = 250;
		table_1.setLayoutData(gd_table_1);
		formToolkit.paintBordersFor(table_1);

		TableViewerColumn tableViewerColumn_2 = new TableViewerColumn(
				tableViewer_1, SWT.NONE);
		TableColumn tblclmnNewColumn_2 = tableViewerColumn_2.getColumn();
		tblclmnNewColumn_2.setResizable(false);
		tblclmnNewColumn_2.setMoveable(false);
		tblclmnNewColumn_2.setWidth(160);
		tblclmnNewColumn_2.setText(Messages.STARTED);
		tableViewerColumn_2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Permission) element).getName();
			}

		});

		tableViewer_1.setContentProvider(new ArrayContentProvider());
		tableViewer_1.setInput(config.getPermissions());
		tableViewer_1.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				
				StructuredSelection ss = (StructuredSelection) event.getSelection();
				Permission p = (Permission) ss.getFirstElement();
				config.getPermissions().remove(p);
				tableViewer_1.setInput(config.getPermissions());
				tableViewer_3.setInput(getUnauthorized(config));
				sctnNewSection_1.setExpanded(false);
				sctnNewSection_1.setExpanded(true);
				dirty = true;
				firePropertyChange(PROP_DIRTY);

			}
		});
		table_1.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				sctnNewSection_1.setExpanded(false);
				sctnNewSection_1.setExpanded(true);				
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				sctnNewSection_1.setExpanded(false);
				sctnNewSection_1.setExpanded(true);
			}
		});

		Label lblNewLabel = new Label(composite_5, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 1, 1));
		formToolkit.adapt(lblNewLabel, true, true);
		lblNewLabel.setText(Messages.CHANGEBUTTON);

		tableViewer_3 = new TableViewer(composite_5, SWT.BORDER
				| SWT.FULL_SELECTION);
		table_3 = tableViewer_3.getTable();
		table_3.setLinesVisible(true);
		table_3.setHeaderVisible(true);
		table_3.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 1, 1));
		formToolkit.paintBordersFor(table_3);

		TableViewerColumn tableViewerColumn = new TableViewerColumn(
				tableViewer_3, SWT.NONE);
		TableColumn tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(false);
		tableColumn.setMoveable(false);
		tableColumn.setWidth(160);
		tableColumn.setText(Messages.UNSTARTED);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Permission) element).getName();
			}

		});

		tableViewer_3.setContentProvider(new ArrayContentProvider());
		tableViewer_3.setInput(getUnauthorized(config));
		tableViewer_3.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				StructuredSelection ss = (StructuredSelection) event.getSelection();
				Permission p = (Permission) ss.getFirstElement();
				config.getPermissions().add(p);
				tableViewer_1.setInput(config.getPermissions());
				tableViewer_3.setInput(getUnauthorized(config));
				sctnNewSection_1.setExpanded(false);
				sctnNewSection_1.setExpanded(true);	
				dirty = true;
				firePropertyChange(PROP_DIRTY);
			}
		});
		table_3.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				sctnNewSection_1.setExpanded(false);
				sctnNewSection_1.setExpanded(true);				
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				sctnNewSection_1.setExpanded(false);
				sctnNewSection_1.setExpanded(true);
			}
		});  

		Section sctnNewSection = formToolkit.createSection(composite_3,
				Section.TWISTIE | Section.TITLE_BAR);
		sctnNewSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false, 1, 1));
		formToolkit.paintBordersFor(sctnNewSection);
		sctnNewSection.setText(Messages.ACCESSSETTINGS);
		sctnNewSection.setExpanded(true);

		Composite composite_9 = formToolkit.createComposite(sctnNewSection,
				SWT.NONE);
		formToolkit.paintBordersFor(composite_9);
		sctnNewSection.setClient(composite_9);
		composite_9.setLayout(new GridLayout(1, false));

		Label lblNewLabel_9 = new Label(composite_9, SWT.NONE);
		lblNewLabel_9.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 1, 1));
		formToolkit.adapt(lblNewLabel_9, true, true);
		lblNewLabel_9.setText(Messages.ACCESSSINFO);

		Composite composite_2 = formToolkit.createComposite(composite_9,
				SWT.NONE);
		composite_2.setLayout(null);
		GridData gd_composite_2 = new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 1, 1);
		gd_composite_2.widthHint = 380;
		composite_2.setLayoutData(gd_composite_2);
		formToolkit.paintBordersFor(composite_2);

		final Label addLabel = formToolkit.createLabel(composite_2, "",
				SWT.NONE);
		addLabel.setImage(Activator.getImage("icons/add.png"));
		addLabel.setBounds(0, 0, 55, 26);
		addLabel.addMouseListener(new MouseAdapter() {

			public void mouseUp(MouseEvent e) {
				Access a = new Access();
				a.setOrigin("*");
				config.getAccesses().add(a);
				tableViewer_2.setInput(config.getAccesses());
				tableViewer_2.refresh();
				dirty = true;
				firePropertyChange(PROP_DIRTY);
			}
		});
		addLabel.addMouseTrackListener(new MouseTrackListener() {
			@Override
			public void mouseHover(MouseEvent e) {
				addLabel.setImage(Activator.getImage("icons/addfocus.png"));
			}

			@Override
			public void mouseExit(MouseEvent e) {
				addLabel.setImage(Activator.getImage("icons/add.png"));
			}

			@Override
			public void mouseEnter(MouseEvent e) {
				addLabel.setImage(Activator.getImage("icons/addfocus.png"));
			}
		});

		final Label deleteLabel = formToolkit.createLabel(composite_2, "",
				SWT.NONE);
		deleteLabel.setImage(Activator.getImage("icons/delete.png"));
		deleteLabel.setBounds(60, 0, 55, 26);
		deleteLabel.addMouseListener(new MouseAdapter() {

			public void mouseUp(MouseEvent e) {
				StructuredSelection ss = (StructuredSelection) tableViewer_2
						.getSelection();
				if(ss.getFirstElement() == null) {
					return;
				}
				Access a = (Access) ss.getFirstElement();
				config.getAccesses().remove(a);
				tableViewer_2.refresh();
				dirty = true;
				firePropertyChange(PROP_DIRTY);
			}
		});
		deleteLabel.addMouseTrackListener(new MouseTrackListener() {
			@Override
			public void mouseHover(MouseEvent e) {
				deleteLabel.setImage(Activator
						.getImage("icons/deletefocus.png"));
			}

			@Override
			public void mouseExit(MouseEvent e) {
				deleteLabel.setImage(Activator.getImage("icons/delete.png"));
			}

			@Override
			public void mouseEnter(MouseEvent e) {
				deleteLabel.setImage(Activator
						.getImage("icons/deletefocus.png"));
			}
		});

		tableViewer_2 = new TableViewer(composite_9, SWT.BORDER
				| SWT.FULL_SELECTION);
		table_2 = tableViewer_2.getTable();
		table_2.setLinesVisible(true);
		table_2.setHeaderVisible(true);
		GridData gd_table_2 = new GridData(SWT.CENTER, SWT.CENTER, true, false,
				1, 1);
		gd_table_2.heightHint = 120;
		table_2.setLayoutData(gd_table_2);
		formToolkit.paintBordersFor(table_2);

		TableViewerColumn tableViewerColumn_3 = new TableViewerColumn(
				tableViewer_2, SWT.NONE);
		TableColumn tblclmnNewColumn_3 = tableViewerColumn_3.getColumn();
		tblclmnNewColumn_3.setWidth(360);
		tblclmnNewColumn_3.setText(Messages.SOURCE);
		tableViewerColumn_3.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Access) element).getOrigin();
			}

		});

		tableViewer_2.setContentProvider(new ArrayContentProvider());
		tableViewer_2.setInput(config.getAccesses());
		tableViewer_2.setColumnProperties(new String[] { "origin" });
		CellEditor[] tableViewer2_editor = new CellEditor[table_2
				.getColumnCount()];
		tableViewer2_editor[0] = new TextCellEditor(table_2);

		tableViewer_2.setCellEditors(tableViewer2_editor);
		tableViewer_2.setCellModifier(new ICellModifier() {

			@Override
			public boolean canModify(Object element, String property) {

				return true;
			}

			@Override
			public Object getValue(Object element, String property) {
				Object value = ""; //$NON-NLS-1$
				Access access = Access.class.cast(element);
				if (property.equals("origin")) {
					value = access.getOrigin();
				}
				return value;
			}

			@Override
			public void modify(Object element, String property, Object value) {
				TableItem item = (TableItem) element;
				Object o = item.getData();
				Access access = Access.class.cast(o);
				if (property.equals("origin")) {
					if ("".equals(value)) {
						MessageDialog.openInformation(null, Messages.AddFeatureDialog_INFORMATION,
								Messages.PERMISSIONISNULL);
					} else {
						if (!access.getOrigin().equals((String) value)) {
							access.setOrigin((String) value);
							dirty = true;
							firePropertyChange(PROP_DIRTY);
							tableViewer_2.refresh();
						}
					}
				}
			}
		});
		scrolledComposite.setMinSize(new Point(700, 1500));
		scrolledComposite.setContent(composite_3);
	}


	@Override
	public void setFocus() {
		getSite().getShell().setFocus();
	}

	public void setValue(Config config) {
		generalSection.setValue(config);
		setPreferences(config);
		tableViewer_1.setInput(config.getPermissions());
		tableViewer_3.setInput(getUnauthorized(config));
		treeViewer.setInput(config.createTreeNode());
		tableViewer_3.refresh();
		tableViewer_1.refresh();
		treeViewer.refresh();
		this.config = config;
	}

	public void change() {
		firePropertyChange(PROP_DIRTY);
	}

	public Config getConfig() {
		return config;
	}

	public boolean hasErrorText() {
		return generalSection.hasErrorText();
	}
	
	private List<Permission> getUnauthorized(Config config) {
		List<Permission> unauthorized = new ArrayList<Permission>();
		selectedUnauthorized(config, unauthorized, "readPhoneState");
		selectedUnauthorized(config, unauthorized, "call");
		selectedUnauthorized(config, unauthorized, "sms");
		selectedUnauthorized(config, unauthorized, "camera");
		selectedUnauthorized(config, unauthorized, "record");
		selectedUnauthorized(config, unauthorized, "location");
		selectedUnauthorized(config, unauthorized, "fileSystem");
		selectedUnauthorized(config, unauthorized, "internet");
		selectedUnauthorized(config, unauthorized, "bootCompleted");
		selectedUnauthorized(config, unauthorized, "hardware");
		selectedUnauthorized(config, unauthorized, "contact");
		return unauthorized;
	}

	private void selectedUnauthorized(Config config, List<Permission> list,
			String name) {
		Permission p = new Permission();
		p.setName(name);
		if (!config.getPermissions().contains(p)) {
			list.add(p);
		}
	}

	private String getPreferenceValue(String name, Config config) {
		for (Preference p : config.getPreferences()) {
			if (p.getName().equals(name)) {
				return p.getValue();
			}
		}
		return null;
	}

	private void changePreferenceValue(String name, String value) {
		Preference preference = new Preference();
		preference.setName(name);
		preference.setValue(value);
		boolean isFind = false;
		for (Preference p : config.getPreferences()) {
			if (p.getName().equals(name)) {
				isFind = true;
				p.setValue(value);
			}
		}
		if(!isFind) 
			config.getPreferences().add(preference);
	}
	
	private void setPreferences(Config config) {
		appBackgroundText
		.setText(getPreferenceValue("appBackground", config) == null ? ""
				: getPreferenceValue("appBackground", config));
		windowBackgroundText
		.setText(getPreferenceValue("windowBackground", config) == null ? ""
				: getPreferenceValue("windowBackground", config));
		frameBackgroundText
		.setText(getPreferenceValue("frameBackgroundColor", config) == null ? ""
				: getPreferenceValue("frameBackgroundColor", config));
		if (Boolean
				.parseBoolean(getPreferenceValue("pageBounce", config) == null ? "false"
						: getPreferenceValue("pageBounce", config))) {
			pageBounceButton_ok.setSelection(true);
		} else {
			pageBounceButton_cancel.setSelection(true);
		}
		
		pageBounceButton_ok.setSelection(false);
		pageBounceButton_cancel.setSelection(false);
		if (Boolean
				.parseBoolean(getPreferenceValue("pageBounce", config) == null ? "false"
						: getPreferenceValue("pageBounce", config))) {
			pageBounceButton_ok.setSelection(true);
		} else {
			pageBounceButton_cancel.setSelection(true);
		}
		hScrollBarEnabledButton_ok.setSelection(false);
		hScrollBarEnabledButton_cancel.setSelection(false);
		if (Boolean
				.parseBoolean(getPreferenceValue("hScrollBarEnabled", config) == null ? "false"
						: getPreferenceValue("hScrollBarEnabled", config))) {
			hScrollBarEnabledButton_ok.setSelection(true);
		} else {
			hScrollBarEnabledButton_cancel.setSelection(true);
		}
		vScrollBarEnabledButton_ok.setSelection(false);
		vScrollBarEnabledButton_cancel.setSelection(false);
		if (Boolean
				.parseBoolean(getPreferenceValue("vScrollBarEnabled", config) == null ? "false"
						: getPreferenceValue("vScrollBarEnabled", config))) {
			vScrollBarEnabledButton_ok.setSelection(true);
		} else {
			vScrollBarEnabledButton_cancel.setSelection(true);
		}
		
		fullScreenButton_ok.setSelection(false);
		fullScreenButton_cancel.setSelection(false);
		if (Boolean
				.parseBoolean(getPreferenceValue("fullScreen", config) == null ? "false"
						: getPreferenceValue("fullScreen", config))) {
			fullScreenButton_ok.setSelection(true);
		} else {
			fullScreenButton_cancel.setSelection(true);
		}
		ios7Button_ok.setSelection(false);
		ios7Button_cancel.setSelection(false);
		if (Boolean
				.parseBoolean(getPreferenceValue("iOS7StatusBarAppearance", config) == null ? "false"
						: getPreferenceValue("iOS7StatusBarAppearance", config))) {
			ios7Button_ok.setSelection(true);
		} else {
			ios7Button_cancel.setSelection(true);
		}
		autoLaunchButton_ok.setSelection(false);
		autoLaunchButton_cancel.setSelection(false);
		if (Boolean
				.parseBoolean(getPreferenceValue("autoLaunch", config) == null ? "false"
						: getPreferenceValue("autoLaunch", config))) {
			autoLaunchButton_ok.setSelection(true);
		} else {
			autoLaunchButton_cancel.setSelection(true);
		}
		autoUpdateButton_ok.setSelection(false);
		autoUpdateButton_cancel.setSelection(false);
		if (Boolean
				.parseBoolean(getPreferenceValue("autoUpdate", config) == null ? "false"
						: getPreferenceValue("autoUpdate", config))) {
			autoUpdateButton_ok.setSelection(true);
		} else {
			autoUpdateButton_cancel.setSelection(true);
		}
		if (Boolean
				.parseBoolean(getPreferenceValue("smartUpdate", config) == null ? "false"
						: getPreferenceValue("smartUpdate", config))) {
			smartUpdateButton_ok.setSelection(true);
		} else {
			smartUpdateButton_cancel.setSelection(true);
		}
	}
}
