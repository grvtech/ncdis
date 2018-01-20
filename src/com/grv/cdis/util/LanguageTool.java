package com.grv.cdis.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class LanguageTool {
	private String error_en = "error_en.properties";
	private String error_fr = "error_fr.properties";
	private File errorenFile = null;
	private File errorfrFile = null;
	
	public LanguageTool(){
		Context initContext;
		 String result = "";
		 try {
			initContext = new InitialContext();
			Context envContext  = (Context)initContext.lookup("java:comp/env");
			String rootPath = (String)envContext.lookup("root-folder");
			errorenFile = new File(rootPath+error_en);
			errorfrFile = new File(rootPath+error_fr);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static File getErrorFile(String language){
		File result = null;
		Context initContext;
		 try {
			initContext = new InitialContext();
			//Context envContext  = (Context)initContext.lookup("java:comp/env");
			String rootPath = (String)initContext.lookup("root-folder");
			result = new File(rootPath+"/content/errors_"+language+".properties");
		}catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public File getErrorenFile() {
		return errorenFile;
	}



	public void setErrorenFile(File errorenFile) {
		this.errorenFile = errorenFile;
	}



	public File getErrorfrFile() {
		return errorfrFile;
	}



	public void setErrorfrFile(File errorfrFile) {
		this.errorfrFile = errorfrFile;
	}

	public static String getError(String eCode, String lang) {
		String result = "";
		try {
			File file = getErrorFile(lang);
			FileInputStream fileInput = new FileInputStream(file);
			Properties properties = new Properties();
			properties.load(fileInput);
			fileInput.close();

			Enumeration<Object> enuKeys = properties.keys();
			while (enuKeys.hasMoreElements()) {
				String key = (String) enuKeys.nextElement();
				String value = properties.getProperty(key);
				if(key.equals(eCode)){
					result = value;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
}
