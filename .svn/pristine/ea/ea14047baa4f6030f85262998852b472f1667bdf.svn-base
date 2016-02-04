/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.common.contentassist;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.formatter.FormattingContext;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.text.edits.TextEdit;

import com.aptana.core.util.ObjectUtil;
import com.aptana.core.util.StringUtil;
import com.aptana.formatter.IScriptFormatter;
import com.aptana.formatter.IScriptFormatterFactory;
import com.aptana.formatter.ScriptFormatterManager;
import com.aptana.formatter.preferences.PreferencesLookupDelegate;
import com.aptana.parsing.lexer.IRange;
import com.aptana.parsing.lexer.Range;

public class CommonCompletionProposal implements ICommonCompletionProposal, ICompletionProposalExtension,
		ICompletionProposalExtension2, ICompletionProposalExtension3, Comparable<ICompletionProposal>
{
	protected String _replacementString;
	protected int _replacementOffset;
	protected int _replacementLength;
	protected int _cursorPosition;
	protected Image _image;
	protected String _displayString;
	protected IContextInformation _contextInformation;
	protected String _additionalProposalInformation;
	protected String _fileLocation;
	private int _hash;

	private IProject project;
	
	private Image[] _userAgentImages;
	private char[] _triggerChars;

	/** @deprecated Use _relevance instead */
	protected boolean _isDefaultSelection;
	/** @deprecated Use _relevance instead */
	private boolean _isSuggestedSelection;

	private int _relevance;

	/**
	 * CommonCompletionProposal
	 * 
	 * @param replacementString
	 * @param replacementOffset
	 * @param replacementLength
	 * @param cursorPosition
	 * @param image
	 * @param displayString
	 * @param contextInformation
	 * @param additionalProposalInfo
	 */
	public CommonCompletionProposal(String replacementString, int replacementOffset, int replacementLength,
			int cursorPosition, Image image, String displayString, IProject project, IContextInformation contextInformation,
			String additionalProposalInfo)
	{
		this._replacementString = (replacementString == null) ? StringUtil.EMPTY : replacementString;
		this._replacementOffset = replacementOffset;
		this._replacementLength = replacementLength;
		this._cursorPosition = cursorPosition;
		this._image = image;
		this._displayString = (displayString == null) ? StringUtil.EMPTY : displayString;
		this._contextInformation = contextInformation;
		this._additionalProposalInformation = additionalProposalInfo;
		this.project = project;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#apply(org.eclipse.jface.text.IDocument)
	 */
	public void apply(IDocument document)
	{
		// not called anymore
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		boolean result = false;

		if (this == obj)
		{
			result = true;
		}
		else if (obj instanceof CommonCompletionProposal)
		{
			CommonCompletionProposal that = (CommonCompletionProposal) obj;

			// @formatter:off
			result =
					ObjectUtil.areEqual(_replacementString, that._replacementString)
				&&	_replacementOffset == that._replacementOffset
				&&	_replacementLength == that._replacementLength
				&&	_cursorPosition == that._cursorPosition
				&&	ObjectUtil.areEqual(_image, that._image)
				&&	ObjectUtil.areEqual(_displayString, that._displayString)
				&&	ObjectUtil.areEqual(_contextInformation, that._contextInformation)
				&&	ObjectUtil.areEqual(_additionalProposalInformation, that._additionalProposalInformation)
				&&	ObjectUtil.areEqual(_fileLocation, that._fileLocation);
			// @formatter:on
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		if (_hash == 0)
		{
			// @formatter:off
			_hash = _hash * 31 + ((_replacementString != null) ? _replacementString.hashCode() : 0);
			_hash = _hash * 31 + _replacementOffset;
			_hash = _hash * 31 + _replacementLength;
			_hash = _hash * 31 + _cursorPosition;
			_hash = _hash * 31 + ((getImage() != null) ? getImage().hashCode() : 0);
			_hash = _hash * 31 + ((getDisplayString() != null) ? getDisplayString().hashCode() : 0);
			_hash = _hash * 31 + ((getContextInformation() != null) ? getContextInformation().hashCode() : 0);
			_hash = _hash * 31 + ((getAdditionalProposalInfo() != null) ? getAdditionalProposalInfo().hashCode() : 0);
			_hash = _hash * 31 + ((getFileLocation() != null) ? getFileLocation().hashCode() : 0);
			// @formatter:on
		}

		return _hash;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getAdditionalProposalInfo()
	 */
	public String getAdditionalProposalInfo()
	{
		return this._additionalProposalInformation;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getContextInformation()
	 */
	public IContextInformation getContextInformation()
	{
		return this._contextInformation;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getDisplayString()
	 */
	public String getDisplayString()
	{
		return this._displayString;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.contentassist.ICommonCompletionProposal#getFileLocation()
	 */
	public String getFileLocation()
	{
		return (this._fileLocation != null) ? this._fileLocation : StringUtil.EMPTY;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getImage()
	 */
	public Image getImage()
	{
		return this._image;
	}

	/**
	 * getReplaceRange
	 * 
	 * @return
	 */
	public IRange getReplaceRange()
	{
		return new Range(this._replacementOffset, this._replacementOffset + this._replacementLength - 1);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getSelection(org.eclipse.jface.text.IDocument)
	 */
	public Point getSelection(IDocument document)
	{
		return new Point(this._replacementOffset + this._cursorPosition, 0);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.contentassist.ICommonCompletionProposal#getUserAgentImages()
	 */
	public Image[] getUserAgentImages()
	{
		return this._userAgentImages;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.contentassist.ICommonCompletionProposal#isDefaultSelection()
	 * @deprecated Use getRelevance instead
	 */
	public boolean isDefaultSelection()
	{
		return this._isDefaultSelection;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.contentassist.ICommonCompletionProposal#isSuggestedSelection()
	 * @deprecated Use getRelevance instead
	 */
	public boolean isSuggestedSelection()
	{
		return this._isSuggestedSelection;
	}

	/**
	 * setLocation
	 * 
	 * @param location
	 */
	public void setFileLocation(String location)
	{
		this._fileLocation = location;
	}

	public void setProject(IProject project){
		this.project = project;
	}

	public IProject getProject() {
		return this.project;
	}
	/**
	 * setIsDefaultSelection
	 * 
	 * @param value
	 * @deprecated Use getRelevance instead
	 */
	public void setIsDefaultSelection(boolean value)
	{
		this._isDefaultSelection = value;
	}

	/**
	 * setIsSuggstedSelection
	 * 
	 * @param value
	 * @deprecated Use getRelevance instead
	 */
	public void setIsSuggestedSelection(boolean value)
	{
		this._isSuggestedSelection = value;
	}

	/**
	 * setUserAgentImages
	 * 
	 * @param images
	 */
	public void setUserAgentImages(Image[] images)
	{
		this._userAgentImages = images;
	}

	public IInformationControlCreator getInformationControlCreator()
	{
		return null;
	}

	public CharSequence getPrefixCompletionText(IDocument document, int completionOffset)
	{
		return _replacementString;
	}

	public int getPrefixCompletionStart(IDocument document, int completionOffset)
	{
		return _replacementOffset;
	}

	public void apply(ITextViewer viewer, char trigger, int stateMask, int offset)
	{
		IDocument document = viewer.getDocument();
		String prefix = getPrefix(document, offset);

		try
		{
			document.replace(offset - prefix.length(), prefix.length(), _replacementString);

			final IPath path = Path.fromOSString(this.getFileLocation());
			final IContentType contentType = Platform.getContentTypeManager().findContentTypeFor(path.lastSegment());

			if(contentType !=null && contentType.getName().equals("HTML")){
				final String type = document.getContentType(offset - prefix.length());

				if(type.equals("__js__dftl_partition_content_type")){
					final IContentType contentType1 = Platform.getContentTypeManager().findContentTypeFor(".js");

					commonProposalFormat(offset, document, prefix, contentType1);
				}
			}

			if(contentType != null && contentType.getName().equals("JavaScript"))
			{
				commonProposalFormat(offset, document, prefix, contentType);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void commonProposalFormat(int offset, IDocument document, String prefix, IContentType contentType){
		try{
			IScriptFormatterFactory factory = ScriptFormatterManager.getSelected(contentType.getId());
			final String lineDelimiter = TextUtilities.getDefaultLineDelimiter(document);
			IProject project = getProject();
			Map<String, String> preferences = factory.retrievePreferences(new PreferencesLookupDelegate(project));
			final IScriptFormatter formatter = factory.createFormatter(lineDelimiter, preferences);
			final FormattingContext context= new FormattingContext();
			final int indentationLevel = formatter.detectIndentationLevel(document, offset - prefix.length(), true, context);
//			FindReplaceDocumentAdapter adapter = new FindReplaceDocumentAdapter(document);
//			IRegion start = adapter.find(offset - prefix.length(), ">", false, true, false, true);
//			int startOffset = start.getOffset() + 2;
//			IRegion end = adapter.find(offset, "</script>", true, true, false, true);
			final TextEdit edit = formatter.format(document.get(), offset- prefix.length(), _replacementString.length(), indentationLevel, true, context, lineDelimiter);
			edit.apply(document);

			if (edit.getLength() > 0) {
				this.set_cursorPosition(edit.getLength() - 1);
				int lastLine = document.getLineOfOffset(edit.getExclusiveEnd());
				document.replace(edit.getExclusiveEnd(), document.getLineLength(lastLine), "");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/*
	 * @see
	 * org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#selected(org.eclipse.jface.text.ITextViewer,
	 * boolean)
	 */
	public void selected(ITextViewer viewer, boolean smartToggle)
	{
	}

	/*
	 * @see
	 * org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#unselected(org.eclipse.jface.text.ITextViewer)
	 */
	public void unselected(ITextViewer viewer)
	{
	}

	/*
	 * @see
	 * org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#validate(org.eclipse.jface.text.IDocument,
	 * int, org.eclipse.jface.text.DocumentEvent)
	 */
	public boolean validate(IDocument document, int offset, DocumentEvent event)
	{
		if (offset < this._replacementOffset)
		{
			return false;
		}

		int overlapIndex = getDisplayString().length() - _replacementString.length();
		overlapIndex = Math.max(0, overlapIndex);
		String endPortion = getDisplayString().substring(overlapIndex);
		boolean validated = isValidPrefix(getPrefix(document, offset), endPortion);

		if (validated && event != null)
		{
			// make sure that we change the replacement length as the document content changes
			int delta = ((event.fText == null) ? 0 : event.fText.length()) - event.fLength;
			final int newLength = Math.max(_replacementLength + delta, 0);
			_replacementLength = newLength;
		}

		return validated;
	}

	/**
	 * Returns the prefix string from the replacement-offset to the given offset. In case the given offset appears
	 * before the replacement offset, we return an empty string.
	 * 
	 * @param document
	 * @param offset
	 */
	protected String getPrefix(IDocument document, int offset)
	{
		try
		{
			int length = offset - _replacementOffset;
			if (length > 0)
				return document.get(_replacementOffset, length);
		}
		catch (BadLocationException x)
		{
		}
		return StringUtil.EMPTY;
	}

	/**
	 * Returns true if the proposal is still valid as the user types while the content assist popup is visible.
	 * 
	 * @param prefix
	 * @param displayString
	 */
	protected boolean isValidPrefix(String prefix, String displayString)
	{
		return isValidPrefix(prefix, displayString, true);
	}

	/**
	 * Returns true if the proposal is still valid as the user types while the content assist popup is visible.
	 * 
	 * @param prefix
	 * @param displayString
	 * @param ignoreCase
	 *            Do we ignore the case of the prefix during comparisons?
	 */
	protected boolean isValidPrefix(String prefix, String displayString, boolean ignoreCase)
	{
		if (prefix == null || displayString == null || prefix.length() > displayString.length())
			return false;
		String start = displayString.substring(0, prefix.length());
		if (ignoreCase)
		{
			return start.equalsIgnoreCase(prefix);
		}
		else
		{
			return start.equals(prefix);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.contentassist.ICommonCompletionProposal#getExtraInfo()
	 */
	public String getExtraInfo()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(ICompletionProposal o)
	{
		if (this == o)
		{
			return 0;
		}

		// not yet sorting on relevance
		return StringUtil.compareCaseInsensitive(this.getDisplayString(), o.getDisplayString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.contentassist.ICommonCompletionProposal#getRelevance()
	 */
	public int getRelevance()
	{
		return _relevance;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.contentassist.ICommonCompletionProposal#setRelevance(int)
	 */
	public void setRelevance(int relevance)
	{
		_relevance = relevance;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension#apply(org.eclipse.jface.text.IDocument,
	 * char, int)
	 */
	public void apply(IDocument document, char trigger, int offset)
	{
		// evidently not called anymore
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.jface.text.contentassist.ICompletionProposalExtension#isValidFor(org.eclipse.jface.text.IDocument,
	 * int)
	 */
	public boolean isValidFor(IDocument document, int offset)
	{
		// evidently not called anymore
		return false;
	}

	/**
	 * Sets the defaults set of trigger characters used to trigger insertion/completion of a proposal
	 * 
	 * @param chars
	 *            an array of characters
	 * @return
	 */
	public void setTriggerCharacters(char[] chars)
	{
		_triggerChars = chars;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension#getTriggerCharacters()
	 */
	public char[] getTriggerCharacters()
	{
		return _triggerChars;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension#getContextInformationPosition()
	 */
	public int getContextInformationPosition()
	{
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aptana.editor.common.contentassist.ICommonCompletionProposal#isTriggerEnabled(org.eclipse.jface.text.IDocument
	 * , int)
	 */
	public boolean validateTrigger(IDocument document, int offset, KeyEvent keyEvent)
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return getDisplayString();
	}

	public int get_cursorPosition() {
		return _cursorPosition;
	}

	public void set_cursorPosition(int _cursorPosition) {
		this._cursorPosition = _cursorPosition;
	}
}
