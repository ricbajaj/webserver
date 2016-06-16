/*package com.adobe.http.request;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.util.HashMap;

import com.adobe.http.response.HttpResponse;

*//**
 * @author rbajaj
 *
 *//*
public class HttpGetRequest extends HttpRequest 
{
	public HttpGetRequest(BufferedReader in, String fileRequested, String method, HashMap<String, String> headers) 
	{
		super(in, fileRequested, method, headers);	
	}

	@Override
	public void sendResponse(HttpResponse response)  {
		try
		{
			initResponse(response);
			
			response.sendHeaders(file);
			response.sendBody(file);
			
		}catch (FileNotFoundException fe)
	    {
	    	response.fileNotFound(url);
	    }	
		
	}

}

*/