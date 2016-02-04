/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.ui.pages;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.json.JSONArray;
import org.json.JSONObject;

import com.apicloud.authentication.AuthenticActivator;
import com.apicloud.commons.model.APICloudProject;
import com.apicloud.navigator.dialogs.Messages;

@SuppressWarnings("restriction")
public class NewUZProjectWizardPage extends WizardPage{
	List<APICloudProject> projects = new ArrayList<APICloudProject>();
	private Label defaultLabel;
	private boolean isSafed;
	private Text nameText = null;
	private boolean canFinish = false;
	private ArrayList<String> list;
	private String initialProjectFieldValue;
    Text projectNameField;

    private Listener nameModifyListener = new Listener() {
        public void handleEvent(Event e) {
            boolean valid = validatePage();
            setPageComplete(valid);
                
        }
    };
    private static final int SIZING_TEXT_FIELD_WIDTH = 175;
    private Composite composite;
	private Composite composit;

    public NewUZProjectWizardPage(String pageName) {
    	super(pageName);
		setTitle(Messages.CREATEAPPPROJECT);
		setDescription(Messages.CREATENEWAPPPROJECT);
	    setPageComplete(false);
	    initData();
    }
	private void initData() {
		String content = read();
		try {
			JSONObject json;
				json = new JSONObject(content);
					JSONArray body = json.getJSONArray("body");
					for(int i = 0; i < body.length(); i++) {
						APICloudProject project = new APICloudProject();
						JSONObject object = (JSONObject)body.get(i);
						project.setId(object.getString("id"));
						project.setImage(object.getString("image"));
						project.setName(object.getString("name"));
						project.setSelectImage(object.getString("selectImage"));
						project.setReViewImage(object.getString("reViewImage"));
						projects.add(project);
					}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	private String read() {
		String result = "";
		  try {
                  InputStreamReader read = new InputStreamReader(
                		  this.getClass().getResourceAsStream("/resource/project.txt"), "UTF-8");
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
	@Override
    public void createControl(Composite parent) {
        composite = new Composite(parent, SWT.NULL);
        initializeDialogUnits(parent);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite,
                IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        createProjectNameGroup(composite);
        
        GridLayout layoutDes = new GridLayout();
		layoutDes.verticalSpacing = 0;
		
		list = new ArrayList<String>();
		GridLayout gdCategory2 = new GridLayout();
		gdCategory2.numColumns=2;
		gdCategory2.verticalSpacing=0;
		
        setPageComplete(validatePage());
        // Show description on opening
        setErrorMessage(null);
        setMessage(null);
        setControl(composite);
        Dialog.applyDialogFont(composite);
        
        Composite composite_4 = new Composite(composite, SWT.NONE);
		GridData gd_composite_4 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2);
		gd_composite_4.widthHint = 20;
		composite_4.setLayoutData(gd_composite_4);
        
        Composite composite_1 = new Composite(composite, SWT.NONE);
		composite_1.setLayout(new GridLayout(1, false));
		GridData gd_composite_1 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2);
		gd_composite_1.widthHint = 240;
		gd_composite_1.heightHint = 480;
		composite_1.setLayoutData(gd_composite_1);
		
		final Label lblNewLabel_3 = new Label(composite_1, SWT.NONE);
		lblNewLabel_3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		
		Composite composite_2 = new Composite(composite, SWT.NONE);
		composite_2.setLayout(new GridLayout(2, false));
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Label lblNewLabel_2 = new Label(composite_2, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		lblNewLabel_2.setText("\u5E94\u7528\u6846\u67B6:");
		
		Composite composite_3 = new Composite(composite_2, SWT.NONE);
		composite_3.setLayout(new FillLayout(SWT.HORIZONTAL));
		GridData gd_composite_3 = new GridData(GridData.FILL_BOTH);
		gd_composite_3.widthHint = 350;
		gd_composite_3.heightHint = 271;
		composite_3.setLayoutData(gd_composite_3);
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(composite_3, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		
		composit = new Composite(scrolledComposite, SWT.NONE);
		composit.setLayout(new GridLayout(4, false));
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		int i = 0;
		for(APICloudProject project : projects) {
			Label l = new Label(composit, SWT.NONE);
			l.setData(project);
			if(i == 0) {
				defaultLabel = l;
				l.setImage(AuthenticActivator.getImage(project.getSelectImage()));
				lblNewLabel_3.setImage(AuthenticActivator.getImage(project.getReViewImage()));
			}else {
				l.setImage(AuthenticActivator.getImage(project.getImage()));
				
			}
			l.setLayoutData(gd_composite);
			l.addMouseListener(new MouseAdapter() {
				public void mouseUp(MouseEvent e) {
					APICloudProject project = (APICloudProject)defaultLabel.getData();
					defaultLabel.setImage(AuthenticActivator.getImage(project.getImage()));
					defaultLabel = (Label)e.getSource();
					project = (APICloudProject)defaultLabel.getData();
					defaultLabel.setImage(AuthenticActivator.getImage(project.getSelectImage()));
					lblNewLabel_3.setImage(AuthenticActivator.getImage(project.getReViewImage()));
				}
			});
			i++;
		}
		scrolledComposite.setContent(composit);
		scrolledComposite.setMinSize(composit.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		new Label(composite, SWT.NONE);
		
    }
   
    private boolean dialogChanged() {
    	if(projectNameField.getText().replaceAll("[\u0391-\uFFE5]", "**").length() > 20) {
    		setPageComplete(false);
    		setErrorMessage(Messages.APPPROJECTNOWARN);
            return false;
    	}
        if (nameText.getText().length() > 200)
        {
        	setPageComplete(false);
        	setErrorMessage(Messages.APPNAMEWARN);
            return false;
        }
        
        
        String regex="[^%$&]{1,}";
        Pattern p=Pattern.compile(regex);
        Matcher m=p.matcher(projectNameField.getText());
        if(!m.matches()){
        	setPageComplete(false);
        	setErrorMessage(Messages.APPNAMECHARWARN);
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
    
	public ArrayList<String> getList() {
		return list;
	}
	
	public String getNameText() {
		return this.nameText.getText();
	}
	
    public Text getProjectNameField() {
		return projectNameField;
	}

    private final void createProjectNameGroup(Composite parent) {
    	   getShell().setText("create APP");
        GridLayout gl_composite = new GridLayout(3, false);
        gl_composite.marginLeft = 20;
        gl_composite.marginHeight = 0;
        gl_composite.marginWidth = 0;
        gl_composite.marginRight = 20;
        composite.setLayout(gl_composite);
        Composite projectGroup = new Composite(parent, SWT.NONE);
        projectGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        projectGroup.setLayout(layout);

        Label projectLabel = new Label(projectGroup, SWT.NONE);
        projectLabel.setText(Messages.APPNAME);
        projectLabel.setFont(parent.getFont());

        projectNameField = new Text(projectGroup, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.verticalAlignment = SWT.FILL;
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        projectNameField.setLayoutData(data);
        projectNameField.setFont(parent.getFont());
        projectNameField.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				setPageComplete(validatePage());
			}
		});

        if (initialProjectFieldValue != null) {
			projectNameField.setText(initialProjectFieldValue);
		}
        projectNameField.addListener(SWT.Modify, nameModifyListener);
        Label nameLabel = new Label(projectGroup, SWT.NONE);
		nameLabel.setText(Messages.APPINSTRUCTION);	
		nameText = new Text(projectGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		GridData data1 = new GridData(GridData.FILL_HORIZONTAL);
		data1.heightHint = 60;
        data1.verticalAlignment = SWT.FILL;
        data1.widthHint = SIZING_TEXT_FIELD_WIDTH;
		nameText.setLayoutData(data1);
        nameText.addModifyListener(new ModifyListener() {
		    public void modifyText(ModifyEvent e) {
		    	setPageComplete(validatePage());
		    }
		});
        GridData gd_projectDesLabelIndex = new GridData(SWT.CENTER, SWT.BOTTOM, false, false, 1, 1);
        gd_projectDesLabelIndex.widthHint = 32;
    }

    public IProject getProjectHandle() {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(
                getProjectName());
    }

    public String getProjectName() {
        if (projectNameField == null) {
			return initialProjectFieldValue;
		}
        return getProjectNameFieldValue();
    }

    private String getProjectNameFieldValue() {
        if (projectNameField == null) {
			return ""; 
		}
        return projectNameField.getText().trim();
    }

    public void setInitialProjectName(String name) {
        if (name == null) {
			initialProjectFieldValue = null;
		} else {
            initialProjectFieldValue = name.trim();
        }
    }

    protected boolean validatePage() {
    	canFinish=false;	
    	   getShell().setText(Messages.CREATEPROJECTWIZARD);
        IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();

        String projectFieldContents = getProjectNameFieldValue();
        if (projectFieldContents.equals("")) {
            setErrorMessage(null);
            setMessage(IDEWorkbenchMessages.WizardNewProjectCreationPage_projectNameEmpty);
            return false;
        }

        IStatus nameStatus = workspace.validateName(projectFieldContents,
                IResource.PROJECT);
        if (!nameStatus.isOK()) {
            setErrorMessage(nameStatus.getMessage());
            return false;
        }
       
        IProject handle = getProjectHandle();
        if (handle.exists()) {
            setErrorMessage(IDEWorkbenchMessages.WizardNewProjectCreationPage_projectExistsMessage);
            return false;
        }
		return dialogChanged();
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
			projectNameField.setFocus();
		}
    }

    public boolean isSafed() {
    	return isSafed;
    }
    
    public String getAPICloudProjectName() {
    	APICloudProject project = (APICloudProject)defaultLabel.getData();
    	return project.getId();
    }
}
