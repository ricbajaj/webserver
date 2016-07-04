package com.adobe.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.log4j.Logger;

import com.adobe.http.request.HttpRequest;
import com.adobe.http.response.HttpResponse;
import com.adobe.server.WebServer;

/**
 * @author rbajaj
 * Defines the connection handler for Web Server
 */
public class HttpConnection implements Runnable {
	
	Logger log = Logger.getLogger(this.getClass().getSimpleName());
	
	private Socket socket = null;
	private WebServer webServer;
	private static final int SOCKET_TIMEOUT = 5000;
	private HttpRequest httpRequest = null;
	private HttpResponse httpResponse = null;
	private InputStream inStream = null;
	private OutputStream outStream = null;
	
	public HttpConnection(Socket socket, WebServer ws){
		try{
			this.socket=socket;
			this.webServer=ws;
		
			socket.setSoTimeout(SOCKET_TIMEOUT);
			
		}catch(SocketException se){
			log.error("Socket Exception: "+se);
		}
		
	}

	public void run()
	{ 
		try{
			inStream = socket.getInputStream();
			outStream = socket.getOutputStream();
			//indefinite while loop to add keep alive behavior
		    	while(true)
		    	{
		    		//parse request
		    		httpRequest = HttpRequest.parseRequest(inStream);
		    		//keep alive boolean
		    		boolean isKeepAlive = httpRequest.keepAlive;
		    		//send response
		    		httpResponse = new HttpResponse(outStream, isKeepAlive);
		    		httpRequest.sendResponse(httpResponse);
		    		if(!isKeepAlive)
		    		{
		    			// When request contains "Connection: close" in the header
		    			break;
		    		}
		    	}
		    	inStream.close();
		    	outStream.close();
		    }
		    catch(SocketTimeoutException ste)
		    {
		    	log.info("Socket timeout. No more requests to cater. Closing connection.");
		    }
		    catch (IOException ioe)
		    {
		    	log.error("Server IO Exception: " + ioe);
		    }
		    finally
		    {
		    	close();
		    	log.info("Connection closed.\n");
		    }
	}
	
	/**
	 * Close all streams and sockets
	 */
	private void close()
	{
		try
	    {
			if(inStream!=null)
			{
				inStream.close();
			}
			if(outStream!=null)
			{
				outStream.close();
			}
			if(httpRequest != null)
			{
				httpRequest.close();
			}
			if(httpResponse != null)
			{
				httpResponse.close();
			}
			if(socket != null)
			{
				socket.close();
			}
	    }
	    catch (Exception e)
	    {
	    	log.error("Error closing connection stream: " + e);
	    }
	}

}
