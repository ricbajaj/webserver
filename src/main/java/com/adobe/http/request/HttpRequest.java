package com.adobe.http.request;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.HashMap;

import com.adobe.http.response.HttpResponse;
import com.adobe.server.WebServer;

import org.apache.log4j.Logger;

/**
 * @author rbajaj
 * Defines Methods and data associated with HTTP request. 
 */
public class HttpRequest {
	
	static Logger log = Logger.getLogger(HttpRequest.class.toString());
	public static BufferedReader inputReader = null;
	protected String url;
	protected String method;
	protected File file;
	protected HashMap<String, String> headers;
	public boolean keepAlive;
	
	private static final String GET = "GET";
	private static final String HEAD = "HEAD";
	public static final String CONNECTION_CLOSE = "close";
	
	/**
	 * @param in
	 * @param url - request url
	 * @param method - request method
	 * @param headers 
	 */
	public HttpRequest(BufferedReader in, String url, String method, HashMap<String, String> headers)
	{
		this.url = url;
		this.method = method;
		inputReader = in;
		
		//as per HTTP 1.1 spec, keep connection alive
		this.keepAlive = true;
		
		if(CONNECTION_CLOSE.equals(headers.get("Connection")))
		{
			this.keepAlive = false;
		}	
	}
	
	/**
	 * @param response
	 * @throws FileNotFoundException
	 * Initialization work prior to sending a response (common to all requests types)
	 */
	protected void initResponse(HttpResponse response) throws FileNotFoundException
	{
		//If we get to here, request method is GET or HEAD
    	url = preparePath(url);
    	
    	//create file object
    	file = new File(WebServer.SERVERROOT, url);
    	
    	//if file doesn't exist, we want to abort before sending headers
    	FileInputStream fis = new FileInputStream(file);
    	
		try {
			fis.close();
		} catch (IOException e) {
			log.error("File not exist: "+e.getMessage());
		}
	}
	
	/**
	 * @param path
	 * @return
	 * Prepare request file path
	 */
	private String preparePath(String path) {
        if (path.equals("/") || path == null || path.contains("..")) {
            path = WebServer.DEFAULT_FILE;
        }
        path.replace('/', File.separator.charAt(0));
        log.info("Requested File Path: "+path);
        return path;
    }
	
	/**
	 * @param inStream
	 * @return
	 * @throws IOException
	 * @throws SocketTimeoutException
	 */
	public static HttpRequest parseRequest(InputStream inStream) throws IOException, SocketTimeoutException
	{
		log.info("Parsing Request");
		BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
		String line = reader.readLine();
    	
		log.info("Request: " + line);
		
		//Verify protocol of the request
		String[] req = line.split(" ", 3);
		String protocol = req[2];
		if (line == null || !protocol.startsWith("HTTP/")) {
			throw new IOException("Server accepts only HTTP requests.");
		}
		      
		//Get method and requested file url
       	String method = req[0];
       	String url = req[1].toLowerCase();
       
       	HashMap<String, String> headers = new HashMap<String, String>();
    	
       	line = reader.readLine();
       	while(line != null && !line.trim().isEmpty()) 
    	{
    		String[] header = line.split(":" , 2);
    		if(header.length!=2){
    			log.info("No headers added");
    			throw new IOException("Cannot parse header from \"" + line + "\"");
    		}
			else {
					headers.put(header[0].trim(), header[1].trim());
			}
    		line = reader.readLine();
    	}
    	
    	return new HttpRequest(reader, url, method, headers);
	}
	
	
	/**
	 * Send response depending upon the request method
	 * @param response
	 */
	public void sendResponse(HttpResponse response) {
		
			try
			{
				initResponse(response);
				log.info("Request Method: "+method);
				//response constructed depending upon the method in request
				if(GET.equals(method))
		    	{					
					//GET Method
					response.sendHeaders(file);
					response.sendBody(file);
		    	} else if(HEAD.equals(method)){
		    		//HEAD method
		    		response.sendHeaders(file);
		    	} else{
		    		//Not Implemented Method
		    		response.sendNotImplementedMethod(method);
		    	}
				
			}catch (FileNotFoundException fe)
		    {
				//File Not Found Response
				log.info("File Not Found.");
		    	response.fileNotFound(url);
		    }	
	}
	
	/**
	 * Close http request reader
	 */
	public void close()
	{
		try
		{
			if(inputReader != null)
			{
				inputReader.close();
			}
			
		}catch(Exception e)
		{
			log.error("Error closing http request stream: " + e);
		}
	}

}
