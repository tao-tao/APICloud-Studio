package com.apicloud.networkservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;


public class ConnectionUtil {
	private static boolean isTrue;
	public static String encodeLoginData(String userName, String passWord) {
		String data = null;
		try {
			data = "userCode=" + URLEncoder.encode(userName, "UTF-8") + "&"
					+ "userPwd=" + URLEncoder.encode(passWord, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		return data;
	}

	public static String encodeSigninData(String userName, String passWord) {
		String data = null;
		try {
			data = "email=" + URLEncoder.encode(userName, "UTF-8") + "&"
					+ "password=" + URLEncoder.encode(passWord, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		return data;
	}

	public static String encodeGetSVNParam(String userName) {
		String data = null;
		try {
			data = "enterUserId=" + URLEncoder.encode(userName, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		return data;
	}

	public static String encodeAppIdData(String appName, String desc,
			int appMsmControl, String userName, String userPassWord) {
		String data = null;
		try {
			data = "name=" + URLEncoder.encode(appName, "UTF-8") + "&"
					+ "desc="
					+ URLEncoder.encode(String.valueOf(desc), "UTF-8") + "&"
					+ "key=" + URLEncoder.encode(userName, "UTF-8") + "&"
					+ "password=" + URLEncoder.encode(userPassWord, "UTF-8")
					+ "&" + "msm="
					+ URLEncoder.encode(String.valueOf(appMsmControl), "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		return data;
	}

	public static String encodeSessionToken(String session) {
		String data = null;
		try {
			data = "sessionToken=" + URLEncoder.encode(session, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		return data;
	}

	public static void main(String[] args) {
		System.out
				.println(needShowPage("http://114.242.213.46/ide/ideStart.html"));
	}

	public static Boolean needShowPage(String data) {
		URL url = null;
		HttpURLConnection conn = null;
		try {
			url = new URL(data);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			String code = new Integer(conn.getResponseCode()).toString();
			if (code.equals("304")) {
				return true;
			}
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}
		return false;

	}	
	
	public static String getConnectionStatus(String data, String cookie){

		URL url = null;
		HttpURLConnection conn = null;
		String returncode="";
		try {
			url = new URL(data);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(20000);
			// 接收数据
			conn.getErrorStream();
		 	returncode=String.valueOf(conn.getResponseCode());
		} catch (NoRouteToHostException e) {
			e.printStackTrace();
			return "{ status:\"0\", msg:\"\u65E0\u6CD5\u8FDE\u63A5\u5230\u4E91\u670D\u52A1\"}";
		} catch (ConnectException e) {
			e.printStackTrace();
			return "{ status:\"0\", msg:\"Connection timed out\"}";
		} catch (SocketTimeoutException e) {
			//处理连接超时
			if(!isTrue()){
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(null, "INFO", "TIMEOUT");
					setTrue(true);
				}
			});
			return "{ status:\"0\", msg:\"\u65E0\u6CD5\u8FDE\u63A5\u5230\u4E91\u670D\u52A1\"}";
		}
			return "{ status:\"0\", msg:\"server error,please wait\"}";
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return returncode;
	}
	
	public static String getConnectionMessage(String data, String cookie) {
		URL url = null;
		HttpURLConnection conn = null;
		String line = "";
		String sTotalString = "";
		try {
			url = new URL(data);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(20000);
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			if (cookie != null) {
				conn.setRequestProperty("Cookie", cookie);
			}
			// 接收数据
			conn.getErrorStream();
			conn.getResponseCode();
			InputStream input = conn.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					input, "utf-8"));
			while ((line = reader.readLine()) != null) {
				sTotalString += line;
			}
			reader.close();
		} catch (NoRouteToHostException e) {
			e.printStackTrace();
			return "{ status:\"0\", msg:\"\u65E0\u6CD5\u8FDE\u63A5\u5230\u4E91\u670D\u52A1\"}";
		} catch (ConnectException e) {
			// MessageDialog.openError(null, "server error", "please wait ");
			e.printStackTrace();
			return "{ status:\"0\", msg:\"Connection timed out\"}";
		} catch (SocketTimeoutException e) {
			// MessageDialog.openError(null, "server error", "please wait ");
			//处理连接超时
			if(!isTrue()){
				Display.getDefault().syncExec(new Runnable() {
					
					public void run() {
						MessageDialog.openInformation(null, "INFO", "TIMEOUT");
						setTrue(true);
					}
				});
				return "{ status:\"0\", msg:\"\u65E0\u6CD5\u8FDE\u63A5\u5230\u4E91\u670D\u52A1\"}";
			}
			return "{ status:\"0\", msg:\"server error,please wait\"}";
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (conn != null)
				conn.disconnect();
		}
		return sTotalString;
	}

	public static String sendPostRequest(String uri, String param, String cookie) {

		// Build parameter string
		HttpURLConnection conn = null;
		try {
			// Send the request
			URL url = new URL(uri);
			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(5000);
			conn.setConnectTimeout(5000);
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			if (cookie != null) {
				conn.setRequestProperty("Cookie", cookie);
			}
			OutputStreamWriter writer = new OutputStreamWriter(
					conn.getOutputStream(), "UTF-8");

			// write parameters
			writer.write(param);
			writer.flush();

			// Get the response
			StringBuffer answer = new StringBuffer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					conn.getInputStream(), "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				answer.append(line);
			}
			writer.close();
			reader.close();

			// Output the response
			return answer.toString();
		} catch(SocketTimeoutException ex){
			return "{ status:\"0\", msg:\"\u65E0\u6CD5\u8FDE\u63A5\u5230\u4E91\u670D\u52A1\"}";
		}
		
		catch (NoRouteToHostException ex) {
			ex.printStackTrace();
			return "{code:\"0\",  status:\"0\", msg:\"\u65E0\u6CD5\u8FDE\u63A5\u5230\u4E91\u670D\u52A1\"}";
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
			return "{code:\"0\",  status:\"0\", msg:\"MalformedURLException\"}";
		} catch (SocketException ex) {
			ex.printStackTrace();
			return "{code:\"0\",  status:\"0\", msg:\"Unexpected end of file from server!\"}";
		} catch (IOException ex) {

			ex.printStackTrace();
			return "{code:\"0\",  status:\"error\", msg:\"service is not start!\"}";
		}
		
		finally {
			if (conn != null)
				conn.disconnect();
		}
		
	}

	public static int resourceSize(String filePath) throws IOException {
		URL url = new URL(filePath);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setReadTimeout(5000);
		if (conn.getResponseCode() == 200) {
			int fileSize = conn.getContentLength();
			return fileSize;
		}
		return 0;
	}

	public static boolean isTrue() {
		return isTrue;
	}

	public static void setTrue(boolean isTrue) {
		ConnectionUtil.isTrue = isTrue;
	}
}
