package com.grv.cdis.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.grv.cdis.controler.Reports;
import com.grv.cdis.db.CdisDBridge;
import com.grv.cdis.db.ChbDBridge;
import com.grv.cdis.model.Action;
import com.grv.cdis.model.Community;
import com.grv.cdis.model.Complications;
import com.grv.cdis.model.Depression;
import com.grv.cdis.model.Diabet;
import com.grv.cdis.model.Event;
import com.grv.cdis.model.Hcp;
import com.grv.cdis.model.Lab;
import com.grv.cdis.model.Lipid;
import com.grv.cdis.model.MDVisit;
import com.grv.cdis.model.Meds;
import com.grv.cdis.model.Message;
import com.grv.cdis.model.MessageResponse;
import com.grv.cdis.model.Miscellaneous;
import com.grv.cdis.model.Note;
import com.grv.cdis.model.Patient;
import com.grv.cdis.model.Profile;
import com.grv.cdis.model.Renal;
import com.grv.cdis.model.Report;
import com.grv.cdis.model.SearchPatient;
import com.grv.cdis.model.Session;
//import com.grv.cdis.model.SearchPatient;
import com.grv.cdis.model.User;
import com.grv.cdis.model.Value;
import com.grv.cdis.model.ValueLimit;
import com.grv.cdis.model.Values;
import com.grv.cdis.util.FileTool;
import com.grv.cdis.util.MailTool;

public class DataProcessor {
	
	/*
	 * /ncdis/service/data/getUser?iduser=XX
	 * */
	public String getUser(Hashtable<String, String[]> args){
		Gson json = new Gson();
		String result = "";
		int iduser = Integer.parseInt(((String[])args.get("iduser"))[0]);
		String language = ((String[])args.get("language"))[0];
		User user = new User(iduser);
		if(user.isValid()){
			ArrayList<Object> obs = new ArrayList<>();
			obs.add(user);
			result = json.toJson(new MessageResponse(true,language,obs));
		}else{
			result = json.toJson(new MessageResponse(false,language,null));
		}
		return result;
	}
	
	public String getUsers(Hashtable<String, String[]> args){
		Gson json = new Gson();
		String result = "";
		String language = ((String[])args.get("language"))[0];
		ArrayList<Object> obs = User.getUsers();
		result = json.toJson(new MessageResponse(true,language,obs));
		
		return result;
	}
	
