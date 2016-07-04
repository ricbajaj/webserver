package test.com.adobe.http;

import static org.junit.Assert.assertNotEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;

import com.adobe.server.WebServer;

/**
 * @author rbajaj
 * Defines some unit tests
 */
public class WebServerTest extends TestCase {		
	
		Logger log = Logger.getLogger(this.getClass().getSimpleName());	
	    private static String host = "127.0.0.1";
	    private static int port = 50001;
	    private static boolean running = false;
	    private static String workingDir = null;
	    
	    /**
	     * Creates the test case.
	     *
	     * @param testName name of the test case.
	     */
	    public WebServerTest(String testName) {
	        super(testName);
	    }

	    /**
	     * Builds test suite dynamically by fetching test methods.
	     * 
	     * @return the suite of tests being tested.
	     */
	    public static Test suite() {
	        return new TestSuite(WebServerTest.class);
	    }
	    
	    /**
	     * Starts an instance of WebServer for integration tests.
	     */
	    public void setUp() {
	        if (!running) {
	            log.info("Starting WebServer test instance on port: " + port);
	            workingDir = System.getProperty("user.dir");
	            log.info("Working directory: " + System.getProperty("user.dir"));
	            log.info("Test WebServer Logs: " + System.getProperty("user.dir") + "/logs/");
	            WebServer ws = new WebServer(port, workingDir, 10);
	            WebServer.initializeDefaultContent();
	            new Thread(ws).start();
	            running = true;
	        }
	    }

	    /**
	     * Opens an http connection and returns the response message
	     *
	     * @param method
	     * @param url the test url.
	     * @param isKeepAlive
	     * @return the http response message
	     */
	    public static HttpURLConnection sendRequest(String method, String url, boolean isKeepAlive) {
	    	HttpURLConnection connection = null;
	        try {
	            URL testURL = new URL(url);
	            connection = (HttpURLConnection) testURL.openConnection();
	            //set request method
	            connection.setRequestMethod(method);
	            if(!isKeepAlive)
	            {
	            	connection.setRequestProperty("Connection", "close");
	            }
	        } catch (MalformedURLException ex) {
	            ex.printStackTrace();
	        } catch (IOException ex) {
	            ex.printStackTrace();
	        }
	        return connection;
	    }
	    
	    public static HttpURLConnection sendRequest(String method, String url) {
	    	return sendRequest(method, url, true);
	    }

	    /**
	     * WebServer test - Verify that an http connection can be established.
	     * All tests requires a running WebServer instance.
	     */
	    public void testOpenConnection() {
	    	log.info("Test Open Connection");
	        HttpURLConnection conn = sendRequest("GET", "http://" + host + ":" + port);
	    	
	        String response = "";
	        try
	        {
	        	response = conn.getResponseMessage();
	        }catch(Exception e)
	        {
	        	log.error("Exception in testOpenConnection "+e);
	        }
	        
	        //Verify response
	        assertEquals("OK", response);
	        
	        conn.disconnect();
	    }
	    
	    /**
	     * Web Server test - Verify GET success
	     * Test requires a running WebServer instance 
	     */
	    public void testGETMethod() {
	    	log.info("Test GET Method, Response 200");
	    	HttpURLConnection conn = sendRequest("GET", "http://" + host + ":" + port + "/index.html");
	    	
	        String response = "";
	        int code = -1;
	        
	        try
	        {
	        	response = conn.getResponseMessage();
	        	code = conn.getResponseCode();
	        }catch(Exception e)
	        {
	        	log.error("Exception in testGETMethod "+e);
	        }
	        
	        //Verify response code
	        assertEquals("OK", response);
	        assertEquals(200, code);
	        
	        //Verify headers
		    assertEquals("Java HTTP Server 1.1", conn.getHeaderField("Server"));
		    assertEquals("GET, HEAD", conn.getHeaderField("Allow"));	    
		    
		    //Verify response body
		    String responseBody = getResponseBody(conn);
			
			assertNotEquals("", responseBody.toString());
		    
		    conn.disconnect();
	    	
	    }

	    /**
	     * WebServer test - Verify 404 response when requested resource doesn't exist.
	     * Test requires a running WebServer instance
	     */
	    public void testGETNotFound() {
	    	log.info("Test GET Method, Response 404");
	    	HttpURLConnection conn = sendRequest("GET", "http://" + host + ":" + port + "/nosuchfile.html");
	    
	        String response = "";
	        int code = -1;
	        
	        try
	        {
	        	response = conn.getResponseMessage();
	        	code = conn.getResponseCode();
	        	
	        }catch(Exception e)
	        {
	        	log.error("Exception in testGETNotFound "+e);
	        }
	        
	        //Verify response code
	        assertEquals("Not Found", response);
	        assertEquals(404, code);
	        
	        //Verify headers
		    assertEquals("Java HTTP Server 1.1", conn.getHeaderField("Server"));
		    assertEquals("GET, HEAD", conn.getHeaderField("Allow"));
		    assertEquals("text/html;charset=UTF-8", conn.getHeaderField("Content-Type"));
		    
		    conn.disconnect();
	    }
	    
	    /**
	     * WebServer test - Verify HEAD success
	     * Test requires a running WebServer instance
	     */
	    public void testHEADMethod() {
	    	log.info("Test HEAD Method, Response 200");
	    	HttpURLConnection conn = sendRequest("HEAD", "http://" + host + ":" + port + "/index.html");
	    	
	        String response = "";
	        int code = -1;
	        
	        try
	        {
	        	response = conn.getResponseMessage();
	        	code = conn.getResponseCode();
	        }catch(Exception e)
	        {
	        	log.error("Exception in testHEADMethod "+e);
	        }
	        
	        //Verify response code
	        assertEquals("OK", response);
	        assertEquals(200, code);
	        
	        //Verify headers
		    assertEquals("Java HTTP Server 1.1", conn.getHeaderField("Server"));
		    assertEquals("GET, HEAD", conn.getHeaderField("Allow"));
		    
		    //Verify response body
		    String responseBody = getResponseBody(conn);
			
			assertEquals("", responseBody.toString());
		    
		    conn.disconnect();
	    	
	    }
	    
