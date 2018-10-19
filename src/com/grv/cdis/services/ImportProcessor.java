package com.grv.cdis.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.grv.cdis.db.CdisDBridge;
import com.grv.cdis.db.ChbDBridge;
import com.grv.cdis.model.Action;
import com.grv.cdis.model.Community;
import com.grv.cdis.model.Complications;
import com.grv.cdis.model.Depression;
import com.grv.cdis.model.Diabet;
import com.grv.cdis.model.Event;
import com.grv.cdis.model.Hcp;
import com.grv.cdis.model.Lab;
import com.grv.cdis.model.Lipid;
import com.grv.cdis.model.MDVisit;
import com.grv.cdis.model.Meds;
import com.grv.cdis.model.Message;
import com.grv.cdis.model.MessageResponse;
import com.grv.cdis.model.Miscellaneous;
import com.grv.cdis.model.Patient;
import com.grv.cdis.model.Profile;
import com.grv.cdis.model.Renal;
import com.grv.cdis.model.SearchPatient;
import com.grv.cdis.model.Session;
//import com.grv.cdis.model.SearchPatient;
import com.grv.cdis.model.User;
import com.grv.cdis.model.Value;
import com.grv.cdis.model.ValueLimit;
import com.grv.cdis.model.Values;
import com.grv.cdis.util.FileTool;
import com.grv.cdis.util.FtpTool;
import com.grv.cdis.util.MailTool;
import com.sun.org.apache.bcel.internal.generic.NEWARRAY;

public class ImportProcessor {
	
	/*
	 * /ncdis/service/data/getUser?iduser=XX
	 * */
	public String exportRamq(Hashtable<String, String[]> args){
		Gson json = new Gson();
		String result = "";
		String exportRamqFile = CdisDBridge.exportRamq();
		if(!FtpTool.putFile(exportRamqFile, "chisasibi")){
			result = "chisasibi - not exported";
		}
		
		if(!FtpTool.putFile(exportRamqFile, "chibougamou")){
			result += "chisbougamou - not exported";
		}
		result = json.toJson(result);
		return result;
	}
	
