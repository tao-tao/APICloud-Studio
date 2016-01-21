/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.common.contentassist;

/***********************************************************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: IBM Corporation - initial API and implementation
 **********************************************************************************************************************/

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.contentassist.IContentAssistSubjectControl;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IEditingSupport;
import org.eclipse.jface.text.IEditingSupportRegistry;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

import com.aptana.core.logging.IdeLog;
import com.aptana.ui.epl.UIEplPlugin;

/**
 * This class is used to present proposals to the user. If additional information exists for a proposal, then selecting
 * that proposal will result in the information being displayed in a secondary window.
 * 
 * @see org.eclipse.jface.text.contentassist.ICompletionProposal
 */
public class CompletionProposalPopup implements IContentAssistListener
{

	/**
	 * Set to <code>true</code> to use a Table with SWT.VIRTUAL. 
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=90321 More details see also:
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=98585#c36
	 * 
	 * @since 3.1
	 */
	private static final boolean USE_VIRTUAL = !("motif".equals(SWT.getPlatform()));//$NON-NLS-1$
	/**
	 * PROPOSAL_ITEM_HEIGHT
	 */
	public static final int PROPOSAL_ITEMS_VISIBLE = 10;

	public static final int DESCRIPTION_PANE_WIDTH = 180;

	private int proposalCurrentPageNumber = 1;

	/**
	 * MAX_PROPOSAL_COLUMN_WIDTH
	 */
	public static final int MAX_PROPOSAL_COLUMN_WIDTH = 500;

	/**
	 * MAX_LOCATION_COLUMN_WIDTH
	 */
	public static final int MAX_LOCATION_COLUMN_WIDTH = 200;

	/**
	 * MIN_PROPOSAL_COLUMN_WIDTH
	 */
	public static final int MIN_PROPOSAL_COLUMN_WIDTH = 180;

	/**
	 * ProposalSelectionListener
	 * 
	 * @author Ingo Muschenetz
	 */
	private final class ProposalSelectionListener implements KeyListener
	{
		/**
		 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
		 */
		public void keyPressed(KeyEvent e)
		{
			if (!Helper.okToUse(fProposalShell))
			{
				return;
			}

			if (e.character == 0 && e.keyCode == SWT.MOD1)
			{
                // http://dev.eclipse.org/bugs/show_bug.cgi?id=34754
			    int index = fProposalTable.getSelectionIndex();
			    if (index >= 0)
	                    {
			        selectProposal(index, true, true);
		             }
				// else
				// {
				// fProposalTable.setTopIndex(0);
				// }
			}
		}

		/**
		 * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
		 */
		public void keyReleased(KeyEvent e)
		{
			if (!Helper.okToUse(fProposalShell))
			{
				return;
			}

			if (e.character == 0 && e.keyCode == SWT.MOD1)
			{
				// http://dev.eclipse.org/bugs/show_bug.cgi?id=34754
			        int index = fProposalTable.getSelectionIndex();
				if (index >= 0)
				{
			            selectProposal(index, false, true);
		                }
				// else
				// {
				// fProposalTable.setTopIndex(0);
				// }
			}
		}
	}
	
        /** The associated text viewer. */
	private ITextViewer fViewer;
        /** The associated code assistant. */
	private ContentAssistant fContentAssistant;
        /** The used additional info controller. */
	private AdditionalInfoController fAdditionalInfoController;
        /** The closing strategy for this completion proposal popup. */
	private PopupCloser fPopupCloser = new PopupCloser();
        /** The popup shell. */
	private Shell fProposalShell;
        /** The proposal table. */
	private Table fProposalTable;
	private Composite detailProposalComposite;
        /** Indicates whether a completion proposal is being inserted. */
	private boolean fInserting = false;
        /** The key listener to control navigation. */
	private ProposalSelectionListener fKeyListener;
        /** List of document events used for filtering proposals. */
	private List<DocumentEvent> fDocumentEvents = new ArrayList<DocumentEvent>();
        /** Listener filling the document event queue. */
	private IDocumentListener fDocumentListener;
        /** Reentrance count for filtered proposals. */
	private long fInvocationCounter = 0;
        /** The filter list of proposals. */
	private ICompletionProposal[] fFilteredProposals;
        /** The computed list of proposals. */
	private ICompletionProposal[] fComputedProposals;
        /** The offset for which the proposals have been computed. */
	private int fInvocationOffset;
	/** The offset for which the computed proposals have been filtered. */
	private int fFilterOffset;
	/** The key last pressed to trigger activation * */
	private char fActivationKey;

	/** Do we insert the selected proposal on tab? * */
	private boolean _insertOnTab;

        /**
	 * The most recently selected proposal.
	 * 
	 * @since 3.0
	 */
	private ICompletionProposal fLastProposal;
	/**
	 * The code assist subject control. This replaces <code>fViewer</code>
	 * 
	 * @since 3.0
	 */
	private IContentAssistSubjectControl fContentAssistSubjectControl;
	/**
	 * The code assist subject control adapter. This replaces <code>fViewer</code>
	 * 
	 * @since 3.0
	 */
	private ContentAssistSubjectControlAdapter fContentAssistSubjectControlAdapter;
	/**
	 * Remembers the size for this completion proposal popup.
	 * 
	 * @since 3.0
	 */
	private Point fSize;
	/**
	 * Editor helper that communicates that the completion proposal popup may have focus while the 'logical focus' is
	 * still with the editor.
	 * 
	 * @since 3.1
	 */
	private IEditingSupport fFocusHelper;
	/**
	 * Set to true by {@link #computeFilteredProposals(int, DocumentEvent)} if the returned proposals are a subset of
	 * {@link #fFilteredProposals}, <code>false</code> if not.
	 * 
	 * @since 3.1
	 */
	private boolean fIsFilteredSubset;

	private IEclipsePreferences projectScopeNode;

	private IEclipsePreferences instanceScopeNode;

	Color bgColor;
	Color fgColor;

	private ICompletionProposal selectedProposal = null;
	private String header;
	private String bufferString = "";

	private Browser descriptionPanel;
	private Browser defaultDescriptionPanel;
	

	/**
	 * Creates a new completion proposal popup for the given elements.
	 * 
	 * @param contentAssistant
	 *            the code assistant feeding this popup
	 * @param viewer
	 *            the viewer on top of which this popup appears
	 * @param infoController
	 *            the information control collaborating with this popup
	 * @since 2.0
	 */
	public CompletionProposalPopup(ContentAssistant contentAssistant, ITextViewer viewer,
			AdditionalInfoController infoController)
	{
		fContentAssistant = contentAssistant;
		fViewer = viewer;
		fAdditionalInfoController = infoController;
		fContentAssistSubjectControlAdapter = new ContentAssistSubjectControlAdapter(fViewer);
		defaultDescriptionPanel = new Browser(new Composite(new Shell(), SWT.NORMAL), SWT.NORMAL);
		createProposalSelector();
	}

	/**
	 * Creates a new completion proposal popup for the given elements.
	 * 
	 * @param contentAssistant
	 *            the code assistant feeding this popup
	 * @param contentAssistSubjectControl
	 *            the code assist subject control on top of which this popup appears
	 * @param infoController
	 *            the information control collaborating with this popup
	 * @since 3.0
	 */
	public CompletionProposalPopup(ContentAssistant contentAssistant,
			IContentAssistSubjectControl contentAssistSubjectControl, AdditionalInfoController infoController)
	{
		fContentAssistant = contentAssistant;
		fContentAssistSubjectControl = contentAssistSubjectControl;
		fAdditionalInfoController = infoController;
		fContentAssistSubjectControlAdapter = new ContentAssistSubjectControlAdapter(fContentAssistSubjectControl);
		defaultDescriptionPanel = new Browser(new Composite(new Shell(), SWT.NORMAL), SWT.NORMAL);
		createProposalSelector();
	}

