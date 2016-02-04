/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.authentication.splashHandlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.internal.StartupThreading.StartupRunnable;
import org.eclipse.ui.splash.AbstractSplashHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;

import com.apicloud.authentication.AuthenticActivator;
import com.apicloud.authentication.Messages;
import com.apicloud.commons.util.OS;
import com.apicloud.networkservice.ConnectionUtil;
import com.apicloud.networkservice.RC4Util;

import org.tigris.subversion.subclipse.core.util.SVNUtil;

@SuppressWarnings("restriction")
public class APICloudSplashHandler extends AbstractSplashHandler{

	private Composite fCompositeLogin_1;
	private Label signup;
	private Label okLabel;
	private Label cancelLabel;
	private Label forgetPassword;
	private boolean fAuthenticated;
	private boolean isLogin = true;
	private APICloudSplashHandler.AbsolutePositionProgressMonitorPart monitor;
	private Composite composite_3;
	private static final String PROT = ":80";
	private static final String IP = "www.apicloud.com";
	private Composite ipComposite;
	private Composite userNameComposite;
	private Composite passWordComposite;
	private UZText ipText;
	private UZText userNameText;
	private UZText passWordText;
	private String userNameFromCookie = "";

	public APICloudSplashHandler() {
		okLabel = null;
		fAuthenticated = false;
	}

	public void init(final Shell splash) {
		super.init(splash);
		if (AuthenticActivator.getFile().exists()
				&&AuthenticActivator.getProperties().getProperty("username") != null) {
			isLogin = false;
			Properties p =AuthenticActivator.getProperties();
			fAuthenticated = login(
					p.getProperty("username"),
					RC4Util.HloveyRC4(p.getProperty("password"),RC4Util.getKey()), p.getProperty("ip"), (!testConnection(p.getProperty("ip"))), "1"); 
			if (!fAuthenticated) {
				isLogin = true;
			}
		}
		configureUISplash();
		createUI();
		splash.layout(true);
		doEventLoop();	
	}

	private boolean testConnection(String ip) {
		String msg =AuthenticActivator. networkInstance.checkServiceState(ip);
		if(msg.equals("200")){
			AuthenticActivator.setConnection(true);
			return true;
		}else{
			AuthenticActivator.setConnection(false);
			return false;
		}
	}


	private void doEventLoop() {
		Shell splash = getSplash();
		while (fAuthenticated == false) {
			if (splash.getDisplay().readAndDispatch() == false) {
				splash.getDisplay().sleep();
			}
		}
	}

