/*
 * Created on Dec 1, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.grv.cdis.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author radu
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FileFilterTool implements FilenameFilter {

	private String filter;
	
	public FileFilterTool() {
		super();
	}
	
	public FileFilterTool(String filter) {
		this.filter = filter;
	}
	
	
	
	public boolean accept(File arg0, String arg1) {
		if(this.filter == null)
			return (arg1.endsWith(".txt"));
		else
			return (arg1.endsWith(filter));
	}


}