	/**
	 * Computes and presents completion proposals. The flag indicates whether this call has be made out of an auto
	 * activation context.
	 * 
	 * @param autoActivated
	 *            <code>true</code> if auto activation context
	 * @return an error message or <code>null</code> in case of no error
	 */
	public String showProposals(final boolean autoActivated)
	{
		if (fKeyListener == null)
		{
			fKeyListener = new ProposalSelectionListener();
		}

		final Control control = fContentAssistSubjectControlAdapter.getControl();

		if (control != null && !control.isDisposed())
		{

            // add the listener before computing the proposals so we don't move the caret
			// when the user types fast.
			fContentAssistSubjectControlAdapter.addKeyListener(fKeyListener);

			BusyIndicator.showWhile(control.getDisplay(), new Runnable()
			{
				public void run()
				{
					fInvocationOffset = fContentAssistSubjectControlAdapter.getSelectedRange().x;
					fFilterOffset = fInvocationOffset;
					fComputedProposals = computeProposals(fInvocationOffset, autoActivated);
					IDocument doc = fContentAssistSubjectControlAdapter.getDocument();
					DocumentEvent initial = new DocumentEvent(doc, fInvocationOffset, 0,"");
					ICompletionProposal[]	computedProposals = filterProposals(fComputedProposals, doc,fInvocationOffset, initial);

					int count = (computedProposals == null) ? 0: computedProposals.length;
					
					// If we don't have any proposals, and we've manually asked for proposals, show "no proposals"
					if (!autoActivated && count == 0)
					{
						computedProposals = createNoProposal();
						count = computedProposals.length;
					}

					if (count == 0)
					{
						hide();
					}
					else if (count == 1 && !autoActivated && fComputedProposals.length >= 1 && canAutoInsert(fComputedProposals[0]))
					{
						insertProposal(fComputedProposals[0], (char) 0, 0, fInvocationOffset);
						hide();
					}
					else
					{
						createPopup(computedProposals);
					}
				}
			});
		}

		return getErrorMessage();
	}

	/**
	 * Create the "no proposals" proposal
	 * 
	 * @return
	 */
	private ICompletionProposal[] createNoProposal()
	{
		fEmptyProposal.fOffset = fFilterOffset;
		fEmptyProposal.fDisplayString = JFaceTextMessages.
				getString("CompletionProposalPopup.no_proposals");  //$NON-NLS-1$
		modifySelection(-1, -1); // deselect everything
		return new ICompletionProposal[] { fEmptyProposal };
	}
	
	/**
	 * Returns the completion proposal available at the given offset of the viewer's document. Delegates the work to the
	 * code assistant.
	 * 
	 * @param offset
	 *            the offset
	 * @param autoActivated
	 * @return the completion proposals available at this offset
	 */
	private ICompletionProposal[] computeProposals(int offset, boolean autoActivated)
	{
		if (fContentAssistSubjectControl != null)
		{
			return fContentAssistant.computeCompletionProposals(fContentAssistSubjectControl, offset, fActivationKey);
		}
	    IContentAssistProcessor processor = fContentAssistant.getProcessor(fViewer, offset);
	    if (processor == null) {
	    	 return null;
	    }
	    if (processor instanceof ICommonContentAssistProcessor) {
	         ICommonContentAssistProcessor commonProcessor = (ICommonContentAssistProcessor)processor;
	         return commonProcessor.computeCompletionProposals(fViewer, offset, fActivationKey, autoActivated);
	    } else {
	         return processor.computeCompletionProposals(fViewer, offset);
	    }
	}

	/**
	 * Returns the error message.
	 * 
	 * @return the error message
	 */
	private String getErrorMessage()
	{
		return fContentAssistant.getErrorMessage();
	}

	/**
	 * Creates the proposal selector.
	 */
	private void createProposalSelector()
	{
		Control control = fContentAssistSubjectControlAdapter.getControl();
		if (Helper.okToUse(fProposalShell))
		{
			// Custom code to force colors again in case theme changed...
			// Not sure why we don't set background for all WS here
			if (!"carbon".equals(SWT.getPlatform())) //$NON-NLS-1$
			{
				fProposalShell.setBackground(getForegroundColor(control));
			}

			Color c = getBackgroundColor(control);
			fProposalTable.setBackground(c);

			c = getForegroundColor(control);
			fProposalTable.setForeground(c);
			return;
		}

		fProposalShell = new Shell(control.getShell(), SWT.ON_TOP | SWT.RESIZE);
		fProposalShell.setFont(JFaceResources.getDefaultFont());
		fProposalShell.setAlpha(244);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		fProposalShell.setLayout(layout);
		if (USE_VIRTUAL)
		{
			fProposalTable = new Table(fProposalShell, SWT.V_SCROLL | SWT.VIRTUAL |  SWT.BORDER | SWT.RESIZE | SWT.SMOOTH);

			Listener listener = new Listener()
			{
				public void handleEvent(Event event)
				{
					handleSetData(event);
				}
			};
			fProposalTable.addListener(SWT.SetData, listener);
		}
		else
		{
			fProposalTable = new Table(fProposalShell, SWT.V_SCROLL | SWT.BORDER | SWT.RESIZE | SWT.SMOOTH);
		}

		fProposalShell.addControlListener(new ControlAdapter()
		{
			public void controlResized(ControlEvent e)
			{
                                TableColumn[] columns = fProposalTable.getColumns();
				columns[0].setWidth(20);
				columns[1].setWidth(180);
			}
		});

		_insertOnTab = true; // store.getBoolean(IPreferenceConstants.INSERT_ON_TAB);

                // Here we add custom columns
		new TableColumn(fProposalTable, SWT.LEFT);

		for (int i = 0; i < fContentAssistant.getUserAgentColumnCount(); i++)
		{
			TableColumn tc = new TableColumn(fProposalTable, SWT.LEFT);
			tc.setWidth(20);
		}

		new TableColumn(fProposalTable, SWT.LEFT);
                // end custom columns

		fProposalTable.setLocation(0, 0);
		if (fAdditionalInfoController != null)
		{
			fAdditionalInfoController.setSizeConstraints(40, 20, true, false);
		}

                // Custom code: We set margins to 1 so we get a border

		fProposalShell.addControlListener(new ControlListener()
		{

			public void controlMoved(ControlEvent e)
			{
			}

			public void controlResized(ControlEvent e)
			{
				if (fAdditionalInfoController != null)
				{
                                        // reset the cached resize constraints
					fAdditionalInfoController.setSizeConstraints(40, 20, true, false);
					fAdditionalInfoController.hideInformationControl();
				        fAdditionalInfoController.handleTableSelectionChanged();
				}

				fSize = fProposalShell.getSize();
			}
		});
                detailProposalComposite = new Composite(fProposalShell, SWT.NORMAL);
                // Custom code: not sure why we don't set background for all WS here
		if (!("carbon".equals(SWT.getPlatform())))
		{
			fProposalShell.setBackground(getForegroundColor(control));
		}

		Color c = getBackgroundColor(control);
		fProposalTable.setBackground(c);

		c = getForegroundColor(control);
		fProposalTable.setForeground(c);

		createDescriptionPanel();
	}	
	
	public String show(boolean isAutoActivated, final boolean reContentAssist, final String per)
	{
		final boolean autoActivated = (isAutoActivated) || (reContentAssist);

		Control control = fContentAssistSubjectControlAdapter.getControl();

		if ((control != null) && (!(control.isDisposed())))
		{

			BusyIndicator.showWhile(control.getDisplay(), new Runnable()
			{
				public void run()
				{
					IDocument doc = fContentAssistSubjectControlAdapter.getDocument();
					DocumentEvent initial = new DocumentEvent(doc, fInvocationOffset, 0,
							"");
					ICompletionProposal[]  computedProposals = filterProposals1(
							fComputedProposals, doc,
							fInvocationOffset, initial, per);
					int count = (computedProposals == null) ? 0
							: computedProposals.length;

					if ((!(autoActivated)) && (count == 0))
					{
						computedProposals = createNoProposal();
						count = computedProposals.length;
					}

					if (count == 0)
					{
						hide();
					}
					else
					{
						createPopup(computedProposals);
					}
				}
			});
		}

		return getErrorMessage();
	}
	
	
	public String showProposals1(boolean isAutoActivated, final boolean reContentAssist, final String per, final int fInvocationOffset)
	{
		final boolean autoActivated = (isAutoActivated) || (reContentAssist);
		if (fKeyListener == null)
		{
			fKeyListener = new ProposalSelectionListener();
		}

		Control control = fContentAssistSubjectControlAdapter.getControl();

		BusyIndicator.showWhile(control.getDisplay(), new Runnable()
		{
			public void run()
			{
				fFilterOffset = fInvocationOffset;
				fComputedProposals = computeProposals(
						fInvocationOffset, autoActivated);
				IDocument doc = fContentAssistSubjectControlAdapter.getDocument();
				DocumentEvent initial = new DocumentEvent(doc, fInvocationOffset, 0,
						"");
				ICompletionProposal[]  computedProposals = filterProposals1(
						fComputedProposals, doc,
						fInvocationOffset, initial, per);
				int count = (computedProposals == null) ? 0
						: computedProposals.length;

				if ((!(autoActivated)) && (count == 0))
				{
					computedProposals = createNoProposal();
					count = computedProposals.length;
				}

				if (count == 0)
				{
					hide();
				}

				else
				{
					createPopup(computedProposals);
				}
			}

		});


		return getErrorMessage();
	}

