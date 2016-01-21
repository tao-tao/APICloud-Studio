/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.ui.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;

import com.apicloud.commons.model.Config;
import com.apicloud.commons.util.XMLUtil;
import com.apicloud.navigator.dialogs.Messages;

public class APICloudMapConfigEditor extends MultiPageEditorPart {
	private ConfigEditor configEditor;
	private SourceEditor sourceEditor;
	
	/**
	 * 判定页面是否编辑过
	 */
	@Override
	public boolean isDirty() {
		boolean flag = false;
		if (this.sourceEditor.isDirty()) {
			flag = this.sourceEditor.isDirty();
		} else if (this.configEditor != null && this.configEditor.isDirty()) {
			flag = this.configEditor.isDirty();
		}
		return flag;
	}

	protected IFile getInputFile() {
		return (IFile) getEditorInput().getAdapter(IFile.class);
	}
	
	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		setPartName(input.getName());
	}
	
	@Override
	protected void createPages() {
		try {
			createConfigEditor();
			createSourceEditor();
		} catch (PartInitException e) {
			ErrorDialog.openError(getSite().getShell(), "ERROR", e.getMessage(), e.getStatus()); //$NON-NLS-1$
		}catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private void createConfigEditor() throws CoreException {
		if(getInputFile() != null) {
			Config config = Config.loadXml(getInputFile().getContents());
			this.configEditor = new ConfigEditor(this, config);
			int pageIndex = this.addPage(this.configEditor, getEditorInput());
			setPageText(pageIndex, "\u7F16\u8F91\u5668");
		}
	}

	private void createSourceEditor() throws PartInitException {
		this.sourceEditor = new SourceEditor();
		int pageIndex = this.addPage(this.sourceEditor, getEditorInput());
		setPageText(pageIndex, "\u6E90\u7801");
	}
	
	@Override
	protected void pageChange(int newPageIndex) {
		if(this.configEditor == null) {
			return;
		}
		try {
			super.pageChange(newPageIndex);
			if (getActiveEditor().equals(this.configEditor)) {
				doPageChangeFromSourceToView();
			} else {
				doPageChangeFromViewToSource();
			}
		} catch (Exception ex) {
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		if(configEditor.hasErrorText()) {
			return ;
		}
		try {
			// 当前活动页面为源码编辑器
			if (getActiveEditor() instanceof com.aptana.editor.xml.XMLEditor) {
				if(!doPageChangeFromSourceToView()){
					MessageDialog.openError(getSite().getShell(), "Error", Messages.CONFIGERROR);
					return ;
				}
			} else {
				doPageChangeFromViewToSource();
			}
			savesource();
			this.sourceEditor.doSave(monitor);
			this.configEditor.setDirty(false);
			firePropertyChange(PROP_DIRTY);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private boolean doPageChangeFromSourceToView() throws Exception {
		return doPageChangeFromXmlToView();
	}

	private void doPageChangeFromViewToSource() throws Exception {
		doPageChangeFromViewToXml();
	}
	
	private void savesource(){
		String xmlText = this.sourceEditor.getDocumentProvider().getDocument(this.sourceEditor.getEditorInput()).get();
		try {
			xmlText = XMLUtil.formatXml(xmlText);
			this.sourceEditor.getDocumentProvider().getDocument(this.sourceEditor.getEditorInput()).set(xmlText);
		} catch (Exception e) {
		}
	}
	private boolean doPageChangeFromXmlToView() throws Exception {
		String xmlText = this.sourceEditor.getDocumentProvider().getDocument(this.sourceEditor.getEditorInput()).get();
		Config config = Config.loadXml(xmlText);
		if(config == null || !config.check()) {
			return false;
		}
		if (!this.configEditor.getConfig().equals(config)) {
			this.configEditor.setValue(config);
		}
		return true;

	}

	private void doPageChangeFromViewToXml() throws Exception {
		String content = Config.getDocumentContent(this.configEditor.getConfig());
		content = XMLUtil.formatXml(content);
		String source = this.sourceEditor.getDocumentProvider().getDocument(this.sourceEditor.getEditorInput()).get();
		if (!source.equals(content)) {
			this.sourceEditor.getDocumentProvider().getDocument(this.sourceEditor.getEditorInput()).set(content);
		}
	}

	public IEditorPart getActiveInnerEditor() {
		return getActiveEditor();
	}
	
	@Override
	public void doSaveAs() {
		
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
}
