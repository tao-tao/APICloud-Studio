/**
d * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.js.contentassist;

import java.io.File;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;

import beaver.Scanner;

import com.apicloud.commons.util.XMLUtil;
import com.aptana.core.IFilter;
import com.aptana.core.util.AndFilter;
import com.aptana.core.util.CollectionsUtil;
import com.aptana.core.util.StringUtil;
import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.CommonContentAssistProcessor;
import com.aptana.editor.common.contentassist.CommonCompletionProposal;
import com.aptana.editor.common.contentassist.ILexemeProvider;
import com.aptana.editor.common.contentassist.UserAgentManager;
import com.aptana.editor.common.util.EditorUtil;
import com.aptana.editor.js.JSPlugin;
import com.aptana.editor.js.JSSourceConfiguration;
import com.aptana.editor.js.internal.JSModelUtil;
import com.aptana.editor.js.text.JSFlexLexemeProvider;
import com.aptana.index.core.Index;
import com.aptana.js.core.IJSConstants;
import com.aptana.js.core.JSLanguageConstants;
import com.aptana.js.core.index.IJSIndexConstants;
import com.aptana.js.core.index.JSIndexQueryHelper;
import com.aptana.js.core.inferencing.JSNodeTypeInferrer;
import com.aptana.js.core.inferencing.JSPropertyCollection;
import com.aptana.js.core.inferencing.JSScope;
import com.aptana.js.core.inferencing.JSTypeUtil;
import com.aptana.js.core.model.FunctionElement;
import com.aptana.js.core.model.PropertyElement;
import com.aptana.js.core.parsing.JSFlexScanner;
import com.aptana.js.core.parsing.JSParseState;
import com.aptana.js.core.parsing.JSTokenType;
import com.aptana.js.core.parsing.ast.IJSNodeTypes;
import com.aptana.js.core.parsing.ast.JSArgumentsNode;
import com.aptana.js.core.parsing.ast.JSAssignmentNode;
import com.aptana.js.core.parsing.ast.JSFunctionNode;
import com.aptana.js.core.parsing.ast.JSGetPropertyNode;
import com.aptana.js.core.parsing.ast.JSInvokeNode;
import com.aptana.js.core.parsing.ast.JSNode;
import com.aptana.js.core.parsing.ast.JSObjectNode;
import com.aptana.js.core.parsing.ast.JSParseRootNode;
import com.aptana.parsing.ParserPoolFactory;
import com.aptana.parsing.ast.INameNode;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.lexer.IRange;
import com.aptana.parsing.lexer.Lexeme;
import com.aptana.parsing.lexer.Range;

public class JSContentAssistProcessor extends CommonContentAssistProcessor
{
	/**
	 * This class is used via {@link CollectionsUtil#filter(Collection, IFilter)} to remove duplicate proposals based on
	 * display names. Duplicate proposals are merged into a single entry
	 */
	public class ProposalMerger implements IFilter<ICompletionProposal>
	{
		private ICompletionProposal lastProposal = null;

		public boolean include(ICompletionProposal item)
		{
			boolean result;

			if (lastProposal == null || !lastProposal.getDisplayString().equals(item.getDisplayString()))
			{
				result = true;
				lastProposal = item;
			}
			else
			{
				result = false;
			}

			return result;
		}
	}

	private static final Image JS_FUNCTION = JSPlugin.getImage("/icons/js_function.png"); //$NON-NLS-1$
	private static final Image JS_PROPERTY = JSPlugin.getImage("/icons/js_property.png"); //$NON-NLS-1$
	private static final Image JS_KEYWORD = JSPlugin.getImage("/icons/keyword.png"); //$NON-NLS-1$
	private static final String CONF_FOLDER_PATH = "/dropins/conf";
	private static final String SNIPPET_FOLDER_NAME = "/APICloud-Snippets";
	private static String ECLIPSELOCAL = Platform.getInstallLocation().getURL().getFile();
	private static String SNIPPETS = ECLIPSELOCAL + CONF_FOLDER_PATH + SNIPPET_FOLDER_NAME;

	/**
	 * Filters out internal properties.
	 */
	private static final IFilter<PropertyElement> isVisibleFilter = new IFilter<PropertyElement>()
	{
		public boolean include(PropertyElement item)
		{
			return !item.isInternal();
		}
	};

	/**
	 * Filters out functions that are constructors.
	 */
	private static final IFilter<PropertyElement> isNotConstructorFilter = new IFilter<PropertyElement>()
	{
		public boolean include(PropertyElement item)
		{
			if (!(item instanceof FunctionElement))
			{
				return true;
			}
			return !((FunctionElement) item).isConstructor();
		}
	};

	private static Set<String> AUTO_ACTIVATION_PARTITION_TYPES = CollectionsUtil.newSet(JSSourceConfiguration.DEFAULT,
			IDocument.DEFAULT_CONTENT_TYPE);

	private JSIndexQueryHelper indexHelper;
	private IParseNode targetNode;
	private IParseNode statementNode;
	private IRange replaceRange;
	private IRange activeRange;
	private String replaceStr;
	private String var;
	private boolean isGlobal;

	/**
	 * JSIndexContentAssistProcessor
	 * 
	 * @param editor
	 */
	public JSContentAssistProcessor(AbstractThemeableEditor editor)
	{
		super(editor);
	}

	/**
	 * JSContentAssistProcessor
	 * 
	 * @param editor
	 * @param activeRange
	 */
	public JSContentAssistProcessor(AbstractThemeableEditor editor, IRange activeRange)
	{
		this(editor);

		this.activeRange = activeRange;
	}

	/**
	 * @param prefix
	 * @param completionProposals
	 */
	private void addKeywords(Set<ICompletionProposal> proposals, int offset, String input)
	{
		for (String name : JSLanguageConstants.KEYWORDS)
		{
			if(name.toLowerCase().startsWith(input.toLowerCase())){
				String description = MessageFormat.format(Messages.JSContentAssistProcessor_KeywordDescription, name);
				addProposal(proposals, name, input, JS_KEYWORD, description, getActiveUserAgentIds(),
						Messages.JSContentAssistProcessor_KeywordLocation, offset);
			}
		}
	}

	/**
	 * addProjectGlobalFunctions
	 * 
	 * @param proposals
	 * @param offset
	 */
	private void addGlobals(Set<ICompletionProposal> proposals, int offset, boolean isGlobal)
	{
		Collection<PropertyElement> projectGlobals = getQueryHelper().getGlobals(getFilename());

		if (!CollectionsUtil.isEmpty(projectGlobals))
		{
			String[] userAgentNames = getActiveUserAgentIds();
			URI projectURI = getProjectURI();

			for (PropertyElement property : CollectionsUtil.filter(projectGlobals, isVisibleFilter))
			{
				String location = null;
				List<String> documents = property.getDocuments();
				if (!CollectionsUtil.isEmpty(documents))
				{
					String docString = documents.get(0);
					int index = docString.lastIndexOf('/');
					if (index != -1)
					{
						location = docString.substring(index + 1);
					}
					else
					{
						location = docString;
					}
				}

				if(isGlobal){
					addProposal(proposals, property, offset, projectURI, location, userAgentNames);
				}
			}
		}
	}

	/**
	 * addProperties
	 * 
	 * @param proposals
	 * @param offset
	 */
	protected void addProperties(Set<ICompletionProposal> proposals, int offset, int num)
	{
		System.out.println("num===================================" + num);
		JSGetPropertyNode node = ParseUtil.getGetPropertyNode(targetNode, statementNode);

		JSScope localScope = ParseUtil.getScopeAtOffset(targetNode, offset);
		IParseNode parseNode = node.getChildren()[0];
		String parserName = parseNode.toString();
		JSPlugin.getDefault().getLog().log(new Status(IStatus.OK, JSPlugin.PLUGIN_ID, 0,parserName, null));

		node.getLeftHandSide();
		String value = node.getLeftHandSide().getText();
		node.getLeftHandSide().getChildren();
		JSPropertyCollection jsc = (JSPropertyCollection)localScope.getObject().getProperty(value);
		JSPropertyCollection collection = (JSPropertyCollection)localScope.getObject();
		collection.getValues();
		if(jsc != null) {
			List<JSInvokeNode> iNodes = new ArrayList<JSInvokeNode>(); 
			for(JSNode jsn : jsc.getValues()) {
				if(jsn instanceof JSInvokeNode) {
					iNodes.add((JSInvokeNode)jsn); 
				}
				if(jsn instanceof JSAssignmentNode) {
					JSAssignmentNode jsnode = 	(JSAssignmentNode)jsn;
					
					value = jsnode.getChild(1).toString();
				}
			}
			if(iNodes.size() == 0) {
				if("api".equals(value)) {
					jsc = null;
				}
			} else {
				value = iNodes.get(iNodes.size() - 1).value.toString();
			}
//			value = iNodes.size() == 0 ? "" : iNodes.get(iNodes.size() - 1).value.toString();
			iNodes.clear();
		}

		if(jsc == null && parserName.equals("api")) {
			JSPlugin.getDefault().getLog().log(new Status(IStatus.OK, JSPlugin.PLUGIN_ID, 0,"join api 1111111111", null));
			proposals.clear();
//			createApicloudProposal(proposals, offset, num, null);
			return;
		}
	}

	/**
	 * addProposal
	 * 
	 * @param proposals
	 * @param property
	 * @param offset
	 * @param projectURI
	 * @param overriddenLocation
	 */
	private void addProposal(Set<ICompletionProposal> proposals, PropertyElement property, String triggerName, int offset, URI projectURI,
			String overriddenLocation)
	{
		List<String> userAgentNameList = property.getUserAgentNames();
		String[] userAgentNames = userAgentNameList.toArray(new String[userAgentNameList.size()]);

		addProposal(proposals, property, offset, projectURI, overriddenLocation, userAgentNames);
	}

	/**
	 * addProposal
	 * 
	 * @param proposals
	 * @param property
	 * @param offset
	 * @param projectURI
	 * @param overriddenLocation
	 * @param userAgentNames
	 */
	private void addProposal(Set<ICompletionProposal> proposals, PropertyElement property, int offset, URI projectURI,
			String overriddenLocation, String[] userAgentNames)
	{
		if (isActiveByUserAgent(userAgentNames))
		{
			int replaceLength = 0;

			replaceLength = replaceStr.length();
			offset = offset - replaceLength;
			String replaceContent = property.getName();
			String displayName = property.getName();

			if(replaceStr.contains(".")){
				replaceContent = replaceStr.substring(0, replaceStr.indexOf(".") + 1) + property.getName();
				displayName = replaceStr.substring(0, replaceStr.indexOf(".") + 1) + property.getName();
			}

			int length = replaceContent.length();

			PropertyElementProposal proposal = new PropertyElementProposal(property, projectURI, replaceContent, offset, replaceLength, length, null, displayName, null, null);
			proposal.setTriggerCharacters(getProposalTriggerCharacters());
			if (!StringUtil.isEmpty(overriddenLocation))
			{
				proposal.setFileLocation(overriddenLocation);
			}

			Image[] userAgents = UserAgentManager.getInstance().getUserAgentImages(getProject(), userAgentNames);
			proposal.setUserAgentImages(userAgents);

			// add the proposal to the list
			if(!proposal.getDisplayString().contains("$")){
				proposals.add(proposal);
			}
		}
	}

	/**
	 * addProposal - The display name is used as the insertion text
	 * 
	 * @param proposals
	 * @param displayName
	 * @param image
	 * @param description
	 * @param userAgents
	 * @param fileLocation
	 * @param offset
	 */
	private void addProposal(Set<ICompletionProposal> proposals, String displayName, String triggerName, Image image, String description,
			String[] userAgentIds, String fileLocation, int offset)
	{
		if (isActiveByUserAgent(userAgentIds))
		{
			int length = displayName.length();

			// calculate what text will be replaced
			int replaceLength = 0;
			
			replaceLength = replaceStr.length();
			offset = offset - replaceLength;

			IContextInformation contextInfo = null;
			Image[] userAgents = UserAgentManager.getInstance().getUserAgentImages(getProject(), userAgentIds);

			CommonCompletionProposal proposal = new CommonCompletionProposal(displayName, offset, replaceLength, length, image, displayName, this.getProject(), contextInfo, description);
			proposal.setFileLocation(fileLocation);
			proposal.setUserAgentImages(userAgents);
			proposal.setTriggerCharacters(getProposalTriggerCharacters());
//			proposal.setTriggerName(replaceStr);

			proposals.add(proposal);
		}
	}

	private void addApicloudProposal(Set<ICompletionProposal> proposals, String displayName, String triggerName, String replaceContent, Image image, String description,
			String[] userAgentIds, String fileLocation, int offset)
	{
		if (isActiveByUserAgent(userAgentIds)) {
			int length = replaceContent.trim().length();
			int replaceLength = 0;

			Pattern pattern = Pattern.compile("\\(''\\)");
			Matcher matcher = pattern.matcher(replaceContent);

			if(matcher.find()){
				int tag = replaceContent.trim().indexOf((String)matcher.group(0)) + 2;
				length = tag;
			}

			replaceLength = replaceStr.length();
			offset = offset - replaceLength;

			IContextInformation contextInfo = null;
			Image[] userAgents = UserAgentManager.getInstance().getUserAgentImages(getProject(), userAgentIds);

			CommonCompletionProposal proposal = new CommonCompletionProposal(replaceContent.trim(), offset, replaceLength, length, image, displayName, this.getProject(), contextInfo, description);
			proposal.setFileLocation(fileLocation);
			proposal.setUserAgentImages(userAgents);
			proposal.setTriggerCharacters(getProposalTriggerCharacters());
			proposal.getContextInformation();
//			proposal.setTriggerName(replaceStr);
			proposals.add(proposal);
		}
	}

	/**
	 * addSymbolsInScope
	 * 
	 * @param proposals
	 */
	protected void addSymbolsInScope(Set<ICompletionProposal> proposals, int offset, String input)
	{
		if (targetNode != null)
		{
			JSScope globalScope = ParseUtil.getGlobalScope(targetNode);

			if (globalScope != null)
			{
				JSScope localScope = globalScope.getScopeAtOffset(offset);
				String fileLocation = getFilename();
				String[] userAgentNames = getActiveUserAgentIds();

				while (localScope != null && localScope != globalScope)
				{
					List<String> symbols = localScope.getLocalSymbolNames();

					for (String symbol : symbols)
					{
						boolean isFunction = false;
						JSPropertyCollection object = localScope.getLocalSymbol(symbol);
						List<JSNode> nodes = object.getValues();

						if (nodes != null)
						{
							for (JSNode node : nodes)
							{
								if (node instanceof JSFunctionNode)
								{
									isFunction = true;
									break;
								}
							}
						}

						String name = symbol;
						String description = "API Object";
						Image image = (isFunction) ? JS_FUNCTION : JS_PROPERTY;

						if(name.startsWith(input) && !name.equals("$")){
							addProposal(proposals, name, input, image, description, userAgentNames, fileLocation, offset);
						}
					}

					localScope = localScope.getParentScope();
				}
			}
		}
	}

	/**
	 * addThisProposals
	 * 
	 * @param proposals
	 * @param offset
	 */
	protected void addThisProperties(Set<ICompletionProposal> proposals, int offset)
	{
		// find containing function or JSParseRootNode
		IParseNode activeNode = getActiveASTNode(offset);

		while (!(activeNode instanceof JSFunctionNode))
		{
			activeNode = activeNode.getParent();
			if (activeNode instanceof JSParseRootNode)
			{
				// If we've gotten to the root, just bail out.
				return;
			}
		}
		JSFunctionNode currentFunctionNode = (JSFunctionNode) activeNode;

		String functionName = getFunctionName(currentFunctionNode);

		if (functionName != null)
		{
			functionName = StringUtil.dotFirst(functionName).trim();
			if (functionName.length() == 0)
			{
				// Empty name
				functionName = null;
			}
		}

		List<JSFunctionNode> functionsToAnalyze;
		if (functionName == null)
		{
			// Unable to get a name for the current function: don't try to find any other
			// JS prototypes.
			functionsToAnalyze = Arrays.asList(currentFunctionNode);
		}
		else
		{
			// We want to match the following:
			// myFunc function(){...}
			// myFunc = function(){...}
			// myFunc.prototype.foo = function(){...}
			IParseNode parent = currentFunctionNode.getParent();
			if (parent.getNodeType() == IJSNodeTypes.ASSIGN)
			{
				parent = parent.getParent();
			}
			IParseNode[] children = parent.getChildren();
			functionsToAnalyze = new LinkedList<JSFunctionNode>();
			for (int i = 0; i < children.length; i++)
			{
				String childName = null;
				IParseNode childNode = children[i];
				JSFunctionNode jsFunctionNode = null;
				if (childNode instanceof JSFunctionNode)
				{
					jsFunctionNode = (JSFunctionNode) childNode;
					childName = jsFunctionNode.getNameNode().getName();

				}
				else if (childNode.getNodeType() == IJSNodeTypes.ASSIGN)
				{
					JSAssignmentNode assignmentNode = (JSAssignmentNode) childNode;
					IParseNode rightHandSide = assignmentNode.getRightHandSide();
					if (rightHandSide instanceof JSFunctionNode)
					{
						jsFunctionNode = (JSFunctionNode) rightHandSide;
						childName = getAssignmentLeftNodeName(assignmentNode);
					}
				}

				if (childName != null && jsFunctionNode != null)
				{
					if (StringUtil.dotFirst(childName).equals(functionName))
					{
						functionsToAnalyze.add(jsFunctionNode);
					}
				}
			}
		}

		for (JSFunctionNode function : functionsToAnalyze)
		{
			// collect all this.property assignments
			ThisAssignmentCollector collector = new ThisAssignmentCollector();
			((JSNode) function.getBody()).accept(collector);
			List<JSAssignmentNode> assignments = collector.getAssignments();

			if (!CollectionsUtil.isEmpty(assignments))
			{
				JSScope globalScope = ParseUtil.getGlobalScope(targetNode);

				if (globalScope != null)
				{
					JSScope localScope = globalScope.getScopeAtOffset(offset);
					Index index = getIndex();
					URI location = EditorUtil.getURI(editor);
					String typeName = StringUtil.concat(getNestedFunctionTypeName(function)
							+ IJSIndexConstants.NESTED_TYPE_SEPARATOR + "this"); //$NON-NLS-1$

					// infer each property and add proposal
					for (JSAssignmentNode assignment : assignments)
					{
						IParseNode lhs = assignment.getLeftHandSide();
						IParseNode rhs = assignment.getRightHandSide();
						String name = lhs.getLastChild().getText();

						JSNodeTypeInferrer nodeInferrer = new JSNodeTypeInferrer(localScope, index, location,
								getQueryHelper());
						((JSNode) rhs).accept(nodeInferrer);
						List<String> types = nodeInferrer.getTypes();

						PropertyElement property = new PropertyElement();
						property.setName(name);
						property.setHasAllUserAgents();

						if (!CollectionsUtil.isEmpty(types))
						{
							for (String type : types)
							{
								property.addType(type);
							}
						}

						addProposal(proposals, property, replaceStr, offset, getProjectURI(), typeName);
					}
				}
			}
		}
	}

	/**
	 * Given a function node, discover its name either declared directly or through a parent assign to the function
	 * (i.e.: myFunc function(){} or myFunc = function(){...}). May return null if unable to get the name.
	 */
	private String getFunctionName(JSFunctionNode currentFunctionNode)
	{
		String functionName = null;
		// Discover the name context name of where we are (function or assign to function).
		INameNode nameNode = currentFunctionNode.getNameNode();
		if (nameNode.getName().length() == 0)
		{
			IParseNode functionParent = currentFunctionNode.getParent();
			if (functionParent.getNodeType() == IJSNodeTypes.ASSIGN)
			{
				functionName = getAssignmentLeftNodeName((JSAssignmentNode) functionParent);
			}
		}
		else
		{
			// Found as: myFunc function(){...}
			functionName = nameNode.getName();
		}
		return functionName;
	}

	//@formatter:off 
	/**
	 * @return the left-hand side name we can discover in an assign
	 * I.e.: something as: 
	 * 
	 * myFunc = function(){...}
	 * myFunc.prototype.foo = function(){...} 
	 * 
	 * Will return myFunc / myFunc.prototype.foo
	 */
	//@formatter:on
	private String getAssignmentLeftNodeName(JSAssignmentNode assignmentNode)
	{
		IParseNode leftHandSide = assignmentNode.getLeftHandSide();
		if (leftHandSide.getNodeType() == IJSNodeTypes.GET_PROPERTY)
		{
			return leftHandSide.toString();
		}
		return null;
	}

	/**
	 * addTypeProperties
	 * 
	 * @param proposals
	 * @param typeName
	 * @param offset
	 */
	@SuppressWarnings("unchecked")
	protected void addTypeProperties(Set<ICompletionProposal> proposals, String typeName, int offset)
	{
		// grab all ancestors of the specified type
		List<String> allTypes = getQueryHelper().getTypeAncestorNames(typeName);

		// include the type in the list as well
		allTypes.add(0, typeName);

		// add properties and methods
		Collection<PropertyElement> properties = getQueryHelper().getTypeMembers(allTypes);
		URI projectURI = getProjectURI();
		for (PropertyElement property : CollectionsUtil.filter(properties, new AndFilter<PropertyElement>(
				isNotConstructorFilter, isVisibleFilter)))
		{
			addProposal(proposals, property, replaceStr, offset, projectURI, null);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aptana.editor.common.CommonContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer
	 * , int)
	 */
	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset)
	{
		List<IContextInformation> result = new ArrayList<IContextInformation>(2);
		FunctionElement function = getFunctionElement(viewer, offset);

		if (function != null)
		{
			JSArgumentsNode node = getArgumentsNode(offset);

			if (node != null)
			{
				boolean inObjectLiteral = false;

				// find argument we're in
				for (IParseNode arg : node)
				{
					if (arg.contains(offset))
					{
						// Not foolproof, but this should cover 99% of the cases we're likely to encounter
						inObjectLiteral = (arg instanceof JSObjectNode);
						break;
					}
				}

				// prevent context info popup from appearing and immediately disappearing
				if (!inObjectLiteral)
				{
					IContextInformation ci = new JSContextInformation(function, getProjectURI(),
							node.getStartingOffset());

					result.add(ci);
				}
			}
		}

		return result.toArray(new IContextInformation[result.size()]);
	}

	/**
	 * createLexemeProvider
	 * 
	 * @param document
	 * @param offset
	 * @return
	 */
	ILexemeProvider<JSTokenType> createLexemeProvider(IDocument document, int offset)
	{
		Scanner scanner = new JSFlexScanner();
		ILexemeProvider<JSTokenType> result;

		// NOTE: use active range temporarily until we get proper partitions for JS inside of HTML
		if (activeRange != null)
		{
			result = new JSFlexLexemeProvider(document, activeRange, scanner);
		}
		else if (statementNode != null)
		{
			result = new JSFlexLexemeProvider(document, statementNode, scanner);
		}
		else
		{
			result = new JSFlexLexemeProvider(document, offset, scanner);
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aptana.editor.common.CommonContentAssistProcessor#doComputeCompletionProposals(org.eclipse.jface.text.ITextViewer
	 * , int, char, boolean)
	 */
	@Override
	protected ICompletionProposal[] doComputeCompletionProposals(ITextViewer viewer, int offset, char activationChar,
			boolean autoActivated)
	{
		// NOTE: Using a linked hash set to preserve add-order. We need this in case we end up filtering proposals. This
		// will give precedence to the first of a collection of proposals with like names
		Set<ICompletionProposal> result = new LinkedHashSet<ICompletionProposal>();

		// grab document
		IDocument document = viewer.getDocument();
		int o = 0;
		LocationType location = LocationType.NONE;
		ICompletionProposal[] resultList = new ICompletionProposal[0];

		try {
			String str = getLineString(offset, document);
			System.out.println(str);
			o = getSpaceNum(o, str);
			System.out.println("current offset ========================"+ offset);
			System.out.println("sapce num is:=======================" + o);

			String doc = document.get(0, offset);
			doc = doc.replaceAll(" ", "").replaceAll("\t", "").replaceAll("\n", "").replaceAll("\r", "").trim();
			getReplaceString(offset, document);

			List<File> locationTypeFolders = new ArrayList<File>();
			List<String> locationTypes = new ArrayList<String>();
//			replaceRange = new Range(offset-replaceStr.length(), offset-replaceStr.length());
			JSLocationIdentifier identifier = new JSLocationIdentifier(offset, getActiveASTNode(offset - 1));
			targetNode = identifier.getTargetNode();
			statementNode = identifier.getStatementNode();
			replaceRange = identifier.getReplaceRange();
			isGlobal = true;

			if(replaceRange == null){
				replaceRange = new Range(offset - replaceStr.length(), offset - replaceStr.length());
			}

			getLocationTypeFolders(SNIPPETS, locationTypeFolders);
			getLocationTypes(offset, locationTypes, locationTypeFolders, document, doc);

			String input = replaceStr;
			System.out.println("replaceStr is : " + input);
			location = checkLocationType(offset, result, document, o, locationTypes, input, doc);
			System.out.println(location.name());

			if (location.equals(LocationType.IN_PROPERTY_NAME)) {
				addTypeProperties(result, "Document", offset);
				addTypeProperties(result, "Window", offset);
			} else if (location.equals(LocationType.IN_CONSTRUCTOR)) {
				addKeywords(result, offset, input);
				addGlobals(result, offset, false);
				addSymbolsInScope(result, offset, input);
			} else if (location.equals(LocationType.IN_THIS)) {
				addThisProperties(result, offset);
			} else if (location.equals(LocationType.IN_GLOBAL)) {
				addGlobalProposals(result, offset, locationTypes, locationTypeFolders, input);
			} else if (location.equals(LocationType.$API) || location.equals(LocationType.API)){
				addApicloudProposals(result, locationTypeFolders, offset, input);
			} else if (location.equals(LocationType.API_REQUIRE)){
				addApiModuleProposals(result, locationTypeFolders, offset, var, input);
			} else if (location.equals(LocationType.NONE)){
				return resultList;
			}

			System.out.println(result.size());
			// merge and remove duplicates from the proposal list
			List<ICompletionProposal> filteredProposalList = getMergedProposals(new ArrayList<ICompletionProposal>(result));
			resultList = filteredProposalList.toArray(new ICompletionProposal[filteredProposalList.size()]);

			// select the current proposal based on the prefix
			if (replaceRange != null) {
				String prefix = document.get(replaceRange.getStartingOffset(), replaceRange.getLength());
				setSelectedProposal(prefix, resultList);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return resultList;
	}

	private void getLocationTypes(int offset, List<String> locationTypes, List<File> locationTypeFolders, IDocument document, String doc) {
		for(Object obj : locationTypeFolders){
			if(obj instanceof File){
				File file = (File)obj;

				if(file.exists() && file.isDirectory()){
					String name = file.getName();

					locationTypes.add(name);
				}
			}
		}

		locationTypes.add("api");
		locationTypes.add("$api");
	}

	private void addGlobalProposals(Set<ICompletionProposal> result, int offset, List<String> locationTypes, List<File> locationTypeFolders, String input) {
		for (String name : locationTypes) {
			if (name.toLowerCase().startsWith(input.toLowerCase())) {
				addProposal(result, name, input, JS_PROPERTY, "APICloud Object", getActiveUserAgentIds(), "APICloud Object", offset);
			}
		}

		addApicloudProposals(result, locationTypeFolders, offset, input);

		addKeywords(result, offset, input);
		addGlobals(result, offset, isGlobal);
		addSymbolsInScope(result, offset, input);
	}

	//递归寻找出snippets目录中所有除了"api"和"$api"的类型字段。
	private void getLocationTypeFolders(String snippets, List<File> locationTypes) {
		File folder = new File(snippets);

		if(folder.exists() && folder.isDirectory()){
			File[] subFolders = folder.listFiles();

			if(subFolders.length > 0){
				int num = 0;

				for(File file: subFolders){
					if(file.isDirectory()){
						num++;
					}
					else continue;
				}

				if (num == 0) {
					locationTypes.add(folder);
				} else {
					for (File file : subFolders) {
						if (file.isDirectory()) {
							getLocationTypeFolders(snippets + "/" + file.getName(), locationTypes);
						}
					}
				}
			}
		}
	}

	private LocationType checkLocationType(int offset, Set<ICompletionProposal> result, IDocument document, int o,
			List<String> locationTypes, String input, String doc) throws BadLocationException
	{
		LocationType location = LocationType.NONE;

		if(input.trim().equals("") || input.trim().equals(".")){
			return location;
		}

		String replace = "";
		int index = 1;

		while(true){

			if(offset>=index){
				replace = document.get(offset-index, 1);
			}else{
				break;
			}

			System.out.println("replaceStrs is : " + replace);

			if (!(replace.matches("\\w") || replace.equals("$") || replace.equals("_") || replace.equals("."))) {
				break;
			}

			if(offset == index) {
				replace = "";
			}

			index++;
		}

		replace = document.get(offset-index+1, index-1);

		if(replace.contains(".")) {
			var = replace.substring(0, replace.indexOf("."));
			int inde = doc.lastIndexOf(var+"=");
			if(inde != -1) {
				var = findVarString(inde, "", doc);
				int num = var.indexOf("=");
				var = var.substring(num +1, var.length());
				System.out.println(var);

				if(var.matches("api.require.+")){
					return LocationType.API_REQUIRE;
				}
			}
		}

		Pattern pattern1 = Pattern.compile("\\w*\\.");
		Pattern pattern2 = Pattern.compile("\\S+\\w*\\.");
		Pattern pattern3 = Pattern.compile("\\$\\w*\\.");
		Pattern pattern4 = Pattern.compile("\\w+|\\$");
		Pattern pattern5 = Pattern.compile("\\$\\w*");
		Pattern pattern6 = Pattern.compile("\\$\\w+");

		Matcher matcher1 = pattern1.matcher(input);
		Matcher matcher2 = pattern2.matcher(input);
		Matcher matcher3 = pattern3.matcher(input);
		Matcher matcher4 = pattern4.matcher(input);
		Matcher matcher5 = pattern5.matcher(input);
		Matcher matcher6 = pattern6.matcher(input);

		String type = null;

		if (matcher1.find()) {
			type = matcher1.group(matcher1.groupCount()).trim();
		} else if (matcher2.find()) {
			type = matcher2.group(matcher2.groupCount()).trim();
		} else if (matcher3.find()) {
			type = matcher3.group(matcher3.groupCount()).trim();
		} else if (matcher4.find()) {
			type = matcher4.group(matcher4.groupCount()).trim();
		} else if (matcher5.find()) {
			type = matcher5.group(matcher5.groupCount()).trim();
		} else if (matcher6.find()) {
			type = matcher6.group(matcher6.groupCount()).trim();
		} else{
			type = "";
		}

		if(type.equals("") || type.equals(".")){
			return location;
		}

		if (type.trim().contains("document.") || type.trim().contains("window.")) 
		{
			location = LocationType.IN_PROPERTY_NAME;
		} 
		else if (!locationTypes.isEmpty() && !type.equals("")) 
		{
			for (Iterator<String> iter = locationTypes.listIterator(); iter.hasNext();) 
			{
					String name = iter.next();

					if (name.equals(type) || type.equals(name + ".")) 
					{
						if ("$api.".equals(name)) 
						{
							location = LocationType.$API;
							} else if ("api.".equals(name)) {
								location = LocationType.API;
								}
						} else if (name.startsWith(type)) 
						{
							location = LocationType.IN_GLOBAL;
							}
			}
		}else{
			location = getLocationType(document, offset, input.trim());
		}

		if( location == LocationType.NONE){
			location = LocationType.IN_GLOBAL;
		}

		return location;
	}

	private int getSpaceNum(int o, String str)
	{
		//find space num
		for(int index = 0; index < str.length(); index++) {
			if(str.charAt(index) == 9) {
				o += 4;
			}

			if(str.charAt(index) == 32) {
				o += 1;
			}
			if(str.charAt(index) < 'z' && str.charAt(index) > 'a') 
				break;
			if(str.charAt(index) < 'Z' && str.charAt(index) > 'A') 
				break;
		}
		return o;
	}

	private String getLineString(int offset, IDocument document) throws BadLocationException
	{
		int lineNum = document.getLineOfOffset(offset);
		int lineOffset = document.getLineOffset(lineNum);
		int lineLength = document.getLineInformation(lineNum).getLength();
		String str = document.get(lineOffset, lineLength);
		return str;
	}

	private void addApiModuleProposals(Set<ICompletionProposal> proposals, List<File> locationTypeFolders, int offset, String var, String input)
	{
		try {
			File snippetsFolder = null;
			Pattern p = Pattern.compile("api\\.require\\('(\\w+)'\\)");
			Matcher matcher = p.matcher(var);
			String type = "";

			if(matcher.find()){
				type = matcher.replaceAll("$1");
			}

			Object folder = getSubFolder(type, locationTypeFolders);

			if(folder != null){
				if (folder instanceof File) {
					snippetsFolder = (File) folder;
				} else if (folder instanceof String) {
					snippetsFolder = new File(SNIPPETS + folder);
				}

				if(snippetsFolder.exists()){
					addApiSnippetsProposals(proposals, offset, input, snippetsFolder, true);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String findVarString(int offset, String replaceStrs, String doc){
		int index = 1;
		while (true) {
			if (offset + index + 1 <= doc.length()) {
				replaceStrs = doc.substring(offset + index, offset + index + 1);
				System.out.println("var string is : " + replaceStrs);
				if (replaceStrs.equals(";")) {
					break;
				}
				if (offset + index == doc.length()) {
					System.out.println("var string =================="
							+ replaceStrs);
					return "";

				}
				index++;
			}
		}

		System.out.println(offset);
		System.out.println(index + offset);
		replaceStrs = doc.substring(offset, index + offset);
		System.out.println("var string ==================" + replaceStrs);
		return replaceStrs;
	}

	private void getReplaceString(int offset, IDocument document) {
		int num = 1;

		try {
			while (true) {
				if(offset>=num){
					replaceStr = document.get(offset - num, 1);
				}else{
					break;
				}

				if (replaceStr.equals(">")
						|| replaceStr.equals("\t")
						|| replaceStr.equals(" ")
						|| replaceStr.equals("\n")
						|| replaceStr.equals("\r")
						|| (replaceStr.matches("\\W") && !replaceStr.equals(".") && !replaceStr.equals("$"))) {
					break;
				}

//				if(!replaceStr.matches("\\d") && !replaceStr.matches("[a-zA-Z]") && !replaceStr.equals("$") && !replaceStr.equals("_")){
//					break;
//				}

				if (offset == num) {
					replaceStr = "";
				}

				num++;
			}

			replaceStr = document.get(offset - num + 1, num - 1);

			if (replaceStr.contains("<") || replaceStr.contains("\"")) {
				replaceStr = replaceStr.substring(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected JSIndexQueryHelper getQueryHelper()
	{
		if (indexHelper == null)
		{
			indexHelper = JSModelUtil.createQueryHelper(editor);
		}
		return indexHelper;
	}

	/**
	 * getActiveASTNode
	 * 
	 * @param offset
	 * @return
	 */
	IParseNode getActiveASTNode(int offset)
	{
		IParseNode result = null;

		try
		{
			// grab document
			IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());

			// grab source which is either the whole document for JS files or a subset for nested JS
			// @formatter:off
			String source =
				(activeRange != null)
					? doc.get(activeRange.getStartingOffset(), activeRange.getLength())
					: doc.get();
					source = source.replace("#", " ").replace("%", " ");
			// @formatter:on
			int startingOffset = (activeRange != null) ? activeRange.getStartingOffset() : 0;

			// create parse state and turn off all processing of comments
			JSParseState parseState = new JSParseState(source, startingOffset, true, true);

			// parse and grab resulting AST
			IParseNode ast = ParserPoolFactory.parse(IJSConstants.CONTENT_TYPE_JS, parseState).getRootNode();

			if (ast != null)
			{
				result = ast.getNodeAtOffset(offset);

				// We won't get a current node if the cursor is outside of the positions
				// recorded by the AST
				if (result == null)
				{
					if (offset < ast.getStartingOffset())
					{
						result = ast.getNodeAtOffset(ast.getStartingOffset());
					}
					else if (ast.getEndingOffset() < offset)
					{
						result = ast.getNodeAtOffset(ast.getEndingOffset());
					}
				}
			}
		}
		catch (Exception e)
		{
			// ignore parse error exception since the user will get markers and/or entries in the Problems View
		}

		return result;
	}

	/**
	 * getArgumentsNode
	 * 
	 * @param offset
	 * @return
	 */
	private JSArgumentsNode getArgumentsNode(int offset)
	{
		IParseNode node = getActiveASTNode(offset);
		JSArgumentsNode result = null;

		// work a way up the AST to determine if we're in an arguments node
		while (node instanceof JSNode && node.getNodeType() != IJSNodeTypes.ARGUMENTS)
		{
			node = node.getParent();
		}

		// process arguments node as long as we're not to the left of the opening parenthesis
		if (node instanceof JSNode && node.getNodeType() == IJSNodeTypes.ARGUMENTS
				&& node.getStartingOffset() != offset)
		{
			result = (JSArgumentsNode) node;
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.CommonContentAssistProcessor#getContextInformationValidator()
	 */
	@Override
	public IContextInformationValidator getContextInformationValidator()
	{
		return new JSContextInformationValidator();
	}

	/**
	 * getFunctionElement
	 * 
	 * @param viewer
	 * @param offset
	 * @return
	 */
	private FunctionElement getFunctionElement(ITextViewer viewer, int offset)
	{
		// process arguments node as long as we're not to the left of the opening parenthesis
		JSArgumentsNode node = getArgumentsNode(offset);
		if (node == null)
		{
			return null;
		}

		// save current replace range. A bit hacky but better than adding a flag into getLocation's signature
		IRange range = replaceRange;

		// grab the content assist location type for the symbol before the arguments list
		int functionOffset = node.getStartingOffset();
		LocationType location = getLocationType(viewer.getDocument(), functionOffset, "");

		// restore replace range
		replaceRange = range;

		// init type and method names
		String typeName = null;
		String methodName = null;
		switch (location)
		{
			case IN_VARIABLE_NAME:
			{
				typeName = JSTypeUtil.getGlobalType(getProject(), getFilename());
				methodName = node.getParent().getFirstChild().getText();
				break;
			}

			case IN_PROPERTY_NAME:
			{
				JSGetPropertyNode propertyNode = ParseUtil.getGetPropertyNode(node,
						((JSNode) node).getContainingStatementNode());
				List<String> types = getParentObjectTypes(propertyNode, offset);

				if (types.size() > 0)
				{
					methodName = propertyNode.getLastChild().getText();
				}
				break;
			}

			default:
				break;
		}

		if (typeName != null && methodName != null)
		{
			JSIndexQueryHelper helper = getQueryHelper();
			return helper.findFunctionInHierarchy(typeName, methodName);
		}

		return null;
	}

	/**
	 * getLocationByLexeme
	 * 
	 * @param lexemeProvider
	 * @param offset
	 * @return
	 */
	LocationType getLocationByLexeme(IDocument document, int offset, String name)
	{
		ILexemeProvider<JSTokenType> lexemeProvider = createLexemeProvider(document, offset);

		LocationType result = LocationType.UNKNOWN;

		int index = lexemeProvider.getLexemeIndex(offset);
		try
		{
			char c = document.getChar(offset);
			System.out.println(c);
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
		
		if (index < 0)
		{
			int candidateIndex = lexemeProvider.getLexemeFloorIndex(offset);
			Lexeme<JSTokenType> lexeme = lexemeProvider.getLexeme(candidateIndex);

			if (lexeme != null)
			{
				System.out.println(lexeme.getType());
				System.out.println(lexeme.getText());
				if (lexeme.getEndingOffset() == offset)
				{
					index = candidateIndex;
				}
				else if (lexeme.getType() == JSTokenType.NEW)
				{
					index = candidateIndex;
				}else {
					System.out.println(lexeme.getType());
					System.out.println(lexeme.getText());

					 if (lexeme.getType() == JSTokenType.EQUAL){
						 System.out.println(lexeme.getType() == JSTokenType.EQUAL);
						 return  LocationType.IN_GLOBAL;
					 }
					 if (lexeme.getType() == JSTokenType.DO) {
						 return  LocationType.IN_GLOBAL;
						 }

					if (lexeme.getType() == JSTokenType.IDENTIFIER) {
						// replaceStr = lexeme.getText();
						System.out.println(lexeme.getText());
						if ("document".equals(lexeme.getText())) {
							return LocationType.IN_PROPERTY_NAME;
						}
						return LocationType.IN_GLOBAL;
					}
				}
			}
		}

		if (index >= 0)
		{
			Lexeme<JSTokenType> lexeme = lexemeProvider.getLexeme(index);
			System.out.println(lexeme.getType());
			switch (lexeme.getType())
			{
				case DO:
					result =  LocationType.IN_GLOBAL;
					break;
				case DOT:
					result = LocationType.IN_PROPERTY_NAME;
					break;

				case SEMICOLON:
					if (index > 0)
					{
						Lexeme<JSTokenType> previousLexeme = lexemeProvider.getLexeme(index - 1);

						switch (previousLexeme.getType())
						{
							case IDENTIFIER:
								result = LocationType.IN_GLOBAL;
								break;

							default:
								break;
						}
					}
					break;

				case LPAREN:
					if (offset == lexeme.getEndingOffset())
					{
						Lexeme<JSTokenType> previousLexeme = lexemeProvider.getLexeme(index - 1);

						if (previousLexeme.getType() != JSTokenType.IDENTIFIER)
						{
							result = LocationType.IN_GLOBAL;
						}
					}
					break;

				case RPAREN:
					if (offset == lexeme.getStartingOffset())
					{
						result = LocationType.IN_GLOBAL;
					}
					break;

				case IDENTIFIER:
					if (index > 0)
					{
//						Lexeme<JSTokenType> previousLexeme = lexemeProvider.getLexeme(index - 1);
						Lexeme<JSTokenType> previousLexeme = lexemeProvider.getLexeme(index);
						System.out.println(previousLexeme.getType());
						System.out.println(previousLexeme.getText());
						switch (previousLexeme.getType())
						{
							case IDENTIFIER:
//								replaceStr = previousLexeme.getText();
								result = LocationType.IN_GLOBAL;
								break;
							case DOT:
								result = LocationType.IN_PROPERTY_NAME;
								break;

							case NEW:
								result = LocationType.IN_CONSTRUCTOR;
								break;

							case VAR:
								result = LocationType.IN_VARIABLE_DECLARATION;
								break;

							default:
								result = LocationType.IN_VARIABLE_NAME;
								break;
						}
					}
					else
					{
						result = LocationType.IN_VARIABLE_NAME;
					}
					break;

				default:
					break;
			}
		}
		else if (lexemeProvider.size() == 0)
		{
			result = LocationType.IN_GLOBAL;
		}

		if(result ==LocationType.UNKNOWN) {
			if(targetNode != null && targetNode.getParent() != null && targetNode.getParent().getParent() != null) {
				System.out.println(targetNode.getParent().getParent());
				return LocationType.IN_PARAMETERS;
			}
		}
		return result;
	}

	/**
	 * getLocation
	 * 
	 * @param lexemeProvider
	 * @param offset
	 * @return
	 */
	LocationType getLocationType(IDocument document, int offset, String name)
	{
		JSLocationIdentifier identifier = new JSLocationIdentifier(offset, getActiveASTNode(offset - 1));
		LocationType result = identifier.getType();

		targetNode = identifier.getTargetNode();
		statementNode = identifier.getStatementNode();
		replaceRange = identifier.getReplaceRange();
//		if(targetNode != null && targetNode.getParent() != null && targetNode.getParent().getParent() != null) {
//			System.out.println(targetNode.getParent().getParent());
//			return LocationType.IN_PARAMETERS;
//		}
		
		// if we couldn't determine the location type with the AST, then
		// fallback to using lexemes
//		if (result == LocationType.UNKNOWN || result == LocationType.NONE)
//		{
			// NOTE: this method call sets replaceRange as a side-effect
			result = getLocationByLexeme(document, offset, name);
//		}

		return result;
	}

	/**
	 * @param result
	 * @return
	 */
	protected List<ICompletionProposal> getMergedProposals(List<ICompletionProposal> proposals)
	{
		// order proposals by display name
		Collections.sort(proposals, new Comparator<ICompletionProposal>()
		{
			public int compare(ICompletionProposal o1, ICompletionProposal o2)
			{
				int result = getImageIndex(o1) - getImageIndex(o2);

				if (result == 0)
				{
					result = o1.getDisplayString().compareTo(o2.getDisplayString());
				}

				return result;
			}

			protected int getImageIndex(ICompletionProposal proposal)
			{
				Image image = proposal.getImage();
				int result = 0;

				if (image == JS_KEYWORD)
				{
					result = 1;
				}
				else if (image == JS_PROPERTY)
				{
					result = 2;
				}
				else if (image == JS_PROPERTY)
				{
					result = 3;
				}

				return result;
			}
		});

		// remove duplicates, merging duplicates into a single proposal
		return CollectionsUtil.filter(proposals, new ProposalMerger());
	}

	private String getNestedFunctionTypeName(JSFunctionNode function)
	{
		List<String> names = new ArrayList<String>();
		IParseNode current = function;

		while (current != null && !(current instanceof JSParseRootNode))
		{
			if (current instanceof JSFunctionNode)
			{
				JSFunctionNode currentFunction = (JSFunctionNode) current;

				names.add(currentFunction.getName().getText());
			}

			current = current.getParent();
		}

		Collections.reverse(names);

		return StringUtil.join(IJSIndexConstants.NESTED_TYPE_SEPARATOR, names);
	}

	/**
	 * getParentObjectTypes
	 * 
	 * @param node
	 * @param offset
	 * @return
	 */
	protected List<String> getParentObjectTypes(JSGetPropertyNode node, int offset)
	{
		return ParseUtil.getReceiverTypeNames(getQueryHelper(), getIndex(), getURI(), targetNode, node, offset);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.CommonContentAssistProcessor#getPreferenceNodeQualifier()
	 */
	protected String getPreferenceNodeQualifier()
	{
		return JSPlugin.PLUGIN_ID;
	}

	/**
	 * Expose replace range field for unit tests
	 * 
	 * @return
	 */
	IRange getReplaceRange()
	{
		return replaceRange;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.CommonContentAssistProcessor#isValidActivationCharacter(char, int)
	 */
	public boolean isValidActivationCharacter(char c, int keyCode)
	{
		return Character.isWhitespace(c);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.CommonContentAssistProcessor#triggerAdditionalAutoActivation(char, int,
	 * org.eclipse.jface.text.IDocument, int)
	 */
	public boolean isValidAutoActivationLocation(char c, int keyCode, IDocument document, int offset)
	{
		// NOTE: If auto-activation logic changes it may be necessary to change this logic
		// to continue walking backwards through partitions until a) a valid activation character
		// or b) a non-whitespace non-valid activation character is encountered. That implementation
		// would need to skip partitions that are effectively whitespace, for example, comment
		// partitions
		boolean result = false;

		try
		{
			ITypedRegion partition = document.getPartition(offset);
			getReplaceString(offset+1, document);
			List<String> locationTypes = new ArrayList<String>();
			List<File> locationTypeFolders = new ArrayList<File>();
			String doc = document.get(0, offset);
			doc = doc.replaceAll(" ", "").replaceAll("\t", "").replaceAll("\n", "").replaceAll("\r", "").trim();
			getLocationTypeFolders(SNIPPETS, locationTypeFolders);
			getLocationTypes(offset, locationTypes, locationTypeFolders, document, doc);

			if (partition != null && AUTO_ACTIVATION_PARTITION_TYPES.contains(partition.getType()))
			{
				int start = partition.getOffset();
				int index = offset - 1;

				while (index >= start)
				{
					char candidate = document.getChar(index);

					if (candidate == ',' || candidate == '(' || candidate == '{' 
							|| isValidateTagName(locationTypes, replaceStr)
							)
					{
						result = true;
						break;
					}
					else if (!Character.isWhitespace(candidate))
					{
						break;
					}

					index--;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return result;
	}

	private boolean isValidateTagName(List<String> locationTypes, String replaceStr) {
		for(String type : locationTypes){
			if(replaceStr.toLowerCase().startsWith(type.toLowerCase()) || type.toLowerCase().startsWith(replaceStr.toLowerCase())){
				return true;
			}
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.CommonContentAssistProcessor#isValidIdentifier(char, int)
	 */
	public boolean isValidIdentifier(char c, int keyCode)
	{
		return Character.isJavaIdentifierStart(c) || Character.isJavaIdentifierPart(c) || c == '$';
	}

	/**
	 * The currently active range
	 * 
	 * @param activeRange
	 */
	public void setActiveRange(IRange activeRange)
	{
		this.activeRange = activeRange;
	}

	private Object getSubFolder(String input, List<File> locationTypeFolders){
		String sub = "";

		if(input.trim().equals("") || input == null){
			return null;
		}

		if(!locationTypeFolders.isEmpty()){
			for(Iterator<File> iter = locationTypeFolders.iterator();iter.hasNext();){
				File folder = (File) iter.next();
				String name = folder.getName();

				if ((name + ".").toLowerCase().startsWith(input.toLowerCase())) {
					return folder;
				}
			}
		}

		if(("api.").startsWith(input)){
			return "";
		}else if("$api.".startsWith(input)){
			sub = new String("/apijs");
		}

		return sub;
	}

	private List<Object> getSubFolders(String input, List<File> locationTypeFolders){
		List<Object> subs = new ArrayList<Object>();

		if(!locationTypeFolders.isEmpty()){
			for(Iterator<File> iter = locationTypeFolders.iterator();iter.hasNext();){
				File folder = (File) iter.next();
				String name = folder.getName();

				if ( input.startsWith(name+".")) {
					subs.add(folder);
					isGlobal = false;
				}

				if (name.toLowerCase().startsWith(input.toLowerCase())){
					subs.add(folder);
					isGlobal = true;
				}
			}
		}

		if ( input.startsWith("api.")) {
			subs.add("");
			isGlobal = false;
		} else if ("$api.".startsWith(input) || input.startsWith("$api.")) {
			subs.add(new String("/apijs"));
			isGlobal = false;
		}

		return subs;
	}

	private void addApicloudProposals(Set<ICompletionProposal> proposals, List<File> locationTypeFolders, int offset, String input)
	{
		try {
			File snippetsFolder = null;

			List<Object> objects = getSubFolders(input, locationTypeFolders);

			if(!objects.isEmpty()){
				for(Object object: objects){
					if(object instanceof String){
						if(object.toString().equals("")){
							snippetsFolder = new File(SNIPPETS);
						}else{
							snippetsFolder = new File(SNIPPETS + "/"+ object.toString());
						}
					}

					if(object instanceof File){
						snippetsFolder = (File)object;
					}

					if(snippetsFolder.exists()){
						addApiSnippetsProposals(proposals, offset, input, snippetsFolder, false);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addApiSnippetsProposals(Set<ICompletionProposal> proposals, int offset, String input, File snippetsFolder, boolean isRequire) throws DocumentException, Exception {
		Document doc = null;

		if(snippetsFolder.exists())
		{
			File[] files = snippetsFolder.listFiles();

			for (File file : files) {
				String tabTrigger = null;
				String value = null;
				String tagName = null;
				String description = null;
				String data = null;

				if (file.isFile()) {
					doc = XMLUtil.loadXmlFile(file);

					Pattern group1 = Pattern.compile("\\w+\\.");
					Matcher m1 = group1.matcher(input);
					Pattern group2 = Pattern.compile("\\w+$");
					Matcher m2 = group2.matcher(input);
					Pattern group3 = Pattern.compile("\\w+\\..+");
					Matcher m3 = group3.matcher(input);

					if (doc != null) {
						Element element = doc.getRootElement();

						List<Object> results = new ArrayList<Object>();

						XMLUtil.parseXML("tabTrigger", element, results);

						if (!results.isEmpty()) {
							if (results.toArray().length == 1) {
								tabTrigger = ((Element) results.get(0)).getStringValue().trim();
								value = tabTrigger.replace("-", ".");

								if (value.contains("-") && value.lastIndexOf("-") < value.length()) {
									tagName = value.substring(value.lastIndexOf("-") + 1);
								} else {
									tagName = value;
								}

								if(tagName.startsWith("apijs") && replaceStr.startsWith("$")){
									tagName = tagName.replaceFirst("^apijs", "\\$api");
								}

								if(!(tagName.toLowerCase().startsWith(input.toLowerCase()) || isRequire)){
									continue;
								}else if(isRequire){
									if(!(tagName.toLowerCase().startsWith(input.toLowerCase()))){
										if(m1.matches()){
											tagName = tagName.replaceFirst("\\w+\\.", input);
										}else if(m2.matches()){
											tagName = tagName.replaceFirst("\\w+", input);
										}else if(m3.matches()){
											Matcher m = Pattern.compile("(\\w+)\\..+").matcher(tagName);

											if(m.find()){
												String replaceName = m.replaceFirst("$1");
												String inputKey = Pattern.compile("(\\w+)\\..+").matcher(input).replaceFirst("$1");
												tagName = tagName.replaceFirst(replaceName, inputKey);
											}
										}
									}
								}
							}

							results.clear();
						}

						XMLUtil.parseXML("description", element, results);

						if (!results.isEmpty()) {
							if (results.toArray().length == 1) {
								description = ((Element) results.get(0)).getStringValue().trim();
							}

							results.clear();
						}else{
							description = "empty";
						}

						XMLUtil.parseXML("content", element, results);

						if( !results.isEmpty()){
							if (results.toArray().length == 1) {
								data = ((Element) results.get(0)).getStringValue().trim();

								if(data.startsWith("\\")){
									data = data.replaceFirst("^\\\\w*", "");
								}

								if(data.contains("//")){
									data = data.replaceAll("//", "%%");
								}

								if(data.contains("$"))
								{
									Pattern pattern1 = Pattern.compile("\\$\\{\\d+:(\\w+|\\s+)\\}(,\\s)");
									Matcher matcher1 = pattern1.matcher(data.trim());

									if(matcher1.find()){
										data = matcher1.replaceAll("$1$2");
									}

									Pattern pattern2 = Pattern.compile("\\$\\{\\d+:(\\w+|\\s+)\\}(\\))");
									Matcher matcher2 = pattern2.matcher(data.trim());

									if(matcher2.find()){
										data = matcher2.replaceAll("$1$2");
									}

									Pattern pattern3 = Pattern.compile("\\$\\{\\d+:(\\w+|\\s+)\\}(\\s)");
									Matcher matcher3 = pattern3.matcher(data.trim());

									if (matcher3.find()) {
										data = matcher3.replaceAll("$1$2");
									}
								}
							}

							if(data.contains("$")){
								Pattern pattern4 = Pattern.compile("(')\\$\\{\\d+:(\\w+|\\s+|\\S+)\\}(')");
								Matcher matcher4 = pattern4.matcher(data.trim());

								if(matcher4.find()){
									data = matcher4.replaceAll("$1$2$3");
								}
							}

							if(data.contains("$")){
								Pattern pattern5 = Pattern.compile("\\$\\{\\d+}");
								Matcher matcher5 = pattern5.matcher(data.trim());

								if(matcher5.find()){
									data = matcher5.replaceAll("");
								}
							}

							if(isRequire){
								if(group3.matcher(data).find()){
									data = data.replaceFirst("\\w+\\.\\w+", tagName);
								}
							}

							if(data.contains("$")){
								Pattern pattern6 = Pattern.compile("\\$\\{\\d+:(%%\\s+\\w+|\\s+)\\s+\\}");
								Matcher matcher6 = pattern6.matcher(data.trim());

								if(matcher6.find()){
									data = matcher6.replaceAll("$1");
								}
							}

							if(data.contains("$")){
								Pattern pattern7 = Pattern.compile("\\$\\{\\d+:\\}");
								Matcher matcher7 = pattern7.matcher(data.trim());

								if(matcher7.find()){
									data = matcher7.replaceAll("");
								}
							}

							if(data.contains("$")){
								Pattern pattern8 = Pattern.compile("\\$\\{\\d+:(\\d+\\.\\d+)\\}");
								Matcher matcher8 = pattern8.matcher(data.trim());

								if(matcher8.find()){
									data = matcher8.replaceAll("$1");
								}
							}

							if(data.contains("$")){
								Pattern pattern9 = Pattern.compile("\\$\\{\\d+:(\\[.+\\])\\}");
								Matcher matcher9 = pattern9.matcher(data.trim());

								if(matcher9.find()){
									data = matcher9.replaceAll("$1");
								}
							}
							if(data.contains("$")){
								Pattern pattern10 = Pattern.compile("\\$\\{\\d+:(\\'.+\\')\\}");
								Matcher matcher10 = pattern10.matcher(data.trim());

								if(matcher10.find()){
									data = matcher10.replaceAll("$1");
								}
							}
							if(data.contains("$")){
								Pattern pattern11 = Pattern.compile("\\$\\{\\d+:(\\w+|\\s+)\\}");
								Matcher matcher11 = pattern11.matcher(data.trim());

								if(matcher11.find()){
									data = matcher11.replaceAll("$1");
								}
							}
							if(data.contains("$")){
								Pattern pattern12 = Pattern.compile("(')\\$\\{\\d+:(\\w+\\s\\w+)\\}(')");
								Matcher matcher12 = pattern12.matcher(data.trim());

								if(matcher12.find()){
									data = matcher12.replaceAll("$1$2$3");
								}
							}
							if(data.contains("$")){
								Pattern pattern13 = Pattern.compile("(')\\$\\{\\d+:(\\d+-\\d+-\\d+\\s+\\d+:\\d+)\\}(')");
								Matcher matcher13 = pattern13.matcher(data.trim());

								if(matcher13.find()){
									data = matcher13.replaceAll("$1$2$3");
								}
							}
							if(data.contains("%%")){
								data = data.replaceAll("%%", "//");
							}
							results.clear();
						}

						if(data.equals(tagName) || tagName.endsWith(data)){
							addProposal(proposals, tagName, input.trim(), JS_PROPERTY, description, getActiveUserAgentIds(), getFilePath(), offset);
						}else{
							addApicloudProposal(proposals, tagName, input.trim(), data, JS_FUNCTION, description, getActiveUserAgentIds(), getFilePath(), offset);
						}
					}
				}
			}
		}
	}
}
