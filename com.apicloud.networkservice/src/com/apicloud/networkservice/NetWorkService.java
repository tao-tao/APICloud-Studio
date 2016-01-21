package com.apicloud.networkservice;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Properties;


public class NetWorkService {
	
	public static NetWorkService network_instance = null;
	private static final String PROT = ":80";
	
	public synchronized static NetWorkService getInstance() {
		if (network_instance == null) {
			return new NetWorkService();
		} else {
			return network_instance;
		}
	}
	
	public String checkServiceState(String ip){
		String url="http://" + ip + PROT+"/ide/testConnectionMsg";
		String message=ConnectionUtil.getConnectionStatus(
				url, null);
		return message;
	}
	
	public  URLConnection checkDevUser( String username,
			String password,String ip) throws IOException{

		URL url = null;
		try {
			url = new URL("http://" + ip + PROT + "/checkDevUser");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} //$NON-NLS-2$

		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestProperty(
				"Content-Type", "application/x-www-form-urlencoded"); //$NON-NLS-2$
		OutputStreamWriter writer = new OutputStreamWriter(
				conn.getOutputStream(), "UTF-8");
		writer.write(ConnectionUtil.encodeLoginData(username, password)); //$NON-NLS-2$
		writer.flush();
		writer.close();
		return conn;
	}
	
	public  String getSvnList(String userName,String cookie,String ip){
		String url = "http://" + ip + PROT+"/getSvnList?useDomain";
		String message = ConnectionUtil.sendPostRequest(url,
				ConnectionUtil.encodeGetSVNParam(userName), cookie);
		return message;
	}
	public  String getConectionMsg(String ip){
		String message = ConnectionUtil.getConnectionMessage(
				"http://" + ip + PROT, null); //$NON-NLS-2$
		return message;
	}
	public  String addUnpack(String id,String cookie,String ip){
		String param = "";
		param = "appId=" + id + "&" + "loader=1";
		String message = ConnectionUtil.sendPostRequest("http://" + ip
				+ "/addUnpack", param, cookie);
		return message;
	}
	
	public  String getLoaderState(String pkgId,String cookie,String ip) throws UnsupportedEncodingException{
		String message = ConnectionUtil.sendPostRequest(
				"http://" +ip + "/getLoaderState", "pkgId="
						+ URLEncoder.encode(pkgId, "UTF-8"),
				cookie);
		return message;
	}
	
	public  String validateUser(String name,String nameText,boolean issafed,String userName,String userPassWord,String cookie,String ip){
		String url = "http://" +ip + ":80/api/ide/app?useDomain";
		
		String message = ConnectionUtil.sendPostRequest(url, ConnectionUtil
				.encodeAppIdData(name, nameText,
						issafed ? 1 : 0, userName,
						MD5Util.String2MD5(userPassWord)), cookie);
		return message;
	}
	
	public  String sigInMsg(String userName,String password,String cookie,String ip){
		 String url = "http://" + ip + PROT+"/ide/forum/cloud/coin"; 
		 String message = ConnectionUtil.sendPostRequest(url, ConnectionUtil.encodeSigninData(userName, RC4Util.HloveyRC4(password, RC4Util.key)), cookie);
		return message;
	}
	public  String getUpdateMsg(String thirdVersion,String adbVersion,String anbVersion,String basePath,String ip){
		
		String url = "http://"
				+ip
				+ "/api/ide/component/versionV2?"
				+ "thirdVersion="
				+ thirdVersion
				+ "&aloaderVersion="
				+ FileUtil.readVersion(adbVersion)
				+ "&iloaderVersion="
				+ FileUtil.readVersion(anbVersion) + "&basicVersion="
				+ FileUtil.readVersion(basePath);
		
		String message = ConnectionUtil.getConnectionMessage(url, null);
		return message;
	}
	
	private boolean isJava_32bit() {
		Properties props = System.getProperties();
		Iterator iter = props.keySet().iterator();
		String bitNum = null;
		while (iter.hasNext()) {
			String key = (String)iter.next();
			if (key.equals("sun.arch.data.model"))
				bitNum = "-d"+props.getProperty(key);
		}
		if (bitNum.equals("-d32")) 
			return true;
		return false;
	}
	
	public  String getMAcUpdateMsg(String thirdVersion,String adbVersion,String anbVersion,String basePath,String ip){
		String basic = "&basic_macVersion=";
		if (isJava_32bit())
			basic = "&basic_32macVersion=";
		String url = "http://"
				+ ip
				+ "/api/ide/component/versionV2?"
				+ "third_macVersion="
				+ thirdVersion
				+ "&aloaderVersion="
				+ FileUtil.readVersion(adbVersion)
				+ "&iloaderVersion="
				+ FileUtil.readVersion(anbVersion) + basic
				+ FileUtil.readVersion(basePath);
		
		String message = ConnectionUtil.getConnectionMessage(url, null);
		return message;
	}
	
	public String getUpdateLoaderMsg(String thirdVersion,String ip){
		String url= "http://"
				+ ip
				+ "/api/ide/component/versionV2?"
				+ "thirdVersion="
				+ thirdVersion
				+ "&aloaderVersion=1.0.0"
				+ "&iloaderVersion=1.0.0";
		String message=ConnectionUtil.getConnectionMessage(url,null);
		return message;
	}
	
	public  String getFeatureInfo(String cookie,String ip){
		String url = "http://" + ip  + ":80/getIDEModule";
		String message = ConnectionUtil.getConnectionMessage(url, cookie);
		return message;
	}
	
}
