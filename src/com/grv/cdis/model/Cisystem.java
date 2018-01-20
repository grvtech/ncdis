package com.grv.cdis.model;

import java.util.HashMap;

import com.grv.cdis.db.ChbDBridge;

public class Cisystem {
	private int idsystem;
	private String name;
	private String code;
	
	
	public void setCisystem(HashMap<String, String> sys){
		this.idsystem = Integer.parseInt(sys.get("idsystem"));
		this.name = sys.get("system_name");
		this.code = sys.get("system_code");
		
	}
	
	public Cisystem(int idsystem, String name, String code){
		this.idsystem = idsystem;
		this.name = name;
		this.code = code;
		
	}

	public Cisystem(int idsystem2) {
		ChbDBridge chbdb = new ChbDBridge();
		HashMap<String, String> u = chbdb.getCisystem(idsystem2);
		setCisystem(u);
	}
	
}
