package com.grv.cdis.model;

import java.util.Date;
import java.util.HashMap;

import com.grv.cdis.db.ChbDBridge;

public class Profile {
	private User user;
	private Role role;
	private Cisystem system;
	private Date created ;
	
	public Profile(int iduser, int idrole, int idsystem){
		this.user = new User(iduser);
		this.role = new Role(idrole);
		this.system = new Cisystem(idsystem);
		this.created = new Date();
	}
	
	public Profile(HashMap<String, Object> map){
		this.user = (User)map.get("user");
		this.role = (Role)map.get("role");
		this.system = (Cisystem)map.get("system");
		this.created = new Date();
	}

	public Profile(){}

	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Cisystem getSystem() {
		return system;
	}

	public void setSystem(Cisystem system) {
		this.system = system;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}
	
	
	public static Profile getProfile(int iduser, int idsystem){
		ChbDBridge db = new ChbDBridge();
		Profile result = new Profile(db.getUserProfile(iduser,idsystem));
		return result;
	}
	
}
