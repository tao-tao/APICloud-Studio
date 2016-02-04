/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.commons.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.eclipse.jface.viewers.TreeNode;

import com.apicloud.commons.util.StringUtils;
import com.apicloud.commons.util.UtilActivator;
import com.apicloud.commons.util.XMLUtil;

@SuppressWarnings("unchecked")
public class Config {
	private String id;
	private String version;
	private String name;
	private String desc;
	private String authorName;
	private String authorEmail;
	private String authorHref;
	private String contentSrc;
	private List<Access> accesses = new ArrayList<Access>();
	private List<Preference> preferences = new ArrayList<Preference>();
	private List<Permission> permissions = new ArrayList<Permission>();
	private List<Feature> features = new ArrayList<Feature>();
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getAuthorName() {
		return authorName;
	}
	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}
	public String getAuthorEmail() {
		return authorEmail;
	}
	public void setAuthorEmail(String authorEmail) {
		this.authorEmail = authorEmail;
	}
	public String getAuthorHref() {
		return authorHref;
	}
	public void setAuthorHref(String authorHref) {
		this.authorHref = authorHref;
	}
	public String getContentSrc() {
		return contentSrc;
	}
	public void setContentSrc(String contentSrc) {
		this.contentSrc = contentSrc;
	}
	
	public List<Access> getAccesses() {
		return accesses;
	}
	public void addAccess(Access access) {
		accesses.add(access);
	}
	public void removeAccess(Access access) {
		accesses.remove(access);
	}
	
	public List<Feature> getFeatures() {
		return features;
	}
	public void addFeature(Feature feature) {
		features.add(feature);
	}
	public void removeFeature(Feature feature) {
		features.remove(feature);
	}
	
	public List<Preference> getPreferences() {
		return preferences;
	}
	public void addPreference(Preference preference) {
		preferences.add(preference);
	}
	public void removePreference(Preference preference) {
		preferences.remove(preference);
	}
	
	public List<Permission> getPermissions() {
		return permissions;
	}
	public void addPermission(Permission permission) {
		permissions.add(permission);
	}
	public void removePermission(Permission permission) {
		permissions.remove(permission);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 17;
		result = prime * result + ((accesses == null) ? 0 : accesses.hashCode());
		result = prime * result
				+ ((authorEmail == null) ? 0 : authorEmail.hashCode());
		result = prime * result
				+ ((authorHref == null) ? 0 : authorHref.hashCode());
		result = prime * result
				+ ((authorName == null) ? 0 : authorName.hashCode());
		result = prime * result
				+ ((contentSrc == null) ? 0 : contentSrc.hashCode());
		result = prime * result + ((desc == null) ? 0 : desc.hashCode());
		result = prime * result
				+ ((features == null) ? 0 : features.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((preferences == null) ? 0 : preferences.hashCode());
		result = prime * result
				+ ((permissions == null) ? 0 : permissions.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Config other = (Config) obj;
		if (accesses == null) {
			if (other.accesses != null)
				return false;
		} else if (!accesses.equals(other.accesses))
			return false;
		if (authorEmail == null) {
			if (other.authorEmail != null)
				return false;
		} else if (!authorEmail.equals(other.authorEmail))
			return false;
		if (authorHref == null) {
			if (other.authorHref != null)
				return false;
		} else if (!authorHref.equals(other.authorHref))
			return false;
		if (authorName == null) {
			if (other.authorName != null)
				return false;
		} else if (!authorName.equals(other.authorName))
			return false;
		if (contentSrc == null) {
			if (other.contentSrc != null)
				return false;
		} else if (!contentSrc.equals(other.contentSrc))
			return false;
		if (desc == null) {
			if (other.desc != null)
				return false;
		} else if (!desc.equals(other.desc))
			return false;
		if (features == null) {
			if (other.features != null)
				return false;
		} else if (!features.equals(other.features))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (preferences == null) {
			if (other.preferences != null)
				return false;
		} else if (!preferences.equals(other.preferences))
			return false;
		if (permissions == null) {
			if (other.permissions != null)
				return false;
		} else if (!permissions.equals(other.permissions))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}
	public static Config loadXml(String xml) {
		try {
			return loadXml(new ByteArrayInputStream(xml.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	public static Config loadXml(InputStream input) {
		Config config= new Config();
        if (input == null) {
            return null;
        }
        Document document = null;
        try {
            document = XMLUtil.loadXmlFile(input);
            if(input != null) {
            	input.close();
            }
        } catch (DocumentException e) {
        	e.printStackTrace();
        	return null;
        }catch (IOException e) {
			e.printStackTrace();
			return null;
		}
        Element rootElement = document.getRootElement();
        String id = rootElement.attributeValue("id");
        String version = rootElement.attributeValue("version");
        config.setId(id);
        config.setVersion(version);
        parseGenral(rootElement, config);
        
        List<Element> preferenceElementList = rootElement.elements("preference");
        List<Preference> preferences  = parsePreference(preferenceElementList);
        config.getPreferences().addAll(preferences);
        
        List<Element> accessElementList = rootElement.elements("access");
        List<Access> accesses  = parseAccess(accessElementList);
        config.getAccesses().addAll(accesses);
        
        List<Element> permissionElementList = rootElement.elements("permission");
        List<Permission> permissions  = parsePermission(permissionElementList);
        config.getPermissions().addAll(permissions);
        
        List<Element> featureElementList = rootElement.elements("feature");
        List<Feature> features  = parseFeature(featureElementList);
        config.getFeatures().addAll(features);
        
        return config;
    }
	
	private static void parseGenral(Element rootElement, Config config) {
		  Element nameElement = rootElement.element("name");
		  String name = nameElement.getText();
		  config.setName(name);
		  
		  Element descriptionElement = rootElement.element("description");
		  String description = descriptionElement.getText();
		  config.setDesc(description);
		  
		  Element authorElement = rootElement.element("author");
		  String authorName = authorElement.getText();
		  String authorEmail = authorElement.attributeValue("email");
		  String authorHref = authorElement.attributeValue("href");
		  config.setAuthorHref(authorHref);
		  config.setAuthorEmail(authorEmail);
		  config.setAuthorName(authorName);
		  
		  Element contentElement = rootElement.element("content");
		  String content = contentElement.attributeValue("src");
		  config.setContentSrc(content);
	}
	private static List<Preference> parsePreference(	List<Element> preferenceElementList) {
		List<Preference> preferences = new ArrayList<Preference>();
		  for (Element pref : preferenceElementList) {
			  	Preference preference = new Preference();
	            String name = pref.attributeValue("name");
	            String value = pref.attributeValue("value");
	            preference.setName(name);
	            preference.setValue(value);
	            preferences.add(preference);
		  }
		return preferences;
	}
	
	private static List<Access> parseAccess(List<Element> accessElementList) {
		List<Access> accesses = new ArrayList<Access>();
		  for (Element acs : accessElementList) {
			  	Access access = new Access();
	            String origin = acs.attributeValue("origin");
	            access.setOrigin(origin);
	            accesses.add(access);
		  }
		return accesses;
	}
	
	private static List<Permission> parsePermission(List<Element> permissionElementList) {
		List<Permission> permissions = new ArrayList<Permission>();
		  for (Element pref : permissionElementList) {
			  	Permission permission = new Permission();
	            String name = pref.attributeValue("name");
	            permission.setName(name);
	            permissions.add(permission);
		  }
		return permissions;
	}
	
	private static List<Feature> parseFeature(List<Element> featureElementList) {
		List<Feature> features = new ArrayList<Feature>();
		 for (Element pref : featureElementList) {
			 Feature feature = new Feature();
	            String name = pref.attributeValue("name");
	            feature.setName(name);
	            features.add(feature);
				List<Element> paramElementList = pref.elements("param");
	            parseParam(paramElementList, feature);
		  }
		return features;
	}
	private static void parseParam(List<Element> paramElementList,
			Feature feature) {
		List<Param> params = new ArrayList<Param>();
		for(Element param : paramElementList) {
			Param pa = new Param();
			String name = param.attributeValue("name");
			String value = param.attributeValue("value");
			pa.setName(name);
			pa.setValue(value);
			params.add(pa);
		}
		feature.getParams().addAll(params);
	}
	
	public static Document getDocument(Config config) {
		    Document document = XMLUtil.createDocument();
	        Element rootElement = document.addElement("widget");
	        rootElement.addAttribute("id", config.getId());
	        rootElement.addAttribute("version", config.getVersion());
	        createGenralElement(rootElement, config);
	        createPreferenceElement(rootElement, config);
	        createPermissionElement(rootElement, config);
	        createAccessElement(rootElement, config);
	        createFeatureElement(rootElement, config);
		 return document;
	}
	
	public static String getDocumentContent(Config config){
		return getDocument(config).asXML();
	}
	
	public static File saveXml(Config config, File file) {
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Document document = XMLUtil.createDocument();
		Element rootElement = document.addElement("widget");
		rootElement.addAttribute("id", config.getId());
		rootElement.addAttribute("version", config.getVersion());
		createGenralElement(rootElement, config);
		createPreferenceElement(rootElement, config);
		createPermissionElement(rootElement, config);
		createAccessElement(rootElement, config);
		createFeatureElement(rootElement, config);
		try {
			XMLUtil.saveXml(file, document);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}
	
	private static void createGenralElement(Element rootElement, Config config) {

        Element nameElement = rootElement.addElement("name");
        nameElement.setText(config.getName());
        
        Element descriptionElement = rootElement.addElement("description");
        descriptionElement.setText(config.getDesc());
        
        Element authorElement = rootElement.addElement("author");
        authorElement.addAttribute("email", config.getAuthorEmail());
        authorElement.addAttribute("href", config.getAuthorHref());
        authorElement.setText(config.getAuthorName());
        
        Element contentElement = rootElement.addElement("content");
        contentElement.addAttribute("src", config.getContentSrc());

	}
	
	private static void createPreferenceElement(Element rootElement,
			Config config) {
		for(Preference preference : config.getPreferences()) {
			Element preferenceElement = rootElement.addElement("preference");
			preferenceElement.addAttribute("name", preference.getName());
			preferenceElement.addAttribute("value", preference.getValue());
		}
	}
	
	private static void createAccessElement(Element rootElement, Config config) {
		for(Access access : config.getAccesses()) {
			Element accessElement = rootElement.addElement("access");
			accessElement.addAttribute("origin", access.getOrigin());
		}
		
	}
	
	private static void createPermissionElement(Element rootElement,
			Config config) {
		for(Permission permission : config.getPermissions()) {
			Element permissionElement = rootElement.addElement("permission");
			permissionElement.addAttribute("name", permission.getName());
		}
	}
	
	private static void createFeatureElement(Element rootElement,
			Config config) {
		for(Feature feature : config.getFeatures()) {
			Element featureElement = rootElement.addElement("feature");
			featureElement.addAttribute("name", feature.getName());
			createParamElement(featureElement, feature);
		}
	}
	
	private static void createParamElement(Element featureElement,
			Feature feature) {
		for(Param param : feature.getParams()) {
			Element paramElement = featureElement.addElement("param");
			paramElement.addAttribute("name", param.getName());
			paramElement.addAttribute("value", param.getValue());
		}
		
	}
	
	public TreeNode[] createTreeNode() {
		TreeNode components[] = new TreeNode[getFeatures().size()];
		for (int i = 0; i < getFeatures().size(); i++) {
			TreeNode component = new TreeNode(getFeatures().get(i));
			component.setChildren(getFeatures().get(i).createTreeNode(component));
			components[i] = component;
		}
		return components;
    }

	public boolean check() {
		if(id == null ||StringUtils.EMPTY_STRING.equals(id)) {
			UtilActivator.logger.info("id false");
			return false;
		}
		if(name == null ||StringUtils.EMPTY_STRING.equals(name)) {
			UtilActivator.logger.info("name false");
			return false;
		}
		if(authorName == null ||StringUtils.EMPTY_STRING.equals(authorName)) {
			UtilActivator.logger.info("authorName false");
			return false;
		}
		if(contentSrc == null ||StringUtils.EMPTY_STRING.equals(contentSrc)) {
			UtilActivator.logger.info("contentSrc false");
			return false;
		}
		if(authorEmail == null ||StringUtils.EMPTY_STRING.equals(authorEmail)) {
			UtilActivator.logger.info("authorEmail false");
			return false;
		}
		if(authorHref == null ||StringUtils.EMPTY_STRING.equals(authorHref)) {
			UtilActivator.logger.info("authorHref false");
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return name;
	}
}
