package com.grv.cdis.db;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.grv.cdis.model.Action;
import com.grv.cdis.model.Cisystem;
import com.grv.cdis.model.Message;
import com.grv.cdis.model.Note;
import com.grv.cdis.model.Role;
import com.grv.cdis.model.ScheduleVisit;
import com.grv.cdis.model.SearchPatient;
import com.grv.cdis.model.Session;
import com.grv.cdis.model.User;


/*
 * Data bridge to users databases and actions also for messaging between users
 * all methods call stored procedures in the db server
 * methods :
 * 
 * add users
 * modify user
 * 
 * 
 * */

public class ChbDBridge {
	
	
	
	public ChbDBridge(){}
	
	
	public Hashtable<String, String> getAction(String actionCode){
		Hashtable<String, String> result = new Hashtable<String, String>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			//Context envContext  = (Context)initContext.lookup("java:comp/env");
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.prepareStatement("exec sp_getActionByCode ?");
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    cs.setString(1, actionCode);
		    rs = cs.executeQuery();
		    while (rs.next()) {
		        result.put("idaction",rs.getString(1));
		        result.put("name", rs.getString(2));
		        result.put("description",rs.getString(3));
		        result.put("code",rs.getString(4));
		    }
		    
		}catch (SQLException se) {
		        se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	
	
	public HashMap<String, String> getUser(String username, String password){
		HashMap<String, String> result = new HashMap<String, String>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			//Context envContext  = (Context)initContext.lookup("java:comp/env");
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.prepareStatement("exec sp_getUserByUsernameAndPassword ?,?");
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    cs.setString(1, username);
		    cs.setString(2, password);
		    rs = cs.executeQuery();
		    while (rs.next()) {
		    	int cols = rs.getMetaData().getColumnCount();
		    	for(int i=1;i<=cols;i++){
		    		result.put(rs.getMetaData().getColumnName(i), rs.getString(i));
		    	}
		    }
		    
		}catch (SQLException se) {
		        se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	

	public ArrayList<Object> getUsers(){
		ArrayList<Object> result = new ArrayList<>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			//Context envContext  = (Context)initContext.lookup("java:comp/env");
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			String sql = "select * from ncdis.ncdis.users where iduser > ? ";
		    cs=conn.prepareStatement("select * from ncdis.ncdis.users where iduser > ? and active = ? or (active=0 and phone='GRV') order by fname,lname asc");
		    
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    cs.setInt(1, 0);
		    cs.setInt(2, 1);
		    rs = cs.executeQuery();
		    while (rs.next()) {
		    	int cols = rs.getMetaData().getColumnCount();
		    	HashMap<String, String> map = new HashMap<>();
		    	for(int i=1;i<=cols;i++){
		    		map.put(rs.getMetaData().getColumnName(i), rs.getString(i));
		    	}
		    	User u = new User();
		    	u.setUser(map);
		    	result.add(u);
		    }
		    
		}catch (SQLException se) {
		        se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	

	
	
	public HashMap<String, String> getUser(int iduser){
		HashMap<String, String> result = new HashMap<String, String>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			//Context envContext  = (Context)initContext.lookup("java:comp/env");
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.prepareStatement("select * from ncdis.ncdis.users where iduser=? and active=1 or (active=0 and phone='GRV')");
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    cs.setInt(1, iduser);
		    rs = cs.executeQuery();
		    while (rs.next()) {
		    	int cols = rs.getMetaData().getColumnCount();
		    	for(int i=1;i<=cols;i++){
		    		result.put(rs.getMetaData().getColumnName(i), rs.getString(i));
		    	}
		    }
		}catch (SQLException se) {
		        se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}

	
	public void setUser(User user){
		HashMap<String, String> result = new HashMap<String, String>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			//Context envContext  = (Context)initContext.lookup("java:comp/env");
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.prepareStatement("update ncdis.ncdis.users set fname=?, lname=?, phone=?, email=?, password=?, active=?, idcommunity=?, idprofesion=? where iduser = ?");
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    cs.setString(1, user.getFirstname());
		    cs.setString(2, user.getLastname());
		    cs.setString(3, user.getPhone());
		    cs.setString(4, user.getEmail());
		    cs.setString(5, user.getPassword());
		    cs.setString(6, user.getActive());
		    cs.setString(7, user.getIdcommunity());
		    cs.setString(8, user.getIdprofesion());
		    cs.setInt(9, Integer.parseInt(user.getIduser()));
		    
		    cs.executeUpdate();
		    /*
		    while (rs.next()) {
		    	int cols = rs.getMetaData().getColumnCount();
		    	for(int i=1;i<=cols;i++){
		    		result.put(rs.getMetaData().getColumnName(i), rs.getString(i));
		    	}
		    }
		    */
		}catch (SQLException se) {
		        se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		
	}

	public int addUser(User user){
		int result = 0;
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		PreparedStatement cs=null;
		Statement st = null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			String sql = "insert into ncdis.ncdis.users (fname,lname,username,password,email,phone,idcommunity,idprofesion,active) "
		    		+ "values ('"+user.getFirstname()+"','"+user.getLastname()+"','"+user.getUsername()+"','"+user.getPassword()+"','"+user.getEmail()+"',"
    				+ "'"+user.getPhone()+"','"+user.getIdcommunity()+"','"+user.getIdprofesion()+"','"+user.getActive()+"')";
		    cs=conn.prepareStatement(sql);
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    /*
		    cs.setString(1, user.getFirstname());
		    cs.setString(2, user.getLastname());
		    cs.setString(3, user.getUsername());
		    cs.setString(4, user.getPassword());
		    cs.setString(5, user.getEmail());
		    cs.setString(6, user.getPhone());
		    cs.setString(7, user.getIdcommunity());
		    cs.setString(8, user.getIdprofesion());
		    cs.setString(9, user.getActive());
		    */
		    //System.out.println(sql);
		    cs.executeUpdate();
		    
		    
		    cs.clearBatch();
		    st = conn.createStatement();
		    rs = st.executeQuery("select TOP 1 iduser from ncdis.ncdis.users order by iduser desc");
		    while(rs.next()){
		    	result = rs.getInt(1); 
		    }
		    
		}catch (SQLException se) {
		        se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	        	rs.close();
	        	st.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}

	
	public boolean setUserProfile(int iduser, int idsystem, int idrole){
		boolean result = false;
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		//PreparedStatement cs=null;
		Statement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			//default search by chart
			String url = "update  ncdis.ncdis.profile set idrole = '"+idrole+"' where iduser = '"+iduser+"' and idsystem = '"+idsystem+"'";
		    cs=conn.createStatement();
		    cs.setEscapeProcessing(true);
		    cs.executeUpdate(url);
		    result = true;
		    
		}catch (SQLException se) {
		    se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	

	public boolean saveUserProfile(int iduser, int idsystem, int idrole){
		boolean result = false;
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		//PreparedStatement cs=null;
		Statement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			//default search by chart
			String url = "insert into ncdis.ncdis.profile (iduser,idsystem,idrole) values ('"+iduser+"','"+idsystem+"','"+idrole+"')";
		    cs=conn.createStatement();
		    cs.setEscapeProcessing(true);
		    cs.executeUpdate(url);
		    result = true;
		    
		}catch (SQLException se) {
		    se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}

	
	
	public HashMap<String, Object> getUserProfile(int iduser, int idsystem){
		HashMap<String, Object> result = new HashMap<String, Object>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		//PreparedStatement cs=null;
		Statement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			//   idsystem system_name system_code idrole
			
			//default search by chart
			String url = "select * from ncdis.profile where iduser = '"+iduser+"' and idsystem = '"+idsystem+"'";
		    cs=conn.createStatement();
		    cs.setEscapeProcessing(true);
		    rs = cs.executeQuery(url);
		    while(rs.next()){
		    	User u = new User(iduser);
		    	Role r =  new Role(Integer.parseInt(rs.getString("idrole")));
		    	Cisystem c = new Cisystem(idsystem);
		    	result.put("user", u);
		    	result.put("role", r);
		    	result.put("system", c);
		    }
		    
		}catch (SQLException se) {
		    se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	
	
	
	public HashMap<String, String> getUser(String idsession){
		HashMap<String, String> result = new HashMap<String, String>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;
		try {
			initContext = new InitialContext();
			//Context envContext  = (Context)initContext.lookup("java:comp/env");
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.prepareStatement("exec sp_getUserBySession ?");
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    cs.setString(1, idsession);
		    rs = cs.executeQuery();
		    while (rs.next()) {
		    	int cols = rs.getMetaData().getColumnCount();
		    	for(int i=1;i<=cols;i++){
		    		result.put(rs.getMetaData().getColumnName(i), rs.getString(i));
		    	}
		    }
		}catch (SQLException se) {
		        se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}

	
	public Hashtable<Integer, Role> loadUserRoles(int iduser){
		Hashtable<Integer,	Role> result = new Hashtable<Integer, Role>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			Context envContext  = (Context)initContext.lookup("java:comp/env");
			ds = (DataSource)envContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			//   idrole role_name role_code idsystem
		    cs=conn.prepareStatement("exec sp_getUserRoles ?");
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    cs.setInt(1, iduser);
		    rs = cs.executeQuery();
		    while (rs.next()) {
		    	Role role = new Role(rs.getInt(1), rs.getString(2), rs.getString(3));
		    	result.put(rs.getInt(4), role);		    
		    }
		}catch (SQLException se) {
		        se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}

	public Hashtable<Integer, Cisystem> loadUserSystems(int iduser){
		Hashtable<Integer,	Cisystem> result = new Hashtable<Integer, Cisystem>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			Context envContext  = (Context)initContext.lookup("java:comp/env");
			ds = (DataSource)envContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			//   idsystem system_name system_code idrole
		    cs=conn.prepareStatement("exec sp_getUserSystems ?");
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    cs.setInt(1, iduser);
		    rs = cs.executeQuery();
		    while (rs.next()) {
		    	Cisystem sys = new Cisystem(rs.getInt(1), rs.getString(2), rs.getString(3));
		    	result.put(rs.getInt(4), sys);		    
		    }
		}catch (SQLException se) {
		        se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	
	
	public Action[] getActionsRole(int idrole){
		ArrayList<Action> result = null;
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			Context envContext  = (Context)initContext.lookup("java:comp/env");
			ds = (DataSource)envContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			//   idsystem system_name system_code idrole
		    cs=conn.prepareStatement("exec sp_getActionsRole ?");
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    cs.setInt(1, idrole);
		    rs = cs.executeQuery();
		    while (rs.next()) {
		    	Action act = new Action(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(3));
		    	result.add(act);
		    }
		}catch (SQLException se) {
		        se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return (Action[]) result.toArray();
	}
	
	
	public boolean setEvent(String iduser, int idaction, int idsystem, String idsession){
		boolean result = false;
		Context initContext;
		DataSource ds;
		//ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			//   idsystem system_name system_code idrole
		    cs=conn.prepareStatement("exec sp_setEvent ?,?,?,?");
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    cs.setString(1, iduser);
		    cs.setInt(2, idaction);
		    cs.setInt(3, idsystem);
		    cs.setString(4, idsession);
		    //cs.setDate(4, crea);
		    cs.executeUpdate();
		    result = true;
		}catch (SQLException se) {
		    se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            //rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	
	public boolean setEvent(String iduser, int idaction, int idsystem, String idsession, String data){
		boolean result = false;
		Context initContext;
		DataSource ds;
		//ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			//   idsystem system_name system_code idrole
		    cs=conn.prepareStatement("exec sp_setEventData ?,?,?,?,?");
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    cs.setString(1, iduser);
		    cs.setInt(2, idaction);
		    cs.setInt(3, idsystem);
		    cs.setString(4, idsession);
		    cs.setString(5, data);
		    //cs.setDate(4, crea);
		    cs.executeUpdate();
		    result = true;
		}catch (SQLException se) {
		    se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            //rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	
	
	public boolean setUserSession(Session ses){
		boolean result = false;
		Context initContext;
		DataSource ds;
		//ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			//   idsystem system_name system_code idrole
		    cs=conn.prepareStatement("exec sp_setSession ?,?,?,?,?,?,?");
		    cs.setEscapeProcessing(true);
		    
		    cs.setString(1,ses.getIdsession());
		    cs.setString(2, ses.getIduser());
		    cs.setString(3, ses.getIpuser());
		    cs.setDate(4, null);
		    cs.setDate(5, null);
		    cs.setInt(6, ses.getReswidth());
		    cs.setInt(7, ses.getResheight());
		    
		    //cs.setDate(4, crea);
		    
		   
		    
		    cs.executeUpdate();
		    result = true;
		}catch (SQLException se) {
		    se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            //rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	

	public Session getUserSession(int iduser, String ip){
		Session result = null;
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			//   idsystem system_name system_code idrole
		    cs=conn.prepareStatement("exec sp_getSession ?,?");
		    cs.setEscapeProcessing(true);
		    
		    cs.setString(2,ip);
		    cs.setInt(1, iduser);
		     
		    rs = cs.executeQuery();
		    
		    if(rs.isBeforeFirst()){
		    	rs.next();
		    	result = new Session(rs.getString("idsession"), rs.getString("iduser"), rs.getString("ipuser"), (rs.getDate("created").getTime()),(rs.getDate("modified").getTime()), rs.getInt("reswidth"), rs.getInt("resheight"), rs.getInt("active"));
		    }
		    
		    
		}catch (SQLException se) {
		    se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}

	public Session isValidSession(String idsession){
		Session result = null;
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			//   idsystem system_name system_code idrole
		    cs=conn.prepareStatement("exec sp_getSessionByIDSESSION ?");
		    cs.setEscapeProcessing(true);
		    cs.setString(1,idsession);
		    rs = cs.executeQuery();
		    if(rs.isBeforeFirst()){
		    	rs.next();
		    	result = new Session(rs.getString("idsession"), rs.getString("iduser"), rs.getString("ipuser"), (rs.getDate("created").getTime()),(rs.getDate("modified").getTime()), rs.getInt("reswidth"), rs.getInt("resheight"), rs.getInt("active"));
		    }
		    
		    
		}catch (SQLException se) {
		    se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	
	
	
	public User isValidUser(String email, String username){
		User result = new User();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			//   idsystem system_name system_code idrole
		    cs=conn.prepareStatement("select * from ncdis.ncdis.users where email='"+email+"' and username = '"+username+"'");
		    cs.setEscapeProcessing(true);
		    rs = cs.executeQuery();
		    if(rs.isBeforeFirst()){
		    	rs.next();
		    	result = new User(rs.getString("username"), rs.getString("password"), rs.getString("fname"), rs.getString("lname"),rs.getString("email"), rs.getString("iduser"), true, rs.getString("phone"),rs.getString("idcommunity"), rs.getString("active"),rs.getString("idprofesion"));
		    }
		    
		}catch (SQLException se) {
		    se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	
	
	public boolean logoutSession(String idsession){
		boolean result = false;
		Context initContext;
		DataSource ds;
		//ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.prepareStatement("exec sp_logoutSession ?");
		    cs.setEscapeProcessing(true);
		    cs.setString(1,idsession);
		    cs.executeUpdate();
		    result = true;
		}catch (SQLException se) {
		    se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            //rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	
	public ArrayList<Object> getPatientsList(String criteria, String term){
		ArrayList<Object> result = new ArrayList<Object>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		//PreparedStatement cs=null;
		Statement cs=null;
		Connection conn = null;
		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			//   idsystem system_name system_code idrole
			
			//default search by chart
			String url = "select * from dbo.SearchPatient where chart like '"+term+"%' order by chart";
			if(criteria.equals("ramq")){
				url = "select * from dbo.SearchPatient where ramq like '"+term+"%' order by ramq";
			}else if(criteria.equals("fnamelname")){
				url = "select * from dbo.SearchPatient where fname like '"+term+"%' or lname like '"+term+"%' order by fname,lname";
			}else if(criteria.equals("ipm")){
				url = "select * from dbo.SearchPatient where giu like '"+term+"%' order by giu";
			}
		    cs=conn.createStatement();
		    cs.setEscapeProcessing(true);
		    rs = cs.executeQuery(url);
		    while(rs.next()){
		    	SearchPatient sp = new SearchPatient(rs.getString("fname"), rs.getString("lname"), rs.getString("ramq"), rs.getString("name_en"), rs.getInt("chart"), rs.getInt("idpatient"), rs.getString("giu"));
		    	result.add(sp);
		    }
		    
		}catch (SQLException se) {
		    se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	
	
	public ArrayList<Object> getPatientsList(String criteria, String term, User user){
		ArrayList<Object> result = new ArrayList<Object>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		//PreparedStatement cs=null;
		Statement cs=null;
		Connection conn = null;
		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			//   idsystem system_name system_code idrole
			
			String comfilter = " and idcommunity = '"+user.getIdcommunity()+"'";
			if(user.getIdcommunity().equals("0")){
				comfilter = "";
			}
			
			//default search by chart
			String url = "select * from dbo.SearchPatient where chart like '"+term+"%' "+comfilter+" order by chart";
			if(criteria.equals("ramq")){
				url = "select * from dbo.SearchPatient where ramq like '"+term+"%' "+comfilter+" order by ramq";
			}else if(criteria.equals("fnamelname")){
				url = "select * from dbo.SearchPatient where fname like '"+term+"%' or lname like '"+term+"%' "+comfilter+" order by fname,lname";
			}else if(criteria.equals("ipm")){
				url = "select * from dbo.SearchPatient where giu like '"+term+"%' "+comfilter+" order by giu";
			}
		    cs=conn.createStatement();
		    cs.setEscapeProcessing(true);
		    rs = cs.executeQuery(url);
		    while(rs.next()){
		    	SearchPatient sp = new SearchPatient(rs.getString("fname"), rs.getString("lname"), rs.getString("ramq"), rs.getString("name_en"), rs.getInt("chart"), rs.getInt("idpatient"), rs.getString("giu"));
		    	//System.out.println(rs.getString("fname")+"-----"+ rs.getString("lname")+"-----"+ rs.getString("ramq")+"-----"+ rs.getString("name_en")+"-----"+ rs.getInt("chart")+"-----"+ rs.getInt("idpatient")+"-----"+ rs.getString("giu"));
		    	result.add(sp);
		    }
		    
		}catch (SQLException se) {
		    se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	
	public ArrayList<Object> getDiabetByCommunity(String graphtype){
		ArrayList<Object> result = new ArrayList<Object>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		//PreparedStatement cs=null;
		Statement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			//   idsystem system_name system_code idrole
			
			//default search by chart
			String url = "select * from dbo.diabetByCommunity where community != 'Non-Cree Community' order by community asc";
		    cs=conn.createStatement();
		    cs.setEscapeProcessing(true);
		    rs = cs.executeQuery(url);
		    ArrayList<ArrayList<String>> series = new ArrayList<ArrayList<String>>();
		    ArrayList<Object> men = new ArrayList<Object>();
	    	ArrayList<Object> women = new ArrayList<Object>();
		    while(rs.next()){
		    	ArrayList<Object> vmen = new ArrayList<Object>();
			    ArrayList<Object> vwomen = new ArrayList<Object>();
			    if(!graphtype.equals("pyramid")){
			    	vmen.add(rs.getString("community"));
			    	vmen.add(rs.getInt("men"));
			    	men.add(vmen);
			    }else{
			    	men.add(rs.getInt("men"));
			    }
			    
			    if(!graphtype.equals("pyramid")){
			    	vwomen.add(rs.getString("community"));
			    	vwomen.add(rs.getString("women"));
			    	women.add(vwomen);
			    }else{
			    	women.add(rs.getInt("women"));
			    }
		    }
		    //series.add(men);
	    	//series.add(women);
		    
	    	result.add(men);
	    	result.add(women);
		}catch (SQLException se) {
		    se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	
	public ArrayList<Object> getDiabetByType(){
		ArrayList<Object> result = new ArrayList<Object>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		//PreparedStatement cs=null;
		Statement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			//   idsystem system_name system_code idrole
			String url = "select * from dbo.diabetByType";
			
		    cs=conn.createStatement();
		    cs.setEscapeProcessing(true);
		    rs = cs.executeQuery(url);
		    ArrayList<ArrayList<String>> series = new ArrayList<ArrayList<String>>();
		    ArrayList<Object> men = new ArrayList<Object>();
	    	ArrayList<Object> women = new ArrayList<Object>();
		    while(rs.next()){
		    	ArrayList<Object> vmen = new ArrayList<Object>();
			    ArrayList<Object> vwomen = new ArrayList<Object>();
			    vmen.add(rs.getString("diabet"));
			    vmen.add(rs.getInt("men"));
		    	
			    vwomen.add(rs.getString("diabet"));
		    	vwomen.add(rs.getInt("women"));
		    	
		    	men.add(vmen);
		    	women.add(vwomen);
		    }
	    	result.add(men);
	    	result.add(women);
		}catch (SQLException se) {
		    se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	
	
	public ArrayList<Object> getDiabetByYear(String year){
		ArrayList<Object> result = new ArrayList<Object>();
		ArrayList<Object> dataset = new ArrayList<Object>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		//PreparedStatement cs=null;
		Statement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			//   idsystem system_name system_code idrole
			String sqlSeries = "select distinct diabet from dbo.DiabetByYear";
			
			
		    cs=conn.createStatement();
		    cs.setEscapeProcessing(true);
		    rs = cs.executeQuery(sqlSeries);
		    ArrayList<Object> series = new ArrayList<Object>();
		    while(rs.next()){
		    	Hashtable<String, String> ob = new Hashtable<String, String>() ;
		    	ob.put("label", rs.getString(1));
		    	series.add(ob);
		    }
		    cs.clearBatch();
		    result.add(series);
		    
		    for(int i=0;i<series.size();i++){
		    	ArrayList<Object> serie = new ArrayList<Object>();
		    	Hashtable<String, String> vs = (Hashtable<String, String>)series.get(i);
		    	String url = "select * from dbo.DiabetByYear where  year > year(GETDATE()) - "+year+" and diabet='"+vs.get("label")+"'  order by year asc, diabet";
		    	rs = cs.executeQuery(url);
		    	
		    	Calendar date = new GregorianCalendar();
		    	int yearNow = date.get(Calendar.YEAR);
		    	int yearFun = Integer.parseInt(year);
		    	
		    	for(int j=(yearNow - yearFun +1);j<=yearNow;j++){
		    		ArrayList<Object> vals = new ArrayList<Object>();
		    		vals.add(Integer.toString(j));
		    		vals.add(0);
		    		serie.add(vals);
		    	}
		    	while(rs.next()){
			    	String yearVal = rs.getString("year");
			    	for(int k=0;k<serie.size();k++){
			    		ArrayList<Object> vval = (ArrayList<Object>)serie.get(k);
			    		String  vY = (String) vval.get(0);
			    		if(vY.equals(yearVal)){
			    			vval.set(1, rs.getInt("patients"));
			    		}
			    	}
			    }
		    	cs.clearBatch();
		    	dataset.add(serie);
	    	}
		    result.add(dataset);
		    
		}catch (SQLException se) {
		    se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	
	public String getLastLogin(String iduser){
		String result = "";
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		Statement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			//   idsystem system_name system_code idrole
			
			//default search by chart
			String url = "select convert(nvarchar(MAX), max(nns.created), 20)  from ncdis.ncdis.session nns left join ncdis.ncdis.events nne on nns.idsession = nne.idsession  left join ncdis.ncdis.action nna on nne.idaction = nna.idaction where nns.iduser = '"+iduser+"' and nna.action_code = 'LOGIN'";
		    cs=conn.createStatement();
		    cs.setEscapeProcessing(true);
		    rs = cs.executeQuery(url);
		    while(rs.next()){
		    	result = rs.getString(1);
		    }
		    
		}catch (SQLException se) {
		    se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}

	public HashMap<String, String> getLastPatient(String iduser){
		HashMap<String, String> result = new HashMap<>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		Statement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			String url = "select  nnc.system_code app, nne.data from ncdis.ncdis.events nne" 
					+" left join ncdis.ncdis.session nns on nne.idsession = nns.idsession"
						+" left join ncdis.ncdis.action nna on nne.idaction = nna.idaction"
						+" left join ncdis.ncdis.cisystems nnc on nne.idsystem = nnc.idsystem"
						+" where  nna.action_code = 'VIEWP' and nne.iduser = '"+iduser+"'" 
						+" and nne.created = (select max(created) from ncdis.ncdis.events where iduser = '"+iduser+"' and idaction = (select idaction from ncdis.ncdis.action where action_code = 'VIEWP'))";
		    cs=conn.createStatement();
		    cs.setEscapeProcessing(true);
		    rs = cs.executeQuery(url);
		    while(rs.next()){
		    	result.put(rs.getString("app"), rs.getString("data"));
		    }
		    
		}catch (SQLException se) {
		    se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}

	public HashMap<String, String> getLastReport(String iduser){
		HashMap<String, String> result = new HashMap<>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		Statement cs=null;
		Connection conn = null;
		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			String url = "select nnr.idreport, nnr.report_name" 
					+" from ncdis.ncdis.reports nnr"
						+" inner join (select nne.* from ncdis.ncdis.events nne"
								+" left join ncdis.ncdis.action nna on nne.idaction = nna.idaction where nna.action_code = 'REPORT') as ne on nnr.idreport = ne.data"
						+" where ne.iduser = '"+iduser+"'"
						+" and ne.created = (select max(created) from ncdis.ncdis.events where iduser = '"+iduser+"' and idaction = (select idaction from ncdis.ncdis.action where action_code = 'REPORT'))";
		    cs=conn.createStatement();
		    cs.setEscapeProcessing(true);

		    rs = cs.executeQuery(url);
		    while(rs.next()){
		    	result.put("id", rs.getString("idreport"));
		    	result.put("name", rs.getString("report_name"));
		    }
		    
		}catch (SQLException se) {
		    se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	
	public ArrayList<Object> getUserMessages(String iduser){
		ArrayList<Object> result = new ArrayList<>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		ResultSet rs1 = null;
		PreparedStatement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			//Context envContext  = (Context)initContext.lookup("java:comp/env");
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.prepareStatement("select uu.lname as from_user_lname,uu.fname as from_user_fname, mm.*,convert(nvarchar(MAX), mm.created, 23) as dcreate from ncdis.mesages mm left join ncdis.users uu on mm.from_iduser = uu.iduser where mm.to_iduser = ?");
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    cs.setString(1, iduser);
		    rs = cs.executeQuery();
		    ArrayList<Message> received = new ArrayList<>();
		    while (rs.next()) {

		    	received.add(new Message(rs.getString("idmessage"), rs.getString("from_user_fname")+" "+rs.getString("from_user_lname"),rs.getString("to_iduser"), rs.getBoolean("read"), rs.getBoolean("isdelivered"), rs.getString("dcreate"),rs.getString("message")));
		    }
		    result.add(received);
		    
		    cs.clearBatch();
		    cs=conn.prepareStatement("select uu.lname as from_user_lname,uu.fname as from_user_fname, mm.*,convert(nvarchar(MAX), mm.created, 23) as dcreate from ncdis.mesages mm left join ncdis.users uu on mm.to_iduser = uu.iduser where mm.from_iduser = ?");
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    cs.setString(1, iduser);
		    rs1 = cs.executeQuery();
		    ArrayList<Message> sent = new ArrayList<>();
		    while (rs1.next()) {
		    	sent.add(new Message(rs1.getString("idmessage"),rs1.getString("from_iduser"), rs1.getString("from_user_fname")+" "+rs1.getString("from_user_lname"), rs1.getBoolean("read"), rs1.getBoolean("isdelivered"), rs1.getString("dcreate"),rs1.getString("message")));
		    }
		    result.add(sent);
		    
		}catch (SQLException se) {
		      se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            rs.close();
	            rs1.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	
	
	public HashMap<String, String> getCisystem(int idsystem){
		HashMap<String, String> result = new HashMap<String, String>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		//PreparedStatement cs=null;
		Statement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			//   idsystem system_name system_code idrole
			
			//default search by chart
			String url = "select * from ncdis.ncdis.cisystems where idsystem = '"+idsystem+"'";
		    cs=conn.createStatement();
		    cs.setEscapeProcessing(true);
		    rs = cs.executeQuery(url);
		    while(rs.next()){
		    	result.put("idsystem", rs.getString("idsystem"));
		    	result.put("system_name", rs.getString("system_name"));
		    	result.put("system_code", rs.getString("system_code"));
		    }
		    
		}catch (SQLException se) {
		    se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	
	public HashMap<String, String> getRole(int idrole){
		HashMap<String, String> result = new HashMap<String, String>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		//PreparedStatement cs=null;
		Statement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			//   idsystem system_name system_code idrole
			
			//default search by chart
			String url = "select * from ncdis.ncdis.role where idrole = '"+idrole+"'";
		    cs=conn.createStatement();
		    cs.setEscapeProcessing(true);
		    rs = cs.executeQuery(url);
		    while(rs.next()){
		    	result.put("idrole", rs.getString("idrole"));
		    	result.put("role_name", rs.getString("role_name"));
		    	result.put("role_code", rs.getString("role_code"));
		    }
		    
		}catch (SQLException se) {
		    se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	
	
	public ArrayList<ArrayList<String>> getUserActions(){
		ArrayList<ArrayList<String>> result = new ArrayList<>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			//Context envContext  = (Context)initContext.lookup("java:comp/env");
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			String sql = "select top 1000  uu.lname, uu.fname, aa.action_name, ee.data, ee.created 	from ncdis.ncdis.events ee 	left join ncdis.ncdis.users uu on ee.iduser = uu.iduser left join ncdis.ncdis.action aa on ee.idaction = aa.idaction order by ee.created desc";
		    cs=conn.prepareStatement(sql);
		    
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    rs = cs.executeQuery();
		    while (rs.next()) {
		    	ArrayList<String> line = new ArrayList<>();
		    	line.add(rs.getString(1));
		    	line.add(rs.getString(2));
		    	line.add(rs.getString(3));
		    	line.add(rs.getString(4));
		    	line.add(rs.getString(5));
		    	result.add(line);
		    }
		    
		}catch (SQLException se) {
		        se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}

	
public ArrayList<Note> getUserNotes(String iduserto){
	ArrayList<Note> result = new ArrayList<>();
	Context initContext;
	DataSource ds;
	ResultSet rs = null;
	Statement cs=null;
	Connection conn = null;
	String sql = "select * from ncdis.ncdis.notes nn where iduserto="+iduserto+" and active=1 and viewed=0 order by datenote desc";
	
	try {
		initContext = new InitialContext();
		ds = (DataSource)initContext.lookup("jdbc/ncdis");
		conn = ds.getConnection();
	    cs=conn.createStatement();		    
	    cs.setEscapeProcessing(true);
	    rs = cs.executeQuery(sql);

	    while (rs.next()) {
	    	Note note = new Note(rs.getString("idnote"),rs.getString("note"),rs.getString("datenote"),rs.getString("iduser"),rs.getString("idpatient"),rs.getString("active"), rs.getString("iduserto"), rs.getString("viewed"));
	    	result.add(note);
	    }

	}catch (SQLException se) {
        se.printStackTrace();
    } catch (NamingException e) {
		e.printStackTrace();
	} finally {
        try {
            rs.close();
            cs.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
   }
	return result;
}
	
	
	
	public ArrayList<ArrayList<Object>> getUserActionsTop5Dataset(){
		ArrayList<ArrayList<Object>> result = new ArrayList<>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			//Context envContext  = (Context)initContext.lookup("java:comp/env");
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			String sql = "select top 5 aa.action_name,count(ee.idaction)  from ncdis.ncdis.events ee left join ncdis.ncdis.action aa on ee.idaction=aa.idaction where ee.created between DATEDIFF(day,7,getdate())  and getdate() group by ee.idaction,aa.action_name";
		    cs=conn.prepareStatement(sql);
		    
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    rs = cs.executeQuery();
		    while (rs.next()) {
		    	ArrayList<Object> line = new ArrayList<>();
		    	line.add(rs.getString(1));
		    	line.add(rs.getInt(2));
		    	result.add(line);
		    }
		    
		}catch (SQLException se) {
		        se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	
	
	public ArrayList<ArrayList<Object>> getUserTop5Dataset(){
		ArrayList<ArrayList<Object>> result = new ArrayList<>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			//Context envContext  = (Context)initContext.lookup("java:comp/env");
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			String sql = "select top 5  concat(uu.lname, ' ', uu.fname), count(ee.iduser) acts from ncdis.ncdis.events ee left join ncdis.ncdis.users uu on ee.iduser = uu.iduser where ee.created between DATEDIFF(day,7,getdate())  and getdate() group by  ee.iduser, uu.lname, uu.fname order by acts desc";
		    cs=conn.prepareStatement(sql);
		    
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    rs = cs.executeQuery();
		    while (rs.next()) {
		    	ArrayList<Object> line = new ArrayList<>();
		    	line.add(rs.getString(1));
		    	line.add(rs.getInt(2));
		    	result.add(line);
		    }
		    
		}catch (SQLException se) {
		        se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	
	
	public boolean readPatientNote(String noteid){
		boolean result = false;
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			//Context envContext  = (Context)initContext.lookup("java:comp/env");
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			String sql = "update ncdis.ncdis.notes set viewed=1 where idnote = '"+noteid+"'";
		    cs=conn.prepareStatement(sql);
		    
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    cs.executeUpdate();
		    result = true;
		}catch (SQLException se) {
		        se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	
	public boolean deletePatientNote(String noteid){
		boolean result = false;
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			//Context envContext  = (Context)initContext.lookup("java:comp/env");
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			String sql = "delete from ncdis.ncdis.notes where idnote = '"+noteid+"'";
		    cs=conn.prepareStatement(sql);
		    
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    cs.executeUpdate();
		    result = true;
		}catch (SQLException se) {
		        se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}

	
	public ScheduleVisit getScheduleVisit(String idpatient, String iduser){
		ScheduleVisit result  = new ScheduleVisit();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			//Context envContext  = (Context)initContext.lookup("java:comp/env");
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			String sql = "select top 1 case when datediff(month, DATEADD(month, DATEDIFF(month, 0, datevisit), 0), getdate()) > 0 then dateadd(month,frequency * ceiling( datediff(month, DATEADD(month, DATEDIFF(month, 0, datevisit), 0), getdate() ) / cast(frequency as float)) ,datevisit) else	datevisit end as nextdate , idpatient,iduser, idprofesion, frequency from ncdis.ncdis.schedulevisits  where idpatient="+idpatient+" and iduser="+iduser+" order by datevisit desc";
			//System.out.println(sql);
		    cs=conn.prepareStatement(sql);
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    rs = cs.executeQuery();
		    while (rs.next()) {
		    	result = new ScheduleVisit(rs.getString(2), rs.getString(3), rs.getString(1), rs.getString(5), rs.getString(4));
		    }
		    
		}catch (SQLException se) {
			se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            if(rs != null)rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	            
	        }
	   }
	return result;
	}

	public boolean setScheduleVisit(String idschedule, String iduser, String idpatient, String scheduledate, String idprofesion, String frequency){
		boolean result = false;
		Context initContext;
		DataSource ds;
		//ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			//   idsystem system_name system_code idrole
			if(idschedule.equals("0")){
				cs=conn.prepareStatement("insert into ncdis.ncdis.schedulevisits (iduser,datevisit,idpatient,idprofesion,frequency) values (?,?,?,?,?)");
				cs.setEscapeProcessing(true);
			    cs.setQueryTimeout(90);
			    cs.setString(1, iduser);
			    cs.setString(2, scheduledate);
			    cs.setString(3, idpatient);
			    cs.setString(4, idprofesion);
			    cs.setString(5, frequency);
			}else{
				cs=conn.prepareStatement("update ncdi.ncdis.schedulevisits set iduser=?, datevisit=?, idpatient=?, frequency=? where idschedulevisits=?");
				cs.setEscapeProcessing(true);
			    cs.setQueryTimeout(90);
			    cs.setString(1, iduser);
			    cs.setString(2, scheduledate);
			    cs.setString(3, idpatient);
			    cs.setString(4, frequency);
			    cs.setString(5, idschedule);
			}
		    
		    
		    cs.executeUpdate();
		    result = true;
		}catch (SQLException se) {
		    se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            //rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}

	
	public ArrayList<Object> getUserPatients(String iduser, String hcpcat){
		ArrayList<Object> result = new ArrayList<>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		ResultSet rs1 = null;
		PreparedStatement cs=null;
		Connection conn = null;

		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    String sql = "SELECT pp.idpatient,concat(pp.fname,' ',pp.lname) as fullname, pp.ramq,pp.chart,pp.idcommunity, cc.name_en"
		    + " FROM [ncdis].[ncdis].[patient] pp "
		    + " left join ncdis.ncdis.community cc on pp.idcommunity = cc.idcommunity"
		    + " left join ncdis.ncdis.patient_hcp ph on pp.idpatient = ph.idpatient "
		    + " where pp.active=1 and (pp.dod is null or pp.dod='1900-01-01') and ph."+hcpcat+" = '"+iduser+"'";
		    //System.out.println(sql);
		    cs = conn.prepareStatement(sql);
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    rs = cs.executeQuery();
		    //ArrayList<Message> received = new ArrayList<>();
		    while (rs.next()) {
		    	HashMap<String, String> obj = new HashMap<>();
		    	obj.put("fullname", rs.getString(2));
		    	obj.put("idpatient", rs.getString(1));
		    	obj.put("ramq", rs.getString(3));
		    	obj.put("chart", rs.getString(4));
		    	obj.put("idcommunity", rs.getString(5));
		    	obj.put("community", rs.getString(6));
		    	ScheduleVisit sv = getScheduleVisit(rs.getString(1), iduser);
		    	obj.put("datevisit", sv.getDatevisit());
		    	result.add(obj);
		    }
		    
		}catch (SQLException se) {
		      se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} finally {
	        try {
	            rs.close();
	            cs.close();
	            conn.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}

	
	
}
