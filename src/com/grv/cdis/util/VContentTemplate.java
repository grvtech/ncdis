package com.grv.cdis.util;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
@SuppressWarnings("unchecked")
public class VContentTemplate extends TagSupport {
	private String lang = "";
	private String bundle = "";
	private String contentPath = "/WEB-INF/content/template/";
	private static final long serialVersionUID = -6588924732317625195L;
	
	public void setLang(String lang) {
		if(lang != null && !lang.equals(""))
			this.lang = lang;
	}
	
	public void setBundle(String bundle) {
		if(bundle != null && !bundle.equals(""))
			this.bundle = bundle;
	}
	
	
	
	
	private String getContent(File fileContent,Hashtable infos){
		String result = "no body";
		
		try {
			BufferedReader bflr = new BufferedReader(new FileReader(fileContent));
			result="";
			
			while(bflr.ready()){
				String linie = bflr.readLine();				
				int cpt=0;
				while ((cpt=linie.indexOf("{",cpt)) >= 0) {
					String str1=linie.substring(linie.indexOf("{",cpt)+1,linie.indexOf("}",cpt));
					String info = (String) infos.get(str1);	
					//linie=linie.replaceAll("\\{[a-zA-Z_0-9]+\\}",info);
					linie=linie.replaceAll("\\{"+str1+"\\}",info);
					cpt++;
		        }
				
				result+=linie;
			}
			bflr.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	/**
	  * @see Tag#doStartTag()
	  */
	 public String getTemplate(Hashtable infos,String bundle,HttpSession hses) {
		String lang = (String)hses.getAttribute("sessionLanguage");
		String f = contentPath+bundle+"_"+lang+".template";
		String template = "no template";
		File fileContent = new File(hses.getServletContext().getRealPath(f));
		if(!fileContent.exists()){
			
		}else{
			template = getContent(fileContent,infos);		
			
			
		}
	    return template;
	 }

	
}


