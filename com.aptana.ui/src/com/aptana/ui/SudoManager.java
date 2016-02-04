/**
 * Aptana Studio
 * Copyright (c) 2012-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ui;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;

import com.aptana.core.ShellExecutable;
import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.EclipseUtil;
import com.aptana.core.util.IProcessRunner;
import com.aptana.core.util.ProcessRunnable;
import com.aptana.core.util.ProcessRunner;
import com.aptana.core.util.StringUtil;
import com.aptana.core.util.SudoProcessRunnable;
import com.aptana.ui.dialogs.SudoPasswordPromptDialog;
import com.aptana.ui.util.UIUtils;

/**
 * This class manages prompting the password dialog to the user and return the password back to the caller. In case user
 * does not provide valid passwords or press cancel button, it returns empty password.
 * 
 * @author pinnamuri
 * @author cwilliams
 */
public class SudoManager
{
	private static final String DISREGARD_CACHED_CREDENTIALS = "-k"; //$NON-NLS-1$
	private static final String SUDO = "sudo"; //$NON-NLS-1$
	private static final String ECHO = "echo"; //$NON-NLS-1$
	private static final String SUDO_INPUT_PWD = "-S"; //$NON-NLS-1$
	private static final String ECHO_MESSAGE = "SUCCESS"; //$NON-NLS-1$
	private static final String NON_INTERACTIVE = "-n"; //$NON-NLS-1$
	private static final String END_OF_OPTIONS = "--"; //$NON-NLS-1$

	// We can run -k alone to invalidate cached credentials (forcing prompt next time)
	// We can run -k <command> to run ignoring cached credentials (forcing prompt now)
	// If password is empty/null, how can we verify if user is allowed to run commands that way?

	private static int MAX_ATTEMPTS = 3;

	/**
	 * A null password mean we have nop stored "good" password. An empty array means sudo requires no password.
	 * Otherwise holds the password required for sudo.
	 */
	private char[] validPassword = null;

	public SudoManager()
	{
	}

	/**
	 * Authenticates based on the given password and returns true if authentication is successful. A null or empty
	 * password is treated as sudo not requiring a password (and in both cases we will cache the password as an empty
	 * char[]).
	 * 
	 * @param password
	 * @return
	 * @throws CoreException
	 */
	public boolean authenticate(char[] password) throws CoreException
	{
		try
		{
			Map<String, String> environment = ShellExecutable.getEnvironment();
			environment.put(IProcessRunner.REDIRECT_ERROR_STREAM, StringUtil.EMPTY);

			ProcessRunnable runnable;
			// If the password is empty/null, don't add -S!
			if (password == null || password.length == 0)
			{
				password = new char[0]; // when we store the password, store it as "empty", not null
				// Just try running sudo -k echo SUCCESS with no password
				Process p = new ProcessRunner().run(environment, SUDO, DISREGARD_CACHED_CREDENTIALS, NON_INTERACTIVE,
						END_OF_OPTIONS, ECHO, ECHO_MESSAGE);

				// Don't pass along password...
				runnable = new SudoProcessRunnable(p, null, ECHO_MESSAGE);
			}
			else
			{
				// Try running and pass password on STDIN
				Process p = new ProcessRunner().run(environment, SUDO, DISREGARD_CACHED_CREDENTIALS, SUDO_INPUT_PWD,
						END_OF_OPTIONS, ECHO, ECHO_MESSAGE);
				runnable = new SudoProcessRunnable(p, password, ECHO_MESSAGE);
			}

			Thread t = new Thread(runnable, "SudoManager authentication thread"); //$NON-NLS-1$
			t.start();
			t.join();
			IStatus status = runnable.getResult();
			if (status.isOK())
			{
				validPassword = password;
				return true;
			}
			IdeLog.log(UIPlugin.getDefault(), status);
		}
		catch (IOException e)
		{
			IdeLog.logError(UIPlugin.getDefault(), e.getMessage());
		}
		catch (InterruptedException e)
		{
			IdeLog.logError(UIPlugin.getDefault(), e.getMessage());
		}
		return false;
	}

	/**
	 * This is responsible for prompting the dialog to the user and returns the password back to the caller. If the user
	 * cancels the dialog, then empty password is passed back to the caller.
	 * 
	 * @param promptMessage
	 * @return
	 * @throws CoreException
	 */
	public char[] getPassword() throws CoreException
	{
		// FIXME Handle when password is empty (meaning sudo doesn't require a password!)
		final IStatus[] status = new IStatus[] { Status.OK_STATUS };
		if (validPassword == null)
		{
			// If the system doesn't require a password, we don't need to pop the prompt at all!
			if (authenticate(null))
			{
				// sudo doesn't require password!
				return new char[0];
			}

			UIUtils.getDisplay().syncExec(new Runnable()
			{
				public void run()
				{
					try
					{
						boolean retry;
						int retryAttempts = 0;
						String promptMessage = MessageFormat.format(Messages.SudoManager_MessagePrompt,
								EclipseUtil.getStudioPrefix());
						do
						{
							retryAttempts++;
							retry = false;
							SudoPasswordPromptDialog sudoDialog = new SudoPasswordPromptDialog(new IShellProvider()
							{

								public Shell getShell()
								{
									return UIUtils.getActiveShell();
								}
							}, promptMessage);
							if (sudoDialog.open() == Dialog.OK && !authenticate(sudoDialog.getPassword()))
							{
								// Re-run the authentication dialog as long as user attempts to provide password.
								retry = true;
							}
							promptMessage = Messages.Sudo_Invalid_Password_Prompt;
						}
						while (retry && retryAttempts < MAX_ATTEMPTS);
						if (validPassword == null && retryAttempts >= MAX_ATTEMPTS)
						{
							// User has exceeded the max attempts to provide the password.
							throw new CoreException(Status.CANCEL_STATUS);
						}
					}
					catch (CoreException e)
					{
						status[0] = e.getStatus();
					}
				}
			});

		}
		if (status[0] != Status.OK_STATUS)
		{
			throw new CoreException(status[0]);
		}
		return validPassword;
	}
}
