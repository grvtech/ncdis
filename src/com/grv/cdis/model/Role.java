package com.grv.cdis.model;

import java.util.HashMap;
import java.util.Hashtable;

import com.grv.cdis.db.ChbDBridge;

public class Role {
	private int idrole;
	private String name;
	private String code;
	private Action[] actions;
	
	
	
	public Role(int idrole, String name, String code){
		this.idrole = idrole;
		this.name = name;
		this.code = code;

	}
	
	
	public Role(int idrole2) {
		ChbDBridge chbdb = new ChbDBridge();
		HashMap<String, String> r = chbdb.getRole(idrole2);
		setRole(r);
	}


	public void setRole(HashMap<String, String> rol){
		this.idrole = Integer.parseInt(rol.get("idrole"));
		this.name = rol.get("role_name");
		this.code = rol.get("role_code");
		
	}
	
	public void setActions(int idrole){
		ChbDBridge chbdb = new ChbDBridge();
		this.actions = chbdb.getActionsRole(idrole);
	}
	

	public int getIdrole() {
		return idrole;
	}

	public void setIdrole(int idrole) {
		this.idrole = idrole;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	
	
}
