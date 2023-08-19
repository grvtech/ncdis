package com.grv.cdis.services;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.grv.cdis.controler.Reports;
import com.grv.cdis.db.CdisDBridge;
import com.grv.cdis.db.ChbDBridge;
import com.grv.cdis.model.Action;
import com.grv.cdis.model.Event;
import com.grv.cdis.model.MessageResponse;
import com.grv.cdis.model.Note;
import com.grv.cdis.model.Renderer;
import com.grv.cdis.model.Report;
import com.grv.cdis.model.ReportCriteria;
import com.grv.cdis.model.ReportSubcriteria;
import com.grv.cdis.model.ScheduleVisit;
import com.grv.cdis.model.Session;

//import com.grv.cdis.model.SearchPatient;
import com.grv.cdis.model.User;
import com.grv.cdis.util.FileTool;
import com.grv.cdis.util.ImportNames;
import com.grv.cdis.util.MailTool;
import com.grv.cdis.util.SecurityTool;


public class ActionProcessor {
	
	/*
	 * /ncdis/service/action/login?username=XXXXX&password=&language=en|fr
	 * */
	public String loginSession(Hashtable<String, String[]> args){
		Gson json = new Gson();
		String result = "";
		String username = ((String[])args.get("username"))[0];
		String password = ((String[])args.get("password"))[0];
		String language = ((String[])args.get("language"))[0];
		String reswidth = ((String[])args.get("reswidth"))[0];
		String resheight = ((String[])args.get("resheight"))[0];
		
		
		
		String encPassword = "";
		String clearPassword = "";
		try {
			clearPassword = new String(Base64.decodeBase64(password), "UTF-8");
			encPassword = SecurityTool.encryptPassword(clearPassword);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		User user = new User(username, encPassword);
		Action act = new Action("LOGIN");
		Session userSession = null;
		
		if(user.isValid()){
			String ip = ((String[])args.get("ipuser"))[0];

			String combination = ip+user.getUsername()+ (new Date()).toString();
			String idsession = DigestUtils.md5Hex(combination);

			userSession = new Session(idsession, user.getIduser(), ip, 0, 0, Integer.parseInt(reswidth),Integer.parseInt(resheight),1);
			
			userSession.setSession();
			
			ArrayList<Object> obs = new ArrayList<>();
			obs.add(user);
			result = json.toJson(new MessageResponse(act,true,language,obs));
			
			Event.registerEvent(user.getIduser(), act.getIdaction(), 1, userSession.getIdsession());
		}else{
			result = json.toJson(new MessageResponse(act,false,language,null));
		}
		
		return result;
	}
	
	
	/*
	 * /ncdis/service/action/search?text=XXXXX&criteria=chart|name|ramq&language=en|fr
	 * */
	
	public String search(Hashtable<String, String[]> args){
		Gson json = new Gson();
		CdisDBridge cdisdb = new CdisDBridge();
		String result = "";
		String searchText = ((String[])args.get("text"))[0];
		String criteria = ((String[])args.get("criteria"))[0];
		String language = ((String[])args.get("language"))[0];
		// criteria can be name|chart|ramq
		Action act = new Action("SEARCH"); 
		if(searchText != null && !searchText.equals("") ){
			//ArrayList<SearchPatient> patientList = cdisdb.getSearchPatientsByCriteria(searchText,criteria);
			//result = json.toJson(patientList);
		}else{
			result = json.toJson(new MessageResponse(act,false,language,null));
		}
		return result;
	}
	
	
	/*
	 * /ncdis/service/action/getReports?sid=XXXXX&language=en|fr
	 * */
	
	public String getReports(Hashtable<String, String[]> args){
		Gson json = new Gson();
		ChbDBridge chbdb = new ChbDBridge();
		String result = "";
		String sid = ((String[])args.get("sid"))[0];
		String language = ((String[])args.get("language"))[0];
		// criteria can be name|chart|ramq
		HashMap<String, String> userData  = chbdb.getUser(sid);
		
		ArrayList<Object> obs = new ArrayList<Object>();
		HashMap<String, ArrayList<Report>> reports = Reports.getUserReports(userData.get("role_code"), userData.get("idcommunity"), userData.get("iduser"));
		obs.add(reports);
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}

	
	
	public String executeReport3CustomValue(Hashtable<String, String[]> args){
		Gson json = new Gson();
		CdisDBridge db = new CdisDBridge();
		String result = "";
		JsonParser jp = new JsonParser();
		
		String language = ((String[])args.get("language"))[0];
		String cvalue = "0";
		if(args.get("cvalue") != null){
			cvalue = ((String[])args.get("cvalue"))[0];
		}
		
		String idcommunity = "0";
		if(args.get("idcommunity") != null){
			idcommunity = ((String[])args.get("idcommunity"))[0];
		}
		String dtype = "0";
		if(args.get("dtype") != null){
			dtype = ((String[])args.get("dtype"))[0];
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		
		
		
	    //Hashtable<String, Object> reportObject = new Hashtable<>();
	    
	    Hashtable<String, Object> reportObject = db.a1cReportCustomValue(cvalue,idcommunity,dtype);
	    
	    //reportObject.put("dataset", set);
	    //reportObject.put("header", header);
		ArrayList<Object> obs = new ArrayList<Object>();
		obs.add(reportObject);
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	
	public String executeReport4CustomValue(Hashtable<String, String[]> args){
		Gson json = new Gson();
		CdisDBridge db = new CdisDBridge();
		String result = "";
		JsonParser jp = new JsonParser();
		
		String language = ((String[])args.get("language"))[0];
		
		String pvalue = "0";
		if(args.get("pvalue") != null){
			pvalue = ((String[])args.get("pvalue"))[0];
		}
		
		String sens = "0";
		if(args.get("sens") != null){
			sens = ((String[])args.get("sens"))[0];
		}
		
		String idcommunity = "0";
		if(args.get("idcommunity") != null){
			idcommunity = ((String[])args.get("idcommunity"))[0];
		}
		
		String dtype = "0";
		if(args.get("dtype") != null){
			dtype = ((String[])args.get("dtype"))[0];
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		
		
		
	    //Hashtable<String, Object> reportObject = new Hashtable<>();
	    
	    Hashtable<String, Object> reportObject = db.ldlReportCustomValue(sens,pvalue,idcommunity,dtype);
	    
	    //reportObject.put("dataset", set);
	    //reportObject.put("header", header);
		ArrayList<Object> obs = new ArrayList<Object>();
		obs.add(reportObject);
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	
	
	public String executeReport(Hashtable<String, String[]> args){
		Gson json = new Gson();
		CdisDBridge db = new CdisDBridge();
		String result = "";
		JsonParser jp = new JsonParser();
		String raw = ((String[])args.get("rawPost"))[0];
		String language = ((String[])args.get("language"))[0];
		String repid = "0";
		if(args.get("idreport") != null){
			repid = ((String[])args.get("idreport"))[0];
		}
		String owner = "";
		if(args.get("owner") != null){
			owner = ((String[])args.get("owner"))[0];
		}
		String type = "list";
		if(args.get("type") != null){
			type = ((String[])args.get("type"))[0];
		}
		
		String graphtype = "none";
		if(args.get("graphtype") != null){
			graphtype = ((String[])args.get("graphtype"))[0];
		}
		
		String title = "Custom Report";
		if(args.get("title") != null){
			title = ((String[])args.get("title"))[0];
		}
		
		String subcriteriatype = "multi"; //multi or single multi - set combined with all sub criterias; single set split by subcriteria
		if(args.get("subcriteriatype") != null){
			subcriteriatype = ((String[])args.get("subcriteriatype"))[0];
		}
		
		
		
		/*
		 * cache conditions:
		 * 
		 * is last add data > timestame cache then execute report + store cache
		 * after each import data execute reports with flag store cache directly
		 * 
		 * */
		
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		
		
		
		Gson gson = new Gson();
	    JsonParser parser = new JsonParser();
	    JsonObject jObject = parser.parse(raw).getAsJsonObject();
	    
	    JsonArray jArrayC = jObject.get("criteria").getAsJsonArray();
	    JsonArray jArraySC = jObject.get("subcriteria").getAsJsonArray();

	    ArrayList<ReportCriteria> lcs = new ArrayList<ReportCriteria>();
	    ArrayList<ReportSubcriteria> slcs = new ArrayList<ReportSubcriteria>();

	    
	    String filter = "all";
	    String hcp = "";
	    String hcpid = "";
	   
	    
	    if(jObject.has("filter")){
	    	filter = jObject.get("filter").getAsString();
			hcp = jObject.get("hcp").getAsString();
			hcpid = jObject.get("hcpid").getAsString();
	    }
	    
		
	   
	    
	    for(JsonElement obj : jArrayC ){
	        ReportCriteria cse = gson.fromJson( obj , ReportCriteria.class);
	        cse.loadIddata();
	        lcs.add(cse);
	    }
	    for(JsonElement obj : jArraySC ){
	        ReportSubcriteria scse = gson.fromJson( obj , ReportSubcriteria.class);
	        scse.loadIddata();
        	slcs.add(scse);
	    }
	    ArrayList<String> header = new ArrayList<>();
	    ArrayList<ArrayList<String>> set = new ArrayList<>();
	    //ArrayList<Object> graphdata = new ArrayList<>();
	    
	    if(type.equals("list")){
	    	ArrayList<String> allIdpatients = new ArrayList<>();
	    	
	    	if(filter.equals("all")){
	    		allIdpatients = db.getIdPatients();
	    	}else{
	    		allIdpatients = db.getIdFilterPatients(hcp,hcpid);
	    	}
	    	
	    	ArrayList<String> idpatients = allIdpatients;
	    	
	    	
	    	Hashtable<String, ArrayList<ArrayList<String>>> report = new Hashtable<>();
		    for(int i=0;i<lcs.size();i++){
		    	ReportCriteria rc = lcs.get(i);
		    	
		    	header.add(rc.getDisplay());
		    	if(rc.getDate().equals("yes")){
		    		header.add(rc.getDatedisplay());
		    	}
		    	
		    		//list : count | idpatient | key | value | date 
			    	//graph : count | key | value 
		    		
			    	ArrayList<ArrayList<String>> criteriaSet = db.executeReport(rc, "list", slcs);
			    	
			    	report.put(rc.getName(), criteriaSet);
			    	ArrayList<String> criteriaPatients = new ArrayList<>();
			    	for(int ii=0;ii<criteriaSet.size();ii++){
			    		ArrayList<String> line = criteriaSet.get(ii);
			    		String idp = line.get(1);
			    		if(!criteriaPatients.contains(idp)){
			    			criteriaPatients.add(idp);
			    		}
			    	}
			    	
			    	if(rc.getType().equals("set")){
			    		
			    		int rem = 0;
			    		int iter = 0;
			    		int iterfound = 0;
			    		ArrayList<String> toRemove = new ArrayList<>();
			    		
			    		
			    		for(int k=0;k<idpatients.size();k++){
			    			String idpatientP = idpatients.get(k);
			    			if(!criteriaPatients.contains(idpatientP)){
		    					if(!toRemove.contains(idpatientP)){
		    						toRemove.add(idpatientP);
		    					}
			    			}
			    		}
			    		idpatients.removeAll(toRemove);
			    	}else{
			    		
			    		ArrayList<String> toRemove = new ArrayList<>();
			    		if(criteriaPatients.size() > idpatients.size()){
			    			for(String idpatient : criteriaPatients){
			    				if(!idpatients.contains(idpatient)){
			    					if(!toRemove.contains(idpatient)){
			    						toRemove.add(idpatient);
			    					}
			    				}
			    			}
			    		}
			    		idpatients.removeAll(toRemove);
			    	}
		    }	
		    
		    for(int x=0;x<idpatients.size();x++){
		    	String idpat = idpatients.get(x);
		    	Hashtable<ReportCriteria, ArrayList<ArrayList<String>>> patientMap = new Hashtable<>();
		    	
		    	
		    	for(int y=0;y<lcs.size();y++){
		    		ReportCriteria rcc = lcs.get(y);
		    		String rccName = rcc.getName();
		    		boolean hasCD = false;
		    		if(rcc.getDate().equals("yes")){
		    			hasCD = true;
		    		}
		    		ArrayList< ArrayList<String>>  rcset =  report.get(rccName);
		    		ArrayList< ArrayList<String>>  rcsetPatient =  new ArrayList<>();
		    		for(int z=0;z<rcset.size();z++){
		    			ArrayList<String> rcLine = rcset.get(z);
		    			String rcIdPat = rcLine.get(1);
		    			if(rcIdPat.equals(idpat)){
		    				rcsetPatient.add(rcLine);
		    			}
		    		}
		    		patientMap.put(rcc, rcsetPatient);
		    	}
		    			    	
		    	
		    	//now obtain bigest set
		    	
		    	int bigSet = 0;
		    	Object[] psetColl = patientMap.values().toArray();
		    	for(int xx=0;xx<psetColl.length;xx++){
		    		ArrayList<ArrayList<String>> pset = (ArrayList<ArrayList<String>>)psetColl[xx];
		    		if(pset.size() > bigSet){
		    			bigSet = pset.size();
		    		}
		    	}
		    	
		    	
		    	//now create line
		    	
		    	for(int q=0;q<bigSet;q++){
		    		ArrayList<String> setLine = new ArrayList<>();
		    		for(int qq=0;qq<lcs.size();qq++){
		    			ReportCriteria r = lcs.get(qq);
		    			
		    			ArrayList<ArrayList<String>> rpset = patientMap.get(r);
		    			
		    			if(r.getName().equals("dtype")){
		    				
		    				
		    				if(q >= rpset.size()){
		    					if(rpset.size() == 0){
		    						setLine.add("");
			    					if(r.getDate().equals("yes")){
			    						setLine.add("");
			    					}
		    					}else{
		    						
			    					ArrayList<String> rpsetLine = rpset.get(rpset.size()-1);
			    					setLine.add(rpsetLine.get(3));
			    					if(r.getDate().equals("yes")){
			    						setLine.add(rpsetLine.get(4));
			    					}
		    					}
		    				}else{
		    					ArrayList<String> rpsetLine = rpset.get(q);
		    					setLine.add(rpsetLine.get(3));
		    					if(r.getDate().equals("yes")){
		    						setLine.add(rpsetLine.get(4));
		    					}
		    				}
		    				/**/
		    			}else{
		    				//ArrayList<ArrayList<String>> rpset = patientMap.get(r);
		    				
		    				
		    				if(rpset.size() > 0){
				    			if(r.getSection().equals("1")){
				    				//the set size is 1
				    				ArrayList<String> rpsetLine = rpset.get(0);
				    				setLine.add(rpsetLine.get(3));
				    			}else{
				    				
				    				if(q >= rpset.size()){
				    					setLine.add(" ");
				    					if(r.getDate().equals("yes")){
				    						setLine.add(" ");
				    					}
				    				}else{
				    					ArrayList<String> rpsetLine = rpset.get(q);
				    					setLine.add(rpsetLine.get(3));
				    					if(r.getDate().equals("yes")){
				    						setLine.add(rpsetLine.get(4));
				    					}
				    				}
				    			}
		    				}else{
		    					setLine.add(" ");
		    					if(r.getDate().equals("yes")){
		    						setLine.add(" ");
		    					}
		    				}
		    			}
		    		}
		    		set.add(setLine);
		    	}
		    	/**/
		    	
		    }
		   
		    
	    }else{
	    	//graphdata = getGraphdata
	    	
	    	Hashtable<ReportCriteria, ArrayList<ArrayList<String>>> map = new Hashtable<>();
	    	for(int i=0;i<lcs.size();i++){
	    		ReportCriteria rc = lcs.get(i);
	    		header.add(rc.getDisplay());
	    		ArrayList<ArrayList<String>> criteriaSet = db.executeReport(rc, "graph", slcs);
	    		map.put(rc, criteriaSet);
	    	}
	    	
	    	if(slcs.size() > 0){
	    		
	    		if(subcriteriatype.equals("single")){
		    		for(int j=0;j<slcs.size();j++){
			    		//ReportSubcriteria rsc = slcs.get(j);
		    			ArrayList<String> setLine = new ArrayList<>();
			    		for(int jj=0;jj<lcs.size();jj++){
			    			ArrayList<ArrayList<String>> criteriaSet = map.get(lcs.get(jj));
			    			
			    			if(criteriaSet.size() > 0){
			    				setLine.add(criteriaSet.get(j).get(2));
			    			}else{
			    				setLine.add("0");
			    			}
			    		}
			    		set.add(setLine);
			    	}
	    		}else{
	    			ArrayList<String> setLine = new ArrayList<>();
		    		for(int jj=0;jj<lcs.size();jj++){
		    			ArrayList<ArrayList<String>> criteriaSet = map.get(lcs.get(jj));
		    			if(criteriaSet.size() > 0){
		    				setLine.add(criteriaSet.get(0).get(2));
		    			}else{
		    				setLine.add("0");
		    			}
		    		}
		    		set.add(setLine);
	    		}
	    	}else{
	    		ArrayList<String> setLine = new ArrayList<>();
	    		for(int jj=0;jj<lcs.size();jj++){
	    			ArrayList<ArrayList<String>> criteriaSet = map.get(lcs.get(jj));
	    			if(criteriaSet.size() > 0){
	    				setLine.add(criteriaSet.get(0).get(2));
	    			}else{
	    				setLine.add("0");
	    			}
	    		}
	    		set.add(setLine);
	    	}
	    	
	    	
	    }
	    	    
	    Hashtable<String, Object> reportObject = new Hashtable<>();
	    
	    reportObject.put("dataset", set);
	    reportObject.put("header", header);
		ArrayList<Object> obs = new ArrayList<Object>();
		obs.add(reportObject);
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	
	public String setFrontPageMessage(Hashtable<String, String[]> args){
		Gson json = new Gson();
		ChbDBridge chbdb = new ChbDBridge();
		String result = "";
		String sid = ((String[])args.get("sid"))[0];
		String language = ((String[])args.get("language"))[0];
		String message = "";
		if(args.get("fpmessage") != null){
			message = ((String[])args.get("fpmessage"))[0];
		}

		
		// criteria can be name|chart|ramq
		HashMap<String, String> userData  = chbdb.getUser(sid);
		
		ArrayList<Object> obs = new ArrayList<Object>();
		InitialContext ic;
		try {
			ic = new InitialContext();
			String rf = (String) ic.lookup("root-folder");
			File frontPageFile = new File(rf+System.getProperty("file.separator")+"config"+System.getProperty("file.separator")+"frontpage.config");
			if(!frontPageFile.exists()){
				frontPageFile.createNewFile();
			}
			FileTool.setMessage(frontPageFile.getAbsolutePath(), message);
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//obs.add(reports);
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	

	public String forgotPassword(Hashtable<String, String[]> args){
		String result = "";
		
		Gson json = new Gson();
		ChbDBridge chbdb = new ChbDBridge();
		
		String usernameUser = ((String[])args.get("usernameUser"))[0];
		String emailUser = ((String[])args.get("emailUser"))[0];
		String language = ((String[])args.get("language"))[0];
		String server = ((String[])args.get("server"))[0];
		User u = chbdb.isValidUser(emailUser, usernameUser);
		if(u.isValid()){
		
			chbdb.setResetPassword(u.getIduser(),"1");
			String params = "rst=1&iduser="+u.getIduser(); 
			String url = "https://"+server+"/ncdis/index.html?"+Base64.encodeBase64String(params.getBytes());
			String messagEmail = "<b><p>CDIS Password reset</p></b><p>Hello "+u.getFirstname()+" "+u.getLastname()+"<br> Click on the button below to reset your password<br><br><a href='"+url+"'>Reset Password</a></p>";
			MailTool.sendMailInHtml("CDIS Password Reset", messagEmail, u.getEmail());
			
			ArrayList<Object> obs = new ArrayList<Object>();
			String msg = "You initiated password reset. Click on Reset Password button in the email you received to reset your password.";
			MessageResponse mr = new MessageResponse(true,language,obs);
			mr.setMessage(msg);
			result = json.toJson(mr);
		}else{
			ArrayList<Object> obs = new ArrayList<Object>();
			result = json.toJson(new MessageResponse("FORGOT-FALSE",false,language,obs));
		}
		
		return result;
	}
	
	
	
	
    
	
	public String subscribe(Hashtable<String, String[]> args){
		Gson json = new Gson();
		ChbDBridge chbdb = new ChbDBridge();
		String result = "";
		String firstnameUser = ((String[])args.get("firstnameSub"))[0];
		String lastnameUser = ((String[])args.get("lastnameSub"))[0];
		String idcommunityUser = ((String[])args.get("idprofesionSub"))[0];
		String idprofesionUser = ((String[])args.get("idprofesionSub"))[0];
		String emailUser = ((String[])args.get("emailSub"))[0];
		String language = ((String[])args.get("language"))[0];
		String usernameUser = lastnameUser.toLowerCase().trim()+firstnameUser.toLowerCase().substring(0,1);
		String pass = ((String[])args.get("passwordSub"))[0];
		
		String server = ((String[])args.get("server"))[0];
		String encPassword = "";
		try {
			String clearPassword = new String(Base64.decodeBase64(pass), "UTF-8");
			encPassword = SecurityTool.encryptPassword(clearPassword);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		User u = chbdb.isValidUser(emailUser, usernameUser);
		if(u.isValid()){
			String message = "Subscribe to CDIS \nYour information is already in CDIS database.\n";
			if(u.getReset().equals("1")){
				message+="A reset password was initiated and you should click on Reset Password button in the email that you received.\n If you did not received an email to reset your password click on Forgot Password link to reset your password again.";
			}else if(u.getConfirmmail().equals("1")){
				message+="You must confirm your password in order to log in.\nYou should click on Confirm Email button in the email that you received.\n If you did not received an email to confirm your subscription contact CDIS Admisitrator or send en email to support@grvtech.ca.";
			}else{
				message = "\nIf you forgot your password you should click on forgot password link to reset your password.";
			}
			ArrayList<Object> obs = new ArrayList<Object>();
			MessageResponse mr = new MessageResponse(false,language,obs);
			mr.setMessage(message);
			result = json.toJson(mr);
		}else{
			u.setActive("1");
			u.setEmail(emailUser);
			u.setFirstname(firstnameUser);
			u.setLastname(lastnameUser);
			u.setUsername(usernameUser);
			u.setPassword(encPassword);
			u.setIdcommunity(idcommunityUser);
			u.setIdprofesion(idprofesionUser);
			u.setPhone(u.getPhone());
			u.setReset("0");
			u.setConfirmmail("1");
			int idPendingUser = chbdb.addUser(u);
			
			
			if(chbdb.saveUserProfile(idPendingUser, 1, 2)){
				//MailTool.sendMailText("CDIS New User Subscribe", , "support@grvtech.ca");
				
				String messagEmail = "<b><p>Hello CDIS Administrator</p></b><p>New user is subscribed to CDIS.<br>The user should confirm the email in order to finish subscription.<br>Login to CDIS and go to Users section to view users pending. <br>The administrator can confirm users email in order to activate subscription.<br><b>User Info:</b><br><b>Name :</b> "+u.getFirstname()+" "+u.getLastname()+"<br><b>Username :</b> "+u.getUsername()+"<br><b>User Email :</b> "+u.getEmail()+"<br><br><b>An email will be sent to the user to confirm email and activate subscription.</b></p>";
				MailTool.sendMailInHtml("CDIS New User Subscribe", messagEmail, "admins@grvtech.ca");
				
				String params = "confirm=1&iduser="+idPendingUser; 
				String url = "https://"+server+"/ncdis/index.html?"+Base64.encodeBase64String(params.getBytes());
				String messagEmailUser = "<b><p>Welcome to CDIS</p></b><p>In order to activate your CDIS subscription you should confirm the email.<br><br><b>Click on the button below to confirm your email and activate the subscription</b><br><br><a href='"+url+"'>Confirm Email</a></p>";
				MailTool.sendMailInHtml("CDIS Subscribe", messagEmailUser, u.getEmail());
				
				String message = "Subscribe to CDIS.\nYou will receive an email with a button to confirm email and activate the subscription. ";
				ArrayList<Object> obs = new ArrayList<Object>();
				MessageResponse mr = new MessageResponse(true,language,obs);
				mr.setMessage(message);
				result = json.toJson(mr);
				
			}else{
				ArrayList<Object> obs = new ArrayList<Object>();
				MessageResponse mr = new MessageResponse(false,language,obs);
				result = json.toJson(mr);
			}
		}
		
		return result;
	}
	
	public String confirmUserEmail(Hashtable<String, String[]> args){
		Gson json = new Gson();
		ChbDBridge chbdb = new ChbDBridge();
		String result = "";
		String language = ((String[])args.get("language"))[0];
		String iduser = ((String[])args.get("iduser"))[0];
		String server = ((String[])args.get("server"))[0];
		
		User u = new User(Integer.parseInt(iduser));
		if(u.isValid()){
			chbdb.setEmailConfirm(iduser, "0");
			
			String messagEmail = "<b><p>Hello "+u.getFirstname()+" "+u.getLastname()+"</p></b><p>You email was confirmed with success!<br><br>Use you new credentials to login into CDIS<br><br><a href='https://"+server+"/ncdis'>Login to CDIS</a></p>";
			MailTool.sendMailInHtml("CDIS Email Confirmed Successfully", messagEmail, u.getEmail());
			
			ArrayList<Object> obs = new ArrayList<Object>();
			MessageResponse mr = new MessageResponse(true,language,obs);
			result = json.toJson(mr);
		}else{
			ArrayList<Object> obs = new ArrayList<Object>();
			MessageResponse mr = new MessageResponse(false,language,obs);
			result = json.toJson(mr);
		}
		return result;
	}
	
	public String resetUserPassword(Hashtable<String, String[]> args){
		Gson json = new Gson();
		ChbDBridge chbdb = new ChbDBridge();
		String result = "";
		
		String language = ((String[])args.get("language"))[0];
		String usernameUser = ((String[])args.get("username"))[0];
		String pass = ((String[])args.get("passwordr"))[0];
		String iduser = ((String[])args.get("iduser"))[0];
		String server = ((String[])args.get("server"))[0];
		
		String encPassword = "";
		String clearPassword = "";
		try {
			clearPassword = new String(Base64.decodeBase64(pass), "UTF-8");
			encPassword = SecurityTool.encryptPassword(clearPassword);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		User u = new User(Integer.parseInt(iduser));
		if(u.isValid()){
			u.setPassword(encPassword);
			chbdb.resetUserPassword(u);
			
			String messagEmail = "<b><p>Hello "+u.getFirstname()+" "+u.getLastname()+"</p></b><p>You reset you password with success!<br><br>Use you new credentials to login into CDIS<br><br><a href='https://"+server+"/ncdis'>Login to CDIS</a></p>";
			MailTool.sendMailInHtml("CDIS Password Reset Successfully", messagEmail, u.getEmail());
			
			String message = "You successfully reset your password\n.";
			ArrayList<Object> obs = new ArrayList<Object>();
			MessageResponse mr = new MessageResponse(true,language,obs);
			mr.setMessage(message);
			result = json.toJson(mr);
		}else{
			ArrayList<Object> obs = new ArrayList<Object>();
			MessageResponse mr = new MessageResponse(false,language,obs);
			mr.setMessage("User is invalid");
			result = json.toJson(mr);
		}		
		return result;
	}
	
	/*
	 * one time deal
	 * https://localhost/ncdis/service/action/encodeUsersPasswords
	 * 
	 * */
	/*
	public String encodeUsersPasswords(Hashtable<String, String[]> args){
		Gson json = new Gson();
		ChbDBridge chbdb = new ChbDBridge();
		String result = "";
		
		String server = ((String[])args.get("server"))[0];
		
		ArrayList<Object> obs = User.getUsers();
		
		for(int i=0;i<obs.size();i++){
			String encPassword = "";
			User u = (User)obs.get(i);
			String cpass = u.getPassword();
			//change passwords for all different than 40 characters
			if(cpass.length() != 40){
				if(u.isValid()){
					encPassword = SecurityTool.encryptPassword(cpass);
					u.setPassword(encPassword);
					chbdb.resetUserPassword(u);
					
					ArrayList<Object> objs = new ArrayList<Object>();
					MessageResponse mr = new MessageResponse(true,"en",objs);
					mr.setMessage("Password reset successfully");
					result = json.toJson(mr);
				}
			}
		}
		return result;
	}
	*/
	
	public String getFrontPageMessage(Hashtable<String, String[]> args){
		
		Gson json = new Gson();
		ChbDBridge chbdb = new ChbDBridge();
		String result = "";
		//String sid = ((String[])args.get("sid"))[0];
		String language = ((String[])args.get("language"))[0];
		// criteria can be name|chart|ramq
		//HashMap<String, String> userData  = chbdb.getUser(sid);
		String message = "";
		ArrayList<Object> obs = new ArrayList<Object>();
		InitialContext ic;
		try {
			ic = new InitialContext();
			String rf = (String) ic.lookup("root-folder");
			
			
			File frontPageFile = new File(rf+System.getProperty("file.separator")+"config"+System.getProperty("file.separator")+"frontpage.config");
			
			if(!frontPageFile.exists()){
				frontPageFile.createNewFile();
			}
			message = FileTool.getMessage(frontPageFile.getAbsolutePath());
			
			HashMap<String, String> map = new HashMap<>();
			map.put("message", message);
			obs.add(map);
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(message.equals("")){
			result = json.toJson(new MessageResponse(false,language,obs));
		}else{
			result = json.toJson(new MessageResponse(true,language,obs));
		}
		//obs.add(reports);
		//result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	
	public String getUserActions(Hashtable<String, String[]> args){
		Gson json = new Gson();
		ChbDBridge chbdb = new ChbDBridge();
		String result = "";
		
		String language = ((String[])args.get("language"))[0];
		// criteria can be name|chart|ramq
		ArrayList<ArrayList<String>> userData  = chbdb.getUserActions();
		
		ArrayList<Object> obs = new ArrayList<Object>();
		
		obs.add(userData);
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	public String getUserNotes(Hashtable<String, String[]> args){
		Gson json = new Gson();
		ChbDBridge chbdb = new ChbDBridge();
		String result = "";
		
		String language = ((String[])args.get("language"))[0];
		String sid = ((String[])args.get("sid"))[0];
		
		User user = new User(sid);
		// criteria can be name|chart|ramq
		ArrayList<Note> userNotes  = chbdb.getUserNotes(user.getIduser());
		ArrayList<Object> obs = new ArrayList<Object>();
		obs.add(userNotes);
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	
	public String readPatientNote(Hashtable<String, String[]> args){
		Gson json = new Gson();
		ChbDBridge chbdb = new ChbDBridge();
		String result = "";
		String language = ((String[])args.get("language"))[0];
		String sid = ((String[])args.get("sid"))[0];
		User user = new User(sid);
		String noteid = ((String[])args.get("noteid"))[0];
		
		chbdb.readPatientNote(noteid);
		ArrayList<Object> obs = new ArrayList<Object>();
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	
	public String setEvent(Hashtable<String, String[]> args){
		Gson json = new Gson();
		ChbDBridge chbdb = new ChbDBridge();
		String result = "";
		String language = ((String[])args.get("language"))[0];
		String sid = ((String[])args.get("sid"))[0];
		String eventCode = ((String[])args.get("eventcode"))[0];
		User user = new User(sid);
		Action a = new Action(eventCode);
		Session session = chbdb.isValidSession(sid);
		if(session != null){
			session.setSession();
		}
		chbdb.setEvent(user.getIduser(), a.getIdaction(), 1, sid);
		ArrayList<Object> obs = new ArrayList<Object>();
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	
	public String deletePatientNote(Hashtable<String, String[]> args){
		Gson json = new Gson();
		ChbDBridge chbdb = new ChbDBridge();
		String result = "";
		String language = ((String[])args.get("language"))[0];
		String sid = ((String[])args.get("sid"))[0];
		User user = new User(sid);
		String noteid = ((String[])args.get("noteid"))[0];
		
		chbdb.deletePatientNote(noteid);
		ArrayList<Object> obs = new ArrayList<Object>();
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	
	public String getUserActionsTop5Dataset(Hashtable<String, String[]> args){
		Gson json = new Gson();
		ChbDBridge chbdb = new ChbDBridge();
		String result = "";
		String language = ((String[])args.get("language"))[0];
		ArrayList<ArrayList<Object>> userData  = chbdb.getUserActionsTop5Dataset();
		ArrayList<Object> obs = new ArrayList<Object>();
		obs.add(userData);
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	
	public String getUserTop5Dataset(Hashtable<String, String[]> args){
		Gson json = new Gson();
		ChbDBridge chbdb = new ChbDBridge();
		String result = "";
		
		String language = ((String[])args.get("language"))[0];
		
		ArrayList<ArrayList<Object>> userData  = chbdb.getUserTop5Dataset();
		
		ArrayList<Object> obs = new ArrayList<Object>();
		
		obs.add(userData);
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	public String saveReport(Hashtable<String, String[]> args){
		Gson json = new Gson();
		CdisDBridge db = new CdisDBridge();
		String result = "";
		JsonParser jp = new JsonParser();
		String raw = ((String[])args.get("rawPost"))[0];
		String language = ((String[])args.get("language"))[0];
		String iduser = ((String[])args.get("iduser"))[0];
		
		
		Gson gson = new Gson();
	    JsonParser parser = new JsonParser();
	    JsonObject jObject = parser.parse(raw).getAsJsonObject();
	    
	    jObject.addProperty("iduser", iduser);
	    String reportCode = db.saveReport(jObject);
	    jObject.addProperty("id", reportCode);
	    User usr = new User(Integer.parseInt(iduser));
	    jObject.addProperty("owner", usr.getFirstname()+" "+usr.getLastname());
	    String reportStr = jObject.toString();
	    
	    FileOutputStream fop = null;
	    InitialContext ic;
		try {
			ic = new InitialContext();
			String rf = (String) ic.lookup("reports-folder");
			File reportFile = new File(rf+System.getProperty("file.separator")+"report.PERSONAL"+reportCode);
			fop = new FileOutputStream(reportFile);
			if (!reportFile.exists()) {
				reportFile.createNewFile();
			}
			// get the content in bytes
			byte[] contentInBytes = reportStr.getBytes();

			fop.write(contentInBytes);
			fop.flush();
			fop.close();
			
			
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    
		ArrayList<Object> obs = new ArrayList<Object>();
		obs.add(jObject);
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	
	public String getScheduleVisit(Hashtable<String, String[]> args){
		Gson json = new Gson();
		ChbDBridge chbdb = new ChbDBridge();
		String result = "";
		
		String language = ((String[])args.get("language"))[0];
		String sid = ((String[])args.get("sid"))[0];
		String idpatient = ((String[])args.get("idpatient"))[0];
		String iduser = ((String[])args.get("iduser"))[0];
		ScheduleVisit sv  = chbdb.getScheduleVisit(idpatient,iduser);
		ArrayList<Object> obs = new ArrayList<Object>();
		obs.add(sv);
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	public String setScheduleVisit(Hashtable<String, String[]> args){
		Gson json = new Gson();
		ChbDBridge chbdb = new ChbDBridge();
		CdisDBridge cdisdb = new CdisDBridge();
		String result = "";
		
		String language = ((String[])args.get("language"))[0];
		String sid = ((String[])args.get("sid"))[0];
		String idschedule = ((String[])args.get("idschedule"))[0];
		String idpatient = ((String[])args.get("idpatient"))[0];
		String iduser = ((String[])args.get("iduser"))[0];
		String scheduledate = ((String[])args.get("scheduledate"))[0];
		String idprofesion = ((String[])args.get("idprofesion"))[0];
		String frequency = ((String[])args.get("frequency"))[0];
		String hcpcode = ((String[])args.get("zone"))[0];
		boolean flag  = chbdb.setScheduleVisit(idschedule,iduser,idpatient,scheduledate,idprofesion,frequency);
		boolean flag1 = cdisdb.setOneHcpOfPatient(idpatient, iduser, hcpcode);
		ArrayList<Object> obs = new ArrayList<Object>();
		//obs.add(userData);
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}



	public void writeReportFile(String reportCode, String content){
		InitialContext ic;
		try {
			ic = new InitialContext();
			String rf = (String) ic.lookup("reports-folder");
			File reportFile = new File(rf+System.getProperty("file.separator")+"report."+reportCode);
			Writer writer = new FileWriter(reportFile);
			writer.write(content);
			writer.close();
		}catch(NamingException e){
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeOutcomeFile(String outcomeFile, String content){
		InitialContext ic;
		try {
			ic = new InitialContext();
			String rf = (String) ic.lookup("reports-folder");
			File reportFile = new File(rf+System.getProperty("file.separator")+"outcomes"+System.getProperty("file.separator")+outcomeFile);
			Writer writer = new FileWriter(reportFile);
			writer.write(content);
			writer.close();
		}catch(NamingException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String generateDataReport(Hashtable<String, String[]> args){
		Gson json = new Gson();
		CdisDBridge db = new CdisDBridge();
		String result = "";
		JsonParser jp = new JsonParser();
		
		String reportCode = ((String[])args.get("rep"))[0];
		
		InitialContext ic;
		try {
			ic = new InitialContext();
			String rf = (String) ic.lookup("reports-folder");
			File reportFile = new File(rf+System.getProperty("file.separator")+"report."+reportCode);
	
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			
			Gson gson = new Gson();
		    JsonParser parser = new JsonParser();
		    //Writer writer = new FileWriter(reportFile);
		    JsonElement je = parser.parse(new FileReader(reportFile));
		    JsonObject jObject = je.getAsJsonObject();
		    JsonArray jArrayC = jObject.get("criteria").getAsJsonArray();
		    JsonArray jArraySC = jObject.get("subcriteria").getAsJsonArray();
		    JsonArray jArrayI = jObject.get("input").getAsJsonArray();
		    
		    String reportType = jObject.get("type").getAsString();
		    String reportId = jObject.get("id").getAsString();
		    
		    ArrayList<Object> header = new ArrayList<Object>();
		    ArrayList<Object> datasets = new ArrayList<Object>();
		    Hashtable<String, Object> dataObject = new Hashtable<>();
		    jObject.addProperty("generated", sdf.format(new Date()));
    		//dataObject.put("timestamp", sdf.format(new Date()));
		    
		    
		    if(reportType.equals("graph")){
			    ArrayList<ReportCriteria> lcs = new ArrayList<ReportCriteria>();
			    ArrayList<ReportSubcriteria> slcs = new ArrayList<ReportSubcriteria>();
			    for(JsonElement obj : jArrayC ){
			        ReportCriteria cse = gson.fromJson( obj , ReportCriteria.class);
			        cse.loadIddata();
			        lcs.add(cse);
			    }
			    
			    for(JsonElement obj : jArraySC ){
			        ReportSubcriteria scse = gson.fromJson( obj , ReportSubcriteria.class);
			        scse.loadIddata();
		        	slcs.add(scse);
			    }
			    
			    //build subcriteria from input if exists 
			    ArrayList<ArrayList<ReportSubcriteria>> matrix = new ArrayList<>();
			    
			    for(int iobj=0;iobj<jArrayI.size();iobj++){
			        JsonObject input = jArrayI.get(iobj).getAsJsonObject();
			        String iname = input.get("name").getAsString();
			        
			        if(matrix.size() > 0){
			        	JsonArray varr = input.get("values").getAsJsonArray();
			        	int ml = matrix.size();
			        	
			        	for(int jj=0;jj<varr.size();jj++){
			        		ReportSubcriteria scs1 = new ReportSubcriteria();
			 		        for(JsonElement sobj : jArraySC ){
			 			        scs1 = gson.fromJson( sobj , ReportSubcriteria.class);
			 			        if(scs1.getSubname().equals(iname)){break;}
			 			    }
			        		scs1.setSubvalue(Integer.toString(jj));
			        		if(iname.equals("idcommunity") && scs1.getSubvalue().equals("0")){scs1.setSuboperator("more than");}
			        		for(int ii=0;ii<ml;ii++){
			        			ArrayList<ReportSubcriteria> ars1 = new ArrayList<>();
				        		ArrayList<ReportSubcriteria> ars = matrix.get(ii);
				        		for(ReportSubcriteria xx : ars){
				        			ars1.add(xx);
				        		}
				        		ars1.add(scs1);
					        	matrix.add(ars1);
				        	}
			        	}
			        	
			        	for(int iii=0;iii<ml;iii++){
			        		ArrayList<ReportSubcriteria> d1 = matrix.get(0);
			        		matrix.remove(0);
			        	}
			        	
			        }else{
			        	JsonArray varr = input.get("values").getAsJsonArray();
			        	for(int j=0;j<varr.size();j++){
			        		ArrayList<ReportSubcriteria> arsc = new ArrayList<>();
			        		ReportSubcriteria scs1 = new ReportSubcriteria();
			 		        for(JsonElement sobj : jArraySC ){
			 			        scs1 = gson.fromJson( sobj , ReportSubcriteria.class);
			 			        if(scs1.getSubname().equals(iname)){break;}
			 			    }
			        		scs1.setSubvalue(Integer.toString(j));
			        		if(iname.equals("idcommunity") && scs1.getSubvalue().equals("0")){scs1.setSuboperator("more than");}
			        		arsc.add(scs1);
			        		matrix.add(arsc);
			        	}
			        }
			    }
			    //now i have a matrix of all combinations of report subcriterias
			    
			    for(int x=0;x<matrix.size();x++){
			    	
			    	
			    	ArrayList<ReportSubcriteria> sc = matrix.get(x);
			    	
				    ArrayList<ArrayList<String>> set = new ArrayList<>();
			    	
				    Hashtable<ReportCriteria, ArrayList<ArrayList<String>>> map = new Hashtable<>();
				    
			    	for(int i=0;i<lcs.size();i++){
			    		ReportCriteria rc = lcs.get(i);
			    		if(!header.contains(rc.getDisplay())){
			    			header.add(rc.getDisplay());
			    		}
			    		ArrayList<ArrayList<String>> criteriaSet = db.executeReport(rc, reportType, sc);
			    		map.put(rc, criteriaSet);
			    	}
			    	
		    		ArrayList<String> setLine = new ArrayList<>();
		    		for(int jj=0;jj<lcs.size();jj++){
		    			ArrayList<ArrayList<String>> criteriaSet = map.get(lcs.get(jj));
		    			if(criteriaSet.size() > 0){
		    				setLine.add(criteriaSet.get(0).get(2));
		    			}else{
		    				setLine.add("0");
		    			}
		    		}
				    Hashtable<String, Object> dataset = new Hashtable<>();
				    for(ReportSubcriteria r : sc ){
				    	dataset.put(r.getSubname(), r.getSubvalue());
				    }
					dataset.put("set", setLine);
				    datasets.add(dataset);
			    }
		    }else if(reportType.equals("flist")){
		    	 for(JsonElement obj : jArrayC ){
				        ReportCriteria cse = gson.fromJson( obj , ReportCriteria.class);
				        HashMap<String, String> col = new HashMap<>();
				        //header.add(cse.getDisplay());
				        col.put("name", cse.getName());
				        col.put("display", cse.getDisplay());
				        col.put("type", cse.getType());
				        col.put("format", cse.getFormat());
				        header.add(col);
				        if(cse.getDate().equals("yes")){
				        	HashMap<String, String> cold = new HashMap<>();
				        	cold.put("name", cse.getDatename());
				        	cold.put("display", cse.getDatedisplay());
				        	cold.put("type", "date");
				        	cold.put("format", cse.getDateformat());
				        	header.add(cold);
				        }
				    }
		    	 String dataName = reportId.substring("LIST.".length()).toLowerCase();
		    	 datasets = executeReportFlist(dataName, jArrayC, jArraySC); 
		    	 
		    }else if(reportType.equals("locallist")){
		    	 JsonArray hdr = jObject.get("data").getAsJsonObject().get("header").getAsJsonArray();
		    	 for(JsonElement obj : hdr ){header.add(obj);}
		    	 datasets = executeReportLocalList(); 
		    	 
		    }else if(reportType.equals("nohba1c")){
		    	 JsonArray hdr = jObject.get("data").getAsJsonObject().get("header").getAsJsonArray();
		    	 for(JsonElement obj : hdr ){header.add(obj);}
		    	 datasets = executeReportNoHBA1c(); 
		    	 
		    }
		    
		    dataObject.put("header",header);
		    dataObject.put("datasets",datasets);
			
			jObject.add("data", json.toJsonTree(dataObject));
			writeReportFile(reportCode, jObject.toString());
			
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "REPORT "+ reportCode+" GENERATED";
	}

	
	public String generateDataGraph(Hashtable<String, String[]> args){
		Gson json = new Gson();
		CdisDBridge db = new CdisDBridge();
		String result = "";
		JsonParser jp = new JsonParser();
		
		String dataset = ((String[])args.get("dataset"))[0];
		InitialContext ic;
		try {
			Gson gson = new Gson();
		    JsonParser parser = new JsonParser();
		    
		    if (dataset.equals("trend")){
		    	//get ticks for trend graph
		    	//get labels for trend graph
		    	
		    	//get series for trend graph
		    	//should be like 3 series negative = improved , positive trend = setback , constant trend
		    	
		    	//variables for the function should be  idcommunity, sex, dateperiod, dype
		    	// dateperiod = last 3 monts means dates from the last 3 months  means 12 weeks  alltime = 24 months = 96 weeks
		    	int dateperiod = 96;
		    	int idc = 0;
		    	int gen = 0;
		    	ArrayList<Integer> dtypes = new ArrayList<>();
		    	dtypes.add(1);
		    	dtypes.add(2);
		    	//ArrayList<ArrayList<Integer>> series =   db.getHbA1cTrendSeries(dateperiod,idc,gen,dtypes);
		    	
		    }
		    
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "DATA GRAPH GENERATED";
	}

	

	public static ArrayList<Object> executeReportFlist(String dataName, JsonArray criterias, JsonArray subcriterias){
		ArrayList<Object> result = new ArrayList<>();
		Gson json = new Gson();
		CdisDBridge db = new CdisDBridge();
		Hashtable<String, Object> datasetObject = new Hashtable<>();
		for(JsonElement obj : subcriterias ){
		        ReportSubcriteria scse = json.fromJson( obj , ReportSubcriteria.class);
		        datasetObject.put(scse.getSubname(), scse.getSubvalue());
		}
		
		ArrayList<Hashtable<String, String>> dataset = db.executeReportFlist(dataName, criterias);
		datasetObject.put("set",dataset);
		result.add(datasetObject);
		return result;
	}
	
	public static ArrayList<Object> executeReportLocalList(){
		ArrayList<Object> result = new ArrayList<>();
		CdisDBridge db = new CdisDBridge();
		result = db.executeReportLocalList();
		return result;
	}

	public static ArrayList<Object> executeReportNoHBA1c(){
		ArrayList<Object> result = new ArrayList<>();
		CdisDBridge db = new CdisDBridge();
		result = db.executeReportNoHBA1c();
		return result;
	}

	/*
	 * 
	 * https://localhost/ncdis/service/action/generateDataOutcomes
	 * */
	public String generateDataOutcomes(Hashtable<String, String[]> args){
		
		CdisDBridge db = new CdisDBridge();
		String result = "";
		JsonParser jp = new JsonParser();
		
		InitialContext ic;
		try {
			ic = new InitialContext();
			Gson gson = new Gson();

		    Hashtable<String,ArrayList<Hashtable<String,String>>> t12 = db.getHbA1cTrend("1_2");
		    Iterator<String> t12keys = t12.keySet().iterator();
		    Hashtable<String, String> t12totals = new Hashtable<>();
		    while(t12keys.hasNext()){
		    	String key = t12keys.next();
		    	ArrayList<Hashtable<String,String>> month = t12.get(key);
		    	int tt = 0;
		    	for(int i=0;i<month.size();i++){
		    		Hashtable<String, String> line = month.get(i);
		    		tt+=Integer.parseInt(line.get("n"));
		    	}
		    	t12totals.put(key, Integer.toString(tt));
		    	String outcomeFile = "t."+key.replace("m", "")+".1_2";
		    	String content = gson.toJson(month);
		    	writeOutcomeFile(outcomeFile, content);
		    }
		    String t12tout  = "tt.1_2";
		    String t12tcontent = gson.toJson(t12totals);
		    writeOutcomeFile(t12tout, t12tcontent);
		    
		    Hashtable<String,ArrayList<Hashtable<String,String>>> tpdm = db.getHbA1cTrend("3");
		    Iterator<String> tpdmkeys = tpdm.keySet().iterator();
		    Hashtable<String, String> tpdmtotals = new Hashtable<>();
		    while(tpdmkeys.hasNext()){
		    	String key = tpdmkeys.next();
		    	ArrayList<Hashtable<String,String>> month = tpdm.get(key);
		    	int tt = 0;
		    	for(int i=0;i<month.size();i++){
		    		Hashtable<String, String> line = month.get(i);
		    		tt+=Integer.parseInt(line.get("n"));
		    	}
		    	tpdmtotals.put(key, Integer.toString(tt));
		    	String outcomeFile = "t."+key.replace("m", "")+".3";
		    	String content = gson.toJson(month);
		    	writeOutcomeFile(outcomeFile, content);
		    }
		    String tpdmtout  = "tt.3";
		    String tpdmtcontent = gson.toJson(tpdmtotals);
		    writeOutcomeFile(tpdmtout, tpdmtcontent);
		    
		    
		    Hashtable<String,ArrayList<Hashtable<String,String>>> p12 = db.getHbA1cPeriod("1_2");
		    Iterator<String> p12keys = p12.keySet().iterator();
		    Hashtable<String, String> p12totals = new Hashtable<>();
		    while(p12keys.hasNext()){
		    	String key = p12keys.next();
		    	ArrayList<Hashtable<String,String>> month = p12.get(key);
		    	int tt = 0;
		    	for(int i=0;i<month.size();i++){
		    		Hashtable<String, String> line = month.get(i);
		    		tt+=Integer.parseInt(line.get("n"));
		    	}
		    	p12totals.put(key, Integer.toString(tt));
		    	String outcomeFile = "p."+key.replace("m", "")+".1_2";
		    	String content = gson.toJson(month);
		    	writeOutcomeFile(outcomeFile, content);
		    }
		    String p12tout  = "tp.1_2";
		    String p12tcontent = gson.toJson(p12totals);
		    writeOutcomeFile(p12tout, p12tcontent);
		    

		    Hashtable<String,ArrayList<Hashtable<String,String>>> ppdm = db.getHbA1cPeriod("3");
		    Iterator<String> ppdmkeys = ppdm.keySet().iterator();
		    Hashtable<String, String> ppdmtotals = new Hashtable<>();
		    while(ppdmkeys.hasNext()){
		    	String key = ppdmkeys.next();
		    	ArrayList<Hashtable<String,String>> month = ppdm.get(key);
		    	int tt = 0;
		    	for(int i=0;i<month.size();i++){
		    		Hashtable<String, String> line = month.get(i);
		    		tt+=Integer.parseInt(line.get("n"));
		    	}
		    	ppdmtotals.put(key, Integer.toString(tt));
		    	String outcomeFile = "p."+key.replace("m", "")+".3";
		    	String content = gson.toJson(month);
		    	writeOutcomeFile(outcomeFile, content);
		    }
		    String ppdmtout  = "tp.3";
		    String ppdmtcontent = gson.toJson(ppdmtotals);
		    writeOutcomeFile(ppdmtout, ppdmtcontent);
		    
		    Hashtable<String,ArrayList<Hashtable<String,String>>> v12 = db.getHbA1cValue("1_2");
		    Iterator<String> v12keys = v12.keySet().iterator();
		    Hashtable<String, String> v12totals = new Hashtable<>();
		    while(v12keys.hasNext()){
		    	String key = v12keys.next();
		    	ArrayList<Hashtable<String,String>> month = v12.get(key);
		    	int tt = 0;
		    	for(int i=0;i<month.size();i++){
		    		Hashtable<String, String> line = month.get(i);
		    		tt+=Integer.parseInt(line.get("n"));
		    	}
		    	v12totals.put(key, Integer.toString(tt));
		    	String outcomeFile = "v."+key.replace("m", "")+".1_2";
		    	String content = gson.toJson(month);
		    	writeOutcomeFile(outcomeFile, content);
		    }
		    String v12tout  = "tv.1_2";
		    String v12tcontent = gson.toJson(v12totals);
		    writeOutcomeFile(v12tout, v12tcontent);
		    

		    Hashtable<String,ArrayList<Hashtable<String,String>>> vpdm = db.getHbA1cValue("3");
		    Iterator<String> vpdmkeys = vpdm.keySet().iterator();
		    Hashtable<String, String> vpdmtotals = new Hashtable<>();
		    while(vpdmkeys.hasNext()){
		    	String key = vpdmkeys.next();
		    	ArrayList<Hashtable<String,String>> month = vpdm.get(key);
		    	int tt = 0;
		    	for(int i=0;i<month.size();i++){
		    		Hashtable<String, String> line = month.get(i);
		    		tt+=Integer.parseInt(line.get("n"));
		    	}
		    	vpdmtotals.put(key, Integer.toString(tt));
		    	String outcomeFile = "v."+key.replace("m", "")+".3";
		    	String content = gson.toJson(month);
		    	writeOutcomeFile(outcomeFile, content);
		    }
		    String vpdmtout  = "tv.3";
		    String vpdmtcontent = gson.toJson(vpdmtotals);
		    writeOutcomeFile(vpdmtout, vpdmtcontent);
		    
		} catch (NamingException e) {
			e.printStackTrace();
		}
		return "Outcome files GENERATED";
	}
	
	public String getImportOmnilabFiles(Hashtable<String, String[]> args){
		Gson json = new Gson();
		
		String result = "";
		String sid = ((String[])args.get("sid"))[0];
		String language = ((String[])args.get("language"))[0];
		String period = ((String[])args.get("period"))[0];;
		
		int p = Integer.parseInt(period)*-1;
		ArrayList<Object> obs = new ArrayList<Object>();
		InitialContext ic;
		try {
			ic = new InitialContext();
			String rf = (String) ic.lookup("reports-folder");
			File importFolder = new File(rf+System.getProperty("file.separator")+"import");
			FileFilter fileFilter = new FileFilter(){
		         public boolean accept(File dir) {          
		            if (dir.isFile()) {
		            	String dName = dir.getName();
		            	String extension = dName.substring(dName.lastIndexOf(".")+1);
		            	if(extension.equals("html")){
		            		return true;
		            	}else{
		            		return false;
		            	}
		            } else {
		               return false;
		            }
		         }
		      };
		     File[] list = importFolder.listFiles(fileFilter);
		     ArrayList<String> files = new ArrayList<>();
		     for(int i=0;i<list.length;i++){
		    	 String fn = list[i].getName();
		    	 Date curentDate = new Date();
		    	 
		    	 DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		    	 Date fileDate = dateFormat.parse(fn.substring(fn.lastIndexOf("_")+1 ,fn.lastIndexOf(".")));
		         String todate = dateFormat.format(curentDate);

		         Calendar cal = Calendar.getInstance();
		         cal.add(Calendar.DATE, p);
		         Date todate1 = cal.getTime();    
		         String fromdate = dateFormat.format(todate1);
		         
		         if(fileDate.after(todate1)){
		        	 files.add(fn);
		         }
		         /*
		    	 if(fn.indexOf(fromdate) >=0 ){
		    		 files.add(fn);
		    	 }
		    	 */
		     }
		     result = json.toJson(files);
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	
}
