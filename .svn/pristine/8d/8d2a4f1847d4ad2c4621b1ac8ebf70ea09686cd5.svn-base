/**
d * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.js.contentassist;

import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
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
import com.aptana.js.core.model.ParameterElement;
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
//	private static final Image JS_PROPERTY = JSPlugin.getImage("/icons/overlays/static.png"); //$NON-NLS-1$
	private static final Image JS_KEYWORD = JSPlugin.getImage("/icons/keyword.png"); //$NON-NLS-1$

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
	private void addKeywords(Set<ICompletionProposal> proposals, int offset)
	{
		for (String name : JSLanguageConstants.KEYWORDS)
		{
			String description = MessageFormat.format(Messages.JSContentAssistProcessor_KeywordDescription, name);
			addProposal(proposals, name, JS_KEYWORD, description, getActiveUserAgentIds(),
					Messages.JSContentAssistProcessor_KeywordLocation, offset);
		}
		addProposal(proposals, "api", JS_PROPERTY, "APICloud Object", getActiveUserAgentIds(),
				"APICloud Object", offset);
		addProposal(proposals, "$api", JS_PROPERTY, "APICloud Object", getActiveUserAgentIds(),
				"APICloud Object", offset);
	}

	/**
	 * addObjectLiteralProperties
	 * 
	 * @param proposals
	 * @param offset
	 */
	protected void addObjectLiteralProperties(Set<ICompletionProposal> proposals, ITextViewer viewer, int offset, int length)
	{
		String location = getString(length);
		JSGetPropertyNode node = ParseUtil.getGetPropertyNode(targetNode, statementNode);
		List<String> types = getParentObjectTypes(node, offset);

		// add all properties of each type to our proposal list
		for (String type : types)
		{
			addTypeProperties(proposals, type, offset);
		}
		JSScope localScope = ParseUtil.getScopeAtOffset(targetNode, offset);
		createApicloudObjectProposals(proposals, node.value.toString(), offset, location);
		
		node.getLeftHandSide();
		String value = node.getLeftHandSide().getText();
		node.getLeftHandSide().getChildren();
		JSPropertyCollection jsc = (JSPropertyCollection)localScope.getObject().getProperty(value);
		if(jsc != null) {
			List<JSInvokeNode> iNodes = new ArrayList<JSInvokeNode>(); 
			for(JSNode jsn : jsc.getValues()) {
				if(jsn instanceof JSInvokeNode) {
					iNodes.add((JSInvokeNode)jsn);
				}
			}
			value = iNodes.size() == 0 ? "" : iNodes.get(iNodes.size() - 1).value.toString();
			iNodes.clear();
			System.out.println(value);
			if(value.equals("api.require('db')") || value.equals("api.require(\"db\")")) {
				proposals.clear();
				return;
			}
		
		}
		
		FunctionElement function = getFunctionElement(viewer, offset);

		if (function != null)
		{
			List<ParameterElement> params = function.getParameters();
			int index = getArgumentIndex(offset);

			if (0 <= index && index < params.size())
			{
				ParameterElement param = params.get(index);
				URI projectURI = getProjectURI();

				for (String type : param.getTypes())
				{
					Collection<PropertyElement> properties = getQueryHelper().getTypeProperties(type);

					for (PropertyElement property : CollectionsUtil.filter(properties, isVisibleFilter))
					{
						addProposal(proposals, property, offset, projectURI, null);
					}
				}
			}
		}
	}

	private void createApicloudObjectProposals(Set<ICompletionProposal> proposals, String value, int offset, String location)
	{
		if("api.installApp".equals(value))
			createObj4InstallApp(proposals, offset, location);
		if("api.openApp".equals(value))
			createObj4OpenApp(proposals, offset, location);
		if("api.openWidget".equals(value))
			createObj4OpenWidget(proposals, offset, location);
		if("api.closeWidget".equals(value))
			createObj4CloseWidget(proposals, offset, location);
		if("api.openWin".equals(value))
			createObj4OpenWin(proposals, offset, location);
		if("api.setWinAttr".equals(value))
			createObj4SetWinAttr(proposals, offset, location);
		if("api.closeWin".equals(value))
			createObj4CloseWin(proposals, offset, location);
		if("api.closeToWin".equals(value))
			createObj4CloseToWin(proposals, offset, location);
		if("api.execScript".equals(value))
			createObj4ExecScript(proposals, offset, location);
		if("api.openFrame".equals(value))
			createObj4OpenFrame(proposals, offset, location);
		if("api.setFrameAttr".equals(value))
			createObj4SetFrameAttr(proposals, offset, location);
		if("api.closeFrame".equals(value))
			createObj4CloseFrame(proposals, offset, location);
		if("api.bringFrameToFront".equals(value))
			createObj4BbringFrameToFront(proposals, offset, location, "bringFrameToFront Param");

		if("api.sendFrameToBack".equals(value))
			createObj4BbringFrameToFront(proposals, offset, location, "sendFrameToBack Param");
		if("api.animation".equals(value))
			createObj4Animation(proposals, offset, location);
		if("api.openFrameGroup".equals(value))
			createObj4OpenFrameGroup(proposals, offset, location);
		if("api.closeFrameGroup".equals(value))
			createObj4CloseFrameGroup(proposals, offset, location);
		if("api.setFrameGroupIndex".equals(value))
			createObj4SetFrameGroupIndex(proposals, offset, location);
		if("api.setFrameGroupAttr".equals(value))
			createObj4SetFrameGroupAttr(proposals, offset, location);
		if("api.openSlidLayout".equals(value))
			createObj4OpenSlidLayout(proposals, offset, location);
		if("api.openSlidPane".equals(value))
			createObj4OpenSlidPane(proposals, offset, location);
//		if("api.closeSlidPane".equals(value))
//			createObj4CloseSlidPane(proposals, offset, location);
		if("api.addEventListener".equals(value))
			createObj4AddEventListener(proposals, offset, location, "addEventListener Param");
		if("api.removeEventListener".equals(value))
			createObj4AddEventListener(proposals, offset, location, "removeEventListener Param");
		if("api.setRefreshHeaderInfo".equals(value))
			createObj4SetRefreshHeaderInfo(proposals, offset, location);
		if("api.alert".equals(value))
			createObj4Alert(proposals, offset, location, "alert Param", "[\"确定\"]");
		if("api.confirm".equals(value))
			createObj4Alert(proposals, offset, location, "confirm Param", "[\"确定\", \"取消\"]");
		if("api.prompt".equals(value))
			createObj4Alert(proposals, offset, location, "prompt Param", "[\"确定\", \"取消\"]");
		if("api.showProgress".equals(value))
			createObj4ShowProgress(proposals, offset, location);
		if("api.toast".equals(value))
			createObj4Toast(proposals, offset, location);
		if("api.setPrefs".equals(value))
			createObj4SetPrefs(proposals, offset, location);
		if("api.getPrefs".equals(value))
			createObj4GetPrefs(proposals, offset, location, "getPrefs Param");
		if("api.removePrefs".equals(value))
			createObj4GetPrefs(proposals, offset, location,  "removePrefs Param");
		if("api.getPicture".equals(value))
			createObj4GetPicture(proposals, offset, location);
		if("api.ajax".equals(value))
			createObj4Ajax(proposals, offset, location);
		if("api.download".equals(value))
			createObj4Download(proposals, offset, location);
		if("api.cancelDownload".equals(value))
			createObj4CancelDownload(proposals, offset, location);
		if("api.call".equals(value))
			createObj4Call(proposals, offset, location);
		if("api.sms".equals(value))
			createObj4Sms(proposals, offset, location);
		if("api.mail".equals(value))
			createObj4Mail(proposals, offset, location);
		if("api.readFile".equals(value))
			createObj4ReadFile(proposals, offset, location);
		if("api.writeFile".equals(value))
			createObj4WriteFile(proposals, offset, location);
		if("api.startRecord".equals(value))
			createObj4StartRecord(proposals, offset, location);
		if("api.startPlay".equals(value))
			createObj4StartPlay(proposals, offset, location);
		if("api.startLocation".equals(value))
			createObj4StartLocation(proposals, offset, location);
		if("api.startSensor".equals(value))
			createObj4StartSensor(proposals, offset, location, "startSensor Param");
		if("api.stopSensor".equals(value))
			createObj4StartSensor(proposals, offset, location, "stopSensor Param");
		if("api.setStatusBarStyle".equals(value))
			createObj4SetStatusBarStyle(proposals, offset, location);
		if("api.setFullScreen".equals(value))
			createObj4SetFullScreenStyle(proposals, offset, location);
		if("api.actionSheet".equals(value))
			createObj4ActionSheet(proposals, offset, location);
		if("api.openPicker".equals(value))
			createObj4OpenPicker(proposals, offset, location);
		if("api.showFloatBox".equals(value))
			createObj4ShowFloatBox(proposals, offset, location);
		if("api.notification".equals(value))
			createObj4Notification(proposals, offset, location);
	}
	private void createObj4Notification(Set<ICompletionProposal> proposals, int offset, String location)
	{
				addApicloudProposal(proposals, "vibrate", 
						showParam(true, "vibrate", "[500,500]")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "vibrate： 伴随节奏的震动，时间数组，时间单位为毫秒<br>"
						, getActiveUserAgentIds(), "notification Param", offset);
		addApicloudProposal(proposals, "sound", 
				showParam(true, "sound", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "sound：提示音文件路径，不传默认使用系统提示音<br>"
				, getActiveUserAgentIds(), "notification Param", offset);
		addApicloudProposal(proposals, "light", 
				showParam(true, "light", "false")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "light：设备提示灯是否闪烁<br>"
				, getActiveUserAgentIds(), "notification Param", offset);
		addApicloudProposal(proposals, "notify", 
				showParam(true, "notify", "{}")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "notify：弹出通知到状态栏<br>"
				, getActiveUserAgentIds(), "notification Param", offset);
	}
	private void createObj4ShowFloatBox(Set<ICompletionProposal> proposals, int offset, String location)
	{
				addApicloudProposal(proposals, "iconPath", 
						showParam(true, "iconPath", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "iconPath： 展示在悬浮框中的图片地址（字符串类型）默认值：应用图标<br>"
						, getActiveUserAgentIds(), "showFloatBox Param", offset);
		addApicloudProposal(proposals, "duration", 
				showParam(true, "duration", "5000")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "duration： 自动消隐时长。在该时长内不发生触摸悬浮框行为，<br>" +
						"悬浮框自动消隐至半透状态（字符串类型）默认值：5000毫秒<br>"
				, getActiveUserAgentIds(), "showFloatBox Param", offset);
	}

	private void createObj4OpenPicker(Set<ICompletionProposal> proposals, int offset, String location)
	{
		
				addApicloudProposal(proposals, "type", 
						showParam(true, "type", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "type： 拾取器类型（详见拾取器类型常量，字符串类型），不能为空 默认值：无<br>"
						, getActiveUserAgentIds(), "openPicker Param", offset);
		addApicloudProposal(proposals, "date", 
				showParam(true, "date", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "date： 时间格式化字符串，格式yyyy-MM-dd HH:mm（字符串类型）默认值：当前时间<br>"
				, getActiveUserAgentIds(), "openPicker Param", offset);
	}

	private void createObj4ActionSheet(Set<ICompletionProposal> proposals, int offset, String location)
	{
				addApicloudProposal(proposals, "title", 
						showParam(true, "title", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "title： 标题（字符串类型）默认值：无<br>"
						, getActiveUserAgentIds(), "actionSheet Param", offset);
				addApicloudProposal(proposals, "cancelTitle", 
						showParam(true, "cancelTitle", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "cancelTitle： 取消按钮标题（字符串类型）默认值：无<br>"
						, getActiveUserAgentIds(), "actionSheet Param", offset);
				addApicloudProposal(proposals, "destructiveTitle", 
						showParam(true, "destructiveTitle", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "destructiveTitle：红色警示按钮标题（字符串类型）默认值：无<br>"
						, getActiveUserAgentIds(), "actionSheet Param", offset);
				addApicloudProposal(proposals, "buttons", 
						showParam(true, "buttons", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "buttons ：其它按钮（数组类型）默认值：无<br>"
						, getActiveUserAgentIds(), "actionSheet Param", offset);
	}

	private void createObj4SetFullScreenStyle(Set<ICompletionProposal> proposals, int offset, String location)
	{
		
		addApicloudProposal(proposals, "fullScreen", 
				showParam(true, "fullScreen", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "fullScreen：是否全屏（布尔类型），不能为空 默认值：无"
				, getActiveUserAgentIds(), "setFullScreen Param", offset);
	}

	private void createObj4SetStatusBarStyle(Set<ICompletionProposal> proposals, int offset, String location)
	{
		
		addApicloudProposal(proposals, "style", 
				showParam(true, "style", "'dark'")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "style： 状态栏样式（详见状态栏样式常量，字符串）默认值：dark<br>"
				, getActiveUserAgentIds(), "setStatusBarStyle Param", offset);
	}

	private void createObj4StartSensor(Set<ICompletionProposal> proposals, int offset, String location, String type)
	{
		
		addApicloudProposal(proposals, "type", 
				showParam(true, "type", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "type：传感器类型(详见传感器类型传感器类型常量)	不能为空 默认值：无<br>"
				, getActiveUserAgentIds(), type, offset);
	}

	private void createObj4StartLocation(Set<ICompletionProposal> proposals, int offset, String location)
	{
				addApicloudProposal(proposals, "accuracy", 
						showParam(true, "accuracy", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "accuracy：精度(详见定位精度常量，字符串类型)	不能为空 默认值：无<br>"
						, getActiveUserAgentIds(), "startLocation Param", offset);
				addApicloudProposal(proposals, "filter", 
						showParam(true, "filter", "1.0")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "filter：位置更新所需最小距离(数字类型，单位米) 默认值：1.0<br>"
						, getActiveUserAgentIds(), "startLocation Param", offset);
				addApicloudProposal(proposals, "autoStop", 
						showParam(true, "autoStop", "true")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "autoStop：获取到位置信息后是否自动停止定位（布尔类型）默认为true<br>"
						, getActiveUserAgentIds(), "startLocation Param", offset);
	}

	private void createObj4StartPlay(Set<ICompletionProposal> proposals, int offset, String location)
	{
		addApicloudProposal(proposals, "path", 
				showParam(true, "path", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "path：文件路径（字符串类型）不能为空 默认值：无<br>"
				, getActiveUserAgentIds(), "startPlay Param", offset);
		
	}

	private void createObj4StartRecord(Set<ICompletionProposal> proposals, int offset, String location)
	{
		addApicloudProposal(proposals, "path", 
				showParam(true, "path", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "path：文件路径，为空时自动创建路径（字符串类型）默认值：无<br>"
				, getActiveUserAgentIds(), "startRecord Param", offset);
		
	}

	private void createObj4WriteFile(Set<ICompletionProposal> proposals, int offset, String location)
	{
		addApicloudProposal(proposals, "path", 
				showParam(true, "path", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "path：文件路径（字符串类型）不能为空 默认值：无<br>"
				, getActiveUserAgentIds(), "writeFile Param", offset);
		addApicloudProposal(proposals, "data", 
				showParam(true, "data", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "data：文件内容（字符串类型）不能为空 默认值：无<br>"
				, getActiveUserAgentIds(), "writeFile Param", offset);
	
	}

	private void createObj4ReadFile(Set<ICompletionProposal> proposals, int offset, String location)
	{
		
		addApicloudProposal(proposals, "path", 
				showParam(true, "path", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "path：文件路径（字符串类型）不能为空 默认值：无<br>"
				, getActiveUserAgentIds(), "readFile Param", offset);
	}

	private void createObj4Mail(Set<ICompletionProposal> proposals, int offset, String location)
	{
				addApicloudProposal(proposals, "recipients", 
						showParam(true, "recipients", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "recipients：收件人（数组类型）不能为空 默认值：无<br>" 
						, getActiveUserAgentIds(), "mail Param", offset);
				addApicloudProposal(proposals, "subject", 
						showParam(true, "subject", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "subject：邮件主题（字符串类型）不能为空 默认值：无<br>" 
						, getActiveUserAgentIds(), "mail Param", offset);
				addApicloudProposal(proposals, "body", 
						showParam(true, "body", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "body：邮件内容（字符串类型）默认值：无<br>" 
						, getActiveUserAgentIds(), "mail Param", offset);
				addApicloudProposal(proposals, "attachments", 
						showParam(true, "attachments", "[]")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "attachments：附件地址（数组类型）默认值：无<br>" 
						, getActiveUserAgentIds(), "mail Param", offset);
	}

	private void createObj4Sms(Set<ICompletionProposal> proposals, int offset, String location)
	{
				addApicloudProposal(proposals, "numbers", 
						showParam(true, "numbers", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "numbers：电话号码（数组类型）不能为空 默认值：无<br>"
						, getActiveUserAgentIds(), "sms Param", offset);
		addApicloudProposal(proposals, "text", 
				showParam(true, "text", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "text：文本内容（字符串类型）不能为空 默认值：无<br>" 
				, getActiveUserAgentIds(), "sms Param", offset);
		addApicloudProposal(proposals, "silent", 
				showParam(true, "silent", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "silent：是否后台发送，iOS不支持（布尔类型） 默认值：无<br>" 
				, getActiveUserAgentIds(), "sms Param", offset);
	}

	private void createObj4Call(Set<ICompletionProposal> proposals, int offset, String location)
	{
				addApicloudProposal(proposals, "type", 
						showParam(true, "type", "'tel_prompt'")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "type：打电话类型（详见电话类型常量，字符串类型）默认值：tel_prompt<br>"
						, getActiveUserAgentIds(), "call Param", offset);
				addApicloudProposal(proposals, "number", 
						showParam(true, "number", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "number：电话号码（字符串类型）不能为空，默认值：无<br>" 
						, getActiveUserAgentIds(), "call Param", offset);
	}

	private void createObj4CancelDownload(Set<ICompletionProposal> proposals, int offset, String location)
	{
		addApicloudProposal(proposals, "url", 
				showParam(true, "url", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "url：下载地址（字符串类型），不能为空 默认值：无<br>"
				, getActiveUserAgentIds(), "cancelDownload Param", offset);
		
	}

	private void createObj4Download(Set<ICompletionProposal> proposals, int offset, String location)
	{
				addApicloudProposal(proposals, "url", 
						showParam(true, "url", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "url：下载地址（字符串类型），不能为空 默认值：无<br>"
						, getActiveUserAgentIds(), "download Param", offset);
				addApicloudProposal(proposals, "savePath", 
						showParam(true, "savePath", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "savePath：存储路径，为空时使用自动创建的路径（字符串类型）默认值：无<br>" 
						, getActiveUserAgentIds(), "download Param", offset);
				addApicloudProposal(proposals, "report", 
						showParam(true, "report", "false")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "report：下载过程是否上报（布尔类型）默认值：false <br>" 
						, getActiveUserAgentIds(), "download Param", offset);
				addApicloudProposal(proposals, "cache", 
						showParam(true, "cache", "true")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "cache：是否使用本地缓存（布尔类型）默认值：true<br>" 
						, getActiveUserAgentIds(), "download Param", offset);
	}

	private void createObj4Ajax(Set<ICompletionProposal> proposals, int offset, String location)
	{
				
				addApicloudProposal(proposals, "url", 
						showParam(true, "url", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "url：地址（字符串类型）不能为空 默认值：无<br>"
						, getActiveUserAgentIds(), "ajax Param", offset);
				addApicloudProposal(proposals, "method", 
						showParam(true, "method", "'get'")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "method：异步请求方法类型(详见异步请求方法类型常量，字符串)默认值：get<br>" 
						, getActiveUserAgentIds(), "ajax Param", offset);
				addApicloudProposal(proposals, "cache", 
						showParam(true, "cache", "false")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "cache：是否缓存（布尔类型）默认值：false<br>"
						, getActiveUserAgentIds(), "ajax Param", offset);
				addApicloudProposal(proposals, "timeout", 
						showParam(true, "timeout", "30")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "	timeout：超时时间，单位秒（数字类型）默认值：30<br>"
						, getActiveUserAgentIds(), "ajax Param", offset);
				addApicloudProposal(proposals, "dataType", 
						showParam(true, "dataType", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "dataType：返回数据类型（详见异步请求返回数据类型常量，字符串）默认值：json<br>" 
						, getActiveUserAgentIds(), "ajax Param", offset);
				addApicloudProposal(proposals, "headers", 
						showParam(true, "headers", "")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "headers：请求头(JSON对象) 默认值：无<br>"
						, getActiveUserAgentIds(), "ajax Param", offset);
				addApicloudProposal(proposals, "data", 
						showParam(true, "data", "{}")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "data：POST数据，method为”get”时不传（JSON对象）包含以下3个参数：<br>"
								+ "body：请求体（字符串类型）默认值：空<br>"
								+ "values：Post参数（JSON对象）默认值：空<br>" 
								+ "files：Post文件（JSON对象）默认值：空<br>"
						, getActiveUserAgentIds(), "ajax Param", offset);
	}

	private void createObj4GetPicture(Set<ICompletionProposal> proposals, int offset, String location)
	{
				addApicloudProposal(proposals, "sourceType", 
						showParam(true, "sourceType", "'library'")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "sourceType：图片源类型（详见图片源类型常量，字符串） 默认值:library<br>"
						, getActiveUserAgentIds(), "getPicture Param", offset);
				addApicloudProposal(proposals, "encodingType", 
						showParam(true, "encodingType", "'png'")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "encodingType：编码格式（详见图片编码类型常量，字符串）默认值：png<br>"
						, getActiveUserAgentIds(), "getPicture Param", offset);
				addApicloudProposal(proposals, "mediaValue", 
						showParam(true, "mediaValue", "'pic'")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "	mediaValue：媒体类型（详见媒体类型常量，字符串）默认值：pic<br>" 
						, getActiveUserAgentIds(), "getPicture Param", offset);
				addApicloudProposal(proposals, "destinationType", 
						showParam(true, "destinationType", "'url'")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "destinationType：返回数据类型（详见图片数据格式常量，字符串）默认值：url<br>"
						, getActiveUserAgentIds(), "getPicture Param", offset);
				addApicloudProposal(proposals, "allowEdit", 
						showParam(true, "allowEdit", "false")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "	allowEdit：是否可以编辑（布尔类型）默认值：false<br>"
						, getActiveUserAgentIds(), "getPicture Param", offset);
				addApicloudProposal(proposals, "quality", 
						showParam(true, "quality", "50")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "quality：图片质量0-100整数（数字类型）默认值：50<br>" 
						, getActiveUserAgentIds(), "getPicture Param", offset);
				addApicloudProposal(proposals, "targetWidth", 
						showParam(true, "targetWidth", "")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "targetWidth：压缩后的图片宽度（数字）默认值：原图宽度<br>" 
						, getActiveUserAgentIds(), "getPicture Param", offset);
				addApicloudProposal(proposals, "targetHeight", 
						showParam(true, "targetHeight", "")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "targetHeight：压缩后的图片高度（数字）默认值：原图高度<br>" 
						, getActiveUserAgentIds(), "getPicture Param", offset);
				addApicloudProposal(proposals, "saveToPhotoAlbum", 
						showParam(true, "saveToPhotoAlbum", "false")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "	saveToPhotoAlbum：是否保存到相册（布尔类型）默认值：false<br>"
						, getActiveUserAgentIds(), "getPicture Param", offset);
	}

	private void createObj4GetPrefs(Set<ICompletionProposal> proposals, int offset, String location, String type)
	{
		addApicloudProposal(proposals, "key", 
				showParam(true, "key", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "key：键（字符串类型）不能为空 默认值：无<br>"
				, getActiveUserAgentIds(), type, offset);
		
	}

	private void createObj4SetPrefs(Set<ICompletionProposal> proposals, int offset, String location)
	{
		
				
				addApicloudProposal(proposals, "key", 
						showParam(true, "key", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "key：键（字符串类型）不能为空 默认值：无<br>"
						, getActiveUserAgentIds(), "setPrefs Param", offset);
				addApicloudProposal(proposals, "value", 
						showParam(true, "value", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "	value：值（字符串类型）不能为空 默认值：无<br>" 
						, getActiveUserAgentIds(), "setPrefs Param", offset);
	}

	private void createObj4Toast(Set<ICompletionProposal> proposals, int offset, String location)
	{
				addApicloudProposal(proposals, "msg", 
						showParam(true, "msg", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "msg：提示消息（字符串类型）不能为空 默认值：无<br>"
						, getActiveUserAgentIds(), "toast Param", offset);
				addApicloudProposal(proposals, "duration", 
						showParam(true, "duration", "2000")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "duration：持续时长，单位：毫秒（字符串类型） 默认值：2000<br>"
						, getActiveUserAgentIds(), "toast Param", offset);
				addApicloudProposal(proposals, "location", 
						showParam(true, "location", "'bottom'")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "location：弹出位置（字符串类型） 默认值：bottom<br>"
						, getActiveUserAgentIds(), "toast Param", offset);
	}

	private void createObj4ShowProgress(Set<ICompletionProposal> proposals, int offset, String location)
	{
				addApicloudProposal(proposals, "style", 
						showParam(true, "style", "'default'")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "style：风格（详见进度提示框风格常量，字符串） 默认值为：default<br>"
						, getActiveUserAgentIds(), "showProgress Param", offset);
				addApicloudProposal(proposals, "animationType", 
						showParam(true, "animationType", "'fade'")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "animationType:动画类型（详见进度提示框动画类型常量，字符串） 默认值为:fade<br>" 
						, getActiveUserAgentIds(), "showProgress Param", offset);
				addApicloudProposal(proposals, "title", 
						showParam(true, "title", "\"加载中\"")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "	title：标题（字符串类型）默认值：\"加载中\"<br>" 
						, getActiveUserAgentIds(), "showProgress Param", offset);
				addApicloudProposal(proposals, "text", 
						showParam(true, "text", "\"请稍后...\"")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "	text：文本（字符串类型）默认值：\"请稍后...\"<br>"
						, getActiveUserAgentIds(), "showProgress Param", offset);
				addApicloudProposal(proposals, "modal", 
						showParam(true, "modal", "true")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "	modal：是否模态（布尔类型）默认值：true<br>"
						, getActiveUserAgentIds(), "showProgress Param", offset);
	}

	private void createObj4Alert(Set<ICompletionProposal> proposals, int offset, String location, String type, String def)
	{
				addApicloudProposal(proposals, "title", 
						showParam(true, "title", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "title：标题（字符串类型）默认值：无<br>"
						, getActiveUserAgentIds(), type, offset);
		addApicloudProposal(proposals, "msg", 
				showParam(true, "msg", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "msg：内容（字符串类型）默认值：无<br>" 
				, getActiveUserAgentIds(), type, offset);
		addApicloudProposal(proposals, "buttons", 
				showParam(true, "buttons", def)
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "buttons：按钮（数组类型）默认值：<br>"
				, getActiveUserAgentIds(), type, offset);
	}

	private void createObj4SetRefreshHeaderInfo(Set<ICompletionProposal> proposals, int offset, String location)
	{
				addApicloudProposal(proposals, "visible", 
						showParam(true, "visible", "true")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "visible：是否可见（布尔类型）	默认值：true<br>"
						, getActiveUserAgentIds(), "setRefreshHeaderInfo Param", offset);
		addApicloudProposal(proposals, "loadingImgae", 
				showParam(true, "loadingImgae", "")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "loadingImgae：刷新图片地址（字符串类型）	默认值：旋转箭头图片<br>" 
				, getActiveUserAgentIds(), "setRefreshHeaderInfo Param", offset);
		addApicloudProposal(proposals, "bgColor", 
				showParam(true, "bgColor", "'rgb(187,236,153,1.0)'")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "bgColor：背景颜色（#fff,#ffffff,rgba(r,g,b,a)，字符串类型）	默认值：rgb(187,236,153,1.0)<br>"
				, getActiveUserAgentIds(), "setRefreshHeaderInfo Param", offset);
		addApicloudProposal(proposals, "textColor", 
				showParam(true, "textColor", "'rgb(109, 128, 153,1.0)'")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "textColor：文字颜色（#fff,#ffffff,rgba(r,g,b,a)，字符串类型）	默认值：rgb(109, 128, 153,1.0)<br>"
				, getActiveUserAgentIds(), "setRefreshHeaderInfo Param", offset);
		addApicloudProposal(proposals, "textDown", 
				showParam(true, "textDown", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "textDown	：下拉文字描述（字符串类型）默认值：下拉可以刷新...<br>"
				, getActiveUserAgentIds(), "setRefreshHeaderInfo Param", offset);
		addApicloudProposal(proposals, "textUp", 
				showParam(true, "textUp", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "textUp：松开时文字描述（字符串类型）默认值：松开可以刷新...<br>"
				, getActiveUserAgentIds(), "setRefreshHeaderInfo Param", offset);
		addApicloudProposal(proposals, "showTime", 
				showParam(true, "showTime", "true")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "	showTime：是否显示更新时间（布尔类型）	默认值：true<br>"		
				, getActiveUserAgentIds(), "setRefreshHeaderInfo Param", offset);
	}

	private void createObj4AddEventListener(Set<ICompletionProposal> proposals, int offset, String location, String type)
	{
		addApicloudProposal(proposals, "name", 
				showParam(true, "name", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "name：事件名字（字符串类型）不能为空 默认值：无<br>"
				, getActiveUserAgentIds(), type, offset);
	}

	private void createObj4OpenSlidPane(Set<ICompletionProposal> proposals, int offset, String location)
	{
		
		addApicloudProposal(proposals, "type", 
				showParam(true, "type", "'all'")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "type：侧滑类型，left或right（字符串类型）不能为空 默认值：无<br>"
				, getActiveUserAgentIds(), "openSlidPane Param", offset);
	}

	private void createObj4OpenSlidLayout(Set<ICompletionProposal> proposals, int offset, String location)
	{
				addApicloudProposal(proposals, "rightEdge", 
						showParam(true, "rightEdge", "60")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "rightEdge：右侧滑时，侧滑window停留时露出的宽度（数字类型） 默认值：60<br>"
								, getActiveUserAgentIds(), "openSlidLayout Param", offset);
								addApicloudProposal(proposals, "leftEdge", 
										showParam(true, "leftEdge", "60")
										, JS_PROPERTY, "<h3>描述</h3>"
												+ "leftEdge：左侧滑时，侧滑window停留时露出的宽度（数字类型） 默认值：60<br>"
						, getActiveUserAgentIds(), "openSlidLayout Param", offset);
				addApicloudProposal(proposals, "type", 
						showParam(true, "type", "'all'")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "type：侧滑类型（left：左侧滑、right：右侧滑、all：左右侧滑，字符串类型） 默认值：all<br>"
						, getActiveUserAgentIds(), "openSlidLayout Param", offset);
				addApicloudProposal(proposals, "fixedPane", 
						showParam(true, "fixedPane", "{}")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "fixedPane：底部固定层window（JSON类型） 默认值：无内部字段：<br>"
								+ "{name:窗口名字（字符串类型）url:地址   bgColor:背景色（#fff,#ffffff,rgba(r,g,b,a)）<br>"
								+ "bounces:是否弹动，默认false  opaque:是否不透明，默认false <br>" 
								+"vScrollBarEnabled:是否显示垂直滚动条，默认true hScrollBarEnabled:是否显示水平滚动条，默认true}<br>"
						, getActiveUserAgentIds(), "openSlidLayout Param", offset);
				addApicloudProposal(proposals, "slidPane", 
						showParam(true, "slidPane", "{}")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "slidPane：侧滑层window（JSON类型） 默认值：无内部字段：<br>"
								+ "{name:窗口名字（字符串类型）url:地址   bgColor:背景色（#fff,#ffffff,rgba(r,g,b,a)）<br>"
								+ "bounces:是否弹动，默认false  opaque:是否不透明，默认false <br>" 
								+"vScrollBarEnabled:是否显示垂直滚动条，默认true hScrollBarEnabled:是否显示水平滚动条，默认true}<br>"
						, getActiveUserAgentIds(), "openSlidLayout Param", offset);
	}

	private void createObj4SetFrameGroupAttr(Set<ICompletionProposal> proposals, int offset, String location)
	{
				addApicloudProposal(proposals, "name", 
						showParam(true, "name", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "name：窗口组名字（字符串类型）不能为空 默认值：无<br>"
						, getActiveUserAgentIds(), "setFrameGroupAttr Param", offset);
		addApicloudProposal(proposals, "hidden", 
				showParam(true, "hidden", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "hidden：窗口组是否隐藏（布尔类型） 默认值：无<br>"
				, getActiveUserAgentIds(), "setFrameGroupAttr Param", offset);
	}

	private void createObj4SetFrameGroupIndex(Set<ICompletionProposal> proposals, int offset, String location)
	{
				addApicloudProposal(proposals, "name", 
						showParam(true, "name", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "name：窗口组名字（字符串类型）不能为空 默认值：无<br>"
						, getActiveUserAgentIds(), "setFrameGroupIndex Param", offset);
				addApicloudProposal(proposals, "scrollEnabled", 
						showParam(true, "scrollEnabled", "false")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "scroll：是否平滑滚动至目标窗口（布尔类型） 默认值：false<br>"		
						, getActiveUserAgentIds(), "setFrameGroupIndex Param", offset);
				addApicloudProposal(proposals, "index", 
						showParam(true, "index", "")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "index：子窗口索引（数组类型）不能为空 默认值：无<br>"
						, getActiveUserAgentIds(), "setFrameGroupIndex Param", offset);
	}

	private void createObj4CloseFrameGroup(Set<ICompletionProposal> proposals, int offset, String location)
	{
		addApicloudProposal(proposals, "name", 
				showParam(true, "name", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "name：窗口组名字（字符串类型）不能为空 默认值：无<br>"
				, getActiveUserAgentIds(), "closeFrameGroup Param", offset);
	}

	private void createObj4OpenFrameGroup(Set<ICompletionProposal> proposals, int offset, String location)
	{
				addApicloudProposal(proposals, "frames", 
						showParam(true, "frames", "[]")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "frames：子窗口数组（数组类型） 默认值：无 包括内部字段<br>"
								+"[name:窗口名字，字符串类型，不能为空;url:地址，字符串类型，不能为空;<br>" +
								"pageParam:{}, 页面参数，JSON对象。默认值：空;bounces:是否弹动，布尔型，默认值：false;<br>" +
								"opaque:是否不透明，布尔型，默认值：false;bgColor:背景色，（#fff,#ffffff,rgba(r,g,b,a)）<br>" +
								"，默认值：无；vScrollBarEnabled:是否显示垂直滚动条，布尔型，默认值：true;<br>" +
								"vScrollBarEnabled:是否显示水平滚动条，布尔型，默认值：false<br>" 
						, getActiveUserAgentIds(), "openFrameGroup Param", offset);
				addApicloudProposal(proposals, "name", 
						showParam(true, "name", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "name：窗口组名字（字符串类型）不能为空 默认值：无<br>"
						, getActiveUserAgentIds(), "openFrameGroup Param", offset);
				addApicloudProposal(proposals, "background", 
						showParam(true, "background", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "background：窗口组背景，背景色（#fff,#ffffff,rgba(r,g,b,a)）或图片（字符串类型） 默认值：无<br>"
						, getActiveUserAgentIds(), "openFrameGroup Param", offset);
				addApicloudProposal(proposals, "scrollEnabled", 
						showParam(true, "scrollEnabled", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "scrollEnabled：窗口组是否能够左右滚动（布尔类型） 默认值：true<br>"
						, getActiveUserAgentIds(), "openFrameGroup Param", offset);
				addApicloudProposal(proposals, "index", 
						showParam(true, "index", "0")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "index：默认显示的页面索引（数字类型） 默认值：无0<br>"
						, getActiveUserAgentIds(), "openFrameGroup Param", offset);
				addApicloudProposal(proposals, "rect", 
						showParamObject(location, true, "rect" , "x:0,", "y:0,", "w:320,", "h:480,")
						, JS_PROPERTY, "<h3>参数</h3>"
								+ "rect：窗口区域（JSON对象）包含以下4个参数：<br>"
								+ "x：左上角x坐标    y：左上角y坐标<br>"
								+ "	w：宽度                 h：高度<br>"
						, getActiveUserAgentIds(), "openFrameGroup Param", offset);
	}

	private void createObj4Animation(Set<ICompletionProposal> proposals, int offset, String location)
	{
				addApicloudProposal(proposals, "name", 
						showParam(true, "name", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "	name：子窗口名字，不能为空（字符串类型） 默认值：当前子窗口 <br>"
						, getActiveUserAgentIds(), "animation Param", offset);
				addApicloudProposal(proposals, "delay", 
						showParam(true, "delay", "0")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "delay：动画延迟时间，单位毫秒，默认立即开始（数字类型） 默认值：0 <br>"
						, getActiveUserAgentIds(), "animation Param", offset);
				addApicloudProposal(proposals, "duration", 
						showParam(true, "duration", "0")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "duration：动画过渡时间，单位毫秒（数字类型） 默认值：0 <br>"
						, getActiveUserAgentIds(), "animation Param", offset);
				addApicloudProposal(proposals, "curve", 
						showParam(true, "curve", "'easeInOut'")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "	curve：动画曲线类型，详见动画曲线类型（字符串类型） 默认值：easeInOut <br>"
						, getActiveUserAgentIds(), "animation Param", offset);
				addApicloudProposal(proposals, "repeatCount", 
						showParam(true, "repeatCount", "0")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "repeatCount：动画次数，默认不重复，为-1时无限重复（数字类型） 默认值：0 <br>"
						, getActiveUserAgentIds(), "animation Param", offset);
				addApicloudProposal(proposals, "autoreverse", 
						showParam(true, "autoreverse", "0")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "autoreverse：一次动画结束后是否自动反转动画（布尔类型） 默认值：false <br>"
						, getActiveUserAgentIds(), "animation Param", offset);
				addApicloudProposal(proposals, "alpha", 
						showParam(true, "alpha", "0")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "alpha：透明度，介于0 - 1之间（数字类型） 默认值：无 <br>"
						, getActiveUserAgentIds(), "animation Param", offset);
				addApicloudProposal(proposals, "translation", 
						showParamObject(location, true, "translation" , "x:0,", "y:0,", "z:0")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "translation：平移参数（JSON类型） 默认值：无  内部字段：{x:x轴方向上的平移距离，<br>"
								+"默认为0 y:y轴方向上的平移距离，默认为0    z:  z轴方向上的平移距离，默认为0}<br>"
						, getActiveUserAgentIds(), "animation Param", offset);
				addApicloudProposal(proposals, "scale", 
						showParamObject(location, true, "scale" , "x:1,", "y:1,", "z:1")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "scale：放大参数（JSON类型） 默认值：无  内部字段：{x:x轴方向上的放大倍率，<br>"
								+"默认为1  y:y轴方向上的放大倍率，默认为1    z:  z轴方向上的放大倍率，默认为1}<br>"
						, getActiveUserAgentIds(), "animation Param", offset);
				addApicloudProposal(proposals, "rotation", 
						showParamObject(location, true, "rotation" , "degree:0,", "x:0,", "y:0,", "z:0")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "rotation：旋转参数（JSON类型） 默认值：无  内部字段：{degree:旋转角度，默认0<br>"
								+ " x:绕x轴旋转，默认为0    y: 绕y轴旋转，默认为0    z: 绕z轴旋转，默认为1}<br>"
						, getActiveUserAgentIds(), "animation Param", offset);
	}

	private void createObj4BbringFrameToFront(Set<ICompletionProposal> proposals, int offset, String location, String type)
	{
		addApicloudProposal(proposals, "from", 
				showParam(true, "from", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "name： 窗口名字(字符串类型） 不能为空 默认值：无 <br>"
				, getActiveUserAgentIds(), "bringFrameToFront Param", offset);
		addApicloudProposal(proposals, "to", 
				showParam(true, "to", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "to： 窗口名字，指定时调整窗口到此窗口前面，否则调整到最前面(字符串类型） 默认值：无 <br>"
				, getActiveUserAgentIds(), type, offset);
		
	}

	private void createObj4CloseFrame(Set<ICompletionProposal> proposals, int offset, String location)
	{
		addApicloudProposal(proposals, "name", 
				showParam(true, "name", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "name： 窗口名称(字符串类型） 不能为空 默认值：无 <br>"
				, getActiveUserAgentIds(), "closeFrame Param", offset);
	}

	private void createObj4SetFrameAttr(Set<ICompletionProposal> proposals, int offset, String location)
	{
		
				addApicloudProposal(proposals, "hidden", 
						showParam(true, "hidden", "")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "hidden：本frame是否隐藏（隐藏即从屏幕上移除，但不销毁，布尔类型）默认值:无<br>"
						, getActiveUserAgentIds(), "setFrameAttr Param", offset);
				addApicloudProposal(proposals, "scaleEnabled", 
						showParam(true, "scaleEnabled", "false")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "scaleEnabled： 页面是否可以缩放（布尔类型） 默认值：false <br>"
						, getActiveUserAgentIds(), "setFrameAttr Param", offset);
				addApicloudProposal(proposals, "rect", 
						showParamObject(location, true, "rect" , "x:0,", "y:0,", "w:320,", "h:480,")
						, JS_PROPERTY, "<h3>参数</h3>"
								+ "rect：窗口区域（JSON对象）包含以下4个参数：<br>"
								+ "x：左上角x坐标    y：左上角y坐标<br>"
								+ "	w：宽度                 h：高度<br>"
						, getActiveUserAgentIds(), "setFrameAttr Param", offset);
				addApicloudProposal(proposals, "name", 
						showParam(true, "name", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "name： 窗口名称(字符串类型） 默认值：无 <br>"
						, getActiveUserAgentIds(), "setFrameAttr Param", offset);
				addApicloudProposal(proposals, "bounces", 
						showParam(true, "bounces", "false")
						, JS_PROPERTY, "<h3>描述</h3>"
						+ "bounces：页面是否弹动（布尔类型）默认值：false<br>"
						, getActiveUserAgentIds(), "setFrameAttr Param", offset);
				addApicloudProposal(proposals, "opaque", 
						showParam(true, "opaque", "false")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "opaque：页面是否透明（布尔类型）默认值：false<br>" 
						, getActiveUserAgentIds(), "setFrameAttr Param", offset);
				addApicloudProposal(proposals, "bgColor", 
						showParam(true, "bgColor", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "bgColor：背景色，格式（#fff,#ffffff,rgba(r,g,b,a)，字符串）默认值：无<br>"
						, getActiveUserAgentIds(), "setFrameAttr Param", offset);
				addApicloudProposal(proposals, "vScrollBarEnabled", 
						showParam(true, "vScrollBarEnabled", "true")
						, JS_PROPERTY, "<h3>描述</h3>"
								+"vScrollBarEnabled：是否显示垂直滚动条（布尔类型）默认值：true<br>"
						, getActiveUserAgentIds(), "setFrameAttr Param", offset);
				addApicloudProposal(proposals, "hScrollBarEnabled", 
						showParam(true, "hScrollBarEnabled", "true")
						, JS_PROPERTY, "<h3>描述</h3>"
								+"hScrollBarEnabled：是否显示水平滚动条（布尔类型）默认值：true<br>"
						, getActiveUserAgentIds(), "setFrameAttr Param", offset);
	}

	private void createObj4OpenFrame(Set<ICompletionProposal> proposals, int offset, String location)
	{
				addApicloudProposal(proposals, "rect", 
						showParamObject(location, true, "rect" , "x:0,", "y:0,", "w:320,", "h:480,")
						, JS_PROPERTY, "<h3>参数</h3>"
								+ "rect：窗口区域（JSON对象）包含以下4个参数：<br>"
								+ "x：左上角x坐标    y：左上角y坐标<br>"
								+ "	w：宽度                 h：高度<br>"
						, getActiveUserAgentIds(), "openFrame Param", offset);
				addApicloudProposal(proposals, "pageParam", 
						showParam(true, "pageParam", "{}")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "pageParam：页面参数（JSON对象）	默认值：无<br>"
						, getActiveUserAgentIds(), "openFrame Param", offset);
				addApicloudProposal(proposals, "bounces", 
						showParam(true, "bounces", "false")
						, JS_PROPERTY, "<h3>描述</h3>"
						+ "bounces：页面是否弹动（布尔类型）默认值：false<br>"
						, getActiveUserAgentIds(), "openFrame Param", offset);
				addApicloudProposal(proposals, "opaque", 
						showParam(true, "opaque", "false")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "opaque：页面是否透明（布尔类型）默认值：false<br>" 
						, getActiveUserAgentIds(), "openFrame Param", offset);
				addApicloudProposal(proposals, "bgColor", 
						showParam(true, "bgColor", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "bgColor：背景色，格式（#fff,#ffffff,rgba(r,g,b,a)，字符串）默认值：无<br>"
						, getActiveUserAgentIds(), "openFrame Param", offset);
				addApicloudProposal(proposals, "showProgress", 
						showParam(true, "showProgress", "false")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "showProgress：是否显示等待框，只在url为网址时有效（布尔类型）默认值：false<br>"
						, getActiveUserAgentIds(), "openFrame Param", offset);
				addApicloudProposal(proposals, "name", 
						showParam(true, "name", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
						+ "name：窗口名字（字符串类型）不能为空   默认值；无<br>"
						, getActiveUserAgentIds(), "openFrame Param", offset);
				addApicloudProposal(proposals, "url", 
						showParam(true, "url", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "url：地址（字符串类型）	默认值：原URL<br>" 
						, getActiveUserAgentIds(), "openFrame Param", offset);
				addApicloudProposal(proposals, "url", 
						showParam(true, "url", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "url：地址（字符串类型）	默认值：原URL<br>" 
						, getActiveUserAgentIds(), "openFrame Param", offset);
				addApicloudProposal(proposals, "vScrollBarEnabled", 
						showParam(true, "vScrollBarEnabled", "true")
						, JS_PROPERTY, "<h3>描述</h3>"
								+"vScrollBarEnabled：是否显示垂直滚动条（布尔类型）默认值：true<br>"
						, getActiveUserAgentIds(), "openFrame Param", offset);
				addApicloudProposal(proposals, "hScrollBarEnabled", 
						showParam(true, "hScrollBarEnabled", "true")
						, JS_PROPERTY, "<h3>描述</h3>"
								+"hScrollBarEnabled：是否显示水平滚动条（布尔类型）默认值：true<br>"
						, getActiveUserAgentIds(), "openFrame Param", offset);
				addApicloudProposal(proposals, "reload", 
						showParam(true, "reload", "")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "reload：页面已经打开时，是否重新加载页面<br>" 
						, getActiveUserAgentIds(), "openFrame Param", offset);
	}

	private void createObj4ExecScript(Set<ICompletionProposal> proposals, int offset, String location)
	{
				addApicloudProposal(proposals, "name", 
						showParam(true, "name", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
						+ "name：窗口名字（字符串类型）  默认值；无<br>"
						, getActiveUserAgentIds(), "execScript Param", offset);
				addApicloudProposal(proposals, "frameName", 
						showParam(true, "frameName", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "frameName：子窗口名字（字符串类型）默认值为：无<br>"
						, getActiveUserAgentIds(), "execScript Param", offset);
				addApicloudProposal(proposals, "script", 
						showParam(true, "script", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "script：js代码（字符串类型）不能为空 默认值为：无<br>" 
						, getActiveUserAgentIds(), "execScript Param", offset);
	}

	private void createObj4CloseToWin(Set<ICompletionProposal> proposals, int offset, String location)
	{
		addApicloudProposal(proposals, "animation", 
				showParamObject(location, true, "animation" , "type: 'flip',", "subType: 'from_bottom',", "duration: 500")
				, JS_PROPERTY, "<h3>参数</h3>"
						+ "animation：动画参数JSON字典类型包含以下3个参数：<br>"
						+ "type：动画类型（详见动画类型常量）<br>"
						+ "subType：动画子类型（详见动画子类型常量）<br>"
						+ "duration：动画时间	默认值：0.3秒<br>"
				, getActiveUserAgentIds(), "closeToWin Param", offset);
addApicloudProposal(proposals, "name", 
		showParam(true, "name", "''")
		, JS_PROPERTY, "<h3>描述</h3>"
		+ "name：窗口名字（字符串类型） 不能为空 默认值；无<br>"
		, getActiveUserAgentIds(), "closeToWin Param", offset);
		
	}

	private void createObj4CloseWin(Set<ICompletionProposal> proposals, int offset, String location)
	{
		addApicloudProposal(proposals, "animation", 
				showParamObject(location, true, "animation" , "type: 'flip',", "subType: 'from_bottom',", "duration: 500")
				, JS_PROPERTY, "<h3>参数</h3>"
						+ "animation：动画参数JSON字典类型包含以下3个参数：<br>"
						+ "type：动画类型（详见动画类型常量）<br>"
						+ "subType：动画子类型（详见动画子类型常量）<br>"
						+ "duration：动画时间	默认值：0.3秒<br>"
				, getActiveUserAgentIds(), "closeWin Param", offset);
addApicloudProposal(proposals, "name", 
		showParam(true, "name", "''")
		, JS_PROPERTY, "<h3>描述</h3>"
		+ "name：窗口名字（字符串类型） 默认值；无<br>"
		, getActiveUserAgentIds(), "closeWin Param", offset);
		
	}

	private void createObj4SetWinAttr(Set<ICompletionProposal> proposals, int offset, String location)
	{
				addApicloudProposal(proposals, "bounces", 
						showParam(true, "bounces", "false")
						, JS_PROPERTY, "<h3>描述</h3>"
						+ "bounces：页面是否弹动（布尔类型）默认值：false<br>"
						, getActiveUserAgentIds(), "setWinAttr Param", offset);
				addApicloudProposal(proposals, "opaque", 
						showParam(true, "opaque", "false")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "opaque：页面是否透明（布尔类型）默认值：false<br>" 
						, getActiveUserAgentIds(), "setWinAttr Param", offset);
				addApicloudProposal(proposals, "bgColor", 
						showParam(true, "bgColor", "''")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "bgColor：背景色，格式（#fff,#ffffff,rgba(r,g,b,a)，字符串）默认值：无<br>"
						, getActiveUserAgentIds(), "setWinAttr Param", offset);
				addApicloudProposal(proposals, "vScrollBarEnabled", 
						showParam(true, "vScrollBarEnabled", "true")
						, JS_PROPERTY, "<h3>描述</h3>"
								+"vScrollBarEnabled：是否显示垂直滚动条（布尔类型）默认值：true<br>"
						, getActiveUserAgentIds(), "setWinAttr Param", offset);
				addApicloudProposal(proposals, "hScrollBarEnabled", 
						showParam(true, "hScrollBarEnabled", "true")
						, JS_PROPERTY, "<h3>描述</h3>"
								+"hScrollBarEnabled：是否显示水平滚动条（布尔类型）默认值：true<br>"
						, getActiveUserAgentIds(), "setWinAttr Param", offset);
				addApicloudProposal(proposals, "slidBackEnabled", 
						showParam(true, "slidBackEnabled", "true")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "slidBackEnabled：是否支持滑动返回。iOS7.0及以上系统中，在新打开的窗口左边缘处向右滑动，可以返回到上一个窗口，该字段只iOS有效（布尔类型）默认值：false<br>"
						, getActiveUserAgentIds(), "setWinAttr Param", offset);
				addApicloudProposal(proposals, "scaleEnabled", 
						showParam(true, "scaleEnabled", "false")
						, JS_PROPERTY, "<h3>描述</h3>"
								+ "scaleEnabled： 页面是否可以缩放（布尔类型） 默认值：false <br>"
						, getActiveUserAgentIds(), "setWinAttr Param", offset);
		
	}

	private void createObj4OpenWin(Set<ICompletionProposal> proposals, int offset, String location)
	{
				addApicloudProposal(proposals, "animation", 
						showParamObject(location, true, "animation" , "type: 'flip',", "subType: 'from_bottom',", "duration: 500")
						, JS_PROPERTY, "<h3>参数</h3>"
								+ "animation：动画参数JSON字典类型包含以下3个参数：<br>"
								+ "type：动画类型（详见动画类型常量）<br>"
								+ "subType：动画子类型（详见动画子类型常量）<br>"
								+ "duration：动画时间	默认值：0.3秒<br>"
						, getActiveUserAgentIds(), "openWin Param", offset);
		addApicloudProposal(proposals, "name", 
				showParam(true, "name", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
				+ "name：窗口名字（字符串类型）不能为空 默认值；无<br>"
				, getActiveUserAgentIds(), "openWin Param", offset);
		addApicloudProposal(proposals, "url", 
				showParam(true, "url", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
				+ "url：地址（字符串类型）	默认值：<br>" 
				, getActiveUserAgentIds(), "openWin Param", offset);
		addApicloudProposal(proposals, "pageParam", 
				showParam(true, "pageParam", "{}")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "pageParam：页面参数（JSON对象）	默认值：无<br>"
				, getActiveUserAgentIds(), "openWin Param", offset);
		addApicloudProposal(proposals, "bounces", 
				showParam(true, "bounces", "false")
				, JS_PROPERTY, "<h3>描述</h3>"
				+ "bounces：页面是否弹动（布尔类型）默认值：false<br>"
				, getActiveUserAgentIds(), "openWin Param", offset);
		addApicloudProposal(proposals, "opaque", 
				showParam(true, "opaque", "false")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "opaque：页面是否透明（布尔类型）默认值：false<br>" 
				, getActiveUserAgentIds(), "openWin Param", offset);
		addApicloudProposal(proposals, "bgColor", 
				showParam(true, "bgColor", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "bgColor：背景色，格式（#fff,#ffffff,rgba(r,g,b,a)，字符串）默认值：无<br>"
				, getActiveUserAgentIds(), "openWin Param", offset);
		addApicloudProposal(proposals, "vScrollBarEnabled", 
				showParam(true, "vScrollBarEnabled", "true")
				, JS_PROPERTY, "<h3>描述</h3>"
						+"vScrollBarEnabled：是否显示垂直滚动条（布尔类型）默认值：true<br>"
				, getActiveUserAgentIds(), "openWin Param", offset);
		addApicloudProposal(proposals, "hScrollBarEnabled", 
				showParam(true, "hScrollBarEnabled", "true")
				, JS_PROPERTY, "<h3>描述</h3>"
						+"hScrollBarEnabled：是否显示水平滚动条（布尔类型）默认值：true<br>"
				, getActiveUserAgentIds(), "openWin Param", offset);
		addApicloudProposal(proposals, "slidBackEnabled", 
				showParam(true, "slidBackEnabled", "true")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "slidBackEnabled：是否支持滑动返回。iOS7.0及以上系统中，在新打开的窗口左边缘处向右滑动，可以返回到上一个窗口，该字段只iOS有效（布尔类型）默认值：false<br>"
				, getActiveUserAgentIds(), "openWin Param", offset);
		addApicloudProposal(proposals, "showProgress", 
				showParam(true, "showProgress", "false")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "showProgress：是否显示等待框，只在url为网址时有效（布尔类型）默认值：false<br>"
				, getActiveUserAgentIds(), "openWin Param", offset);
		addApicloudProposal(proposals, "delay", 
				showParam(true, "delay", "0")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "delay：窗口显示延迟时间，适用于将被打开的窗口中可能需要打开有耗时操作的模块时，可延迟窗口展示到屏幕的时间，保持UI的整体性（数字类型） 默认值；0<br>"
				, getActiveUserAgentIds(), "openWin Param", offset);
		addApicloudProposal(proposals, "animation", 
				showParamObject(location, true, "animation" , "type: 'flip',", "subType: 'from_bottom',", "duration: 500")
				, JS_PROPERTY, "<h3>参数</h3>"
						+ "animation：动画参数JSON字典类型包含以下3个参数：<br>"
						+ "type：动画类型（详见动画类型常量）<br>"
						+ "subType：动画子类型（详见动画子类型常量）<br>"
						+ "duration：动画时间	默认值：0.3秒<br>"
				, getActiveUserAgentIds(), "closeWidget Param", offset);
		
	}

	private void createObj4CloseWidget(Set<ICompletionProposal> proposals, int offset, String location)
	{
		addApicloudProposal(proposals, "animation", 
				showParamObject(location, true, "animation" , "type: 'flip',", "subType: 'from_bottom',", "duration: 500")
				, JS_PROPERTY, "<h3>参数</h3>"
						+ "animation：动画参数JSON字典类型包含以下3个参数：<br>"
						+ "type：动画类型（详见动画类型常量）<br>"
						+ "subType：动画子类型（详见动画子类型常量）<br>"
						+ "duration：动画时间	默认值：0.3秒<br>"
				, getActiveUserAgentIds(), "closeWidget Param", offset);
		addApicloudProposal(proposals, "id", 
				showParam(true, "id", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
				+ "id：widget ID（字符串类型）必须指定 默认值：无<br>"
				, getActiveUserAgentIds(), "closeWidget Param", offset);
		addApicloudProposal(proposals, "retData", 
				showParam(true, "retData", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "retData：返回值（JSON对象） 默认值：无<br>" 
				, getActiveUserAgentIds(), "closeWidget Param", offset);
		addApicloudProposal(proposals, "silent", 
				showParam(true, "silent", "false")
				, JS_PROPERTY, "<h3>描述</h3>"
						+ "silent：是否静默退出，只在主widget中有效（boolean类型） 默认值：false<br>" 
				, getActiveUserAgentIds(), "closeWidget Param", offset);
	}

	private void createObj4OpenWidget(Set<ICompletionProposal> proposals, int offset, String location)
	{
		addApicloudProposal(proposals, "id", 
				showParam(true, "id", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
				+ "id：widget ID（字符串类型）必须指定 默认值：无<br>"
				, getActiveUserAgentIds(), "OpenWidget Param", offset);
		addApicloudProposal(proposals, "wgtParam", 
				showParam(true, "wgtParam", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
				+ "wgtParam：wgt参数（JSON对象）默认值：无<br>" 
				, getActiveUserAgentIds(), "OpenWidget Param", offset);
		addApicloudProposal(proposals, "animation", 
				showParamObject(location, true, "animation" , "type: 'flip',", "subType: 'from_bottom',", "duration: 500")
				, JS_PROPERTY, "<h3>参数</h3>"
						+ "animation：动画参数JSON字典类型包含以下3个参数：<br>"
						+ "type：动画类型（详见动画类型常量）<br>"
						+ "subType：动画子类型（详见动画子类型常量）<br>"
						+ "duration：动画时间	默认值：0.3秒<br>"
				, getActiveUserAgentIds(), "OpenWidget Param", offset);
	}
	private void createObj4OpenApp(Set<ICompletionProposal> proposals, int offset, String location)
	{
		addApicloudProposal(proposals, "appParam", 
				showParam(true, "appParam", "{}")
				, JS_PROPERTY, "<h3>描述</h3>"
				+ "appParam：传给目标应用的参数（JSON对象）	默认值：无<br>"
				, getActiveUserAgentIds(), "OpenApp Param", offset);
		addApicloudProposal(proposals, "iosUrl", 
				showParam(true, "iosUrl", "'file://xxx.apk'")
				, JS_PROPERTY, "<h3>描述</h3>"
				+ "iosUrl：目标应用的url（iOS平台使用），iOS下不能为空<br>" 
				, getActiveUserAgentIds(), "OpenApp Param", offset);
		addApicloudProposal(proposals, "androidPkg", 
				showParam(true, "appUri", "'file://xxx.apk'")
				, JS_PROPERTY, "<h3>描述</h3>"
				+ "appUri：目标应用的包名或action（Android平台使用），Android下不能为空（字符串类型）	默认值：无<br>"
				, getActiveUserAgentIds(), "OpenApp Param", offset);
		addApicloudProposal(proposals, "mimeType", 
				showParam(true, "mimeType", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
				+ "mimeType：指定目标应用的响应数据类型，如：\"text/html\"（Android平台使用，字符串类型）<br>"
				, getActiveUserAgentIds(), "OpenApp Param", offset);
		addApicloudProposal(proposals, "uri", 
				showParam(true, "uri", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
				+ "uri：指定目标应用响应的uri（Android平台使用）<br>"
				, getActiveUserAgentIds(), "OpenApp Param", offset);
		
	}
	
	private void createObj4InstallApp(Set<ICompletionProposal> proposals, int offset, String location)
	{
		
		addApicloudProposal(proposals, "appUri", 
				showParam(true, "appUri", "''")
				, JS_PROPERTY, "<h3>描述</h3>"
				+ "appUri：目标应用的资源文件标识。Android上为apk包的本地路径，<br>" 
				+"如file://xxx；IOS上为对应的itunes地址（字符串类型）	默认值：无<br>"
				, getActiveUserAgentIds(), "InstallApp Param", offset);
	}

	/**
	 * addProjectGlobalFunctions
	 * 
	 * @param proposals
	 * @param offset
	 */
	private void addGlobals(Set<ICompletionProposal> proposals, int offset)
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
				addProposal(proposals, property, offset, projectURI, location, userAgentNames);
			}
		}
	}

	protected void addPropertie1(IDocument document, Set<ICompletionProposal> proposals, int offset, int num)
	{
		System.out.println(targetNode.toString());
		System.out.println(targetNode.getParent().toString());
		if(targetNode.getParent().toString().contains("api")) {
			addProperties(proposals, offset, num);
		} else {
			JSGetPropertyNode node = ParseUtil.getGetPropertyNode(targetNode, statementNode);
			List<String> types = getParentObjectTypes(node, offset);
			if(types.isEmpty()) {
				addTypeProperties(proposals, "Document", offset);
				addTypeProperties(proposals, "Window", offset);
			}
			// add all properties of each type to our proposal list
			for (String type : types)
			{
				addTypeProperties(proposals, type, offset);
			}
		}
		System.out.println(proposals.size());
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
//		List<String> types = getParentObjectTypes(node, offset);
//
//		// add all properties of each type to our proposal list
//		for (String type : types)
//		{
//			addTypeProperties(proposals, type, offset);
//		}
		JSScope localScope = ParseUtil.getScopeAtOffset(targetNode, offset);
		IParseNode parseNode = node.getChildren()[0];
		String parserName = parseNode.toString();
		JSPlugin.getDefault().getLog().log(new Status(IStatus.OK, JSPlugin.PLUGIN_ID, 0,parserName, null));
		if(parserName.equals("api.require('db')") || parserName.equals("api.require(\"db\")")) {
			proposals.clear();
			createDBProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('msm')") || parserName.equals("api.require(\"msm\")")) {
			proposals.clear();
			createMSM(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('mam')") || parserName.equals("api.require(\"mam\")")) {
			proposals.clear();
			createMAM(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('push')") || parserName.equals("api.require(\"push\")")) {
			proposals.clear();
			createPUSH(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('imageBrowser')") || parserName.equals("api.require(\"imageBrowser\")")) {
			proposals.clear();
			createImageBrows(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('scanner')") || parserName.equals("api.require(\"scanner\")")) {
			proposals.clear();
			createScanner(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('fs')") || parserName.equals("api.require(\"fs\")")) {
			proposals.clear();
			createFSProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('baiDuLocation')") || parserName.equals("api.require(\"baiDuLocation\")")) {
			proposals.clear();
			createBaiDuLocation(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('baiDuMap')") || parserName.equals("api.require(\"baiDuMap\")")) {
			proposals.clear();
			createBaiDuMap(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('fileBrowser')") || parserName.equals("api.require(\"fileBrowser\")")) {
			proposals.clear();
			createFileBrowser(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('qq')") || parserName.equals("api.require(\"qq\")")) {
			proposals.clear();
			createQQ(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('weiXin')") || parserName.equals("api.require(\"weiXin\")")) {
			proposals.clear();
			createWeiXin(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('sinaWeiBo')") || parserName.equals("api.require(\"sinaWeiBo\")")) {
			proposals.clear();
			createSinaWeiBo(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('aliPay')") || parserName.equals("api.require(\"aliPay\")")) {
			proposals.clear();
			createAliPayProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('calendar')") || parserName.equals("api.require(\"calendar\")")) {
			proposals.clear();
			createCalendarProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('cardReader')") || parserName.equals("api.require(\"cardReader\")")) {
			proposals.clear();
			createCardReaderProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('cityList')") || parserName.equals("api.require(\"cityList\")")) {
			proposals.clear();
			createCityListProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('contact')") || parserName.equals("api.require(\"contact\")")) {
			proposals.clear();
			createContactProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('docReader')") || parserName.equals("api.require(\"docReader\")")) {
			proposals.clear();
			createDocReaderProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('inputField')") || parserName.equals("api.require(\"inputField\")")) {
			proposals.clear();
			createInputFieldProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('pdfReader')") || parserName.equals("api.require(\"pdfReader\")")) {
			proposals.clear();
			createPdfReaderProposal(proposals, offset, num);
			return;
		} 
		if(parserName.equals("api.require('pieChart')") || parserName.equals("api.require(\"pieChart\")")) {
			proposals.clear();
			createPieChartProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('speechRecognizer')") || parserName.equals("api.require(\"speechRecognizer\")")) {
			proposals.clear();
			createSpeechRecognizerProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('personalCenter')") || parserName.equals("api.require(\"personalCenter\")")) {
			proposals.clear();
			createPersonalCenterProposal(proposals, offset, num);
			return;
		} 
		if(parserName.equals("api.require('downloadManager')") || parserName.equals("api.require(\"downloadManager\")")) {
			proposals.clear();
			createDownloadManagerProposal(proposals, offset, num);
			return;
		} 
		if(parserName.equals("api.require('barChart')") || parserName.equals("api.require(\"barChart\")")) {
			proposals.clear();
			createBarChartProposal(proposals, offset, num);
			return;
		} 
		if(parserName.equals("api.require('lineChart')") || parserName.equals("api.require(\"lineChart\")")) {
			proposals.clear();
			createLineChartProposal(proposals, offset, num);
			return;
		} 
		if(parserName.equals("api.require('coverFlow')") || parserName.equals("api.require(\"coverFlow\")")) {
			proposals.clear();
			createCoverFlowProposal(proposals, offset, num);
			return;
		} 
		if(parserName.equals("api.require('scannerView')") || parserName.equals("api.require(\"scannerView\")")) {
			proposals.clear();
			createScannerViewProposal(proposals, offset, num);
			return;
		} 
		if(parserName.equals("api.require('slider')") || parserName.equals("api.require(\"slider\")")) {
			proposals.clear();
			createSliderProposal(proposals, offset, num);
			return;
		} 
		if(parserName.equals("api.require('tabBar')") || parserName.equals("api.require(\"tabBar\")")) {
			proposals.clear();
			createTabBarProposal(proposals, offset, num);
			return;
		} 
		if(parserName.equals("api.require('listView')") || parserName.equals("api.require(\"listView\")")) {
			proposals.clear();
			createListViewProposal(proposals, offset, num);
			return;
		} 
		if(parserName.equals("api.require('actionButton')") || parserName.equals("api.require(\"actionButton\")")) {
			proposals.clear();
			createActionButtonProposal(proposals, offset, num);
			return;
		} 
		if(parserName.equals("api.require('panorama')") || parserName.equals("api.require(\"panorama\")")) {
			proposals.clear();
			createPanoramaProposal(proposals, offset, num);
			return;
		} 
		if(parserName.equals("api.require('zip')") || parserName.equals("api.require(\"zip\")")) {
			proposals.clear();
			createZipProposal(proposals, offset, num);
			return;
		} 
		if(parserName.equals("api.require('arcMenu')") || parserName.equals("api.require(\"arcMenu\")")) {
			proposals.clear();
			createArcMenuProposal(proposals, offset, num);
			return;
		} 
		if(parserName.equals("api.require('arcProgress')") || parserName.equals("api.require(\"arcProgress\")")) {
			proposals.clear();
			createArcProgressProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('trans')") || parserName.equals("api.require(\"trans\")")) {
			proposals.clear();
			createTransProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('periodSelector')") || parserName.equals("api.require(\"periodSelector\")")) {
			proposals.clear();
			createPeriodSelectorProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('bluetooth')") || parserName.equals("api.require(\"bluetooth\")")) {
			proposals.clear();
			createBluetoothProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('timeSelector')") || parserName.equals("api.require(\"timeSelector\")")) {
			proposals.clear();
			createTimeSelectorProposal(proposals, offset, num);
			return;
		} 
		if(parserName.equals("api.require('navigationMenu')") || parserName.equals("api.require(\"navigationMenu\")")) {
			proposals.clear();
			createNavigationMenuProposal(proposals, offset, num);
			return;
		} 
		if(parserName.equals("api.require('pullMenu')") || parserName.equals("api.require(\"pullMenu\")")) {
			proposals.clear();
			createPullMenuProposal(proposals, offset, num);
			return;
		} 
		if(parserName.equals("api.require('scrollPicture')") || parserName.equals("api.require(\"scrollPicture\")")) {
			proposals.clear();
			createScrollPictureProposal(proposals, offset, num);
			return;
		} 
		if(parserName.equals("api.require('searchBar')") || parserName.equals("api.require(\"searchBar\")")) {
			proposals.clear();
			createSearchBarProposal(proposals, offset, num);
			return;
		} 
		if(parserName.equals("api.require('bubbleMenu')") || parserName.equals("api.require(\"bubbleMenu\")")) {
			proposals.clear();
			createBbubbleMenuProposal(proposals, offset, num);
			return;
		} 
		if(parserName.equals("api.require('loadingLabel')") || parserName.equals("api.require(\"loadingLabel\")")) {
			proposals.clear();
			createLoadingLabelProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('citySelector')") || parserName.equals("api.require(\"citySelector\")")) {
			proposals.clear();
			createCitySelectorProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('sideMenu')") || parserName.equals("api.require(\"sideMenu\")")) {
			proposals.clear();
			createSideMenuProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('graph')") || parserName.equals("api.require(\"graph\")")) {
			proposals.clear();
			createGraphProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('netAudio')") || parserName.equals("api.require(\"netAudio\")")) {
			proposals.clear();
			createNetAudioProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('shakeView')") || parserName.equals("api.require(\"shakeView\")")) {
			proposals.clear();
			createShakeViewProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('listContact')") || parserName.equals("api.require(\"listContact\")")) {
			proposals.clear();
			createListContactProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('scrollRotation')") || parserName.equals("api.require(\"scrollRotation\")")) {
			proposals.clear();
			createScrollRotationProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('tabBarMenu')") || parserName.equals("api.require(\"tabBarMenu\")")) {
			proposals.clear();
			createTabBarMenuProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('circularMenu')") || parserName.equals("api.require(\"circularMenu\")")) {
			proposals.clear();
			createCircularMenuProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('multiSelector')") || parserName.equals("api.require(\"multiSelector\")")) {
			proposals.clear();
			createMultiSelectorProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('chatBox')") || parserName.equals("api.require(\"chatBox\")")) {
			proposals.clear();
			createChatBoxProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('stackMenu')") || parserName.equals("api.require(\"stackMenu\")")) {
			proposals.clear();
			createStackMenuProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('bookReader')") || parserName.equals("api.require(\"bookReader\")")) {
			proposals.clear();
			createBookReaderProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('navigationBar')") || parserName.equals("api.require(\"navigationBar\")")) {
			proposals.clear();
			createNavigationBarProposal(proposals, offset, num);
			return;
		}
		if(parserName.equals("api.require('meChat')") || parserName.equals("api.require(\"meChat\")")) {
			proposals.clear();
			createMeChatProposal(proposals, offset, num);
			return;
		}
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
			if(value.equals("api.require('db')") || value.equals("api.require(\"db\")")) {
				proposals.clear();
				createDBProposal(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('msm')") || value.equals("api.require(\"msm\")")) {
				proposals.clear();
				createMSM(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('mam')") || value.equals("api.require(\"mam\")")) {
				proposals.clear();
				createMAM(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('push')") || value.equals("api.require(\"push\")")) {
				proposals.clear();
				createPUSH(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('imageBrowser')") || value.equals("api.require(\"imageBrowser\")")) {
				proposals.clear();
				createImageBrows(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('scanner')") || value.equals("api.require(\"scanner\")")) {
				proposals.clear();
				createScanner(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('fs')") || value.equals("api.require(\"fs\")")) {
				proposals.clear();
				createFSProposal(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('baiDuLocation')") || value.equals("api.require(\"baiDuLocation\")")) {
				proposals.clear();
				createBaiDuLocation(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('baiDuMap')") || value.equals("api.require(\"baiDuMap\")")) {
				proposals.clear();
				createBaiDuMap(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('fileBrowser')") || value.equals("api.require(\"fileBrowser\")")) {
				proposals.clear();
				createFileBrowser(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('qq')") || value.equals("api.require(\"qq\")")) {
				proposals.clear();
				createQQ(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('weiXin')") || value.equals("api.require(\"weiXin\")")) {
				proposals.clear();
				createWeiXin(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('sinaWeiBo')") || value.equals("api.require(\"sinaWeiBo\")")) {
				proposals.clear();
				createSinaWeiBo(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('aliPay')") || value.equals("api.require(\"aliPay\")")) {
				proposals.clear();
				createAliPayProposal(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('calendar')") || value.equals("api.require(\"calendar\")")) {
				proposals.clear();
				createCalendarProposal(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('cardReader')") || value.equals("api.require(\"cardReader\")")) {
				proposals.clear();
				createCardReaderProposal(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('cityList')") || value.equals("api.require(\"cityList\")")) {
				proposals.clear();
				createCityListProposal(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('contact')") || value.equals("api.require(\"contact\")")) {
				proposals.clear();
				createContactProposal(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('docReader')") || value.equals("api.require(\"docReader\")")) {
				proposals.clear();
				createDocReaderProposal(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('inputField')") || value.equals("api.require(\"inputField\")")) {
				proposals.clear();
				createInputFieldProposal(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('pdfReader')") || value.equals("api.require(\"pdfReader\")")) {
				proposals.clear();
				createPdfReaderProposal(proposals, offset, num);
				return;
			} 
			if(value.equals("api.require('pieChart')") || value.equals("api.require(\"pieChart\")")) {
				proposals.clear();
				createPieChartProposal(proposals, offset, num);
				return;
			} 
			if(value.equals("api.require('speechRecognizer')") || value.equals("api.require(\"speechRecognizer\")")) {
				proposals.clear();
				createSpeechRecognizerProposal(proposals, offset, num);
				return;
			}   
			if(value.equals("api.require('personalCenter')") || value.equals("api.require(\"personalCenter\")")) {
				proposals.clear();
				createPersonalCenterProposal(proposals, offset, num);
				return;
			} 
			if(value.equals("api.require('downloadManager')") || value.equals("api.require(\"downloadManager\")")) {
				proposals.clear();
				createDownloadManagerProposal(proposals, offset, num);
				return;
			} 
			if(value.equals("api.require('barChart')") || value.equals("api.require(\"barChart\")")) {
				proposals.clear();
				createBarChartProposal(proposals, offset, num);
				return;
			} 
			if(value.equals("api.require('lineChart')") || value.equals("api.require(\"lineChart\")")) {
				proposals.clear();
				createLineChartProposal(proposals, offset, num);
				return;
			} 
			if(value.equals("api.require('coverFlow')") || value.equals("api.require(\"coverFlow\")")) {
				proposals.clear();
				createCoverFlowProposal(proposals, offset, num);
				return;
			} 
			if(value.equals("api.require('scannerView')") || value.equals("api.require(\"scannerView\")")) {
				proposals.clear();
				createScannerViewProposal(proposals, offset, num);
				return;
			} 
			if(value.equals("api.require('slider')") || value.equals("api.require(\"slider\")")) {
				proposals.clear();
				createSliderProposal(proposals, offset, num);
				return;
			} 
			if(value.equals("api.require('tabBar')") || value.equals("api.require(\"tabBar\")")) {
				proposals.clear();
				createTabBarProposal(proposals, offset, num);
				return;
			} 
			if(value.equals("api.require('listView')") || value.equals("api.require(\"listView\")")) {
				proposals.clear();
				createListViewProposal(proposals, offset, num);
				return;
			} 
			if(value.equals("api.require('actionButton')") || value.equals("api.require(\"actionButton\")")) {
				proposals.clear();
				createActionButtonProposal(proposals, offset, num);
				return;
			} 
			if(value.equals("api.require('panorama')") || value.equals("api.require(\"panorama\")")) {
				proposals.clear();
				createPanoramaProposal(proposals, offset, num);
				return;
			} 
			if(value.equals("api.require('zip')") || value.equals("api.require(\"zip\")")) {
				proposals.clear();
				createZipProposal(proposals, offset, num);
				return;
			} 
			if(value.equals("api.require('arcMenu')") || value.equals("api.require(\"arcMenu\")")) {
				proposals.clear();
				createArcMenuProposal(proposals, offset, num);
				return;
			} 
			if(value.equals("api.require('arcProgress')") || value.equals("api.require(\"arcProgress\")")) {
				proposals.clear();
				createArcProgressProposal(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('trans')") || value.equals("api.require(\"trans\")")) {
				proposals.clear();
				createTransProposal(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('periodSelector')") || value.equals("api.require(\"periodSelector\")")) {
				proposals.clear();
				createPeriodSelectorProposal(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('bluetooth')") || value.equals("api.require(\"bluetooth\")")) {
				proposals.clear();
				createBluetoothProposal(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('timeSelector')") || value.equals("api.require(\"timeSelector\")")) {
				proposals.clear();
				createTimeSelectorProposal(proposals, offset, num);
				return;
			} 
			if(value.equals("api.require('navigationMenu')") || value.equals("api.require(\"navigationMenu\")")) {
				proposals.clear();
				createNavigationMenuProposal(proposals, offset, num);
				return;
			} 
			if(value.equals("api.require('pullMenu')") || value.equals("api.require(\"pullMenu\")")) {
				proposals.clear();
				createPullMenuProposal(proposals, offset, num);
				return;
			} 
			if(value.equals("api.require('scrollPicture')") || value.equals("api.require(\"scrollPicture\")")) {
				proposals.clear();
				createScrollPictureProposal(proposals, offset, num);
				return;
			} 
			if(value.equals("api.require('searchBar')") || value.equals("api.require(\"searchBar\")")) {
				proposals.clear();
				createSearchBarProposal(proposals, offset, num);
				return;
			} 
			if(value.equals("api.require('bubbleMenu')") || value.equals("api.require(\"bubbleMenu\")")) {
				proposals.clear();
				createBbubbleMenuProposal(proposals, offset, num);
				return;
			} 
			if(value.equals("api.require('loadingLabel')") || value.equals("api.require(\"loadingLabel\")")) {
				proposals.clear();
				createLoadingLabelProposal(proposals, offset, num);
				return;
			} 
			if(value.equals("api.require('citySelector')") || value.equals("api.require(\"citySelector\")")) {
				proposals.clear();
				createCitySelectorProposal(proposals, offset, num);
				return;
			} 
			if(value.equals("api.require('sideMenu')") || value.equals("api.require(\"sideMenu\")")) {
				proposals.clear();
				createSideMenuProposal(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('graph')") || value.equals("api.require(\"graph\")")) {
				proposals.clear();
				createGraphProposal(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('netAudio')") || value.equals("api.require(\"netAudio\")")) {
				proposals.clear();
				createNetAudioProposal(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('shakeView')") || value.equals("api.require(\"shakeView\")")) {
				proposals.clear();
				createShakeViewProposal(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('listContact')") || value.equals("api.require(\"listContact\")")) {
				proposals.clear();
				createListContactProposal(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('scrollRotation')") || value.equals("api.require(\"scrollRotation\")")) {
				proposals.clear();
				createScrollRotationProposal(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('tabBarMenu')") || value.equals("api.require(\"tabBarMenu\")")) {
				proposals.clear();
				createTabBarMenuProposal(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('circularMenu')") || value.equals("api.require(\"circularMenu\")")) {
				proposals.clear();
				createCircularMenuProposal(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('multiSelector')") || value.equals("api.require(\"multiSelector\")")) {
				proposals.clear();
				createMultiSelectorProposal(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('chatBox')") || value.equals("api.require(\"chatBox\")")) {
				proposals.clear();
				createChatBoxProposal(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('stackMenu')") || value.equals("api.require(\"stackMenu\")")) {
				proposals.clear();
				createStackMenuProposal(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('bookReader')") || value.equals("api.require(\"bookReader\")")) {
				proposals.clear();
				createBookReaderProposal(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('navigationBar')") || value.equals("api.require(\"navigationBar\")")) {
				proposals.clear();
				createNavigationBarProposal(proposals, offset, num);
				return;
			}
			if(value.equals("api.require('meChat')") || value.equals("api.require(\"meChat\")")) {
				proposals.clear();
				createMeChatProposal(proposals, offset, num);
				return;
			}
		}
		if(jsc == null && parserName.equals("api")) {
			JSPlugin.getDefault().getLog().log(new Status(IStatus.OK, JSPlugin.PLUGIN_ID, 0,"join api 1111111111", null));
			proposals.clear();
			createApicloudProposal(proposals, offset, num);
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
	private void addProposal(Set<ICompletionProposal> proposals, PropertyElement property, int offset, URI projectURI,
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


			PropertyElementProposal proposal = new PropertyElementProposal(property, offset, replaceLength, projectURI);
			proposal.setTriggerCharacters(getProposalTriggerCharacters());
			if (!StringUtil.isEmpty(overriddenLocation))
			{
				proposal.setFileLocation(overriddenLocation);
			}

			Image[] userAgents = UserAgentManager.getInstance().getUserAgentImages(getProject(), userAgentNames);
			proposal.setUserAgentImages(userAgents);

			// add the proposal to the list
			proposals.add(proposal);
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
	private void addProposal(Set<ICompletionProposal> proposals, String displayName, Image image, String description,
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

			CommonCompletionProposal proposal = new CommonCompletionProposal(displayName, offset, replaceLength,
					length, image, displayName, contextInfo, description);
			proposal.setFileLocation(fileLocation);
			proposal.setUserAgentImages(userAgents);
			proposal.setTriggerCharacters(getProposalTriggerCharacters());

			// add the proposal to the list
			proposals.add(proposal);
		}
	}
	
	private void addApicloudRequire(Set<ICompletionProposal> proposals, String displayName, String replaceName, Image image, String description,
			String[] userAgentIds, String fileLocation, int offset)
	{
		if (isActiveByUserAgent(userAgentIds))
		{
			int length = replaceName.length();

			// calculate what text will be replaced
			int replaceLength = 0;

//			if (replaceRange != null)
//			{
//				offset = replaceRange.getStartingOffset(); // $codepro.audit.disable questionableAssignment
//				replaceLength = replaceRange.getLength();
//			} else {
				replaceLength = replaceStr.length();
				offset = offset - replaceLength;
//			}

			// build proposal
			IContextInformation contextInfo = null;
			Image[] userAgents = UserAgentManager.getInstance().getUserAgentImages(getProject(), userAgentIds);

			CommonCompletionProposal proposal = new CommonCompletionProposal(replaceName, offset, replaceLength,
					length - 3, image, displayName, contextInfo, description);
			proposal.setFileLocation(fileLocation);
			proposal.setUserAgentImages(userAgents);
			proposal.setTriggerCharacters(getProposalTriggerCharacters());

			// add the proposal to the list
			proposals.add(proposal);
		}	}
	
	private void addApicloudProposal(Set<ICompletionProposal> proposals, String displayName, String replaceName, Image image, String description,
			String[] userAgentIds, String fileLocation, int offset)
	{
		if (isActiveByUserAgent(userAgentIds))
		{
			int length = replaceName.length();

			// calculate what text will be replaced
			int replaceLength = 0;

//			if (replaceRange != null)
//			{
//				offset = replaceRange.getStartingOffset(); // $codepro.audit.disable questionableAssignment
//				replaceLength = replaceRange.getLength();
//				System.out.println("replaceLength============================" + replaceLength);
//			} else {
				replaceLength = replaceStr.length();
				offset = offset - replaceLength;
//			}

			// build proposal
			IContextInformation contextInfo = null;
			Image[] userAgents = UserAgentManager.getInstance().getUserAgentImages(getProject(), userAgentIds);

			CommonCompletionProposal proposal = new CommonCompletionProposal(replaceName, offset, replaceLength,
					length, image, displayName, contextInfo, description);
			proposal.setFileLocation(fileLocation);
			proposal.setUserAgentImages(userAgents);
			proposal.setTriggerCharacters(getProposalTriggerCharacters());
			proposal.getContextInformation();
			// add the proposal to the list
			proposals.add(proposal);
		}
	}

	/**
	 * addSymbolsInScope
	 * 
	 * @param proposals
	 */
	protected void addSymbolsInScope(Set<ICompletionProposal> proposals, int offset)
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
						String description = null;
						Image image = (isFunction) ? JS_FUNCTION : JS_PROPERTY;

						addProposal(proposals, name, image, description, userAgentNames, fileLocation, offset);
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

						addProposal(proposals, property, offset, getProjectURI(), typeName);
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
			addProposal(proposals, property, offset, projectURI, null);
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
		try
		{
			String str = getLineString(offset, document);
			System.out.println(str);
			o = getSpaceNum(o, str);
			str = str.replaceAll("\t", "    ");
			System.out.println("current offset ========================" + offset);
			System.out.println("sapce num is:=======================" + o);
			
			String doc = document.get(0, offset);
			doc = doc.replaceAll(" ", "").replaceAll("\t", "").replaceAll("\n", "").replaceAll("\r", "").trim();
					
			getReplaceString(offset, document);
			
		location = checkLocationType(offset, result, document, o, location, str, doc);
		System.out.println(location.name());
		// process the resulting location
		switch (location)
		{
			case IN_PROPERTY_NAME:
				addTypeProperties(result, "Document", offset);
				addTypeProperties(result, "Window", offset);
				break;

			case IN_VARIABLE_NAME:
			case IN_GLOBAL:
			case IN_CONSTRUCTOR:
				addKeywords(result, offset);
				addGlobals(result, offset);
				addSymbolsInScope(result, offset);
//				addThisProperties(result, offset);
				break;

			case IN_OBJECT_LITERAL_PROPERTY:
				addObjectLiteralProperties(result, viewer, offset, o);
				break;
			case IN_THIS:
				addThisProperties(result, offset);
				break;
			case IN_PARAMETERS:
				addParameter(result, offset);
				break;
			case API:
				createApicloudProposal(result, offset, o);
				break;
			case $API:
				create$ApicloudProposal(result, offset, o);
				break;
			default:
				break;
		}
		System.out.println(result.size());
		// merge and remove duplicates from the proposal list
		List<ICompletionProposal> filteredProposalList = getMergedProposals(new ArrayList<ICompletionProposal>(result));
		resultList = filteredProposalList.toArray(new ICompletionProposal[filteredProposalList
				.size()]);

		// select the current proposal based on the prefix
		if (replaceRange != null)
		{
			String prefix = document.get(replaceRange.getStartingOffset(), replaceRange.getLength());
			setSelectedProposal(prefix, resultList);
		}
		}
		catch (BadLocationException e1)
		{
			System.out.println("error 2856 line");
		}
		return resultList;
	}

	private LocationType checkLocationType(int offset, Set<ICompletionProposal> result, IDocument document, int o,
			LocationType location, String str, String doc) throws BadLocationException
	{
		if(addAPIModuleProposals(offset, o, document, result, doc) 
//				&& false
				) {
			
			
		}else if(str.trim().contains("$api.") && str.trim().contains("api.")) {
			int last_$api_num = str.lastIndexOf("$api.");
			String results = str.replace("$api.", "12345");
			int last_api_num = results.lastIndexOf("api.");
			if(last_$api_num < last_api_num) {
				location = LocationType.API;
			} else {
				 location = LocationType.$API;
			}
		 } else if(str.trim().contains("$api.")) {
			 location = LocationType.$API;
		 } else if(str.trim().contains("api.")) {
			 location = LocationType.API;
		 } else if( str.trim().contains("document.") ||  str.trim().contains("window.") ) {
			 location = LocationType.IN_PROPERTY_NAME;
		 }		 else {
		// determine the content assist location type
		 location = getLocationType(document, offset, str.trim());
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

	private boolean addAPIModuleProposals(int offset, int o, IDocument document, Set<ICompletionProposal> proposal, String doc) throws BadLocationException
	{
		String replaceStrs = "";
		int index = 1;
		while(true){
			replaceStrs = document.get(offset-index, 1);
			System.out.println("replaceStrs is : " + replaceStrs);
			if(replaceStrs.equals(";") || replaceStrs.equals(">") || replaceStrs.equals("\t") || replaceStrs.equals(" ") || replaceStrs.equals("\n") || replaceStrs.equals("\r")) {
				break;
			}
			if(offset == index) {
				replaceStrs = "";
			}
			index++;
		}
		replaceStrs = document.get(offset-index+1, index-1);
		
		System.out.println("replaceStrs is : " + replaceStrs);
		if(replaceStrs.contains("api.require")) {
			String var = replaceStrs.substring(0, replaceStrs.lastIndexOf("."));
			return matchName(proposal, var, o, offset);
		}

		if(replaceStrs.contains(".")) {
			String var = replaceStrs.substring(0, replaceStrs.indexOf("."));
			int inde = doc.lastIndexOf(var+"=");
			if(inde != -1) {
				var = findVarString(inde, "", doc);
				int num = var.indexOf("=");
				var = var.substring(num +1, var.length());
				System.out.println(var);
				return matchName(proposal, var, o, offset);
			}
		}
		return false;
	}

	private boolean  matchName(Set<ICompletionProposal> proposals, String parserName, int num, int offset)
	{
		if(parserName.equals("api.require('db')")) {
			proposals.clear();
			createDBProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('msm')") || parserName.equals("api.require(\"msm\")")) {
			proposals.clear();
			createMSM(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('mam')") || parserName.equals("api.require(\"mam\")")) {
			proposals.clear();
			createMAM(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('push')") || parserName.equals("api.require(\"push\")")) {
			proposals.clear();
			createPUSH(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('imageBrowser')") || parserName.equals("api.require(\"imageBrowser\")")) {
			proposals.clear();
			createImageBrows(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('scanner')") || parserName.equals("api.require(\"scanner\")")) {
			proposals.clear();
			createScanner(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('fs')") || parserName.equals("api.require(\"fs\")")) {
			proposals.clear();
			createFSProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('baiDuLocation')") || parserName.equals("api.require(\"baiDuLocation\")")) {
			proposals.clear();
			createBaiDuLocation(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('baiDuMap')") || parserName.equals("api.require(\"baiDuMap\")")) {
			proposals.clear();
			createBaiDuMap(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('fileBrowser')") || parserName.equals("api.require(\"fileBrowser\")")) {
			proposals.clear();
			createFileBrowser(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('qq')") || parserName.equals("api.require(\"qq\")")) {
			proposals.clear();
			createQQ(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('weiXin')") || parserName.equals("api.require(\"weiXin\")")) {
			proposals.clear();
			createWeiXin(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('sinaWeiBo')") || parserName.equals("api.require(\"sinaWeiBo\")")) {
			proposals.clear();
			createSinaWeiBo(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('aliPay')") || parserName.equals("api.require(\"aliPay\")")) {
			proposals.clear();
			createAliPayProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('calendar')") || parserName.equals("api.require(\"calendar\")")) {
			proposals.clear();
			createCalendarProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('cardReader')") || parserName.equals("api.require(\"cardReader\")")) {
			proposals.clear();
			createCardReaderProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('cityList')") || parserName.equals("api.require(\"cityList\")")) {
			proposals.clear();
			createCityListProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('contact')") || parserName.equals("api.require(\"contact\")")) {
			proposals.clear();
			createContactProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('docReader')") || parserName.equals("api.require(\"docReader\")")) {
			proposals.clear();
			createDocReaderProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('inputField')") || parserName.equals("api.require(\"inputField\")")) {
			proposals.clear();
			createInputFieldProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('pdfReader')") || parserName.equals("api.require(\"pdfReader\")")) {
			proposals.clear();
			createPdfReaderProposal(proposals, offset, num);
			return true;
		} 
		if(parserName.equals("api.require('pieChart')") || parserName.equals("api.require(\"pieChart\")")) {
			proposals.clear();
			createPieChartProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('speechRecognizer')") || parserName.equals("api.require(\"speechRecognizer\")")) {
			proposals.clear();
			createSpeechRecognizerProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('personalCenter')") || parserName.equals("api.require(\"personalCenter\")")) {
			proposals.clear();
			createPersonalCenterProposal(proposals, offset, num);
			return true;
		} 
		if(parserName.equals("api.require('downloadManager')") || parserName.equals("api.require(\"downloadManager\")")) {
			proposals.clear();
			createDownloadManagerProposal(proposals, offset, num);
			return true;
		} 
		if(parserName.equals("api.require('barChart')") || parserName.equals("api.require(\"barChart\")")) {
			proposals.clear();
			createBarChartProposal(proposals, offset, num);
			return true;
		} 
		if(parserName.equals("api.require('lineChart')") || parserName.equals("api.require(\"lineChart\")")) {
			proposals.clear();
			createLineChartProposal(proposals, offset, num);
			return true;
		} 
		if(parserName.equals("api.require('coverFlow')") || parserName.equals("api.require(\"coverFlow\")")) {
			proposals.clear();
			createCoverFlowProposal(proposals, offset, num);
			return true;
		} 
		if(parserName.equals("api.require('scannerView')") || parserName.equals("api.require(\"scannerView\")")) {
			proposals.clear();
			createScannerViewProposal(proposals, offset, num);
			return true;
		} 
		if(parserName.equals("api.require('slider')") || parserName.equals("api.require(\"slider\")")) {
			proposals.clear();
			createSliderProposal(proposals, offset, num);
			return true;
		} 
		if(parserName.equals("api.require('tabBar')") || parserName.equals("api.require(\"tabBar\")")) {
			proposals.clear();
			createTabBarProposal(proposals, offset, num);
			return true;
		} 
		if(parserName.equals("api.require('listView')") || parserName.equals("api.require(\"listView\")")) {
			proposals.clear();
			createListViewProposal(proposals, offset, num);
			return true;
		} 
		if(parserName.equals("api.require('actionButton')") || parserName.equals("api.require(\"actionButton\")")) {
			proposals.clear();
			createActionButtonProposal(proposals, offset, num);
			return true;
		} 
		if(parserName.equals("api.require('panorama')") || parserName.equals("api.require(\"panorama\")")) {
			proposals.clear();
			createPanoramaProposal(proposals, offset, num);
			return true;
		} 
		if(parserName.equals("api.require('zip')") || parserName.equals("api.require(\"zip\")")) {
			proposals.clear();
			createZipProposal(proposals, offset, num);
			return true;
		} 
		if(parserName.equals("api.require('arcMenu')") || parserName.equals("api.require(\"arcMenu\")")) {
			proposals.clear();
			createArcMenuProposal(proposals, offset, num);
			return true;
		} 
		if(parserName.equals("api.require('arcProgress')") || parserName.equals("api.require(\"arcProgress\")")) {
			proposals.clear();
			createArcProgressProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('trans')") || parserName.equals("api.require(\"trans\")")) {
			proposals.clear();
			createTransProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('periodSelector')") || parserName.equals("api.require(\"periodSelector\")")) {
			proposals.clear();
			createPeriodSelectorProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('bluetooth')") || parserName.equals("api.require(\"bluetooth\")")) {
			proposals.clear();
			createBluetoothProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('timeSelector')") || parserName.equals("api.require(\"timeSelector\")")) {
			proposals.clear();
			createTimeSelectorProposal(proposals, offset, num);
			return true;
		} 
		if(parserName.equals("api.require('navigationMenu')") || parserName.equals("api.require(\"navigationMenu\")")) {
			proposals.clear();
			createNavigationMenuProposal(proposals, offset, num);
			return true;
		} 
		if(parserName.equals("api.require('pullMenu')") || parserName.equals("api.require(\"pullMenu\")")) {
			proposals.clear();
			createPullMenuProposal(proposals, offset, num);
			return true;
		} 
		if(parserName.equals("api.require('scrollPicture')") || parserName.equals("api.require(\"scrollPicture\")")) {
			proposals.clear();
			createScrollPictureProposal(proposals, offset, num);
			return true;
		} 
		if(parserName.equals("api.require('searchBar')") || parserName.equals("api.require(\"searchBar\")")) {
			proposals.clear();
			createSearchBarProposal(proposals, offset, num);
			return true;
		} 
		if(parserName.equals("api.require('bubbleMenu')") || parserName.equals("api.require(\"bubbleMenu\")")) {
			proposals.clear();
			createBbubbleMenuProposal(proposals, offset, num);
			return true;
		} 
		if(parserName.equals("api.require('loadingLabel')") || parserName.equals("api.require(\"loadingLabel\")")) {
			proposals.clear();
			createLoadingLabelProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('citySelector')") || parserName.equals("api.require(\"citySelector\")")) {
			proposals.clear();
			createCitySelectorProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('sideMenu')") || parserName.equals("api.require(\"sideMenu\")")) {
			proposals.clear();
			createSideMenuProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('graph')") || parserName.equals("api.require(\"graph\")")) {
			proposals.clear();
			createGraphProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('netAudio')") || parserName.equals("api.require(\"netAudio\")")) {
			proposals.clear();
			createNetAudioProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('shakeView')") || parserName.equals("api.require(\"shakeView\")")) {
			proposals.clear();
			createShakeViewProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('listContact')") || parserName.equals("api.require(\"listContact\")")) {
			proposals.clear();
			createListContactProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('scrollRotation')") || parserName.equals("api.require(\"scrollRotation\")")) {
			proposals.clear();
			createScrollRotationProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('tabBarMenu')") || parserName.equals("api.require(\"tabBarMenu\")")) {
			proposals.clear();
			createTabBarMenuProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('circularMenu')") || parserName.equals("api.require(\"circularMenu\")")) {
			proposals.clear();
			createCircularMenuProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('multiSelector')") || parserName.equals("api.require(\"multiSelector\")")) {
			proposals.clear();
			createMultiSelectorProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('chatBox')") || parserName.equals("api.require(\"chatBox\")")) {
			proposals.clear();
			createChatBoxProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('stackMenu')") || parserName.equals("api.require(\"stackMenu\")")) {
			proposals.clear();
			createStackMenuProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('bookReader')") || parserName.equals("api.require(\"bookReader\")")) {
			proposals.clear();
			createBookReaderProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('navigationBar')") || parserName.equals("api.require(\"navigationBar\")")) {
			proposals.clear();
			createNavigationBarProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('meChat')") || parserName.equals("api.require(\"meChat\")")) {
			proposals.clear();
			createMeChatProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('model')") || parserName.equals("api.require(\"model\")")) {
			proposals.clear();
			createModelProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('query')") || parserName.equals("api.require(\"query\")")) {
			proposals.clear();
			createQueryProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('relation')") || parserName.equals("api.require(\"relation\")")) {
			proposals.clear();
			createRelationProposal(proposals, offset, num);
			return true;
		}
		if(parserName.equals("api.require('user')") || parserName.equals("api.require(\"user\")")) {
			proposals.clear();
			createUserProposal(proposals, offset, num);
			return true;
		}
		
		return false;
	}

	private void getReplaceString(int offset, IDocument document) throws BadLocationException
	{
		int num = 1;
		while(true){
			replaceStr = document.get(offset-num, 1);
			System.out.println("replaceStr is : " + replaceStr);
			if(replaceStr.equals(".") || replaceStr.equals(">") || replaceStr.equals("\t") || replaceStr.equals(" ") || replaceStr.equals("\n") || replaceStr.equals("\r")) {
				break;
			}
			if(offset == num) {
				replaceStr = "";
			}
			num++;
		}
		replaceStr = document.get(offset-num+1, num-1);
		System.out.println("replaceStr is : " + replaceStr);
		if(replaceStr.contains("<") || replaceStr.contains("\"") ) {
			replaceStr = replaceStr.substring(1);
		}
	}
	
	private String findVarString(int offset, String replaceStrs, String doc) throws BadLocationException {
		int index = 1;
		while(true){
			replaceStrs = doc.substring(offset+index, offset+index +1);
			System.out.println("var string is : " + replaceStrs);
			if(replaceStrs.equals(";")) {
				break;
			}
			if(offset + index == doc.length()) {
				System.out.println("var string ==================" +replaceStrs);
				return "";
				
			}
			index++;
		}
		System.out.println(offset);
		System.out.println(index + offset);
		replaceStrs = doc.substring(offset, index + offset);
		System.out.println("var string ==================" +replaceStrs);
		return replaceStrs;
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
	 * getArgumentIndex
	 * 
	 * @param offset
	 * @return
	 */
	private int getArgumentIndex(int offset)
	{
		JSArgumentsNode arguments = getArgumentsNode(offset);
		int result = -1;

		if (arguments != null)
		{
			for (IParseNode child : arguments)
			{
				if (child.contains(offset))
				{
					result = child.getIndex();
					break;
				}
			}
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
					 if (lexeme.getType() == JSTokenType.DOT) {
						 return  LocationType.IN_PROPERTY_NAME;
					 }
					
					 if (lexeme.getType() == JSTokenType.EQUAL){
						 System.out.println(lexeme.getType() == JSTokenType.EQUAL);
						 return  LocationType.IN_GLOBAL;
					 }
					 if (lexeme.getType() == JSTokenType.DO) {
						 return  LocationType.IN_GLOBAL;
						 }
						 
						 if (lexeme.getType() == JSTokenType.IDENTIFIER) {
//							 replaceStr = lexeme.getText();
							 System.out.println(lexeme.getText());
							 if("api".equals(lexeme.getText())) {
								 return  LocationType.API;
							 } else if ("document".equals(lexeme.getText())) {
								 return  LocationType.IN_PROPERTY_NAME;
							 }
							 return  LocationType.IN_GLOBAL;
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

			if (partition != null && AUTO_ACTIVATION_PARTITION_TYPES.contains(partition.getType()))
			{
				int start = partition.getOffset();
				int index = offset - 1;

				while (index >= start)
				{
					char candidate = document.getChar(index);

					if (candidate == ',' || candidate == '(' || candidate == '{')
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
		catch (BadLocationException e)
		{
			// ignore
		}

		return result;
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
	
	private void createApicloudProposal(Set<ICompletionProposal> proposals, int offset, int location) {
		createApicloudPropertyProposal(proposals, offset);
		createApicloudFunctionProposal(proposals, offset, location);
	}
	
	private void create$ApicloudProposal(Set<ICompletionProposal> proposals, int offset, int location) {
		create$APIFunctionProposal(proposals, offset, location);
	}

	private void addParameter(Set<ICompletionProposal> proposals, int offset)
	{
//		String api = targetNode.getParent().getParent().toString();
//		int index = api.indexOf(".");
//		int index2 = api.indexOf("(");
//		if(index == -1 || index2 == -1) {
////			System.out.println(targetNode.getParent().getParent().getParent().getParent().toString());
//			return;
//		}
//		String apiName = api.substring(0, index);
//		String methodName = api.substring(index + 1, index2);
//		System.out.println(apiName);
//		System.out.println(methodName);
	}
	
	private void create$APIFunctionProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		addApicloudProposal(proposals, "trim", "trim(str);",  JS_FUNCTION, "<h3>描述</h3>"
//		addProposal(proposals, "require", JS_FUNCTION, "<h3>描述</h3>"
				+"去掉字符串首尾空格<br>"
				+ "<h3>参数</h3>"
				+ "str (类型：String)<br>"
				+ "<h3>实例</h3>"
				+ "$api.trimAll('  abc 123  ');  // => \"abc123\"<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "trimAll", "trimAll(str);",  JS_FUNCTION, "<h3>描述</h3>"
						+"去掉字符串所有空格<br>"
						+ "<h3>参数</h3>"
						+ "str (类型：String)<br>"
						+ "<h3>实例</h3>"
						+ "$api.trimAll('  abc 123  ');  // => \"abc123\"<br>"
						, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "isArray", "isArray(obj);",  JS_FUNCTION, "<h3>描述</h3>"
				+"判断对象是否为数组<br>"
				+ "<h3>参数</h3>"
				+ "obj (类型：Object)<br>"
				+ "<h3>实例</h3>"
				+ "$api.isArray([1,2,3]);  // => true<br>"
				+ "$api.isArray('123')  // => false<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "addEvt", "addEvt(el, name, fn, useCapture);",  JS_FUNCTION, "<h3>描述</h3>"
				+"为DOM元素绑定事件<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "name (类型：String)：事件类型<br>"
				+ "fn (类型：Function)：事件回调<br>"
				+ "useCapture (类型：Boolean)：事件捕获，默认为 false<br>"
				+ "<h3>实例</h3>"
				+ "$api.addEvt(element, 'click', function(){<br>  //do something<br>});<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "rmEvt", "rmEvt(el, name, fn, useCapture);",  JS_FUNCTION, "<h3>描述</h3>"
				+"移除DOM元素绑定的事件<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "name (类型：String)：事件类型<br>"
				+ "fn (类型：Function)：事件回调<br>"
				+ "useCapture (类型：Boolean)：事件捕获，默认为 false<br>"
				+ "<h3>实例</h3>"
				+ "$api.rmEvt(element, 'click', function(){<br>  //do something<br>});<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "one", "one(el, name, fn, useCapture);",  JS_FUNCTION, "<h3>描述</h3>"
				+"为DOM元素绑定事件，事件只执行一次<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "name (类型：String)：事件类型<br>"
				+ "fn (类型：Function)：事件回调<br>"
				+ "useCapture (类型：Boolean)：事件捕获，默认为 false<br>"
				+ "<h3>实例</h3>"
				+ "$api.one(element, 'click', function(){<br>  //do something<br>});<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "dom", "dom(el, selector);",  JS_FUNCTION, "<h3>描述</h3>"
				+"选择首个匹配的DOM元素<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "selector (类型：Selector)：CSS 选择器<br>"
				+ "<h3>实例</h3>"
				+ "$api.dom(el, '#id');<br>"
				+ "$api.dom(el, '.list[type=\"text\"]');<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "domAll", "domAll(el, selector);",  JS_FUNCTION, "<h3>描述</h3>"
				+"选择所有匹配的DOM元素<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "selector (类型：Selector)：CSS 选择器<br>"
				+ "<h3>实例</h3>"
				+ "$api.domAll(el, '.class');<br>"
				+ "$api.domAll(el, '.list:fist-child');<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "byId", "byId(id);",  JS_FUNCTION, "<h3>描述</h3>"
				+"通过Id选择DOM元素<br>"
				+ "<h3>参数</h3>"
				+ "id(类型：String)：CSS id 字符串<br>"
				+ "<h3>实例</h3>"
				+ "$api.byId('idStr');<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "first", "first(el, selector);",  JS_FUNCTION, "<h3>描述</h3>"
				+"选择DOM元素的第一个子元素<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "selector (类型：Selector)：CSS 选择器<br>"
				+ "<h3>实例</h3>"
				+ "$api.first(el,'li');<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "last", "last(el, selector);",  JS_FUNCTION, "<h3>描述</h3>"
				+"选择DOM元素的最后一个子元素<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "selector (类型：Selector)：CSS 选择器<br>"
				+ "<h3>实例</h3>"
				+ "$api.last(el,'li');<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "eq", "eq(el, index);",  JS_FUNCTION, "<h3>描述</h3>"
				+"选择第几个子元素<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "index (类型：String | Number)：索引值<br>"
				+ "<h3>实例</h3>"
				+ "$api.eq(el, 2);<br>"
				+ "$api.eq(el, '2');<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "not", "not(el, selector);",  JS_FUNCTION, "<h3>描述</h3>"
				+"根据排除选择器选择子元素<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "selector (类型：Selector)：CSS 选择器<br>"
				+ "<h3>实例</h3>"
				+ "$api.not(el, '.active');<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "prev", "prev(el);",  JS_FUNCTION, "<h3>描述</h3>"
				+"选择相邻的前一个元素<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "<h3>实例</h3>"
				+ "$api.prev(el);<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "prev", "prev(el);",  JS_FUNCTION, "<h3>描述</h3>"
				+"选择相邻的前一个元素<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "<h3>实例</h3>"
				+ "$api.prev(el);<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "next", "next(el);",  JS_FUNCTION, "<h3>描述</h3>"
				+"选择相邻的下一个元素<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "<h3>实例</h3>"
				+ "$api.next(el);<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "contains", "contains(parentEl, targetEl);",  JS_FUNCTION, "<h3>描述</h3>"
				+"判断某一个元素是否包含目标元素<br>"
				+ "<h3>参数</h3>"
				+ "parentEl (类型：Element)：DOM元素<br>"
				+ "targetEl (类型：Element)：DOM元素<br>"
				+ "<h3>实例</h3>"
				+ "$api.contains(parentEl, targetEl);<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "closest", "closest(el, selector);",  JS_FUNCTION, "<h3>描述</h3>"
				+"根据选择器匹配最近的父元素<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "selector (类型：Selector)：CSS 选择器<br>"
				+ "<h3>实例</h3>"
				+ "$api.closest(el, '.parent');<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "remove", "remove(el);",  JS_FUNCTION, "<h3>描述</h3>"
				+"移除DOM元素<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "<h3>实例</h3>"
				+ "$api.remove(el);<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "attr", "attr(el, name, value);",  JS_FUNCTION, "<h3>描述</h3>"
				+"获取或设置DOM元素的属性<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "name (类型：String)：属性名<br>"
				+ "value (类型：String)：属性值<br>"
				+ "<h3>实例</h3>"
				+ "$api.attr(el,'data','123');<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "removeAttr", "removeAttr(el, name);",  JS_FUNCTION, "<h3>描述</h3>"
				+"移除DOM元素的属性<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "name (类型：String)：属性名<br>"
				+ "<h3>实例</h3>"
				+ "$api.removeAttr(el, 'data');<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "hasCls", "hasCls(el, cls);",  JS_FUNCTION, "<h3>描述</h3>"
				+"DOM元素是否含有某个className<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "cls (类型：String)：className<br>"
				+ "<h3>实例</h3>"
				+ "$api.hasCls(el, 'classname');<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "addCls", "addCls(el, cls);",  JS_FUNCTION, "<h3>描述</h3>"
				+"为DOM元素增加className<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "cls (类型：String)：className<br>"
				+ "<h3>实例</h3>"
				+ "$api.addCls(el, 'newclass'); <br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "removeCls", "removeCls(el, cls);",  JS_FUNCTION, "<h3>描述</h3>"
				+"移除指定的className<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "cls (类型：String)：className<br>"
				+ "<h3>实例</h3>"
				+ "$api.removeCls(el, 'newclass');<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "toggleCls", "toggleCls(el, cls);",  JS_FUNCTION, "<h3>描述</h3>"
				+"切换指定的className<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "cls (类型：String)：className<br>"
				+ "<h3>实例</h3>"
				+ "$api.toggleCls(el, 'display');<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "val", "val(el, val);",  JS_FUNCTION, "<h3>描述</h3>"
				+"获取或设置常用 Form 表单元素的 value 值<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "val (类型：String)：想设置的value值<br>"
				+ "<h3>实例</h3>"
				+ "$api.val(el,'123'); <br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "prepend", "prepend(el, html);",  JS_FUNCTION, "<h3>描述</h3>"
				+"在DOM元素内部，首个子元素前插入HTML字符串<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "html (类型：htmlString)：HTML字符串<br>"
				+ "<h3>实例</h3>"
				+ "$api.prepend(el,'<li>hello</li>');<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "prepend", "prepend(el, html);",  JS_FUNCTION, "<h3>描述</h3>"
				+"在DOM元素内部，首个子元素前插入HTML字符串<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "html (类型：htmlString)：HTML字符串<br>"
				+ "<h3>实例</h3>"
				+ "$api.prepend(el,'<li>hello</li>');<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "append", "append(el, html);",  JS_FUNCTION, "<h3>描述</h3>"
				+"在DOM元素内部，最后一个子元素后面插入HTML字符串<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "html (类型：htmlString)：HTML字符串<br>"
				+ "<h3>实例</h3>"
				+ "$api.append(el,'<li>hello</li>');<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "before", "before(el, html);",  JS_FUNCTION, "<h3>描述</h3>"
				+"在DOM元素前面插入HTML字符串<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "html (类型：htmlString)：HTML字符串<br>"
				+ "<h3>实例</h3>"
				+ "$api.before(el,'<h1>world</h1>');<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "after", "after(el, html);",  JS_FUNCTION, "<h3>描述</h3>"
				+"在DOM元素后面插入HTML字符串<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "html (类型：htmlString)：HTML字符串<br>"
				+ "<h3>实例</h3>"
				+ "$api.after(el,'<h1>world</h1>');<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "html", "html(el, html);",  JS_FUNCTION, "<h3>描述</h3>"
				+"获取或设置DOM元素的innerHTML<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "html (类型：htmlString)：HTML字符串<br>"
				+ "<h3>实例</h3>"
				+ "$api.html(el,'<h1>world</h1>');<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "text ", "text(el, txt);",  JS_FUNCTION, "<h3>描述</h3>"
				+"设置或者获取元素的文本内容<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "html (类型：htmlString)：HTML字符串<br>"
				+ "<h3>实例</h3>"
				+ "var a = document.getElementById('a');<br>"
				+ "$api.text(a, 'text'); <br>"
				, getActiveUserAgentIds(), "$API", offset);	
		addApicloudProposal(proposals, "offset  ", "offset(el);",  JS_FUNCTION, "<h3>描述</h3>"
				+"获取元素在页面中的位置与宽高，(此为距离页面左侧及顶端的位置，并非距离窗口的位置)<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "<h3>实例</h3>"
				+ "var a = document.getElementById('a');<br>"
				+ "$api.offset(el);<br>"
				, getActiveUserAgentIds(), "$API", offset);	
		addApicloudProposal(proposals, "css", "css(el,css);",  JS_FUNCTION, "<h3>描述</h3>"
				+"设置所传入的DOM元素的样式，可传入多条样式<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "css(类型：String)：想要设置的CSS样式<br>"
				+ "<h3>实例</h3>"
				+ "$api.css(el,'width:800px;border:1px solid red');<br>"
				, getActiveUserAgentIds(), "$API", offset);	
		addApicloudProposal(proposals, "cssVal", "cssVal(el,prop);",  JS_FUNCTION, "<h3>描述</h3>"
				+"获取指定DOM元素的指定属性的完整的值，如800px<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "prop(类型：String)：CSS属性<br>"
				+ "<h3>实例</h3>"
				+ "$api.cssVal(el,'width');<br>"
				, getActiveUserAgentIds(), "$API", offset);	
		addApicloudProposal(proposals, "jsonToStr", "jsonToStr(json);",  JS_FUNCTION, "<h3>描述</h3>"
				+"将标准的JSON 对象转换成字符串格式<br>"
				+ "<h3>参数</h3>"
				+ "json(类型：JSON)<br>"
				+ "<h3>实例</h3>"
				+ "var json = {a:111, b:222};<br>"
				+ "$api.jsonToStr(json);<br>"
				, getActiveUserAgentIds(), "$API", offset);	
		addApicloudProposal(proposals, "strToJson", "strToJson(str);",  JS_FUNCTION, "<h3>描述</h3>"
				+"将JSON字符串转换成JSON对象<br>"
				+ "<h3>参数</h3>"
				+ "str(类型：String)：JSON字符串<br>"
				+ "<h3>实例</h3>"
				+ "var a = '{\"a\":\"111\", \"b\":\"222\"}';<br>"
				+ "$api.strToJson(a); <br>"
				, getActiveUserAgentIds(), "$API", offset);	
		addApicloudProposal(proposals, "strToJson", "strToJson(str);",  JS_FUNCTION, "<h3>描述</h3>"
				+"将JSON字符串转换成JSON对象<br>"
				+ "<h3>参数</h3>"
				+ "str(类型：String)：JSON字符串<br>"
				+ "<h3>实例</h3>"
				+ "var a = '{\"a\":\"111\", \"b\":\"222\"}';<br>"
				+ "$api.strToJson(a); <br>"
				, getActiveUserAgentIds(), "$API", offset);	
		addApicloudProposal(proposals, "setStorage", "setStorage(key,value);",  JS_FUNCTION, "<h3>描述</h3>"
				+"设置localStorage数据<br>"
				+ "<h3>参数</h3>"
				+ "key(类型：String)：键名<br>"
				+ "value(类型：任意类型)：值<br>"
				+ "<h3>实例</h3>"
				+ "$api.setStorage('name','Tom');<br>"
				, getActiveUserAgentIds(), "$API", offset);	
		addApicloudProposal(proposals, "setStorage", "setStorage(key,value);",  JS_FUNCTION, "<h3>描述</h3>"
				+"设置localStorage数据<br>"
				+ "<h3>参数</h3>"
				+ "key(类型：String)：键名<br>"
				+ "value(类型：任意类型)：值<br>"
				+ "<h3>实例</h3>"
				+ "$api.setStorage('name','Tom');<br>"
				, getActiveUserAgentIds(), "$API", offset);	
		addApicloudProposal(proposals, "getStorage", "getStorage(key);",  JS_FUNCTION, "<h3>描述</h3>"
				+"获取localStorage数据，必须与$api.setStorage()配套使用<br>"
				+ "<h3>参数</h3>"
				+ "key(类型：String)：键名<br>"
				+ "<h3>实例</h3>"
				+ "$api.getStorage('name');<br>"
				, getActiveUserAgentIds(), "$API", offset);	
		addApicloudProposal(proposals, "rmStorage", "rmStorage(key);",  JS_FUNCTION, "<h3>描述</h3>"
				+"清除localStorage中与键名对应的值<br>"
				+ "<h3>参数</h3>"
				+ "key(类型：String)：键名<br>"
				+ "<h3>实例</h3>"
				+ "$api.rmStorage('name');<br>"
				, getActiveUserAgentIds(), "$API", offset);	
		addApicloudProposal(proposals, "clearStorage", "clearStorage();",  JS_FUNCTION, "<h3>描述</h3>"
				+"清除localStorage的所有数据，慎用<br>"
				+ "<h3>参数</h3>"
				+ "<h3>实例</h3>"
				+ "$api.clearStorage();<br>"
				, getActiveUserAgentIds(), "$API", offset);	
		addApicloudProposal(proposals, "fixIos7Bar", "fixIos7Bar(el);",  JS_FUNCTION, "<h3>描述</h3>"
				+"适配iOS7+系统状态栏，为传入的DOM元素增加20px的上内边距<br>"
				+ "<h3>参数</h3>"
				+ "el (类型：Element)：DOM元素<br>"
				+ "<h3>实例</h3>"
				+ "$api.fixIos7Bar(header);<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "toast", "toast(title,text,time);",  JS_FUNCTION, "<h3>描述</h3>"
				+"延时提示框<br>"
				+ "<h3>参数</h3>"
				+ "title (类型：String) ： 标题(可选参数)<br>"
				+ "text(类型：String)：内容(可选参数)<br>"
				+ "time(类型：Number)：延时的时间(可选参数),单位为毫秒<br>"
				+ "<h3>实例</h3>"
				+ "$api.toast('演示', '延时提示框',1000);<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "get", "get(url,fnSuc,dataType);",  JS_FUNCTION, "<h3>描述</h3>"
				+"api.ajax()方法的get方式简写<br>"
				+ "<h3>参数</h3>"
				+ "url (类型：String) :  url(必传参数)<br>"
				+ "fnSuc (类型：Function)：成功回调函数(可选参数)<br>"
				+ "dataType(类型：String)：返回值的类型(可选参数)，有text与json两种类型，默认为json<br>"
				+ "<h3>实例</h3>"
				+ "$api.get('http://www.apicloud.com',function(ret){<br>  alert(ret);<br>},'text');<br>"
				, getActiveUserAgentIds(), "$API", offset);
		addApicloudProposal(proposals, "post", "post(url,data,fnSuc,dataType);",  JS_FUNCTION, "<h3>描述</h3>"
				+"api.ajax()方法的post方式简写<br>"
				+ "<h3>参数</h3>"
				+ "url (类型：String) :  url(必传参数)<br>"
				+ "data(类型：JSON):  可以有body：请求体（字符串类型）values：post参数（JSON对象）files：post文件（JSON对象）等参数(可选参数)<br>"
				+ "fnSuc (类型：Function)：成功回调函数(可选参数)<br>"
				+ "dataType(类型：String)：返回值的类型(可选参数)，有text与json两种类型，默认为json<br>"
				+ "<h3>实例</h3>"
				+ "$api.post('http://www.apicloud.com', {body: 'String'}, function(ret){<br>  alert(ret);<br>},'text');<br>"
				, getActiveUserAgentIds(), "$API", offset);
		
	}

	private void createApicloudPropertyProposal(Set<ICompletionProposal> proposals, int offset)
	{
		addProposal(proposals, "appName", JS_PROPERTY, "<h3>描述</h3>" 
				+ "应用在桌面显示名称，字符串类型<br>"
				+ "<h3>可用性</h3>" 
				+ "IOS系统，安卓系统，PC模拟器<br>"
				+ "可提供的1.0.0及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addProposal(proposals, "version", JS_PROPERTY, "<h3>描述</h3>" 
				+ "引擎版本信息，字符串类型<br>"
				+ "<h3>可用性</h3>" 
				+ "IOS系统，安卓系统，PC模拟器<br>"
				+ "可提供的1.0.0及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addProposal(proposals, "systemType", JS_PROPERTY, "<h3>描述</h3>" 
				+ "系统类型，字符串类型<br>"
				+ "<h3>补充说明</h3>"
				+ "取值范围详见系统类型常量<br>"
				+ "<h3>可用性</h3>" 
				+ "IOS系统，安卓系统，PC模拟器<br>"
				+ "可提供的1.0.0及更高版本<br>"
				, getActiveUserAgentIds(), "API", offset);
		addProposal(proposals, "systemVersion", JS_PROPERTY, "<h3>描述</h3>" 
				+ "手机平台的系统版本，字符串类型<br>"
				+ "<h3>可用性</h3>" 
				+ "IOS系统，安卓系统，PC模拟器<br>"
				+ "可提供的1.0.0及更高版本<br>"
				, getActiveUserAgentIds(), "API", offset);
		addProposal(proposals, "deviceId", JS_PROPERTY, "<h3>描述</h3>" 
				+ "设备唯一标识，字符串类型<br>"
				+ "<h3>可用性</h3>" 
				+ "IOS系统，安卓系统，PC模拟器<br>"
				+ "可提供的1.0.0及更高版本<br>"
				, getActiveUserAgentIds(), "API", offset);
		addProposal(proposals, "deviceModel", JS_PROPERTY, "<h3>描述</h3>" 
				+ "设备型号，字符串类型<br>"
				+ "<h3>可用性</h3>" 
				+ "IOS系统，安卓系统，PC模拟器<br>"
				+ "可提供的1.0.0及更高版本<br>"
				, getActiveUserAgentIds(), "API", offset);
		addProposal(proposals, "deviceName", JS_PROPERTY, "<h3>描述</h3>" 
				+ "设备名称，字符串类型<br>"
				+ "<h3>可用性</h3>" 
				+ "IOS系统，安卓系统，PC模拟器<br>"
				+ "可提供的1.0.0及更高版本<br>"
				, getActiveUserAgentIds(), "API", offset);
		addProposal(proposals, "connectionType", JS_PROPERTY, "<h3>描述</h3>" 
				+ "网络类型，字符串类型<br>"
				+ "<h3>补充说明</h3>"
				+ "取值范围详见网络类型常量<br>"
				+ "<h3>可用性</h3>" 
				+ "IOS系统，安卓系统，PC模拟器<br>"
				+ "可提供的1.0.0及更高版本<br>"
				, getActiveUserAgentIds(), "API", offset);
		addProposal(proposals, "fullScreen", JS_PROPERTY, "<h3>描述</h3>" 
				+ "应用是否全屏，布尔类型，只支持iOS<br>"
				+ "<h3>可用性</h3>" 
				+ "IOS系统<br>"
				+ "可提供的1.0.0及更高版本<br>"
				, getActiveUserAgentIds(), "API", offset);
		addProposal(proposals, "wgtParam", JS_PROPERTY, "<h3>描述</h3>" 
				+ "widget参数，提供widget间参数传递，JSON对象类型<br>"
				+ "<h3>可用性</h3>" 
				+ "IOS系统，安卓系统，PC模拟器<br>"
				+ "可提供的1.0.0及更高版本<br>"
				, getActiveUserAgentIds(), "API", offset);
		addProposal(proposals, "pageParam", JS_PROPERTY, "<h3>描述</h3>" 
				+ "窗口参数，提供窗口间参数传递，JSON对象类型<br>"
				+ "<h3>可用性</h3>" 
				+ "IOS系统，安卓系统，PC模拟器<br>"
				+ "可提供的1.0.0及更高版本<br>"
				, getActiveUserAgentIds(), "API", offset);
		addProposal(proposals, "appParam", JS_PROPERTY, "<h3>描述</h3>" 
				+ "应用参数，提供应用间参数传递，字符串类型<br>"
				+ "<h3>可用性</h3>" 
				+ "IOS系统，安卓系统，PC模拟器<br>"
				+ "可提供的1.0.0及更高版本<br>"
				, getActiveUserAgentIds(), "API", offset);
		addProposal(proposals, "wgtRootDir", JS_PROPERTY, "<h3>描述</h3>" 
				+ "widget根目录，字符串类型<br>"
				+ "<h3>可用性</h3>" 
				+ "IOS系统，安卓系统，PC模拟器<br>"
				+ "可提供的1.0.0及更高版本<br>"
				, getActiveUserAgentIds(), "API", offset);
		addProposal(proposals, "winName", JS_PROPERTY, "<h3>描述</h3>" 
				+ "主窗口名称，字符串类型<br>"
				+ "<h3>可用性</h3>" 
				+ "IOS系统，安卓系统，PC模拟器<br>"
				+ "可提供的1.0.0及更高版本<br>"
				, getActiveUserAgentIds(), "API", offset);
		addProposal(proposals, "winWidth", JS_PROPERTY, "<h3>描述</h3>" 
				+ "主窗口宽度，数字类型<br>"
				+ "<h3>可用性</h3>" 
				+ "IOS系统，安卓系统，PC模拟器<br>"
				+ "可提供的1.0.0及更高版本<br>"
				, getActiveUserAgentIds(), "API", offset);
		addProposal(proposals, "winHeight", JS_PROPERTY, "<h3>描述</h3>" 
				+ "主窗口高度，数字类型<br>"
				+ "<h3>可用性</h3>" 
				+ "IOS系统，安卓系统，PC模拟器<br>"
				+ "可提供的1.0.0及更高版本<br>"
				, getActiveUserAgentIds(), "API", offset);
		addProposal(proposals, "frameName", JS_PROPERTY, "<h3>描述</h3>" 
				+ "子窗口名称，字符串类型<br>"
				+ "<h3>可用性</h3>" 
				+ "IOS系统，安卓系统，PC模拟器<br>"
				+ "可提供的1.0.0及更高版本<br>"
				, getActiveUserAgentIds(), "API", offset);
		addProposal(proposals, "frameWidth", JS_PROPERTY, "<h3>描述</h3>" 
				+ "子窗口宽度，数字类型<br>"
				+ "<h3>可用性</h3>" 
				+ "IOS系统，安卓系统，PC模拟器<br>"
				+ "可提供的1.0.0及更高版本<br>"
				, getActiveUserAgentIds(), "API", offset);
		addProposal(proposals, "frameHeight", JS_PROPERTY, "<h3>描述</h3>" 
				+ "子窗口高度，数字类型<br>"
				+ "<h3>可用性</h3>" 
				+ "IOS系统，安卓系统，PC模拟器<br>"
				+ "可提供的1.0.0及更高版本<br>"
				, getActiveUserAgentIds(), "API", offset);
		
	}
	
	private void createApicloudFunctionProposal(Set<ICompletionProposal> proposals, int offset, int length)	{
		String location = getString(length);
		addApicloudRequire(proposals, "require", "require('');",  JS_FUNCTION, "<h3>描述</h3>"
//		addProposal(proposals, "require", JS_FUNCTION, "<h3>描述</h3>"
				+"创建其他模块对象<br>"
				+ "<h3>参数</h3>"
				+ "String：其他模块的名称<br>"
				+"<h3>回调函数</h3>"
				+"无<br>"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "installApp", 
				showCode(location,  "installApp", false, "appUri: ''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"安装应用<br>"
				+ "<h3>参数</h3>"
				+ "appUri：目标应用的资源文件标识。Android上为apk包的本地路径，<br>" 
				+"如file://xxx；IOS上为对应的itunes地址或安装包地址（对应的plist文件地址，字符串类型）	默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "openApp", 
						showCode(location, "openApp", true, "iosUrl: '',", 	"androidPkg: ''")
						, JS_FUNCTION, "<h3>描述</h3>"
						+"打开手机上其它应用<br>"
						+ "<h3>参数</h3>"
						+ "androidPkg：目标应用的包名或action（Android平台使用，字符串类型）	，Android下不能为空<br>"
						+ "iosUrl：目标应用的url（ios平台使用，字符串类型），iOS下不能为空<br>" 
						+ "mimeType：指定目标应用的响应数据类型，如：\"text/html\"（Android平台使用，字符串类型）<br>"
						+ "uri：指定目标应用响应的uri（Android平台使用）<br>"
						+ "appParam：传给目标应用的参数（JSON对象）	默认值：无<br>"
						+"<h3>回调函数</h3>"
						+"function({目标应用关闭后的返回值（JSON类型）}，｛code:错误码，msg:错误描述｝){}"
						+"<br><h3>可用性</h3>"
						+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
						, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "openWidget",
				showCode(location, "openWidget", true, "id: 'UZ00000001'"	)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开Widget<br>"
				+ "<h3>参数</h3>"
				+ "id：widget ID（字符串类型）必须指定 默认值：无<br>"
				+ "wgtParam：wgt参数（JSON对象）默认值：无<br>" 
				+ "animation：动画参数JSON字典类型包含以下3个参数：<br>"
				+ "type：动画类型（详见动画类型常量）<br>"
				+ "subType：动画子类型（详见动画子类型常量）<br>"
				+ "duration：动画时间	默认值：0.3秒<br>"
				+"<h3>回调函数</h3>"
				+"function({新wgt关闭时候的返回值，JSON对象}，｛code:错误码，msg:错误描述｝){}"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "closeWidget",
				showCode(location, "closeWidget", false, "id: ''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭Widget<br>"
				+ "<h3>参数</h3>"
				+ "id：widget ID（字符串类型），不能为空 默认值为：无<br>"
				+ "retData：返回给上个widget的返回值（JSON对象） 默认值：无<br>" 
				+ "silent：是否静默退出，只在主widget中有效（布尔型类型） 默认值：false<br>" 
				+ "animation：动画参数，为空时使用默认动画（JSON对象）<br>"
				+ "type：动画类型（详见动画类型常量）<br>"
				+ "subType：动画子类型（详见动画子类型常量）<br>"
				+ "duration：动画过渡时间时间	默认值：0.3秒<br>"
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "openWin",
				showCode(location, "openWin", false, "name: '',", "url: ''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开窗口<br>"
				+ "<h3>参数</h3>"
				+ "name：窗口名字（字符串类型）不能为空 默认值；无<br>"
				+ "url：请求地址（字符串类型）不能为空	默认值：无<br>" 
				+ "pageParam：页面参数（JSON对象）	默认值：无<br>"
				+ "bounces：页面是否弹动（布尔类型）默认值：false<br>"
				+ "opaque：页面是否透明（布尔类型）默认值：false<br>" 
				+ "bgColor：背景色，格式（#fff,#ffffff,rgba(r,g,b,a)，字符串）默认值：无<br>"
				+"vScrollBarEnabled：是否显示垂直滚动条（布尔类型）默认值：true<br>"
				+"hScrollBarEnabled：是否显示水平滚动条（布尔类型）默认值：true<br>"
				+ "scaleEnabled： 页面是否可以缩放（布尔类型） 默认值：false <br>"
				+ "slidBackEnabled：是否支持滑动返回。iOS7.0及以上系统中，在新打开的窗口左边缘处向右滑动，可以返回到上一个窗口，该字段只iOS有效（布尔类型）默认值：false<br>"
				+ "animation：动画参数，为空时使用默认动画<br>"
				+ "type：动画类型（详见动画类型常量）<br>"
				+ "subType：动画子类型（详见动画子类型常量）<br>"
				+ "duration：动画时间	默认值：0.3秒<br>"
				+ "showProgress：是否显示等待框，只在url为网址时有效（布尔类型）默认值：false<br>"
				+ "delay：窗口显示延迟时间，适用于将被打开的窗口中可能需要打开有耗时操作的模块时，可延迟窗口展示到屏幕的时间，保持UI的整体性（数字类型） 默认值；0<br>"
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本<br>"
				+"模拟器除(bounces，opaque，bgColor，vScrollBarEnabled，hScrollBarEnabled）以外"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "setWinAttr", showCode(location, "setWinAttr", false)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置主窗口属性<br>"
				+ "<h3>参数</h3>"
				+ "bounces： 页面是否弹动（布尔类型） 默认值：无 <br>"
				+ "opaque： 页面是否不透明（布尔类型） 默认值：无 <br>" 
				+ "bgColor：背景色，格式（#fff,#ffffff,rgba(r,g,b,a)，字符串）默认值：无<br>"
				+"vScrollBarEnabled：是否显示垂直滚动条（布尔类型）默认值：无<br>"
				+"hScrollBarEnabled：是否显示水平滚动条（布尔类型）默认值：无<br>"
				+ "scaleEnabled： 页面是否可以缩放（布尔类型） 默认值：false <br>"
				+ "slidBackEnabled： 是否支持滑动返回。iOS7.0及以上系统中，在新打开的窗口左边缘处向右滑动，可以返回到上一个窗口，若不期望返回到之前的页面，则设置为false，该字段只iOS有效（布尔类型） 默认值：true <br>"
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本<br>"
				+"模拟器除(bounces，opaque，bgColor，vScrollBarEnabled，hScrollBarEnabled，slidBackEnabled）以外"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "closeWin", showCode(location, "closeWin", false)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭窗口<br>"
				+ "<h3>参数</h3>"
				+ "name：窗口名字（字符串类型） 默认值为：无<br>"
				+ "animation：动画参数，为空时使用默认动画（JSON对象）内部字段：{<br>"
				+ "type：动画类型（详见动画类型常量）<br>"
				+ "subType：动画子类型（详见动画子类型常量）<br>"
				+ "duration：动画过渡时间	默认值：0.3秒}<br>"
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "closeToWin", showCode(location, "closeToWin", false, "name: 'page1'")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭到窗口<br>"
				+ "<h3>参数</h3>"
				+ "name：窗口名字（字符串类型）不能为空 默认值为：无<br>"
				+ "animation：动画参数，为空时使用默认动画（JSON对象）内部字段：{<br>"
				+ "type：动画类型（详见动画类型常量）<br>"
				+ "subType：动画子类型（详见动画子类型常量）<br>"
				+ "duration：动画过渡时间	默认值：0.3秒}<br>"
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "execScript", showCode(location, "execScript", false, "script: 'exec();'")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"在指定窗口中执行脚本<br>"
				+ "<h3>参数</h3>"
				+ "name：窗口名字（字符串类型）默认值为：无<br>"
				+ "frameName：子窗口名字（字符串类型）默认值为：无<br>"
				+ "script：js代码（字符串类型）不能为空 默认值为：无<br>" 
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "openFrame", showCode(location, "openFrame", false, "name: '',", "url: '',",
				showObject(location, true, "rect" , "x:0,", "y:0,", "w:0,", "h:0"))
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开子窗口<br>"
				+ "<h3>参数</h3>"
				+ "name：窗口名字（字符串类型）不能为空 默认值：无<br>"
				+ "url：请求地址（字符串类型）	默认值：无<br>" 
				+"vScrollBarEnabled：是否显示垂直滚动条（布尔类型）默认值：true<br>"
				+"hScrollBarEnabled：是否显示水平滚动条（布尔类型）默认值：true<br>"
				+ "scaleEnabled： 页面是否可以缩放（布尔类型） 默认值：false <br>"
				+ "rect：窗口区域（JSON对象）包含以下4个参数：<br>"
				+ "x：左上角x坐标    y：左上角y坐标<br>"
				+ "	w：宽度                 h：高度<br>"
				+ "pageParam：页面参数（JSON对象）默认值：无<br>"
				+ "bounces：页面是否弹动（布尔类型）默认值：true<br>"
				+ "	opaque：页面是否不透明（布尔类型）	默认值；false<br>" 
				+ "bgColor：背景色（#fff,#ffffff,rgba(r,g,b,a)）	默认值：无<br>"
				+ "showProgress：是否显示等待框，只在url为网址时有效（布尔类型）默认值：false<br>"
				+ "reload：页面已经打开时，是否重新加载页面（布尔类型）默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本<br>"
				+"模拟器除(bounces，opaque，bgColor，vScrollBarEnabled，hScrollBarEnabled，scaleEnabled）以外"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "setFrameAttr", showCode(location, "setFrameAttr", false, "name: ''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置主窗口属性<br>"
				+ "<h3>参数</h3>"
				+ "name： 窗口名称(字符串类型）不能为空 默认值：无 <br>"
				+"vScrollBarEnabled：是否显示垂直滚动条（布尔类型）默认值：无<br>"
				+"hScrollBarEnabled：是否显示水平滚动条（布尔类型）默认值：无<br>"
				+ "rect：窗口区域（JSON对象）默认值：无 包含以下4个参数：<br>"
				+ "x：左上角x坐标    y：左上角y坐标<br>"
				+ "	w：宽度            h：高度<br>"
				+ "bounces：页面是否弹动（布尔类型）默认值:无<br>"
				+ "scaleEnabled：页面是否可以缩放（布尔类型）默认值:无<br>"
				+ "hidden：本frame是否隐藏（隐藏即从屏幕上移除，但不销毁，布尔类型）默认值:无<br>"
				+ "	opaque：页面是否透明（布尔类型）	默认值:无<br>" 
				+ "bgColor：背景色（#fff,#ffffff,rgba(r,g,b,a)）	默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本<br>"
				+"模拟器除(bounces，opaque，bgColor，vScrollBarEnabled，hScrollBarEnabled，scaleEnabled）以外"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "closeFrame", showCode(location, "closeFrame", false, "name: ''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭子窗口<br>"
				+"<h3>参数</h3>"
				+ "name：窗口名字（字符串类型）不能为空 默认值为：无<br>"
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "bringFrameToFront", showCode(location, "bringFrameToFront", false, "from: ''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"调整子窗口到前面<br>"
				+ "<h3>参数</h3>"
				+ "	from：窗口名字，不能为空（字符串类型） 默认值：无 <br>"
				+ "to：窗口名字， 指定时调整窗口到此窗口前面，否则调整到最前面（字符串类型） 默认值：无 <br>"
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "sendFrameToBack", showCode(location, "sendFrameToBack", false, "from: ''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"调整子窗口到后面<br>"
				+ "<h3>参数</h3>"
				+ "	from：窗口名字，不能为空（字符串类型） 默认值：无 <br>"
				+ "to：窗口名字， 指定时调整窗口到此窗口后面，否则调整到最后面（字符串类型） 默认值：无 <br>"
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "animation", showCode(location, "animation", false, "name: ''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"子窗口动画，支持平移，缩放，旋转和透明度变化<br>"
				+ "<h3>参数</h3>"
				+ "	name：子窗口名字，不能为空（字符串类型） 默认值：当前子窗口 <br>"
				+ "delay：动画延迟时间，单位毫秒，默认立即开始（数字类型） 默认值：0 <br>"
				+ "duration：动画过渡时间，单位毫秒（数字类型） 默认值：0 <br>"
				+ "	curve：动画曲线类型，详见动画曲线类型（字符串类型） 默认值：easeInOut <br>"
				+ "repeatCount：动画次数，默认不重复，为-1时无限重复（数字类型） 默认值：0 <br>"
				+ "autoreverse：一次动画结束后是否自动反转动画（布尔类型） 默认值：false <br>"
				+ "alpha：透明度，介于0 - 1之间（数字类型） 默认值：无 <br>"
				+ "translation：平移参数（JSON类型） 默认值：无  内部字段：{x:x轴方向上的平移距离，<br>"
				+"默认为0 y:y轴方向上的平移距离，默认为0    z:  z轴方向上的平移距离，默认为0}<br>"
				+ "scale：放大参数（JSON类型） 默认值：无  内部字段：{x:x轴方向上的放大倍率，<br>"
				+"默认为1  y:y轴方向上的放大倍率，默认为1    z:  z轴方向上的放大倍率，默认为1}<br>"
				+ "rotation：旋转参数（JSON类型） 默认值：无  内部字段：{degree:旋转角度，默认0<br>"
				+ " x:绕x轴旋转，默认为0    y: 绕y轴旋转，默认为0    z: 绕z轴旋转，默认为1}<br>"
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "openFrameGroup", showCode(location, "openFrameGroup", true, "name: ''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开窗口组<br>"
				+ "<h3>参数</h3>"
				+ "name：窗口组名字（字符串类型）不能为空 默认值：无<br>"
				+ "background：窗口组背景，背景色（#fff,#ffffff,rgba(r,g,b,a)）或图片（字符串类型） 默认值：无<br>"
				+ "scrollEnabled：窗口组是否能够左右滚动（布尔类型） 默认值：true<br>"
				+ "rect：窗口组区域（JSON对象）默认值：无 包含以下4个参数：<br>"
				+ "x：左上角x坐标    y：左上角y坐标<br>"
				+ "	w：宽度            h：高度<br>"
				+ "index：默认显示的页面索引（数字类型） 默认值：0<br>"
				+ "frames：子窗口数组（数组类型）不能为空 默认值：无 包括内部字段<br>"
				+"[name:窗口名字，字符串类型，不能为空;url:地址，字符串类型，不能为空;<br>" +
				"pageParam:{}, 页面参数，JSON对象。默认值：空;bounces:是否弹动，布尔型，默认值：false;<br>" +
				"opaque:是否不透明，布尔型，默认值：false;bgColor:背景色，（#fff,#ffffff,rgba(r,g,b,a)）<br>" +
				"，默认值：无；vScrollBarEnabled:是否显示垂直滚动条，布尔型，默认值：true;<br>" +
				"vScrollBarEnabled:是否显示水平滚动条，布尔型，默认值：false<br>" 
				+"<h3>回调函数</h3>"
				+"function({name:当前窗口名称；index: 当前窗口索引}){}"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "closeFrameGroup", showCode(location, "closeFrameGroup", false, "name: ''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭窗口组<br>"
				+ "<h3>参数</h3>"
				+ "name：窗口组名字（字符串类型）不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "setFrameGroupIndex", showCode(location, "setFrameGroupIndex", false, "name: '',","index:")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置窗口组当前可见子窗口<br>"
				+ "<h3>参数</h3>"
				+ "name：窗口组名字（字符串类型）不能为空 默认值：无<br>"
				+ "index：子窗口索引（数字类型）不能为空 默认值：无<br>"
				+ "scroll：是否平滑滚动至目标窗口（布尔类型） 默认值：false<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "setFrameGroupAttr", showCode(location, "setFrameGroupAttr", false, "name: ''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置窗口组属性<br>"
				+ "<h3>参数</h3>"
				+ "name：窗口组名字（字符串类型）不能为空 默认值：无<br>"
				+ "hidden：窗口组是否隐藏（布尔类型） 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "openSlidLayout", showCode(location, "openSlidLayout", true, "fixedPane:{},", "slidPane:{}")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开侧滑式布局<br>"
				+ "<h3>参数</h3>"
				+ "type：侧滑类型（left：左侧滑、right：右侧滑、all：左右侧滑，字符串类型） 默认值：all<br>"
				+ "leftEdge：左侧滑时，侧滑window停留时露出的宽度（数字类型） 默认值：60<br>"
				+ "rightEdge：右侧滑时，侧滑window停留时露出的宽度（数字类型） 默认值：60<br>"
				+ "fixedPane：底部固定层window（JSON类型）不能为空 默认值：无内部字段：<br>"
				+ "{name:窗口名字（字符串类型）不能为空 url:地址 不能为空  bgColor:背景色（#fff,#ffffff,rgba(r,g,b,a)）<br>"
				+ "bounces:是否弹动，默认false  opaque:是否不透明，默认false <br>" 
				+"vScrollBarEnabled:是否显示垂直滚动条，默认true hScrollBarEnabled:是否显示水平滚动条，默认true}<br>"
				+ "slidPane：侧滑层window（JSON类型）不能为空 默认值：无内部字段：<br>"
				+ "{name:窗口名字（字符串类型）不能为空 url:地址 不能为空  bgColor:背景色（#fff,#ffffff,rgba(r,g,b,a)）<br>"
				+ "bounces:是否弹动，默认false  opaque:是否不透明，默认false <br>" 
				+"vScrollBarEnabled:是否显示垂直滚动条，默认true hScrollBarEnabled:是否显示水平滚动条，默认true}<br>"
				+"<h3>回调函数</h3>"
				+"function({  type: ' left'    //侧滑方向，left或right}){}"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "openSlidPane", showCode(location, "openSlidPane", false, "type:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"侧滑<br>"
				+ "<h3>参数</h3>"
				+ "type：侧滑类型，left或right（字符串类型）不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "closeSlidPane",  "closeSlidPane();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"当SlidPane处于打开状态时，将其收起<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "addEventListener", showCode(location, "addEventListener", true, "name:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"监听事件<br>"
				+ "<h3>参数</h3>"
				+ "name：事件名字（字符串类型）不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({事件发生时传递的参数JSON对象}){}"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "removeEventListener", showCode(location, "removeEventListener", false, "name:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"删除事件<br>"
				+ "<h3>参数</h3>"
				+ "name：事件名字（详见事件，字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "setRefreshHeaderInfo", showCode(location, "setRefreshHeaderInfo", true)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"显示顶部刷新<br>"
				+ "<h3>参数</h3>"
				+ "visible：是否可见（布尔类型）	默认值：true<br>"
				+ "loadingImgae：刷新图片地址（字符串类型）	默认值：旋转箭头图片<br>" 
				+ "bgColor：背景颜色（#fff,#ffffff,rgba(r,g,b,a)，字符串类型）	默认值：rgb(187,236,153,1.0)<br>"
				+ "textColor：文字颜色（#fff,#ffffff,rgba(r,g,b,a)，字符串类型）	默认值：rgb(109, 128, 153,1.0)<br>"
				+ "textDown	：下拉文字描述（字符串类型）默认值：下拉可以刷新...<br>"
				+ "textUp：松开时文字描述（字符串类型）默认值：松开可以刷新...<br>"
				+ "	showTime：是否显示更新时间（布尔类型）	默认值：true<br>"
				+"<h3>回调函数</h3>"
				+"function({可以为空},{code:错误码，msg:错误描述}){}"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统，PC模拟器（loadingImgae，showTime以外）可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "refreshHeaderLoadDone", "refreshHeaderLoadDone();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"通知顶部刷新数据加载完毕<br>"
				+ "<h3>参数</h3>"
				+ "	无<br>"
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "alert", showCode(location, "alert", true)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"alert对话框<br>"
				+ "<h3>参数</h3>"
				+ "title：标题（字符串类型）默认值：无<br>"
				+ "msg：内容（字符串类型）默认值：无<br>" 
				+ "buttons：按钮（数组类型）默认值：[\"确定\"]<br>"
				+"<h3>回调函数</h3>"
				+"function({buttonIndex：按钮序号，从1开始},{code:错误码，msg:错误描述}){}"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "confirm", showCode(location, "confirm", true)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"confirm对话框<br>"
				+ "<h3>参数</h3>"
				+ "title：标题（字符串类型）默认值：无<br>"
				+ "msg：内容（字符串类型）默认值：无<br>" 
				+ "buttons：按钮（数组类型）默认值：[\"确定\"，\"取消\"]<br>"
				+"<h3>回调函数</h3>"
				+"function({buttonIndex：按钮序号，从1开始},{code:错误码，msg:错误描述}){}必须指定"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "prompt", showCode(location, "prompt", true)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"prompt对话框<br>"
				+ "<h3>参数</h3>"
				+ "title：标题（字符串类型）默认值：无<br>"
				+ "msg：内容（字符串类型）默认值：无<br>" 
				+ "buttons：按钮（数组类型）默认值：[\"确定\"，\"取消\"]<br>"
				+"<h3>回调函数</h3>"
				+"function({buttonIndex：按钮序号从1开始，text:输入文字},{code:错误码，msg:错误描述}){}必须指定"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "showProgress", showCode(location, "showProgress", false)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"显示<br>"
				+ "<h3>参数</h3>"
				+ "style：风格（详见进度提示框风格常量，字符串） 默认值为：default<br>"
				+ "animationType:动画类型（详见进度提示框动画类型常量，字符串） 默认值为:fade<br>" 
				+ "	title：标题（字符串类型）默认值：\"加载中\"<br>"
				+ "	text：文本（字符串类型）默认值：\"请稍后...\"<br>"
				+ "	modal：是否模态（布尔类型）默认值：true<br>"
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "hideProgress", "hideProgress();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"隐藏进度提示框<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "toast", showCode(location, "toast", false,"msg:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"弹出一个定时自动关闭的提示框<br>"
				+ "<h3>参数</h3>"
				+ "msg：提示消息（字符串类型）不能为空 默认值：无<br>"
				+ "duration：持续时长，单位：毫秒（字符串类型） 默认值：2000<br>"
				+ "location：弹出位置（字符串类型） 默认值：bottom<br>"
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "setPrefs", showCode(location, "setPrefs", false,"key:'',", "value:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置<br>"
				+ "<h3>参数</h3>"
				+ "key：键（字符串类型）不能为空 默认值：无<br>"
				+ "	value：值（字符串类型）不能为空 默认值：无<br>" 
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "getPrefs", showCode(location, "getPrefs", true,"key:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"获取偏好设置值<br>"
				+ "<h3>参数</h3>"
				+ "key：键（字符串类型）不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({value:值},{code:错误码，msg:错误描述}){}必须指定"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "removePrefs", showCode(location, "removePrefs", false,"key:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"删除<br>"
				+ "<h3>参数</h3>"
				+ "key：键（字符串类型）不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "getPicture", showCode(location, "getPicture", true)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"获取图片<br>"
				+ "<h3>参数</h3>"
				+ "sourceType：图片源类型（详见图片源类型常量，字符串） 默认值:library<br>"
				+ "encodingType：编码格式（详见图片编码类型常量，字符串）默认值：png<br>"
				+ "	mediaValue：媒体类型（详见媒体类型常量，字符串）默认值：pic<br>" 
				+ "destinationType：返回数据类型（详见图片数据格式常量，字符串）默认值：url<br>"
				+ "	allowEdit：是否可以编辑（布尔类型）默认值：false<br>"
				+ "quality：图片质量0-100整数（数字类型）默认值：50<br>" 
				+ "targetWidth：压缩后的图片宽度（数字）默认值：原图宽度<br>" 
				+ "targetHeight：压缩后的图片高度（数字）默认值：原图高度<br>" 
				+ "	saveToPhotoAlbum：是否保存到相册（布尔类型）默认值：false<br>"
				+"<h3>回调函数</h3>"
				+" function({data:图片路径或Base64数据，duration：视频时长},{code:错误码，msg:错误描述}){}必须指定"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "ajax", showCode(location, "ajax", true,"url:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"异步请求<br>"
				+ "<h3>参数</h3>"
				+ "url：地址（字符串类型）不能为空 默认值：无<br>"
				+ "method：异步请求方法类型(详见异步请求方法类型常量，字符串)默认值：get<br>" 
				+ "cache：是否缓存（布尔类型）默认值：false<br>"
				+ "	timeout：超时时间，单位秒（数字类型）默认值：30<br>"
				+ "dataType：返回数据类型（详见异步请求返回数据类型常量，字符串）默认值：json<br>" 
				+ "headers：请求头(JSON对象) 默认值：无<br>"
				+ "needHeaders：是否需要返回头信息，返回的头信息获取方法（ret.headers，布尔类型） 默认值：false<br>"
				+ "data：POST数据，method为”get”时不传（JSON对象）包含以下3个参数：<br>"
				+ "body：请求体（字符串类型）默认值：空<br>"
				+ "values：Post参数（JSON对象）默认值：空<br>" 
				+ "files：Post文件（JSON对象）默认值：空<br>"
				+"<h3>回调函数</h3>"
				+"function({类型依赖于传入的dataType，JSON类型},{code:错误码,msg:错误描述,statusCode:网络状态码}){}"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "download", showCode(location, "download", true,"url:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"文件下载<br>"
				+ "<h3>参数</h3>"
				+ "url：下载地址（字符串类型），不能为空 默认值：无<br>"
				+ "savePath：存储路径，为空时使用自动创建的路径（字符串类型）默认值：无<br>" 
				+ "report：下载过程是否上报（布尔类型）默认值：false <br>" 
				+ "cache：是否使用本地缓存（布尔类型）默认值：true<br>" 
				+ "allowResume：是否允许断点续传（布尔类型）默认值：false<br>" 
				+"<h3>回调函数</h3>"
				+"function({ fileSize:文件大小（数字类型）,percent:下载进度(0-100数字类型)," +
				"state:下载状态（下载状态常亮）,savePath:存储路径（字符串类型）},{msg:错误描述}){}必须指定"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "cancelDownload", showCode(location, "cancelDownload", false,"url:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"取消文件下载<br>"
				+ "<h3>参数</h3>"
				+ "url：下载地址（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "call", showCode(location, "call", false,"number:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"电话<br>"
				+ "<h3>参数</h3>"
				+ "type：打电话类型（详见电话类型常量，字符串类型）默认值：tel_prompt<br>"
				+ "number：电话号码（字符串类型）不能为空，默认值：无<br>" 
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "sms", showCode(location, "sms", true, "number:'',", "text:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"短信<br>"
				+ "<h3>参数</h3>"
				+ "numbers：电话号码（数组类型）不能为空 默认值：无<br>"
				+ "text：文本内容（字符串类型）不能为空 默认值：无<br>" 
				+ "silent：是否后台发送，iOS不支持（布尔类型） 默认值：false<br>" 
				+"<h3>回调函数</h3>"
				+"function({status:发送状态，布尔类型},{code:错误码，msg:错误描述}){}"
				+"<br><h3>可用性</h3>"
				+"ios系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "mail", showCode(location, "mail", true, "recipients:'',", "subject:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"发送邮件<br>"
				+ "<h3>参数</h3>"
				+ "recipients：收件人（数组类型）不能为空 默认值：无<br>" 
				+ "subject：邮件主题（字符串类型）不能为空 默认值：无<br>" 
				+ "body：邮件内容（字符串类型）默认值：无<br>" 
				+ "attachments：附件地址（数组类型）默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:发送状态，布尔类型},{code:错误码，msg:错误描述}){}"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "readFile", showCode(location, "readFile", true, "path:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"读取文件<br>"
				+ "<h3>参数</h3>"
				+ "path：文件路径（字符串类型）不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({data:文件内容，字符串类型,status:操作成功状态值},{code:错误码,msg：错误描述}){}"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "writeFile", showCode(location, "writeFile", true, "path:'',", "data:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"写入文件<br>"
				+ "<h3>参数</h3>"
				+ "path：文件路径（字符串类型）不能为空 默认值：无<br>"
				+ "data：文件内容（字符串类型）不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态值},{ code:错误码，msg:错误描述}){}"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "startRecord", showCode(location, "startRecord", false)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"开始录音<br>"
				+ "<h3>参数</h3>"
				+ "path：文件路径，为空时自动创建路径（字符串类型）默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "stopRecord", showFunction(location, "stopRecord")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"停止录音<br>"
				+ "<h3>参数</h3>"
				+"无<br>"
				+"<h3>回调函数</h3>"
				+"function({path 返回的音频地址，字符串类型，duration：音频的时长，数字类型}" +
				"，{ code:错误码，msg:错误描述}){}"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "startPlay", showCode(location, "startPlay", false, "path:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"开始播放音频<br>"
				+ "<h3>参数</h3>"
				+ "path：文件路径（字符串类型）不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "stopPlay", "stopPlay();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"停止播放音频<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "startLocation", showCode(location, "startLocation", true, "accuracy:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"开始定位<br>"
				+ "<h3>参数</h3>"
				+ "accuracy：精度(详见定位精度常量，字符串类型)	不能为空 默认值：无<br>"
				+ "filter：位置更新所需最小距离(数字类型，单位米) 默认值：1.0<br>"
				+ "autoStop：获取到位置信息后是否自动停止定位（布尔类型）默认为true<br>"
				+"<h3>回调函数</h3>"
				+"function({latitude:纬度,longitude:经度,timestamp:时间戳,status:定位状态},{msg:错误描述}){}"
				+"<br><h3>可用性</h3>"
				+"IOS系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals,  "stopLocation", "stopLocation();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"停止定位<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"IOS系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "getLocation", showFunction(location, "getLocation")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"获取位置信息<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"function({latitude:纬度,longitude:经度,timestamp:时间戳},{msg:错误描述}){}"
				+"<br><h3>可用性</h3>"
				+"ios系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "startSensor", showCode(location, "startSensor", true, "type:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"开始记录<br>"
				+ "<h3>参数</h3>"
				+ "type：传感器类型(详见传感器类型传感器类型常量)	不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({x:x轴方向值,y:y轴方向值,z:z轴方向值,proximity: 是否接近设备,status:操作成功状态值},{msg:错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "stopSensor", showCode(location, "stopSensor", true, "type:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"停止传感器<br>"
				+ "<h3>参数</h3>"
				+ "type：传感器类型(详见传感器类型传感器类型常量，字符串类型)	不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "parseTapmode", "parseTapmode();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"解析元素tapmode属性，优化点击事件处理<br>"
				+ "<h3>参数</h3>"
				+"无<br>"
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "setStatusBarStyle", showCode(location, "setStatusBarStyle", false)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置状态栏样式<br>"
				+ "<h3>参数</h3>"
				+ "style： 状态栏样式（详见状态栏样式常量，字符串）默认值：dark<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "setFullScreen", showCode(location, "setFullScreen", false, "fullScreen:")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置是否全屏<br>"
				+ "<h3>参数</h3>"
				+ "fullScreen：是否全屏（布尔类型），不能为空 默认值：无"
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "openContacts", showFunction(location, "openContacts")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开通讯录<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态值,name:姓名,phone:电话号码},{msg:错误描述}){}"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "clearCache", "clearCache();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"清除缓存<br>"
				+ "<h3>参数</h3>"
				+ "无"
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "actionSheet", showCode(location, "actionSheet", true)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"底部弹出框<br>"
				+ "<h3>参数</h3>"
				+ "title： 标题（字符串类型）默认值：无<br>"
				+ "cancelTitle： 取消按钮标题（字符串类型）默认值：无<br>"
				+ "destructiveTitle：红色警示按钮标题（字符串类型）默认值：无<br>"
				+ "buttons ：其它按钮（数组类型）默认值：无<br>"
				+ "style ：样式设置，为空时使用默认样式 （JSON对象）默认值：无 内部字段：{<br>"
				+ " layerColor:’’,    遮蔽层颜色，仅支持rgba颜色，默认值：rgba（0, 0, 0, 0.4）<br>"
				+ "itemNormalColor:’’, 选项按钮正常状态背景颜色，支持#000、#000000、rgb、rgba， 默认值：#F1F1F1<br>"
				+ "itemPressColor:’’,  选项按钮按下时背景颜色，支持#000、#000000、rgb、rgba，默认值：#E6E6E6<br>"
				+ "fontNormalColor:’’,  选项按钮正常状态文字颜色，支持#000、#000000、rgb、rgba，默认值：#007AFF<br>"
				+ "fontPressColor:’’,  选项按钮按下时文字颜色，支持#000、#000000、rgb、rgba，默认值：#0060F0<br>"
				+ "titleFontColor:’’  标题文字颜色，支持#000、#000000、rgb、rgba，默认值：#8F8F8F}<br>"
				+"<h3>回调函数</h3>"
				+"function({buttonIndex:按钮点击序号，从1开始},{msg:错误描述}){}"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "openPicker", showCode(location, "openPicker", true, "type:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开时间选择器<br>"
				+ "<h3>参数</h3>"
				+ "type： 拾取器类型（详见拾取器类型常量，字符串类型），不能为空 默认值：无<br>"
				+ "date： 时间格式化字符串，格式yyyy-MM-dd HH:mm（字符串类型）默认值：当前时间<br>"
				+ "title： 标题（字符串类型）默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({year:年 month:月 day:日 hour:时 minute:分}){}"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "showFloatBox", showCode(location, "showFloatBox", false)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"展示一个悬浮框，浮动在屏幕上，点击时关闭当前widget<br>"
				+ "<h3>参数</h3>"
				+ "iconPath： 展示在悬浮框中的图片地址（字符串类型）默认值：应用图标<br>"
				+ "duration： 自动消隐时长。在该时长内不发生触摸悬浮框行为，<br>"
				+"悬浮框自动消隐至半透状态（字符串类型）默认值：5000毫秒<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "removeLaunchView", "removeLaunchView();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"移除启动图。若config.xml里面配置autoLaunch为false，则启动图不会自动消失，直到调用此方法移除。<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "loadSecureValue", showCode(location, "loadSecureValue", true, "key:")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"从加密文件中读取指定数据<br>"
				+ "<h3>参数</h3>"
				+ "key：键，不能为空（字符串类型）默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({value: 值}){}"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "notification", showCode(location, "notification", true)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"向用户发出震动、声音提示、灯光闪烁、状态栏消息等通知<br>"
				+ "<h3>参数</h3>"
				+ "vibrate：伴随节奏的震动，时间数组，时间单位为毫秒（数组类型）默认值：[500,500]<br>"
				+ "sound：提示音文件路径，不传默认使用系统提示音（字符串）默认值：系统提示音<br>"
				+ "light：设备提示灯是否闪烁（布尔型）默认值：false<br>"
				+ "notify：弹出通知到状态栏（JSON对象）默认值：无<br>"
				+"内部字段：{title:标题，默认值为应用名称，只Android有效  content:内容，默认值为'有新消息'}<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		addApicloudProposal(proposals, "setScreenOrientation", showCode(location, "setScreenOrientation", false, "orientation:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置屏幕旋转方向<br>"
				+ "<h3>参数</h3>"
				+ "orientation：旋转屏幕到指定方向，或根据重力感应自动旋转；如果参数指定方向，则屏幕不会在重力感应下自动旋转，详见屏幕旋转方向常量，不能为空（字符串类型）默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "API", offset);
		
	}
	private String showFunction(String location, String name)
	{
		String code = name + "(";
		code += "\n" + location + "function(ret,err) {\n" + location + "\t//coding...\n" + location + "});";
		return code;
	}
	private String showCode(String location, String name, boolean hasFun, String... codes)
	{
		String code = name + "({";
		for(String str : codes) {
			code +=  "\n\t" + location + str;
		}
		code += hasFun ? "\n" + location + "},function(ret,err){\n" + location + "\t//coding...\n" + location + "});" : "\n" + location + "});";
		return code;
//				"openApp({" 
//		+ "\n\t" + location + "iosUrl: 'wechat://',"
//		+"\n\t" + location + "appParam: {'appParam': 'app'},"
//		+"\n\t" + location + "androidPkg: 'android.intent.action.VIEW',"
//		+"\n\t" + location + "mimeType: 'text/html',"
//		+"\n\t" + location + "uri: 'http://www.baidu.com'"
//		+	"\n" + location + "},function(ret,err){});";
	}
	private String showParamObject(String location, boolean isLast, String name,String... codes)
	{
		return buildParam(location, isLast, name, "\n\t", "\n", codes);
	}

	private String buildArray(String location, boolean isLast, String name, String tab1, String tab2, String... codes)
	{
		String code = name + ": [{";
		for(String str : codes) {
			code +=  tab1 + location + str;
		}
		code += isLast ? tab2 + location + "}]" : tab2 + location + "}],";
		return code;
	}
	
	private String buildParam(String location, boolean isLast, String name, String tab1, String tab2, String... codes)
	{
		String code = name + ": {";
		for(String str : codes) {
			code +=  tab1 + location + str;
		}
		code += isLast ? tab2 + location + "}" : tab2 + location + "},";
		return code;
	}
	private String showObject(String location, boolean isLast, String name,String... codes)
	{
		return buildParam(location, isLast, name, "\n\t\t", "\n\t", codes);
//		String code = name + ": {";
//		for(String str : codes) {
//			code +=  "\n\t\t" + location + str;
//		}
//		code += isLast ? "\n\t" + location + "}" : "\n\t" + location + "},";
//		return code;
//				"openApp({" 
//		+ "\n\t" + location + "iosUrl: 'wechat://',"
//		+"\n\t" + location + "appParam: {'appParam': 'app'},"
//		+"\n\t" + location + "androidPkg: 'android.intent.action.VIEW',"
//		+"\n\t" + location + "mimeType: 'text/html',"
//		+"\n\t" + location + "uri: 'http://www.baidu.com'"
//		+	"\n" + location + "},function(ret,err){});";
	}
	private String showParam(boolean isLast, String name,String value) {
		return isLast ? name + ":" + value : name + ":" + value+ ",";
	}
	private String getString(int length)
	{
		String location = "";
		for(int i = 0; i < length; i++) {
			location += " ";
		}
		System.out.println("len==============================" + location.length());
		return location;
	}
	
	private void createDBProposal(Set<ICompletionProposal> proposals, int offset, int length) {
		System.out.println("db len======================" + length);
		String location = getString(length);
		addApicloudProposal(proposals, "openDatabase", showCode(location, "openDatabase", true, "name:''")
				,  JS_FUNCTION, "<h3>描述</h3>"
//		addProposal(proposals, "openDatabase", JS_FUNCTION, "<h3>描述</h3>"
				+"打开数据库<br>"
				+ "<h3>参数</h3>"
				+ "name：数据库名称（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态值},{msg:错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "DB API", offset);
		addApicloudProposal(proposals, "closeDatabase", showCode(location, "closeDatabase", true, "name:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭数据库<br>"
				+ "<h3>参数</h3>"
				+ "name：数据库名称（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态},{msg:错误描述}){ }	"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "DB API", offset);
		addApicloudProposal(proposals, "transaction", showCode(location, "transaction", true, "name:'',", "operation:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"执行事务操作语句<br>"
				+ "<h3>参数</h3>"
				+ "name：数据库名称（字符串类型），不能为空 默认值：无<br>"
				+ "operation：事务操作（详见事务操作类型常量，字符串），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态},{msg:错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "DB API", offset);
		addApicloudProposal(proposals, "executeSql", showCode(location, "executeSql", true, "name:'',", "sql:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"执行sql<br>"
				+ "<h3>参数</h3>"
				+ "name：数据库名称（字符串类型），不能为空 默认值：无<br>"
				+ "sql：sql 语句 （字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态},{ msg：错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "DB API", offset);
		addApicloudProposal(proposals, "selectSql", showCode(location, "selectSql", true, "name:'',", "sql:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"查询sql<br>"
				+ "<h3>参数</h3>"
				+ "name：数据库名称（字符串类型），不能为空 默认值：无<br>"
				+ "sql：sql 语句 （字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({data[]:查询结果数据,数组类型；status:操作成功状态},{msg:错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "DB API", offset);
//		addProposal(proposals, "TransactionOperation.BEGIN", JS_PROPERTY, "<h3>描述</h3>事物开始", getActiveUserAgentIds(),
//				"DB API", offset);
//		addProposal(proposals, "TransactionOperation.COMMIT", JS_PROPERTY, "<h3>描述</h3>提交事物", getActiveUserAgentIds(),
//				"DB API", offset);
//		addProposal(proposals, "TransactionOperation.ROLLBACK", JS_PROPERTY, "<h3>描述</h3>回滚", getActiveUserAgentIds(),
//				"DB API", offset);
	}
	
	private void createFSProposal(Set<ICompletionProposal> proposals, int offset, int length) 	{
		String location = getString(length);
		addApicloudProposal(proposals, "createDir", showCode(location, "createDir", true, "path: ''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"创建目录<br>"
				+ "<h3>参数</h3>"
				+ "path：目标路径（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态},{code：错误码,msg：错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "FS API", offset);
		addApicloudProposal(proposals, "createFile", showCode(location, "createFile", true, "path: ''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"创建文件<br>"
				+ "<h3>参数</h3>"
				+ "path：目标路径（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态},{code：错误码,msg：错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "FS API", offset);
		addApicloudProposal(proposals, "remove", showCode(location, "remove", true, "path: ''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"删除文件<br>"
				+ "<h3>参数</h3>"
				+ "path：目标路径（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态},{code：错误码,msg：错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "FS API", offset);
		addApicloudProposal(proposals, "copyTo", showCode(location, "copyTo", true, "oldPath: '',", "newPath: ''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"拷贝文件<br>"
				+ "<h3>参数</h3>"
				+ "oldPath：源路径（字符串类型），不能为空 默认值：无<br>"
				+ "newPath：目标路径（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态},{code：错误码,msg：错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "FS API", offset);
		addApicloudProposal(proposals, "moveTo", showCode(location, "moveTo", true, "oldPath: '',", "newPath: ''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"移动文件<br>"
				+ "<h3>参数</h3>"
				+ "oldPath：源路径（字符串类型），不能为空 默认值：无<br>"
				+ "newPath：目标路径（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态},{code：错误码,msg：错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "FS API", offset);
		addApicloudProposal(proposals, "rename", showCode(location, "rename", true, "oldPath: '',", "newPath: ''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"重命名<br>"
				+ "<h3>参数</h3>"
				+ "oldPath：源路径（字符串类型），不能为空 默认值：无<br>"
				+ "newPath：目标路径（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态},{code：错误码,msg：错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "FS API", offset);
		addApicloudProposal(proposals, "readDir", showCode(location, "readDir", true, "path: ''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"列出目录<br>"
				+ "<h3>参数</h3>"
				+ "path：目标路径（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态，data[]：所有子文件名称},{code：错误码,msg：错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "FS API", offset);
		addApicloudProposal(proposals, "open", showCode(location, "open", true, "path: '',", "flags: ''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开文件<br>"
				+ "<h3>参数</h3>"
				+ "path：目标路径（字符串类型），不能为空 默认值：无<br>"
				+ "flags：文件打开方式(详见文件打开方式常量，字符串类型)  不能为空 默认值：read<br>"
				+"<h3>回调函数</h3>"
				+"function({fd：文件句柄,status：操作状态},{code：错误码,msg:错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "FS API", offset);
		addApicloudProposal(proposals, "read", showCode(location, "read", true, "fd:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"读取文件<br>"
				+ "<h3>参数</h3>"
				+ "fd：open方法得到的文件句柄（字符串类型），不能为空 默认值：无<br>"
				+ "offset：当前文件偏移量（数字类型）默认值：0<br>"
				+ "length：读取内容长度（数字类型）默认值：0<br>"
				+"<h3>回调函数</h3>"
				+"function({data：文件内容,status：操作成功状态值},{code：错误码,msg:错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "FS API", offset);
		addApicloudProposal(proposals, "readUp", showFunction(location, "readUp")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"读取文件<br>"
				+ "<h3>参数</h3>"
				+ "fd：open方法得到的文件句柄（字符串类型），不能为空 默认值：当前文件句柄<br>"
				+ "length：此次向上读取数据的长度（数字类型）默认值：当前最近一次读取数据的length<br>"
				+"<h3>回调函数</h3>"
				+"function({data：文件内容,status：操作成功状态值},{code：错误码,msg:错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "FS API", offset);
		addApicloudProposal(proposals, "readDown", showFunction(location, "readDown")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"读取文件<br>"
				+ "<h3>参数</h3>"
				+ "fd：open方法得到的文件句柄（字符串类型），不能为空 默认值：当前文件句柄<br>"
				+ "length：此次向下读取数据的长度（数字类型）默认值：当前最近一次读取数据的length<br>"
				+"<h3>回调函数</h3>"
				+"function({data：文件内容,status：操作成功状态值},{code：错误码,msg:错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "FS API", offset);
		addApicloudProposal(proposals, "write", showCode(location, "write", true, "fd:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"写入文件<br>"
				+ "<h3>参数</h3>"
				+ "fd：open方法得到的文件句柄（字符串类型），不能为空 默认值：无<br>"
				+ "offset：当前文件偏移量（数字类型）默认值：0<br>"
				+ "data：写入数据（字符串类型）默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status：操作成功状态值},{code：错误码,msg:错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "FS API", offset);
		addApicloudProposal(proposals, "close", showCode(location, "close", true, "fd:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭文件<br>"
				+ "<h3>参数</h3>"
				+ "fd：open方法得到的文件句柄（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status：操作成功状态值},{code：错误码,msg:错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "FS API", offset);
		addApicloudProposal(proposals, "exist", showCode(location, "exist", true, "fd:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"判断文件是否存在<br>"
				+ "<h3>参数</h3>"
				+ "path：要判断的文件路径（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({exist:文件是否存在directory:文件是否是文件夹}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "FS API", offset);

//		addProposal(proposals, "ErrorCode.NO_ERROR", JS_PROPERTY, "<h3>描述</h3>错误", getActiveUserAgentIds(),
//				"FS API", offset);
//		addProposal(proposals, "ErrorCode.NOT_FOUND_ERR", JS_PROPERTY, "<h3>描述</h3>错误", getActiveUserAgentIds(),
//				"FS API", offset);
//		addProposal(proposals, "ErrorCode.NOT_READABLE_ERR ", JS_PROPERTY, "<h3>描述</h3>错误", getActiveUserAgentIds(),
//				"FS API", offset);
//		addProposal(proposals, "ErrorCode.ENCODING_ERR", JS_PROPERTY, "<h3>描述</h3>错误", getActiveUserAgentIds(),
//				"FS API", offset);
//		addProposal(proposals, "ErrorCode.NO_MODIFICATION_ALLOWED_ERR", JS_PROPERTY, "<h3>描述</h3>错误", getActiveUserAgentIds(),
//				"FS API", offset);
//		addProposal(proposals, "ErrorCode.INVALID_MODIFICATION_ERR", JS_PROPERTY, "<h3>描述</h3>错误", getActiveUserAgentIds(),
//				"FS API", offset);
//		addProposal(proposals, "ErrorCode.QUOTA_EXCEEDED_ERR", JS_PROPERTY, "<h3>描述</h3>错误", getActiveUserAgentIds(),
//				"FS API", offset);
//		addProposal(proposals, "ErrorCode.PATH_EXISTS_ERR", JS_PROPERTY, "<h3>描述</h3>错误", getActiveUserAgentIds(),
//				"FS API", offset);
//		addProposal(proposals, "FileOpenFlags.READ", JS_PROPERTY, "<h3>描述</h3>只读", getActiveUserAgentIds(),
//				"FS API", offset);
//		addProposal(proposals, "FileOpenFlags.WRITE", JS_PROPERTY, "<h3>描述</h3>可写", getActiveUserAgentIds(),
//				"FS API", offset);
//		addProposal(proposals, "FileOpenFlags.READ_WRITE", JS_PROPERTY, "<h3>描述</h3>可读，可写", getActiveUserAgentIds(),
//				"FS API", offset);

	}
	
	private void createBaiDuLocation(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "startLocation", showCode(location, "startLocation", true, "accuracy:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"开始定位<br>"
				+ "<h3>参数</h3>"
				+ "accuracy:精度（详见精度常量，字符串类型），不能为空 默认值：无<br>"
				+ "filter:位置更新所需最小距离（数字类型,单位米）默认值：1.0<br>"
				+ "autoStop:获取到位置信息后是否自动停止定位（布尔类型）默认值： true<br>"
				+"<h3>回调函数</h3>"
				+"function({status: 操作成功状态值,longitude：经度,latitude：纬度,timestamp：时间戳},{ msg:错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuLocation API", offset);
		addApicloudProposal(proposals, "stopLocation", "stopLocation();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"停止定位<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuLocation API", offset);
		addApicloudProposal(proposals, "getLocation", showFunction(location, "getLocation")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"获取位置信息<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"function({longitude：经度,latitude：纬度,timestamp：时间戳},{ msg:错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，pc模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuLocation API", offset);
	}
	
	private void createSinaWeiBo(Set<ICompletionProposal> proposals, int offset, int length) {
		String location = getString(length);
//		
//		addProposal(proposals, "registerApp", JS_FUNCTION, "<h3>描述</h3>"
//				+"注册应用<br>"
//				+ "<h3>参数</h3>"
//				+ "appId：开发者申请的应用appId（字符串类型），不能为空 默认值：无<br>"
//				+"<h3>回调函数</h3>"
//				+"function({status:操作成功状态},{msg:错误描述}){ }"
//				+"<br><h3>可用性</h3>"
//				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
//				, getActiveUserAgentIds(), "SinaWeiBo API", offset);
		addApicloudProposal(proposals, "auth", showCode(location, "auth", true	, "redirectUrl:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"授权<br>"
				+ "<h3>参数</h3>"
				+ "redirectUrl：微博应用配置里面的授权回调地址（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态，accessToken：token，userId：userID},｛code:错误码，msg:错误描述｝){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "SinaWeiBo API", offset);
		addApicloudProposal(proposals, "cancelAuth", showCode(location, "cancelAuth", true	, "redirectUrl:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"登出当前账号<br>"
				+ "<h3>参数</h3>"
				+ "无br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态，accessToken：token，userId：userID},｛code:错误码，msg:错误描述｝){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "SinaWeiBo API", offset);
		addApicloudProposal(proposals, "sendRequest", showCode(location, "sendRequest", true	, "contentType:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"发表内容<br>"
				+ "<h3>参数</h3>"
				+ "contentType：发表内容类型(详见内容类型常量，字符串)，不能为空 默认值：无<br>"
				+ "text：文本内容（contentType为TEXT时不能为空，字符串类型）默认值：无<br>"
				+ "	imageUrl：图片url地址（contentType为IMAGE时不能为空，字符串类型）默认值：无<br>"
				+ "media:多媒体内容(contentType为TEXT或IMAGE时可以为空）包含以下6个参数：<br>"
				+ "title：多媒体标题，字符串类型，不能为空<br>"
				+ "	description：多媒体描述，字符串类型，不能为空<br>"
				+ "thumbUrl：多媒体缩略图本地路径，字符串类型，不能为空<br>"
				+ "musicUrl：音乐网页url，字符串类型，contentType为music时不能为空<br>"
				+ "videoUrl：视频网页url，字符串类型，contentType为video时不能为空<br>"
				+ "webpageUrl：网页url，字符串类型，contentType为web_page时不能为空<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态},{code：错误码,msg：错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "SinaWeiBo API", offset);
//		addProposal(proposals, "ContentType.TEXT", JS_PROPERTY, "<h3>描述</h3>文本", getActiveUserAgentIds(),
//				"SinaWeiBo API", offset);
//		addProposal(proposals, "ContentType.IMAGE", JS_PROPERTY, "<h3>描述</h3>图片", getActiveUserAgentIds(),
//				"SinaWeiBo API", offset);
//		addProposal(proposals, "ContentType.MUSIC", JS_PROPERTY, "<h3>描述</h3>音乐", getActiveUserAgentIds(),
//				"SinaWeiBo API", offset);
//		addProposal(proposals, "ContentType.WEB_PAGE", JS_PROPERTY, "<h3>描述</h3>页面", getActiveUserAgentIds(),
//				"SinaWeiBo API", offset);
//		addProposal(proposals, "ErrorCode.NO_ERROR", JS_PROPERTY, "<h3>描述</h3>错误", getActiveUserAgentIds(),
//				"SinaWeiBo API", offset);
//		addProposal(proposals, "ErrorCode.NO_ERROR", JS_PROPERTY, "<h3>描述</h3>错误", getActiveUserAgentIds(),
//				"SinaWeiBo API", offset);
//		addProposal(proposals, "ErrorCode.USER_CANCEL", JS_PROPERTY, "<h3>描述</h3>用户取消", getActiveUserAgentIds(),
//				"SinaWeiBo API", offset);
//		addProposal(proposals, "ErrorCode.SENT_FAIL", JS_PROPERTY, "<h3>描述</h3>发送失败", getActiveUserAgentIds(),
//				"SinaWeiBo API", offset);
//		addProposal(proposals, "ErrorCode.AUTH_ERROR", JS_PROPERTY, "<h3>描述</h3>授权失败", getActiveUserAgentIds(),
//				"SinaWeiBo API", offset);
//		addProposal(proposals, "ErrorCode.CANCEL_INSTALL", JS_PROPERTY, "<h3>描述</h3>取消安装", getActiveUserAgentIds(),
//				"SinaWeiBo API", offset);
//		addProposal(proposals, "ErrorCode.UNSUPPORT", JS_PROPERTY, "<h3>描述</h3>未经证实", getActiveUserAgentIds(),
//				"SinaWeiBo API", offset);
//		addProposal(proposals, "ErrorCode.UNKNOWN", JS_PROPERTY, "<h3>描述</h3>未知", getActiveUserAgentIds(),
//				"SinaWeiBo API", offset);
		
	}

	private void createWeiXin(Set<ICompletionProposal> proposals, int offset, int length) {
		String location = getString(length);
		addApicloudProposal(proposals, "registerApp", showFunction(location, "registerApp")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"注册应用<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态},{ code:错误码，msg:错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "WeiXin API", offset);
		addApicloudProposal(proposals, "sendRequest", showCode(location, "sendRequest", true, "scene: 'timeline',"
				, "contentType:'',", "title:'',", "description:'',", "thumbUrl:'',", "contentUrl:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"发表内容<br>"
				+ "<h3>参数</h3>"
				+ "scene：场景(详见场景场景常量，字符串类型)，不能为空 默认值：timeline<br>"
				+ "contentType：发表内容类型(详见内容类型常量，字符串类型)，不能为空 默认值：无<br>"
				+ "title：标题（字符串类型），不能为空 默认值：无<br>"
				+ "description：内容（字符串类型），不能为空 默认值：无<br>"
				+ "thumbUrl：缩略图本地url地址（字符串类型），不能为空 默认值：无<br>"
				+ "	contentUrl：内容url地址（contentType为TEXT时可以不传）<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态},{code：错误码,msg：错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "WeiXin API", offset);
		addApicloudProposal(proposals, "getToken", showCode(location, "getToken", true, "secret: ''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"获取支付token<br>"
				+ "<h3>参数</h3>"
				+ "secret：商家从微信官方申请的secret(字符串类型)，不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({ status:操作成功状态值;token:返回的token值;expires:token过期时间},{msg：错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "WeiXin API", offset);
		addApicloudProposal(proposals, "getPayOrder", showCode(location, "getPayOrder", true, "token: '',", "orderInfo:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"获取预支付订单<br>"
				+ "<h3>参数</h3>"
				+ "token：上个接口获取到的token(字符串类型)，不能为空 默认值：无<br>"
				+ "orderInfo：订单详情(字符串类型)，不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态值;orderId:返回的订单号},{msg：错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "WeiXin API", offset);
		addApicloudProposal(proposals, "payOrder", showCode(location, "payOrder", true, "orderId: '',"
				, "partnerId:'',", "nonceStr:'',", "timeStamp:'',", "package:'',", "sign:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"支付订单<br>"
				+ "<h3>参数</h3>"
				+ "orderId：上个接口生成的订单号(字符串类型)，不能为空 默认值：无<br>"
				+ "partnerId：商家和微信合作的id号，审核通过后微信发送到商家邮箱(字符串类型)，不能为空 默认值：无<br>"
				+ "nonceStr：随机串，防重发(字符串类型)，不能为空 默认值：无<br>"
				+ "timeStamp：时间戳，防重发(字符串类型)，不能为空 默认值：无<br>"
				+ "package：商家根据财付通文档填写的数据和签名(字符串类型)，不能为空 默认值：无<br>"
				+ "sign：商家根据微信开放平台文档对数据做的签名(字符串类型)，不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态值;result:返回的支付结果},{msg：错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "WeiXin API", offset);
		
//		addProposal(proposals, "SceneSESSION", JS_PROPERTY, "<h3>描述</h3>...", getActiveUserAgentIds(),
//				"WeiXin API", offset);
//		addProposal(proposals, "Scene.TIMELINE", JS_PROPERTY, "<h3>描述</h3>...", getActiveUserAgentIds(),
//				"WeiXin API", offset);
//		addProposal(proposals, "Scene.FAVORITE", JS_PROPERTY, "<h3>描述</h3>...", getActiveUserAgentIds(),
//				"WeiXin API", offset);
//		addProposal(proposals, "ContentType.TEXT", JS_PROPERTY, "<h3>描述</h3>文本", getActiveUserAgentIds(),
//				"WeiXin API", offset);
//		addProposal(proposals, "ContentType.IMAGE", JS_PROPERTY, "<h3>描述</h3>图片", getActiveUserAgentIds(),
//				"WeiXin API", offset);
//		addProposal(proposals, "ContentType.MUSIC", JS_PROPERTY, "<h3>描述</h3>音乐", getActiveUserAgentIds(),
//				"WeiXin API", offset);
//		addProposal(proposals, "ContentType.VIDEO", JS_PROPERTY, "<h3>描述</h3>视频", getActiveUserAgentIds(),
//				"WeiXin API", offset);
//		addProposal(proposals, "ContentType.WEB_PAGE", JS_PROPERTY, "<h3>描述</h3>页面", getActiveUserAgentIds(),
//				"WeiXin API", offset);
//		addProposal(proposals, "ErrorCode.NO_ERROR", JS_PROPERTY, "<h3>描述</h3>错误", getActiveUserAgentIds(),
//				"WeiXin API", offset);
//		addProposal(proposals, "ErrorCode.UNSUPPORT", JS_PROPERTY, "<h3>描述</h3>未经证实", getActiveUserAgentIds(),
//				"WeiXin API", offset);
//		addProposal(proposals, "ErrorCode.COMMON", JS_PROPERTY, "<h3>描述</h3>...", getActiveUserAgentIds(),
//				"WeiXin API", offset);
//		addProposal(proposals, "ErrorCode.USER_CANCEL", JS_PROPERTY, "<h3>描述</h3>用户取消", getActiveUserAgentIds(),
//				"WeiXin API", offset);
//		addProposal(proposals, "ErrorCode.SENT_FAIL", JS_PROPERTY, "<h3>描述</h3>用户取消", getActiveUserAgentIds(),
//				"WeiXin API", offset);
//		addProposal(proposals, "ErrorCode.AUTH_DENY", JS_PROPERTY, "<h3>描述</h3>拒绝授权", getActiveUserAgentIds(),
//				"WeiXin API", offset);
	}
	
	private void createMAM(Set<ICompletionProposal> proposals, int offset, int length) {
		String location = getString(length);
		addApicloudProposal(proposals, "checkUpdate", showFunction(location, "checkUpdate")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"检查当前版本是否有更新或者被强制关闭<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态值,update:是否有更新,closed:设备上当前版本是否被强行关闭,<br>" +
				"version:新版本版本号,versionDes:新版本更新描述,closeTip:提示用户应用版本被强行关闭时弹框的提示语,<br>" +
				"updateTip:新版本安装包的下载地址,source:新版本安装包的下载地址,time:新版本发布时间},{msg:错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "MAM API", offset);
		
	}
	private void createPUSH(Set<ICompletionProposal> proposals, int offset, int length) {
		String location = getString(length);
		addApicloudProposal(proposals, "bind", showCode(location, "bind", true, "userName:'',", "userId:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"将来自业务系统的用户信息绑定至推送服务器<br>"
				+ "<h3>参数</h3>"
				+ "userName：用户名称，来自业务系统（字符串类型），不能为空 默认值：无<br>"
				+ "userId：用户Id，来自业务系统（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status: 操作成功状态值},{msg：错误描述})"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "PUSH API", offset);
		addApicloudProposal(proposals, "joinGroup", showCode(location, "joinGroup", true,"groupName:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"加入（绑定）某个群组<br>"
				+ "<h3>参数</h3>"
				+ "groupName：群组名称（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status: 操作成功状态值},{msg：错误描述})"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "PUSH API", offset);
		addApicloudProposal(proposals, "leaveGroup", showCode(location, "leaveGroup", true,"groupName:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"退出群组绑定<br>"
				+ "<h3>参数</h3>"
				+ "groupName：群组名称（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status: 操作成功状态值}，{msg:错误描述})"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "PUSH API", offset);
//		addProposal(proposals, "checkPushStatus", JS_FUNCTION, "<h3>描述</h3>"
//				+"检查推送服务目前的状态<br>"
//				+ "<h3>参数</h3>"
//				+ "无<br>"
//				+"<h3>回调函数</h3>"
//				+"function({status: 服务状态值（Status常量）}, { code：错误码, msg：错误描述})"
//				, getActiveUserAgentIds(), "PUSH API", offset);
//		addProposal(proposals, "start", JS_FUNCTION, "<h3>描述</h3>"
//				+"启动推送服务<br>"
//				+ "<h3>参数</h3>"
//				+ "无<br>"
//				+"<h3>回调函数</h3>"
//				+"function({status: 操作成功状态值}，{ code：错误码，msg：错误描述})"
//				, getActiveUserAgentIds(), "PUSH API", offset);
//		addProposal(proposals, "stop", JS_FUNCTION, "<h3>描述</h3>"
//				+"停止推送服务<br>"
//				+ "<h3>参数</h3>"
//				+ "无<br>"
//				+"<h3>回调函数</h3>"
//				+"function({status: 操作成功状态值}，{ code：错误码，msg：错误描述})"
//				, getActiveUserAgentIds(), "PUSH API", offset);
		addApicloudProposal(proposals, "setListener", showFunction(location, "setListener")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"注册监听推送消息<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"function({type：消息类型， data：消息内容}，{ code：错误码， msg：错误描述}"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "PUSH API", offset);
		addApicloudProposal(proposals, "removeListener", "removeListener();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"移除推送消息监听<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "PUSH API", offset);
		addApicloudProposal(proposals, "setPreference", showCode(location, "setPreference", false)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"推送偏好设置<br>"
				+ "<h3>参数</h3>"
				+ "notify：是否弹出消息通知（布尔类型） 默认值：true<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "PUSH API", offset);
		
		
	}
	private void createMSM(Set<ICompletionProposal> proposals, int offset, int length) {
		String location = getString(length);
		addApicloudProposal(proposals, "getAuthInfo", showFunction(location, "getAuthInfo")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"获取认证信息<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态,result：{msg:说明,authType:认证方式,<br>" +
				"authStatus:认证状态,fields:手动认证申请字段,{<br>" +
				"email:邮箱是否必填,name:姓名是否必填,group:分组是否必填,<br>" +
				"description:申请说明是否必填,photo:证件是否必填,groups:分组数据，数组类型<br>" +
				"[\"group1\",\"group2\"]}}},{ msg:错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "MSM API", offset);
		addApicloudProposal(proposals, "certApply", showCode(location, "certApply", true, "email:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"证书申请<br>"
				+ "<h3>参数</h3>"
				+ "email：邮箱（字符串型），不能为空 默认值：无<br>"
				+ "name：姓名（字符串型）默认值：无<br>"
				+ "group：分组编号（字符串型）默认值：无<br>"
				+ "description：申请说明（字符串型）默认值：无<br>"
				+ "photo：证件照（字符串型）默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status: 操作成功状态值},{ msg:错误描述}) "
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "MSM API", offset);
		addApicloudProposal(proposals, "certVerify", showCode(location, "certVerify", true, "authCode:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"认证码验证<br>"
				+ "<h3>参数</h3>"
				+ "authCode：认证码（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status: 操作成功状态值},{ msg:错误描述})"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "MSM API", offset);
		addApicloudProposal(proposals, "login", showCode(location, "login", true, "userName:'',", "password:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"登录<br>"
				+ "<h3>参数</h3>"
				+ "userName：用户名（字符串类型），不能为空 默认值：无<br>"
				+ "password：用户密码（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status: 操作成功状态值},{ msg:错误描述})"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "MSM API", offset);
	
	}
	
	private void createScanner(Set<ICompletionProposal> proposals, int offset, int length) {
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开二维码／条码扫描器<br>"
				+ "<h3>参数</h3>"
				+ "needBr：如果此值为true，则所扫二维码的信息字符串如果包含回车键(\n)则用<br>替换。false则直接将\n去掉。不传则默认为false(布尔类型)<br>"
				+ "sound：扫描结束后的提示音文件的路径，可为空(字符串类型) 默认值：无<br>"
				+ "save：所生成的图片保存位置，可为空（JSON类型） 默认值：见内部字段<br>"
				+"内部段：{album:布尔值，是否保存到系统相册，默认false，可为空 imgPath: 保存的文件路径,字符串类型，无默认值,可为空,空则不保存若路径不存在文件夹则创建此目录" 
				+"imgName:保存的图片名字，字符串类型，无默认值,可为空,空则不保存支持png和jpg格式，若不指定格式，则默认png"
				+"size:生成的图片(正方形)的边长，数字，默认200，可为空}<br>"
				+"<h3>回调函数</h3>"
				+"function({msg：返回扫描信息（扫码失败则返回失败信息）}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "Scanner API", offset);
		addApicloudProposal(proposals, "openView", showCode(location, "openView", true)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开自定义视图大小的二维码／条码扫描器<br>"
				+ "<h3>参数</h3>"
				+ "x：视图左上角点的坐标（数字类型）默认值：0<br>"
				+ "y：视图左上角点的坐标（数字类型）默认值：64<br>"
				+ "w：视图的宽（数字类型） 默认值：200<br>"
				+ "h：视图的高（数字类型）默认值：200<br>"
				+ "needBr：如果此值为true，则所扫二维码的信息字符串如果包含回车键(\n)则用<br>替换。false则直接将\n去掉。不传则默认为false(布尔类型)<br>"
				+ "sound：扫描结束后的提示音文件的路径，可为空(字符串类型) 默认值：无<br>"
				+ "fixedOn：将此模块视图添加到指定窗口的名字，可为空（字符串类型）默认值：当前主窗口的名字<br>"
				+ "fixed：是否将模块视图固定到网页内。android不支持此功能，可为空（布尔类型）默认值：false<br>"
				+ "save：所生成的图片保存位置，可为空（JSON类型） 默认值：见内部字段<br>"
				+"内部段：{album:布尔值，是否保存到系统相册，默认false，可为空 imgPath: 保存的文件路径,字符串类型，无默认值,可为空,空则不保存若路径不存在文件夹则创建此目录" 
				+"imgName:保存的图片名字，字符串类型，无默认值,可为空,空则不保存支持png和jpg格式，若不指定格式，则默认png"
				+"size:生成的图片(正方形)的边长，数字，默认200，可为空}<br>"
				+"<h3>回调函数</h3>"
				+"function({msg：返回扫描信息（扫码失败则返回失败信息）}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "Scanner API", offset);
		addApicloudProposal(proposals, "closeView", "closeView();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭自定义视图大小的二维码／条码扫描器<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "Scanner API", offset);
		addApicloudProposal(proposals, "decode", showCode(location, "decode", true)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"图片解码<br>"
				+ "<h3>参数</h3>"
				+ "needBr：如果此值为true，则所扫二维码的信息字符串如果包含回车键(\n)则用<br>替换。false则直接将\n去掉。不传则默认为false(布尔类型)<br>"
				+ "sound：扫描结束后的提示音文件的路径，可为空(字符串类型) 默认值：无<br>"
				+ "imgPath：要解码的图片，可为空，若为空则去系统相册选取图片(字符串类型) 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({msg:返回扫描信息（扫码失败则返回失败信息）}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "Scanner API", offset);
		addApicloudProposal(proposals, "encode", showCode(location, "encode", true)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"将字符串生成条码／二维码<br>"
				+ "<h3>参数</h3>"
				+ "type：生成图片的类型，取值范围见生成图片类型，可为空(字符串类型) 默认值：qr_image<br>"
				+ "string：所要生成的条码/二维码的字符串，可为空(字符串类型) 默认值：\"test\"<br>"
				+ "save：所生成的图片保存位置，可为空（JSON类型） 默认值：见内部字段<br>"
				+"内部段：{album:布尔值，是否保存到系统相册，默认false，可为空 imgPath: 保存的文件路径,字符串类型，无默认值,可为空,空则不保存若路径不存在文件夹则创建此目录" 
				+"imgName:保存的图片名字，字符串类型，无默认值,可为空,空则不保存支持png和jpg格式，若不指定格式，则默认png"
				+"size:生成的图片(正方形)的边长，数字，默认200，可为空}<br>"
				+"<h3>回调函数</h3>"
				+"function({status: 是否生成成功}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "Scanner API", offset);
	}
	
	private void createImageBrows(Set<ICompletionProposal> proposals, int offset, int length) {
		String location = getString(length);
		addApicloudProposal(proposals, "openImages", showCode(location, "openImages", false, "imageUrls:[],", "showList:true")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开图片浏览器<br>"
				+ "<h3>参数</h3>"
				+ "imageUrls：图片的url（支持res路径和http路径，如：res://resImageTEST.png<br>"
				+ "http://ww3.sinaimg.cn/bmiddle/7394feabjw1ef2p5xg10vg208f05yx6p.gif）组成的数组 不能为空 默认值：无<br>"
				+ "showList：是否以九宫格方式显示图片（布尔类型）不能为空 默认值：true<br>"
				+ "activeIndex：当不用九宫格方式显示时，当前要显示的图片在集合中的索引（数字类型）默认值：0<br>"
				+"<h3>回调函数</h3>"
				+"无<br>"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "imageBrows API", offset);
		
	}
	private void createQQ(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "login", showFunction(location, "login")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"登陆qq<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"function({ {statur:操作成功状态值,accessToken:token,openId:openId}，err:{msg:错误描述}}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "qq API", offset);
		addApicloudProposal(proposals, "logout", showFunction(location, "logout")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"登出qq<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"function({ {statur:操作成功状态值}，err:{msg:错误描述}}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "qq API", offset);
		addApicloudProposal(proposals, "shareText", showCode(location, "shareText", true, "text:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"分享纯文本到空间（android暂不支持该接口）<br>"
				+ "<h3>参数</h3>"
				+ "text：要分享的文本（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({ {statur:操作成功状态值}，err:{msg:错误描述}}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "qq API", offset);
		addApicloudProposal(proposals, "shareImage", showCode(location, "shareImage", true, "title:'',", "description:'',", "imgPath:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"分享图片（本地）到空间<br>"
				+ "<h3>参数</h3>"
				+ "title：要分享的图片标题（字符串类型），不能为空 默认值：无<br>"
				+ "description：要分享的图片描述（字符串类型），不能为空 默认值：无<br>"
				+ "imgPath：要分享的图片路径（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({ {statur:操作成功状态值}，err:{msg:错误描述}}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "qq API", offset);
		addApicloudProposal(proposals, "shareNews", showCode(location, "shareNews", true, "url:'',", "title:'',", "description:'',", "imgUrl:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"分享新闻到空间<br>"
				+ "<h3>参数</h3>"
				+ "url：要分享的新闻链接地址（字符串类型），不能为空 默认值：无<br>"
				+ "title：要分享的新闻标题（字符串类型），不能为空 默认值：无<br>"
				+ "description：要分享的新闻描述（字符串类型），不能为空 默认值：无<br>"
				+ "imgUrl：要分享的新闻缩略图的url（网络资源图片,字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({ {statur:操作成功状态值}，err:{msg:错误描述}}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "qq API", offset);
		addApicloudProposal(proposals, "shareMusic", showCode(location, "shareMusic", true, "url:'',", "title:'',", "description:'',", "imgUrl:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"分享音乐到空间<br>"
				+ "<h3>参数</h3>"
				+ "url：要分享的音乐链接地址（字符串类型），不能为空 默认值：无<br>"
				+ "title：要分享的音乐标题（字符串类型），不能为空 默认值：无<br>"
				+ "description：要分享的音乐描述（字符串类型），不能为空 默认值：无<br>"
				+ "imgUrl：要分享的音乐缩略图的url（网络资源图片,字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({ {statur:操作成功状态值}，err:{msg:错误描述}}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "qq API", offset);
		addApicloudProposal(proposals, "shareVideo", showCode(location, "shareVideo", true, "url:'',", "title:'',", "description:'',", "imgUrl:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"分享视频到空间<br>"
				+ "<h3>参数</h3>"
				+ "url：要分享的视频链接地址（字符串类型），不能为空 默认值：无<br>"
				+ "title：要分享的视频标题（字符串类型），不能为空 默认值：无<br>"
				+ "description：要分享的视频描述（字符串类型），不能为空 默认值：无<br>"
				+ "imgUrl：要分享的视频缩略图的url（网络资源图片,字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({ {statur:操作成功状态值}，err:{msg:错误描述}}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "qq API", offset);
		
	}
	

	private void createFileBrowser(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showFunction(location, "open")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开文件浏览器<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "fileBrowser API", offset);
	}
	
	private void createCityListProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true, "x:,", "y:,", "width:,"
				, "height:,", "currentCity:'',", "resource:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开城市列表<br>"
				+ "<h3>参数</h3>"
				+ "x：城市列表视图左上角点坐标（数字类型），不可为空 默认值：无<br>"
				+ "y：城市列表视图左上角点坐标（数字类型），不可为空 默认值：无<br>"
				+ "width：视图的宽（数字类型），不能为空 默认值：无<br>"
				+ "height：视图的高（数字类型），不能为空 默认值：无<br>"
				+ "currentCity：用户当前所在城市（字符串类型），不能为空 默认值：无<br>"
				+ "resource：城市列表的数据源文件路径（字符串类型），不能为空 默认值：无<br>"
				+ "fixedOn：将此模块视图添加到指定窗口的名字，可为空（字符串类型）默认值：当前主窗口的名字<br>"
				+"<h3>回调函数</h3>"
				+"function({ cityInfo:返回用户选择的城市信息}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "cityList API", offset);
		addApicloudProposal(proposals, "close", "close();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭城市列表<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "cityList API", offset);
		
	}
	private void createBarChartProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", false, "x:,", "y:,", "width:,"
				, "height:,", "yAxisMax:,", "yAxisStep:,", "xAxisMarks:[],", "datas:[]")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开柱状图视图<br>"
				+ "<h3>参数</h3>"
				+ "x：视图左上角点的x坐标（数字类型）不可为空 默认值：无<br>"
				+ "y：视图左上角点的y坐标（数字类型）不可为空  默认值：无<br>"
				+ "width：视图宽（数字类型）不可为空 默认值：无<br>"
				+ "height：视图高（数字类型）不可为空 默认值：无<br>"
				+ "yAxisMax ：坐标系y轴配置参数，y轴上的最大值（数字类型）不可为空 默认值：无<br>"
				+ "yAxisStep：坐标系y轴配置参数，Y轴上数据的步幅（数字类型）不可为空 默认值：无<br>"
				+ "xAxisMarks：轴上各个标注所组成的数组，数组元素类型为字符串（数组对象）不可为空 默认值：无<br>"
				+ "datas：各个柱图的数据大小组成的数组，数组元素类型为数字（数组对象）不可为空 默认值：无<br>"
				+ "fixedOn：将此模块视图添加到指定窗口的名字，可为空（字符串类型）默认值：当前主窗口的名字<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "barChart API", offset);
		addApicloudProposal(proposals, "close", showCode(location, "close", false, "id:")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭柱状图<br>"
				+ "<h3>参数</h3>"
				+ "id：视图左上角点坐标（数字类型）不可为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "barChart API", offset);
		
	}
	private void createCoverFlowProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true, "x:,", "y:,", "width:,"
				, "height:,", "paths:[]")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开coverFlow<br>"
				+ "<h3>参数</h3>"
				+ "x：视图左上角点的x坐标（数字类型）不可为空 默认值：无<br>"
				+ "y：视图左上角点的y坐标（数字类型） 不可为空 默认值：无<br>"
				+ "width：视图宽（数字类型）不可为空 默认值：无<br>"
				+ "height：视图高（数字类型）不可为空 默认值：无<br>"
				+ "backGroundColor：K线图背景颜色，十六进制值字符串（字符串类型） 默认值：背景颜色十六进制值<br>"
				+ "paths：图片路径组成的数组（数组类型）不可为空 默认值：无<br>"
				+ "fixedOn：将此模块视图添加到指定窗口的名字，可为空（字符串类型）默认值：当前主窗口的名字<br>"
				+"<h3>回调函数</h3>"
				+"function({ index: 返回用户选择的图片的下标}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "CoverFlow API", offset);
		addApicloudProposal(proposals, "close", "close();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭coverFlow<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "CoverFlow API", offset);
		
	}
	
	private void createScannerViewProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open",showFunction(location, "open")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开二维码／条码扫描器<br>"
				+ "<h3>参数</h3>"
				+ "needBr：如果此值为true，则所扫二维码的信息字符串如果包含回车键(\n)则用<br>替换。false则直接将\n去掉。不传则默认为false，可为空（布尔类型）默认值：false<br>"
				+ "fixedOn：将视图添加到指定的视图的名字（字符串类型）默认值：空<br>"
				+ "fixed：是否将模块视图固定在目标视图滚动框（布尔类型） 默认值：false<br>"
				+"<h3>回调函数</h3>"
				+"function({msg:返回扫描信息（扫码失败则返回失败信息）}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "scannerView API", offset);
		addApicloudProposal(proposals, "openView",showFunction(location, "openView")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开自定义视图大小的二维码／条码扫描器<br>"
				+ "<h3>参数</h3>"
				+ "x：视图左上角点的坐标（数字类型） 默认值：0<br>"
				+ "y：视图左上角点的坐标（数字类型） 默认值：64<br>"
				+ "width：视图的宽（数字类型） 默认值：200<br>"
				+ "height：视图的高（数字类型） 默认值：200<br>"
				+ "sound：扫描结束后的提示音文件的路径（字符串类型）默认值：无<br>"
				+ "fixedOn：要把扫描视图添加到某视图的名字，可为空，为空时默认获取当前的（字符串类型）默认值：nil<br>"
				+ "fixed：要把扫描视图嵌入到web页面里（布尔类型） 默认值：false<br>"
				+"<h3>回调函数</h3>"
				+"function({msg:返回扫描信息（扫码失败则返回失败信息）}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "scannerView API", offset);
		addApicloudProposal(proposals, "closeView","closeView();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭自定义视图大小的二维码／条码扫描器<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "scannerView API", offset);
		
	}
	private void createTabBarProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true, "bgImg:'',", "selectImg:'',", "items:[]")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开tabBar<br>"
				+ "<h3>参数</h3>"
				+ "bgImg：tabBar的背景图片路径，不能为空（字符串类型） 默认值：无<br>"
				+ "selectImg：选中按钮后的按钮效果图片路径，不能为空（字符串类型） 默认值：无<br>"
				+ "perScreenBtn：每屏显示按钮的个数（数字类型） 默认值：6<br>"
				+ "items：多个按钮的信息组成的数组，不能为空（数组类型） 默认值：无<br>"
				+"内部字段：[{ img:按钮图片路径，字符串，不可为空 highlight:选中按钮的图，字符串，可为空<br>"
				+"selected:选中后的背景图，可为空，为空时显示selectImg title:按钮名字，字符串，不可为空<br>"
				+"color:按钮标题颜色，可为空，默认白色badge:按钮左上角的badge，字符串类型，可为空，为空时不显示badge}]<br>"
				+"<h3>回调函数</h3>"
				+"function({ index:  点击某个按钮返回其下标}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "tabBar API", offset);
		addApicloudProposal(proposals, "setBadge", showCode(location, "setBadge", true, "index:")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置按钮左上角的badge标注<br>"
				+ "<h3>参数</h3>"
				+ "index：要设置的按钮的下标，不可为空（数字类型） 默认值：无<br>"
				+ "badge：要设置的标注（字符串类型） 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "tabBar API", offset);
		addApicloudProposal(proposals, "hidden", "hidden();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"隐藏tabBar<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "tabBar API", offset);
		addApicloudProposal(proposals, "show", "show();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"显示隐藏的tabBar<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "tabBar API", offset);
		addApicloudProposal(proposals, "close", "close();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭tabBar<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "tabBar API", offset);
		
	}
	
	private void createSliderProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开slider<br>"
				+ "<h3>参数</h3>"
				+ "x：slider左边点的坐标，可为空（数字类型）默认值：30<br>"
				+ "y：slider左边点的坐标，可为空（数字类型） 默认值：104<br>"
				+ "w：slider的宽，可为空（数字类型）默认值：260<br>"
				+ "h：slider的高，可为空（数字类型）默认值：5<br>"
				+ "bgimg：slider的背景图片，可为空（字符串类型） 默认值：无<br>"
				+ "selectedBgImg：slider滑块左边划过的区域图片，可为空（字符串类型） 默认值：无<br>"
				+ "bar:气泡设置，可为空,若为空则不显示滑块（JSON类型） 默认值：见内部字段<br>"
				+"内部段：{barWidth:滑块宽，数字，可为空，默认30  barHeight:滑块的高，数字，可为空，默认18 barImg:滑块背景，字符串，可为空，默认#4f94dc，支持rgb，rgba，#，img}<br>"
				+ "bubble:气泡设置，可为空（JSON类型） 默认值：无<br>"
				+"内部段：{ bubbleWidth:数字类型，默认60，气泡的宽，可为空  bubbleHeight:数字类型，默认40，气泡的高，可为空" +
				" bubbleImg:字符串，默认#5cacee，气泡背景，可为空，支持rgb,rgba,#,img  titleSuffix:字符串，默认℃，气泡后缀，可为空   titleColor： //字符串，默认#000000，可为空，支持rgb，rgba，#}<br>"
				+ "minValue：slider最小值，可为空（数字类型）默认值：无<br>"
				+ "maxValue：slider最大值，可为空（数字类型）默认值：无<br>"
				+ "defValue：slider开启默认值，可为空（数字类型）默认值：无<br>"
				+ "fixedOn：把模块视图添加到指定窗口的名字，可为空（字符串类型） 默认值：当前主窗口的名字<br>"
				+"<h3>回调函数</h3>"
				+"function({touchCancel:是否是滑动结束事件 value:滑动时返回滑动的值}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "slider API", offset);
		addApicloudProposal(proposals, "setValue", showCode(location, "setValue", true,  "value:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置slider的值<br>"
				+ "<h3>参数</h3>"
				+ "value：要设置的滑块的值（在最大值和最小值直接的一个值），不可为空 （数字类型） 默认值：无<br>"
				+ "minValue：slider最小值，可为空（数字类型）默认值：无<br>"
				+ "maxValue：slider最大值，可为空（数字类型）默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "slider API", offset);
		addApicloudProposal(proposals, "lock", "lock();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"锁定slider的值<br>"
				+ "<h3>参数</h3>"
				+ "lock：是否锁定当前slider的值，可为空（布尔类型） 默认值：true<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "slider API", offset);
		addApicloudProposal(proposals, "close", "close();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭slider<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "slider API", offset);
		addApicloudProposal(proposals, "show", "show();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"显示滑动器<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "slider API", offset);
		addApicloudProposal(proposals, "hide", "hide();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"隐藏滑动器<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "slider API", offset);
		
	}
	private void createActionButtonProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true, "items:[]")		
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开弹动按钮菜单<br>"
				+ "<h3>参数</h3>"
				+ "items：弹出的子菜单按钮的信息，该数组有多少个元素，则有多少个菜单按钮。不可为空<br>"
				+"（数组类型） 默认值：无内部字段：[{ bgColor: 背景色值   image：图片路  title：标题}]<br>"
				+ "pageControl：配置页面控制器的显示，可为空，若为空，则不显示页面控制器（JSON类型） 默认值：无<br>"
				+"内部字段：[{activeColor:当前页颜色值，可为空 ，默认为红色inactiveColor:非当前页颜色值，可为空，默认为灰色}]<br>"
				+ "topHeight：上边一排按钮距离屏幕顶部的高度（数字类型） 默认值：屏幕的一半<br>"
				+ "fixedOn：将此模块视图添加到指定窗口的名字，可为空（字符串类型）默认值：当前主窗口的名字<br>"
				+"<h3>回调函数</h3>"
				+"function({ index:  点击子菜单返回其下标}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "actionButton API", offset);
		addApicloudProposal(proposals, "close", "close();"	
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭菜单<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "actionButton API", offset);
		
	}
	private void createPanoramaProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开全景展示视图<br>"
				+ "<h3>参数</h3>"
				+ "type：打开全景展示的类型，0为圆球展示方式，1为立方体展示方式，<br>"
				+"当为1时需要传六张图片（数字类型）默认值：0<br>"
				+ "x：视图左上角坐标点，可为空（数字类型）默认值：0<br>"
				+ "y：视图左上角坐标点，可为空（数字类型） 默认值：0<br>"
				+ "width：视图的宽，可为空（数字类型）默认值：屏幕宽度<br>"
				+ "height：视图的高，可为空（数字类型）默认值：屏幕的高的一半<br>"
				+ "imgPath：要展示的360度全景图片的路径，当type为0时传此接口（字符串类型） 默认值：无<br>"
				+ "imgPaths：要展示的六张立方体全景图片的路径组成的数组，当type为1时传此接口（数组类型）默认值：无<br>"
				+ "fixedOn：将此模块视图添加到指定窗口的名字，可为空（字符串类型）默认值：当前主窗口的名字<br>"
				+"<h3>回调函数</h3>"
				+"function({ status:操作成功状态值id:视图的id，据此id关闭该视图},{msg:错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "panorama API", offset);
		addApicloudProposal(proposals, "close", showCode(location, "close", false, "id:")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭全景展示视图<br>"
				+ "<h3>参数</h3>"
				+ "id：要关闭的视图的id，不能为空（数字类型）默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "panorama API", offset);
		
	}
	private void createArcMenuProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", false, buildArray(location, false, "items", "\n\t\t", "\n\t"
				, "w:40,", "h:40,", "img:'',", "imgLight:''"), "startAngle:,", "radius:")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开弹动菜单<br>"
				+ "<h3>参数</h3>"
				+ "type：弹出的子菜单的类型。0为弧形菜单，1为直线型菜单（数字类型） 默认值：0<br>"
				+ "mainMenu：主菜单的位置大小和背景图（JSON类型）默认值：无<br>"
				+ "内部字段：{x:0,起点x坐标，数字，不能为空 y:0, 起点y坐标，数字，不能为空<br>"
				+"w:50,宽度，数字，不能为空 h:50,高度，数字，不能为空<br>"
				+" img:''背景图片路径，字符串，不能为空 imgLight:''高亮状态下背景图片路径，字符串，不能为空}<br>"
				+ "items：子菜单集合（数组对象）不能为空 默认值：无<br>"
				+ "内部字段：[{ w:40,    宽度，数字，不能为空    h:40,   高度，数字 ，不能为空<br>"
				+"img:'' 背景图片路径，字符串，不能为空 imgLight:''高亮状态下背景图片路径，字符串，不能为空}]<br>"
				+ "startAngle：起点角度（12点钟方向为0度，顺时针方向计数，数字类型）不能为空 默认值：无<br>"
				+ "wholeAngle：弧形菜单的角度大小，当type为0时不能为空，当type为1时此参数（数字类型） 默认值：无<br>"
				+ "radius：弧形子菜单距离主菜单的半径，若是直线型子菜单，则为最远的子菜单距离主菜单的距离（数字类型）不能为空 默认值：无<br>"
				+ "fixedOn：将此模块视图添加到指定窗口的名字，可为空（字符串类型）默认值：当前主窗口的名字<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "arcMenu API", offset);
		addApicloudProposal(proposals, "close", showCode(location, "close", true, "id:")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭菜单<br>"
				+ "<h3>参数</h3>"
				+ "id：要关闭的菜单的id，为空则关闭所有（数字类型） 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({id: 打开后返回idindex: 点击子菜单返回其下标}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "arcMenu API", offset);
		
	}	
	private void createArcProgressProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开圆形进度条<br>"
				+ "<h3>参数</h3>"
				+ "type：进度视图类型，0-环、1-扇形、2-类月牙形（数字类型） 默认值：0<br>"
				+ "centerX：视图中心点坐标（数字类型） 默认值：100<br>"
				+ "centerY：视图中心点坐标（数字类型） 默认值：100<br>"
				+ "radius：视图圆半径（数字类型） 默认值：60<br>"
				+ "bgColor：进度背景色（字符串类型） 默认值：#C0C0C0<br>"
				+ "pgColor：进度色（字符串类型） 默认值：#2e8b57<br>"
				+ "loopWidth：当类型为环形进度条时，此参数表上环的宽度。若为其它类型，则此参数无用可为空（数字类型） 默认值：3<br>"
				+ "viewName：要把该视图添加到某视图的名字，可为空（字符串类型） 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({id: 打开圆形进度条的id}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "arcProgress API", offset);
		addApicloudProposal(proposals, "close", showCode(location, "close", true, "id:")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭圆形进度视图<br>"
				+ "<h3>参数</h3>"
				+ "id：要关闭的视图的id，若为空，则关闭全部（数字类型） 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "arcProgress API", offset);
		addApicloudProposal(proposals, "setValue", showCode(location, "setValue", true, "id:0")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置进度值<br>"
				+ "<h3>参数</h3>"
				+ "id：要设置视图的id（数字类型）不可为空 默认值：0<br>"
				+ "value：设置的值（数字类型） 默认值：0<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "arcProgress API", offset);
		
	}
	private void createTransProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "parse", showCode(location, "parse", true)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"将xml文件或数据解析成JSON对象<br>"
				+ "<h3>参数</h3>"
				+ "path：xml文件路径（字符串类型） 默认值：无<br>"
				+ "data：xml数据（字符串类型） 默认值：无<br>"
				+ "customKey：所解析的xml值无对应的key时，需要填充一个自定义key，可为空（字符串类型） 默认值：plainText<br>"
				+"<h3>回调函数</h3>"
				+"function(ret,err){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，Android系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "trans API", offset);
		
	}
	private void createPeriodSelectorProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开时段选择器<br>"
				+ "<h3>参数</h3>"
				+ "x：选择器视图左上角点坐标（数字类型） 默认值：0<br>"
				+ "y：选择器视图左上角点坐标（数字类型） 默认值：64<br>"
				+ "width：选择器视图宽（数字类型） 默认值：当前设备屏幕的宽度<br>"
				+ "height：选择器视图高（数字类型） 默认值：宽度减70px<br>"
				+ "viewName：要把该视图添加到某视图的名字（字符串类型） 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({sHour:选择的起始小时    sMinit:选择的起始分钟<br>"
				+"   eHour: 选择的结束小时eMinit: 选择的结束分钟},err){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "PeriodSelector API", offset);
		addApicloudProposal(proposals, "close", "close();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开时段选择器<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "PeriodSelector API", offset);
		
	}
	private void createBluetoothProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "connect", showFunction(location, "connect")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开蓝牙连接<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"function({progress:发送接收数据时的进度message收到的数据信息，如果是字符串则直接显示，如果是文件则返回其路径},err){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，Android系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "bluetooth API", offset);
		addApicloudProposal(proposals, "send", showCode(location, "send", false, "type:")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"发送数据<br>"
				+ "<h3>参数</h3>"
				+ "type：要发送的数据类型：0—字符串；1---文件；2----本地相册里的图片；3-----本地视频库里的视频（数字类型）不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，Android系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "bluetooth API", offset);
		addApicloudProposal(proposals, "cancel", "cancel();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"取消蓝牙连接<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，Android系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "bluetooth API", offset);
		
	}
	private void createTimeSelectorProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开时间选择器<br>"
				+ "<h3>参数</h3>"
				+ "x：选择器视图左上角点坐标（数字类型） 默认值：0<br>"
				+ "y：选择器视图左上角点坐标（数字类型） 默认值：64<br>"
				+ "width：选择器视图宽（数字类型） 默认值：当前设备屏幕的宽度<br>"
				+ "height：选择器视图高（数字类型） 默认值：宽度减70px<br>"
				+ "viewName：要把该视图添加到某视图的名字（字符串类型） 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({hour:选择的小时minit:选择的分钟},err){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "timeSelector API", offset);
		addApicloudProposal(proposals, "close", "close();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭时间选择器<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "timeSelector API", offset);
		
	} 
	private void createNavigationMenuProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true, buildArray(location, true, "btnInfo",  "\n\t\t", "\n\t", "normal:''"))
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开导航菜单<br>"
				+ "<h3>参数</h3>"
				+ "color：按钮文字颜色（字符串类型） 默认值：#FFFFFF<br>"
				+ "highlight：按钮文字选中后的颜色（字符串类型） 默认值：#d36bff<br>"
				+ "btnInfo：菜单里按钮的参数配置（数组类型） ，不可为空 默认值：无<br>"
				+"内部字段：[{  normal:按钮背景图片路径，字符串，不可为空  highlight:  按钮点击时背景图片路径，字符串<br>"
				+"selected:按钮选中后背景图片路径，字符串，可为空 title:按钮的标题文字，字符串，可为空}]<br>"
				+ "fixedOn：将此模块视图添加到指定窗口的名字，可为空（字符串类型）默认值：当前主窗口的名字<br>"
				+"<h3>回调函数</h3>"
				+"function({index：用户点击按钮的下标}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "navigationMenu API", offset);
		addApicloudProposal(proposals, "hidden", "hidden();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"隐藏菜单<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "navigationMenu API", offset);
		addApicloudProposal(proposals, "show", "show();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"显示菜单<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "navigationMenu API", offset);
		addApicloudProposal(proposals, "close", "close();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭菜单<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "navigationMenu API", offset);
		
	}
	private void createPullMenuProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true, buildArray(location, true, "btnArray",  "\n\t\t", "\n\t", "normal:'',", "highlight:''"))
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开拖拉菜单<br>"
				+ "<h3>参数</h3>"
				+ "type：菜单类型，0代表从上往下弹出，1代表从下往上弹出（数字类型） 默认值：0<br>"
				+ "btnWidth：菜单里每个按钮的（正方形）边长（数字类型） 默认值：60<br>"
				+ "alph：菜单背景透明度，取值范围为0-1（数字类型） 默认值：0.8<br>"
				+ "bgColor：菜单背景色（字符串类型） 默认值：#FFFFFF<br>"
				+ "btnArray：菜单里每个按钮的背景图片（本地路径），选中图片（本地路径）（数组类型） ，不可为空 默认值：无<br>"
				+"内部字段：[{ normal: 字符串，支撑本地协议，不可为空 highlight:字符串，支撑本地协议，不可为空}]<br>"
				+ "fixedOn：将此模块视图添加到指定窗口的名字，可为空（字符串类型）默认值：当前主窗口的名字<br>"
				+"<h3>回调函数</h3>"
				+"function({index：用户点击按钮的下标}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "pullMenu API", offset);
		addApicloudProposal(proposals, "hidden", "hidden();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"隐藏菜单<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "pullMenu API", offset);
		addApicloudProposal(proposals, "show", "show();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"显示菜单<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "pullMenu API", offset);
		addApicloudProposal(proposals, "close", "close();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭菜单<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "pullMenu API", offset);
		
	}
	private void createScrollPictureProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true, showParam(false, "paths", "[]")
				, showParam(false, "placeHoldImg", "''"))
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开图片滚动界面<br>"
				+ "<h3>参数</h3>"
				+ "x：视图左上角点坐标（数字类型） 默认值：0<br>"
				+ "y：图左上角点坐标（数字类型） 默认值：导航条下边缘位置<br>"
				+ "width：视图的宽（数字类型） 默认值：当前设备屏幕宽<br>"
				+ "height：视图的高（数字类型） 默认值：视图的宽减120<br>"
				+ "intervalTime：图片变换间隔，单位是s（数字类型） 默认值：3<br>"
				+ "paths：图片路径组成的数组，支持http，https，widget，fs各种协议（数组类型）不可为空 默认值：无<br>"
				+ "placeHoldImg：当加载网络图片时，屏幕上显示的占位图片，不可为空，paths都是本地路径，该参数可为空（字符串类型） 默认值：无<br>"
				+ "subtitle：图片的说明文字，可为空，若为空则不显示说明文字（JSON对象） 默认值：无<br>"
				+"内部字段：{  height：数字类型，可为空，说明文字的视图的高 titles： 数组类型，不可为空，说明的文字组成的数组<br>"
				+"color：字符串类型，可为空，说明文字的颜色 size：数字类型，可为空，说明文字的字体大小  bgColor：字符串类型，可为空，说明文字视图的背景颜色}<br>"
				+ "control：页面控制器，可为空，若为空则不显示（JSON对象） 默认值：无<br>"
				+"内部字段：{ position：页面控制器位置，0-中间；1-左边；2-右边，默认0<br>"
				+"activeColor: 当前圆点颜色 默认:DA70D6,可为空 inactiveClor:圆点颜色 默认：#FFFFFF，可为空}<br>"
				+"viewName：要添加到某个视图上，可为空，若为空则默认当前视图（字符串类型） 默认值：当前视图的名字<br>"
				+"<h3>回调函数</h3>"
				+"function({index：用户点击按钮的下标}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "scrollPicture API", offset);
		addApicloudProposal(proposals, "close", "close();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭页面<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "scrollPicture API", offset);
		
	}
	private void createSearchBarProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开图片滚动界面<br>"
				+ "<h3>参数</h3>"
				+ "placeholder：搜索提示文本（字符串类型） 默认值：请输入搜索关键字<br>"
				+ "bgImg：搜索输入框背景图片（字符串类型） 默认值：var。。。/searchBar_bg.png<br>"
				+ "cancelColor：取消按钮的颜色（字符串类型） 默认值：#D2691E<br>"
				+ "cancelSize：取消按钮大小（数字类型） 默认值：16<br>"
				+ "textColor：搜索输入文本的字体颜色（字符串类型） 默认值：#000000<br>"
				+ "textFielWidth：搜索输入框宽度（数字类型） 默认值：250<br>"
				+ "textFieldHeight：搜索输入框的高度（数字类型） 默认值：44<br>"
				+ "recordCount：搜索历史记录条数（数字类型） 默认值：10<br>"
				+ "barBgColor：导航条背景色（字符串类型） 默认值：#FFFFFF<br>"
				+ "listBgColor：历史记录表背景色（字符串类型） 默认值：#FFFFFF<br>"
				+ "listColor：搜索历史记录文本字体颜色（字符串类型） 默认值：#696969<br>"
				+ "listSize：搜索历史记录字体大小（数字类型） 默认值：16<br>"
				+ "cleanColor：清除历史记录字体颜色（字符串类型） 默认值：#000000<br>"
				+ "cleanSize：清除历史记录字体大小（数字类型） 默认值：16<br>"
				+ "animation：打开页面时是否有动画（布尔类型） 默认值：true<br>"
				+"<h3>回调函数</h3>"
				+"function({index：用户点击按钮的下标}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "searchBar API", offset);
		addApicloudProposal(proposals, "setText", showCode(location, "setText", false)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置搜索页面搜索框的文字<br>"
				+ "<h3>参数</h3>"
				+ "text：文本（字符串类型） 默认值：无<br>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "searchBar API", offset);
		addApicloudProposal(proposals, "close", "close();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭页面<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "searchBar API", offset);
		
	}
	private void createBbubbleMenuProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true, "btnArray:[]")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开菜单<br>"
				+ "<h3>参数</h3>"
				+ "bgColor：菜单背景色（字符串类型） 默认值：#000000<br>"
				+ "lightColor：菜单按钮点击色（字符串类型） 默认值：#009ACD<br>"
				+ "borderColor：菜单边框色（字符串类型） 默认值：#000000<br>"
				+ "cutLineColor：按钮间分割线颜色（字符串类型） 默认值：#636363<br>"
				+ "centerX：气泡菜单箭头点的坐标（数字类型） 默认值：120<br>"
				+ "centerY：气泡菜单箭头点的坐标（数字类型） 默认值：120<br>"
				+ "btnArray：按钮的信息组成的数组，不可为空（数组类型）<br>"
				+ " 默认值：无，内部字段：[{icon:按钮的图片地址，字符串，可与标题配合使用，可为空<br>"
				+"title:按钮的标题，字符串，与图片配合使用 length:按钮的长度，数字类型，默认65}]<br>"
				+ "fixedOn：将此模块视图添加到指定窗口的名字，可为空（字符串类型）默认值：当前主窗口的名字<br>"
				+"<h3>回调函数</h3>"
				+"function({index：用户点击按钮的下标}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "bubbleMenu API", offset);
		addApicloudProposal(proposals, "hidden", "hidden();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"隐藏菜单<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "bubbleMenu API", offset);
		addApicloudProposal(proposals, "show", "show();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"显示菜单<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "bubbleMenu API", offset);
		addApicloudProposal(proposals, "close", "close();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭菜单<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "bubbleMenu API", offset);
		
	}
	private void createLoadingLabelProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		addApicloudProposal(proposals, "open", "open();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开加载标签<br>"
				+ "<h3>参数</h3>"
				+ "bgColor：菜单背景色（字符串类型） 默认值：#474747<br>"
				+ "lightColor：菜单按钮点击色（字符串类型） 默认值：#7A8B8B<br>"
				+ "centerX：加载标签的中心点坐标（数字类型） 默认值：当前设备屏幕的中间<br>"
				+ "centerY：加载标签的中心点坐标（数字类型） 默认值：导航条的最下边<br>"
				+ "fixedOn：将此模块视图添加到指定窗口的名字，可为空（字符串类型）默认值：当前主窗口的名字<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "loadingLabel API", offset);
		addApicloudProposal(proposals, "stop", "stop();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"停止加载<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "loadingLabel API", offset);
		addApicloudProposal(proposals, "start", "start();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"开始加载<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "loadingLabel API", offset);
		addApicloudProposal(proposals, "close", "close();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭加载标签<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "loadingLabel API", offset);
		
	}
	
	private void createCitySelectorProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开城市选择器<br>"
				+ "<h3>参数</h3>"
				+ "y：选择器视图上边缘距离当前设备屏幕顶部距离（数字类型） 默认值：当前设备屏幕下边缘减244<br>"
				+ "height：选择器视图的高（数字类型） 默认值：244<br>"
				+ "cancelImg：取消按钮的背景图片的本地路径（字符串类型） 默认值：默认图标<br>"
				+ "enterImg：确定按钮的背景图片的本地路径（字符串类型） 默认值：默认图标<br>"
				+ "titleImg：选择器顶端导航条背景图片的本地路径（字符串类型） 默认值：默认图标<br>"
				+ "bgImg：选择器背景图片的本地路径（字符串类型） 默认值：默认图标<br>"
				+ "fontColor：选择器字体颜色（字符串类型） 默认值：#000000<br>"
				+ "selectedColor：选中字体颜色（字符串类型） 默认值：#8B0000<br>"
				+ "animation：是否添加弹出动画（布尔类型） 默认值：false<br>"
				+ "fixedOn：将此模块视图添加到指定窗口的名字，可为空（字符串类型）默认值：当前主窗口的名字<br>"
				+"<h3>回调函数</h3>"
				+"function({ province:选中的省city:选中的市county:选中的县}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "citySelector API", offset);
		addApicloudProposal(proposals, "hidden", "hidden();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"隐藏选择器<br>"
				+ "<h3>参数</h3>"
				+ "animation：是否添加动画（布尔类型） 默认值：false<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "citySelector API", offset);
		addApicloudProposal(proposals, "show", "show();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"显示选择器<br>"
				+ "<h3>参数</h3>"
				+ "animation：是否添加动画（布尔类型） 默认值：false<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "citySelector API", offset);
		addApicloudProposal(proposals, "close", "close();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭选择器<br>"
				+ "<h3>参数</h3>"
				+ "animation：是否添加动画（布尔类型） 默认值：false<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "citySelector API", offset);
		
	}
	private void createSideMenuProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true, "btnArray:[]")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开菜单<br>"
				+ "<h3>参数</h3>"
				+ "type：菜单类型，0代表从左往右弹出，非0代表从右往左弹出（数字类型） 默认值：0<br>"
				+ "startPosition：菜单最上边的第一个按钮的起始位置（数字类型） 默认值：紧贴导航条下边缘<br>"
				+ "btnHeight：菜单里单个按钮的高度（数字类型） 默认值：60<br>"
				+ "interval：菜单里两个相邻按钮间的距离（数字类型） 默认值：10<br>"
				+ "trajectoryColor：弹出的按钮的弹道的颜色，可为空，若为空则不显示弹道（字符串类型） 默认值：无<br>"
				+ "btnArray：弹出的按钮的信息，不可为空（数组类型） 默认值：无<br>"
				+"内部字段：[{ icon:字符串，按钮的图标，支持本地协议，不可为空<br>"
				+"bgImg  :  字符串，按钮背景图标，支持本地协议，不可为空}]<br>"	
				+"<h3>回调函数</h3>"
				+"function({ id:菜单的id   index:s用户点击按钮的下标}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "sideMenu API", offset);
		addApicloudProposal(proposals, "hidden", showCode(location, "hidden", false, "id:")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"隐藏菜单<br>"
				+ "<h3>参数</h3>"
				+ "id：要操作的菜单的id，不可为空（数字类型） 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "sideMenu API", offset);
		addApicloudProposal(proposals, "show", showCode(location, "show", false, "id:")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"显示菜单<br>"
				+ "<h3>参数</h3>"
				+ "id：要操作的菜单的id，不可为空（数字类型） 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "sideMenu API", offset);
		addApicloudProposal(proposals, "close", showCode(location, "close", false, "id:")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭菜单<br>"
				+ "<h3>参数</h3>"
				+ "id：要操作的菜单的id，不可为空（数字类型） 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "sideMenu API", offset);
		
	}
	private void createGraphProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", false, "bubbleUp:'',", "bubbleDown:'',", "data:[]")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开曲线图视图<br>"
				+ "<h3>参数</h3>"
				+ "x：曲线图左上角点坐标（数字类型） 默认值：15<br>"
				+ "y：曲线图左上角点坐标（数字类型） 默认值：64<br>"
				+ "width：曲线图视图的宽（数字类型） 默认值：当前设备屏幕宽减30<br>"
				+ "height：曲线图视图的高（数字类型） 默认值：当前设备屏幕的宽减170<br>"
				+ "bgColor：曲线图视图的背景色（字符串类型） 默认值：#FFFFFF<br>"
				+ "coordColor：曲线图视图的坐标系颜色（字符串类型） 默认值：#A9A9A9<br>"
				+ "markColor：曲线图视图的坐标系标注颜色（字符串类型） 默认值：#000000<br>"
				+ "lineColor：曲线颜色（字符串类型） 默认值：#1E90FF<br>"
				+ "bubbleUp：点击结点弹出下气泡的背景图，不可为空（字符串类型） 默认值：无<br>"
				+ "bubbleDown：点击结点弹出上气泡的背景图，不可为空（字符串类型） 默认值：无<br>"
				+ "data：曲线数据，不可为空（数组类型） 默认值：无<br>"
				+"内部字段：[{ time:”15：00”,时间点data:”50”, 数据isonline:’1’ 保留使用}]<br>"	
				+ "fixedOn：将此模块视图添加到指定窗口的名字，可为空（字符串类型）默认值：当前主窗口的名字<br>"
				+"<h3>回调函数</h3>"
				+"function({orient:用户拖动曲线到头（向左或向右或缩小曲线）返回拖动事件，其值分别为：left，right，narrow}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "graph API", offset);
		addApicloudProposal(proposals, "reload", showCode(location, "reload", false, "data:[]")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"刷新曲线数据<br>"
				+ "<h3>参数</h3>"
				+ "type：刷新方式：0-----向曲线左边拼接数据、1-----向曲线右边拼接数据、2-----刷新整个曲线数据（数字类型） 默认值：2<br>"
				+ "data：要刷新的数据，不可为空（数组类型） 默认值：无<br>"
				+"内部字段：[{ time:”15：00”,时间点data:”50”, 数据isonline:’1’ 保留使用}]<br>"	
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "graph API", offset);
		addApicloudProposal(proposals, "close", showCode(location, "close", false)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭曲线图视图<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "graph API", offset);
		
	}
	private void createNetAudioProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "play", showCode(location, "play", true, "type:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"播放网络音频<br>"
				+ "<h3>参数</h3>"
				+ "path：网络音频资源地址，不能为空（字符串类型） 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({duration:音频总时长current:当前播放位置}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "netAudio API", offset);
		addApicloudProposal(proposals, "setVolume", "setVolume();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置音量<br>"
				+ "<h3>参数</h3>"
				+ "volume：音量大小（0-1）（数字类型） 默认值：0<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "netAudio API", offset);
		addApicloudProposal(proposals, "setProgress", "setProgress();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置播放位置<br>"
				+ "<h3>参数</h3>"
				+ "progress：播放位置百分比（0-100）（数字类型） 默认值：0<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "netAudio API", offset);
		addApicloudProposal(proposals, "pause", "pause();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"暂停播放<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "netAudio API", offset);
		addApicloudProposal(proposals, "stop", "stop();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"停止播放<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "netAudio API", offset);
	}
	private void createShakeViewProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		addApicloudProposal(proposals, "open", "opne();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开摇一摇视图<br>"
				+ "<h3>参数</h3>"
				+ "x：视图左上角点坐标，可为空（数字类型） 默认值：0<br>"
				+ "y：视图左上角点坐标，可为空（数字类型） 默认值：紧贴导航条下边缘<br>"
				+ "w：视图的宽度，可为空（数字类型） 默认值：当前设备屏幕宽<br>"
				+ "h：视图高度，可为空（数字类型） 默认值：w<br>"
				+ "type：摇一摇视图类型，取值范围见摇一摇类型常量，可为空（字符串类型） 默认值：up_down<br>"
				+ "anim：视图动画的参数配置，可为空（JSON对象） 默认值：无<br>"
				+ "内部字段：{time:动画持续时间，数字，默认3.0秒，可为空sound:摇动后的音效文件路径，字符串，可为空<br>"
				+ "isShake:是否添加手机震动效果，布尔值，默认false，可为空}<br>"
				+ "img：视图界面图片配置，可为空（JSON对象） 默认值：无<br>"
				+ "内部字段：{leftUp:左边（上面）的图片路径，字符串类型，默认灰色界面可为空 rightDown:右边（下面）的图片路径，字符串类型，默认灰色界面可为空<br>"
				+ "bg:背景图片路径，字符串类型，默认绿色面板，可为空 shake:摇动效果动画时摇动的图片，可为空。当type为up_down或left_right时，忽略此参数}<br>"
				+ "fixedOn：将模块视图添加到指定窗口名，可为空（字符串类型） 默认值：当前窗口名<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "shakeView API", offset);
		addApicloudProposal(proposals, "shake", "shake();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"触发摇一摇事件<br>"
				+ "<h3>参数</h3>"
				+ "anim：视图动画的参数配置，可为空（JSON对象） 默认值：无<br>"
				+ "内部字段：[{time:动画持续时间，数字，默认3.0秒，可为空sound:摇动后的音效文件路径，字符串，可为空<br>"
				+ "isShake:是否添加手机震动效果，布尔值，默认false，可为空}]<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "shakeView API", offset);
		addApicloudProposal(proposals, "hide", "hide();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"隐藏视图<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "shakeView API", offset);
		addApicloudProposal(proposals, "show", "show();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"显示视图<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "shakeView API", offset);
		addApicloudProposal(proposals, "close", "close();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭视图<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "shakeView API", offset);
		
	}
	
	private void createListContactProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true, "data:[]")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开<br>"
				+ "<h3>参数</h3>"
				+ "x：列表视图的左上角点的坐标，可为空（数字类型） 默认值：0<br>"
				+ "y：列表视图的左上角点的坐标，可为空（数字类型） 默认值：紧贴导航条位置<br>"
				+ "w：列表视图的宽，可为空（数字类型） 默认值：当前屏幕宽度<br>"
				+ "h：列表视图的高，可为空（数字类型） 默认值：全屏<br>"
				+ "bgColor：列表的背景色，可为空（字符串类型） 默认值：#FFFFFF<br>"
				+ "groupTitle：配置组标题的字体颜色和大小，可为空，为空时显示字段里默认值（JSON对象） 默认值：无<br>"
				+ "内部字段：{color:字符串类型，默认#949494,可为空 size:数字类型，默认13，可为空 bgColor:标题背景色，默认#DBDBDB,可为空}<br>"
				+ "leftBtn：往右滑动cell露出的按钮组成的数字，可为空，为空时则表示cell不可向右滑动（数组对象） 默认值：无<br>"
				+ "内部字段：[{ color:按钮背景色，不可为空 title:按钮名字，不可为空 selectColor:按钮选中时候的颜色值，不可为空}]<br>"
				+ "leftBg：往右滑动cell露出的视图的背景色（字符串类型） 默认值：#5cacee<br>"
				+ "rightBtn：往左滑动cell露出的按钮组成的数字，可为空，为空时则表示cell不可向左滑动（数组对象） 默认值：无<br>"
				+ "内部字段：[{ color:按钮背景色，不可为空 title:按钮名字，不可为空 selectColor:按钮选中时候的颜色值，不可为空}]<br>"
				+ "rightBg：往左滑动cell露出的视图的背景色，可为空。（字符串类型） 默认值：#6c7b8b<br>"
				+ "borderColor：每个cell之间分割线的颜色（字符串类型） 默认值：#696969<br>"
				+ "cellBgColor：cell的背景色（字符串类型） 默认值：#AFEEEE<br>"
				+ "cellSelectColor：选中cell时的颜色（字符串类型） 默认值：#F5F5F5<br>"
				+ "cellHeight：每个cell的高度（数字类型） 默认值：55<br>"
				+ "imgHeight：自适应cell的高度（数字类型） 默认值：头像的高<br>"
				+ "imgWidth：头像的宽（数字类型） 默认值：自适应cell的高度<br>"
				+ "data：数据库名称，不能为空（JSON对象） 默认值：无<br>"
				+ "内部字段：{comman 索引值: [{<br>"
				+ "img:               //cell的头像，一个网络路径，此图片会被缓存到本地，可为空<br>"
				+ "placeholderImg:       //头像,本地路径,加载网络图片时显示界面上的图，不可为空<br>"
				+ "title:               //cell的标题，若subtitle为空时，title上下居中位置，不可为空<br>"
				+ "subTitle:            //cell的子标题，可为空，为空时title上下居中显示<br>"
				+ "titleLocation         //标题在水平线上的位置center,left,right<br>"
				+ "titleSize             //标题字体的大小默认12，可维空<br>"
				+ "titleColor            //标题字体的颜色值，默认黑色，可为空<br>"
				+ "subTitleLocation      //子标题在水平线上的位置center,left,right默认left，可为空<br>"
				+ "subTitleSize          //子标题字体的大小默认12，可为空<br>"
				+ "subTitleColor         //子标题字体的颜色值,默认黑色,可为可空  }]}<br>"
				+ "indicator：是否添加右边索引导航条，可为空，若为空则不添加 默认值（JSON对象） 默认值：无<br>"
				+ "内部字段：{ bgColor:背景色，字符串，默认透明，可为空 color:索引指示器的颜色,字符串类型，默认#A1A1A1,可为空}<br>"
				+ "fixedOn：将模块视图添加在某个窗口上的名字，若为空则默认当前视图（字符串类型） 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({ index:          	//点击某个cell或其内部按钮返回其下标<br>"
				+"	    section:           //被点击的cell活着button所在的组号<br>"
				+"	clickType:      	//点击类型，0-cell；1-右边按钮；2-左边的按钮<br>"
				+"	btnIndex:       	//点击按钮时返回其下标}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "listContact API", offset);
		addApicloudProposal(proposals, "close", "close();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "listContact API", offset);
		addApicloudProposal(proposals, "reloadData",  showCode(location, "reloadData", true, "data:[]")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"刷新列表数据<br>"
				+ "<h3>参数</h3>"
				+ "data：数据库名称，不能为空（JSON对象） 默认值：无<br>"
				+ "内部字段：{comman 索引值: [{<br>"
				+ "img:               //cell的头像，一个网络路径，此图片会被缓存到本地，可为空<br>"
				+ "placeholderImg:       //头像,本地路径,加载网络图片时显示界面上的图，不可为空<br>"
				+ "title:               //cell的标题，若subtitle为空时，title上下居中位置，不可为空<br>"
				+ "subTitle:            //cell的子标题，可为空，为空时title上下居中显示<br>"
				+ "titleLocation         //标题在水平线上的位置center,left,right<br>"
				+ "titleSize             //标题字体的大小默认12，可维空<br>"
				+ "titleColor            //标题字体的颜色值，默认黑色，可为空<br>"
				+ "subTitleLocation      //子标题在水平线上的位置center,left,right默认left，可为空<br>"
				+ "subTitleSize          //子标题字体的大小默认12，可为空<br>"
				+ "subTitleColor         //子标题字体的颜色值,默认黑色,可为可空  }]}<br>"
				+"<h3>回调函数</h3>"
				+"function({ index:          	//点击某个cell或其内部按钮返回其下标<br>"
				+"	    section:           //被点击的cell活着button所在的组号<br>"
				+"	clickType:      	//点击类型，0-cell；1-右边按钮；2-左边的按钮<br>"
				+"	btnIndex:       	//点击按钮时返回其下标}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "listContact API", offset);
		addApicloudProposal(proposals, "setRefreshHeader", showCode(location, "setRefreshHeader", true, "loadingImg:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置下拉刷新样式<br>"
				+ "<h3>参数</h3>"
				+ "loadingImg：下拉刷新的小箭头本地图片的路径，不能为空（字符串类型） 默认值：无<br>"
				+ "bgColor：下拉刷新视图的背景色，可为空（字符串类型） 默认值：#f5f5f5<br>"
				+ "textColor：提示文字颜色（字符串类型） 默认值：#8e8e8e<br>"
				+ "textDown：提示文字（字符串类型） 默认值：下拉可以刷新…<br>"
				+ "textUp：提示文字（字符串类型） 默认值：松开开始刷新…<br>"
				+ "showTime：是否显示刷新时间（布尔类型） 默认值：true<br>"
				+"<h3>回调函数</h3>"
				+"function({ //触发加载事件}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "listContact API", offset);
		
	}
	
	private void createScrollRotationProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showFunction(location, "open")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开滚动旋转器<br>"
				+ "<h3>参数</h3>"
				+ "x：视图左上角点坐标，可为空（数字类型） 默认值：0<br>"
				+ "y：图左上角点坐标，可为空（数字类型） 默认值：导航条下边缘位置<br>"
				+ "w：视图的宽，可为空（数字类型） 默认值：当前设备屏幕宽<br>"
				+ "h：视图的高，可为空（数字类型） 默认值：视图的宽减20<br>"
				+ "items：每条目的信息成的数组，可为空（数组对象） 默认值：#008B00纯色板<br>"
				+ "内部字段：[{ imgPath :   字符串，图片路径，支持各种本地协议,可为空,默认#008B00纯色板<br>"
				+ " title:字符串，说明文字，可为空，为空时不显示文字 fontColor:    //字符串，字体颜色，可为空，默认白色   fontSize:     //数字，字体大小，可为空，默认13}]<br>"
				+ "cornerRadius：每条目图片的圆角大小（圆角的半径），可为空（数字类型） 默认值：0<br>"
				+ "intervalTime：描述：自动连播时间间隔，可为空，若为空则不自动连播（数字类型） 默认值：无<br>"
				+ "pageControl：页面控制器参数，可为空，若为空则不显示页面控制器（JSON对象） 默认值：无<br>"
				+ "内部字段：{ normalColor：字符串类型，可为空，常色值，默认#FFFFFF<br>"
				+ "selectedColor：     //字符串类型，可为空，选中值，默认#DA70D6 heightPercent：数字类型，可为空，Y轴高度百分比，默认50.0}<br>"
				+ "fixedOn：要添加到某个视图上，可为空，若为空则默认当前视图（字符串类型） 默认值：当前视图的名字<br>"
				+"<h3>回调函数</h3>"
				+"function({ click:是否是点击事件，布尔值类型index：滚动后中间图片及其点击事件的下标}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "scrollRotation API", offset);
		addApicloudProposal(proposals, "show", "show();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"显示视图<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "scrollRotation API", offset);
		addApicloudProposal(proposals, "hide", "hide();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"隐藏视图<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "scrollRotation API", offset);
		addApicloudProposal(proposals, "close", "close();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭视图<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "scrollRotation API", offset);
		
	}
	
	private void createTabBarMenuProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showFunction(location, "open")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开<br>"
				+ "<h3>参数</h3>"
				+ "defaultBarSelect：默认选中的标签栏按钮.可为空（数字类型） 默认值：0<br>"
				+ "autoLayout：是否自动智能调整当前页面webView的位置: 使其距离屏幕底部距离变为 49.可为空（布尔类型） 默认值：true<br>"
				+ "barItems：标签栏各按钮信息（数组类型） 默认值：无<br>"
				+"内部字段:[{title: 标题.bgImg:背景图路径.bgImgClick:被点击后的背景图路径.}]<br>"
				+ "barItemConfig：标签栏各按钮的配置（JSON对象） 默认值：无<br>"
				+"内部字段:{titleColor:文本颜色, 默认“#ffffff”(白色), 格式为#fff、#ffffff、rgba(r,g,b,a).<br>"
				+"titleSelectColor:选中状态时,按钮文本的颜色, 默认“#ffffff”(白色). fontSize:文字尺寸默认11.0<br>"
			    +"textMarginTop:文本距离按钮上边界的距离,默认41.0 primaryItem:点击会弹出菜单的按钮的下标, 默认 2(即第三个按钮)}<br>"
			    + "barConfig：标签栏配置（JSON对象） 默认值：无<br>"
				+"内部字段:{bgImg: 背景图片路径.}<br>"
				+ "menuItems：菜单各菜单项的信息（数组对象） 默认值：无<br>"
				+ "内部字段：[{title:标题  bgImg:背景图片.bgImgClick: 点击时的背景图片.}]<br>"
				+ "menuItemConfig：菜单项配置（JSON对象） 默认值：无<br>"
				+ "内部字段：{titleColor:	文本颜色, 默认“#ffffff”(白色), 格式为#fff、#ffffff、rgba(r,g,b,a)等.<br>"
				+ "titleSelectColor:	选中状态时,按钮文本的颜色, 默认“#ffffff”(白色).<br>"
				+ "fontSize: 文字大小,默认11.0.textMarginTop:文本距离按钮上边界的距离,默认90.0}<br>"
				+ "menuConfig：菜单配置（JSON对象） 默认值：无<br>"
				+ "内部字段：{coverBgColor: 遮罩背景色, 默认“# 000000”(黑色) 格式为#fff、#ffffff、rgba(r,g,b,a)<br>"
				+ "coverAlpha: 遮罩的透明度, ,默认0.8, 取值范围0.0~1.0. rows:单页每行显示的按钮数,默认4.}<br>"
				+"<h3>回调函数</h3>"
				+"function({item:对象,表示被点击的按钮 {type:被点击的按钮所属控件,字符串,可选”bar”, “menu”<br>"
				+"index:	// 被点击的按钮的下标. 标签栏和菜单部分的按钮的下标均分别从 0 开始计数}},{msg:错误信息}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "tabBarMenu API", offset);
		addApicloudProposal(proposals, "setBarSelect", "setBarSelect();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"模拟点击标签栏按钮<br>"
				+ "<h3>参数</h3>"
				+ "barItems：按钮下标.（数字类型） 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "tabBarMenu API", offset);
		addApicloudProposal(proposals, "setBadge", "setBadge();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置徽章<br>"
				+ "<h3>参数</h3>"
				+ "barConfig：要设置徽章的按钮（JSON对象） 默认值：无<br>"
				+"内部字段:{ type: 按钮所属控件,字符串,可选”bar”, “menu”<br>"
				+"  index:按钮的下标. 数字,标签栏和菜单部分的按钮的下标均分别从 0 开始计数}<br>"
				+ "title：要设置的按钮的内容,可为空;为空时,type会自动设为”center”（字符串类型） 默认值：\"\"<br>"
				+ "type：徽章风格,可选”left”, “center”, “right”,可为空（字符串类型） 默认值：\"center\"<br>"
				+ "bgColor：徽章的背景色, 格式为#fff、#ffffff、rgba(r,g,b,a)等,可为空.（字符串类型） 默认值：#ff0000 (红色)<br>"
				+ "titleColor：文本颜色, 格式为#fff、#ffffff、rgba(r,g,b,a)等,可为空（字符串类型） 默认值：#ffffff (白色)<br>"
				+ "fontSize：字体大小.可为空（数字类型） 默认值：11.0<br>"
				+ "marginTop：徽章距离按钮上边缘的距离.可为空.（数字类型） 默认值：17<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "tabBarMenu API", offset);
		addApicloudProposal(proposals, "removeBadge", "removeBadge();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"移除徽章<br>"
				+ "<h3>参数</h3>"
				+"  index:按钮下标. 标签栏和菜单部分的按钮的下标均分别从 0 开始计数,互不影响（数字类型） 默认值：无.<br>"
				+ "type：按钮所属控件,可选 “bar”, “menu”.（字符串类型） 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "tabBarMenu API", offset);
		addApicloudProposal(proposals, "show", "show();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"显示<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "tabBarMenu API", offset);
		addApicloudProposal(proposals, "showMenu", "showMenu();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"显示弹出菜单<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "tabBarMenu API", offset);
		addApicloudProposal(proposals, "hide", "hide();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"隐藏<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "tabBarMenu API", offset);
		addApicloudProposal(proposals, "hideMenu", "hideMenu();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"隐藏弹出菜单<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "tabBarMenu API", offset);
		addApicloudProposal(proposals, "close", "close();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "tabBarMenu API", offset);
		
	}
	
	private void createCircularMenuProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true, "items:[]")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开转盘菜单<br>"
				+ "<h3>参数</h3>"
				+ "centerX：环形菜单的圆心坐标，可为空（数字类型） 默认值：当前屏幕的中间<br>"
				+ "centerY：环形菜单的圆心坐标，可为空（数字类型） 默认值：导航条下边缘加模块半径的位置<br>"
				+ "radius：环形菜单的圆半径，可为空（数字类型） 默认值：150<br>"
				+ "centerBtnRadius：环形菜单中间圆形按钮的半径，可为空（数字类型） 默认值：radius/3.0<br>"
				+ "bgImg：环形菜单的背景图片，可为空，为空则背景透明（字符串类型） 默认值：无<br>"
				+ "centerBtnImg：环形菜单的中间按钮的背景图片，可为空，为空则没有中间按钮（字符串类型） 默认值：无<br>"
				+ "indicatorPosition：环形菜单的指针位置，取值范围见常量指针位置，可为空（字符串类型） 默认值：left<br>"
				+ "fixedOn：视图添加到目标窗口的名字，可为空，为空则添加到当前window（字符串类型） 默认值：无<br>"
				+ "items：子菜单信息组成的数组，不可为空（数组对象） 默认值：无<br>"
				+ "内部字段：[normal:  按钮常态背景图片，不可为空   highlight:  按钮高亮背景图片，可为空<br>"
				+ " title:  标题，可为空     titleColor: 标题颜色，可为空，默认#919191}]<br>"
				+"<h3>回调函数</h3>"
				+"function({ click:布尔值，判断是否是点击事件的callBack index:用户点击按钮的下标，中间按钮的下标为最大 indicatorIndex:旋转停止后指针所指位置下的按钮的下标}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "circularMenu API", offset);
		addApicloudProposal(proposals, "close", "close();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭环形菜单<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "circularMenu API", offset);
		
	}
	
	private void createMultiSelectorProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true, "content:[]")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开选择器<br>"
				+ "<h3>参数</h3>"
				+ "y：视图的起点y轴坐标，可为空（数字类型） 默认值：屏幕高度-244<br>"
				+ "height：视图的高度，可为空（数字类型） 默认值：244<br>"
				+ "cancelImg：选择器左上角取消按钮的图片，可为空（字符串类型） 默认值：默认图片<br>"
				+ "enterImg：选择器右上角确定按钮的图片，可为空（字符串类型） 默认值：默认图片<br>"
				+ "titleImg：选择器工具栏的背景图片，可为空（字符串类型） 默认值：默认图片<br>"
				+ "bgImg：选择的背景图片，可为空（字符串类型） 默认值：默认图片<br>"
				+ "fontColor：字体颜色，可为空（字符串类型） 默认值：#828282<br>"
				+ "selectedColor：选中字体颜色，可为空（字符串类型） 默认值：#79CDCD<br>"
				+ "animation：打开关闭时是否添加动画，可为空（布尔类型） 默认值：true<br>"
				+ "content：选择器的内容，不可为空（数组对象） 默认值：无<br>"
				+ "fixedOn：将此模块视图添加到指定窗口的名字，可为空（字符串类型）默认值：当前主窗口的名字<br>"
				+"<h3>回调函数</h3>"
				+"function({selectAry:选中的字符串组成的数组}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "circularMenu API", offset);
		addApicloudProposal(proposals, "close", "close();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭选择器<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "circularMenu API", offset);
		addApicloudProposal(proposals, "show", "show();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"显示选择器<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "circularMenu API", offset);
		addApicloudProposal(proposals, "hidden", "hidden();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"隐藏选择器<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "circularMenu API", offset);
		
	}
	
	private void createChatBoxProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true, "sourcePath:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开输入框<br>"
				+ "<h3>参数</h3>"
				+ "bgColor：输入视图背景色的十六进制值，可为可空（字符串类型） 默认值：#f2f2f2<br>"
				+ "lineColor：输入框视图最上边的分割线色的十六进制值，可为空（字符串类型） 默认值：#d9d9d9<br>"
				+ "borderColor：输入框边框色的十六进制值，可为空（字符串类型） 默认值：#B3B3B3<br>"
				+ "fileBgColor：输入框背景色的十六进制值，可为空（字符串类型） 默认值：#FFFFFF<br>"
				+ "switchButton：表情键盘加号的按钮图片，可为空（JSON对象） 默认值：无<br>"
				+"内部字段:{faceNormal:表情按钮背景图片路径，可为空，默认纯绿色 faceHighlight:表情按钮高亮图片路径，可为空，默认纯绿色<br>"
				+"addNormal:添加按钮背景图片路径，可为空，默认纯绿色addHighlight:添加按钮高亮图片路径，可为空，默认纯绿色<br>"
				+"keyboardNormal:键盘按钮背景图片路径，可为空，默认纯绿色 keyboardHightlight:键盘按钮高亮图片路径，可为空，默认纯绿色}<br>"
				+ "sourcePath:自定义表情源文件(.json的文件和图片表情集文件同名且在同一路径下)的路径，不可为空，" 
				+"json文件格式如下：[{name:’Expression_1’,text:’[微笑]’}]（字符串对象） 默认值：无<br>"
				+ "addButtons：添加界面的按钮信息，可为空（数组对象） 默认值：无<br>"
				+"内部字段:[{normal:常态按钮背景图片，可为空，默认绿色面板 highlight:高亮按钮背景图片，可为空，默认暗色<br>"
				+"title:按钮标题，可为空，默认无 titleSize:标题大小，可为空，默认10 titleColor:标题颜色，可为空，默认#a3a3a3}]<br>"
				+ "fixedOn：将此模块视图添加到指定窗口的名字，可为空（字符串类型）默认值：当前主窗口的名字<br>"
				+"<h3>回调函数</h3>"
				+"function({click:是否是点击事件的回调，布尔类型 index：         //若click为true，则此参数为用户点击按钮的下标，否则undefined<br>"
				+"msg: 返回输入的文字，若emotion为true，则此字符串里包含[微笑]这样的表情图片名字。若干click为true则此参数为undefined}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "chatBox API", offset);
		addApicloudProposal(proposals, "close", "close();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭聊天输入框<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "chatBox API", offset);
		addApicloudProposal(proposals, "show", "show();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"显示聊天输入框<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "chatBox API", offset);
		addApicloudProposal(proposals, "hide", "hide();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"隐藏聊天输入框<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "chatBox API", offset);
		
	}
	
	private void createStackMenuProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true, "sourcePath:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开stack菜单<br>"
				+ "<h3>参数</h3>"
				+ "startX：stack菜单起点坐标，可为空（数字类型） 默认值：120<br>"
				+ "startY：stack菜单起点坐标，可为空（数字类型） 默认值：当前屏幕高减去70<br>"
				+ "itemSize：子菜单大小，可为空（数字类型） 默认值：50<br>"
				+ "direction：弹出子菜单方向，详情参考弹出菜单方向常量，可为空（字符串类型） 默认值：rightUp<br>"
				+ "titleColor：子菜单标题颜色，可为空（字符串类型） 默认值：#8b3e2f<br>"
				+ "items：子菜单参数组成的数组，不可为空 （数组对象） 默认值：无<br>"
				+"内部字段:[{  title：子按钮标题     icon:子按钮头像}]<br>"
				+"<h3>回调函数</h3>"
				+"function({ index:选中的子菜单按钮的下标}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "stackMenu API", offset);
		addApicloudProposal(proposals, "close", "close();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭菜单<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "stackMenu API", offset);
		
	}
	
	private void createBookReaderProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true, "filePath:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开阅读器<br>"
				+ "<h3>参数</h3>"
				+ "x：模块视图左上角点的坐标，可为空（数字类型） 默认值：0<br>"
				+ "y：模块视图左上角点的坐标，可为空（数字类型） 默认值：紧贴导航栏下边缘<br>"
				+ "w：模块视图左上角点的宽，可为空（数字类型） 默认值：当前设备屏幕的宽<br>"
				+ "h：模块视图左上角点的高，可为空（数字类型） 默认值：宽减20<br>"
				+ "bg：阅读器的背景色，支持grb，rgba，img，#，可为空（字符串类型） 默认值：#FFFFF0<br>"
				+ "animType：翻页动画效果，可为空。（保留使用）（字符串类型） 默认值：curl<br>"
				+ "progress：阅读器打开时的进度，取值范围0-100，可为空（数字类型） 默认值：0<br>"
				+ "textStyle：阅读文本字体样式设置，可为空 （json对象） 默认值：见内部字段<br>"
				+"内部字段:{size:字体大小，数字，默认12，可为空 color:字体颜色，字符串，支持rgb，rgba，#，可为空。默认#424242}<br>"
				+ "filePath：阅读器数据源文件地址，支持widget等本地路径协议，不可为空（字符串类型） 默认值：无<br>"
				+ "fixedOn：将模块视图添加到指定窗口的名字，可为空（字符串类型） 默认值：当前主窗口的名字<br>"
				+"<h3>回调函数</h3>"
				+"function({eventType:事件类型，字符串，取值范围见事件类型 progress:阅读当前进度（翻页时）}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "bookReader API", offset);
		addApicloudProposal(proposals, "setValue", showCode(location, "setValue", true, "filePath:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置阅读器的参数<br>"
				+ "<h3>参数</h3>"
				+ "bg：阅读器的背景色，支持grb，rgba，img，#，可为空（字符串类型） 默认值：#FFFFF0<br>"
				+ "animType：翻页动画效果，可为空。（保留使用）（字符串类型） 默认值：curl<br>"
				+ "progress：阅读器打开时的进度，取值范围0-100，可为空（数字类型） 默认值：0<br>"
				+ "textStyle：阅读文本字体样式设置，可为空 （json对象） 默认值：见内部字段<br>"
				+"内部字段:{size:字体大小，数字，默认12，可为空 color:字体颜色，字符串，支持rgb，rgba，#，可为空。默认#424242}<br>"
				+ "filePath：阅读器数据源文件地址，支持widget等本地路径协议，可为空（字符串类型） 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态值，布尔值},{msg:错误描述，字符串}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "bookReader API", offset);
		addApicloudProposal(proposals, "show", "show();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"显示阅读器<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态值，布尔值},{msg:错误描述，字符串}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "bookReader API", offset);
		addApicloudProposal(proposals, "hide", "hide();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"隐藏阅读器<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态值，布尔值},{msg:错误描述，字符串}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "bookReader API", offset);
		addApicloudProposal(proposals, "close", "close();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭阅读器<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态值，布尔值},{msg:错误描述，字符串}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "bookReader API", offset);
		
	}
	
	private void createNavigationBarProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true, "items:[]")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开阅读器<br>"
				+ "<h3>参数</h3>"
				+ "x：导航条横坐标.可为空（数字类型） 默认值：0<br>"
				+ "y：导航条纵坐标.可为空（数字类型） 默认值：0<br>"
				+ "w：导航条宽度.可为空（数字类型） 默认值：当前 frame 宽度.默认值,仅在style参数为\" left_to_right\" 或 \"right_to_left\"时有效.<br>"
				+ "h：导航条高度.可为空（数字类型） 默认值：当前frame高度.默认值,仅在style参数为\" up_to_down \" 或 \"down_to_up\"时有效<br>"
				+ "style：导航条风格.取值范围见导航条方向设置. 可为空（字符串类型） 默认值： \"left_to_right\"<br>"
				+ "items：按钮项（数组类型） 默认值：无<br>"
				+"内部字段:[{title: 标题. 字符串类型.不可为空  titleSelected:选中后的标题.字符串.默认与title相同.可为空."
				+"bg:	背景,支持rgb,rgba,# , img. 字符串，默认#696969，可为空	bgSelected:选中后背景,支持rgb,rgba,# , img.字符串.默认与bg相同.可为空.}]"
				+ "selectedIndex：被选中的导航项的下标.可为空.不传表示不选中任何item（数字类型） 默认值：无<br>"
				+ "font：导航项字体的大小和颜色.可为空（JSON类型） 默认值：与系统设置相同.<br>"
				+"内部字段:{size:航项字体大小. 数字.默认系统字号，可为空  sizeSelected:选中时,导航项字体大小.默认size大小，可为空"
			    +"color:导航条字体颜色.字符串.默认#FFFFFF,可为空 	colorSelected:导航条字体颜色.字符串.默认与 color 相同.可为空	alpha:背景透明度. 数字.取值范围0-1，默认1，可为空}<br>"
				+ "bg：导航条背景,支持rgb,rgba,# , img.可为空（字符串类型） 默认值：#6b6b6b<br>"
				+ "alpha：背景透明度. 取值范围0-1，默认1，可为空（数字类型） 默认值：1.0<br>"
				+ "itemSize：单个导航项的宽度和高度.可不传.（JSON类型） 默认值：无<br>"
				+"内部字段:{w:单个导航项的宽度.数字.默认为 导航条宽度/导航项个数. 可不传.仅在导航条style参数为 \"left_to_right\"或\"right_to_left\"时有效."
			    +"h:单个导航项的高度.数字.默认为 导航条高度/导航项个数.可不传. 仅在导航条style参数为 \"up_to_down\"或\"down_to_up\"时有效}<br>"
			    + "popItem：pop按钮.用户可通过此按钮打开其他自定义窗口或视图.若不传,则不显示pop按钮（JSON类型） 默认值:无.<br>"
				+"内部字段:{ position:pop按钮位置. 字符串.默认\"tail\" 取值范围\"head\",\"tail\".可为空.  title:	标题. 字符串.默认\"打开\"，可为空"
		        +"titleSelected:选中时的标题.字符串.默认与title相同.可为空.  bg:	背景,支持rgb,rgba,# , img. 字符串.默认#696969，可为空"
				+"bgSelected:选中后背景,支持rgb,rgba,# , img.字符串.默认与bg相同.可为空.}<br>"
				+ "fixedOn：将模块视图添加到指定窗口名，可为空（字符串类型） 默认值：当前窗口名<br>"
				+"<h3>回调函数</h3>"
				+"function({id: 导航条对象唯一标识.数字类型.  index:导航项下标.数字类型. 当点击 pop按钮时,此值为 -1. }){msg:错误信息}"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "navigationBar API", offset);
		addApicloudProposal(proposals, "show", "show();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"显示<br>"
				+ "<h3>参数</h3>"
				+ "id:如果当前页面存在且仅存在一个 导航条 对象,则默认操作此对象.此时参数可为空.（数字类型） 默认值:无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "navigationBar API", offset);
		addApicloudProposal(proposals, "hide", "hide();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"隐藏<br>"
				+ "<h3>参数</h3>"
				+ "id:如果当前页面存在且仅存在一个 导航条 对象,则默认操作此对象.此时参数可为空.（数字类型） 默认值:无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "navigationBar API", offset);
		addApicloudProposal(proposals, "close", "close();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭<br>"
				+ "<h3>参数</h3>"
				+ "id:如果当前页面存在且仅存在一个 导航条 对象,则默认操作此对象.此时参数可为空.（数字类型） 默认值:无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "navigationBar API", offset);
		addApicloudProposal(proposals, "config", showCode(location, "config", true, "key:'',", "value:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置或获取模块配置的值<br>"
				+ "<h3>参数</h3>"
				+ "id:如果当前页面存在且仅存在一个 导航条 对象,则默认操作此对象.此时参数可为空.（数字类型） 默认值:无<br>"
				+ "key:要配置的参数名称. 可选范围: open方法中传入的 params 的任一字段.（字符串类型） 默认值:无<br>"
				+ "value:要配置的模块的值.不传此参数,则可以直接获取当前配置的值（混合类型.应与key期望的类型一致） 默认值:无<br>"
				+"<h3>回调函数</h3>"
				+"function({ key:字符串. 参数名称.  oldValue:混合类型,与参数期望的值类型一致. 配置的原值. newValue:混合类型. 配置的新值. 当为获取配置的操作时, 与oldValue相同.,{msg:错误信息}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "navigationBar API", offset);
		
	}
	
	private void createZipProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "archive", showCode(location, "archive", true, "files:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"压缩文件<br>"
				+ "<h3>参数</h3>"
				+ "password：压缩的密码，可为空（字符串类型） 默认值：无<br>"
				+ "files：压缩的文件路径组成的数组，不能为空（数组类型）默认值：无<br>"
				+ "内部字段：[‘widget://res/1.docx’]<br>"
				+ "toPath：压缩后的文件存放路径，若未指定文件名，则默认原文件名（若源文件为多个则取第一个），可为空。为空时默认为原文件（若源文件为多个则取第一个）路径。（字符串类型） 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status: 状态值},{msg:错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "zip API", offset);
		addApicloudProposal(proposals, "unarchive", showCode(location, "unarchive", true, "files:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"解压文件<br>"
				+ "<h3>参数</h3>"
				+ "password：压缩的密码，可为空（字符串类型） 默认值：无<br>"
				+ "toPath：解压后的文件路径，可为空。为空时默认原文件路径（字符串类型）默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status: 状态值},{msg:错误描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "zip API", offset);
		
	}
	
	private void createMeChatProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "initMeChat", showCode(location, "initMeChat", false, "appkey:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"初始化美洽<br>"
				+ "<h3>参数</h3>"
				+ "appkey：注册美洽后，从美洽后台获得的appkey，不可为空（字符串类型） 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "meChat API", offset);
		addApicloudProposal(proposals, "show", "show();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"弹出美洽聊天界面<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "meChat API", offset);
		addApicloudProposal(proposals, "specifyAlloc", showCode(location, "initMeChat", true,  "groupId:'',",  "agentId:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"指定分配客服与客服组<br>"
				+ "<h3>参数</h3>"
				+ "agentId：在美洽系统中客服对应的ID，与groupId配合为空（字符串类型） 默认值：无<br>"
				+ "groupId：在美洽系统中客服对应的ID，与agentId配合为空（字符串类型） 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "meChat API", offset);
		addApicloudProposal(proposals, "addUserInfo", showCode(location, "addUserInfo", true)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"添加规范化用户信息<br>"
				+ "<h3>参数</h3>"
				+ "realName：真实姓名，可为空（字符串类型） 默认值：无<br>"
				+ "sex：性别，可为空（字符串类型） 默认值：无<br>"
				+ "birthday：生日，可为空（字符串类型） 默认值：无<br>"
				+ "age：年龄，可为空（字符串类型） 默认值：无<br>"
				+ "job：职业，可为空（字符串类型） 默认值：无<br>"
				+ "avatar：头像url，可为空（字符串类型） 默认值：无<br>"
				+ "comment：备注，可为空（字符串类型） 默认值：无<br>"
				+ "appUserId：用户识别符，可为空（字符串类型） 默认值：无<br>"
				+ "appUserName：登陆名，可为空（字符串类型） 默认值：无<br>"
				+ "appNickName：昵称，可为空（字符串类型） 默认值：无<br>"
				+ "tel：电话，可为空（字符串类型） 默认值：无<br>"
				+ "email：邮箱，可为空（字符串类型） 默认值：无<br>"
				+ "address：地址，可为空（字符串类型） 默认值：无<br>"
				+ "QQ：qq号，可为空（字符串类型） 默认值：无<br>"
				+ "weibo：新浪微博ID，可为空（字符串类型） 默认值：无<br>"
				+ "weixin：微信号，可为空（字符串类型） 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "meChat API", offset);
		addApicloudProposal(proposals, "addOtherInfo", showCode(location, "addOtherInfo", true, "files:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"添加自定义信息<br>"
				+ "<h3>参数</h3>"
				+ "所有参数均为字符串类型，均无默认值，key可自定义<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "meChat API", offset);
		
	}
	
	private void createModelProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "config", showCode(location, "config", false, "appKey:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"全局函数，配置appKey等应用信息。设置一次即生效，直到下一次设置，不设置默认为当前应用的信息<br>"
				+ "<h3>参数</h3>"
				+ "appId：类型：字符串 默认值：widget目录下config.xml里面的id 述：应用的id，在APICloud上应用概览里获取，可以为空<br>"
				+ "appKey：类型：字符串 默认值：无 描述：应用的安全校验Key，在APICloud上应用概览里获取，不能为空<br>"
				+ "host：类型：字符串 默认值：无 描述：应用服务器地址，可为空，为空时默认为编译时的服务器地址<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "insert", showCode(location, "insert", true, "class:'',", "value:{}")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"向对象插入一条数据<br>"
				+ "<h3>参数</h3>"
				+ "class：类型：字符串 默认值：无 描述：对象的名称，对应服务器上的同名class，不能为空<br>"
				+ "value：类型：JSON对象 默认值：无 描述：插入的键值对，与服务器上class中键值对应，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"ret：类型：JSON对象     描述：插入成功后对应的该条数据在服务器的所有键值<br>"
				+"err：类型：JSON对象     描述：错误信息<br>"
				+"<h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "deleteById", showCode(location, "deleteById", true, "class:'',", "id:'',", "value:{}")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"根据ID删除对象的一条数据<br>"
				+ "<h3>参数</h3>"
				+ "class：类型：字符串 默认值：无 描述：对象的名称，对应服务器上的同名class，不能为空<br>"
				+ "id：类型：字符串 默认值：无 描述：被删除数据的行ID，不能为空<br>"
				+ "value：类型：JSON对象 默认值：无 描述：将要更新的键值对，与服务器上class中键值对应，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"ret：类型：JSON对象     描述：更新成功后对应的该条数据在服务器的所有键值<br>"
				+"err：类型：JSON对象     描述：错误信息<br>"
				+"<h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "updateById", showCode(location, "updateById", true, "class:'',", "id:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"根据ID更新对象的一条数据<br>"
				+ "<h3>参数</h3>"
				+ "class：类型：字符串 默认值：无 描述：对象的名称，对应服务器上的同名class，不能为空<br>"
				+ "id：类型：字符串 默认值：无 描述：将要更新数据的行ID，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"ret：类型：JSON对象     描述：成功信息<br>"
				+"err：类型：JSON对象     描述：错误信息<br>"
				+"<h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "findById", showCode(location, "findById", true, "class:'',", "id:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"根据ID查找对象的一条数据<br>"
				+ "<h3>参数</h3>"
				+ "class：类型：字符串 默认值：无 描述：对象的名称，对应服务器上的同名class，不能为空<br>"
				+ "id：类型：字符串 默认值：无 描述：被查找数据的行ID，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"ret：类型：JSON对象     描述：查找成功后对应的该条数据在服务器的所有键值<br>"
				+"err：类型：JSON对象     描述：错误信息<br>"
				+"<h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "findAll", showCode(location, "findAll", true, "class:'',", "qid:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"根据条件查找对象中所有符合条件的数据<br>"
				+ "<h3>参数</h3>"
				+ "class：类型：字符串 默认值：无 描述：对象的名称，对应服务器上的同名class，不能为空<br>"
				+ "qid：类型：字符串 默认值：无 描述：通过query对象创建的查询条件对象ID，见query对象，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"ret：类型：JSON对象     描述：查找成功后对应的所有满足条件的数据<br>"
				+"err：类型：JSON对象     描述：错误信息<br>"
				+"<h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "count", showCode(location, "count", true, "class:'',", "qid:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"根据条件查找对象中所有符合条件的数据<br>"
				+ "<h3>参数</h3>"
				+ "class：类型：字符串 默认值：无 描述：对象的名称，对应服务器上的同名class，不能为空<br>"
				+ "qid：类型：字符串 默认值：无 描述：通过query对象创建的查询条件对象ID，见query对象，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"ret：类型：JSON对象     描述：成功信息<br>"
				+"err：类型：JSON对象     描述：错误信息<br>"
				+"<h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "exist", showCode(location, "exist", true, "class:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"查询某对象/对象下某行是否存在<br>"
				+ "<h3>参数</h3>"
				+ "class：类型：字符串 默认值：无 描述：对象的名称，对应服务器上的同名class，不能为空<br>"
				+ "id：类型：字符串 默认值：无 描述：被查找数据的行ID，可为空，为空时返回对象是否存在<br>"
				+"<h3>回调函数</h3>"
				+"ret：类型：JSON对象     描述：成功信息<br>"
				+"err：类型：JSON对象     描述：错误信息<br>"
				+"<h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
	}
	
	private void createQueryProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "createQuery", showCode(location, "createQuery", true)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"创建一个query对象<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"ret：类型：JSON对象  字段：{	qid：		//query对象的句柄ID，数字型}"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "limit", showCode(location, "limit", false, "qid:'',", "value:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置查询返回结果限制为n条<br>"
				+ "<h3>参数</h3>"
				+ "qid：类型：数字 默认值：无 描述：query对象句柄ID，由createQuery创建而来，不能为空<br>"
				+ "value：类型：字符串 默认值：无 描述：被限制的数目值，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "skip", showCode(location, "skip", false, "qid:'',", "value:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置查询返回结果中忽略前n条<br>"
				+ "<h3>参数</h3>"
				+ "qid：类型：数字 默认值：无 描述：query对象句柄ID，由createQuery创建而来，不能为空<br>"
				+ "value：类型：字符串 默认值：无 描述：被忽略的数目值，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "asc", showCode(location, "asc", false, "qid:'',", "column:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置查询返回结果按某列正序排列<br>"
				+ "<h3>参数</h3>"
				+ "qid：类型：数字 默认值：无 描述：query对象句柄ID，由createQuery创建而来，不能为空<br>"
				+ "column：类型：字符串 默认值：无 描述：用于排序的列，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "desc", showCode(location, "asc", false, "qid:'',", "column:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置查询返回结果按某列倒序排列<br>"
				+ "<h3>参数</h3>"
				+ "qid：类型：数字 默认值：无 描述：query对象句柄ID，由createQuery创建而来，不能为空<br>"
				+ "column：类型：字符串 默认值：无 描述：用于排序的列，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "include", showCode(location, "include", false, "qid:'',", "column:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置查询返回结果中包含pointer指向的对象<br>"
				+ "<h3>参数</h3>"
				+ "qid：类型：数字 默认值：无 描述：query对象句柄ID，由createQuery创建而来，不能为空<br>"
				+ "column：类型：字符串 默认值：无 描述：pointer列名，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "whereEqual", showCode(location, "whereEqual", false, "qid:'',", "column:'',", "value:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置查询条件为某列等于某值<br>"
				+ "<h3>参数</h3>"
				+ "qid：类型：数字 默认值：无 描述：query对象句柄ID，由createQuery创建而来，不能为空<br>"
				+ "column：类型：字符串 默认值：无 描述：作为条件的列名，不能为空<br>"
				+ "value：类型：字符串 默认值：无 描述：作为条件的值，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "whereNotEqual", showCode(location, "whereNotEqual", false, "qid:'',", "column:'',", "value:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置查询条件为某列不等于某值<br>"
				+ "<h3>参数</h3>"
				+ "qid：类型：数字 默认值：无 描述：query对象句柄ID，由createQuery创建而来，不能为空<br>"
				+ "column：类型：字符串 默认值：无 描述：作为条件的列名，不能为空<br>"
				+ "value：类型：字符串 默认值：无 描述：作为条件的值，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "whereLike", showCode(location, "whereLike", false,  "qid:'',", "column:'',", "value:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置查询条件为某列内容中包含某值<br>"
				+ "<h3>参数</h3>"
				+ "qid：类型：数字 默认值：无 描述：query对象句柄ID，由createQuery创建而来，不能为空<br>"
				+ "column：类型：字符串 默认值：无 描述：作为条件的列名，不能为空<br>"
				+ "value：类型：字符串 默认值：无 描述：作为条件的值，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "whereUnLike", showCode(location, "whereUnLike", false,  "qid:'',", "column:'',", "value:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置查询条件为某列内容中不包含某值<br>"
				+ "<h3>参数</h3>"
				+ "qid：类型：数字 默认值：无 描述：query对象句柄ID，由createQuery创建而来，不能为空<br>"
				+ "column：类型：字符串 默认值：无 描述：作为条件的列名，不能为空<br>"
				+ "value：类型：字符串 默认值：无 描述：作为条件的值，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "whereStartWith", showCode(location, "whereStartWith", false,  "qid:'',", "column:'',", "value:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置查询条件为某列内容以某值开头<br>"
				+ "<h3>参数</h3>"
				+ "qid：类型：数字 默认值：无 描述：query对象句柄ID，由createQuery创建而来，不能为空<br>"
				+ "column：类型：字符串 默认值：无 描述：作为条件的列名，不能为空<br>"
				+ "value：类型：字符串 默认值：无 描述：作为条件的值，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "whereEndWith", showCode(location, "whereEndWith", false, "qid:'',", "column:'',", "value:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置查询条件为某列内容以某值结尾<br>"
				+ "<h3>参数</h3>"
				+ "qid：类型：数字 默认值：无 描述：query对象句柄ID，由createQuery创建而来，不能为空<br>"
				+ "column：类型：字符串 默认值：无 描述：作为条件的列名，不能为空<br>"
				+ "value：类型：字符串 默认值：无 描述：作为条件的值，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "whereExist", showCode(location, "whereExist", false, "qid:'',", "column:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置查询条件为某列内容不为空<br>"
				+ "<h3>参数</h3>"
				+ "qid：类型：数字 默认值：无 描述：query对象句柄ID，由createQuery创建而来，不能为空<br>"
				+ "column：类型：字符串 默认值：无 描述：作为条件的列名，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "whereNotExist", showCode(location, "whereNotExist", false, "qid:'',", "column:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置查询条件为某列内容为空<br>"
				+ "<h3>参数</h3>"
				+ "qid：类型：数字 默认值：无 描述：query对象句柄ID，由createQuery创建而来，不能为空<br>"
				+ "column：类型：字符串 默认值：无 描述：作为条件的列名，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "whereContain", showCode(location, "whereContain", false, "qid:'',", "column:'',", "value:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置查询条件为某列内容中包含某值<br>"
				+ "<h3>参数</h3>"
				+ "qid：类型：数字 默认值：无 描述：query对象句柄ID，由createQuery创建而来，不能为空<br>"
				+ "column：类型：字符串 默认值：无 描述：作为条件的列名，不能为空<br>"
				+ "value：类型：字符串 默认值：无 描述：作为条件的值，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "whereContainAll", showCode(location, "whereContainAll", false, "qid:'',", "column:'',", "value:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置查询条件为某列内容中包含某几个值<br>"
				+ "<h3>参数</h3>"
				+ "qid：类型：数字 默认值：无 描述：query对象句柄ID，由createQuery创建而来，不能为空<br>"
				+ "column：类型：字符串 默认值：无 描述：作为条件的列名，不能为空<br>"
				+ "value：类型：字符串 默认值：无 描述：作为条件的值，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "whereNotContain", showCode(location, "whereNotContain", false, "qid:'',", "column:'',", "value:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置查询条件为某列内容中不包含某值<br>"
				+ "<h3>参数</h3>"
				+ "qid：类型：数字 默认值：无 描述：query对象句柄ID，由createQuery创建而来，不能为空<br>"
				+ "column：类型：字符串 默认值：无 描述：作为条件的列名，不能为空<br>"
				+ "value：类型：字符串 默认值：无 描述：作为条件的值，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "whereGreaterThan", showCode(location, "whereGreaterThan", false, "qid:'',", "column:'',", "value:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置查询条件为某列的内容大于某值<br>"
				+ "<h3>参数</h3>"
				+ "qid：类型：数字 默认值：无 描述：query对象句柄ID，由createQuery创建而来，不能为空<br>"
				+ "column：类型：字符串 默认值：无 描述：作为条件的列名，不能为空<br>"
				+ "value：类型：字符串 默认值：无 描述：作为条件的值，数字或者date型<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "whereGreaterThanOrEqual", showCode(location, "whereGreaterThanOrEqual", false, "qid:'',", "column:'',", "value:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置查询条件为某列的内容大于等于某值<br>"
				+ "<h3>参数</h3>"
				+ "qid：类型：数字 默认值：无 描述：query对象句柄ID，由createQuery创建而来，不能为空<br>"
				+ "column：类型：字符串 默认值：无 描述：作为条件的列名，不能为空<br>"
				+ "value：类型：字符串 默认值：无 描述：作为条件的值，数字或者date型<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "whereLessThan", showCode(location, "whereLessThan", false, "qid:'',", "column:'',", "value:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置查询条件为某列的内容小于某值<br>"
				+ "<h3>参数</h3>"
				+ "qid：类型：数字 默认值：无 描述：query对象句柄ID，由createQuery创建而来，不能为空<br>"
				+ "column：类型：字符串 默认值：无 描述：作为条件的列名，不能为空<br>"
				+ "value：类型：字符串 默认值：无 描述：作为条件的值，数字或者date型<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "whereLessThanOrEqual", showCode(location, "whereLessThanOrEqual", false, "qid:'',", "column:'',", "value:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置查询条件为某列的内容小于等于某值<br>"
				+ "<h3>参数</h3>"
				+ "qid：类型：数字 默认值：无 描述：query对象句柄ID，由createQuery创建而来，不能为空<br>"
				+ "column：类型：字符串 默认值：无 描述：作为条件的列名，不能为空<br>"
				+ "value：类型：字符串 默认值：无 描述：作为条件的值，数字或者date型<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "justFields", showCode(location, "justFields", false, "qid:'',", "value:[]")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置查询仅返回需要的字段<br>"
				+ "<h3>参数</h3>"
				+ "qid：类型：数字 默认值：无 描述：query对象句柄ID，由createQuery创建而来，不能为空<br>"
				+ "column：类型：字符串 默认值：无 描述：作为条件的列名，不能为空<br>"
				+ "value：类型：字符串数组 默认值：无 描述：需要返回数据的列名数组，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "exceptFields", showCode(location, "exceptFields", false, "qid:'',", "value:[]")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置查询返回除某些字段以外的字段<br>"
				+ "<h3>参数</h3>"
				+ "qid：类型：数字 默认值：无 描述：query对象句柄ID，由createQuery创建而来，不能为空<br>"
				+ "column：类型：字符串 默认值：无 描述：作为条件的列名，不能为空<br>"
				+ "value：类型：字符串数组 默认值：无 描述：不需要返回数据的列名数组，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
	}
	
	private void createRelationProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "insert", showCode(location, "insert", true, "class:'',", "id:'',", "column:'',", "value:{}")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"向对象的某关系列下插入一条内容<br>"
				+ "<h3>参数</h3>"
				+ "class：类型：字符串 默认值：无 描述：对象的名称，对应服务器上的同名class，不能为空<br>"
				+ "id：类型：字符串 默认值：无 描述：被插入对象ID，不能为空<br>"
				+ "column：类型：字符串 默认值：无 描述：关系列的名称，对应服务器上的同名relation，不能为空<br>"
				+ "value：类型：JSON对象 默认值：无 描述：插入的键值对，与服务器上class中键值对应，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"ret：类型：JSON对象     描述：成功信息<br>"
				+"err：类型：JSON对象     描述：错误信息<br>"
				+"<h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "count", showCode(location, "count", true, "class:'',", "id:'',", "column:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"查找对象某关系列下对应的数据总条数<br>"
				+ "<h3>参数</h3>"
				+ "class：类型：字符串 默认值：无 描述：对象的名称，对应服务器上的同名class，不能为空<br>"
				+ "id：类型：字符串 默认值：无 描述：被查找对象ID，不能为空<br>"
				+ "column：类型：字符串 默认值：无 描述：关系列的名称，对应服务器上的同名relation，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"ret：类型：JSON对象     描述：成功信息<br>"
				+"err：类型：JSON对象     描述：错误信息<br>"
				+"<h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "findAll", showCode(location, "findAll", true, "class:'',", "id:'',", "column:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"查找对象某关系列下对应的所有数据<br>"
				+ "<h3>参数</h3>"
				+ "class：类型：字符串 默认值：无 描述：对象的名称，对应服务器上的同名class，不能为空<br>"
				+ "id：类型：字符串 默认值：无 描述：被查找对象ID，不能为空<br>"
				+ "column：类型：字符串 默认值：无 描述：关系列的名称，对应服务器上的同名relation，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"ret：类型：JSON对象     描述：成功信息<br>"
				+"err：类型：JSON对象     描述：错误信息<br>"
				+"<h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "deleteAll", showCode(location, "deleteAll", true, "class:'',", "qid:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"删除对象某关系列下对应的所有数据<br>"
				+ "<h3>参数</h3>"
				+ "class：类型：字符串 默认值：无 描述：对象的名称，对应服务器上的同名class，不能为空<br>"
				+ "id：类型：字符串 默认值：无 描述：被删除对象ID，不能为空<br>"
				+ "column：类型：字符串 默认值：无 描述：关系列的名称，对应服务器上的同名relation，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"ret：类型：JSON对象     描述：成功信息<br>"
				+"err：类型：JSON对象     描述：错误信息<br>"
				+"<h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "exist", showCode(location, "exist", true, "class:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"查询某对象/对象下某行是否存在<br>"
				+ "<h3>参数</h3>"
				+ "class：类型：字符串 默认值：无 描述：对象的名称，对应服务器上的同名class，不能为空<br>"
				+ "id：类型：字符串 默认值：无 描述：被查找数据的行ID，可为空，为空时返回对象是否存在<br>"
				+"<h3>回调函数</h3>"
				+"ret：类型：JSON对象     描述：成功信息<br>"
				+"err：类型：JSON对象     描述：错误信息<br>"
				+"<h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
	}
	
	private void createUserProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "login", showCode(location, "login", true, "username:'',", "password:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"登陆<br>"
				+ "<h3>参数</h3>"
				+ "username：类型：字符串 默认值：无 描述：用户名，不能为空<br>"
				+ "password：类型：字符串 默认值：无 描述：密码，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"ret：类型：JSON对象     描述：成功信息<br>"
				+"err：类型：JSON对象     描述：错误信息<br>"
				+"<h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "register", showCode(location, "register", true, "username:'',", "password:'',", "email:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"注册<br>"
				+ "<h3>参数</h3>"
				+ "username：类型：字符串 默认值：无 描述：用户名，不能为空<br>"
				+ "password：类型：字符串 默认值：无 描述：密码，不能为空<br>"
				+ "email：类型：字符串 默认值：无 描述：邮箱，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"ret：类型：JSON对象     描述：成功信息<br>"
				+"err：类型：JSON对象     描述：错误信息<br>"
				+"<h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "logout", showCode(location, "logout", true)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"注销登陆<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"ret：类型：JSON对象     描述：成功信息<br>"
				+"err：类型：JSON对象     描述：错误信息<br>"
				+"<h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "updatePassword", showCode(location, "updatePassword", true, "password:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"修改密码<br>"
				+ "<h3>参数</h3>"
				+ "password：类型：字符串 默认值：无 描述：密码，不能为空<br>"
				+"<h3>回调函数</h3>"
				+"ret：类型：JSON对象     描述：成功信息<br>"
				+"err：类型：JSON对象     描述：错误信息<br>"
				+"<h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
		addApicloudProposal(proposals, "exist", showCode(location, "exist", true, "class:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"查询某对象/对象下某行是否存在<br>"
				+ "<h3>参数</h3>"
				+ "class：类型：字符串 默认值：无 描述：对象的名称，对应服务器上的同名class，不能为空<br>"
				+ "id：类型：字符串 默认值：无 描述：被查找数据的行ID，可为空，为空时返回对象是否存在<br>"
				+"<h3>回调函数</h3>"
				+"ret：类型：JSON对象     描述：成功信息<br>"
				+"err：类型：JSON对象     描述：错误信息<br>"
				+"<h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "mcm API", offset);
	}
	
	private void createListViewProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true, "data:[]")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开<br>"
				+ "<h3>参数</h3>"
				+ "x：列表视图的左上角点的坐标（数字类型）默认值：0<br>"
				+ "y：列表视图的左上角点的坐标（数字类型） 默认值：紧贴导航条位置<br>"
				+ "w：列表视图的宽（数字类型）默认值：当前屏幕宽度<br>"
				+ "h：列表视图的高（数字类型）默认值：全屏<br>"
				+ "leftBtn：往右滑动cell露出的按钮组成的数字，可为空，为空时则表示cell不可向右滑动（数组类型）<br>"
				+ " 默认值：无，内部字段：[{color:按钮背景色    title:按钮名字    selecteColor:按钮选中时候的颜色值}]<br>"
				+ "leftBg：往右滑动cell露出的视图的背景色，可为空（字符串类型） 默认值：#5cacee<br>"
				+ "rightBtn：往左滑动cell露出的按钮组成的数字，可为空，为空时则表示cell不可向左滑动（数组类型）<br>"
				+ " 默认值：无，内部字段：[{color:按钮背景色    title:按钮名字    selecteColor:按钮选中时候的颜色值}]<br>"
				+ "rightBg：往左滑动cell露出的视图的背景色（字符串类型） 默认值：#6c7b8b<br>"
				+ "borderColor：每个cell之间分割线的颜色（字符串类型） 默认值：#696969<br>"
				+ "cellBgColor：cell的背景色，可为空（字符串类型） 默认值：#AFEEEE<br>"
				+ "cellSelectColor：选中cell时的颜色（字符串类型） 默认值：#F5F5F5<br>"
				+ "cellHeight：每个cell的高度（数字类型）默认值：55<br>"
				+ "imgHeight：头像的高（数字类型）默认值：自适应cell的高度<br>"
				+ "imgWidth：头像的宽（数字类型）默认值：自适应cell的高度<br>"
				+ "data：数据库名称，不能为空（数组类型）默认值：无<br>"
				+"内部字段：[{img:cell的头像，一个网络路径，此图片会被缓存到本地，可为空<br>"
				+"placeHoldImg:cell的头像，一个本地路径，加载网络图片时show在界面上的图 <br>"
				+"title:cell的标题，若subtitle为空时，title上下居中位置subTitle:cell的子标题，可为空 <br>"
				+"titleLocation:标题在水平线上的位置  titleSize :标题字体的大小titleColor:标题字体的颜色值<br>"
				+"subTitleLocation :子标题在水平线上的位置 subTitleSize :子标题字体的大小subTitleColor:子标题字体的颜色值}]<br>"	
				+ "fixedOn：将此模块视图添加到指定窗口的名字，可为空（字符串类型）默认值：当前主窗口的名字<br>"
				+"<h3>回调函数</h3>"
				+"function({ index:点击某个cell或其内部按钮返回其下标 clickType:点击类型，<br>"
				+"0-cell；1-右边按钮；2-左边的按钮 btnIndex: 点击按钮时返回其下标}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "listView API", offset);
		addApicloudProposal(proposals, "reloadData", showCode(location, "reloadData", false, "data:[]")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"刷新列表数据<br>"
				+ "<h3>参数</h3>"
				+ "data：数据库名称，不能为空（数组类型） 默认值：无<br>"
				+ "内部字段：[{placeHoldImg:占位图(本地图片的地址)，不可为空<br>"
				 +"img:cell的头像，一个网络路径，此图片会被缓存到本地<br>"
				+ "title: cell的标题    subtitle: cell的子标题 }]<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "listView API", offset);
		addApicloudProposal(proposals, "appendData", showCode(location, "appendData", false, "data:[]")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"往列表添加数据<br>"
				+ "<h3>参数</h3>"
				+ "data：数据库名称，不能为空（数组类型） 默认值：无<br>"
				+ "内部字段：[{placeHoldImg:占位图(本地图片的地址)，不可为空<br>"
				 +"img:cell的头像，一个网络路径，此图片会被缓存到本地<br>"
				+ "title: cell的标题    subtitle: cell的子标题 }]<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "listView API", offset);
		addApicloudProposal(proposals, "setRefreshHeader", showCode(location, "setRefreshHeader", true, "loadingImg:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置下拉刷新样式<br>"
				+ "<h3>参数</h3>"
				+ "loadingImg：下拉刷新的小箭头本地图片的路径，不能为空（字符串类型） 默认值：无<br>"
				+ "bgColor：下拉刷新视图的背景色（字符串类型） 默认值：#f5f5f5<br>"
				+ "textColor：提示文字颜色（字符串类型） 默认值：#8e8e8e<br>"
				+ "textDown：提示文字（字符串类型） 默认值：下拉可以刷新…<br>"
				+ "textUp：提示文字（字符串类型） 默认值：松开可以刷新…<br>"
				+ "showTime：是否显示刷新时间（布尔类型） 默认值：true<br>"
				+"<h3>回调函数</h3>"
				+"返回触发刷新事件"
				+"<br><h3>可用性</h3>"
				+"IOS系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "listView API", offset);
		addApicloudProposal(proposals, "setRefreshFooter", showCode(location, "setRefreshFooter", true, "loadingImg:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置上拉加载更多样式<br>"
				+ "<h3>参数</h3>"
				+ "loadingImg：上拉加载更多的小箭头本地图片的路径，不能为空（字符串类型） 默认值：无<br>"
				+ "bgColor：上拉加载视图的背景色（字符串类型） 默认值：#f5f5f5<br>"
				+ "textColor：提示文字颜色（字符串类型） 默认值：#8e8e8e<br>"
				+ "textDown：提示文字（字符串类型） 默认值：上拉可以加载更多…<br>"
				+ "textUp：提示文字（字符串类型） 默认值：松开开始加载…<br>"
				+ "showTime：是否显示加载时间（布尔类型） 默认值：true<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "listView API", offset);
		addApicloudProposal(proposals, "close", "close();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭listView<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "listView API", offset);
		
	}
	private void createLineChartProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true, "x:,", "y:,", "width:,", "height:,", "yAxis:{},"
				, "xAxis:{},", "lines:[],", "backGroundColor:'',", "coorLineColor:'',", "markColor:'',", "id:")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开K线图视图<br>"
				+ "<h3>参数</h3>"
				+ "x：视图左上角点的x坐标（数字类型） 不可为空 默认值：无<br>"
				+ "y：视图左上角点的y坐标（数字类型） 不可为空 默认值：无<br>"
				+ "width：视图宽（数字类型）不可为空 默认值：无<br>"
				+ "height：视图高（数字类型）不可为空 默认值：无<br>"
				+ "yAxis：K线图坐标系y轴配置参数（JSON对象）不可为空 默认值：无<br>"
				+"内部字段 { max:y轴最大值  step :Y轴步幅}<br>"
				+ "xAxis：K线图坐标系x轴配置参数（JSON对象） 不可为空 默认值：无<br>"
				+"内部字段 { indexs:[一月，二月，三月] x轴标注（本字段是个数组对象）screenXcount:每屏显示纵轴个数}<br>"
				+ "lines：K线配置参数（数组对象） 不可为空 默认值：无<br>"
				+"内部字段 {  color:十六进制颜色字符串  datas: [ 200,400,-300,500,-400] K线的数据组成的数组  id:k线的id}<br>"
				+ "backGroundColor：K线图背景颜色，十六进制值字符串（字符串类型）不可为空  默认值：无<br>"
				+ "coorLineColor：K线图折线的颜色，十六进制值字符串（字符串类型） 不可为空 默认值：无<br>"
				+ "markColor：K线图xy轴标记字体颜色，十六进制值字符串（字符串类型） 不可为空 默认值：无<br>"
				+ "id：K线图id，根据此id关闭该视图（数字类型）不可为空  默认值：无<br>"
				+"fixedOn：要把该视图添加到某视图的名字，可为空（字符串类型）默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({  status:状态值 index:点击的k线的结点的下标，数字  id：点击的K线的id ，数字}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "lineChart API", offset);
		addApicloudProposal(proposals, "close", showCode(location, "close", true, "id:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭K线图<br>"
				+ "<h3>参数</h3>"
				+ "id：K线图id（数字类型） 不可为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "lineChart API", offset);
		
	}
	private void createDownloadManagerProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "openManagerView", showCode(location, "openManagerView", true, "title:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开下载管理界面<br>"
				+ "<h3>参数</h3>"
				+ "title：显示在下载界面顶部栏的标题（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({id:’’,下载记录id	mimeType:’’,文件类型 savePath:’’,下载文件的本地存储路径 uncompressPath:’压缩文件解压缩后路径}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "downloadManager API", offset);
		addApicloudProposal(proposals, "closeManagerView", "closeManagerView();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭下载管理界面<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "downloadManager API", offset);
		addApicloudProposal(proposals, "enqueue", showCode(location, "enqueue", true, "url:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"开始一个下载<br>"
				+ "<h3>参数</h3>"
				+ "url：资源地址（字符串类型），不能为空 默认值：无<br>"
				+ "savePath：存储路径，为空时使用自动创建的路径（字符串类型）默认值：无<br>"
				+ "cache：是否使用缓存（布尔类型） 默认值：true<br>"
				+ "allowResume：是否开启断点续传，需要服务器支持（布尔类型） 默认值：false<br>"
				+ "uncompress：下载完成时是否解压缩文件（布尔类型） 默认值：false<br>"
				+ "headers：请求头（JSON类型）默认值：无<br>"
				+ "mimeType：被下载目标的类型（*/*）（字符串类型）默认值：通过响应头获取<br>"
				+ "title：展示在managerView列表中的文件标题（字符串类型）默认值：通过资源地址截取<br>"
				+ "iconPath：该项下载显示在managerView中对应的图标（字符串类型）默认值：无<br>"
				+ "networkTypes：允许自动下载的网络环境，参考网络环境常量（字符串类型）默认值：all<br>"
				+"<h3>回调函数</h3>"
				+"function({id: 新下载记录的id}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "downloadManager API", offset);
		addApicloudProposal(proposals, "pause", showCode(location, "pause", true, "id:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"暂停下载<br>"
				+ "<h3>参数</h3>"
				+ "id：下载记录的id（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作状态；msg:操作失败时的描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "downloadManager API", offset);
		addApicloudProposal(proposals, "resume", showCode(location, "resume", true, "id:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"继续下载<br>"
				+ "<h3>参数</h3>"
				+ "id：下载记录的id（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作状态；msg:操作失败时的描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "downloadManager API", offset);
		addApicloudProposal(proposals, "remove", showCode(location, "remove", true, "ids:[]")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"删除下载<br>"
				+ "<h3>参数</h3>"
				+ "ids：下载记录的id数组（数组类型），不能为空 默认值：无<br>"
				+ "deleteFiles：是否同时删除本地文件（布尔类型） 默认值：false<br>"
				+"<h3>回调函数</h3>"
				+"function({number:成功完成删除操作的总数，没有则返回-1}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "downloadManager API", offset);
		addApicloudProposal(proposals, "query", showCode(location, "query", true)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"查询下载状态<br>"
				+ "<h3>参数</h3>"
				+ "ids：下载记录的id数组（数组类型）默认值：无<br>"
				+ "status：下载状态（数字类型）默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({data:[{id:下载id;status:下载状态;url:源地址;savePath:下载文件本地存储路径<br>"
				+"title:下载文件标题;totalSize:文件总大小，单位byte;finishSize:已完成下载大小，单位byte<br>"
				+"mimeType:文件类型;iconPath:图片路径;reason:当下载发生错误时，错误的描述。详见下载错误常量表}]}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "downloadManager API", offset);
		addApicloudProposal(proposals, "openDownloadedFile", showCode(location, "openDownloadedFile", true, "id:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开下载文件<br>"
				+ "<h3>参数</h3>"
				+ "id：下载记录的id（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作状态;msg:操作失败时的描述}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "downloadManager API", offset);
		
	}
	private void createPersonalCenterProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true, "imgPath:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开个人中心<br>"
				+ "<h3>参数</h3>"
				+ "y：个人中心视图上边距屏幕位置（数字类型） 默认值：紧贴导航条下边缘<br>"
				+ "height：视图的高（数字类型） 默认值：220<br>"
				+ "imgPath：头像图片的路径（如果为网络路径,图片会被缓存到本地），支持http，https，widget，file，fs协议（字符串类型）不可为空 默认值：无<br>"
				+ "placeHoldImg：头像占位图片的路径，支持widget，file，fs协议，可为空。当imgPath为网络路径时，不可为空（字符串类型） 默认值：无<br>"
				+ "username：用户名（字符串类型）  默认值：username<br>"
				+ "count：积分（字符串类型）  默认值：0<br>"
				+ "userColor ：用户名和积分字体颜色（字符串类型） 默认值：#FFFFFF<br>"
				+ "showLeftBtn：是否显示左上角修改按钮（字符串类型） 默认值：true<br>"
				+ "showRightBtn：是否显示右上角设置按钮（字符串类型） 默认值：true<br>"
				+ "modButton ：修改按钮参数，可为空，若为空则不显示修改按钮（JSON类型） 默认值：无<br>"
				+"内部字段：{bgImg:字符串，按钮背景图片，不可为空lightImg:字符串，按钮点击效果图路径，可为空}<br>"
				+ "btnArray ：下边按钮的参数信息，可为空，为空则不显示下边按钮（数组类型） 默认值：无<br>"
				+"内部字段：[{bgImg:按钮背景图片，字符串，不可为空 selectedImg:按钮点击图片，字符串，可为空<br>"
				+"lightImg:按钮选中后图片，字符串，可为空 title:按钮上的标题，字符串，可为空count:按钮上的数据，字符串，可为空<br>"
				+"titleColor:按钮上的标题颜色，字符串，可为空，默认#AAAAAA titleLightColor:按钮选中标题的颜色，字符串，可为空，默认#A4D3EE<br>"
				+"countColor:按钮上数字颜色，字符串，可为空，默认#FFFFFF countLightColor:按钮上数字选中颜色，字符串，可为空，默认#A4D3EE }]<br>"
				+ "fixedOn：将此模块视图添加到指定窗口的名字，可为空（字符串类型）默认值：当前主窗口的名字<br>"
				+ "count：是否将模块视图固定在目标视图滚动框（scrollView）内（布尔类型）  默认值：false<br>"
				+"<h3>回调函数</h3>"
				+"function({ click：点击的按钮的index---- 对应下边按钮数字的下标，如果存在修改按钮，<br>"
				+"则其index是按钮数组总下标加一，若存在左上角按钮，则其index是按钮数组总下标加二，<br>"
				+"若存在右上角按钮，则其inidex是按钮数组总下标加三}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "personalCenter API", offset);
		addApicloudProposal(proposals, "updateValue", "updateValue();"
				, JS_FUNCTION, "<h3>描述</h3>"
		+"刷新个人中心显示数据<br>"
		+ "<h3>参数</h3>"
		+ "username：用户名，可为空，若为空，则显示之前的数据（字符串类型）  默认值：无<br>"
		+ "count：积分，可为空，若为空，则显示之前的数据（字符串类型） 默认值：无<br>"
		+ "imgPath：头像地址，可为空，若为空则刷新占位图的图片，若placehodlmg也为空则不刷新头像（字符串类型）默认值：无<br>"
		+ "placeHoldImg：头像占位图地址，可为空，若为空则不刷新头像（字符串类型） 默认值：无<br>"
		+ "btnArray ：下边按钮显示的数据，可为空，若为空则不刷新（数组类型） 默认值：无<br>"
		+"内部字段：[{ count:’123’字符串，按钮上的数据大小，不可为空}]<br>"
		+"<h3>回调函数</h3>"
		+"无"
		+"<br><h3>可用性</h3>"
		+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
		, getActiveUserAgentIds(), "personalCenter API", offset);
		addApicloudProposal(proposals, "close", "close();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭个人中心<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "personalCenter API", offset);
//		addProposal(proposals, "setLeft", JS_FUNCTION, "<h3>描述</h3>"
//				+"设置左边修改按钮是否显示<br>"
//				+ "<h3>参数</h3>"
//				+ "display：是否显示左上角按钮（布尔类型） ，不能为空 默认值：不显示<br>"
//				+"<h3>回调函数</h3>"
//				+"无"
//				+"<br><h3>可用性</h3>"
//				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
//				, getActiveUserAgentIds(), "personalCenter API", offset);
//		addProposal(proposals, "setRight", JS_FUNCTION, "<h3>描述</h3>"
//				+"设置右边修改按钮是否显示<br>"
//				+ "<h3>参数</h3>"
//				+ "display：是否显示右上角按钮（布尔类型） ，不能为空 默认值：不显示<br>"
//				+"<h3>回调函数</h3>"
//				+"无"
//				+"<br><h3>可用性</h3>"
//				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
//				, getActiveUserAgentIds(), "personalCenter API", offset);
//		addProposal(proposals, "reloadData", JS_FUNCTION, "<h3>描述</h3>"
//				+"刷新用户名和积分数据<br>"
//				+ "<h3>参数</h3>"
//				+ "username：用户名（字符串类型） ，不能为空 默认值：无<br>"
//				+ "count：积分（字符串类型），不能为空  默认值：无<br>"
//				+"<h3>回调函数</h3>"
//				+"无"
//				+"<br><h3>可用性</h3>"
//				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
//				, getActiveUserAgentIds(), "personalCenter API", offset);
//		addProposal(proposals, "setCollect", JS_FUNCTION, "<h3>描述</h3>"
//				+"刷新收藏数据<br>"
//				+ "<h3>参数</h3>"
//				+ "collect：收藏的数据户名（字符串类型） ，不能为空 默认值：无<br>"
//				+"<h3>回调函数</h3>"
//				+"无"
//				+"<br><h3>可用性</h3>"
//				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
//				, getActiveUserAgentIds(), "personalCenter API", offset);
//		addProposal(proposals, "setBrowse", JS_FUNCTION, "<h3>描述</h3>"
//				+"刷新浏览数据<br>"
//				+ "<h3>参数</h3>"
//				+ "browse：浏览的数据（字符串类型） ，不能为空 默认值：无<br>"
//				+"<h3>回调函数</h3>"
//				+"无"
//				+"<br><h3>可用性</h3>"
//				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
//				, getActiveUserAgentIds(), "personalCenter API", offset);
//		addProposal(proposals, "setDownLoad", JS_FUNCTION, "<h3>描述</h3>"
//				+"刷新下载数据<br>"
//				+ "<h3>参数</h3>"
//				+ "downLoad：下载的数据（字符串类型） ，不能为空 默认值：无<br>"
//				+"<h3>回调函数</h3>"
//				+"无"
//				+"<br><h3>可用性</h3>"
//				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
//				, getActiveUserAgentIds(), "personalCenter API", offset);
//		addProposal(proposals, "setActivity", JS_FUNCTION, "<h3>描述</h3>"
//				+"刷新活动数据<br>"
//				+ "<h3>参数</h3>"
//				+ "activity：活动的数据（字符串类型） ，不能为空 默认值：无<br>"
//				+"<h3>回调函数</h3>"
//				+"无"
//				+"<br><h3>可用性</h3>"
//				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
//				, getActiveUserAgentIds(), "personalCenter API", offset);
		
	}
	private void createSpeechRecognizerProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "record", showCode(location, "record", true)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"识别语音返回文字<br>"
				+ "<h3>参数</h3>"
				+ "vadbos：前断点时间（静音时间，即用户多长时间不说话做超时处理），范围是0-10000单位ms（数字类型）  默认值：5000<br>"
				+ "vadeos：后断点时间（静音时间，即用户多长时间不说话做超时处理），单位ms，范围是0-10000（数字类型） 默认值：5000<br>"
				+ "rate：采样率（支持16000，8000）（数字类型） ，不能为空 默认值：16000<br>"
				+ "asrptt：返回的语句是否有标点符号，0-无，1-有（数字类型） 默认值：1<br>"
				+ "audioPath：录制的音频文件保存路径（如fs://123.mp3,一定要价后缀名），若为空则不保存。（字符串类型）  默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态值;wordStr:识别语音后的文字},{msg:错误码}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "speechRecognizer API", offset);
		addApicloudProposal(proposals, "stopRecord", "stopRecord();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"停止录音<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "speechRecognizer API", offset);
		addApicloudProposal(proposals, "cancel", "cancel();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"取消语音识别<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "speechRecognizer API", offset);
		addApicloudProposal(proposals, "read", showCode(location, "read", true, "readStr:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"用语音读取文字信息<br>"
				+ "<h3>参数</h3>"
				+ "readStr：要读取的文字信息（字符串类型），不能为空 默认值：无<br>"
				+ "speed：朗读的语速，范围是0-100（数字类型）默认值：60<br>"
				+ "volume：朗读的声音大小，范围是0-100（数字类型）默认值：60<br>"
				+ "voice：朗读人0-xiaoli；1-xiaoyu（数字类型）默认值：0<br>"
				+ "rate：采样率(支持16000，8000)（数字类型）默认值：16000<br>"
				+ "audioPath：朗读的音频保存路径（如fs://123.mp3,一定要价后缀名），若为空则不保存（字符串类型）默认值：16000<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态值},{msg:错误码}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "speechRecognizer API", offset);
		
	}
	private void createPieChartProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", false, "id:,"
				, "x:,", "y:,", "radius:,", "title:'',", "subTitle:'',", "parts:[]")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开饼图<br>"
				+ "<h3>参数</h3>"
				+ "id：视图的id，根据此id删除此视图（数字类型）不能为空 默认值：无<br>"
				+ "x：圆心坐标（数字类型）不能为空 默认值：无<br>"
				+ "y：圆心坐标（数字类型）不能为空 默认值：无<br>"
				+ "radius：圆心半径（数字类型）不能为空 默认值：无<br>"
				+ "title：饼图中间的说明文字（字符串类型）不能为空默认值：无<br>"
				+ "subTitle：饼图子说明文字（字符串类型）不能为空 默认值：无<br>"
				+ "parts：模块字典对象组成的数组其内部字段是：value:大小,color:颜色值（数组）不能为空 默认值：无<br>"
				+ "fixedOn：将此模块视图添加到指定窗口的名字，可为空（字符串类型）默认值：当前主窗口的名字<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "pieChart API", offset);
		addApicloudProposal(proposals, "close", showCode(location, "close", false, "id:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭饼图<br>"
				+ "<h3>参数</h3>"
				+ "id：要关闭的饼图的id（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "pieChart API", offset);
		
	}
	private void createPdfReaderProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", false, "path:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开一个pdf格式的文档<br>"
				+ "<h3>参数</h3>"
				+ "path：文档的路径（支持的路径协议：widget://,fs://，file://）（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "pdfReader API", offset);
		
	}
	private void createInputFieldProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true, "sendImg:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开输入框<br>"
				+ "<h3>参数</h3>"
				+ "bgColor：输入视图背景色的十六进制值（字符串类型）默认值：灰色<br>"
				+ "lineColor：输入框视图最上边的分割线色的十六进制值（字符串类型）默认值：黑色<br>"
				+ "borderColor：输入框边框色的十六进制值（字符串类型）默认值：红色<br>"
				+ "fileBgColor：输入框背景色的十六进制值（字符串类型）默认值：白色<br>"
				+ "sendImg：发送按钮的背景图片（字符串类型）不能为空 默认值：无<br>"
				+ "sendImgHighlight：发送按钮的背景图片（字符串类型）不能为空默认值：无<br>"
				+ "fixedOn：将此模块视图添加到指定窗口的名字，可为空（字符串类型）默认值：当前主窗口的名字<br>"
				+"<h3>回调函数</h3>"
				+"function({msg:返回输入的文字}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "inputField API", offset);
		addApicloudProposal(proposals, "close", "close();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭输入框<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "inputField API", offset);
		
	}
	private void createDocReaderProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", false, "path:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开文件<br>"
				+ "<h3>参数</h3>"
				+ "path：文档的路径（支持的路径协议：widget://,fs://，file://。字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "docReader API", offset);
		
	}
	private void createContactProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "openContact", showFunction(location, "openContact")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"从通讯录读取一个联系人<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态值;id:联系人id;name:联系人姓名,由通讯录里该联系人的姓和名组成 <br>" +
				"phones:电话号码组成的字典对象;email:邮箱;company:公司;title:职位;address:地址（是一个数组对象）;<br>" +
				"note:备注;groupId:联系人在通讯录中所属分组的id（为空时表示未分组）;<br>" +
				"groupName:所在分组的名字（为空时表示未分组）phones:{kye-value 需要遍历该对象的属性来读取电话号码值}<br>" +
				"address: {City:城市;Country:国家;CountryCode:国家缩写;State:省份;Street:街道; ZIP:邮编}}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "contact API", offset);
		addApicloudProposal(proposals, "addContact", showCode(location, "addContact", true)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"往通讯录添加一个联系人<br>"
				+ "<h3>参数</h3>"
				+ "lastname：联系人姓，可为空（字符串类型） 默认值：若姓名全部为空则默认值为未命名<br>"
				+ "firstname：联系人名，可为空（字符串类型） 默认值：若姓名全部为空则默认值为未命名<br>"
				+ "groupId：分组id，可为空，为空是表示未分组（数字类型）， 默认值：若姓名全部为空则默认值为未命名<br>"
				+ "phone：联系人电话字典对象组成的数组对象，可为空包括每个对象有两个属性："
				+"<br>label（标签）和phone（电话号码），两者都是字符串类型（json数组对象）， 默认值：无<br>"
				+ "email：联系人邮箱，可为空（字符串类型）默认值：无<br>"
				+ "title：联系人职位，可为空（字符串类型） 默认值：无<br>"
				+ "address：联系人地址组成的字典对象，可为空（json类型） 默认值：无<br>"
				+ "note：联系人备注，可为空（字符串类型） 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态值}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "contact API", offset);
		addApicloudProposal(proposals, "deleteContact", showCode(location, "deleteContact", true, "ids:[]")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"从通讯录删除多个联系人<br>"
				+ "<h3>参数</h3>"
				+ "ids：一个数组对象，要删除的联系人的名字组成的数组<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态值}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "contact API", offset);
		addApicloudProposal(proposals, "updateContact", showCode(location, "updateContact", true, "id:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"修改通讯录里的一个联系人<br>"
				+ "<h3>参数</h3>"
				+ "id：要修改的联系人的id（字符串类型），不能为空 默认值：无<br>"
				+ "groupId：要修改的联系人的分组id，若为空则不移动（数字类型），默认值：无<br>"
				+ "lastname：联系人的姓，若为空则不修改此属性（字符串类型），默认值：无<br>"
				+ "firstname：联系人的名，若为空则不修改此属性（字符串类型），默认值：无<br>"
				+ "phone：联系人电话组成的key-value字典对象，若为空则不修改此属性（json类型），默认值：无<br>"
				+ "email：联系人邮箱，若为空则不修改此属性（字符串类型），默认值：无<br>"
				+ "company：联系人公司，若为空则不修改此属性（字符串类型），默认值：无<br>"
				+ "title：联系人职位，若为空则不修改此属性（字符串类型），默认值：无<br>"
				+ "address：联系人地址组成的JSON对象，若为空则不修改此属性（json类型），默认值：无<br>"
				+ "note：联系人备注，若为空则不修改此属性（字符串类型），默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态值}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "contact API", offset);
		addApicloudProposal(proposals, "queryContact", showCode(location, "queryContact", true)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"从通讯录查找联系人<br>"
				+ "<h3>参数</h3>"
				+ "ids：要查找的联系人的id组成的数组，若为空则返回全部联系人信息（数组类型） 默认值；无 <br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态值;id:联系人id;name:联系人姓名,由通讯录里该联系人的姓和名组成 <br>" +
				"phones:电话号码组成的字典对象;email:邮箱;company:公司;title:职位;address:地址（是一个数组对象）;<br>" +
				"note:备注;groupId:联系人在通讯录中所属分组的id（为空时表示未分组）;<br>" +
				"groupName:所在分组的名字（为空时表示未分组）phones:{kye-value 需要遍历该对象的属性来读取电话号码值}<br>" +
				"address: {City:城市;Country:国家;CountryCode:国家缩写;State:省份;Street:街道; ZIP:邮编}}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "contact API", offset);
		addApicloudProposal(proposals, "addGroup", showCode(location, "addGroup", true, "name:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"创建分组<br>"
				+ "<h3>参数</h3>"
				+ "name：分组名（字符串类型） 默认值；无 <br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态值; id:创建成功返回分组的id }){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "contact API", offset);
		addApicloudProposal(proposals, "queryContactByKey", showCode(location, "queryContactByKey", true, "key:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"根据关键字从通讯录查找一个联系人<br>"
				+ "<h3>参数</h3>"
				+ "key：要查询的关键字（字符串类型） 默认值；无 <br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态值;id:联系人id;name:联系人姓名,由通讯录里该联系人的姓和名组成 <br>" +
				"phone:电话号码组成的字典对象;email:邮箱;company:公司;title:职位;address:地址（是一个数组对象）;<br>" +
				"note:备注;groupId:联系人在通讯录中所属分组的id（为空时表示未分组）;<br>" +
				"groupName:所在分组的名字（为空时表示未分组）phones:{kye-value 需要遍历该对象的属性来读取电话号码值}<br>" +
				"address: {City:城市;Country:国家;CountryCode:国家缩写;State:省份;Street:街道; ZIP:邮编}}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "contact API", offset);
		addApicloudProposal(proposals, "queryGroups", showFunction(location, "queryGroups")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"获取所有分组信息<br>"
				+ "<h3>参数</h3>"
				+ "无 <br>"
				+"<h3>回调函数</h3>"
				+"function({ groups: {name:分组明; id:创建成功返回分组的id}}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "contact API", offset);
		addApicloudProposal(proposals, "queryContactByGroupId", showCode(location, "queryContactByGroupId", true)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"获取所有分组信息<br>"
				+ "<h3>参数</h3>"
				+ "id: 要查找的分组的id，若为空则返回全部联系人信息 （字符串类型） 默认值；无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态值;id:联系人id;name:联系人姓名,由通讯录里该联系人的姓和名组成 <br>" +
				"phone:电话号码组成的字典对象;email:邮箱;company:公司;title:职位;address:地址（是一个数组对象）;<br>" +
				"note:备注;groupId:联系人在通讯录中所属分组的id（为空时表示未分组）;<br>" +
				"groupName:所在分组的名字（为空时表示未分组）phones:{kye-value 需要遍历该对象的属性来读取电话号码值}<br>" +
				"address: {City:城市;Country:国家;CountryCode:国家缩写;State:省份;Street:街道; ZIP:邮编}}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "contact API", offset);
		addApicloudProposal(proposals, "updateGroupName", showCode(location, "updateGroupName", true, "name:'',", "id:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"修改分组名（目前ios暂不支持此接口，可通过删除分组，然后在创建来实现此功能）<br>"
				+ "<h3>参数</h3>"
				+ "name: 要修改的分组名（字符串类型） 不可为空 默认值；无<br>"
				+ "id: 要修改的分组id（数字类型） 不可为空 默认值；无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态值}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "contact API", offset);
		addApicloudProposal(proposals, "deleteGroup", showCode(location, "deleteGroup", true, "id:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"删除分组<br>"
				+ "<h3>参数</h3>"
				+ "id: 要删除的分组id （数字类型）不可为空 默认值；无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态值}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "contact API", offset);
		
	}
	private void createCardReaderProposal(Set<ICompletionProposal> proposals, int offset,int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showFunction(location, "open")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开信用卡识别器<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"function({status:操作成功状态值;cardNum:卡号;expiryMonth:过期日期的月;<br>" 
				+"expiryYear:过期日期的年;cvv:cvv},{ msg:报错信息}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "cardReader API", offset);
		
	}
	private void createCalendarProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true,  "x:'',", "y:'',", "width:,", "height:")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开日历<br>"
				+ "<h3>参数</h3>"
				+ "x：日历视图左上角点的x坐标（数字类型），不能为空 默认值：无<br>"
				+ "y：日历视图左上角点的y坐标（数字类型），不能为空 默认值：无<br>"
				+ "width：日历视图宽（数字类型），不能为空 默认值：无<br>"
				+ "height：日历视图高（数字类型），不能为空 默认值：无<br>"
				+ "specialDate：需要标记的特殊日期组成的数组（数字类型） 默认值：无<br>"
				+ "fixedOn：将此模块视图添加到指定窗口的名字，可为空（字符串类型）默认值：当前主窗口的名字<br>"
				+"<h3>回调函数</h3>"
				+"function({ {date:返回用户点击的日期格式为****-**-**}}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "Calendar API", offset);
		addProposal(proposals, "close", JS_FUNCTION, "<h3>描述</h3>"
				+"关闭日历<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "Calendar API", offset);
	}
	private void createAliPayProposal(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "pay", showCode(location, "pay", true, "subject:'',", "body:'',", "amount:'',", "tradeNo:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"调用支付宝客户端支付<br>"
				+ "<h3>参数</h3>"
				+ "subject：交易商品名（字符串类型），不能为空 默认值：无<br>"
				+ "body：交易商品的简介（字符串类型），不能为空 默认值：无<br>"
				+ "amount：交易商品的价钱（单位为元，精确到分如：10.29元 字符串类型），不能为空 默认值：无<br>"
				+ "tradeNo：交易订单编号（由商家按自己的规则生成 字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({{msg:支付情况返回的信息, code:支付情况的代码},{}}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "aliPay API", offset);
		addApicloudProposal(proposals, "config", showCode(location, "config", true, "partner:'',", "seller:'',", "rsaPriKey:'',"
				, "rsaPuKey:'',", "notifyURL:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"配置支付宝信息<br>"
				+ "<h3>参数</h3>"
				+ "partner：商户id（字符串类型），不能为空 默认值：无<br>"
				+ "seller：商户账户id（字符串类型），不能为空 默认值：无<br>"
				+ "rsaPriKey：商户私钥（字符串类型），不能为空 默认值：无<br>"
				+ "rsaPuKey：支付宝公钥（字符串类型），不能为空 默认值：无<br>"
				+ "notifyURL：支付完成后，支付宝会通知客户端，也会把支付结果通知给商家，此url即是商家的地址（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status: 状态码，是否初始化成功},{msg: 错误描述，字符串}){ }"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "aliPay API", offset);
	}
	
	private void createBaiDuMap(Set<ICompletionProposal> proposals, int offset, int length)
	{
		String location = getString(length);
		addApicloudProposal(proposals, "open", showCode(location, "open", true, "x:'',", "y:'',", "width:'',", "height:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"打开百度地图<br>"
				+ "<h3>参数</h3>"
				+ "x：地图视图左上角点的x坐标（数字类型），不能为空 默认值：无<br>"
				+ "y：地图视图左上角点的y坐标（数字类型），不能为空 默认值：无<br>"
				+ "width：地图视图的宽（数字类型），不能为空 默认值：无<br>"
				+ "height：地图视图的高（数字类型），不能为空 默认值：无<br>"
				+ "lon：开启地图时设置的中心点的经度（数字类型）默认值：<br>"
				+ "lat：开启地图时设置的中心点的经度（数字类型） 默认值：无<br>"
				+ "fixedOn：将此模块视图添加到指定窗口的名字，可为空（字符串类型）默认值：当前主窗口的名字<br>"
				+"<h3>回调函数</h3>"
				+"function({status: 操作成功状态值})"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "close", "close();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"关闭百度地图<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "setType", showCode(location, "setType", false, "mapType:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置百度地图类型<br>"
				+ "<h3>参数</h3>"
				+ "mapType：地图类型（详见地图类型常量，字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "startLocation", showCode(location, "startLocation", true, "accuracy:'',", "filter:1.0,", "autoStop:true")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"开始定位<br>"
				+ "<h3>参数</h3>"
				+ "accuracy：精度（详见精度常量，字符串类型），不能为空 默认值：无<br>"
				+ "filter：位置更新所需最小距离（单位米，数字类型）不能为空  默认值：1.0<br>"
				+ "autoStop：获取到位置信息后是否自动停止定位（布尔类型）不能为空 默认值：true<br>"
				+"<h3>回调函数</h3>"
				+"function({longitude:经度,latitude:纬度,timestamp:时间戳},{msg:错误描述}){}"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "stopLocation", "stopLocation();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"停止定位<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "getLocation", showFunction(location, "getLocation")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"获取位置信息<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"function({longitude:经度,latitude:纬度,timestamp:时间戳},{msg:错误描述}){}"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "getBaiduFromGoogle", showCode(location, "getBaiduFromGoogle", true, "lon:,", "lat:")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"将谷歌坐标转换为百度坐标<br>"
				+ "<h3>参数</h3>"
				+ "lon：经度（数字类型），不能为空 默认值：无<br>"
				+ "lat：纬度（数字类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({lon：经度,lat：纬度}，{msg：错误描述}){}"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "setCenter", showCode(location, "setCenter", false, "lon:,", "lat:")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置百度地图中心点<br>"
				+ "<h3>参数</h3>"
				+ "lon：经度（数字类型），不能为空 默认值：无<br>"
				+ "lat：纬度（数字类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "setZoomLevel", showCode(location, "setZoomLevel", false, "zoomLevel:")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置百度地图缩放等级<br>"
				+ "<h3>参数</h3>"
				+ "zoomLevel：地图比例尺级别，在手机上当前可使用的级别为3-18级（数字类型） 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiduMap API", offset);
		addApicloudProposal(proposals, "setZoomEnable", showCode(location, "setZoomEnable", false, "enable:true")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置百度地图是否能放大缩小<br>"
				+ "<h3>参数</h3>"
				+ "enable：捏合手势可否缩放地图（布尔类型）默认值：true<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "setScroolEnable", showCode(location, "setScroolEnable", false, "enable:true")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置百度地图是否能滚动<br>"
				+ "<h3>参数</h3>"
				+ "enable：拖动手势可否移动地图（布尔类型） 默认值：true<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "setHidden", showCode(location, "setHidden", false, "enable:")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置是否隐藏地图，此功能只是隐藏地图，不影响地图上的标注气泡等覆盖物<br>"
				+ "<h3>参数</h3>"
				+ "hidden：是否隐藏地图视图（布尔类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "ShowUserLocation", showCode(location, "ShowUserLocation", false, "isShow:true,", "trackingMode:")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置是否在地图上显示用户位置<br>"
				+ "<h3>参数</h3>"
				+ "isShow：是否显示用户位置（布尔类型）默认值：true<br>"
				+ "trackingMode：用户当前位置显示形式（字符串类型）默认值：true<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "zoomOut", "zoomOut();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"放大地图<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "zoomIn", "zoomIn();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"缩小地图<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "addAnnotations", showCode(location, "addAnnotations", true, "annoArray:[]")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"在地图上添加大头针<br>"
				+ "<h3>参数</h3>"
				+ "annoArray：添加的大头针的信息组成的数组（数组类型）默认值：无<br>"
				+ "{id：大头针的id，lon：大头针所在位置的经度，lat：大头针所在位置的维度<br>"
				+ "title：点击大头针时弹出的气泡的大标题，subtitle：点击大头针时弹出的气泡的小标题}<br>"
				+"<h3>回调函数</h3>"
				+"function({bubbleID:用户点击大头针时返回其id}){}"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "setBubbleStyle", showCode(location, "setBubbleStyle", false, "bubbleBgImg:'',", "imgPath:'',", "id:")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置大头针上气泡的类型<br>"
				+ "<h3>参数</h3>"
				+ "bubbleBgImg：气泡的背景图片路径（字符串类型）默认值：无<br>"
				+ "imgPath：自定义类型的气泡上的头像图片路径（字符串类型）默认值：无<br>"
				+ "id：所要设置的大头针的id（数字类型）默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "addMarks", showCode(location, "addMarks", true)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"在地图上添加布告牌<br>"
				+ "<h3>参数</h3>"
				+ "marks：添加的布告牌的信息组成的数组（数组类型）默认值：无<br>"
				+ "{id：布告牌的id，lon：布告牌所在位置的经度，lat：布告牌所在位置的维度<br>"
				+ "title：布告牌的大标题，subtitle：布告牌的小标题}<br>"
				+"<h3>回调函数</h3>"
				+"function({markID:用户点击布告牌时返回该布告牌的id},{code:错误码，msg:错误描述}){}"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "setMarkStyle", showCode(location, "setMarkStyle", false, "headImg:'',", "bgImg:'',", "id:")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"设置布告牌的类型<br>"
				+ "<h3>参数</h3>"
				+ "headImg：自定义类型的布告牌上的头像图片路径（字符串类型）默认值：无<br>"
				+ "bgImg：自定义的布告牌的背景图片的路径（字符串类型）默认值：无<br>"
				+ "id：所要设置的布告牌的id（数字类型）默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "removeAnnotations", showCode(location, "removeAnnotations", false, "idArray:[]")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"移除指定id的大头针或布告牌<br>"
				+ "<h3>参数</h3>"
				+ "idArray：所要移除的大头针或布告牌id（数组类型）默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "addLine", showCode(location, "addLine", false)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"在地图上添加线<br>"
				+ "<h3>参数</h3>"
				+ "style：画线的类型（JSON类型）默认值：无 包含4个参数<br>"
				+ "{id：线的id，fillColor：画线的填充色<br>"
				+ "strokeColor：画线的边框色，lineWidth：画线的宽度，默认为2.0}<br>"
				+ "pointArray：确定画线的各个点组成的数组（数组类型）默认值：无 包含2个参数<br>"
				+ "{lon：经度，lat：纬度}<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "addPolygon", showCode(location, "addPolygon", false)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"在地图上添加多边形<br>"
				+ "<h3>参数</h3>"
				+ "style：画线的类型（JSON类型）默认值：无 包含4个参数<br>"
				+ "{id：线的id，fillColor：画线的填充色<br>"
				+ "strokeColor：画线的边框色，lineWidth：画线的宽度，默认为2.0}<br>"
				+ "pointArray：确定画线的各个点组成的数组（数组类型）默认值：无 包含2个参数<br>"
				+ "{lon：经度，lat：纬度}<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "addCircle", showCode(location, "addCircle", false, "id:'',", "fillColor:'',"
				, "strokeColor:'',", "lon:,", "lat:,", "radius:")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"在地图上添加圆圈<br>"
				+ "<h3>参数</h3>"
				+ "id：线的id（字符串类型），不能为空 默认值：无<br>"
				+ "fillColor：画线的填充色（字符串类型），不能为空 默认值：无<br>"
				+ "strokeColor：画线的边框色（字符串类型），不能为空 默认值：无<br>"
				+ "lineWidth：画线的宽度（数字类型）默认值：2<br>"
				+ "lon：经度（数字类型），不能为空 默认值：无<br>"
				+ "lat：纬度（数字类型），不能为空 默认值：无<br>"
				+ "radius：圆的半径（数字类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "addOverMark", showCode(location, "addOverMark", false, "id:'',", "imgName:'',"
				, "lbLon:,", "lbLat:,", "rtLon:,", "rtLat:")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"在地图上添加图片<br>"
				+ "<h3>参数</h3>"
				+ "id：id号（字符串类型），不能为空 默认值：无<br>"
				+ "imgName：图片的名字，需要把图片放在插件资源目录下（字符串类型），不能为空 默认值：无<br>"
				+ "lbLon：左下角点的经度（数字类型），不能为空 默认值：无<br>"
				+ "lbLat：左下角点的纬度（数字类型），不能为空 默认值：无<br>"
				+ "rtLon：右上角点的经度（数字类型），不能为空 默认值：无<br>"
				+ "rtLat：右上角点的纬度（数字类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "removeOverlays", showCode(location, "removeOverlays", false)
				, JS_FUNCTION, "<h3>描述</h3>"
				+"移除指定id的覆盖物<br>"
				+ "<h3>参数</h3>"
				+ "idArray：所要移除的id['1','2']（数组类型）默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "addRoute", showCode(location, "addRoute", false, "id:'',", "type:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"添加路线<br>"
				+ "<h3>参数</h3>"
				+ "id：id号（字符串类型），不能为空 默认值：无<br>"
				+ "type：路线类型 （drive开车，transit公交，walk步行，字符串类型） 默认值：无<br>"
				+ "stPoint：起点信息（JSON对象），默认值：无 包含下面4个参数<br>"
				+ "{city：起点城市，name：起点名  -----和经纬度配合使用，有经纬度时，此参数可为空"
				+"，lon：起点经度  -----和经纬度配合使用，有起点名时，此参数可为空"
				+"，lat：起点纬度  -----和经纬度配合使用，有起点名时，此参数可为空}<br>"
				+ "enPoint：终点信息（JSON对象），默认值：无 包含下面4个参数<br>"
				+ "{city：终点城市，name：终点名  -----和经纬度配合使用，有经纬度时，此参数可为空"
				+"，lon：终点经度  -----和经纬度配合使用，有终点名时，此参数可为空"
				+"，lat：终点纬度  -----和经纬度配合使用，有终点名时，此参数可为空}<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "removeRoute", showCode(location, "removeRoute", false, "idArray:[]")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"移除指定id的路线<br>"
				+ "<h3>参数</h3>"
				+ "idArray：所要移除的id['1','2']（数组类型）默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "searchInCity", showCode(location, "searchInCity", false, "city:'',", "key:'',", "index:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"根据关键字搜索兴趣点<br>"
				+ "<h3>参数</h3>"
				+ "city：指定的城市内搜索（字符串类型），不能为空 默认值：无<br>"
				+ "key：指定的要搜索的关键字（字符串类型），不能为空 默认值：无<br>"
				+ "index：页码，如果是第一次发起搜索，填0，根据返回的结果可以去获取第n页的结果，<br>"
				+"页码从0开始（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status: 是否查找成功;totalNum:本次搜索的总结果数;curNum:当前页的结果数;<br>"
				+"totalPage:本次搜索的总页数;pageIndex:当前页的索引;resultArray: 返回搜索结果列表，是一个数组}<br>"
				+",resultArray：{lon:当前内容的经度;lat:当前内容的纬度;name:名称;uid:id;add:地址;city:所在城市;<br>"
				+"phone:电话号码;postCode:邮编;poiType:POI类型，0:普通点 1:公交站 2:公交线路 3:地铁站 4:地铁线路}<br>"
				+"{msg: 返回报错信息})"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "searchNearBy", showCode(location, "searchNearBy", false, "key:'',", "lon:'',", "lat:'',", "radius:'',", "pageIndex:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"根据单个关键字在指定圆圈区域内搜索兴趣点<br>"
				+ "<h3>参数</h3>"
				+ "key：指定的要搜索的关键字（字符串类型），不能为空 默认值：无<br>"
				+ "lon：区域中心点经度（字符串类型），不能为空 默认值：无<br>"
				+ "lat：指定的区域中心点纬度（字符串类型），不能为空 默认值：无<br>"
				+ "radius：指定的区域半径（字符串类型），不能为空 默认值：无<br>"
				+ "pageIndex：页码，如果是第一次发起搜索，填0，根据返回的结果<br>" 
				+"可以去获取第n页的结果，页码从0开始（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status: 是否查找成功;totalNum:本次搜索的总结果数;curNum:当前页的结果数;<br>"
				+"totalPage:本次搜索的总页数;pageIndex:当前页的索引;resultArray: 返回搜索结果列表，是一个数组}<br>"
				+",resultArray：数组类型{lon:当前内容的经度;lat:当前内容的纬度;name:名称;uid:id;add:地址;city:所在城市;<br>"
				+"phone:电话号码;postCode:邮编;poiType:POI类型，0:普通点 1:公交站 2:公交线路 3:地铁站 4:地铁线路}<br>"
				+"{msg: 返回报错信息})"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
//		addProposal(proposals, "multiSearchNearBy", JS_FUNCTION, "<h3>描述</h3>"
//				+"根据多个关键字在指定圆圈区域内搜索兴趣点<br>"
//				+ "<h3>参数</h3>"
//				+ "keys：指定的要搜索的关键字列表（数组类型），不能为空 默认值：无<br>"
//				+ "lon：区域中心点经度（字符串类型），不能为空 默认值：无<br>"
//				+ "lat：指定的区域中心点纬度（字符串类型），不能为空 默认值：无<br>"
//				+ "radius：指定的区域半径（字符串类型），不能为空 默认值：无<br>"
//				+ "pageIndex：页码，如果是第一次发起搜索，填0，根据返回的结果<br>" 
//				+"可以去获取第n页的结果，页码从0开始（字符串类型），不能为空 默认值：无<br>"
//				+"<h3>回调函数</h3>"
//				+"function({status: 是否查找成功;totalNum:本次搜索的总结果数;curNum:当前页的结果数;<br>"
//				+"totalPage:本次搜索的总页数;pageIndex:当前页的索引;resultArray: 返回搜索结果列表，是一个数组}<br>"
//				+",resultArray：{lon:当前内容的经度;lat:当前内容的纬度;name:名称;uid:id;add:地址;city:所在城市;<br>"
//				+"phone:电话号码;postCode:邮编;poiType:POI类型，0:普通点 1:公交站 2:公交线路 3:地铁站 4:地铁线路}<br>"
//				+"{msg: 返回报错信息})"
//				+"<br><h3>可用性</h3>"
//				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
//				, getActiveUserAgentIds(), "baiduMap API", offset);
		addApicloudProposal(proposals, "searchInBounds", showCode(location, "searchInBounds", false, "key:'',", "lon:'',", "lat:'',", "radius:'',", "pageIndex:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"根据单个关键字在指定方形区域内搜索兴趣点<br>"
				+ "<h3>参数</h3>"
				+ "key：指定的要搜索的关键字（字符串类型），不能为空 默认值：无<br>"
				+ "lbLon：区域左下角经度（字符串类型），不能为空 默认值：无<br>"
				+ "lbLat：指定的区域左下角纬度（字符串类型），不能为空 默认值：无<br>"
				+ "rbLon：区域右下角经度（字符串类型），不能为空 默认值：无<br>"
				+ "rbLat：指定的区域右下角纬度（字符串类型），不能为空 默认值：无<br>"
				+ "pageIndex：页码，如果是第一次发起搜索，填0，根据返回的结果<br>" 
				+"可以去获取第n页的结果，页码从0开始（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status: 是否查找成功;totalNum:本次搜索的总结果数;curNum:当前页的结果数;<br>"
				+"totalPage:本次搜索的总页数;pageIndex:当前页的索引;resultArray: 返回搜索结果列表，是一个数组}<br>"
				+",resultArray：{lon:当前内容的经度;lat:当前内容的纬度;name:名称;uid:id;add:地址;city:所在城市;<br>"
				+"phone:电话号码;postCode:邮编;poiType:POI类型，0:普通点 1:公交站 2:公交线路 3:地铁站 4:地铁线路}<br>"
				+"{msg: 返回报错信息})"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
//		addProposal(proposals, "multiSearchInBounds", JS_FUNCTION, "<h3>描述</h3>"
//				+"根据多个关键字在指定方形区域内搜索兴趣点<br>"
//				+ "<h3>参数</h3>"
//				+ "keys：指定的要搜索的关键字列表（字符串类型），不能为空 默认值：无<br>"
//				+ "lbLon：区域左下角经度（字符串类型），不能为空 默认值：无<br>"
//				+ "lbLat：指定的区域左下角纬度（字符串类型），不能为空 默认值：无<br>"
//				+ "rbLon：区域右下角经度（字符串类型），不能为空 默认值：无<br>"
//				+ "rbLat：指定的区域右下角纬度（字符串类型），不能为空 默认值：无<br>"
//				+ "pageIndex：页码，如果是第一次发起搜索，填0，根据返回的结果<br>" 
//				+"可以去获取第n页的结果，页码从0开始（字符串类型），不能为空 默认值：无<br>"
//				+"<h3>回调函数</h3>"
//				+"function({status: 是否查找成功;totalNum:本次搜索的总结果数;curNum:当前页的结果数;<br>"
//				+"totalPage:本次搜索的总页数;pageIndex:当前页的索引;resultArray: 返回搜索结果列表，是一个数组}<br>"
//				+",resultArray：{lon:当前内容的经度;lat:当前内容的纬度;name:名称;uid:id;add:地址;city:所在城市;<br>"
//				+"phone:电话号码;postCode:邮编;poiType:POI类型，0:普通点 1:公交站 2:公交线路 3:地铁站 4:地铁线路}<br>"
//				+"{msg: 返回报错信息})"
//				+"<br><h3>可用性</h3>"
//				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
//				, getActiveUserAgentIds(), "baiduMap API", offset);
		addApicloudProposal(proposals, "getLocationFromName", showCode(location, "getLocationFromName", false, "city:'',", "address:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"根据地址查找经纬度<br>"
				+ "<h3>参数</h3>"
				+ "city：所要搜索的地址所在的城市（字符串类型），不能为空 默认值：无<br>"
				+ "address：所要获得的经纬度的地址信息（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status: 是否查找成功;lon:返回经度lat:返回纬度},{msg: 返回报错信息})"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "getNameFromLocation", showCode(location, "getNameFromLocation", false, "lon:'',", "lat:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"根据经纬度查找地址信息<br>"
				+ "<h3>参数</h3>"
				+ "lon：经度（字符串类型），不能为空 默认值：无<br>"
				+ "lat：纬度（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status: 是否查找成功;lon:返回经度lat:返回纬度;add:地址;province:所在省份;<br>" 
				+"city:所在城市;district所在县区;streetName:街道名;streetNumber:街道号},{msg: 返回报错信息})"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "getBusRouteFromLineNum", showCode(location, "getBusRouteFromLineNum", false, "city:'',", "line:''")
				, JS_FUNCTION, "<h3>描述</h3>"
				+"根据公交线路号查询详情并在地图上显示<br>"
				+ "<h3>参数</h3>"
				+ "city：公交所在城市（字符串类型），不能为空 默认值：无<br>"
				+ "line：公交线路（字符串类型），不能为空 默认值：无<br>"
				+"<h3>回调函数</h3>"
				+"function({status: 是否查找成功;busName:公交名字;company:公交公司;startTime:最早发车时间;endTime:最晚班车时间;<br>" 
				+"stepInfo:是一个数组包含了站点信息stepInfo:[{lon:’’,lat:’’,title:’’},{…}…]lon站点经度lat站点纬度title站点名},{msg: 返回报错信息})"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
		addApicloudProposal(proposals, "removeBusRoute", "removeBusRoute();"
				, JS_FUNCTION, "<h3>描述</h3>"
				+"移除地图上查询的公交线路<br>"
				+ "<h3>参数</h3>"
				+ "无<br>"
				+"<h3>回调函数</h3>"
				+"无"
				+"<br><h3>可用性</h3>"
				+"IOS系统，安卓系统，PC模拟器；可提供的1.0.0以及更高版本"
				, getActiveUserAgentIds(), "baiDuMap API", offset);
	}
}