	/**
	 * Returns the background color to use.
	 * 
	 * @param control
	 *            the control to get the display from
	 * @return the background color
	 * @since 3.2
	 */
	private Color getBackgroundColor(Control control)
	{
		if ((bgColor == null) || (bgColor.isDisposed()))
		{
			bgColor = new Color(fProposalShell.getDisplay(), 238, 247, 236);
			
		}
		return bgColor;
	}
	/**
	 * Returns the foreground color to use.
	 * 
	 * @param control
	 *            the control to get the display from
	 * @return the foreground color
	 * @since 3.2
	 */
	private Color getForegroundColor(Control control)
	{
		if ((fgColor == null) || (fgColor.isDisposed()))
		{
			fgColor = new Color(fProposalShell.getDisplay(), 51, 51, 51);
		}
		return fgColor;
	}

	private char fLastKeyPressed;

	/**
	 * The (reusable) empty proposal.
	 * 
	 * @since 3.2
	 */
	private final EmptyProposal fEmptyProposal = new EmptyProposal();
	/**
	 * The text for the empty proposal, or <code>null</code> to use the default text.
	 * 
	 * @since 3.2
	 */
	private String fEmptyMessage = null;

	private void handleSetData(Event event)
	{
		TableItem item = (TableItem) event.item;
		int index = fProposalTable.indexOf(item);
		if(index >= 0 && index < fFilteredProposals.length) 
		{
		ICompletionProposal current = fFilteredProposals[index];
			item.setData("isAlt", "false");

			item.setImage(0, current.getImage());
			item.setText(1, current.getDisplayString());
			boolean isItalics = false;

			if (!(isItalics))
			{
				setDefaultStyle(item);
			}
			item.setData(current);
		}
	}

	/**
	 * Resizes the table to match the internal items
	 */
	private void resizeTable(int objectColumn, int locationColumn)
	{
		// Try/catch is fix for LH where we are strangely getting an ArrayIndexOutOfBounds exception
		// Not entirely sure how it's happening: https://aptana.lighthouseapp.com/projects/35272/tickets/2017
		try
		{
			fProposalTable.setRedraw(false);
			int height = fProposalTable.getItemHeight() * PROPOSAL_ITEMS_VISIBLE;

			fProposalTable.setLayoutData(GridDataFactory.fillDefaults().hint(SWT.DEFAULT, height).grab(true, true).
					create());

			fProposalTable.getColumn(0).setWidth(20);
			fProposalTable.getColumn(0).setAlignment(SWT.CENTER);

			fProposalTable.getColumn(1).setWidth(180);
			fProposalTable.setRedraw(true);
			fProposalShell.pack(true);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			IdeLog.logError(UIEplPlugin.getDefault(),
					JFaceTextMessages.getString("CompletionProposalPopup.Error_Resizing_Popup"), e); //$NON-NLS-1$
		}
	}

	/**
	 * Gets the interior width of the CA proposal table
	 * 
	 * @return
	 */
	private int getTableWidth()
	{
		Rectangle area = fProposalShell.getClientArea();
		Point preferredSize = fProposalTable.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		int width = area.width - 2 * fProposalTable.getBorderWidth();
		if (preferredSize.y > area.height + fProposalTable.getHeaderHeight())
		{
			// Subtract the scrollbar width from the total column width
			// if a vertical scrollbar will be required
			if (fProposalTable.getVerticalBar() != null)
			{
				Point vBarSize = fProposalTable.getVerticalBar().getSize();
				width -= vBarSize.x;
			}
		}

		// We subtract an extra 2 pixels because it seems this calculation overestimates
		// the table width a tiny bit. This might be a calculatable value.
		width -= 2;

		return width;
	}

	/**
	 * Returns the proposal selected in the proposal selector.
	 * 
	 * @return the selected proposal
	 * @since 2.0
	 */
	private ICompletionProposal getSelectedProposal()
	{
		if (fProposalTable == null || fProposalTable.isDisposed())
		{
			return null;
		}
		int i = fProposalTable.getSelectionIndex() + 
				(proposalCurrentPageNumber - 1) * PROPOSAL_ITEMS_VISIBLE;
		if (fFilteredProposals == null || i < 0 || i >= fFilteredProposals.length)
		{
			return null;
		}
		return fFilteredProposals[i];
	}

	/**
	 * Takes the selected proposal and applies it.
	 * 
	 * @param stateMask
	 *            the state mask
	 * @since 2.1
	 */
	private boolean selectProposalWithMask(int stateMask)
	{
		ICompletionProposal p = getSelectedProposal();
		hide();
		if (p == null)
		{
			return false;
		}

		insertProposal(p, '\0', stateMask, fContentAssistSubjectControlAdapter.getSelectedRange().x);
		return true;
	}

	/**
	 * Applies the given proposal at the given offset. The given character is the one that triggered the insertion of
	 * this proposal.
	 * 
	 * @param p
	 *            the completion proposal
	 * @param trigger
	 *            the trigger character
	 * @param stateMask
	 *            the state mask
	 * @param offset
	 *            the offset
	 * @since 2.1
	 */
	private void insertProposal(ICompletionProposal p, char trigger, int stateMask, final int offset)
	{

		fInserting = true;
		IRewriteTarget target = null;
		IEditingSupport helper = new IEditingSupport()
		{

			public boolean isOriginator(DocumentEvent event, IRegion focus)
			{
				return focus.getOffset() <= offset && focus.getOffset() + focus.getLength() >= offset;
			}

			public boolean ownsFocusShell()
			{
				return false;
			}

		};
		
		try
		{

			IDocument document = fContentAssistSubjectControlAdapter.getDocument();

			if (fViewer instanceof ITextViewerExtension)
			{
				ITextViewerExtension extension = (ITextViewerExtension) fViewer;
				target = extension.getRewriteTarget();
			}

			if (target != null)
			{
				target.beginCompoundChange();
			}

			if (fViewer instanceof IEditingSupportRegistry)
			{
				IEditingSupportRegistry registry = (IEditingSupportRegistry) fViewer;
				registry.register(helper);
			}

			if (p instanceof ICompletionProposalExtension2 && fViewer != null)
			{
				ICompletionProposalExtension2 e = (ICompletionProposalExtension2) p;
				e.apply(fViewer, trigger, stateMask, offset);
			}
			else if (p instanceof ICompletionProposalExtension)
			{
				ICompletionProposalExtension e = (ICompletionProposalExtension) p;
				e.apply(document, trigger, offset);
			}
			else
			{
				p.apply(document);
			}

			Point selection = p.getSelection(document);
			if (selection != null)
			{											
					fContentAssistSubjectControlAdapter.setSelectedRange(selection.x, selection.y);
				fContentAssistSubjectControlAdapter.revealRange(selection.x, selection.y);
			}

			IContextInformation info = p.getContextInformation();
			if (info != null)
			{

				int contextInformationOffset;
				if (p instanceof ICompletionProposalExtension)
				{
					ICompletionProposalExtension e = (ICompletionProposalExtension) p;
					contextInformationOffset = e.getContextInformationPosition();
				}
				else
				{
					if (selection == null)
					{
						selection = fContentAssistSubjectControlAdapter.getSelectedRange();
					}
					contextInformationOffset = selection.x + selection.y;
				}

				fContentAssistant.showContextInformation(info, contextInformationOffset);
			}
			else
			{
				fContentAssistant.showContextInformation(null, -1);
			}

		}
		finally
		{
			if (target != null)
			{
				target.endCompoundChange();
			}

			if (fViewer instanceof IEditingSupportRegistry)
			{
				IEditingSupportRegistry registry = (IEditingSupportRegistry) fViewer;
				registry.unregister(helper);
			}
			fInserting = false;
		}
	}

	/**
	 * Returns whether this popup has the focus.
	 * 
	 * @return <code>true</code> if the popup has the focus
	 */
	public boolean hasFocus()
	{
		if (fPopupCloser != null && fPopupCloser.isAdditionalInfoInFocus())
		{
			// TISTUD-913
			return true;
		}
		if (Helper.okToUse(fProposalShell))
		{
			return (fProposalShell.isFocusControl() || fProposalTable.isFocusControl()
					|| descriptionPanel.isFocusControl() || detailProposalComposite.isFocusControl());
		}

		return false;
	}

