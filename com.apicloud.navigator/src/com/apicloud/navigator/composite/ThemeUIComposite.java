/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.composite;

import java.io.IOException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.themes.ThemeElementHelper;
import org.eclipse.ui.themes.ITheme;

import com.apicloud.navigator.ui.delegate.OpenAPICloudWizardActionDelegate;
import com.aptana.core.util.PlatformUtil;
import com.aptana.theme.Theme;
import com.aptana.theme.ThemePlugin;

@SuppressWarnings("restriction")
public class ThemeUIComposite {
	private UZWizardComposite browserComposite;
	private Shell parent;
	private Shell shell;
	private Font defaultFont;
	private boolean isHandler;

	public ThemeUIComposite(final Shell shell, String url, int w, int h, boolean isHandler) {
		this.shell = shell;
		this.isHandler = isHandler;
		Rectangle ret = shell.getBounds();
		parent = new Composite(shell, SWT.NONE);
		parent.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				if(e.character == 27) {
					shell.setEnabled(true);
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
			}
		});
		parent.setBounds((ret.width - w) / 2, (ret.height - h) / 2, w, h);
		try {
			parent.setLayout(new FillLayout());
			browserComposite = new UZWizardComposite(parent, SWT.NONE, url);
			browserComposite.addKeyListener(new KeyListener() {
				
				@Override
				public void keyReleased(KeyEvent e) {
					if(e.character == 27) {
						shell.setEnabled(true);
					}
				}
				
				@Override
				public void keyPressed(KeyEvent e) {
					// TODO Auto-generated method stub
					
				}
			});
			if (PlatformUtil.isWindows()) {
				Browser browser = (Browser)browserComposite.getBrowser();
				browser.addProgressListener(
						new ProgressListener() {
							@Override
							public void completed(ProgressEvent event) {
								ThemeUIComposite.this
										.registerWizardFunction(browserComposite);
							}

							public void changed(ProgressEvent event) {
							}
						});
			    } else {
			    	Browser browser = (Browser)browserComposite.getBrowser();
			    	browser.addProgressListener(
							new ProgressListener() {
								@Override
								public void completed(ProgressEvent event) {
									ThemeUIComposite.this
											.registerWizardFunction(browserComposite);
								}

								public void changed(ProgressEvent event) {
								}
							});
			    }
			this.browserComposite.forceFocus();
			shell.setEnabled(false);
			parent.open();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected void registerWizardFunction(
			final UZWizardComposite browserComposite) {
		browserComposite.registerFunction("closea",
				new UZWizardComposite.IScriptHandler() {
					public Object handle(Object[] arguments) {
						shell.setEnabled(true);
						parent.close();
						if (!isHandler) {
							showUZWizard();
						}
						return null;
					}

				});

		browserComposite.registerFunction("initFont",
				new UZWizardComposite.IScriptHandler() {
					public Object handle(Object[] arguments) {
						defaultFont = JFaceResources.getFontRegistry().get(
								"org.eclipse.jface.textfont");
						if ((defaultFont == null)
								|| (defaultFont.getFontData() == null)
								|| (defaultFont.getFontData().length <= 0)) {
							return "";
						}
						FontData data = defaultFont.getFontData()[0];
						if (PlatformUtil.isWindows()) {
							Browser browser = (Browser)browserComposite.getBrowser();
						      browser.execute(
										"changeFont('" + data.getName() + "',"
												+ Integer.valueOf(data.getHeight())
												+ ")");
						    } else {
						    	Browser browser = (Browser)browserComposite.getBrowser();
						    	 browser.execute(
										"changeFont('" + data.getName() + "',"
													+ Integer.valueOf(data.getHeight())
													+ ")");
						    }
						return null;
					}
				});

		browserComposite.registerFunction("setTheme",
				new UZWizardComposite.IScriptHandler() {
					public Object handle(Object[] arguments) {
						String url;
						try {
							url = FileLocator.toFileURL(
									super.getClass().getResource(
											"/content/step5.html")).toString();
							browserComposite.redirectTo(url);
						} catch (IOException e) {
							e.printStackTrace();
						}
						return null;
					}

				});

		browserComposite.registerFunction("selectTheme",
				new UZWizardComposite.IScriptHandler() {
					public Object handle(Object[] arguments) {
						String themeName = (String) arguments[0];
						performDefaultTheme(themeName);
						return null;
					}
				});

		browserComposite.registerFunction("setFont",
				new UZWizardComposite.IScriptHandler() {
					public Object handle(Object[] arguments) {
						FontDialog fontDialog = new FontDialog(shell);
						fontDialog.setFontList(defaultFont.getFontData());
						FontData data = fontDialog.open();
						if (data == null)
							return null;
						Font newFont = new Font(defaultFont.getDevice(),
								fontDialog.getFontList());
						if (defaultFont.equals(newFont)) {
							return null;
						}
						FontData newData = newFont.getFontData()[0];
						defaultFont = newFont;
						setDefaultFont(newFont);
						if (PlatformUtil.isWindows()) {
							Browser browser = (Browser)browserComposite.getBrowser();
						      browser.execute(
										"changeFont('" + newData.getName() + "',"
												+ Integer.valueOf(newData.getHeight())
												+ ")");
						    } else {
						    	Browser browser = (Browser)browserComposite.getBrowser();
						    	 browser.execute(
											"changeFont('" + newData.getName() + "',"
													+ Integer.valueOf(newData.getHeight())
													+ ")");
						    }
						return null;
					}

				});

	}

	private void performDefaultTheme(String themeName) {
		com.aptana.theme.IThemeManager themeManager = ThemePlugin.getDefault()
				.getThemeManager();
		Theme theme = themeManager.getTheme(themeName);
		if (theme == null) {
			return;
		}
		themeManager.setCurrentTheme(theme);
	}

	private void setDefaultFont(Font newFont) {
		String[] fontIds = { "org.eclipse.jface.textfont",
				"org.eclipse.ui.workbench.texteditor.blockSelectionModeFont" };
		FontData[] newData = newFont.getFontData();
		for (String fontId : fontIds) {
			setFont(fontId, newData);
		}

		newData = newFont.getFontData();
		FontData[] smaller = new FontData[newData.length];
		int j = 0;
		for (FontData fd : newData) {
			int height = fd.getHeight();
			if (height >= 12) {
				fd.setHeight(height - 2);
			} else if (height >= 10) {
				fd.setHeight(height - 1);
			}
			smaller[(j++)] = fd;
		}
		setFont("com.aptana.explorer.font", smaller);
	}

	private void setFont(String fontId, FontData[] data) {
		String fdString = PreferenceConverter.getStoredRepresentation(data);

		Font existing = JFaceResources.getFont(fontId);
		String existingString = "";
		if (!(existing.isDisposed())) {
			existingString = PreferenceConverter
					.getStoredRepresentation(existing.getFontData());
		}
		if (existingString.equals(fdString)) {
			return;
		}
		JFaceResources.getFontRegistry().put(fontId, data);

		ITheme currentTheme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
		String key = ThemeElementHelper.createPreferenceKey(currentTheme,fontId);
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		store.setValue(key, fdString);
	}

	private void showUZWizard() {
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		if (OpenAPICloudWizardActionDelegate.input == null) {
			OpenAPICloudWizardActionDelegate.input = new IEditorInput() {
				@SuppressWarnings("rawtypes")
				public Object getAdapter(Class adapter) {
					return null;
				}

				public String getToolTipText() {
					return "test";
				}

				public IPersistableElement getPersistable() {
					return null;
				}

				public String getName() {
					return "uz";
				}

				public ImageDescriptor getImageDescriptor() {
					return null;
				}

				public boolean exists() {
					return true;
				}
			};
		}
		IEditorPart part = page.findEditor(OpenAPICloudWizardActionDelegate.input);
		if (part != null) {
			page.bringToTop(part);
			return;
		}
		try {
			IDE.openEditor(page, OpenAPICloudWizardActionDelegate.input,
					"com.apicloud.navigator.APICloudWizard");
		} catch (PartInitException e) {
			e.printStackTrace();
		}

	}
	
	public void setFocus() {
		shell.setFocus();
	}
	
	public void close() {
	Display.getCurrent().asyncExec(new Runnable() {
		@Override
		public void run() {
			shell.setEnabled(true);
			parent.close();
		}
	});
	}
	class Composite extends Shell {
		private Shell shell;
		public Composite(Shell parent, int style) {
			super(parent, style);
			this.shell = parent;
			// TODO Auto-generated constructor stub
		}

		@Override
		public void close() {
			shell.setEnabled(true);
			super.close();
		}
		  @Override
		    protected void checkSubclass() {
		        // TODO Auto-generated method stub
		    }
	}
}