	public String getHcps(Hashtable<String, String[]> args){
		Gson json = new Gson();
		CdisDBridge db = new CdisDBridge();
		String result = "";
		String language = ((String[])args.get("language"))[0];
		String criteria = ((String[])args.get("criteria"))[0];
		String term = ((String[])args.get("term"))[0];
		ArrayList<Object> obs = new ArrayList<Object>();
		ArrayList<HashMap<String, String>> hcps = db.getHcps(criteria,term);
		obs.add(hcps);
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	
	public String getPatientNotes(Hashtable<String, String[]> args){
		Gson json = new Gson();
		CdisDBridge db = new CdisDBridge();
		String result = "";
		String sid = ((String[])args.get("sid"))[0];
		String language = ((String[])args.get("language"))[0];
		String ramq = ((String[])args.get("ramq"))[0];
			
		Patient pat = db.getPatientByRamq(ramq);
		
		ArrayList<Note> notes = db.getPatientNotes(pat.getIdpatient());
		ArrayList<Object> obs = new ArrayList<Object>();
		obs.add(notes);
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	public String setPatientNotes(Hashtable<String, String[]> args){
		Gson json = new Gson();
		CdisDBridge db = new CdisDBridge();
		ChbDBridge chbdb = new ChbDBridge();
		String result = "";
		String sid = ((String[])args.get("sid"))[0];
		String language = ((String[])args.get("language"))[0];
		String ramq = ((String[])args.get("ramq"))[0];
		String notestr = ((String[])args.get("note"))[0];
		String iduserto = ((String[])args.get("iduserto"))[0];
		Session session = chbdb.isValidSession(sid);
		if(session != null){
			session.setSession();
		}
		
		
		Patient pat = db.getPatientByRamq(ramq);
		User u = new User(sid);
		Note note = new Note("0", notestr, "", u.getIduser(), Integer.toString(pat.getIdpatient()), "1", iduserto, "0");
		
		User userto = new User(Integer.parseInt(iduserto));
		String usertoEmail = userto.getEmail();
		if((usertoEmail != null) && (!usertoEmail.equals(""))){
			if(usertoEmail.indexOf("/") >= 0 ){
				MailTool.sendMailInHtml("CDIS Patient Message", "<h2>Hello Admin</h2> <p>The user "+userto.getFirstname()+" "+userto.getLastname()+" with the username  <b>"+userto.getUsername()+"</b> does not have a valid email defined. Please contact user and set the email.</p>", "support@grvtech.ca");
			}else{
				MailTool.sendMailInHtml("CDIS Patient Message", "<h2>Hello "+userto.getFirstname()+" "+userto.getLastname()+"</h2> <p>There is a new patient message addressed to you in CDIS.<br><br>Please login to CDIS to see the message!.<br><br><b>Thank you.</b></p>", usertoEmail);
			}
		}else{
			MailTool.sendMailInHtml("CDIS Patient Message", "<h2>Hello Admin</h2> <p>The user "+userto.getFirstname()+" "+userto.getLastname()+" with the username  <b>"+userto.getUsername()+"</b> does not have a valid email defined. Please contact user and set the email.</p>", "support@grvtech.ca");
		}
		
		ArrayList<Object> obs = new ArrayList<Object>();
		if(db.setPatientNotes(note)){
			result = json.toJson(new MessageResponse(true,language,obs));
			Action act = new Action("NOTE");
			Event.registerEvent(u.getIduser(), act.getIdaction(), 1, sid, pat.getRamq());
		}else{
			result = json.toJson(new MessageResponse(false,language,obs));
		}
		
		return result;
	}
	
	
	public String deleteUser(Hashtable<String, String[]> args){
		Gson json = new Gson();
		ChbDBridge db = new ChbDBridge();
		String result = "";
		String language = ((String[])args.get("language"))[0];
		String iduser = ((String[])args.get("iduser"))[0];
		
		User u = new User(Integer.parseInt(iduser));
		u.setActive("0");
		db.setUser(u);
		ArrayList<Object> obs = User.getUsers();
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	public String activateUser(Hashtable<String, String[]> args){
		Gson json = new Gson();
		ChbDBridge db = new ChbDBridge();
		String result = "";
		String language = ((String[])args.get("language"))[0];
		String iduser = ((String[])args.get("iduser"))[0].trim();
		
		User u = new User(Integer.parseInt(iduser));
		u.setActive("1");
		u.setPhone("");
		db.setUser(u);
		
		String messagEmail = "<b><p>Hello "+u.getFirstname()+" "+u.getLastname()+"</p></b><p>Your CDIS account was activated.<br>You can login to CDIS by clicking <a href='http://cdis.reg18.rtss.qc.ca/ncdis/'>HERE</a><br> Your login information:<br><b>Username:</b>"+u.getUsername()+"<br><b>Password:</b>"+u.getPassword()+"<br><b>For any problems you can contact <a href='mailto:support@grvtech.ca'>CDIS Support</a></p>";
		MailTool.sendMailInHtml("CDIS User Activation", messagEmail, u.getEmail());
		
		ArrayList<Object> obs = User.getUsers();
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	public String saveUser(Hashtable<String, String[]> args){
		Gson json = new Gson();
		ChbDBridge db = new ChbDBridge();
		String result = "";
		String language = ((String[])args.get("language"))[0];
		String iduser = ((String[])args.get("iduser"))[0];
		User u = new User();
		if(!iduser.equals("0") ){
			u = new User(Integer.parseInt(iduser));
		}
		u.setFirstname(((String[])args.get("firstname"))[0]);
		u.setLastname(((String[])args.get("lastname"))[0]);
		u.setEmail(((String[])args.get("email"))[0]);
		u.setUsername(((String[])args.get("username"))[0]);
		u.setPassword(((String[])args.get("password"))[0]);
		u.setPhone(((String[])args.get("phone"))[0]);
		u.setIdcommunity(((String[])args.get("idcommunity"))[0]);
		String profesion = ((String[])args.get("profession"))[0];
		if(profesion.equals("1")){
			u.setIdprofesion("4");
		}else if(profesion.equals("2")){
			u.setIdprofesion("1");
		}else if(profesion.equals("3")){
			u.setIdprofesion("2");
		}else if(profesion.equals("4")){
			u.setIdprofesion("3");
		}
		
		if(!iduser.equals("0") ){
			db.setUser(u);
			db.setUserProfile(Integer.parseInt(u.getIduser()), 1, Integer.parseInt(((String[])args.get("role"))[0]));
		}else{
			int iu = db.addUser(u);
			db.saveUserProfile(iu, 1, Integer.parseInt(((String[])args.get("role"))[0]));
		}
		
		ArrayList<Object> obs = User.getUsers();
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	

	
	public String setUserPassword(Hashtable<String, String[]> args){
		Gson json = new Gson();
		ChbDBridge db = new ChbDBridge();
		String result = "";
		int iduser = Integer.parseInt(((String[])args.get("iduser"))[0]);
		String language = ((String[])args.get("language"))[0];
		User user = new User(iduser);
		user.setPassword(((String[])args.get("newpassword"))[0]);
		
		if(user.isValid()){
			db.setUser(user);
			ArrayList<Object> obs = new ArrayList<>();
			obs.add(user);
			result = json.toJson(new MessageResponse(true,language,obs));
		}else{
			result = json.toJson(new MessageResponse(false,language,null));
		}
		return result;
	}
	
	public String getUserProfile(Hashtable<String, String[]> args){
		Gson json = new Gson();
		String result = "";
		int iduser = Integer.parseInt(((String[])args.get("iduser"))[0]);
		int idsystem = Integer.parseInt(((String[])args.get("idsystem"))[0]);
		String language = ((String[])args.get("language"))[0];
		Profile userProfile = Profile.getProfile(iduser, idsystem);
		ArrayList<Object> obs = new ArrayList<>();
		obs.add(userProfile);
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	
	public String getUserBySession(Hashtable<String, String[]> args){
		Gson json = new Gson();
		String result = "";
		String sid = ((String[])args.get("sid"))[0];
		String language = ((String[])args.get("language"))[0];
		User user = new User(sid);
		if(user.isValid()){
			ArrayList<Object> obs = new ArrayList<>();
			/*add user info*/
			obs.add(user);
			/*add user stats*/
			//Hashtable<String, Object> stats = user.getUserStats();
			//obs.add(stats);
			result = json.toJson(new MessageResponse(true,language,obs));
		}else{
			result = json.toJson(new MessageResponse(false,language,null));
		}
		return result;
	}
	
	public String getUserMessages(Hashtable<String, String[]> args){
		Gson json = new Gson();
		String result = "";
		String iduser = ((String[])args.get("iduser"))[0];
		String language = ((String[])args.get("language"))[0];
		ChbDBridge db = new ChbDBridge();
		ArrayList<Object> obs = db.getUserMessages(iduser); 
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	
	/*
	 * /ncdis/service/data/getSession?iduser=XX
	 * */
	public String getUserSession(Hashtable<String, String[]> args){
		Gson json = new Gson();
		ChbDBridge chb = new ChbDBridge();
		String result = "";
		int iduser = Integer.parseInt(((String[])args.get("iduser"))[0]);
		String ip = ((String[])args.get("ipuser"))[0];
		String language = ((String[])args.get("language"))[0];
		User user = new User(iduser);
		if(user.isValid()){
			ArrayList<Object> obs = new ArrayList<>();
			Session sess = chb.getUserSession(iduser, ip);

			obs.add(sess);
			result = json.toJson(new MessageResponse(true,language,obs));
		}else{
			result = json.toJson(new MessageResponse(false,language,null));
		}
		return result;
	}
	
	public String isValidSession(Hashtable<String, String[]> args){
		Gson json = new Gson();
		ChbDBridge chb = new ChbDBridge();
		String result = "";
		String sid = ((String[])args.get("sid"))[0];
		String language = ((String[])args.get("language"))[0];
		ArrayList<Object> obs = new ArrayList<>();
		Session sess = chb.isValidSession(sid);
		obs.add(sess);
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	public String logoutSession(Hashtable<String, String[]> args){
		Gson json = new Gson();
		ChbDBridge chb = new ChbDBridge();
		String result = "";
		String sid = ((String[])args.get("sid"))[0];
		String language = ((String[])args.get("language"))[0];
		chb.logoutSession(sid);
		result = json.toJson(new MessageResponse(true,language,null));
		return result;
	}
	
	public String searchPatient(Hashtable<String, String[]> args){
		Gson json = new Gson();
		ChbDBridge chb = new ChbDBridge();
		String result = "";
		String sid = ((String[])args.get("sid"))[0];
		String language = ((String[])args.get("language"))[0];
		String criteria = ((String[])args.get("criteria"))[0];
		String term = ((String[])args.get("term"))[0];
		User user = new User(sid);
		Session session = chb.isValidSession(sid);
		if(session != null){
			session.setSession();
		}
		//ArrayList<Object> obs = chb.getPatientsList(criteria,term);
		ArrayList<Object> obs = chb.getPatientsList(criteria,term,user);
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	public String getABCData(Hashtable<String, String[]> args){
		Gson json = new Gson();
		CdisDBridge db = new CdisDBridge();
		String result = "";
		String sid = ((String[])args.get("sid"))[0];
		String language = ((String[])args.get("language"))[0];
		String idpatient = ((String[])args.get("idpatient"))[0];
		HashMap<String, String> abc = db.getABCData(idpatient);
		ArrayList<Object> obs = new ArrayList<>();
		obs.add(abc);
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	public String diabetByCommunity(Hashtable<String, String[]> args){
		Gson json = new Gson();
		ChbDBridge chb = new ChbDBridge();
		String result = "";
		String sid = ((String[])args.get("sid"))[0];
		String language = ((String[])args.get("language"))[0];
		String graphtype = ((String[])args.get("graphtype"))[0];
		ArrayList<Object> obs = new ArrayList<>();
		ArrayList<Object> series = chb.getDiabetByCommunity(graphtype);
		obs.add(series);
		if(graphtype.equals("pyramid")){
			ArrayList<String> coms = Community.getAllCommunities();
			obs.add(coms);
		}
		
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	
	
	
	public String diabetByType(Hashtable<String, String[]> args){
		Gson json = new Gson();
		ChbDBridge chb = new ChbDBridge();
		String result = "";
		String sid = ((String[])args.get("sid"))[0];
		String language = ((String[])args.get("language"))[0];
		ArrayList<Object> obs = chb.getDiabetByType();
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	public String diabetByYear(Hashtable<String, String[]> args){
		Gson json = new Gson();
		ChbDBridge chb = new ChbDBridge();
		String result = "";
		String sid = ((String[])args.get("sid"))[0];
		String language = ((String[])args.get("language"))[0];
		String years = ((String[])args.get("years"))[0];
		ArrayList<Object> obs = chb.getDiabetByYear(years);
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	/*
	 * 
	 * patien reposnse
	 * 
	 * {true,en, obs:[patient, hcp, Latest_diabet, latest_mdvisits, latest_renal, latest_lipid, latest_lab, latest_complications, latest_miscellaneous] }
	 * 
	 * 
	 * */
	public String getPatientRecord(Hashtable<String, String[]> args){
		Gson json = new GsonBuilder().serializeNulls().create();
		CdisDBridge db = new CdisDBridge();
		String result = "";
		String sid = ((String[])args.get("sid"))[0];
		String language = ((String[])args.get("language"))[0];
		Patient pat = null;
		String chart = null, ramq = null, id = null;
		if(args.get("ramq") != null){
			ramq = ((String[])args.get("ramq"))[0];
		}
		
		if(args.get("id") != null){
			id = ((String[])args.get("id"))[0];
		}
		ArrayList<Object> obs = new ArrayList<Object>();
		if(!ramq.equals("") && ramq != null){
			pat = db.getPatientByRamq(ramq);
		}else{
			pat = db.getPatientById(Integer.parseInt(id));
		}
		obs.add(pat);
		Hcp hcps = db.getHcpOfPatient(pat.getIdpatient());
		obs.add(hcps);
		Diabet latest_diabet = (Diabet) db.getValues("Diabet", pat.getIdpatient(),"asc");
		obs.add(latest_diabet);
		MDVisit latest_mdvisit = (MDVisit) db.getValues("MDVisit", pat.getIdpatient(),"asc");
		//obs.add(latest_mdvisit.getLatestMDVisit());
		obs.add(latest_mdvisit);
		Renal latest_renal = (Renal) db.getValues("Renal", pat.getIdpatient(),"asc");
		//obs.add(latest_renal.getLatestRenal());
		obs.add(latest_renal);
		Lipid latest_lipid = (Lipid) db.getValues("Lipid", pat.getIdpatient(),"asc");
		//obs.add(latest_lipid.getLatestLipid());
		obs.add(latest_lipid);
		Lab latest_lab = (Lab) db.getValues("Lab", pat.getIdpatient(),"asc");
		//obs.add(latest_lab.getLatestLab());
		obs.add(latest_lab);
		Complications latest_complications = (Complications) db.getValues("Complications", pat.getIdpatient(),"asc");
		//obs.add(latest_complications.getLatestComplications());
		obs.add(latest_complications);
		Miscellaneous latest_miscellaneous = (Miscellaneous) db.getValues("Miscellaneous", pat.getIdpatient(),"asc");
		//obs.add(latest_miscellaneous.getLatestMiscellaneous());
		obs.add(latest_miscellaneous);
		Meds latest_meds = (Meds) db.getValues("Meds", pat.getIdpatient(),"asc");
		obs.add(latest_meds);
		Depression latest_dep = (Depression) db.getValues("Depression", pat.getIdpatient(),"asc");
		obs.add(latest_dep);
		result = json.toJson(new MessageResponse(true,language,obs));
		Action act = new Action("VIEWP");
		User u = new User(sid);
		Event.registerEvent(u.getIduser(), act.getIdaction(), 1, sid, pat.getRamq());
		return result;
	}
	
	
	public String getPatientInfo(Hashtable<String, String[]> args){
		Gson json = new GsonBuilder().serializeNulls().create();
		CdisDBridge db = new CdisDBridge();
		String result = "";
		
		String language = ((String[])args.get("language"))[0];
		Patient pat = null;
		String idpatient = ((String[])args.get("idpatient"))[0];
		pat = db.getPatientById(Integer.parseInt(idpatient));
		ArrayList<Object> obs = new ArrayList<Object>();
		obs.add(pat);
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	
	
	public String getValueLimits(Hashtable<String, String[]> args){
		Gson json = new Gson();
		String result = "";
		String sid = ((String[])args.get("sid"))[0];
		String value = ((String[])args.get("name"))[0];
		String language = ((String[])args.get("language"))[0];
		CdisDBridge chb = new CdisDBridge();
		ValueLimit vl = chb.getValueLimits(value);
		ArrayList<Object> obs = new ArrayList<Object>();
		obs.add(vl);
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	public String saveValue(Hashtable<String, String[]> args){
		Gson json = new GsonBuilder().serializeNulls().create();
		String result = "";
		String sid = ((String[])args.get("sid"))[0];
		String valueName = ((String[])args.get("valueName"))[0];
		String valueDate = ((String[])args.get("date"))[0];
		String valueValue = ((String[])args.get("value"))[0];
		String idpatient = ((String[])args.get("idpatient"))[0];
		String idvalue = ((String[])args.get("idvalue"))[0];
		String language = ((String[])args.get("language"))[0];
		Patient pat = null;
		CdisDBridge chb = new CdisDBridge();
		ChbDBridge chbdb = new ChbDBridge();
		Session session = chbdb.isValidSession(sid);
		if(session != null){
			session.setSession();
		}
		
		boolean flag = false;
		String action = "ADDDATA";
		if(idvalue.equals("0")){
			flag = chb.addValue(valueName, valueValue, valueDate, idpatient);
		}else{
			flag = chb.editValue(valueName, valueValue, valueDate, idpatient, idvalue);
			action = "EDITDATA";
		}
		ArrayList<Object> obs = new ArrayList<Object>();
		
		pat = chb.getPatientById(Integer.parseInt(idpatient));
		obs.add(pat);
		Hcp hcps = chb.getHcpOfPatient(pat.getIdpatient());
		obs.add(hcps);
		Diabet latest_diabet = (Diabet) chb.getValues("Diabet", pat.getIdpatient(),"asc");
		obs.add(latest_diabet);
		MDVisit latest_mdvisit = (MDVisit) chb.getValues("MDVisit", pat.getIdpatient(),"asc");
		//obs.add(latest_mdvisit.getLatestMDVisit());
		obs.add(latest_mdvisit);
		Renal latest_renal = (Renal) chb.getValues("Renal", pat.getIdpatient(),"asc");
		//obs.add(latest_renal.getLatestRenal());
		obs.add(latest_renal);
		Lipid latest_lipid = (Lipid) chb.getValues("Lipid", pat.getIdpatient(),"asc");
		//obs.add(latest_lipid.getLatestLipid());
		obs.add(latest_lipid);
		Lab latest_lab = (Lab) chb.getValues("Lab", pat.getIdpatient(),"asc");
		//obs.add(latest_lab.getLatestLab());
		obs.add(latest_lab);
		Complications latest_complications = (Complications) chb.getValues("Complications", pat.getIdpatient(),"asc");
		//obs.add(latest_complications.getLatestComplications());
		obs.add(latest_complications);
		Miscellaneous latest_miscellaneous = (Miscellaneous) chb.getValues("Miscellaneous", pat.getIdpatient(),"asc");
		//obs.add(latest_miscellaneous.getLatestMiscellaneous());
		obs.add(latest_miscellaneous);
		Meds latest_meds = (Meds) chb.getValues("Meds", pat.getIdpatient(),"asc");
		obs.add(latest_meds);
		Depression latest_dep = (Depression) chb.getValues("Depression", pat.getIdpatient(),"asc");
		obs.add(latest_dep);
		//result = json.toJson(new MessageResponse(true,language,obs));
		Action act = new Action(action);
		User u = new User(sid);
		Event.registerEvent(u.getIduser(), act.getIdaction(), 1, sid, pat.getRamq());
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}

	
	public String deleteValue(Hashtable<String, String[]> args){
		Gson json = new GsonBuilder().serializeNulls().create();
		String result = "";
		String sid = ((String[])args.get("sid"))[0];
		String idvalue = ((String[])args.get("idvalue"))[0];
		String idpatient = ((String[])args.get("idpatient"))[0];
		String language = ((String[])args.get("language"))[0];
		Patient pat = null;
		CdisDBridge chb = new CdisDBridge();
		
		ChbDBridge chbdb = new ChbDBridge();
		Session session = chbdb.isValidSession(sid);
		if(session != null){
			session.setSession();
		}
		
		boolean flag = false;
		String action = "DELDATA";
		
		ArrayList<Object> obs = new ArrayList<Object>();
		if(chb.deleteValue(idvalue)){
			pat = chb.getPatientById(Integer.parseInt(idpatient));
			obs.add(pat);
			Hcp hcps = chb.getHcpOfPatient(pat.getIdpatient());
			obs.add(hcps);
			Diabet latest_diabet = (Diabet) chb.getValues("Diabet", pat.getIdpatient(),"asc");
			obs.add(latest_diabet);
			MDVisit latest_mdvisit = (MDVisit) chb.getValues("MDVisit", pat.getIdpatient(),"asc");
			//obs.add(latest_mdvisit.getLatestMDVisit());
			obs.add(latest_mdvisit);
			Renal latest_renal = (Renal) chb.getValues("Renal", pat.getIdpatient(),"asc");
			//obs.add(latest_renal.getLatestRenal());
			obs.add(latest_renal);
			Lipid latest_lipid = (Lipid) chb.getValues("Lipid", pat.getIdpatient(),"asc");
			//obs.add(latest_lipid.getLatestLipid());
			obs.add(latest_lipid);
			Lab latest_lab = (Lab) chb.getValues("Lab", pat.getIdpatient(),"asc");
			//obs.add(latest_lab.getLatestLab());
			obs.add(latest_lab);
			Complications latest_complications = (Complications) chb.getValues("Complications", pat.getIdpatient(),"asc");
			//obs.add(latest_complications.getLatestComplications());
			obs.add(latest_complications);
			Miscellaneous latest_miscellaneous = (Miscellaneous) chb.getValues("Miscellaneous", pat.getIdpatient(),"asc");
			//obs.add(latest_miscellaneous.getLatestMiscellaneous());
			obs.add(latest_miscellaneous);
			Meds latest_meds = (Meds) chb.getValues("Meds", pat.getIdpatient(),"asc");
			obs.add(latest_meds);
			Depression latest_dep = (Depression) chb.getValues("Depression", pat.getIdpatient(),"asc");
			obs.add(latest_dep);
		}
		
		result = json.toJson(new MessageResponse(true,language,obs));
		Action act = new Action(action);
		User u = new User(sid);
		Event.registerEvent(u.getIduser(), act.getIdaction(), 1, sid, pat.getRamq());
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
	
	public String savePatientRecord(Hashtable<String, String[]> args){
		Gson json = new GsonBuilder().serializeNulls().create();
		CdisDBridge db = new CdisDBridge();
		String result = "";
		String sid = ((String[])args.get("sid"))[0];
		String language = ((String[])args.get("language"))[0];
		String casem = ((String[])args.get("casem"))[0];
		
		String chr = ((String[])args.get("chrid"))[0];
		String md = ((String[])args.get("mdid"))[0];
		String nut = ((String[])args.get("nutid"))[0];
		String nur = ((String[])args.get("nurid"))[0];
		Patient pat = null;
		String chart = null, ramq = null, id = null;
		if(args.get("ramq") != null){
			ramq = ((String[])args.get("ramq"))[0];
		}
		
		if(args.get("idpatient") != null){
			id = ((String[])args.get("idpatient"))[0];
		}
		ArrayList<Object> obs = new ArrayList<Object>();
		if(!ramq.equals("") && ramq != null){
			pat = db.getPatientByRamq(ramq);
		}else{
			pat = db.getPatientById(Integer.parseInt(id));
		}
		
		ChbDBridge chbdb = new ChbDBridge();
		Session session = chbdb.isValidSession(sid);
		if(session != null){
			session.setSession();
		}
		
		MessageResponse patient =  pat.setPatient(args);
		if(patient.getStatus() == 1){
			patient = db.updatePatient(pat);
			String valueName = ((String[])args.get("diabetcode"))[0];
			String valueValue = ((String[])args.get("dtype"))[0];
			String valueDate = ((String[])args.get("ddate"))[0];
			String idvalue = ((String[])args.get("diabetidvalue"))[0];
			//we add diabet in edit
			
			Diabet latest_diabet = (Diabet) db.getValues("Diabet", pat.getIdpatient(),"desc");
			Values dtypes = latest_diabet.getDtype();
			ArrayList<Value> dtypeArray =  dtypes.getValues();
			boolean isUpdateDate = false;
			boolean isUpdateValue = false;
			for(int i=0;i<dtypeArray.size();i++){
				Value vdtype = dtypeArray.get(i);
				String dtypeDateStr = vdtype.getDate();
				String dtypeValueStr = vdtype.getValue();
				if(dtypeDateStr.equals(valueDate)){
					isUpdateDate = true;
				}
				if(dtypeValueStr.equals(valueValue)){
					isUpdateValue = true;
				}
			}
			
			if(isUpdateDate && isUpdateValue){
				patient.setStatus(1);
			}else if(!isUpdateDate && !isUpdateValue){
				if(!db.addValue(valueName, valueValue, valueDate, String.valueOf(pat.getIdpatient()))){
					patient.setStatus(1);
				}
			}else{
				if(!db.editValue(valueName, valueValue, valueDate, String.valueOf(pat.getIdpatient()), idvalue)){
					patient.setStatus(1);
				}
			}
			//if(!db.editValue(valueName, valueValue, valueDate, String.valueOf(pat.getIdpatient()), idvalue)){
			db.setHcpOfPatient(pat.getIdpatient(), casem, md, nut, nur, chr);
			
		}
		
		result = json.toJson(patient);
		Action act = new Action("EDITP");
		User u = new User(sid);
		Event.registerEvent(u.getIduser(), act.getIdaction(), 1, sid, pat.getRamq());
		return result;
	}
	
	public String addPatientRecord(Hashtable<String, String[]> args){
		Gson json = new GsonBuilder().serializeNulls().create();
		CdisDBridge db = new CdisDBridge();
		String result = "";
		String sid = ((String[])args.get("sid"))[0];
		String language = ((String[])args.get("language"))[0];
		String casem = null;
		String chr = null;
		String md = null;
		String nut = null;
		String nur = null;
		Patient pat = new Patient();
		String chart = null, ramq = null, id = null;
		if(args.get("ramq") != null){
			ramq = ((String[])args.get("ramq"))[0];
		}
		
		if(args.get("casemid") != null){casem = ((String[])args.get("casemid"))[0];}
		if(args.get("chrid") != null){chr = ((String[])args.get("chrid"))[0];}
		if(args.get("mdid") != null){md = ((String[])args.get("mdid"))[0];}
		if(args.get("nutid") != null){nut = ((String[])args.get("nutid"))[0];}
		if(args.get("nurid") != null){nur = ((String[])args.get("nurid"))[0];}
		
		ChbDBridge chbdb = new ChbDBridge();
		Session session = chbdb.isValidSession(sid);
		if(session != null){
			session.setSession();
		}
		ArrayList<Object> obs = new ArrayList<Object>();
		
		MessageResponse patient =  pat.setPatient(args);
		if(patient.getStatus() == 1){
			patient = db.addPatient(pat);
			pat = db.getPatientByRamq(pat.getRamq());
			String valueName = ((String[])args.get("diabetcode"))[0];
			String valueValue = ((String[])args.get("dtype"))[0];
			String valueDate = ((String[])args.get("ddate"))[0];
			String idvalue = ((String[])args.get("diabetidvalue"))[0];
			if(!db.addValue(valueName, valueValue, valueDate, String.valueOf(pat.getIdpatient()))){
				patient.setStatus(1);
			}
			db.setHcpOfPatient(pat.getIdpatient(), casem, md, nut, nur, chr);
		}
		
		result = json.toJson(patient);
		Action act = new Action("ADDP");
		User u = new User(sid);
		Event.registerEvent(u.getIduser(), act.getIdaction(), 1, sid, pat.getRamq());
		return result;
	}
	
	
	
	public String deletePatientRecord(Hashtable<String, String[]> args){
		Gson json = new GsonBuilder().serializeNulls().create();
		CdisDBridge db = new CdisDBridge();
		ChbDBridge chb = new ChbDBridge();
		String result = "";
		String sid = ((String[])args.get("sid"))[0];
		String language = ((String[])args.get("language"))[0];
		String idpatient = ((String[])args.get("idpatient"))[0];
		
		MessageResponse msg = new MessageResponse();
		
		Session session = chb.isValidSession(sid);
		if(session != null){
			session.setSession();
		}
		
		ArrayList<Object> obs = new ArrayList<Object>();
		Session ses = chb.isValidSession(sid);
		if (ses != null){
			if(db.deletePatient(idpatient)){
				Action act = new Action("DELP");
				User u = new User(sid);
				Event.registerEvent(u.getIduser(), act.getIdaction(), 1, sid, idpatient);
				msg = new MessageResponse(act,true,language,null);
			}
		}else{
			msg = new MessageResponse(false,language,null);
		}
		
		result = json.toJson(msg);
		return result;
	}
	
	public String getUserPatients(Hashtable<String, String[]> args){
		Gson json = new Gson();
		String result = "";
		String iduser = ((String[])args.get("iduser"))[0];
		String hcpcat = ((String[])args.get("hcpcat"))[0];
		
		String language = ((String[])args.get("language"))[0];
		ChbDBridge db = new ChbDBridge();
		ArrayList<Object> obs = db.getUserPatients(iduser, hcpcat); 
		result = json.toJson(new MessageResponse(true,language,obs));
		return result;
	}
	
}