	public String importOmnilab(Hashtable<String, String[]> args){
		Gson json = new Gson();
		String result = "";
		String chisasibiStr = importData("chisasibi");
		String chibougamouStr = importData("chibougamou");
		//MailTool.sendMailText("CDIS Import Interface", chisasibiStr+"\n\n"+chibougamouStr, "radu@grvtech.ca");
		
		MailTool.sendMailInHtml("CDIS Import Interface", chisasibiStr+"<br><br>"+chibougamouStr, "radu@grvtech.ca");
		
		result = json.toJson(chisasibiStr+"\n"+chibougamouStr);	
		return result;
	}
	
	
	private static String updateData(Object obj, String[] vars, Hashtable<String, String> dataLine, Patient pat){
		String result = "0";
		CdisDBridge db = new CdisDBridge();
		SimpleDateFormat fileFormat = new SimpleDateFormat("MM/dd/yyyy");
	    SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd");
	    String date = dataLine.get("collecteddate");
	    String ramq = dataLine.get("ramq");
	    Date fileDate = null;
		try {
			fileDate = fileFormat.parse(date);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
	    String updates = "Updates:";
	    String inserts = "Inserts:";
	    int ups = 0;
	    int ins = 0;
		String var = "";
		
		for(int x=0;x<vars.length;x++){
			if(vars[x].equals("Pcr")){
				var = "pccmgmmol";
			}else if(vars[x].equals("Pcrg")){
				var = "pcconvgg";
			}else{
				var = vars[x];
			}
			
			String value = dataLine.get(var.toLowerCase());
			 
			//System.out.println("RAMQ: "+ramq + "     date :"+date +"     value:"+value+"     var:"+vars[x]+"      idpatient:"+pat.getIdpatient());
			
			if(value == null || value.equals("")){
				//System.out.println("THERE IS NO VALUE FOR "+vars[x]);
			}else{
				double fileDoubleValue = 0;
				try{
					if(value.indexOf(">") >= 0){
						value = value.replaceAll(">", "");
						fileDoubleValue = Double.parseDouble(value) + 1;
					}else if(value.indexOf("<") >= 0){
						value = value.replaceAll("<", "");
						fileDoubleValue = Double.parseDouble(value) - 1;
					}else if(value.indexOf("non") >= 0){
						
					}else if(value.indexOf("*") >= 0){
						
					}else if(value.indexOf("re") >= 0){
						
					}else if(value.equals("\\.")){
						
					}else if(value.equals("cancel")){
						
					}else if(value.indexOf("ann") >= 0){
						
					}else{
						fileDoubleValue = Double.parseDouble(value);
					}
					//System.out.println("Value :"+ value );
				}catch(NumberFormatException ex){
					//ex.printStackTrace();
					System.out.println("parsing error:"+value);
				}
				Class<?> cls =  obj.getClass();
				Values vals = new Values();
				try {
					Method m = Class.forName(cls.getName()).getDeclaredMethod("get"+vars[x]);
					vals = (Values) m.invoke(obj);
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				boolean inBD = false;
								
				if(vals != null ){
					ArrayList<Value> listVals = vals.getValues();
					
					for(int xx =0; xx<listVals.size();xx++){
						Value v = listVals.get(xx); 
						String dateValue = v.getDate();
						String valuevalue = v.getValue();
						if(!dateValue.equals("NULL")){
							Date dbDate = null;
							try {
								dbDate = dbFormat.parse(dateValue);
							} catch (ParseException e) {
								e.printStackTrace();
							}
							if(valuevalue.equals("null")){
								//System.out.println("String null in value: ");
								db.deleteValue(Integer.toString(v.getIdvalue()));
							}else{
								double dbValueDouble = 0;
								try{
									dbValueDouble = Double.parseDouble(valuevalue);
								}catch(NumberFormatException ex){
									ex.printStackTrace();
									db.deleteValue(Integer.toString(v.getIdvalue()));
								}
								
								if(fileDate.equals(dbDate)){
									if(dbValueDouble != fileDoubleValue){
										//System.out.println("DB VLAUE : "+dbValueDouble +"    FILE Value:"+fileDoubleValue);
										if(fileDoubleValue > 0){
											ups ++;
											
											/*treatement special for hba1c*/
											if(v.getCode().equals("hba1c")){
												// check id file value is < 1
												if(fileDoubleValue > 1){
													fileDoubleValue = fileDoubleValue * 0.01;
												}
												db.editValue(v.getCode(), Double.toString(fileDoubleValue), v.getDate(), Integer.toString(pat.getIdpatient()), Integer.toString(v.getIdvalue()));
											}else{
												db.editValue(v.getCode(), Double.toString(fileDoubleValue), v.getDate(), Integer.toString(pat.getIdpatient()), Integer.toString(v.getIdvalue()));
											}
										}
									}else{
										//System.out.println("SAME VALUE ");
										/*treatement special for hba1c*/
										if(v.getCode().equals("hba1c")){
											if(dbValueDouble > 1){
												dbValueDouble = dbValueDouble * 0.01;
												db.editValue(v.getCode(), Double.toString(dbValueDouble), v.getDate(), Integer.toString(pat.getIdpatient()), Integer.toString(v.getIdvalue()));
											}
										}
									}
									inBD = true;
								}
							}
						}
					}//end for
				}
				
				if(!inBD){
					if(fileDoubleValue > 0){
						ins++;
						if(vars[x].toLowerCase().equals("hba1c")){
							if(fileDoubleValue > 1){
								fileDoubleValue = fileDoubleValue * 0.01;
							}
						}
						db.addValue(vars[x].toLowerCase(), Double.toString(fileDoubleValue), dbFormat.format(fileDate), Integer.toString(pat.getIdpatient()));
					}
				}
			}
		}
		result = ups+":"+ins;
		
		return result;
	}

	
	public String importData(String place){
		Gson json = new Gson();
		String result = place.toUpperCase() + "\n";
		CdisDBridge db = new CdisDBridge();
		//download file 
		Date today = new Date();
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat fileFormat = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = DATE_FORMAT.format(today);
		InitialContext ic;
		
		String[] lipidVars = {"Tchol","Tglycer","Hdl","Ldl","Tchdl"};
		String[] labVars = {"Hba1c","Acglu","Ogtt"};
		String[] renalVars = {"Acratio","Prote","Crcl","Crea","Egfr","Pcr","Pcrg"};
		File logFile = null;
		String rf = "";
		FileWriter fw = null;
		try {
			ic = new InitialContext();
			rf = (String) ic.lookup("root-folder");
			logFile = new File(rf+System.getProperty("file.separator")+"files"+System.getProperty("file.separator")+"log-"+place+"_"+dateStr+".log");
			fw = new FileWriter(logFile,true);
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			File importFile = new File(rf+System.getProperty("file.separator")+"files"+System.getProperty("file.separator")+"import-"+place+"_"+dateStr+".csv");
			if(FtpTool.getFile(importFile.getAbsolutePath(), place)){
				fw.write("File download from "+place+" - SUCCES\n");
				FileInputStream fstream = new FileInputStream(importFile);
				BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
				Patient pat = new Patient();
				Renal ren = null;
				Lab lab = null;
				Lipid lip = null;
				String strLine;
				
				//Read File Line By Line
				int index = 2;
				List<String> header = new ArrayList<String>();
				
				ArrayList<String> ramqs = new ArrayList<>();
				HashMap<String, ArrayList<Hashtable<String, String>>> data = new HashMap<>();
				int nolines = 0;
				
				while ((strLine = br.readLine()) != null)   {
					strLine = strLine.replaceAll("\\\"", "");
					strLine = strLine.replaceAll("\\\'", "");
					strLine = strLine.replaceAll("\\(", "");
					strLine = strLine.replaceAll("\\)", "");
					strLine = strLine.replaceAll(" ", "");
					strLine = strLine.toLowerCase();
					
					//System.out.println(strLine);
					
					Hashtable<String, String> dataLine	= new Hashtable<>();
					List<String> lineList = new ArrayList<>();
					
					if(strLine.indexOf("ramq") >= 0){
						//first line
						header = Arrays.asList(strLine.split(","));
						
					}else{
						
						lineList = Arrays.asList(strLine.split(","));
						
						for(int i=0;i<lineList.size();i++){
							dataLine.put(header.get(i).trim(), lineList.get(i).trim());
						}
						
						String ramq = dataLine.get("ramq");
						ArrayList<Hashtable<String, String>> l = new ArrayList<>();
						if(!ramqs.contains(ramq)){
							ramqs.add(ramq);
						}else{
							l = data.get(ramq);
						}
						
						l.add(dataLine);
						data.put(ramq, l);					
						index++;
					}
					nolines ++;
				}
				
				//Close the input stream
				br.close();
				result += "File lines : "+nolines+".\n";
				String lastramq = "";
				result += "Total activity for this file : \n";
				int inserts = 0;
				int updates = 0;
				for(int j=0;j<ramqs.size();j++){
					String ramqStr = ramqs.get(j);
					fw.write("Patient:"+ramqStr+" - ");
					ArrayList<Hashtable<String, String>> dls = data.get(ramqStr);
					int pinsLab=0 , pinsLipid=0, pinsRenal=0, pupsLab=0, pupsLipid=0, pupsRenal = 0;
					boolean inCDIS = true;
					for(int jj=0;jj<dls.size();jj++){
						Hashtable<String, String> dl = dls.get(jj);
						if(!ramqStr.equals(lastramq)){
							//System.out.println("TREATING RAMQ: "+ramqStr);
							pat = db.getPatientByRamq(ramqStr);
							ren = (Renal)db.getAllValues("Renal","REN", pat.getIdpatient(), "");
							lab = (Lab)db.getAllValues("Lab","LAB", pat.getIdpatient(), "");
							lip = (Lipid)db.getAllValues("Lipid","LIP", pat.getIdpatient(), "");
							lastramq = ramqStr;
						}
						if(pat.getIdpatient() != 0){
							
							String labUpdate = updateData(lab, labVars, dl, pat);
							int isLab = parseCount("insert", labUpdate);
							int usLab = parseCount("update", labUpdate);
							inserts += isLab;
							updates += usLab;
							pinsLab += isLab;
							pupsLab += usLab;
							
							String lipidUpdate = updateData(lip, lipidVars, dl, pat);
							int isLipid = parseCount("insert", lipidUpdate);
							int usLipid = parseCount("update", lipidUpdate);
							inserts += isLipid;
							updates += usLipid;
							pinsLipid += isLipid;
							pupsLipid += usLipid;
							
							
							String renalUpdate = updateData(ren, renalVars, dl, pat);
							int isRenal = parseCount("insert", renalUpdate);
							int usRenal = parseCount("update", renalUpdate);
							inserts += isRenal;
							updates += usRenal;
							pinsRenal += isRenal;
							pupsRenal += usRenal;
							
							
						}else{
							inCDIS = false;
						}	
					}
					if(inCDIS){
						fw.write("LAB="+pinsLab+":"+pupsLab+" - LIPID="+pinsLipid+":"+pupsLipid+" - RENAL="+pinsRenal+":"+pupsRenal+ " - DONE\n");
					}else{
						fw.write(" NOT IN CDIS\n");
					}
				}
				result += "Total new values added from "+place.toUpperCase()+ ": "+inserts+"\n";
				result += "Total values modified from "+place.toUpperCase()+ ": "+updates+"\n";
				result += place.toUpperCase() + " - File imported\n";
			}
			
			
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
			
				
		return result;
	}
	
	
	
	public int parseCount(String variable, String value){
		String[] parts = value.split(":");
		if(variable.endsWith("insert")){
			return Integer.parseInt(parts[1]);
		}else{
			return Integer.parseInt(parts[0]);
		}
	}
	
	
}
