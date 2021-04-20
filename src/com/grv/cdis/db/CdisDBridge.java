package com.grv.cdis.db;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.grv.cdis.model.Hcp;
import com.grv.cdis.model.MessageResponse;
import com.grv.cdis.model.Note;
import com.grv.cdis.model.Patient;
import com.grv.cdis.model.Renderer;
import com.grv.cdis.model.Report;
import com.grv.cdis.model.ReportCriteria;
import com.grv.cdis.model.ReportSubcriteria;
import com.grv.cdis.model.Value;
import com.grv.cdis.model.ValueLimit;
import com.grv.cdis.model.Values;
import com.grv.cdis.util.FileTool;



public class CdisDBridge {
	
	public ArrayList<String> getAllCommunities(){
		ArrayList<String> result = new ArrayList<String>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		Statement cs=null;
		Connection conn = null;
		String sql = "select name_en from ncdis.community where community_code != 'NONC' order by name_en asc";
		try {
			initContext = new InitialContext();
			//Context envContext  = (Context)initContext.lookup("java:comp/env");
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.createStatement();		    
		    cs.setEscapeProcessing(true);
		    rs = cs.executeQuery(sql);
		    while (rs.next()) {
		    	result.add(rs.getString(1));
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
	
	public ArrayList<HashMap<String, String>> getHcps(String hcp , String term){
		ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		Statement cs=null;
		Connection conn = null;
		String sql = "SELECT iduser,concat(UPPER(LEFT(fname,1))+LOWER(SUBSTRING(fname,2,LEN(fname))),' ',UPPER(LEFT(lname,1))+LOWER(SUBSTRING(lname,2,LEN(lname)))) FROM ncdis.ncdis.users where active=1 and idprofesion = (select [idprofesion] from ncdis.ncdis.profesion where profesion_code = '"+hcp.toUpperCase()+"') and (lname like '%"+term+"%' or fname like '%"+term+"%')";
		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.createStatement();		    
		    cs.setEscapeProcessing(true);
		    rs = cs.executeQuery(sql);
		    while (rs.next()) {
		    	HashMap<String, String> m = new HashMap<>();
		    	m.put("iduser", rs.getString(1));
		    	m.put("name", rs.getString(2));
		    	result.add(m);
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
	
	
	public Patient getPatientByRamq(String ramq){
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;
		Patient result = new Patient();
		try {
			initContext = new InitialContext();
			//Context envContext  = (Context)initContext.lookup("java:comp/env");
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.prepareStatement("exec sp_getPatientByRamq ?");
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    cs.setString(1, ramq);
		    rs = cs.executeQuery();
		    while (rs.next()) {
		        result = 	new Patient(rs.getInt("idpatient"), rs.getString("ramq"), rs.getString("chart"), rs.getString("band"),
		        		rs.getString("giu"), rs.getString("jbnqa"), rs.getString("fname"), rs.getString("lname"), rs.getString("sex"),
		        		rs.getString("dob"), rs.getString("mfname"), rs.getString("mlname"), rs.getString("pfname"),
		    			rs.getString("plname"), rs.getString("address"), rs.getString("city"), rs.getString("province"),
		    			rs.getString("postalcode"), rs.getInt("consent"), rs.getInt("iscree"), rs.getString("dod"),
		    			rs.getString("dcause"), rs.getString("entrydate"), rs.getString("idcommunity"), rs.getString("community"), rs.getString("idprovince"),rs.getString("phone")); 
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
	
	
	
	public Patient getPatientById(int id){
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;
		Patient result = null;
		try {
			initContext = new InitialContext();
			//Context envContext  = (Context)initContext.lookup("java:comp/env");
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.prepareStatement("exec sp_getPatientById ?");
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    cs.setInt(1, id);
		    rs = cs.executeQuery();
		    while (rs.next()) {
		        result = 	new Patient(rs.getInt("idpatient"), rs.getString("ramq"), rs.getString("chart"), rs.getString("band"),
		        		rs.getString("giu"), rs.getString("jbnqa"), rs.getString("fname"), rs.getString("lname"), rs.getString("sex"),
		        		rs.getString("dob"), rs.getString("mfname"), rs.getString("mlname"), rs.getString("pfname"),
		    			rs.getString("plname"), rs.getString("address"), rs.getString("city"), rs.getString("province"),
		    			rs.getString("postalcode"), rs.getInt("consent"), rs.getInt("iscree"), rs.getString("dod"),
		    			rs.getString("dcause"), rs.getString("entrydate"), rs.getString("idcommunity"), rs.getString("community"), rs.getString("idprovince"),rs.getString("phone")); 
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

	public MessageResponse updatePatient(Patient pat){
		Context initContext;
		DataSource ds;
		PreparedStatement cs=null;
		Connection conn = null;
		MessageResponse result = new MessageResponse("EDITP-DB",false,"en",new ArrayList<>());
		
		try {
			initContext = new InitialContext();
			//Context envContext  = (Context)initContext.lookup("java:comp/env");
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			String query = "update ncdis.ncdis.patient set ";
			
		    //cs=conn.prepareStatement("update ncdis.ncdis.patient set ramq=?, chart=?, band=?, giu=?, jbnqa=?, fname=?, lname=?, sex=?,dob=?, mfname=?, mlname=?, pfname=?, plname=?, address=?, postalcode=?, dod=?, death_cause=?, idcommunity=? where idpatient=?");
			
		    cs=conn.prepareStatement("update ncdis.ncdis.patient set chart=?, band=?, giu=?, jbnqa=?, fname=?, lname=?, sex=?,"
		    		+ "dob=?, mfname=?, mlname=?, pfname=?, plname=?, address=?, postalcode=?, dod=?, death_cause=?, idcommunity=?, iscree=?, phone=? where idpatient=?");
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    //cs.setString(1, pat.getRamq());
		    cs.setString(1, pat.getChart());
		    
		    cs.setString(2, pat.getBand());
		    cs.setString(3, pat.getGiu());
		    cs.setString(4, pat.getJbnqa());
		    cs.setString(5, pat.getFname());
		    cs.setString(6, pat.getLname());
		    cs.setString(7, pat.getSex());
		    cs.setString(8, pat.getDob());
		    cs.setString(9, pat.getMfname());
		    cs.setString(10, pat.getMlname());
		    cs.setString(11, pat.getPfname());
		    cs.setString(12, pat.getPlname());
		    cs.setString(13, pat.getAddress());
		    cs.setString(14, pat.getPostalcode());
		    cs.setString(15, pat.getDod());
		    cs.setString(16, pat.getDcause());
		    cs.setString(17, pat.getIdcommunity());
		    cs.setInt(18, pat.getIscree());
		    cs.setString(19, pat.getPhone());
		    cs.setInt(20, pat.getIdpatient());
		    cs.executeUpdate();
		    result.setStatus(0);
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
	
	public MessageResponse addPatient(Patient pat){
		Context initContext;
		DataSource ds;
		PreparedStatement cs=null;
		Connection conn = null;
		MessageResponse result = new MessageResponse("EDITP-DB",false,"en",new ArrayList<>());
		
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
			initContext = new InitialContext();
			//Context envContext  = (Context)initContext.lookup("java:comp/env");
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
			String query = "insert into ncdis.ncdis.patient (ramq,chart,band,giu,jbnqa,fname,lname,sex,dob,mfname,mlname,pfname,plname,address,postalcode,dod,death_cause,idcommunity,phone,entrydate,active) "
					+" values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			
		    cs=conn.prepareStatement(query);
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    cs.setString(1, pat.getRamq().toUpperCase());
		    cs.setString(2, pat.getChart());
		    cs.setString(3, pat.getBand().toUpperCase());
		    cs.setString(4, pat.getGiu());
		    cs.setString(5, pat.getJbnqa());
		    cs.setString(6, pat.getFname());
		    cs.setString(7, pat.getLname());
		    cs.setString(8, pat.getSex());
		    cs.setString(9, pat.getDob());
		    cs.setString(10, pat.getMfname());
		    cs.setString(11, pat.getMlname());
		    cs.setString(12, pat.getPfname());
		    cs.setString(13, pat.getPlname());
		    cs.setString(14, pat.getAddress());
		    cs.setString(15, pat.getPostalcode());
		    cs.setString(16, pat.getDod());
		    cs.setString(17, pat.getDcause());
		    cs.setString(18, pat.getIdcommunity());
		    cs.setString(19, pat.getPhone());
		    cs.setString(20, sdf.format(new Date()));
		    cs.setString(21, "1");
		    
		    cs.executeUpdate();
		    
		    result.setStatus(0);
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
	
	
	public Hcp getHcpById(String idpatient){
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;
		Hcp result = null;
		try {
			initContext = new InitialContext();
			//Context envContext  = (Context)initContext.lookup("java:comp/env");
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.prepareStatement("exec sp_getHcpById ?");
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    cs.setString(1, idpatient);
		    rs = cs.executeQuery();
		    while (rs.next()) {
		        result = 	new Hcp(rs.getString("casem"), rs.getString("md"), rs.getString("nut"), rs.getString("nur"), rs.getString("chr"), rs.getString("idpatient")); 
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
	
	
	public Hcp getHcpOfPatient(int idpatient){
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;
		Hcp result = new Hcp();
		try {
			initContext = new InitialContext();
			//Context envContext  = (Context)initContext.lookup("java:comp/env");
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.prepareStatement("exec sp_getHcpOfPatient ?");
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    cs.setInt(1, idpatient);
		    rs = cs.executeQuery();
		    while (rs.next()) {
		       result = new Hcp(rs.getString("casem"), rs.getString("md"), rs.getString("nut"), rs.getString("nur"), rs.getString("chr"), rs.getString("idpatient"));
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
	
	
	public boolean setHcpOfPatient(int idpatient,  String casem, String md, String nut, String nur, String chr){
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;
		boolean result = false;
		try {
			initContext = new InitialContext();
			//Context envContext  = (Context)initContext.lookup("java:comp/env");
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.prepareStatement("exec sp_setHcpOfPatient ?, ?, ?, ?, ?, ?");
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    cs.setInt(1, idpatient);
		    cs.setString(2, casem);
		    cs.setString(3, md);
		    cs.setString(4, nut);
		    cs.setString(5, nur);
		    cs.setString(6, chr);
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
	
	public boolean setOneHcpOfPatient(String idpatient,  String iduser, String hcpcode){
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;
		boolean result = false;
		try {
			initContext = new InitialContext();
			//Context envContext  = (Context)initContext.lookup("java:comp/env");
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.prepareStatement("update ncdis.ncdis.patient_hcp set "+hcpcode+"="+iduser+" where idpatient="+idpatient);
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
	
	
	
	public Object getLatestValues(String section, int idpatient){
		Object result = null;
		
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;
		
		/*
		 * Param1Type param1;
			Param2Type param2;
			String className = "Class1";
			Class cl = Class.forName(className);
			Constructor con = cl.getConstructor(Param1Type.class, Param2Type.class);
			Object xyz = con.newInstance(param1, param2);
		 * */
		
		try {
			initContext = new InitialContext();
			//Context envContext  = (Context)initContext.lookup("java:comp/env");
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.prepareStatement("exec sp_getLatestOf"+section+" ?");
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    cs.setInt(1, idpatient);
		    rs = cs.executeQuery();
		    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		    HashMap<String, Values> params = new HashMap<String, Values>();
		    
		    ArrayList<String> columns = new ArrayList<>();
		    
		    while (rs.next()) {
		    	Values av = new Values();
		    	String c = rs.getString("code");
		    	
		    	if(columns.contains(c)){
		    		av = params.get(c);
		    	}else{
		    		columns.add(c);
		    	}
		    	String dStr = "NULL";
		    	if(rs.getDate("date") != null){
		    		dStr = sdf.format(rs.getDate("date"));
		    	}
		        Value val = new Value(rs.getInt("idvalue"), rs.getString("name"), rs.getString("value"), rs.getString("type"), dStr, rs.getString("unit"), rs.getString("code"), rs.getInt("dorder"));

		        av.addValue(val);
		        params.put(rs.getString("code"),av);
		    }
		    String className = "com.grv.cdis.model."+section;
		    
		    try {
		    	Class cl = Class.forName(className);
			    Constructor[] cons = cl.getConstructors();
			    //Constructor con = cons[0];
			    Constructor con =  cl.getConstructor(HashMap.class);
				result = con.newInstance(params);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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

	
	public Object getValues(String section, int idpatient, String sort){
		Object result = null;
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
		    cs=conn.prepareStatement("exec sp_getValuesOf"+section+" ?");
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    cs.setInt(1, idpatient);
		    rs = cs.executeQuery();
		    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		    HashMap<String, Values> params = new HashMap<String, Values>();
		    ArrayList<String> columns = new ArrayList<>();
		    while (rs.next()) {
		    	Values av = new Values();
		    	String c = rs.getString("code");
		    	if(columns.contains(c)){
		    		av = params.get(c);
		    	}else{
		    		columns.add(c);
		    	}
		    	String dStr = "NULL";
		    	if(rs.getDate("date") != null){
		    		dStr = sdf.format(rs.getDate("date"));
		    	}
		        Value val = new Value(rs.getInt("idvalue"), rs.getString("name"), rs.getString("value"), rs.getString("type"), dStr, rs.getString("unit"), rs.getString("code"), rs.getInt("dorder"));
		        av.addValue(val);
		        params.put(c,av);
		    }
		    String className = "com.grv.cdis.model."+section;
		    try {
		    	Class cl = Class.forName(className);
			    Constructor[] cons = cl.getConstructors();
			    //Constructor con = cons[0];
			    Constructor con =  cl.getConstructor(HashMap.class);
				result = con.newInstance(params);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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

	
	
	public Object getAllValues(String section, String sectionCODE, int idpatient, String sort){
		Object result = null;
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
			
			String sql = "select avv.idvalue , dv.data_name as name, dv.data_unit as unit, avv.datevalue as date, avv.value as value , dv.data_type as type, lower(dv.data_code) as code, dv.data_order as dorder"
					+ " from"
					+ " (select dd.iddata, dd.data_name, dd.data_unit , dd.data_code,dd.data_type, dd.data_order"
					+ " from ncdis.ncdis.cdis_data dd left join ncdis.ncdis.cdis_section css on dd.idsection = css.idsection "
					+ " where css.section_code = '"+sectionCODE+"') as dv"
					+ " "
				+ " left join (select vv.idvalue, vv.datevalue, vv.value, vv.iddata"
				+ " from ncdis.ncdis.cdis_value vv where vv.idpatient = ?  and vv.iddata in (select dd.iddata"
										+ " from ncdis.ncdis.cdis_data dd left join ncdis.ncdis.cdis_section css on dd.idsection = css.idsection"
										+ " where css.section_code = '"+sectionCODE+"' )"
				+ " ) avv on dv.iddata = avv.iddata order by avv.datevalue desc";
			
			
		    cs=conn.prepareStatement(sql);
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    cs.setInt(1, idpatient);
		    rs = cs.executeQuery();
		    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		    HashMap<String, Values> params = new HashMap<String, Values>();
		    ArrayList<String> columns = new ArrayList<>();
		    while (rs.next()) {
		    	Values av = new Values();
		    	String c = rs.getString("code");
		    	if(columns.contains(c)){
		    		av = params.get(c);
		    	}else{
		    		columns.add(c);
		    	}
		    	String dStr = "NULL";
		    	if(rs.getDate("date") != null){
		    		dStr = sdf.format(rs.getDate("date"));
		    	}
		        Value val = new Value(rs.getInt("idvalue"), rs.getString("name"), rs.getString("value"), rs.getString("type"), dStr, rs.getString("unit"), rs.getString("code"), rs.getInt("dorder"));
		        av.addValue(val);
		        params.put(c,av);
		    }
		    String className = "com.grv.cdis.model."+section;
		    try {
		    	Class cl = Class.forName(className);
			    Constructor[] cons = cl.getConstructors();
			    //Constructor con = cons[0];
			    Constructor con =  cl.getConstructor(HashMap.class);
				result = con.newInstance(params);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
	
	
	
	
	public ValueLimit getValueLimits(String valuename){
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		Statement cs=null;
		Connection conn = null;
		ValueLimit result = null;
		String sql = "select cdl.*  from ncdis.ncdis.cdis_data_limit cdl left join ncdis.ncdis.cdis_data cd on cdl.iddata=cd.iddata where cd.data_code = '"+valuename+"'";
		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.createStatement();		    
		    cs.setEscapeProcessing(true);
		    rs = cs.executeQuery(sql);
		    while (rs.next()) {
		    	result = new ValueLimit(rs.getString("minvalue"), rs.getString("maxvalue"), rs.getString("startvalue"), rs.getString("endvalue"));
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
	
	public HashMap<String, String> getABCData(String idpatient){
		HashMap<String, String> result = new HashMap<>();
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
		    cs=conn.prepareStatement("select * from ncdis.configuration cc where cc.keia like 'abcgraph%' order by keia asc");
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    //cs.setString(1, "abcgraph");
		    rs = cs.executeQuery();
		    
		    while (rs.next()) {
		    	String k = rs.getString("keia");
		    	String v = rs.getString("value");
		    	result.put(k, v);
		    }
		    
		}catch (SQLException se) {
		      se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
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

	public boolean addValue(String valueName, String valueValue, String valueDate, String idpatient){
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		Statement cs=null;
		Connection conn = null;
		boolean result = false;
		String sql = "insert into ncdis.ncdis.cdis_value (idpatient, datevalue, value, iddata) values ("+idpatient+", '"+valueDate+"', '"+valueValue+"', (select iddata from ncdis.ncdis.cdis_data where data_code = '"+valueName+"'));";
		
		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.createStatement();		    
		    cs.setEscapeProcessing(true);
		    cs.executeUpdate(sql);
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
	
	public boolean deleteValue(String idvalue){
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		Statement cs=null;
		Connection conn = null;
		boolean result = false;
		String sql = "delete from ncdis.ncdis.cdis_value where idvalue = '"+idvalue+"'";
		
		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.createStatement();		    
		    cs.setEscapeProcessing(true);
		    cs.executeUpdate(sql);
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
	
	
	public boolean editValue(String valueName, String valueValue, String valueDate, String idpatient, String idvalue){
		Context initContext;
		DataSource ds;
		//ResultSet rs = null;
		Statement cs=null;
		Connection conn = null;
		boolean result = false;
		String sql = "update ncdis.ncdis.cdis_value set datevalue = '"+valueDate+"', value = '"+valueValue+"' where idpatient = '"+idpatient+"' and iddata = (select iddata from ncdis.ncdis.cdis_data where data_code = '"+valueName+"') and idvalue = '"+idvalue+"';";
		//System.out.println(sql);
		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.createStatement();		    
		    cs.setEscapeProcessing(true);
		    cs.executeUpdate(sql);
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
	
	public boolean editPatient(Patient patient){
		Context initContext;
		DataSource ds;
		//ResultSet rs = null;
		Statement cs=null;
		Connection conn = null;
		boolean result = false;
		String sql = "update ncdis.ncdis.patient set "
				+ " band = '"+patient.getBand()+"', "
				+ " ramq = '"+patient.getRamq()+"', "
				+ " giu = '"+patient.getGiu()+"', "
				+ " jbnqa = '"+patient.getJbnqa()+"', "
				+ " fname = '"+patient.getFname()+"', "
				+ " lname = '"+patient.getLname()+"', "
				+ " sex = '"+patient.getSex()+"', "
				+ " dob = '"+((patient.getDob() == null)?"":patient.getDob())+"', " 
				+ " mfname = '"+patient.getMfname()+"', "
				+ " mlname = '"+patient.getMlname()+"', "
				+ " pfname = '"+patient.getPfname()+"', "
				+ " plname = '"+patient.getPlname()+"', "
				+ " address = '"+patient.getAddress()+"', "
				+ " city = '"+patient.getCity()+"', "
				+ " idprovince = "+patient.getIdprovince()+", "
				+ " postalcode = '"+patient.getPostalcode()+"', "
				+ " consent = "+patient.getConsent()+", "
				+ " iscree = "+patient.getIscree()+", "
				+ " dod = '"+((patient.getDod() == null)?"":patient.getDod())+"', "
				+ " death_cause = '"+patient.getDcause()+"', "
				+ " idcommunity = "+patient.getIdcommunity()+" "
				+ " where idpatient = '"+patient.getIdpatient()+"' ";
		
		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.createStatement();		    
		    cs.setEscapeProcessing(true);
		    cs.executeUpdate(sql);
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
	
	
	public boolean deletePatient(String idpatient){
		Context initContext;
		DataSource ds;
		//ResultSet rs = null;
		Statement cs=null;
		Connection conn = null;
		boolean result = false;
		String sql = "update ncdis.ncdis.patient  set active=0 where idpatient = '"+idpatient+"'";
		
		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.createStatement();		    
		    cs.setEscapeProcessing(true);
		    cs.executeUpdate(sql);
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
	
	
	
	public ArrayList<Report> getReports(String iduser, String idcommunity, String type){
		ArrayList<Report> result = new ArrayList<>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		Statement cs=null;
		Connection conn = null;
		//String sql = "select rr.*,CONCAT(uu.fname, uu.lname) as owner  from ncdis.ncdis.reports rr left join ncdis.ncdis.users uu on rr.iduser = uu.iduser where rr.report_code like '"+type+"%' and uu.iduser='"+iduser+"'";
		
		String sql = "";
		
		if(type.equals("ADMIN")){
			sql = "select rr.*,CONCAT(uu.fname, uu.lname) as owner  from ncdis.ncdis.reports rr left join ncdis.ncdis.users uu on rr.iduser = uu.iduser where rr.report_code like 'ADMIN%'";
		}else if(type.equals("PERSONAL")){
			sql = "select rr.*,CONCAT(uu.fname, uu.lname) as owner  from ncdis.ncdis.reports rr left join ncdis.ncdis.users uu on rr.iduser = uu.iduser where rr.iduser = '"+iduser+"' and rr.report_code like 'PERSONAL%'";
		}else if(type.equals("REP")){
			sql = "select rr.*,CONCAT(uu.fname, uu.lname) as owner  from ncdis.ncdis.reports rr left join ncdis.ncdis.users uu on rr.iduser = uu.iduser where rr.report_code like 'REP%'";
		}else if(type.equals("LIST")){
			sql = "select rr.*,CONCAT(uu.fname, uu.lname) as owner  from ncdis.ncdis.reports rr left join ncdis.ncdis.users uu on rr.iduser = uu.iduser where rr.report_code like 'LIST.%'";
		}
		
		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.createStatement();		    
		    cs.setEscapeProcessing(true);
		    rs = cs.executeQuery(sql);
		    while (rs.next()) {
		    	Report rep = new Report(rs.getString("idreport"), rs.getString("report_name"), rs.getString("report_code"), rs.getString("owner"), rs.getString("modified"));
		    	result.add(rep);
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

	
	public String saveReport(JsonObject reportObject){
		String result = "";
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		Statement cs=null;
		Connection conn = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		result = sdf.format(new Date());
		String sql = "insert into ncdis.ncdis.reports (report_name, report_code, iduser, created) values ('"+reportObject.get("title").getAsString()+"','PERSONAL"+sdf.format(new Date())+"','"+reportObject.get("iduser").getAsString()+"','"+reportObject.get("generated").getAsString()+"')";
		
		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.createStatement();		    
		    cs.setEscapeProcessing(true);
		    cs.executeUpdate(sql);
			    		
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

	
	public String getIddata(String dataName){
		String result = "";
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		Statement cs=null;
		Connection conn = null;
		
		String sql = "select iddata from ncdis.ncdis.cdis_data where data_code = '"+dataName.toUpperCase()+"'";
		
		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.createStatement();		    
		    cs.setEscapeProcessing(true);
		    rs = cs.executeQuery(sql);
			while (rs.next()){
				result = rs.getString(1);
			}    		
		    
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
	
	
	public ArrayList<ArrayList<String>> executeReport(ReportCriteria criteria, String reportType, ArrayList<ReportSubcriteria> subcriterias){
		
		//System.out.println("REPORT TYPE"+reportType   +"criteria = "+criteria.getName() +"  subcriteria size "+subcriterias.size());
		
		ArrayList<ArrayList<String>> result = new ArrayList<>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		Statement cs=null;
		Connection conn = null;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		//System.out.println("AAAAAA");
		if(reportType.equals("list")){
			
			//System.out.println("REPORT TYPE LIST");
			String val = criteria.getValue();
			String nom = criteria.getName();
			String op = Renderer.renderOperator(criteria.getOperator());
			
			if(criteria.getSection().equals("1")){
				String criteriaStr = " and nn."+nom+" "+op+" '"+val+"' ";
				if(op.equals("between")){
					String[] parts = val.split("\\s*and+\\s*");
					String part1 = parts[0].trim();
					String part2 = parts[1].trim();
					String between1 = "'"+part1+"'";
					String between2 = "'"+part2+"'";
					
					if(part1.indexOf("(") >= 0 && part1.indexOf(")") >= 0){
						between1 = part1;
					}
					if(part2.indexOf("(") >= 0 && part2.indexOf(")") >= 0){
						between2 = part2;
					}
					criteriaStr = " and nn."+nom+" "+op+" "+between1+" and "+ between2;
				}
				
				if(val.equals("all") || val.equals("0")){
					criteriaStr = "";
				}
				String vn = "nn."+nom;
				String sql = "select nn.idpatient, "+vn+"  from ncdis.ncdis.patient nn  "
						+ " where nn.idpatient > 0 and nn.active=1 and (nn.dod is null or nn.dod = '1900-01-01') "
						+ " "+ criteriaStr+ " "
						+ "order by nn.idpatient asc";
				//sql = "select * from ncdis.ncdis.patient";
				//System.out.println(sql);
				try {
					initContext = new InitialContext();
					ds = (DataSource)initContext.lookup("jdbc/ncdis");
					conn = ds.getConnection();
				    cs=conn.createStatement();		    
				    cs.setEscapeProcessing(true);
				    rs = cs.executeQuery(sql);
				    int index=0;
				    
				    while (rs.next()) {
				    	ArrayList<String> line = new ArrayList<>();
				    	if(rs.getString(nom) == null || rs.getString(nom).equals("null")) {
				    		line.add(Integer.toString(index));
				    		line.add(rs.getString("idpatient"));
				    		line.add(nom);
				    		line.add("");
				    		line.add("");
				    	}else{
				    		line.add(Integer.toString(index));
				    		line.add(rs.getString("idpatient"));
				    		line.add(nom);
				    		if(criteria.getName().equals("sex") || criteria.getName().equals("idcommunity") || criteria.getName().equals("dtype")){
				    			line.add( Renderer.renderName(nom+"."+rs.getString(nom)));
							}else{
								line.add(rs.getString(nom));
							}
				    		line.add("");
				    	}
				    	result.add(line);
				    	index++;
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
			}else{
				
				String sql = "";
				String criteriaStr = " and cast(nn.value as float) "+op+" "+val+" ";
				String criteriaStrSub = " and cast(aa.value as float) "+op+" "+val+" ";

				if(op.equals("=")){
					String[] parts = val.split("\\s*or+\\s*");
					if(parts.length > 1){
						criteriaStr = "  ";
						criteriaStrSub = "  ";
						for(int i=0;i<parts.length;i++){
							String part = parts[i].trim();
							if(i == 0){
								criteriaStr += " and (cast(nn.value as float) "+op+" "+part+" ";
								criteriaStrSub += " and (cast(aa.value as float) "+op+" "+part+" ";
							}else if(i == parts.length -1){
								criteriaStr += " or cast(nn.value as float) "+op+" "+part+") ";
								criteriaStrSub += " or cast(aa.value as float) "+op+" "+part+") ";
							}else{
								criteriaStr += " or cast(nn.value as float) "+op+" "+part+" ";
								criteriaStrSub += " or cast(aa.value as float) "+op+" "+part+" ";
							}
							
						}
					}
				}
				
				
				if(criteria.getDatevalue().equals("last")){
					if(val.equals("all") || val.equals("0")){
						criteriaStr = "";
						criteriaStrSub = "";
					}
					
					sql = "select aa.idpatient,replace(convert(varchar,aa.datevalue,102),'.','-') as datevalue, aa.value "
							+ " from ncdis.dbo.LastdateValue aa "
							+ "where isnumeric(aa.value)=1  "
							+ "and aa.iddata='"+criteria.getIddata()+"' "
							+ " "+ criteriaStrSub+ " "
							+ "order by aa.idpatient asc";
					
					
				}else if(criteria.getDatevalue().equals("all")){
					if(val.equals("all")){
						criteriaStr = "";
					}
					sql = "select nn.idpatient, replace(convert(varchar,nn.datevalue,102),'.','-') as datevalue,nn.value from ncdis.ncdis.cdis_value nn  "
							+ "where "
							+ "isnumeric(nn.value) =1 and  nn.iddata = '"+criteria.getIddata()+"' "
							+ " "+ criteriaStr+ " "
							+ "order by nn.idpatient asc";
				}else{
					if(val.equals("all")){
						criteriaStr = "";
					}
					
					String dateValue = criteria.getDatevalue();
					String dateOperator = criteria.getDateoperator();
					if(dateOperator.equals("between")){
						String[] parts = dateValue.split("\\s*and+\\s*");
						String part1 = parts[0].trim();
						String part2 = parts[1].trim();
						String between1 = "'"+part1+"'";
						String between2 = "'"+part2+"'";
						
						if(part1.indexOf("(") >= 0 && part1.indexOf(")") >= 0){
							between1 = part1;
						}
						if(part2.indexOf("(") >= 0 && part2.indexOf(")") >= 0){
							between2 = part2;
						}
						dateValue = between1+" and "+between2;
					}
					
					sql = "select nn.idpatient, replace(convert(varchar,nn.datevalue,102),'.','-') as datevalue,nn.value from ncdis.ncdis.cdis_value nn  "
							+ "where nn.datevalue "+Renderer.renderOperator(dateOperator)+" "+dateValue
									+ " and nn.iddata = '"+criteria.getIddata()+"' "
									+ " "+ criteriaStr+ " "
											+ "order by nn.idpatient asc";
					
					
				}
					
				//System.out.println(sql);
				
				try {
					initContext = new InitialContext();
					ds = (DataSource)initContext.lookup("jdbc/ncdis");
					conn = ds.getConnection();
				    cs=conn.createStatement();		    
				    cs.setEscapeProcessing(true);
				    rs = cs.executeQuery(sql);
				    int index = 0;
				    
				    while (rs.next()) {
				    	ArrayList<String> line = new ArrayList<>();
				    	line.add(Integer.toString(index));
			    		line.add(rs.getString("idpatient"));
			    		line.add(nom);
			    		if(criteria.getName().equals("sex") || criteria.getName().equals("idcommunity") || criteria.getName().equals("dtype")){
			    			line.add( Renderer.renderName(nom+"."+rs.getString("value")));
						}else{
							line.add(rs.getString("value"));
						}
			    		line.add(rs.getString("datevalue"));
			    		result.add(line);
			    		index++;
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
			}
		}else if(reportType.equals("graph")){
			
			String val = criteria.getValue();
			String nom = criteria.getName();
			String op = Renderer.renderOperator(criteria.getOperator());
			String sql = "";
			int sec = Integer.parseInt(criteria.getSection());
			
			
			if(sec == 1){
				String subStrFrom = "";
				String subStrWhere = "";
				if(subcriterias.size() > 0){
					for(int x=0;x<subcriterias.size();x++){
						ReportSubcriteria rsc = subcriterias.get(x);
						if(rsc.getSubsection().equals("1")){
							//subStrFrom = " left join ncdis.ncdis.patient dd on pp.idpatient = dd.idpatient ";
							subStrWhere = " and bb."+rsc.getSubname()+" "+Renderer.renderOperator(rsc.getSuboperator())+" '"+rsc.getSubvalue()+ "' ";
							sql = "select count(*) as cnt "
									+ " from ncdis.ncdis.patient bb"
									+ " inner join (select idpatient, dob as maxdate from ncdis.ncdis.patient where "+nom+" "+op+" '"+val+"' and (dod is null or dod ='1900-01-01')) cc"
									+ " on bb.idpatient = cc.idpatient"
									+ " where bb.idpatient > 0 and bb.active=1 and (bb.dod is null or bb.dod = '1900-01-01')"
									+ subStrWhere;
						}else{
							//subStrFrom = " left join ncdis.ncdis.cdis_value dd on pp.idpatient = dd.idpatient ";
							
							sql = "select count(*) cnt"
									+ " from ncdis.ncdis.patient bb"
									+ "     inner join 	(select xx.idpatient, max(xx.datevalue) as mdate from ncdis.ncdis.cdis_value xx where xx.iddata='"+criteria.getIddata()+"'	and cast(case when coalesce(patindex('%[0-9]%', xx.value),0) = 0  then '0' else xx.value end as float) "+Renderer.renderOperator(rsc.getSuboperator())+" '"+rsc.getSubvalue()+"' group by xx.idpatient) dd on dd.idpatient = bb.idpatient"
									+ " where"
									+ " bb.active  = '1' "
									+ " and "+nom+" "+op+" "+"'"+val+"'"
									+ " and (bb.dod is null or bb.dod = '1900-01-01')";

						}
					
						//sql = "select count(pp.idpatient) as cnt from ncdis.ncdis.patient pp "+subStrFrom+" where pp."+nom+" "+op+" '"+val+"' "+subStrWhere +"  ";
						try {
							initContext = new InitialContext();
							ds = (DataSource)initContext.lookup("jdbc/ncdis");
							conn = ds.getConnection();
						    cs=conn.createStatement();		    
						    cs.setEscapeProcessing(true);
						    
						    System.out.println(sql);
						    
						    rs = cs.executeQuery(sql);
						    int index = 0;
						    while (rs.next()) {
						    	ArrayList<String> line = new ArrayList<>();
						    	line.add(Integer.toString(index));
					    		line.add(nom);
								line.add(rs.getString("cnt"));
					    		result.add(line);
					    		index++;
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

					
					}
				}else{
					sql = "select count(pp.idpatient) as cnt from ncdis.ncdis.patient pp "+subStrFrom+" where pp.active=1 and (pp.dod is null or pp.dod = '1900-01-01') and pp."+nom+" "+op+" '"+val+"' "+subStrWhere +"  ";
					
					try {
						initContext = new InitialContext();
						ds = (DataSource)initContext.lookup("jdbc/ncdis");
						conn = ds.getConnection();
					    cs=conn.createStatement();		    
					    cs.setEscapeProcessing(true);
					    
					    //System.out.println(sql);
					    
					    rs = cs.executeQuery(sql);
					    int index = 0;
					    while (rs.next()) {
					    	ArrayList<String> line = new ArrayList<>();
					    	line.add(Integer.toString(index));
				    		line.add(nom);
							line.add(rs.getString("cnt"));
				    		result.add(line);
				    		index++;
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
				}
				
			}else if(sec >= 50 ){
				
				String table = "ncdis.dbo.PatientAgePeriods";
				if(sec == 50){
					table = "ncdis.dbo.PatientAgePeriods";
				}else if(sec == 51){
					table = "ncdis.dbo.DiabetAgePeriods";
				}else if(sec == 52){
					table = "ncdis.dbo.A1CGroups";
				}else if(sec == 53){
					table = "ncdis.dbo.LDLGroups";
				}else if(sec == 54){
					table = "ncdis.dbo.GFRGroups";
				}else if(sec == 55){
					table = "ncdis.dbo.PCRACRGroups";
				}
				
				
				
				/* start section 50++   */
				String criteriaStr = "select count(*) as cnt  from "+table+" bb";
				String criteriaWhere = "where bb."+criteria.getName()+" "+Renderer.renderOperator(criteria.getOperator())+" "+criteria.getValue();
				String subcriteriaStr = "";
				if(subcriterias.size() > 0){
					for(int x=0;x<subcriterias.size();x++){
						ReportSubcriteria rsc = subcriterias.get(x);
						if(rsc.getSubsection().equals("1")){
							//subcriteriaStr += " inner join (select distinct idpatient from ncdis.ncdis.patient where "+rsc.getSubname()+" "+Renderer.renderOperator(rsc.getSuboperator())+" "+rsc.getSubvalue()+" and (dod is null or dod ='1900-01-01')) cc"+x+" on bb.idpatient = cc"+x+".idpatient";
							subcriteriaStr += " and bb."+rsc.getSubname()+" "+Renderer.renderOperator(rsc.getSuboperator())+" "+rsc.getSubvalue()+" ";
						}else if(rsc.getSubsection().equals("90")){
							//subcriteriaStr += " inner join (select distinct idpatient from ncdis.ncdis.cdis_value where iddata = '"+rsc.getSubiddata()+"' and value "+Renderer.renderOperator(rsc.getSuboperator())+" "+Renderer.renderValue(rsc.getSubsection()+"."+rsc.getSubvalue())+") cc"+x+" on bb.idpatient = cc"+x+".idpatient";
							subcriteriaStr += " and bb.dtype "+Renderer.renderOperator(rsc.getSuboperator())+" "+Renderer.renderValue(rsc.getSubsection()+"."+rsc.getSubvalue())+" ";
						}else{
							subcriteriaStr += " inner join (select distinct idpatient from ncdis.ncdis.cdis_value where iddata = '"+rsc.getSubiddata()+"' and value "+Renderer.renderOperator(rsc.getSuboperator())+" "+rsc.getSubvalue()+") cc"+x+" on bb.idpatient = cc"+x+".idpatient";
						}
					}
					
					//sql = criteriaStr +" "+subcriteriaStr + " "+criteriaWhere;
					sql = criteriaStr + " "+criteriaWhere +" "+subcriteriaStr ;
					//System.out.println(sql);
					try {
						initContext = new InitialContext();
						ds = (DataSource)initContext.lookup("jdbc/ncdis");
						conn = ds.getConnection();
					    cs=conn.createStatement();		    
					    cs.setEscapeProcessing(true);
					    long ts1 = (new Date()).getTime();
					    //System.out.println("SQL-"+sec+"-"+criteria.getName()+":"+sql);
					    rs = cs.executeQuery(sql);
					    int index = 0;
					    //long ts11 = (new Date()).getTime();
					   // System.out.println("Exec Time SQL before parsing-"+sec+"-"+criteria.getName()+": "+ (ts11-ts1));
					    while (rs.next()) {
					    	ArrayList<String> line = new ArrayList<>();
					    	line.add(Integer.toString(index));
				    		line.add(nom);
							line.add(rs.getString("cnt"));
				    		result.add(line);
				    		index++;
					    }
					    long ts2 = (new Date()).getTime();
					    //System.out.println("Exec Time SQL after parsing-"+sec+"-"+criteria.getName()+": "+ (ts2-ts1));
					    
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
					
				}else{
					/*section 50 no subcriteria*/
					sql = criteriaStr + " "+ criteriaWhere;
					try {
						initContext = new InitialContext();
						ds = (DataSource)initContext.lookup("jdbc/ncdis");
						conn = ds.getConnection();
					    cs=conn.createStatement();		    
					    cs.setEscapeProcessing(true);
					    rs = cs.executeQuery(sql);
					    int index = 0;
					    while (rs.next()) {
					    	ArrayList<String> line = new ArrayList<>();
					    	line.add(Integer.toString(index));
				    		line.add(nom);
							line.add(rs.getString("cnt"));
				    		result.add(line);
				    		index++;
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
				}
				
				/*end section 50 ++ */
			}else{
				
				/*section is not 1*/
				String criteriaStr = " and nn.value "+op+" '"+val+"' ";
				String criteriaStrSub = " and aa.value "+op+" '"+val+"' ";
				String subStrFrom = "";
				String subStrWhere = "";
				
				if(subcriterias.size() > 0){
					for(int x=0;x<subcriterias.size();x++){
						ReportSubcriteria rsc = subcriterias.get(x);
						
						//System.out.println("SUBin SQL :"+rsc.getSubvalue());
						
						if(rsc.getSubsection().equals("1")){
							sql = "select count(*) cnt"
									+ " from ncdis.ncdis.patient bb"
									+ "     inner join 	(select aa.idpatient, max(datevalue) maxdate from ncdis.ncdis.cdis_value aa where  aa.iddata='"+criteria.getIddata()+"' and aa.value "+Renderer.renderOperator(criteria.getOperator())+" '"+criteria.getValue()+"' group by aa.idpatient) cc"
									+ "     on bb.idpatient = cc.idpatient"
									+ " where (bb.dod is null or bb.dod = '1900-01-01') and "
									+ "  bb."+rsc.getSubname()+" "+Renderer.renderOperator(rsc.getSuboperator())+" '"+rsc.getSubvalue()+ "' ";
						
						}else if(rsc.getSubsection().equals("90")){
							sql = "select count(*) cnt"
									+ " from ncdis.ncdis.cdis_value bb"
									+ "   left join 	(select aa.idpatient, max(datevalue) maxdate from ncdis.ncdis.cdis_value aa where  aa.iddata='"+criteria.getIddata()+"' and aa.value "+Renderer.renderOperator(criteria.getOperator())+" '"+criteria.getValue()+"' group by aa.idpatient) cc"
									+ "    on bb.idpatient = cc.idpatient"
									+ " where  "
									+ " bb.iddata  = '"+rsc.getSubiddata()+"'"
									+ " and cast(case when coalesce(patindex('%[0-9]%', bb.value),0) = 0  then '0' else bb.value end as float) "+Renderer.renderOperator(rsc.getSuboperator())+" '"+rsc.getSubvalue()+"'"
									+ " and cc.maxdate is not null"
									+ " and bb.datevalue = (select max(xx.datevalue) from ncdis.ncdis.cdis_value xx where xx.iddata='"+rsc.getSubiddata()+"' and xx.idpatient = bb.idpatient group by xx.idpatient)";
						}else{
							sql = "select count(*) cnt"
									+ " from ncdis.ncdis.cdis_value bb"
									+ "   left join 	(select aa.idpatient, max(datevalue) maxdate from ncdis.ncdis.cdis_value aa where  aa.iddata='"+criteria.getIddata()+"' and aa.value "+Renderer.renderOperator(criteria.getOperator())+" '"+criteria.getValue()+"' group by aa.idpatient) cc"
									+ "    on bb.idpatient = cc.idpatient"
									+ " where  "
									+ " bb.iddata  = '"+rsc.getSubiddata()+"'"
									+ " and cast(case when coalesce(patindex('%[0-9]%', bb.value),0) = 0  then '0' else bb.value end as float) "+Renderer.renderOperator(rsc.getSuboperator())+" '"+rsc.getSubvalue()+"'"
									+ " and cc.maxdate is not null"
									+ " and bb.datevalue = (select max(xx.datevalue) from ncdis.ncdis.cdis_value xx where xx.iddata='"+rsc.getSubiddata()+"' and xx.idpatient = bb.idpatient group by xx.idpatient)";
						}
						
						try {
							initContext = new InitialContext();
							ds = (DataSource)initContext.lookup("jdbc/ncdis");
							conn = ds.getConnection();
						    cs=conn.createStatement();		    
						    cs.setEscapeProcessing(true);
						    
						    //System.out.println(sql);
						    rs = cs.executeQuery(sql);
						    int index = 0;
						    while (rs.next()) {
						    	ArrayList<String> line = new ArrayList<>();
						    	line.add(Integer.toString(index));
					    		line.add(nom);
								line.add(rs.getString("cnt"));
					    		result.add(line);
					    		index++;
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
					}
				}else{
					sql = "select count(nn.idpatient) as cnt from ncdis.ncdis.cdis_value nn  "+subStrFrom
							+ "where nn.datevalue = (select max(datevalue) from ncdis.ncdis.cdis_value aa where aa.idpatient = nn.idpatient "
							+ "and aa.iddata=(select iddata from ncdis.ncdis.cdis_data where data_code = '"+criteria.getName().toUpperCase()+"')) 	"
									+ "and nn.iddata = '"+criteria.getIddata()+"' "
									+ " "+ criteriaStr+ " "+subStrWhere
											+ " ";
			
					
					try {
						initContext = new InitialContext();
						ds = (DataSource)initContext.lookup("jdbc/ncdis");
						conn = ds.getConnection();
					    cs=conn.createStatement();		    
					    cs.setEscapeProcessing(true);
					    
					    //System.out.println(sql);
					    
					    rs = cs.executeQuery(sql);
					    int index = 0;
					    while (rs.next()) {
					    	ArrayList<String> line = new ArrayList<>();
					    	line.add(Integer.toString(index));
				    		line.add(nom);
							line.add(rs.getString("cnt"));
				    		result.add(line);
				    		index++;
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
				}
			}
		}
		return result;
	}

	public ArrayList<Hashtable<String, String>> executeReportFlist(String dataName, JsonArray criterias){
		ArrayList<Hashtable<String, String>> result = new ArrayList<>();
		Gson gson = new Gson();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		Statement cs=null;
		Connection conn = null;
		String iddata = getIddata(dataName);
		
		String sql = "SELECT p.idpatient, p.ramq, p.chart, p.giu, p.sex, DATEDIFF(year, p.dob, GETDATE()) AS age, p.idcommunity, lv.value as "+dataName+", lv.datevalue as "+dataName+"Date, ldv.value as dtype, ldv.datevalue as dtypeDate"
				+ " FROM ncdis.ncdis.patient as p"
				+ "	left join (select * from ncdis.[dbo].[LastdateValue] where iddata='"+iddata+"') lv on p.idpatient = lv.idpatient"
				+ "	left join ncdis.dbo.LastdateValue ldv on p.idpatient = ldv.idpatient"
				+ "  where p.active =1 and (p.dod is null or p.dod='1900-01-01') and ldv.iddata=1";

		
		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.createStatement();		    
		    cs.setEscapeProcessing(true);
		    rs = cs.executeQuery(sql);
		    ResultSetMetaData rsm =  rs.getMetaData();
		    int columns = rsm.getColumnCount();
		    ArrayList<ReportCriteria> rcList = new ArrayList<>();
		    for(JsonElement criteria : criterias ){
		        ReportCriteria cse = gson.fromJson( criteria , ReportCriteria.class);
		        rcList.add(cse);
		    }
		   
		    while (rs.next()) {
		    	Hashtable<String, String> row = new Hashtable<>();
		    	for(ReportCriteria rc : rcList){
		    		for(int i=1;i<=columns;i++){
		    			//System.out.println(" rc: "+rc.getName()+"  column : "+rsm.getColumnName(i));
		    			if(rc.getName().equals(rsm.getColumnName(i))){
		    				String colVal = rs.getString(rsm.getColumnName(i)); 
		    				if(colVal == null) colVal = "";
		    				row.put(rc.getName(), colVal);
		    				if(rc.getDate().equals("yes")){
		    					String colValDate = rs.getString(rsm.getColumnName(i)+"Date");
		    					if(colValDate == null) colValDate = "";
		    					row.put(rc.getName()+"_collecteddate", colValDate);
		    				}
		    				break;
		    			}
		    		}
		    	}
		    	result.add(row);
		    	/*
		    	for(Hashtable<String, String> r : result){
		    		System.out.println(" ramq: "+r.get("ramq")+"  chart : "+r.get("chart"));
		    	}
		    	*/
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
	

	
	public ArrayList<Object> executeReportLocalList(){
		ArrayList<Object> result = new ArrayList<>();
		ArrayList<Object> resultBuffer = new ArrayList<>();
		Gson gson = new Gson();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		Statement cs=null;
		Connection conn = null;
		
		
		/*
		String sql = "select ISNULL(u.fname,'')+' '+ ISNULL(u.lname,'') as fullname, "
				+ "u.ramq as ramq, "
				+ "u.sex as sex, "
				+ "u.chart as chart ,"
				+ "u.idcommunity, tt.value as dtype, "
				+ "datediff(year, u.dob, getdate()) as age, "
				+ "dd.idpatient , "
				+ "datediff(day, dd.date1, getdate()) as dayslastlab,"
				+ "dd.date1 as last_hba1c_collecteddate , "
				+ "round(dd.value1,3) as last_hba1c, "
				+ "dd.date2 as secondlast_hba1c_collecteddate, "
				+ "round(dd.value2,3) as secondlast_hba1c, "
				+ "dd.deltavalue as delta "
				+ " from "
					+ "("
						+ "select aa.idpatient, aa.datevalue as date1, aa.value as value1, bb.datevalue as date2, bb.value as value2,round(try_convert(float, aa.value)- try_convert(float, bb.value), 3) as deltavalue, datediff(day , bb.datevalue , aa.datevalue) as deltadate  "
						+ " from (select cc.idpatient, cc.datevalue, cc.value, cc.seqnum from ( select cd.* , row_number() over (partition by cd.idpatient order by datevalue desc) as seqnum from ncdis.ncdis.cdis_value cd where cd.iddata=27 ) cc  where cc.seqnum =1) aa "
							+ "left join "
							+ "(select cc.idpatient, cc.datevalue, cc.value, cc.seqnum from ( select cd.* , row_number() over (partition by cd.idpatient order by datevalue desc) as seqnum from ncdis.ncdis.cdis_value cd where cd.iddata=27 ) cc where cc.seqnum =2) bb "
							+ "on aa.idpatient = bb.idpatient"
					+ ") dd "
					+ "left join "
					+ "ncdis.ncdis.patient u "
						+ "on dd.idpatient=u.idpatient "
					+ "left join "
					+ "(select cc.idpatient, cc.datevalue, cc.value, cc.seqnum from ( select cd.* , row_number() over (partition by cd.idpatient order by datevalue desc) as seqnum from ncdis.ncdis.cdis_value cd where cd.iddata=1 ) cc where cc.seqnum =1) tt "
					+ "on dd.idpatient = tt.idpatient "
				+ "where u.active=1 and (u.dod is null or u.dod='1900-01-01') order by delta desc";

		*/
		
		String sql = "select tt1.idpatient"
				+ ",ISNULL(p.fname,'')+' '+ ISNULL(p.lname,'') as fullname "
				+ ",p.ramq as ramq "
				+ ",p.sex as sex "
				+ ",p.chart as chart"
				+ ",p.idcommunity "
				+ ",datediff(year, p.dob, getdate()) as age "
				+ ",tt3.value as dtype "
				+ ",tt3.datevalue as ddate "
				+ ",tt4.users as users "
				+ ",datediff(day, tt1.datevalue, getdate()) as dayslastlab"
				+ ",tt1.datevalue as last_hba1c_collecteddate "
				+ ",round(tt1.value,3) as last_hba1c "
				+ ",tt2.datevalue as secondlast_hba1c_collecteddate "
				+ ",round(tt2.value,3) as secondlast_hba1c "
				+ ",round(try_convert(float, tt1.value) - try_convert(float, tt2.value), 3) as delta"
					+ " from "  
					+ "(select aa.datevalue, aa.value, aa.idpatient, aa.seqnum from (select cd.* , row_number() over (partition by cd.idpatient order by datevalue desc) as seqnum from ncdis.ncdis.cdis_value cd where cd.iddata=27 and cd.datevalue <= getdate()) aa where aa.seqnum = 1) as tt1 "
					+ "left join "
						+ "(select aa.datevalue, aa.value, aa.idpatient, aa.seqnum from (select cd.* , row_number() over (partition by cd.idpatient order by datevalue desc) as seqnum from ncdis.ncdis.cdis_value cd where cd.iddata=27 and cd.datevalue <= getdate()) aa where aa.seqnum = 2) as tt2 "
							+ "on tt1.idpatient = tt2.idpatient "
						+ "left join "
						+ "ncdis.ncdis.patient p "
							+ "on  tt1.idpatient = p.idpatient "
						+ "left join "
						+ "(select aa.datevalue, aa.value, aa.idpatient, aa.seqnum from (select cd.* , row_number() over (partition by cd.idpatient order by datevalue desc) as seqnum from ncdis.ncdis.cdis_value cd where cd.iddata=1 and cd.datevalue <= getdate()) aa where aa.seqnum = 1) as tt3 "
							+ "on tt1.idpatient = tt3.idpatient "
						+ "left join "
						+ "(SELECT idpatient, case when isnumeric(chr) =1  or isnumeric(nut)=1 or isnumeric(nur)=1 or isnumeric(md)=1 then concat(chr,';',nur,';',nut,';',md) else '0' end as users FROM [ncdis].[ncdis].[patient_hcp] where isnumeric(chr) =1  or isnumeric(nut)=1 or isnumeric(nur)=1 or isnumeric(md)=1) as tt4 "
							+ "on tt1.idpatient = tt4.idpatient "
					+ " where "
						+ " tt2.value is not null "
						+ " and p.active=1 and (p.dod is null or p.dod='1900-01-01') "
						+ " and p.idcommunity != 10 "
				+ " order by delta desc ";
		
		
		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.createStatement();		    
		    cs.setEscapeProcessing(true);
		    rs = cs.executeQuery(sql);
		    ResultSetMetaData rsm =  rs.getMetaData();
		    int columns = rsm.getColumnCount();
		   int deltaZTotal = 0;
		   int deltaPTotal = 0;
		   int deltaNTotal = 0;
		   int index = 0;
		    while (rs.next()) {
		    	Hashtable<String, String> row = new Hashtable<>();
		    	for(int i=1;i<=columns;i++){
		    		String colVal = rs.getString(rsm.getColumnName(i)); 
		    		if(colVal == null) colVal = "";
   					row.put(rsm.getColumnName(i), colVal);
		    	}
		    	String v = row.get("delta");
		    	if(!v.isEmpty()){
		    		float d = Float.parseFloat(row.get("delta"));
			    	if(d > 0 ){
			    		deltaPTotal++;
			    		row.put("indexDelta",Integer.toString(deltaPTotal));
			    	}
			    	if(d == 0 ){
			    		deltaZTotal++;
			    		row.put("indexDelta",Integer.toString(deltaZTotal));
			    	}
			    	if(d < 0 ){
			    		deltaNTotal++;
			    		row.put("indexDelta",Integer.toString(deltaNTotal));
			    	}
		    	}
		    	if(row.get("dtype").equals("10")){row.put("dtype", "3");}
		    	if(row.get("dtype").equals("11")){row.put("dtype", "4");}
		    	
		    	resultBuffer.add(row);
		    }
		    for(int i=0;i<resultBuffer.size();i++){
		    	Hashtable<String, String> r = (Hashtable<String, String>)resultBuffer.get(i);
		    	String ss = r.get("delta");
		    	if(!ss.isEmpty()){
			    	float d =  Float.parseFloat(r.get("delta"));
			    	if(d > 0 ){
			    		r.put("totalDelta",Integer.toString(deltaPTotal));
			    	}
			    	if(d == 0 ){
			    		r.put("totalDelta",Integer.toString(deltaZTotal));
			    	}
			    	if(d < 0 ){
			    		r.put("totalDelta",Integer.toString(deltaNTotal));
			    	}
		    	}
		    	result.add(r);
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

	
	public ArrayList<Object> executeReportNoHBA1c(){
		ArrayList<Object> result = new ArrayList<>();
		Gson gson = new Gson();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		Statement cs=null;
		Connection conn = null;
		
		String sql = "select  u.idpatient,u.active,u.sex, ISNULL(u.fname,'')+' '+ ISNULL(u.lname,'') as fullname, u.ramq,u.chart,u.idcommunity,"
				+ " datediff(year, u.dob, getdate()) as age, "
				+ " tt.value as dtype,tt.datevalue as dtype_collecteddate "
				+ " from "
				+ "(select aa.idpatient, aa.datevalue as date1, aa.value as value1 from "
						+ "(select cc.idpatient, cc.datevalue, cc.value, cc.seqnum from "
								+ "( select cd.* , row_number() over (partition by cd.idpatient order by datevalue desc) as seqnum "
								+ "from ncdis.ncdis.cdis_value cd where cd.iddata=27 ) cc "
						+ "where cc.seqnum =1) aa "
				+ ") dd "
				+ " right join ncdis.ncdis.patient u on dd.idpatient=u.idpatient "
				+ " left join (select cc.idpatient, cc.datevalue, cc.value, cc.seqnum from ( select cd.* , row_number() over (partition by cd.idpatient order by datevalue desc) as seqnum from ncdis.ncdis.cdis_value cd where cd.iddata=1 ) cc where cc.seqnum =1) tt  on u.idpatient = tt.idpatient "
				+ "where u.active=1 and (u.dod is null or u.dod='1900-01-01') and value1 is null";
		
		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.createStatement();		    
		    cs.setEscapeProcessing(true);
		    rs = cs.executeQuery(sql);
		    ResultSetMetaData rsm =  rs.getMetaData();
		    int columns = rsm.getColumnCount();
		   
		    while (rs.next()) {
		    	Hashtable<String, String> row = new Hashtable<>();
		    	for(int i=1;i<=columns;i++){
		    		String colVal = rs.getString(rsm.getColumnName(i)); 
		    		if(colVal == null) colVal = "";
   					row.put(rsm.getColumnName(i), colVal);
		    	}
		    	result.add(row);
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

	
	
	public ArrayList<String> getIdPatients(){
		ArrayList<String> result = new ArrayList<>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		Statement cs=null;
		Connection conn = null;
		String sql = "select distinct nn.idpatient from ncdis.ncdis.patient nn where (dod is null or dod = '1900-01-01')";
		
		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.createStatement();		    
		    cs.setEscapeProcessing(true);
		    rs = cs.executeQuery(sql);
		    int index=0;
		    while (rs.next()) {
		    	result.add(rs.getString(1));
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

	public ArrayList<String> getIdFilterPatients(String hcp, String hcpid){
		ArrayList<String> result = new ArrayList<>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		Statement cs=null;
		Connection conn = null;
		String sql = "select distinct nn.idpatient from ncdis.ncdis.patient nn left join ncdis.ncdis.patient_hcp ph on nn.idpatient = ph.idpatient where (nn.dod is null or nn.dod = '1900-01-01') and ph."+hcp+"='"+hcpid+"'";
		//System.out.println(sql);
		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.createStatement();		    
		    cs.setEscapeProcessing(true);
		    rs = cs.executeQuery(sql);
		    int index=0;
		    while (rs.next()) {
		    	result.add(rs.getString(1));
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
	
	
	public ArrayList<Note> getPatientNotes(int idpatient){
		ArrayList<Note> result = new ArrayList<>();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		Statement cs=null;
		Connection conn = null;
		String sql = "select * from ncdis.ncdis.notes nn where idpatient = '"+idpatient+"' and active = '1' order by datenote desc";
		
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
	
	public boolean setPatientNotes(Note note){
		boolean result = false;
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;
		String sql = "insert into ncdis.ncdis.notes (note,datenote,iduser,idpatient,active,iduserto, viewed) values (?,getdate(),'"+note.getIduser()+"','"+note.getIdpatient()+"','1', '"+note.getIduserto()+"','"+note.getViewed()+"')";
		try {
			initContext = new InitialContext();
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.prepareStatement(sql);
		    cs.setString(1, note.getNote());
		    cs.setEscapeProcessing(true);
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
	
	
	public static String exportRamq(){
		String result = "";
		DataSource ds;
		ResultSet rs = null;
		Statement cs=null;
		Connection conn = null;
		Date today = new Date();
	      
      
        //formatting date in Java using SimpleDateFormat
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
        String dateStr = DATE_FORMAT.format(today);
        String sql = "select distinct ramq from ncdis.ncdis.patient";
		
		InitialContext ic;
		try {
			ic = new InitialContext();
			String rf = (String) ic.lookup("root-folder");
			File exportFile = new File(rf+System.getProperty("file.separator")+"files"+System.getProperty("file.separator")+"export_"+dateStr+".csv");
			FileWriter fw = new FileWriter(exportFile);
			
			ds = (DataSource)ic.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.createStatement();		    
		    cs.setEscapeProcessing(true);
		    rs = cs.executeQuery(sql);
		    while (rs.next()) {
		    	fw.write(rs.getString(1).toUpperCase()+"\r\n");
		    }
		    fw.close();
		    result = exportFile.getAbsolutePath();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException se) {
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

	public  Hashtable<String, ArrayList<Object>> getHbA1cTrendItem(int period, String idcommunity, String sex, String dtype, String age, String hba1c) {

		Hashtable<String, ArrayList<Object>> result = new Hashtable();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		Statement cs=null;
		Connection conn = null;
		
		String cStr = " and a.idcommunity > 0 ";
		if(!idcommunity.equals("0"))cStr = " and a.idcommunity="+idcommunity+" "; 
		String gStr = " and a.sex > 0 ";
		if(!sex.equals("0"))gStr = " and a.sex="+sex+" ";
		String dtStr =" and (a.dtype=1 or a.dtype=2) ";
		if(!dtype.equals("1_2")){
			dtype = dtype.replaceAll("_", "");
			String[] parts = dtype.split("(?!^)");
			dtStr = " and (";
			for(int x=0;x<parts.length;x++){
				String s = " a.dtype="+parts[x];
				if(parts[x].equals("3")){s = " a.dtype=3 or a.dtype=10 ";}
				if(parts[x].equals("4")){s = " a.dtype=4 or a.dtype=11 ";}
				
				if(x == 0){
					dtStr +=  s;
				}else{
					dtStr +=  " or "+s;
				}
			}
			dtStr += " ) ";
		}
		String aStr = " and a.age > 0 ";
		if(!age.equals("0")){
			String[] parts = age.split("_");
			aStr = " and a.age >="+parts[0]+" and a.age <="+parts[1]+" ";
		}
		
		String vStr = " and try_convert(float,a.value1) > 0.0 ";
		if(hba1c.equals("0")){
			vStr = vStr;
		}else  if(hba1c.equals("1")){
			vStr = " and try_convert(float,a.value1) >= 0.075 ";
		}else{
			String[] parts = hba1c.split("_");
			vStr = " and try_convert(float,a.value1) >="+parts[0]+" and try_convert(float,a.value1) <="+parts[1]+" ";
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		Date now = new Date();
		
		//we have to shift 3 years back because of data
		
		Calendar calStart = Calendar.getInstance();
		Calendar calEnd = Calendar.getInstance();
		calStart.setTime(now);
		//calStart.add(Calendar.YEAR, -3);
		calEnd.setTime(now);
		//calEnd.add(Calendar.YEAR, -3);
		
		calStart.add(Calendar.MONTH, (period)*-1 ); // we go back period number and 1 month more because we exclude current month
		calStart.set(Calendar.DAY_OF_MONTH, 1);
		calEnd.add(Calendar.MONTH, -1);
		calEnd.set(Calendar.DAY_OF_MONTH, calEnd.getActualMaximum(Calendar.DAY_OF_MONTH));
				try {
					initContext = new InitialContext();
					ds = (DataSource)initContext.lookup("jdbc/ncdis");
					conn = ds.getConnection();
				    cs=conn.createStatement();		    
				    cs.setEscapeProcessing(true);
				    
				    ArrayList<Integer> improved = new ArrayList<>();
					ArrayList<Integer> setback = new ArrayList<>();
					ArrayList<Integer> constant = new ArrayList<>();
					ArrayList<Object> series = new ArrayList<>();
					ArrayList<Object> ticks = new ArrayList<>();
					
					ArrayList<Object> labels = new ArrayList<>();
					Hashtable<String, String> label1 = new Hashtable<>();
					label1.put("label", "Increased");
					labels.add(label1);
					Hashtable<String, String> label2 = new Hashtable<>();
					label2.put("label", "No Change");
					labels.add(label2);
					Hashtable<String, String> label3 = new Hashtable<>();
					label3.put("label", "Improved");
					labels.add(label3);
					result.put("labels", labels);
					
					for(int i=0;i<period;i++){
						Calendar c = Calendar.getInstance();
						c.set(Calendar.YEAR, calStart.get(Calendar.YEAR));
						c.set(Calendar.MONTH, calStart.get(Calendar.MONTH));
						c.set(Calendar.DAY_OF_MONTH, calStart.get(Calendar.DAY_OF_MONTH));
						c.add(Calendar.MONTH, i);
						String d1 = sdf.format(c.getTime());
						c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
						String d2 = sdf.format(c.getTime());
						
						String sql = "SELECT "
								+ " sum(case when a.deltavalue > 0 "+cStr+" "+dtStr+" "+gStr+" "+aStr+" "+vStr+" then 1 else 0 end) as pTotal "
								+ ",sum(case when a.deltavalue < 0 "+cStr+" "+dtStr+" "+gStr+" "+aStr+" "+vStr+" then 1 else 0 end) as nTotal "
								+ ",sum(case when a.deltavalue = 0 "+cStr+" "+dtStr+" "+gStr+" "+aStr+" "+vStr+" then 1 else 0 end) as cTotal "
								+ " from "
									+ "(select tt1.idpatient, tt1.value as value1, tt2.value as value2 , tt1.datevalue as date1, tt2.datevalue as date2 "
									+ " ,datediff(year, p.dob, getdate()) as age  "
									+ " ,round(try_convert(float, tt1.value) - try_convert(float, tt2.value), 3) as deltavalue , p.idcommunity ,p.sex,tt3.value as dtype ,tt3.datevalue as ddate "
										+ " from "  
										+ "(select aa.datevalue, aa.value, aa.idpatient, aa.seqnum from (select cd.* , row_number() over (partition by cd.idpatient order by datevalue desc) as seqnum from ncdis.ncdis.cdis_value cd where cd.iddata=27 and cd.datevalue between '"+d1+"' and '"+d2+"') aa where aa.seqnum = 1) as tt1 "
//										+ "(select aa.datevalue, aa.value, aa.idpatient, aa.seqnum from (select cd.* , row_number() over (partition by cd.idpatient order by datevalue desc) as seqnum from ncdis.ncdis.cdis_value cd where cd.iddata=27 and cd.datevalue between '"+d1+"' and '"+d2+"') aa where aa.seqnum = 1) as tt1 "
										+ "left join "
											+ "(select aa.datevalue, aa.value, aa.idpatient, aa.seqnum from (select cd.* , row_number() over (partition by cd.idpatient order by datevalue desc) as seqnum from ncdis.ncdis.cdis_value cd where cd.iddata=27 and cd.datevalue <= '"+d2+"' ) aa where aa.seqnum = 2) as tt2 "
												+ "on tt1.idpatient = tt2.idpatient "
											+ "left join "
											+ "ncdis.ncdis.patient p "
												+ "on  tt1.idpatient = p.idpatient and p.active=1 and (p.dod is null or p.dod='1900-01-01') "
											+ "left join "
											+ "(select aa.datevalue, aa.value, aa.idpatient, aa.seqnum from (select cd.* , row_number() over (partition by cd.idpatient order by datevalue desc) as seqnum from ncdis.ncdis.cdis_value cd where cd.iddata=1 and cd.datevalue <= '"+d2+"') aa where aa.seqnum = 1) as tt3 "
												+ "on tt1.idpatient = tt3.idpatient "
										+ " where "
											+ " tt2.value is not null "
											+ " and p.idcommunity != 10 "
									+ " ) as a ";

									
							//System.out.println(sql);
							rs = cs.executeQuery(sql);
						    while (rs.next()) {
						    	
						    	improved.add(rs.getInt("nTotal"));
						    	setback.add(rs.getInt("pTotal"));
						    	constant.add(rs.getInt("cTotal"));
						    	
						    	ArrayList<Object> tick = new ArrayList<Object>();
						    	tick.add(i+1);
						    	tick.add(sdf.format(c.getTime()));
						    	ticks.add(tick);
						    }
						    
						    rs.clearWarnings();
						    cs.clearBatch();
						    
						    
					}
					
					
				    series.add(setback);
				    series.add(constant);
				    series.add(improved);
				    
					result.put("series", series);
					result.put("ticks", ticks);
					
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
	
	
	

	public  Hashtable<String, ArrayList<Object>> getHbA1cPeriodItem(int period, String idcommunity, String sex, String dtype, String age, String hba1c) {

		Hashtable<String, ArrayList<Object>> result = new Hashtable();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		Statement cs=null;
		Connection conn = null;
		
		String cStr = " a.idcommunity > 0 ";
		if(!idcommunity.equals("0"))cStr = " a.idcommunity="+idcommunity+" "; 
		String gStr = " and a.sex > 0 ";
		if(!sex.equals("0"))gStr = " and a.sex="+sex+" ";
		String dtStr =" and (a.dtype=1 or a.dtype=2) ";
		if(!dtype.equals("1_2")){
			dtype = dtype.replaceAll("_", "");
			String[] parts = dtype.split("(?!^)");
			dtStr = " and (";
			for(int x=0;x<parts.length;x++){
				String s = " a.dtype="+parts[x];
				if(parts[x].equals("3")){s = " a.dtype=3 or a.dtype=10 ";}
				if(parts[x].equals("4")){s = " a.dtype=4 or a.dtype=11 ";}
				
				if(x == 0){
					dtStr +=  s;
				}else{
					dtStr +=  " or "+s;
				}
			}
			dtStr += " ) ";
		}
		String aStr = " and a.age > 0 ";
		if(!age.equals("0")){
			String[] parts = age.split("_");
			aStr = " and a.age >="+parts[0]+" and a.age <="+parts[1]+" ";
		}
		
		String vStr = " and try_convert(float,a.value1) > 0.0 ";
		if(hba1c.equals("0")){
			vStr = vStr;
		}else  if(hba1c.equals("1")){
			vStr = " and try_convert(float,a.value1) >= 0.075 ";
		}else{
			String[] parts = hba1c.split("_");
			vStr = " and try_convert(float,a.value1) >="+parts[0]+" and try_convert(float,a.value1) <="+parts[1]+" ";
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		Date now = new Date();
		
		//we have to shift 3 years back because of data
		
		Calendar calStart = Calendar.getInstance();
		Calendar calEnd = Calendar.getInstance();
		calStart.setTime(now);
		//calStart.add(Calendar.YEAR, -3);
		calEnd.setTime(now);
		//calEnd.add(Calendar.YEAR, -3);
		
		calStart.add(Calendar.MONTH, (period)*-1 ); // we go back period number and 1 month more because we exclude current month
		calStart.set(Calendar.DAY_OF_MONTH, 1);
		calEnd.add(Calendar.MONTH, -1);
		calEnd.set(Calendar.DAY_OF_MONTH, calEnd.getActualMaximum(Calendar.DAY_OF_MONTH));
		
				try {
					initContext = new InitialContext();
					ds = (DataSource)initContext.lookup("jdbc/ncdis");
					conn = ds.getConnection();
				    cs=conn.createStatement();		    
				    cs.setEscapeProcessing(true);
				    
				    ArrayList<Integer> number = new ArrayList<>();
					ArrayList<Integer> total = new ArrayList<>();

					ArrayList<Object> series = new ArrayList<>();
					ArrayList<Object> ticks = new ArrayList<>();
					
					ArrayList<Object> labels = new ArrayList<>();
					Hashtable<String, String> label1 = new Hashtable<>();
					label1.put("label", "Number");
					labels.add(label1);
					Hashtable<String, String> label2 = new Hashtable<>();
					label2.put("label", "Percentage");
					labels.add(label2);
					result.put("labels", labels);
					
					for(int i=0;i<period;i++){
						Calendar c = Calendar.getInstance();
						c.set(Calendar.YEAR, calStart.get(Calendar.YEAR));
						c.set(Calendar.MONTH, calStart.get(Calendar.MONTH));
						c.set(Calendar.DAY_OF_MONTH, calStart.get(Calendar.DAY_OF_MONTH));
						c.add(Calendar.MONTH, i);
						c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
						String d1 = sdf.format(c.getTime());
						//last 12 month
						String dtick = sdf.format(c.getTime());
						c.add(Calendar.MONTH, -12);
						String d2 = sdf.format(c.getTime());
						String dateStr = "and a.date1 >= '"+d2+"'"; 
						String sql = "SELECT "
								+ " sum(case when "+cStr+" "+dtStr+" "+gStr+" "+aStr+" "+vStr+" "+dateStr+" then 1 else 0 end) as pNumber "
								+ ",sum(case when "+cStr+" then 1 else 0 end) as pTotal "
								+ " from "
									+ "(select tt1.idpatient, tt1.value as value1, tt1.datevalue as date1 "
									+ " ,datediff(year, p.dob, getdate()) as age  "
									+ " ,p.idcommunity ,p.sex,tt3.value as dtype ,tt3.datevalue as ddate "
										+ " from "  
										+ "(select aa.datevalue, aa.value, aa.idpatient, aa.seqnum from (select cd.* , row_number() over (partition by cd.idpatient order by datevalue desc) as seqnum from ncdis.ncdis.cdis_value cd where cd.iddata=27 and cd.datevalue <= '"+d1+"') aa where aa.seqnum = 1) as tt1 "
										+ "left join "
											+ "ncdis.ncdis.patient p "
										+ "on  tt1.idpatient = p.idpatient and p.active=1 and (p.dod is null or p.dod='1900-01-01') "
										+ "left join "
											+ "(select aa.datevalue, aa.value, aa.idpatient, aa.seqnum from (select cd.* , row_number() over (partition by cd.idpatient order by datevalue desc) as seqnum from ncdis.ncdis.cdis_value cd where cd.iddata=1 and cd.datevalue <= '"+d1+"') aa where aa.seqnum = 1) as tt3 "
												+ "on tt1.idpatient = tt3.idpatient "
										+ " where "
											+ " tt1.value is not null "
											+ " and p.idcommunity != 10 "
									+ " ) as a ";

							//System.out.println(sql);
							rs = cs.executeQuery(sql);
						    while (rs.next()) {
						    	
						    	number.add(rs.getInt("pNumber"));
						    	total.add(rs.getInt("pTotal"));
						    	
						    	ArrayList<Object> tick = new ArrayList<Object>();
						    	tick.add(i+1);
						    	tick.add(dtick);
						    	ticks.add(tick);
						    }
						    
						    rs.clearWarnings();
						    cs.clearBatch();
						    
					}
					series.add(number);
				    series.add(total);
					result.put("series", series);
					result.put("ticks", ticks);
					
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
	
	


	public  Hashtable<String, ArrayList<Object>> getHbA1cValueItem(int period, String idcommunity, String sex, String dtype, String age, String hba1c) {

		Hashtable<String, ArrayList<Object>> result = new Hashtable();
		Context initContext;
		DataSource ds;
		ResultSet rs = null;
		Statement cs=null;
		Connection conn = null;
		
		String cStr = " a.idcommunity > 0 ";
		if(!idcommunity.equals("0"))cStr = " a.idcommunity="+idcommunity+" "; 
		String gStr = " and a.sex > 0 ";
		if(!sex.equals("0"))gStr = " and a.sex="+sex+" ";
		String dtStr =" and (a.dtype=1 or a.dtype=2) ";
		if(!dtype.equals("1_2")){
			dtype = dtype.replaceAll("_", "");
			String[] parts = dtype.split("(?!^)");
			dtStr = " and (";
			for(int x=0;x<parts.length;x++){
				String s = " a.dtype="+parts[x];
				if(parts[x].equals("3")){s = " a.dtype=3 or a.dtype=10 ";}
				if(parts[x].equals("4")){s = " a.dtype=4 or a.dtype=11 ";}
				
				if(x == 0){
					dtStr +=  s;
				}else{
					dtStr +=  " or "+s;
				}
			}
			dtStr += " ) ";
		}
		String aStr = " and a.age > 0 ";
		if(!age.equals("0")){
			String[] parts = age.split("_");
			aStr = " and a.age >="+parts[0]+" and a.age <="+parts[1]+" ";
		}
		
		String vStr = " and try_convert(float,a.value1) > 0.0 ";
		if(hba1c.equals("0")){
			vStr = vStr;
		}else  if(hba1c.equals("1")){
			vStr = " and try_convert(float,a.value1) >= 0.075 ";
		}else{
			String[] parts = hba1c.split("_");
			vStr = " and try_convert(float,a.value1) >="+parts[0]+" and try_convert(float,a.value1) <="+parts[1]+" ";
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		Date now = new Date();
		
		//we have to shift 3 years back because of data
		
		Calendar calStart = Calendar.getInstance();
		Calendar calEnd = Calendar.getInstance();
		calStart.setTime(now);
		//calStart.add(Calendar.YEAR, -3);
		calEnd.setTime(now);
		//calEnd.add(Calendar.YEAR, -3);
		
		calStart.add(Calendar.MONTH, (period)*-1 ); // we go back period number and 1 month more because we exclude current month
		calStart.set(Calendar.DAY_OF_MONTH, 1);
		calEnd.add(Calendar.MONTH, -1);
		calEnd.set(Calendar.DAY_OF_MONTH, calEnd.getActualMaximum(Calendar.DAY_OF_MONTH));
				try {
					initContext = new InitialContext();
					ds = (DataSource)initContext.lookup("jdbc/ncdis");
					conn = ds.getConnection();
				    cs=conn.createStatement();		    
				    cs.setEscapeProcessing(true);
				    
				    ArrayList<Integer> number = new ArrayList<>();
					ArrayList<Integer> total = new ArrayList<>();

					ArrayList<Object> series = new ArrayList<>();
					ArrayList<Object> ticks = new ArrayList<>();
					
					ArrayList<Object> labels = new ArrayList<>();
					Hashtable<String, String> label1 = new Hashtable<>();
					label1.put("label", "Number");
					labels.add(label1);
					Hashtable<String, String> label2 = new Hashtable<>();
					label2.put("label", "Percentage");
					labels.add(label2);
					result.put("labels", labels);
					
					for(int i=0;i<period;i++){
						Calendar c = Calendar.getInstance();
						c.set(Calendar.YEAR, calStart.get(Calendar.YEAR));
						c.set(Calendar.MONTH, calStart.get(Calendar.MONTH));
						c.set(Calendar.DAY_OF_MONTH, calStart.get(Calendar.DAY_OF_MONTH));
						c.add(Calendar.MONTH, i);
						c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
						String d1 = sdf.format(c.getTime());
						//last 12 month
						
						
						 
						String sql = "SELECT "
								+ " sum(case when "+cStr+" "+dtStr+" "+gStr+" "+aStr+" "+vStr+" then 1 else 0 end) as pNumber "
								+ ",sum(case when "+cStr+" then 1 else 0 end) as pTotal "
								+ " from "
									+ "(select tt1.idpatient, tt1.value as value1, tt1.datevalue as date1 "
									+ " ,datediff(year, p.dob, getdate()) as age  "
									+ " ,p.idcommunity ,p.sex,tt3.value as dtype ,tt3.datevalue as ddate "
										+ " from "  
										+ "(select aa.datevalue, aa.value, aa.idpatient, aa.seqnum from (select cd.* , row_number() over (partition by cd.idpatient order by datevalue desc) as seqnum from ncdis.ncdis.cdis_value cd where cd.iddata=27 and cd.datevalue <= '"+d1+"') aa where aa.seqnum = 1) as tt1 "
										+ "left join "
											+ "ncdis.ncdis.patient p "
										+ "on  tt1.idpatient = p.idpatient and p.active=1 and (p.dod is null or p.dod='1900-01-01') "
										+ "left join "
											+ "(select aa.datevalue, aa.value, aa.idpatient, aa.seqnum from (select cd.* , row_number() over (partition by cd.idpatient order by datevalue desc) as seqnum from ncdis.ncdis.cdis_value cd where cd.iddata=1 and cd.datevalue <= '"+d1+"') aa where aa.seqnum = 1) as tt3 "
												+ "on tt1.idpatient = tt3.idpatient "
										+ " where "
											+ " tt1.value is not null "
											+ " and p.idcommunity != 10 "
									+ " ) as a ";

									
							//System.out.println(sql);
							rs = cs.executeQuery(sql);
						    while (rs.next()) {
						    	
						    	number.add(rs.getInt("pNumber"));
						    	total.add(rs.getInt("pTotal"));
						    	
						    	ArrayList<Object> tick = new ArrayList<Object>();
						    	tick.add(i+1);
						    	tick.add(sdf.format(c.getTime()));
						    	ticks.add(tick);
						    }
						    
						    rs.clearWarnings();
						    cs.clearBatch();
						    
					}
					series.add(number);
				    series.add(total);
					result.put("series", series);
					result.put("ticks", ticks);
					
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
