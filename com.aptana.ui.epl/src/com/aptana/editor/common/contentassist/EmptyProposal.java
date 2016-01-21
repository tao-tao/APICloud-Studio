package com.aptana.editor.common.contentassist;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;


/**
 * The empty proposal displayed if there is nothing else to show.
 * 
 * @since 3.2
 */

public class EmptyProposal implements ICompletionProposal, ICompletionProposalExtension,
ICompletionProposalExtension4{


	String fDisplayString;
	int fOffset;

	/*
	 * @see ICompletionProposal#apply(IDocument)
	 */
	public void apply(IDocument document)
	{
	}

	/*
	 * @see ICompletionProposal#getSelection(IDocument)
	 */
	public Point getSelection(IDocument document)
	{
		return new Point(fOffset, 0);
	}

	/*
	 * @see ICompletionProposal#getContextInformation()
	 */
	public IContextInformation getContextInformation()
	{
		return null;
	}

	/*
	 * @see ICompletionProposal#getImage()
	 */
	public Image getImage()
	{
		return null;
	}

	/*
	 * @see ICompletionProposal#getDisplayString()
	 */
	public String getDisplayString()
	{
		return fDisplayString;
	}

	/*
	 * @see ICompletionProposal#getAdditionalProposalInfo()
	 */
	public String getAdditionalProposalInfo()
	{
		return null;
	}

	/*
	 * @see
	 * org.eclipse.jface.text.contentassist.ICompletionProposalExtension#apply(org.eclipse.jface.text.IDocument,
	 * char, int)
	 */
	public void apply(IDocument document, char trigger, int offset)
	{
	}

	/*
	 * @see
	 * org.eclipse.jface.text.contentassist.ICompletionProposalExtension#isValidFor(org.eclipse.jface.text.IDocument
	 * , int)
	 */
	public boolean isValidFor(IDocument document, int offset)
	{
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension#getTriggerCharacters()
	 */
	public char[] getTriggerCharacters()
	{
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension#getContextInformationPosition()
	 */
	public int getContextInformationPosition()
	{
		return -1;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension#isAutoInsertable()
	 */
	public boolean isAutoInsertable()
	{
		return false;
	}

}
