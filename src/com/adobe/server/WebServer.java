package com.adobe.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.adobe.connection.HttpConnection;
import com.adobe.logutils.MyLogger;

/**
 * @author rbajaj
 *
 * Defines web server and supporting methods
 * This implementation relies on : 
 * www.sourcestream.com/programming-stuff/java-http-server
 */
public class WebServer implements Runnable {
	
	Logger log = MyLogger.getLogger(this.getClass().getSimpleName());

	public static String SERVERROOT = null;
	public static final String DEFAULT_FILE = "index.html";
	public static final String DEFAULT_FILE2 = "index2.html";
	private ServerSocket serverSocket;
	private ExecutorService threadPool;

	private final int port;
	private final int threadThreshold;
	
	public WebServer(int port, String webRoot, int maxThreads) {
		this.port = port;
		this.threadThreshold = maxThreads;
		SERVERROOT = webRoot;
		log.info("Running server on the port "+port+" with web root folder \""+ webRoot + "\"");
	}
	
	public static void main(String[] args) {
		int port = 8080;
		String webRoot = "root";
		int maxThreads = 10;
				
		if (args.length == 0 || args[0].equals("-h") || args[0].equals("-help")){
			System.out.println("Usage: java -cp ThreadPoolWebServer <port> <web root> <threads limit>\n");
			}
		else {
			port = Integer.parseInt(args[0]);
			webRoot = args[1];
			maxThreads = Integer.parseInt(args[2]);
		}
		if (isAvailablePort(port)) {
			new Thread(new WebServer(port, webRoot, maxThreads)).start();
		}
		
	}
	
	/**
	 * Verifies that a port on the system is available and not already in use
	 * @param port
	 * @return
	 */
	public static boolean isAvailablePort(int port) {
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(port);

			// no exception means port is available
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					throw new RuntimeException("Fatal error.", e);
				}
			}
		}
	}

	public void run() 
	{
		try
		{
			threadPool = Executors.newCachedThreadPool();
			serverSocket = new ServerSocket(port);
	    	log.info("Web Server listening for connections on port " + port);
   
	    	//listen unless interrupted
	    	while (!Thread.interrupted()) {
				try {
					HttpConnection conn = new HttpConnection(serverSocket.accept(), this);
					threadPool.submit(new Thread(conn));
				} catch (IOException ioe) {
					log.error("Cannot submit another connection thread. IO Exception: "+ioe.getMessage());
				} catch (Exception e){
					log.error("Exception while submitting new Thread in Thread pool: "+e);
				}
			}
		}catch (IOException e) {
			log.error("Cannot listen on port " + port);
			log.error("Server Error: "+e.getMessage());
			System.exit(1);
		}
		
		try
		{
			if(serverSocket != null)
			{
				serverSocket.close();
			}
		}catch(IOException e1)
		{
			log.error("Exception while closing server socket: " + e1);
		}
		
		threadPool.shutdown();	
		try {
			if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) 
				threadPool.shutdownNow();
		} catch (InterruptedException e) {
			log.error("Interrupted Exception while thread pool shutdown: "+e);
		}
		
	}
}
