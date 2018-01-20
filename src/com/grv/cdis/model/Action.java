package com.grv.cdis.model;

import java.util.Hashtable;

import com.grv.cdis.db.ChbDBridge;


public class Action {
	private int idaction;
	private String name ;
	private String description;
	private String code;
	
	
	public Action(int idaction, String name, String description, String code) {
		super();
		this.idaction = idaction;
		this.name = name;
		this.description = description;
		this.code = code;
	}

	public Action(String code){
		ChbDBridge chbdb = new ChbDBridge();
		Hashtable<String, String> actionMap = chbdb.getAction(code);
		setAction(actionMap);
	}
	
	private void setAction(java.util.Hashtable<String, String> actionMap){
		this.idaction = Integer.parseInt(actionMap.get("idaction"));
		this.name = actionMap.get("name");
		this.description = actionMap.get("description");
		this.code = actionMap.get("code");
	}
	
	public int getIdaction() {
		return idaction;
	}

	public void setIdaction(int idaction) {
		this.idaction = idaction;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	
	
	
}
