package com.adobe.logutils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * @author rbajaj
 *
 */
public class MyLogger  
{
  public static Logger getLogger(String logFile)
  {
	  Logger log= null;
	  try{
		  Properties logProperties = new Properties();
	      // load our log4j properties / configuration file
		  InputStream is = MyLogger.class.getClassLoader().getResourceAsStream("log4j.properties");
		  //new File(resource.toURI());
	      logProperties.load(is);
	      System.setProperty("file.name",logFile+".log");
	      PropertyConfigurator.configure(logProperties);
	      log = Logger.getLogger(logFile);
	      
	  }catch(FileNotFoundException fe){
		  
	  }catch(IOException ie){
		  
	  }
	  return log;
	  
  }
  
  
  public static String getStackTraceAsString(Exception e){
	    
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      return sw.toString();
	    
  }
  
}
