package com.apicloud.navigator.dialogs;

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.apicloud.commons.util.OS;

public class CheckPhoneBindDialog<K, V> extends Dialog {

	public CheckPhoneBindDialog(Shell parentShell) {
		super(parentShell);
		// TODO è‡ªåŠ¨ç”Ÿæˆ�çš„æž„é€ å‡½æ•°å­˜æ ¹
	}

	protected void createButtonsForButtonBar(Composite parent) {
		super.createButton(parent, IDialogConstants.OK_ID, "ç¡®å®š", true);
	}
	
	protected Point getInitialSize() {
		return new Point(480, 120);
	}
	
	protected int getShellStyle() {
		return super.getShellStyle()|SWT.RESIZE|SWT.CLOSE;
	}

	private HashMap buttons = new HashMap();

	@Override
	protected Button createButton(Composite parent, int id, String label,
			boolean defaultButton) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;
		Button button = new Button(parent, SWT.PUSH);
		GridData data = new GridData(GridData.CENTER, GridData.BEGINNING, true, true, 0, 0);
		button.setText(label);
		button.setFont(JFaceResources.getDialogFont());
		button.setLayoutData(data);
		button.setData(new Integer(id));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				buttonPressed(((Integer) event.widget.getData()).intValue());
			}
		});
		if (defaultButton) {
			Shell shell = parent.getShell();
			if (shell != null) {
				shell.setDefaultButton(button);
			}
		}
		buttons.put(new Integer(id), button);
		setButtonLayoutData(button);
		return button;
	}

	@Override
	protected Button getButton(int id) {
		// TODO è‡ªåŠ¨ç”Ÿæˆ�çš„æ–¹æ³•å­˜æ ¹
		return super.getButton(id);
	}

	
	@Override
	protected Control createButtonBar(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		// create a layout with spacing and margins appropriate for the font
		// size.
		GridLayout layout = new GridLayout();
		layout.numColumns = 0; // this is incremented by createButton
		layout.makeColumnsEqualWidth = true;
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		composite.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER
				| GridData.VERTICAL_ALIGN_CENTER);
		composite.setLayoutData(data);
		composite.setFont(parent.getFont());
		
		// Add the buttons to the button bar.
		createButtonsForButtonBar(composite);
		return composite;
	}

	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, true, true, 1, 1));
		Link link = new Link(container, SWT.NORMAL);
		link.setText("ä¸ºäº†åŠ å¼ºæ‚¨çš„è´¦æˆ·å®‰å…¨ï¼Œè¯·ç™»å½•<A>http://www.apicloud.com/profile</A>åŽ»ç»‘å®šæ‰‹æœºå�·ç �ï¼�");
		link.addListener (SWT.Selection, new Listener () {
			@Override
			public void handleEvent(Event event) {
				System.out.println("Selection: " + event.text);
				String cmd = "rundll32 url.dll,FileProtocolHandler http://www.apicloud.com/profile";
				if (OS.isMacintosh()) {
					cmd = "/usr/bin/open http://www.apicloud.com/profile";
				}
				try {
					Runtime.getRuntime().exec(cmd);
					return;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		return container;
	}
}
