package com.grv.cdis.model;

import java.util.Hashtable;
import com.grv.cdis.db.ChbDBridge;

public class UserLogin extends User {
	private Hashtable<Integer,Role> roles;
	private Hashtable<Integer, Cisystem>  systems;
	
	public UserLogin(int iduser){
		super(iduser);
		this.roles = loadUserRoles(iduser);
		this.systems = loadUserSystems(iduser);
	}
	
	private Hashtable<Integer, Role> loadUserRoles(int iduser){
		ChbDBridge chbdb = new ChbDBridge();
		Hashtable<Integer, Role> result = chbdb.loadUserRoles(iduser);
		return result;
	}
	
	
	private Hashtable<Integer, Cisystem> loadUserSystems(int iduser){
		ChbDBridge chbdb = new ChbDBridge();
		Hashtable<Integer, Cisystem> result = chbdb.loadUserSystems(iduser);
		return result;
	}
	
	public static boolean validateLogin(int iduser, int idsystem){
		boolean result = false;
		UserLogin ul = new UserLogin(iduser);
		if(ul.getRoles().containsKey(idsystem)){
			result = true;
		}
		return result;
	}

	public static boolean validateAction(int iduser, int idsystem, String actionCode){
		boolean result = false;
		UserLogin ul = new UserLogin(iduser);
		// retireve the id role for the id system 
		int idr = ((Role)(ul.getRoles().get(idsystem))).getIdrole();
		ChbDBridge chbdb = new ChbDBridge();
		Action[] actions  = chbdb.getActionsRole(idr);
		for(int i=0; i<actions.length;i++){
			Action a = actions[i];
			if(a.getCode().equals(actionCode)){
				result = true;
				break;
			}
		}
		return result;
	}

	
	public Hashtable<Integer, Role> getRoles() {
		return roles;
	}

	public void setRoles(Hashtable<Integer, Role> roles) {
		this.roles = roles;
	}

	public Hashtable<Integer, Cisystem> getSystems() {
		return systems;
	}

	public void setSystems(Hashtable<Integer, Cisystem> systems) {
		this.systems = systems;
	}
	
	
	
	
}