	/**
	 * Hides this popup.
	 */
	public void hide()
	{
		bufferString = "";
		selectedProposal = null;
		fLastKeyPressed = '\0';
		unregister();

		if (fViewer instanceof IEditingSupportRegistry)
		{
			IEditingSupportRegistry registry = (IEditingSupportRegistry) fViewer;
			registry.unregister(fFocusHelper);
		}

		if (Helper.okToUse(fProposalShell))
		{
			fContentAssistant.removeContentAssistListener(this, ContentAssistant.PROPOSAL_SELECTOR);
			// TISTUD-913: moved the 'fPopupCloser.uninstall();' to disposePopup()
			if (fAdditionalInfoController != null)
			{
				fAdditionalInfoController.disposeInformationControl();
			}

			if (Helper.okToUse(fProposalTable))
			{
				fProposalTable.removeAll();
				fProposalTable.update();
			}
                        // TISTUD-1550: Call to dispose, instead of fProposalShell.setVisible(false);
			fProposalShell.setVisible(false);
		}
		}

	/**
	 * Disposes the popup
	 */
	public void disposePopup()
	{
		if ((fProposalShell != null) && (!(fProposalShell.isDisposed())))
		{
			fProposalShell.dispose();
		}

		if ((defaultDescriptionPanel != null) && (!(defaultDescriptionPanel.isDisposed())))
		{
			defaultDescriptionPanel.dispose();
		}

		if ((descriptionPanel != null) && (!(descriptionPanel.isDisposed())))
		{
			descriptionPanel.dispose();
		}

		try
		{
			if (projectScopeNode != null)
			{
				projectScopeNode = null;
			}
			if (instanceScopeNode != null)
			{
				instanceScopeNode = null;
			}
		}
		catch (IllegalStateException e)
		{
			// ignores
		}
		if (fPopupCloser != null)
		{
			// TISTUD-913
			fPopupCloser.uninstall();
		}
	}

	/**
	 * Unregister this completion proposal popup.
	 * 
	 * @since 3.0
	 */
	private void unregister()
	{
		if (fDocumentListener != null)
		{
			IDocument document = fContentAssistSubjectControlAdapter.getDocument();
			if (document != null)
			{
				document.removeDocumentListener(fDocumentListener);
			}
			fDocumentListener = null;
		}
		fDocumentEvents.clear();

		if (fKeyListener != null && Helper.okToUse(fContentAssistSubjectControlAdapter.getControl()))
		{
			fContentAssistSubjectControlAdapter.removeKeyListener(fKeyListener);
			fKeyListener = null;
		}

		if (fLastProposal != null)
		{
			if (fLastProposal instanceof ICompletionProposalExtension2 && fViewer != null)
			{
				ICompletionProposalExtension2 extension = (ICompletionProposalExtension2) fLastProposal;
				extension.unselected(fViewer);
			}
			fLastProposal = null;
		}

		fFilteredProposals = null;
		fComputedProposals = null;

		fContentAssistant.possibleCompletionsClosed();
	}

	/**
	 * Returns whether this popup is active. It is active if the proposal selector is visible.
	 * 
	 * @return <code>true</code> if this popup is active
	 */
	public boolean isActive()
	{
		return Helper.okToUse(fProposalShell) && fProposalShell.isVisible();
	}

	/**
	 * Initializes the proposal selector with these given proposals.
	 * 
	 * @param proposals
	 *            the proposals
	 * @param isFilteredSubset
	 *            if <code>true</code>, the proposal table is not cleared, but the proposals that are not in the passed
	 *            array are removed from the displayed set
	 */
	private void setProposals(ICompletionProposal[] proposals, boolean isFilteredSubset)
	{
		ICompletionProposal[] oldProposals = fFilteredProposals;
		ICompletionProposal oldProposal = getSelectedProposal(); // may trigger filtering and a reentrant call to
                // setProposals()
		if (oldProposals != fFilteredProposals) 
			return;
		if (!(Helper.okToUse(fProposalTable)))
			return;
		if ((oldProposal instanceof ICompletionProposalExtension2) && (fViewer != null))
		{
			((ICompletionProposalExtension2) oldProposal).unselected(fViewer);
		}
		if (proposals == null)
		{
			proposals = new ICompletionProposal[0];
		}

		fFilteredProposals = proposals;
		int length = proposals.length;
		TableItem[] items;
		if (USE_VIRTUAL)
		{
			fProposalTable.clearAll();
			fProposalTable.setItemCount(length);
			items = fProposalTable.getItems();
			for (int i = 0; i < items.length; ++i)
			{
				setItem(items, i);
			}
		}
		else
		{
			fProposalTable.setRedraw(false);
			fProposalTable.setItemCount(length);
			items = fProposalTable.getItems();
			for (int i = 0; i < items.length; i++)
			{
				TableItem item = items[i];
				ICompletionProposal proposal = proposals[i];
				item.setData("isAlt", "false");
					
				item.setImage(0, proposal.getImage());
				item.setText(1, proposal.getDisplayString());
				setDefaultStyle(item);

				item.setData(proposal);
			}
			fProposalTable.setRedraw(true);
		}

		// Custom code for modifying selection/size
		int defaultIndex = -1;
		int suggestedIndex = -1;

		// select the first proposal
		if (proposals.length > 0)
		{
			defaultIndex = 0;
			suggestedIndex = 0;
		}
		modifySelection(defaultIndex, suggestedIndex);

		if (!(isFilteredSubset))
		{
			resizeTable(0, 0);
		}

	}
	


		
	private void setDefaultStyle(TableItem item)
	{
		Color c = getForegroundColor(fContentAssistSubjectControlAdapter.getControl());
		Font f = JFaceResources.getDefaultFont();
		item.setFont(2, f);
		item.setForeground(2, c);
	}


	protected void modifySelection(int defaultIndex, int suggestedIndex)
	{
		// IM changed this to deselect the table if there is no default selection
		if (defaultIndex != -1)
		{
			selectProposal(defaultIndex, false, true);
		}
		else if (suggestedIndex != -1)
		{
			fProposalTable.deselectAll();
			setScroll(suggestedIndex);
		}
		else
		{
			if (!(Helper.okToUse(fProposalTable)))
				return;
			if ((fLastKeyPressed == '\b') && (defaultIndex == -1))
			{
				hide();
			}
			else
			{
				fProposalTable.setTopIndex(0);
				fProposalTable.deselectAll();
			}
		}
	}

	/**
	 * Returns the graphical location at which this popup should be made visible.
	 * 
	 * @param width
	 * @param height
	 * @return the location of this popup
	 */
	/*
	 * private Point getLocation(int width, int height) { // get character index into the source for the current caret
	 * position int caret = fContentAssistSubjectControlAdapter.getCaretOffset(); // get coordinate of caret position
	 * Point clientPoint = fContentAssistSubjectControlAdapter.getLocationAtOffset(caret); // make sure clientPoint is
	 * within the visible client area if (clientPoint.x < 0) clientPoint.x = 0; if (clientPoint.y < 0) clientPoint.y =
	 * 0; // convert coordinate to screen coordinates Point screenPoint =
	 * fContentAssistSubjectControlAdapter.getControl().toDisplay(clientPoint); Rectangle screenRect =
	 * Display.getCurrent().getClientArea(); int lineHeight = fContentAssistSubjectControlAdapter.getLineHeight(); int x
	 * = screenPoint.x; int y = screenPoint.y + lineHeight + 2; if (x + width > screenRect.width) { // hangs over the
	 * right side of the screen if (screenPoint.x - width > 0) { x = screenPoint.x - width; } else { // doesn't fit on
	 * the left either // This should only happen on a really small screen or with a large popup } } if (y + height >
	 * screenRect.height) { // doesn't fit below current line // TODO: '7' is a magic number. We really need a reliable
	 * way to get the true height/width of this widget int yOffset = height + lineHeight + 7; if (screenPoint.y -
	 * yOffset > 0) { y = screenPoint.y - yOffset; } else { // doesn't fit above current line either // This should only
	 * happen on a really small screen or with a large popup } } return new Point(x, y); }
	 */

