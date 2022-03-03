package com.grv.cdis.controler;

import java.util.ArrayList;
import java.util.HashMap;

import com.grv.cdis.db.CdisDBridge;
import com.grv.cdis.model.Report;


public class Reports {
	/*
	 * Reports 
	 * predefined reports
	 *  number of patients of diabet per comunity (all) (community) by sex and by age  System
	 *  types of diabet in comunities per community (all) (community)  System
	 *  new cases of diabet by year by community (all) (community) System
	 *  number of patients with complications by community (all) (community) System 
	 * 
	 * admin reports
	 *  user activity by activity and per month  System
	 *  import from Omnilab report daily (last 10 reports) System
	 *  export all data per section (lab, mdvisits, renal, lipid, ) per year
	 *  
	 *  
	 *  
	 * ROOT
	 *     - predefined reports
	 *     - admin reports
	 *     - personal reports
	 * USER
	 *    - predefined reports per community 
	 *    - personal reports 
	 * 
	 * */
	
	
	public static HashMap<String, ArrayList<Report>> getUserReports(String userRole, String idcommunity, String iduser){
		HashMap<String, ArrayList<Report>> result = new HashMap<>();
		ArrayList<Report> predefinedReports = new ArrayList<>();
		ArrayList<Report> listsReports = new ArrayList<>();
		ArrayList<Report> adminReports = new ArrayList<>();
		ArrayList<Report> personalReports = new ArrayList<>();
		
		if(userRole.equals("ROOT")){
			predefinedReports = getPredefinedReports(iduser, "0");
			adminReports = getAdminReports();
			result.put("predefined",predefinedReports);
			personalReports = getPersonalReports(iduser, idcommunity, userRole);
			listsReports = getListsReports(iduser, "0");
			result.put("lists",listsReports);
			result.put("personal",personalReports);
			result.put("admin",adminReports);
		}else if(userRole.equals("USER")){
			predefinedReports = getPredefinedReports(iduser, idcommunity);
			result.put("predefined",predefinedReports);
			personalReports = getPersonalReports(iduser, idcommunity, userRole);
			result.put("personal",personalReports);
			listsReports = getListsReports(iduser, "0");
			result.put("lists",listsReports);
		}else{
			predefinedReports = getPredefinedReports(iduser, "0");
			result.put("predefined",predefinedReports);
		}
		return result;
	}
	
	
	
	public static ArrayList<Report> getPredefinedReports(String iduser, String community){
		ArrayList<Report> result = new ArrayList<>();
		CdisDBridge db = new CdisDBridge();
		result = db.getReports(iduser, community,"REP");
		return result;
	}
	
	
	public static ArrayList<Report> getListsReports(String iduser, String community){
		ArrayList<Report> result = new ArrayList<>();
		CdisDBridge db = new CdisDBridge();
		result = db.getReports(iduser, community,"LIST");
		return result;
	}
	
	
	public static ArrayList<Report> getPersonalReports(String iduser, String community, String userRole){
		ArrayList<Report> result = new ArrayList<>();
		CdisDBridge db = new CdisDBridge();
		if(userRole.equals("ROOT")){
			result = db.getReports(iduser, "0","PERSONAL");
		}else{
			result = db.getReports(iduser, community,"PERSONAL");
		}
		return result;
	}
	
	public static ArrayList<Report> getAdminReports(){
		ArrayList<Report> result = new ArrayList<>();
		CdisDBridge db = new CdisDBridge();
		result = db.getReports("1", "0", "ADMIN");
		return result;
	}
}
