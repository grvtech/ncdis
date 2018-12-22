package com.grv.cdis.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;

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
		User user = new User(username, password);
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
	    
	    //System.out.println(raw);
	    
	    
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
	        //System.out.println("SUBCRITERIA : "+scse.getSuboperator()+"   VALUE : "+ scse.getSubvalue());
        	slcs.add(scse);
	    }
	    ArrayList<String> header = new ArrayList<>();
	    ArrayList<ArrayList<String>> set = new ArrayList<>();
	    //ArrayList<Object> graphdata = new ArrayList<>();
	    
	    if(type.equals("list")){
	    	ArrayList<String> allIdpatients = new ArrayList<>();
	    	
	    	//System.out.println("Filter : "+filter+"    hcp:"+hcp+"    hcpid:"+hcpid);
	    	
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
	
	public String sendUserMessage(Hashtable<String, String[]> args){
		Gson json = new Gson();
		ChbDBridge chbdb = new ChbDBridge();
		String result = "";
		String nameUser = ((String[])args.get("nameUser"))[0];
		String messageUser = ((String[])args.get("messageUser"))[0];
		String emailUser = ((String[])args.get("emailUser"))[0];
		String adminUser = ((String[])args.get("adminUser"))[0];
		String language = ((String[])args.get("language"))[0];
		
		//MailTool.sendMailText("CDIS User Message", emailUser+"\nSent the message:\n\n"+messageUser, FileTool.getEmailProperty("admin."+adminUser));
		
		String messagEmail = "<b><p>Hello CDIS user</p></b><p>New message from : "+emailUser+"<br><br><b>Message:</b><br><pre>"+messageUser+"</pre></p>";
		MailTool.sendMailInHtml("CDIS User Message", messagEmail, FileTool.getEmailProperty("admin."+adminUser));
		
		ArrayList<Object> obs = new ArrayList<Object>();
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}

	public String forgotPassword(Hashtable<String, String[]> args){
		Gson json = new Gson();
		ChbDBridge chbdb = new ChbDBridge();
		String result = "";
		String firstnameUser = ((String[])args.get("firstnameUser"))[0];
		String lastnameUser = ((String[])args.get("lastnameUser"))[0];
		String usernameUser = ((String[])args.get("usernameUser"))[0];
		String profesionUser = ((String[])args.get("profesionUser"))[0];
		String emailUser = ((String[])args.get("emailUser"))[0];
		String language = ((String[])args.get("language"))[0];
		User u = chbdb.isValidUser(emailUser, usernameUser);
		if(u.isValid()){
			//String message = "Recovery Password \n Your password has been recovered.\nYour password is: "+u.getPassword()+" \nYou can now login to CDIS by clicking here : <a href='http://cdis.reg18.rtss.qc.ca/ncdis/'>CDIS</a> ";
			//MailTool.sendMailText("CDIS Recover Password", message, emailUser);
			
			String messagEmail = "<h2>Recovery Password</h2> <p>Hello "+u.getFirstname()+" "+u.getLastname()+"<br><br>Your password has been recovered.<br><br><b>Your username is :</b>"+u.getUsername()+"<br><b>Your password is :</b>"+u.getPassword()+"<br><br>You can now login to CDIS by clicking here : <a href='http://cdis.reg18.rtss.qc.ca/ncdis/'>go to CDIS</a></p>";
			MailTool.sendMailInHtml("CDIS Recover Password", messagEmail, emailUser);
			
			ArrayList<Object> obs = new ArrayList<Object>();
			result = json.toJson(new MessageResponse("FORGOT-TRUE",false,language,obs));
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
		User u = chbdb.isValidUser(emailUser, usernameUser);
		if(u.isValid()){
			String message = "Subscribe to CDIS \nYour information is already in CDIS database.\nIf you forgot your password you should click on forgot password link to recover your password.";
			//MailTool.sendMailText("CDIS Recover Password", message, emailUser);
			ArrayList<Object> obs = new ArrayList<Object>();
			MessageResponse mr = new MessageResponse(false,language,obs);
			mr.setMessage(message);
			result = json.toJson(mr);
		}else{
			u.setActive("0");
			u.setEmail(emailUser);
			u.setFirstname(firstnameUser);
			u.setLastname(lastnameUser);
			u.setUsername(usernameUser);
			u.setPassword("cdis2017");
			u.setIdcommunity(idcommunityUser);
			u.setIdprofesion(idprofesionUser);
			u.setPhone("GRV");
			int idPendingUser = chbdb.addUser(u);
			
			
			if(chbdb.saveUserProfile(idPendingUser, 1, 2)){
				//MailTool.sendMailText("CDIS New User Subscribe", , "support@grvtech.ca");
				
				String messagEmail = "<b><p>Hello Administrator</p></b><p>New user is subscribed to CDIS.<br>Login to CDIS and go to Users section.<br>Click on the button pending users to see the users that subscribed to CDIS but are not active yet.Click on the user to select it and click on the button Activate to allow the user to log in to CDIS.<br><br><b>An email will be sent to the user to annouce the activation.</b></p>";
				MailTool.sendMailInHtml("CDIS New User Subscribe", messagEmail, "support@grvtech.ca");
				
				String message = "Subscribe to CDIS \nYour account is sent to CDIS Administrators to be activated.\nYou will receive an email with the authentification information when your account will be activated";
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
	
	public String generateDataReport(Hashtable<String, String[]> args){
		Gson json = new Gson();
		CdisDBridge db = new CdisDBridge();
		String result = "";
		JsonParser jp = new JsonParser();
		
		//System.out.println("METHOD LAUNCH ");
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
		    
		    //System.out.println(reportFile.getAbsolutePath());
		    //System.out.println(jObject.toString());
		    String reportType = jObject.get("type").getAsString();
		    String reportId = jObject.get("id").getAsString();
		    
		    ArrayList<Object> header = new ArrayList<Object>();
		    ArrayList<Object> datasets = new ArrayList<Object>();
		    Hashtable<String, Object> dataObject = new Hashtable<>();
    		dataObject.put("timestamp", sdf.format(new Date()));
		    
		    
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
			    
			    //System.out.println("INput array size : "+jArrayI.size());
			    for(int iobj=0;iobj<jArrayI.size();iobj++){
			        JsonObject input = jArrayI.get(iobj).getAsJsonObject();
			        String iname = input.get("name").getAsString();
			        
			        if(matrix.size() > 0){
			        	JsonArray varr = input.get("values").getAsJsonArray();
			        	int ml = matrix.size();
			        	//System.out.println("freeze matrix size : "+ml);
			        	//System.out.println("start loop for subscriteria :   "+iname);
			        	
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
				        		//System.out.println("subcriteria array size in loop: "+ars.size());
				        		for(ReportSubcriteria xx : ars){
				        			ars1.add(xx);
				        		}
				        		//System.out.println("subcriteria array1 size in loop before add: "+ars1.size());
			        			//ars1 = ars;
				        		ars1.add(scs1);
				        		//System.out.println("subcriteria array1 size in loop after add: "+ars1.size());
					        	matrix.add(ars1);
				        	}
			        	}
			        	
			        	for(int iii=0;iii<ml;iii++){
			        		ArrayList<ReportSubcriteria> d1 = matrix.get(0);
			        		//System.out.println("delete array from index :"+iii+"   with size : "+d1.size());
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
			    /* 
			    System.out.println("Matrix size: "+matrix.size());
			    for(ArrayList<ReportSubcriteria> aa : matrix){
			    	System.out.println("Subscriteria arrai size: "+aa.size());
			    	for(ReportSubcriteria bb : aa){
			    		System.out.println("Subscriteria name: "+bb.getSubname());
			    		System.out.println("Subscriteria value: "+bb.getSubvalue());
			    		System.out.println("Subscriteria operator: "+bb.getSuboperator());
			    	}
			    }
			    */
			    //now i have a matrix of all combinations of report subcriterias
			    
			    

	    		//System.out.println("-------------------------------------------------matrix size "+matrix.size());
			    for(int x=0;x<matrix.size();x++){
			    	
			    	
			    	ArrayList<ReportSubcriteria> sc = matrix.get(x);
			    	
				    ArrayList<ArrayList<String>> set = new ArrayList<>();
			    	
				    Hashtable<ReportCriteria, ArrayList<ArrayList<String>>> map = new Hashtable<>();
				    
				    //System.out.println("-------------------------------------------------report criteria size "+lcs.size());
				    
			    	for(int i=0;i<lcs.size();i++){
			    		ReportCriteria rc = lcs.get(i);
			    		if(!header.contains(rc.getDisplay())){
			    			header.add(rc.getDisplay());
			    		}
			    		//System.out.println("-------------------------------------------------criteria "+rc.getName() + "     value : "+rc.getValue()+ " report type = "+reportType);
			    		
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
		    	 
		    }
		    System.out.println("-------------------------------------------------");
		    dataObject.put("header",header);
		    dataObject.put("datasets",datasets);
			
			jObject.add("data", json.toJsonTree(dataObject));
			writeReportFile(reportCode, jObject.toString());
			System.out.println("-------------------------------------------------");
			
			System.out.println("REPORT "+ reportCode+" GENERATED");
			System.out.println("-------------------------------------------------");
			
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "REPORT "+ reportCode+" GENERATED";
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
	
	
	
}
