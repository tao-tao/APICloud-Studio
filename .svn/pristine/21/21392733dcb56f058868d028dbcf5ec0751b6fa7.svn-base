/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.editor.common.internal.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;

import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.ui.util.UIUtils;

public class ToggleCommentHandler extends AbstractHandler {
	private static final String NEWLINE = "\r\n";
	private static final String TYPE_HTML = "html";
	private static final String TYPE_CSS = "css";
	private static final String TYPE_JS = "js";
	private static final String TYPE_XML = "xml";
	private static final String TYPE_HTML_PHP = "__html_php";
	private static final String TYPE_HTML_SP = "__html_sp";
	private static final String TYPE_HTML_COMMENT = "__html_comment";
	private static final String TYPE_HTML_COMMENT_BEGIN = "<!--";
	private static final String TYPE_HTML_COMMENT_END = "-->";
	private static final String TYPE_CSS_M_COMMENT = "__css_multiline_comment";
	private static final String TYPE_CSS_M_COMMENT_END = "*/";
	private static final String TYPE_CSS_M_COMMENT_BEGIN = "/*";
	private static final String SINGLELINE = "//";
	private int selecedRangeOffset = 0;
	private static IDocument iDocument = null;	
	private int lineStartOffset = 0;
	private int lineEndOffset = 0;
	private int subLength = 0;

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		// TODO Auto-generated method stub
		IEditorPart activeEditor = UIUtils.getActiveEditor();
		selecedRangeOffset = -1;
		if (activeEditor instanceof AbstractThemeableEditor) {
			AbstractThemeableEditor editor = (AbstractThemeableEditor) activeEditor;

			ISelection selection = editor.getSelectionProvider().getSelection();
			iDocument = editor.getDocumentProvider().getDocument(
					editor.getEditorInput());

			ITextSelection iTextSelection = ((ITextSelection) selection);
			String contentType = editor.getContentType();
			StringBuilder buf = new StringBuilder();
			try {
				lineStartOffset = iDocument.getLineInformation(iTextSelection
						.getStartLine()).getOffset();
				lineEndOffset = iDocument.getLineInformation(iTextSelection
						.getEndLine()).getOffset()+iDocument.getLineInformation(iTextSelection
								.getEndLine()).getLength();
				ITypedRegion region = iDocument.getPartition(editor
						.getCaretOffset());
				String regonType = region.getType();
				boolean isLine = false;
				if (iTextSelection.getStartLine() == iTextSelection
						.getStartLine()) {
					isLine = true;
				}
				if (!(TYPE_HTML_PHP.equals(regonType))
						&& !(TYPE_HTML_SP.equals(regonType))) {

					if (regonType.contains(TYPE_CSS)
							|| regonType.contains(TYPE_CSS)) {
						
						if (isLine) {
							getTypeContent(buf, iTextSelection,
									TYPE_CSS);
						} else {
							getTypeContent(
									iTextSelection.getStartLine(),
									iTextSelection.getEndLine(), buf,
									iTextSelection, TYPE_CSS);
							subLength = (lineEndOffset - lineStartOffset);
						}
					} else {
						if ((contentType.contains(TYPE_HTML)
								|| contentType.contains(TYPE_XML)
								|| regonType.contains(TYPE_HTML) || regonType
									.contains(TYPE_XML))
								&& !regonType.contains(TYPE_JS)
								&& !regonType.contains(TYPE_CSS)) {
							
							if (isLine) {
								getTypeContent(buf, iTextSelection,
										TYPE_HTML);
							} else {
								getTypeContent(
										iTextSelection.getStartLine(),
										iTextSelection.getEndLine(), buf,
										iTextSelection, TYPE_HTML);
								subLength = (lineEndOffset - lineStartOffset);
							}
						} else {
							getJSContent(
									iTextSelection.getStartLine(),
									iTextSelection.getEndLine(), buf,
									iTextSelection);
						}
					}

				}
				
				if (buf.length() > 0) {
					String content = buf.toString();
					while (content.endsWith(NEWLINE)) {
						content = content.substring(0, content.length()
								- NEWLINE.length());
					}
					while (content.startsWith(NEWLINE)) {
						content = content.substring(NEWLINE.length(),
								content.length());
					}
					
					iDocument.replace(lineStartOffset, subLength, content);
					if(selecedRangeOffset > -1){
						editor.getISourceViewer().setSelectedRange(
								selecedRangeOffset, 0);
					}
				} else {
					iDocument.replace(lineStartOffset, subLength, "");
				}
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}

			// this._lineStartOffset = startInfo.getOffset();

		}
		return null;
	}

	private void getTypeContent(StringBuilder buf,
			ITextSelection iTextSelection, String type)
			throws BadLocationException {
		int offset = 0;
		if (iTextSelection.getOffset() == iDocument.getLength()) {
			offset = iDocument.getLength() - 1;
		} else {
			offset = iTextSelection.getOffset();
		}
		ITypedRegion typeRegion = iDocument.getPartition(offset);
		String text = iTextSelection.getText();
		ITypedRegion typeRegion_new = null;
		if (offset > 0) {
			typeRegion_new = iDocument.getPartition(offset - 1);
		}
		String TYPE_COMMENT = TYPE_HTML_COMMENT;
		String TYPE_COMMENT_BEGIN = TYPE_HTML_COMMENT_BEGIN;
		String TYPE_COMMENT_END = TYPE_HTML_COMMENT_END;
		if (TYPE_CSS.equals(type)) {
			TYPE_COMMENT = TYPE_CSS_M_COMMENT;
			TYPE_COMMENT_BEGIN = TYPE_CSS_M_COMMENT_BEGIN;
			TYPE_COMMENT_END = TYPE_CSS_M_COMMENT_END;
		}

		if (iTextSelection.getLength() == 0) {
			if (typeRegion != null && typeRegion.getType().equals(TYPE_COMMENT)) {
				setTypeContent(buf, typeRegion, TYPE_COMMENT_BEGIN,
						TYPE_COMMENT_END);
				return;
			} else if (typeRegion_new != null
					&& typeRegion_new.getType().equals(TYPE_COMMENT)) {
				setTypeContent(buf, typeRegion, TYPE_COMMENT_BEGIN,
						TYPE_COMMENT_END);
				return;
			}

			int lineNum = iTextSelection.getStartLine();
			IRegion lineInfo = iDocument.getLineInformation(lineNum);
			text = iDocument.get(lineInfo.getOffset(), lineInfo.getLength());
			String content = text.trim();
			if (content == null || content.length() == 0) {
				if (TYPE_HTML.equals(type)) {
					buf.append("<!---->");
				} else {
					buf.append("/**/");
				}

				subLength = 0;
				selecedRangeOffset = (iTextSelection.getOffset() + TYPE_COMMENT_BEGIN
						.length());
				return;
			}
			if (text != null && content.endsWith(TYPE_COMMENT_END)) {
				int index = text.indexOf(TYPE_COMMENT_END);
				typeRegion = iDocument.getPartition(iTextSelection.getOffset()
						- (text.length() - index));
				if ((typeRegion != null)
						&& (typeRegion.getType().equals(TYPE_COMMENT))) {

					setTypeContent(buf, typeRegion, TYPE_COMMENT_BEGIN,
							TYPE_COMMENT_END);
					selecedRangeOffset = (lineInfo.getOffset()
							+ lineInfo.getLength() + buf.toString().length() - text
							.length());
					return;
				}
			}

			String replaceText = TYPE_COMMENT_BEGIN + content
					+ TYPE_COMMENT_END;
			text = text.replace(content, replaceText);
			buf.append(text);
			subLength = lineInfo.getLength();
			lineStartOffset = lineInfo.getOffset();
		} else {
			if ((typeRegion != null)
					&& (typeRegion.getType().equals(TYPE_COMMENT))) {
				setTypeContent(buf, typeRegion, TYPE_COMMENT_BEGIN,
						TYPE_COMMENT_END);
			} else {
				buf.append(TYPE_COMMENT_BEGIN);
				buf.append(text);
				buf.append(TYPE_COMMENT_END);
				subLength = iTextSelection.getLength();
				lineStartOffset = iTextSelection.getOffset();
			}
		}
	}

	private void getTypeContent(int startNum, int endNum,
			StringBuilder buf, ITextSelection iTextSelection, String type)
			throws BadLocationException {
		ITypedRegion typeRegion = iDocument.getPartition(iTextSelection
				.getOffset());
		ITypedRegion typeRegion_new = null;
		if (iTextSelection.getOffset() > 0) {
			typeRegion_new = iDocument
					.getPartition(iTextSelection.getOffset() - 1);
		}

		String TYPE_COMMENT = TYPE_HTML_COMMENT;
		String TYPE_COMMENT_BEGIN = TYPE_HTML_COMMENT_BEGIN;
		String TYPE_COMMENT_END = TYPE_HTML_COMMENT_END;
		if (TYPE_CSS.equals(type)) {
			TYPE_COMMENT = TYPE_CSS_M_COMMENT;
			TYPE_COMMENT_BEGIN = TYPE_CSS_M_COMMENT_BEGIN;
			TYPE_COMMENT_END = TYPE_CSS_M_COMMENT_END;
		}

		String content;
		ITypedRegion this_typeRegion = null;
		if (typeRegion != null && typeRegion.getType().equals(TYPE_COMMENT)) {
			this_typeRegion = typeRegion;
		} else if (typeRegion_new != null
				&& typeRegion_new.getType().equals(TYPE_COMMENT)) {
			this_typeRegion = typeRegion_new;
		}
		if (this_typeRegion != null) {
			content = iDocument.get(this_typeRegion.getOffset(),
					this_typeRegion.getLength());
			content = content.replace(TYPE_COMMENT_BEGIN, "");
			content = content.replace(TYPE_COMMENT_END, "");
			buf.append(content);
			lineStartOffset = this_typeRegion.getOffset();
			lineEndOffset = (lineStartOffset + this_typeRegion.getLength());
		} else {
			for (int num = startNum; num < endNum + 1; num++) {
				IRegion information = iDocument.getLineInformation(num);
				String lineText = iDocument.get(information.getOffset(),
						information.getLength());
				if (num == startNum) {
					String text = lineText.trim();
					String replaceText = TYPE_COMMENT_BEGIN + text;
					lineText = lineText.replace(text, replaceText);
				}
				if (num == endNum) {
					buf.append(lineText);
					buf.append(TYPE_COMMENT_END);
				} else {
					buf.append(lineText).append(NEWLINE);
				}
			}
		}
	}

	private void setTypeContent(StringBuilder buf, ITypedRegion region,
			String begin, String end) throws BadLocationException {
		String selectText = iDocument.get(region.getOffset(),
				region.getLength());
		selectText = selectText.substring(begin.length());
		selectText = selectText
				.substring(0, selectText.length() - end.length());

		buf.append(selectText);
		subLength = region.getLength();
		lineStartOffset = region.getOffset();
	}

	private void getJSContent(int startNum, int endNum,
			StringBuilder buf, ITextSelection iTextSelection)
			throws BadLocationException {
		
		boolean isSingle = true;
		for(int num = startNum;num < endNum + 1;num++){
			IRegion information = iDocument.getLineInformation(num);
			String text = iDocument.get(information.getOffset(), information.getLength());
			if (text != null && text.length() > 0) {
				text = text.trim();
				if (!(text.startsWith(SINGLELINE))) {
					isSingle = false;
					break;
				}
			}
		}
		for (int num = startNum;num < endNum + 1;num++) {
			IRegion information = iDocument.getLineInformation(num);
			if ((information == null) || (information.getLength() < 0) || information.getOffset() < 0){
				continue;
			}
			String lineText = iDocument.get(information.getOffset(), information.getLength());
			
			if ((lineText == null || lineText.length() == 0) && (num == endNum + 1)
					&& (startNum == endNum)) {
				buf.append(SINGLELINE);
				lineStartOffset = iTextSelection.getOffset();
				subLength = 0;
				selecedRangeOffset = (lineStartOffset + SINGLELINE.length());
				return;
			}
			
			if (isSingle) {
				lineText = lineText.replaceFirst(SINGLELINE, "");
				if (lineText.startsWith(" ")){
					lineText = "  " + lineText;
				}	
			} else {
				if (lineText.startsWith("  ")) {
					lineText = lineText.replaceFirst("  ", SINGLELINE);
				} else {
					lineText = SINGLELINE + lineText;
				}
			}
			if (num == endNum + 1) {
				buf.append(lineText);
			} else {
				buf.append(lineText).append(NEWLINE);
			}
		}
		
		subLength = (lineEndOffset - lineStartOffset);
	}

}