	    /**
	     * WebServer test - Verify POST fails
	     * Test requires a running WebServer instance
	     */
	    public void testPOSTMethod() {
	    	log.info("Test POST Method, Response 501");
	    	HttpURLConnection conn = sendRequest("POST", "http://" + host + ":" + port + "/index.html");
	    	
	        String response = "";
	        int code = -1;
	        
	        try
	        {
	        	response = conn.getResponseMessage();
	        	code = conn.getResponseCode();
	        }catch(Exception e)
	        {
	        	log.error("Exception in testPOSTMethod "+e);
	        }
	        
	        //Verify response code
	        assertEquals("Not Implemented", response);
	        assertEquals(501, code);
	        
	        //Verify headers
		    assertEquals("Java HTTP Server 1.1", conn.getHeaderField("Server"));
		    assertEquals("GET, HEAD", conn.getHeaderField("Allow"));
		    assertEquals("text/html;charset=UTF-8", conn.getHeaderField("Content-Type"));
	    	
		    conn.disconnect();
	    }
	   
	    
	    /**
	     * Web Server test - Verify GET success and connection closes when keep alive is disabled
	     * Test requires a running WebServer instance
	     */
	    public void testKeepAliveDisabled() {
	    	log.info("Test GET Method, Keep Alive Disabled");
	    	HttpURLConnection conn = sendRequest("GET", "http://" + host + ":" + port + "/index.html", false);
	    	
	        String response = "";
	        int code = -1;
	        
	        try
	        {
	        	response = conn.getResponseMessage();
	        	code = conn.getResponseCode();
	        }catch(Exception e)
	        {
	        	log.error("Exception in testKeepAliveDisabled "+e);
	        }
	        
	        //Verify response code
	        assertEquals("OK", response);
	        assertEquals(200, code);
	        
	        //Verify headers
		    assertEquals("Java HTTP Server 1.1", conn.getHeaderField("Server"));
		    assertEquals("GET, HEAD", conn.getHeaderField("Allow"));
		    assertEquals("close", conn.getHeaderField("Connection"));
		    
		    //Verify response body
		    String responseBody = getResponseBody(conn);
			
			assertNotEquals("", responseBody.toString());
		    
		    conn.disconnect();
	    	
	    }
	    
	    /**
	     * WebServer test - Verify GET success with keep alive on two separate requests
	     * Test requires a running WebServer instance
	     */
	    public void testKeepAliveEnabled() {
	    	log.info("Test GET Method, Keep Alive with 2 requests");
	    	HttpURLConnection conn = sendRequest("GET", "http://" + host + ":" + port + "/index.html");
	    	
	        String response = "";
	        int code = -1;
	        
	        try
	        {
	        	response = conn.getResponseMessage();
	        	code = conn.getResponseCode();
	        }catch(Exception e)
	        {
	        	log.error("Exception in testKeepAliveEnabled "+e);
	        }
	        
	        //Verify response code
	        assertEquals("OK", response);
	        assertEquals(200, code);
	        
	        //Verify headers
		    assertEquals("Java HTTP Server 1.1", conn.getHeaderField("Server"));
		    assertEquals("GET, HEAD", conn.getHeaderField("Allow"));
		    assertNotEquals("close", conn.getHeaderField("Connection"));
		    
		    //Verify response body
		    String responseBody = getResponseBody(conn);
			
			assertNotEquals("", responseBody.toString());
		    
		    conn.disconnect();
		    
		    conn = sendRequest("GET", "http://" + host + ":" + port + "/index2.html");
	        
	        try
	        {
	        	response = conn.getResponseMessage();
	        	code = conn.getResponseCode();
	        }catch(Exception e)
	        {
	        	log.error("Exception in testKeepAliveEnabled "+e);
	        }
	        
	        //Verify response code
	        assertEquals("OK", response);
	        assertEquals(200, code);
	        
	        //Verify headers
		    assertEquals("Java HTTP Server 1.1", conn.getHeaderField("Server"));
		    assertEquals("GET, HEAD", conn.getHeaderField("Allow"));
		    assertNotEquals("close", conn.getHeaderField("Connection"));
		    
		    //Verify response body
		    responseBody = getResponseBody(conn);
			
			assertNotEquals("", responseBody.toString());
		    
		    conn.disconnect();
	    	
	    }
	   	    
	    /**
	     * Extracts the response body from an open url connection
	     * 
	     * @param conn the connection
	     * @return
	     */
	    private String getResponseBody(HttpURLConnection conn)
	    {
	    	BufferedReader in = null;
		    
		    StringBuffer responseBody = new StringBuffer();
		    
		    try
		    {
		    	in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String inputLine;
				responseBody = new StringBuffer();
		 
				while ((inputLine = in.readLine()) != null) {
					responseBody.append(inputLine);
				}
		    }catch(Exception e)
		    {
		    	log.error("Exception in getResponseBody "+e);
		    }
		    finally
		    {
		    	if(in != null)
		    	{
		    		try
		    		{
		    			in.close();
		    		}catch(Exception e)
		    		{
		    			log.error("Exception while closing buffered reader "+e);
		    		}
		    	}
		    }
		    
		    return responseBody.toString();
	    }
}