	/**
	 * Returns the graphical location at which this popup should be made visible.
	 * 
	 * @param proposalBox
	 * @param displayBounds
	 * @param caretLocation
	 * @param lineHeight
	 * @return the location of this popup
	 */
	public static Point computeLocation(Rectangle proposalBox, Rectangle displayBounds, Point caretLocation,
			int lineHeight)
	{

		if (caretLocation.y + proposalBox.height > displayBounds.height + displayBounds.y)
		{
			return new Point(caretLocation.x, caretLocation.y - (lineHeight + proposalBox.height));
		}
		else
		{
			return new Point(caretLocation.x, caretLocation.y + lineHeight);
		}
	}

	/**
	 * Returns the size of this completion proposal popup.
	 * 
	 * @return a Point containing the size
	 * @since 3.0
	 */
	Point getSize()
	{
		return fSize;
	}

	/**
	 * Displays this popup and install the additional info controller, so that additional info is displayed when a
	 * proposal is selected and additional info is available.
	 */
	private void displayProposals()
	{

		if (!Helper.okToUse(fProposalShell) || !Helper.okToUse(fProposalTable))
		{
			return;
		}

		if (fContentAssistant.addContentAssistListener(this, ContentAssistant.PROPOSAL_SELECTOR))
		{

			if (fDocumentListener == null)
			{
				fDocumentListener = new IDocumentListener()
				{
					public void documentAboutToBeChanged(DocumentEvent event)
					{
						if (fInserting)
							return;
						fDocumentEvents.add(event);
					}

					public void documentChanged(DocumentEvent event)
					{
						if (fInserting)
							return;
						filterProposals();
					}
				};
			}
			IDocument document = fContentAssistSubjectControlAdapter.getDocument();
			if (document != null)
			{
				document.addDocumentListener(fDocumentListener);
			}
			if (fFocusHelper == null)
			{
				fFocusHelper = new IEditingSupport()
				{

					public boolean isOriginator(DocumentEvent event, IRegion focus)
					{
						return false; // this helper just covers the focus change to the proposal
						// shell, no remote
						// editions
					}

					public boolean ownsFocusShell()
					{
						return true;
					}

				};
			}
			if (fViewer instanceof IEditingSupportRegistry)
			{
				IEditingSupportRegistry registry = (IEditingSupportRegistry) fViewer;
				registry.register(fFocusHelper);
			}

			/*
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=52646 on GTK, setVisible and such may run the event loop
			 * (see also https://bugs.eclipse.org/bugs/show_bug.cgi?id=47511) Since the user may have already canceled
			 * the popup or selected an entry (ESC or RETURN), we have to double check whether the table is still
			 * okToUse. See comments below
			 */
			if (!(fProposalShell.isVisible())) // may run event loop on GTK
			{
				fProposalShell.setVisible(true);
			}

			if ((!(fContentAssistSubjectControlAdapter.supportsVerifyKeyListener()))
					&& (Helper.okToUse(fProposalShell)))
			{
				fProposalShell.setFocus();
			}

			if (fAdditionalInfoController == null)
				return;
			Helper.okToUse(fProposalTable);
		}
		else
		{
			hide();
		}
	}

	/**
	 * @see IContentAssistListener#verifyKey(VerifyEvent)
	 */
	public boolean verifyKey(VerifyEvent e)
	{
		return verifyKeyEvent(e);
	}

	private boolean verifyKeyEvent(KeyEvent e)
	{
		if (!(Helper.okToUse(fProposalShell)))
		{
			return true;
		}

		char key = e.character;
		fLastKeyPressed = e.character;
		boolean smartToggle = false;
		if (key == 0)
		{
			int newSelection = fProposalTable.getSelectionIndex();
			int visibleRows = (fProposalTable.getSize().y / fProposalTable.getItemHeight()) - 1;
			switch (e.keyCode)
			{

				case SWT.ARROW_LEFT:
				case SWT.ARROW_RIGHT:
					// filterProposals();
					hide();
					return true;

				case SWT.ARROW_UP:
					newSelection -= 1;
					if (newSelection < 0)
					{
						newSelection = fProposalTable.getItemCount() - 1;
					}
					break;

				case SWT.ARROW_DOWN:
					newSelection += 1;
					if (newSelection > fProposalTable.getItemCount() - 1)
					{
						newSelection = 0;
					}
					break;

				case SWT.PAGE_DOWN:
					newSelection += visibleRows;
					if (newSelection >= fProposalTable.getItemCount())
					{
						newSelection = fProposalTable.getItemCount() - 1;
					}
					break;

				case SWT.PAGE_UP:
					newSelection -= visibleRows;
					if (newSelection < 0)
					{
						newSelection = 0;
					}
					break;
					
				case SWT.HOME:
					hide();
					return true;
					// newSelection= 0;
					// break;
					//
				case SWT.END:
					hide();
					return true;
					// newSelection= fProposalTable.getItemCount() - 1;
					// break;

				default:
					if (e.keyCode != SWT.CAPS_LOCK && e.keyCode != SWT.MOD1 && e.keyCode != SWT.MOD2
							&& e.keyCode != SWT.MOD3 && e.keyCode != SWT.MOD4)
					{
						hide();
					}
					return true;
			}

			selectProposal(newSelection, smartToggle, false);

			e.doit = false;
			return false;
		}

		if(key <= 'Z' && key >= 'A' || key <= 'z' && key >= 'a' || key == '-') {
			bufferString = bufferString + key;
			show(true, true, String.valueOf(key));
			return true;
		}
		if( key == '.') {
			hide();
			bufferString = bufferString + key;

			fInvocationOffset += header.length();
			showProposals1(true, true, ".", fInvocationOffset);
			return true;
		}
		
		if(e.keyCode == 8) {
				hide();
				return true;
		}

		switch (key)
		{
			case '\t':
				if (!(_insertOnTab))
				{
					e.doit = true;
					hide();
					break;
				}

				if (selectProposalWithMask(e.stateMask))
				{
					e.doit = false;
					break;
				}

				return true;
			case SWT.ESC: // Esc
				e.doit = false;
				hide();
				break;
			case SWT.LF:
			case SWT.CR:
				if (selectProposalWithMask(e.stateMask))
				{
					e.doit = false;
					break;
				}

				return true;
			default:
				ICompletionProposal p = getSelectedProposal();
				if (!(p instanceof ICompletionProposalExtension))
					return true;
				ICompletionProposalExtension t = (ICompletionProposalExtension) p;
				char[] triggers = t.getTriggerCharacters();
				if (!(contains(triggers, key)))
				{
					return true;
				}
				boolean triggerEnabled = true;
				if (p instanceof ICommonCompletionProposal)
				{
					triggerEnabled = ((ICommonCompletionProposal) p).validateTrigger(
							fContentAssistSubjectControlAdapter.getDocument(), fFilterOffset, e);
				}
				if (!(triggerEnabled))
					return true;
				e.doit = false;
				insertProposal(p, key, e.stateMask, fContentAssistSubjectControlAdapter.getSelectedRange().x);
		}

		return true;
	}

	private void setItem(TableItem[] items, int i)
	{
		TableItem item = items[i];
		ICompletionProposal proposal = fFilteredProposals[i];
		item.setData("isAlt", "false");
			
		item.setImage(0, proposal.getImage());
		item.setText(1, proposal.getDisplayString());
		boolean isItalics = false;
		if (!(isItalics))
		{
			setDefaultStyle(item);
	    }
		item.setData(proposal);

	}

	/**
	 * Selects the entry with the given index in the proposal selector and feeds the selection to the additional info
	 * controller.
	 * 
	 * @param index
	 *            the index in the list
	 * @param smartToggle
	 *            <code>true</code> if the smart toggle key has been pressed
	 * @param autoScroll
	 *            Do we scroll the item into view at the middle of the list
	 * @since 2.1
	 */
	private void selectProposal(int index, boolean smartToggle, boolean autoScroll)
	{
		if (fFilteredProposals == null)
		{
			return;
		}

		ICompletionProposal oldProposal = getSelectedProposal();
		if (oldProposal instanceof ICompletionProposalExtension2 && fViewer != null)
		{
			((ICompletionProposalExtension2) oldProposal).unselected(fViewer);
		}

		ICompletionProposal proposal = fFilteredProposals[index];
		if (proposal instanceof ICompletionProposalExtension2 && fViewer != null)
		{
			((ICompletionProposalExtension2) proposal).selected(fViewer, smartToggle);
		}

		fLastProposal = proposal;
		fProposalTable.setSelection(index);

		if (autoScroll)
		{
			setScroll(index);
		}
		fProposalTable.showSelection();

		if (fAdditionalInfoController != null)
		{
		    fAdditionalInfoController.handleTableSelectionChanged();
	    }
	}

