package org.tigris.subversion;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.tigris.subversion.svnclientadapter.SVNClientException;

public class JHLSVNException extends SVNClientException {

	private int aprError = NONE;

	private static final long serialVersionUID = 1L;

	public static final int NONE = -1;
	public static final int MERGE_CONFLICT = 155015;
	public static final int UNSUPPORTED_FEATURE = 200007;
	public static final String OPERATION_INTERRUPTED = "operation was interrupted";

	/**
	 * Constructs a new exception with <code>null</code> as its detail message.
	 */
	public JHLSVNException() {
		super();
	}

	/**
	 * Constructs a new exception with the specified detail message.
	 * 
	 * @param message
	 *            the detail message (which is saved for later retrieval by the
	 *            {@link #getMessage()} method).
	 */
	public JHLSVNException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * 
	 * @param message
	 *            the detail message (which is saved for later retrieval by the
	 *            {@link #getMessage()} method).
	 * @param cause
	 *            the cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method). (A <tt>null</tt> value is
	 *            permitted, and indicates that the cause is nonexistent or
	 *            unknown.)
	 */
	public JHLSVNException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new exception with the specified cause.
	 * 
	 * @param cause
	 *            the cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method). (A <tt>null</tt> value is
	 *            permitted, and indicates that the cause is nonexistent or
	 *            unknown.)
	 */
	public JHLSVNException(Throwable cause) {
		
		
		
		final String sss = ResourcesPlugin.getWorkspace().getRoot().getProject()
				.getFullPath().toString();
		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				MessageDialog.openInformation(Display.getDefault()
						.getActiveShell(), "8888", sss);

			}
		});

		// upgradeProject(path,"update");
		// super(cause);
	}

	/**
	 * Facorty method for creating a delegating/wrapping exception.
	 * 
	 * @param e
	 *            exception to wrap SVNClientException around
	 * @return an SVNClientException instance
	 */
	public static SVNClientException wrapException(Exception e) {
		Throwable t = e;
		if (e instanceof InvocationTargetException) {
			Throwable target = ((InvocationTargetException) e)
					.getTargetException();
			if (target instanceof SVNClientException) {
				return (SVNClientException) target;
			}
			t = target;
		}
		return new SVNClientException(t);
	}

	public int getAprError() {
		return aprError;
	}

	public void setAprError(int aprError) {
		this.aprError = aprError;
	}

	public boolean operationInterrupted() {
		return getMessage() != null
				&& getMessage().indexOf(OPERATION_INTERRUPTED) != -1;
	}

}
