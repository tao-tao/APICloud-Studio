/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.ui.wizards;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.json.JSONArray;
import org.json.JSONObject;

import com.apicloud.authentication.AuthenticActivator;
import com.apicloud.commons.model.APICloudPage;
import com.apicloud.navigator.dialogs.Messages;

public class TempleteFarmeWizardPage extends WizardPage {
	private boolean canFinish = false;
	private IProject[] projects;
	private IProject project;
	private Text pageName;
	private ComboViewer list;
	private String path;
	private Label lblNewLabel_3;
	private  APICloudPage page;
	private ISelection selection;
	List<APICloudPage> pages = new ArrayList<APICloudPage>();

	/**
	 * Create the wizard.
	 */
	public TempleteFarmeWizardPage(ISelection selection) {
		super(Messages.CREATEPAGEWIAZRD);
		setTitle(Messages.CREATEPAGEFREAMWORK);
		setDescription(Messages.CREATEPAGEONEFREAMWORK);
		this.selection = selection;
		init();
	}

	private void init() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		this.projects = FilterProject(projects);
		initData();
	}
	
	private void initData() {
		String content = read();
		try {
			JSONObject json;
				json = new JSONObject(content);
					JSONArray body = json.getJSONArray("body");
					for(int i = 0; i < body.length(); i++) {
						APICloudPage page = new APICloudPage();
						JSONObject object = (JSONObject)body.get(i);
						page.setId(object.getString("id"));
						page.setName(object.getString("name"));
						page.setReViewImage(object.getString("reViewImage"));
						pages.add(page);
					}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	private String read() {
		String result = "";
		  try {
                  InputStreamReader read = new InputStreamReader(
                		  this.getClass().getResourceAsStream("/templates/page.txt"), "UTF-8");
                  BufferedReader bufferedReader = new BufferedReader(read);
                  String lineTxt = null;
                  while((lineTxt = bufferedReader.readLine()) != null){
                      result += lineTxt;
                  }
                  read.close();
      } catch (Exception e) {
          e.printStackTrace();
          return "{}";
      }
		return result;
	}
	
	private void selectProject(IProject p) {
		if(p != null) {
			this.path = p.getLocation().toOSString(); //$NON-NLS-1$
		}
	}
	
	public IProject getIProject() {
		return  project;
	}
	
	public String getSourcePath() {
		return path;
	}
	
	public String getPagePath() {
		return page.getId();
	}
	
	public String getPageName() {
		return pageName.getText();
	}
	
	private IProject[] FilterProject(IProject[] projects) {
		List<IProject> list = new ArrayList<IProject>();
		for(IProject p : projects) {
			File config = new File(p.getLocation().toOSString()+ File.separator + "config.xml");
			if(config.exists()) {
				list.add(p);
			}
		}
		return list.toArray(new IProject[list.size()]);
	}
	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		GridLayout gl_container = new GridLayout(3, false);
		gl_container.marginRight = 20;
		gl_container.marginHeight = 0;
		gl_container.marginLeft = 20;
		container.setLayout(gl_container);
		
		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		Label lblNewLabel = new Label(composite, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("\u6240\u5C5E\u9879\u76EE:");
		
		this.list = new ComboViewer(composite, SWT.NONE | SWT.READ_ONLY);
		this.list.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		this.list.setLabelProvider(new ProjectLabelProvider());
		this.list.setContentProvider(new ArrayContentProvider());
		this.list.setInput(projects);
		this.list.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection ss = (StructuredSelection)list.getSelection();
				IProject p = (IProject)ss.getFirstElement();
				project = p;
				selectProject(p);
			}
		});
		
		Label lblNewLabel_1 = new Label(composite, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText(Messages.PAGENAME);
		
		pageName = new Text(composite, SWT.BORDER);
		pageName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		pageName.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				setPageComplete(validatePage());
			}
		});
		
		Composite composite_4 = new Composite(container, SWT.NONE);
		GridData gd_composite_4 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2);
		gd_composite_4.widthHint = 20;
		composite_4.setLayoutData(gd_composite_4);
		
		Composite composite_1 = new Composite(container, SWT.NONE);
		composite_1.setLayout(new GridLayout(1, false));
		GridData gd_composite_1 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2);
		gd_composite_1.widthHint = 240;
		gd_composite_1.heightHint = 480;
		composite_1.setLayoutData(gd_composite_1);
		
		lblNewLabel_3 = new Label(composite_1, SWT.NONE);
		lblNewLabel_3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		lblNewLabel_3.setImage(AuthenticActivator.getImage(pages.get(0).getReViewImage()));
		
		Composite composite_2 = new Composite(container, SWT.NONE);
		composite_2.setLayout(new GridLayout(2, false));
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Label lblNewLabel_2 = new Label(composite_2, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		lblNewLabel_2.setText(Messages.PAGEFREAWORK);
		
		Composite composite_3 = new Composite(composite_2, SWT.NONE);
		composite_3.setLayout(new GridLayout(1, false));
		GridData gd_composite_3 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite_3.heightHint = 350;
		composite_3.setLayoutData(gd_composite_3);
		TableViewer UIViewer = createTable(composite_3);
		UIViewer.setInput(pages);
		
		UIViewer.setSelection(new StructuredSelection(pages.get(0)));
		new Label(container, SWT.NONE);
		page = pages.get(0);
		
		updateDate();
		setPageComplete(validatePage());
	}
	
    private void updateDate() {
		   if ((this.selection != null) && (!(this.selection.isEmpty())) && 
			       (this.selection instanceof IStructuredSelection)) {
			   IStructuredSelection ssel = (IStructuredSelection)this.selection;
		       if (ssel.size() > 1)
		         return;
		       Object obj = ssel.getFirstElement();
		       if (obj instanceof IResource)
		       {
		    	   IResource res = (IResource)obj;
		    	  project = res.getProject();
		       }
		   }
		   if(project != null) {
			   this.list.setSelection(new StructuredSelection(project));
		   }
	}

	protected boolean validatePage() {
    	canFinish=false;	
    	StructuredSelection ss = (StructuredSelection)list.getSelection();
    	IProject p = (IProject)ss.getFirstElement();
		if(project == null) {
			setPageComplete(false);
    		setErrorMessage("\u8BF7\u5148\u9009\u62E9\u4E00\u4E2A\u9879\u76EE!");
            return false;
		}
        if ("".equals(pageName.getText()))
        {
        	setPageComplete(false);
        	setErrorMessage(Messages.PAGENAMEISNOTNULL);
            return false;
        }
        
        String fileName = pageName.getText() + "_window.html";
		
		IResource resource = p.findMember(new Path("/html/" + fileName));
		if(resource != null) {
			setPageComplete(false);
        	setErrorMessage("doub");
            return false;
		}
    	setErrorMessage(null);
        setMessage(null);
        canFinish=true;	
    	return true;

    }

	public boolean isCanFinish() {
		return canFinish;
	}
	
	private TableViewer createTable(Composite tabFolder) {
		TableViewer tableViewer = new TableViewer(tabFolder, SWT.BORDER | SWT.FULL_SELECTION);
		final Table table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.addListener(SWT.MeasureItem, new Listener() {
			@Override
			public void handleEvent(Event event) {
				event.height = 30;
				
			}
		});
		table.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				TableItem[] ti = table.getSelection();
				page = (APICloudPage)ti[0].getData();
				lblNewLabel_3.setImage(AuthenticActivator.getImage(page.getReViewImage()));
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		TableViewerColumn tableViewerColumn_name = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tableColumn_name = tableViewerColumn_name.getColumn();
		tableColumn_name.setWidth(300);
		tableViewerColumn_name.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((APICloudPage)element).getName();
			}

		});
		
		tableViewer.setContentProvider(new ArrayContentProvider());
		return tableViewer;
	}
}
class ProjectLabelProvider extends LabelProvider {
	@Override
	public String getText(Object element) {
		IProject p = (IProject)element;
		return p.getName();
	}
}