	/**
	 * Sets the postition of the table
	 * 
	 * @param index
	 */
	private void setScroll(int index)
	{
		int topIndex = index - 4 > 0 ? index - 4 : 0;
		fProposalTable.setTopIndex(topIndex);
	}

	/**
	 * Returns whether the given character is contained in the given array of characters.
	 * 
	 * @param characters
	 *            the list of characters
	 * @param c
	 *            the character to look for in the list
	 * @return <code>true</code> if character belongs to the list
	 * @since 2.0
	 */
	private boolean contains(char[] characters, char c)
	{

		if (characters == null)
		{
			return false;
		}

		for (int i = 0; i < characters.length; i++)
		{
			if (c == characters[i])
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * @see org.eclipse.jface.text.IEventConsumer#processEvent(org.eclipse.swt.events.VerifyEvent)
	 */
	public void processEvent(VerifyEvent e)
	{
	}

	/**
	 * Filters the displayed proposal based on the given cursor position and the offset of the original invocation of
	 * the code assistant.
	 */
	private void filterProposals()
	{
		++fInvocationCounter;
		final Control control = fContentAssistSubjectControlAdapter.getControl();
		control.getDisplay().asyncExec(new Runnable()
		{
			final long fCounter = 0;

			public void run()
			{
				if (fCounter != fInvocationCounter)
				{
					return;
				}

				if (control.isDisposed())
				{
					return;
				}

				int offset = fContentAssistSubjectControlAdapter.getSelectedRange().x;
				if (fInvocationOffset > offset)
				{
					hide();
				}
				ICompletionProposal[] proposals = null;
				try
				{
					if (offset > -1)
					{
						DocumentEvent event = TextUtilities.mergeProcessedDocumentEvents(fDocumentEvents);
						proposals = computeFilteredProposals(offset, event);
					}
				}
				catch (BadLocationException x)
				{
				}
				finally
				{
					fDocumentEvents.clear();
				}
				fFilterOffset = offset;

				if (proposals != null && proposals.length > 0)
				{
					fContentAssistant.promoteKeyListener();
					setProposals(proposals,fIsFilteredSubset);
				}
				else
				{
					hide();
				}
			}
		});
	}

	/**
	 * Computes the subset of already computed proposals that are still valid for the given offset.
	 * 
	 * @param offset
	 *            the offset
	 * @param event
	 *            the merged document event
	 * @return the set of filtered proposals
	 * @since 3.0
	 */
	private ICompletionProposal[] computeFilteredProposals(int offset, DocumentEvent event)
	{

		if (offset == fInvocationOffset && event == null)
		{
			fIsFilteredSubset = false;
			return fComputedProposals;
		}
		IDocument document = fContentAssistSubjectControlAdapter.getDocument();
		// this does go through the array twice (once to figure out if it's okay to use the else case, and the second
		// time to actual filter the proposals, but it is what the original logic suggests
		if ((offset < fInvocationOffset) || ((event != null) && (event.fText.startsWith("("))))
		{
			fIsFilteredSubset = false;
			fInvocationOffset = offset;
			fComputedProposals = computeProposals(fInvocationOffset, false);
			fComputedProposals = filterProposals(fComputedProposals, document, fInvocationOffset, event);
			return fComputedProposals;
		}

		ICompletionProposal[] proposals;
		if (offset < fFilterOffset)
		{
			proposals = fComputedProposals;
			fIsFilteredSubset = false;
		}
		else
		{
			proposals = fFilteredProposals;
			fIsFilteredSubset = true;
		}

		if (proposals == null)
		{
			fIsFilteredSubset = false;
			return null;
		}

		for (int i = 0; i < proposals.length; i++)
		{
			ICompletionProposal proposal = proposals[i];
			if ((proposal instanceof ICompletionProposalExtension2)
					|| (proposal instanceof ICompletionProposalExtension))
			{
				continue;
			}
			fIsFilteredSubset = false;
			fInvocationOffset = offset;
			fComputedProposals = computeProposals(fInvocationOffset, false);

			return fComputedProposals;
		}

		ICompletionProposal[] filtered = filterProposals(proposals, document, offset, event);
		return filtered;
	}

	/**
	 * Filters the list of proposals to only those that are valid in the current context of the document event
	 * 
	 * @param proposals
	 * @param document
	 * @param offset
	 * @param event
	 * @return
	 */
	private ICompletionProposal[] filterProposals(ICompletionProposal[] proposals, IDocument document, int offset,
			DocumentEvent event)
	{
		int length = proposals == null ? 0 : proposals.length;
		List<ICompletionProposal> filtered = new ArrayList<ICompletionProposal>(length);
		for (int i = 0; i < length; i++)
		{
			ICompletionProposal proposal = proposals[i];

			if (proposal instanceof ICompletionProposalExtension2)
			{
				ICompletionProposalExtension2 p = (ICompletionProposalExtension2) proposal;

				if (p.validate(document, offset, event))
				{
					filtered.add(proposal);
				}

			}
			else if (proposal instanceof ICompletionProposalExtension)
			{
				ICompletionProposalExtension p = (ICompletionProposalExtension) proposal;

				if (p.isValidFor(document, offset))
				{
					filtered.add(proposal);
				}
			}
		}


		return filtered.toArray(new ICompletionProposal[filtered.size()]);
	}
	


	private ICompletionProposal[] filterProposals1(ICompletionProposal[] proposals, IDocument document, int offset,
			DocumentEvent event, String per)
	{
	        String type = "";
		try
		{
			if ("__html_tag".equals(document.getPartition(offset).getType()))
				type = "html";
			if ("__css___dftl_partition_content_type".equals(document.getPartition(offset).getType()))
				type = "css";
			if ("__js__dftl_partition_content_type".equals(document.getPartition(offset).getType()))
				type = "js";
			if ("__dftl_partition_content_type".equals(document.getPartition(offset).getType()))
				type = "default";
		}
		
		catch (BadLocationException e1)
		{
			e1.printStackTrace();
		}
		if(per.equals("bs")) {
			header = filterPrefix1(proposals, document, offset, type);
			header += bufferString;

			if(header.length() == 1 ) {
//				header = "";
			}else if(header.length() == 0) {
				System.out.println("hhaha");
				hide();
				return null;
			}	
		}else {
			header = filterPrefix1(proposals, document, offset, type);
			header += bufferString;
		}
	

		List<ICompletionProposal> list = new ArrayList<ICompletionProposal>();
		
		for(ICompletionProposal p : proposals) {

			if(p.getDisplayString().toLowerCase().startsWith(header.toLowerCase())) {
				list.add(p);
			}
		}
		Collections.sort(list, new Comparator<ICompletionProposal>()
		{

			public int compare(ICompletionProposal o1, ICompletionProposal o2)
			{
				return o1.getDisplayString().compareTo(o2.getDisplayString());
			}
			
		});
		return list.toArray(new ICompletionProposal[list.size()]);
	}

	private String filterPrefix1(ICompletionProposal[] proposals, IDocument document, int offset, String type)
	{
		String prefix = "";
		if (proposals.length < 1)
		{
			return prefix;
		}
		try
		{
			prefix = document.get(offset-2, 1);
			int num = 1;
			while(true){
				prefix = document.get(offset-num, 1);
				if(type.equals("html") && prefix.equals(".")|| prefix.equals(">") || prefix.equals("\t") || prefix.equals(" ")|| prefix.equals("\n")
						|| prefix.equals("\r") || prefix.equals(";")|| prefix.equals("}")) {
					break;
				}
				if(type.equals("js") && prefix.equals(".")|| prefix.equals(">") || prefix.equals("\t") || prefix.equals(" ")|| prefix.equals("\n")
						|| prefix.equals("\r") || prefix.equals(";")|| prefix.equals("}")) {
					break;
				}
				if(type.equals("css") &&  prefix.equals(">") || prefix.equals("\t") || prefix.equals(" ")|| prefix.equals("\n")
						|| prefix.equals("\r") || prefix.equals(";")|| prefix.equals("}")) {
					break;
				}
				if(type.equals("default") && prefix.equals(".")|| prefix.equals(">") || prefix.equals("\t") || prefix.equals(" ")|| prefix.equals("\n")
						|| prefix.equals("\r") || prefix.equals(";")|| prefix.equals("}")) {
					break;
				}
				if(offset == num) {
					return "";
				}
				num++;
			}
			prefix = document.get(offset-num+1, num-1).trim();
			if(prefix.contains("<") || prefix.contains("\"") ) {
				prefix = prefix.substring(1);
				return prefix;
			}
			return prefix;
		}
		catch (BadLocationException e)
		{
			return prefix;
		}
	}

	public void setFocus()
	{
		if (!(Helper.okToUse(fProposalShell)))
			return;
		fProposalShell.setFocus();
	}

	/**
	 * Returns <code>true</code> if <code>proposal</code> should be auto-inserted, <code>false</code> otherwise.
	 * 
	 * @param proposal
	 *            the single proposal that might be automatically inserted
	 * @return <code>true</code> if <code>proposal</code> can be inserted automatically, <code>false</code> otherwise
	 * @since 3.1
	 */
	private boolean canAutoInsert(ICompletionProposal proposal)
	{
		if (fContentAssistant.isAutoInserting())
		{
			if (proposal instanceof ICompletionProposalExtension4)
			{
				ICompletionProposalExtension4 ext = (ICompletionProposalExtension4) proposal;
				return ext.isAutoInsertable();
			}
			return true; // default behavior before ICompletionProposalExtension4 was introduced
		}
		return false;
	}

	/**
	 * Completes the common prefix of all proposals directly in the code. If no common prefix can be found, the proposal
	 * popup is shown.
	 * 
	 * @return an error message if completion failed.
	 * @since 3.0
	 */
	public String incrementalComplete()
	{
		if (Helper.okToUse(fProposalShell) && fFilteredProposals != null)
		{
			completeCommonPrefix();
		}
		else
		{
			final Control control = fContentAssistSubjectControlAdapter.getControl();

			if (fKeyListener == null)
			{
				fKeyListener = new ProposalSelectionListener();
			}

			if (!Helper.okToUse(fProposalShell) && !control.isDisposed())
			{
				fContentAssistSubjectControlAdapter.addKeyListener(fKeyListener);
			}

			BusyIndicator.showWhile(control.getDisplay(), new Runnable()
			{
				public void run()
				{
					fInvocationOffset = fContentAssistSubjectControlAdapter.getSelectedRange().x;
					fFilterOffset = fInvocationOffset;
					fFilteredProposals = computeProposals(fInvocationOffset, false);

					int count = (fFilteredProposals == null) ? 0: fFilteredProposals.length;
					if (count == 0)
					{
						// IM turned off for the moment, as it's annoying more than helpful to beep.
						// control.getDisplay().beep();
						hide();
					}
					else if ((count == 1)&& (canAutoInsert(fFilteredProposals[0])))
					{
						insertProposal(fFilteredProposals[0],'\0', 0, fInvocationOffset);
					}
					else if (completeCommonPrefix())
					{
						hide();
					}
					else
					{
						fComputedProposals = fFilteredProposals;
						createPopup(fComputedProposals);
					}
				}
			});
		}

		return getErrorMessage();
	}

	/**
	 * Calls the necessary methods to popup the content assist method. This was moved to one single method to unify the
	 * path to show the window and to provide an easy way to time the operations needed to populate and create the
	 * widget.
	 */
	private void createPopup(ICompletionProposal[] computedProposals)
	{
		createProposalSelector();
		setProposals(computedProposals, false);
		fContentAssistant.addToLayout(this, fProposalShell, 0, fContentAssistant.getSelectionOffset());
		displayProposals();
	}



	public Browser createDescriptionPanel()
	{
		descriptionPanel = defaultDescriptionPanel;
	    if (descriptionPanel == null || descriptionPanel.isDisposed()) {
	    	descriptionPanel = new Browser(detailProposalComposite, SWT.NORMAL);
	    }
	    if (detailProposalComposite == null) {
	        detailProposalComposite = new Composite(fProposalShell, SWT.NORMAL);
	    }
	    descriptionPanel.setParent(detailProposalComposite);
	    URL templateUrl = UIEplPlugin.getDefault().getBundle().getResource("template/template.html");
	    URL fileURL = null;
	    try {
	    	fileURL = FileLocator.toFileURL(templateUrl);
	    } catch (Exception e) {
	    	
	    }
	    String urlString = fileURL.toString();
	    descriptionPanel.setUrl(urlString);
		descriptionPanel.addProgressListener(new BrowserProgressAdapter());
		return descriptionPanel;
	}


	/**
	 * Acts upon <code>fFilteredProposals</code>: if there is just one valid proposal, it is inserted, otherwise, the
	 * common prefix of all proposals is inserted into the document. If there is no common prefix, nothing happens and
	 * <code>false</code> is returned.
	 * 
	 * @return <code>true</code> if a single proposal was inserted and the selector can be closed, <code>false</code> if
	 *         more than once choice remain
	 * @since 3.0
	 */
	private boolean completeCommonPrefix()
	{

		// 0: insert single proposals
		if (fFilteredProposals.length == 1)
		{
			if (canAutoInsert(fFilteredProposals[0]))
			{
				insertProposal(fFilteredProposals[0], '\0', 0, fFilterOffset);
				return true;
			}
			return false;
		}

		// 1: extract pre- and postfix from all remaining proposals
		IDocument document = fContentAssistSubjectControlAdapter.getDocument();

		// contains the common postfix in the case that there are any proposals matching our LHS
		StringBuffer rightCasePostfix = null;
		List<ICompletionProposal> rightCase = new ArrayList<ICompletionProposal>();

		// whether to check for non-case compatible matches. This is initially true, and stays so
		// as long as there are i) no case-sensitive matches and ii) all proposals share the same
		// (although not corresponding with the document contents) common prefix.
		boolean checkWrongCase = true;
		// the prefix of all case insensitive matches. This differs from the document
		// contents and will be replaced.
		CharSequence wrongCasePrefix = null;
		int wrongCasePrefixStart = 0;
		// contains the common postfix of all case-insensitive matches
		StringBuffer wrongCasePostfix = null;
		List<ICompletionProposal> wrongCase = new ArrayList<ICompletionProposal>();

		for (int i = 0; i < fFilteredProposals.length; i++)
		{
			ICompletionProposal proposal = fFilteredProposals[i];
			CharSequence insertion = getPrefixCompletion(proposal);
			int start = getPrefixCompletionOffset(proposal);
			try
			{
				int prefixLength = fFilterOffset - start;
				int relativeCompletionOffset = Math.min(insertion.length(), prefixLength);
				String prefix = document.get(start, prefixLength);
				if (insertion.toString().startsWith(prefix))
				{
					checkWrongCase = false;
					rightCase.add(proposal);
					CharSequence newPostfix = insertion.subSequence(relativeCompletionOffset, insertion.length());
					if (rightCasePostfix == null)
					{
						rightCasePostfix = new StringBuffer(newPostfix.toString());
						break;
					}

					truncatePostfix(rightCasePostfix, newPostfix);
					break;
				}

				if (checkWrongCase)
				{
					CharSequence newPrefix = insertion.subSequence(0, relativeCompletionOffset);
					if (isPrefixCompatible(wrongCasePrefix, wrongCasePrefixStart, newPrefix, start, document))
					{
						wrongCasePrefix = newPrefix;
						wrongCasePrefixStart = start;
						CharSequence newPostfix = insertion.subSequence(relativeCompletionOffset, insertion.length());
						if (wrongCasePostfix == null)
						{
							wrongCasePostfix = new StringBuffer(newPostfix.toString());
						}
						else
						{
							truncatePostfix(wrongCasePostfix, newPostfix);
						}
						wrongCase.add(proposal);
					}
					else
					{
						checkWrongCase = false;
					}
				}
			}
			catch (BadLocationException e2)
			{
				// bail out silently
				return false;
			}

			if (rightCasePostfix != null && rightCasePostfix.length() == 0 && rightCase.size() > 1)
			{
				return false;
			}
		}

		// 2: replace single proposals
		ICompletionProposal proposal;
		if (rightCase.size() == 1)
		{
			proposal = (ICompletionProposal) rightCase.get(0);
			if (canAutoInsert(proposal))
			{
				insertProposal(proposal, '\0', 0, fInvocationOffset);

				return true;
			}
			return false;
		}
		if (checkWrongCase && wrongCase.size() == 1)
		{
			proposal = (ICompletionProposal) wrongCase.get(0);
			if (canAutoInsert(proposal))
			{
				insertProposal(proposal, '\0', 0, fInvocationOffset);
				return true;
			}
			return false;
		}

		// 3: replace post- / prefixes

		CharSequence prefix;
		if (checkWrongCase)
		{
			prefix = wrongCasePrefix;
		}
		else
		{
			prefix = ""; //$NON-NLS-1$
		}

		CharSequence postfix;
		if (checkWrongCase)
		{
			postfix = wrongCasePostfix;
		}
		else
		{
			postfix = rightCasePostfix;
		}

		if (prefix == null || postfix == null)
		{
			return false;
		}

		try
		{
			// 4: check if parts of the postfix are already in the document
			int to = Math.min(document.getLength(), fFilterOffset + postfix.length());
			StringBuffer inDocument = new StringBuffer(document.get(fFilterOffset, to - fFilterOffset));
			truncatePostfix(inDocument, postfix);

			// 5: replace and reveal
			document.replace(fFilterOffset - prefix.length(), prefix.length() + inDocument.length(),prefix.toString() 
                                          + postfix.toString());

			fContentAssistSubjectControlAdapter.setSelectedRange(fFilterOffset + postfix.length(), 0);
			fContentAssistSubjectControlAdapter.revealRange(fFilterOffset + postfix.length(), 0);

			return false;
		}
		catch (BadLocationException e)
		{
			// ignore and return false
			return false;
		}
	}

	/**
	 * @since 3.1
	 */
	private boolean isPrefixCompatible(CharSequence oneSequence, int oneOffset, CharSequence twoSequence,
			int twoOffset, IDocument document) throws BadLocationException
	{
		if (oneSequence == null || twoSequence == null)
		{
			return true;
		}

		int min = Math.min(oneOffset, twoOffset);
		int oneEnd = oneOffset + oneSequence.length();
		int twoEnd = twoOffset + twoSequence.length();

		String one = document.get(oneOffset, min - oneOffset) + oneSequence
				+ document.get(oneEnd, Math.min(fFilterOffset, fFilterOffset - oneEnd));
		String two = document.get(twoOffset, min - twoOffset) + twoSequence
				+ document.get(twoEnd, Math.min(fFilterOffset, fFilterOffset - twoEnd));

		return one.equals(two);
	}

	/**
	 * Truncates <code>buffer</code> to the common prefix of <code>buffer</code> and <code>sequence</code>.
	 * 
	 * @param buffer
	 *            the common postfix to truncate
	 * @param sequence
	 *            the characters to truncate with
	 */
	private void truncatePostfix(StringBuffer buffer, CharSequence sequence)
	{
		// find common prefix
		int min = Math.min(buffer.length(), sequence.length());
		for (int c = 0; c < min; c++)
		{
			if (sequence.charAt(c) != buffer.charAt(c))
			{
			    buffer.delete(c, buffer.length());
			    return;
		        }
		}

		// all equal up to minimum
		buffer.delete(min, buffer.length());
	}

	/**
	 * Extracts the completion offset of an <code>ICompletionProposal</code>. If <code>proposal</code> is a
	 * <code>ICompletionProposalExtension3</code>, its <code>getCompletionOffset</code> method is called, otherwise, the
	 * invocation offset of this popup is shown.
	 * 
	 * @param proposal
	 *            the proposal to extract the offset from
	 * @return the proposals completion offset, or <code>fInvocationOffset</code>
	 * @since 3.1
	 */
	private int getPrefixCompletionOffset(ICompletionProposal proposal)
	{
		if (proposal instanceof ICompletionProposalExtension3)
		{
			return ((ICompletionProposalExtension3) proposal).getPrefixCompletionStart(
					fContentAssistSubjectControlAdapter.getDocument(), fFilterOffset);
		}
		return fInvocationOffset;
	}

	/**
	 * Extracts the replacement string from an <code>ICompletionProposal</code>. If <code>proposal</code> is a
	 * <code>ICompletionProposalExtension3</code>, its <code>getCompletionText</code> method is called, otherwise, the
	 * display string is used.
	 * 
	 * @param proposal
	 *            the proposal to extract the text from
	 * @return the proposals completion text
	 * @since 3.1
	 */
	private CharSequence getPrefixCompletion(ICompletionProposal proposal)
	{
		CharSequence insertion = null;
		if (proposal instanceof ICompletionProposalExtension3)
		{
			insertion = ((ICompletionProposalExtension3) proposal).getPrefixCompletionText(
					fContentAssistSubjectControlAdapter.getDocument(), fFilterOffset);
		}

		if (insertion == null)
		{
			insertion = proposal.getDisplayString();
		}

		return insertion;
	}

	/**
	 * Gets the activation key
	 * 
	 * @return char
	 */
	public char getActivationKey()
	{
		return fActivationKey;
	}

	/**
	 * Sets the activation key
	 * 
	 * @param activationKey
	 */
	public void setActivationKey(char activationKey)
	{
		fActivationKey = activationKey;
	}

	private void setDetailCompositeContent(FontData fontData, Event event)
	{
		TableItem item = (TableItem) event.item;
		ICompletionProposal curProposal = (ICompletionProposal) item.getData();
		if (curProposal == selectedProposal)
		{
			return;
		}
		selectedProposal = curProposal;
		
		descriptionPanel.execute("setDocFont(\"" + fontData.getName() + "\",\"" + fontData.getHeight() + "\")");
		GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		int itemHeight = item.getBounds().height;
		int rightHeight = itemHeight * PROPOSAL_ITEMS_VISIBLE;
		gd.heightHint = rightHeight;
		detailProposalComposite.setLayoutData(gd);
		detailProposalComposite.setBounds(fProposalTable.getSize().x + 2, 1, 320, rightHeight);
		Rectangle rect = detailProposalComposite.getBounds();
		descriptionPanel.setBounds(0, 0, rect.width, rightHeight - 1);
		
		if (curProposal instanceof ICommonCompletionProposal)
		{
			ICommonCompletionProposal proposal = (ICommonCompletionProposal)curProposal;
			String location = "";
			
			String title = proposal.getDisplayString();
			title = title.replace("<", "&lt;");
			title = title.replace(">", "&gt;");
			descriptionPanel.execute("setTitle(\"" + title + "\")");

			String info = proposal.getAdditionalProposalInfo();
			descriptionPanel.execute("cleanAddition()");

			if(info == null) {
				info = "";
			}
			info = info.replace("\"", "'");
			info = info.replaceAll("(\n|\r)\\s*", "<br/>");
			descriptionPanel.execute("setAddition(\"" + info + "\")");
			descriptionPanel.execute("clearLocation()");
			descriptionPanel.execute("setLocation(\"" + location + "\")");
		}
		fProposalShell.pack();
	}
	
	class ProposalSelectionAdapter extends SelectionAdapter {
		public void widgetDefaultSelected(SelectionEvent e)
		{
			selectProposalWithMask(e.stateMask);
			if (!("win32".equals(Platform.getOS())))
				return;
			disposePopup();
		}
	}
	
	class BrowserProgressAdapter extends ProgressAdapter {
		public void completed(ProgressEvent event)
		{
			// add listener for fProposalTable.
			fProposalTable.addListener(SWT.MeasureItem, new Listener()
			{
				public void handleEvent(Event event)
				{
					TableItem item = (TableItem) event.item;
					if ((item != null) && (!item.isDisposed()))
					{
						event.height = 25;
					}
				}
			});
			fProposalTable.addListener(SWT.EraseItem, new Listener()
			{
				public void handleEvent(Event event)
				{
					if ((event.detail & SWT.TRAVERSE_ESCAPE) != 0)
					{
						FontData fontData = JFaceResources.getDefaultFont().getFontData()[0];
						setDetailCompositeContent(fontData, event);
					}
				}
			});
			KeyListener keyListener = new KeyListener()
			{
				public void keyReleased(KeyEvent e)
				{
					verifyKeyEvent(e);
				}

				public void keyPressed(KeyEvent e)
				{
				}
			};
			fProposalTable.addKeyListener(keyListener);
			fProposalTable.addSelectionListener(new ProposalSelectionAdapter());
			
			// add listener for fProposalShell.
			fProposalShell.addDisposeListener(new DisposeListener()
			{
				public void widgetDisposed(DisposeEvent e)
				{
					unregister();
				}
			});
			fProposalShell.addKeyListener(keyListener);
			
			// add listener for detailProposalComposite.
			detailProposalComposite.addKeyListener(keyListener);
			
			fPopupCloser.install(fContentAssistant, fProposalTable, fAdditionalInfoController);
		}
	}
}