	private void handleButtonRegisterWidgetSelected() {
		String ip = ipText.getText().getText();

		String cmd = "rundll32 url.dll,FileProtocolHandler http://" + ip + PROT + "/signup";
		if (OS.isMacintosh()) {
			cmd = "/usr/bin/open http://" + ip + PROT + "/signup";
		}
		try {
			Runtime.getRuntime().exec(cmd);
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handleButtonResetPassWidgetSelected() {
		String ip = ipText.getText().getText();
		String cmd = "rundll32 url.dll,FileProtocolHandler http://" + ip + PROT
				+ "/retrieve";
		if (OS.isMacintosh()) {
			cmd = "/usr/bin/open http://" + ip + PROT + "/retrieve";
		}
		try {
			Runtime.getRuntime().exec(cmd);
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handleButtonCancelWidgetSelected() {
		if (!(MessageDialog.openConfirm(getSplash(),
				Messages.ApicloudSplashHandler_EXIT,
				Messages.ApicloudSplashHandler_EXIT_MESSAGE)))
			return;
		System.exit(0);
	}

	private void handleButtonOKWidgetSelected() {
		String username = userNameText.getText().getText().toLowerCase();
		String password = passWordText.getText().getText();
		String ip = ipText.getText().getText();
		if ((username.length() > 0) && (password.length() > 0)) {
			if (checkIp(ip))
				fAuthenticated = login(username, password, ip.isEmpty() ? IP
						: ip, false, "0");
		} else {
			setEnabled(true);
			MessageDialog
					.openError(getSplash(), Messages.ApicloudSplashHandler_VALIDATE_ERROR,
							Messages.ApicloudSplashHandler_USERNAME_OR_PASSWORD_VALIDATE_ERROR);
		}
	}

	private boolean login(String userName, String passWord, String ip,
			boolean isLogin, String first) {

		return isLogin ? offLineLogin(first) : normalLogin(userName, passWord,
				ip, first);

	}

	private boolean normalLogin(String username, String password, String ip,
			String first) {
		try {
			// Send the request
			URLConnection conn = getConnection(ip, username, password);
			String cookie = getCookie(conn);
			if (userNameFromCookie.equals("")) {
				return isRightUser(conn) ? saveAndAddSvn(username, password, ip,
						first, cookie) : showErrorMessage(
						Messages.ApicloudSplashHandler_FAIL,
						Messages.ApicloudSplashHandler_USERNAME_OR_PASSWORD_ERROR);
			} else {
				return isRightUser(conn) ? saveAndAddSvn(userNameFromCookie, password, ip,
						first, cookie) : showErrorMessage(
						Messages.ApicloudSplashHandler_FAIL,
						Messages.ApicloudSplashHandler_USERNAME_OR_PASSWORD_ERROR);
			}
		} catch (NoRouteToHostException ex) {
			ex.printStackTrace();
			return showErrorMessage(
					Messages.ApicloudSplashHandler_ERROR, Messages.ApicloudSplashHandler_NETWORK_ERROR); //$NON-NLS-2$
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
			setEnabled(true);
			return false;
		} catch (IOException ex) {
			ex.printStackTrace();
			return showErrorMessage(
					"error", Messages.ApicloudSplashHandler_NETWORK_ERROR); //$NON-NLS-2$
		} catch (JSONException e) {
			e.printStackTrace();
			showErrorMessage(Messages.ApicloudSplashHandler_FAIL,
					Messages.ApicloudSplashHandler_USERNAME_OR_PASSWORD_ERROR);
			setEnabled(true);
			return false;
		}
	}

	private boolean saveAndAddSvn(String username, String password, String ip,
			String first, String cookie) {
		saveUserInfo(username, password, ip, first, cookie);
		addSvnToView(ip, username, cookie);
		return true;
	}

	private void saveUserInfo(String username, String password, String ip,
			String first, String cookie) {
		Properties p = AuthenticActivator.getProperties();
		p.put("username",username );
		p.put("password",RC4Util.HloveyRC4(password, RC4Util.getKey()));
		p.put("ip", ip);
		p.put(Messages.ApicloudSplashHandler_26, cookie);
		if (p.getProperty("first") == null) {
			p.put("first", first);
		}
		AuthenticActivator.store(p);
		AuthenticActivator.setConnection(true);
		AuthenticActivator.loadProperties();
	}

	private boolean showErrorMessage(String title, String message) {
		setEnabled(true);
		MessageDialog.openError(getSplash(), title, message);
		AuthenticActivator
				.getDefault()
				.getLog()
				.log(new Status(IStatus.ERROR, AuthenticActivator.PLUGIN_ID, 0, title
						+ message, null));
		return false;
	}

	private boolean isRightUser(URLConnection conn) throws IOException,
			JSONException {
		StringBuffer answer = new StringBuffer();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				conn.getInputStream(), "UTF-8"));
		String line;
		while ((line = reader.readLine()) != null) {
			answer.append(line);
		}

		reader.close();

		AuthenticActivator
				.getDefault()
				.getLog()
				.log(new Status(IStatus.ERROR, AuthenticActivator.PLUGIN_ID, 0, answer
						.toString(), null));

		return isSuccessed(answer.toString());
	}

	private boolean isSuccessed(String message) throws JSONException {
		JSONObject json = new JSONObject(message);
		String status = json.getString("status");
		if (status.equals("1")) {
			return true;
		}
		return false;
	}

	private String getCookie(URLConnection conn) {
		String cookie = null;
		Map<String, List<String>> map = conn.getHeaderFields();
		Set<String> set = map.keySet();
		for (Iterator<String> iterator = set.iterator(); iterator.hasNext();) {
			String key = iterator.next();
			if (key != null && key.equals("Set-Cookie")) {
				List<String> list = map.get(key);
				StringBuilder builder = new StringBuilder();
				for (String str : list) {
					str = str.replaceAll("HttpOnly", ""); //$NON-NLS-2$
					builder.append(str).toString();
					if (str.startsWith("username=")) {
						int endIndex = str.indexOf(";");
						userNameFromCookie = str.substring(9, endIndex);
						userNameFromCookie = userNameFromCookie.replaceAll("%40", "@");
					}
				}
				cookie = builder.toString();
			}
		}
		return cookie;
	}

	private URLConnection getConnection(String ip, String username,
			String password) throws MalformedURLException, IOException {

		URLConnection conn = AuthenticActivator.networkInstance.checkDevUser(username, password,ip);
		return conn;
	}

	private boolean offLineLogin(String first) {
        AuthenticActivator.setConnection(false);
		Properties p = AuthenticActivator.getProperties();
		p.put("first", first);
		AuthenticActivator.store(p);
		return true;
	}

	private void addSvnToView(final String ip, final String userName,
			final String cookie) {
		final Runnable r = new Runnable() {
			public void run() {
				try {
					String url = "http://" + ip + ":80/getSvnList?useDomain";
					String message = ConnectionUtil.sendPostRequest(url,
							ConnectionUtil.encodeGetSVNParam(userName), cookie);
					JSONObject json;
					json = new JSONObject(message);
					String status = json.getString("status");
					if (status.equals("0")) {
						MessageDialog.openError(getSplash(), Messages.ApicloudSplashHandler_ERROR,
								Messages.ApicloudSplashHandler_SVN_ERROR);

					} else {
						JSONArray body = json.getJSONArray("body");
						SVNProviderPlugin provider = SVNProviderPlugin
								.getPlugin();
						if (provider == null) {
							System.err.println("svn delete error");
						}
						for (ISVNRepositoryLocation location : provider
								.getRepositories().getKnownRepositories(
										new NullProgressMonitor())) {
							try {
								provider.getRepositories().disposeRepository(
										location);
							} catch (SVNException e) {
								e.printStackTrace();
							}
						}
						for (int i = 0; i < body.length(); i++) {
							SVNUtil.addSVNToView((String) body.get(i));
						}
					}
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
			}
		};

		StartupRunnable startupRunnable = new StartupRunnable() {

			public void runWithException() throws Throwable {
				r.run();

			}
		};
		getSplash().getDisplay().asyncExec(startupRunnable);

	}

	private void createUI() {
		createUICompositeLogin();
		createUICompositeBlank();
	}

	private void createUICompositeBlank() {
		KeyListener keyListener = new KeyListener() {
			public void keyReleased(KeyEvent e) {
				verifyKeyEvent(e);
			}

			public void keyPressed(KeyEvent e) {
			}
		};
		Composite cop = new Composite(fCompositeLogin_1, SWT.NONE);
		cop.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false, 2, 1));
		cop.setLayout(new GridLayout(1, false));

		cancelLabel = new Label(cop, SWT.NONE);
		cancelLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true,
				false, 1, 1));
		cancelLabel.setBounds(0, 0, 61, 17);
		cancelLabel.setImage(AuthenticActivator.getImage("icons/cancel.png"));
		cancelLabel.setVisible(isLogin);
		cancelLabel.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				handleButtonCancelWidgetSelected();
			}
		});
		cancelLabel.addMouseTrackListener(new MouseTrackListener() {
			@Override
			public void mouseHover(MouseEvent e) {
				cancelLabel.setImage(AuthenticActivator.getImage("icons/cancel1.png"));
			}

			@Override
			public void mouseExit(MouseEvent e) {
				cancelLabel.setImage(AuthenticActivator.getImage("icons/cancel.png"));
			}

			@Override
			public void mouseEnter(MouseEvent e) {
				cancelLabel.setImage(AuthenticActivator.getImage("icons/cancel1.png"));
			}
		});

		Composite composite = new Composite(fCompositeLogin_1, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false,
				1, 1));
		composite.setLayout(new GridLayout(3, false));

		monitor = new AbsolutePositionProgressMonitorPart(composite);
		GridData data = new GridData(SWT.CENTER, SWT.NONE, false, false);
		data.widthHint = 270;
		data.horizontalSpan = 3;
		monitor.setLayoutData(data);
		monitor.getProgressText().setBounds(new Rectangle(56, 0, 158, 15));
		monitor.getProgressText().setForeground(
				new Color(getSplash().getShell().getDisplay(), new RGB(255,
						255, 255)));
		monitor.getProgressIndicator().setBackground(
				new Color(getSplash().getShell().getDisplay(), new RGB(65, 69,
						74)));
		monitor.getProgressIndicator().setBounds(new Rectangle(56, 15, 158, 4));
		monitor.getProgressText().setBackground(
				new Color(getSplash().getShell().getDisplay(), new RGB(65, 69,
						74)));
		monitor.setBackgroundImage(getSplash().getShell().getBackgroundImage());
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		signup = new Label(composite, SWT.NONE);
		signup.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false,
				1, 1));
		signup.setEnabled(isLogin);
		signup.setImage(AuthenticActivator.getImage("icons/signup.png"));
		signup.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				handleButtonRegisterWidgetSelected();
			}
		});
		signup.addMouseTrackListener(new MouseTrackListener() {
			@Override
			public void mouseHover(MouseEvent e) {
				signup.setImage(AuthenticActivator.getImage("icons/signup1.png"));
			}

			@Override
			public void mouseExit(MouseEvent e) {
				signup.setImage(AuthenticActivator.getImage("icons/signup.png"));
			}

			@Override
			public void mouseEnter(MouseEvent e) {
				signup.setImage(AuthenticActivator.getImage("icons/signup1.png"));
			}
		});

		Label line = new Label(composite, SWT.NONE);
		line.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false,
				1, 1));
		line.setImage(AuthenticActivator.getImage("icons/line.png"));

		forgetPassword = new Label(composite, SWT.NONE);
		forgetPassword.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, true,
				false, 1, 1));
		forgetPassword.setEnabled(isLogin);
		forgetPassword.setImage(AuthenticActivator.getImage("icons/forgotpwd.png"));
		forgetPassword.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				handleButtonResetPassWidgetSelected();
			}
		});
		forgetPassword.addMouseTrackListener(new MouseTrackListener() {
			@Override
			public void mouseHover(MouseEvent e) {
				forgetPassword.setImage(AuthenticActivator
						.getImage("icons/forgotpwd1.png"));
			}

			@Override
			public void mouseExit(MouseEvent e) {
				forgetPassword.setImage(AuthenticActivator
						.getImage("icons/forgotpwd.png"));
			}

			@Override
			public void mouseEnter(MouseEvent e) {
				forgetPassword.setImage(AuthenticActivator
						.getImage("icons/forgotpwd1.png"));
			}
		});

		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);

		Composite composite_1 = new Composite(fCompositeLogin_1, SWT.NONE);
		composite_1.setLayout(new FillLayout(SWT.HORIZONTAL));
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
				1, 1));

		composite_3 = new Composite(composite_1, SWT.NONE);

		ipComposite = new Composite(composite_3, SWT.BORDER);

		ipComposite.setVisible(false);

		ipText = new UZText(ipComposite,
				Messages.ApicloudSplashHandler_IP_ADDRESS, "icons/ip1.png"); 
		Properties p = AuthenticActivator.getProperties();
		ipText.getText().setText(
				p.getProperty("ip") == null ? IP : p.getProperty("ip")); 
		ipComposite.setBounds(2, 35, 265, 36);

		userNameComposite = new Composite(composite_3, SWT.BORDER);
		userNameComposite.setEnabled(isLogin);
		userNameText = new UZText(userNameComposite,
				Messages.ApicloudSplashHandler_EMAIL, "icons/username.png"); 
		userNameText
				.getText()
				.setText(
						p.getProperty("username") == null ? "" : p.getProperty("username")); 
		if (OS.isWindows()) {

			userNameComposite.setBounds(2, 76, 265, 36);
		} else {
			userNameComposite.setBounds(2, 41, 265, 36);
		}
		passWordComposite = new Composite(composite_3, SWT.BORDER);
		passWordComposite.setEnabled(isLogin);
		passWordText = new UZText(passWordComposite,
				Messages.ApicloudSplashHandler_PASSWORD, "icons/password.png"); 
		passWordText
				.getText()
				.setText(
						p.getProperty("password") == null ? "" : p.getProperty("password")); 
		if (OS.isWindows()) {

			passWordComposite.setBounds(2, 117, 265, 36);
		} else {
			passWordComposite.setBounds(2, 82, 265, 36);
		}
		passWordText.getText().setEchoChar('*');

		okLabel = new Label(composite_3, SWT.NONE);
		if (OS.isWindows()) {

			okLabel.setBounds(2, 168, 265, 50);
		} else {
			okLabel.setBounds(2, 132, 265, 50);
		}

		okLabel.setImage(AuthenticActivator.getImage("icons/login2.png"));
		okLabel.setEnabled(isLogin);
		okLabel.addMouseTrackListener(new MouseTrackListener() {
			@Override
			public void mouseHover(MouseEvent e) {
				okLabel.setImage(AuthenticActivator.getImage("icons/login1.png"));
			}

			@Override
			public void mouseExit(MouseEvent e) {
				okLabel.setImage(AuthenticActivator.getImage("icons/login2.png"));
			}

			@Override
			public void mouseEnter(MouseEvent e) {
				okLabel.setImage(AuthenticActivator.getImage("icons/login1.png"));
			}
		});
		okLabel.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				setEnabled(false);
				handleButtonOKWidgetSelected();
			}
		});

		getSplash().addKeyListener(keyListener);
		fCompositeLogin_1.addKeyListener(keyListener);
		ipText.getText().addKeyListener(keyListener);
		userNameText.getText().addKeyListener(keyListener);
		passWordText.getText().addKeyListener(keyListener);
		cop.addKeyListener(keyListener);
		composite.addKeyListener(keyListener);
		composite_1.addKeyListener(keyListener);
		composite_3.addKeyListener(keyListener);

	}

	private void createUICompositeLogin() {

		// Create the composite
		fCompositeLogin_1 = new Composite(getSplash(), SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		fCompositeLogin_1.setLayout(layout);
	}

	private void configureUISplash() {
		FillLayout layout = new FillLayout();
		getSplash().setLayout(layout);
		getSplash().setBackgroundMode(SWT.INHERIT_DEFAULT);
	}

	private boolean checkIp(String str) {

		if (str.isEmpty() || str.equals("www.apicloud.com")) {
			return true;
		}

		String[] ipValue = str.split("\\.");
		if (ipValue.length != 4) {
			setEnabled(true);
			MessageDialog.openError(getSplash(), Messages.ApicloudSplashHandler_IP_VALIDATE_ERROR,
					Messages.ApicloudSplashHandler_IP_ERROR);
			return false;
		}
		for (int i = 0; i < ipValue.length; i++) {
			try {
				Integer q = Integer.valueOf(ipValue[i]);
				if (q > 255) {
					setEnabled(true);
					MessageDialog.openError(getSplash(),
							Messages.ApicloudSplashHandler_IP_VALIDATE_ERROR,
							Messages.ApicloudSplashHandler_IP_ERROR);
					return false;
				}
			} catch (Exception e) {
				return false;
			}
		}
		return true;
	}

	@Override
	public IProgressMonitor getBundleProgressMonitor() {
		return monitor;
	}

	private void updateUI(final Runnable r) {
		Shell splashShell = getSplash();
		if (splashShell == null || splashShell.isDisposed())
			return;

		Display display = splashShell.getDisplay();

		if (Thread.currentThread() == display.getThread())
			r.run();
		else {
			StartupRunnable startupRunnable = new StartupRunnable() {

				public void runWithException() throws Throwable {
					r.run();
				}
			};
			display.asyncExec(startupRunnable);
		}
	}

	class AbsolutePositionProgressMonitorPart extends ProgressMonitorPart {
		public AbsolutePositionProgressMonitorPart(Composite parent) {
			super(parent, null);
			setLayout(null);
		}

		public ProgressIndicator getProgressIndicator() {
			return fProgressIndicator;
		}

		public Label getProgressText() {
			return fLabel;
		}

		public void beginTask(final String name, final int totalWork) {

			updateUI(new Runnable() {

				public void run() {
					if (isDisposed())
						return;
					AbsolutePositionProgressMonitorPart.super.beginTask(name,
							totalWork);
				}
			});

		}

		public void done() {

			updateUI(new Runnable() {

				public void run() {
					if (isDisposed())
						return;
					AbsolutePositionProgressMonitorPart.super.done();
				}
			});

		}

		public void internalWorked(final double work) {

			updateUI(new Runnable() {

				public void run() {
					if (isDisposed())
						return;
					AbsolutePositionProgressMonitorPart.super
							.internalWorked(work);
				}
			});

		}

		public void setFont(final Font font) {

			updateUI(new Runnable() {

				public void run() {
					if (isDisposed())
						return;
					AbsolutePositionProgressMonitorPart.super.setFont(font);
				}
			});

		}

		protected void updateLabel() {

			updateUI(new Runnable() {

				public void run() {
					if (isDisposed())
						return;
					AbsolutePositionProgressMonitorPart.super.updateLabel();
				}
			});

		}
	}

	private void setEnabled(boolean flag) {
		if (cancelLabel != null)
			cancelLabel.setVisible(flag);
		if (okLabel != null)
			okLabel.setEnabled(flag);
		if (userNameComposite != null)
			userNameComposite.setEnabled(flag);
		if (passWordComposite != null)
			passWordComposite.setEnabled(flag);
		if (signup != null)
			signup.setEnabled(flag);
		if (forgetPassword != null)
			forgetPassword.setEnabled(flag);
	}

	private void verifyKeyEvent(KeyEvent e) {
		char key = e.character;
		if (key == 13) {
			setEnabled(false);
			handleButtonOKWidgetSelected();
		}
	}

	class UZText {
		private Text text;
		private Label label;

		public UZText(Composite parent, String msg, String iconPath) {
			parent.setLayout(new GridLayout(2, false));
			parent.setBackground(new Color(null, 255, 255, 255));
			label = new Label(parent, SWT.NONE);
			label.setImage(AuthenticActivator.getImage(iconPath));
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
					false, 1, 1));
			text = new Text(parent, SWT.NONE);
			text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
					1, 1));
			text.setMessage(msg);
		}

		public Text getText() {
			return text;
		}

	}
}
