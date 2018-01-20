package com.grv.cdis.model;

import com.grv.cdis.db.ChbDBridge;

public class Session {
	private String idsession;
	private String iduser;
	private String ipuser;
	private long created;
	
	
	
	public Session(String id, String idu, String ipu, long ts){
		this.idsession = id;
		this.iduser = idu;
		this.ipuser = ipu;
		this.created = ts;
		
	}
	
	
	
	public String getIdsession() {
		return idsession;
	}



	public void setIdsession(String idsession) {
		this.idsession = idsession;
	}



	public String getIduser() {
		return iduser;
	}



	public void setIduser(String iduser) {
		this.iduser = iduser;
	}



	public String getIpuser() {
		return ipuser;
	}



	public void setIpuser(String ipuser) {
		this.ipuser = ipuser;
	}



	public long getCreated() {
		return created;
	}



	public void setCreated(long created) {
		this.created = created;
	}



	public boolean isValidSession(String idsession){
		boolean result = false;
		ChbDBridge chb = new ChbDBridge();
		result = chb.setUserSession(this);
		
		return result;
	}
	
	
	public boolean setSession(){
		boolean result = false;
		//System.out.println("SESSION ID IN SET SESSION : "+ this.getIdsession());		
		ChbDBridge chb = new ChbDBridge();
		result = chb.setUserSession(this);
		return result;
	}
	
	
	public Session getSession(){
		return this;
	}
	
	
}
