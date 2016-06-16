package com.adobe.http.response;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;

import org.apache.log4j.Logger;

import com.adobe.http.request.HttpRequest;

/**
 * @author rbajaj
 *
 */
public class HttpResponse {
	static Logger log = Logger.getLogger(HttpResponse.class.toString());
	public static final String OK = "200 OK";
	public static final String NOT_FOUND = "404 Not Found";
	public static final String NOT_IMPLEMENTED = "501 Not Implemented";

	private static final String protocol = "HTTP/1.1";
	private PrintWriter out = null;
	private BufferedOutputStream outStream = null;
	private String contentType;
	private boolean keepAlive;
	private byte[] body = null;
	
	public HttpResponse(OutputStream os, boolean keepAlive) {
		out = new PrintWriter(os);
		outStream = new BufferedOutputStream(os);

		this.keepAlive = keepAlive;
	}

	/**
	 * getContentType returns the proper MIME content type according to the
	 * requested file's extension.
	 *
	 * @param fname
	 *            File requested by client
	 */
	private String getContentType(String fname) {
		if (fname.endsWith(".htm") || fname.endsWith(".html")) {
			return "text/html";
		} else if (fname.endsWith(".gif")) {
			return "image/gif";
		} else if (fname.endsWith(".jpg") || fname.endsWith(".jpeg")) {
			return "image/jpeg";
		} else if (fname.endsWith(".class") || fname.endsWith(".jar")) {
			return "applicaton/octet-stream";
		} else {
			return "text/plain";
		}
	}

	/**
	 * Send the headers back to the client
	 * 
	 * @param file
	 */
	public void sendHeaders(File file) {
		contentType = getContentType(file.getName());
		log.info("Content Type: " + contentType);

		// send HTTP headers
		//out.println("HTTP/1.1 200 OK");
		out.println(protocol + " " + OK);
		out.println("Server: Java HTTP Server 1.1");
		out.println("Date: " + new Date());
		out.println("Allow: GET, HEAD");
		if (!keepAlive) {
			out.println("Connection: " + HttpRequest.CONNECTION_CLOSE);
		}
		out.println("Content-Type: " + contentType);
		out.println("Content-Length: " + file.length());
		out.println();
		out.flush();

		log.info("File " + file.getName() + " of type " + contentType + " returned.");
	}

	/**
	 * Send the response body back to the client
	 * 
	 * @param file
	 *            file being requested
	 * @throws FileNotFoundException
	 */
	public void sendBody(File file) throws FileNotFoundException {
		FileInputStream fileIn = null;

		try {
			int fileLength = (int) file.length();
			// create byte array to store file data
			byte[] fileData = new byte[fileLength];

			// open input stream from file
			fileIn = new FileInputStream(file);
			// read file into byte array
			fileIn.read(fileData);

			outStream.write(fileData, 0, fileLength);
			outStream.flush(); // flush binary output stream buffer
		} catch (IOException ioe) {
			log.error("Server Error: " + ioe);
		} finally {
			if (fileIn != null) {
				try {
					fileIn.close();
				} catch (Exception e) {
					log.error("Error closing file stream: " + e);
				}
			}
		}
	}

	/**
	 * Send the response headers and body back to the client upon receiving a
	 * request for an unimplemented method.
	 * 
	 * @param method
	 *            that was requested but not support by the server
	 */
	public void sendNotImplementedMethod(String method) {
		log.info("501 Not Implemented: " + method + " method.");

		// send Not Implemented message to client
		//out.println("HTTP/1.1 501 Not Implemented");
		out.println(protocol + " " + NOT_IMPLEMENTED);
		out.println("Server: Java HTTP Server 1.1");
		out.println("Date: " + new Date());
		out.println("Allow: GET, HEAD");
		if (!keepAlive) {
			out.println("Connection: " + HttpRequest.CONNECTION_CLOSE);
		}
		out.println("Content-Type: text/html;charset=UTF-8");
			
		//String msg = "Method " + method + " not implemented.";
		String msg ="<H2>501 Method " + method + " not implemented.</H2>";
		String length = Integer.toString(msg.length());
		out.println("Content-Length: " + length);
	    out.println();
		out.flush();

		log.info("501 Not Implemented: " + method + " method.");
		
		body = msg.getBytes();
		sendBody(body);
		
		log.info("Response 501 returned");
	}

	/**
	 * fileNotFound informs client that requested file does not exist.
	 *
	 * @param file
	 *            File requested by client
	 */
	
	public void fileNotFound(String file)
	{
		//send file not found HTTP headers
	    //out.println("HTTP/1.1 404 Not Found");
		out.println(protocol + " " + NOT_FOUND);
	    out.println("Server: Java HTTP Server 1.1");
	    out.println("Allow: GET, HEAD");
	    if(!keepAlive)
		{
			out.println("Connection: " + HttpRequest.CONNECTION_CLOSE);
		}
	    out.println("Date: " + new Date());
	    out.println("Content-Type: text/html;charset=UTF-8");
	    
	    //String msg = "File " + file + " not found.";
	    String msg ="<H2>404 File Not Found: " + file + "</H2>";
	    out.println("Content-Length: 100");
	    out.println();
		out.flush();

		log.info("404 File Not Found: " + file);
		
		body = msg.getBytes();
		sendBody(body);
	    
		log.info("Response 404 returned");
	}

	/**
	 * Sends the response body back to the client
	 * 
	 * @param body
	 */
	public void sendBody(byte[] body) {
		try {
			int bodyLength = (int) body.length;

			outStream.write(body, 0, bodyLength);
			outStream.flush(); // flush binary output stream buffer
		} catch (IOException ioe) {
			log.error("Server Error: " + ioe);
		}
	}

	/**
	 * close method closes the streams associated with an http response.
	 * 
	 */
	public void close() {
		try {
			if (outStream != null) {
				outStream.close();
			}
			if (out != null) {
				out.close();
			}
		} catch (Exception e) {
			log.error("Error closing http response stream: " + e);
		}
	}

}
