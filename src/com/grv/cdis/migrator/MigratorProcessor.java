package com.grv.cdis.migrator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.grv.cdis.model.SearchPatient;
import com.grv.cdis.util.ImportNames;

public class MigratorProcessor {

	
	public String MigrateCdis(Hashtable<String, String[]> args){
		String result = "insuccess";
		Context initContext;
		DataSource ds;
		//ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;
		
		String spInit = "exec sp_Migrate_All";
		//String spPatientHcp = "exec sp_Migrate_PatientHcp";
		try {
			initContext = new InitialContext();
			//Context envContext  = (Context)initContext.lookup("java:comp/env");
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.prepareStatement(spInit);		    
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    cs.execute();
		    cs.clearBatch();
		    result = "success";
		}catch (SQLException se) {
		        se.printStackTrace();
		        System.exit(0);
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
		
		MigratePatients(new Hashtable<String,String[]>());
		MigratePatientHcp(new Hashtable<String,String[]>());
		MigrateDiabetes(new Hashtable<String,String[]>());
		MigrateLab(new Hashtable<String,String[]>());
		MigrateRenal(new Hashtable<String,String[]>());
		MigrateMDVisits(new Hashtable<String,String[]>());
		MigrateLipid(new Hashtable<String,String[]>());
		MigrateComplications(new Hashtable<String,String[]>());
		MigrateMiscellaneous(new Hashtable<String,String[]>());
		MigrateMeds(new Hashtable<String,String[]>());
		
		
		ImportNames.importNames();
		/**/
		ImportNames.importDiabet();
		
		return result;
	}
	
	
	public String MigratePatientHcp(Hashtable<String, String[]> args){
		String result = "";
		Context initContext;
		DataSource ds;
		//ResultSet rs = null;
		PreparedStatement cs=null;
		Connection conn = null;
		String spPatientHcp = "exec sp_Migrate_PatientHcp";
		try {
			initContext = new InitialContext();
			//Context envContext  = (Context)initContext.lookup("java:comp/env");
			ds = (DataSource)initContext.lookup("jdbc/ncdis");
			conn = ds.getConnection();
		    cs=conn.prepareStatement(spPatientHcp);		    
		    cs.setEscapeProcessing(true);
		    cs.setQueryTimeout(90);
		    cs.execute();
		    cs.clearBatch();
		}catch (SQLException se) {
		        se.printStackTrace();
		        System.exit(0);
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
	
	
	public String MigratePatients(Hashtable<String, String[]> args){
		String result = "";
		Context initContext;
		DataSource dsCDIS;
		DataSource dsNCDIS;
		ResultSet rsCDIS = null;
		ResultSet rsNCDIS = null;
		Statement psCDIS = null;
		PreparedStatement psNCDIS = null;
		Statement sNCDIS = null;
		Connection connCDIS = null;
		Connection connNCDIS = null;
		String sqlCDIS = "select * from dbo.cdis_patient";
		try {
			initContext = new InitialContext();
			//Context envContext  = (Context)initContext.lookup("java:comp/env");
			dsNCDIS = (DataSource)initContext.lookup("jdbc/ncdis");
			dsCDIS = (DataSource)initContext.lookup("jdbc/cdis");
			connCDIS = dsCDIS.getConnection();
			connNCDIS = dsNCDIS.getConnection();
		    //psCDIS = connCDIS.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		    psCDIS = connCDIS.createStatement();
		    
		    String  siion = "SET IDENTITY_INSERT ncdis.patient ON";
		    String  siioff = "SET IDENTITY_INSERT ncdis.patient OFF";
		    
		    Statement sis = connNCDIS.createStatement();
	    	sis.execute(siion);
	    	sis.clearBatch();
		    //psCDIS.setEscapeProcessing(true);
		    rsCDIS = psCDIS.executeQuery(sqlCDIS);
		    while (rsCDIS.next()) {
		    	String sqlNCDIS = "insert into ncdis.patient (ramq,sex,dob,dod,idcommunity,iscree,chart,band,consent,death_cause,lname,fname,idpatient,entrydate) values (?,?,?,?,?,?,?,?,?,?,'LASTNAME','FIRSTNAME',?,?)";
		    	psNCDIS = connNCDIS.prepareStatement(sqlNCDIS);
		    	psNCDIS.setString(1, rsCDIS.getString("ramq"));
		    	psNCDIS.setInt(2, rsCDIS.getInt("sex"));
		    	psNCDIS.setInt(9, rsCDIS.getInt("consent"));
		    	psNCDIS.setInt(6, rsCDIS.getInt("native"));
		    	psNCDIS.setDate(3, rsCDIS.getDate("dob"));
		    	
		    	//sNCDIS = connNCDIS.createStatement();
		    	java.sql.Date dod = rsCDIS.getDate("dod");
		    	if(dod != null ){
			    	if(dod.getYear() == 0){
			    		dod = null;
			    	}
		    	}
		    	psNCDIS.setDate(4, dod);
		    	//Date dob = (Date)rsCDIS.getDate("dob");
		    	//String dodStr = "";
		    	//String dobStr = null;
		    	
		    	//}
		    	//if(dob != null ){
		    	//	dobStr = dob.toString();
		    	//}
		    	
		    	String dcause =  rsCDIS.getString("dcause").replace("'", "''");
		    	
		    	//String sqlNCDIS = "insert into ncdis.patient (ramq,sex,dob,dod,idcommunity,iscree,chart,band,entrydate,consent,death_cause,lname,fname,idpatient) values "
				// +"('"+rsCDIS.getString("ramq")+"',"+rsCDIS.getInt("sex")+",'"+dobStr+"','"+dodStr+"',"+rsCDIS.getInt("idcommunity")+","+rsCDIS.getInt("native")+","+rsCDIS.getInt("chart")+",'"+rsCDIS.getString("band")+"',GETDATE(),"+rsCDIS.getInt("consent")+",'"+dcause+"','LASTNAME','FIRSTNAME',"+rsCDIS.getInt("idpatient")+")";

		    	psNCDIS.setInt(5, rsCDIS.getInt("idcommunity"));
		    	psNCDIS.setInt(7, rsCDIS.getInt("chart"));
		    	psNCDIS.setString(8, rsCDIS.getString("band"));
		    	psNCDIS.setString(10, dcause);
		    	psNCDIS.setInt(11, rsCDIS.getInt("idpatient"));
		    	psNCDIS.setDate(12, rsCDIS.getDate("entrydate"));
		    	
		    	psNCDIS.execute();
		    	psNCDIS.clearBatch();
		    }
		    result = "success";
	    	sis.execute(siioff);
	    	sis.close();
		}catch (SQLException se) {
		        se.printStackTrace();
		        System.exit(0);
	    } catch (NamingException e) {
			e.printStackTrace();
		}  catch (Exception e) {
			e.printStackTrace();
			
		} finally {
	        try {
	            rsCDIS.close();
	            psCDIS.close();
	            psNCDIS.close();
	            connCDIS.close();
	            connNCDIS.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	
	
	public String MigrateDiabetes(Hashtable<String, String[]> args){
		String result = "";
		Context initContext;
		DataSource dsCDIS;
		DataSource dsNCDIS;
		ResultSet rsCDIS = null;
		Statement psCDIS = null;
		PreparedStatement psNCDIS = null;
		//Statement psNCDIS = null;
		Connection connCDIS = null;
		Connection connNCDIS = null;
		String sqlCDIS = "select cd.* from dbo.cdis_diabet cd left join dbo.cdis_patient cp on cd.idpatient = cp.idpatient where cp.idpatient > 0";
		try {
			initContext = new InitialContext();
			dsNCDIS = (DataSource)initContext.lookup("jdbc/ncdis");
			dsCDIS = (DataSource)initContext.lookup("jdbc/cdis");
			connCDIS = dsCDIS.getConnection();
			connNCDIS = dsNCDIS.getConnection();
		    psCDIS = connCDIS.createStatement();
		    rsCDIS = psCDIS.executeQuery(sqlCDIS);
		    String a1 = "";
		    while (rsCDIS.next()) {
		    	String sqlNCDIS = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'DTYPE'),getdate())";
		    	a1 = rsCDIS.getString("idpatient");
		    	psNCDIS = connNCDIS.prepareStatement(sqlNCDIS);
		    	psNCDIS.setInt(1, rsCDIS.getInt("idpatient"));
		    	psNCDIS.setDate(2, rsCDIS.getDate("dateonset"));
		    	psNCDIS.setString(3, rsCDIS.getString("iddiabetype"));
		    	try{
		    		psNCDIS.execute();
		    	}catch(SQLException se){
		    		se.printStackTrace();
		    		//System.exit(0);
		    	}
		    	psNCDIS.clearBatch();

		    }
		    result = "success";
		}catch (SQLException se) {
		        se.printStackTrace();
	    } catch (NamingException e) {
			e.printStackTrace();
			  System.exit(0);
		}  catch (Exception e) {
			e.printStackTrace();
			
		} finally {
	        try {
	            rsCDIS.close();
	            psCDIS.close();
	            psNCDIS.close();
	            connCDIS.close();
	            connNCDIS.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	

	
	public String MigrateLab(Hashtable<String, String[]> args){
		String result = "";
		Context initContext;
		DataSource dsCDIS;
		DataSource dsNCDIS;
		ResultSet rsCDIS = null;
		Statement psCDIS = null;
		PreparedStatement psNCDIS = null;
		//Statement psNCDIS = null;
		Connection connCDIS = null;
		Connection connNCDIS = null;
		String sqlCDIS = "select cl.* from dbo.cdis_lab cl left join dbo.cdis_patient cp on cl.idpatient = cp.idpatient  where cp.idpatient > 0";
		try {
			initContext = new InitialContext();
			dsNCDIS = (DataSource)initContext.lookup("jdbc/ncdis");
			dsCDIS = (DataSource)initContext.lookup("jdbc/cdis");
			
			
			
			connCDIS = dsCDIS.getConnection();
			connNCDIS = dsNCDIS.getConnection();
		    psCDIS = connCDIS.createStatement();
		    rsCDIS = psCDIS.executeQuery(sqlCDIS);
		    while (rsCDIS.next()) {
		    	//OGTT
		    	//ACGLU
		    	//HBA1C
		    	String a1 = rsCDIS.getString("idpatient");
		    	double hba1c = rsCDIS.getDouble("hba1c");
		    	if(hba1c > 0){
		    		String sqlNCDIS_lab1 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'HBA1C'),getdate())";
		    		PreparedStatement psNCDIS_lab1 = connNCDIS.prepareStatement(sqlNCDIS_lab1);
		    		psNCDIS_lab1.setInt(1, rsCDIS.getInt("idpatient"));
		    		psNCDIS_lab1.setDate(2, rsCDIS.getDate("datelab"));
		    		psNCDIS_lab1.setString(3, rsCDIS.getString("hba1c"));
		    		try{
			    		psNCDIS_lab1.execute();
			    	}catch(SQLException se){
			    		se.printStackTrace();
			    		System.exit(0);
			    	}
		    		psNCDIS_lab1.clearBatch();
		    	}
		    	
		    	double acglu = rsCDIS.getDouble("acglu");
		    	if(acglu > 0){
		    		String sqlNCDIS_lab2 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'ACGLU'),getdate())";
		    		PreparedStatement psNCDIS_lab2 = connNCDIS.prepareStatement(sqlNCDIS_lab2);
		    		psNCDIS_lab2.setInt(1, rsCDIS.getInt("idpatient"));
		    		psNCDIS_lab2.setDate(2, rsCDIS.getDate("datelab"));
		    		psNCDIS_lab2.setString(3, rsCDIS.getString("acglu"));
		    		try{
			    		psNCDIS_lab2.execute();
			    	}catch(SQLException se){
			    		se.printStackTrace();
			    		System.exit(0);
			    	}
		    		psNCDIS_lab2.clearBatch();
		    	}
		    		
		    	double ogtt = rsCDIS.getDouble("ogtt");
		    	if(ogtt > 0){
		    		String sqlNCDIS_lab3 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'OGTT'),getdate())";
		    	   	PreparedStatement psNCDIS_lab3 = connNCDIS.prepareStatement(sqlNCDIS_lab3);
		    	   	psNCDIS_lab3.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_lab3.setDate(2, rsCDIS.getDate("datelab"));
		    	   	psNCDIS_lab3.setString(3, rsCDIS.getString("ogtt"));
		    	   	try{
		    	   		psNCDIS_lab3.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_lab3.clearBatch();
		    	}

		    }
		    result = "success";
		}catch (SQLException se) {
		        se.printStackTrace();
		        System.exit(0);
	    } catch (NamingException e) {
			e.printStackTrace();
		}  catch (Exception e) {
			e.printStackTrace();
			
		} finally {
	        try {
	            rsCDIS.close();
	            psCDIS.close();
	            //psNCDIS_lab1.close();
	            connCDIS.close();
	            connNCDIS.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}

	
	public String MigrateRenal(Hashtable<String, String[]> args){
		String result = "";
		Context initContext;
		DataSource dsCDIS;
		DataSource dsNCDIS;
		ResultSet rsCDIS = null;
		Statement psCDIS = null;
		PreparedStatement psNCDIS = null;
		//Statement psNCDIS = null;
		Connection connCDIS = null;
		Connection connNCDIS = null;
		String sqlCDIS = "select cl.* from dbo.cdis_renal cl left join dbo.cdis_patient cp on cl.idpatient = cp.idpatient where cp.idpatient > 0";
		try {
			initContext = new InitialContext();
			dsNCDIS = (DataSource)initContext.lookup("jdbc/ncdis");
			dsCDIS = (DataSource)initContext.lookup("jdbc/cdis");
			
			connCDIS = dsCDIS.getConnection();
			connNCDIS = dsNCDIS.getConnection();
		    psCDIS = connCDIS.createStatement();
		    rsCDIS = psCDIS.executeQuery(sqlCDIS);
		    while (rsCDIS.next()) {
		    	//ACRATIO  doub
		    	//CREA  int
		    	//CRCL dob
		    	//PROTE dob
		    	//EGFR	int	    	
		    	double acratio = rsCDIS.getDouble("acratio");
		    	if(acratio > 0){
		    		String sqlNCDIS_renal1 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'ACRATIO'),getdate())";
		    		PreparedStatement psNCDIS_renal1 = connNCDIS.prepareStatement(sqlNCDIS_renal1);
		    		psNCDIS_renal1.setInt(1, rsCDIS.getInt("idpatient"));
		    		psNCDIS_renal1.setDate(2, rsCDIS.getDate("daterenal"));
		    		psNCDIS_renal1.setString(3, rsCDIS.getString("acratio"));
		    		try{
			    		psNCDIS_renal1.execute();
			    	}catch(SQLException se){
			    		se.printStackTrace();
			    		System.exit(0);
			    	}
		    		psNCDIS_renal1.clearBatch();
		    	}
		    	
		    	double crcl = rsCDIS.getDouble("crcl");
		    	if(crcl > 0){
		    		String sqlNCDIS_renal2 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'CRCL'),getdate())";
		    		PreparedStatement psNCDIS_renal2 = connNCDIS.prepareStatement(sqlNCDIS_renal2);
		    		psNCDIS_renal2.setInt(1, rsCDIS.getInt("idpatient"));
		    		psNCDIS_renal2.setDate(2, rsCDIS.getDate("daterenal"));
		    		psNCDIS_renal2.setString(3, rsCDIS.getString("crcl"));
		    		try{
			    		psNCDIS_renal2.execute();
			    	}catch(SQLException se){
			    		se.printStackTrace();
			    		System.exit(0);
			    	}
		    		psNCDIS_renal2.clearBatch();
		    	}
		    		
		    	double prote = rsCDIS.getDouble("prote");
		    	if(prote > 0){
		    		String sqlNCDIS_renal3 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'PROTE'),getdate())";
		    	   	PreparedStatement psNCDIS_renal3 = connNCDIS.prepareStatement(sqlNCDIS_renal3);
		    	   	psNCDIS_renal3.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_renal3.setDate(2, rsCDIS.getDate("daterenal"));
		    	   	psNCDIS_renal3.setString(3, rsCDIS.getString("prote"));
		    	   	try{
		    	   		psNCDIS_renal3.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_renal3.clearBatch();
		    	}
		    	int crea = rsCDIS.getInt("crea");
		    	if(crea > 0){
		    		String sqlNCDIS_renal4 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'CREA'),getdate())";
		    	   	PreparedStatement psNCDIS_renal4 = connNCDIS.prepareStatement(sqlNCDIS_renal4);
		    	   	psNCDIS_renal4.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_renal4.setDate(2, rsCDIS.getDate("daterenal"));
		    	   	psNCDIS_renal4.setString(3, rsCDIS.getString("crea"));
		    	   	try{
		    	   		psNCDIS_renal4.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_renal4.clearBatch();
		    	}
		    	
		    	int egfr = rsCDIS.getInt("egfr");
		    	if(egfr > 0){
		    		String sqlNCDIS_renal5 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'EGFR'),getdate())";
		    	   	PreparedStatement psNCDIS_renal5 = connNCDIS.prepareStatement(sqlNCDIS_renal5);
		    	   	psNCDIS_renal5.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_renal5.setDate(2, rsCDIS.getDate("daterenal"));
		    	   	psNCDIS_renal5.setString(3, rsCDIS.getString("egfr"));
		    	   	try{
		    	   		psNCDIS_renal5.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_renal5.clearBatch();
		    	}

		    }
		    result = "success";
		}catch (SQLException se) {
		        se.printStackTrace();
		        System.exit(0);
	    } catch (NamingException e) {
			e.printStackTrace();
		}  catch (Exception e) {
			e.printStackTrace();
			
		} finally {
	        try {
	            rsCDIS.close();
	            psCDIS.close();
	            //psNCDIS_lab1.close();
	            connCDIS.close();
	            connNCDIS.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	
	public String MigrateLipid(Hashtable<String, String[]> args){
		String result = "";
		Context initContext;
		DataSource dsCDIS;
		DataSource dsNCDIS;
		ResultSet rsCDIS = null;
		Statement psCDIS = null;
		PreparedStatement psNCDIS = null;
		//Statement psNCDIS = null;
		Connection connCDIS = null;
		Connection connNCDIS = null;
		String sqlCDIS = "select cl.* from dbo.cdis_lipid cl left join dbo.cdis_patient cp on cl.idpatient = cp.idpatient where cp.idpatient > 0";
		try {
			initContext = new InitialContext();
			dsNCDIS = (DataSource)initContext.lookup("jdbc/ncdis");
			dsCDIS = (DataSource)initContext.lookup("jdbc/cdis");
			
			connCDIS = dsCDIS.getConnection();
			connNCDIS = dsNCDIS.getConnection();
		    psCDIS = connCDIS.createStatement();
		    rsCDIS = psCDIS.executeQuery(sqlCDIS);
		    while (rsCDIS.next()) {
		    	//TCHOL
		    	//TGLYCER
		    	//HDL
		    	//LDL
		    	//TCHDL   	
		    	double tchol = rsCDIS.getDouble("tchol");
		    	if(tchol > 0){
		    		String sqlNCDIS_lipid1 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'TCHOL'),getdate())";
		    		PreparedStatement psNCDIS_lipid1 = connNCDIS.prepareStatement(sqlNCDIS_lipid1);
		    		psNCDIS_lipid1.setInt(1, rsCDIS.getInt("idpatient"));
		    		psNCDIS_lipid1.setDate(2, rsCDIS.getDate("datelipid"));
		    		psNCDIS_lipid1.setString(3, rsCDIS.getString("tchol"));
		    		try{
			    		psNCDIS_lipid1.execute();
			    	}catch(SQLException se){
			    		se.printStackTrace();
			    		System.exit(0);
			    	}
		    		psNCDIS_lipid1.clearBatch();
		    	}
		    	
		    	double tglycer = rsCDIS.getDouble("tglycer");
		    	if(tglycer > 0){
		    		String sqlNCDIS_lipid2 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'TGLYCER'),getdate())";
		    		PreparedStatement psNCDIS_lipid2 = connNCDIS.prepareStatement(sqlNCDIS_lipid2);
		    		psNCDIS_lipid2.setInt(1, rsCDIS.getInt("idpatient"));
		    		psNCDIS_lipid2.setDate(2, rsCDIS.getDate("datelipid"));
		    		psNCDIS_lipid2.setString(3, rsCDIS.getString("tglycer"));
		    		try{
			    		psNCDIS_lipid2.execute();
			    	}catch(SQLException se){
			    		se.printStackTrace();
			    		System.exit(0);
			    	}
		    		psNCDIS_lipid2.clearBatch();
		    	}
		    		
		    	double hdl = rsCDIS.getDouble("hdl");
		    	if(hdl > 0){
		    		String sqlNCDIS_lipid3 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'HDL'),getdate())";
		    	   	PreparedStatement psNCDIS_lipid3 = connNCDIS.prepareStatement(sqlNCDIS_lipid3);
		    	   	psNCDIS_lipid3.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_lipid3.setDate(2, rsCDIS.getDate("datelipid"));
		    	   	psNCDIS_lipid3.setString(3, rsCDIS.getString("hdl"));
		    	   	try{
		    	   		psNCDIS_lipid3.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_lipid3.clearBatch();
		    	}
		    	double ldl = rsCDIS.getDouble("ldl");
		    	if(ldl > 0){
		    		String sqlNCDIS_lipid4 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'LDL'),getdate())";
		    	   	PreparedStatement psNCDIS_lipid4 = connNCDIS.prepareStatement(sqlNCDIS_lipid4);
		    	   	psNCDIS_lipid4.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_lipid4.setDate(2, rsCDIS.getDate("datelipid"));
		    	   	psNCDIS_lipid4.setString(3, rsCDIS.getString("ldl"));
		    	   	try{
		    	   		psNCDIS_lipid4.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_lipid4.clearBatch();
		    	}
		    	
		    	double tchdl = rsCDIS.getDouble("tchdl");
		    	if(tchdl > 0){
		    		String sqlNCDIS_lipid5 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'TCHDL'),getdate())";
		    	   	PreparedStatement psNCDIS_lipid5 = connNCDIS.prepareStatement(sqlNCDIS_lipid5);
		    	   	psNCDIS_lipid5.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_lipid5.setDate(2, rsCDIS.getDate("datelipid"));
		    	   	psNCDIS_lipid5.setString(3, rsCDIS.getString("tchdl"));
		    	   	try{
		    	   		psNCDIS_lipid5.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_lipid5.clearBatch();
		    	}

		    }
		    result = "success";
		}catch (SQLException se) {
		        se.printStackTrace();
		        System.exit(0);
	    } catch (NamingException e) {
			e.printStackTrace();
		}  catch (Exception e) {
			e.printStackTrace();
			
		} finally {
	        try {
	            rsCDIS.close();
	            psCDIS.close();
	            //psNCDIS_lab1.close();
	            connCDIS.close();
	            connNCDIS.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	
	public String MigrateMDVisits(Hashtable<String, String[]> args){
		String result = "";
		Context initContext;
		DataSource dsCDIS;
		DataSource dsNCDIS;
		ResultSet rsCDIS = null;
		Statement psCDIS = null;
		PreparedStatement psNCDIS = null;
		//Statement psNCDIS = null;
		Connection connCDIS = null;
		Connection connNCDIS = null;
		String sqlCDIS = "select md.*,cp.height from dbo.cdis_mdvisits md left join dbo.cdis_patient cp on md.idpatient = cp.idpatient where cp.idpatient > 0";
		try {
			initContext = new InitialContext();
			dsNCDIS = (DataSource)initContext.lookup("jdbc/ncdis");
			dsCDIS = (DataSource)initContext.lookup("jdbc/cdis");
			
			connCDIS = dsCDIS.getConnection();
			connNCDIS = dsNCDIS.getConnection();
		    psCDIS = connCDIS.createStatement();
		    rsCDIS = psCDIS.executeQuery(sqlCDIS);
		    while (rsCDIS.next()) {
		    	//SBP  int
		    	//DBP  int
		    	//SMOKE int
		    	//AER int
		    	//HIPO int  episodes per month
		    	//ACGLU dob
		    	//PCGLU dob
		    	//FOOT int
		    	//NEURO int
		    	//PSYCO int
		    	//DEPR int
		    	//WEIGHT dob
		    	//HEIGHT dob	
		    	String bp = rsCDIS.getString("bp");
		    	String[] bps = bp.split("/");
			    if(bps.length == 2 ){
			    	String sbp = bps[0];
			    	String dbp = bps[1];
			    	if(!sbp.equals("000")){
			    		String sqlNCDIS_mdvisits1 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'SBP'),getdate())";
			    		PreparedStatement psNCDIS_mdvisits1 = connNCDIS.prepareStatement(sqlNCDIS_mdvisits1);
			    		psNCDIS_mdvisits1.setInt(1, rsCDIS.getInt("idpatient"));
			    		psNCDIS_mdvisits1.setDate(2, rsCDIS.getDate("datemdvisits"));
			    		psNCDIS_mdvisits1.setString(3, sbp);
			    		try{
				    		psNCDIS_mdvisits1.execute();
				    	}catch(SQLException se){
				    		se.printStackTrace();
				    		System.exit(0);
				    	}
			    		psNCDIS_mdvisits1.clearBatch();
			    	}
			    	
			    	if(!sbp.equals("000")){
			    		String sqlNCDIS_mdvisits2 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'DBP'),getdate())";
			    		PreparedStatement psNCDIS_mdvisits2 = connNCDIS.prepareStatement(sqlNCDIS_mdvisits2);
			    		psNCDIS_mdvisits2.setInt(1, rsCDIS.getInt("idpatient"));
			    		psNCDIS_mdvisits2.setDate(2, rsCDIS.getDate("datemdvisits"));
			    		psNCDIS_mdvisits2.setString(3, dbp);
			    		try{
				    		psNCDIS_mdvisits2.execute();
				    	}catch(SQLException se){
				    		se.printStackTrace();
				    		System.exit(0);
				    	}
			    		psNCDIS_mdvisits2.clearBatch();
			    	}
		    	}
		    	
		    	int smoke = rsCDIS.getInt("smoker");  // 1 yes 2 no
		    	if(smoke > 0){
		    		String sqlNCDIS_mdvisits3 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'SMOKE'),getdate())";
		    	   	PreparedStatement psNCDIS_mdvisits3 = connNCDIS.prepareStatement(sqlNCDIS_mdvisits3);
		    	   	psNCDIS_mdvisits3.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_mdvisits3.setDate(2, rsCDIS.getDate("datemdvisits"));
		    	   	psNCDIS_mdvisits3.setString(3, rsCDIS.getString("smoker"));
		    	   	try{
		    	   		psNCDIS_mdvisits3.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_mdvisits3.clearBatch();
		    	}
		    	int aerobic = rsCDIS.getInt("aerobic");
		    	if(aerobic > 0){
		    		String sqlNCDIS_mdvisits4 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'AER'),getdate())";
		    	   	PreparedStatement psNCDIS_mdvisits4 = connNCDIS.prepareStatement(sqlNCDIS_mdvisits4);
		    	   	psNCDIS_mdvisits4.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_mdvisits4.setDate(2, rsCDIS.getDate("datemdvisits"));
		    	   	psNCDIS_mdvisits4.setString(3, rsCDIS.getString("aerobic"));
		    	   	try{
		    	   		psNCDIS_mdvisits4.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_mdvisits4.clearBatch();
		    	}
		    	
		    	int hypo = rsCDIS.getInt("hypo");
		    	if(hypo > 0){
		    		String sqlNCDIS_mdvisits5 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'HIPO'),getdate())";
		    	   	PreparedStatement psNCDIS_mdvisits5 = connNCDIS.prepareStatement(sqlNCDIS_mdvisits5);
		    	   	psNCDIS_mdvisits5.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_mdvisits5.setDate(2, rsCDIS.getDate("datemdvisits"));
		    	   	psNCDIS_mdvisits5.setString(3, rsCDIS.getString("hypo"));
		    	   	try{
		    	   		psNCDIS_mdvisits5.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_mdvisits5.clearBatch();
		    	}

		    	
		    	double acglu = rsCDIS.getDouble("ac_glu");
		    	if(acglu > 0){
		    		String sqlNCDIS_mdvisits6 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'ACGLUMD'),getdate())";
		    	   	PreparedStatement psNCDIS_mdvisits6 = connNCDIS.prepareStatement(sqlNCDIS_mdvisits6);
		    	   	psNCDIS_mdvisits6.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_mdvisits6.setDate(2, rsCDIS.getDate("datemdvisits"));
		    	   	psNCDIS_mdvisits6.setString(3, rsCDIS.getString("ac_glu"));
		    	   	try{
		    	   		psNCDIS_mdvisits6.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_mdvisits6.clearBatch();
		    	}
		    	double pcglu = rsCDIS.getDouble("pc_glu");
		    	if(pcglu > 0){
		    		String sqlNCDIS_mdvisits7 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'PCGLUMD'),getdate())";
		    	   	PreparedStatement psNCDIS_mdvisits7 = connNCDIS.prepareStatement(sqlNCDIS_mdvisits7);
		    	   	psNCDIS_mdvisits7.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_mdvisits7.setDate(2, rsCDIS.getDate("datemdvisits"));
		    	   	psNCDIS_mdvisits7.setString(3, rsCDIS.getString("pc_glu"));
		    	   	try{
		    	   		psNCDIS_mdvisits7.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_mdvisits7.clearBatch();
		    	}
		    	
		    	int foot = rsCDIS.getInt("foot");
		    	if(foot == 1){
		    		String sqlNCDIS_mdvisits8 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'FOOT'),getdate())";
		    	   	PreparedStatement psNCDIS_mdvisits8 = connNCDIS.prepareStatement(sqlNCDIS_mdvisits8);
		    	   	psNCDIS_mdvisits8.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_mdvisits8.setDate(2, rsCDIS.getDate("datemdvisits"));
		    	   	psNCDIS_mdvisits8.setString(3, rsCDIS.getString("foot"));
		    	   	try{
		    	   		psNCDIS_mdvisits8.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_mdvisits8.clearBatch();
		    	}
		    	
		    	int neuro = rsCDIS.getInt("neurorp");
		    	if(neuro == 1){
		    		String sqlNCDIS_mdvisits13 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'NEUROMD'),getdate())";
		    	   	PreparedStatement psNCDIS_mdvisits13 = connNCDIS.prepareStatement(sqlNCDIS_mdvisits13);
		    	   	psNCDIS_mdvisits13.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_mdvisits13.setDate(2, rsCDIS.getDate("datemdvisits"));
		    	   	psNCDIS_mdvisits13.setString(3, rsCDIS.getString("neurorp"));
		    	   	try{
		    	   		psNCDIS_mdvisits13.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_mdvisits13.clearBatch();
		    	}
		    	
		    	
		    	int psycho = rsCDIS.getInt("psyco");
		    	if(psycho == 1){
		    		String sqlNCDIS_mdvisits9 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'PSYCO'),getdate())";
		    	   	PreparedStatement psNCDIS_mdvisits9 = connNCDIS.prepareStatement(sqlNCDIS_mdvisits9);
		    	   	psNCDIS_mdvisits9.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_mdvisits9.setDate(2, rsCDIS.getDate("datemdvisits"));
		    	   	psNCDIS_mdvisits9.setString(3, rsCDIS.getString("psyco"));
		    	   	try{
		    	   		psNCDIS_mdvisits9.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_mdvisits9.clearBatch();
		    	}
		    	
		    	int depr = rsCDIS.getInt("depression");
		    	if(depr == 1){
		    		String sqlNCDIS_mdvisits10 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'DEPR'),getdate())";
		    	   	PreparedStatement psNCDIS_mdvisits10 = connNCDIS.prepareStatement(sqlNCDIS_mdvisits10);
		    	   	psNCDIS_mdvisits10.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_mdvisits10.setDate(2, rsCDIS.getDate("datemdvisits"));
		    	   	psNCDIS_mdvisits10.setString(3, rsCDIS.getString("depression"));
		    	   	try{
		    	   		psNCDIS_mdvisits10.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_mdvisits10.clearBatch();
		    	}
		    	
		    	double weight = rsCDIS.getDouble("weight");
		    	if(weight > 0){
		    		String sqlNCDIS_mdvisits11 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'WEIGHT'),getdate())";
		    	   	PreparedStatement psNCDIS_mdvisits11 = connNCDIS.prepareStatement(sqlNCDIS_mdvisits11);
		    	   	psNCDIS_mdvisits11.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_mdvisits11.setDate(2, rsCDIS.getDate("datemdvisits"));
		    	   	psNCDIS_mdvisits11.setString(3, rsCDIS.getString("weight"));
		    	   	try{
		    	   		psNCDIS_mdvisits11.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_mdvisits11.clearBatch();
		    	}
		    	
		    	double height = rsCDIS.getDouble("height");
		    	if(height > 0){
		    		String sqlNCDIS_mdvisits12 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'HEIGHT'),getdate())";
		    	   	PreparedStatement psNCDIS_mdvisits12 = connNCDIS.prepareStatement(sqlNCDIS_mdvisits12);
		    	   	psNCDIS_mdvisits12.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_mdvisits12.setDate(2, rsCDIS.getDate("datemdvisits"));
		    	   	psNCDIS_mdvisits12.setString(3, rsCDIS.getString("height"));
		    	   	try{
		    	   		psNCDIS_mdvisits12.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_mdvisits12.clearBatch();
		    	}
		    	
		    	
		    }
		    result = "success";
		}catch (SQLException se) {
		        se.printStackTrace();
		        System.exit(0);
	    } catch (NamingException e) {
			e.printStackTrace();
		}  catch (Exception e) {
			e.printStackTrace();
			
		} finally {
	        try {
	            rsCDIS.close();
	            psCDIS.close();
	            //psNCDIS_lab1.close();
	            connCDIS.close();
	            connNCDIS.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}

	public String MigrateComplications(Hashtable<String, String[]> args){
		String result = "";
		Context initContext;
		DataSource dsCDIS;
		DataSource dsNCDIS;
		ResultSet rsCDIS = null;
		Statement psCDIS = null;
		PreparedStatement psNCDIS = null;
		//Statement psNCDIS = null;
		Connection connCDIS = null;
		Connection connNCDIS = null;
		String sqlCDIS = "select cl.* from dbo.cdis_complications cl left join dbo.cdis_patient cp on cl.idpatient = cp.idpatient where cp.idpatient > 0";
		try {
			initContext = new InitialContext();
			dsNCDIS = (DataSource)initContext.lookup("jdbc/ncdis");
			dsCDIS = (DataSource)initContext.lookup("jdbc/cdis");
			connCDIS = dsCDIS.getConnection();
			connNCDIS = dsNCDIS.getConnection();
		    psCDIS = connCDIS.createStatement();
		    rsCDIS = psCDIS.executeQuery(sqlCDIS);
		    while (rsCDIS.next()) {
		    	
		    	//RETI
		    	//LTHER
		    	//LBLIND
		    	//MICRO
		    	//MACRO
		    	//RENF
		    	//DIAL
		    	//NEURO
		    	//FULCER
		    	//AMPUT
		    	//IMPOT
		    	//CAD
		    	//CVD
		    	//PVD
		    	
		    	Date reti = rsCDIS.getDate("reti");
		    	if((reti != null) && (reti.getYear() > 0)){
		    		String sqlNCDIS_com1 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'RETI'),getdate())";
		    	   	PreparedStatement psNCDIS_com1 = connNCDIS.prepareStatement(sqlNCDIS_com1);
		    	   	psNCDIS_com1.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_com1.setDate(2, rsCDIS.getDate("reti"));
		    	   	psNCDIS_com1.setString(3, null);
		    	   	try{
		    	   		psNCDIS_com1.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_com1.clearBatch();
		    	}

		    	Date lther = rsCDIS.getDate("lther");
		    	if((lther != null) && (lther.getYear() > 0)){
		    		String sqlNCDIS_com2 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'LTHER'),getdate())";
		    	   	PreparedStatement psNCDIS_com2 = connNCDIS.prepareStatement(sqlNCDIS_com2);
		    	   	psNCDIS_com2.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_com2.setDate(2, rsCDIS.getDate("lther"));
		    	   	psNCDIS_com2.setString(3, null);
		    	   	try{
		    	   		psNCDIS_com2.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_com2.clearBatch();
		    	}

		    	Date lblind = rsCDIS.getDate("lblind");
		    	if((lblind != null) && (lblind.getYear() > 0)){
		    		String sqlNCDIS_com3 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'LBLIND'),getdate())";
		    	   	PreparedStatement psNCDIS_com3 = connNCDIS.prepareStatement(sqlNCDIS_com3);
		    	   	psNCDIS_com3.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_com3.setDate(2, rsCDIS.getDate("lblind"));
		    	   	psNCDIS_com3.setString(3, null);
		    	   	try{
		    	   		psNCDIS_com3.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_com3.clearBatch();
		    	}
		    	
		    	Date micro = rsCDIS.getDate("micro");
		    	if((micro != null) && (micro.getYear() > 0)){
		    		String sqlNCDIS_com4 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'MICRO'),getdate())";
		    	   	PreparedStatement psNCDIS_com4 = connNCDIS.prepareStatement(sqlNCDIS_com4);
		    	   	psNCDIS_com4.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_com4.setDate(2, rsCDIS.getDate("micro"));
		    	   	psNCDIS_com4.setString(3, null);
		    	   	try{
		    	   		psNCDIS_com4.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_com4.clearBatch();
		    	}

		    	Date macro = rsCDIS.getDate("macro");
		    	if((macro != null) && (macro.getYear() > 0)){
		    		String sqlNCDIS_com5 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'MACRO'),getdate())";
		    	   	PreparedStatement psNCDIS_com5 = connNCDIS.prepareStatement(sqlNCDIS_com5);
		    	   	psNCDIS_com5.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_com5.setDate(2, rsCDIS.getDate("macro"));
		    	   	psNCDIS_com5.setString(3, null);
		    	   	try{
		    	   		psNCDIS_com5.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_com5.clearBatch();
		    	}
		    	
		    	Date renf = rsCDIS.getDate("renf");
		    	if((renf != null) && (renf.getYear() > 0)){
		    		String sqlNCDIS_com6 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'RENF'),getdate())";
		    	   	PreparedStatement psNCDIS_com6 = connNCDIS.prepareStatement(sqlNCDIS_com6);
		    	   	psNCDIS_com6.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_com6.setDate(2, rsCDIS.getDate("renf"));
		    	   	psNCDIS_com6.setString(3, null);
		    	   	try{
		    	   		psNCDIS_com6.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_com6.clearBatch();
		    	}
		    	
		    	Date dial = rsCDIS.getDate("dial");
		    	if((dial != null) && (dial.getYear() > 0)){
		    		String sqlNCDIS_com7 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'DIAL'),getdate())";
		    	   	PreparedStatement psNCDIS_com7 = connNCDIS.prepareStatement(sqlNCDIS_com7);
		    	   	psNCDIS_com7.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_com7.setDate(2, rsCDIS.getDate("dial"));
		    	   	psNCDIS_com7.setString(3, null);
		    	   	try{
		    	   		psNCDIS_com7.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_com7.clearBatch();
		    	}
		    	
		    	Date neuro = rsCDIS.getDate("neuro");
		    	if((neuro != null) && (neuro.getYear() > 0)){
		    		String sqlNCDIS_com8 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'NEURO'),getdate())";
		    	   	PreparedStatement psNCDIS_com8 = connNCDIS.prepareStatement(sqlNCDIS_com8);
		    	   	psNCDIS_com8.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_com8.setDate(2, rsCDIS.getDate("neuro"));
		    	   	psNCDIS_com8.setString(3, null);
		    	   	try{
		    	   		psNCDIS_com8.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_com8.clearBatch();
		    	}
		    	
		    	Date fulcer = rsCDIS.getDate("fulcer");
		    	if((fulcer != null) && (fulcer.getYear() > 0)){
		    		String sqlNCDIS_com9 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'FULCER'),getdate())";
		    	   	PreparedStatement psNCDIS_com9 = connNCDIS.prepareStatement(sqlNCDIS_com9);
		    	   	psNCDIS_com9.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_com9.setDate(2, rsCDIS.getDate("fulcer"));
		    	   	psNCDIS_com9.setString(3, null);
		    	   	try{
		    	   		psNCDIS_com9.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_com9.clearBatch();
		    	}
		    	
		    	Date amput = rsCDIS.getDate("amput");
		    	if((amput != null) && (amput.getYear() > 0)){
		    		String sqlNCDIS_com10 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'AMPUT'),getdate())";
		    	   	PreparedStatement psNCDIS_com10 = connNCDIS.prepareStatement(sqlNCDIS_com10);
		    	   	psNCDIS_com10.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_com10.setDate(2, rsCDIS.getDate("amput"));
		    	   	psNCDIS_com10.setString(3, null);
		    	   	try{
		    	   		psNCDIS_com10.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_com10.clearBatch();
		    	}
		    	
		    	Date impot = rsCDIS.getDate("impot");
		    	if((impot != null) && (impot.getYear() > 0)){
		    		String sqlNCDIS_com11 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'IMPOT'),getdate())";
		    	   	PreparedStatement psNCDIS_com11 = connNCDIS.prepareStatement(sqlNCDIS_com11);
		    	   	psNCDIS_com11.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_com11.setDate(2, rsCDIS.getDate("impot"));
		    	   	psNCDIS_com11.setString(3, null);
		    	   	try{
		    	   		psNCDIS_com11.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_com11.clearBatch();
		    	}
		    	
		    	Date cad = rsCDIS.getDate("cad");
		    	if((cad != null) && (cad.getYear() > 0)){
		    		String sqlNCDIS_com12 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'CAD'),getdate())";
		    	   	PreparedStatement psNCDIS_com12 = connNCDIS.prepareStatement(sqlNCDIS_com12);
		    	   	psNCDIS_com12.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_com12.setDate(2, rsCDIS.getDate("cad"));
		    	   	psNCDIS_com12.setString(3, null);
		    	   	try{
		    	   		psNCDIS_com12.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_com12.clearBatch();
		    	}
		    	
		    	Date cvd = rsCDIS.getDate("cvd");
		    	if((cvd != null) && (cvd.getYear() > 0)){
		    		String sqlNCDIS_com13 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'CVD'),getdate())";
		    	   	PreparedStatement psNCDIS_com13 = connNCDIS.prepareStatement(sqlNCDIS_com13);
		    	   	psNCDIS_com13.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_com13.setDate(2, rsCDIS.getDate("cvd"));
		    	   	psNCDIS_com13.setString(3, null);
		    	   	try{
		    	   		psNCDIS_com13.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_com13.clearBatch();
		    	}
		    	
		    	Date pvd = rsCDIS.getDate("pvd");
		    	if((pvd != null) && (pvd.getYear() > 0)){
		    		String sqlNCDIS_com14 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'PVD'),getdate())";
		    	   	PreparedStatement psNCDIS_com14 = connNCDIS.prepareStatement(sqlNCDIS_com14);
		    	   	psNCDIS_com14.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_com14.setDate(2, rsCDIS.getDate("pvd"));
		    	   	psNCDIS_com14.setString(3, null);
		    	   	try{
		    	   		psNCDIS_com14.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_com14.clearBatch();
		    	}
		    	
		    }
		    result = "success";
		}catch (SQLException se) {
		        se.printStackTrace();
		        System.exit(0);
	    } catch (NamingException e) {
			e.printStackTrace();
		}  catch (Exception e) {
			e.printStackTrace();
			
		} finally {
	        try {
	            rsCDIS.close();
	            psCDIS.close();
	            //psNCDIS.close();
	            connCDIS.close();
	            connNCDIS.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	
	public String MigrateMiscellaneous(Hashtable<String, String[]> args){
		String result = "";
		Context initContext;
		DataSource dsCDIS;
		DataSource dsNCDIS;
		ResultSet rsCDIS = null;
		Statement psCDIS = null;
		PreparedStatement psNCDIS = null;
		//Statement psNCDIS = null;
		Connection connCDIS = null;
		Connection connNCDIS = null;
		String sqlCDIS = "select cl.* from dbo.cdis_miscellaneous cl left join dbo.cdis_patient cp on cl.idpatient = cp.idpatient where cp.idpatient > 0";
		try {
			initContext = new InitialContext();
			dsNCDIS = (DataSource)initContext.lookup("jdbc/ncdis");
			dsCDIS = (DataSource)initContext.lookup("jdbc/cdis");
			connCDIS = dsCDIS.getConnection();
			connNCDIS = dsNCDIS.getConnection();
		    psCDIS = connCDIS.createStatement();
		    rsCDIS = psCDIS.executeQuery(sqlCDIS);
		    while (rsCDIS.next()) {
		    	
		    	//DOPHTA
		    	//DINFLU
		    	//DPNEU
		    	//DPPD
		    	//PPD
		    	//INH
		    	
		    	Date dophta = rsCDIS.getDate("dateophta");
		    	if((dophta != null) && (dophta.getYear() > 0)){
		    		String sqlNCDIS_misc1 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'DOPHTA'),getdate())";
		    	   	PreparedStatement psNCDIS_misc1 = connNCDIS.prepareStatement(sqlNCDIS_misc1);
		    	   	psNCDIS_misc1.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_misc1.setDate(2, rsCDIS.getDate("dateophta"));
		    	   	psNCDIS_misc1.setString(3, null);
		    	   	try{
		    	   		psNCDIS_misc1.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_misc1.clearBatch();
		    	}

		    	Date dinfu = rsCDIS.getDate("dateinfluenza");
		    	if((dinfu != null) && (dinfu.getYear() > 0)){
		    		String sqlNCDIS_com2 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'DINFLU'),getdate())";
		    	   	PreparedStatement psNCDIS_com2 = connNCDIS.prepareStatement(sqlNCDIS_com2);
		    	   	psNCDIS_com2.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_com2.setDate(2, rsCDIS.getDate("dateinfluenza"));
		    	   	psNCDIS_com2.setString(3, null);
		    	   	try{
		    	   		psNCDIS_com2.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_com2.clearBatch();
		    	}

		    	Date dpneu = rsCDIS.getDate("datepneu");
		    	if((dpneu != null) && (dpneu.getYear() > 0)){
		    		String sqlNCDIS_com3 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'DPNEU'),getdate())";
		    	   	PreparedStatement psNCDIS_com3 = connNCDIS.prepareStatement(sqlNCDIS_com3);
		    	   	psNCDIS_com3.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_com3.setDate(2, rsCDIS.getDate("datepneu"));
		    	   	psNCDIS_com3.setString(3, null);
		    	   	try{
		    	   		psNCDIS_com3.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_com3.clearBatch();
		    	}
		    	
		    	Date dppd = rsCDIS.getDate("dateppd");
		    	if((dppd != null) && (dppd.getYear() > 0)){
		    		String sqlNCDIS_com4 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'DPPD'),getdate())";
		    	   	PreparedStatement psNCDIS_com4 = connNCDIS.prepareStatement(sqlNCDIS_com4);
		    	   	psNCDIS_com4.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_com4.setDate(2, rsCDIS.getDate("dateppd"));
		    	   	psNCDIS_com4.setString(3, null);
		    	   	try{
		    	   		psNCDIS_com4.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_com4.clearBatch();
		    	}
		    	
		    	
		    	int ppd = rsCDIS.getInt("ppd");
		    	if(ppd > 0){
		    		String sqlNCDIS__misc10 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'PPD'),getdate())";
		    	   	PreparedStatement psNCDIS__misc10 = connNCDIS.prepareStatement(sqlNCDIS__misc10);
		    	   	psNCDIS__misc10.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS__misc10.setDate(2, rsCDIS.getDate("dateppd"));
		    	   	psNCDIS__misc10.setString(3, rsCDIS.getString("ppd"));
		    	   	try{
		    	   		psNCDIS__misc10.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS__misc10.clearBatch();
		    	}
		    	
		    	
		    	int inh = rsCDIS.getInt("inh");
		    	if(ppd > 0){
		    		String sqlNCDIS_misc11 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'INH'),getdate())";
		    	   	PreparedStatement psNCDIS_misc11 = connNCDIS.prepareStatement(sqlNCDIS_misc11);
		    	   	psNCDIS_misc11.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_misc11.setDate(2, rsCDIS.getDate("dateppd"));
		    	   	psNCDIS_misc11.setString(3, rsCDIS.getString("inh"));
		    	   	try{
		    	   		psNCDIS_misc11.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_misc11.clearBatch();
		    	}
		    	
		    	
		    }
		    result = "success";
		}catch (SQLException se) {
		        se.printStackTrace();
		        System.exit(0);
	    } catch (NamingException e) {
			e.printStackTrace();
		}  catch (Exception e) {
			e.printStackTrace();
			
		} finally {
	        try {
	            rsCDIS.close();
	            psCDIS.close();
	            //psNCDIS.close();
	            connCDIS.close();
	            connNCDIS.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	
	
	public String MigrateMeds(Hashtable<String, String[]> args){
		String result = "";
		Context initContext;
		DataSource dsCDIS;
		DataSource dsNCDIS;
		ResultSet rsCDIS = null;
		Statement psCDIS = null;
		PreparedStatement psNCDIS = null;
		//Statement psNCDIS = null;
		Connection connCDIS = null;
		Connection connNCDIS = null;
		String sqlCDIS = "select cl.* from dbo.cdis_meds cl left join dbo.cdis_patient cp on cl.idpatient = cp.idpatient where cp.idpatient > 0";
		try {
			initContext = new InitialContext();
			dsNCDIS = (DataSource)initContext.lookup("jdbc/ncdis");
			dsCDIS = (DataSource)initContext.lookup("jdbc/cdis");
			connCDIS = dsCDIS.getConnection();
			connNCDIS = dsNCDIS.getConnection();
		    psCDIS = connCDIS.createStatement();
		    rsCDIS = psCDIS.executeQuery(sqlCDIS);
		    while (rsCDIS.next()) {
		    	
		    	//ORALA   radio
		    	//INSULIN radio
		    	//ACEI
		    	//STATIN
		    	//ASA
		    	
		    	int orala = rsCDIS.getInt("orala");
		    	if((orala > 0)){
		    		String sqlNCDIS_meds1 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'ORALA'),getdate())";
		    	   	PreparedStatement psNCDIS_meds1 = connNCDIS.prepareStatement(sqlNCDIS_meds1);
		    	   	psNCDIS_meds1.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_meds1.setDate(2, null);
		    	   	psNCDIS_meds1.setString(3, rsCDIS.getString("orala"));
		    	   	try{
		    	   		psNCDIS_meds1.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_meds1.clearBatch();
		    	}
		    	
		    	
		    	int insulin = rsCDIS.getInt("insulin");
		    	if((insulin > 0)){
		    		String sqlNCDIS_meds2 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'INSULIN'),getdate())";
		    	   	PreparedStatement psNCDIS_meds2 = connNCDIS.prepareStatement(sqlNCDIS_meds2);
		    	   	psNCDIS_meds2.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_meds2.setDate(2, null);
		    	   	psNCDIS_meds2.setString(3, rsCDIS.getString("insulin"));
		    	   	try{
		    	   		psNCDIS_meds2.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_meds2.clearBatch();
		    	}

		    	int acei = rsCDIS.getInt("acei");
		    	if((acei > 0)){
		    		String sqlNCDIS_meds3 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'ACEI'),getdate())";
		    	   	PreparedStatement psNCDIS_meds3 = connNCDIS.prepareStatement(sqlNCDIS_meds3);
		    	   	psNCDIS_meds3.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_meds3.setDate(2, null);
		    	   	psNCDIS_meds3.setString(3, rsCDIS.getString("acei"));
		    	   	try{
		    	   		psNCDIS_meds3.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_meds3.clearBatch();
		    	}
		    	
		    	
		    	int statin = rsCDIS.getInt("statin");
		    	if((statin > 0)){
		    		String sqlNCDIS_meds4 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'STATIN'),getdate())";
		    	   	PreparedStatement psNCDIS_meds4 = connNCDIS.prepareStatement(sqlNCDIS_meds4);
		    	   	psNCDIS_meds4.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_meds4.setDate(2, null);
		    	   	psNCDIS_meds4.setString(3, rsCDIS.getString("statin"));
		    	   	try{
		    	   		psNCDIS_meds4.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_meds4.clearBatch();
		    	}
		    	
		    	int asa = rsCDIS.getInt("asa");
		    	if((asa > 0)){
		    		String sqlNCDIS_meds5 = "insert into ncdis.cdis_value (idpatient,datevalue,value,iddata,entrydate) values (?,?,?,(select iddata from ncdis.cdis_data where data_code = 'STATIN'),getdate())";
		    	   	PreparedStatement psNCDIS_meds5 = connNCDIS.prepareStatement(sqlNCDIS_meds5);
		    	   	psNCDIS_meds5.setInt(1, rsCDIS.getInt("idpatient"));
		    	   	psNCDIS_meds5.setDate(2, null);
		    	   	psNCDIS_meds5.setString(3, rsCDIS.getString("asa"));
		    	   	try{
		    	   		psNCDIS_meds5.execute();
		    	   	}catch(SQLException se){
		    	   		se.printStackTrace();
		    	   		System.exit(0);
		    	   	}
		    	   	psNCDIS_meds5.clearBatch();
		    	}
		    }
		    result = "success";
		}catch (SQLException se) {
		        se.printStackTrace();
		        System.exit(0);
	    } catch (NamingException e) {
			e.printStackTrace();
		}  catch (Exception e) {
			e.printStackTrace();
			
		} finally {
	        try {
	            rsCDIS.close();
	            psCDIS.close();
	            //psNCDIS.close();
	            connCDIS.close();
	            connNCDIS.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	   }
		return result;
	}
	
	
}
