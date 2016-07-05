package com.adobe.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.adobe.logutils.MyLogger;

import org.apache.log4j.Logger;

import com.adobe.connection.HttpConnection;

/**
 * @author rbajaj
 *
 * Defines web server and supporting methods
 * This implementation relies on : 
 * www.sourcestream.com/programming-stuff/java-http-server
 */
public class WebServer implements Runnable {
	
	static Logger log = null;

	public static String SERVERROOT = null;
	public static final String DEFAULT_FILE = "index.html";
	public static final String DEFAULT_FILE2 = "index2.html";
	public static final String DEFAULT_404 = "404.html"; 
	private ServerSocket serverSocket;
	private ExecutorService threadPool;

	private final int port;
	private final int threadThreshold;
	
	/**
	 * WebServer constructor
	 * @param port
	 * @param webRoot
	 * @param maxThreads
	 */
	public WebServer(int port, String webRoot, int maxThreads) {
		log = MyLogger.getLogger(WebServer.class.getSimpleName());
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
			log.info("Port "+port+" already in use");
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
			//threadPool = Executors.newCachedThreadPool();
			threadPool = Executors.newFixedThreadPool(threadThreshold);
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
				//close server socket
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
	
	 /**
	 * Initializes the default content for tests
	 */
	public static void initializeDefaultContent() {
	        File root = new File(SERVERROOT);
	        if (!root.exists()) {
	            root.mkdir();
	            log.info("Initialized default webroot directory: " + root.getAbsolutePath());
	        }
	        initializeFile(SERVERROOT + File.separator + DEFAULT_FILE, "<!DOCTYPE html><html><head><title>WebServer</title></head><body><h1>Web Server Response!!!</h1></body></html>");
	        initializeFile(SERVERROOT + File.separator + DEFAULT_FILE2, "<!DOCTYPE html><html><head><title>WebServer</title></head><body><h1>Web Server Response 2!!!</h1></body></html>");
	        initializeFile(SERVERROOT + File.separator + DEFAULT_404, "<!DOCTYPE html><html><head><title>404</title></head><body>404 - Page not found</body></html>");
	    }

	    /**
	     * Initializes file if it doesn't exist.
	     *
	     * @param path    the file path.
	     * @param content the file content.
	     */
	    private static void initializeFile(String path, String content) {
	        File file = new File(path);
	        if (!file.exists()) {
	            try {
	                if (content != null) {
	                    FileWriter writer = new FileWriter(file);
	                    writer.write(content);
	                    writer.close();
	                } else {
	                    file.createNewFile();
	                }
	                log.info("Initialized default content: " + path);
	            } catch (IOException ex) {
	                log.error("Exception occured while initializing default file: " + path, ex);
	            }
	        }
	    }
}
